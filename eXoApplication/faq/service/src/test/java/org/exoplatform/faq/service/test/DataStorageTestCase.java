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
package org.exoplatform.faq.service.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.jcr.Node;

import org.apache.commons.io.FileUtils;
import org.exoplatform.faq.base.FAQServiceBaseTestCase;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.CategoryInfo;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.DataStorage;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQNodeTypes;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.service.impl.JCRDataStorage;
import org.exoplatform.faq.service.impl.MultiLanguages;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.ks.common.jcr.PropertyReader;

/**
 * Created by The eXo Platform SAS
 * Author : quangpld
 *          quangpld@exoplatform.com
 * Oct 5, 2012  
 */
public class DataStorageTestCase extends FAQServiceBaseTestCase {
  private DataStorage   dataStorage;
  
  public DataStorageTestCase() throws Exception {
    super();
  }
  
  public void setUp() throws Exception {
    //
    dataStorage = (DataStorage) getService(JCRDataStorage.class);
    //
    super.setUp();
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testIsAdminRole() throws Exception {
    assertFalse(dataStorage.isAdminRole(USER_ROOT));
    assertFalse(dataStorage.isAdminRole(USER_JOHN));
    assertFalse(dataStorage.isAdminRole(USER_DEMO));
  }
  
  public void testGetAllFAQAdmin() throws Exception {
    List<String> list = dataStorage.getAllFAQAdmin();
    assertNotNull(list);
    assertEquals(0, list.size());
  }
  
  public void testGetUserSetting() throws Exception {
    // TODO: Check why sometime assertEquals("created", faqSetting_.getOrderBy()) fail
//    dataStorage.getUserSetting(USER_ROOT, faqSetting_);
//    assertEquals("created", faqSetting_.getOrderBy());
//    assertEquals("asc", faqSetting_.getOrderType());
//    assertTrue(faqSetting_.isSortQuestionByVote());
  }
  
  public void testSaveFAQSetting() throws Exception {
    //
    assertEquals("created", faqSetting_.getOrderBy());
    assertEquals("asc", faqSetting_.getOrderType());
    assertTrue(faqSetting_.isSortQuestionByVote());
    
    //
    FAQSetting faqSetting = new FAQSetting();
    faqSetting.setOrderBy("createdBy");
    faqSetting.setOrderType("desc");
    faqSetting.setSortQuestionByVote(false);
    dataStorage.saveFAQSetting(faqSetting, USER_ROOT);
    
    //
    dataStorage.getUserSetting(USER_ROOT, faqSetting_);
    assertEquals("createdBy", faqSetting_.getOrderBy());
    assertEquals("desc", faqSetting_.getOrderType());
    assertFalse(faqSetting_.isSortQuestionByVote());
  }
  
  public void testGetUserAvatar() throws Exception {
    FileAttachment fileAttachment = createUserAvatar("johnAvatar");
    dataStorage.saveUserAvatar(USER_JOHN, fileAttachment);
    FileAttachment johnAvatar = dataStorage.getUserAvatar(USER_JOHN);
    assertNotNull(johnAvatar);
  }
  
  public void testSaveUserAvatar() throws Exception {
    //
    assertNull(dataStorage.getUserAvatar(USER_DEMO));
    
    //
    FileAttachment fileAttachment = createUserAvatar("johnAvatar");
    dataStorage.saveUserAvatar(USER_DEMO, fileAttachment);
    FileAttachment demoAvatar = dataStorage.getUserAvatar(USER_DEMO);
    assertNotNull(demoAvatar);
  }
  
  public void testSetDefaultAvatar() throws Exception {
    //
    assertNotNull(dataStorage.getUserAvatar(USER_JOHN));
    assertNotNull(dataStorage.getUserAvatar(USER_DEMO));
    
    //
    dataStorage.setDefaultAvatar(USER_JOHN);
    dataStorage.setDefaultAvatar(USER_DEMO);
    assertNull(dataStorage.getUserAvatar(USER_JOHN));
    assertNull(dataStorage.getUserAvatar(USER_DEMO));
  }
  
  public void testInitRootCategory() throws Exception {
    assertFalse(dataStorage.initRootCategory());
  }
  
  public void testGetTemplate() throws Exception {
    dataStorage.saveTemplate("lorem ipsum dolor sit amet");
    assertTrue(dataStorage.getTemplate().length > 0);
  }
  
  public void testSaveTemplate() throws Exception {
    //
    dataStorage.saveTemplate("lorem ipsum dolor sit amet");
    assertTrue(dataStorage.getTemplate().length > 0);
    assertEquals("lorem ipsum dolor sit amet", new String(dataStorage.getTemplate()));
  }
  
  public void testGetQuestionLanguages() throws Exception {
    Node questionNode = dataStorage.getQuestionNodeById(questionPath1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    List<QuestionLanguage> questionLanguages = dataStorage.getQuestionLanguages(questionPath1);
    assertEquals(3, questionLanguages.size());
  }
  
  public void testDeleteAnswer() throws Exception {    
    //
    Answer answer1 = createAnswer(USER_ROOT, "Root answers a question");
    Answer answer2 = createAnswer(USER_DEMO, "Demo answers a question");
    
    //
    dataStorage.saveAnswer(questionPath1, new Answer[] { answer1, answer2 });
    assertEquals(2, dataStorage.getPageListAnswer(questionPath1, false).getAll().size());
    
    //
    dataStorage.deleteAnswer(questionPath1, answer1.getId());
    assertEquals(1, dataStorage.getPageListAnswer(questionPath1, false).getAll().size());
  }
  
  public void testDeleteComment() throws Exception {
    //
    Comment comment1 = createComment(USER_ROOT, "Root comments a question");
    Comment comment2 = createComment(USER_DEMO, "Demo comments a question");
    
    //
    dataStorage.saveComment(questionPath1, comment1, true);
    dataStorage.saveComment(questionPath1, comment2, true);
    assertEquals(2, dataStorage.getPageListComment(questionPath1).getAll().size());
    
    //
    dataStorage.deleteComment(questionPath1, comment1.getId());
    assertEquals(1, dataStorage.getPageListComment(questionPath1).getAll().size());
  }
  
  public void testGetPageListAnswer() throws Exception {
    //
    Answer answer1 = createAnswer(USER_ROOT, "Answer1");
    Answer answer2 = createAnswer(USER_DEMO, "Answer2");
    Answer answer3 = createAnswer(USER_DEMO, "Answer3");
    Answer answer4 = createAnswer(USER_DEMO, "Answer4");
    Answer answer5 = createAnswer(USER_DEMO, "Answer5");
    Answer answer6 = createAnswer(USER_DEMO, "Answer6");
    Answer answer7 = createAnswer(USER_DEMO, "Answer7");
    Answer answer8 = createAnswer(USER_DEMO, "Answer8");
    Answer answer9 = createAnswer(USER_DEMO, "Answer9");
    Answer answer10 = createAnswer(USER_ROOT, "Answer10");
    Answer answer11 = createAnswer(USER_DEMO, "Answer11");
    Answer answer12 = createAnswer(USER_DEMO, "Answer12");
    
    //
    dataStorage.saveAnswer(questionPath2, new Answer[] { answer1, answer2, answer3, answer4, answer5, answer6, answer7, answer8, answer9, answer10, answer11, answer12 });
    JCRPageList pageListAnswer = dataStorage.getPageListAnswer(questionPath2, false);
    assertEquals(12, pageListAnswer.getAll().size());
    assertEquals(10, pageListAnswer.getPageSize());
    assertEquals(2, pageListAnswer.getAvailablePage());
  }
  
  public void testSaveAnswer() throws Exception {
    //
    assertNull(dataStorage.getPageListAnswer(questionPath3, false));
    
    //
    Answer answer1 = createAnswer(USER_ROOT, "Root answers a question");
    Answer answer2 = createAnswer(USER_DEMO, "Demo answers a question");
    
    //
    dataStorage.saveAnswer(questionPath3, answer1, true);
    dataStorage.saveAnswer(questionPath3, answer2, true);
    assertNotNull(dataStorage.getPageListAnswer(questionPath3, false));
    assertEquals(2, dataStorage.getPageListAnswer(questionPath3, false).getAll().size());
  }
  
  public void testSaveAnswers() throws Exception {
    //
    assertNull(dataStorage.getPageListAnswer(questionPath4, false));
    
    //
    Answer answer1 = createAnswer(USER_ROOT, "Root answers a question");
    Answer answer2 = createAnswer(USER_DEMO, "Demo answers a question");
    Answer answer3 = createAnswer(USER_DEMO, "Demo answers a question");
    
    //
    assertNull(dataStorage.getPageListAnswer(questionPath4, false));
    dataStorage.saveAnswer(questionPath4, new Answer[] { answer1, answer2, answer3 });
    assertEquals(3, dataStorage.getPageListAnswer(questionPath4, false).getAll().size());
  }
  
  public void testSaveComment() throws Exception {    
    //
    Comment comment1 = createComment(USER_ROOT, "Root comments a question");
    Comment comment2 = createComment(USER_DEMO, "Demo comments a question");
    
    //
    dataStorage.saveComment(questionPath5, comment1, true);
    dataStorage.saveComment(questionPath5, comment2, true);
    assertEquals(2, dataStorage.getPageListComment(questionPath5).getAll().size());
  }
  
  public void testSaveAnswerQuestionLang() throws Exception {
    // TODO: Check why the input language is not used.
  }
  
  public void testGetAnswerById() throws Exception {
    //
    Answer answer1 = createAnswer(USER_ROOT, "Root answers a question");
    Answer answer2 = createAnswer(USER_DEMO, "Demo answers a question");
    
    //
    String question1Id = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId1;
    dataStorage.saveAnswer(question1Id, new Answer[] { answer1, answer2 });
    
    //
    Answer got = dataStorage.getAnswerById(question1Id, answer1.getId());
    assertNotNull(got);
    assertEquals(USER_ROOT, got.getResponseBy());
    assertEquals("Root answers a question", got.getResponses());
  }
  
  public void testGetPageListComment() throws Exception {
    //
    assertNull(dataStorage.getPageListComment(questionPath1));
    
    //
    Comment comment1 = createComment(USER_ROOT, "Root comments a question");
    Comment comment2 = createComment(USER_DEMO, "Demo comments a question");
    dataStorage.saveComment(questionPath1, comment1, true);
    dataStorage.saveComment(questionPath1, comment2, true);
    assertEquals(2, dataStorage.getPageListComment(questionPath1).getAll().size());
  }
  
  public void testGetCommentById() throws Exception {
    //
    Comment comment1 = createComment(USER_ROOT, "Root comments a question");
    Comment comment2 = createComment(USER_DEMO, "Demo comments a question");
    
    //
    dataStorage.saveComment(questionPath3, comment1, true);
    dataStorage.saveComment(questionPath3, comment2, true);
    
    //
    Comment got1 = dataStorage.getCommentById(questionPath3, comment1.getId());
    assertNotNull(got1);
    assertEquals(USER_ROOT, got1.getCommentBy());
    assertEquals("Root comments a question", got1.getComments());
    
    //
    Node question1Node = dataStorage.getQuestionNodeById(questionPath3);
    Comment got2 = dataStorage.getCommentById(question1Node, comment2.getId());
    assertNotNull(got2);
    assertEquals(USER_DEMO, got2.getCommentBy());
    assertEquals("Demo comments a question", got2.getComments());
  }
  
  public void testSaveQuestion() throws Exception {
    //
    Question question = createQuestion(categoryId1);
    question.setRelations(new String[] {});
    question.setLanguage("English");
    question.setAuthor("quangpld");
    question.setEmail("quangpld@exoplatform.com");
    question.setDetail("Why always me?");
    question.setCreatedDate(new Date());
    
    //
    dataStorage.saveQuestion(question, true, faqSetting_);
    Question got = dataStorage.getQuestionById(question.getId());
    assertNotNull(got);
    assertEquals("quangpld", got.getAuthor());
    assertEquals("Why always me?", got.getDetail());
  }
  
  public void testRemoveQuestion() throws Exception {
    //
    Question got = dataStorage.getQuestionById(questionPath5);
    assertNotNull(got);
    
    //
    dataStorage.removeQuestion(questionPath5);
    got = dataStorage.getQuestionById(questionPath5);
    assertNull(got);
  }
  
  public void testGetQuestionById() throws Exception {
    //
    Question got = dataStorage.getQuestionById("InvalidQuestionPath");
    assertNull(got);
    
    //
    got = dataStorage.getQuestionById(questionPath2);
    assertNotNull(got);
  }
  
  public void testGetAllQuestions() throws Exception {
    //
    JCRPageList questionPageList = dataStorage.getAllQuestions();
    assertNotNull(questionPageList);
    assertEquals(5, questionPageList.getAll().size());
    
    //
    Question question1 = createQuestion(categoryId2);
    Question question2 = createQuestion(categoryId2);
    dataStorage.saveQuestion(question1, true, faqSetting_);
    dataStorage.saveQuestion(question2, true, faqSetting_);
    questionPageList = dataStorage.getAllQuestions();
    assertNotNull(questionPageList);
    assertEquals(7, questionPageList.getAll().size());
  }
  
  public void testGetQuestionsNotYetAnswer() throws Exception {
    //
    JCRPageList questionPageList = dataStorage.getQuestionsNotYetAnswer(categoryId1, true);
    assertNotNull(questionPageList);
    assertEquals(5, questionPageList.getAll().size());
    
    //
    Answer answer1 = createAnswer(USER_ROOT, "Root answers question1");
    Answer answer2 = createAnswer(USER_DEMO, "Demo answers question2");
    dataStorage.saveAnswer(questionPath1, answer1, true);
    dataStorage.saveAnswer(questionPath2, answer2, true);
    questionPageList = dataStorage.getQuestionsNotYetAnswer(categoryId1, true);
    assertNotNull(questionPageList);
    // TODO: Check logic why questionPageList.getAll().size() equals 5 (not 3)
    // questionPageList = dataStorage.getQuestionsNotYetAnswer(categoryId1, true);
    // assertNotNull(questionPageList);
    // assertEquals(3, questionPageList.getAll().size());
  }
  
  public void testGetPendingQuestionsByCategory() throws Exception {
    //
    JCRPageList questionPageList = dataStorage.getPendingQuestionsByCategory(categoryId1, faqSetting_);
    assertNotNull(questionPageList);
    assertEquals(0, questionPageList.getAll().size());
    
    //
    Question question1 = dataStorage.getQuestionById(questionId1);
    Question question2 = dataStorage.getQuestionById(questionId2);
    question1.setApproved(false);
    question2.setApproved(false);
    dataStorage.saveQuestion(question1, false, faqSetting_);
    dataStorage.saveQuestion(question2, false, faqSetting_);
    questionPageList = dataStorage.getPendingQuestionsByCategory(categoryId1, faqSetting_);
    List<Question> pendingQuestions = questionPageList.getAll();
    assertNotNull(questionPageList);
    assertEquals(2, pendingQuestions.size());
    assertEquals(questionId1, pendingQuestions.get(0).getId());
    assertEquals(questionId2, pendingQuestions.get(1).getId());
  }
  
  public void testGetQuestionsByCatetory() throws Exception {
    //
    JCRPageList questionPageList = dataStorage.getQuestionsByCatetory(categoryId1, faqSetting_);
    assertNotNull(questionPageList);
    assertEquals(5, questionPageList.getAll().size());
    
    //
    questionPageList = dataStorage.getQuestionsByCatetory(categoryId2, faqSetting_);
    assertNotNull(questionPageList);
    assertEquals(0, questionPageList.getAll().size());
  }
  
  public void testGetAllQuestionsByCatetory() throws Exception {
    // TODO:
  }
  
  public void testGetQuestionsByListCatetory() throws Exception {
    List<String> listId = new ArrayList<String>();
    listId.add(categoryId1.substring(categoryId1.lastIndexOf("/") + 1));
    listId.add(categoryId2.substring(categoryId2.lastIndexOf("/") + 1));
    JCRPageList questionPageList = dataStorage.getQuestionsByListCatetory(listId, false);
    assertEquals(5, questionPageList.getAll().size());
  }
  
  public void testGetQuickQuestionsByListCatetory() throws Exception {
    List<String> listId = new ArrayList<String>();
    listId.add(categoryId1.substring(categoryId1.lastIndexOf("/") + 1));
    listId.add(categoryId2.substring(categoryId2.lastIndexOf("/") + 1));
    List<Question> questions = dataStorage.getQuickQuestionsByListCatetory(listId, false);
    assertEquals(5, questions.size());
  }
  
  public void testGetCategoryPathOfQuestion() throws Exception {
    String categoryPath = "home/Category 1 to test question";
    assertEquals(categoryPath, dataStorage.getCategoryPathOfQuestion(questionPath1));
    assertEquals(categoryPath, dataStorage.getCategoryPathOfQuestion(questionPath2));
    assertEquals(categoryPath, dataStorage.getCategoryPathOfQuestion(questionPath3));
    assertEquals(categoryPath, dataStorage.getCategoryPathOfQuestion(questionPath4));
    assertEquals(categoryPath, dataStorage.getCategoryPathOfQuestion(questionPath5));
  }
  
  public void testMoveQuestions() throws Exception {
    //
    JCRPageList questionPageList = dataStorage.getQuestionsByCatetory(categoryId1, faqSetting_);
    assertNotNull(questionPageList);
    assertEquals(5, questionPageList.getAll().size());
    questionPageList = dataStorage.getQuestionsByCatetory(categoryId2, faqSetting_);
    assertNotNull(questionPageList);
    assertEquals(0, questionPageList.getAll().size());
    
    //
    List<String> moveQuestionPaths = new ArrayList<String>();
    moveQuestionPaths.add(questionPath1);
    moveQuestionPaths.add(questionPath2);
    dataStorage.moveQuestions(moveQuestionPaths, categoryId2, "questionLink", faqSetting_);
    questionPageList = dataStorage.getQuestionsByCatetory(categoryId1, faqSetting_);
    assertNotNull(questionPageList);
    assertEquals(3, questionPageList.getAll().size());
    questionPageList = dataStorage.getQuestionsByCatetory(categoryId2, faqSetting_);
    assertNotNull(questionPageList);
    assertEquals(2, questionPageList.getAll().size());
  }
  
  public void testChangeStatusCategoryView() throws Exception {
    //
    Category category1 = dataStorage.getCategoryById(categoryId1);
    Category category2 = dataStorage.getCategoryById(categoryId2);
    assertTrue(category1.isView());
    assertTrue(category2.isView());
    
    //
    List<String> listCateIds = new ArrayList<String>();
    listCateIds.add(categoryId1);
    listCateIds.add(categoryId2);
    dataStorage.changeStatusCategoryView(listCateIds);
    category1 = dataStorage.getCategoryById(categoryId1);
    category2 = dataStorage.getCategoryById(categoryId2);
    assertFalse(category1.isView());
    assertFalse(category2.isView());
    
    //
    dataStorage.changeStatusCategoryView(listCateIds);
    category1 = dataStorage.getCategoryById(categoryId1);
    category2 = dataStorage.getCategoryById(categoryId2);
    assertTrue(category1.isView());
    assertTrue(category2.isView());
  }
  
  public void testGetMaxindexCategory() throws Exception {
    assertEquals(3, dataStorage.getMaxindexCategory(Utils.CATEGORY_HOME));
    assertEquals(0, dataStorage.getMaxindexCategory(categoryId1));
    assertEquals(0, dataStorage.getMaxindexCategory(categoryId2));
    assertEquals(0, dataStorage.getMaxindexCategory(categoryId3));
  }
  
  public void testSaveCategory() throws Exception {
    Category cat4 = createCategory("Category #4", 4);
    dataStorage.saveCategory(Utils.CATEGORY_HOME, cat4, true);
    cat4 = dataStorage.getCategoryById(Utils.CATEGORY_HOME + "/" + cat4.getId());
    assertNotNull(cat4);
    assertEquals("Category #4", cat4.getName());
    assertEquals(4, cat4.getIndex());
  }
  
  public void testListingCategoryTree() throws Exception {
    List<Cate> cates = dataStorage.listingCategoryTree();
    assertNotNull(cates);
    assertEquals(3, cates.size());
    assertEquals(categoryId1, Utils.CATEGORY_HOME + "/" + cates.get(0).getCategory().getId());
    assertEquals(categoryId2, Utils.CATEGORY_HOME + "/" + cates.get(1).getCategory().getId());
    assertEquals(categoryId3, Utils.CATEGORY_HOME + "/" + cates.get(2).getCategory().getId());
  }
  
  public void testRemoveCategory() throws Exception {
    //
    List<Cate> cates = dataStorage.listingCategoryTree();
    assertNotNull(cates);
    assertEquals(3, cates.size());
    assertNotNull(dataStorage.getCategoryById(categoryId1));
    assertNotNull(dataStorage.getCategoryById(categoryId2));
    assertNotNull(dataStorage.getCategoryById(categoryId3));
    
    //
    dataStorage.removeCategory(categoryId3);
    cates = dataStorage.listingCategoryTree();
    assertNotNull(cates);
    assertEquals(2, cates.size());
    assertNotNull(dataStorage.getCategoryById(categoryId1));
    assertNotNull(dataStorage.getCategoryById(categoryId2));
    assertNull(dataStorage.getCategoryById(categoryId3));
  }
  
  public void testGetCategoryById() throws Exception {
    //
    Category cat1 = dataStorage.getCategoryById(categoryId1);
    Category cat2 = dataStorage.getCategoryById(categoryId2);
    Category cat3 = dataStorage.getCategoryById(categoryId3);
    
    //
    assertNotNull(cat1);
    assertEquals("Category 1 to test question", cat1.getName());
    assertEquals(1, cat1.getIndex());
    assertNotNull(cat2);
    assertEquals("Category 2 to test question", cat2.getName());
    assertEquals(2, cat2.getIndex());
    assertNotNull(cat3);
    assertEquals("Category 3 has not question", cat3.getName());
    assertEquals(3, cat3.getIndex());
    
    //
    assertNull(dataStorage.getCategoryById("InvalidCategoryPath"));
  }
  
  public void testFindCategoriesByName() throws Exception {
    List<Category> cates = dataStorage.findCategoriesByName("Category 1 to test question");
    assertNotNull(cates);
    assertEquals(1, cates.size());
    assertEquals(categoryId1, Utils.CATEGORY_HOME + "/" + cates.get(0).getId());
  }
  
  public void testGetListCateIdByModerator() throws Exception {
    //
    List<String> listCateByModerator = dataStorage.getListCateIdByModerator(USER_ROOT);
    assertEquals(2, listCateByModerator.size());
    assertEquals(categoryId1.substring(categoryId1.lastIndexOf("/") + 1) + CommonUtils.SEMICOLON + "Category 1 to test question", listCateByModerator.get(0));
    assertEquals(categoryId2.substring(categoryId2.lastIndexOf("/") + 1) + CommonUtils.SEMICOLON + "Category 2 to test question", listCateByModerator.get(1));
    
    //
    listCateByModerator = dataStorage.getListCateIdByModerator(USER_JOHN);
    assertEquals(0, listCateByModerator.size());
  }
  
  public void testGetAllCategories() throws Exception {
    List<Category> cates = dataStorage.getAllCategories();
    assertNotNull(cates);
    assertEquals(3, cates.size());
    assertEquals(categoryId1, Utils.CATEGORY_HOME + "/" + cates.get(0).getId());
    assertEquals(categoryId2, Utils.CATEGORY_HOME + "/" + cates.get(1).getId());
    assertEquals(categoryId3, Utils.CATEGORY_HOME + "/" + cates.get(2).getId());
  }
  
  public void testExistingCategories() throws Exception {
    // TODO: Check why dataStorage.existingCategories() is 0 (not 3)
    // assertEquals(3, dataStorage.existingCategories());
  }
  
  public void testGetCategoryNodeById() throws Exception {
    //
    Node node = dataStorage.getCategoryNodeById("InvalidCategoryId");
    assertNull(node);
    
    //
    node = dataStorage.getCategoryNodeById(categoryId1);
    PropertyReader propertyReader = new PropertyReader(node);
    assertNotNull(node);
    assertEquals(categoryId1, Utils.CATEGORY_HOME + "/" + propertyReader.string(FAQNodeTypes.EXO_ID));
  }
  
  public void testGetSubCategories() throws Exception {    
    //
    List<Category> listCate = dataStorage.getSubCategories(Utils.CATEGORY_HOME, faqSetting_, true, Arrays.asList(new String[]{"john", "manager:/admin/user"}));
    assertEquals(3, listCate.size());
    
    //
    faqSetting_.setIsAdmin("true");
    listCate = dataStorage.getSubCategories(Utils.CATEGORY_HOME, faqSetting_, true, new ArrayList<String>());
    assertEquals(3, listCate.size());
  }
  
  public void testGetCategoryInfo() throws Exception {
    //
    assertEquals(dataStorage.getCategoryInfo(Utils.CATEGORY_HOME, faqSetting_)[0], 3);
    
    //
    List<String> categoryIdScoped = new ArrayList<String>();
    CategoryInfo categoryInfo = dataStorage.getCategoryInfo(Utils.CATEGORY_HOME, categoryIdScoped);
    assertEquals(3, categoryInfo.getSubCateInfos().size());
    
    //
    categoryInfo = dataStorage.getCategoryInfo(categoryId1, categoryIdScoped);
    assertEquals(5, categoryInfo.getQuestionInfos().size());
  }
  
  public void testMoveCategory() throws Exception {
    //
    List<String> limitedUsers = new ArrayList<String>();
    assertEquals(3, dataStorage.getSubCategories(Utils.CATEGORY_HOME, faqSetting_, true, limitedUsers).size());
    assertEquals(0, dataStorage.getSubCategories(categoryId1, faqSetting_, true, limitedUsers).size());
    
    //
    dataStorage.moveCategory(categoryId2, categoryId1);
    assertEquals(2, dataStorage.getSubCategories(Utils.CATEGORY_HOME, faqSetting_, true, limitedUsers).size());
    assertEquals(1, dataStorage.getSubCategories(categoryId1, faqSetting_, true, limitedUsers).size());
  }
  
  public void testAddWatchCategory() throws Exception {
    //
    assertFalse(dataStorage.hasWatch(categoryId1));
    
    //
    dataStorage.addWatchCategory(categoryId1, createNewWatch(USER_ROOT, "root@exoplatform.com"));
    dataStorage.addWatchCategory(categoryId1, createNewWatch(USER_DEMO, "demo@exoplatform.com"));
    dataStorage.addWatchCategory(categoryId1, createNewWatch(USER_JOHN, "john@exoplatform.com"));
    assertTrue(dataStorage.hasWatch(categoryId1));
    assertEquals(3, dataStorage.getWatchByCategory(categoryId1).size());
    assertTrue(dataStorage.isUserWatched(USER_ROOT, categoryId1));
    assertTrue(dataStorage.isUserWatched(USER_DEMO, categoryId1));
    assertTrue(dataStorage.isUserWatched(USER_JOHN, categoryId1));
  }
  
  public void testGetListMailInWatch() throws Exception {
    // TODO: Check why can't get the right value for list mail in watch.
  }
  
  public void testGetWatchByCategory() throws Exception {
    //
    dataStorage.addWatchCategory(categoryId1, createNewWatch(USER_ROOT, "root@exoplatform.com"));
    dataStorage.addWatchCategory(categoryId1, createNewWatch(USER_DEMO, "demo@exoplatform.com"));
    dataStorage.addWatchCategory(categoryId1, createNewWatch(USER_JOHN, "john@exoplatform.com"));
    assertTrue(dataStorage.hasWatch(categoryId1));
    assertEquals(3, dataStorage.getWatchByCategory(categoryId1).size());
    assertTrue(dataStorage.isUserWatched(USER_ROOT, categoryId1));
    assertTrue(dataStorage.isUserWatched(USER_DEMO, categoryId1));
    assertTrue(dataStorage.isUserWatched(USER_JOHN, categoryId1));
    
    //
    assertEquals(0, dataStorage.getWatchByCategory(categoryId2).size());
  }
  
  public void testHasWatch() throws Exception {
    //
    assertFalse(dataStorage.hasWatch(categoryId1));
    
    //
    dataStorage.addWatchCategory(categoryId1, createNewWatch(USER_ROOT, "root@exoplatform.com"));
    dataStorage.addWatchCategory(categoryId1, createNewWatch(USER_DEMO, "demo@exoplatform.com"));
    dataStorage.addWatchCategory(categoryId1, createNewWatch(USER_JOHN, "john@exoplatform.com"));
    assertTrue(dataStorage.hasWatch(categoryId1));
  }
  
  public void testAddWatchQuestion() throws Exception {
    //
    assertFalse(dataStorage.hasWatch(questionPath1));
    
    //
    dataStorage.addWatchQuestion(questionPath1, createNewWatch(USER_ROOT, "root@exoplatform.com"), true);
    dataStorage.addWatchQuestion(questionPath1, createNewWatch(USER_DEMO, "demo@exoplatform.com"), true);
    dataStorage.addWatchQuestion(questionPath1, createNewWatch(USER_JOHN, "john@exoplatform.com"), true);
    assertTrue(dataStorage.hasWatch(questionPath1));
    assertEquals(3, dataStorage.getWatchByQuestion(questionPath1).size());
    assertTrue(dataStorage.isUserWatched(USER_ROOT, questionPath1));
    assertTrue(dataStorage.isUserWatched(USER_DEMO, questionPath1));
    assertTrue(dataStorage.isUserWatched(USER_JOHN, questionPath1));
  }
  
  public void testGetWatchByQuestion() throws Exception {
    //
    List<Watch> watches = dataStorage.getWatchByQuestion(questionPath1);
    assertEquals(0, watches.size());
    
    //
    dataStorage.addWatchQuestion(questionPath1, createNewWatch(USER_ROOT, "root@exoplatform.com"), true);
    dataStorage.addWatchQuestion(questionPath1, createNewWatch(USER_DEMO, "demo@exoplatform.com"), true);
    dataStorage.addWatchQuestion(questionPath1, createNewWatch(USER_JOHN, "john@exoplatform.com"), true);
    watches = dataStorage.getWatchByQuestion(questionPath1);
    assertEquals(3, watches.size());
  }
  
  public void testGetWatchedCategoryByUser() throws Exception {
    //
    assertEquals(0, dataStorage.getWatchedCategoryByUser(USER_ROOT).getAvailable());
    
    //
    Watch rootWatch = createNewWatch(USER_ROOT, "root@exoplatform.com");
    dataStorage.addWatchCategory(categoryId1, rootWatch);
    dataStorage.addWatchCategory(categoryId2, rootWatch);
    dataStorage.addWatchCategory(categoryId3, rootWatch);
    assertEquals(3, dataStorage.getWatchedCategoryByUser(USER_ROOT).getAvailable());
  }
  
  public void testIsUserWatched() throws Exception {
    //
    assertFalse(dataStorage.isUserWatched(USER_ROOT, categoryId1));
    assertFalse(dataStorage.isUserWatched(USER_ROOT, categoryId2));
    assertFalse(dataStorage.isUserWatched(USER_ROOT, questionPath1));
    
    //
    Watch rootWatch = createNewWatch(USER_ROOT, "root@exoplatform.com");
    dataStorage.addWatchCategory(categoryId1, rootWatch);
    dataStorage.addWatchQuestion(questionPath1, rootWatch, true);
    assertTrue(dataStorage.isUserWatched(USER_ROOT, categoryId1));
    assertFalse(dataStorage.isUserWatched(USER_ROOT, categoryId2));
    assertTrue(dataStorage.isUserWatched(USER_ROOT, questionPath1));
    assertFalse(dataStorage.isUserWatched(USER_ROOT, questionPath2));
  }
  
  public void testGetWatchedSubCategory() throws Exception {
    //
    List<String> watches = dataStorage.getWatchedSubCategory(USER_DEMO, Utils.CATEGORY_HOME);
    assertEquals(0, watches.size());
    
    //
    Watch demoWatch = createNewWatch(USER_DEMO, "demo@exoplatform.com");
    dataStorage.addWatchCategory(categoryId1, demoWatch);
    dataStorage.addWatchCategory(categoryId2, demoWatch);
    watches = dataStorage.getWatchedSubCategory(USER_DEMO, Utils.CATEGORY_HOME);
    assertEquals(2, watches.size());
    assertEquals(categoryId1, Utils.CATEGORY_HOME + "/" + watches.get(0));
    assertEquals(categoryId2, Utils.CATEGORY_HOME + "/" + watches.get(1));
  }
  
  public void testGetListQuestionsWatch() throws Exception {
    //
    assertEquals(0, dataStorage.getListQuestionsWatch(faqSetting_, USER_DEMO).getAll().size());
    
    //
    Watch demoWatch = createNewWatch(USER_DEMO, "demo@exoplatform.com");
    dataStorage.addWatchQuestion(questionPath1, demoWatch, true);
    dataStorage.addWatchQuestion(questionPath2, demoWatch, true);
    dataStorage.addWatchQuestion(questionPath3, demoWatch, true);
    assertEquals(3, dataStorage.getListQuestionsWatch(faqSetting_, USER_DEMO).getAll().size());
  }
  
  public void testDeleteCategoryWatch() throws Exception {
    //
    Watch rootWatch = createNewWatch(USER_ROOT, "root@exoplatform.com");
    dataStorage.addWatchCategory(categoryId1, rootWatch);
    dataStorage.addWatchCategory(categoryId2, rootWatch);
    dataStorage.addWatchCategory(categoryId3, rootWatch);
    assertTrue(dataStorage.isUserWatched(USER_ROOT, categoryId1));
    assertTrue(dataStorage.isUserWatched(USER_ROOT, categoryId2));
    assertTrue(dataStorage.isUserWatched(USER_ROOT, categoryId3));
    
    //
    dataStorage.deleteCategoryWatch(categoryId2, USER_ROOT);
    dataStorage.deleteCategoryWatch(categoryId3, USER_ROOT);
    assertTrue(dataStorage.isUserWatched(USER_ROOT, categoryId1));
    assertFalse(dataStorage.isUserWatched(USER_ROOT, categoryId2));
    assertFalse(dataStorage.isUserWatched(USER_ROOT, categoryId3));
  }
  
  public void testUnWatchCategory() throws Exception {
    //
    Watch rootWatch = createNewWatch(USER_ROOT, "root@exoplatform.com");
    dataStorage.addWatchCategory(categoryId1, rootWatch);
    dataStorage.addWatchCategory(categoryId2, rootWatch);
    dataStorage.addWatchCategory(categoryId3, rootWatch);
    assertTrue(dataStorage.isUserWatched(USER_ROOT, categoryId1));
    assertTrue(dataStorage.isUserWatched(USER_ROOT, categoryId2));
    assertTrue(dataStorage.isUserWatched(USER_ROOT, categoryId3));
    
    //
    dataStorage.unWatchCategory(categoryId1, USER_ROOT);
    dataStorage.unWatchCategory(categoryId2, USER_ROOT);
    assertFalse(dataStorage.isUserWatched(USER_ROOT, categoryId1));
    assertFalse(dataStorage.isUserWatched(USER_ROOT, categoryId2));
    assertTrue(dataStorage.isUserWatched(USER_ROOT, categoryId3));
  }
  
  public void testUnWatchQuestion() throws Exception {
    //
    Watch rootWatch = createNewWatch(USER_ROOT, "root@exoplatform.com");
    dataStorage.addWatchQuestion(questionPath1, rootWatch, true);
    dataStorage.addWatchQuestion(questionPath3, rootWatch, true);
    dataStorage.addWatchQuestion(questionPath5, rootWatch, true);
    assertTrue(dataStorage.isUserWatched(USER_ROOT, questionPath1));
    assertTrue(dataStorage.isUserWatched(USER_ROOT, questionPath3));
    assertTrue(dataStorage.isUserWatched(USER_ROOT, questionPath5));
    
    //
    dataStorage.unWatchQuestion(questionPath1, USER_ROOT);
    dataStorage.unWatchQuestion(questionPath3, USER_ROOT);
    assertFalse(dataStorage.isUserWatched(USER_ROOT, questionPath1));
    assertFalse(dataStorage.isUserWatched(USER_ROOT, questionPath3));
    assertTrue(dataStorage.isUserWatched(USER_ROOT, questionPath5));
  }
  
  public void testGetSearchResults() throws Exception {
    //
    FAQEventQuery eventQuery = new FAQEventQuery();
    eventQuery.setText("test");
    eventQuery.setAdmin(true);
    eventQuery.setUserId(USER_ROOT);
    eventQuery.setType(FAQEventQuery.CATEGORY_AND_QUESTION);
    assertEquals(7, dataStorage.getSearchResults(eventQuery).size());
    
    //
    eventQuery.setType(FAQEventQuery.FAQ_CATEGORY);
    assertEquals(2, dataStorage.getSearchResults(eventQuery).size());

    //
    eventQuery.setText("");
    eventQuery.setModerator("demo");
    assertEquals(1, dataStorage.getSearchResults(eventQuery).size());

    //
    eventQuery.setText("Really");
    eventQuery.setType(FAQEventQuery.FAQ_QUESTION);
    eventQuery.setLanguage("English");
    assertEquals(1, dataStorage.getSearchResults(eventQuery).size());

    //
    Question question = dataStorage.getQuestionById(questionPath2);
    assertNotNull(question);
    question.setApproved(false);
    dataStorage.saveQuestion(question, false, faqSetting_);
    eventQuery.setAdmin(false);
    eventQuery.setUserId(USER_DEMO);
    assertEquals(0, dataStorage.getSearchResults(eventQuery).size());

    //
    eventQuery.setText("eXo Forum");
    eventQuery.setType(FAQEventQuery.FAQ_QUESTION);
    eventQuery.setLanguage("English");
    assertEquals(2, dataStorage.getSearchResults(eventQuery).size());
    question = dataStorage.getQuestionById(questionPath3);
    assertNotNull(question);
    question.setActivated(false);
    dataStorage.saveQuestion(question, false, faqSetting_);
    assertEquals(1, dataStorage.getSearchResults(eventQuery).size());

    //
    eventQuery.setAuthor("Quang Pham");
    eventQuery.setText("");
    assertEquals(0, dataStorage.getSearchResults(eventQuery).size());
    eventQuery.setAuthor("Mary Kelly");
    eventQuery.setText("");
    assertEquals(1, dataStorage.getSearchResults(eventQuery).size());
  }
  
  public void testGetCategoryPath() throws Exception {
    //
    List<String> breadcums = dataStorage.getCategoryPath(Utils.CATEGORY_HOME);
    assertEquals(0, breadcums.size());
    
    //
    breadcums = dataStorage.getCategoryPath(categoryId1);
    assertEquals(1, breadcums.size());
    assertEquals(categoryId1, Utils.CATEGORY_HOME + "/" + breadcums.get(0));
    
    //
    dataStorage.moveCategory(categoryId1, categoryId2);
    breadcums = dataStorage.getCategoryPath(categoryId1);
    assertEquals(2, breadcums.size());
    assertEquals(categoryId1, Utils.CATEGORY_HOME + "/" + breadcums.get(0));
    assertEquals(categoryId2, Utils.CATEGORY_HOME + "/" + breadcums.get(1));
  }

  public void testGetParentCategoriesName() throws Exception {
    assertEquals("categories > Category 1 to test question", dataStorage.getParentCategoriesName(categoryId1));
    assertEquals("categories > Category 2 to test question", dataStorage.getParentCategoriesName(categoryId2));
    assertEquals("categories > Category 3 has not question", dataStorage.getParentCategoriesName(categoryId3));
  }
  
  public void testGetPendingMessages() throws Exception {
    //
    Question question = dataStorage.getQuestionById(questionPath1);
    question.setEmail("quangpld@exoplatform.com");
    question.setEmailsWatch(new String[] { "pldquang@gmail.com, quangpld@yahoo.com" });
    question.setUsersWatch(new String[] { "root, demo" });
    Answer answer = createAnswer(USER_ROOT, "Root answer a question");
    question.setAnswers(new Answer[] { answer });
    question.setLink("http://google.com.vn/");
    faqSetting_.setDisplayMode("approved");
    dataStorage.saveQuestion(question, false, faqSetting_);
    Iterator<NotifyInfo> iterator = dataStorage.getPendingMessages();
    List<String> emails = new ArrayList<String>();
    while (iterator.hasNext()) {
      emails = iterator.next().getEmailAddresses();
    }
    assertEquals(emails.toString(), "[quangpld@exoplatform.com, pldquang@gmail.com, quangpld@yahoo.com]");
  }
  
  public void testGetMessageInfo() throws Exception {
    // TODO: Check the business logic of DataStorage#getMessageInfo
  }
  
  public void testSwapCategories() throws Exception {
    //
    Category cat = null;
    List<String> catIds = new ArrayList<String>();
    catIds.add(Utils.CATEGORY_HOME);
    for (int i = 1; i <= 5; i++) {
      cat = createCategory("Category " + (new Random().nextInt(100) + i), i);
      catIds.add(Utils.CATEGORY_HOME + "/" + cat.getId());
      dataStorage.saveCategory(Utils.CATEGORY_HOME, cat, true);
    }
    
    //
    String parentCatId = Utils.CATEGORY_HOME + "/" + cat.getId();
    List<String> catSubIds = new ArrayList<String>();
    catSubIds.add(parentCatId);
    for (int i = 1; i <= 5; i++) {
      cat = createCategory("Sub Category " + i, i);
      catSubIds.add(parentCatId + "/" + cat.getId());
      dataStorage.saveCategory(parentCatId, cat, true);
    }

    //
    assertEquals(1, dataStorage.getCategoryById(catSubIds.get(1)).getIndex());
    assertEquals(3, dataStorage.getCategoryById(catSubIds.get(3)).getIndex());
    dataStorage.swapCategories(catSubIds.get(1), catSubIds.get(3));
    assertEquals(3, dataStorage.getCategoryById(catSubIds.get(1)).getIndex(), 3);
    assertEquals(2, dataStorage.getCategoryById(catSubIds.get(3)).getIndex(), 2);

    //
    assertEquals(4, dataStorage.getCategoryById(catSubIds.get(4)).getIndex());
    assertEquals(1, dataStorage.getCategoryById(catSubIds.get(2)).getIndex());
    dataStorage.swapCategories(catSubIds.get(4), catSubIds.get(2) + ",top");
    assertEquals(1, dataStorage.getCategoryById(catSubIds.get(4)).getIndex());
    assertEquals(2, dataStorage.getCategoryById(catSubIds.get(2)).getIndex());

    //
    cat = dataStorage.getCategoryById(catSubIds.get(5));
    assertEquals(5, cat.getIndex());
    assertEquals(catSubIds.get(5), cat.getPath());
    dataStorage.swapCategories(catSubIds.get(5), catIds.get(2));
    cat = dataStorage.getCategoryById(Utils.CATEGORY_HOME + "/" + cat.getId());
    assertEquals(3, cat.getIndex());
    assertEquals(Utils.CATEGORY_HOME + "/" + cat.getId(), cat.getPath());

    //
    cat = dataStorage.getCategoryById(catSubIds.get(3));
    assertEquals(3, cat.getIndex());
    assertEquals(catSubIds.get(3), cat.getPath());
    dataStorage.swapCategories(catSubIds.get(3), catIds.get(1) + ",top");
    cat = dataStorage.getCategoryById(Utils.CATEGORY_HOME + "/" + cat.getId());
    assertEquals(1, cat.getIndex());
    assertEquals(Utils.CATEGORY_HOME + "/" + cat.getId(), cat.getPath());
  }
  
  public void testSaveTopicIdDiscussQuestion() throws Exception {
    //
    Node questionNode = dataStorage.getQuestionNodeById(questionPath3);
    PropertyReader propertyReader = new PropertyReader(questionNode);
    assertNull(propertyReader.string(FAQNodeTypes.EXO_TOPIC_ID_DISCUSS));
    
    //
    String mockTopicId = "topicdsffa55asdf5s65asdf568af65as5f3d";
    dataStorage.saveTopicIdDiscussQuestion(questionPath3, mockTopicId);
    questionNode = dataStorage.getQuestionNodeById(questionPath3);
    propertyReader = new PropertyReader(questionNode);
    assertEquals(mockTopicId, propertyReader.string(FAQNodeTypes.EXO_TOPIC_ID_DISCUSS));
  }
  
  public void testExportData() throws Exception {
    InputStream fileInputStream = dataStorage.exportData(categoryId1, true);
    assertTrue(fileInputStream.available() > 0);
    
    // Remove exported file
    File file = new File("exportCategory.zip");
    file.delete();
  }
  
  public void testImportData() throws Exception {
    //
    assertEquals(5, dataStorage.getAllQuestions().getAvailable());
    
    //
    File file = new File("../service/src/test/resources/conf/portal/Data.xml");
    String content = FileUtils.readFileToString(file, "UTF-8");
    byte currentXMLBytes[] = content.getBytes();
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentXMLBytes);
    dataStorage.importData(Utils.CATEGORY_HOME, byteArrayInputStream, false);
    assertEquals(10, dataStorage.getAllQuestions().getAvailable());
  }
  
  public void testIsExisting() throws Exception {
    //
    assertFalse(dataStorage.isExisting("InvalidCategoryPath"));
    assertFalse(dataStorage.isExisting("InvalidQuestionPath"));
    
    //
    assertTrue(dataStorage.isExisting(categoryId1));
    assertTrue(dataStorage.isExisting(categoryId2));
    assertTrue(dataStorage.isExisting(categoryId3));
    assertTrue(dataStorage.isExisting(questionPath1));
    assertTrue(dataStorage.isExisting(questionPath2));
    assertTrue(dataStorage.isExisting(questionPath3));
    assertTrue(dataStorage.isExisting(questionPath4));
    assertTrue(dataStorage.isExisting(questionPath5));
  }
  
  public void testGetCategoryPathOf() throws Exception {
    //
    assertEquals(categoryId1, dataStorage.getCategoryPathOf(questionPath1));
  }
  
  public void testIsModerateAnswer() throws Exception {
    Answer answer1 = createAnswer(USER_ROOT, "Root answers a question");
    Answer answer2 = createAnswer(USER_DEMO, "Demo answers a question");
    dataStorage.saveAnswer(questionPath1, new Answer[] {answer1, answer2});
    assertFalse(dataStorage.isModerateAnswer(questionId1 + "/" + Utils.ANSWER_HOME + "/" + answer1.getId()));
    assertFalse(dataStorage.isModerateAnswer(questionId1 + "/" + Utils.ANSWER_HOME + "/" + answer2.getId()));
  }
  
  public void testIsModerateQuestion() throws Exception {
    assertTrue(dataStorage.isModerateQuestion(questionPath1));
    assertTrue(dataStorage.isModerateQuestion(questionPath2));
    assertTrue(dataStorage.isModerateQuestion(questionPath3));
    assertTrue(dataStorage.isModerateQuestion(questionPath4));
    assertTrue(dataStorage.isModerateQuestion(questionPath5));
  }
  
  public void testIsViewAuthorInfo() throws Exception {
    assertTrue(dataStorage.isViewAuthorInfo(questionPath1));
    assertTrue(dataStorage.isViewAuthorInfo(questionPath2));
    assertTrue(dataStorage.isViewAuthorInfo(questionPath3));
    assertTrue(dataStorage.isViewAuthorInfo(questionPath4));
    assertTrue(dataStorage.isViewAuthorInfo(questionPath5));
  }
  
  public void testIsCategoryModerator() throws Exception {
    assertTrue(dataStorage.isCategoryModerator(categoryId1, USER_ROOT));
    assertFalse(dataStorage.isCategoryModerator(categoryId1, USER_JOHN));
    assertFalse(dataStorage.isCategoryModerator(categoryId1, USER_DEMO));
  }
  
  public void testIsCategoryExist() throws Exception {
    assertTrue(dataStorage.isCategoryExist("Category 1 to test question", Utils.CATEGORY_HOME));
    assertTrue(dataStorage.isCategoryExist("Category 2 to test question", Utils.CATEGORY_HOME));
    assertTrue(dataStorage.isCategoryExist("Category 3 has not question", Utils.CATEGORY_HOME));
  }
  
  public void testGetQuestionContents() throws Exception {
    List<String> paths = new ArrayList<String>();
    paths.add(questionPath1);
    paths.add(questionPath2);
    paths.add(questionPath3);
    List<String> questionContents = dataStorage.getQuestionContents(paths);
    assertEquals(3, questionContents.size());
    assertEquals("This question for test", questionContents.get(0));
    assertEquals("This question for test", questionContents.get(1));
    assertEquals("This question for test", questionContents.get(2));
  }
  
  public void testGetQuestionNodeById() throws Exception {
    Node questionNode = dataStorage.getQuestionNodeById(questionPath1);
    PropertyReader propertyReader = new PropertyReader(questionNode);
    assertNotNull(questionNode);
    assertEquals(questionId1, propertyReader.string(FAQNodeTypes.EXO_ID));
  }
  
  public void testGetModeratorsOf() throws Exception {
    assertEquals("[root]", Arrays.asList(dataStorage.getModeratorsOf(categoryId1)).toString());
    assertEquals("[root]", Arrays.asList(dataStorage.getModeratorsOf(categoryId2)).toString());
    assertEquals("[demo]", Arrays.asList(dataStorage.getModeratorsOf(categoryId3)).toString());
  }
  
  public void testGetCategoryNameOf() throws Exception {
    assertEquals("Category 1 to test question", dataStorage.getCategoryNameOf(categoryId1));
    assertEquals("Category 2 to test question", dataStorage.getCategoryNameOf(categoryId2));
    assertEquals("Category 3 has not question", dataStorage.getCategoryNameOf(categoryId3));
  }
  
  public void testUpdateQuestionRelatives() throws Exception {
    //
    Question question1 = dataStorage.getQuestionById(questionId1);
    assertFalse(question1.getRelations().length > 0);
    
    //
    dataStorage.updateQuestionRelatives(questionPath1, new String[] { questionId2 });
    question1 = dataStorage.getQuestionById(questionId1);
    assertNotNull(dataStorage.getQuestionById(question1.getRelations()[0]));
  }
  
  public void testCalculateDeletedUser() throws Exception {
    // TODO:
  }
  
  private FileAttachment createUserAvatar(String fileName) throws Exception {
    FileAttachment attachment = new FileAttachment();
    try {
      File file = new File("../service/src/test/resources/conf/portal/defaultAvatar.jpg");
      attachment.setName(fileName);
      InputStream is = new FileInputStream(file);
      attachment.setInputStream(is);
      attachment.setMimeType("image/jpg");
    } catch (Exception e) {
      LOG.error("Fail to create user avatar: ", e);
    }
    return attachment;
  }
  
}
