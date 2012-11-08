/*
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
 */
package org.exoplatform.faq.service.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.exoplatform.faq.base.FAQServiceBaseTestCase;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.CategoryInfo;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.ObjectSearchResult;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.ks.common.NotifyInfo;

/**
 * Created by The eXo Platform SAS
 * Author : quangpld
 *          quangpld@exoplatform.com
 * Oct 15, 2012  
 */
@SuppressWarnings("unused")
public class FAQServiceTestCase extends FAQServiceBaseTestCase {

  private List<FileAttachment> listAttachments = new ArrayList<FileAttachment>();

  public FAQServiceTestCase() throws Exception {
    super();
  }

  public void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testFAQService() throws Exception {
    assertNotNull(faqService_);
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

  public void testCategory() throws Exception {
    // Remove default data
    removeData();
    // add category Id
    Category cate1 = createCategory("Cate 1", 0);
    cate1.setIndex(1);
    cate1.setUserPrivate(new String[]{"test", "manager:/admin/user"});
    faqService_.saveCategory(Utils.CATEGORY_HOME, cate1, true);

    Category cate2 = createCategory("Cate 2", 0);
    cate2.setIndex(2);
    cate2.setName("Nguyen van truong test category222222");
    cate2.setModerators(new String[] { "Demo" });
    cate2.setUserPrivate(new String[]{"test", "member:/abc/fod", "*:/admin/user"});
    faqService_.saveCategory(Utils.CATEGORY_HOME, cate2, true);

    // add sub category 1
    Category subCate1 = createCategory("Sub Cate 1", 0);
    subCate1.setIndex(1);
    subCate1.setName("Nguyen van truong test Sub category 1");
    subCate1.setModerators(new String[] { "marry", "Demo" });
    faqService_.saveCategory(Utils.CATEGORY_HOME + "/" + cate1.getId(), subCate1, true);

    // is Category Exist
    assertEquals("Category has name:" + cate1.getName() + "  is no longer exists.", faqService_.isCategoryExist(cate1.getName(), Utils.CATEGORY_HOME), true);
    // Get category by id
    cate1 = faqService_.getCategoryById(Utils.CATEGORY_HOME + "/" + cate1.getId());
    assertNotNull("Category have not been added", cate1);
    // Check category is already exist
    assertEquals("This category is't already exist", faqService_.isExisting(cate1.getPath()), true);

    // get infor of root category:
    assertEquals("Have two categories in root category", faqService_.getCategoryInfo(Utils.CATEGORY_HOME, faqSetting_)[0], 2);

    // Get path of category
    assertNotNull("Path of category node is null", faqService_.getCategoryPath(cate1.getPath()));

    // update category
    cate1.setName("Nguyen van truong test category111111");
    cate1.setCreatedDate(new Date());
    faqService_.saveCategory(Utils.CATEGORY_HOME, cate1, false);
    cate1 = faqService_.getCategoryById(cate1.getPath());
    assertEquals("Name of category 1 haven't been changed", "Nguyen van truong test category111111", cate1.getName());

    // get Categories with normal user
    FAQSetting faqSetting = new FAQSetting();
    faqSetting.setIsAdmin("false");
    List<Category> listCate = faqService_.getSubCategories(Utils.CATEGORY_HOME, faqSetting, true, Arrays.asList(new String[]{"demo", "/admin/user"}));
    assertEquals(1, listCate.size());
    // get Categories with moderators/administrators 
    listCate = faqService_.getSubCategories(Utils.CATEGORY_HOME, faqSetting, true, Arrays.asList(new String[]{"john", "manager:/admin/user"}));
    assertEquals(2, listCate.size());
    faqSetting.setIsAdmin("true");
    listCate = faqService_.getSubCategories(Utils.CATEGORY_HOME, faqSetting, true, new ArrayList<String>());
    assertEquals(2, listCate.size());

    // Get Maxindex of cateogry
    assertEquals("Root have two category and maxIndex of subcategories in root is't 2", faqService_.getMaxindexCategory(Utils.CATEGORY_HOME), 2);

    // get sub category
    List<Category> listSubCate = faqService_.getSubCategories(cate1.getPath(), faqSetting_, false, null);
    assertEquals("Category 1 not only have one subcategory", listSubCate.size(), 1);

    // update sub category
    subCate1 = listSubCate.get(0);
    subCate1.setName("Sub category 1");
    faqService_.saveCategory(cate1.getPath(), subCate1, false);
    assertEquals("Name of SubCategory 1 have not been changed from \"Sub Cate 1\" to \"Sub category 1\"", "Sub category 1", subCate1.getName());

    // get all Category
    List<Category> listAll = faqService_.getAllCategories();
    assertEquals("In FAQ System have less than 3 categories", listAll.size(), 3);

    // move category
    cate2 = faqService_.getCategoryById(Utils.CATEGORY_HOME + "/" + cate2.getId());
    faqService_.moveCategory(cate2.getPath(), cate1.getPath());
    cate2 = faqService_.getCategoryById(cate1.getPath() + "/" + cate2.getId());
    assertNotNull("Category 2 is not already exist in FAQ", cate2);

    // Delete category 2
    faqService_.removeCategory(cate2.getPath());
    List<Category> listAllAfterRemove = faqService_.getAllCategories();
    assertEquals("Category 2 have not been removed, in system have more than 2 categoies", listAllAfterRemove.size(), 2);

    // get list category by moderator
    List<String> listCateByModerator = faqService_.getListCateIdByModerator(null);
    assertEquals("User Root is't moderator of category Home and cate1", listCateByModerator.size(), 2);
  }

  public void testSwapCategories() throws Exception {
    // Remove default data.
    removeData();
    // add some categories in root category
    Category cat = null;
    List<String> catIds = new ArrayList<String>();
    catIds.add(Utils.CATEGORY_HOME);
    for (int i = 1; i <= 5; i++) {
      cat = createCategory("Category " + (new Random().nextInt(100) + " " + i), i);
      catIds.add(Utils.CATEGORY_HOME + "/" + cat.getId());
      faqService_.saveCategory(Utils.CATEGORY_HOME, cat, true);
    }
    // save some categories in last sub category
    String parentCatId = Utils.CATEGORY_HOME + "/" + cat.getId();
    List<String> catSubIds = new ArrayList<String>();
    catSubIds.add(parentCatId);
    for (int i = 1; i <= 5; i++) {
      cat = createCategory("Sub Category " + i, i);
      catSubIds.add(parentCatId + "/" + cat.getId());
      faqService_.saveCategory(parentCatId, cat, true);
    }

    // case 1: Move same parent, with index = 1 down to 3
    assertEquals("Index of category 1 before swap is't 1", faqService_.getCategoryById(catSubIds.get(1)).getIndex(), 1);
    assertEquals("Index of category 3 before swap is't 3", faqService_.getCategoryById(catSubIds.get(3)).getIndex(), 3);
    faqService_.swapCategories(catSubIds.get(1), catSubIds.get(3));
    assertEquals("Index of category 1 after swap is't 3", faqService_.getCategoryById(catSubIds.get(1)).getIndex(), 3);
    assertEquals("Index of category 3 after swap is't 2", faqService_.getCategoryById(catSubIds.get(3)).getIndex(), 2);

    // case 2: Move same parent, with index = 4 up to 1
    assertEquals("Index of category 4 before swap is't 4", faqService_.getCategoryById(catSubIds.get(4)).getIndex(), 4);
    assertEquals("Index of category 2 before swap is't 1", faqService_.getCategoryById(catSubIds.get(2)).getIndex(), 1);
    faqService_.swapCategories(catSubIds.get(4), catSubIds.get(2)+",top");
    assertEquals("Index of category 4 after swap is't 1", faqService_.getCategoryById(catSubIds.get(4)).getIndex(), 1);
    assertEquals("Index of category 2 after swap is't 2", faqService_.getCategoryById(catSubIds.get(2)).getIndex(), 2);

    // case 3: Move category of index x up to parent category with index y (y > 1).
    cat = faqService_.getCategoryById(catSubIds.get(5));
    assertEquals("Index of category 5 before swap is't 5", cat.getIndex(), 5);
    assertEquals(String.format("Path of category 5 before swap is't %s",cat.getPath()) , cat.getPath(), catSubIds.get(5));
    // move sub category 5 up to parent and has new index is 3  
    faqService_.swapCategories(catSubIds.get(5), catIds.get(2));
    cat = faqService_.getCategoryById(Utils.CATEGORY_HOME + "/" + cat.getId());
    assertEquals("Index of category 5 after swap is't 3", cat.getIndex(), 3);
    assertEquals(String.format("Path of category 5 after swap is't %s",cat.getPath()) , cat.getPath(), Utils.CATEGORY_HOME + "/" + cat.getId());

    //case 4: Move category of index x up to top parent category (new index = 1) .
    cat = faqService_.getCategoryById(catSubIds.get(3));
    assertEquals("Index of category 3 before swap is't 3", cat.getIndex(), 3);
    assertEquals(String.format("Path of category 3 before swap is't %s",cat.getPath()) , cat.getPath(), catSubIds.get(3));
    faqService_.swapCategories(catSubIds.get(3), catIds.get(1)+",top");
    cat = faqService_.getCategoryById(Utils.CATEGORY_HOME + "/" + cat.getId());
    assertEquals("Index of category 3 before swap is't 3", cat.getIndex(), 1);
    assertEquals(String.format("Path of category 3 before swap is't %s",cat.getPath()) , cat.getPath(), Utils.CATEGORY_HOME + "/" + cat.getId());
    /*
     * case 5: Move category into other category of the same level as this category.
     * and case 6: Move category up to the parent category and then move it into other category of the same level as the parent category.
     * it used function moveCategory(String categoryId, String destCategoryId); not use swapCategories(String cateId1, String cateId2);
     * 
    */
  }
  
  // FAQPortlet
  public void testCategoryInfo() throws Exception {
    // Get categoryInfo
    List<String> categoryIdScoped = new ArrayList<String>();
    CategoryInfo categoryInfo = faqService_.getCategoryInfo(Utils.CATEGORY_HOME, categoryIdScoped);
    assertEquals("Can not get info of category by categoryInfo.", categoryInfo.getSubCateInfos().size(), 3);
    // get QuestionInfo
    categoryIdScoped = new ArrayList<String>();
    categoryInfo = faqService_.getCategoryInfo(categoryId1, categoryIdScoped);
    assertEquals("Can not questionInfo  of category.", categoryInfo.getQuestionInfos().size(), 5);
    // remove Data when tested category
    // faqService_.removeCategory(Utils.CATEGORY_HOME);
  }

  public void testQuestion() throws Exception {
    // get question 1
    Question question = faqService_.getQuestionById(questionPath1);
    assertNotNull(question);
    List<Question> listQuestion = faqService_.getQuestionsNotYetAnswer(Utils.CATEGORY_HOME, false).getAll();
    assertEquals("have some questions are not yet answer", listQuestion.size(), 0);

    // update question
    String detail = "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur";
    question.setDetail(detail);
    faqService_.saveQuestion(question, false, faqSetting_);
    question = faqService_.getQuestionById(questionPath1);
    assertEquals(detail, question.getDetail());

    // update Question Relatives
    faqService_.updateQuestionRelatives(questionPath1, new String[] { questionPath2 });
    question = faqService_.getQuestionById(questionPath1);
    assertNotNull(questionPath1, question.getRelations()[0]);
    
    // move question 2 to category 2
    Category cate2 = faqService_.getCategoryById(categoryId2);
    List<String> listId = new ArrayList<String>();
    listId.add(questionPath2);
    assertEquals(0, faqService_.getQuestionsByCatetory(cate2.getPath(), faqSetting_).getAll().size());
    faqService_.moveQuestions(listId, cate2.getPath(), "", faqSetting_);
    assertEquals(1, faqService_.getQuestionsByCatetory(cate2.getPath(), faqSetting_).getAll().size());
    
    // Get question by list category
    listId = new ArrayList<String>();
    String catId = Utils.CATEGORY_HOME;
    if (!categoryId1.equals(Utils.CATEGORY_HOME)) {
      catId = categoryId1.substring(categoryId1.lastIndexOf("/") + 1);
    }
    listId.add(catId);
    JCRPageList pageList = faqService_.getQuestionsByListCatetory(listId, false);
    pageList.setPageSize(10);
    assertEquals(4, pageList.getPage(1, "root").size());

    // get list all question
    List<Question> listAllQuestion = faqService_.getAllQuestions().getAll();
    assertEquals(5, listAllQuestion.size());

    // get list question by category of question 1
    List<Question> listQuestionByCategory = faqService_.getQuestionsByCatetory(categoryId1, faqSetting_).getAll();
    assertEquals(4, listQuestionByCategory.size());

    // Get list paths of all question in category - removed
    // List<String> listPaths = faqService_.getListPathQuestionByCategory(cate.getId());
    // assertEquals("In Category 1 have more than 4 questions, because can't move question 2 to category 2", listPaths.size(), 4);

    // Get question node by id - removed
    // assertNotNull("Question1 is not already existing in system", faqService_.getQuestionNodeById(question1.getId()));

    // remove question
    faqService_.removeQuestion(questionPath1);
    assertNull(faqService_.getQuestionById(questionPath1));
  }

  public void testSearch() throws Exception {

    FAQEventQuery eventQuery = new FAQEventQuery();

    // search with text = "test"
    eventQuery.setText("test");
    eventQuery.setAdmin(true);
    eventQuery.setUserId(USER_ROOT);

    // quick search (for all questions and categories)
    eventQuery.setType(FAQEventQuery.CATEGORY_AND_QUESTION);
    List<ObjectSearchResult> listQuickSearch = faqService_.getSearchResults(eventQuery);
    assertEquals(7, listQuickSearch.size());// 2 category and 4 question

    // for all category
    eventQuery.setType(FAQEventQuery.FAQ_CATEGORY);
    listQuickSearch = faqService_.getSearchResults(eventQuery);
    assertEquals(2, listQuickSearch.size());// 2 category

    // search categories by moderator.
    eventQuery.setText("");
    eventQuery.setModerator("demo");
    listQuickSearch = faqService_.getSearchResults(eventQuery);
    assertEquals(listQuickSearch.size(), 1);

    // for all questions
    eventQuery.setText("test");
    eventQuery.setType(FAQEventQuery.FAQ_QUESTION);
    eventQuery.setLanguage("English");
    listQuickSearch = faqService_.getSearchResults(eventQuery);
    assertEquals(listQuickSearch.size(), 5);// 4 question

    // Search with Disapprove question by demo.
    Question question = faqService_.getQuestionById(questionPath2);
    assertNotNull(question);
    question.setApproved(false);
    faqService_.saveQuestion(question, false, faqSetting_);

    eventQuery.setAdmin(false);
    eventQuery.setUserId(USER_DEMO);
    listQuickSearch = faqService_.getSearchResults(eventQuery);
    assertEquals(4, listQuickSearch.size());

    // Search with UnActivate question by demo.
    question = faqService_.getQuestionById(questionPath3);
    assertNotNull(question);
    question.setActivated(false);
    faqService_.saveQuestion(question, false, faqSetting_);
    listQuickSearch = faqService_.getSearchResults(eventQuery);
    assertEquals(3, listQuickSearch.size());

    // search by author
    question.setAuthor("userTest");
    faqService_.saveQuestion(question, false, faqSetting_);
    
    eventQuery.setAuthor("userTest");
    eventQuery.setAdmin(true);
    eventQuery.setText("");
    listQuickSearch = faqService_.getSearchResults(eventQuery);
    assertEquals(1, listQuickSearch.size());
  }

  public void testAnswer() throws Exception {
    // create Answer
    Answer answer1 = createAnswer(USER_ROOT, "Root answer 1 for question");
    Answer answer2 = createAnswer(USER_DEMO, "Demo answer 2 for question");

    // Save answer:
    String questionId = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId1;
    faqService_.saveAnswer(questionId, new Answer[] { answer1, answer2 });

    // Get answer by id:
    assertNotNull("Answer 2 have not been added", faqService_.getAnswerById(questionId, answer2.getId()));

    // Update answers:
    assertEquals(answer1.getResponses(), "Root answer 1 for question");
    String content = "Root answer 1 for question edit";
    answer1.setResponses(content);
    faqService_.saveAnswer(questionId, answer1, false);
    assertEquals("Content of Answer have not been changed to \"Root answer 1 for question edit\"", faqService_.getAnswerById(questionId, answer1.getId()).getResponses(), content);

    // Get all answers of question:
    JCRPageList pageList = faqService_.getPageListAnswer(questionId, false);
    pageList.setPageSize(10);
    assertEquals("Question have 2 answers", pageList.getPageItem(0).size(), 2);

    // Delete answer
    faqService_.deleteAnswer(questionId, answer1.getId());
    pageList = faqService_.getPageListAnswer(questionId, false);
    pageList.setPageSize(10);
    assertEquals("Answer 1 have not been removed, question only have one answer", pageList.getPageItem(0).size(), 1);

    // remove Data when tested answer
    // faqService_.removeCategory(Utils.CATEGORY_HOME);
  }

  public void testComment() throws Exception {
    Comment comment1 = createComment(USER_ROOT, "Root comment 1 for question");
    Comment comment2 = createComment(USER_DEMO, "Demo comment 2 for question");
    // Save comment
    String questionId = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId1;
    faqService_.saveComment(questionId, comment1, true);
    faqService_.saveComment(questionId, comment2, true);

    // Get comment by Id:
    assertNotNull("Comment 1 have not been added ", faqService_.getCommentById(questionId, comment1.getId()));
    assertNotNull("Comment 1 have not been added ", faqService_.getCommentById(questionId, comment2.getId()));

    // Get all comment of question
    JCRPageList pageList = faqService_.getPageListComment(questionId);
    pageList.setPageSize(10);
    assertEquals("Question have two comments", pageList.getPageItem(0).size(), 2);

    // Delete comment by id
    faqService_.deleteComment(questionId, comment1.getId());
    pageList = faqService_.getPageListComment(questionId);
    pageList.setPageSize(10);
    assertEquals("Comment 1 is not removed", pageList.getPageItem(0).size(), 1);

  }

  public void _testImportData() throws Exception {
    // Before import data, number question is 0
    assertEquals("Before import data, number question is not 0", faqService_.getAllQuestions().getAvailable(), 0);
    try {
      File file = new File("../service/src/test/resources/conf/portal/Data.xml");
      String content = FileUtils.readFileToString(file, "UTF-8");
      byte currentXMLBytes[] = content.getBytes();
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentXMLBytes);
      faqService_.importData(Utils.CATEGORY_HOME, byteArrayInputStream, false);
    } catch (IOException e) {
      LOG.debug("Testing import data fail: ", e);
    }
    // After imported data, number questions is 5
    assertEquals("Before import data, number question is not 5", faqService_.getAllQuestions().getAvailable(), 5);
  }

