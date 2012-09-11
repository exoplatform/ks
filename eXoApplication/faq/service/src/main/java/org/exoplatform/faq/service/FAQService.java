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
package org.exoplatform.faq.service;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.faq.service.DataStorage.LoadMoreType;
import org.exoplatform.faq.service.impl.AnswerEventListener;
import org.exoplatform.ks.common.NotifyInfo;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.list;

/**
 * Created by The eXo Platform SARL.
 * <p>
 * FAQService is interface provide functions for processing database
 * with category and question include: add, edit, remove and search
 * categories or questions.
 * 
 * @author  Hung Nguyen Quang
 *           hung.nguyen@exoplatform.com
 * @since   Mar 04, 2008
 */
public interface FAQService extends FAQServiceLegacy {

  /**
   * Adds the plugin.
   * 
   * @param plugin the plugin
   * 
   * @throws Exception the exception
   */
  public void addPlugin(ComponentPlugin plugin) throws Exception;

  /**
   * Plugin to initialize default FAQ data
   * @param plugin
   * @throws Exception
   */
  public void addInitialDataPlugin(InitialDataPlugin plugin) throws Exception;

  /**
   * This method should check exists category or NOT to create new or update exists category
   * <p>
   * This function is used to add new or edit category in list. User will input information of fields need
   * in form add category, so user save then category will persistent in data
   * 
   * @param    parentId is address id of the category parent where user want add sub category
   * when paretId = null so this category is parent category else sub category  
   * @param    cat is properties that user input to interface will save on data
   * @param    isAddNew is true when add new category else update category
   * @return  List parent category or list sub category
   * @see     list category
   */
  public void saveCategory(String parentId, Category cat, boolean isAddNew);

  /**
   * This method should change view of category
   * 
   * @param   listCateIds is address ids of the category need to change 
   * @throws Exception the exception
   */
  public void changeStatusCategoryView(List<String> listCateIds) throws Exception;

  /**
   * This method should check exists of category and remove it
   * 
   * @param    categoryId is address id of the category need remove 
   * @throws Exception the exception
   */
  public void removeCategory(String categoryId) throws Exception;

  /**
   * This method should lookup category via identify 
   * and convert to Category object and return
   * 
   * @param    categoryId is address id of the category so you want get
   * @return  category is id = categoryId
   * @see     current category
   * @throws Exception the exception
   */
  public Category getCategoryById(String categoryId) throws Exception;

  /**
   * This method should lookup all the category
   * and convert to category object and return list of category object
   * 
   * @return Category list
   * 
   * @throws Exception the exception
   */
  public List<Category> getAllCategories() throws Exception;

  /**
   * Get all categories of user.
   * the first lookup all the categories, find categories which have 
   * <code>user</code> in moderators after that put this categories 
   * into a list category object
   * 
   * @param user      the name of user
   * 
   * @return Category list
   * 
   * @throws Exception if can't found user
   */
  public List<String> getListCateIdByModerator(String user) throws Exception;

  /**
   * This method should lookup all sub-categories of a category
   * and convert to category object and return list of category object
   * 
   * @param categoryId the category id
   * @param userView the list of users to view categories.
   * @return Category list
   * 
   * @throws Exception the exception
   */
  public List<Category> getSubCategories(String categoryId, FAQSetting faqSetting, boolean isGetAll, List<String> userView) throws Exception;

  /**
   * This method should move a category.
   * @param categoryId the category id should move
   * @param destCategoryId : category is moved
   * @throws Exception the exception
   */
  public void moveCategory(String categoryId, String destCategoryId) throws Exception;

  /**
   * Save question after create new question or edit infor of quesiton which is existed.
   * If param <code>isAddNew</code> is <code>true</code> then create new question node
   * and set properties of question object to this node else reset infor for
   * this question node
   * 
   * @param question  the question
   * @param isAddNew  is <code>true</code> if create new question node
   *                  and is <code>false</code> if edit question node
   * 
   * @return the question node
   * 
   * @throws Exception if path of question nod not found
   */
  public Node saveQuestion(Question question, boolean isAddNew, FAQSetting faqSetting) throws Exception;

