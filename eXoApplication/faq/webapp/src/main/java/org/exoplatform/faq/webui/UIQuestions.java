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
package org.exoplatform.faq.webui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.rendering.RenderHelper;
import org.exoplatform.faq.rendering.RenderingException;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.popup.UICategoryForm;
import org.exoplatform.faq.webui.popup.UICommentForm;
import org.exoplatform.faq.webui.popup.UIDeleteQuestion;
import org.exoplatform.faq.webui.popup.UIExportForm;
import org.exoplatform.faq.webui.popup.UIImportForm;
import org.exoplatform.faq.webui.popup.UIMoveQuestionForm;
import org.exoplatform.faq.webui.popup.UIPrintAllQuestions;
import org.exoplatform.faq.webui.popup.UIQuestionForm;
import org.exoplatform.faq.webui.popup.UIQuestionManagerForm;
import org.exoplatform.faq.webui.popup.UIResponseForm;
import org.exoplatform.faq.webui.popup.UISendMailForm;
import org.exoplatform.faq.webui.popup.UISettingForm;
import org.exoplatform.faq.webui.popup.UIViewUserProfile;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.WebUIUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.utils.TimeConvertUtils;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com 
 * Aus 01, 2007 2:48:18 PM
 */

@ComponentConfig(
    template = "app:/templates/faq/webui/UIQuestions.gtmpl", 
    events = { 
        @EventConfig(listeners = UIQuestions.DownloadAttachActionListener.class),
        @EventConfig(listeners = UIQuestions.DeleteCategoryActionListener.class, confirm = "UIQuestions.msg.confirm-delete-category"),
        @EventConfig(listeners = UIQuestions.ChangeStatusAnswerActionListener.class) , 
        @EventConfig(listeners = UIQuestions.AddCategoryActionListener.class),
        @EventConfig(listeners = UIQuestions.AddNewQuestionActionListener.class) , 
        @EventConfig(listeners = UIQuestions.SettingActionListener.class),
        @EventConfig(listeners = UIQuestions.QuestionManagamentActionListener.class) , 
        @EventConfig(listeners = UIQuestions.ViewQuestionActionListener.class),
        @EventConfig(listeners = UIQuestions.OpenQuestionActionListener.class) , 
        @EventConfig(listeners = UIQuestions.CloseQuestionActionListener.class),
        @EventConfig(listeners = UIQuestions.ViewUserProfileActionListener.class) , 
        @EventConfig(listeners = UIQuestions.ResponseQuestionActionListener.class),
        @EventConfig(listeners = UIQuestions.EditAnswerActionListener.class) , 
        @EventConfig(listeners = UIQuestions.EditQuestionActionListener.class),
        @EventConfig(listeners = UIQuestions.DeleteQuestionActionListener.class) , 
        @EventConfig(listeners = UIQuestions.MoveQuestionActionListener.class),
        @EventConfig(listeners = UIQuestions.SendQuestionActionListener.class) , 
        @EventConfig(listeners = UIQuestions.CommentQuestionActionListener.class),
        @EventConfig(listeners = UIQuestions.DeleteCommentActionListener.class, confirm = "UIQuestions.msg.confirm-delete-comment"),
        @EventConfig(listeners = UIQuestions.DeleteAnswerActionListener.class, confirm = "UIQuestions.msg.confirm-delete-answer"),
        @EventConfig(listeners = UIQuestions.UnVoteQuestionActionListener.class, confirm = "UIQuestions.msg.confirm-unvote-question"),
        @EventConfig(listeners = UIQuestions.CommentToAnswerActionListener.class) , 
        @EventConfig(listeners = UIQuestions.VoteQuestionActionListener.class),
        @EventConfig(listeners = UIQuestions.ChangeLanguageActionListener.class) , 
        @EventConfig(listeners = UIQuestions.SortAnswerActionListener.class),
        @EventConfig(listeners = UIQuestions.ExportActionListener.class) , 
        @EventConfig(listeners = UIQuestions.ImportActionListener.class) , 
        @EventConfig(listeners = UIQuestions.EditCategoryActionListener.class) , 
        @EventConfig(listeners = UIQuestions.VoteAnswerActionListener.class),
        @EventConfig(listeners = UIQuestions.PrintAllQuestionActionListener.class) , 
        @EventConfig(listeners = UIQuestions.DiscussForumActionListener.class) 
    }
)
public class UIQuestions extends UIContainer {
  private static Log                    log                   = ExoLogger.getLogger(UIQuestions.class);

  protected static String               SEARCH_INPUT          = "SearchInput";

  protected static String               COMMENT_ITER          = "CommentIter";

  protected static String               ANSWER_ITER           = "AnswerIter";

  public static final String            OBJECT_ITERATOR       = "object_iter";

  public static final String            OBJECT_BACK           = "/back";

  public static final String            OBJECT_LANGUAGE       = "/language=";

  public static final String            OBJECT_RELATION       = "/relation=";

  public FAQSetting                     faqSetting_           = null;

  private Map<String, Question>         questionMap_          = new LinkedHashMap<String, Question>();

  public JCRPageList                    pageList;

  private boolean                       canEditQuestion       = false;

  public Boolean                        isSortAnswerUp        = null;

  public String                         categoryId_           = null;

  public String                         viewingQuestionId_    = "";

  private String                        currentUser_          = "";

  private FAQService                    faqService_           = null;

  private Map<String, QuestionLanguage> languageMap           = new HashMap<String, QuestionLanguage>();

