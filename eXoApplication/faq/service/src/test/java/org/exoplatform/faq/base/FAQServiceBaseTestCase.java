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
package org.exoplatform.faq.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * Created by The eXo Platform SAS
 * Author : quangpld
 *          quangpld@exoplatform.com
 * Oct 15, 2012  
 */

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.faq.component.core.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.faq.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.faq.test.portal-configuration.xml")
})
public abstract class FAQServiceBaseTestCase extends BaseTestCase {

  protected static Log    LOG         = ExoLogger.getLogger(FAQServiceBaseTestCase.class);

  protected static String USER_ROOT   = "root";

  protected static String USER_JOHN   = "john";

  protected static String USER_DEMO   = "demo";

  protected static String questionPath1;

  protected static String questionPath2;

  protected static String questionPath3;

  protected static String questionPath4;

  protected static String questionPath5;

  protected static String categoryId1;

  protected static String categoryId2;

  protected static String categoryId3;

  protected static String questionId1;

  protected static String questionId2;

  protected static String questionId3;

  protected static String questionId4;

  protected static String questionId5;

  protected FAQService    faqService_;

  protected FAQSetting    faqSetting_ = new FAQSetting();

  public FAQServiceBaseTestCase() throws Exception {
  }

  public void setUp() throws Exception {
    begin();
    ConversationState conversionState = ConversationState.getCurrent();
    if(conversionState == null) {
      conversionState = new ConversationState(new Identity(USER_ROOT));
      ConversationState.setCurrent(conversionState);
    }
    faqService_ = (FAQService) getService(FAQService.class);
    
    //
    faqSetting_ = new FAQSetting();
    faqSetting_.setDisplayMode("both");
    faqSetting_.setOrderBy("created");
    faqSetting_.setOrderType("asc");
    faqSetting_.setSortQuestionByVote(true);
    faqSetting_.setIsAdmin("TRUE");
    faqSetting_.setEmailMoveQuestion("content email move question");
    faqSetting_.setEmailSettingSubject("Send notify watched");
    faqSetting_.setEmailSettingContent("Question content: &questionContent_ <br/>Response: &questionResponse_ <br/> link: &questionLink_");
    
    //
    defaultData();
  }

