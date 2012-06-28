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
package org.exoplatform.faq.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.faq.service.*;
import org.exoplatform.ks.bbcode.core.BBCodeServiceImpl;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.list;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 04, 2008  
 */
@ManagedBy(FAQServiceManaged.class)
public class FAQServiceImpl implements FAQService, Startable {

  public static final int               CATEGORY   = 1;

  public static final int               QUESTION   = 2;

  public static final int               SEND_EMAIL = 1;

  private DataStorage                   jcrData_;

  private MultiLanguages                multiLanguages_;

  private BBCodeServiceImpl             bbcodeObject_;

  private TemplatePlugin                template_;

  private ConfigurationManager          configManager_;

  // private EmailNotifyPlugin emailPlugin_ ;
  private Collection<InitialDataPlugin> initDataPlugins;

  private KSDataLocation                locator;

  FAQServiceManaged                     managed;                                               // will be automatically set at @ManagedBy processing

  private static Log                    log        = ExoLogger.getLogger(FAQServiceImpl.class);

  protected List<AnswerEventListener>   listeners_ = new ArrayList<AnswerEventListener>(3);

  public FAQServiceImpl(InitParams params, KSDataLocation locator, DataStorage storage, ConfigurationManager configManager) throws Exception {
    configManager_ = configManager;
    multiLanguages_ = new MultiLanguages();
    initDataPlugins = new ArrayList<InitialDataPlugin>();
    this.locator = locator;
    storage.init(locator);
    jcrData_ = storage;
    bbcodeObject_ = new BBCodeServiceImpl();
  }

  public void addPlugin(ComponentPlugin plugin) throws Exception {
    jcrData_.addPlugin(plugin);
  }

  public void addRolePlugin(ComponentPlugin plugin) throws Exception {
    jcrData_.addRolePlugin(plugin);
  }

  public void addInitialDataPlugin(InitialDataPlugin plugin) throws Exception {
    initDataPlugins.add(plugin);
  }

  public void addTemplatePlugin(ComponentPlugin plugin) throws Exception {
    if (plugin instanceof TemplatePlugin)
      template_ = (TemplatePlugin) plugin;
  }

  public void start() {
    log.info("initializing FAQ default data...");
    try {
      jcrData_.initRootCategory();
    } catch (Exception e) {
      throw new RuntimeException("Error while initializing the root category", e);
    }

    for (InitialDataPlugin plugin : initDataPlugins) {
      try {
        if (plugin.importData(this, configManager_)) {
          log.info("imported plugin " + plugin);
        }
      } catch (Exception e) {
        log.error("Error while initializing Data plugin " + plugin.getName(), e);
      }
    }

    try {
      log.info("initializing FAQ template...");
      initViewerTemplate();
    } catch (Exception e) {
      log.error("Error while initializing FAQ template", e);
    }

    // management views
   /*try {
      log.info("initializing management view...");
      // Note: call FAQServiceManaged to register mgmt beans
    } catch (Exception e) {
      log.error("Error while initializing Management view: " + e.getMessage());
    }*/

    try {
      log.info("initializing Question Node listeners...");
      jcrData_.reInitQuestionNodeListeners();
    } catch (Exception e) {
      log.error("Error while initializing Question Node listeners", e);
    }

  }

  public void stop() {
  }

  @SuppressWarnings("deprecation")
  private void initViewerTemplate() throws Exception {
    if (template_ == null) {
      log.warn("No default template was configured for FAQ.");
      return;
    }
    SessionProvider provider = CommonUtils.createSystemProvider();
    if (!locator.getSessionManager().getSession(provider).getRootNode()
                .hasNode(locator.getFaqTemplatesLocation() + "/" + Utils.UI_FAQ_VIEWER)) {
      InputStream in = configManager_.getInputStream(template_.getPath());
      byte[] data = new byte[in.available()];
      in.read(data);
      saveTemplate(new String(data));
    }
    configManager_ = null;
    template_ = null;
  }

  /**
   * This method get all admin in FAQ
   * @return userName list
   * @throws Exception the exception
   */
  public List<String> getAllFAQAdmin() throws Exception {
    return jcrData_.getAllFAQAdmin();
  }

  /**
   * This method get all the category
   * @param   sProvider is session provider
   * @return Category list
   * @throws Exception the exception
   */
  public List<Category> getAllCategories(SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getAllCategories();
  }

  public List<Category> getAllCategories() throws Exception {
    return jcrData_.getAllCategories();
  }

  /**
   * This method get all the question 
   * and convert to list of question object (QuestionPageList)
   * @param    sProvider
   * @return   QuestionPageList
   * @throws Exception the exception
   */
  public QuestionPageList getAllQuestions(SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getAllQuestions();
  }

  public QuestionPageList getAllQuestions() throws Exception {
    return jcrData_.getAllQuestions();
  }

