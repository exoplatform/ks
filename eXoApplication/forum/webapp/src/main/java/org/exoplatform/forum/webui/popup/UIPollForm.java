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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.TimeConvertUtils;
import org.exoplatform.forum.info.UIForumPollPortlet;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIFormMultiValueInputSet;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.PollService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIPollForm.gtmpl",
    events = {
      @EventConfig(listeners = UIPollForm.SaveActionListener.class), 
      @EventConfig(listeners = UIPollForm.RefreshActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPollForm.CancelActionListener.class,phase = Phase.DECODE)
    }
)
public class UIPollForm extends BaseForumForm implements UIPopupComponent {
  public static final String       FIELD_QUESTION_INPUT     = "Question";

  final static public String       FIELD_OPTIONS            = "Option";

  public static final String       FIELD_TIMEOUT_INPUT      = "TimeOut";

  public static final String       FIELD_AGAINVOTE_CHECKBOX = "VoteAgain";

  public static final String       FIELD_MULTIVOTE_CHECKBOX = "MultiVote";

  public static final String       COLON                      = ":";

  public static final String       DELETED                      = "deleted";

  public static final String       ZERO                       = "0.0";

  public static final int          MAX_TITLE                  = 100;

  private UIFormMultiValueInputSet uiFormMultiValue         = new UIFormMultiValueInputSet(FIELD_OPTIONS, FIELD_OPTIONS);

  private String                   TopicPath;

  private Poll                     poll                     = new Poll();

  private boolean                  isUpdate                 = false;

  public UIPollForm() throws Exception {
    UIFormStringInput question = new UIFormStringInput(FIELD_QUESTION_INPUT, FIELD_QUESTION_INPUT, null);
    UIFormStringInput timeOut = new UIFormStringInput(FIELD_TIMEOUT_INPUT, FIELD_TIMEOUT_INPUT, null);
    timeOut.addValidator(PositiveNumberFormatValidator.class);
    UICheckBoxInput VoteAgain = new UICheckBoxInput(FIELD_AGAINVOTE_CHECKBOX, FIELD_AGAINVOTE_CHECKBOX, false);
    UICheckBoxInput MultiVote = new UICheckBoxInput(FIELD_MULTIVOTE_CHECKBOX, FIELD_MULTIVOTE_CHECKBOX, false);
    addUIFormInput(question.addValidator(MandatoryValidator.class));
    addUIFormInput(timeOut);
    addUIFormInput(VoteAgain);
    addUIFormInput(MultiVote);
    setDefaulFall();
  }

