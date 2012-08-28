package org.exoplatform.ks.extras.injection.faq;

import java.util.HashMap;

import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Question;

public class AnswerInjector extends AbstractFAQInjector {
  
  /** . */
  private static final String NUMBER = "number";
  
  /** . */
  private static final String FROM_QUES = "fromQues";
  
  /** . */
  private static final String TO_QUES = "toQues";

  /** . */
  private static final String QUESTION_PREFIX = "quesPrefix";
  
  /** . */
  private static final String ANSWER_PREFIX = "answerPrefix";
  
  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    //
    int number = param(params, NUMBER);
    int fromQues = param(params, FROM_QUES);
    int toQues = param(params, TO_QUES);
    String questionPrefix = params.get(QUESTION_PREFIX);
    String answerPrefix = params.get(ANSWER_PREFIX);
    init(null, null, questionPrefix, answerPrefix, null, 0);
    
    //
    String questionName = null;
    String answerName = null;
    Question question = null;
    Answer answer = null;
    
    for (int i = fromQues;i <= toQues; i++) {
      //
      questionName = questionBase + i;
      question = getQuestionByName(questionName);
      if (question == null) {
        getLog().info("Question name '" + questionName + "' is wrong. Aborting injection ..." );
        return;
      }
      
      //
      for (int j = 0; j < number; j++) {
        //
        answerName = answerName();
       
        //
        answer = new Answer();
        answer.setFullName(answerName);
        answer.setLanguage("English");
        answer.setMarksVoteAnswer(0.0);
        answer.setMarkVotes(0);
        answer.setNew(true);
        answer.setResponseBy(question.getAuthor());
        answer.setResponses(lorem.getParagraphs(1));
        
        //
        faqService.saveAnswer(question.getPath(), answer, true);
        answerNumber++;
        
        //
        getLog().info("Answer '" + answerName + "' created by " + question.getAuthor());
      }
    }
  }
}
