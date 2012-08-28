package org.exoplatform.ks.extras.injection.faq;

import java.util.HashMap;

import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.Question;

public class CommentInjector extends AbstractFAQInjector {
  
  /** . */
  private static final String NUMBER = "number";
  
  /** . */
  private static final String TO_QUES = "toQues";
   
  /** . */
  private static final String QUESTION_PREFIX = "quesPrefix";
  
  /** . */
  private static final String COMMENT_PREFIX = "commentPrefix";
   
  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    //
    int number = param(params, NUMBER);
    int toQues = param(params, TO_QUES);
    String questionPrefix = params.get(QUESTION_PREFIX);
    String commentPrefix = params.get(COMMENT_PREFIX);
    init(null, null, questionPrefix, null, commentPrefix, 0);

    //
    String questionName = questionBase + toQues;
    Question question = getQuestionByName(questionName);
    if (question == null) {
      getLog().info("Question name '" + questionName + "' is wrong. Aborting injection ..." );
      return;
    }
    
    //
    String commentName = null;
    Comment comment = null;

    for (int i = 0; i < number; i++) {
      //
      commentName = commentName();
      
      //
      comment = new Comment();
      comment.setCommentBy(question.getAuthor());
      comment.setComments(lorem.getParagraphs(1));
      comment.setFullName(commentName);
      comment.setNew(true);
      comment.setPostId("");
      
      //
      faqService.saveComment(question.getPath(), comment, true);
      commentNumber++;
      
      //
      getLog().info("Comment '" + commentName + "' created by " + question.getAuthor());
    }
  }
}
