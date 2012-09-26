package org.exoplatform.ks.extras.injection.faq;

import java.util.HashMap;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQNodeTypes;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.impl.JCRDataStorage;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.ks.extras.injection.utils.ExoNameGenerator;
import org.exoplatform.ks.extras.injection.utils.LoremIpsum4J;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.UserHandler;

abstract public class AbstractFAQInjector extends DataInjector {

  /** . */
  private static Log LOG = ExoLogger.getLogger(AbstractFAQInjector.class);
  
  /** . */
  private final static String DEFAULT_USER_BASE = "bench.user";
  
  /** . */
  private final static String DEFAULT_CATEGORY_BASE = "bench.cat";
  
  /** . */
  private final static String DEFAULT_QUESTION_BASE = "bench.ques";

  /** . */
  private final static String DEFAULT_ANSWER_BASE = "bench.answer";

  /** . */
  private final static String DEFAULT_COMMENT_BASE = "bench.comment";
  
  /** . */
  private final static int DEFAULT_BYTE_SIZE_BASE = 100;
  
  /** . */
  protected final static String PASSWORD = "exo";

  /** . */
  protected final static String DOMAIN = "exoplatform.int";
  
  /** . */
  protected String userBase;
  
  /** . */
  protected String categoryBase;
  
  /** . */
  protected String questionBase;

  /** . */
  protected String answerBase;

  /** . */
  protected String commentBase;
  
  /** . */
  protected int byteSizeBase;
  
  /** . */
  protected final OrganizationService organizationService;

  /** . */
  protected final FAQService faqService;
  
  /** . */
  protected final KSDataLocation locator;

  /** . */
  protected final UserHandler userHandler;
  
  /** . */
  protected FAQSetting faqSetting;
  
  /** . */
  protected int userNumber;
  
  /** . */
  protected int categoryNumber;

  /** . */
  protected int questionNumber;

  /** . */
  protected int answerNumber;

  /** . */
  protected int commentNumber;
  
  /** . */
  protected final Random random;

  /** . */
  protected ExoNameGenerator exoNameGenerator;

  /** . */
  protected LoremIpsum4J lorem;
  
  /** . */
  private Category categoryRoot;
  
  public AbstractFAQInjector() {
    PortalContainer c = PortalContainer.getInstance();
    this.faqService = (FAQService) c.getComponentInstanceOfType(FAQService.class);
    this.organizationService = (OrganizationService) c.getComponentInstanceOfType(OrganizationService.class);
    this.locator = (KSDataLocation) c.getComponentInstanceOfType(KSDataLocation.class);

    //
    this.userHandler = organizationService.getUserHandler();
    this.exoNameGenerator = new ExoNameGenerator();
    this.random = new Random();
    this.lorem = new LoremIpsum4J();
    
    this.faqSetting = new FAQSetting();
    this.faqSetting.setDisplayMode("");
    this.faqSetting.setEmailSettingSubject("eXo Answers Notification");
    this.faqSetting.setEmailSettingContent("<p>We have a new question or answer by injector datas in category " + 
                                           "<strong>&categoryName_</strong></p><p><em>&questionContent_</em></p>");
  }
  
  public void init(String userPrefix, String categoryPrefix, String questionPrefix, String answerPrefix, String commentPrefix, int byteSize) {

    //
    userBase = (userPrefix == null ? DEFAULT_USER_BASE : userPrefix);
    categoryBase = (categoryPrefix == null ? DEFAULT_CATEGORY_BASE : categoryPrefix);
    questionBase = (questionPrefix == null ? DEFAULT_QUESTION_BASE : questionPrefix);
    answerBase = (answerPrefix == null ? DEFAULT_ANSWER_BASE : answerPrefix);
    commentBase = (commentPrefix == null ? DEFAULT_COMMENT_BASE : commentPrefix);
    byteSizeBase = (byteSize == 0 ? DEFAULT_BYTE_SIZE_BASE : byteSize);

    //
    categoryNumber = 0;
    questionNumber = 0;
    answerNumber = 0;
    commentNumber = 0;

    try {
      userNumber = userNumber(userBase);
      categoryNumber = categoryNumber(categoryBase);
      questionNumber = questionNumber(questionBase);
      answerNumber = answerNumber(answerBase);
      commentNumber = commentNumber(commentBase);
    }
    catch (Exception e) {
      // If no user is existing, set keep 0 as value.
    }

    //
    LOG.info("Initial user number : " + userNumber);
    LOG.info("Initial category number : " + categoryNumber);
    LOG.info("Initial question number : " + questionNumber);
    LOG.info("Initial answer number : " + answerNumber);
    LOG.info("Initial comment number : " + commentNumber);
  }
  
  public int userNumber(String base) throws Exception {
    Query query = new Query();
    query.setUserName(base + "*");
    
    return userHandler.findUsersByQuery(query).getSize();
  }
  