  public void testWatchCategory() throws Exception {
    // add watch
    faqService_.addWatchCategory(categoryId1, createNewWatch(USER_ROOT, "maivanha1610@gmail.com"));
    faqService_.addWatchCategory(categoryId1, createNewWatch(USER_DEMO, "maivanha1610@yahoo.com"));
    faqService_.addWatchCategory(categoryId1, createNewWatch(USER_JOHN, "john@localhost.com"));

    // Check hasWatch of category
    assertEquals("This category has not watch.", faqService_.hasWatch(categoryId1), true);
    // check get All watch in category.
    assertEquals("Size of all watch in this category is not 3", faqService_.getWatchByCategory(categoryId1).size(), 3);
    // Check category is watched by user
    assertEquals("User root didn't watch this category", faqService_.isUserWatched(USER_ROOT, categoryId1), true);

    // get all categories are watched by user
    assertEquals("user root have not watched some categories", faqService_.getWatchedCategoryByUser(USER_ROOT).getAvailable(), 1);

    // Check unWatch Category by user
    faqService_.unWatchCategory(categoryId1, USER_ROOT);
    assertEquals("User root has watching this category", faqService_.isUserWatched(USER_ROOT, categoryId1), false);
  }

  public void testQuestionMultilanguage() throws Exception {
    // Add question language for question
    String questionId = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId1;
    faqService_.addLanguage(questionId, createQuestionLanguage("VietNam"));
    faqService_.addLanguage(questionId, createQuestionLanguage("French"));
    // Get all question language (it is 3: English(default), VietNam and French):
    List<QuestionLanguage> questionLanguages = faqService_.getQuestionLanguages(questionId);
    assertEquals("Language of this question is not 3", questionLanguages.size(), 3);
    // Get Question_language by language
    QuestionLanguage questionLanguage = faqService_.getQuestionLanguageByLanguage(questionId, "VietNam");
    assertNotNull("QuestionLanguage is Null.", questionLanguage);
    // add answer1 in question language by questionLanguage
    Answer answer = createAnswer(USER_ROOT, "Answer of language VietNam 1");
    String answerId = answer.getId();
    answer.setLanguage("VietNam");
    questionLanguage.setAnswers(new Answer[] { answer });
    faqService_.saveAnswer(questionId, questionLanguage);
    assertNotNull("Answer1 in question language is not save.", faqService_.getAnswerById(questionId, answerId, "VietNam"));

    // add answer2 in question language by answer
    answer = createAnswer(USER_ROOT, "Answer of language VietNam 2");
    answerId = answer.getId();
    answer.setLanguage("VietNam");
    faqService_.saveAnswer(questionId, answer, "VietNam");
    assertNotNull("Answer2 in question language is not save.", faqService_.getAnswerById(questionId, answerId, "VietNam"));

    // add comment in question language
    Comment comment = createComment(USER_ROOT, "New comment of question language");
    String commentId = comment.getId();
    comment.setNew(true);
    faqService_.saveComment(questionId, comment, "VietNam");
    assertNotNull("Comment in question language is not save.", faqService_.getCommentById(questionId, commentId, "VietNam"));
    // Delete answer in question language.
    faqService_.deleteAnswerQuestionLang(questionId, answerId, "VietNam");
    assertNull("Answer2 in question language is not deleted.", faqService_.getAnswerById(questionId, answerId, "VietNam"));
    // Delete comment in question language.
    faqService_.deleteCommentQuestionLang(questionId, commentId, "VietNam");
    assertNull("Comment in question language is not deleted.", faqService_.getCommentById(questionId, commentId, "VietNam"));
  }