  /**
   * Delete question by question's id. Check question if it's existed then remove it
   * 
   * @param questionId  the id of question is deleted
   * 
   * @throws Exception  if question not found
   */
  public void removeQuestion(String questionId) throws Exception;

  /**
   * Lookup the question node via identify, convert to question object and return
   * 
   * @param questionId the question id
   * 
   * @return Question
   * 
   * @throws Exception the exception
   */
  public Question getQuestionById(String questionId) throws Exception;

  /**
   * Get all questions
   * 
   * 
   * @return List of question
   * 
   * @throws Exception  if attachment not foune
   */
  public QuestionPageList getAllQuestions() throws Exception;

  /**
   * Get all questisons not yet answered, the first get all questions 
   * which have property response is null (have not yet answer) then
   * convert to list of question object
   * 
   * 
   * @return List of question
   * 
   * @throws Exception  if lost attachment
   */
  public QuestionPageList getQuestionsNotYetAnswer(String categoryId, boolean isApproved) throws Exception;

  /**
   * Get questions are activagted and approved in the category.
   * The first get category from id which is specified by param <code>categoryId</code>
   * then lookup questions of this category, only question is activated and approved  
   * via category identify, the last convert to list of question object
   * 
   * @param categoryId  the category id
   * 
   * @return Question list
   * 
   * @throws Exception  if can't found category
   */
  public QuestionPageList getQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception;

  /**
   * Get all questions of the category.
   * The first get category from id which is specified by param <code>categoryId</code>
   * then get all questions of this category and put them into an question page list object
   * 
   * @param categoryId    the id of category
   * 
   * @return Question page list
   * 
   * @throws Exception    when category not found
   */
  public QuestionPageList getAllQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception;

  /**
   * Get some informations of category: Lookup category node by category's id
   * and count sub-categories and questions are contained in this category.
   * 
   * @param categoryId the category id
   * 
   * @return              number of sub-categories
   * number of questions
   * number of questions is not approved
   * number of question is have not yet answered
   * 
   * @throws  Exception   if not found category by id
   * if not found question or lose file attach
   * @throws Exception the exception
   */
  public long[] getCategoryInfo(String categoryId, FAQSetting setting) throws Exception;

  /**
   * Get questions in list categories.
   * <p>
   * With each category in the list categories, if <code>isNotYetAnswer</code>
   * is <code>false</code> get all questoin in this catgory else get questions 
   * which is not yet answered, and put them in to a QuestionPageList object
   * 
   * @param listCategoryId  the list category id
   * @param isNotYetAnswer  is <code>true</code> if get qeustions not yet answered
   *                        is <code>false</code> if want get all questions in list categories
   * 
   * @return Question page list
   * 
   * @throws Exception      the exception
   */
  public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception;

  /**
   * Get path of question. For example question A is included in category C and C is child of
   * category B, then, this function will be return C > B
   * @param categoryId  id of category is contain question
   * @return  name of categories
   * @throws Exception
   */
  public String getCategoryPathOfQuestion(String categoryId) throws Exception;

  /**
   * Get all language nodes of question node which have id is specified,
   * with each language node get properties of them and set into 
   * QuestionLanguage object. One QuestionLanguage object have properties: 
   * <ul>
   * <li> Language: the name of language
   * <li> Question: content of questions is written by Language
   * <li> Response: content of response is written by Language
   * </ul>
   * 
   * @param questionId  the id of question
   * 
   * @return list languages are support by the question
   */
  public List<QuestionLanguage> getQuestionLanguages(String questionId);

  /**
   * This method should lookup languageNode of question
   * so find child node of language node is searched
   * and find properties of child node, if contain input of user, get this question
   * 
   * @param   Question list
   * @param   langage want search
   * @param   term content want search in all field question
   * @return   Question list
   * @throws Exception the exception
   */
  // public List<Question> searchQuestionByLangageOfText(List<Question> listQuestion, String languageSearch, String text) throws Exception ;

