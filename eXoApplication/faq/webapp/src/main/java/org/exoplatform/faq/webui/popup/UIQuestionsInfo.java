/*
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
 */
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.BaseUIFAQForm;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersPageIterator;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com 
 * May 15, 2008 ,4:09:44 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIQuestionsInfo.gtmpl", 
    events = {
        @EventConfig(listeners = UIQuestionsInfo.ChangeCategoryActionListener.class), 
        @EventConfig(listeners = UIQuestionsInfo.CloseActionListener.class), 
        @EventConfig(listeners = UIQuestionsInfo.EditQuestionActionListener.class), 
        @EventConfig(listeners = UIQuestionsInfo.DeleteQuestionActionListener.class), 
        @EventConfig(listeners = UIQuestionsInfo.ChangeTabActionListener.class), 
        @EventConfig(listeners = UIQuestionsInfo.ChangeQuestionStatusActionListener.class), 
        @EventConfig(listeners = UIQuestionsInfo.ResponseQuestionActionListener.class) 
    }
)
public class UIQuestionsInfo extends BaseUIFAQForm implements UIPopupComponent {
  private static final String            LIST_QUESTION_INTERATOR              = "FAQUserPageIteratorTab1";

  private static final String            LIST_QUESTION_NOT_ANSWERED_INTERATOR = "FAQUserPageIteratorTab2";

  private static final String            LIST_CATEGORIES                      = "ListCategories";

  private FAQSetting                     faqSetting_                          = new FAQSetting();

  private JCRPageList                    pageList;

  private JCRPageList                    pageListNotAnswer;

  private UIAnswersPageIterator          pageIterator;

  private UIAnswersPageIterator          pageQuesNotAnswerIterator;

  private List<Question>                 listQuestion_                        = new ArrayList<Question>();

  private List<Question>                 listQuestionNotYetAnswered_          = new ArrayList<Question>();

  private List<SelectItemOption<String>> listCategories                       = new ArrayList<SelectItemOption<String>>();

  private long                           pageSelect                           = 1;

  private long                           pageSelectNotAnswer                  = 1;

  private List<String>                   moderateCates                        = new ArrayList<String>();

  private boolean                        isEditTab_                           = true;

  private boolean                        isResponseTab_                       = false;

  private boolean                        isChangeTab_                         = false;

  private String                         cateId_                              = Utils.ALL;

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public UIQuestionsInfo() throws Exception {
    isEditTab_ = true;
    isResponseTab_ = false;
    addChild(UIAnswersPageIterator.class, null, LIST_QUESTION_INTERATOR);
    addChild(UIAnswersPageIterator.class, null, LIST_QUESTION_NOT_ANSWERED_INTERATOR);
    setActions(new String[] { "" });
  }

  public void setFAQSetting(FAQSetting setting) throws Exception {
    this.faqSetting_ = setting;
    FAQUtils.getEmailSetting(faqSetting_, false, false);
    setListCate();
    UIFormSelectBox selectCategory = new UIFormSelectBox(LIST_CATEGORIES, LIST_CATEGORIES, listCategories);
    selectCategory.setOnChange("ChangeCategory");
    this.addUIFormInput(selectCategory);
    setListQuestion();
  }

  protected boolean hasInGroup(List<String> listGroup, String[] listPermission) {
    for (String per : listPermission) {
      if (per != null && per.trim().length() > 0 && listGroup.contains(per))
        return true;
    }
    return false;
  }