  public boolean                        isChangeLanguage      = false;

  public List<String>                   listLanguage          = new ArrayList<String>();

  public String                         backPath_             = "";

  public String                         language_             = FAQUtils.getDefaultLanguage();

  protected String                      discussId             = "";

  private String[]                      firstTollbar_         = new String[] { "AddNewQuestion", "QuestionManagament" };

  private String[]                      menuCateManager       = new String[] { "EditCategory", "AddCategory", "DeleteCategory", "Export", "Import", };

  private String[]                      userActionsCate_      = new String[] { "AddNewQuestion", "Watch" };

  private String[]                      moderatorActionQues_  = new String[] { "CommentQuestion", "ResponseQuestion", "EditQuestion", "DeleteQuestion", "MoveQuestion", "SendQuestion" };

  private String[]                      moderatorActionQues2_ = new String[] { "ResponseQuestion", "EditQuestion", "DeleteQuestion", "MoveQuestion", "SendQuestion" };

  private String[]                      userActionQues_       = new String[] { "CommentQuestion", "ResponseQuestion", "SendQuestion" };

  private String[]                      userActionQues2_      = new String[] { "SendQuestion" };

  private String[]                      userActionQues3_      = new String[] { "ResponseQuestion", "SendQuestion" };

  private String[]                      sizes_                = new String[] { "bytes", "KB", "MB" };

  private boolean                       isViewRootCate        = true;

  public boolean                        viewAuthorInfor       = false;

  private RenderHelper                  renderHelper          = new RenderHelper();

  public UIAnswersPageIterator          pageIterator          = null;

  public long                           pageSelect            = 0;
  
  public UIQuestions() throws Exception {
    backPath_ = null;
    this.categoryId_ = Utils.CATEGORY_HOME;
    currentUser_ = FAQUtils.getCurrentUser();
    addChild(UIAnswersPageIterator.class, null, OBJECT_ITERATOR);
    if (faqService_ == null)
      faqService_ = (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);
    if (FAQUtils.isFieldEmpty(getId()))
      setId("UIQuestions");
  }

  protected boolean isNotInSpace() {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    return (portletPref.getValue("SPACE_URL", null) != null) ? false : true;
  }

  private boolean isCategoryHome() {
    return (categoryId_ == null || categoryId_.equals(Utils.CATEGORY_HOME)) ? true : false;
  }

  public boolean isViewRootCate() {
    return isViewRootCate;
  }

  public void setViewRootCate() {
      boolean b = (boolean) ((UIAnswersContainer) getParent()).isRenderCategory(categoryId_);
      if (b != isViewRootCate) {
        isViewRootCate = b;
      }
      setListObject();
  }

  protected boolean isAddQuestion() {
    if (isViewRootCate && (currentUser_ != null || currentUser_ == null && faqSetting_.isEnableAnonymousSubmitQuestion())) {
      if (isCategoryHome() && !faqSetting_.isPostQuestionInRootCategory()) {
        return false;
      }
      return true;
    }
    return false;
  }

  public String getRSSLink() {
    String catepath = categoryId_.substring(categoryId_.lastIndexOf("/") + 1);
    return CommonUtils.getRSSLink("faq", getPortalName(), catepath);
  }

  public String getPortalName() {
    PortalContainer pcontainer = PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }

  public String getImageUrl(String imagePath) throws Exception {
    String url = "";
    try {
      url = CommonUtils.getImageUrl(imagePath);
    } catch (Exception e) {
      log.debug("Failed to get url of image.", e);
    }
    return url;
  }

  protected boolean isDiscussForum() throws Exception {
    return faqSetting_.getIsDiscussForum();
  }

  public void setListObject() {
    try {
      String objectId = null;
      if (pageList != null)
        objectId = pageList.getObjectId();
      if (isViewRootCate) {
        pageList = faqService_.getQuestionsByCatetory(this.categoryId_, this.faqSetting_);
        pageList.setPageSize(10);
        if (objectId != null && objectId.trim().length() > 0)
          pageList.setObjectId(objectId);
        pageIterator = this.getChildById(OBJECT_ITERATOR);
        pageIterator.updatePageList(pageList);
      } else {
        pageList = null;
      }
    } catch (Exception e) {
      log.debug("Failed to get list question in category.", e);
    }
  }

  protected Answer[] getPageListAnswer(String questionId) throws Exception {
    if (isSortAnswerUp == null) {
      return languageMap.get(language_).getAnswers();
    }
    Answer[] answers = languageMap.get(language_).getAnswers();
    if (isSortAnswerUp) {
      Arrays.sort(answers, new FAQUtils.VoteComparator(true));
    } else {
      Arrays.sort(answers, new FAQUtils.VoteComparator(false));
    }
    return answers;
  }

  protected Comment[] getPageListComment(String questionId) throws Exception {
    return languageMap.get(language_).getComments();
  }

  protected String[] getActionTollbar() {
    return firstTollbar_;
  }

  protected String[] getMenuCateManager() {
    return menuCateManager;
  }

  public FAQSetting getFAQSetting() {
    if (faqSetting_ == null) {
      faqSetting_ = new FAQSetting();
      FAQUtils.getPorletPreference(faqSetting_);
    }
    return faqSetting_;
  }

  protected String[] getActionCategoryWithUser() {
    if (currentUser_ != null)
      return userActionsCate_;
    else if (faqSetting_.isEnableAutomaticRSS())
      return new String[] { userActionsCate_[0], "RSSFAQ" };
    else
      return new String[] { userActionsCate_[0] };
  }

