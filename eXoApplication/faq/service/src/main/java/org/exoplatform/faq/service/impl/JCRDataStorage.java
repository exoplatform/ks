/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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

package org.exoplatform.faq.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.CategoryInfo;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.DataStorage;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQNodeTypes;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.MessageBuilder;
import org.exoplatform.faq.service.ObjectSearchResult;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionInfo;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.QuestionNodeListener;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.service.SubCategoryInfo;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.EmailNotifyPlugin;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.conf.RoleRulesPlugin;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.ks.common.jcr.SessionManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.security.ConversationState;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang hung.nguyen@exoplatform.com Jul 10, 2007
 */
public class JCRDataStorage implements DataStorage, FAQNodeTypes {

  private static final Log        log                  = ExoLogger.getLogger(JCRDataStorage.class);

  protected Map<String, String>   serverConfig_        = new HashMap<String, String>();

  private Map<String, NotifyInfo> messagesInfoMap_     = new HashMap<String, NotifyInfo>();

  private Map<String, EventListener> listeners         = new HashMap<String, EventListener>();

  final Queue<NotifyInfo>         pendingMessagesQueue = new ConcurrentLinkedQueue<NotifyInfo>();

  private final String            ADMIN_               = "ADMIN".intern();

  private List<RoleRulesPlugin>   rulesPlugins_        = new ArrayList<RoleRulesPlugin>();

  private SessionManager          sessionManager;

  private KSDataLocation          dataLocator;

  public JCRDataStorage(KSDataLocation dataLocator) throws Exception {
    this.dataLocator = dataLocator;
    sessionManager = dataLocator.getSessionManager();
  }

  @Override
  public void addPlugin(ComponentPlugin plugin) throws Exception {
    try {
      serverConfig_ = ((EmailNotifyPlugin) plugin).getServerConfiguration();
    } catch (Exception e) {
      log.error("\nFailed to add plugin\n ", e);
    }
  }

  @Override
  public void addRolePlugin(ComponentPlugin plugin) throws Exception {
    try {
      if (plugin instanceof RoleRulesPlugin) {
        rulesPlugins_.add((RoleRulesPlugin) plugin);
      }
    } catch (Exception e) {
      log.error("Failed to add role plugin\n", e);
    }
  }

