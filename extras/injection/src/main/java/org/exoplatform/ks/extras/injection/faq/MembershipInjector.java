package org.exoplatform.ks.extras.injection.faq;

import java.util.Arrays;
import java.util.HashMap;

import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;

public class MembershipInjector extends AbstractFAQInjector {
  
  /** . */
  private static final String TYPE        = "type";

  /** . */
  private static final String TO_TYPE     = "toType";
  
  /** . */
  private static final String TYPE_PREFIX = "typePrefix";

  /** . */
  private static final String FROM_USER   = "fromUser";

  /** . */
  private static final String TO_USER     = "toUser";

  /** . */
  private static final String USER_PREFIX = "userPrefix";
  
  private int      toType;
  private String   typePrefix;
  private int      fromUser;
  private int      toUser;
  private String   userPrefix;

  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    //
    String type = params.get(TYPE);
    if (type == null | type.length() <= 0) {
      getLog().info("Type value is wrong! Please set it exactly with 'category', 'question', or 'answer' value. Aborting injection ...");
      return;
    }
    
    //
    this.toType = param(params, TO_TYPE);
    this.typePrefix = params.get(TYPE_PREFIX);
    this.fromUser = param(params, FROM_USER);
    this.toUser = param(params, TO_USER);
    this.userPrefix = params.get(USER_PREFIX);
    
    //
    if ("category".equals(type)) {
      init(userPrefix, typePrefix, null, null, null, 0);
      injectCategory();
    } else if ("question".equals(type)) {
      init(userPrefix, null, typePrefix, null, null, 0);
      injectQuestion();
    } else if ("answer".equals(type)) {
      init(userPrefix, null, null, typePrefix, null, 0);
      injectAnswer();
    }
  }
  
  private void injectCategory() throws Exception {
    //
    String categoryName = categoryBase + toType;
    Category cat = getCategoryByName(categoryName);
    if (cat == null) {
      getLog().info("category name '" + categoryName + "' is wrong. Aborting injection ...");
      return;
    }
    
    //
    String[] userNames = getUserNames();
    if (userNames == null | userNames.length <= 0) {
      getLog().info("Don't assign permission any user to '" + categoryName + "' category. Aborting injection ...");
      return;
    }

    //
    cat = faqService.getCategoryById(cat.getPath().substring(cat.getPath().lastIndexOf("/") + 1));
    cat.setModerators(userNames);
    cat.setUserPrivate(userNames);
    
    //
    faqService.saveCategory(getCategoryRoot(true).getPath(), cat, false);

    //
    getLog().info("Assign permission '" + Arrays.toString(userNames) + "' user(s) into '" + categoryName + "' category.");
  }
  
  private void injectQuestion() throws Exception {
    //
    String questionName = questionBase + toType;
    Question question = getQuestionByName(questionName);
    if (question == null) {
      getLog().info("question name is '" + questionName + "' is wrong. Aborting injection ...");
      return;
    }
    
    //
    String[] userNames = getUserNames();
    if (userNames == null | userNames.length <=0) {
      getLog().info("Don't assign permission any user to '" + questionName + "' question. Aborting injection ...");
      return;
    }

    //
    question = faqService.getQuestionById(question.getPath());
    question.setUsersVote(userNames);
    
    //
    faqService.saveQuestion(question, false, faqSetting);
    
    //
    getLog().info("Assign permission '" + Arrays.toString(userNames) + "' user(s) into '" + questionName + "' question in '" + question.getCategoryId() + "' category.");
  }
  
  private void injectAnswer() throws Exception {
    //
    String aswerName = answerBase + toType;
    Answer answer = getAnswerByName(aswerName);
    if (answer == null) {
      getLog().info("answer name is '" + aswerName + "' is wrong. Aborting injection ...");
      return;
    }
    
    //
    String[] userNames = getUserNames();
    if (userNames == null | userNames.length <=0) {
      getLog().info("Don't assign permission any user to '" + aswerName + "' answer. Aborting injection ...");
      return;
    }

    //
    String questionId = answer.getPath().substring(answer.getPath().indexOf(Utils.CATEGORY_HOME), answer.getPath().indexOf(Utils.ANSWER_HOME) - 1);
    answer = faqService.getAnswerById(questionId, answer.getId());
    answer.setUsersVoteAnswer(userNames);
    
    // Tip and trick for save answer: Set isNew to true and language if null
    if (answer.getLanguage() == null) {
      answer.setNew(true);
      answer.setLanguage("English");
    }
    faqService.saveAnswer(questionId, answer, true);
    
    //
    getLog().info("Assign permission '" + Arrays.toString(userNames) + "' user(s) into '" + aswerName + "' answer in '" + questionId + "' question.");
  }
  
  private String[] getUserNames() throws Exception {
    //
    String[] result = new String[toUser - fromUser + 1];
    int userIdx = 0;
    
    //
    for(int i = fromUser; i <= toUser; i++)  {
      String username = userBase + i;
      if (userHandler.findUserByName(username) != null) {
        result[userIdx] = username;
        userIdx++;
      }
    }
    
    //
    return result;
  }
}
