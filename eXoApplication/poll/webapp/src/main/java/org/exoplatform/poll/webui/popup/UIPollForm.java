/***************************************************************************
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.poll.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIFormMultiValueInputSet;
import org.exoplatform.ks.common.webui.UIGroupSelector;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.UISelector;
import org.exoplatform.poll.Utils;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.PollNodeTypes;
import org.exoplatform.poll.webui.BasePollForm;
import org.exoplatform.poll.webui.UIPollManagement;
import org.exoplatform.poll.webui.UIPollPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

/**
 * Created by The eXo Platform SAS 
 * Author : Vu Duy Tu 
 *          tu.duy@exoplatform.com 
 * 24 June 2010, 08:00:59
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/poll/webui/popup/UIPollForm.gtmpl",
    events = { 
        @EventConfig(listeners = UIPollForm.SaveActionListener.class),
        @EventConfig(listeners = UIPollForm.RefreshActionListener.class, phase = Phase.DECODE), 
        @EventConfig(listeners = UIPollForm.AddGroupActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIPollForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIPollForm extends BasePollForm implements UIPopupComponent, UISelector {
  public static final String       FIELD_QUESTION_INPUT       = "Question";

  final static public String       FIELD_OPTIONS              = "Option";

  public static final String       FIELD_TIMEOUT_INPUT        = "TimeOut";

  public static final String       FIELD_GROUP_PRIVATE_INPUT  = "GroupPrivate";

  public static final String       FIELD_AGAINVOTE_CHECKBOX   = "VoteAgain";

  public static final String       FIELD_MULTIVOTE_CHECKBOX   = "MultiVote";

  public static final String       FIELD_PUBLIC_DATA_CHECKBOX = "PublicData";

  public static final String       COLON                      = ":";

  public static final String       DELETED                      = "deleted";

  public static final String       ZERO                       = "0.0";

  public static final int          MAX_TITLE                  = 100;

  private UIFormMultiValueInputSet uiFormMultiValue           = new UIFormMultiValueInputSet(FIELD_OPTIONS, FIELD_OPTIONS);

  private Poll                     poll                       = new Poll();

  private boolean                  isUpdate                   = false;

  public boolean                   isEditPath                 = true;

  private boolean                  isPublic                   = true;

  public UIPollForm() throws Exception {
    UIFormStringInput question = new UIFormStringInput(FIELD_QUESTION_INPUT, FIELD_QUESTION_INPUT, "");
    UIFormStringInput timeOut = new UIFormStringInput(FIELD_TIMEOUT_INPUT, FIELD_TIMEOUT_INPUT, "");
    timeOut.addValidator(PositiveNumberFormatValidator.class);
    UICheckBoxInput VoteAgain = new UICheckBoxInput(FIELD_AGAINVOTE_CHECKBOX, FIELD_AGAINVOTE_CHECKBOX, false);
    UICheckBoxInput MultiVote = new UICheckBoxInput(FIELD_MULTIVOTE_CHECKBOX, FIELD_MULTIVOTE_CHECKBOX, false);
    UICheckBoxInput PublicData = new UICheckBoxInput(FIELD_PUBLIC_DATA_CHECKBOX, FIELD_PUBLIC_DATA_CHECKBOX, true);
    PublicData.setChecked(isPublic);
    UIFormStringInput GroupPrivate = new UIFormStringInput(FIELD_GROUP_PRIVATE_INPUT, FIELD_GROUP_PRIVATE_INPUT, "");
    GroupPrivate.setReadOnly(true);
    addUIFormInput(question);
    addUIFormInput(timeOut);
    addUIFormInput(VoteAgain);
    addUIFormInput(MultiVote);
    addUIFormInput(PublicData);
    addUIFormInput(GroupPrivate);
    setDefaulFall();
    setActions(new String[] { "Save", "Refresh", "Cancel" });
  }

  private void initMultiValuesField(List<String> list) throws Exception {
    if (uiFormMultiValue != null)
      removeChildById(FIELD_OPTIONS);
    uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, null, null);
    uiFormMultiValue.setId(FIELD_OPTIONS);
    uiFormMultiValue.setName(FIELD_OPTIONS);
    uiFormMultiValue.setType(UIFormStringInput.class);
    uiFormMultiValue.setValue(list);
    addUIFormInput(uiFormMultiValue);
  }

  protected String getDateAfter() throws Exception {
    Date date = new Date();
    if (poll != null && poll.getTimeOut() > 0) {
      date = poll.getModifiedDate();
    }
    String format = "MM-dd-yyyy";
    return Utils.getFormatDate(format, date);
  }

  public void setUpdatePoll(Poll poll, boolean isUpdate) throws Exception {
    if (isUpdate) {
      this.poll = poll;
      getUIStringInput(FIELD_QUESTION_INPUT).setValue(CommonUtils.decodeSpecialCharToHTMLnumber(poll.getQuestion()));
      getUIStringInput(FIELD_TIMEOUT_INPUT).setValue(String.valueOf(poll.getTimeOut()));
      getUICheckBoxInput(FIELD_AGAINVOTE_CHECKBOX).setChecked(poll.getIsAgainVote());
      UICheckBoxInput multiVoteCheckInput = getUICheckBoxInput(FIELD_MULTIVOTE_CHECKBOX);
      multiVoteCheckInput.setChecked(poll.getIsMultiCheck());
      multiVoteCheckInput.setDisabled(true);
      String group = poll.getParentPath();
      poll.setOldParentPath(group);
      if (group.indexOf(PollNodeTypes.APPLICATION_DATA) > 0) {
        isPublic = false;
        getUICheckBoxInput(FIELD_PUBLIC_DATA_CHECKBOX).setChecked(isPublic);
        group = group.substring(group.indexOf("/", 2), group.indexOf(PollNodeTypes.APPLICATION_DATA) - 1);
        getUIStringInput(FIELD_GROUP_PRIVATE_INPUT).setValue(group);
      } else if (group.indexOf(PollNodeTypes.POLLS) < 0) {
        isEditPath = false;
      }
      this.isUpdate = isUpdate;
      setDefaulFall();
    }
  }

  private void setDefaulFall() throws Exception {
    List<String> list = new ArrayList<String>();
    if (isUpdate) {
      for (String string : this.poll.getOption()) {
        list.add(CommonUtils.decodeSpecialCharToHTMLnumber(string));
      }
    } else {
      list.add("");
      list.add("");
    }
    this.initMultiValuesField(list);
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void updateSelect(String selectField, String value) throws Exception {
    UIFormStringInput GroupPrivate = getUIStringInput(selectField);
    GroupPrivate.setValue(value);
  }

  static public class SaveActionListener extends EventListener<UIPollForm> {
    public void execute(Event<UIPollForm> event) throws Exception {
      UIPollForm uiForm = event.getSource();
      String question = uiForm.getUIStringInput(FIELD_QUESTION_INPUT).getValue();
      question = CommonUtils.encodeSpecialCharInTitle(question);
      String timeOutStr = uiForm.getUIStringInput(FIELD_TIMEOUT_INPUT).getValue();
      timeOutStr = Utils.removeZeroFirstNumber(timeOutStr);
      long timeOut = 0;
      if (!Utils.isEmpty(timeOutStr)) {
        if (timeOutStr.length() > 4) {
          uiForm.warning("UIPollForm.msg.longTimeOut", new String[] { uiForm.getLabel(FIELD_TIMEOUT_INPUT) }, false);
        }
        timeOut = Long.parseLong(timeOutStr);
      }
      boolean isAgainVote = uiForm.getUICheckBoxInput(FIELD_AGAINVOTE_CHECKBOX).isChecked();
      boolean isMultiVote = uiForm.getUICheckBoxInput(FIELD_MULTIVOTE_CHECKBOX).isChecked();
      String sms = "";
      @SuppressWarnings("unchecked")
      List<String> values = (List<String>) uiForm.uiFormMultiValue.getValue();
      List<String> values_ = new ArrayList<String>();
      int i = 1;
      for (String value : values) {
        if (!Utils.isEmpty(value)) {
          if (value.length() > MAX_TITLE) {
            String[] args = new String[] { uiForm.getLabel(FIELD_OPTIONS) + "(" + i + ")", String.valueOf(MAX_TITLE) };
            uiForm.warning("NameValidator.msg.warning-long-text", args, false);
            return;
          }
          values_.add(CommonUtils.encodeSpecialCharInTitle(value));
        }
        ++i;
      }
      String[] options = values_.toArray(new String[] {});

      int sizeOption = values_.size();
      if (sizeOption < 2)
        sms = "Minimum";
      if (sizeOption > 10)
        sms = "Maximum";
      if (Utils.isEmpty(question)) {
        sms = "NotQuestion";
        sizeOption = 0;
      } else {
        if (question.length() > MAX_TITLE) {
          String[] args = { uiForm.getLabel(FIELD_QUESTION_INPUT), String.valueOf(MAX_TITLE) };
          uiForm.warning("NameValidator.msg.warning-long-text", args, false);
          return;
        }
      }
      if (sizeOption >= 2 && sizeOption <= 10) {
        String[] newUser = null;
        String[] vote = new String[sizeOption];
        for (int j = 0; j < sizeOption; j++) {
          vote[j] = ZERO;
        }
        if (uiForm.isUpdate) {
          List<Integer> listIndexItemRemoved = uiForm.uiFormMultiValue.getListIndexItemRemoved();
          String[] oldVote = uiForm.poll.getVote();
          String[] oldUserVote = uiForm.poll.getUserVote();
          String[] voteTp = new String[oldVote.length];

          double rmPecent = 0;
          List<String> voteRemoved = new ArrayList<String>();
          for (Integer integer : listIndexItemRemoved) {
            if (integer < oldVote.length) {
              rmPecent = rmPecent + Double.parseDouble(oldVote[integer]);
              voteRemoved.add(String.valueOf(integer));
            }
          }
          double leftPecent = 100 - rmPecent;
          i = 0;
          for (int k = 0; k < oldVote.length; ++k) {
            if (listIndexItemRemoved.contains(k)) {
              voteTp[k] = DELETED;
              continue;
            }
            if (leftPecent > 1) {
              double newVote = Double.parseDouble(oldVote[k]);
              String vl = String.valueOf((newVote * 100) / leftPecent);
              voteTp[k] = vl;
              vote[i] = vl;
            } else {
              voteTp[k] = ZERO;
              vote[i] = ZERO;
            }
            ++i;
          }

          if (!uiForm.poll.getIsMultiCheck()) {
            if (leftPecent > 1) {
              List<String> userL = new ArrayList<String>();
              for (String string : oldUserVote) {
                boolean isAdd = true;
                for (String j : voteRemoved) {
                  if (string.indexOf(COLON + j) > 0) {
                    isAdd = false;
                  }
                }
                if (isAdd)
                  userL.add(string);
              }

              newUser = new String[] {};
              i = 0;
              Map<String, String> mab = new HashMap<String, String>();
              for (int j = 0; j < voteTp.length; j++) {
                if (voteTp[j].equals(DELETED)) {
                  continue;
                }
//                vote[i] = voteTp[j];
                for (String str : userL) {
                  if (str.indexOf(COLON + j) > 0) {
                    mab.put(str, str.replace(COLON + j, COLON + i));
                  } else {
                    if (!mab.keySet().contains(str)) {
                      mab.put(str, str);
                    }
                  }
                }
                ++i;
              }
              newUser = mab.values().toArray(new String[userL.size()]);
            } else if (voteRemoved.size() > 0 && rmPecent > 0.0) {
              newUser = new String[] {};
            }
            // multi vote
          } else {
            List<String> newUserVote = new ArrayList<String>();
            for (String uv : oldUserVote) {
              StringBuffer sbUserInfo = new StringBuffer();
              for (String string : uv.split(COLON)) {
                if (!voteRemoved.contains(string)) {
                  if (sbUserInfo.length() > 0)
                    sbUserInfo.append(COLON);
                  sbUserInfo.append(string);
                }
              }
              String userInfo = sbUserInfo.toString();
              if (userInfo.split(COLON).length >= 2)
                newUserVote.add(userInfo);
            }

            i = 0;
            Map<String, String> mab = new HashMap<String, String>();
            for (int j = 0; j < voteTp.length; j++) {
              if (voteTp[j].equals(DELETED)) {
                continue;
              }
//              vote[i] = voteTp[j];
              for (String str : newUserVote) {
                if (str.indexOf(COLON + j) > 0) {
                  if (mab.containsKey(str))
                    mab.put(str, mab.get(str).replace(COLON + j, COLON + i));
                  else
                    mab.put(str, str.replace(COLON + j, COLON + i));
                } else {
                  if (!mab.keySet().contains(str)) {
                    mab.put(str, str);
                  }
                }
              }
              ++i;
            }
            newUser = mab.values().toArray(new String[newUserVote.size()]);
          }
        }
        String userName = UserHelper.getCurrentUser();
        Poll poll = uiForm.poll;
        poll.setQuestion(question);
        poll.setModifiedBy(userName);
        poll.setModifiedDate(new Date());
        poll.setIsAgainVote(isAgainVote);
        poll.setIsMultiCheck(isMultiVote);
        poll.setOption(options);
        poll.setVote(vote);
        poll.setTimeOut(timeOut);
        poll.setIsClosed(uiForm.poll.getIsClosed());
        try {
          if (Utils.isEmpty(poll.getParentPath()) || poll.getParentPath().contains(PollNodeTypes.POLLS) || poll.getParentPath().contains(PollNodeTypes.EXO_POLLS)) {
            boolean isPublic = uiForm.getUICheckBoxInput(FIELD_PUBLIC_DATA_CHECKBOX).isChecked();
            String parentPath = "";
            // if poll of topic : parentPath = topic.getPath();
            // if poll of Group : parentPath = $GROUP/${PollNodeTypes.APPLICATION_DATA}/${PollNodeTypes.EXO_POLLS}
            // if poll of public: parentPath = $PORTAL/${PollNodeTypes.POLLS}
            if (isPublic) {
              // test for public:
              parentPath = ExoContainerContext.getCurrentContainer().getContext().getName() + "/" + PollNodeTypes.POLLS;
            } else {
              parentPath = uiForm.getUIStringInput(FIELD_GROUP_PRIVATE_INPUT).getValue();
              if (parentPath.indexOf("/") == 0)
                parentPath = parentPath.substring(1);
              parentPath = parentPath + "/" + PollNodeTypes.APPLICATION_DATA + "/" + PollNodeTypes.EXO_POLLS;
            }
            poll.setParentPath(parentPath);
          }
          if (uiForm.isUpdate) {
            if (newUser != null) {
              poll.setUserVote(newUser);
            }
            uiForm.getPollService().savePoll(poll, false, false);
          } else {
            poll.setOwner(userName);
            poll.setCreatedDate(new Date());
            poll.setUserVote(new String[] {});
            uiForm.getPollService().savePoll(poll, true, false);
          }
        } catch (Exception e) {
          uiForm.warning("UIPollForm.msg.fail-save");
          return;
        }
        uiForm.isUpdate = false;
        UIPollPortlet pollPortlet = uiForm.getAncestorOfType(UIPollPortlet.class);
        pollPortlet.cancelAction();
        pollPortlet.getChild(UIPollManagement.class).updateGrid();
        event.getRequestContext().addUIComponentToUpdateByAjax(pollPortlet);
      }
      if (!Utils.isEmpty(sms)) {
        uiForm.warning("UIPollForm.msg." + sms, false);
      }
    }
  }

  static public class RefreshActionListener extends EventListener<UIPollForm> {
    public void execute(Event<UIPollForm> event) throws Exception {
      UIPollForm uiForm = event.getSource();
      if(uiForm.isUpdate) {
        uiForm.setUpdatePoll(uiForm.poll, uiForm.isUpdate);
      } else {
        List<String> list = new ArrayList<String>();
        list.add("");
        list.add("");
        uiForm.initMultiValuesField(list);
        uiForm.getUIStringInput(FIELD_QUESTION_INPUT).setValue("");
        uiForm.getUIStringInput(FIELD_TIMEOUT_INPUT).setValue("0");
        uiForm.getUICheckBoxInput(FIELD_AGAINVOTE_CHECKBOX).setChecked(false);
        uiForm.getUICheckBoxInput(FIELD_MULTIVOTE_CHECKBOX).setChecked(false);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class AddGroupActionListener extends BaseEventListener<UIPollForm> {
    public void onEvent(Event<UIPollForm> event, UIPollForm uiForm, String objctId) throws Exception {
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
      popupAction.getChild(UIPopupWindow.class).setId("UIPopupChildWindow");
      UIGroupSelector uiGroupSelector = popupAction.activate(UIGroupSelector.class, 600);
      uiGroupSelector.setId("UIGroupSelector");
      uiGroupSelector.setType(UISelectComponent.TYPE_GROUP);
      uiGroupSelector.setSelectedGroups(null);
      uiGroupSelector.setComponent(uiForm, new String[] { FIELD_GROUP_PRIVATE_INPUT });
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

  static public class CancelActionListener extends EventListener<UIPollForm> {
    public void execute(Event<UIPollForm> event) throws Exception {
      UIPollForm uiForm = event.getSource();
      UIPollPortlet pollPortlet = uiForm.getAncestorOfType(UIPollPortlet.class);
      pollPortlet.cancelAction();
      uiForm.isUpdate = false;
    }
  }
}