  /**
   * Search question by language.
   * <p>
   * From list questions, find all questions which support the language is specified and
   * have properties question and response is specified to search.
   * <p>
   * With each question object of list questions, find child node which have name is
   * <code>languageSearch</code>, if it's existed then compare it's properties
   * question and response with params <code>questionSearch</code> and <code>responseSearch</code>,
   * have some cases: 
   * <ul>
   * <li>The first: if one of params <code>questionSearch</code> and <code>responseSearch</code> is null 
   *     then bypassing this property when search. 
   * <li>The second: if all of params <code>questionSearch</code> and <code>responseSearch</code> is null
   *     then get this question object.
   * <li>The third: if all of  params <code>questionSearch</code> and <code>responseSearch</code> not null 
   *     then compare language node's properties question and content with them.
   * </ul>
   * else bypassing this question node and check next question object
   * 
   * @param listQuestion    the list question to search, this list questions are must be specified to search
   * @param languageSearch  the name of language node
   * @param questionSearch  the question's content, if <code>null</code> then bypassing this property
   * @param responseSearch  the response's content, if <code>null</code> then bypassing this property
   * 
   * @return                Question list
   * 
   * @throws Exception      when question node not found
   */
  // public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch) throws Exception ;

  /**
   * Move all of questions to category which have id is  specified
   * 
   * @param questions the questions
   * @param destCategoryId the dest category id
   * @param questionLink the question link
   * @param faqSetting the FAQSetting
   * @throws Exception the exception
   */
  public void moveQuestions(List<String> questions, String destCategoryId, String questionLink, FAQSetting faqSetting) throws Exception;

  /**
   * This method to update FAQ setting.
   * 
   * @param newSetting the new setting
   * @throws Exception the exception
   */
  public void saveFAQSetting(FAQSetting faqSetting, String userName) throws Exception;

  /**
   * This function is used to allow user can watch a category. 
   * You have to register your email for whenever there is new question is inserted 
   * in the category or new category then there will  a notification sent to you.
   * 
   * @param    id of category with user want add watch on that category 
   * @param    value, this address email (multiple value) with input to interface will save on data
   * @throws Exception the exception
   *  
   */
  public void addWatchCategory(String id, Watch watch) throws Exception;

  /**
   * This method will get list mail of one category. User see list this mails and 
   * edit or delete mail if need
   * 
   * @param    CategoryId is id of category
   * @return  list email of current category
   * @see      list email where user manager  
   * @throws Exception the exception        
   */
  // public QuestionPageList getListMailInWatch(String categoryId) throws Exception ;

  /**
   * This method will delete watch in one category 
   * 
   * @param   categoryId is id of current category
   * @param   emails is location current of one watch with user want delete 
   * @throws Exception the exception
   */
  public void deleteCategoryWatch(String categoryId, String user) throws Exception;

  /**
   * This method will un watch in one category 
   * 
   * @param   categoryId is id of current category
   * @param   userCurrent is user current then you un watch
   * @throws Exception the exception
   */
  public void unWatchCategory(String categoryId, String userCurrent) throws Exception;

  /**
   * This method will un watch in one question 
   * 
   * @param   questionId is id of current category
   * @param   userCurrent is user current then you un watch
   * @throws Exception the exception
   */
  public void unWatchQuestion(String questionID, String userCurrent) throws Exception;

  /**
   * This method will return list category when user input value search
   * <p>
   * With many categories , it's difficult to find a category which user want to see.
   * So to support to users can find their categories more quickly and accurate,
   *  user can use 'Search Category' function
   * 
   * @param   eventQuery is object save value in form advanced search 
   * @throws Exception the exception
   */
  // public List<Category> getAdvancedSearchCategory(FAQEventQuery eventQuery) throws Exception ;

  /**
   * This method should lookup all the categories node 
   * so find category have user in moderators
   * and convert to category object and return list of category object
   * 
   * @param  user is name when user login
   * @return Category list
   * @throws Exception the exception
   */

  // public List<Question> getAdvancedSearchQuestion(FAQEventQuery eventQuery) throws Exception ;

  public List<ObjectSearchResult> getSearchResults(FAQEventQuery eventQuery) throws Exception;