  public int categoryNumber(String base) throws Exception {
    StringBuffer sb = new StringBuffer(FAQNodeTypes.JCR_ROOT);
    sb.append("/").append(locator.getFaqCategoriesLocation()).append("//element(*,");
    sb.append(FAQNodeTypes.EXO_FAQ_CATEGORY).append(")[jcr:like(exo:name, '%").append(base).append("%')]");

    return (int)search(sb.toString()).getSize();
  }
  
  public int questionNumber(String base) throws Exception {
    StringBuffer sb = new StringBuffer(FAQNodeTypes.JCR_ROOT);
    sb.append("/").append(locator.getFaqCategoriesLocation()).append("//element(*,");
    sb.append(FAQNodeTypes.EXO_FAQ_QUESTION).append(")[jcr:like(exo:title, '%").append(base).append("%')]");

    return (int)search(sb.toString()).getSize();
  }

  public int answerNumber(String base) throws Exception {
    StringBuffer sb = new StringBuffer(FAQNodeTypes.JCR_ROOT);
    sb.append("/").append(locator.getFaqCategoriesLocation()).append("//element(*,");
    sb.append(FAQNodeTypes.EXO_ANSWER).append(")[jcr:like(exo:fullName, '%").append(base).append("%')]");

    return (int)search(sb.toString()).getSize();
  }

  public int commentNumber(String base) throws Exception {
    StringBuffer sb = new StringBuffer(FAQNodeTypes.JCR_ROOT);
    sb.append("/").append(locator.getFaqCategoriesLocation()).append("//element(*,");
    sb.append(FAQNodeTypes.EXO_COMMENT).append(")[jcr:like(exo:fullName, '%").append(base).append("%')]");

    return (int)search(sb.toString()).getSize();
  }
  
  protected Category getCategoryRoot(boolean isUpdate) {
    try {
      if (isUpdate || categoryRoot == null) {
        categoryRoot = faqService.getCategoryById(KSDataLocation.Locations.FAQ_CATEGORIES_HOME);
      }
      return categoryRoot;
    } catch (Exception e) {
      return null;
    }
  } 
  
