/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 18, 2009  
 */
public interface DataStorage {

  void init(KSDataLocation location);

  void addPlugin(ComponentPlugin plugin) throws Exception;

  void addRolePlugin(ComponentPlugin plugin) throws Exception;

  /**
   * 
   * @param userName userName
   * @return true if userName has admin role. The current user is implied if userName is null.
   * @throws Exception
   */
  boolean isAdminRole(String userName) throws Exception;

  List<String> getAllFAQAdmin() throws Exception;

  void getUserSetting(String userName, FAQSetting faqSetting) throws Exception;

  void saveFAQSetting(FAQSetting faqSetting, String userName) throws Exception;

  FileAttachment getUserAvatar(String userName) throws Exception;

  void saveUserAvatar(String userId, FileAttachment fileAttachment) throws Exception;

  void setDefaultAvatar(String userName) throws Exception;

  void reInitQuestionNodeListeners() throws Exception;

  boolean initRootCategory() throws Exception;

  byte[] getTemplate() throws Exception;

  void saveTemplate(String str) throws Exception;

  Iterator<NotifyInfo> getPendingMessages();

  List<QuestionLanguage> getQuestionLanguages(String questionId);

  void deleteAnswer(String questionId, String answerId) throws Exception;

  void deleteComment(String questionId, String commentId) throws Exception;

  JCRPageList getPageListAnswer(String questionId, boolean isSortByVote) throws Exception;

  void saveAnswer(String questionId, Answer answer, boolean isNew) throws Exception;

  void saveAnswer(String questionId, Answer[] answers) throws Exception;

  void saveComment(String questionId, Comment comment, boolean isNew) throws Exception;

  void saveAnswerQuestionLang(String questionId, Answer answer, String language, boolean isNew) throws Exception;

  Answer getAnswerById(String questionId, String answerid) throws Exception;

  JCRPageList getPageListComment(String questionId) throws Exception;

  Comment getCommentById(String questionId, String commentId) throws Exception;

  Node saveQuestion(Question question, boolean isAddNew, FAQSetting faqSetting) throws Exception;

  void removeQuestion(String questionId) throws Exception;

  Comment getCommentById(Node questionNode, String commentId) throws Exception;

  Question getQuestionById(String questionId) throws Exception;

  QuestionPageList getAllQuestions() throws Exception;

  QuestionPageList getQuestionsNotYetAnswer(String categoryId, boolean isApproved) throws Exception;

  QuestionPageList getPendingQuestionsByCategory(String categoryId, FAQSetting faqSetting) throws Exception;

  QuestionPageList getQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception;

  QuestionPageList getAllQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception;

  QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception;

  List<Question> getQuickQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception;

  String getCategoryPathOfQuestion(String questionPath) throws Exception;

  void moveQuestions(List<String> questions, String destCategoryId, String questionLink, FAQSetting faqSetting) throws Exception;

  void changeStatusCategoryView(List<String> listCateIds) throws Exception;

  long getMaxindexCategory(String parentId) throws Exception;

  void saveCategory(String parentId, Category cat, boolean isAddNew);

  List<Cate> listingCategoryTree() throws Exception;

  void removeCategory(String categoryId) throws Exception;

  /**
   * 
   * @param categoryId the path of the category starting from home. It should be in the form of "categories/CategoryXXX/CategoryXXX"
   * @return
   * @throws Exception
   */
  Category getCategoryById(String categoryId) throws Exception;

  List<Category> findCategoriesByName(String categoryName) throws Exception;

  List<String> getListCateIdByModerator(String user) throws Exception;

  List<Category> getAllCategories() throws Exception;

  Object readQuestionProperty(String questionId, String propertyName, Class returnType) throws Exception;

  Object readCategoryProperty(String categoryId, String propertyName, Class returnType) throws Exception;
  
  long existingCategories() throws Exception;

  Node getCategoryNodeById(String categoryId) throws Exception;

  List<Category> getSubCategories(String categoryId, FAQSetting faqSetting, boolean isGetAll, List<String> limitedUsers) throws Exception;

  long[] getCategoryInfo(String categoryId, FAQSetting faqSetting) throws Exception;

  void moveCategory(String categoryId, String destCategoryId) throws Exception;

  void addWatchCategory(String id, Watch watch) throws Exception;

  // TODO Going to remove
  QuestionPageList getListMailInWatch(String categoryId) throws Exception;

  List<Watch> getWatchByCategory(String categoryId) throws Exception;

  boolean hasWatch(String categoryPath);

  void addWatchQuestion(String questionId, Watch watch, boolean isNew) throws Exception;

  List<Watch> getWatchByQuestion(String questionId) throws Exception;

  QuestionPageList getWatchedCategoryByUser(String userId) throws Exception;

  boolean isUserWatched(String userId, String cateId);

  List<String> getWatchedSubCategory(String userId, String cateId) throws Exception;

  QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser) throws Exception;

  // Going to remove
  void deleteCategoryWatch(String categoryId, String user) throws Exception;

  void unWatchCategory(String categoryId, String user) throws Exception;

  void unWatchQuestion(String questionId, String user) throws Exception;

  List<ObjectSearchResult> getSearchResults(FAQEventQuery eventQuery) throws Exception;

  List<String> getCategoryPath(String categoryId) throws Exception;

  String getParentCategoriesName(String path) throws Exception;

  NotifyInfo getMessageInfo(String name) throws Exception;

  void swapCategories(String cateId1, String cateId2) throws Exception;

  void saveTopicIdDiscussQuestion(String questionId, String topicId) throws Exception;

  InputStream exportData(String categoryId, boolean createZipFile) throws Exception;

  boolean importData(String parentId, InputStream inputStream, boolean isZip) throws Exception;

  boolean isExisting(String path) throws Exception;

  String getCategoryPathOf(String id) throws Exception;

  boolean isModerateAnswer(String id) throws Exception;

  boolean isModerateQuestion(String id) throws Exception;

  boolean isViewAuthorInfo(String id);

  /**
   * 
   * @param categoryId id of category
   * @param user username
   * @return true if user is moderator of the category. The current user is implied if user is null.
   * @throws Exception
   */
  boolean isCategoryModerator(String categoryId, String user) throws Exception;

  boolean isCategoryExist(String name, String path);

  List<String> getQuestionContents(List<String> paths) throws Exception;

  Map<String, String> getRelationQuestion(List<String> paths) throws Exception;

  // will be remove
  Node getQuestionNodeById(String path) throws Exception;

  String[] getModeratorsOf(String path) throws Exception;

  String getCategoryNameOf(String categoryPath) throws Exception;

  CategoryInfo getCategoryInfo(String categoryPath, List<String> categoryIdScoped) throws Exception;

  void reCalculateInfoOfQuestion(String absPathOfProp) throws Exception;

  void updateQuestionRelatives(String questionPath, String[] relatives) throws Exception;

  void calculateDeletedUser(String userName) throws Exception;

  InputStream createAnswerRSS(String cateId) throws Exception;

  Comment[] getComments(String questionId) throws Exception;

  Node getFAQServiceHome(SessionProvider sProvider) throws Exception;
  
}