  @Override
  public boolean isAdminRole(String userName) throws Exception {
    String name = userName;
    if (name == null) {
      name = ConversationState.getCurrent().getIdentity().getUserId();
    }
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node cateHomeNode = getCategoryHome(sProvider, null);
      for (int i = 0; i < rulesPlugins_.size(); ++i) {
        List<String> list = new ArrayList<String>();
        list.addAll(rulesPlugins_.get(i).getRules(this.ADMIN_));
        list.addAll(new PropertyReader(cateHomeNode).list(EXO_MODERATORS, new ArrayList<String>()));
        if (list.contains(name))
          return true;
        if (Utils.hasPermission(list, UserHelper.getAllGroupAndMembershipOfUser(userName)))
          return true;
      }
    } catch (Exception e) {
      log.debug("Check user whether is admin: ", e);
    }
    return false;
  }

  @Override
  public List<String> getAllFAQAdmin() throws Exception {
    List<String> list = new ArrayList<String>();
    try {
      for (int i = 0; i < rulesPlugins_.size(); ++i) {
        list.addAll(rulesPlugins_.get(i).getRules(this.ADMIN_));
      }
      list = FAQServiceUtils.getUserPermission(list.toArray(new String[] {}));
    } catch (Exception e) {
      log.error("Failed to get all FAQ admin: ", e);
    }
    return list;
  }

  @Override
  public void getUserSetting(String userName, FAQSetting faqSetting) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node userSettingNode = getUserSettingHome(sProvider).getNode(userName);
      PropertyReader reader = new PropertyReader(userSettingNode);
      faqSetting.setOrderBy(reader.string(EXO_ORDE_BY, EMPTY_STR));
      faqSetting.setOrderType(reader.string(EXO_ORDE_TYPE, EMPTY_STR));
      faqSetting.setSortQuestionByVote(reader.bool(EXO_SORT_QUESTION_BY_VOTE));
    } catch (Exception e) {
      saveFAQSetting(faqSetting, userName);
    }
  }

  @Override
  public void saveFAQSetting(FAQSetting faqSetting, String userName) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node userSettingNode = getUserSettingHome(sProvider).getNode(userName);
      userSettingNode.setProperty(EXO_ORDE_BY, faqSetting.getOrderBy());
      userSettingNode.setProperty(EXO_ORDE_TYPE, faqSetting.getOrderType());
      userSettingNode.setProperty(EXO_SORT_QUESTION_BY_VOTE, faqSetting.isSortQuestionByVote());
      userSettingNode.save();
    } catch (PathNotFoundException e) {
      Node userSettingNode = getUserSettingHome(sProvider).addNode(userName, EXO_FAQ_USER_SETTING);
      userSettingNode.setProperty(EXO_ORDE_BY, faqSetting.getOrderBy());
      userSettingNode.setProperty(EXO_ORDE_TYPE, faqSetting.getOrderType());
      userSettingNode.setProperty(EXO_SORT_QUESTION_BY_VOTE, faqSetting.isSortQuestionByVote());
      userSettingNode.getSession().save();
    }
  }

  @Override
  public FileAttachment getUserAvatar(String userName) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node node = getKSUserAvatarHomeNode(sProvider).getNode(userName);
      return getFileAttachment(node);
    } catch (Exception e) {
      return null;
    }
  }
  
  @Override
  public void saveUserAvatar(String userId, FileAttachment fileAttachment) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node ksAvatarHomeNode = getKSUserAvatarHomeNode(sProvider);
      Node avatarNode;
      if (ksAvatarHomeNode.hasNode(userId))
        avatarNode = ksAvatarHomeNode.getNode(userId);
      else
        avatarNode = ksAvatarHomeNode.addNode(userId, NT_FILE);
      FAQServiceUtils.reparePermissions(avatarNode, "any");
      Node nodeContent;
      if (avatarNode.hasNode(JCR_CONTENT))
        nodeContent = avatarNode.getNode(JCR_CONTENT);
      else
        nodeContent = avatarNode.addNode(JCR_CONTENT, NT_RESOURCE);
      nodeContent.setProperty(JCR_MIME_TYPE, fileAttachment.getMimeType());
      nodeContent.setProperty(JCR_DATA, fileAttachment.getInputStream());
      nodeContent.setProperty(JCR_LAST_MODIFIED, CommonUtils.getGreenwichMeanTime().getTimeInMillis());
      if (avatarNode.isNew())
        ksAvatarHomeNode.getSession().save();
      else
        ksAvatarHomeNode.save();
    } catch (Exception e) {
      log.error("Failed to save user avatar: ", e);
    }
  }

  @Override
  public void setDefaultAvatar(String userName) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node avatarHome = getKSUserAvatarHomeNode(sProvider);
      if (avatarHome.hasNode(userName)) {
        Node node = avatarHome.getNode(userName);
        if (node.isNodeType(NT_FILE)) {
          node.remove();
          avatarHome.save();
        }
      }
    } catch (Exception e) {
      log.error("Failed to set default avatar: ", e);
    }
  }

  public NodeIterator getQuestionsIterator(SessionProvider sProvider) throws Exception {
    try {
      Node faqHome = getFAQServiceHome(sProvider);
      return getQuestionsIterator(faqHome, EMPTY_STR, true);
    } catch (Exception e) {
      log.error("Failed to get question iterator: ", e);
      return null;
    }
  }

  private NodeIterator getQuestionsIterator(Node parentNode, String strQuery, boolean isAll) throws Exception {
    StringBuffer queryString = new StringBuffer(JCR_ROOT).append(parentNode.getPath()).append((isAll) ? "//" : "/").append("element(*,exo:faqQuestion)").append(strQuery);
    QueryManager qm = parentNode.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  public void reInitQuestionNodeListeners() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      NodeIterator iter = getQuestionsIterator(sProvider);
      if (iter == null)
        return;
      while (iter.hasNext()) {
        Node quesNode = iter.nextNode();
        registerQuestionNodeListener(quesNode);
      }
    } catch (Exception e) {
      log.error("Failed to get question iterator: ", e);
    }
  }

  @Override
  public boolean initRootCategory() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqServiceHome = getFAQServiceHome(sProvider);
      if (faqServiceHome.hasNode(Utils.CATEGORY_HOME)) {
        log.debug("root category is already created");
        return false;
      }
      Node categoryHome = faqServiceHome.addNode(Utils.CATEGORY_HOME, EXO_FAQ_CATEGORY);
      categoryHome.addMixin(MIX_FAQ_SUB_CATEGORY);
      categoryHome.setProperty(EXO_NAME, "Answers");
      categoryHome.setProperty(EXO_IS_VIEW, true);
      faqServiceHome.save();
      log.info("Initialized root category : " + categoryHome.getPath());
      return true;
    } catch (Exception e) {
      log.error("Could not initialize root category", e);
      return false;
    }
  }

  @Override
  public byte[] getTemplate() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node templateHome = getTemplateHome(sProvider);
      Node fileNode = templateHome.getNode(Utils.UI_FAQ_VIEWER);
      if (fileNode.isNodeType(NT_FILE)) {
        Node contentNode = fileNode.getNode(JCR_CONTENT);
        InputStream inputStream = contentNode.getProperty(JCR_DATA).getStream();
        byte[] data = new byte[inputStream.available()];
        inputStream.read(data);
        inputStream.close();
        return data;
      }
    } catch (Exception e) {
      log.error("Failed to get template", e);
    }
    return null;
  }

  @Override
  public void saveTemplate(String str) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node templateHome = getTemplateHome(sProvider);
      Node fileNode;
      try {
        fileNode = templateHome.getNode(Utils.UI_FAQ_VIEWER);
      } catch (Exception e) {
        fileNode = templateHome.addNode(Utils.UI_FAQ_VIEWER, NT_FILE);
      }
      Node nodeContent = null;
      InputStream inputStream = null;
      byte[] byte_ = str.getBytes();
      inputStream = new ByteArrayInputStream(byte_);
      try {
        nodeContent = fileNode.addNode(JCR_CONTENT, NT_RESOURCE);
      } catch (Exception e) {
        nodeContent = fileNode.getNode(JCR_CONTENT);
      }
      nodeContent.setProperty(JCR_MIME_TYPE, "application/x-groovy+html");
      nodeContent.setProperty(JCR_DATA, inputStream);
      nodeContent.setProperty(JCR_LAST_MODIFIED, CommonUtils.getGreenwichMeanTime().getTimeInMillis());
      if (templateHome.isNew()) {
        templateHome.getSession().save();
      } else {
        templateHome.save();
      }
    } catch (Exception e) {
      log.error("Failed to save template: ", e);
    }
  }

  protected Value[] booleanToValues(Node node, Boolean[] bools) throws Exception {
    if (bools == null)
      return new Value[] { node.getSession().getValueFactory().createValue(true) };
    Value[] values = new Value[bools.length];
    for (int i = 0; i < values.length; i++) {
      values[i] = node.getSession().getValueFactory().createValue(bools[i]);
    }
    return values;
  }

  private boolean questionHasAnswer(Node questionNode) throws Exception {
    if (questionNode.hasNode(Utils.ANSWER_HOME) && questionNode.getNode(Utils.ANSWER_HOME).hasNodes())
      return true;
    return false;
  }

  private void sendNotifyWatcher(SessionProvider sProvider, Question question, FAQSetting faqSetting, boolean isNew) {
    // Send notification when add new question in watching category
    List<String> emails = new ArrayList<String>();
    List<String> users = new ArrayList<String>();
    List<String> emailsList = new ArrayList<String>();
    emailsList.add(question.getEmail());
    try {
      Node cate = getCategoryNode(sProvider, question.getCategoryPath());
      PropertyReader reader = new PropertyReader(cate);
      // watch in category parent
      emails.addAll(reader.list(EXO_EMAIL_WATCHING, new ArrayList<String>()));
      users.addAll(reader.list(EXO_USER_WATCHING, new ArrayList<String>()));
      // watch in this question
      if (!CommonUtils.isEmpty(question.getEmailsWatch())) {
        emails.addAll(Arrays.asList(question.getEmailsWatch()));
        users.addAll(Arrays.asList(question.getUsersWatch()));
      }
      if (!question.isActivated() || (!question.isApproved())) {
        // only send notification to administrations or moderators
        List<String> moderators = reader.list(EXO_MODERATORS, new ArrayList<String>());
        List<String> temps = new ArrayList<String>();
        int i = 0;
        for (String user : users) {
          if (!temps.contains(user)) {
            temps.add(user);
            if (isAdminRole(user) || Utils.hasPermission(moderators, UserHelper.getAllGroupAndMembershipOfUser(user))) {
              emailsList.add(emails.get(i));
            }
          }
          ++i;
        }
      } else {
        emailsList.addAll(emails);
      }
      if (!emailsList.isEmpty()) {
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setType(MessageBuilder.TYPESEND.NEW_QUESTION);
        messageBuilder.setCategoryName(reader.string(EXO_NAME, "Root"));
        messageBuilder.setContent(faqSetting.getEmailSettingContent());
        messageBuilder.setSubject(faqSetting.getEmailSettingSubject());
        messageBuilder.setQuestionOwner(question.getAuthor());
        messageBuilder.setQuestionEmail(question.getEmail());
        messageBuilder.setQuestionLink(question.getLink());
        messageBuilder.setQuestionDetail(question.getDetail());
        messageBuilder.setQuestionContent(question.getQuestion());
        
        if (question.getAnswers() != null && question.getAnswers().length > 0) {
          messageBuilder.setQuestionResponse(question.getAnswers()[0].getResponses());
        } else {
          messageBuilder.setQuestionResponse(EMPTY_STR);
        }
        sendEmailNotification(emailsList, messageBuilder.getMessage());
      }
    } catch (Exception e) {
      log.error("Failed to send a nofify for category watcher: ", e);
    }
  }

  @Override
  public List<QuestionLanguage> getQuestionLanguages(String questionId){
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>();
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
      try {
        listQuestionLanguage.add(getQuestionLanguage(questionNode));
      } catch (Exception e) {
        log.debug("Adding a question node failed: ", e);
      }
      if (questionNode.hasNode(Utils.LANGUAGE_HOME)) {
        Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME);
        NodeIterator nodeIterator = languageNode.getNodes();
        while (nodeIterator.hasNext()) {
          try {
            listQuestionLanguage.add(getQuestionLanguage(nodeIterator.nextNode()));
          } catch (RepositoryException e) {
            log.debug(String.format("Failed to get languages for question %s", questionId), e);
          }
        }
      }
    } catch (Exception e) {
      log.error("Failed to get question language: ", e);
    }
    return listQuestionLanguage;
  }

  private QuestionLanguage getQuestionLanguage(Node questionNode) throws RepositoryException{
    QuestionLanguage questionLanguage = new QuestionLanguage();
    questionLanguage.setState(QuestionLanguage.VIEW);
    questionLanguage.setId(questionNode.getName());
    PropertyReader reader = new PropertyReader(questionNode);
    questionLanguage.setLanguage(reader.string(EXO_LANGUAGE, EMPTY_STR));
    questionLanguage.setQuestion(reader.string(EXO_TITLE, EMPTY_STR));
    questionLanguage.setDetail(reader.string(EXO_NAME, EMPTY_STR));
    Comment[] comments = getComment(questionNode);
    Answer[] answers = getAnswers(questionNode);
    questionLanguage.setComments(comments);
    questionLanguage.setAnswers(answers);
    return questionLanguage;
  }

  @Override
  public void deleteAnswer(String questionId, String answerId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
      Node answerNode = questionNode.getNode(Utils.ANSWER_HOME).getNode(answerId);
      answerNode.remove();
      questionNode.save();
    } catch (Exception e) {
      log.error("Failed to delete a answer: ", e);
    }
  }

  @Override
  public void deleteComment(String questionId, String commentId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
      Node commnetNode = questionNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
      commnetNode.remove();
      questionNode.save();
    } catch (Exception e) {
      log.error("Failed to delete a commnent: ", e);
    }
  }

  private Answer[] getAnswers(Node questionNode) {
    try {
      if (!questionNode.hasNode(Utils.ANSWER_HOME))
        return new Answer[] {};
      NodeIterator nodeIterator = questionNode.getNode(Utils.ANSWER_HOME).getNodes();
      List<Answer> answers = new ArrayList<Answer>();
      Answer ans;
      String language = questionNode.getProperty(EXO_LANGUAGE).getString();
      while (nodeIterator.hasNext()) {
        try {
          Node node = nodeIterator.nextNode();
          ans = getAnswerByNode(node);
          ans.setLanguage(language);
          answers.add(ans);
        } catch (Exception e) {
          log.error("Failed to get anwser", e);
        }
      }
      return answers.toArray(new Answer[] {});
    } catch (Exception e) {
      log.error("Failed to get answer: ", e);
    }
    return new Answer[] {};
  }

  private Answer getAnswerByNode(Node answerNode) throws Exception {
    Answer answer = new Answer();
    answer.setId(answerNode.getName());
    PropertyReader reader = new PropertyReader(answerNode);
    answer.setResponses(reader.string(EXO_RESPONSES, EMPTY_STR));
    answer.setResponseBy(reader.string(EXO_RESPONSE_BY, EMPTY_STR));
    answer.setFullName(reader.string(EXO_FULL_NAME, EMPTY_STR));
    answer.setDateResponse((answerNode.getProperty(EXO_DATE_RESPONSE).getDate().getTime()));
    answer.setUsersVoteAnswer(reader.strings(EXO_USERS_VOTE_ANSWER, new String[] {}));
    answer.setMarkVotes(reader.l(EXO_MARK_VOTES));
    answer.setApprovedAnswers(reader.bool(EXO_APPROVE_RESPONSES, true));
    answer.setActivateAnswers(reader.bool(EXO_ACTIVATE_RESPONSES, true));
    answer.setPostId(reader.string(EXO_POST_ID, EMPTY_STR));
    String path = answerNode.getPath();
    answer.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
    return answer;
  }

  @Override
  public JCRPageList getPageListAnswer(String questionId, boolean isSortByVote) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
      if (questionNode.hasNode(Utils.ANSWER_HOME)) {
        Node answerHome = questionNode.getNode(Utils.ANSWER_HOME);
        QueryManager qm = answerHome.getSession().getWorkspace().getQueryManager();
        StringBuffer queryString = new StringBuffer(JCR_ROOT).append(answerHome.getPath()).append("//element(*,exo:answer)");
        if ((Boolean) isSortByVote == null)
          queryString.append("order by @exo:dateResponse ascending");
        else if (isSortByVote)
          queryString.append("order by @exo:MarkVotes ascending");
        else
          queryString.append("order by @exo:MarkVotes descending");
        Query query = qm.createQuery(queryString.toString(), Query.XPATH);
        QueryResult result = query.execute();
        QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true);
        return pageList;
      }
    } catch (Exception e) {
      log.error("Failed to get page list answers", e);
    }
    return null;
  }

  @Override
  public void saveAnswer(String questionId, Answer answer, boolean isNew) throws Exception {
    Answer[] answers = { answer };
    saveAnswer(questionId, answers);
  }

  @Override
  public void saveAnswer(String questionId, Answer[] answers) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node quesNode = getFAQServiceHome(sProvider).getNode(questionId);
      if (!quesNode.isNodeType(MIX_FAQI_1_8N)) {
        quesNode.addMixin(MIX_FAQI_1_8N);
      }
      Node answerHome;
      String qId = quesNode.getName();
      String categoryId = quesNode.getProperty(EXO_CATEGORY_ID).getString();
      String defaultLang = quesNode.getProperty(EXO_LANGUAGE).getString();

      for (Answer answer : answers) {

        if (answer.getLanguage().equals(defaultLang)) {
          try {
            answerHome = quesNode.getNode(Utils.ANSWER_HOME);
          } catch (Exception e) {
            answerHome = quesNode.addNode(Utils.ANSWER_HOME, EXO_ANSWER_HOME);
          }
        } else { // answer for other languages
          Node langNode = getLanguageNodeByLanguage(quesNode, answer.getLanguage());
          try {
            answerHome = langNode.getNode(Utils.ANSWER_HOME);
          } catch (Exception e) {
            answerHome = langNode.addNode(Utils.ANSWER_HOME, EXO_ANSWER_HOME);
          }
        }
        saveAnswer(answer, answerHome, qId, categoryId);
      }
      quesNode.save();
    } catch (Exception e) {
      log.error("Failed to save answer: ", e);
    }
  }

  private void saveAnswer(Answer answer, Node answerHome, String questionId, String categoryId) throws Exception {
    Node answerNode;
    try {
      answerNode = answerHome.getNode(answer.getId());
    } catch (PathNotFoundException e) {
      answerNode = answerHome.addNode(answer.getId(), EXO_ANSWER);
    }
    if (!answer.isNew()) { // remove answer
      answerNode.remove();
      return;
    }
    try {
      if (answerNode.isNew()) {
        answerNode.setProperty(EXO_DATE_RESPONSE, CommonUtils.getGreenwichMeanTime());
        answerNode.setProperty(EXO_ID, answer.getId());
        answerNode.setProperty(EXO_APPROVE_RESPONSES, answer.getApprovedAnswers());
        answerNode.setProperty(EXO_ACTIVATE_RESPONSES, answer.getActivateAnswers());
      } else {
        if (new PropertyReader(answerNode).bool(EXO_APPROVE_RESPONSES, false) != answer.getApprovedAnswers())
          answerNode.setProperty(EXO_APPROVE_RESPONSES, answer.getApprovedAnswers());
        if (new PropertyReader(answerNode).bool(EXO_ACTIVATE_RESPONSES, false) != answer.getActivateAnswers())
          answerNode.setProperty(EXO_ACTIVATE_RESPONSES, answer.getActivateAnswers());
      }
      if (answer.getPostId() != null && answer.getPostId().length() > 0) {
        answerNode.setProperty(EXO_POST_ID, answer.getPostId());
      }
      answerNode.setProperty(EXO_RESPONSES, answer.getResponses());
      answerNode.setProperty(EXO_RESPONSE_BY, answer.getResponseBy());
      answerNode.setProperty(EXO_FULL_NAME, answer.getFullName());
      answerNode.setProperty(EXO_USERS_VOTE_ANSWER, answer.getUsersVoteAnswer());
      answerNode.setProperty(EXO_MARK_VOTES, answer.getMarkVotes());
      answerNode.setProperty(EXO_RESPONSE_LANGUAGE, answer.getLanguage());
      answerNode.setProperty(EXO_QUESTION_ID, questionId);
      answerNode.setProperty(EXO_CATEGORY_ID, categoryId);
    } catch (Exception e) {
      log.error("Failed to save Answer: ", e);
    }
  }

  @Override
  public void saveComment(String questionId, Comment comment, boolean isNew) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node quesNode = getFAQServiceHome(sProvider).getNode(questionId);
      if (!quesNode.isNodeType(MIX_FAQI_1_8N)) {
        quesNode.addMixin(MIX_FAQI_1_8N);
      }
      Node commentHome = null;
      try {
        commentHome = quesNode.getNode(Utils.COMMENT_HOME);
      } catch (PathNotFoundException e) {
        commentHome = quesNode.addNode(Utils.COMMENT_HOME, EXO_COMMENT_HOME);
      }
      Node commentNode;
      if (isNew) {
        commentNode = commentHome.addNode(comment.getId(), EXO_COMMENT);
        commentNode.setProperty(EXO_DATE_COMMENT, CommonUtils.getGreenwichMeanTime());
        commentNode.setProperty(EXO_ID, comment.getId());
      } else {
        commentNode = commentHome.getNode(comment.getId());
      }

      if (comment.getPostId() != null && comment.getPostId().length() > 0) {
        commentNode.setProperty(EXO_POST_ID, comment.getPostId());
      }
      commentNode.setProperty(EXO_COMMENTS, comment.getComments());
      commentNode.setProperty(EXO_COMMENT_BY, comment.getCommentBy());
      commentNode.setProperty(EXO_FULL_NAME, comment.getFullName());
      commentNode.setProperty(EXO_CATEGORY_ID, quesNode.getProperty(EXO_CATEGORY_ID).getString());
      commentNode.setProperty(EXO_QUESTION_ID, quesNode.getName());
      commentNode.setProperty(EXO_COMMENT_LANGUAGE, quesNode.getProperty(EXO_LANGUAGE).getString());
      if (commentNode.isNew())
        quesNode.getSession().save();
      else
        quesNode.save();
    } catch (Exception e) {
      log.error("Failed to save comment: ", e);
    }
  }

  @Override
  public void saveAnswerQuestionLang(String questionId, Answer answer, String language, boolean isNew) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node quesNode = getFAQServiceHome(sProvider).getNode(questionId);
      Node answerHome = null;
      try {
        answerHome = quesNode.getNode(Utils.ANSWER_HOME);
      } catch (PathNotFoundException e) {
        answerHome = quesNode.addNode(Utils.ANSWER_HOME, EXO_ANSWER_HOME);
      }
      Node answerNode;
      if (isNew) {
        answerNode = answerHome.addNode(answer.getId(), EXO_ANSWER);
        answerNode.setProperty("exo:dateResponses", CommonUtils.getGreenwichMeanTime());
        answerNode.setProperty(EXO_APPROVE_RESPONSES, answer.getApprovedAnswers());
        answerNode.setProperty(EXO_ACTIVATE_RESPONSES, answer.getActivateAnswers());
      } else {
        answerNode = answerHome.getNode(answer.getId());
        if (new PropertyReader(answerNode).bool(EXO_APPROVE_RESPONSES, false) != answer.getApprovedAnswers())
          answerNode.setProperty(EXO_APPROVE_RESPONSES, answer.getApprovedAnswers());
        if (new PropertyReader(answerNode).bool(EXO_ACTIVATE_RESPONSES, false) != answer.getActivateAnswers())
          answerNode.setProperty(EXO_ACTIVATE_RESPONSES, answer.getActivateAnswers());
      }
      answerNode.setProperty(EXO_RESPONSES, answer.getResponses());
      answerNode.setProperty(EXO_RESPONSE_BY, answer.getResponseBy());
      answerNode.setProperty(EXO_FULL_NAME, answer.getFullName());
      answerNode.setProperty(EXO_USERS_VOTE_ANSWER, answer.getUsersVoteAnswer());
    } catch (Exception e) {
      log.error("Failed to save answer question language: ", e);
    }
  }

  @Override
  public Answer getAnswerById(String questionId, String answerid) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node answerNode = getFAQServiceHome(sProvider).getNode(questionId).getNode(Utils.ANSWER_HOME).getNode(answerid);
      return getAnswerByNode(answerNode);
    } catch (Exception e) {
      log.error("Failed to get answer by id.", e);
    }
    return null;
  }

  private Comment[] getComment(Node questionNode) {
    try {
      if (questionNode == null || !questionNode.hasNode(Utils.COMMENT_HOME))
        return new Comment[] {};
      NodeIterator nodeIterator = questionNode.getNode(Utils.COMMENT_HOME).getNodes();
      Comment[] comments = new Comment[(int) nodeIterator.getSize()];
      Node commentNode = null;
      int i = 0;
      while (nodeIterator.hasNext()) {
        commentNode = nodeIterator.nextNode();
        comments[i] = getCommentByNode(commentNode);
        i++;
      }
      return comments;
    } catch (Exception e) {
      log.error("Failed to get comment: ", e);
      return new Comment[] {};
    }
  }

  @Override
  public JCRPageList getPageListComment(String questionId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node commentHome = getFAQServiceHome(sProvider).getNode(questionId + "/" + Utils.COMMENT_HOME);
      QueryManager qm = commentHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(commentHome.getPath()).append("//element(*,exo:comment)").append("order by @exo:dateComment ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true);
      return pageList;
    } catch (Exception e) {
      log.debug("Failed to get page list comments", e);
      return null;
    }
  }

  private Comment getCommentByNode(Node commentNode) throws Exception {
    Comment comment = new Comment();
    comment.setId(commentNode.getName());
    PropertyReader reader = new PropertyReader(commentNode);
    comment.setComments((reader.string(EXO_COMMENTS, EMPTY_STR)));
    comment.setCommentBy((reader.string(EXO_COMMENT_BY, EMPTY_STR)));
    comment.setDateComment((commentNode.getProperty(EXO_DATE_COMMENT).getDate().getTime()));
    comment.setFullName((reader.string(EXO_FULL_NAME, EMPTY_STR)));
    comment.setPostId(reader.string(EXO_POST_ID, EMPTY_STR));
    return comment;
  }

  @Override
  public Comment getCommentById(String questionId, String commentId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node commentNode = getFAQServiceHome(sProvider).getNode(questionId + "/" + Utils.COMMENT_HOME + "/" + commentId);
      return getCommentByNode(commentNode);
    } catch (Exception e) {
      log.error("Failed to get comment by id: " + commentId, e);
      return null;
    }
  }

  private Node getLanguageNodeByLanguage(Node questionNode, String languge) throws Exception {
    NodeIterator nodeIterator = questionNode.getNode(Utils.LANGUAGE_HOME).getNodes();
    Node languageNode = null;
    while (nodeIterator.hasNext()) {
      languageNode = nodeIterator.nextNode();
      if (languageNode.getProperty(EXO_LANGUAGE).getString().equals(languge))
        return languageNode;
    }
    return null;
  }

  private void saveQuestion(Node questionNode, Question question, boolean isNew, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
    questionNode.setProperty(EXO_ID, questionNode.getName());
    questionNode.setProperty(EXO_NAME, question.getDetail());
    questionNode.setProperty(EXO_AUTHOR, question.getAuthor());
    questionNode.setProperty(EXO_EMAIL, question.getEmail());
    questionNode.setProperty(EXO_TITLE, question.getQuestion());
    Calendar calendar = CommonUtils.getGreenwichMeanTime();
    questionNode.setProperty(EXO_LAST_ACTIVITY, getLastActivityInfo(question.getAuthor(), calendar.getTimeInMillis()));
    if (isNew) {
      questionNode.setProperty(EXO_CREATED_DATE, calendar);
      questionNode.setProperty(EXO_LANGUAGE, question.getLanguage());
    }
    String cateId = questionNode.getParent().getParent().getName();
    questionNode.setProperty(EXO_CATEGORY_ID, cateId);
    questionNode.setProperty(EXO_IS_ACTIVATED, question.isActivated());
    questionNode.setProperty(EXO_IS_APPROVED, question.isApproved());
    questionNode.setProperty(EXO_USERS_VOTE, question.getUsersVote());
    questionNode.setProperty(EXO_MARK_VOTE, question.getMarkVote());
    questionNode.setProperty(EXO_LINK, question.getLink());
    List<FileAttachment> listFileAtt = question.getAttachMent();

    List<String> listNodeNames = new ArrayList<String>();
    if (!listFileAtt.isEmpty()) {
      for (FileAttachment att : listFileAtt) {
        listNodeNames.add(att.getNodeName());
        try {
          Node nodeFile = null;
          if (questionNode.hasNode(att.getNodeName()))
            nodeFile = questionNode.getNode(att.getNodeName());
          else
            nodeFile = questionNode.addNode(att.getNodeName(), EXO_FAQ_ATTACHMENT);
          // fix permission to download file in ie 6:
          FAQServiceUtils.reparePermissions(nodeFile, "any");
          Node nodeContent = null;
          if (nodeFile.hasNode(JCR_CONTENT))
            nodeContent = nodeFile.getNode(JCR_CONTENT);
          else
            nodeContent = nodeFile.addNode(JCR_CONTENT, EXO_FAQ_RESOURCE);
          nodeContent.setProperty(EXO_FILE_NAME, att.getName());
          nodeContent.setProperty(EXO_CATEGORY_ID, cateId);
          nodeContent.setProperty(JCR_MIME_TYPE, att.getMimeType());
          nodeContent.setProperty(JCR_DATA, att.getInputStream());
          nodeContent.setProperty(JCR_LAST_MODIFIED, CommonUtils.getGreenwichMeanTime().getTimeInMillis());
        } catch (Exception e) {
          log.error("Failed to save question: ", e);
        }
      }
    }
    // remove attachments
    NodeIterator nodeIterator = questionNode.getNodes();
    Node node = null;
    while (nodeIterator.hasNext()) {
      node = nodeIterator.nextNode();
      if (node.isNodeType(EXO_FAQ_ATTACHMENT) && !listNodeNames.contains(node.getName()))
        node.remove();
    }

    question.setId(questionNode.getName());
    sendNotifyWatcher(sProvider, question, faqSetting, isNew);
  }

  @Override
  public Node saveQuestion(Question question, boolean isAddNew, FAQSetting faqSetting) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode;
      Node questionHome;
      Node category;
      if (isAddNew) {
        category = getFAQServiceHome(sProvider).getNode(question.getCategoryPath());
        try {
          questionHome = category.getNode(Utils.QUESTION_HOME);
        } catch (PathNotFoundException ex) {
          questionHome = category.addNode(Utils.QUESTION_HOME, EXO_FAQ_QUESTION_HOME);
        }
        questionNode = questionHome.addNode(question.getId(), EXO_FAQ_QUESTION);
      } else {
        questionNode = getFAQServiceHome(sProvider).getNode(question.getPath());
      }
      saveQuestion(questionNode, question, isAddNew, sProvider, faqSetting);
      if (questionNode.isNew()) {
        questionNode.getSession().save();
        registerQuestionNodeListener(questionNode);
      } else
        questionNode.save();

      return questionNode;
    } catch (Exception e) {
      log.error("Failed to save question ", e);
    }
    return null;
  }

  @Override
  public void removeQuestion(String questionId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
      Node questionHome = questionNode.getParent();
      questionNode.remove();
      questionHome.save();
    } catch (Exception e) {
      log.error("Fail ro remove question: ", e);
    }
  }

  @Override
  public Comment getCommentById(Node questionNode, String commentId) throws Exception {
    try {
      Comment comment = new Comment();
      Node commentNode = questionNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
      comment.setId(commentNode.getName());
      PropertyReader reader = new PropertyReader(commentNode);
      comment.setComments((reader.string(EXO_COMMENTS, EMPTY_STR)));
      comment.setCommentBy((reader.string(EXO_COMMENT_BY, EMPTY_STR)));
      comment.setDateComment(reader.date(EXO_DATE_COMMENT));
      comment.setPostId(reader.string(EXO_POST_ID, EMPTY_STR));
      return comment;
    } catch (Exception e) {
      log.error("Failed to get comment through id: ", e);
      return null;
    }
  }

  private Question getQuestion(Node questionNode) throws Exception {
    Question question = new Question();
    PropertyReader reader = new PropertyReader(questionNode);
    question.setId(questionNode.getName());
    question.setLanguage(reader.string(EXO_LANGUAGE, EMPTY_STR));
    question.setDetail(reader.string(EXO_NAME, EMPTY_STR));
    question.setAuthor(reader.string(EXO_AUTHOR, EMPTY_STR));
    question.setEmail(reader.string(EXO_EMAIL, EMPTY_STR));
    question.setQuestion(reader.string(EXO_TITLE, EMPTY_STR));
    question.setCreatedDate(reader.date(EXO_CREATED_DATE));
    question.setActivated(reader.bool(EXO_IS_ACTIVATED, true));
    question.setApproved(reader.bool(EXO_IS_APPROVED, true));
    question.setRelations(reader.strings(EXO_RELATIVES, new String[] {}));
    question.setNameAttachs(reader.strings(EXO_NAME_ATTACHS, new String[] {}));
    question.setUsersVote(reader.strings(EXO_USERS_VOTE, new String[] {}));
    question.setMarkVote(reader.d(EXO_MARK_VOTE));
    question.setEmailsWatch(reader.strings(EXO_EMAIL_WATCHING, new String[] {}));
    question.setUsersWatch(reader.strings(EXO_USER_WATCHING, new String[] {}));
    question.setTopicIdDiscuss(reader.string(EXO_TOPIC_ID_DISCUSS, EMPTY_STR));
    question.setLink(reader.string(EXO_LINK, EMPTY_STR));
    question.setLastActivity(reader.string(EXO_LAST_ACTIVITY, EMPTY_STR));
    question.setNumberOfPublicAnswers(reader.l(EXO_NUMBER_OF_PUBLIC_ANSWERS, 0));

    String path = questionNode.getPath();
    question.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
    question.setCategoryId(reader.string(EXO_CATEGORY_ID, EMPTY_STR));
    Node node = questionNode.getParent();
    while (!node.isNodeType(EXO_FAQ_CATEGORY)) {
      node = node.getParent();
      question.setCategoryId(node.getName());
      question.setCategoryPath(node.getPath());
    }
    question.setAttachMent(getFileAttachments(questionNode));
    question.setAnswers(getAnswers(questionNode));
    question.setComments(getComment(questionNode));
    return question;
  }
  
  
  private static FileAttachment getFileAttachment(Node node) throws Exception {
    FileAttachment attachment = null;
    try {
      if (node.isNodeType(EXO_FAQ_ATTACHMENT) || node.isNodeType(NT_FILE)) {
        PropertyReader readerContent;
        String workspace = node.getSession().getWorkspace().getName();
        attachment = new FileAttachment();
        readerContent = new PropertyReader(node.getNode(JCR_CONTENT));
        attachment.setId(node.getPath());
        attachment.setMimeType(readerContent.string(JCR_MIME_TYPE));
        attachment.setNodeName(node.getName());
        attachment.setWorkspace(workspace);
        attachment.setPath(CommonUtils.SLASH + workspace + node.getPath());
        attachment.setSize(readerContent.stream(JCR_DATA).available());
        String fileName = readerContent.string(EXO_FILE_NAME);
        if (CommonUtils.isEmpty(fileName)) {
          String type = attachment.getMimeType(); 
          if (type.indexOf(CommonUtils.SLASH) > 0) {
            type = type.substring(type.indexOf(CommonUtils.SLASH) + 1);
          }
          fileName = "avatar." + type;
        }
        attachment.setName(fileName);
      }
    } catch (Exception e) {
      logDebug("Failed to get attachment in node: " + node.getName(), e);
    }
    return attachment;
  }
  
  public static List<FileAttachment> getFileAttachments(Node node) throws Exception {
    List<FileAttachment> listFile = new ArrayList<FileAttachment>();
    NodeIterator iter = node.getNodes();
    while (iter.hasNext()) {
      FileAttachment attachment = getFileAttachment(iter.nextNode());
      if (attachment != null) {
        listFile.add(attachment);
      }
    }
    return listFile;
  }

  @Override
  public Question getQuestionById(String questionId) throws Exception {
    SessionProvider sessionProvider = CommonUtils.createSystemProvider();
    return getQuestion(getQuestionNode(sessionProvider, questionId));
  }

  private List<String> getViewableCategoryIds(SessionProvider sessionProvider) throws Exception {
    List<String> listId = new ArrayList<String>();
    Node cateHomeNode = getCategoryHome(sessionProvider, null);
    StringBuffer queryString = new StringBuffer(JCR_ROOT).append(cateHomeNode.getPath()).append("//element(*,exo:faqCategory)[@exo:isView='true'] order by @exo:createdDate descending");
    QueryManager qm = cateHomeNode.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator iter = result.getNodes();
    while (iter.hasNext()) {
      listId.add(iter.nextNode().getName());
    }
    listId.add(Utils.CATEGORY_HOME);
    return listId;
  }

  private List<String> getRetrictedCategories(String userId, List<String> usermemberships) throws Exception {
    List<String> categoryList = new ArrayList<String>();
    SessionProvider sessionProvider = CommonUtils.createSystemProvider();
    try {
      Node faqHome = getFAQServiceHome(sessionProvider);
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(faqHome.getPath()).append("//element(*,exo:faqCategory)[@exo:userPrivate != ''] order by @exo:createdDate descending");
      QueryManager qm = faqHome.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      boolean isAudience = false;
      List<String> audiences;
      while (iter.hasNext()) {
        if (usermemberships.size() > 0) {
          Node cat = iter.nextNode();
          try {
            audiences = new PropertyReader(cat).list(EXO_USER_PRIVATE, new ArrayList<String>());
            isAudience = false;
            for (String id : usermemberships) {
              for (String audien : audiences) {
                if (id.equals(audien)) {
                  isAudience = true;
                  break;
                }
              }
              if (isAudience)
                break;
            }
            if (!isAudience)
              categoryList.add(cat.getName());
          } catch (Exception e) {
            log.error("Failed to check audience ", e);
          }
        } else {
          categoryList.add(iter.nextNode().getName());
        }
      }
    } catch (Exception e) {
      log.error("Failed to get restricte category: ", e);
    }
    return categoryList;
  }

  @Override
  public QuestionPageList getAllQuestions() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:faqQuestion)[");
      List<String> listIds = getViewableCategoryIds(sProvider);
      for (int i = 0; i < listIds.size(); i++) {
        if (i > 0)
          queryString.append(" or ");
        queryString.append("(exo:categoryId='").append(listIds.get(i)).append("')");
      }
      queryString.append("]order by @exo:createdDate ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true);
      return pageList;
    } catch (Exception e) {
      log.error("Failed to get all questions: ", e);
    }
    return null;
  }

  @Override
  public QuestionPageList getQuestionsNotYetAnswer(String categoryId, boolean isApproved) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      String qr = EMPTY_STR;
      boolean isOpenQs = false;
      if (categoryId.indexOf(" ") > 0) {
        String[] strs = categoryId.split(" ");
        categoryId = strs[0];
        if (strs.length == 3) {
          qr = strs[1];
          isOpenQs = Boolean.parseBoolean(strs[2]);
        } else {
          isOpenQs = Boolean.parseBoolean(strs[1]);
        }
      }
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:faqQuestion)[");
      if (categoryId.equals(Utils.ALL)) {
        List<String> listIds = getViewableCategoryIds(sProvider);
        for (int i = 0; i < listIds.size(); i++) {
          if (i > 0)
            queryString.append(" or ");
          queryString.append("(exo:categoryId='").append(listIds.get(i)).append("')");
        }
      } else {
        queryString.append("((@exo:categoryId='").append(categoryId).append("')").append((categoryId.indexOf("/") > 0) ? (" or (@exo:categoryId='" + categoryId.substring(categoryId.lastIndexOf("/") + 1) + "'))") : ")");
      }
      if (isApproved)
        queryString.append(" and (@exo:isApproved='true')");
      if (qr.length() > 0)
        queryString.append(" and ((@exo:isApproved='true') or ").append(qr).append(")");
      queryString.append("] order by @exo:createdDate ascending");

      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true);
      pageList.setNotYetAnswered(true);
      pageList.setOpenQuestion(isOpenQs);
      return pageList;
    } catch (Exception e) {
      log.error("Get question not yet answer failed: ", e);
    }
    return null;
  }

  @Override
  public QuestionPageList getPendingQuestionsByCategory(String categoryId, FAQSetting faqSetting) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = null;
      if (categoryId == null || categoryId.trim().length() < 1)
        categoryId = "null";
      queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath())
                                              .append("//element(*,")
                                              .append(EXO_FAQ_QUESTION)
                                              .append(")[((")
                                              .append(AT)
                                              .append(EXO_CATEGORY_ID)
                                              .append("='")
                                              .append(categoryId)
                                              .append("')")
                                              .append((categoryId.indexOf("/") > 0) ? (" or (" + AT + EXO_CATEGORY_ID + "='" + categoryId.substring(categoryId.lastIndexOf("/") + 1) + "'))") : ")")
                                              .append(" and (")
                                              .append(AT)
                                              .append(EXO_IS_ACTIVATED)
                                              .append("='true') and (")
                                              .append(AT)
                                              .append(EXO_IS_APPROVED)
                                              .append("='false')]");
      queryString.append("order by ").append(Utils.getOderBy(faqSetting));
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true);
      return pageList;
    } catch (Exception e) {
      log.error("Get pedding question through category failed: ", e);
    }
    return null;
  }

  @Override
  public QuestionPageList getQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      String id;
      Node categoryNode;
      if (categoryId == null || Utils.CATEGORY_HOME.equals(categoryId)) {
        id = Utils.CATEGORY_HOME;
        categoryId = Utils.CATEGORY_HOME;
        categoryNode = getCategoryHome(sProvider, null);
      } else {
        id = categoryId.substring(categoryId.lastIndexOf("/") + 1);
        categoryNode = getFAQServiceHome(sProvider).getNode(categoryId);
      }
      categoryNode = getFAQServiceHome(sProvider).getNode(categoryId);
      QueryManager qm = categoryNode.getSession().getWorkspace().getQueryManager();
      String userId = faqSetting.getCurrentUser();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryNode.getPath()).append("/").append(Utils.QUESTION_HOME).append("/element(*,")
                                 .append(EXO_FAQ_QUESTION).append(")[(@").append(EXO_CATEGORY_ID).append("='").append(id)
                                 .append("') and (@").append(EXO_IS_ACTIVATED).append("='true')");
      if (!faqSetting.isCanEdit()) {
        queryString.append(" and (@").append(EXO_IS_APPROVED).append("='true'");
        if (userId != null && userId.length() > 0 && FAQSetting.DISPLAY_BOTH.equals(faqSetting.getDisplayMode())) {
          queryString.append(" or @").append(EXO_AUTHOR).append("='").append(userId).append("')");
        } else {
          queryString.append(")");
        }
      } else {
        if (FAQSetting.DISPLAY_APPROVED.equals(faqSetting.getDisplayMode())) {
          queryString.append(" and (@").append(EXO_IS_APPROVED).append("='true')");
        }
      }
      queryString.append("] order by ").append(Utils.getOderBy(faqSetting));
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true);
      return pageList;
    } catch (Exception e) {
      log.debug("Getting question through category failed: ", e);
    }
    return null;
  }

  @Override
  public QuestionPageList getAllQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = null;
      if (FAQSetting.DISPLAY_APPROVED.equals(faqSetting.getDisplayMode())) {
        queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')").append(" and (@exo:isApproved='true')").append("]");
      } else {
        queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:faqQuestion)[@exo:categoryId='").append(categoryId).append("'").append("]");
      }
      queryString.append("order by ").append(Utils.getOderBy(faqSetting));
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true);
      return pageList;
    } catch (Exception e) {
      log.debug("Failed to get all question through category: ", e);
    }
    return null;
  }

  @Override
  public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);

      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:faqQuestion) [");
      queryString.append(" (");
      int i = 0;
      for (String categoryId : listCategoryId) {
        if (i > 0)
          queryString.append(" or ");
        queryString.append("(@exo:categoryId='").append(categoryId).append("')");
        i++;
      }
      queryString.append(")]");
      queryString.append(" order by @exo:createdDate ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = null;
      pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true);
      pageList.setNotYetAnswered(isNotYetAnswer);
      return pageList;
    } catch (Exception e) {
      log.debug("Failed get questions through list of category: ", e);
    }
    return null;
  }

  @Override
  public List<Question> getQuickQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<Question> questions = new ArrayList<Question>();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:faqQuestion)[(");
      int i = 0;
      for (String categoryId : listCategoryId) {
        if (i > 0)
          queryString.append(" or ");
        queryString.append("(@exo:categoryId='").append(categoryId).append("')");
        i++;
      }
      queryString.append(")]order by @exo:createdDate ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      while (iter.hasNext()) {
        questions.add(getQuickQuestion(iter.nextNode()));
      }
    } catch (Exception e) {
      log.debug("Getting quick questions through list of category failed: ", e);
    }
    return questions;
  }

  private Question getQuickQuestion(Node questionNode) throws Exception {
    Question question = new Question();
    question.setId(questionNode.getName());
    PropertyReader reader = new PropertyReader(questionNode);
    question.setCategoryId(reader.string(EXO_CATEGORY_ID, EMPTY_STR));
    String path = questionNode.getPath();
    question.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
    question.setQuestion(reader.string(EXO_TITLE, EMPTY_STR));
    question.setApproved(reader.bool(EXO_IS_APPROVED));
    question.setActivated(reader.bool(EXO_IS_ACTIVATED));
    return question;
  }

  @Override
  public String getCategoryPathOfQuestion(String questionPath) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    StringBuilder pathName = new StringBuilder();
    Node faqHome = null;
    try {
      faqHome = getFAQServiceHome(sProvider);
      Node question = faqHome.getNode(questionPath);
      Node subCat = question.getParent().getParent();
      pathName.append(new PropertyReader(faqHome).string(EXO_NAME, "home"));
      while (!subCat.getName().equals(Utils.CATEGORY_HOME)) {
        pathName.append(CommonUtils.SLASH).append(new PropertyReader(subCat).string(EXO_NAME, subCat.getName()));
        subCat = subCat.getParent();
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Getting category path of the question failed: ", e);
      }
    }
    return pathName.toString();
  }

  @Override
  public void moveQuestions(List<String> questions, String destCategoryId, String questionLink, FAQSetting faqSetting) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqHome = getFAQServiceHome(sProvider);
      String homePath = faqHome.getPath();
      Node destQuestionHome;
      try {
        destQuestionHome = (Node) faqHome.getNode(destCategoryId + "/" + Utils.QUESTION_HOME);
      } catch (Exception e) {
        destQuestionHome = faqHome.getNode(destCategoryId).addNode(Utils.QUESTION_HOME, EXO_FAQ_QUESTION_HOME);
        faqHome.getSession().save();
      }
      for (String id : questions) {
        try {
          Node destCateNode = faqHome.getNode(id).getParent();
          faqHome.getSession().move(homePath + "/" + id, destQuestionHome.getPath() + id.substring(id.lastIndexOf("/")));
          faqHome.getSession().save();
          Node questionNode = faqHome.getNode(destCategoryId + "/" + Utils.QUESTION_HOME + id.substring(id.lastIndexOf("/")));
          String catId = destCategoryId.substring(destCategoryId.lastIndexOf("/") + 1);
          questionNode.setProperty(EXO_CATEGORY_ID, catId);
          NodeIterator iter = questionNode.getNodes();
          Node attNode;
          while (iter.hasNext()) {
            attNode = iter.nextNode();
            if (attNode.isNodeType(EXO_FAQ_ATTACHMENT)) {
              attNode.getNode(JCR_CONTENT).setProperty(EXO_CATEGORY_ID, catId);
            }
          }
          updateDatas(questionNode, catId, true);
          updateDatas(questionNode, catId, false);
          questionNode.save();
          unregisterQuestionNodeListener(getQuestionNodeById(id));
          registerQuestionNodeListener(questionNode);
          try {
            sendNotifyMoveQuestion(destCateNode, questionNode, catId, questionLink, faqSetting);
          } catch (Exception e) {
            log.warn("Failed to send notification of moved questions", e);
          }
        } catch (ItemNotFoundException ex) {
          log.warn(String.format("Destination category with %s node is not found", id), ex);
        }
      }
    } catch (Exception e) {
      log.error("Failed to remove question: ", e);
    }
  }

  private void sendNotifyMoveQuestion(Node destCateNode, Node questionNode, String cateId, String link, FAQSetting faqSetting) throws Exception {
    Node categoryName = questionNode.getParent().getParent();
    PropertyReader reader = new PropertyReader(categoryName);
    
    MessageBuilder messageBuilder = new MessageBuilder();
    messageBuilder.setType(MessageBuilder.TYPESEND.MOVE_QUESTION);
    messageBuilder.setCategoryName(reader.string(EXO_NAME, "Root"));
    messageBuilder.setContent(faqSetting.getEmailMoveQuestion());
    messageBuilder.setSubject(faqSetting.getEmailSettingSubject());
    
    reader = new PropertyReader(questionNode);
    messageBuilder.setQuestionOwner(reader.string(EXO_AUTHOR, EMPTY_STR));
    messageBuilder.setQuestionEmail(reader.string(EXO_EMAIL, EMPTY_STR));
    messageBuilder.setQuestionLink(link);
    messageBuilder.setQuestionDetail(reader.string(EXO_NAME, EMPTY_STR));
    messageBuilder.setQuestionContent(reader.string(EXO_TITLE, EMPTY_STR));
    
    Set<String> emails = new HashSet<String>();
    emails.addAll(calculateMoveEmail(destCateNode));
    emails.addAll(calculateMoveEmail(questionNode.getParent()));
    emails.add(messageBuilder.getQuestionEmail());
    sendEmailNotification(new ArrayList<String>(emails), messageBuilder.getMessage());
  }

  private Set<String> calculateMoveEmail(Node node) throws Exception {
    Set<String> set = new HashSet<String>();
    while (!node.getName().equals(Utils.CATEGORY_HOME)) {
      if (node.isNodeType(EXO_FAQ_WATCHING)) {
        set.addAll(new PropertyReader(node).list(EXO_EMAIL_WATCHING, new ArrayList<String>()));
      }
      node = node.getParent();
    }
    return set;
  }

  private void updateDatas(Node question, String catId, boolean isAnswer) throws Exception {
    try {
      QueryManager qm = question.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(question.getPath()).append("//element(*,").append((isAnswer) ? EXO_ANSWER : EXO_COMMENT).append(")");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      while (iter.hasNext()) {
        iter.nextNode().setProperty(EXO_CATEGORY_ID, catId);
      }
    } catch (Exception e) {
      log.error((isAnswer) ? "Updating answers failed: " : "Updating comments failed: ", e);
    }
  }

  @Override
  public void changeStatusCategoryView(List<String> listCateIds) throws Exception {
    if (listCateIds == null || listCateIds.size() < 1)
      return;
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqHome = getFAQServiceHome(sProvider);
      Node cat;
      for (String id : listCateIds) {
        cat = faqHome.getNode(id);
        cat.setProperty(EXO_IS_VIEW, !cat.getProperty(EXO_IS_VIEW).getBoolean());
      }
      faqHome.save();
    } catch (Exception e) {
      log.error("Changing status category view failed: ", e);
    }
  }

  @Override
  public long getMaxindexCategory(String parentId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    long max = 0;
    try {
      NodeIterator iter = getFAQServiceHome(sProvider).getNode((parentId == null) ? Utils.CATEGORY_HOME : parentId).getNodes();
      while (iter.hasNext()) {
        Node node = iter.nextNode();
        if (node.isNodeType(EXO_FAQ_CATEGORY))
          max = max + 1;
      }
    } catch (Exception e) {
      log.error("Failed to get max index category", e);
    }
    return max;
  }

  private void saveCategory(Node categoryNode, Category category, boolean isNew, SessionProvider sProvider) throws Exception {
    Map<String, String> moderators = new HashMap<String, String>();
    if (!categoryNode.getName().equals(Utils.CATEGORY_HOME)) {
      Node parentCategory = categoryNode.getParent();
      if (parentCategory.hasProperty(EXO_MODERATORS)) {
        for (Value vl : parentCategory.getProperty(EXO_MODERATORS).getValues()) {
          moderators.put(vl.getString(), vl.getString());
        }
      }
    }

    if (category.getId() != null) {
      categoryNode.setProperty(EXO_ID, category.getId());
      categoryNode.setProperty(EXO_CREATED_DATE, CommonUtils.getGreenwichMeanTime());
      categoryNode.setProperty(EXO_IS_VIEW, category.isView());
    }
    categoryNode.setProperty(EXO_INDEX, category.getIndex());
    categoryNode.setProperty(EXO_NAME, category.getName());
    categoryNode.setProperty(EXO_DESCRIPTION, category.getDescription());
    for (String mod : category.getModerators()) {
      moderators.put(mod, mod);
    }
    categoryNode.setProperty(EXO_MODERATORS, moderators.values().toArray(new String[] {}));
    categoryNode.setProperty(EXO_IS_MODERATE_QUESTIONS, category.isModerateQuestions());
    categoryNode.setProperty(EXO_VIEW_AUTHOR_INFOR, category.isViewAuthorInfor());
    categoryNode.setProperty(EXO_IS_MODERATE_ANSWERS, category.isModerateAnswers());
    categoryNode.setProperty(EXO_USER_PRIVATE, category.getUserPrivate());
    if (!isNew) {
      try {
        updateModeratorForChildCategories(categoryNode, moderators);
      } catch (Exception e) {
        log.debug("Updating moderator for child category failed: ", e);
      }
    }
    if (categoryNode.isNew())
      categoryNode.getSession().save();
    else
      categoryNode.save();
  }

  private void updateModeratorForChildCategories(Node currentCategory, Map<String, String> moderators) throws Exception {
    Map<String, String> modMap = new HashMap<String, String>();
    Node cat;
    NodeIterator iter = currentCategory.getNodes();
    while (iter.hasNext()) {
      cat = iter.nextNode();
      if (cat.isNodeType(EXO_FAQ_CATEGORY)) {
        modMap.clear();
        modMap.putAll(moderators);
        for (Value vl : cat.getProperty(EXO_MODERATORS).getValues()) {
          modMap.put(vl.getString(), vl.getString());
        }
        cat.setProperty(EXO_MODERATORS, modMap.values().toArray(new String[] {}));
        cat.save();
        if (cat.hasNodes()) {
          updateModeratorForChildCategories(cat, modMap);
        }
      }
    }
  }

  private void resetIndex(Node goingCategory, long index, long gindex) throws Exception {
    Node parent = goingCategory.getParent();
    Node node;
    NodeIterator iter = getCategoriesIterator(parent);
    if (index <= iter.getSize()) {
      if (index < 1) {
        goingCategory.setProperty(EXO_INDEX, 1);
      }
      long l = 1;
      while (iter.hasNext()) {
        node = iter.nextNode();
        if (index < 1) {// move up to top
          if (node.getName().equals(goingCategory.getName()))
            continue;
          else {
            l++;
            node.setProperty(EXO_INDEX, l);
          }
        } else if (index > gindex) {// move down to index
          l = node.getProperty(EXO_INDEX).getLong();
          if (l >= gindex && l <= index) {
            if (l == gindex) {
              goingCategory.setProperty(EXO_INDEX, index);
            } else {
              node.setProperty(EXO_INDEX, l - 1);
            }
          }
          if (l > index)
            break;
        } else {// move up to index
          l = node.getProperty(EXO_INDEX).getLong();
          if (l > index) {
            if (l == gindex) {
              goingCategory.setProperty(EXO_INDEX, index + 1);
            } else {
              node.setProperty(EXO_INDEX, l + 1);
            }
          }
        }
      }
      parent.save();
    } else {
      goingCategory.setProperty(EXO_INDEX, iter.getSize());
      goingCategory.save();
    }
    reUpdateIndex(parent);
  }
  
  private void reUpdateIndex(Node parentCateNode) throws Exception {
    NodeIterator iter = getCategoriesIterator(parentCateNode);
    long i = 1;
    Node node;
    while (iter.hasNext()) {
      node = iter.nextNode();
      node.setProperty(EXO_INDEX, i);
      i++;
    }
    parentCateNode.save();
  }

  @Override
  public void saveCategory(String parentId, Category cat, boolean isAddNew){
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node newCategory;
      if (isAddNew) {
        Node parentNode = getFAQServiceHome(sProvider).getNode(parentId);
        newCategory = parentNode.addNode(cat.getId(), EXO_FAQ_CATEGORY);
        newCategory.addMixin(MIX_FAQ_SUB_CATEGORY);
        newCategory.addNode(Utils.QUESTION_HOME, EXO_FAQ_QUESTION_HOME);
        newCategory.getSession().save();
      } else {
        newCategory = getFAQServiceHome(sProvider).getNode(cat.getPath());
      }
      long index = cat.getIndex();
      long oldIndex = new PropertyReader(newCategory).l(EXO_INDEX, newCategory.getParent().getNodes().getSize());
      boolean isResetIndex = (isAddNew || (oldIndex != index));
      if (isResetIndex) {
        cat.setIndex(oldIndex);
      }
      saveCategory(newCategory, cat, isAddNew, sProvider);
      if (isResetIndex) {
        index = (index < oldIndex) ? index - 1 : index;
        resetIndex(newCategory, index, oldIndex);
      }
    } catch (Exception e) {
      log.error("Failed to save category: ", e);
    }
  }

  private List<Cate> listingSubTree(Node currentCategory, int i) throws Exception {
    Node cat;
    int j = i;
    j = j + 1;
    List<Cate> cateList = new ArrayList<Cate>();
    Cate cate;
    NodeIterator iter = currentCategory.getNodes();
    while (iter.hasNext()) {
      cat = iter.nextNode();
      if (cat.isNodeType(EXO_FAQ_CATEGORY)) {
        cate = new Cate();
        cate.setCategory(getCategory(cat));
        cate.setDeft(i);
        cateList.add(cate);
        if (cat.hasNodes()) {
          cateList.addAll(listingSubTree(cat, j));
          ;
        }
      }
    }
    return cateList;
  }

  @Override
  public List<Cate> listingCategoryTree() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    Node cateHome = getCategoryHome(sProvider, null);
    int i = 1;
    List<Cate> cateList = new ArrayList<Cate>();
    cateList.addAll(listingSubTree(cateHome, i));
    return cateList;
  }

  @Override
  public void removeCategory(String categoryId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqHome = getFAQServiceHome(sProvider);
      Node node = faqHome.getNode(categoryId);
      Node parent = node.getParent();
      node.remove();
      faqHome.save();
      // update index
      reUpdateIndex(parent);
    } catch (Exception e) {
      log.error("Can not remove category has id: " + categoryId);
    }
  }

  private Category getCategory(Node categoryNode) throws Exception {
    if (categoryNode == null)
      return null;
    Category category = new Category();
    PropertyReader reader = new PropertyReader(categoryNode);
    category.setId(categoryNode.getName());
    category.setName(reader.string(EXO_NAME, EMPTY_STR));
    category.setDescription(reader.string(EXO_DESCRIPTION, EMPTY_STR));
    category.setCreatedDate(reader.date(EXO_CREATED_DATE));
    category.setModerators(reader.strings(EXO_MODERATORS, new String[] {}));
    category.setUserPrivate(reader.strings(EXO_USER_PRIVATE, new String[] {}));
    category.setModerateQuestions(reader.bool(EXO_IS_MODERATE_QUESTIONS));
    category.setModerateAnswers(reader.bool(EXO_IS_MODERATE_ANSWERS));
    category.setViewAuthorInfor(reader.bool(EXO_VIEW_AUTHOR_INFOR));
    category.setIndex(reader.l(EXO_INDEX, 1));
    category.setView(reader.bool(EXO_IS_VIEW, true));
    String path = categoryNode.getPath();
    category.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
    return category;
  }

  @Override
  public Category getCategoryById(String categoryId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      return getCategory(getCategoryNode(sProvider, categoryId));
    } catch (Exception e) {
      log.debug("Category not found " + categoryId);
    }
    return null;
  }

  @Override
  public List<Category> findCategoriesByName(String categoryName) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:faqCategory)[@exo:name='").append(categoryName).append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult queryResult = query.execute();
      NodeIterator iter = queryResult.getNodes();
      List<Category> result = new ArrayList<Category>();
      while (iter.hasNext()) {
        result.add(getCategory(iter.nextNode()));
      }
      return result;
    } catch (Exception e) {
      log.error("Could not retrieve categories by name " + categoryName, e);
    }
    return null;
  }

  @Override
  public List<String> getListCateIdByModerator(String user) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      List<String> allOfUser = UserHelper.getAllGroupAndMembershipOfUser(user);
      StringBuilder builder = new StringBuilder();      
      int i = 0;
      if(allOfUser != null && allOfUser.size() > 0){
        builder.append(" ( ");
        for (String strUser : allOfUser) {
          strUser = strUser.trim();
          if (strUser.length() > 0) {
            if (i > 0) {
              builder.append(" or ");
            }
            builder.append("@").append(EXO_MODERATORS).append("='").append(strUser).append("'");
            i++;
          }
        }
        builder.append(" ) and ");
      }
      builder.append("(@").append(EXO_IS_VIEW).append("='true')");
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getParent().getPath())
                                                           .append("//element(*,").append(EXO_FAQ_CATEGORY).append(")[").append(builder).append("]");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      List<String> listCateId = new ArrayList<String>();
      while (iter.hasNext()) {
        Node cate = iter.nextNode();
        try {
          listCateId.add(cate.getName() + CommonUtils.SEMICOLON + cate.getProperty(EXO_NAME).getString());
        } catch (Exception e) {
          log.debug("Getting property of " + cate + " node failed: ", e);
        }
      }
      return listCateId;
    } catch (Exception e) {
      log.error("Failed to get list of CateID through Moderator: ", e);
    }
    return null;
  }

  @Override
  public List<Category> getAllCategories() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:faqCategory)");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      List<Category> catList = new ArrayList<Category>();
      while (iter.hasNext()) {
        catList.add(getCategory(iter.nextNode()));
      }
      return catList;
    } catch (Exception e) {
      log.error("Getting all category failed: ", e);
    }
    return null;

  }
  
  public Object readQuestionProperty(String questionId, String propertyName, Class returnType) throws Exception{
    if (questionId == null || propertyName == null) 
      throw new IllegalArgumentException("The parameter is null");
    Node questionNode = getQuestionNodeById(questionId);
    if (questionNode != null)
      return new PropertyReader(questionNode).readProperty(propertyName, returnType);
    else return null;
  }
  
  /**
   * read property of the category by its name
   * @param categoryId id of the category
   * @param propertyName name of the property
   * @param returnValue expected return-type. The supported class types are String[], String, Long, Boolean, Double and Date.  
   * @return 
   * @throws Exception
   */
  public Object readCategoryProperty(String categoryId, String propertyName, Class returnType) throws Exception {
    if (categoryId == null || propertyName == null) 
      throw new IllegalArgumentException("The parameter is null");
    Node categoryNode = getCategoryNodeById(categoryId);
    if (categoryNode != null)
      return new PropertyReader(categoryNode).readProperty(propertyName, returnType);
    else return null;
  }
  
  public long existingCategories() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:faqCategory)");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      result.getNodes().getSize();
    } catch (Exception e) {
      log.error("Failed to check existing categories", e);
    }
    return 0;
  }

  public Node getCategoryNodeById(String categoryId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      return getCategoryNode(sProvider, categoryId);
    } catch (Exception e) {
      log.error("Getting node failed: ", e);
    }
    return null;
  }

  /**
   * Get node category by categoryId or category relative path.
   * 
   * @param: sProvider the SessionProvider.
   * @param: param the category id or category relative path.
   * @return: The Node has node type is exo:faqCategory. 
   */
  private Node getCategoryNode(SessionProvider sProvider, String param) throws Exception {
    try {
      Node faqHome = getFAQServiceHome(sProvider);
      return faqHome.getNode(param);
    } catch (PathNotFoundException e) {
      param = (param.indexOf("/") > 0) ? param.substring(param.lastIndexOf("/") + 1) : param;
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,").append(EXO_FAQ_CATEGORY).append(")").append("[fn:name()='").append(param).append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      if (iter.getSize() != 0)
        return iter.nextNode();
    }
    return null;
  }

  @Override
  public List<Category> getSubCategories(String categoryId, FAQSetting faqSetting, boolean isGetAll, List<String> limitedUsers) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<Category> catList = new ArrayList<Category>();
    if (limitedUsers == null) {
      limitedUsers = UserHelper.getAllGroupAndMembershipOfUser(null);
    }
    try {
      Node parentCategory;
      if (categoryId == null || categoryId.equals(Utils.CATEGORY_HOME)) {
        parentCategory = getCategoryHome(sProvider, null);
      } else
        parentCategory = getFAQServiceHome(sProvider).getNode(categoryId);
      if (!faqSetting.isAdmin()) {
        PropertyReader reader = new PropertyReader(parentCategory);
        List<String> userPrivates = reader.list(EXO_USER_PRIVATE, new ArrayList<String>());
        if (!userPrivates.isEmpty() && !Utils.hasPermission(limitedUsers, userPrivates))
          return catList;
      }

      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(parentCategory.getPath());
      if (faqSetting.isAdmin()) {
        queryString.append("/element(*,").append(EXO_FAQ_CATEGORY)
                   .append(") [@").append(EXO_IS_VIEW).append("='true'] order by @").append(EXO_INDEX).append(" ascending");
      } else {
        queryString.append("/element(*,").append(EXO_FAQ_CATEGORY)
                   .append(") [@").append(EXO_IS_VIEW).append("='true' and ( not(@").append(EXO_USER_PRIVATE)
                   .append(") or @").append(EXO_USER_PRIVATE).append("=''");
        if (limitedUsers.size() > 0) {
          queryString.append(" or ").append(Utils.buildQueryListOfUser(EXO_USER_PRIVATE, limitedUsers));
          queryString.append(" or ").append(Utils.buildQueryListOfUser(EXO_MODERATORS, limitedUsers));
        }
        queryString.append(")] order by @").append(EXO_INDEX).append(" ascending");
      }
      QueryManager qm = parentCategory.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      while (iter.hasNext()) {
        catList.add(getCategory(iter.nextNode()));
      }
    } catch (Exception e) {
      throw e;
    }
    return catList;
  }

  @Override
  public long[] getCategoryInfo(String categoryId, FAQSetting faqSetting) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    long[] cateInfo = new long[] { 0, 0, 0, 0 };// categories, all, open, pending
    try {
      Node parentCategory;
      String id;
      parentCategory = getFAQServiceHome(sProvider).getNode(categoryId);
      if (categoryId.indexOf("/") > 0)
        id = categoryId.substring(categoryId.lastIndexOf("/") + 1);
      else
        id = categoryId;
      NodeIterator iter = parentCategory.getNodes();
      cateInfo[0] = iter.getSize();
      QueryManager qm = parentCategory.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(parentCategory.getPath()).append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(id).append("') and (@exo:isActivated='true')").append("]").append("order by @exo:createdDate ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator nodeIterator = result.getNodes();
      cateInfo[1] = nodeIterator.getSize();// all

      Node questionNode = null;
      boolean onlyGetApproved, questionIsApproved = true, isShow = true;
      onlyGetApproved = (FAQSetting.DISPLAY_APPROVED.equals(faqSetting.getDisplayMode()));
      while (nodeIterator.hasNext()) {
        questionNode = nodeIterator.nextNode();
        questionIsApproved = questionNode.getProperty(EXO_IS_APPROVED).getBoolean();
        isShow = (questionIsApproved || ((faqSetting.isCanEdit() || questionNode.getProperty(EXO_AUTHOR).getString().equals(faqSetting.getCurrentUser())) && !onlyGetApproved));
        if (!questionIsApproved) {
          cateInfo[3]++;// pending
          if (!isShow)
            cateInfo[1]--;
        }
        if (isShow) {
          if (!hasAnswerInQuestion(qm, questionNode))
            cateInfo[2]++;// open
        }
      }
    } catch (Exception e) {
      log.error("Failed to get category info: ", e);
    }
    return cateInfo;
  }

  private boolean hasAnswerInQuestion(QueryManager qm, Node questionNode) throws Exception {
    StringBuffer queryString = new StringBuffer(JCR_ROOT).append(questionNode.getPath()).append("//element(*,exo:answer)[(@exo:approveResponses='true') and (@exo:activateResponses='true')] order by @exo:dateResponse ascending");
    QueryImpl impl = (QueryImpl) qm.createQuery(queryString.toString(), Query.XPATH);
    impl.setOffset(0);
    impl.setLimit(1);
    QueryResult result = impl.execute();
    NodeIterator iter = result.getNodes();
    return (iter.getSize() > 0) ? true : false;
  }

  @Override
  public void moveCategory(String categoryId, String destCategoryId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqHome = getFAQServiceHome(sProvider);
      Node srcNode = faqHome.getNode(categoryId);
      Node parentNode = srcNode.getParent();
      String srcPath = srcNode.getPath();
      String destPath = faqHome.getPath() + "/" + destCategoryId + "/" + srcNode.getName();
      faqHome.getSession().move(srcPath, destPath);
      faqHome.getSession().save();
      Node destNode = faqHome.getNode(destCategoryId + "/" + srcNode.getName());
      destNode.setProperty(EXO_INDEX, destNode.getParent().getNodes().getSize());
      destNode.save();
      // update index for children categories of parent category moved. 
      reUpdateIndex(parentNode);
    } catch (ItemExistsException e) {
      throw e;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug(e.getMessage());
    }
  }

  @Override
  public void addWatchCategory(String id, Watch watch) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqHome = getFAQServiceHome(sProvider);
      Map<String, String> watchs = new HashMap<String, String>();
      Node watchingNode = faqHome.getNode(id);
      if (watchingNode.isNodeType(EXO_FAQ_WATCHING)) {
        Value[] emails = watchingNode.getProperty(EXO_EMAIL_WATCHING).getValues();
        Value[] users = watchingNode.getProperty(EXO_USER_WATCHING).getValues();
        if (emails != null && users != null) {
          for (int i = 0; i < users.length; i++) {
            watchs.put(users[i].getString(), emails[i].getString());
          }
        }
        watchs.put(watch.getUser(), watch.getEmails());
        watchingNode.setProperty(EXO_EMAIL_WATCHING, watchs.values().toArray(new String[] {}));
        watchingNode.setProperty(EXO_USER_WATCHING, watchs.keySet().toArray(new String[] {}));
      } else {
        watchingNode.addMixin(EXO_FAQ_WATCHING);
        watchingNode.setProperty(EXO_EMAIL_WATCHING, new String[] { watch.getEmails() });
        watchingNode.setProperty(EXO_USER_WATCHING, new String[] { watch.getUser() });
      }
      watchingNode.save();
    } catch (Exception e) {
      log.error("Failed to add watch category: ", e);
    }
  }

  // TODO Going to remove
  @Override
  public QuestionPageList getListMailInWatch(String categoryId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 5, queryString.toString(), true);
      return pageList;
    } catch (Exception e) {
      log.error("Failed to get list of mail watch: ", e);
    }
    return null;
  }

  @Override
  public List<Watch> getWatchByCategory(String categoryId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<Watch> listWatches = new ArrayList<Watch>();
    try {
      Node category = getFAQServiceHome(sProvider).getNode(categoryId);
      if (category.isNodeType(EXO_FAQ_WATCHING)) {
        PropertyReader reader = new PropertyReader(category);
        String[] userWatch = reader.strings(EXO_USER_WATCHING);
        String[] emails = reader.strings(EXO_EMAIL_WATCHING);
        if (userWatch != null && userWatch.length > 0) {
          Watch watch;
          for (int i = 0; i < userWatch.length; i++) {
            watch = new Watch();
            watch.setEmails(emails[i]);
            watch.setUser(userWatch[i]);
            listWatches.add(watch);
          }
        }
      }
      return listWatches;
    } catch (Exception e) {
      log.error("Failed to get watch through category: ", e);
    }
    return listWatches;
  }

  @Override
  public boolean hasWatch(String categoryPath) {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node cat = getFAQServiceHome(sProvider).getNode(categoryPath);
      if (new PropertyReader(cat).strings(EXO_USER_WATCHING, new String[] {}).length > 0)
        return true;
    } catch (Exception e) {
      log.error("Failed to check has watch", e);
    }
    return false;
  }

  @Override
  public void addWatchQuestion(String questionId, Watch watch, boolean isNew) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    Map<String, String> watchMap = new HashMap<String, String>();
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
      if (questionNode.isNodeType(EXO_FAQ_WATCHING)) {
        Value[] values = questionNode.getProperty(EXO_EMAIL_WATCHING).getValues();
        Value[] users = questionNode.getProperty(EXO_USER_WATCHING).getValues();
        for (int i = 0; i < users.length; i++) {
          watchMap.put(users[i].getString(), values[i].getString());
        }
        watchMap.put(watch.getUser(), watch.getEmails());

        questionNode.setProperty(EXO_EMAIL_WATCHING, watchMap.values().toArray(new String[] {}));
        questionNode.setProperty(EXO_USER_WATCHING, watchMap.keySet().toArray(new String[] {}));
        questionNode.save();
      } else {
        questionNode.addMixin(EXO_FAQ_WATCHING);
        questionNode.setProperty(EXO_EMAIL_WATCHING, new String[] { watch.getEmails() });
        questionNode.setProperty(EXO_USER_WATCHING, new String[] { watch.getUser() });
        questionNode.save();
      }
    } catch (Exception e) {
      log.error("Failed to add a watch question: ", e);
    }
  }

  @Override
  public List<Watch> getWatchByQuestion(String questionId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<Watch> listWatches = new ArrayList<Watch>();
    try {
      Node quetionNode = getFAQServiceHome(sProvider).getNode(questionId);
      if (quetionNode.isNodeType(EXO_FAQ_WATCHING)) {
        PropertyReader reader = new PropertyReader(quetionNode);
        String[] userWatch = reader.strings(EXO_EMAIL_WATCHING);
        String[] emails = reader.strings(EXO_USER_WATCHING);
        if (userWatch != null && userWatch.length > 0) {
          Watch watch;
          for (int i = 0; i < userWatch.length; i++) {
            watch = new Watch();
            watch.setEmails(emails[i]);
            watch.setUser(userWatch[i]);
            listWatches.add(watch);
          }
        }
      }
    } catch (Exception e) {
      log.error("Failed to get watch through question: ", e);
    }
    return listWatches;
  }

  @Override
  public QuestionPageList getWatchedCategoryByUser(String userId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = null;
      queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:faqCategory)[(@exo:userWatching='").append(userId).append("')]");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true);
      return pageList;
    } catch (Exception e) {
      log.error("Failed to get watched category through user: ", e);
    }
    return null;
  }

  @Override
  public boolean isUserWatched(String userId, String cateId) {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqHome = getFAQServiceHome(sProvider);
      Node cate = faqHome.getNode(cateId);
      List<String> list = new PropertyReader(cate).list(EXO_USER_WATCHING, new ArrayList<String>());
      for (String vl : list) {
        if (vl.equals(userId))
          return true;
      }
    } catch (Exception e) {
      log.error("Failed to check user watched", e);
    }
    return false;
  }

  @Override
  public List<String> getWatchedSubCategory(String userId, String cateId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<String> watchedSub = new ArrayList<String>();
    try {
      Node faqHome = getFAQServiceHome(sProvider);
      Node category = faqHome.getNode(cateId);
      QueryManager qm = faqHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(category.getPath()).append("/element(*,exo:faqCategory)[(@exo:userWatching='").append(userId).append("')]");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      while (iter.hasNext()) {
        watchedSub.add(iter.nextNode().getName());
      }
    } catch (Exception e) {
      log.error("Getting watched sub category failed: ", e);
    }
    return watchedSub;
  }

  @Override
  public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:faqQuestion)[(@exo:userWatching='").append(currentUser).append("')");
      if (FAQSetting.DISPLAY_APPROVED.equals(faqSetting.getDisplayMode())) {
        queryString.append(" and (@exo:isApproved='true')");
      }
      if (!faqSetting.isAdmin())
        queryString.append(" and (@exo:isActivated='true')");
      queryString.append("] order by ").append(Utils.getOderBy(faqSetting));
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true);
      return pageList;
    } catch (Exception e) {
      log.error("Failed to get list of question watch: ", e);
    }
    return null;
  }

  // Going to remove
  @Override
  public void deleteCategoryWatch(String categoryId, String user) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node category = getFAQServiceHome(sProvider).getNode(categoryId);
      Map<String, String> emailMap = new HashMap<String, String>();
      Value[] emailValues = category.getProperty(EXO_EMAIL_WATCHING).getValues();
      Value[] userValues = category.getProperty(EXO_USER_WATCHING).getValues();
      for (int i = 0; i < emailValues.length; i++) {
        emailMap.put(userValues[i].getString(), emailValues[i].getString());
      }
      emailMap.remove(user);
      category.setProperty(EXO_USER_WATCHING, emailMap.keySet().toArray(new String[] {}));
      category.setProperty(EXO_EMAIL_WATCHING, emailMap.values().toArray(new String[] {}));
      category.save();
    } catch (Exception e) {
      log.error("Failed to deleted category watch", e);
    }
  }

  @Override
  public void unWatchCategory(String categoryId, String user) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node category = getFAQServiceHome(sProvider).getNode(categoryId);
      Map<String, String> userMap = new HashMap<String, String>();
      Value[] emailValues = category.getProperty(EXO_EMAIL_WATCHING).getValues();
      Value[] userValues = category.getProperty(EXO_USER_WATCHING).getValues();
      for (int i = 0; i < userValues.length; i++) {
        userMap.put(userValues[i].getString(), emailValues[i].getString());
      }
      userMap.remove(user);
      category.setProperty(EXO_EMAIL_WATCHING, userMap.values().toArray(new String[] {}));
      category.setProperty(EXO_USER_WATCHING, userMap.keySet().toArray(new String[] {}));
      category.save();
    } catch (Exception e) {
      log.error("Failed to unWatch category", e);
    }
  }

  @Override
  public void unWatchQuestion(String questionId, String user) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node question = getFAQServiceHome(sProvider).getNode(questionId);
      Map<String, String> userMap = new HashMap<String, String>();
      Value[] emailValues = question.getProperty(EXO_EMAIL_WATCHING).getValues();
      Value[] userValues = question.getProperty(EXO_USER_WATCHING).getValues();
      for (int i = 0; i < userValues.length; i++) {
        userMap.put(userValues[i].getString(), emailValues[i].getString());
      }
      userMap.remove(user);
      question.setProperty(EXO_EMAIL_WATCHING, userMap.values().toArray(new String[] {}));
      question.setProperty(EXO_USER_WATCHING, userMap.keySet().toArray(new String[] {}));
      question.save();
    } catch (Exception e) {
      log.error("Unwatching question failed: ", e);
    }
  }

  @Override
  public List<ObjectSearchResult> getSearchResults(FAQEventQuery eventQuery) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();

    eventQuery.setViewingCategories(getViewableCategoryIds(sProvider));
    List<String> retrictedCategoryList = new ArrayList<String>();
    if (!eventQuery.isAdmin())
      retrictedCategoryList = getRetrictedCategories(eventQuery.getUserId(), eventQuery.getUserMembers());

    Node categoryHome = getCategoryHome(sProvider, null);
    eventQuery.setPath(categoryHome.getPath());
    try {
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(eventQuery.getQuery(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      Node nodeObj = null;
      if (eventQuery.getType().equals(FAQEventQuery.FAQ_CATEGORY)) { // Category search
        List<ObjectSearchResult> results = new ArrayList<ObjectSearchResult>();
        while (iter.hasNext()) {
          if (eventQuery.isAdmin()) {
            Node cat = iter.nextNode();
            // for retricted audiences
            if (retrictedCategoryList.size() > 0) {
              String path = cat.getPath();
              for (String id : retrictedCategoryList) {
                if (path.indexOf(id) > 0) {
                  results.add(getResultObj(cat));
                  break;
                }
              }
            } else {
              results.add(getResultObj(cat));
            }
          } else {
            results.add(getResultObj(iter.nextNode()));
          }

        }
        return results;
      } else if (eventQuery.getType().equals(FAQEventQuery.FAQ_QUESTION)) { // Question search
        List<ObjectSearchResult> results = new ArrayList<ObjectSearchResult>();
        Map<String, Node> mergeQuestion = new HashMap<String, Node>();
        Map<String, Node> mergeQuestion2 = new HashMap<String, Node>();
        List<Node> listQuestion = new ArrayList<Node>();
        List<Node> listLanguage = new ArrayList<Node>();
        Map<String, Node> listAnswerandComment = new HashMap<String, Node>();
        while (iter.hasNext()) {
          nodeObj = iter.nextNode();
          if (!eventQuery.isAdmin()) {
            try {
              if (nodeObj.isNodeType(EXO_FAQ_QUESTION)) {
                if ((nodeObj.getProperty(EXO_IS_APPROVED).getBoolean() == true && nodeObj.getProperty(EXO_IS_ACTIVATED).getBoolean() == true) || (nodeObj.getProperty(EXO_AUTHOR).getString().equals(eventQuery.getUserId()) && nodeObj.getProperty(EXO_IS_ACTIVATED).getBoolean() == true))
                  // for retricted audiences
                  if (retrictedCategoryList.size() > 0) {
                    String path = nodeObj.getPath();
                    boolean isCanView = true;
                    for (String id : retrictedCategoryList) {
                      if (path.indexOf(id) > 0) {
                        isCanView = false;
                        break;
                      }
                    }
                    if (isCanView)
                      listQuestion.add(nodeObj);
                  } else {
                    listQuestion.add(nodeObj);
                  }

              }

              if (nodeObj.isNodeType(EXO_FAQ_RESOURCE)) {
                Node nodeQuestion = nodeObj.getParent().getParent();
                if ((nodeQuestion.getProperty(EXO_IS_APPROVED).getBoolean() == true && nodeQuestion.getProperty(EXO_IS_ACTIVATED).getBoolean() == true) || (nodeQuestion.getProperty(EXO_AUTHOR).getString().equals(eventQuery.getUserId()) && nodeQuestion.getProperty(EXO_IS_ACTIVATED).getBoolean() == true))
                  // for retricted audiences
                  if (retrictedCategoryList.size() > 0) {
                    boolean isCanView = true;
                    String path = nodeObj.getPath();
                    for (String id : retrictedCategoryList) {
                      if (path.indexOf(id) > 0) {
                        isCanView = false;
                        break;
                      }
                    }
                    if (isCanView)
                      listQuestion.add(nodeQuestion);
                  } else {
                    listQuestion.add(nodeQuestion);
                  }

              }

              if (nodeObj.isNodeType(EXO_FAQ_LANGUAGE)) {
                Node nodeQuestion = nodeObj.getParent().getParent();
                if ((nodeQuestion.getProperty(EXO_IS_APPROVED).getBoolean() == true && nodeQuestion.getProperty(EXO_IS_ACTIVATED).getBoolean() == true) || (nodeQuestion.getProperty(EXO_AUTHOR).getString().equals(eventQuery.getUserId()) && nodeQuestion.getProperty(EXO_IS_ACTIVATED).getBoolean() == true))
                  // for retricted audiences
                  if (retrictedCategoryList.size() > 0) {
                    boolean isCanView = true;
                    String path = nodeObj.getPath();
                    for (String id : retrictedCategoryList) {
                      if (path.indexOf(id) > 0) {
                        isCanView = false;
                        break;
                      }
                    }
                    if (isCanView)
                      listLanguage.add(nodeObj);
                  } else {
                    listLanguage.add(nodeObj);
                  }
              }

              if (nodeObj.isNodeType(EXO_ANSWER) || nodeObj.isNodeType(EXO_COMMENT)) { // answers of default language
                String quesId = nodeObj.getProperty(EXO_QUESTION_ID).getString();
                if (!listAnswerandComment.containsKey(quesId)) {
                  Node nodeQuestion = nodeObj.getParent().getParent();
                  if (nodeQuestion.isNodeType(EXO_FAQ_QUESTION)) {
                    if ((nodeQuestion.getProperty(EXO_IS_APPROVED).getBoolean() == true && nodeQuestion.getProperty(EXO_IS_ACTIVATED).getBoolean() == true) || (nodeQuestion.getProperty(EXO_AUTHOR).getString().equals(eventQuery.getUserId()) && nodeQuestion.getProperty(EXO_IS_ACTIVATED).getBoolean() == true))
                      // for retricted audiences
                      if (retrictedCategoryList.size() > 0) {
                        boolean isCanView = true;
                        String path = nodeObj.getPath();
                        for (String id : retrictedCategoryList) {
                          if (path.indexOf(id) > 0) {
                            isCanView = false;
                            break;
                          }
                        }
                        if (isCanView)
                          listAnswerandComment.put(quesId, nodeObj);
                      } else {
                        listAnswerandComment.put(quesId, nodeObj);
                      }
                  } else { // answers of other languages
                    nodeQuestion = nodeObj.getParent().getParent().getParent().getParent();
                    if ((nodeQuestion.getProperty(EXO_IS_APPROVED).getBoolean() == true && nodeQuestion.getProperty(EXO_IS_ACTIVATED).getBoolean() == true) || (nodeQuestion.getProperty(EXO_AUTHOR).getString().equals(eventQuery.getUserId()) && nodeQuestion.getProperty(EXO_IS_ACTIVATED).getBoolean() == true)) {
                      // for retricted audiences
                      if (retrictedCategoryList.size() > 0) {
                        boolean isCanView = true;
                        String path = nodeObj.getPath();
                        for (String id : retrictedCategoryList) {
                          if (path.indexOf(id) > 0) {
                            isCanView = false;
                            break;
                          }
                        }
                        if (isCanView)
                          listAnswerandComment.put(quesId, nodeObj);
                      } else {
                        listAnswerandComment.put(quesId, nodeObj);
                      }
                    }
                  }

                }

              }
            } catch (Exception e) {
              log.error("Failed to add item in list search", e);
            }

          } else {
            if (nodeObj.isNodeType(EXO_FAQ_QUESTION))
              listQuestion.add(nodeObj);
            if (nodeObj.isNodeType(EXO_FAQ_RESOURCE))
              listQuestion.add(nodeObj.getParent().getParent());
            if (nodeObj.isNodeType(EXO_FAQ_LANGUAGE))
              listLanguage.add(nodeObj);
            if (nodeObj.isNodeType(EXO_ANSWER) || nodeObj.isNodeType(EXO_COMMENT))
              listAnswerandComment.put(nodeObj.getProperty(EXO_QUESTION_ID).getString(), nodeObj);
          }
        }

        boolean isInitiated = false;
        if (eventQuery.isQuestionLevelSearch()) {
          // directly return because there is only one this type of search
          if (!eventQuery.isLanguageLevelSearch() && !eventQuery.isAnswerCommentLevelSearch()) {
            List<String> list = new ArrayList<String>();
            for (Node node : listQuestion) {
              if (list.contains(node.getName()))
                continue;
              else
                list.add(node.getName());
              results.add(getResultObj(node));
            }
            return results;
          }
          // merging results
          if (!listQuestion.isEmpty()) {
            isInitiated = true;
            for (Node node : listQuestion) {
              mergeQuestion.put(node.getName(), node);
            }
          }
        }
        if (eventQuery.isLanguageLevelSearch()) {
          // directly return because there is only one this type of search
          if (!eventQuery.isQuestionLevelSearch() && !eventQuery.isAnswerCommentLevelSearch()) {
            for (Node node : listLanguage) {
              results.add(getResultObj(node));
            }
            return results;
          }

          // merging results
          if (isInitiated) {
            for (Node node : listLanguage) {
              String id = node.getProperty(EXO_QUESTION_ID).getString();
              if (mergeQuestion.containsKey(id)) {
                mergeQuestion2.put(id, mergeQuestion.get(id));
              }
            }
          } else {
            for (Node node : listLanguage) {
              mergeQuestion2.put(node.getProperty(EXO_QUESTION_ID).getString(), node);
            }
            isInitiated = true;
          }
        }

        if (eventQuery.isAnswerCommentLevelSearch()) {
          // directly return because there is only one this type of search
          if (!eventQuery.isLanguageLevelSearch() && !eventQuery.isQuestionLevelSearch()) {
            for (Node node : listAnswerandComment.values()) {
              results.add(getResultObj(node));
            }
            return results;
          }
          // merging results
          if (isInitiated) {
            if (eventQuery.isLanguageLevelSearch()) {
              if (mergeQuestion2.isEmpty())
                return results;
              for (Node node : listAnswerandComment.values()) {
                String id = node.getProperty(EXO_QUESTION_ID).getString();
                if (mergeQuestion2.containsKey(id)) {
                  results.add(getResultObj(node));
                }
              }
            } else { // search on question level
              if (mergeQuestion.isEmpty())
                return results;
              for (Node node : listAnswerandComment.values()) {
                String id = node.getProperty(EXO_QUESTION_ID).getString();
                if (mergeQuestion.containsKey(id)) {
                  results.add(getResultObj(node));
                }
              }
            }
          } else {
            for (Node node : listAnswerandComment.values()) {
              results.add(getResultObj(node));
            }
          }
        }
        // mix all result for fultext search on questions
        if (!eventQuery.isQuestionLevelSearch() && !eventQuery.isAnswerCommentLevelSearch() && !eventQuery.isLanguageLevelSearch()) {
          Map<String, ObjectSearchResult> tmpResult = new HashMap<String, ObjectSearchResult>();
          ObjectSearchResult rs;
          for (Node node : listAnswerandComment.values()) {
            rs = getResultObj(node);
            tmpResult.put(rs.getId(), rs);
          }
          for (Node node : listQuestion) {
            rs = getResultObj(node);
            tmpResult.put(rs.getId(), rs);
          }
          for (Node node : listLanguage) {
            rs = getResultObj(node);
            tmpResult.put(rs.getId(), rs);
          }
          results.addAll(tmpResult.values());
        }
        return results;

      } else if (eventQuery.getType().equals(FAQEventQuery.CATEGORY_AND_QUESTION)) { // Quick search
        String nodePath = EMPTY_STR;
        Session session = categoryHome.getSession();
        Map<String, ObjectSearchResult> searchMap = new HashMap<String, ObjectSearchResult>();

        while (iter.hasNext()) {
          boolean isResult = true;
          nodeObj = iter.nextNode();
          nodePath = nodeObj.getPath();
          if (nodePath.indexOf("/Question") > 0 && nodePath.lastIndexOf("/") >= nodePath.indexOf("/Question")) {
            nodePath = nodePath.substring(0, nodePath.indexOf("/Question") + 41);
            nodeObj = (Node) session.getItem(nodePath);
            if (!eventQuery.isAdmin()) {
              try {
                if ((nodeObj.getProperty(EXO_IS_APPROVED).getBoolean() == true && nodeObj.getProperty(EXO_IS_ACTIVATED).getBoolean() == true) || (nodeObj.getProperty(EXO_AUTHOR).getString().equals(eventQuery.getUserId()) && nodeObj.getProperty(EXO_IS_ACTIVATED).getBoolean() == true)) {
                  // for retricted audiences

                  if (retrictedCategoryList.size() > 0) {
                    String path = nodeObj.getPath();
                    for (String id : retrictedCategoryList) {
                      if (path.indexOf(id) > 0) {
                        isResult = false;
                        break;
                      }
                    }
                  }
                } else {
                  isResult = false;
                }
              } catch (Exception e) {
                log.debug(nodeObj + " node must exist: ", e);
                isResult = false;
              }
            }
          } else if (nodeObj.isNodeType(EXO_FAQ_CATEGORY)) {
            if (!eventQuery.isAdmin()) {
              // for restricted audiences
              if (retrictedCategoryList.size() > 0) {
                String path = nodeObj.getPath();
                for (String id : retrictedCategoryList) {
                  if (path.indexOf(id) > 0) {
                    isResult = false;
                    break;
                  }
                }
              }
            }
          }
          if (!searchMap.containsKey(nodeObj.getName()) && isResult) {
            searchMap.put(nodeObj.getName(), getResultObj(nodeObj));
          }
        }
        return new ArrayList<ObjectSearchResult>(searchMap.values());
      }
    } catch (Exception e) {
      throw e;
    }
    return new ArrayList<ObjectSearchResult>();
  }

  private ObjectSearchResult getResultObj(Node node) throws Exception {
    ObjectSearchResult objectResult = new ObjectSearchResult();
    if (node.isNodeType(EXO_FAQ_CATEGORY)) {
      objectResult.setIcon("FAQCategorySearch");
      objectResult.setName(node.getProperty(EXO_NAME).getString());
      objectResult.setType("faqCategory");
      String path = node.getPath();
      objectResult.setId(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
      objectResult.setCreatedDate(node.getProperty(EXO_CREATED_DATE).getDate().getTime());
    } else {
      if (node.isNodeType(EXO_FAQ_QUESTION)) {
        if (questionHasAnswer(node)) {
          objectResult.setIcon("QuestionSearch");
        } else {
          objectResult.setIcon("NotResponseSearch");
        }
        objectResult.setName(node.getProperty(EXO_TITLE).getString());
        String path = node.getPath();
        objectResult.setId(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
        objectResult.setCreatedDate(node.getProperty(EXO_CREATED_DATE).getDate().getTime());
      } else {
        objectResult.setIcon("QuestionSearch");
        String nodePath = node.getPath();
        nodePath = nodePath.substring(0, nodePath.indexOf("/Question") + 41);
        Node questionNode = (Node) node.getSession().getItem(nodePath);
        objectResult.setName(questionNode.getProperty(EXO_TITLE).getString());
        String path = questionNode.getPath();
        objectResult.setId(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
        objectResult.setCreatedDate(questionNode.getProperty(EXO_CREATED_DATE).getDate().getTime());
      }
      objectResult.setType("faqQuestion");
    }

    return objectResult;
  }

  @Override
  public List<String> getCategoryPath(String categoryId) throws Exception {
    List<String> breadcums = new ArrayList<String>();
    try {
      Node category = getCategoryNodeById(categoryId);
      while (!category.getName().equals(Utils.CATEGORY_HOME)) {
        breadcums.add(category.getName());
        category = category.getParent();
      }
    } catch (Exception e) {
      log.error("Failed to get category: ", e);
    }
    return breadcums;
  }

  @Override
  public String getParentCategoriesName(String path) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    StringBuilder names = new StringBuilder();
    List<String> list = new ArrayList<String>();
    try {
      Node category = getFAQServiceHome(sProvider).getNode(path);
      while (category.isNodeType(EXO_FAQ_CATEGORY)) {
        if (category.hasProperty(EXO_NAME)) {
          list.add(category.getProperty(EXO_NAME).getString());
        } else {
          list.add(category.getName());
        }
        category = category.getParent();
      }
      for (int i = list.size() - 1; i >= 0; i--) {
        if (i != list.size() - 1)
          names.append(" > ");
        names.append(list.get(i));
      }
    } catch (Exception e) {
      log.error("Failed to get parent categories name", e);
    }
    return names.toString();
  }

  private void sendEmailNotification(List<String> addresses, Message message) {
    pendingMessagesQueue.add(new NotifyInfo(addresses, message));
  }

  @Override
  public Iterator<NotifyInfo> getPendingMessages() {
    Iterator<NotifyInfo> pending = new ArrayList<NotifyInfo>(pendingMessagesQueue).iterator();
    pendingMessagesQueue.clear();
    return pending;
  }

  @Override
  public NotifyInfo getMessageInfo(String name) throws Exception {
    NotifyInfo messageInfo = messagesInfoMap_.get(name);
    messagesInfoMap_.remove(name);
    return messageInfo;
  }

  @Override
  public void swapCategories(String cateId1, String cateId2) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      String[] strs = cateId2.split(",");
      boolean isTop = (strs.length > 1 && strs[1].trim().length() > 0);
      Node faqHome = getFAQServiceHome(sProvider);
      Node goingCategory = faqHome.getNode(cateId1);
      Node mockCategory = faqHome.getNode(strs[0].trim());
      long index = (isTop) ? 0 : mockCategory.getProperty(EXO_INDEX).getLong();
      if (goingCategory.getParent().getPath().equals(mockCategory.getParent().getPath())) {
        long gindex = goingCategory.getProperty(EXO_INDEX).getLong();
        resetIndex(goingCategory, index, gindex);
      } else {
        Node parent = mockCategory.getParent();
        String asPath = parent.getPath().replaceFirst(dataLocator.getFaqHomeLocation(), EMPTY_STR).replaceFirst("//", EMPTY_STR);
        if (!isCategoryExist(sProvider, new PropertyReader(goingCategory).string(EXO_NAME, EMPTY_STR), asPath)) {
          Node parentNode = goingCategory.getParent();
          String id = goingCategory.getName();
          mockCategory.getSession().move(goingCategory.getPath(), parent.getPath() + "/" + id);
          faqHome.getSession().save();
          Node destCat = parent.getNode(id);
          long l = 1;
          if (!isTop) {
            l = parent.getNodes().getSize();
            destCat.setProperty(EXO_INDEX, l);
            parent.save();
          }
          resetIndex(destCat, index, l);
          // update index for children categories of parent category moved.
          reUpdateIndex(parentNode);
        } else {
          throw new RuntimeException();
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      log.debug("Failed to swap categories.", e);
    }
  }

  private NodeIterator getCategoriesIterator(Node parentCategory) throws Exception {
    QueryManager qm = parentCategory.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer(JCR_ROOT).append(parentCategory.getPath());
    queryString.append("/element(*,").append(EXO_FAQ_CATEGORY).append(") order by @").append(EXO_INDEX).append(" ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  @Override
  public void saveTopicIdDiscussQuestion(String questionId, String topicId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
      questionNode.setProperty(EXO_TOPIC_ID_DISCUSS, topicId);
      questionNode.save();
    } catch (Exception e) {
      log.error("Failed to save topic discuss question: ", e);
    }
  }

  @Override
  public InputStream exportData(String categoryId, boolean createZipFile) throws Exception {
    Node categoryNode = getCategoryNodeById(categoryId);
    Session session = categoryNode.getSession();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    File file = null;
    List<File> listFiles = new ArrayList<File>();
    session.exportSystemView(categoryNode.getPath(), bos, false, false);
    listFiles.add(CommonUtils.getXMLFile(bos, "eXo Knowledge Suite - Answers", "Category", CommonUtils.getGreenwichMeanTime().getTime(), categoryNode.getName()));
    ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream("exportCategory.zip"));
    try {
      int byteReads;
      byte[] buffer = new byte[4096]; // Create a buffer for copying
      FileInputStream inputStream = null;
      ZipEntry zipEntry = null;
      for (File f : listFiles) {
        try {
          inputStream = new FileInputStream(f);
          zipEntry = new ZipEntry(f.getPath());
          zipOutputStream.putNextEntry(zipEntry);
          while ((byteReads = inputStream.read(buffer)) != -1)
            zipOutputStream.write(buffer, 0, byteReads);
        } finally {
          inputStream.close();
        }
      }
    } finally {
      zipOutputStream.close();
    }

    file = new File("exportCategory.zip");
    InputStream fileInputStream = new FileInputStream(file);
    return fileInputStream;
  }

  @Override
  public boolean importData(String parentId, InputStream inputStream, boolean isZip) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      List<String> patchNodeImport = new ArrayList<String>();
      Node categoryNode = getFAQServiceHome(sProvider).getNode(parentId);
      Session session = categoryNode.getSession();
      NodeIterator iter = categoryNode.getNodes();
      while (iter.hasNext()) {
        patchNodeImport.add(iter.nextNode().getName());
      }
      if (isZip) { // Import from zipfile
        ZipInputStream zipStream = new ZipInputStream(inputStream);
        while (zipStream.getNextEntry() != null) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          int available = -1;
          byte[] data = new byte[2048];
          while ((available = zipStream.read(data, 0, 1024)) > -1) {
            out.write(data, 0, available);
          }
          zipStream.closeEntry();
          out.close();
          InputStream input = new ByteArrayInputStream(out.toByteArray());
          session.importXML(categoryNode.getPath(), input, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
          session.save();
        }
        zipStream.close();
        calculateImportRootCategory(categoryNode);
      } else { // import from xml
        session.importXML(categoryNode.getPath(), inputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
        session.save();
      }
      categoryNode = (Node) session.getItem(categoryNode.getPath());
      iter = categoryNode.getNodes();
      while (iter.hasNext()) {
        Node node = iter.nextNode();
        if (patchNodeImport.contains(node.getName()))
          patchNodeImport.remove(node.getName());
        else
          patchNodeImport.add(node.getName());
      }
      for (String string : patchNodeImport) {
        Node nodeParentQuestion = categoryNode.getNode(string);
        iter = getQuestionsIterator(nodeParentQuestion, EMPTY_STR, true);
        // Update number answers and regeister question node listener
        while (iter.hasNext()) {
          Node node = iter.nextNode();
          reUpdateNumberOfPublicAnswers(node);
          registerQuestionNodeListener(node);
        }
      }
    } catch (Exception e) {
      log.warn("Failed to import data in category " + parentId + ":\n" + e.getMessage());
      return false;
    }
    return true;
  }

  private void calculateImportRootCategory(Node categoryRootNode) {
    try {
      NodeIterator iterator0 = categoryRootNode.getNodes();
      int i = 0;
      while (iterator0.hasNext()) {
        if (iterator0.nextNode().isNodeType(EXO_FAQ_CATEGORY)) {
          i = i + 1;
        }
      }
      if (categoryRootNode.hasNode(Utils.CATEGORY_HOME)) {
        Node categoryNode = categoryRootNode.getNode(Utils.CATEGORY_HOME);
        NodeIterator iterator = categoryNode.getNodes();
        String rootPath = categoryRootNode.getPath();
        Session session = categoryRootNode.getSession();
        Workspace workspace = session.getWorkspace();
        while (iterator.hasNext()) {
          Node node = iterator.nextNode();
          try {
            if (node.isNodeType(EXO_FAQ_CATEGORY)) {
              node.setProperty(EXO_INDEX, i);
              i = i + 1;
              workspace.move(node.getPath(), rootPath + "/" + node.getName());
            } else if (node.isNodeType(EXO_FAQ_QUESTION_HOME)) {
              if (categoryRootNode.hasNode(Utils.QUESTION_HOME)) {
                NodeIterator iter = node.getNodes();
                while (iter.hasNext()) {
                  Node node_ = iter.nextNode();
                  workspace.move(node_.getPath(), rootPath + "/" + Utils.QUESTION_HOME + "/" + node_.getName());
                }
              } else {
                workspace.move(node.getPath(), rootPath + "/" + node.getName());
              }
            }
          } catch (Exception e) {
            if (log.isDebugEnabled()) {
              log.debug("Failed to move node " + node.getName(), e);
            }
          }
        }
        categoryNode.remove();
        session.save();
      }
    } catch (Exception e) {
        log.warn("Failed to calculate imported root category");
    }
  }

  @Override
  public boolean isExisting(String path) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    return getFAQServiceHome(sProvider).hasNode(path);
  }

  @Override
  public String getCategoryPathOf(String id) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node node = getFAQServiceHome(sProvider).getNode(id);
      String path = null;
      if (node != null) {
        if (node.isNodeType(EXO_FAQ_QUESTION)) {
          path = node.getParent().getParent().getPath();
        } else if (node.isNodeType(EXO_FAQ_CATEGORY)) {
          path = node.getPath();
        }
        return path.substring(path.indexOf(Utils.CATEGORY_HOME));
      }
    } catch (Exception e) {
      logDebug("Failed to get category of path: " + id + "\n" + e.getMessage());
    }
    return null;
  }
  
  private boolean getBooleanPropertyOfCategory(String id, String property) {
    try {
      SessionProvider sProvider = CommonUtils.createSystemProvider();
      Node node = getQuestionNode(sProvider, id);
      if (node != null && node.isNodeType(EXO_FAQ_QUESTION)) {
        node = node.getParent().getParent();
      } else {
        node = getCategoryNode(sProvider, id);
      }
      return new PropertyReader(node).bool(property);
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean isModerateAnswer(String id) {
    return getBooleanPropertyOfCategory(id, EXO_IS_MODERATE_ANSWERS);
  }

  @Override
  public boolean isModerateQuestion(String id) {
    return getBooleanPropertyOfCategory(id, EXO_IS_MODERATE_QUESTIONS);
  }

  @Override
  public boolean isViewAuthorInfo(String id) {
    return getBooleanPropertyOfCategory(id, EXO_VIEW_AUTHOR_INFOR);
  }

  @Override
  public boolean isCategoryModerator(String categoryId, String user) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node node = getCategoryNode(sProvider, categoryId);
      List<String> values = new PropertyReader(node).list(EXO_MODERATORS, new ArrayList<String>());
      List<String> userGroups = UserHelper.getAllGroupAndMembershipOfUser(user);
      return Utils.hasPermission(userGroups, values);
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public boolean isCategoryExist(String name, String path) {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    return isCategoryExist(sProvider, name, path);
  }

  private boolean isCategoryExist(SessionProvider sProvider, String name, String path) {
    if (CommonUtils.isEmpty(path)) {
      path = Utils.CATEGORY_HOME;
    }
    try {
      NodeIterator iter = getFAQServiceHome(sProvider).getNode(path).getNodes();
      while (iter.hasNext()) {
        Node catNode = iter.nextNode();
        if (catNode.isNodeType(EXO_FAQ_CATEGORY)) {
          if (new PropertyReader(catNode).string(EXO_NAME, EMPTY_STR).equalsIgnoreCase(name)) {
            return true;
          }
        }
      }
    } catch (Exception e) {
      log.error("Cheking whether catagory is exist: ", e);
    }
    return false;
  }

  @Override
  public List<String> getQuestionContents(List<String> paths) throws Exception {
    PropertyReader reader = null;
    List<String> contents = new ArrayList<String>();
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    for (String path : paths) {
      try {
        reader = new PropertyReader(getQuestionNode(sProvider, path));
        contents.add(reader.string(EXO_TITLE));
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Failed to get question node with path " + path, e);
        }
      }
    }
    return contents;
  }
  
  /**
   * Get a map which contains id as questionID and value as question's title
   * @param paths
   * @return a map which has key: questionID and value: question title
   * @throws Exception
   */
  public Map<String, String> getRelationQuestion(List<String> paths) throws Exception {
    Map<String, String> mReturn = new LinkedHashMap<String, String>();
    PropertyReader reader = null;
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    for (String path : paths) {
      try {
        reader = new PropertyReader(getQuestionNode(sProvider, path));
        if(reader.bool(EXO_IS_ACTIVATED) && reader.bool(EXO_IS_APPROVED)){
          mReturn.put(path, reader.string(EXO_TITLE));
        }
      } catch (Exception e) {
        log.error("getRelationQuestion fails, exception:", e);
      }
    }
    return mReturn;
  }
  

  // will be remove
  @Override
  public Node getQuestionNodeById(String path) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      return getQuestionNode(sProvider, path);
    } catch (Exception e) {
      log.error("Failed to get question node by path:" + path, e);
    }
    return null;
  }

  /**
   *Get Node question by param
   *
   *@param: sProvider the @SessionProvider
   *@param: param the question id or question relative path.
   *@return: the question Node.
  */
  private Node getQuestionNode(SessionProvider sProvider, String param) throws Exception {
    Node serviceHome = getFAQServiceHome(sProvider);
    try {
      return serviceHome.getNode(param);
    } catch (PathNotFoundException e) {
      param = (param.indexOf("/") > 0) ? param.substring(param.lastIndexOf("/") + 1) : param;
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(serviceHome.getPath()).append("//element(*,exo:faqQuestion)[fn:name()='").append(param).append("']");
      QueryManager qm = serviceHome.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      if (iter.getSize() > 0)
        return iter.nextNode();
    } catch (Exception e) {
      // The function return null when can not get Question Node by question id.
      return null;
    }
    return null;
  }

  @Override
  public String[] getModeratorsOf(String path) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node node = getFAQServiceHome(sProvider).getNode(path);
      if (node.isNodeType(EXO_FAQ_QUESTION)) {
        return new PropertyReader(node.getParent().getParent()).strings(EXO_MODERATORS, new String[] {});
      } else if (node.isNodeType(EXO_FAQ_CATEGORY)) {
        return new PropertyReader(node).strings(EXO_MODERATORS, new String[] {});
      }
    } catch (Exception e) {
      log.error("Failed to get moderators of path: " + path, e);
    }
    return new String[] {};
  }

  @Override
  public String getCategoryNameOf(String categoryPath) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node node = getFAQServiceHome(sProvider).getNode(categoryPath);
      return new PropertyReader(node).string(EXO_NAME, node.getName());
    } catch (Exception e) {
      log.error("Failed to get category name of path: " + categoryPath, e);
    }
    return null;
  }

  @Override
  public CategoryInfo getCategoryInfo(String categoryPath, List<String> categoryIdScoped) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    CategoryInfo categoryInfo = new CategoryInfo();
    try {
      Node categoryNode = getFAQServiceHome(sProvider).getNode(categoryPath);
      categoryInfo.setId(categoryNode.getName());
      String path = categoryNode.getPath();
      categoryInfo.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
      if (categoryNode.hasProperty(EXO_NAME))
        categoryInfo.setName(categoryNode.getProperty(EXO_NAME).getString());
      else
        categoryInfo.setName(categoryNode.getName());
      // set Path Name
      Node node = categoryNode;
      List<String> pathName = new ArrayList<String>();
      String categoryName;
      while (node.isNodeType(EXO_FAQ_CATEGORY)) {
        if (node.hasProperty(EXO_NAME))
          categoryName = node.getProperty(EXO_NAME).getString();
        else
          categoryName = node.getName();
        pathName.add(categoryName);
        node = node.getParent();
      }
      categoryInfo.setPathName(pathName);
      // declare question info
      categoryInfo.setQuestionInfos(getQuestionInfo(categoryNode));

      // declare category info
      if (categoryNode.hasNodes()) {
        List<SubCategoryInfo> subList = new ArrayList<SubCategoryInfo>();
        NodeIterator subIter = categoryNode.getNodes();
        Node sub;
        SubCategoryInfo subCat;
        while (subIter.hasNext()) {
          sub = subIter.nextNode();
          if (categoryIdScoped.isEmpty() || categoryIdScoped.contains(sub.getName())) {
            if (sub.isNodeType(EXO_FAQ_CATEGORY)) {
              subCat = new SubCategoryInfo();
              subCat.setId(sub.getName());
              subCat.setName(sub.getProperty(EXO_NAME).getString());
              subCat.setPath(categoryInfo.getPath() + "/" + sub.getName());
              subCat.setSubCateInfos(getSubCategoryInfo(sub, categoryIdScoped));
              subCat.setQuestionInfos(getQuestionInfo(sub));
              subList.add(subCat);
            }
          }
        }
        categoryInfo.setSubCateInfos(subList);
      }
    } catch (Exception e) {
      return null;
    }
    return categoryInfo;
  }

  private void registerQuestionNodeListener(Node questionNode) {
    try {
      ObservationManager observation = questionNode.getSession().getWorkspace().getObservationManager();
      QuestionNodeListener listener = new QuestionNodeListener();
      String questionPath = questionNode.getPath();
      observation.addEventListener(listener, Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED, questionPath, true, null, null, false);
      listeners.put(questionPath, listener);
    } catch (Exception e) {
      log.error("can not add listener to question node", e);
    }

  }
  
  private void unregisterQuestionNodeListener(Node questionNode) {
    try {
      String questionPath = questionNode.getPath();
      if (listeners.containsKey(questionPath)) {
        ObservationManager observation = questionNode.getSession().getWorkspace().getObservationManager();
        observation.removeEventListener((QuestionNodeListener) listeners.get(questionPath));
        listeners.remove(questionPath);
      }
    } catch (Exception e) {
      log.error("can not remove listener from question node", e);
    }
  }

  public void reCalculateInfoOfQuestion(String absPathOfProp) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Item item = null;
      Node quesNode = null;
      Node serviceHomeNode = getFAQServiceHome(sProvider);

      // ----- get Question Node -------------
      int quesNameIndex = absPathOfProp.lastIndexOf(Utils.QUESTION_HOME) + Utils.QUESTION_HOME.length() + 2;
      String quesPath = absPathOfProp.substring(0, absPathOfProp.indexOf("/", quesNameIndex));
      try {
        quesNode = (Node) serviceHomeNode.getSession().getItem(quesPath);
      } catch (PathNotFoundException pe) {
        return;
      }
      String lastActivityInfo = null;
      if (quesNode.hasProperty(EXO_LAST_ACTIVITY))
        lastActivityInfo = quesNode.getProperty(EXO_LAST_ACTIVITY).getString();
      long timeOfLastActivity = Utils.getTimeOfLastActivity(lastActivityInfo);
      long numberOfAnswers = 0;
      if (quesNode.hasProperty(EXO_NUMBER_OF_PUBLIC_ANSWERS)) {
        numberOfAnswers = quesNode.getProperty(EXO_NUMBER_OF_PUBLIC_ANSWERS).getLong();
      }
      // ------------- end -----------------

      // -------------- get updated Item ----------------
      try {
        item = getFAQServiceHome(sProvider).getSession().getItem(absPathOfProp);
      } catch (PathNotFoundException pnfe) {
        // item has been removed. Update last activity of question.
        reUpdateLastActivityOfQuestion(quesNode);
        reUpdateNumberOfPublicAnswers(quesNode);
        return;
      }

      if (item instanceof Property) {
        Property prop = (Property) item;
        if (prop.getName().equalsIgnoreCase(EXO_ACTIVATE_RESPONSES) || prop.getName().equalsIgnoreCase(EXO_APPROVE_RESPONSES)) {
          // if activate or approve property has been changed.
          Node answerNode = prop.getParent();
          boolean isActivated = false, isApproved = false;
          if (answerNode.hasProperty(EXO_ACTIVATE_RESPONSES))
            isActivated = answerNode.getProperty(EXO_ACTIVATE_RESPONSES).getBoolean();
          if (answerNode.hasProperty(EXO_APPROVE_RESPONSES))
            isApproved = answerNode.getProperty(EXO_APPROVE_RESPONSES).getBoolean();
          long answerTime = 0;
          if (answerNode.hasProperty(EXO_DATE_RESPONSE))
            answerTime = answerNode.getProperty(EXO_DATE_RESPONSE).getDate().getTimeInMillis();
          if (isActivated && isApproved) {
            numberOfAnswers++;
            quesNode.setProperty(EXO_NUMBER_OF_PUBLIC_ANSWERS, numberOfAnswers);
            // admin changed this answer to public ...
            if (timeOfLastActivity < answerTime) {
              String author = answerNode.getProperty(EXO_RESPONSE_BY).getString();
              quesNode.setProperty(EXO_LAST_ACTIVITY, getLastActivityInfo(author, answerTime));
            }
            quesNode.save();

            return;
          } else {
            // if admin change answer status from viewable to unapproved and
            // inactivated
            reUpdateNumberOfPublicAnswers(quesNode);
            // reUpdateLastActivityOfQuestion(quesNode);
            if (timeOfLastActivity == answerTime) {
              // re-update last activity now
              reUpdateLastActivityOfQuestion(quesNode);
              return;
            }
          }
        }

      }

      if (item instanceof Node) {
        // case of adding new comment.
        Node node = (Node) item;
        if (node.getPrimaryNodeType().getName().equalsIgnoreCase(EXO_COMMENT)) {
          long commentTime = node.getProperty(EXO_DATE_COMMENT).getDate().getTimeInMillis();
          if (commentTime > timeOfLastActivity) {
            String author = node.getProperty(EXO_COMMENT_BY).getString();
            quesNode.setProperty(EXO_LAST_ACTIVITY, getLastActivityInfo(author, commentTime));
            quesNode.save();
          }
        }
      }
    } catch (Exception e) {
      log.error("Failed to re calculateInfo of question", e);
    }
  }

  private void reUpdateNumberOfPublicAnswers(Node questionNode) throws RepositoryException {
    QueryManager qm = questionNode.getSession().getWorkspace().getQueryManager();
    StringBuilder sb = new StringBuilder();
    sb.append(JCR_ROOT).append(questionNode.getPath()).append("//element(*, exo:answer)[@exo:activateResponses='true' and @exo:approveResponses='true']");
    Query query = qm.createQuery(sb.toString(), Query.XPATH);
    QueryResult result = query.execute();
    long size = result.getNodes().getSize();
    size = size < 0 ? 0 : size;
    questionNode.setProperty(EXO_NUMBER_OF_PUBLIC_ANSWERS, size);
    questionNode.save();

  }

  private void reUpdateLastActivityOfQuestion(Node quesNode) throws RepositoryException {
    QueryManager qm = quesNode.getSession().getWorkspace().getQueryManager();

    StringBuilder sb = new StringBuilder();
    sb.append(JCR_ROOT).append(quesNode.getPath()).append("//element(*, exo:answer)[@exo:activateResponses='true' and @exo:approveResponses='true'] order by @exo:dateResponse descending");
    QueryImpl query = (QueryImpl) qm.createQuery(sb.toString(), Query.XPATH);
    query.setLimit(1);
    QueryResult result = query.execute();
    NodeIterator iter = result.getNodes();

    String author = null;
    long lastTime = -1;
    if (iter.hasNext()) {
      Node node = iter.nextNode();

      if (node.hasProperty(EXO_DATE_RESPONSE)) {
        lastTime = node.getProperty(EXO_DATE_RESPONSE).getDate().getTimeInMillis();
      }
      if (node.hasProperty(EXO_RESPONSE_BY)) {
        author = node.getProperty(EXO_RESPONSE_BY).getString();
      }

    }

    sb = new StringBuilder();
    sb.append(JCR_ROOT).append(quesNode.getPath()).append("//element(*, exo:comment) order by @exo:dateComment descending");
    query = (QueryImpl) qm.createQuery(sb.toString(), Query.XPATH);
    query.setLimit(1);
    result = query.execute();
    iter = result.getNodes();
    if (iter.hasNext()) {
      Node commentNode = iter.nextNode();
      if (commentNode.hasProperty(EXO_DATE_COMMENT) && commentNode.hasProperty(EXO_COMMENT_BY)) {
        long commentTime = commentNode.getProperty(EXO_DATE_COMMENT).getDate().getTimeInMillis();
        if (lastTime < commentTime) {
          lastTime = commentTime;
          author = commentNode.getProperty(EXO_COMMENT_BY).getString();
        }
      }
    }
    if (lastTime > 0) {
      quesNode.setProperty(EXO_LAST_ACTIVITY, getLastActivityInfo(author, lastTime));
    } else {
      quesNode.setProperty(EXO_LAST_ACTIVITY, EMPTY_STR);
    }
    quesNode.save();
  }

  private List<SubCategoryInfo> getSubCategoryInfo(Node category, List<String> categoryIdScoped) throws Exception {
    List<SubCategoryInfo> subList = new ArrayList<SubCategoryInfo>();
    if (category.hasNodes()) {
      NodeIterator iter = category.getNodes();
      Node sub;
      SubCategoryInfo cat;
      while (iter.hasNext()) {
        try {
          sub = iter.nextNode();
          if (sub.isNodeType(EXO_FAQ_CATEGORY)) {
            if (categoryIdScoped.isEmpty() || categoryIdScoped.contains(sub.getName())) {
              cat = new SubCategoryInfo();
              cat.setName(sub.getProperty(EXO_NAME).getString());
              String path = sub.getPath();
              cat.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
              cat.setId(sub.getName());
              subList.add(cat);
            }
          }
        } catch (Exception e) {
          log.error("Failed to get sub category info: ", e);
        }
      }
    }
    return subList;
  }

  private NodeIterator getNodeIteratorAnswerAccess(Node answerHome) throws Exception {
    StringBuffer queryString = new StringBuffer(JCR_ROOT).append(answerHome.getPath()).append("/element(*,exo:answer)[@exo:approveResponses='true' and @exo:activateResponses='true']");
    QueryManager qm = answerHome.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  private List<QuestionInfo> getQuestionInfo(Node categoryNode) throws Exception {
    List<QuestionInfo> questionInfoList = new ArrayList<QuestionInfo>();
    if (categoryNode.hasNode(Utils.QUESTION_HOME)) {
      QuestionInfo questionInfo;
      String strQuery = "[@exo:isActivated='true' and @exo:isApproved='true']";
      NodeIterator iter = getQuestionsIterator(categoryNode.getNode(Utils.QUESTION_HOME), strQuery, false);
      Node question;
      while (iter.hasNext()) {
        question = iter.nextNode();
        questionInfo = new QuestionInfo();
        try {
          questionInfo.setQuestion(question.getProperty(EXO_TITLE).getString());
          questionInfo.setDetail(question.getProperty(EXO_NAME).getString());
          questionInfo.setId(question.getName());
          if (question.hasNode(Utils.ANSWER_HOME)) {
            List<String> answers = new ArrayList<String>();
            NodeIterator ansIter = getNodeIteratorAnswerAccess(question.getNode(Utils.ANSWER_HOME));
            while (ansIter.hasNext()) {
              answers.add(ansIter.nextNode().getProperty(EXO_RESPONSES).getString());
            }
            questionInfo.setAnswers(answers);
          }
          questionInfoList.add(questionInfo);
        } catch (Exception e) {
          log.error("Failed to add answer by question node: " + question.getName(), e);
        }
      }
    }
    return questionInfoList;
  }

  @Override
  public void updateQuestionRelatives(String questionPath, String[] relatives) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node question = getFAQServiceHome(sProvider).getNode(questionPath);
      question.setProperty(EXO_RELATIVES, relatives);
      question.save();
    } catch (Exception e) {
      log.error("Failed to update question relatives: ", e);
    }
  }

  @Override
  public void calculateDeletedUser(String userName) throws Exception {
    // remove setting by user
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node node = getUserSettingHome(sProvider);
      if (node.hasNode(userName))
        node.getNode(userName).remove();
      StringBuilder queryString = new StringBuilder(JCR_ROOT).append("/").append(dataLocator.getFaqCategoriesLocation()).append("//*[");
      String[] strs = new String[] { EXO_RESPONSE_BY, EXO_COMMENT_BY, EXO_AUTHOR, EXO_USER_WATCHING };
      for (int i = 0; i < strs.length; i++) {
        queryString.append("@").append(strs[i]).append("='").append(userName).append((i == strs.length - 1) ? "']" : "' or ");
      }
      Session session = node.getSession();
      QueryManager qm = session.getWorkspace().getQueryManager();
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      Node item;
      String newUserName = userName + Utils.DELETED + (new Random().nextInt(1000));
      PropertyReader reader;
      while (iter.hasNext()) {
        item = iter.nextNode();
        reader = new PropertyReader(item);
        for (int i = 0; i < strs.length; i++) {
          if (i < 3 && reader.string(strs[i], "").equals(userName)) {
            item.setProperty(strs[i], newUserName);
          } else {
            List<String> list = reader.list(strs[i], new ArrayList<String>());
            if (list.size() > 0 && list.contains(userName)) {
              list.remove(userName);
              item.setProperty(strs[i], list.toArray(new String[list.size()]));
            }
          }
        }
      }
      session.save();
      // LastActivity
      queryString = new StringBuilder(JCR_ROOT).append("/").append(dataLocator.getFaqCategoriesLocation())
                                               .append("//element(*,").append(EXO_FAQ_QUESTION).append(")[(jcr:contains(@")
                                               .append(EXO_LAST_ACTIVITY).append(", '").append(userName).append(Utils.HYPHEN).append("*'))]");
      query = qm.createQuery(queryString.toString(), Query.XPATH);
      iter = query.execute().getNodes();
      Question question = new Question();
      String info;
      while (iter.hasNext()) {
        item = iter.nextNode();
        info = new PropertyReader(item).string(EXO_LAST_ACTIVITY);
        if (!CommonUtils.isEmpty(info)) {
          question.setLastActivity(info);
          if (userName.equals(question.getAuthorOfLastActivity())) {
            item.setProperty(EXO_LAST_ACTIVITY, getLastActivityInfo(newUserName, question.getTimeOfLastActivity()));
          }
        }
      }
      session.save();
    } catch (Exception e) {
      log.debug("Failed to calculate delete user: " + userName, e);
    }
  }

  public InputStream createAnswerRSS(String cateId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node cateNode = getCategoryNode(sProvider, cateId);
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      StringBuilder queryString = new StringBuilder(JCR_ROOT).append(cateNode.getPath()).append("//element(*,exo:faqQuestion)");
      List<String> list = getListCategoryIdPublic(sProvider, cateNode);
      if (!list.isEmpty())
        queryString.append("[");
      PropertyReader reader = new PropertyReader(cateNode);
      if (reader.list(EXO_USER_PRIVATE, new ArrayList<String>()).isEmpty()) {
        if (!list.isEmpty())
          list.add(cateNode.getName());
      }
      boolean isOr = false;
      for (String id : list) {
        if (isOr) {
          queryString.append(" or (@exo:categoryId='").append(id).append("')");
        } else {
          queryString.append("(@exo:categoryId='").append(id).append("')");
          isOr = true;
        }
      }
      if (!list.isEmpty())
        queryString.append("]");

      QueryManager qm = cateNode.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      Node nodeQs;

      while (iter.hasNext()) {
        nodeQs = iter.nextNode();
        if (nodeQs.getParent().getParent().isNodeType(EXO_FAQ_CATEGORY)) {
          entries.add(createQuestionEntry(sProvider, nodeQs));
        }
      }

      SyndFeed feed = createNewFeed(cateNode, "http://www.exoplatform.com");
      feed.setEntries(entries);

      SyndFeedOutput output = new SyndFeedOutput();
      String s = output.outputString(feed);
      s = StringUtils.replace(s, "&amp;", "&");
      s = s.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
      s = StringUtils.replace(s, "ST[CDATA[", "<![CDATA[");
      s = StringUtils.replace(s, "END]]", "]]>");

      return new ByteArrayInputStream(s.getBytes());
    } catch (Exception e) {
      log.error("Failed to create answer RSS ", e);
    }
    return null;
  }

  private List<String> getListCategoryIdPublic(SessionProvider sProvider, Node cateNode) throws Exception {
    List<String> list = new ArrayList<String>();

    StringBuilder queryString = new StringBuilder(JCR_ROOT).append(cateNode.getPath()).append("//element(*,exo:faqCategory)[@exo:isView='true' and ( not(@exo:userPrivate) or @exo:userPrivate='')]");

    QueryManager qm = cateNode.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator iter = result.getNodes();
    while (iter.hasNext()) {
      list.add(iter.nextNode().getName());
    }
    return list;
  }

  private SyndEntry createQuestionEntry(SessionProvider sProvider, Node questionNode) throws Exception {
    // Create new entry
    List<String> listContent = new ArrayList<String>();
    StringBuffer content = new StringBuffer();
    for (String answer : getStrAnswers(questionNode))
      content.append(answer);
    for (String comment : getComments(questionNode))
      content.append(comment);

    listContent.add(content.toString());
    SyndEntry entry = createNewEntry(questionNode, listContent);
    return entry;
  }

  private List<String> getStrAnswers(Node questionNode) throws Exception {
    List<String> listAnswers = new ArrayList<String>();
    try {
      Node answerHome = questionNode.getNode(Utils.ANSWER_HOME);
      QueryManager qm = answerHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(answerHome.getPath()).append("//element(*,exo:answer)[(@exo:approveResponses='true') and (@exo:activateResponses='true')]").append("order by @exo:dateResponse ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator nodeIterator = result.getNodes();
      Node answerNode = null;
      while (nodeIterator.hasNext()) {
        answerNode = nodeIterator.nextNode();
        if (answerNode.hasProperty(EXO_RESPONSES))
          listAnswers.add("<strong><u>Answer:</u></strong> " + (answerNode.getProperty(EXO_RESPONSES).getString()) + "<br/>");
      }
    } catch (Exception e) {
      log.error("Failed to get answers for " + questionNode.getName());
    }
    return listAnswers;
  }

  public Comment[] getComments(String questionId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    Node questionNode = getQuestionNode(sProvider, questionId);
    return getComment(questionNode);
  }

  private List<String> getComments(Node questionNode) throws Exception {
    List<String> listComment = new ArrayList<String>();
    try {
      Node commentHome = questionNode.getNode(Utils.COMMENT_HOME);
      QueryManager qm = commentHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer(JCR_ROOT).append(commentHome.getPath()).append("//element(*,exo:comment)").append(" order by @exo:dateComment ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator nodeIterator = result.getNodes();
      Node commentNode = null;
      while (nodeIterator.hasNext()) {
        commentNode = nodeIterator.nextNode();
        if (commentNode.hasProperty(EXO_COMMENTS))
          listComment.add("<strong><u>Comment:</u></strong>" + (commentNode.getProperty(EXO_COMMENTS).getString()) + "<br/>");
      }
    } catch (Exception e) {
      log.error("Failed to get comments for " + questionNode.getName());
    }
    return listComment;
  }

  private SyndFeed createNewFeed(Node node, String link) throws Exception {
    PropertyReader reader = new PropertyReader(node);
    String desc = reader.string(EXO_DESCRIPTION, " ");
    SyndFeed feed = new SyndFeedImpl();
    feed.setFeedType("rss_2.0");
    feed.setTitle("ST[CDATA[" + reader.string(EXO_NAME, "Root") + "END]]");
    feed.setPublishedDate(reader.date(EXO_CREATED_DATE, CommonUtils.getGreenwichMeanTime().getTime()));
    feed.setLink("ST[CDATA[" + link + "END]]");
    feed.setDescription("ST[CDATA[" + desc + "END]]");
    feed.setEncoding("UTF-8");
    return feed;
  }

  private SyndEntry createNewEntry(Node questionNode, List<String> listContent) throws Exception {
    PropertyReader question = new PropertyReader(questionNode);
    SyndContent description = new SyndContentImpl();
    description.setType("text/plain");
    description.setValue("ST[CDATA[" + question.string(EXO_TITLE, EMPTY_STR) + "<br/>" + (listContent.isEmpty() ? EMPTY_STR : listContent.get(0)) + "END]]");
    SyndEntry entry = new SyndEntryImpl();
    entry.setUri("ST[CDATA[" + questionNode.getName() + "END]]");
    entry.setTitle("ST[CDATA[" + question.string(EXO_TITLE) + "END]]");
    entry.setLink("ST[CDATA[" + question.string(EXO_LINK, "http://www.exoplatform.com") + "END]]");
    entry.setContributors(listContent);
    entry.setDescription(description);
    entry.setPublishedDate(question.date(EXO_CREATED_DATE, CommonUtils.getGreenwichMeanTime().getTime()));
    entry.setAuthor("ST[CDATA[" + question.string(EXO_AUTHOR) + "END]]");
    return entry;
  }

  protected Node getFAQServiceHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getFaqHomeLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
  }

  private Node getKSUserAvatarHomeNode(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getAvatarsLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
  }

  private Node getUserSettingHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getFaqUserSettingsLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
  }

  private Node getCategoryHome(SessionProvider sProvider, String username) throws Exception {
    String path = dataLocator.getFaqCategoriesLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
  }

  private Node getTemplateHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getFaqTemplatesLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
  }

  private String getLastActivityInfo(String author, long answerTime) {
    return new StringBuilder(author).append(Utils.HYPHEN).append(answerTime).toString();
  }
  
  private static void logDebug(String message, Throwable e) {
    if(log.isDebugEnabled()) {
      if(e != null) {
        log.debug(message, e);
      } else {
        log.debug(message);
      }
    }
  }

  protected static void logDebug(String message) {
    logDebug(message, null);
  }
}