  protected NodeIterator search(String queryString) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      QueryManager qm = getFAQHomeNode(sProvider).getSession().getWorkspace().getQueryManager();
      javax.jcr.query.Query query = qm.createQuery(queryString, javax.jcr.query.Query.XPATH);
      QueryResult result = query.execute();
      return result.getNodes();
    } catch (Exception e) {
      LOG.error("Failed to search", e);
    }
    return null;
  }
  
  protected Node getFAQHomeNode(SessionProvider sProvider) throws Exception {
    String path = locator.getForumHomeLocation();
    return locator.getSessionManager().getSession(sProvider).getRootNode().getNode(path);
  }
  
  public Category getCategoryByName(String catName) throws Exception {
    StringBuffer sb = new StringBuffer(FAQNodeTypes.JCR_ROOT);
    sb.append("/").append(locator.getFaqCategoriesLocation()).append("//element(*,");
    sb.append(FAQNodeTypes.EXO_FAQ_CATEGORY).append(")[jcr:like(exo:name, '%").append(catName).append("%')]");

    NodeIterator iterator =  search(sb.toString());
    if (iterator.hasNext()) {
      Node cateNode = (Node)iterator.next();
      PropertyReader reader = new PropertyReader(cateNode);
      
      Category cat = new Category();
      cat.setName(cateNode.getName());
      cat.setPath(cateNode.getPath());
      cat.setCreatedDate(reader.date(FAQNodeTypes.EXO_CREATED_DATE));
      cat.setDescription(reader.string(FAQNodeTypes.EXO_DESCRIPTION));
      cat.setId(reader.string(FAQNodeTypes.EXO_ID));
      cat.setIndex(reader.l(FAQNodeTypes.EXO_INDEX));
      cat.setModerators(reader.strings(FAQNodeTypes.EXO_MODERATORS));
      cat.setUserPrivate(reader.strings(FAQNodeTypes.EXO_USER_PRIVATE));
      cat.setViewAuthorInfor(reader.bool(FAQNodeTypes.EXO_VIEW_AUTHOR_INFOR));
      
      return cat;
    }
    return null;
  }

  public Question getQuestionByName(String questionName) throws Exception {
    StringBuffer sb = new StringBuffer(FAQNodeTypes.JCR_ROOT);
    sb.append("/").append(locator.getFaqCategoriesLocation()).append("//element(*,");
    sb.append(FAQNodeTypes.EXO_FAQ_QUESTION).append(")[jcr:like(exo:title, '%").append(questionName).append("%')]");

    NodeIterator iterator =  search(sb.toString());
    if (iterator.hasNext()) {
      Node quesNode = (Node)iterator.next();
      String quesNodePath = quesNode.getPath();
      
      Question question = new Question();
      PropertyReader reader = new PropertyReader(quesNode);
      question.setAttachMent(JCRDataStorage.getFileAttachments(quesNode));
      question.setAuthor(reader.string(FAQNodeTypes.EXO_AUTHOR));
      question.setCategoryId(reader.string(FAQNodeTypes.EXO_CATEGORY_ID));
      question.setCategoryPath(quesNodePath.substring(quesNodePath.indexOf(KSDataLocation.Locations.FAQ_CATEGORIES_HOME), quesNodePath.indexOf(Utils.QUESTION_HOME) -1));
      question.setEmail(reader.string(FAQNodeTypes.EXO_EMAIL));
      question.setEmailsWatch(reader.strings(FAQNodeTypes.EXO_EMAIL_WATCHING));
      question.setId(reader.string(FAQNodeTypes.EXO_ID));
      question.setLanguage(reader.string(FAQNodeTypes.EXO_LANGUAGE));
      question.setLink(reader.string(FAQNodeTypes.EXO_LINK));
      question.setMarkVote(reader.d(FAQNodeTypes.EXO_MARK_VOTE));
      question.setQuestion(questionName);
      question.setPath(quesNodePath.substring(quesNodePath.indexOf(KSDataLocation.Locations.FAQ_CATEGORIES_HOME)));
      question.setTopicIdDiscuss(reader.string(FAQNodeTypes.EXO_TOPIC_ID_DISCUSS));
      question.setUsersVote(reader.strings(FAQNodeTypes.EXO_USERS_VOTE));
      
      return question;
    }
    return null;
  }
  
  public Answer getAnswerByName(String answerName) throws Exception {
    StringBuffer sb = new StringBuffer(FAQNodeTypes.JCR_ROOT);
    sb.append("/").append(locator.getFaqCategoriesLocation()).append("//element(*,");
    sb.append(FAQNodeTypes.EXO_ANSWER).append(")[jcr:like(exo:fullName, '%").append(answerName).append("%')]");

    NodeIterator iterator =  search(sb.toString());
    if (iterator.hasNext()) {
      Node answerNode = (Node)iterator.next();
      Answer answer = new Answer();
      answer.setPath(answerNode.getPath());
      PropertyReader reader = new PropertyReader(answerNode);
      answer.setDateResponse(reader.date(FAQNodeTypes.EXO_DATE_RESPONSE));
      answer.setId(reader.string(FAQNodeTypes.EXO_ID));
      answer.setLanguage(reader.string(FAQNodeTypes.EXO_LANGUAGE));
      answer.setUsersVoteAnswer(reader.strings(FAQNodeTypes.EXO_USERS_VOTE_ANSWER));
      
      return answer;
    }
    return null;
  }
  
  public Comment getCommentByName(String commentName) throws Exception {
    StringBuffer sb = new StringBuffer(FAQNodeTypes.JCR_ROOT);
    sb.append("/").append(locator.getFaqCategoriesLocation()).append("//element(*,");
    sb.append(FAQNodeTypes.EXO_COMMENT).append(")[jcr:like(exo:fullName, '%").append(commentName).append("%')]");

    NodeIterator iterator =  search(sb.toString());
    if (iterator.hasNext()) {
      Node commentNode = (Node)iterator.next();
      Comment comment = new Comment();
      PropertyReader reader = new PropertyReader(commentNode);
      comment.setDateComment(reader.date(FAQNodeTypes.EXO_DATE_COMMENT));
      comment.setId(reader.string(FAQNodeTypes.EXO_ID));
      
      return comment;
    }
    return null;
  }
  
  protected String userName() {
    return userBase + userNumber;
  }
  
  protected String categoryName() {
    return categoryBase + categoryNumber;
  }
  
  protected String questionName() {
    return questionBase + questionNumber;
  }

  protected String answerName() {
    return answerBase + answerNumber;
  }

  protected String commentName() {
    return commentBase + commentNumber;
  }
  
  @Override
  public Object execute(HashMap<String, String> stringStringHashMap) throws Exception {
    return null;
  }

  @Override
  public void reject(HashMap<String, String> stringStringHashMap) throws Exception {
  }
  
  @Override
  public Log getLog() {
    return ExoLogger.getExoLogger(this.getClass());
  }
  
  protected int param(HashMap<String, String> params, String name) {

    //
    if (params == null) {
      throw new NullPointerException();
    }

    //
    if (name == null) {
      throw new NullPointerException();
    }

    //
    try {
      String value = params.get(name);
      if (value != null) {
        return Integer.valueOf(value);
      }
    } catch (NumberFormatException e) {
      LOG.warn("Integer number expected for property " + name);
    }
    return 0;
  }
}