  /**
   * This method should lookup all the categories node 
   * so find category have user in moderators
   * and convert to category object and return list of category object
   * 
   * @param  user is name when user login
   * @return Category list
   * @throws Exception the exception
   */
  // public List<Question> searchQuestionWithNameAttach(FAQEventQuery eventQuery) throws Exception ;

  /**
   * This method return path of category identify
   * @param  category identify
   * @return list category name is sort(path of this category)
   * @throws Exception the exception
   */
  public List<String> getCategoryPath(String categoryId) throws Exception;

  /**
   * This method will get messages to send notify
   */
  public Iterator<NotifyInfo> getPendingMessages();

  /**
   * Add language for question node, this function only use for Question node, 
   * and language node is added not default.
   * <p>
   * the first, get this language node if it's existed, opposite add new language node.
   * Then set properties for this node: node's name, question's content and
   * response's content.
   * 
   * @param questionNode  add language node for this question node
   * @param language      The QuestionLanguage object which is add for the question node,
   *                      language object have some properties: name, question's content
   *                      and response's content. Property <code>response</code> may be don't need
   *                      setting value if question not yet answered
   * 
   * @throws Exception the exception
   */
  public void addLanguage(Node questionNode, QuestionLanguage language) throws Exception;

  /**
   * Get setting of user to view data (categories and questions). At first time user come, 
   * system will create setting for user (automatically) base on setting of admin 
   * (Default setting of FAQ system). After that, when user login again, his setting is getted.
   * 
   * @param userName  the name of user
   * @param faqSetting  the setting of user
   * @throws Exception  when can't find user or faqSetting
   */
  public void getUserSetting(String userName, FAQSetting faqSetting) throws Exception;

  /**
   * Get informations about message
   * @param name  key of message
   * @return informations contain message and email addresses. 
   * @throws Exception
   */
  public NotifyInfo getMessageInfo(String name) throws Exception;

  /**
   * Check permission of user
   * @param userName  id or user name of user who is checked permission
   * @return  return <code>true</code> if user is admin. The current user is implied if userName is null.
   * @throws Exception
   */
  public boolean isAdminRole(String userName) throws Exception;

  /**
   * Get all user is admin.
   * 
   * @throws Exception the exception
   */
  public List<String> getAllFAQAdmin() throws Exception;

  /**
   * Adds the plugin.
   * 
   * @param plugin the plugin
   * 
   * @throws Exception the exception
   */
  public void addRolePlugin(ComponentPlugin plugin) throws Exception;

  /**
   * Add watch for a question
   * @param questionId id of question
   * @param watch contains information of users
   * @param isNew add new or edit 
   * @throws Exception
   */
  public void addWatchQuestion(String questionId, Watch watch, boolean isNew) throws Exception;

  /** 
   * Save topic
   * @param questionId Question to discuss
   * @param pathDiscuss path to discussion 
   * @throws Exception
   */
  public void saveTopicIdDiscussQuestion(String questionId, String pathDiscuss) throws Exception;

  // public QuestionPageList getListMailInWatchQuestion(String questionId) throws Exception;

