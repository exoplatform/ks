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

package org.exoplatform.ks.extras.injection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.ks.extras.injection.faq.AnswerInjector;
import org.exoplatform.ks.extras.injection.faq.AttachmentInjector;
import org.exoplatform.ks.extras.injection.faq.CategoryInjector;
import org.exoplatform.ks.extras.injection.faq.CommentInjector;
import org.exoplatform.ks.extras.injection.faq.MembershipInjector;
import org.exoplatform.ks.extras.injection.faq.ProfileInjector;
import org.exoplatform.ks.extras.injection.faq.QuestionInjector;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;

/**
 * @author <a href="mailto:thanhvc@exoplatform.com">Thanh Vu</a>
 * @version $Revision$
 */
public class InjectorFAQTestCase extends BaseTestCase {

  private OrganizationService organizationService;
  private FAQService faqService;
  private UserHandler userHandler;
 
  //
  private AnswerInjector answerInjector;
  private CategoryInjector categoryInjector;
  private CommentInjector commentInjector;
  private ProfileInjector profileInjector;
  private QuestionInjector questionInjector;
  private AttachmentInjector attachmentInjector;
  private MembershipInjector membershipInjector;
  
  
  private HashMap<String, String> params;
  private List<String> users;
  
  
  
  @Override
  public void setUp() throws Exception {

    super.setUp();
    
    //
    profileInjector = (ProfileInjector) getContainer().getComponentInstanceOfType(ProfileInjector.class);
    categoryInjector = (CategoryInjector) getContainer().getComponentInstanceOfType(CategoryInjector.class);
    answerInjector = (AnswerInjector) getContainer().getComponentInstanceOfType(AnswerInjector.class);
    commentInjector = (CommentInjector) getContainer().getComponentInstanceOfType(CommentInjector.class);
    questionInjector = (QuestionInjector) getContainer().getComponentInstanceOfType(QuestionInjector.class);
    attachmentInjector = (AttachmentInjector) getContainer().getComponentInstanceOfType(AttachmentInjector.class);
    membershipInjector = (MembershipInjector) getContainer().getComponentInstanceOfType(MembershipInjector.class);
    
    //
    organizationService = (OrganizationService) getContainer().getComponentInstanceOfType(OrganizationService.class);
    faqService = (FAQService) getContainer().getComponentInstanceOfType(FAQService.class);
    userHandler = organizationService.getUserHandler();
    
    
    assertNotNull(profileInjector);
    assertNotNull(categoryInjector);
    assertNotNull(answerInjector);
    assertNotNull(commentInjector);
    assertNotNull(questionInjector);
    assertNotNull(attachmentInjector);
    assertNotNull(organizationService);
    assertNotNull(faqService);
    
    //
    params = new HashMap<String, String>();
    users = new ArrayList<String>();
  }

  @Override
  public void tearDown() throws Exception {
    //
    List<Category> categories =  faqService.getAllCategories();
    for (Category cat : categories) {
      faqService.removeCategory(cat.getPath());
    }
    
    List<Question> questions = faqService.getAllQuestions().getAll();
    for (Question ques : questions) {
      faqService.removeQuestion(ques.getId());
    }
    
    //
    for(String user : users) {
      userHandler.removeUser(user, true);
    }
    
    super.tearDown();
  }
  
  public void testDefaultProfile() throws Exception {
    performProfileTest(null);
  }
  
  public void testPrefixProfile() throws Exception {
    performProfileTest("foo");
  }
  
  public void testDefaultCategory() throws Exception {
    performCategoryTest(null, null);
  }
  
  public void testPrefixCategory() throws Exception {
    performCategoryTest("foo", "bar");
  }
  
  public void testDefaultQuestion() throws Exception {
    performQuestionTest(null, null, null);
  }
  
  public void testPrefixQuestion() throws Exception {
    performQuestionTest("foo", "bar", "foo.question");
  }
  
  public void testDefaultAnswer() throws Exception {
    performAnswerTest(null, null, null, null);
  }
  
  public void testPrefixAnswer() throws Exception {
    performAnswerTest("foo", "foo.category", "bar", "bar.answer");
  }
  
  public void testDefaultComment() throws Exception {
    performCommentTest(null, null, null, null, null);
  }
  
  public void testPrefixComment() throws Exception {
    performCommentTest("foo", "foo.category", "bar.question", "bar.answer", "bar.comment");
  }
  
