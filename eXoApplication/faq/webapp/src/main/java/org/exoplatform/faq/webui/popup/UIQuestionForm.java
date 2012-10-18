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
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.webui.BaseUIFAQForm;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.WebUIUtils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

/**
  * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com 
 * Aus 01, 2007 2:48:18 PM
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/templates/faq/webui/popup/UIQuestionForm.gtmpl", events = {
    @EventConfig(listeners = UIQuestionForm.SelectLanguageActionListener.class),
    @EventConfig(listeners = UIQuestionForm.DeleteLanguageActionListener.class, confirm = "UIQuestionForm.msg.DeleteQuestionInLanguage"),
    @EventConfig(listeners = UIQuestionForm.AttachmentActionListener.class),
    @EventConfig(listeners = UIQuestionForm.SaveActionListener.class),
    @EventConfig(listeners = UIQuestionForm.CancelActionListener.class),
    @EventConfig(listeners = UIQuestionForm.RemoveAttachmentActionListener.class) })
public class UIQuestionForm extends BaseUIFAQForm implements UIPopupComponent {
  public static final String             AUTHOR                 = "Author";

  public static final String             EMAIL_ADDRESS          = "EmailAddress";

  public static final String             QUESTION_CONTENT       = "QuestionTitle";

  public static final String             ALL_LANGUAGES          = "AllLanguages";

  public static final String             QUESTION_DETAIL        = "Question";

  public static final String             ATTACHMENTS            = "Attachment";

  public static final String             FILE_ATTACHMENTS       = "FileAttach";

  public static final String             REMOVE_FILE_ATTACH     = "RemoveFile";

  public static final String             IS_APPROVED            = "IsApproved";

  public static final String             IS_ACTIVATED           = "IsActivated";

  public static final String             DELETE_LANGUAGE_ACTION = "DeleteLanguage";

  private UIFormStringInput              inputAuthor          = null;

  private UIFormStringInput              inputEmailAddress    = null;

  private UIFormStringInput              inputQuestionContent = null;

  private UIFormWYSIWYGInput             inputQuestionDetail  = null;

  private UIFormSelectBox                selectLanguage       = null;

  private UICheckBoxInput   inputIsApproved      = null;

  private UICheckBoxInput   inputIsActivated     = null;

  private UIFormInputWithActions         inputAttachcment     = null;

  private Question                       question_            = null;

  private Map<String, List<ActionData>>  actionField_;

  private List<SelectItemOption<String>> listSystemLanguages  = new ArrayList<SelectItemOption<String>>();

  private List<FileAttachment>           listFileAttach_      = new ArrayList<FileAttachment>();

  private Map<String, QuestionLanguage>  mapLanguage          = new HashMap<String, QuestionLanguage>();

  private String                         categoryId_          = "";

  private String                         questionId_          = null;

  private String                         defaultLanguage_     = "";

  private String                         lastLanguage_        = "";

  private String                         author_              = "";

  private String                         email_               = "";

  private List<String>                   questionContents_    = new ArrayList<String>();

  private boolean                        isApproved_          = true;

  private boolean                        isActivated_         = true;

  private boolean                        isMode               = false;

  private boolean                        isChildOfManager     = false;

  private boolean                        isModerate           = false;

  private boolean                        isAddCheckBox        = false;

  private Locale                         currentLocale        = null;
  
  private String                         editLanguage         = null;

  protected boolean                      isRenderSelectLang   = false;

  private FAQSetting                     faqSetting_;
  
  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void setFAQSetting(FAQSetting faqSetting) {
    this.faqSetting_ = faqSetting;
  }
  
  public String getEditLanguage() {
    return editLanguage;
  }

  public void setEditLanguage(String editLanguage) {
    this.editLanguage = editLanguage;
  }

  public UIQuestionForm() throws Exception {
    isChildOfManager = false;
    listFileAttach_.clear();
    mapLanguage.clear();
    questionContents_.clear();

    listFileAttach_ = new ArrayList<FileAttachment>();
    actionField_ = new HashMap<String, List<ActionData>>();
    questionId_ = "";
    question_ = null;

    this.setActions(new String[] { "Save", "Cancel" });
  }

  public void refresh() throws Exception {
    listFileAttach_.clear();
  }

  private String capitalizeFirstLetter(String word) {
    if (word == null) {
      return null;
    }
    if (word.length() == 0) {
      return word;
    }
    StringBuilder result = new StringBuilder(word);
    result.replace(0, 1, result.substring(0, 1).toUpperCase());
    return result.toString();
  }

  public void setLanguages() throws Exception {
    Locale currentLocale = Util.getPortalRequestContext().getLocale();
    if (this.currentLocale == null || !this.currentLocale.getLanguage().equals(currentLocale.getLanguage())) {
      this.currentLocale = currentLocale;
      setListSystemLanguages();
    }
  }

  private void setListSystemLanguages() {
    listSystemLanguages.clear();
    List<String> languages = FAQUtils.getAllLanguages(this);
    if (languages.size() <= 1)
      isRenderSelectLang = false;
    else
      isRenderSelectLang = true;
    LocaleConfigService localeConfigService = getApplicationComponent(LocaleConfigService.class);
    String displyByLocal, lang;
    for (LocaleConfig localeConfig : localeConfigService.getLocalConfigs()) {
      lang = localeConfig.getLocale().getDisplayLanguage();
      displyByLocal = capitalizeFirstLetter(localeConfig.getLocale().getDisplayLanguage(currentLocale));
      if (lang.equals(defaultLanguage_))
        listSystemLanguages.add(new SelectItemOption<String>(displyByLocal + " (" + getLabel("default") + ") ", lang));
      else
        listSystemLanguages.add(new SelectItemOption<String>(displyByLocal, lang));
    }
  }

  public void initPage(boolean isEdit) throws Exception {
    setLanguages();
    inputAuthor = new UIFormStringInput(AUTHOR, AUTHOR, author_);
    if (author_.trim().length() > 0)
      inputAuthor.setReadOnly(true);
    inputEmailAddress = new UIFormStringInput(EMAIL_ADDRESS, EMAIL_ADDRESS, email_);
    inputQuestionContent = new UIFormStringInput(QUESTION_CONTENT, QUESTION_CONTENT, null);
    selectLanguage = new UIFormSelectBox(ALL_LANGUAGES, ALL_LANGUAGES, listSystemLanguages);
    if (!FAQUtils.isFieldEmpty(getEditLanguage())) {
      selectLanguage.setValue(getEditLanguage());
      selectLanguage.setSelectedValues(new String[] { getEditLanguage() });
    } else if (!FAQUtils.isFieldEmpty(defaultLanguage_)) {
      selectLanguage.setValue(defaultLanguage_);
      selectLanguage.setSelectedValues(new String[] { defaultLanguage_ });
    }
    selectLanguage.setOnChange("SelectLanguage");
    inputIsApproved = new UICheckBoxInput(IS_APPROVED, IS_APPROVED, false);
    inputIsActivated = new UICheckBoxInput(IS_ACTIVATED, IS_ACTIVATED, false);
    inputAttachcment = new UIFormInputWithActions(ATTACHMENTS);
    inputAttachcment.addUIFormInput(new UIFormInputInfo(FILE_ATTACHMENTS, FILE_ATTACHMENTS, null));
    try {
      inputAttachcment.setActionField(FILE_ATTACHMENTS, getActionList());
    } catch (Exception e) {
      log.error("Set Attachcments in to InputActachcment is fall, exception: " + e.getMessage());
    }

    inputQuestionDetail = new UIFormWYSIWYGInput(QUESTION_DETAIL, QUESTION_DETAIL, "");
    inputQuestionDetail.setFCKConfig(WebUIUtils.getFCKConfig());
    inputQuestionDetail.setToolBarName("Basic");
    if (!questionContents_.isEmpty()) {
      String input = questionContents_.get(0);
      if (input != null && input.indexOf("<p>") >= 0 && input.indexOf("</p>") >= 0) {
        input = input.replace("<p>", "");
        input = input.substring(0, input.lastIndexOf("</p>") - 1);
      }
      inputQuestionDetail.setValue(input);
    }
    addChild(inputQuestionContent);
    addChild(inputQuestionDetail);
    addChild(selectLanguage);
    addChild(inputAuthor);
    addChild(inputEmailAddress);
    isModerate = getFAQService().isModerateQuestion(getCategoryId());
    isAddCheckBox = false;
    if (getIsModerator()) {
      if (questionId_ != null && questionId_.trim().length() > 0) {
        addChild(inputIsApproved.setChecked(isApproved_));
        addChild(inputIsActivated.setChecked(isActivated_));
        isAddCheckBox = true;
      } else {
        if (isModerate) {
          addChild(inputIsApproved.setChecked(false));
          addChild(inputIsActivated.setChecked(true));
          isAddCheckBox = true;
        }
      }
    }
    addUIFormInput(inputAttachcment);
    if (question_ != null && !isEdit) {
      this.setListFileAttach(question_.getAttachMent());
      try {
        refreshUploadFileList();
      } catch (Exception e) {
        log.error("Refresh upload InputActachcment is fall, exception: " + e.getMessage());
      }
    }
  }

  private boolean getIsModerator() throws Exception {
    try {
      if (faqSetting_.isAdmin() || isMode) {
        isMode = true;
      } else
        isMode = getFAQService().isCategoryModerator(categoryId_, null);
    } catch (Exception e) {
      log.debug("Failed to get isModerator.", e);
    }
    return isMode;
  }

  public void setIsChildOfManager(boolean isChild) {

    isChildOfManager = isChild;
    this.removeChildById(AUTHOR);
    this.removeChildById(EMAIL_ADDRESS);
    this.removeChildById(QUESTION_CONTENT);
    this.removeChildById(QUESTION_DETAIL);
    this.removeChildById(ATTACHMENTS);
    this.removeChildById(IS_APPROVED);
    this.removeChildById(IS_ACTIVATED);
    this.removeChildById(ALL_LANGUAGES);
    listFileAttach_.clear();
    mapLanguage.clear();
  }

  public void setQuestion(Question question) throws Exception {
    questionId_ = question.getPath();
    categoryId_ = question.getCategoryId();
    try {
      question_ = question;
      defaultLanguage_ = question_.getLanguage();
      lastLanguage_ = defaultLanguage_;
      List<QuestionLanguage> questionLanguages = getFAQService().getQuestionLanguages(questionId_);
      for (QuestionLanguage questionLanguage : questionLanguages) {
        mapLanguage.put(questionLanguage.getLanguage(), questionLanguage);
      }
      isApproved_ = question_.isApproved();
      isActivated_ = question_.isActivated();
      initPage(false);
      UIFormStringInput authorQ = this.getChildById(AUTHOR);
      authorQ.setValue(question_.getAuthor());
      UIFormStringInput emailQ = this.getChildById(EMAIL_ADDRESS);
      emailQ.setValue(question_.getEmail());
      
      if(mapLanguage.containsKey(this.editLanguage)){
        inputQuestionDetail.setValue(mapLanguage.get(this.editLanguage).getDetail());
        inputQuestionContent.setValue(CommonUtils.decodeSpecialCharToHTMLnumber(mapLanguage.get(this.editLanguage).getQuestion()));        
      }else{
        inputQuestionContent.setValue(CommonUtils.decodeSpecialCharToHTMLnumber(question_.getQuestion()));
        inputQuestionDetail.setValue(question_.getDetail());
      }
    } catch (Exception e) {
      log.error("Set question is fall, exception: " + e.getMessage());
      initPage(false);
    }
  }

  public boolean isMode() {
    return isMode;
  }

  public void setIsMode(boolean isMode) {
    this.isMode = isMode;
  }

  public String getQuestionId() {
    return questionId_;
  }
  
  public void setDefaultLanguage(String defaultLanguage) {
    this.defaultLanguage_ = defaultLanguage;
  }

  public String getDefaultLanguage() {
    return this.defaultLanguage_;
  }

  protected String getAuthor() {
    return this.author_;
  }

  public void setAuthor(String author) {
    this.author_ = author;
  }

  protected String getEmail() {
    return this.email_;
  }

  public void setEmail(String email) {
    this.email_ = email;
  }

  private String getCategoryId() {
    return this.categoryId_;
  }

  public void setCategoryId(String categoryId) throws Exception {
    this.categoryId_ = categoryId;
    questionId_ = null;
    defaultLanguage_ = FAQUtils.getDefaultLanguage();
    lastLanguage_ = defaultLanguage_;
    initPage(false);
  }

  protected UIForm getParentForm() {
    return (UIForm) this.getParent();
  }

  public List<ActionData> getActionList() {
    List<ActionData> uploadedFiles = new ArrayList<ActionData>();
    for (FileAttachment attachdata : listFileAttach_) {
      ActionData uploadAction = new ActionData();
      uploadAction.setActionListener("Download");
      uploadAction.setActionParameter(attachdata.getPath());
      uploadAction.setActionType(ActionData.TYPE_ICON);
      uploadAction.setCssIconClass("AttachmentIcon"); // "AttachmentIcon ZipFileIcon"
      uploadAction.setActionName(attachdata.getName() + " (" + attachdata.getSize() + " B)");
      uploadAction.setShowLabel(true);
      uploadedFiles.add(uploadAction);
      ActionData removeAction = new ActionData();
      removeAction.setActionListener("RemoveAttachment");
      removeAction.setActionName(REMOVE_FILE_ATTACH);
      removeAction.setActionParameter(attachdata.getPath());
      removeAction.setCssIconClass("LabelLink");
      removeAction.setActionType(ActionData.TYPE_LINK);
      uploadedFiles.add(removeAction);
    }
    return uploadedFiles;
  }

  public void setListFileAttach(List<FileAttachment> listFileAttachment) {
    listFileAttach_.addAll(listFileAttachment);
  }

  public void setListFileAttach(FileAttachment fileAttachment) {
    listFileAttach_.add(fileAttachment);
  }

  protected List<FileAttachment> getListFile() {
    return listFileAttach_;
  }

  public void refreshUploadFileList() throws Exception {
    ((UIFormInputWithActions) this.getChildById(ATTACHMENTS)).setActionField(FILE_ATTACHMENTS, getActionList());
  }

  public void setActionField(String fieldName, List<ActionData> actions) throws Exception {
    actionField_.put(fieldName, actions);
  }

  public List<ActionData> getActionField(String fieldName) {
    return actionField_.get(fieldName);
  }

  static public class SelectLanguageActionListener extends BaseEventListener<UIQuestionForm> {
    public void onEvent(Event<UIQuestionForm> event, UIQuestionForm questionForm, String objectId) throws Exception {
      String language = questionForm.selectLanguage.getValue();
      String detail = questionForm.inputQuestionDetail.getValue();
      String question = questionForm.inputQuestionContent.getValue();
      if (!ValidatorDataInput.fckContentIsNotEmpty(detail)){
        detail = " ";
      }
      if (!ValidatorDataInput.fckContentIsNotEmpty(question)) {
        if (questionForm.mapLanguage.containsKey(questionForm.lastLanguage_)) {
          questionForm.mapLanguage.get(questionForm.lastLanguage_).setState(QuestionLanguage.DELETE);
        }
      } else {
        QuestionLanguage langObj = new QuestionLanguage();
        if (questionForm.mapLanguage.containsKey(questionForm.lastLanguage_)) {
          langObj = questionForm.mapLanguage.get(questionForm.lastLanguage_);
          langObj.setState(QuestionLanguage.EDIT);
        }
        langObj.setQuestion(question);
        langObj.setDetail(detail);
        langObj.setLanguage(questionForm.lastLanguage_);
        questionForm.mapLanguage.put(langObj.getLanguage(), langObj);
      }
      questionForm.lastLanguage_ = language;
      if (questionForm.mapLanguage.containsKey(language)) {
        questionForm.inputQuestionDetail.setValue(questionForm.mapLanguage.get(language).getDetail());
        questionForm.inputQuestionContent.setValue(questionForm.mapLanguage.get(language).getQuestion());
      } else {
        questionForm.inputQuestionDetail.setValue("");
        questionForm.inputQuestionContent.setValue("");
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(questionForm);
    }
  }

  static public class DeleteLanguageActionListener extends EventListener<UIQuestionForm> {
    public void execute(Event<UIQuestionForm> event) throws Exception {
      UIQuestionForm questionForm = event.getSource();
      questionForm.inputQuestionDetail.setValue("");
      questionForm.inputQuestionContent.setValue("");
      event.getRequestContext().addUIComponentToUpdateByAjax(questionForm);
    }
  }

  static public class SaveActionListener extends BaseEventListener<UIQuestionForm> {
    public void onEvent(Event<UIQuestionForm> event, UIQuestionForm questionForm, String objectId) throws Exception {
      try {
        boolean isNew = true;
        String author = questionForm.inputAuthor.getValue();
        String emailAddress = questionForm.inputEmailAddress.getValue();
        String questionContent = questionForm.inputQuestionContent.getValue();
        UICheckBoxInput inputIsApproved = questionForm.getUICheckBoxInput(IS_APPROVED);
        UICheckBoxInput inputIsActivated = questionForm.getUICheckBoxInput(IS_ACTIVATED);
        if (author == null || author.trim().length() < 1) {
          warning("UIQuestionForm.msg.author-is-null");
          return;
        } else if (FAQUtils.getCurrentUser() == null && UserHelper.getUserByUserId(author) != null) {
          warning("UIQuestionForm.msg.author-is-duplicate");
          return;
        }
        if (emailAddress == null || emailAddress.trim().length() < 1 || !FAQUtils.isValidEmailAddresses(emailAddress)) {
          warning("UIQuestionForm.msg.email-address-invalid");
          return;
        }
        String language = questionForm.selectLanguage.getValue();
        language = FAQUtils.isFieldEmpty(language) ? questionForm.defaultLanguage_ : language;
        // Duy Tu: Check require question content not empty.
        if (FAQUtils.isFieldEmpty(questionContent)) {
          if (language.equals(questionForm.defaultLanguage_)) {
            warning("UIQuestionForm.msg.default-question-null");
          } else {
            warning("UIQuestionForm.msg.mutil-language-question-null", new String[] { language });
          }
          return;
        }

        if (!language.equals(questionForm.defaultLanguage_)) {
          if (questionForm.mapLanguage.isEmpty() || questionForm.mapLanguage.get(questionForm.getDefaultLanguage()) == null) {
            warning("UIQuestionForm.msg.default-question-null");
            return;
          }
        }

        String questionDetail = questionForm.inputQuestionDetail.getValue();
        if (!ValidatorDataInput.fckContentIsNotEmpty(questionDetail))
          questionDetail = " ";
        if (!ValidatorDataInput.fckContentIsNotEmpty(questionContent)) {
          if (questionForm.mapLanguage.containsKey(language)) {
            questionForm.mapLanguage.get(language).setState(QuestionLanguage.DELETE);
          }
        }
        questionDetail = CommonUtils.encodeSpecialCharInContent(questionDetail);
        questionContent = CommonUtils.encodeSpecialCharInTitle(questionContent);

        Question question = questionForm.getQuestion();
        if(questionForm.questionId_ == null || questionForm.questionId_.trim().length() < 1) { //Add new question
          question = new Question() ;
          String catId = questionForm.getCategoryId();
          question.setCategoryId((catId.indexOf("/") > 0) ? catId.substring(catId.lastIndexOf("/") + 1) : catId);
          question.setCategoryPath(catId);
          question.setRelations(new String[] {""});
          questionForm.isModerate = questionForm.getFAQService().isModerateQuestion(catId);
        } else { // Edit question
          isNew = false ;
          String questionPath = question.getPath();
          String categorySubPath = questionPath.substring(0, questionPath.indexOf("/" + org.exoplatform.faq.service.Utils.QUESTION_HOME));
          questionForm.isModerate = questionForm.getFAQService().isModerateQuestion(categorySubPath);
        }

        question.setApproved(!questionForm.isModerate || (questionForm.isMode && isNew)) ;
        if(questionForm.isAddCheckBox && inputIsApproved != null && inputIsActivated != null){
          question.setApproved(inputIsApproved.isChecked()) ;
          question.setActivated(inputIsActivated.isChecked()) ;
        }

        question.setLanguage(questionForm.getDefaultLanguage());
        question.setAuthor(author);
        question.setEmail(emailAddress);
        if (language.equals(questionForm.defaultLanguage_)) {
          question.setQuestion(questionContent);
          question.setDetail(questionDetail);
        } else {
          question.setQuestion(questionForm.mapLanguage.get(questionForm.getDefaultLanguage()).getQuestion());
          question.setDetail(questionForm.mapLanguage.get(questionForm.getDefaultLanguage()).getDetail());
          QuestionLanguage otherLang = new QuestionLanguage();
          if (questionForm.mapLanguage.containsKey(language)) {
            otherLang = questionForm.mapLanguage.get(language);
            otherLang.setState(QuestionLanguage.EDIT);
          }
          otherLang.setQuestion(questionContent);
          otherLang.setDetail(questionDetail);
          otherLang.setLanguage(language);
          questionForm.mapLanguage.put(language, otherLang);
        }
        questionForm.mapLanguage.remove(question.getLanguage());
        question.setMultiLanguages(questionForm.mapLanguage.values().toArray(new QuestionLanguage[] {}));
        question.setAttachMent(questionForm.listFileAttach_);
        UIAnswersPortlet portlet = questionForm.getAncestorOfType(UIAnswersPortlet.class);
        UIQuestions questions = portlet.getChild(UIAnswersContainer.class).getChild(UIQuestions.class);
        // Create link by Vu Duy Tu.
        if (FAQUtils.isFieldEmpty(question.getLink())) {
          question.setLink(FAQUtils.getQuestionURI(question.getId(), false));
        }

        // For discuss in forum
        try {
          FAQUtils.getEmailSetting(questionForm.faqSetting_, isNew, !isNew);
          FAQSetting faqSetting = new FAQSetting();
          FAQUtils.getPorletPreference(faqSetting);
          if (faqSetting.getIsDiscussForum()) {
            String topicId = question.getTopicIdDiscuss();
            if (topicId != null && topicId.length() > 0) {
              try {
                ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
                Topic topic = (Topic) forumService.getObjectNameById(topicId, Utils.TOPIC);
                if (topic != null) {
                  String[] ids = topic.getPath().split("/");
                  int t = ids.length;
                  topic.setModifiedBy(FAQUtils.getCurrentUser());
                  topic.setTopicName(question.getQuestion());
                  topic.setDescription(question.getDetail());
                  topic.setIsApproved(!forumService.getForum(ids[t - 3], ids[t - 2]).getIsModerateTopic());
                  forumService.saveTopic(ids[t - 3], ids[t - 2], topic, false, false, new MessageBuilder());
                }
              } catch (Exception e) {
                questionForm.log.debug("Failed to get topic discussion.", e);
              }
            }
          }
          // end discuss
          if (questionForm.getFAQService().saveQuestion(question, isNew, questionForm.faqSetting_) == null) {
            warning("UIQuestions.msg.question-deleted", false);
            portlet.cancelAction();
            return;
          }
        } catch (Exception e) {
          questionForm.log.error("Can not run discuss question in to forum portlet", false);
        }
        if(!(questionForm.isModerate) || (inputIsApproved != null && inputIsApproved.isChecked())){
          if(isNew) info("UIQuestionForm.msg.add-new-question-successful", false) ;
        } else {
          info("UIQuestionForm.msg.question-not-is-approved", false) ;
        }

        if (!questionForm.isChildOfManager) {
          portlet.cancelAction();
        } else {
          UIQuestionManagerForm questionManagerForm = questionForm.getParent();
          UIResponseForm responseForm = questionManagerForm.getChild(UIResponseForm.class);
          if (questionManagerForm.isResponseQuestion && questionForm.getQuestionId().equals(responseForm.questionId_)) {
            responseForm.updateChildOfQuestionManager(true);
            responseForm.setModertator(true);
            responseForm.setQuestionId(question, "", !questionForm.getFAQService().isModerateAnswer(question.getPath()));
          }
          questionManagerForm.isEditQuestion = false;
          UIPopupContainer popupContainer = questionManagerForm.getParent();
          event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
        }
        // update question list in question container.
        questions.setDefaultLanguage();
        questions.updateCurrentQuestionList();
        if (!isNew && question.getPath().equals(questions.viewingQuestionId_)) {
          questions.updateLanguageMap();
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIAnswersContainer.class));
      } catch (Exception e) {
        questionForm.log.debug("Failed to save action creating question.", e);
      }
    }
  }

  static public class AttachmentActionListener extends EventListener<UIQuestionForm> {
    public void execute(Event<UIQuestionForm> event) throws Exception {
      UIQuestionForm questionForm = event.getSource();
      UIPopupContainer popupContainer = questionForm.getAncestorOfType(UIPopupContainer.class);
      UIAttachmentForm attachMentForm = questionForm.openPopup(popupContainer, UIAttachmentForm.class, 550, 0);
      attachMentForm.setIsChangeAvatar(false);
      attachMentForm.setNumberUpload(5);
    }
  }

  static public class RemoveAttachmentActionListener extends BaseEventListener<UIQuestionForm> {
    public void onEvent(Event<UIQuestionForm> event, UIQuestionForm questionForm, String attFileId) throws Exception {
      for (FileAttachment att : questionForm.listFileAttach_) {
        if (att.getId() != null) {
          if (att.getId().equals(attFileId)) {
            questionForm.listFileAttach_.remove(att);
            break;
          }
        } else {
          if (att.getPath().equals(attFileId)) {
            questionForm.listFileAttach_.remove(att);
            break;
          }
        }
      }
      questionForm.refreshUploadFileList();
      event.getRequestContext().addUIComponentToUpdateByAjax(questionForm);
    }
  }

  static public class CancelActionListener extends EventListener<UIQuestionForm> {
    public void execute(Event<UIQuestionForm> event) throws Exception {
      UIQuestionForm questionForm = event.getSource();
      if (!questionForm.isChildOfManager) {
        UIAnswersPortlet portlet = questionForm.getAncestorOfType(UIAnswersPortlet.class);
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } else {
        UIQuestionManagerForm questionManagerForm = questionForm.getParent();
        questionManagerForm.isEditQuestion = false;
        UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
        if (popupAction != null) {
          popupAction.deActivate();
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
      }
    }
  }

  public Question getQuestion() {
    return question_;
  }

}