  private void setListCate() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    this.listCategories.add(new SelectItemOption<String>(res.getString("UIQuestionsInfo.label.All"), Utils.ALL));
    FAQService faqService = (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);
    if (faqSetting_.isAdmin()) {
      List<Cate> listCate = faqService.listingCategoryTree();
      this.listCategories.add(new SelectItemOption<String>(getFAQService().getCategoryNameOf(Utils.CATEGORY_HOME), Utils.CATEGORY_HOME));
      for (Cate cat : listCate) {
        this.listCategories.add(new SelectItemOption<String>(cat.getCategory().getName(), cat.getCategory().getPath()));
      }
    } else {
      List<String> listCate = getFAQService().getListCateIdByModerator(FAQUtils.getCurrentUser());
      moderateCates.clear();
      for (String str : listCate) {
        try {
          int idxOfSemicolon = str.indexOf(CommonUtils.SEMICOLON) ;
          this.listCategories.add(new SelectItemOption<String>(str.substring(idxOfSemicolon + 1), str.substring(0, idxOfSemicolon)));
          moderateCates.add(str.substring(0, idxOfSemicolon));
        } catch (StringIndexOutOfBoundsException e) {
          if (str.indexOf(Utils.CATEGORY_HOME) == 0) {
            this.listCategories.add(new SelectItemOption<String>(str.substring(Utils.CATEGORY_HOME.length()), Utils.CATEGORY_HOME));
            moderateCates.add(Utils.CATEGORY_HOME);
          }
        } catch (Exception e) {
          log.error("Can not set List Category, exception: " + e.getMessage());
        }

      }
    }
  }

  protected String[] getQuestionActions() {
    return new String[] { "AddLanguage", "Attachment", "Save", "Close" };
  }

  protected String[] getQuestionNotAnsweredActions() {
    return new String[] { "QuestionRelation", "Attachment", "Save", "Close" };
  }

  protected String[] getTab() {
    return new String[] { "Question managerment", "Question not yet answered" };
  }

  protected boolean getIsEdit() {
    return isEditTab_;
  }

  protected boolean getIsResponse() {
    return isResponseTab_;
  }

  protected long getTotalpages(String pageInteratorId) {
    UIAnswersPageIterator pageIterator = this.getChildById(pageInteratorId);
    try {
      return pageIterator.getInfoPage().get(3);
    } catch (Exception e) {
      log.error("Can not get tatal pages, exception: " + e.getMessage());
      return 1;
    }
  }

  public void setListQuestion() throws Exception {
    listQuestion_.clear();
    listQuestionNotYetAnswered_.clear();
    pageIterator = this.getChildById(LIST_QUESTION_INTERATOR);
    pageQuesNotAnswerIterator = this.getChildById(LIST_QUESTION_NOT_ANSWERED_INTERATOR);
    if (faqSetting_.isAdmin()) {
      if (this.cateId_.equals(Utils.ALL)) {
        this.pageList = getFAQService().getAllQuestions();
        pageListNotAnswer = getFAQService().getQuestionsNotYetAnswer(Utils.ALL, false);
      } else {
        String cateId = cateId_;
        if (cateId.indexOf("/") > 0)
          cateId = cateId.substring(cateId.lastIndexOf("/") + 1);
        this.pageList = getFAQService().getAllQuestionsByCatetory(cateId, this.faqSetting_);
        pageListNotAnswer = getFAQService().getQuestionsNotYetAnswer(cateId, false);
      }
      this.pageList.setPageSize(5);
      pageIterator.updatePageList(this.pageList);

      pageListNotAnswer.setPageSize(5);
      pageQuesNotAnswerIterator.updatePageList(pageListNotAnswer);
    } else {
      List<String> listCateId = new ArrayList<String>();
      if (cateId_.equals(Utils.ALL)) {
        listCateId.addAll(moderateCates);
      } else {
        if (cateId_.indexOf("/") > 0)
          listCateId.add(cateId_.substring(cateId_.lastIndexOf("/") + 1));
        else
          listCateId.add(cateId_);
      }

      if (listCateId.size() > 0) {
        this.pageList = getFAQService().getQuestionsByListCatetory(listCateId, false);
        this.pageList.setPageSize(5);
        pageIterator.updatePageList(this.pageList);

        this.pageListNotAnswer = getFAQService().getQuestionsByListCatetory(listCateId, true);
        this.pageListNotAnswer.setPageSize(5);
        pageQuesNotAnswerIterator.updatePageList(this.pageListNotAnswer);
      }
    }
  }

  protected String getCategoryPath(String questionPath) {
    try {
      return getFAQService().getParentCategoriesName(questionPath.substring(0, questionPath.indexOf("/" + Utils.QUESTION_HOME)));
    } catch (Exception e) {
      log.error("Can not get category path, exception: " + e.getMessage());
      return questionPath;
    }
  }

  protected List<Question> getListQuestion() {
    if (!isChangeTab_) {
      pageSelect = pageIterator.getPageSelected();
      listQuestion_ = new ArrayList<Question>();
      try {
        listQuestion_.addAll(this.pageList.getPage(pageSelect, null));
        if (listQuestion_.isEmpty()) {
          UIAnswersPageIterator pageIterator = null;
          while (listQuestion_.isEmpty() && pageSelect > 1) {
            pageIterator = this.getChildById(LIST_QUESTION_INTERATOR);
            listQuestion_.addAll(this.pageList.getPage(--pageSelect, null));
            pageIterator.setSelectPage(pageSelect);
          }
        }
      } catch (Exception e) {
        log.error("Can not get list Question, exception: " + e.getMessage());
      }
    }
    isChangeTab_ = false;
    return listQuestion_;
  }

  /**
   * Gets the list question not answered.
   * 
   * @return the list question not answered
   */
  protected List<Question> getListQuestionNotAnswered() {
    if (!isChangeTab_) {
      pageSelectNotAnswer = pageQuesNotAnswerIterator.getPageSelected();
      listQuestionNotYetAnswered_.clear();
      try {
        listQuestionNotYetAnswered_.addAll(this.pageListNotAnswer.getPage(pageSelectNotAnswer, null));
        UIAnswersPageIterator pageIterator = null;
        while (listQuestionNotYetAnswered_.isEmpty() && pageSelectNotAnswer > 1) {
          pageIterator = this.getChildById(LIST_QUESTION_NOT_ANSWERED_INTERATOR);
          listQuestionNotYetAnswered_.addAll(this.pageListNotAnswer.getPage(--pageSelectNotAnswer, null));
          pageIterator.setSelectPage(pageSelectNotAnswer);
        }
      } catch (Exception e) {
        log.error("Can not get list Question not Answered, exception: " + e.getMessage());
      }
    }
    isChangeTab_ = false;
    return listQuestionNotYetAnswered_;
  }

  static public class EditQuestionActionListener extends BaseEventListener<UIQuestionsInfo> {
    @SuppressWarnings("static-access")
    public void onEvent(Event<UIQuestionsInfo> event, UIQuestionsInfo questionsInfo, String quesId) throws Exception {
      UIQuestionManagerForm questionManagerForm = questionsInfo.getAncestorOfType(UIQuestionManagerForm.class);
      try {
        Question question = questionsInfo.getFAQService().getQuestionById(quesId);
        UIQuestionForm questionForm = questionManagerForm.getChildById(questionManagerForm.UI_QUESTION_FORM);
        questionForm.setFAQSetting(questionsInfo.faqSetting_);
        questionForm.setIsChildOfManager(true);
        questionForm.setQuestion(question);
        questionManagerForm.isViewEditQuestion = true;
        questionManagerForm.isViewResponseQuestion = false;
        questionManagerForm.isEditQuestion = true;
      } catch (Exception e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));        
        for (int i = 0; i < questionsInfo.listQuestion_.size(); i++) {
          if (questionsInfo.listQuestion_.get(i).getId().equals(quesId)) {
            questionsInfo.listQuestion_.remove(i);
            break;
          }
        }
      }
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

  static public class ResponseQuestionActionListener extends BaseEventListener<UIQuestionsInfo> {
    public void onEvent(Event<UIQuestionsInfo> event, UIQuestionsInfo questionsInfo, String ids) throws Exception {
      String questionId = ids.substring(0, ids.lastIndexOf("/"));
      String language = ids.substring(ids.lastIndexOf("/") + 1);
      UIQuestionManagerForm questionManagerForm = questionsInfo.getAncestorOfType(UIQuestionManagerForm.class);
      try {
        Question question = questionsInfo.getFAQService().getQuestionById(questionId);
        boolean isModerateAnswer = questionsInfo.getFAQService().isModerateAnswer(question.getPath());
        UIResponseForm responseForm = questionManagerForm.getChildById(UIQuestionManagerForm.UI_RESPONSE_FORM);
        responseForm.setFAQSetting(questionsInfo.faqSetting_);
        responseForm.updateChildOfQuestionManager(true);
        responseForm.setModertator(true);
        /*
         * if(param.length == 1) responseForm.setQuestionId(question, null, isModerateAnswer) ; else
         */
        responseForm.setQuestionId(question, language, isModerateAnswer);
        questionManagerForm.isViewEditQuestion = false;
        questionManagerForm.isViewResponseQuestion = true;
        questionManagerForm.isResponseQuestion = true;
      } catch (Exception e) {
        warning("UIQuestions.msg.question-id-deleted", false);
        for (int i = 0; i < questionsInfo.listQuestion_.size(); i++) {
          if (questionsInfo.listQuestion_.get(i).getPath().equals(questionId)) {
            questionsInfo.listQuestion_.remove(i);
            break;
          }
        }
      }
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

  static public class DeleteQuestionActionListener extends BaseEventListener<UIQuestionsInfo> {
    public void onEvent(Event<UIQuestionsInfo> event, UIQuestionsInfo questionsInfo, String questionId) throws Exception {
      UIPopupContainer popupContainer = questionsInfo.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true);
      try {
        Question question = questionsInfo.getFAQService().getQuestionById(questionId);
        UIDeleteQuestion deleteQuestion = popupAction.activate(UIDeleteQuestion.class, 500);
        deleteQuestion.setQuestionId(question);
        deleteQuestion.setIsManagement(true);
        //deleteQuestion.setId("Confirm Delete");
        UIQuestionManagerForm questionManagerForm = questionsInfo.getParent();
        if (questionManagerForm.isEditQuestion) {
          UIQuestionForm questionForm = questionManagerForm.getChild(UIQuestionForm.class);
          questionForm.setIsMode(true);
          if (questionForm.getQuestionId().equals(questionId)) {
            questionManagerForm.isEditQuestion = false;
          }
        }
        if (questionManagerForm.isResponseQuestion) {
          UIResponseForm responseForm = questionManagerForm.getChild(UIResponseForm.class);
          if (responseForm.questionId_.equals(questionId)) {
            questionManagerForm.isResponseQuestion = false;
          }
        }
        // event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } catch (Exception e) {
        warning("UIQuestions.msg.question-id-deleted", false);
        for (int i = 0; i < questionsInfo.listQuestion_.size(); i++) {
          if (questionsInfo.listQuestion_.get(i).getId().equals(questionId)) {
            questionsInfo.listQuestion_.remove(i);
            break;
          }
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

  static public class ChangeCategoryActionListener extends EventListener<UIQuestionsInfo> {
    public void execute(Event<UIQuestionsInfo> event) throws Exception {
      UIQuestionsInfo questionsInfo = event.getSource();
      String cateId = ((UIFormSelectBox) questionsInfo.getChildById(LIST_CATEGORIES)).getValue();
      questionsInfo.cateId_ = cateId;
      questionsInfo.setListQuestion();
      UIQuestionManagerForm questionManagerForm = questionsInfo.getAncestorOfType(UIQuestionManagerForm.class);
      questionManagerForm.isResponseQuestion = false;
      questionManagerForm.isEditQuestion = false;
      UIPopupContainer popupContainer = questionsInfo.getAncestorOfType(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

  static public class CloseActionListener extends EventListener<UIQuestionsInfo> {
    public void execute(Event<UIQuestionsInfo> event) throws Exception {
      UIQuestionsInfo questionManagerForm = event.getSource();
      UIAnswersPortlet portlet = questionManagerForm.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class ChangeTabActionListener extends BaseEventListener<UIQuestionsInfo> {
    public void onEvent(Event<UIQuestionsInfo> event, UIQuestionsInfo questionsInfo, String idTab) throws Exception {
      UIQuestionManagerForm questionManagerForm = questionsInfo.getAncestorOfType(UIQuestionManagerForm.class);
      if (idTab.equals("0")) {
        questionsInfo.isEditTab_ = true;
        questionsInfo.isResponseTab_ = false;

        questionManagerForm.isViewEditQuestion = true;
        questionManagerForm.isViewResponseQuestion = false;
      } else {
        questionsInfo.isEditTab_ = false;
        questionsInfo.isResponseTab_ = true;

        questionManagerForm.isViewEditQuestion = false;
        questionManagerForm.isViewResponseQuestion = true;
      }
      questionsInfo.isChangeTab_ = true;
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

  static public class ChangeQuestionStatusActionListener extends BaseEventListener<UIQuestionsInfo> {
    public void onEvent(Event<UIQuestionsInfo> event, UIQuestionsInfo questionsInfo, String ids) throws Exception {
      String action = ids.substring(0, ids.indexOf("/"));
      String questionId = ids.substring(ids.indexOf("/") + 1);
      try {
        Question question = questionsInfo.getFAQService().getQuestionById(questionId);
        if (action.equals("approved")) {
          question.setApproved(!question.isApproved());
        } else {
          question.setActivated(!question.isActivated());
        }
        FAQUtils.getEmailSetting(questionsInfo.faqSetting_, false, true);
        questionsInfo.getFAQService().saveQuestion(question, false, questionsInfo.faqSetting_);
        UIAnswersPortlet portlet = questionsInfo.getAncestorOfType(UIAnswersPortlet.class);
        UIQuestions questions = portlet.findFirstComponentOfType(UIQuestions.class);
        questions.setDefaultLanguage();
        questions.updateCurrentQuestionList();
        if (question.getPath().equals(questions.viewingQuestionId_)) {
          questions.updateLanguageMap();
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(questions);
      } catch (Exception e) {
        questionsInfo.log.error("Can not Change Question Status, exception: " + e.getMessage());
        warning("UIQuestions.msg.question-id-deleted", false);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(questionsInfo.getAncestorOfType(UIPopupContainer.class));
    }
  }
}
