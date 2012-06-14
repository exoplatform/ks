/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIGroupSelector;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.UISelector;
import org.exoplatform.ks.common.webui.UIUserSelect;
import org.exoplatform.ks.common.webui.WebUIUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 9, 2008 - 8:19:24 AM  
 */
@ComponentConfigs ( 
  {
    @ComponentConfig(
      lifecycle = UIFormLifecycle.class,
      template = "app:/templates/forum/webui/popup/UIPrivateMessegeForm.gtmpl",
      events = {
        @EventConfig(listeners = UIPrivateMessageForm.AddValuesUserActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UIPrivateMessageForm.AddUserActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UIPrivateMessageForm.SelectTabActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UIPrivateMessageForm.CancelActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UIPrivateMessageForm.SendPrivateMessageActionListener.class)
      }
    ),
    @ComponentConfig(
       id = "UIPMUserPopupWindow",
       type = UIPopupWindow.class,
       template = "system:/groovy/webui/core/UIPopupWindow.gtmpl",
       events = {
         @EventConfig(listeners = UIPrivateMessageForm.ClosePopupActionListener.class, name = "ClosePopup")  ,
         @EventConfig(listeners = UIPrivateMessageForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
         @EventConfig(listeners = UIPrivateMessageForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
      }
    )
  }
)
public class UIPrivateMessageForm extends BaseForumForm implements UIPopupComponent, UISelector {
  private String             userName;

  private int                id                        = 0;

  private boolean            fullMessage               = true;

  public static final String FIELD_SENDTO_TEXTAREA     = "SendTo";

  public static final String FIELD_MAILTITLE_INPUT     = "MailTitle";

  public static final String FIELD_MAILMESSAGE_INPUT   = "MailMessage";

  public static final String FIELD_SENDMESSAGE_TAB     = "MessageTab";

  public static final String FIELD_REPLY_LABEL         = "Reply";

  public static final String FIELD_FORWARD_LABEL       = "Forward";

  public static final String USER_SELECTOR_POPUPWINDOW = "UIPMUserPopupWindow";

  public UIPrivateMessageForm() throws Exception {
    UIFormTextAreaInput SendTo = new UIFormTextAreaInput(FIELD_SENDTO_TEXTAREA, FIELD_SENDTO_TEXTAREA, null);
    SendTo.addValidator(MandatoryValidator.class);
    UIFormStringInput MailTitle = new UIFormStringInput(FIELD_MAILTITLE_INPUT, FIELD_MAILTITLE_INPUT, null);
    MailTitle.addValidator(MandatoryValidator.class);
    UIFormWYSIWYGInput formWYSIWYGInput = new UIFormWYSIWYGInput(FIELD_MAILMESSAGE_INPUT, FIELD_MAILMESSAGE_INPUT, ForumUtils.EMPTY_STR);
    formWYSIWYGInput.addValidator(MandatoryValidator.class);
    formWYSIWYGInput.setToolBarName("Basic");
    formWYSIWYGInput.setFCKConfig(WebUIUtils.getFCKConfig());
    UIFormInputWithActions sendMessageTab = new UIFormInputWithActions(FIELD_SENDMESSAGE_TAB);
    sendMessageTab.addUIFormInput(SendTo);
    sendMessageTab.addUIFormInput(MailTitle);
    sendMessageTab.addUIFormInput(formWYSIWYGInput);

    String[] strings = new String[] { "SelectUser", "SelectMemberShip", "SelectGroup" };
    ActionData ad;
    int i = 0;
    List<ActionData> actions = new ArrayList<ActionData>();
    for (String string : strings) {
      ad = new ActionData();
      if (i == 0)
        ad.setActionListener("AddUser");
      else
        ad.setActionListener("AddValuesUser");
      ad.setActionParameter(String.valueOf(i));
      ad.setCssIconClass(string + "Icon");
      ad.setActionName(string);
      actions.add(ad);
      ++i;
    }
    sendMessageTab.setActionField(FIELD_SENDTO_TEXTAREA, actions);
    addUIFormInput(sendMessageTab);
    addChild(UIListInBoxPrivateMessage.class, null, null);
    addChild(UIListSentPrivateMessage.class, null, null);
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void setUserProfile(UserProfile userProfile) {
    this.userProfile = userProfile;
    this.userName = userProfile.getUserId();
  }

  public void setSendtoField(String str) {
    this.getUIFormTextAreaInput(FIELD_SENDTO_TEXTAREA).setValue(str);
  }

  public void updateSelect(String selectField, String value) throws Exception {
    UIFormTextAreaInput fieldInput = getUIFormTextAreaInput(selectField);
    String values = fieldInput.getValue();
    fieldInput.setValue(ForumUtils.updateMultiValues(value, values));
  }

  private String removeCurrentUser(String s) throws Exception {
    if (s.equals(userName))
      return ForumUtils.EMPTY_STR;
    if (s.contains(userName + ForumUtils.COMMA))
      s = StringUtils.remove(s, userName + ForumUtils.COMMA);
    if (s.contains(ForumUtils.COMMA + userName))
      s = StringUtils.remove(s, ForumUtils.COMMA + userName);
    return s;
  }

  protected int getIsSelected() {
    return this.id;
  }

  static public class SelectTabActionListener extends EventListener<UIPrivateMessageForm> {
    public void execute(Event<UIPrivateMessageForm> event) throws Exception {
      String id = event.getRequestContext().getRequestParameter(OBJECTID);
      UIPrivateMessageForm messageForm = event.getSource();
      messageForm.id = Integer.parseInt(id);
      event.getRequestContext().addUIComponentToUpdateByAjax(messageForm);
    }
  }

  static public class SendPrivateMessageActionListener extends EventListener<UIPrivateMessageForm> {
    public void execute(Event<UIPrivateMessageForm> event) throws Exception {
      UIPrivateMessageForm messageForm = event.getSource();
      UIFormInputWithActions MessageTab = messageForm.getChildById(FIELD_SENDMESSAGE_TAB);
      UIFormTextAreaInput areaInput = messageForm.getUIFormTextAreaInput(FIELD_SENDTO_TEXTAREA);
      String sendTo = areaInput.getValue();
      sendTo = ForumUtils.removeSpaceInString(sendTo);
      sendTo = ForumUtils.removeStringResemble(sendTo);
      sendTo = messageForm.removeCurrentUser(sendTo);
      if (ForumUtils.isEmpty(sendTo)) {
        messageForm.warning("UIPrivateMessageForm.msg.sendToCurrentUser", new String[] { messageForm.getLabel(FIELD_SENDTO_TEXTAREA) });
        return;
      }

      String erroUser = UserHelper.checkValueUser(sendTo);
      if (!ForumUtils.isEmpty(erroUser)) {
        messageForm.warning("NameValidator.msg.erroUser-input", new String[] { messageForm.getLabel(FIELD_SENDTO_TEXTAREA), erroUser });
        return;
      }
      UIFormStringInput stringInput = MessageTab.getUIStringInput(FIELD_MAILTITLE_INPUT);
      String mailTitle = stringInput.getValue();
      int maxText = 80;
      if (mailTitle.length() > maxText) {
        messageForm.warning("NameValidator.msg.warning-long-text", new String[] { messageForm.getLabel(FIELD_MAILTITLE_INPUT), String.valueOf(maxText) });
        return;
      }
      mailTitle = CommonUtils.encodeSpecialCharInTitle(mailTitle);
      UIFormWYSIWYGInput formWYSIWYGInput = MessageTab.getChild(UIFormWYSIWYGInput.class);
      String message = formWYSIWYGInput.getValue();
      if (!ForumUtils.isEmpty(message)) {
        ForumPrivateMessage privateMessage = new ForumPrivateMessage();
        privateMessage.setFrom(messageForm.userName);
        privateMessage.setSendTo(sendTo);
        privateMessage.setName(mailTitle);
        privateMessage.setMessage(message);
        try {
          messageForm.getForumService().savePrivateMessage(privateMessage);
        } catch (Exception e) {
          messageForm.log.warn("Failed to save private message", e);
        }
        areaInput.setValue(ForumUtils.EMPTY_STR);
        stringInput.setValue(ForumUtils.EMPTY_STR);
        formWYSIWYGInput.setValue(ForumUtils.EMPTY_STR);
        messageForm.info("UIPrivateMessageForm.msg.sent-successfully", false);
        if (messageForm.fullMessage) {
          messageForm.id = 1;
          event.getRequestContext().addUIComponentToUpdateByAjax(messageForm.getParent());
        } else {
          UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
          forumPortlet.cancelAction();
        }
      } else {
        messageForm.warning("NameValidator.msg.empty-input", new String[] { messageForm.getLabel(FIELD_MAILMESSAGE_INPUT) });
      }
    }
  }

  static public class AddValuesUserActionListener extends EventListener<UIPrivateMessageForm> {
    public void execute(Event<UIPrivateMessageForm> event) throws Exception {
      UIPrivateMessageForm messageForm = event.getSource();
      String type = event.getRequestContext().getRequestParameter(OBJECTID);
      if (!ForumUtils.isEmpty(type)) {
        UIPopupContainer popupContainer = messageForm.getAncestorOfType(UIPopupContainer.class);
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
        UIUserSelect uiUserSelect = popupContainer.findFirstComponentOfType(UIUserSelect.class);
        if (uiUserSelect != null) {
          UIPopupWindow popupWindow = uiUserSelect.getParent();
          closePopupWindow(popupWindow);
        }
        UIGroupSelector uiGroupSelector = popupAction.activate(UIGroupSelector.class, 600);
        if (type.equals(UIGroupSelector.TYPE_MEMBERSHIP))
          uiGroupSelector.setId("UIMemberShipSelector");
        else if (type.equals(UIGroupSelector.TYPE_GROUP))
          uiGroupSelector.setId("UIGroupSelector");
        uiGroupSelector.setType(type);
        uiGroupSelector.setSelectedGroups(null);
        uiGroupSelector.setComponent(messageForm, new String[] { FIELD_SENDTO_TEXTAREA });
        uiGroupSelector.getChild(UITree.class).setId(UIGroupSelector.TREE_GROUP_ID);
        uiGroupSelector.getChild(org.exoplatform.webui.core.UIBreadcumbs.class).setId(UIGroupSelector.BREADCUMB_GROUP_ID);
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
      }
    }
  }

  public void setUpdate(ForumPrivateMessage privateMessage, boolean isReply) throws Exception {
    UIFormInputWithActions MessageTab = this.getChildById(FIELD_SENDMESSAGE_TAB);
    UIFormStringInput stringInput = MessageTab.getUIStringInput(FIELD_MAILTITLE_INPUT);
    UIFormWYSIWYGInput message = MessageTab.getChild(UIFormWYSIWYGInput.class);
    String content = privateMessage.getMessage();
    String label = this.getLabel(FIELD_REPLY_LABEL);
    String title = CommonUtils.decodeSpecialCharToHTMLnumber(privateMessage.getName());
    if (title.indexOf(label) < 0) {
      title = new StringBuffer(label).append(": ").append(title).toString();
    }
    if (isReply) {
      UIFormTextAreaInput areaInput = this.getUIFormTextAreaInput(FIELD_SENDTO_TEXTAREA);
      areaInput.setValue(privateMessage.getFrom());
      stringInput.setValue(title);
      content = new StringBuffer("<br/><br/><br/><div style=\"padding: 5px; border-left:solid 2px blue;\">").append(content).append("</div>").toString();
    } else {
      label = this.getLabel(FIELD_FORWARD_LABEL);
      stringInput.setValue(title);
    }
    message.setValue(content);
    this.id = 2;
  }

  public boolean isFullMessage() {
    return fullMessage;
  }

  public void setFullMessage(boolean fullMessage) {
    this.fullMessage = fullMessage;
  }

  static public class CancelActionListener extends EventListener<UIPrivateMessageForm> {
    public void execute(Event<UIPrivateMessageForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }

  static public class CloseActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupWindow popupWindow = uiUserSelector.getParent();
      closePopupWindow(popupWindow);
    }
  }

  static public class ClosePopupActionListener extends EventListener<UIPopupWindow> {
    public void execute(Event<UIPopupWindow> event) throws Exception {
      UIPopupWindow popupWindow = event.getSource();
      closePopupWindow(popupWindow);
    }
  }

  static public class AddActionListener extends EventListener<UIUserSelect> {
    public void execute(Event<UIUserSelect> event) throws Exception {
      UIUserSelect uiUserSelector = event.getSource();
      String values = uiUserSelector.getSelectedUsers();
      UIForumPortlet forumPortlet = uiUserSelector.getAncestorOfType(UIForumPortlet.class);
      UIPrivateMessageForm messageForm = forumPortlet.findFirstComponentOfType(UIPrivateMessageForm.class);
      if (messageForm != null) {
        messageForm.updateSelect(FIELD_SENDTO_TEXTAREA, values);
      }
      UIPopupWindow popupWindow = uiUserSelector.getParent();
      closePopupWindow(popupWindow);
      event.getRequestContext().addUIComponentToUpdateByAjax(messageForm);
    }
  }

  static public class AddUserActionListener extends EventListener<UIPrivateMessageForm> {
    public void execute(Event<UIPrivateMessageForm> event) throws Exception {
      UIPrivateMessageForm messageForm = event.getSource();
      UIPopupContainer uiPopupContainer = messageForm.getAncestorOfType(UIPopupContainer.class);
      messageForm.showUIUserSelect(uiPopupContainer, USER_SELECTOR_POPUPWINDOW, ForumUtils.EMPTY_STR);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
    }
  }
}
