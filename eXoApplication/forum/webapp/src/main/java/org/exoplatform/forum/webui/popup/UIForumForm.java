/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategories;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIGroupSelector;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.UISelector;
import org.exoplatform.ks.common.webui.UIUserSelect;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfigs ( {
        @ComponentConfig(
            lifecycle = UIFormLifecycle.class,
            template = "app:/templates/forum/webui/popup/UIForumForm.gtmpl",
            events = {
              @EventConfig(listeners = UIForumForm.SaveActionListener.class), 
              @EventConfig(listeners = UIForumForm.AddValuesUserActionListener.class, phase=Phase.DECODE),
              @EventConfig(listeners = UIForumForm.AddUserActionListener.class, phase=Phase.DECODE),
              @EventConfig(listeners = UIForumForm.CancelActionListener.class, phase=Phase.DECODE),
              @EventConfig(listeners = UIForumForm.SelectTabActionListener.class, phase=Phase.DECODE),
              @EventConfig(listeners = UIForumForm.OnChangeAutoEmailActionListener.class, phase=Phase.DECODE)
            }
        )
      ,
        @ComponentConfig(
             id = "UIForumUserPopupWindow",
             type = UIPopupWindow.class,
             template = "system:/groovy/webui/core/UIPopupWindow.gtmpl",
             events = {
               @EventConfig(listeners = UIForumForm.ClosePopupActionListener.class, name = "ClosePopup")  ,
               @EventConfig(listeners = UIForumForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
               @EventConfig(listeners = UIForumForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
             }
        )
    }
)
public class UIForumForm extends BaseForumForm implements UIPopupComponent, UISelector {
  private boolean            isCategoriesUpdate                  = true;

  private boolean            isForumUpdate                       = false;

  private boolean            isActionBar                         = false;

  private boolean            isMode                              = false;

  private boolean            isAddValue                          = true;

  private boolean            isUpdate                            = false;

  private String             forumId                             = ForumUtils.EMPTY_STR;

  private String             categoryId                          = ForumUtils.EMPTY_STR;

  private int                id                                  = 0;

  private boolean            isDoubleClickSubmit;

  public static final String FIELD_NEWFORUM_FORM                 = "newForum";

  public static final String FIELD_MODERATOROPTION_FORM          = "moderationOptions";

  public static final String FIELD_FORUMPERMISSION_FORM          = "forumPermission";

  public static final String FIELD_CATEGORY_SELECTBOX            = "Category";

  public static final String FIELD_FORUMTITLE_INPUT              = "ForumTitle";

  public static final String FIELD_FORUMORDER_INPUT              = "ForumOrder";

  public static final String FIELD_FORUMSTATUS_SELECTBOX         = "ForumStatus";

  public static final String FIELD_FORUMSTATE_SELECTBOX          = "ForumState";

  public static final String FIELD_DESCRIPTION_TEXTAREA          = "Description";

  public static final String FIELD_AUTOADDEMAILNOTIFY_CHECKBOX   = "AutoAddEmailNotify";

  public static final String FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE = "NotifyWhenAddTopic";

  public static final String FIELD_NOTIFYWHENADDPOST_MULTIVALUE  = "NotifyWhenAddPost";

  public static final String FIELD_MODERATETHREAD_CHECKBOX       = "ModerateThread";

  public static final String FIELD_MODERATEPOST_CHECKBOX         = "ModeratePost";

  public static final String FIELD_MODERATOR_MULTIVALUE          = "Moderator";

  public static final String FIELD_VIEWER_MULTIVALUE             = "Viewer";

  public static final String FIELD_POSTABLE_MULTIVALUE           = "Postable";

  public static final String FIELD_TOPICABLE_MULTIVALUE          = "Topicable";

  public static final String USER_SELECTOR_POPUPWINDOW           = "UIForumUserPopupWindow";

  public UIForumForm() throws Exception {
    isDoubleClickSubmit = false;
  }

  public boolean isMode() {
    return isMode;
  }

  public void setMode(boolean isMode) {
    this.isMode = isMode;
  }

  public void initForm() throws Exception {
    List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>();
    if (ForumUtils.isEmpty(categoryId)) {
      List<Category> categorys = getForumService().getCategories();
      for (Category category : categorys) {
        list.add(new SelectItemOption<String>(category.getCategoryName(), category.getId()));
      }
      if(list.size() > 0){
        categoryId = list.get(0).getValue();
      }
    } else {
      Category category = getForumService().getCategory(categoryId);
      list.add(new SelectItemOption<String>(category.getCategoryName(), categoryId));
    }

    UIFormSelectBox selictCategoryId = new UIFormSelectBox(FIELD_CATEGORY_SELECTBOX, FIELD_CATEGORY_SELECTBOX, list);
    selictCategoryId.setDefaultValue(categoryId);

    UIFormStringInput forumTitle = new UIFormStringInput(FIELD_FORUMTITLE_INPUT, FIELD_FORUMTITLE_INPUT, null);
    forumTitle.addValidator(MandatoryValidator.class);
    UIFormStringInput forumOrder = new UIFormStringInput(FIELD_FORUMORDER_INPUT, FIELD_FORUMORDER_INPUT, "0");
    forumOrder.addValidator(PositiveNumberFormatValidator.class);
    List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>();
    ls.add(new SelectItemOption<String>(getLabel("Open"), "open"));
    ls.add(new SelectItemOption<String>(getLabel("Closed"), "closed"));
    UIFormSelectBox forumState = new UIFormSelectBox(FIELD_FORUMSTATE_SELECTBOX, FIELD_FORUMSTATE_SELECTBOX, ls);
    forumState.setDefaultValue("open");
    ls = new ArrayList<SelectItemOption<String>>();
    ls.add(new SelectItemOption<String>(this.getLabel("UnLock"), "unlock"));
    ls.add(new SelectItemOption<String>(this.getLabel("Locked"), "locked"));
    UIFormSelectBox forumStatus = new UIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX, FIELD_FORUMSTATUS_SELECTBOX, ls);
    forumStatus.setDefaultValue("unlock");
    UIFormTextAreaInput description = new UIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA, FIELD_DESCRIPTION_TEXTAREA, null);

    UICheckBoxInput checkWhenAddTopic = new UICheckBoxInput(FIELD_MODERATETHREAD_CHECKBOX, FIELD_MODERATETHREAD_CHECKBOX, false);
    UIFormTextAreaInput notifyWhenAddPost = new UIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE, FIELD_NOTIFYWHENADDPOST_MULTIVALUE, null);
    UIFormTextAreaInput notifyWhenAddTopic = new UIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE, FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE, null);

    UIFormTextAreaInput moderator = new UIFormTextAreaInput(FIELD_MODERATOR_MULTIVALUE, FIELD_MODERATOR_MULTIVALUE, null);
    UIFormTextAreaInput viewer = new UIFormTextAreaInput(FIELD_VIEWER_MULTIVALUE, FIELD_VIEWER_MULTIVALUE, null);
    UIFormTextAreaInput postable = new UIFormTextAreaInput(FIELD_POSTABLE_MULTIVALUE, FIELD_POSTABLE_MULTIVALUE, null);
    UIFormTextAreaInput topicable = new UIFormTextAreaInput(FIELD_TOPICABLE_MULTIVALUE, FIELD_TOPICABLE_MULTIVALUE, null);

    UICheckBoxInput autoAddEmailNotify = new UICheckBoxInput(FIELD_AUTOADDEMAILNOTIFY_CHECKBOX, FIELD_AUTOADDEMAILNOTIFY_CHECKBOX, true);
    autoAddEmailNotify.setValue(true);
    autoAddEmailNotify.setOnChange("OnChangeAutoEmail");
    addUIFormInput(selictCategoryId);
    UIFormInputWithActions newForum = new UIFormInputWithActions(FIELD_NEWFORUM_FORM);
    newForum.addUIFormInput(forumTitle);
    newForum.addUIFormInput(forumOrder);
    newForum.addUIFormInput(forumState);
    newForum.addUIFormInput(forumStatus);
    newForum.addUIFormInput(description);

    UIFormInputWithActions moderationOptions = new UIFormInputWithActions(FIELD_MODERATOROPTION_FORM);
    moderationOptions.addUIFormInput(moderator);
    moderationOptions.addUIFormInput(autoAddEmailNotify);
    moderationOptions.addUIFormInput(notifyWhenAddPost);
    moderationOptions.addUIFormInput(notifyWhenAddTopic);
    moderationOptions.addUIFormInput(checkWhenAddTopic);

    UIFormInputWithActions forumPermission = new UIFormInputWithActions(FIELD_FORUMPERMISSION_FORM);
    forumPermission.addUIFormInput(topicable);
    forumPermission.addUIFormInput(postable);
    forumPermission.addUIFormInput(viewer);
    String[] fieldPermissions = getChildIds();
    String[] strings = new String[] { "SelectUser", "SelectMemberShip", "SelectGroup" };
    List<ActionData> actions;
    ActionData ad;
    int i;
    for (String fieldPermission : fieldPermissions) {
      actions = new ArrayList<ActionData>();
      i = 0;
      for (String string : strings) {
        ad = new ActionData();
        if (i == 0)
          ad.setActionListener("AddUser");
        else
          ad.setActionListener("AddValuesUser");
        ad.setActionParameter(fieldPermission + ForumUtils.SLASH + String.valueOf(i));
        ad.setCssIconClass(string + "Icon");
        ad.setActionName(string);
        actions.add(ad);
        ++i;
      }
      if (fieldPermission.equals(FIELD_MODERATOR_MULTIVALUE)) {
        if (isMode)
          continue;
        else
          moderationOptions.setActionField(fieldPermission, actions);
      } else
        forumPermission.setActionField(fieldPermission, actions);
    }

    addUIFormInput(newForum);
    addUIFormInput(moderationOptions);
    addUIFormInput(forumPermission);
    this.setActions(new String[] { "Save", "Cancel" });
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  protected boolean getIsSelected(int id) {
    if (this.id == id)
      return true;
    return false;
  }

  public void setForumValue(Forum forum, boolean isUpdate) throws Exception {
    this.isUpdate = isUpdate;
    if (isUpdate) {
      forumId = forum.getId();
      forum = getForumService().getForum(categoryId, forumId);
      UIFormInputWithActions newForum = this.getChildById(FIELD_NEWFORUM_FORM);
      newForum.getUIStringInput(FIELD_FORUMTITLE_INPUT).setValue(CommonUtils.decodeSpecialCharToHTMLnumber(forum.getForumName()));
      newForum.getUIStringInput(FIELD_FORUMORDER_INPUT).setValue(String.valueOf(forum.getForumOrder()));
      String stat = "open";
      if (forum.getIsClosed())
        stat = "closed";
      newForum.getUIFormSelectBox(FIELD_FORUMSTATE_SELECTBOX).setValue(stat);
      if (forum.getIsLock())
        stat = "locked";
      else
        stat = "unlock";
      newForum.getUIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX).setValue(stat);
      newForum.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA)
              .setDefaultValue(CommonUtils.decodeSpecialCharToHTMLnumber(forum.getDescription()));

      UIFormInputWithActions moderationOptions = this.getChildById(FIELD_MODERATOROPTION_FORM);
      boolean isAutoAddEmail = forum.getIsAutoAddEmailNotify();
      UICheckBoxInput boxInput = getUICheckBoxInput(FIELD_AUTOADDEMAILNOTIFY_CHECKBOX);
      boxInput.setChecked(isAutoAddEmail);
      boxInput.setReadOnly(isMode);

      UIFormTextAreaInput areaInput = moderationOptions.getUIFormTextAreaInput(FIELD_MODERATOR_MULTIVALUE);
      areaInput.setValue(ForumUtils.unSplitForForum(forum.getModerators()));
      areaInput.setReadOnly(isMode);
      areaInput.setDisabled(isMode);
      UIFormTextAreaInput notifyWhenAddPost = moderationOptions.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE);
      UIFormTextAreaInput notifyWhenAddTopic = moderationOptions.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE);
      notifyWhenAddPost.setValue(ForumUtils.unSplitForForum(forum.getNotifyWhenAddPost()));
      notifyWhenAddTopic.setValue(ForumUtils.unSplitForForum(forum.getNotifyWhenAddTopic()));
      getUICheckBoxInput(FIELD_MODERATETHREAD_CHECKBOX).setChecked(forum.getIsModerateTopic());

      UIFormInputWithActions forumPermission = this.getChildById(FIELD_FORUMPERMISSION_FORM);
      forumPermission.getUIFormTextAreaInput(FIELD_VIEWER_MULTIVALUE).setValue(ForumUtils.unSplitForForum(forum.getViewer()));
      forumPermission.getUIFormTextAreaInput(FIELD_TOPICABLE_MULTIVALUE).setValue(ForumUtils.unSplitForForum(forum.getCreateTopicRole()));
      forumPermission.getUIFormTextAreaInput(FIELD_POSTABLE_MULTIVALUE).setValue(ForumUtils.unSplitForForum(forum.getPoster()));
    }
  }

  public void setCategoryValue(String categoryId, boolean isEditable) throws Exception {
    if (!ForumUtils.isEmpty(categoryId))
      getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX).setValue(categoryId);
    getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX).setDisabled(!isEditable);
    isCategoriesUpdate = isEditable;
    this.categoryId = categoryId;
    isUpdate = false;
  }

  public void setForumUpdate(boolean isForumUpdate) {
    this.isForumUpdate = isForumUpdate;
  }

  public boolean isActionBar() {
    return isActionBar;
  }

  public void setActionBar(boolean isActionBar) {
    this.isActionBar = isActionBar;
  }

  private String[] getChildIds() {
    return new String[] { FIELD_MODERATOR_MULTIVALUE, FIELD_TOPICABLE_MULTIVALUE, FIELD_POSTABLE_MULTIVALUE, FIELD_VIEWER_MULTIVALUE };
  }

  private static String listToString(Collection<String> list) {
    return list.toString().replace("[", ForumUtils.EMPTY_STR).replace("]", ForumUtils.EMPTY_STR);
  }
  
  public void updateSelect(String selectField, String value) throws Exception {
    UIFormTextAreaInput fieldInput = getUIFormTextAreaInput(selectField);
    if (selectField.indexOf("Notify") >= 0) {
      fieldInput.setValue(value);
    } else {
      String values = fieldInput.getValue();
      fieldInput.setValue(ForumUtils.updateMultiValues(value, values));
      if (selectField.equals(FIELD_MODERATOR_MULTIVALUE)) {
        UIFormInputWithActions moderationOptions = this.getChildById(FIELD_MODERATOROPTION_FORM);
        boolean isAutoAddEmail = getUICheckBoxInput(FIELD_AUTOADDEMAILNOTIFY_CHECKBOX).isChecked();
        if (isAutoAddEmail) {
          this.setDefaultEmail(moderationOptions);
        }
        this.isAddValue = false;
      } else {
        this.isAddValue = true;
      }
    }
  }

  static public class SaveActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      UIForumForm uiForm = event.getSource();
      if (uiForm.isDoubleClickSubmit)
        return;
      uiForm.isDoubleClickSubmit = true;
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);

      UIFormSelectBox categorySelectBox = uiForm.getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX);
      String categoryId = categorySelectBox.getValue();

      UIFormInputWithActions newForumForm = uiForm.getChildById(FIELD_NEWFORUM_FORM);
      String forumTitle = newForumForm.getUIStringInput(FIELD_FORUMTITLE_INPUT).getValue();
      forumTitle = forumTitle.trim();
      int maxText = 50;// ForumUtils.MAXTITLE ;
      if (forumTitle.length() > maxText) {
        uiForm.warning("NameValidator.msg.warning-long-text", new String[] { uiForm.getLabel(FIELD_FORUMTITLE_INPUT), String.valueOf(maxText) });
        uiForm.isDoubleClickSubmit = false;
        return;
      }
      forumTitle = CommonUtils.encodeSpecialCharInTitle(forumTitle);
      String forumOrder = newForumForm.getUIStringInput(FIELD_FORUMORDER_INPUT).getValue();
      if (ForumUtils.isEmpty(forumOrder))
        forumOrder = "0";
      forumOrder = ForumUtils.removeZeroFirstNumber(forumOrder);
      if (forumOrder.length() > 3) {
        uiForm.warning("NameValidator.msg.erro-large-number", new String[] { uiForm.getLabel(FIELD_FORUMORDER_INPUT) });
        uiForm.isDoubleClickSubmit = false;
        return;
      }
      String forumState = newForumForm.getUIFormSelectBox(FIELD_FORUMSTATE_SELECTBOX).getValue();
      String forumStatus = newForumForm.getUIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX).getValue();
      String description = newForumForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA).getValue();
      
      description = CommonUtils.encodeSpecialCharInTitle(description);
      UIFormInputWithActions moderationOptions = uiForm.getChildById(FIELD_MODERATOROPTION_FORM);
      boolean isAutoAddEmail = uiForm.getUICheckBoxInput(FIELD_AUTOADDEMAILNOTIFY_CHECKBOX).isChecked();
      String moderators = moderationOptions.getUIFormTextAreaInput(FIELD_MODERATOR_MULTIVALUE).getValue();
      // set email
      if (isAutoAddEmail && uiForm.isAddValue) {
        // uiForm.setDefaultEmail(moderationOptions);
      }
      String notifyWhenAddTopics = moderationOptions.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE).getValue();
      String notifyWhenAddPosts = moderationOptions.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE).getValue();

      if (!ForumUtils.isValidEmailAddresses(notifyWhenAddPosts) || !ForumUtils.isValidEmailAddresses(notifyWhenAddTopics)) {
        uiForm.warning("UIAddMultiValueForm.msg.invalid-field");
        uiForm.isDoubleClickSubmit = false;
        return;
      }
      String[] notifyWhenAddTopic = ForumUtils.splitForForum(notifyWhenAddTopics);
      String[] notifyWhenAddPost = ForumUtils.splitForForum(notifyWhenAddPosts);
      boolean ModerateTopic = uiForm.getUICheckBoxInput(FIELD_MODERATETHREAD_CHECKBOX).getValue();

      UIFormInputWithActions forumPermission = uiForm.getChildById(FIELD_FORUMPERMISSION_FORM);
      String topicable = forumPermission.getUIFormTextAreaInput(FIELD_TOPICABLE_MULTIVALUE).getValue();
      String postable = forumPermission.getUIFormTextAreaInput(FIELD_POSTABLE_MULTIVALUE).getValue();
      String viewer = forumPermission.getUIFormTextAreaInput(FIELD_VIEWER_MULTIVALUE).getValue();

      moderators = ForumUtils.removeSpaceInString(moderators);
      topicable = ForumUtils.removeSpaceInString(topicable);
      postable = ForumUtils.removeSpaceInString(postable);
      viewer = ForumUtils.removeSpaceInString(viewer);

      String userName = UserHelper.getCurrentUser();
      Forum newForum = new Forum();
      newForum.setForumName(forumTitle);
      newForum.setOwner(userName);
      newForum.setForumOrder(Integer.valueOf(forumOrder).intValue());
      newForum.setCreatedDate(new Date());
      newForum.setDescription(description);
      newForum.setLastTopicPath(ForumUtils.EMPTY_STR);
      newForum.setPath(ForumUtils.EMPTY_STR);
      newForum.setModifiedBy(userName);
      newForum.setModifiedDate(new Date());
      newForum.setPostCount(0);
      newForum.setTopicCount(0);
      newForum.setIsAutoAddEmailNotify(isAutoAddEmail);
      newForum.setNotifyWhenAddPost(notifyWhenAddPost);
      newForum.setNotifyWhenAddTopic(notifyWhenAddTopic);
      newForum.setIsModeratePost(false);
      newForum.setIsModerateTopic(ModerateTopic);
      if (forumState.equals("closed")) {
        newForum.setIsClosed(true);
      }
      if (forumStatus.equals("locked")) {
        newForum.setIsLock(true);
      }
      String erroUser = UserHelper.checkValueUser(moderators);
      if (!ForumUtils.isEmpty(erroUser)) {
        String[] args = new String[] { uiForm.getLabel(FIELD_MODERATOR_MULTIVALUE), erroUser };
        uiForm.warning("NameValidator.msg.erroUser-input", args);
        uiForm.isDoubleClickSubmit = false;
        return;
      }
      erroUser = UserHelper.checkValueUser(topicable);
      if (!ForumUtils.isEmpty(erroUser)) {
        String[] args = new String[] { uiForm.getLabel(FIELD_TOPICABLE_MULTIVALUE), erroUser };
        uiForm.warning("NameValidator.msg.erroUser-input", args);
        uiForm.isDoubleClickSubmit = false;
        return;
      }
      erroUser = UserHelper.checkValueUser(postable);
      if (!ForumUtils.isEmpty(erroUser)) {
        String[] args = new String[] { uiForm.getLabel(FIELD_POSTABLE_MULTIVALUE), erroUser };
        uiForm.warning("NameValidator.msg.erroUser-input", args);
        uiForm.isDoubleClickSubmit = false;
        return;
      }
      erroUser = UserHelper.checkValueUser(viewer);
      if (!ForumUtils.isEmpty(erroUser)) {
        String[] args = new String[] { uiForm.getLabel(FIELD_VIEWER_MULTIVALUE), erroUser };
        uiForm.warning("NameValidator.msg.erroUser-input", args);
        uiForm.isDoubleClickSubmit = false;
        return;
      }

      String[] setModerators = ForumUtils.splitForForum(moderators);
      String[] setTopicable = ForumUtils.splitForForum(topicable);
      String[] setPostable = ForumUtils.splitForForum(postable);
      String[] setViewer = ForumUtils.splitForForum(viewer);

      newForum.setModerators(setModerators);
      newForum.setCreateTopicRole(setTopicable);
      newForum.setPoster(setPostable);
      newForum.setViewer(setViewer);

      try {
        if (!ForumUtils.isEmpty(uiForm.forumId)) {
          newForum.setId(uiForm.forumId);
          uiForm.getForumService().saveForum(categoryId, newForum, false);
        } else {
          uiForm.getForumService().saveForum(categoryId, newForum, true);
          List<String> invisibleCategories = forumPortlet.getInvisibleCategories();
          List<String> invisibleForums = forumPortlet.getInvisibleForums();
          String listForumId = ForumUtils.EMPTY_STR, listCategoryId = ForumUtils.EMPTY_STR;
          if (!invisibleCategories.isEmpty()) {
            if (invisibleCategories.contains(categoryId)) {
              invisibleForums.add(newForum.getId());
              listForumId = listToString(invisibleForums).replaceAll(" ", ForumUtils.EMPTY_STR);
              listCategoryId = listToString(invisibleCategories).replaceAll(" ", ForumUtils.EMPTY_STR);
              ForumUtils.savePortletPreference(listCategoryId, listForumId);
            }
          }
        }
      } catch (Exception e) {
        uiForm.log.error("Save portlet preference is fall, exception: ", e);
      }
      forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks();

      forumPortlet.cancelAction();
      WebuiRequestContext context = event.getRequestContext();

      if (uiForm.isUpdate && !uiForm.isForumUpdate) {
        if (uiForm.isCategoriesUpdate) {
          UICategories uiCategories = forumPortlet.findFirstComponentOfType(UICategories.class);
          uiCategories.setIsgetForumList(true);
          if (!uiForm.isActionBar)
            context.addUIComponentToUpdateByAjax(uiCategories);
        } else {
          UICategory uiCategory = forumPortlet.findFirstComponentOfType(UICategory.class);
          uiCategory.setIsEditForum(true);
          if (!uiForm.isActionBar)
            context.addUIComponentToUpdateByAjax(uiCategory);
        }
        if (uiForm.isActionBar) {
          forumPortlet.findFirstComponentOfType(UICategory.class).setIsEditForum(true);
          forumPortlet.findFirstComponentOfType(UICategories.class).setIsgetForumList(true);
          context.addUIComponentToUpdateByAjax(forumPortlet);
        }
      } else {
        UITopicContainer uiTopicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
        if (!uiForm.isForumUpdate) {
          forumPortlet.updateIsRendered(ForumUtils.FORUM);
          UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class);
          uiForumContainer.setIsRenderChild(true);
          uiTopicContainer.updateByBreadcumbs(categoryId, newForum.getId(), true, 1);
          forumPortlet.getChild(UIForumLinks.class).setValueOption(categoryId + ForumUtils.SLASH + newForum.getId());
        }
        UIForumDescription forumDescription = forumPortlet.findFirstComponentOfType(UIForumDescription.class);
        forumDescription.setForum(newForum);
        UIBreadcumbs breadcumbs = forumPortlet.getChild(UIBreadcumbs.class);
        breadcumbs.setUpdataPath(categoryId + ForumUtils.SLASH + newForum.getId());
        forumPortlet.findFirstComponentOfType(UITopicContainer.class).setForum(true);
        context.addUIComponentToUpdateByAjax(forumPortlet);
      }
    }
  }

  static public class AddValuesUserActionListener extends BaseEventListener<UIForumForm> {
    public void onEvent(Event<UIForumForm> event, UIForumForm forumForm, String objctId) throws Exception {
      String[] array = objctId.split(ForumUtils.SLASH);
      String childId = array[0];
      if (!ForumUtils.isEmpty(childId)) {
        UIPopupContainer popupContainer = forumForm.getAncestorOfType(UIPopupContainer.class);
        UIUserSelect uiUserSelect = popupContainer.findFirstComponentOfType(UIUserSelect.class);
        if (uiUserSelect != null) {
          UIPopupWindow popupWindow = uiUserSelect.getParent();
          closePopupWindow(popupWindow);
        }

        UIGroupSelector uiGroupSelector = null;
        if (array[1].equals(UIGroupSelector.TYPE_MEMBERSHIP)) {
          uiGroupSelector = openPopup(popupContainer, UIGroupSelector.class, "UIMemberShipSelector", 600, 0);
        } else if (array[1].equals(UIGroupSelector.TYPE_GROUP)) {
          uiGroupSelector = openPopup(popupContainer, UIGroupSelector.class, "GroupSelector", 600, 0);
        }
        uiGroupSelector.setType(array[1]);
        uiGroupSelector.setSpaceGroupId(forumForm.getAncestorOfType(UIForumPortlet.class).getSpaceGroupId());
        uiGroupSelector.setComponent(forumForm, new String[] { childId });
        uiGroupSelector.getChild(UITree.class).setId(UIGroupSelector.TREE_GROUP_ID);
        uiGroupSelector.getChild(org.exoplatform.webui.core.UIBreadcumbs.class).setId(UIGroupSelector.BREADCUMB_GROUP_ID);
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }

  static public class OnChangeAutoEmailActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      UIForumForm forumForm = event.getSource();
      UIFormInputWithActions moderationOptions = forumForm.getChildById(FIELD_MODERATOROPTION_FORM);
      boolean isCheck = forumForm.getUICheckBoxInput(FIELD_AUTOADDEMAILNOTIFY_CHECKBOX).isChecked();
      if (isCheck) {
        forumForm.setDefaultEmail(moderationOptions);
      } else
        event.getRequestContext().addUIComponentToUpdateByAjax(moderationOptions);
    }
  }
  
  private String listEmailForSendNotify(Set<String> listModerator, String inputValue) throws Exception {
    Set<String> newset = new HashSet<String>(listModerator);
    if (!ForumUtils.isEmpty(inputValue)) {
      newset.addAll(Arrays.asList(ForumUtils.splitForForum(inputValue)));
    }
    return listToString(newset);
  }

  private void setDefaultEmail(UIFormInputWithActions moderationOptions) throws Exception {
    String moderators = moderationOptions.getUIFormTextAreaInput(FIELD_MODERATOR_MULTIVALUE).getValue();
    if (!ForumUtils.isEmpty(moderators)) {
      UIFormTextAreaInput notifyWhenAddTopics = moderationOptions.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE);
      UIFormTextAreaInput notifyWhenAddPosts = moderationOptions.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE);
      String emailTopic = notifyWhenAddTopics.getValue();
      String emailPost = notifyWhenAddPosts.getValue();
      String[] moderators_ = ForumUtils.splitForForum(moderators);
      Set<String> listModerator = new HashSet<String>();
      String email;
      User user = null;
      List<String> list = ForumServiceUtils.getUserPermission(moderators_);
      for (String string : list) {
        user = UserHelper.getUserByUserId(string);
        if (user != null) {
          email = user.getEmail();
          listModerator.add(email);
        }
      }
      notifyWhenAddTopics.setValue(listEmailForSendNotify(listModerator, emailTopic));
      notifyWhenAddPosts.setValue(listEmailForSendNotify(listModerator, emailPost));
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      context.addUIComponentToUpdateByAjax(moderationOptions);
    }
  }

  static public class SelectTabActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      String id = event.getRequestContext().getRequestParameter(OBJECTID);
      UIForumForm forumForm = event.getSource();
      forumForm.id = Integer.parseInt(id);
      event.getRequestContext().addUIComponentToUpdateByAjax(forumForm);
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

  private void setValueField(UIFormInputWithActions withActions, String field, String values) throws Exception {
    try {
      UIFormTextAreaInput textArea = withActions.getUIFormTextAreaInput(field);
      if (textArea != null) {
        String vls = textArea.getValue();
        if (!ForumUtils.isEmpty(vls)) {
          values = values + ForumUtils.COMMA + vls;
          values = ForumUtils.removeStringResemble(values.replaceAll(",,", ForumUtils.COMMA));
        }
        textArea.setValue(values);
        if (field.equals(FIELD_MODERATOR_MULTIVALUE)) {
          boolean isAutoAddEmail = getUICheckBoxInput(FIELD_AUTOADDEMAILNOTIFY_CHECKBOX).isChecked();
          if (isAutoAddEmail) {
            this.setDefaultEmail(withActions);
          }
          this.isAddValue = false;
        } else {
          this.isAddValue = true;
        }
      }
    } catch (Exception e) {
      log.error("Set value in field is fall, exception: ", e);
    }
  }

  static public class AddActionListener extends EventListener<UIUserSelect> {
    public void execute(Event<UIUserSelect> event) throws Exception {
      UIUserSelect uiUserSelector = event.getSource();
      String values = uiUserSelector.getSelectedUsers();
      UIForumPortlet forumPortlet = uiUserSelector.getAncestorOfType(UIForumPortlet.class);
      UIForumForm forumForm = forumPortlet.findFirstComponentOfType(UIForumForm.class);
      UIPopupWindow popupWindow = uiUserSelector.getParent();
      String id = uiUserSelector.getPermisionType();
      UIFormInputWithActions inputWithActions = forumForm.getChildById((id.equals(FIELD_MODERATOR_MULTIVALUE)) ? FIELD_MODERATOROPTION_FORM : FIELD_FORUMPERMISSION_FORM);
      forumForm.setValueField(inputWithActions, id, values);
      closePopupWindow(popupWindow);
      event.getRequestContext().addUIComponentToUpdateByAjax(forumForm);
    }
  }

  static public class AddUserActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      UIForumForm forumForm = event.getSource();
      String id = event.getRequestContext().getRequestParameter(OBJECTID).replace("/0", ForumUtils.EMPTY_STR);
      UIPopupContainer uiPopupContainer = forumForm.getAncestorOfType(UIPopupContainer.class);
      forumForm.showUIUserSelect(uiPopupContainer, USER_SELECTOR_POPUPWINDOW, id);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
    }
  }
}