  private void initMultiValuesField(List<String> list) throws Exception {
    if (uiFormMultiValue != null)
      removeChildById(FIELD_OPTIONS);
    uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, null, null);
    uiFormMultiValue.addValidator(MandatoryValidator.class);
    uiFormMultiValue.setId(FIELD_OPTIONS);
    uiFormMultiValue.setName(FIELD_OPTIONS);
    uiFormMultiValue.setType(UIFormStringInput.class);
    uiFormMultiValue.setValue(list);
    if (this.isUpdate) {
      uiFormMultiValue.resetListIndexItemRemoved();
      uiFormMultiValue.setMaxOld(list.size());
    }
    addUIFormInput(uiFormMultiValue);
  }

  public void setTopicPath(String topicPath) {
    this.TopicPath = topicPath;
  }

  protected String getDateAfter() throws Exception {
    Date date = new Date();
    if (poll != null && poll.getTimeOut() > 0) {
      date = poll.getModifiedDate();
    }
    String format = "MM-dd-yyyy";
    try {
      format = this.getAncestorOfType(UIForumPortlet.class).getUserProfile().getShortDateFormat();
    } catch (NullPointerException e) {
      format = getForumService().getDefaultUserProfile(UserHelper.getCurrentUser(), null).getShortDateFormat();
    }
    return TimeConvertUtils.getFormatDate(format, date);
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
      list.add(ForumUtils.EMPTY_STR);
      list.add(ForumUtils.EMPTY_STR);
    }
    this.initMultiValuesField(list);
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class SaveActionListener extends EventListener<UIPollForm> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIPollForm> event) throws Exception {
      UIPollForm uiForm = event.getSource();
      UIFormStringInput questionInput = uiForm.getUIStringInput(FIELD_QUESTION_INPUT);
      String question = questionInput.getValue();
      question = CommonUtils.encodeSpecialCharInTitle(question);
      String timeOutStr = uiForm.getUIStringInput(FIELD_TIMEOUT_INPUT).getValue();
      timeOutStr = ForumUtils.removeZeroFirstNumber(timeOutStr);
      long timeOut = 0;
      if (!ForumUtils.isEmpty(timeOutStr)) {
        if (timeOutStr.length() > 4) {
          uiForm.warning("UIPollForm.msg.longTimeOut", new String[] { uiForm.getLabel(FIELD_TIMEOUT_INPUT) }, false);
        }
        timeOut = Long.parseLong(timeOutStr);
      }
      boolean isAgainVote = uiForm.getUICheckBoxInput(FIELD_AGAINVOTE_CHECKBOX).isChecked();
      boolean isMultiVote = uiForm.getUICheckBoxInput(FIELD_MULTIVOTE_CHECKBOX).isChecked();
      String sms = ForumUtils.EMPTY_STR;
      List<UIComponent> childs = uiForm.uiFormMultiValue.getChildren();
      List<String> values = (List<String>) uiForm.uiFormMultiValue.getValue();
      List<String> values_ = new ArrayList<String>();
      if (childs.size() != values.size()) {
    	  uiForm.warning("UIPollForm.msg.FillAllOptions",false);
    	  return;
      }
      int i = 1;
      for (String value : values) {
        if (!ForumUtils.isEmpty(value)) {
          if (value.length() > MAX_TITLE) {
            String[] args = new String[] { uiForm.getLabel(FIELD_OPTIONS) + "(" + i + ")", String.valueOf(MAX_TITLE) };
            uiForm.warning("NameValidator.msg.warning-long-text", args);
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
      if (ForumUtils.isEmpty(question)) {
        sms = "NotQuestion";
        sizeOption = 0;
      } else {
        if (question.length() > MAX_TITLE) {
          String[] args = { uiForm.getLabel(FIELD_QUESTION_INPUT), String.valueOf(MAX_TITLE) };
          uiForm.warning("NameValidator.msg.warning-long-text", args);
          return;
        }
      }
      /*
       * sizeOption: size option newUser: list user after edit poll oldUserVote: list user before edit poll vote: % vote after edit poll oldVote: % old vote before edit poll voteRemoved: % vote removed rmPecent: % removed
       */
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
        uiForm.poll.setOwner(userName);
        uiForm.poll.setQuestion(question);
        uiForm.poll.setModifiedBy(userName);
        uiForm.poll.setIsAgainVote(isAgainVote);
        uiForm.poll.setIsMultiCheck(isMultiVote);
        uiForm.poll.setOption(options);
        uiForm.poll.setVote(vote);
        uiForm.poll.setTimeOut(timeOut);
        uiForm.poll.setIsClosed(uiForm.poll.getIsClosed());
        String[] id = uiForm.TopicPath.trim().split(ForumUtils.SLASH);
        try {
          PollService pollSv = (PollService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
          if (uiForm.isUpdate) {
            if (newUser != null) {
              uiForm.poll.setUserVote(newUser);
            }
            pollSv.savePoll(uiForm.poll, false, false);
          } else {
            uiForm.poll.setUserVote(new String[] {});
            uiForm.poll.setId(id[id.length - 1].replace(Utils.TOPIC, Utils.POLL));
            uiForm.poll.setParentPath(uiForm.TopicPath.trim());
            pollSv.savePoll(uiForm.poll, true, false);
          }
        } catch (Exception e) {
          uiForm.log.debug("Failed to save poll.", e);
        }
        uiForm.isUpdate = false;
        try {
          UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
          forumPortlet.cancelAction();
          UITopicDetailContainer detailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class);
          detailContainer.setRederPoll(true);
          detailContainer.getChild(UITopicPoll.class).updateFormPoll(id[id.length - 3], id[id.length - 2], id[id.length - 1]);
          detailContainer.getChild(UITopicDetail.class).hasPoll(true);
          event.getRequestContext().addUIComponentToUpdateByAjax(detailContainer);
        } catch (Exception e) {
          UIForumPollPortlet forumPollPortlet = uiForm.getAncestorOfType(UIForumPollPortlet.class);
          forumPollPortlet.cancelAction();
          forumPollPortlet.getChild(UITopicPoll.class).updateFormPoll(id[id.length - 3], id[id.length - 2], id[id.length - 1]);
          event.getRequestContext().addUIComponentToUpdateByAjax(forumPollPortlet);
        }
      }
      if (!ForumUtils.isEmpty(sms)) {
        uiForm.warning("UIPollForm.msg." + sms);
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
        list.add(ForumUtils.EMPTY_STR);
        list.add(ForumUtils.EMPTY_STR);
        uiForm.initMultiValuesField(list);
        uiForm.getUIStringInput(FIELD_QUESTION_INPUT).setValue(ForumUtils.EMPTY_STR);
        uiForm.getUIStringInput(FIELD_TIMEOUT_INPUT).setValue("0");
        uiForm.getUICheckBoxInput(FIELD_AGAINVOTE_CHECKBOX).setChecked(false);
        uiForm.getUICheckBoxInput(FIELD_MULTIVOTE_CHECKBOX).setChecked(false);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class CancelActionListener extends EventListener<UIPollForm> {
    public void execute(Event<UIPollForm> event) throws Exception {
      UIPollForm uiForm = event.getSource();
      try {
        UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.cancelAction();
      } catch (Exception e) {
        UIForumPollPortlet forumPollPortlet = uiForm.getAncestorOfType(UIForumPollPortlet.class);
        forumPollPortlet.cancelAction();
      }
      uiForm.isUpdate = false;
    }
  }
}