  protected String[] getActionQuestion() {
    return (canEditQuestion) ? ((faqSetting_.isEnanbleVotesAndComments()) ? moderatorActionQues_ : moderatorActionQues2_) : ((FAQUtils.isFieldEmpty(currentUser_)) ? userActionQues2_ : ((faqSetting_.isEnanbleVotesAndComments()) ? userActionQues_ : userActionQues3_));
  }

  public void updateCurrentQuestionList() throws Exception {
    questionMap_.clear();
    if (pageList != null) {
      pageSelect = pageIterator.getPageSelected();
      for (Question question : pageList.getPage(pageSelect, null)) {
        questionMap_.put(question.getId(), question);
      }
      pageSelect = this.pageList.getCurrentPage();
      pageIterator.setSelectPage(pageSelect);
    }
  }

  public void setFAQSetting(FAQSetting setting) {
    this.faqSetting_ = setting;
  }

  public void setFAQService(FAQService service) {
    faqService_ = service;
  }

  public void setLanguageView(String language) {
    this.language_ = language;
  }

  protected String getQuestionContent() {
    if (languageMap.containsKey(language_)) {
      return languageMap.get(language_).getQuestion();
    }
    return "";
  }

  protected Question getQuestionDetail() {
    Question question = new Question();
    if (languageMap.containsKey(language_)) {
      question.setDetail(languageMap.get(language_).getDetail());
    }
    return question;
  }

  protected void setIsModerators() throws Exception {
    canEditQuestion = isModerators(categoryId_);
  }

  private boolean isModerators(String categoryId) throws Exception {
    return (getFAQSetting().isAdmin() || faqService_.isCategoryModerator(categoryId, null)) ? true : false;
  }

  public String getVoteScore(Question question) {
    double vote = question.getMarkVote();
    vote = vote < 0 ? 0 : vote;
    DecimalFormat df = new DecimalFormat("0");
    return df.format(vote);
  }

  // should be check canVote in Question object
  protected boolean canVote(Question question) {
    if (question.getUsersVote() != null)
      for (String user : question.getUsersVote()) {
        if (user.contains(currentUser_ + "/"))
          return false;
      }
    return true;
  }

  public void setDefaultLanguage() {
    String language = FAQUtils.getDefaultLanguage();
    if (languageMap.containsKey(language) || FAQUtils.isFieldEmpty(language_))
      language_ = language;
  }

  protected String convertSize(long size) {
    String result = "";
    long residual = 0;
    int i = 0;
    while (size >= 1000) {
      i++;
      residual = size % 1024;
      size /= 1024;
    }
    if (residual > 500) {
      result = (size + 1) + " " + sizes_[i];
    } else {
      result = size + " " + sizes_[i];
    }
    return result;
  }

  protected Question[] getListQuestion() {
    try {
      updateCurrentQuestionList();
    } catch (Exception e) {
      log.debug("Failed to update current question list.", e);
    }
    return questionMap_.values().toArray(new Question[] {});
  }

  protected boolean getCanEditQuestion() {
    return this.canEditQuestion;
  }

  protected String getQuestionView() {
    return this.viewingQuestionId_;
  }

  protected String[] getQuestionLangauges(String questionPath) {
    return languageMap.keySet().toArray(new String[] {});
  }

  protected String getAvatarUrl(String userId) throws Exception {
    return FAQUtils.getUserAvatar(userId);
  }

  public String getCategoryId() {
    return this.categoryId_;
  }

  public void setCategoryId(String categoryId) {
    viewAuthorInfor = faqService_.isViewAuthorInfo(categoryId);
    this.categoryId_ = categoryId;
    setViewRootCate();
  }

  public void viewQuestion(Question question) throws Exception {
    if (!questionMap_.containsKey(question.getLanguage())) {
      List<QuestionLanguage> languages = faqService_.getQuestionLanguages(question.getPath());
      languageMap.clear();
      for (QuestionLanguage lang : languages) {
        languageMap.put(lang.getLanguage(), lang);
      }
      if (!questionMap_.containsKey(question.getId()))
        questionMap_.put(question.getLanguage(), question);
      viewingQuestionId_ = question.getPath();
    }
  }

  // update current language of viewing question
  public void updateCurrentLanguage() {
    if (viewingQuestionId_ != null && viewingQuestionId_.length() > 0) {
      try {
        languageMap.put(language_, faqService_.getQuestionLanguageByLanguage(viewingQuestionId_, language_));
      } catch (Exception e) {
        log.debug("Failed to update current language ", e);
      }
    } else
      languageMap.clear();
  }

  public void updateQuestionLanguageByLanguage(String questionPath, String language) throws Exception {
    try {
      languageMap.put(language, faqService_.getQuestionLanguageByLanguage(questionPath, language));
    } catch (Exception e) {
      log.debug("Failed to update map language by viewing question", e);
    }
  }

  public void updateLanguageMap() throws Exception {
    try {
      if (viewingQuestionId_ != null && viewingQuestionId_.length() > 0) {
        List<QuestionLanguage> languages = faqService_.getQuestionLanguages(viewingQuestionId_);
        languageMap.clear();
        for (QuestionLanguage lang : languages) {
          languageMap.put(lang.getLanguage(), lang);
        }
      }
    } catch (Exception e) {
      viewingQuestionId_ = "";
      log.debug("Failed to update map language by viewing question", e);
    }
  }
  