  /**
   * This method get all question node have not yet answer 
   * and convert to list of question object (QuestionPageList)
   * @param    sProvider
   * @return  QuestionPageList
   * @throws Exception the exception
   */
  public QuestionPageList getQuestionsNotYetAnswer(SessionProvider sProvider, String categoryId, FAQSetting setting) throws Exception {
    sProvider.close();
    return getQuestionsNotYetAnswer(categoryId, FAQSetting.DISPLAY_APPROVED.equals(setting.getDisplayMode()));
  }

  public QuestionPageList getQuestionsNotYetAnswer(String categoryId, boolean isApproved) throws Exception {
    return jcrData_.getQuestionsNotYetAnswer(categoryId, isApproved);
  }

  /**
   *This method get some informations of category: 
   *to count sub-categories, to count questions, to count question have not yet answer,
   *to count question is not approved are contained in this category
   * 
   * @param   categoryId
   * @param   sProvider
   * @return  number of (sub-categories, questions, questions is not approved,question is have not yet answered)
   * @throws Exception the exception
   */
  public long[] getCategoryInfo(String categoryId, SessionProvider sProvider, FAQSetting setting) throws Exception {
    sProvider.close();
    return getCategoryInfo(categoryId, setting);
  }

  public long[] getCategoryInfo(String categoryId, FAQSetting setting) throws Exception {
    return jcrData_.getCategoryInfo(categoryId, setting);
  }

  /**
   * Returns an category that can then be get property of this category.
   * <p>
   *
   * @param    categoryId is address id of the category so you want get
   * @param    sProvider
   * @return  category is id equal categoryId
   * @see     current category
   * @throws Exception the exception
   */
  public Category getCategoryById(String categoryId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getCategoryById(categoryId);
  }

  public Category getCategoryById(String categoryId) throws Exception {
    return jcrData_.getCategoryById(categoryId);
  }

  /**
   * This method should get question node via identify
   * @param   question identify
   * @param    sProvider
   * @return   Question
   * @throws Exception the exception 
   */
  public Question getQuestionById(String questionId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getQuestionById(questionId);
  }

  public Comment[] getComments(String questionId) throws Exception {
    return jcrData_.getComments(questionId);
  }

  public Question getQuestionById(String questionId) throws Exception {
    return jcrData_.getQuestionById(questionId);
  }

