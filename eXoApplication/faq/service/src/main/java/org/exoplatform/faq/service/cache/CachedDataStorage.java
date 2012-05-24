package org.exoplatform.faq.service.cache;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.faq.cache.FAQMarkupData;
import org.exoplatform.faq.cache.FAQMarkupKey;
import org.exoplatform.faq.service.*;
import org.exoplatform.faq.service.impl.JCRDataStorage;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.ks.common.cache.CacheType;
import org.exoplatform.ks.common.cache.selector.ScopeCacheSelector;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.jcr.Node;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class CachedDataStorage implements DataStorage {

  private final DataStorage storage;
  private final ExoCache<FAQMarkupKey, FAQMarkupData> faqMarkup;

  private static final Log LOG = ExoLogger.getLogger(CachedDataStorage.class);

  public CachedDataStorage(JCRDataStorage storage, CacheService service) {
    
    this.storage = storage;
    this.faqMarkup = CacheType.FAQ_MARKUP.getFromService(service);

  }

  private void cleanCache() {
    try {
      faqMarkup.select(new ScopeCacheSelector<FAQMarkupKey, FAQMarkupData>());
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  public void init(KSDataLocation location) {
    storage.init(location);
  }

  public void addPlugin(ComponentPlugin plugin) throws Exception {
    storage.addPlugin(plugin);
  }

  public void addRolePlugin(ComponentPlugin plugin) throws Exception {
    storage.addRolePlugin(plugin);
  }

  public boolean isAdminRole(String userName) throws Exception {
    return storage.isAdminRole(userName);
  }

  public List<String> getAllFAQAdmin() throws Exception {
    return storage.getAllFAQAdmin();
  }

  public void getUserSetting(String userName, FAQSetting faqSetting) throws Exception {
    storage.getUserSetting(userName, faqSetting);
  }

  public void saveFAQSetting(FAQSetting faqSetting, String userName) throws Exception {
    storage.saveFAQSetting(faqSetting, userName);
    cleanCache();
  }

  public FileAttachment getUserAvatar(String userName) throws Exception {
    return storage.getUserAvatar(userName);
  }

  public void saveUserAvatar(String userId, FileAttachment fileAttachment) throws Exception {
    storage.saveUserAvatar(userId, fileAttachment);
    cleanCache();
  }

  public void setDefaultAvatar(String userName) throws Exception {
    storage.setDefaultAvatar(userName);
    cleanCache();
  }

  public void reInitQuestionNodeListeners() throws Exception {
    storage.reInitQuestionNodeListeners();
  }

  public boolean initRootCategory() throws Exception {
    boolean b = storage.initRootCategory();
    cleanCache();
    return b;
  }

  public byte[] getTemplate() throws Exception {
    return storage.getTemplate();
  }

  public void saveTemplate(String str) throws Exception {
    storage.saveTemplate(str);
    cleanCache();
  }

  public Iterator<NotifyInfo> getPendingMessages() {
    return storage.getPendingMessages();
  }

  public List<QuestionLanguage> getQuestionLanguages(String questionId) {
    return storage.getQuestionLanguages(questionId);
  }

  public void deleteAnswer(String questionId, String answerId) throws Exception {
    storage.deleteAnswer(questionId, answerId);
    cleanCache();
  }

  public void deleteComment(String questionId, String commentId) throws Exception {
    storage.deleteComment(questionId, commentId);
    cleanCache();
  }

  public JCRPageList getPageListAnswer(String questionId, boolean isSortByVote) throws Exception {
    return storage.getPageListAnswer(questionId, isSortByVote);
  }

  public void saveAnswer(String questionId, Answer answer, boolean isNew) throws Exception {
    storage.saveAnswer(questionId, answer, isNew);
    cleanCache();
  }

  public void saveAnswer(String questionId, Answer[] answers) throws Exception {
    storage.saveAnswer(questionId, answers);
    cleanCache();
  }

  public void saveComment(String questionId, Comment comment, boolean isNew) throws Exception {
    storage.saveComment(questionId, comment, isNew);
    cleanCache();
  }

  public void saveAnswerQuestionLang(String questionId, Answer answer, String language, boolean isNew) throws Exception {
    storage.saveAnswerQuestionLang(questionId, answer, language, isNew);
    cleanCache();
  }

  public Answer getAnswerById(String questionId, String answerid) throws Exception {
    return storage.getAnswerById(questionId, answerid);
  }

  public JCRPageList getPageListComment(String questionId) throws Exception {
    return storage.getPageListComment(questionId);
  }

  public Comment getCommentById(String questionId, String commentId) throws Exception {
    return storage.getCommentById(questionId, commentId);
  }

  public Node saveQuestion(Question question, boolean isAddNew, FAQSetting faqSetting) throws Exception {
    Node node = storage.saveQuestion(question, isAddNew, faqSetting);
    cleanCache();
    return node;
  }

  public void removeQuestion(String questionId) throws Exception {
    storage.removeQuestion(questionId);
    cleanCache();
  }

  public Comment getCommentById(Node questionNode, String commentId) throws Exception {
    return storage.getCommentById(questionNode, commentId);
  }

  public Question getQuestionById(String questionId) throws Exception {
    return storage.getQuestionById(questionId);
  }

  public QuestionPageList getAllQuestions() throws Exception {
    return storage.getAllQuestions();
  }

  public QuestionPageList getQuestionsNotYetAnswer(String categoryId, boolean isApproved) throws Exception {
    return storage.getQuestionsNotYetAnswer(categoryId, isApproved);
  }

  public QuestionPageList getPendingQuestionsByCategory(String categoryId, FAQSetting faqSetting) throws Exception {
    return storage.getPendingQuestionsByCategory(categoryId, faqSetting);
  }

  public QuestionPageList getQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
    return storage.getQuestionsByCatetory(categoryId, faqSetting);
  }

  public QuestionPageList getAllQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
    return storage.getAllQuestionsByCatetory(categoryId, faqSetting);
  }

  public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
    return storage.getQuestionsByListCatetory(listCategoryId, isNotYetAnswer);
  }

  public List<Question> getQuickQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
    return storage.getQuickQuestionsByListCatetory(listCategoryId, isNotYetAnswer);
  }

  public String getCategoryPathOfQuestion(String questionPath) throws Exception {
    return storage.getCategoryPathOfQuestion(questionPath);
  }

  public void moveQuestions(List<String> questions, String destCategoryId, String questionLink, FAQSetting faqSetting) throws Exception {
    storage.moveQuestions(questions, destCategoryId, questionLink, faqSetting);
    cleanCache();
  }

  public void changeStatusCategoryView(List<String> listCateIds) throws Exception {
    storage.changeStatusCategoryView(listCateIds);
    cleanCache();
  }

  public long getMaxindexCategory(String parentId) throws Exception {
    return storage.getMaxindexCategory(parentId);
  }

  public void saveCategory(String parentId, Category cat, boolean isAddNew) {
    storage.saveCategory(parentId, cat, isAddNew);
    cleanCache();
  }

  public List<Cate> listingCategoryTree() throws Exception {
    return storage.listingCategoryTree();
  }

  public void removeCategory(String categoryId) throws Exception {
    storage.removeCategory(categoryId);
    cleanCache();
  }

  public Category getCategoryById(String categoryId) throws Exception {
    return storage.getCategoryById(categoryId);
  }

  public List<Category> findCategoriesByName(String categoryName) throws Exception {
    return storage.findCategoriesByName(categoryName);
  }

  public List<String> getListCateIdByModerator(String user) throws Exception {
    return storage.getListCateIdByModerator(user);
  }

  public List<Category> getAllCategories() throws Exception {
    return storage.getAllCategories();
  }

  public Object readQuestionProperty(String questionId, String propertyName, Class returnType) throws Exception {
    return storage.readQuestionProperty(questionId, propertyName, returnType);
  }

  public Object readCategoryProperty(String categoryId, String propertyName, Class returnType) throws Exception {
    return storage.readCategoryProperty(categoryId, propertyName, returnType);
  }

  public long existingCategories() throws Exception {
    return storage.existingCategories();
  }

  public Node getCategoryNodeById(String categoryId) throws Exception {
    return storage.getCategoryNodeById(categoryId);
  }

  public List<Category> getSubCategories(String categoryId, FAQSetting faqSetting, boolean isGetAll, List<String> limitedUsers) throws Exception {
    return storage.getSubCategories(categoryId, faqSetting, isGetAll, limitedUsers);
  }

  public long[] getCategoryInfo(String categoryId, FAQSetting faqSetting) throws Exception {
    return storage.getCategoryInfo(categoryId, faqSetting);
  }

  public void moveCategory(String categoryId, String destCategoryId) throws Exception {
    storage.moveCategory(categoryId, destCategoryId);
    cleanCache();
  }

  public void addWatchCategory(String id, Watch watch) throws Exception {
    storage.addWatchCategory(id, watch);
    cleanCache();
  }

  public QuestionPageList getListMailInWatch(String categoryId) throws Exception {
    return storage.getListMailInWatch(categoryId);
  }

  public List<Watch> getWatchByCategory(String categoryId) throws Exception {
    return storage.getWatchByCategory(categoryId);
  }

  public boolean hasWatch(String categoryPath) {
    return storage.hasWatch(categoryPath);
  }

  public void addWatchQuestion(String questionId, Watch watch, boolean isNew) throws Exception {
    storage.addWatchQuestion(questionId, watch, isNew);
    cleanCache();
  }

  public List<Watch> getWatchByQuestion(String questionId) throws Exception {
    return storage.getWatchByQuestion(questionId);
  }

  public QuestionPageList getWatchedCategoryByUser(String userId) throws Exception {
    return storage.getWatchedCategoryByUser(userId);
  }

  public boolean isUserWatched(String userId, String cateId) {
    return storage.isUserWatched(userId, cateId);
  }

  public List<String> getWatchedSubCategory(String userId, String cateId) throws Exception {
    return storage.getWatchedSubCategory(userId, cateId);
  }

  public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser) throws Exception {
    return storage.getListQuestionsWatch(faqSetting, currentUser);
  }

  public void deleteCategoryWatch(String categoryId, String user) throws Exception {
    storage.deleteCategoryWatch(categoryId, user);
    cleanCache();
  }

  public void unWatchCategory(String categoryId, String user) throws Exception {
    storage.unWatchCategory(categoryId, user);
    cleanCache();
  }

  public void unWatchQuestion(String questionId, String user) throws Exception {
    storage.unWatchQuestion(questionId, user);
    cleanCache();
  }

  public List<ObjectSearchResult> getSearchResults(FAQEventQuery eventQuery) throws Exception {
    return storage.getSearchResults(eventQuery);
  }

  public List<String> getCategoryPath(String categoryId) throws Exception {
    return storage.getCategoryPath(categoryId);
  }

  public String getParentCategoriesName(String path) throws Exception {
    return storage.getParentCategoriesName(path);
  }

  public NotifyInfo getMessageInfo(String name) throws Exception {
    return storage.getMessageInfo(name);
  }

  public void swapCategories(String cateId1, String cateId2) throws Exception {
    storage.swapCategories(cateId1, cateId2);
    cleanCache();
  }

  public void saveTopicIdDiscussQuestion(String questionId, String topicId) throws Exception {
    storage.saveTopicIdDiscussQuestion(questionId, topicId);
    cleanCache();
  }

  public InputStream exportData(String categoryId, boolean createZipFile) throws Exception {
    return storage.exportData(categoryId, createZipFile);
  }

  public boolean importData(String parentId, InputStream inputStream, boolean isZip) throws Exception {
    boolean b = storage.importData(parentId, inputStream, isZip);
    cleanCache();
    return b;
  }

  public boolean isExisting(String path) throws Exception {
    return storage.isExisting(path);
  }

  public String getCategoryPathOf(String id) throws Exception {
    return storage.getCategoryPathOf(id);
  }

  public boolean isModerateAnswer(String id) throws Exception {
    return storage.isModerateAnswer(id);
  }

  public boolean isModerateQuestion(String id) throws Exception {
    return storage.isModerateQuestion(id);
  }

  public boolean isViewAuthorInfo(String id) {
    return storage.isViewAuthorInfo(id);
  }

  public boolean isCategoryModerator(String categoryId, String user) throws Exception {
    return storage.isCategoryModerator(categoryId, user);
  }

  public boolean isCategoryExist(String name, String path) {
    return storage.isCategoryExist(name, path);
  }

  public List<String> getQuestionContents(List<String> paths) throws Exception {
    return storage.getQuestionContents(paths);
  }

  public Map<String, String> getRelationQuestion(List<String> paths) throws Exception {
    return storage.getRelationQuestion(paths);
  }

  public Node getQuestionNodeById(String path) throws Exception {
    return storage.getQuestionNodeById(path);
  }

  public String[] getModeratorsOf(String path) throws Exception {
    return storage.getModeratorsOf(path);
  }

  public String getCategoryNameOf(String categoryPath) throws Exception {
    return storage.getCategoryNameOf(categoryPath);
  }

  public CategoryInfo getCategoryInfo(String categoryPath, List<String> categoryIdScoped) throws Exception {
    return storage.getCategoryInfo(categoryPath, categoryIdScoped);
  }

  public void reCalculateInfoOfQuestion(String absPathOfProp) throws Exception {
    storage.reCalculateInfoOfQuestion(absPathOfProp);
    cleanCache();
  }

  public void updateQuestionRelatives(String questionPath, String[] relatives) throws Exception {
    storage.updateQuestionRelatives(questionPath, relatives);
    cleanCache();
  }

  public void calculateDeletedUser(String userName) throws Exception {
    storage.calculateDeletedUser(userName);
    cleanCache();
  }

  public InputStream createAnswerRSS(String cateId) throws Exception {
    return storage.createAnswerRSS(cateId);
  }

  public Comment[] getComments(String questionId) throws Exception {
    return storage.getComments(questionId);
  }

  public Node getFAQServiceHome(SessionProvider sProvider) throws Exception {
    return storage.getFAQServiceHome(sProvider);
  }

}