  protected Map<String, String> getQuestionRelation(List<String> questionIdLst) {
    Map<String, String> mapReturn = new LinkedHashMap<String, String>();
    try {
      mapReturn = faqService_.getRelationQuestion(questionIdLst);
    } catch (Exception e) {
      log.debug("Failed to get question relation", e);
    }
    return mapReturn;
  }

  protected String getBackPath() {
    return this.backPath_;
  }

  public String render(Object obj) throws RenderingException {
    if (obj instanceof Question)
      return renderHelper.renderQuestion((Question) obj);
    else if (obj instanceof Answer)
      return renderHelper.renderAnswer((Answer) obj);
    else if (obj instanceof Comment)
      return renderHelper.renderComment((Comment) obj);
    return "";
  }

  protected String calculateTimeMessageOfLastActivity(long time) {
    Calendar calendar = CommonUtils.getGreenwichMeanTime();
    calendar.setTimeInMillis(time);
    return TimeConvertUtils.convertXTimeAgo(calendar.getTime(), "EEE,MMM dd,yyyy", TimeConvertUtils.MONTH);
  }

  public boolean checkQuestionToView(Question question, WebuiRequestContext context) throws Exception {
    if (question != null && (question.isActivated() && question.isApproved()) || isModerators(question.getCategoryPath())) {
      return true;
    } else {
      context.getUIApplication().addMessage(new ApplicationMessage("UIQuestions.msg.question-pending", null, ApplicationMessage.WARNING));
      context.addUIComponentToUpdateByAjax(getAncestorOfType(UIAnswersContainer.class));
      return false;
    }
  }

  public void setLanguage(String language) {
    this.language_ = language;
  }
  
  public FAQService getFAQService(){
    return this.faqService_;
  }
  
  public String getLanguage(){
    return this.language_;
  }
  
 