  /**
   * This method should view questions, only question node is activated and approved  via category identify
   * and convert to list of question object
   * 
   * @param    Category identify
   * @param    sProvider
   * @return   QuestionPageList
   * @throws Exception the exception
   */
  public QuestionPageList getQuestionsByCatetory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
    sProvider.close();
    return getQuestionsByCatetory(categoryId, faqSetting);
  }

  public QuestionPageList getQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
    return jcrData_.getQuestionsByCatetory(categoryId, faqSetting);
  }

  /**
   * This method get all questions via category identify and convert to list of question list
   * 
   * @param   Category identify
   * @param    sProvider
   * @return   QuestionPageList
   * @throws Exception the exception
   */
  public QuestionPageList getAllQuestionsByCatetory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
    sProvider.close();
    return getAllQuestionsByCatetory(categoryId, faqSetting);
  }

  public QuestionPageList getAllQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
    return jcrData_.getAllQuestionsByCatetory(categoryId, faqSetting);
  }

  /**
   * This method every category should get list question, all question convert to list of question object
   * @param   listCategoryId  is list via identify
   * @param   isNotYetAnswer  if isNotYetAnswer equal true then return list question is not yet answer
   *           isNotYetAnswer  if isNotYetAnswer equal false then return list all questions
   * @param    sProvider
   * @return   QuestionPageList
   * @throws Exception the exception
   */
  public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getQuestionsByListCatetory(listCategoryId, isNotYetAnswer);
  }

  public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
    return jcrData_.getQuestionsByListCatetory(listCategoryId, isNotYetAnswer);
  }
  // NOTE: this function not use in ks-2.0 and more
  public String getCategoryPathOfQuestion(String questionPath, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getCategoryPathOfQuestion(questionPath);
  }
  // NOTE: this function not use in ks-2.2.4 and more
  public String getCategoryPathOfQuestion(String questionPath) throws Exception {
    return jcrData_.getCategoryPathOfQuestion(questionPath);
  }

  /**
   * This method should lookup languageNode of question
   * and find all child node of language node
   * 
   * @param   Question identify
   * @param    sProvider
   * @return   language list
   * @throws Exception the exception
   */
  public List<QuestionLanguage> getQuestionLanguages(String questionId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getQuestionLanguages(questionId);
  }

  public List<QuestionLanguage> getQuestionLanguages(String questionId) {
    return jcrData_.getQuestionLanguages(questionId);
  }

  /**
   * This method should lookup languageNode of question
   * so find child node of language node is searched
   * and find properties of child node, if contain input of user, get this question
   * 
   * @param   Question list
   * @param   langage want search
   * @param   term content want search in all field question
   * @param    sProvider 
   * @return   Question list
   * @throws Exception the exception
   */
  /*
   * public List<Question> searchQuestionByLangageOfText(List<Question> listQuestion, String languageSearch, String text, SessionProvider sProvider) throws Exception { sProvider.close() ; return searchQuestionByLangageOfText(listQuestion, languageSearch, text) ; } public List<Question> searchQuestionByLangageOfText(List<Question> listQuestion, String languageSearch, String text) throws Exception {
   * return jcrData_.searchQuestionByLangageOfText(listQuestion, languageSearch, text) ; }
   */

  /**
   * This method should lookup languageNode of question
   * so find child node of language node is searched
   * and find properties of child node, if contain input of user, get this question
   * 
   * @param   Question list
   * @param   langage want search
   * @param   question's content want search
   * @param   response's content want search
   * @param    sProvider 
   * @return   Question list
   * @throws Exception the exception
   */
  /*
   * public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch, SessionProvider sProvider) throws Exception { sProvider.close() ; return searchQuestionByLangage(listQuestion, languageSearch, questionSearch, responseSearch) ; } public List<Question> searchQuestionByLangage(List<Question> listQuestion, String
   * languageSearch, String questionSearch, String responseSearch) throws Exception { return jcrData_.searchQuestionByLangage(listQuestion, languageSearch, questionSearch, responseSearch) ; }
   */

  /**
   * Returns an list category that can then be view on the screen.
   * <p>
   * This method always returns immediately, this view list category in screen.
   * if categoryId equal null then list category is list parent category
   * else this view list category of one value parent category that you communicate categoryId 
   *  
   * @param    categoryId is address id of the category 
   * @param    sProvider
   * @return  List parent category or list sub category
   * @see     list category
   * @throws Exception the exception
   */
  public List<Category> getSubCategories(String categoryId, SessionProvider sProvider, FAQSetting faqSetting, boolean isGetAll, List<String> userView) throws Exception {
    sProvider.close();
    return getSubCategories(categoryId, faqSetting, isGetAll, userView);
  }

  public List<Category> getSubCategories(String categoryId, FAQSetting faqSetting, boolean isGetAll, List<String> userView) throws Exception {
    return jcrData_.getSubCategories(categoryId, faqSetting, isGetAll, userView);
  }

  /**
   * This method should lookup questions via question identify and from category identify
   * so lookup destination category and move questions to destination category
   * 
   * @param Question identify list
   * @param destination category identify
   * @param sProvider
   * @throws Exception the exception
   */
  public void moveQuestions(List<String> questions, String destCategoryId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    this.moveQuestions(questions, destCategoryId, "", new FAQSetting());
  }

  public void moveQuestions(List<String> questions, String destCategoryId, String questionLink, FAQSetting faqSetting) throws Exception {
    jcrData_.moveQuestions(questions, destCategoryId, questionLink, faqSetting);
  }

  /**
   * Remove one category in list
   * <p>
   * This function is used to remove one category in list
   * 
   * @param    categoryId is address id of the category need remove 
   * @param    sProvider
   * @throws Exception the exception
   */
  public void removeCategory(String categoryId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    removeCategory(categoryId);
  }

  public void removeCategory(String categoryId) throws Exception {
    jcrData_.removeCategory(categoryId);
  }

  /**
   * Remove one question in list
   * <p>
   * This function is used to remove one question in list
   * 
   * @param    question identify
   * @param    sProvider
   * @throws Exception the exception
   */
  public void removeQuestion(String questionId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    removeQuestion(questionId);
  }

  public void removeQuestion(String questionId) throws Exception {
    jcrData_.removeQuestion(questionId);
  }

  /**
   * Add new or edit category in list.
   * <p>
   * This function is used to add new or edit category in list. User will input information of fields need
   * in form add category, so user save then category will persistent in data
   * 
   * @param    parentId is address id of the category parent where user want add sub category
   * when paretId equal null so this category is parent category else sub category  
   * @param    cat is properties that user input to interface will save on data
   * @param    isAddNew is true when add new category else update category
   * @param    sProvider
   * @return  List parent category or list sub category
   * @see     list category
   * @throws Exception the exception
   */
  public void saveCategory(String parentId, Category cat, boolean isAddNew, SessionProvider sProvider) throws Exception {
    sProvider.close();
    saveCategory(parentId, cat, isAddNew);
  }

  public void saveCategory(String parentId, Category cat, boolean isAddNew) {
    jcrData_.saveCategory(parentId, cat, isAddNew);
  }

  public void changeStatusCategoryView(List<String> listCateIds, SessionProvider sProvider) throws Exception {
    sProvider.close();
    changeStatusCategoryView(listCateIds);
  }

  public void changeStatusCategoryView(List<String> listCateIds) throws Exception {
    jcrData_.changeStatusCategoryView(listCateIds);
  }

  /**
   * This method should create new question or update exists question
   * @param question is information but user input or edit to form interface of question 
   * @param isAddNew equal true then add new question
   *         isAddNew equal false then update question
   * @param  sProvider
   */
  public Node saveQuestion(Question question, boolean isAddNew, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
    sProvider.close();
    return saveQuestion(question, isAddNew, faqSetting);
  }

  public Node saveQuestion(Question question, boolean isAddNew, FAQSetting faqSetting) throws Exception {
    Node questionNode = jcrData_.saveQuestion(question, isAddNew, faqSetting);
    SessionProvider provider = CommonUtils.createSystemProvider();
    questionNode = (Node) locator.getSessionManager().getSession(provider).getItem(questionNode.getPath());
    for (QuestionLanguage lang : question.getMultiLanguages()) {
      if (lang.getState().equals(QuestionLanguage.ADD_NEW) || lang.getState().equals(QuestionLanguage.EDIT)) {
        MultiLanguages.addLanguage(questionNode, lang);
      } else if (lang.getState().equals(QuestionLanguage.DELETE)) {
        MultiLanguages.removeLanguage(questionNode, lang);
      }
    }
    for (AnswerEventListener ae : listeners_) {
      ae.saveQuestion(question, isAddNew);
    }
    return questionNode;
  }

  /**
   * This function is used to set some properties of FAQ. 
   * <p>
   * This function is used(Users of FAQ Administrator) choose some properties in object FAQSetting
   * 
   * @param   newSetting is properties of object FAQSetting that user input to interface will save on data
   * @param   sProvider
   * @return all value depend FAQSetting will configuration follow properties but user choose
   * @throws Exception the exception
   */
  public void saveFAQSetting(FAQSetting faqSetting, String userName, SessionProvider sProvider) throws Exception {
    sProvider.close();
    saveFAQSetting(faqSetting, userName);
  }

  public void saveFAQSetting(FAQSetting faqSetting, String userName) throws Exception {
    jcrData_.saveFAQSetting(faqSetting, userName);
  }

  /**
   * Move one category in list to another plate in project.
   * <p>
   * This function is used to move category in list. User will right click on category need
   * move, so user choose in list category one another category want to put
   * 
   * @param    categoryId is address id of the category that user want plate
   * @param    destCategoryId is address id of the category that user want put( destination )
   * @param    sProvider
   * @return  category will put new plate
   * @see     no see category this plate but user see that category at new plate
   * @throws Exception the exception
   */
  public void moveCategory(String categoryId, String destCategoryId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    moveCategory(categoryId, destCategoryId);
  }

  public void moveCategory(String categoryId, String destCategoryId) throws Exception {
    jcrData_.moveCategory(categoryId, destCategoryId);
  }

  /**
   * This function is used to allow user can watch a category. 
   * You have to register your email for whenever there is new question is inserted 
   * in the category or new category then there will  a notification sent to you.
   * 
   * @param    id of category with user want add watch on that category 
   * @param    value, this address email (multiple value) with input to interface will save on data
   * @param    sProvider
   * @throws Exception the exception
   *  
   */
  public void addWatch(String id, Watch watch, SessionProvider sProvider) throws Exception {
    sProvider.close();
    addWatchCategory(id, watch);
  }

  public void addWatchCategory(String id, Watch watch) throws Exception {
    jcrData_.addWatchCategory(id, watch);
  }

  /**
   * This method will get list mail of one category. User see list this mails and 
   * edit or delete mail if need
   * 
   * @param    CategoryId is id of category
   * @param    sProvider
   * @return  list email of current category
   * @see      list email where user manager  
   * @throws Exception the exception        
   */
  /*
   * public QuestionPageList getListMailInWatch(String categoryId, SessionProvider sProvider) throws Exception { sProvider.close() ; return getListMailInWatch(categoryId); } public QuestionPageList getListMailInWatch(String categoryId) throws Exception { return jcrData_.getListMailInWatch(categoryId); }
   */

  /**
   * This function will delete watch in one category 
   * 
   * @param   categoryId is id of current category
   * @param   sProvider
   * @param   emails is location current of one watch with user want delete 
   * @throws Exception the exception
   */
  public void deleteMailInWatch(String categoryId, SessionProvider sProvider, String user) throws Exception {
    sProvider.close();
    deleteCategoryWatch(categoryId, user);
  }

  public void deleteCategoryWatch(String categoryId, String user) throws Exception {
    jcrData_.deleteCategoryWatch(categoryId, user);
  }

  /**
   * This function will un watch in one category 
   * 
   * @param   categoryId is id of current category
   * @param   sProvider
   * @param   emails is location current of one watch with user want delete 
   * @throws Exception the exception
   */
  public void UnWatch(String categoryId, SessionProvider sProvider, String userCurrent) throws Exception {
    sProvider.close();
    unWatchCategory(categoryId, userCurrent);
  }

  public void unWatchCategory(String categoryId, String userCurrent) throws Exception {
    jcrData_.unWatchCategory(categoryId, userCurrent);
  }

  /**
   * This function will un watch in one category 
   * 
   * @param   categoryId is id of current category
   * @param   sProvider
   * @param   emails is location current of one watch with user want delete 
   * @throws Exception the exception
   */
  public void UnWatchQuestion(String questionID, SessionProvider sProvider, String userCurrent) throws Exception {
    sProvider.close();
    unWatchQuestion(questionID, userCurrent);
  }

  public void unWatchQuestion(String questionID, String userCurrent) throws Exception {
    jcrData_.unWatchQuestion(questionID, userCurrent);
  }

  /**
   * This method should lookup all the categories node 
   * so find category have user in moderators
   * and convert to category object and return list of category object
   * 
   * @param  user is name when user login
   * @param   sProvider  
   * @return Category list
   * @throws Exception the exception
   */
  public List<String> getListCateIdByModerator(String user, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getListCateIdByModerator(user);
  }

  public List<String> getListCateIdByModerator(String user) throws Exception {
    return jcrData_.getListCateIdByModerator(user);
  }

  /**
   * This method will return list question when user input value search
   * <p>
   * With many questions , it's difficult to find a question which user want to see.
   * So to support to users can find their questions more quickly and accurate,
   *  user can use 'Search Question' function
   * 
   * @param   sProvider
   * @param   eventQuery is object save value in form advanced search 
   * @throws Exception the exception
   */

  /*
   * public List<Question> getAdvancedSearchQuestion(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception { public List<ObjectSearchResult> getSearchResults(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception { sProvider.close() ; return getSearchResults(eventQuery) ; } public List<ObjectSearchResult> getSearchResults(FAQEventQuery eventQuery) throws Exception {
   * return jcrData_.getSearchResults(eventQuery) ; }
   */

  public List<ObjectSearchResult> getSearchResults(FAQEventQuery eventQuery) throws Exception {
    return jcrData_.getSearchResults(eventQuery);
  }

  /**
   * This method will return list question when user input value search
   * <p>
   * With many questions , it's difficult to find a question which user want to see.
   * So to support to users can find their questions more quickly and accurate,
   *  user can use 'Search Question' function
   * 
   * @param   sProvider
   * @param   eventQuery is object save value in form advanced search 
   * @throws Exception the exception
   */
  /*
   * public List<Question> searchQuestionWithNameAttach(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception { sProvider.close() ; return searchQuestionWithNameAttach(eventQuery) ; } public List<Question> searchQuestionWithNameAttach(FAQEventQuery eventQuery) throws Exception { return jcrData_.searchQuestionWithNameAttach(eventQuery) ; }
   */

  /**
   * This method return path of category identify
   * @param  category identify
   * @param   sProvider
   * @return list category name is sort(path of this category)
   * @throws Exception the exception
   */
  public List<String> getCategoryPath(SessionProvider sProvider, String categoryId) throws Exception {
    sProvider.close();
    return getCategoryPath(categoryId);
  }

  public List<String> getCategoryPath(String categoryId) throws Exception {
    return jcrData_.getCategoryPath(categoryId);
  }

  /**
   * This function will send all the pending notification message 
   * 
   */
  public Iterator<NotifyInfo> getPendingMessages() {
    return jcrData_.getPendingMessages();
  }

  /**
   * Adds the language node, when question have multiple language, 
   * each language is a child node of question node.
   * 
   * @param questionNode  the question node which have multiple language
   * @param language the  language which is added in to questionNode
   * @throws Exception    throw an exception when save a new language node
   */
  public void addLanguage(Node questionNode, QuestionLanguage language) throws Exception {
    multiLanguages_.addLanguage(questionNode, language);
  }

  public void getUserSetting(SessionProvider sProvider, String userName, FAQSetting faqSetting) throws Exception {
    sProvider.close();
    getUserSetting(userName, faqSetting);
  }

  public void getUserSetting(String userName, FAQSetting faqSetting) throws Exception {
    jcrData_.getUserSetting(userName, faqSetting);
  }

  public NotifyInfo getMessageInfo(String name) throws Exception {
    return jcrData_.getMessageInfo(name);
  }

  public boolean isAdminRole(String userName, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return isAdminRole(userName);
  }

  public boolean isAdminRole(String userName) throws Exception {
    return jcrData_.isAdminRole(userName);
  }

  public Node getCategoryNodeById(String categoryId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getCategoryNodeById(categoryId);
  }

  public Node getCategoryNodeById(String categoryId) throws Exception {
    return jcrData_.getCategoryNodeById(categoryId);
  }

  public void addWatchQuestion(String questionId, Watch watch, boolean isNew, SessionProvider sProvider) throws Exception {
    sProvider.close();
    addWatchQuestion(questionId, watch, isNew);
  }

  public void addWatchQuestion(String questionId, Watch watch, boolean isNew) throws Exception {
    jcrData_.addWatchQuestion(questionId, watch, isNew);
  }

  /*
   * public QuestionPageList getListMailInWatchQuestion(String questionId, SessionProvider sProvider) throws Exception { sProvider.close() ; return getListMailInWatchQuestion(questionId); } public QuestionPageList getListMailInWatchQuestion(String questionId) throws Exception { return jcrData_.getListMailInWatchQuestion(questionId); }
   */

  public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getListQuestionsWatch(faqSetting, currentUser);
  }

  public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser) throws Exception {
    return jcrData_.getListQuestionsWatch(faqSetting, currentUser);
  }

  /*
   * public List<String> getListPathQuestionByCategory(String categoryId, SessionProvider sProvider) throws Exception{ sProvider.close() ; return getListPathQuestionByCategory(categoryId); } public List<String> getListPathQuestionByCategory(String categoryId) throws Exception{ return jcrData_.getListPathQuestionByCategory(categoryId); }
   */

  public void importData(String categoryId, Session session, InputStream inputStream, boolean isZip, SessionProvider sProvider) throws Exception {
    sProvider.close();
    importData(categoryId, inputStream, isZip);
  }

  public boolean importData(String categoryId, InputStream inputStream, boolean isZip) throws Exception {
    return jcrData_.importData(categoryId, inputStream, isZip);
  }

  public InputStream exportData(String categoryId, boolean createZipFile) throws Exception {
    return jcrData_.exportData(categoryId, createZipFile);
  }

  /*
   * public boolean categoryAlreadyExist(String categoryId, SessionProvider sProvider) throws Exception { sProvider.close() ; return categoryAlreadyExist(categoryId); } public boolean categoryAlreadyExist(String categoryId) throws Exception { return jcrData_.categoryAlreadyExist(categoryId); }
   */

  public void swapCategories(String parentCateId, String cateId1, String cateId2, SessionProvider sProvider) throws Exception {
    sProvider.close();
    swapCategories(cateId1, cateId2);
  }

  public void swapCategories(String cateId1, String cateId2) throws Exception {
    jcrData_.swapCategories(cateId1, cateId2);
  }

  /*
   * public Node getQuestionNodeById(String questionId, SessionProvider sProvider) throws Exception{ sProvider.close() ; return getQuestionNodeById(questionId); } public Node getQuestionNodeById(String questionId) throws Exception{ return jcrData_.getQuestionNodeById(questionId); }
   */

  public void saveTopicIdDiscussQuestion(String questionId, String pathDiscuss, SessionProvider sProvider) throws Exception {
    sProvider.close();
    saveTopicIdDiscussQuestion(questionId, pathDiscuss);
  }

  public void saveTopicIdDiscussQuestion(String questionId, String pathDiscuss) throws Exception {
    jcrData_.saveTopicIdDiscussQuestion(questionId, pathDiscuss);
  }

  public long getMaxindexCategory(String parentId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getMaxindexCategory(parentId);
  }

  public long getMaxindexCategory(String parentId) throws Exception {
    return jcrData_.getMaxindexCategory(parentId);
  }

  public void deleteAnswer(String questionId, String answerId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    deleteAnswer(questionId, answerId);
  }

  public void deleteAnswer(String questionId, String answerId) throws Exception {
    jcrData_.deleteAnswer(questionId, answerId);
  }

  public void deleteComment(String questionId, String commentId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    deleteComment(questionId, commentId);
  }

  public void deleteComment(String questionId, String commentId) throws Exception {
    jcrData_.deleteComment(questionId, commentId);
  }

  public void saveAnswer(String questionId, Answer answer, boolean isNew, SessionProvider sProvider) throws Exception {
    sProvider.close();
    saveAnswer(questionId, answer, isNew);
  }

  public void saveAnswer(String questionId, Answer answer, boolean isNew) throws Exception {
    jcrData_.saveAnswer(questionId, answer, isNew);
    for (AnswerEventListener ae : listeners_) {
      ae.saveAnswer(questionId, answer, isNew);
    }
  }

  public void saveComment(String questionId, Comment comment, boolean isNew, SessionProvider sProvider) throws Exception {
    sProvider.close();
    saveComment(questionId, comment, isNew);
  }

  public void saveComment(String questionId, Comment comment, boolean isNew) throws Exception {
    jcrData_.saveComment(questionId, comment, isNew);
    for (AnswerEventListener ae : listeners_) {
      ae.saveComment(questionId, comment, isNew);
    }
  }

  public Comment getCommentById(SessionProvider sProvider, String questionId, String commentId) throws Exception {
    sProvider.close();
    return getCommentById(questionId, commentId);
  }

  public Comment getCommentById(String questionId, String commentId) throws Exception {
    return jcrData_.getCommentById(questionId, commentId);
  }

  public Answer getAnswerById(String questionId, String answerid, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getAnswerById(questionId, answerid);
  }

  public Answer getAnswerById(String questionId, String answerid) throws Exception {
    return jcrData_.getAnswerById(questionId, answerid);
  }

  /**
   * @deprecated use {@link #saveAnswer(String questionId, Answer[] answers)}
   */
  public void saveAnswer(String questionId, Answer[] answers, SessionProvider sProvider) throws Exception {
    sProvider.close();
    saveAnswer(questionId, answers);
  }

  public void saveAnswer(String questionId, Answer[] answers) throws Exception {
    jcrData_.saveAnswer(questionId, answers);
    for (AnswerEventListener ae : listeners_) {
      ae.saveAnswer(questionId, answers, true);
    }
  }

  public JCRPageList getPageListAnswer(SessionProvider sProvider, String questionId, boolean isSortByVote) throws Exception {
    sProvider.close();
    return getPageListAnswer(questionId, isSortByVote);
  }

  public JCRPageList getPageListAnswer(String questionId, boolean isSortByVote) throws Exception {
    return jcrData_.getPageListAnswer(questionId, isSortByVote);
  }

  public JCRPageList getPageListComment(SessionProvider sProvider, String questionId) throws Exception {
    sProvider.close();
    return getPageListComment(questionId);
  }

  public JCRPageList getPageListComment(String questionId) throws Exception {
    return jcrData_.getPageListComment(questionId);
  }

  public QuestionPageList getListCategoriesWatch(String userId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getWatchedCategoryByUser(userId);
  }

  public QuestionPageList getWatchedCategoryByUser(String userId) throws Exception {
    return jcrData_.getWatchedCategoryByUser(userId);
  }

  public FileAttachment getUserAvatar(String userName, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return getUserAvatar(userName);
  }

  public FileAttachment getUserAvatar(String userName) throws Exception {
    return jcrData_.getUserAvatar(userName);
  }

  public void saveUserAvatar(String userId, FileAttachment fileAttachment, SessionProvider sProvider) throws Exception {
    sProvider.close();
    saveUserAvatar(userId, fileAttachment);
  }

  public void saveUserAvatar(String userId, FileAttachment fileAttachment) throws Exception {
    jcrData_.saveUserAvatar(userId, fileAttachment);
  }

  public void setDefaultAvatar(String userName, SessionProvider sProvider) throws Exception {
    sProvider.close();
    setDefaultAvatar(userName);
  }

  public void setDefaultAvatar(String userName) throws Exception {
    jcrData_.setDefaultAvatar(userName);
  }

  public boolean getWatchByUser(String userId, String cateId, SessionProvider sProvider) throws Exception {
    sProvider.close();
    return isUserWatched(userId, cateId);
  }

  public boolean isUserWatched(String userId, String cateId) {
    return jcrData_.isUserWatched(userId, cateId);
  }

  public QuestionPageList getPendingQuestionsByCategory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
    sProvider.close();
    return getPendingQuestionsByCategory(categoryId, faqSetting);
  }

  public QuestionPageList getPendingQuestionsByCategory(String categoryId, FAQSetting faqSetting) throws Exception {
    return jcrData_.getPendingQuestionsByCategory(categoryId, faqSetting);
  }

  public boolean isExisting(String path) throws Exception {
    return jcrData_.isExisting(path);
  }

  public String getCategoryPathOf(String id) throws Exception {
    return jcrData_.getCategoryPathOf(id);
  }

  public List<String> getQuestionContents(List<String> paths) throws Exception {
    return jcrData_.getQuestionContents(paths);
  }
  
  public Map<String, String> getRelationQuestion(List<String> paths) throws Exception{
    return jcrData_.getRelationQuestion(paths);
  }

  public Node getQuestionNodeById(String path) throws Exception {
    return jcrData_.getQuestionNodeById(path);
  }

  public boolean isModerateAnswer(String id) throws Exception {
    return jcrData_.isModerateAnswer(id);
  }

  public boolean isModerateQuestion(String id) throws Exception {
    return jcrData_.isModerateQuestion(id);
  }

  public String getParentCategoriesName(String path) throws Exception {
    return jcrData_.getParentCategoriesName(path);
  }

  public QuestionPageList getListMailInWatch(String categoryId) throws Exception {
    return jcrData_.getListMailInWatch(categoryId);
  }

  public boolean isCategoryModerator(String categoryId, String user) throws Exception {
    return jcrData_.isCategoryModerator(categoryId, user);
  }

  public void addLanguage(String questionPath, QuestionLanguage language) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = jcrData_.getFAQServiceHome(sProvider).getNode(questionPath);
      MultiLanguages.addLanguage(questionNode, language);
    } catch (Exception e) {
      log.error("Fail to add language: ", e);
    }
  }

  public void deleteAnswerQuestionLang(String questionPath, String answerId, String language) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = jcrData_.getFAQServiceHome(sProvider).getNode(questionPath);
      MultiLanguages.deleteAnswerQuestionLang(questionNode, answerId, language);
    } catch (Exception e) {
      log.error("Fail to delete " + answerId + " :", e);
    }
  }

  public void deleteCommentQuestionLang(String questionPath, String commentId, String language) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = jcrData_.getFAQServiceHome(sProvider).getNode(questionPath);
      MultiLanguages.deleteCommentQuestionLang(questionNode, commentId, language);
    } catch (Exception e) {
      log.error("Fail to delete " + commentId + " comment question", e);
    }
  }

  public QuestionLanguage getQuestionLanguageByLanguage(String questionPath, String language) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = jcrData_.getFAQServiceHome(sProvider).getNode(questionPath);
      return MultiLanguages.getQuestionLanguageByLanguage(questionNode, language);
    } catch (Exception e) {
      throw e;
    }
  }

  public Comment getCommentById(String questionPath, String commentId, String language) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = jcrData_.getFAQServiceHome(sProvider).getNode(questionPath);
      return MultiLanguages.getCommentById(questionNode, commentId, language);
    } catch (Exception e) {
      log.error("Fail to get comment", e);
    }
    return null;
  }

  public Answer getAnswerById(String questionPath, String answerid, String language) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = jcrData_.getFAQServiceHome(sProvider).getNode(questionPath);
      return MultiLanguages.getAnswerById(questionNode, answerid, language);
    } catch (Exception e) {
      log.error("Fail to get answer: " + e.getMessage());
    }
    return null;
  }

  public void saveAnswer(String questionPath, Answer answer, String language) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = jcrData_.getFAQServiceHome(sProvider).getNode(questionPath);
      MultiLanguages.saveAnswer(questionNode, answer, language);
    } catch (Exception e) {
      log.error("Fail to save answer:", e);
    }
  }

  public void saveAnswer(String questionPath, QuestionLanguage questionLanguage) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = jcrData_.getFAQServiceHome(sProvider).getNode(questionPath);
      MultiLanguages.saveAnswer(questionNode, questionLanguage);
    } catch (Exception e) {
      log.error("Fail to save answer: ", e);
    }
  }

  public void saveComment(String questionPath, Comment comment, String languge) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = jcrData_.getFAQServiceHome(sProvider).getNode(questionPath);
      MultiLanguages.saveComment(questionNode, comment, languge);
      for (AnswerEventListener ae : listeners_) {
        ae.saveComment(questionNode.getName(), comment, true);
      }
    } catch (Exception e) {
      log.error("\nFail to save comment\n ", e);
    }
  }

  public void removeLanguage(String questionPath, List<String> listLanguage) {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = jcrData_.getFAQServiceHome(sProvider).getNode(questionPath);
      MultiLanguages.removeLanguage(questionNode, listLanguage);
    } catch (Exception e) {
      log.error("\nFail to remove language\n", e);
    }
  }

  public void voteAnswer(String answerPath, String userName, boolean isUp) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node answerNode = jcrData_.getFAQServiceHome(sProvider).getNode(answerPath);
      MultiLanguages.voteAnswer(answerNode, userName, isUp);
    } catch (Exception e) {
      log.error("\nFail to vote answer\n", e);
    }
  }

  public void voteQuestion(String questionPath, String userName, int number) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = jcrData_.getFAQServiceHome(sProvider).getNode(questionPath);
      MultiLanguages.voteQuestion(questionNode, userName, number);
    } catch (Exception e) {
      log.error("\nFail to vote question\n", e);
    }
  }

  public void unVoteQuestion(String questionPath, String userName) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = jcrData_.getFAQServiceHome(sProvider).getNode(questionPath);
      MultiLanguages.unVoteQuestion(questionNode, userName);
    } catch (Exception e) {
      log.error("\nFail to unvote question\n", e);
    }
  }

  public String[] getModeratorsOf(String path) throws Exception {
    return jcrData_.getModeratorsOf(path);
  }

  public boolean isViewAuthorInfo(String id) {
    return jcrData_.isViewAuthorInfo(id);
  }

  public long existingCategories() throws Exception {
    return jcrData_.existingCategories();
  }

  public String getCategoryNameOf(String categoryPath) throws Exception {
    return jcrData_.getCategoryNameOf(categoryPath);
  }

  public List<Question> getQuickQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
    return jcrData_.getQuickQuestionsByListCatetory(listCategoryId, isNotYetAnswer);
  }

  public List<Cate> listingCategoryTree() throws Exception {
    return jcrData_.listingCategoryTree();
  }

  public List<Watch> getWatchByCategory(String categoryId) throws Exception {
    return jcrData_.getWatchByCategory(categoryId);
  }

  public boolean hasWatch(String categoryPath) {
    return jcrData_.hasWatch(categoryPath);
  }

  public CategoryInfo getCategoryInfo(String categoryPath, List<String> categoryIdScoped) throws Exception {
    return jcrData_.getCategoryInfo(categoryPath, categoryIdScoped);
  }

  public byte[] getTemplate() throws Exception {
    return jcrData_.getTemplate();
  }

  public void saveTemplate(String str) throws Exception {
    jcrData_.saveTemplate(str);
  }

  public boolean isCategoryExist(String name, String path) {
    return jcrData_.isCategoryExist(name, path);
  }

  public void updateQuestionRelatives(String questionPath, String[] relatives) throws Exception {
    jcrData_.updateQuestionRelatives(questionPath, relatives);
  }

  public InputStream createAnswerRSS(String cateId) throws Exception {
    return jcrData_.createAnswerRSS(cateId);
  }

  public void reCalculateLastActivityOfQuestion(String absPathOfProp) throws Exception {
    jcrData_.reCalculateInfoOfQuestion(absPathOfProp);
  }

  public void addListenerPlugin(AnswerEventListener listener) throws Exception {
    listeners_.add(listener);
  }

  public void calculateDeletedUser(String userName) throws Exception {
    jcrData_.calculateDeletedUser(userName);
  }

  @Override
  public Object readCategoryProperty(String categoryId, String propertyName, Class returnType) throws Exception {
    return jcrData_.readCategoryProperty(categoryId, propertyName, returnType);
  }

  @Override
  public Object readQuestionProperty(String questionId, String propertyName, Class returnType) throws Exception {
    return jcrData_.readQuestionProperty(questionId, propertyName, returnType);
  }
}