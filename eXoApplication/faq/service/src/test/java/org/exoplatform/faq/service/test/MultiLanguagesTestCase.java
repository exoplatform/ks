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

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.faq.base.FAQServiceBaseTestCase;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.impl.MultiLanguages;

/**
 * Created by The eXo Platform SAS
 * Author : quangpld
 *          quangpld@exoplatform.com
 * Oct 5, 2012  
 */
public class MultiLanguagesTestCase extends FAQServiceBaseTestCase {

  public MultiLanguagesTestCase() throws Exception {
    super();
  }
  
  public void setUp() throws Exception {
    //
    super.setUp();
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testAddLanguage() throws Exception {
    //
    List<QuestionLanguage> questionLanguages = faqService_.getQuestionLanguages(questionPath1);
    assertEquals(1, questionLanguages.size());
    
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    questionLanguages = faqService_.getQuestionLanguages(questionPath1);
    assertEquals(3, questionLanguages.size());
  }
  
  public void testDeleteAnswerQuestionLang() throws Exception {
    //
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    QuestionLanguage questionLanguage = faqService_.getQuestionLanguageByLanguage(questionPath1, "VietNam");
    Answer answer = createAnswer("root", "Answer of language VietNam 1");
    String answerId = answer.getId();
    answer.setLanguage("VietNam");
    questionLanguage.setAnswers(new Answer[] { answer });
    faqService_.saveAnswer(questionPath1, questionLanguage);
    assertNotNull(faqService_.getAnswerById(questionPath1, answerId, "VietNam"));
    
    //
    MultiLanguages.deleteAnswerQuestionLang(questionNode, answerId, "VietNam");
    assertNull(faqService_.getAnswerById(questionPath1, answerId, "VietNam"));
  }
  
  public void testDeleteCommentQuestionLang() throws Exception {
    //
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    Comment comment = createComment("root", "New comment of question language");
    String commentId = comment.getId();
    comment.setNew(true);
    faqService_.saveComment(questionPath1, comment, "VietNam");
    assertNotNull(faqService_.getCommentById(questionPath1, commentId, "VietNam"));
    
    //
    MultiLanguages.deleteCommentQuestionLang(questionNode, commentId, "VietNam");
    assertNull(faqService_.getCommentById(questionPath1, commentId, "VietNam"));
  }
  
  public void testGetQuestionLanguageByLanguage() throws Exception {
    //
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    QuestionLanguage questionLanguage = MultiLanguages.getQuestionLanguageByLanguage(questionNode, "VietNam");
    assertNotNull(questionLanguage);
    assertEquals("VietNam", questionLanguage.getLanguage());
  }
  
  public void testGetCommentById() throws Exception {
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    Comment comment = createComment("root", "New comment of question language");
    String commentId = comment.getId();
    comment.setNew(true);
    faqService_.saveComment(questionPath1, comment, "VietNam");
    assertNotNull(MultiLanguages.getCommentById(questionNode, commentId, "VietNam"));
    assertNull(MultiLanguages.getCommentById(questionNode, commentId, "Thailand"));
  }
  
  public void testGetAnswerById() throws Exception {
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    QuestionLanguage questionLanguage = faqService_.getQuestionLanguageByLanguage(questionPath1, "VietNam");
    Answer answer = createAnswer("root", "Answer of language VietNam 1");
    String answerId = answer.getId();
    answer.setLanguage("VietNam");
    questionLanguage.setAnswers(new Answer[] { answer });
    faqService_.saveAnswer(questionPath1, questionLanguage);
    assertNotNull(MultiLanguages.getAnswerById(questionNode, answerId, "VietNam"));
  }
  
  public void testSaveAnswer() throws Exception {
    //
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    QuestionLanguage questionLanguage = faqService_.getQuestionLanguageByLanguage(questionPath1, "VietNam");
    Answer answer = createAnswer("root", "Answer of language VietNam 1");
    String answerId = answer.getId();
    answer.setLanguage("VietNam");
    questionLanguage.setAnswers(new Answer[] { answer });
    assertNull(MultiLanguages.getAnswerById(questionNode, answerId, "VietNam"));
    
    //
    MultiLanguages.saveAnswer(questionNode, questionLanguage);
    assertNotNull(MultiLanguages.getAnswerById(questionNode, answerId, "VietNam"));
  }
  
  public void testSaveComment() throws Exception {
    //
    Node questionNode = faqService_.getQuestionNodeById(questionId1);
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("VietNam"));
    MultiLanguages.addLanguage(questionNode, createQuestionLanguage("French"));
    Comment comment = createComment("root", "New comment of question language");
    String commentId = comment.getId();
    comment.setNew(true);
    assertNull(MultiLanguages.getCommentById(questionNode, commentId, "VietNam"));
    
    //
    MultiLanguages.saveComment(questionNode, comment, "VietNam");
    assertNotNull(MultiLanguages.getCommentById(questionNode, commentId, "VietNam"));
  }
  
  public void testRemoveLanguage() throws Exception {
    // TODO:
  }
  
  public void testVoteAnswer() throws Exception {
    // TODO:
  }
  
  public void testVoteQuestion() throws Exception {
    // TODO:
  }
  
  public void testUnVoteQuestion() throws Exception {
    // TODO:
  }

}