  public void testDefaultAttachment() throws Exception {
    performAttachmentTest(null, null, null, null, null);
  }
  
  public void testPrefixAttachment() throws Exception {
    performAttachmentTest("foo", "foo.category", "bar.question", "bar.answer", "bar.comment");
  }
  
  public void testDefaultMembership() throws Exception {
    performMembershipTest(null, null, null, null);
  }
  
  public void testPrefixMembership() throws Exception {
    performMembershipTest("foo", "foo.category", "bar", "bar.answer");
  }
  
  private void performProfileTest(String prefix) throws Exception {
    //
    String baseName = (prefix == null ? "bench.user" : prefix);
    assertClean(baseName, null, null);
    
    //
    params.put("number", "5");
    if (prefix != null) {
      params.put("userPrefix", prefix);
    }

    profileInjector.inject(params);
    assertEquals(5, profileInjector.userNumber(baseName));
    assertNotNull(userHandler.findUserByName(baseName + "0"));
    assertNotNull(userHandler.findUserByName(baseName + "1"));
    assertNotNull(userHandler.findUserByName(baseName + "2"));
    assertNotNull(userHandler.findUserByName(baseName + "3"));
    assertNotNull(userHandler.findUserByName(baseName + "4"));
    
    //
    cleanProfile(baseName, 5);
  }
  
  private void performCategoryTest(String userPrefix, String catPrefix) throws Exception {
    //
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String catBaseName = (catPrefix == null ? "bench.cat" : catPrefix);
    assertClean(userBaseName, catBaseName, null);
    
    //
    params.put("number", "3");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    
    //
    profileInjector.inject(params);
    assertEquals(3, profileInjector.userNumber(userBaseName));
    assertNotNull(userHandler.findUserByName(userBaseName + "0"));
    assertNotNull(userHandler.findUserByName(userBaseName + "1"));
    assertNotNull(userHandler.findUserByName(userBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "6");
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    categoryInjector.inject(params);
    assertEquals(6, categoryInjector.categoryNumber(catBaseName));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "0"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "1"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "2"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "3"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "4"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "5"));
    