  /**
   * Get list of questions that user watches 
   * @param faqSetting setting of user
   * @param currentUser username
   * @return List of questions 
   * @throws Exception
   */
  public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser) throws Exception;

  /**
   * Get category node
   * @param categoryId id of category
   * @return node of category 
   * @throws Exception
   */
  public Node getCategoryNodeById(String categoryId) throws Exception;

  // public List<String> getListPathQuestionByCategory(String categoryId) throws Exception;

  /**
   * Import data to category
   * @param name  key of message
   * @return informations contain message and email addresses. 
   * @throws Exception
   */
  public boolean importData(String categoryId, InputStream inputStream, boolean isZip) throws Exception;

  // public boolean categoryAlreadyExist(String categoryId) throws Exception ;

  /**
   * Swap two categories
   * @param cateId1 id of category 1
   * @param cateId2 id of category 2 
   * @throws Exception
   */
  public void swapCategories(String cateId1, String cateId2) throws Exception;

  // public Node getQuestionNodeById(String questionId) throws Exception;

  /**
   * Get max index of categories
   * @param parentId id of parent
   * @return index 
   * @throws Exception
   */
  public long getMaxindexCategory(String parentId) throws Exception;

  /**
   * Remove an answer
   * @param questionId id of question
   * @param answerId id of answer 
   * @throws Exception
   */
  public void deleteAnswer(String questionId, String answerId) throws Exception;

  /**
   * Remove comment of question
   * @param questionId id of question
   * @param commentId id of comment 
   * @throws Exception
   */
  public void deleteComment(String questionId, String commentId) throws Exception;

  /**
   * Save an answer
   * @param questionId id of question
   * @param answer saved answer
   * @param isNew save new or edit 
   * @throws Exception
   */
  public void saveAnswer(String questionId, Answer answer, boolean isNew) throws Exception;

  /**
   * Save comment of question
   * @param questionId id of question
   * @param comment saved comment
   * @param isNew save new or edit 
   * @throws Exception
   */
  public void saveComment(String questionId, Comment comment, boolean isNew) throws Exception;

  /**
   * Get comment of question
   * @param questionId id of question
   * @param commentId id of comment
   * @return comment 
   * @throws Exception
   */
  public Comment getCommentById(String questionId, String commentId) throws Exception;

  /**
   * Get answer of question
   * @param questionId id of question
   * @param answerid id of answer
   * @return an Answer 
   * @throws Exception
   */
  public Answer getAnswerById(String questionId, String answerid) throws Exception;

  /**
   * Save an answer
   * @param questionId id of question
   * @param answers saved answers 
   * @throws Exception
   */
  public void saveAnswer(String questionId, Answer[] answers) throws Exception;

  /**
   * Get comments of question
   * @param questionId id of question
   * @return comment page list 
   * @throws Exception
   */
  public JCRPageList getPageListComment(String questionId) throws Exception;

  /**
   * Get answers of question
   * @param questionId id of question
   * @param isSortByVote sort by vote
   * @return answers page list 
   * @throws Exception
   */
  public JCRPageList getPageListAnswer(String questionId, boolean isSortByVote) throws Exception;

  /**
   * Get list questions that user watches
   * @param userId username 
   * @return question page list 
   * @throws Exception
   */
  public QuestionPageList getWatchedCategoryByUser(String userId) throws Exception;

  /**
   * Get avatar of user
   * @param userName username
   * @return avatar of user 
   * @throws Exception
   */
  public FileAttachment getUserAvatar(String userName) throws Exception;

  /**
   * Save avatar of an user
   * @param userId username
   * @param fileAttachment avatar of user 
   * @throws Exception
   */
  public void saveUserAvatar(String userId, FileAttachment fileAttachment) throws Exception;

  /**
   * Check that user is watching a category
   * @param userId username
   * @param cateId id of category
   * @return true if user is watching and false if isn't 
   * @throws Exception
   */
  public boolean isUserWatched(String userId, String cateId);

  /**
   * set default avatar for an user
   * @param userName username 
   * @throws Exception
   */
  public void setDefaultAvatar(String userName) throws Exception;

  /**
   * Get list pending questions in a category
   * @param categoryId id of category
   * @param faqSetting settings
   * @return question page list 
   * @throws Exception
   */
  public QuestionPageList getPendingQuestionsByCategory(String categoryId, FAQSetting faqSetting) throws Exception;

  /**
   * Export category to stream
   * @param categoryId id of category
   * @param createZipFile create zip file or not
   * @return input stream of category 
   * @throws Exception
   */
  public InputStream exportData(String categoryId, boolean createZipFile) throws Exception;

  /**
   * Check a path exist or not
   * @param path path need check
   * @return exist or not 
   * @throws Exception
   */
  public boolean isExisting(String path) throws Exception;

  /**
   * get path of Category by id
   * @param id category id 
   * @return path of category
   * @throws Exception
   */
  public String getCategoryPathOf(String id) throws Exception;

  /**
   * Get titles of questions active
   * @param paths  path of questions
   * @return list titles of questions 
   * @throws Exception
   */
  public Map<String, String> getRelationQuestion(List<String> paths) throws Exception;
  
  /**
   * Get titles of questions
   * @param paths  path of questions
   * @return list titles of questions 
   * @throws Exception
   */
  public List<String> getQuestionContents(List<String> paths) throws Exception;

  /**
   * Check moderate answer or not
   * @param id id of category
   * @return answer or not 
   * @throws Exception
   */
  public boolean isModerateAnswer(String id) throws Exception;

  /**
   * Get question node
   * @param path id of question
   * @return question node 
   * @throws Exception
   */
  public Node getQuestionNodeById(String path) throws Exception;

  /**
   * Get name of parent category
   * @param path id of category
   * @return name of parent category 
   * @throws Exception
   */
  public String getParentCategoriesName(String path) throws Exception;

  /**
   * Get email addresses that watch in a category
   * @param categoryId id of category
   * @return question page list 
   * @throws Exception
   */
  public QuestionPageList getListMailInWatch(String categoryId) throws Exception;

  /**
   * Check user is moderator or not
   * @param categoryId id of category
   * @param user username
   * @return user is moderator or not. The current user is implied if user is null.
   * @throws Exception
   */
  public boolean isCategoryModerator(String categoryId, String user) throws Exception;

  /**
   * Add language to a question
   * @param questionPath patch of question
   * @param language question language 
   * @throws Exception
   */
  public void addLanguage(String questionPath, QuestionLanguage language) throws Exception;

  /**
   * Delete language in a answer
   * @param questionPath path of question
   * @param answerId id of answer
   * @param language deleted language 
   * @throws Exception
   */
  public void deleteAnswerQuestionLang(String questionPath, String answerId, String language) throws Exception;

  /**
   * Delete language in a comment
   * @param questionPath path of question
   * @param commentId id of comment
   * @param language deleted language 
   * @throws Exception
   */
  public void deleteCommentQuestionLang(String questionPath, String commentId, String language) throws Exception;

  /**
   * Get language of question
   * @param questionPath path of question
   * @param language got language
   * @return Language of question 
   * @throws Exception
   */
  public QuestionLanguage getQuestionLanguageByLanguage(String questionPath, String language) throws Exception;

  /**
   * Get Comment of question
   * @param questionPath path of question
   * @param commentId id of comment
   * @param language
   * @return comment of question 
   * @throws Exception
   */
  public Comment getCommentById(String questionPath, String commentId, String language) throws Exception;

  /**
   * Get answer object
   * @param questionPath path of question
   * @param answerid id of answer
   * @param language  
   * @return answer has inputed id  
   * @throws Exception
   */
  public Answer getAnswerById(String questionPath, String answerid, String language) throws Exception;

  /**
   * Save an answer of question
   * @param questionPath path of question
   * @param answer object answer want to save
   * @param languge language of answer 
   * @throws Exception
   */
  public void saveAnswer(String questionPath, Answer answer, String languge) throws Exception;

  /**
   * Save an answer of question
   * @param questionPath path of question
   * @param questionLanguage language of answer 
   * @throws Exception
   */
  public void saveAnswer(String questionPath, QuestionLanguage questionLanguage) throws Exception;

  /**
   * Save comment of a question
   * @param questionPath path of question
   * @param comment comment want to save
   * @param languge language of comment 
   * @throws Exception
   */
  public void saveComment(String questionPath, Comment comment, String languge) throws Exception;

  /**
   * Remove languages from question
   * @param questionPath path of question
   * @param listLanguage removed languages 
   * @throws Exception
   */
  public void removeLanguage(String questionPath, List<String> listLanguage);

  /**
   * vote for an answer
   * @param answerPath path of answer
   * @param userName username of user vote for answer
   * @param isUp up or not 
   * @throws Exception
   */
  public void voteAnswer(String answerPath, String userName, boolean isUp) throws Exception;

  /**
   * vote for a question
   * @param questionPath path of question
   * @param userName username of user vote for question
   * @param number value user vote 
   * @throws Exception
   */
  public void voteQuestion(String questionPath, String userName, int number) throws Exception;

  /**
   * Get moderators of question or category
   * @param path path of question or category
   * @return array users are moderator 
   * @throws Exception
   */
  public String[] getModeratorsOf(String path) throws Exception;

  /**
   * Remove vote for question
   * @param questionPath path of question
   * @param userName username remove vote 
   * @throws Exception
   */
  public void unVoteQuestion(String questionPath, String userName) throws Exception;

  /**
   * Check view author information or not
   * @param id id of question
   * @return is view author information or not
   */
  public boolean isViewAuthorInfo(String id);

  /**
   * Get number of categories
   * @return number of categories 
   * @throws Exception
   */
  public long existingCategories() throws Exception;

  /**
   * Get name of category
   * @param categoryPath path of category
   * @return name of category
   * @throws Exception
   */
  public String getCategoryNameOf(String categoryPath) throws Exception;

  /**
   * Get quick questions
   * @param listCategoryId id of some categories
   * @param isNotYetAnswer is answer or not
   * @return list of questions 
   * @throws Exception
   */
  public List<Question> getQuickQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception;

  /**
   * Get list of categories
   * @return list of categories 
   * @throws Exception
   */
  public List<Cate> listingCategoryTree() throws Exception;

  /**
   * Get list of watches
   * @param categoryId id of category
   * @return list of watches in a category 
   * @throws Exception
   */
  public List<Watch> getWatchByCategory(String categoryId) throws Exception;

  /**
   * Get information has watch or not
   * @param categoryPath path of category
   * @return has watch or has not 
   * @throws Exception
   */
  public boolean hasWatch(String categoryPath);

  /**
   * Get informations about category
   * @param categoryPath path of category
   * @param categoryIdScoped list sub of category
   * @return informations in CategoryInfo object 
   * @throws Exception
   */
  public CategoryInfo getCategoryInfo(String categoryPath, List<String> categoryIdScoped) throws Exception;
  
  public CategoryInfo loadMore(LoadMoreType type, String categoryPath, List<String> categoryIdScoped, int offset, int limit) throws Exception;

  /**
   * Get template of questions
   * @return template 
   * @throws Exception
   */
  public byte[] getTemplate() throws Exception;

  /**
   * Save a template
   * @param str template 
   * @throws Exception
   */
  public void saveTemplate(String str) throws Exception;

  /**
   * Check category is exist or not
   * @param name name of category
   * @param path path of category
   * @return is exist or not 
   * @throws Exception
   */
  public boolean isCategoryExist(String name, String path);

  /**
   * Update relatives for a question
   * @param questionPath path of question
   * @param relatives input relatives 
   * @throws Exception
   */
  public void updateQuestionRelatives(String questionPath, String[] relatives) throws Exception;

  /**
   * Check question has moderator or not
   * @param id id of question
   * @return is moderate or not 
   * @throws Exception
   */
  public boolean isModerateQuestion(String id) throws Exception;

  /**
   * Create RSS for answer
   * @param cateId id of category
   * @return stream of answer rss 
   * @throws Exception
   */
  public InputStream createAnswerRSS(String cateId) throws Exception;

  /**
   * Save last active information of question 
   * @param absPathOfItem path of question 
   * @throws Exception
   */
  public void reCalculateLastActivityOfQuestion(String absPathOfItem) throws Exception;

  /**
   * Add listener for answer 
   * @param listener answer event listener 
   * @throws Exception
   */
  public void addListenerPlugin(AnswerEventListener listener) throws Exception;

  /**
   * Get comments of a question 
   * @param questionId id of question
   * @return comments of question 
   * @throws Exception
   */
  public Comment[] getComments(String questionId) throws Exception;

  public void calculateDeletedUser(String userName) throws Exception;
  
  /**
   * read property of the category by its name
   * @param categoryId id of the category
   * @param propertyName name of the property
   * @param returnType expected return-type. The supported class are String[], String, Long, Boolean, Double and Date .  
   * @return 
   * @throws Exception
   */
  public Object readCategoryProperty(String categoryId, String propertyName, Class returnType) throws Exception;
  
  /**
   * read property of the question by its name
   * @param questionId id of the question
   * @param propertyName name of the property
   * @param returnType expected return-type. The supported class are String[], String, Long, Boolean, Double and Date.
   * @return
   * @throws Exception
   */
  public Object readQuestionProperty(String questionId, String propertyName, Class returnType) throws Exception;
  
}