  public void testUserSetting() throws Exception {
    // save userSetting information into user node
    faqSetting_.setDisplayMode("both");
    faqSetting_.setOrderBy("created");
    faqSetting_.setOrderType("asc");
    assertEquals("All data is not sorted by created date", faqSetting_.getOrderBy(), "created");
    assertEquals("Data is not sorted asc", faqSetting_.getOrderType(), "asc");
    faqService_.saveFAQSetting(faqSetting_, USER_ROOT);

    // get all userSetting information from user node and set for FAQSetting object
    FAQSetting setting = new FAQSetting();
    setting.setOrderBy(null);
    setting.setOrderType(null);
    assertNull("Set order by is not null before get user Setting", setting.getOrderBy());
    assertNull("Set order type is not null before get user setting", setting.getOrderType());
    faqService_.getUserSetting(USER_ROOT, setting);
    assertEquals("Get setting of user,data is not order by created date", setting.getOrderBy(), "created");
    assertEquals("Get setting of user,data is not order asc", setting.getOrderType(), "asc");

    // update userSetting information in to user node
    setting.setSortQuestionByVote(false);
    setting.setOrderBy("alpha");
    setting.setOrderType("des");
    faqService_.saveFAQSetting(setting, USER_ROOT);
    assertEquals("user setting before save,do not order by created date", faqSetting_.getOrderBy(), "created");
    assertEquals("user setting before save,do not order asc", faqSetting_.getOrderType(), "asc");
    faqService_.getUserSetting(USER_ROOT, faqSetting_);
    assertEquals("user setting after saved,do not order by created alphabet", faqSetting_.getOrderBy(), "alpha");
    assertEquals("user setting before saveddo ,do not order des", faqSetting_.getOrderType(), "des");

    // Get all admins of FAQ
    List<String> list = faqService_.getAllFAQAdmin();
    assertNotNull(list);
    assertEquals("User demo is addmin of FAQ System", faqService_.isAdminRole(USER_DEMO), false);
  }