    cleanProfile(userBaseName, 3);
  }
  
  private void performQuestionTest(String userPrefix, String catPrefix, String quesPrefix) throws Exception {
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String catBaseName = (catPrefix == null ? "bench.cat" : catPrefix);
    String quesBaseName = (quesPrefix == null ? "bench.ques" : quesPrefix);
    assertClean(userBaseName, catBaseName, quesBaseName);

    params.put("number", "3");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    
    profileInjector.inject(params);
    
    assertNotNull(userHandler.findUserByName(userBaseName + "0"));
    assertNotNull(userHandler.findUserByName(userBaseName + "1"));
    assertNotNull(userHandler.findUserByName(userBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "3");
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    categoryInjector.inject(params);
    assertEquals(3, categoryInjector.categoryNumber(catBaseName));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "0"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "1"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "6");
    params.put("toCat", "0");
    params.put("toUser", "0");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    questionInjector.inject(params);
    assertEquals(6, questionInjector.questionNumber(quesBaseName));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "0"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "1"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "2"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "3"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "4"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "5"));
    
    cleanProfile(userBaseName, 3);
  }
  
  private void performAnswerTest(String userPrefix, String catPrefix, String quesPrefix, String answerPrefix) throws Exception {
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String catBaseName = (catPrefix == null ? "bench.cat" : catPrefix);
    String quesBaseName = (quesPrefix == null ? "bench.ques" : quesPrefix);
    String answerBaseName = (answerPrefix == null ? "bench.answer" : answerPrefix);
    assertClean(userBaseName, catBaseName, quesBaseName);

    params.put("number", "3");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    
    profileInjector.inject(params);
    
    assertNotNull(userHandler.findUserByName(userBaseName + "0"));
    assertNotNull(userHandler.findUserByName(userBaseName + "1"));
    assertNotNull(userHandler.findUserByName(userBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "3");
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    categoryInjector.inject(params);
    assertEquals(3, categoryInjector.categoryNumber(catBaseName));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "0"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "1"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "3");
    params.put("toCat", "0");
    params.put("toUser", "0");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    questionInjector.inject(params);
    assertEquals(3, questionInjector.questionNumber(quesBaseName));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "0"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "1"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "6");
    params.put("toQues", "0");
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    if (answerPrefix != null) {
      params.put("answerPrefix", answerPrefix);
    }
    answerInjector.inject(params);
    assertEquals(6, answerInjector.answerNumber(answerBaseName));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "0"));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "1"));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "2"));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "3"));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "4"));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "5"));
    
    cleanProfile(userBaseName, 3);
  }
  
  private void performCommentTest(String userPrefix, String catPrefix, String quesPrefix, String answerPrefix, String commentPrefix) throws Exception {
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String catBaseName = (catPrefix == null ? "bench.cat" : catPrefix);
    String quesBaseName = (quesPrefix == null ? "bench.ques" : quesPrefix);
    String answerBaseName = (answerPrefix == null ? "bench.answer" : answerPrefix);
    String commentBaseName = (commentPrefix == null ? "bench.comment" : commentPrefix);
    assertClean(userBaseName, catBaseName, quesBaseName);

    params.put("number", "3");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    
    profileInjector.inject(params);
    
    assertNotNull(userHandler.findUserByName(userBaseName + "0"));
    assertNotNull(userHandler.findUserByName(userBaseName + "1"));
    assertNotNull(userHandler.findUserByName(userBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "3");
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    categoryInjector.inject(params);
    assertEquals(3, categoryInjector.categoryNumber(catBaseName));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "0"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "1"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "3");
    params.put("toCat", "0");
    params.put("toUser", "0");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    questionInjector.inject(params);
    assertEquals(3, questionInjector.questionNumber(quesBaseName));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "0"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "1"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "3");
    params.put("toQues", "0");
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    if (answerPrefix != null) {
      params.put("answerPrefix", answerPrefix);
    }
    answerInjector.inject(params);
    assertEquals(3, answerInjector.answerNumber(answerBaseName));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "0"));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "1"));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "6");
    params.put("toQues", "0");
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    if (commentPrefix != null) {
      params.put("commentPrefix", commentPrefix);
    }
    commentInjector.inject(params);
    assertEquals(6, commentInjector.commentNumber(commentBaseName));
    assertNotNull(commentInjector.getCommentByName(commentBaseName + "0"));
    assertNotNull(commentInjector.getCommentByName(commentBaseName + "1"));
    assertNotNull(commentInjector.getCommentByName(commentBaseName + "2"));
    assertNotNull(commentInjector.getCommentByName(commentBaseName + "3"));
    assertNotNull(commentInjector.getCommentByName(commentBaseName + "4"));
    assertNotNull(commentInjector.getCommentByName(commentBaseName + "5"));
    
    cleanProfile(userBaseName, 3);
  }
  
  private void performAttachmentTest(String userPrefix, String catPrefix, String quesPrefix, String answerPrefix, String commentPrefix) throws Exception {
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String catBaseName = (catPrefix == null ? "bench.cat" : catPrefix);
    String quesBaseName = (quesPrefix == null ? "bench.ques" : quesPrefix);
    String answerBaseName = (answerPrefix == null ? "bench.answer" : answerPrefix);
    String commentBaseName = (commentPrefix == null ? "bench.comment" : commentPrefix);
    assertClean(userBaseName, catBaseName, quesBaseName);

    params.put("number", "3");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    
    profileInjector.inject(params);
    
    assertNotNull(userHandler.findUserByName(userBaseName + "0"));
    assertNotNull(userHandler.findUserByName(userBaseName + "1"));
    assertNotNull(userHandler.findUserByName(userBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "3");
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    categoryInjector.inject(params);
    assertEquals(3, categoryInjector.categoryNumber(catBaseName));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "0"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "1"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "3");
    params.put("toCat", "0");
    params.put("toUser", "0");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    questionInjector.inject(params);
    assertEquals(3, questionInjector.questionNumber(quesBaseName));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "0"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "1"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "3");
    params.put("toQues", "0");
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    if (answerPrefix != null) {
      params.put("answerPrefix", answerPrefix);
    }
    answerInjector.inject(params);
    assertEquals(3, answerInjector.answerNumber(answerBaseName));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "0"));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "1"));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "3");
    params.put("toQues", "0");
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    if (commentPrefix != null) {
      params.put("commentPrefix", commentPrefix);
    }
    commentInjector.inject(params);
    assertEquals(3, commentInjector.commentNumber(commentBaseName));
    assertNotNull(commentInjector.getCommentByName(commentBaseName + "0"));
    assertNotNull(commentInjector.getCommentByName(commentBaseName + "1"));
    assertNotNull(commentInjector.getCommentByName(commentBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "3");
    params.put("fromQues", "0");
    params.put("toQues", "2");
    params.put("byteSize", "50");
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    attachmentInjector.inject(params);
    assertEquals(3, attachmentInjector.getQuestionByName(quesBaseName + "0").getAttachMent().size());
    assertEquals(3, attachmentInjector.getQuestionByName(quesBaseName + "1").getAttachMent().size());
    assertEquals(3, attachmentInjector.getQuestionByName(quesBaseName + "2").getAttachMent().size());
    
    cleanProfile(userBaseName, 3);
  }
  
  private void performMembershipTest(String userPrefix, String catPrefix, String quesPrefix, String answerPrefix) throws Exception {
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String catBaseName = (catPrefix == null ? "bench.cat" : catPrefix);
    String quesBaseName = (quesPrefix == null ? "bench.ques" : quesPrefix);
    String answerBaseName = (answerPrefix == null ? "bench.answer" : answerPrefix);
    assertClean(userBaseName, catBaseName, quesBaseName);

    params.put("number", "3");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    
    profileInjector.inject(params);
    
    assertNotNull(userHandler.findUserByName(userBaseName + "0"));
    assertNotNull(userHandler.findUserByName(userBaseName + "1"));
    assertNotNull(userHandler.findUserByName(userBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "3");
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    categoryInjector.inject(params);
    assertEquals(3, categoryInjector.categoryNumber(catBaseName));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "0"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "1"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "2"));
    
    //
    params.clear();
    params.put("type", "category");
    params.put("toType", "2");
    params.put("fromUser", "0");
    params.put("toUser", "2");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
      params.put("typePrefix", catPrefix);
    }
    membershipInjector.inject(params);
    assertEquals(3, categoryInjector.getCategoryByName(catBaseName + "2").getModerators().length);
    assertEquals(3, categoryInjector.getCategoryByName(catBaseName + "2").getUserPrivate().length);
    
    //
    params.clear();
    params.put("number", "3");
    params.put("toCat", "0");
    params.put("toUser", "0");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    questionInjector.inject(params);
    assertEquals(3, questionInjector.questionNumber(quesBaseName));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "0"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "1"));
    assertNotNull(questionInjector.getQuestionByName(quesBaseName + "2"));
    
    //
    params.clear();
    params.put("type", "question");
    params.put("toType", "1");
    params.put("fromUser", "0");
    params.put("toUser", "2");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
      params.put("typePrefix", quesPrefix);
    }
    membershipInjector.inject(params);
    assertEquals(3, questionInjector.getQuestionByName(quesBaseName + "1").getUsersVote().length);
    
    //
    params.clear();
    params.put("number", "3");
    params.put("toQues", "0");
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    if (answerPrefix != null) {
      params.put("answerPrefix", answerPrefix);
    }
    answerInjector.inject(params);
    assertEquals(3, answerInjector.answerNumber(answerBaseName));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "0"));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "1"));
    assertNotNull(answerInjector.getAnswerByName(answerBaseName + "2"));
    
    //
    params.clear();
    params.put("type", "answer");
    params.put("toType", "0");
    params.put("fromUser", "0");
    params.put("toUser", "2");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (quesPrefix != null) {
      params.put("quesPrefix", quesPrefix);
    }
    if (answerPrefix != null) {
      params.put("answerPrefix", answerPrefix);
      params.put("typePrefix", answerPrefix);
    }
    membershipInjector.inject(params);
    assertEquals(3, answerInjector.getAnswerByName(answerBaseName + "0").getUsersVoteAnswer().length);
    
    cleanProfile(userBaseName, 3);
  }
  
  private void assertClean(String userBaseName, String categoryBaseName, String questionBaseName) throws Exception {
    if (userBaseName != null) {
      assertNull(userHandler.findUserByName(userBaseName + "0"));
      assertNull(categoryInjector.getCategoryByName(categoryBaseName + "0"));
      assertNull(questionInjector.getQuestionByName(questionBaseName + "0"));
    }
  }
  
  private void cleanProfile(String prefix, int number) {

    for (int i = 0; i < number; i++) {
      users.add(prefix + i);
    }
  }
}