  static public class DownloadAttachActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions question = event.getSource();
      event.getRequestContext().addUIComponentToUpdateByAjax(question);
    }
  }

  static public class AddCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions question = event.getSource();
      UIAnswersPortlet uiPortlet = question.getAncestorOfType(UIAnswersPortlet.class);
      String categoryId = question.getCategoryId();
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);      
      UIPopupContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupContainer.class, null, null);
      UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null);
      if (!FAQUtils.isFieldEmpty(categoryId)) {
        try {
          if (question.faqSetting_.isAdmin() || question.faqService_.isCategoryModerator(categoryId, null)) {
            uiPopupAction.activate(uiPopupContainer, 580, 500);
            uiPopupContainer.setId("SubCategoryForm");
            category.setParentId(categoryId);
            category.updateAddNew(true);
          } else {
            event.getRequestContext()
                 .getUIApplication()
                 .addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action",
                                                    null,
                                                    ApplicationMessage.WARNING));            
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
            return;
          }
        } catch (Exception e) {
          FAQUtils.findCateExist(question.faqService_, question.getAncestorOfType(UIAnswersContainer.class));
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted",
                                                                                         null,
                                                                                         ApplicationMessage.WARNING));          
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
          return;
        }
      } else {
        uiPopupAction.activate(uiPopupContainer, 540, 400);
        uiPopupContainer.setId("AddCategoryForm");
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  static public class AddNewQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class);
      if (!questions.faqService_.isExisting(questions.categoryId_)) {        
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));        
        UIAnswersContainer fAQContainer = questions.getAncestorOfType(UIAnswersContainer.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer);
        return;
      }
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null);
      String email = "";
      String name = "";
      String userName = FAQUtils.getCurrentUser();
      if (!FAQUtils.isFieldEmpty(userName)) {
        name = userName;
        email = FAQUtils.getEmailUser(null);
      }
      questionForm.setFAQSetting(questions.faqSetting_);
      questionForm.setAuthor(name);
      questionForm.setEmail(email);
      questionForm.setCategoryId(questions.categoryId_);
      questionForm.refresh();
      popupContainer.setId("AddQuestion");
      popupAction.activate(popupContainer, 900, 420);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class SettingActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions question = event.getSource();
      UIAnswersPortlet uiPortlet = question.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UISettingForm uiSetting = popupContainer.addChild(UISettingForm.class, null, null);
      uiSetting.setFaqSetting(question.faqSetting_);
      uiSetting.init();
      popupContainer.setId("CategorySettingForm");
      popupAction.activate(popupContainer, 480, 0);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class QuestionManagamentActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UIQuestionManagerForm questionManagerForm = popupContainer.addChild(UIQuestionManagerForm.class, null, null);
      popupContainer.setId("FAQQuestionManagerment");
      popupAction.activate(popupContainer, 900, 850);
      questionManagerForm.setFAQSetting(questions.faqSetting_);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class ExportActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      popupContainer.setId("FAQExportForm");
      UIExportForm exportForm = popupContainer.addChild(UIExportForm.class, null, null);
      popupAction.activate(popupContainer, 500, 200);
      exportForm.setObjectId(categoryId);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class EditCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersPortlet uiPortlet = questions.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);      
      try {
        if (questions.faqSetting_.isAdmin() || questions.canEditQuestion) {
          Category category = questions.faqService_.getCategoryById(categoryId);
          UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 540);
          uiPopupContainer.setId("EditCategoryForm");
          UICategoryForm uiCategoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null);
          uiCategoryForm.setParentId(category.getPath().replaceFirst(CommonUtils.SLASH + category.getId(), CommonUtils.EMPTY_STR));
          uiCategoryForm.updateAddNew(false);
          uiCategoryForm.setCategoryValue(category, true);
          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
        } else {
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action",
                                                  null,
                                                  ApplicationMessage.WARNING));          
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        }
      } catch (Exception e) {
        log.debug("Failed to edit category.", e);
        FAQUtils.findCateExist(questions.faqService_, questions.getAncestorOfType(UIAnswersContainer.class));
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));        
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
      }
    }
  }

  static public class VoteAnswerActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      String answerPath = objectId.substring(0, objectId.lastIndexOf("/"));
      String voteType = objectId.substring(objectId.lastIndexOf("/") + 1);
      boolean isUp = true;
      if (voteType.equals("down"))
        isUp = false;
      try {
        questions.faqService_.voteAnswer(answerPath, FAQUtils.getCurrentUser(), isUp);
        questions.updateCurrentLanguage();
        event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIAnswersContainer.class));
      } catch (Exception e) {
        questions.showMessageDeletedQuestion(event.getRequestContext());
      }
    }
  }

  static public class ImportActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class);
      String categoryId = questions.getCategoryId();
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      popupContainer.setId("FAQImportForm");
      UIImportForm importForm = popupContainer.addChild(UIImportForm.class, null, null);
      popupAction.activate(popupContainer, 500, 170);
      importForm.setCategoryId(categoryId);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class SortAnswerActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      if (questions.isSortAnswerUp == null) {
        questions.isSortAnswerUp = false;
      } else {
        questions.isSortAnswerUp = !questions.isSortAnswerUp;
      }
      questions.updateCurrentLanguage();
      event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIAnswersContainer.class));
    }
  }

  static public class ViewQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      WebuiRequestContext context = event.getRequestContext();
      String questionId = context.getRequestParameter(OBJECTID);
      String asn = context.getRequestParameter(Utils.ANSWER_NOW_PARAM);
      event.getSource().getAncestorOfType(UIAnswersPortlet.class)
                       .viewQuestionById(event.getRequestContext(), questionId, "true".equals(asn), true);
    }
  }

  static public class OpenQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions uiQuestions = event.getSource();
      WebuiRequestContext context = event.getRequestContext();
      String questionId = context.getRequestParameter(OBJECTID);
      String id = questionId.substring(questionId.lastIndexOf("/") + 1);
      Question question = uiQuestions.questionMap_.get(id);
      if (!uiQuestions.checkQuestionToView(question, context)) {
        return;
      }
      uiQuestions.language_ = question.getLanguage();
      uiQuestions.isSortAnswerUp = null;
      uiQuestions.backPath_ = "";
      uiQuestions.viewingQuestionId_ = questionId;
      uiQuestions.updateLanguageMap();
      context.addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIAnswersContainer.class));
    }
  }

  static public class CloseQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions uiQuestions = event.getSource();
      uiQuestions.isSortAnswerUp = null;
      uiQuestions.language_ = FAQUtils.getDefaultLanguage();
      uiQuestions.backPath_ = "";
      uiQuestions.viewingQuestionId_ = "";
      event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIAnswersContainer.class));
    }
  }

  /**
   * this function is pick up from <code>ResponseQuestionActionListener</code> for reuse in <code>ViewQuestionActionListener</code>
   * @param event
   * @param questionId
   * @throws Exception
   */
  public void processResponseQuestionAction(WebuiRequestContext context, String questionId) throws Exception {
    boolean isAnswerApproved = false;
    try {
      Question question = faqService_.getQuestionById(questionId);
      isAnswerApproved = !faqService_.isModerateAnswer(questionId);
      UIAnswersPortlet portlet = getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UIResponseForm responseForm = popupContainer.addChild(UIResponseForm.class, null, null);
      responseForm.setModertator(canEditQuestion);
      if (questionId.equals(viewingQuestionId_)) { // response for viewing question or not
        responseForm.setQuestionId(question, language_, isAnswerApproved);
      } else {
        responseForm.setQuestionId(question, "", isAnswerApproved);
      }
      responseForm.setFAQSetting(faqSetting_);
      popupContainer.setId("FAQResponseQuestion");
      popupAction.activate(popupContainer, 900, 500);
      context.addUIComponentToUpdateByAjax(popupAction);
    } catch (Exception e) {
      updateCurrentQuestionList();
      showMessageDeletedQuestion(context);
    }
  }

  static public class ResponseQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      WebuiRequestContext context = event.getRequestContext();
      ((UIQuestions) event.getSource()).processResponseQuestionAction(context, context.getRequestParameter(OBJECTID));
    }
  }

  static public class EditAnswerActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions uiQuestions = event.getSource();
      Question question = null;
      Answer answer = null;
      String answerId = event.getRequestContext().getRequestParameter(OBJECTID);
      try {
        question = uiQuestions.getFAQService().getQuestionById(uiQuestions.viewingQuestionId_);
        answer = uiQuestions.getFAQService().getAnswerById(uiQuestions.viewingQuestionId_, answerId, uiQuestions.language_);
      } catch (javax.jcr.PathNotFoundException e) {
        uiQuestions.showMessageDeletedQuestion(event.getRequestContext());
        return;
      } catch (Exception e) {
        log.debug("Failed to edit answer by Id: " + answerId, e);
      }
      UIAnswersPortlet portlet = uiQuestions.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UIResponseForm responseForm = popupContainer.addChild(UIResponseForm.class, null, null);
      responseForm.setModertator(uiQuestions.canEditQuestion);
      responseForm.setAnswerInfor(question, answer, uiQuestions.language_);
      responseForm.setFAQSetting(uiQuestions.faqSetting_);
      popupContainer.setId("FAQResponseQuestion");
      popupAction.activate(popupContainer, 900, 500);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class ViewUserProfileActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions question = event.getSource();
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      User user = UserHelper.getUserByUserId(userId);
      if (user != null) {
        UIAnswersPortlet portlet = question.getAncestorOfType(UIAnswersPortlet.class);
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
        UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
        UIViewUserProfile viewUserProfile = popupContainer.addChild(UIViewUserProfile.class, null, null);
        popupContainer.setId("ViewUserProfile");
        viewUserProfile.setUser(user);
        popupAction.activate(popupContainer, 680, 350);
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } else {
        event.getRequestContext().getUIApplication()
             .addMessage(new ApplicationMessage("UIQuestions.msg.user-is-not-exist", 
             new String[] {(userId.contains(Utils.DELETED)) ? userId.substring(0, userId.indexOf(Utils.DELETED)): userId},
             ApplicationMessage.WARNING));        
        return;
      }
    }
  }

  static public class EditQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions uiQuestions = event.getSource();
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersPortlet portlet = uiQuestions.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      Question question = null;
      try {
        question = uiQuestions.getFAQService().getQuestionById(questionId);
      } catch (Exception e) {
        uiQuestions.showMessageDeletedQuestion(event.getRequestContext());
        return;
      }
      UIQuestionForm uiQuestionForm = popupContainer.addChild(UIQuestionForm.class, null, null);
      uiQuestionForm.setFAQSetting(uiQuestions.faqSetting_);
      uiQuestionForm.setEditLanguage(uiQuestions.getLanguage());
      uiQuestionForm.setQuestion(question);
      popupContainer.setId("EditQuestion");
      popupAction.activate(popupContainer, 900, 450);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  private boolean checkExistingQuestion(WebuiRequestContext context, String questionId) throws Exception {
    if (!this.getFAQService().isExisting(questionId)) {
      showMessageDeletedQuestion(context);
      return false;
    }
    return true;
  }

  public void showMessageDeletedQuestion(WebuiRequestContext context) throws Exception {
    UIAnswersPortlet portlet = this.getAncestorOfType(UIAnswersPortlet.class);
    context.getUIApplication().addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING));    
    context.addUIComponentToUpdateByAjax(portlet);
  }

  static public class DeleteQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (questions.checkExistingQuestion(event.getRequestContext(), questionId)) {
        UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class);
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
        UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
        UIDeleteQuestion deleteQuestion = popupContainer.addChild(UIDeleteQuestion.class, null, null);
        deleteQuestion.setQuestionId(questions.getFAQService().getQuestionById(questionId));
        popupContainer.setId("FAQDeleteQuestion");
        popupAction.activate(popupContainer, 450, 250);
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      }
    }
  }

  static public class PrintAllQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      if (!questions.getFAQService().isExisting(questions.categoryId_)) {
        FAQUtils.findCateExist(questions.getFAQService(), questions.getAncestorOfType(UIAnswersContainer.class));        
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action",
                                                null,
                                                ApplicationMessage.WARNING));
        
        event.getRequestContext().addUIComponentToUpdateByAjax(portlet);
        return;
      }
      UIPrintAllQuestions uiPrintAll = popupContainer.addChild(UIPrintAllQuestions.class, null, null);
      uiPrintAll.setCategoryId(questions.categoryId_, questions.getFAQService(), questions.faqSetting_, questions.canEditQuestion);
      popupContainer.setId("FAQPrintAllQuestion");
      popupAction.activate(popupContainer, 800, 500);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class DeleteAnswerActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      if (questions.checkExistingQuestion(event.getRequestContext(), questions.viewingQuestionId_)) {
        String answerId = event.getRequestContext().getRequestParameter(OBJECTID);
        questions.getFAQService().deleteAnswerQuestionLang(questions.viewingQuestionId_, answerId, questions.language_);
        questions.updateCurrentLanguage();
        event.getRequestContext().addUIComponentToUpdateByAjax(questions);
      }
    }
  }

  static public class DeleteCommentActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      String commentId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (questions.checkExistingQuestion(event.getRequestContext(), questions.viewingQuestionId_)) {
        questions.getFAQService().deleteCommentQuestionLang(questions.viewingQuestionId_, commentId, questions.language_);
        questions.updateCurrentLanguage();
        event.getRequestContext().addUIComponentToUpdateByAjax(questions);
      }
    }
  }

  // approve comment become answer
  static public class CommentToAnswerActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      String commentId = event.getRequestContext().getRequestParameter(OBJECTID);
      try {
        Comment comment = questions.getFAQService().getCommentById(questions.viewingQuestionId_, commentId, questions.language_);
        if (comment != null) {
          Answer answer = new Answer();
          answer.setNew(true);
          answer.setResponses(comment.getComments());
          answer.setResponseBy(comment.getCommentBy());
          answer.setFullName(comment.getFullName());
          answer.setDateResponse(comment.getDateComment());
          answer.setMarksVoteAnswer(0);
          answer.setUsersVoteAnswer(null);
          answer.setActivateAnswers(true);
          answer.setApprovedAnswers(true);
          questions.getFAQService().saveAnswer(questions.viewingQuestionId_, answer, questions.language_);
          questions.getFAQService().deleteCommentQuestionLang(questions.viewingQuestionId_, commentId, questions.language_);
        } else {
          questions.showMessageDeletedQuestion(event.getRequestContext());
          return;
        }
      } catch (Exception e) {
        questions.showMessageDeletedQuestion(event.getRequestContext());
        return;
      }
      questions.setLanguageView(questions.language_);
      questions.updateCurrentLanguage();
      event.getRequestContext().addUIComponentToUpdateByAjax(questions);
    }
  }

  static public class CommentQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      String objIds = event.getRequestContext().getRequestParameter(OBJECTID);
      try {
        String questionId = objIds.substring(0, objIds.lastIndexOf("/"));
        String commentId = objIds.substring(objIds.lastIndexOf("/") + 1);
        if (commentId.indexOf("Question") >= 0) {
          questionId = objIds;
          commentId = "new";
        }
        UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class);
        if (questions.checkExistingQuestion(event.getRequestContext(), questionId)) {
          Question question = questions.getFAQService().getQuestionById(questionId);
          if (question != null) {
            UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
            UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
            UICommentForm commentForm = popupContainer.addChild(UICommentForm.class, null, null);
            commentForm.setInfor(question, commentId, questions.faqSetting_, questions.language_);
            popupContainer.setId("FAQCommentForm");
            popupAction.activate(popupContainer, 850, 500);
            event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
          } else {
            questions.showMessageDeletedQuestion(event.getRequestContext());
            return;
          }
        }
      } catch (Exception e) {
        log.debug("Failed to comment in question questionId: " + objIds);
      }
    }
  }

  static public class VoteQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (questions.checkExistingQuestion(event.getRequestContext(), questions.viewingQuestionId_)) {
        String userName = FAQUtils.getCurrentUser();
        int number = Integer.parseInt(objectId);
        questions.getFAQService().voteQuestion(questions.viewingQuestionId_, userName, number);
        Question question = questions.getFAQService().getQuestionById(questions.viewingQuestionId_);
        if (question != null) {
          if (questions.questionMap_.containsKey(question.getId())) {
            questions.questionMap_.put(question.getId(), question);
          } else if (questions.questionMap_.containsKey(question.getLanguage())) {
            questions.questionMap_.put(question.getLanguage(), question);
          }
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIAnswersContainer.class));
      }
    }
  }

  static public class UnVoteQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (questions.checkExistingQuestion(event.getRequestContext(), questionId)) {
        String userName = FAQUtils.getCurrentUser();
        questions.getFAQService().unVoteQuestion(questionId, userName);
        Question question = questions.getFAQService().getQuestionById(questionId);
        if (question != null) {
          if (questions.questionMap_.containsKey(question.getId())) {
            questions.questionMap_.put(question.getId(), question);
          } else if (questions.questionMap_.containsKey(question.getLanguage())) {
            questions.questionMap_.put(question.getLanguage(), question);
          }
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIAnswersContainer.class));
      }
    }
  }

  static public class MoveQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource();
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (questions.checkExistingQuestion(event.getRequestContext(), questionId)) {
        UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class);
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
        UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
        UIMoveQuestionForm moveQuestionForm = popupContainer.addChild(UIMoveQuestionForm.class, null, null);
        moveQuestionForm.setQuestionId(questionId);
        popupContainer.setId("FAQMoveQuestion");
        moveQuestionForm.setFAQSetting(questions.faqSetting_);
        popupAction.activate(popupContainer, 600, 400);
        moveQuestionForm.updateSubCategory();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      }
    }
  }

  static public class SendQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions uiQuestions = event.getSource();
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (questionId.indexOf("/true") > 0) {
        questionId = questionId.replace("/true", "");
      }
      if (uiQuestions.checkExistingQuestion(event.getRequestContext(), questionId)) {
        UIAnswersPortlet portlet = uiQuestions.getAncestorOfType(UIAnswersPortlet.class);
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
        UIPopupContainer watchContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
        UISendMailForm sendMailForm = watchContainer.addChild(UISendMailForm.class, null, null);
        if (!questionId.equals(uiQuestions.viewingQuestionId_) || FAQUtils.isFieldEmpty(uiQuestions.language_))
          sendMailForm.setUpdateQuestion(questionId, "");
        else
          sendMailForm.setUpdateQuestion(questionId, uiQuestions.language_);
        watchContainer.setId("FAQSendMailForm");
        popupAction.activate(watchContainer, 700, 0);
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      }
    }
  }

  // switch language
  static public class ChangeLanguageActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions uiQuestions = event.getSource();
      uiQuestions.language_ = event.getRequestContext().getRequestParameter(OBJECTID);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions);
    }
  }

  // approve/activate
  static public class ChangeStatusAnswerActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions uiQuestions = event.getSource();
      String[] param = event.getRequestContext().getRequestParameter(OBJECTID).split("/");
      String questionId = uiQuestions.viewingQuestionId_;
      String language = uiQuestions.language_;
      String answerId = param[0];
      String action = param[1];
      try {
        if (language == null || language.equals(""))
          language = FAQUtils.getDefaultLanguage();
        QuestionLanguage questionLanguage = uiQuestions.languageMap.get(language);
        for (Answer answer : questionLanguage.getAnswers()) {
          if (answer.getId().equals(answerId)) {
            if (action.equals("Activate"))
              answer.setActivateAnswers(!answer.getActivateAnswers());
            else
              answer.setApprovedAnswers(!answer.getApprovedAnswers());
            uiQuestions.getFAQService().saveAnswer(questionId, answer, language);
            break;
          }
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIAnswersContainer.class));
      } catch (Exception e) {
        uiQuestions.showMessageDeletedQuestion(event.getRequestContext());
        return;
      }
    }
  }

  static public class DiscussForumActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions uiForm = event.getSource();
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersPortlet portlet = uiForm.getAncestorOfType(UIAnswersPortlet.class);
      FAQUtils.getPorletPreference(uiForm.faqSetting_);
      String forumId = uiForm.faqSetting_.getIdNameCategoryForum();
      forumId = forumId.substring(0, forumId.indexOf(";"));
      ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
      String categoryId;
      try {
        Forum forum = (Forum) forumService.getObjectNameById(forumId, org.exoplatform.forum.service.Utils.FORUM);
        String[] paths = forum.getPath().split("/");
        categoryId = paths[paths.length - 2];
        Topic topic = new Topic();
        String topicId = topic.getId();
        uiForm.discussId = topicId;
        String link = FAQUtils.getLinkDiscuss(topicId);
        Question question = uiForm.getFAQService().getQuestionById(questionId);
        String userName = question.getAuthor();
        String remoteAddr = WebUIUtils.getRemoteIP();
        if (UserHelper.getUserByUserId(userName) == null) {
          String temp = userName;
          String listMode[] = uiForm.getFAQService().getModeratorsOf(question.getPath());
          if (listMode != null && listMode.length > 0) {
            List<String> modes = FAQServiceUtils.getUserPermission(listMode);
            if (modes.size() > 0) {
              userName = modes.get(0);
            } else {
              List<String> listAdmin = uiForm.getFAQService().getAllFAQAdmin();
              userName = listAdmin.get(0);
            }
          } else {
            List<String> listAdmin = uiForm.getFAQService().getAllFAQAdmin();
            userName = listAdmin.get(0);
          }
          if (userName.equals(temp)) {
            userName = "user";
          }
        }
        topic.setOwner(userName);
        topic.setTopicName(question.getQuestion());
        topic.setDescription(question.getDetail());
        topic.setIcon("IconsView");
        topic.setIsModeratePost(true);
        topic.setLink(link);
        topic.setRemoteAddr(remoteAddr);
        topic.setIsApproved(!forum.getIsModerateTopic());
        topic.setCanView(new String[] { "" });
        forumService.saveTopic(categoryId, forumId, topic, true, false, new MessageBuilder());
        uiForm.getFAQService().saveTopicIdDiscussQuestion(questionId, topicId);
        Post post = new Post();

        Answer[] answers = question.getAnswers();
        if (answers != null && answers.length > 0) {
          for (int i = 0; i < answers.length; ++i) {
            post = new Post();
            post.setIcon("IconsView");
            post.setName("Re: " + question.getQuestion());
            post.setMessage(answers[i].getResponses());
            post.setOwner(answers[i].getResponseBy());
            post.setLink(link);
            post.setIsApproved(false);
            post.setRemoteAddr(remoteAddr);
            forumService.savePost(categoryId, forumId, topicId, post, true, new MessageBuilder());
            answers[i].setPostId(post.getId());
            answers[i].setNew(true);
            if (answers[i].getLanguage() == null)
              answers[i].setLanguage(question.getLanguage());
          }
          uiForm.getFAQService().saveAnswer(questionId, answers);
        }

        Comment[] comments = question.getComments();
        for (int i = 0; i < comments.length; ++i) {
          post = new Post();
          post.setIcon("IconsView");
          post.setName("Re: " + question.getQuestion());
          post.setMessage(comments[i].getComments());
          post.setOwner(comments[i].getCommentBy());
          post.setLink(link);
          post.setIsApproved(false);
          post.setRemoteAddr(remoteAddr);
          forumService.savePost(categoryId, forumId, topicId, post, true, new MessageBuilder());
          comments[i].setPostId(post.getId());
          uiForm.getFAQService().saveComment(questionId, comments[i], false);
        }
        uiForm.updateCurrentQuestionList();
      } catch (Exception e) {
        uiForm.discussId = "";
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIQuestions.msg.Discuss-forum-fall",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet);
    }
  }

  static public class DeleteCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions uiQuestions = event.getSource();
      String categoryId = uiQuestions.getCategoryId(); // event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersPortlet uiPortlet = uiQuestions.getAncestorOfType(UIAnswersPortlet.class);      
      UICategories categories = uiPortlet.findFirstComponentOfType(UICategories.class);
      try {
        Category cate = uiQuestions.getFAQService().getCategoryById(categoryId);
        if (uiQuestions.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
          uiQuestions.getFAQService().removeCategory(categoryId);
          uiQuestions.updateCurrentQuestionList();
          if (categoryId.indexOf("/") > 0)
            categoryId = categoryId.substring(0, categoryId.lastIndexOf("/"));
          else
            categoryId = Utils.CATEGORY_HOME;
          UIBreadcumbs breadcumbs = uiPortlet.findFirstComponentOfType(UIBreadcumbs.class);
          breadcumbs.setUpdataPath(categoryId);
          categories.setPathCategory(categoryId);
          uiQuestions.setCategoryId(categoryId);
        } else {
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action",
                                                  null,
                                                  ApplicationMessage.WARNING));          
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
      } catch (Exception e) {
        FAQUtils.findCateExist(uiQuestions.getFAQService(), uiQuestions.getAncestorOfType(UIAnswersContainer.class));
        uiQuestions.showMessageDeletedQuestion(event.getRequestContext());
      }
    }
  }
}