  public void tearDown() throws Exception {
    removeData();
    end();
  }
  
  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }
  
  protected void defaultData() throws Exception {
    //
    Category cate1 = createCategory("Category 1 to test question", 1);
    categoryId1 = Utils.CATEGORY_HOME + "/" + cate1.getId();

    //
    Category cate2 = createCategory("Category 2 to test question", 2);
    categoryId2 = Utils.CATEGORY_HOME + "/" + cate2.getId();

    //
    Category cate3 = createCategory("Category 3 has not question", 3);
    categoryId3 = Utils.CATEGORY_HOME + "/" + cate3.getId();
    cate3.setModerators(new String[] { "demo" });

    //
    faqService_.saveCategory(Utils.CATEGORY_HOME, cate1, true);
    faqService_.saveCategory(Utils.CATEGORY_HOME, cate2, true);
    faqService_.saveCategory(Utils.CATEGORY_HOME, cate3, true);

    //
    Question question1 = createQuestion(categoryId1);
    questionId1 = question1.getId();
    questionPath1 = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId1;

    //
    Question question2 = createQuestion(categoryId1);
    question2.setRelations(new String[] {});
    question2.setLanguage("English");
    question2.setAuthor(USER_ROOT);
    question2.setEmail("root@exoplatform.com");
    question2.setDetail("Really?");
    question2.setCreatedDate(new Date());
    questionId2 = question2.getId();
    questionPath2 = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId2;

    //
    Question question3 = createQuestion(categoryId1);
    question3.setRelations(new String[] {});
    question3.setLanguage("English");
    question3.setAuthor("Kenny");
    question3.setEmail("kenny@exoplatform.com");
    question3.setDetail("What does eXo Forum do?");
    question3.setCreatedDate(new Date());
    questionId3 = question3.getId();
    questionPath3 = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId3;

    //
    Question question4 = createQuestion(categoryId1);
    question4.setRelations(new String[] {});
    question4.setLanguage("English");
    question4.setAuthor("John Anthony");
    question4.setEmail("john@exoplatform.com");
    question4.setDetail("Tell me why?");
    question4.setCreatedDate(new Date());
    questionId4 = question4.getId();
    questionPath4 = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId4;

    //
    Question question5 = createQuestion(categoryId1);
    question5.setRelations(new String[] {});
    question5.setLanguage("English");
    question5.setAuthor("Mary Kelly");
    question5.setEmail("mary@exoplatform.com");
    question5.setDetail("How can I build eXo Forum?");
    question5.setCreatedDate(new Date());
    questionId5 = question5.getId();
    questionPath5 = categoryId1 + "/" + Utils.QUESTION_HOME + "/" + questionId5;

    //
    faqService_.saveQuestion(question1, true, faqSetting_);
    faqService_.saveQuestion(question2, true, faqSetting_);
    faqService_.saveQuestion(question3, true, faqSetting_);
    faqService_.saveQuestion(question4, true, faqSetting_);
    faqService_.saveQuestion(question5, true, faqSetting_);
  }
  
  protected void removeData() throws Exception {
    FAQSetting faqSetting = new FAQSetting();
    faqSetting.setIsAdmin("TRUE");
    List<Category> categories = faqService_.getSubCategories(Utils.CATEGORY_HOME, faqSetting, false, null);
    for (Category category : categories) {
      faqService_.removeCategory(category.getPath());
    }
  }
  
  protected Category createCategory(String categoryName, int  index) {
    Date date = new Date();
    Category category = new Category();
    category.setName(categoryName);
    category.setDescription("Description");
    category.setModerateQuestions(true);
    category.setModerateAnswers(true);
    category.setViewAuthorInfor(true);
    category.setModerators(new String[] { USER_ROOT });
    category.setCreatedDate(date);
    category.setUserPrivate(new String[] { "" });
    category.setIndex(index);
    category.setView(true);
    return category;
  }

  protected Question createQuestion(String cateId) throws Exception {
    Question question = new Question();
    question.setLanguage("English");
    question.setQuestion("This question for test");
    question.setDetail("Add new question 1");
    question.setAuthor(USER_ROOT);
    question.setEmail("exotesting@exoplatform.com");
    question.setActivated(true);
    question.setApproved(true);
    question.setCreatedDate(new Date());
    question.setCategoryId(cateId);
    question.setCategoryPath(cateId);
    question.setRelations(new String[] {});
    question.setAttachMent(new ArrayList<FileAttachment>());
    question.setAnswers(new Answer[] {});
    question.setComments(new Comment[] {});
    question.setUsersVote(new String[] {});
    question.setMarkVote(0.0);
    question.setUsersWatch(new String[] {});
    question.setEmailsWatch(new String[] {});
    question.setTopicIdDiscuss(null);
    return question;
  }
  
  protected Answer createAnswer(String user, String content) {
    Answer answer = new Answer();
    answer.setActivateAnswers(true);
    answer.setApprovedAnswers(true);
    answer.setDateResponse(new Date());
    answer.setMarksVoteAnswer(0);
    answer.setMarkVotes(0);
    answer.setNew(true);
    answer.setPostId(null);
    answer.setResponseBy(user);
    answer.setResponses(content);
    answer.setUsersVoteAnswer(null);
    answer.setLanguage("English");
    return answer;
  }

  protected Comment createComment(String user, String content) {
    Comment comment = new Comment();
    comment.setCommentBy(user);
    comment.setComments(content);
    comment.setDateComment(new Date());
    comment.setNew(true);
    comment.setPostId(null);
    comment.setFullName(user + " " + user);
    return comment;
  }

  protected QuestionLanguage createQuestionLanguage(String language) {
    QuestionLanguage questionLanguage = new QuestionLanguage();
    questionLanguage.setAnswers(null);
    questionLanguage.setComments(null);
    questionLanguage.setDetail("detail for language " + language);
    questionLanguage.setLanguage(language);
    questionLanguage.setQuestion("test question for language " + language);
    return questionLanguage;
  }

  protected Watch createNewWatch(String user, String mail) {
    Watch watch = new Watch();
    watch.setUser(user);
    watch.setEmails(mail);
    return watch;
  }
}
