/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.bench.wiki;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.CategoryInfo;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.InitialDataPlugin;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.ObjectSearchResult;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.service.impl.AnswerEventListener;
import org.exoplatform.ks.common.NotifyInfo;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * May 28, 2012  
 */
public class MockFAQService implements FAQService {

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#addPlugin(org.exoplatform.container.component.ComponentPlugin)
   */
  @Override
  public void addPlugin(ComponentPlugin plugin) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#addInitialDataPlugin(org.exoplatform.faq.service.InitialDataPlugin)
   */
  @Override
  public void addInitialDataPlugin(InitialDataPlugin plugin) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#saveCategory(java.lang.String, org.exoplatform.faq.service.Category, boolean)
   */
  @Override
  public void saveCategory(String parentId, Category cat, boolean isAddNew) {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#changeStatusCategoryView(java.util.List)
   */
  @Override
  public void changeStatusCategoryView(List<String> listCateIds) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#removeCategory(java.lang.String)
   */
  @Override
  public void removeCategory(String categoryId) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getCategoryById(java.lang.String)
   */
  @Override
  public Category getCategoryById(String categoryId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getAllCategories()
   */
  @Override
  public List<Category> getAllCategories() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getListCateIdByModerator(java.lang.String)
   */
  @Override
  public List<String> getListCateIdByModerator(String user) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getSubCategories(java.lang.String, org.exoplatform.faq.service.FAQSetting, boolean, java.util.List)
   */
  @Override
  public List<Category> getSubCategories(String categoryId, FAQSetting faqSetting, boolean isGetAll, List<String> userView) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#moveCategory(java.lang.String, java.lang.String)
   */
  @Override
  public void moveCategory(String categoryId, String destCategoryId) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#saveQuestion(org.exoplatform.faq.service.Question, boolean, org.exoplatform.faq.service.FAQSetting)
   */
  @Override
  public Node saveQuestion(Question question, boolean isAddNew, FAQSetting faqSetting) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#removeQuestion(java.lang.String)
   */
  @Override
  public void removeQuestion(String questionId) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getQuestionById(java.lang.String)
   */
  @Override
  public Question getQuestionById(String questionId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getAllQuestions()
   */
  @Override
  public QuestionPageList getAllQuestions() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getQuestionsNotYetAnswer(java.lang.String, boolean)
   */
  @Override
  public QuestionPageList getQuestionsNotYetAnswer(String categoryId, boolean isApproved) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getQuestionsByCatetory(java.lang.String, org.exoplatform.faq.service.FAQSetting)
   */
  @Override
  public QuestionPageList getQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getAllQuestionsByCatetory(java.lang.String, org.exoplatform.faq.service.FAQSetting)
   */
  @Override
  public QuestionPageList getAllQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getCategoryInfo(java.lang.String, org.exoplatform.faq.service.FAQSetting)
   */
  @Override
  public long[] getCategoryInfo(String categoryId, FAQSetting setting) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getQuestionsByListCatetory(java.util.List, boolean)
   */
  @Override
  public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getCategoryPathOfQuestion(java.lang.String)
   */
  @Override
  public String getCategoryPathOfQuestion(String categoryId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getQuestionLanguages(java.lang.String)
   */
  @Override
  public List<QuestionLanguage> getQuestionLanguages(String questionId) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#moveQuestions(java.util.List, java.lang.String, java.lang.String, org.exoplatform.faq.service.FAQSetting)
   */
  @Override
  public void moveQuestions(List<String> questions, String destCategoryId, String questionLink, FAQSetting faqSetting) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#saveFAQSetting(org.exoplatform.faq.service.FAQSetting, java.lang.String)
   */
  @Override
  public void saveFAQSetting(FAQSetting faqSetting, String userName) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#addWatchCategory(java.lang.String, org.exoplatform.faq.service.Watch)
   */
  @Override
  public void addWatchCategory(String id, Watch watch) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#deleteCategoryWatch(java.lang.String, java.lang.String)
   */
  @Override
  public void deleteCategoryWatch(String categoryId, String user) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#unWatchCategory(java.lang.String, java.lang.String)
   */
  @Override
  public void unWatchCategory(String categoryId, String userCurrent) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#unWatchQuestion(java.lang.String, java.lang.String)
   */
  @Override
  public void unWatchQuestion(String questionID, String userCurrent) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getSearchResults(org.exoplatform.faq.service.FAQEventQuery)
   */
  @Override
  public List<ObjectSearchResult> getSearchResults(FAQEventQuery eventQuery) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getCategoryPath(java.lang.String)
   */
  @Override
  public List<String> getCategoryPath(String categoryId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getPendingMessages()
   */
  @Override
  public Iterator<NotifyInfo> getPendingMessages() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#addLanguage(javax.jcr.Node, org.exoplatform.faq.service.QuestionLanguage)
   */
  @Override
  public void addLanguage(Node questionNode, QuestionLanguage language) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getUserSetting(java.lang.String, org.exoplatform.faq.service.FAQSetting)
   */
  @Override
  public void getUserSetting(String userName, FAQSetting faqSetting) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getMessageInfo(java.lang.String)
   */
  @Override
  public NotifyInfo getMessageInfo(String name) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#isAdminRole(java.lang.String)
   */
  @Override
  public boolean isAdminRole(String userName) throws Exception {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getAllFAQAdmin()
   */
  @Override
  public List<String> getAllFAQAdmin() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#addRolePlugin(org.exoplatform.container.component.ComponentPlugin)
   */
  @Override
  public void addRolePlugin(ComponentPlugin plugin) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#addWatchQuestion(java.lang.String, org.exoplatform.faq.service.Watch, boolean)
   */
  @Override
  public void addWatchQuestion(String questionId, Watch watch, boolean isNew) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#saveTopicIdDiscussQuestion(java.lang.String, java.lang.String)
   */
  @Override
  public void saveTopicIdDiscussQuestion(String questionId, String pathDiscuss) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getListQuestionsWatch(org.exoplatform.faq.service.FAQSetting, java.lang.String)
   */
  @Override
  public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getCategoryNodeById(java.lang.String)
   */
  @Override
  public Node getCategoryNodeById(String categoryId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#importData(java.lang.String, java.io.InputStream, boolean)
   */
  @Override
  public boolean importData(String categoryId, InputStream inputStream, boolean isZip) throws Exception {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#swapCategories(java.lang.String, java.lang.String)
   */
  @Override
  public void swapCategories(String cateId1, String cateId2) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getMaxindexCategory(java.lang.String)
   */
  @Override
  public long getMaxindexCategory(String parentId) throws Exception {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#deleteAnswer(java.lang.String, java.lang.String)
   */
  @Override
  public void deleteAnswer(String questionId, String answerId) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#deleteComment(java.lang.String, java.lang.String)
   */
  @Override
  public void deleteComment(String questionId, String commentId) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#saveAnswer(java.lang.String, org.exoplatform.faq.service.Answer, boolean)
   */
  @Override
  public void saveAnswer(String questionId, Answer answer, boolean isNew) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#saveComment(java.lang.String, org.exoplatform.faq.service.Comment, boolean)
   */
  @Override
  public void saveComment(String questionId, Comment comment, boolean isNew) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getCommentById(java.lang.String, java.lang.String)
   */
  @Override
  public Comment getCommentById(String questionId, String commentId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getAnswerById(java.lang.String, java.lang.String)
   */
  @Override
  public Answer getAnswerById(String questionId, String answerid) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#saveAnswer(java.lang.String, org.exoplatform.faq.service.Answer[])
   */
  @Override
  public void saveAnswer(String questionId, Answer[] answers) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getPageListComment(java.lang.String)
   */
  @Override
  public JCRPageList getPageListComment(String questionId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getPageListAnswer(java.lang.String, boolean)
   */
  @Override
  public JCRPageList getPageListAnswer(String questionId, boolean isSortByVote) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getWatchedCategoryByUser(java.lang.String)
   */
  @Override
  public QuestionPageList getWatchedCategoryByUser(String userId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getUserAvatar(java.lang.String)
   */
  @Override
  public FileAttachment getUserAvatar(String userName) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#saveUserAvatar(java.lang.String, org.exoplatform.faq.service.FileAttachment)
   */
  @Override
  public void saveUserAvatar(String userId, FileAttachment fileAttachment) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#isUserWatched(java.lang.String, java.lang.String)
   */
  @Override
  public boolean isUserWatched(String userId, String cateId) {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#setDefaultAvatar(java.lang.String)
   */
  @Override
  public void setDefaultAvatar(String userName) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getPendingQuestionsByCategory(java.lang.String, org.exoplatform.faq.service.FAQSetting)
   */
  @Override
  public QuestionPageList getPendingQuestionsByCategory(String categoryId, FAQSetting faqSetting) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#exportData(java.lang.String, boolean)
   */
  @Override
  public InputStream exportData(String categoryId, boolean createZipFile) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#isExisting(java.lang.String)
   */
  @Override
  public boolean isExisting(String path) throws Exception {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getCategoryPathOf(java.lang.String)
   */
  @Override
  public String getCategoryPathOf(String id) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getRelationQuestion(java.util.List)
   */
  @Override
  public Map<String, String> getRelationQuestion(List<String> paths) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getQuestionContents(java.util.List)
   */
  @Override
  public List<String> getQuestionContents(List<String> paths) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#isModerateAnswer(java.lang.String)
   */
  @Override
  public boolean isModerateAnswer(String id) throws Exception {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getQuestionNodeById(java.lang.String)
   */
  @Override
  public Node getQuestionNodeById(String path) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getParentCategoriesName(java.lang.String)
   */
  @Override
  public String getParentCategoriesName(String path) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getListMailInWatch(java.lang.String)
   */
  @Override
  public QuestionPageList getListMailInWatch(String categoryId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#isCategoryModerator(java.lang.String, java.lang.String)
   */
  @Override
  public boolean isCategoryModerator(String categoryId, String user) throws Exception {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#addLanguage(java.lang.String, org.exoplatform.faq.service.QuestionLanguage)
   */
  @Override
  public void addLanguage(String questionPath, QuestionLanguage language) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#deleteAnswerQuestionLang(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void deleteAnswerQuestionLang(String questionPath, String answerId, String language) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#deleteCommentQuestionLang(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void deleteCommentQuestionLang(String questionPath, String commentId, String language) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getQuestionLanguageByLanguage(java.lang.String, java.lang.String)
   */
  @Override
  public QuestionLanguage getQuestionLanguageByLanguage(String questionPath, String language) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getCommentById(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Comment getCommentById(String questionPath, String commentId, String language) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getAnswerById(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Answer getAnswerById(String questionPath, String answerid, String language) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#saveAnswer(java.lang.String, org.exoplatform.faq.service.Answer, java.lang.String)
   */
  @Override
  public void saveAnswer(String questionPath, Answer answer, String languge) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#saveAnswer(java.lang.String, org.exoplatform.faq.service.QuestionLanguage)
   */
  @Override
  public void saveAnswer(String questionPath, QuestionLanguage questionLanguage) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#saveComment(java.lang.String, org.exoplatform.faq.service.Comment, java.lang.String)
   */
  @Override
  public void saveComment(String questionPath, Comment comment, String languge) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#removeLanguage(java.lang.String, java.util.List)
   */
  @Override
  public void removeLanguage(String questionPath, List<String> listLanguage) {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#voteAnswer(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public void voteAnswer(String answerPath, String userName, boolean isUp) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#voteQuestion(java.lang.String, java.lang.String, int)
   */
  @Override
  public void voteQuestion(String questionPath, String userName, int number) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getModeratorsOf(java.lang.String)
   */
  @Override
  public String[] getModeratorsOf(String path) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#unVoteQuestion(java.lang.String, java.lang.String)
   */
  @Override
  public void unVoteQuestion(String questionPath, String userName) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#isViewAuthorInfo(java.lang.String)
   */
  @Override
  public boolean isViewAuthorInfo(String id) {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#existingCategories()
   */
  @Override
  public long existingCategories() throws Exception {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getCategoryNameOf(java.lang.String)
   */
  @Override
  public String getCategoryNameOf(String categoryPath) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getQuickQuestionsByListCatetory(java.util.List, boolean)
   */
  @Override
  public List<Question> getQuickQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#listingCategoryTree()
   */
  @Override
  public List<Cate> listingCategoryTree() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getWatchByCategory(java.lang.String)
   */
  @Override
  public List<Watch> getWatchByCategory(String categoryId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#hasWatch(java.lang.String)
   */
  @Override
  public boolean hasWatch(String categoryPath) {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getCategoryInfo(java.lang.String, java.util.List)
   */
  @Override
  public CategoryInfo getCategoryInfo(String categoryPath, List<String> categoryIdScoped) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getTemplate()
   */
  @Override
  public byte[] getTemplate() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#saveTemplate(java.lang.String)
   */
  @Override
  public void saveTemplate(String str) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#isCategoryExist(java.lang.String, java.lang.String)
   */
  @Override
  public boolean isCategoryExist(String name, String path) {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#updateQuestionRelatives(java.lang.String, java.lang.String[])
   */
  @Override
  public void updateQuestionRelatives(String questionPath, String[] relatives) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#isModerateQuestion(java.lang.String)
   */
  @Override
  public boolean isModerateQuestion(String id) throws Exception {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#createAnswerRSS(java.lang.String)
   */
  @Override
  public InputStream createAnswerRSS(String cateId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#reCalculateLastActivityOfQuestion(java.lang.String)
   */
  @Override
  public void reCalculateLastActivityOfQuestion(String absPathOfItem) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#addListenerPlugin(org.exoplatform.faq.service.impl.AnswerEventListener)
   */
  @Override
  public void addListenerPlugin(AnswerEventListener listener) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#getComments(java.lang.String)
   */
  @Override
  public Comment[] getComments(String questionId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#calculateDeletedUser(java.lang.String)
   */
  @Override
  public void calculateDeletedUser(String userName) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#readCategoryProperty(java.lang.String, java.lang.String, java.lang.Class)
   */
  @Override
  public Object readCategoryProperty(String categoryId, String propertyName, Class returnType) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.FAQService#readQuestionProperty(java.lang.String, java.lang.String, java.lang.Class)
   */
  @Override
  public Object readQuestionProperty(String questionId, String propertyName, Class returnType) throws Exception {
    return null;
  }

}