  public void testUserAvatar() throws Exception {
    // Add new avatar for user:
    faqService_.saveUserAvatar(USER_ROOT, createUserAvatar("defaultAvatar.jpg"));

    // Get user avatar
    assertNotNull(faqService_.getUserAvatar(USER_ROOT));

    // Set default avartar for user
    faqService_.setDefaultAvatar(USER_ROOT);
    assertNull(faqService_.getUserAvatar(USER_ROOT));
  }

  public void testGetPendingMessages() throws Exception {
    Question question = faqService_.getQuestionById(categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId1);
    question.setEmail("dttempmail@gmail.com");
    question.setEmailsWatch(new String[] { "duytucntt@gmail.com, tu.duy@exoplatform.com" });
    question.setUsersWatch(new String[] { "root, demo" });
    Answer answer = createAnswer(USER_ROOT, "Answer Content");
    question.setAnswers(new Answer[] { answer });
    question.setLink("http://domain.com/portal/public/classic");
    faqSetting_.setDisplayMode("approved");
    // save question for send email watched
    faqService_.saveQuestion(question, false, faqSetting_);
    Iterator<NotifyInfo> iterator = faqService_.getPendingMessages();
    List<String> emails = new ArrayList<String>();
    while (iterator.hasNext()) {
      NotifyInfo notifyInfo = iterator.next();
      emails = notifyInfo.getEmailAddresses();
    }
    assertEquals(emails.toString(), "[dttempmail@gmail.com, duytucntt@gmail.com, tu.duy@exoplatform.com]");
  }

}
