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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.CategoryInfo;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.DataStorage;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
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
import org.exoplatform.services.mail.MailService;
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
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *					hung.nguyen@exoplatform.com
 * Jul 10, 2007	
 */
public class JCRDataStorage implements DataStorage {

  private static final Log log = ExoLogger.getLogger(JCRDataStorage.class);
  
  public static final String      SEMICOLON            = ";";

  final private static String MIMETYPE_TEXTHTML = "text/html".intern() ;
  @SuppressWarnings("unused")
  private Map<String, String> serverConfig_ = new HashMap<String, String>();
  private Map<String, NotifyInfo> messagesInfoMap_ = new HashMap<String, NotifyInfo>() ;
  final Queue<NotifyInfo> pendingMessagesQueue = new ConcurrentLinkedQueue<NotifyInfo>();
  //	private Map<String, FAQRSSEventListener> rssListenerMap_ = new HashMap<String, FAQRSSEventListener> () ;
  private final String ADMIN_="ADMIN".intern();
  //	private final String FAQ_RSS = "ks.rss";
  private List<RoleRulesPlugin> rulesPlugins_ = new ArrayList<RoleRulesPlugin>() ;
  //	private boolean isInitRssListener_ = true ;
  private SessionManager sessionManager;
  private KSDataLocation dataLocator;

  public JCRDataStorage(KSDataLocation dataLocator) throws Exception {
    this.dataLocator = dataLocator;
    sessionManager = dataLocator.getSessionManager();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#addPlugin(org.exoplatform.container.component.ComponentPlugin)
   */
  public void addPlugin(ComponentPlugin plugin) throws Exception {
    try{
      serverConfig_ = ((EmailNotifyPlugin)plugin).getServerConfiguration() ;
    }catch(Exception e) {
      log.error("\nFailed to add plugin\n ", e);
    }

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#addRolePlugin(org.exoplatform.container.component.ComponentPlugin)
   */
  public void addRolePlugin(ComponentPlugin plugin) throws Exception {
    try {
      if(plugin instanceof RoleRulesPlugin){
        rulesPlugins_.add((RoleRulesPlugin)plugin) ;
      }
    } catch (Exception e) {
      log.error("Failed to add role plugin\n", e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#addInitRssPlugin(org.exoplatform.container.component.ComponentPlugin)
   */
  public void addInitRssPlugin(ComponentPlugin plugin) throws Exception {
    /*if(plugin instanceof InitialRSSListener) {
			isInitRssListener_	= ((InitialRSSListener)plugin).isInitRssListener() ;
		}*/		
  }

  public boolean isAdminRole(String userName) throws Exception {
    String name = userName;
    if (name == null) {
      name = ConversationState.getCurrent().getIdentity().getUserId();
    }
    SessionProvider sProvider = CommonUtils.createSystemProvider();		
    try {
      Node cateHomeNode = getCategoryHome(sProvider, null);
      for(int i = 0; i < rulesPlugins_.size(); ++i) {
        List<String> list = new ArrayList<String>();
        list.addAll(rulesPlugins_.get(i).getRules(this.ADMIN_));				
        if(cateHomeNode.hasProperty("exo:moderators")) 
          list.addAll(Utils.valuesToList(cateHomeNode.getProperty("exo:moderators").getValues())) ;
        if(list.contains(name)) return true;
        if(Utils.hasPermission(list, UserHelper.getAllGroupAndMembershipOfUser(userName))) return true;
      }
    } catch (Exception e) {
      log.debug("Check user whether is admin: ", e);
    }
    return false ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getAllFAQAdmin()
   */
  public List<String> getAllFAQAdmin() throws Exception {
    List<String> list = new ArrayList<String>();
    try {
      for(int i = 0; i < rulesPlugins_.size(); ++i) {
        list.addAll(rulesPlugins_.get(i).getRules(this.ADMIN_));
      }
      list =	FAQServiceUtils.getUserPermission(list.toArray(new String[]{}));
    } catch (Exception e) {
      log.error("Failed to get all FAQ admin: ", e);
    }
    return list;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getUserSetting(java.lang.String, org.exoplatform.faq.service.FAQSetting)
   */
  public void getUserSetting(String userName, FAQSetting faqSetting) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node userSettingHome = getUserSettingHome(sProvider) ;
      Node userSettingNode = userSettingHome.getNode(userName) ;
      if(userSettingNode.hasProperty("exo:ordeBy")) faqSetting.setOrderBy(userSettingNode.getProperty("exo:ordeBy").getValue().getString());
      if(userSettingNode.hasProperty("exo:ordeType")) faqSetting.setOrderType(userSettingNode.getProperty("exo:ordeType").getValue().getString());
      if(userSettingNode.hasProperty("exo:sortQuestionByVote")) faqSetting.setSortQuestionByVote(userSettingNode.getProperty("exo:sortQuestionByVote").getValue().getBoolean());
    }catch (Exception e) {
      saveFAQSetting(faqSetting, userName);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#saveFAQSetting(org.exoplatform.faq.service.FAQSetting, java.lang.String)
   */
  public void saveFAQSetting(FAQSetting faqSetting,String userName) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{			
      Node userSettingNode = getUserSettingHome(sProvider).getNode(userName);
      userSettingNode.setProperty("exo:ordeBy", faqSetting.getOrderBy());
      userSettingNode.setProperty("exo:ordeType", faqSetting.getOrderType());
      userSettingNode.setProperty("exo:sortQuestionByVote", faqSetting.isSortQuestionByVote());
      userSettingNode.save() ;
    }catch ( PathNotFoundException e) {
      Node userSettingNode = getUserSettingHome(sProvider).addNode(userName, "exo:faqUserSetting") ;
      userSettingNode.setProperty("exo:ordeBy", faqSetting.getOrderBy());
      userSettingNode.setProperty("exo:ordeType", faqSetting.getOrderType());
      userSettingNode.setProperty("exo:sortQuestionByVote", faqSetting.isSortQuestionByVote());
      userSettingNode.getSession().save() ;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getUserAvatar(java.lang.String)
   */
  public FileAttachment getUserAvatar(String userName) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node node = getKSUserAvatarHomeNode(sProvider).getNode(userName);
      if(node.isNodeType("nt:file")) {
        FileAttachment attachment = new FileAttachment() ;
        Node nodeFile = node.getNode("jcr:content") ;
        attachment.setId(node.getPath());
        attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
        attachment.setNodeName(node.getName());
        attachment.setName("avatar." + attachment.getMimeType());
        String workspace = node.getSession().getWorkspace().getName() ;
        attachment.setWorkspace(workspace) ;
        attachment.setPath("/" + workspace + node.getPath()) ;
        attachment.setSize(nodeFile.getProperty("jcr:data").getStream().available());
        return attachment ;	
      }			
    }catch (PathNotFoundException e) {
      return null;
    }catch(Exception e){
      log.error("Failed to get user avatar", e);
    }
    return null ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#saveUserAvatar(java.lang.String, org.exoplatform.faq.service.FileAttachment)
   */
  public void saveUserAvatar(String userId, FileAttachment fileAttachment) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node ksAvatarHomeNode = getKSUserAvatarHomeNode(sProvider);
      Node avatarNode ;
      if(ksAvatarHomeNode.hasNode(userId)) avatarNode = ksAvatarHomeNode.getNode(userId);
      else avatarNode = ksAvatarHomeNode.addNode(userId, "nt:file");
      FAQServiceUtils.reparePermissions(avatarNode, "any");
      Node nodeContent ;
      if (avatarNode.hasNode("jcr:content")) nodeContent = avatarNode.getNode("jcr:content");
      else	nodeContent = avatarNode.addNode("jcr:content", "nt:resource") ;
      nodeContent.setProperty("jcr:mimeType", fileAttachment.getMimeType());
      nodeContent.setProperty("jcr:data", fileAttachment.getInputStream());
      nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
      if(avatarNode.isNew()) ksAvatarHomeNode.getSession().save();
      else ksAvatarHomeNode.save();
    }catch (Exception e) {
      log.error("Failed to save user avatar: ", e);
    }	
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#setDefaultAvatar(java.lang.String)
   */
  public void setDefaultAvatar(String userName)throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node avatarHome = getKSUserAvatarHomeNode(sProvider);
      if(avatarHome.hasNode(userName)){
        Node node = avatarHome.getNode(userName);
        if(node.isNodeType("nt:file")) {
          node.remove();
          avatarHome.save();
        }
      }
    }catch (Exception e) {
      log.error("Failed to set default avatar: ", e);
    }
  }



  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getQuestionsIterator()
   */
  public NodeIterator getQuestionsIterator() throws Exception {
		SessionProvider sProvider = CommonUtils.createSystemProvider();
		try {
			Node faqHome = getFAQServiceHome(sProvider);
			return getQuestionsIterator(faqHome, "", true);
		} catch (Exception e) {
			log.error("Failed to get question iterator: ", e);
		}
		return null;
	}

	private NodeIterator getQuestionsIterator(Node parentNode, String strQuery, boolean isAll) throws Exception {
		StringBuffer queryString = new StringBuffer("/jcr:root").append(parentNode.getPath()).append((isAll)?"//":"/").append("element(*,exo:faqQuestion)").append(strQuery);
		QueryManager qm = parentNode.getSession().getWorkspace().getQueryManager();
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		return result.getNodes();
	}
  
  //	TODO: remove RSS Listener
  protected void addRSSListener(Node node) throws Exception{
    //		try{
    //			if(!isInitRssListener_)return;
    //			if(rssListenerMap_.containsKey(node.getPath())) return ;
    //			String path = node.getPath() ;
    //			ObservationManager observation = node.getSession().getWorkspace().getObservationManager() ;
    //			FAQRSSEventListener questionRSS = new FAQRSSEventListener(dataLocator) ;
    //			questionRSS.setPath(path) ;
    //			observation.addEventListener(questionRSS, Event.NODE_ADDED & Event.PROPERTY_CHANGED & Event.NODE_REMOVED,
    //																	 path, true, null, null, false) ;
    //			rssListenerMap_.put(path, questionRSS) ;
    //		}catch(Exception e) {
    //			log.error("Fail to listen when add RSS: ", e);
    //		}
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#reInitRSSEvenListener()
   */
  //	remove
  public void reInitRSSEvenListener() throws Exception{
    //		if(!isInitRssListener_)return;
    //		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
    //		Node faqHome = getFAQServiceHome(sProvider) ;
    //		QueryManager qm = faqHome.getSession().getWorkspace().getQueryManager();
    //		StringBuffer queryString = new StringBuffer("/jcr:root").append(faqHome.getPath()).append("//element(*,exo:faqQuestionHome)") ;
    //		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    //		QueryResult result = query.execute();
    //		NodeIterator iter = result.getNodes() ;
    //		rssListenerMap_.clear() ;
    //		while(iter.hasNext()) {
    //			addRSSListener(iter.nextNode()) ;			
    //		}		
  }


  public void reInitQuestionNodeListeners() throws Exception {
    NodeIterator iter = getQuestionsIterator();
    if (iter == null) return;
    while (iter.hasNext()) {
      Node quesNode = iter.nextNode();
      registerQuestionNodeListener(quesNode);
    }
  }


  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#initRootCategory()
   */
  public boolean initRootCategory() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqServiceHome = getFAQServiceHome(sProvider) ;
      if (faqServiceHome.hasNode(Utils.CATEGORY_HOME)) {
        if (log.isDebugEnabled()) {
          log.debug("root category is already created");
        }
        return false;
      }			
      Node categoryHome = faqServiceHome.addNode(Utils.CATEGORY_HOME, "exo:faqCategory") ;
      categoryHome.addMixin("mix:faqSubCategory") ;
      categoryHome.setProperty("exo:name", "Answers") ;
      categoryHome.setProperty("exo:isView", true);
      faqServiceHome.save() ;	
      log.info("Initialized root category : " + categoryHome.getPath());
      return true;
    }catch (Exception e) {
      log.error("Could not initialize root category", e);
      return false;
    }

  }



  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getTemplate()
   */
  public byte[] getTemplate() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node templateHome = getTemplateHome(sProvider);
      Node fileNode = templateHome.getNode(Utils.UI_FAQ_VIEWER);
      if (fileNode.isNodeType("nt:file")) {
        Node contentNode = fileNode.getNode("jcr:content");
        InputStream inputStream = contentNode.getProperty("jcr:data").getStream();
        byte[] data = new byte[inputStream.available()];
        inputStream.read(data) ;
        inputStream.close();
        return data;
      }
    } catch (Exception e) {
      log.error("Failed to get template", e);
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#saveTemplate(java.lang.String)
   */
  public void saveTemplate(String str) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node templateHome = getTemplateHome(sProvider);
      Node fileNode ;
      try {
        fileNode = templateHome.getNode(Utils.UI_FAQ_VIEWER);
      } catch (Exception e) {
        fileNode = templateHome.addNode(Utils.UI_FAQ_VIEWER,"nt:file");
      }
      Node nodeContent = null;
      InputStream inputStream = null;
      byte []byte_ = str.getBytes();
      inputStream = new ByteArrayInputStream(byte_);
      try {
        nodeContent = fileNode.addNode("jcr:content", "nt:resource");
      } catch (Exception e) {
        nodeContent = fileNode.getNode("jcr:content");
      }
      nodeContent.setProperty("jcr:mimeType", "application/x-groovy+html");
      nodeContent.setProperty("jcr:data", inputStream);
      nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
      if(templateHome.isNew()){
        templateHome.getSession().save();
      } else {
        templateHome.save();
      }
    } catch (Exception e) {
      log.error("Failed to save template: ", e);
    }
  }

  protected Value[] booleanToValues(Node node, Boolean[] bools) throws Exception{
    if(bools == null) return new Value[]{node.getSession().getValueFactory().createValue(true)};
    Value[] values = new Value[bools.length]; 
    for(int i = 0; i < values.length; i ++){
      values[i] = node.getSession().getValueFactory().createValue(bools[i]);
    }
    return values;
  }

  private boolean questionHasAnswer(Node questionNode) throws Exception {
    if(questionNode.hasNode(Utils.ANSWER_HOME) && questionNode.getNode(Utils.ANSWER_HOME).hasNodes()) return true;
    return false;
  }

  /**
   * @deprecated use {@link JCRDataStorage#sendNotifyWatcher(Question question, FAQSetting faqSetting, boolean isNew)}
   */
	@SuppressWarnings("unused")
	@Deprecated
  private void sendNotifyForQuestionWatcher (Question question, FAQSetting faqSetting) {
    List<String> emailsList = new ArrayList<String>() ;
    emailsList.add(question.getEmail()) ;
    try {
      Node cate = getCategoryNodeById(question.getCategoryId()) ;
      if(cate.isNodeType("exo:faqWatching")){
        for(String email: org.exoplatform.ks.common.Utils.ValuesToList(cate.getProperty("exo:emailWatching").getValues())) {
          emailsList.add(email) ;
        }
      }
      if(question.getEmailsWatch() != null) {
        for(String email: question.getEmailsWatch()) {
          emailsList.add(email) ;				
        }
      }
      if(emailsList != null && emailsList.size() > 0) {
        Message message = new Message();
        message.setMimeType(MIMETYPE_TEXTHTML) ;
        message.setFrom(question.getAuthor());
        message.setSubject(faqSetting.getEmailSettingSubject() + ": " + question.getQuestion());
        String body = StringUtils.replace(faqSetting.getEmailSettingContent(), "&questionContent_", question.getDetail());
        body = StringUtils.replace(body, "&questionLink_", question.getLink());
        if(question.getAnswers() != null && question.getAnswers().length > 0) {
          body = StringUtils.replace(body, "&questionResponse_", question.getAnswers()[0].getResponses());
        } else {
          body = StringUtils.replace(body, "&questionResponse_", "");
        }
        message.setBody(body);
        sendEmailNotification(emailsList, message) ;
      }
    } catch(Exception e) {
      log.error("Failed to send a notify for question watcher: ", e);
    }
  }
  
	private void sendNotifyWatcher(Question question, FAQSetting faqSetting, boolean isNew) {
		// Send notification when add new question in watching category
		List<String> emails = new ArrayList<String>();
		List<String> users = new ArrayList<String>();
		List<String> emailsList = new ArrayList<String>();
		emailsList.add(question.getEmail());
		try {
			Node cate = getCategoryNodeById(question.getCategoryId());
			PropertyReader reader = new PropertyReader(cate);
			// watch in category parent
			emails.addAll(reader.list("exo:emailWatching", new ArrayList<String>()));
			users.addAll(reader.list("exo:userWatching", new ArrayList<String>()));
			// watch in this question
			if (question.getEmailsWatch() != null && question.getEmailsWatch().length > 0) {
				emails.addAll(Arrays.asList(question.getEmailsWatch()));
				users.addAll(Arrays.asList(question.getUsersWatch()));
			}
			if (!question.isActivated() || (!question.isApproved() && faqSetting.getDisplayMode().equals("approved"))) {
				// only send notification to administrations or moderators
				List<String> moderators = reader.list("exo:moderators", new ArrayList<String>());
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
			if (emailsList != null && emailsList.size() > 0) {
				Message message = new Message();
				message.setFrom(question.getAuthor());
				message.setMimeType(MIMETYPE_TEXTHTML);
				message.setSubject(faqSetting.getEmailSettingSubject() + ": " + question.getQuestion());
				String body = faqSetting.getEmailSettingContent();
				if (isNew) {
				  body = StringUtils.replace(body, "&questionContent_", question.getDetail());
				  body = StringUtils.replace(body, "&categoryName_", reader.string("exo:name", ""));
				} else {
				  body = StringUtils.replace(body, "&questionContent_", question.getQuestion());
					if (question.getAnswers().length > 0) {
					  body = StringUtils.replace(body, "&questionResponse_", question.getAnswers()[0].getResponses());
					} else {
					  body = StringUtils.replace(body, "&questionResponse_", "");
					}
				}
				body = StringUtils.replace(body, "&questionLink_", question.getLink());
				message.setBody(body);
				sendEmailNotification(emailsList, message);
			}
		} catch (Exception e) {
			log.error("Failed to send a nofify for category watcher: ", e);
		}
	}

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#sendMessage(org.exoplatform.services.mail.Message)
   */
  public void sendMessage(Message message) throws Exception {
    try{
      MailService mService = (MailService)PortalContainer.getComponent(MailService.class) ;
      mService.sendMessage(message) ;		
    }catch(NullPointerException e) {
      MailService mService = (MailService)StandaloneContainer.getInstance().getComponentInstanceOfType(MailService.class) ;
      mService.sendMessage(message) ;		
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getQuestionLanguages(java.lang.String)
   */
  public List<QuestionLanguage> getQuestionLanguages(String questionId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId) ;
      try {
        listQuestionLanguage.add(getQuestionLanguage(questionNode)) ;
      }catch (Exception e){log.debug("Adding a question node failed: ", e);}
      if(questionNode.hasNode(Utils.LANGUAGE_HOME)) {
        Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME) ;
        NodeIterator nodeIterator = languageNode.getNodes() ;
        while(nodeIterator.hasNext()) {
          try {
            listQuestionLanguage.add(getQuestionLanguage(nodeIterator.nextNode())) ;
          }catch (Exception e){}					
        }
      }
    }catch (Exception e){
      log.error("Failed to get question language: ", e);
    }
    return listQuestionLanguage ;
  }

  private QuestionLanguage getQuestionLanguage(Node questionNode) throws Exception{
    QuestionLanguage questionLanguage = new QuestionLanguage() ;
    questionLanguage.setState(QuestionLanguage.VIEW) ;
    questionLanguage.setId(questionNode.getName()) ;
    questionLanguage.setLanguage(questionNode.getProperty("exo:language").getValue().getString());
    questionLanguage.setQuestion(questionNode.getProperty("exo:title").getValue().getString());
    if(questionNode.hasProperty("exo:name")) questionLanguage.setDetail(questionNode.getProperty("exo:name").getValue().getString());
    Comment[] comments = getComment(questionNode);
    Answer[] answers = getAnswers(questionNode);
    questionLanguage.setComments(comments);
    questionLanguage.setAnswers(answers);
    return questionLanguage ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#deleteAnswer(java.lang.String, java.lang.String)
   */
  public void deleteAnswer(String questionId, String answerId) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
      Node answerNode = questionNode.getNode(Utils.ANSWER_HOME).getNode(answerId);
      answerNode.remove();
      questionNode.save();
    }catch (Exception e) {
      log.error("Failed to delete a answer: ", e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#deleteComment(java.lang.String, java.lang.String)
   */
  public void deleteComment(String questionId, String commentId) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
      Node commnetNode = questionNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
      commnetNode.remove();
      questionNode.save();
    }catch (Exception e) {
      log.error("Failed to delete a commnent: ", e);
    }
  }

  private Answer[] getAnswers(Node questionNode) throws Exception{
    try{
      if(!questionNode.hasNode(Utils.ANSWER_HOME)) return new Answer[]{};
      NodeIterator nodeIterator = questionNode.getNode(Utils.ANSWER_HOME).getNodes();
      List<Answer> answers = new ArrayList<Answer>();
      Answer ans;
      String language = questionNode.getProperty("exo:language").getString() ;
      while(nodeIterator.hasNext()){
        try{
          Node node = nodeIterator.nextNode();
          ans = getAnswerByNode(node);
          ans.setLanguage(language) ;
          answers.add(ans);
        }catch (Exception e) {
          log.error("Failed to get anwser", e);
        }				
      }
      return answers.toArray(new Answer[]{});
    } catch (Exception e){
      log.error("Failed to get answer: ", e);
    }
    return new Answer[]{};
  }

  private Answer getAnswerByNode(Node answerNode) throws Exception {
    Answer answer = new Answer();
    answer.setId(answerNode.getName()) ;
    if(answerNode.hasProperty("exo:responses")) answer.setResponses((answerNode.getProperty("exo:responses").getValue().getString())) ;
    if(answerNode.hasProperty("exo:responseBy")) answer.setResponseBy((answerNode.getProperty("exo:responseBy").getValue().getString())) ;		
    if(answerNode.hasProperty("exo:fullName")) answer.setFullName((answerNode.getProperty("exo:fullName").getValue().getString())) ;		
    if(answerNode.hasProperty("exo:dateResponse")) answer.setDateResponse((answerNode.getProperty("exo:dateResponse").getValue().getDate().getTime())) ;
    if(answerNode.hasProperty("exo:usersVoteAnswer")) answer.setUsersVoteAnswer(Utils.valuesToArray(answerNode.getProperty("exo:usersVoteAnswer").getValues())) ;
    if(answerNode.hasProperty("exo:MarkVotes")) answer.setMarkVotes(answerNode.getProperty("exo:MarkVotes").getValue().getLong()) ;
    if(answerNode.hasProperty("exo:approveResponses")) answer.setApprovedAnswers(answerNode.getProperty("exo:approveResponses").getValue().getBoolean()) ;
    if(answerNode.hasProperty("exo:activateResponses")) answer.setActivateAnswers(answerNode.getProperty("exo:activateResponses").getValue().getBoolean()) ;
    if(answerNode.hasProperty("exo:postId")) answer.setPostId(answerNode.getProperty("exo:postId").getString()) ;
    String path = answerNode.getPath() ;
    answer.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
    return answer;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getPageListAnswer(java.lang.String, java.lang.Boolean)
   */
  public JCRPageList getPageListAnswer(String questionId, Boolean isSortByVote) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node answerHome = getFAQServiceHome(sProvider).getNode(questionId + "/" + Utils.ANSWER_HOME);
      QueryManager qm = answerHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(answerHome.getPath()). 
      append("//element(*,exo:answer)");
      if(isSortByVote == null) queryString.append("order by @exo:dateResponse ascending");
      else if(isSortByVote) queryString.append("order by @exo:MarkVotes ascending");
      else	queryString.append("order by @exo:MarkVotes descending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
      return pageList;
    } catch (Exception e) {
      log.error("Failed to get page list answers", e);
      return null;
    }
  }


  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#saveAnswer(java.lang.String, org.exoplatform.faq.service.Answer, boolean)
   */
  public void saveAnswer(String questionId, Answer answer, boolean isNew) throws Exception{
    Answer[] answers = {answer} ;
    saveAnswer(questionId, answers) ;		
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#saveAnswer(java.lang.String, org.exoplatform.faq.service.Answer[])
   */
  public void saveAnswer(String questionId, Answer[] answers) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node quesNode = getFAQServiceHome(sProvider).getNode(questionId);
      if(!quesNode.isNodeType("mix:faqi18n")) {
        quesNode.addMixin("mix:faqi18n") ;
      }
      //			String lastActivityInfo = null;
      //			if (quesNode.hasProperty("exo:lastActivity")) 
      //				lastActivityInfo = quesNode.getProperty("exo:lastActivity").getString();
      //			long timeOfLastActivity = Utils.getTimeOfLastActivity(lastActivityInfo);
      Node answerHome;
      String qId = quesNode.getName() ;
      String categoryId = quesNode.getProperty("exo:categoryId").getString() ;
      String defaultLang = quesNode.getProperty("exo:language").getString() ;

      for(Answer answer : answers){

        if(answer.getLanguage().equals(defaultLang)){
          try{
            answerHome = quesNode.getNode(Utils.ANSWER_HOME);
          } catch (Exception e){
            answerHome = quesNode.addNode(Utils.ANSWER_HOME, "exo:answerHome") ;
          }
        }else { //answer for other languages
          Node langNode = getLanguageNodeByLanguage(quesNode, answer.getLanguage()) ;
          try{
            answerHome = langNode.getNode(Utils.ANSWER_HOME);
          } catch (Exception e){
            answerHome = langNode.addNode(Utils.ANSWER_HOME, "exo:answerHome") ;
          }					
        }
        saveAnswer(answer, answerHome, qId, categoryId) ;
        //				if (answer.getApprovedAnswers() || answer.getActivateAnswers()) {
        //					long answerTime = answer.getDateResponse().getTime();
        //					if (answerTime > timeOfLastActivity) {
        //						timeOfLastActivity = answerTime;
        //						lastActivityInfo = answer.getResponseBy() + "-" + timeOfLastActivity;
        //					}
        //				}
      }
      //			if (lastActivityInfo != null)
      //				quesNode.setProperty("exo:lastActivity", lastActivityInfo);
      quesNode.save() ;
    }catch (Exception e) {
      log.error("Failed to save answer: ", e);
    }
  }

  private void saveAnswer(Answer answer, Node answerHome, String questionId, String categoryId) throws Exception {
    Node answerNode;
    try{
      answerNode = answerHome.getNode(answer.getId());
    } catch(PathNotFoundException e) {
      answerNode = answerHome.addNode(answer.getId(), "exo:answer");
    }
    if(!answer.isNew()) { //remove answer
      answerNode.remove() ;
      return ;
    }
    try {
      if(answerNode.isNew()){
        java.util.Calendar calendar = GregorianCalendar.getInstance();					
        if(answer.getDateResponse() != null) calendar.setTime(answer.getDateResponse());
        answerNode.setProperty("exo:dateResponse", calendar) ;
        answerNode.setProperty("exo:id", answer.getId());
        answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers()) ;
        answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers()) ;
      } else {
      	if(new PropertyReader(answerNode).bool("exo:approveResponses", false) != answer.getApprovedAnswers())
      		answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers()) ;
      	if(new PropertyReader(answerNode).bool("exo:activateResponses", false) != answer.getActivateAnswers())
      		answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers()) ;
      }
      if(answer.getPostId() != null && answer.getPostId().length() > 0) {
        answerNode.setProperty("exo:postId", answer.getPostId());
      }
      answerNode.setProperty("exo:responses", answer.getResponses()) ;
      answerNode.setProperty("exo:responseBy", answer.getResponseBy()) ;
      answerNode.setProperty("exo:fullName", answer.getFullName());
      answerNode.setProperty("exo:usersVoteAnswer", answer.getUsersVoteAnswer()) ;
      answerNode.setProperty("exo:MarkVotes", answer.getMarkVotes()) ;
      answerNode.setProperty("exo:responseLanguage", answer.getLanguage()) ;
      answerNode.setProperty("exo:questionId", questionId) ;
      answerNode.setProperty("exo:categoryId", categoryId) ;								
    }catch (Exception e) {
      log.error("Failed to save Answer: ", e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#saveComment(java.lang.String, org.exoplatform.faq.service.Comment, boolean)
   */
  public void saveComment(String questionId, Comment comment, boolean isNew) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node quesNode = getFAQServiceHome(sProvider).getNode(questionId);
      if(!quesNode.isNodeType("mix:faqi18n")) {
        quesNode.addMixin("mix:faqi18n") ;
      }

      //			String lastActInfo = null;
      //			if (quesNode.hasProperty("exo:lastActivity"))
      //				lastActInfo = quesNode.getProperty("exo:lastActivity").getString();
      //			long timeOfLastAct = Utils.getTimeOfLastActivity(lastActInfo);

      Node commentHome = null;
      try{
        commentHome = quesNode.getNode(Utils.COMMENT_HOME);
      } catch (PathNotFoundException e){
        commentHome = quesNode.addNode(Utils.COMMENT_HOME, "exo:commentHome");
      }
      Node commentNode;
      if(isNew){
        commentNode = commentHome.addNode(comment.getId(), "exo:comment");
        java.util.Calendar calendar = GregorianCalendar.getInstance();
        commentNode.setProperty("exo:dateComment", calendar);
        commentNode.setProperty("exo:id", comment.getId());				
      } else {
        commentNode = commentHome.getNode(comment.getId());
      }

      if(comment.getPostId() != null && comment.getPostId().length() > 0) {
        commentNode.setProperty("exo:postId", comment.getPostId());
      }
      commentNode.setProperty("exo:comments", comment.getComments()) ;
      commentNode.setProperty("exo:commentBy", comment.getCommentBy()) ;
      commentNode.setProperty("exo:fullName", comment.getFullName());
      commentNode.setProperty("exo:categoryId", quesNode.getProperty("exo:categoryId").getString());
      commentNode.setProperty("exo:questionId", quesNode.getName());
      commentNode.setProperty("exo:commentLanguage", quesNode.getProperty("exo:language").getString());

      //			long commentTime = comment.getDateComment().getTime();
      //			if (commentTime > timeOfLastAct) {
      //				timeOfLastAct = commentTime;
      //				lastActInfo = comment.getCommentBy() + "-" + timeOfLastAct;
      //				quesNode.setProperty("exo:lastActivity", lastActInfo);
      //			}

      if(commentNode.isNew()) quesNode.getSession().save();
      else quesNode.save();
    }catch(Exception e) {
      log.error("Failed to save comment: ", e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#saveAnswerQuestionLang(java.lang.String, org.exoplatform.faq.service.Answer, java.lang.String, boolean)
   */
  public void saveAnswerQuestionLang(String questionId, Answer answer, String language, boolean isNew) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node quesNode = getFAQServiceHome(sProvider).getNode(questionId);
      Node answerHome = null;
      try{
        answerHome = quesNode.getNode(Utils.ANSWER_HOME);
      } catch (PathNotFoundException e){
        answerHome = quesNode.addNode(Utils.ANSWER_HOME, "exo:answerHome");
      }
      Node answerNode;
      if(isNew){
        answerNode = answerHome.addNode(answer.getId(), "exo:answer");
        java.util.Calendar calendar = GregorianCalendar.getInstance();
        //calendar.setTime(answer.getDateResponse());
        answerNode.setProperty("exo:dateResponses", calendar);
        answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers()) ;
        answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers()) ;
      } else {
        answerNode = answerHome.getNode(answer.getId());
        if(new PropertyReader(answerNode).bool("exo:approveResponses", false) != answer.getApprovedAnswers())
      		answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers()) ;
      	if(new PropertyReader(answerNode).bool("exo:activateResponses", false) != answer.getActivateAnswers())
      		answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers()) ;
      }
      answerNode.setProperty("exo:responses", answer.getResponses()) ;
      answerNode.setProperty("exo:responseBy", answer.getResponseBy()) ;
      answerNode.setProperty("exo:fullName", answer.getFullName());
      answerNode.setProperty("exo:usersVoteAnswer", answer.getUsersVoteAnswer()) ;
    }catch (Exception e) {
      log.error("Failed to save answer question language: ", e);
    }
  }

  /*public void saveCommentQuestionLang(String questionId, Comment comment, String language, boolean isNew) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node quesNode = getFAQServiceHome(sProvider).getNode(questionId);
			Node commentHome = null;
			try{
				commentHome = quesNode.getNode(Utils.COMMENT_HOME);
			} catch (PathNotFoundException e){
				commentHome = quesNode.addNode(Utils.COMMENT_HOME, "exo:commentHome");
			}
			Node commentNode;
			if(isNew){
				commentNode = commentHome.addNode(comment.getId(), "exo:comment");
				java.util.Calendar calendar = GregorianCalendar.getInstance();
				//calendar.setTime(comment.getDateComment());
				commentNode.setProperty("exo:dateComment", calendar);
			} else {
				commentNode = commentHome.getNode(comment.getId());
			}
			commentNode.setProperty("exo:comments", comment.getComments()) ;
			commentNode.setProperty("exo:commentBy", comment.getCommentBy()) ;
			commentNode.setProperty("exo:fullName", comment.getFullName());
			commentNode.setProperty("exo:categoryId", quesNode.getProperty("exo:categoryId").getString());
			commentNode.setProperty("exo:questionId", quesNode.getName());
			if(commentNode.isNew()) quesNode.getSession().save();
			else quesNode.save();
		}catch (Exception e) {
		}finally { sProvider.close() ;}

	}*/

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getAnswerById(java.lang.String, java.lang.String)
   */
  public Answer getAnswerById(String questionId, String answerid) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node answerNode = getFAQServiceHome(sProvider).getNode(questionId).getNode(Utils.ANSWER_HOME).getNode(answerid);
      return getAnswerByNode(answerNode);
    } catch (Exception e){
      log.error("Failed to get answer by id.", e);
    }
    return null;	
  }

  public Comment[] getComments(String questionId) throws Exception {
    Node questionNode = getQuestionNodeById(questionId);
    return getComment(questionNode);
    
  }
  
  private Comment[] getComment(Node questionNode) throws Exception{
    try{
      if(!questionNode.hasNode(Utils.COMMENT_HOME)) return new Comment[]{};
      NodeIterator nodeIterator = questionNode.getNode(Utils.COMMENT_HOME).getNodes();
      Comment[] comments = new Comment[(int) nodeIterator.getSize()];
      Node commentNode = null;
      int i = 0;
      while(nodeIterator.hasNext()){
        commentNode = nodeIterator.nextNode();
        comments[i] = getCommentByNode(commentNode);
        i ++;
      }
      return comments;
    } catch (Exception e){
      log.error("Failed to get comment: ", e);
      return new Comment[]{};
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getPageListComment(java.lang.String)
   */
  public JCRPageList getPageListComment(String questionId) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node commentHome = getFAQServiceHome(sProvider).getNode(questionId + "/" + Utils.COMMENT_HOME) ;
      QueryManager qm = commentHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(commentHome.getPath()). 
      append("//element(*,exo:comment)").append("order by @exo:dateComment ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
      return pageList;
    } catch (Exception e) {
      log.error("Failed to get page list comments", e);
      return null;
    }
  }

  private Comment getCommentByNode(Node commentNode) throws Exception {
    Comment comment = new Comment();
    comment.setId(commentNode.getName()) ;
    if(commentNode.hasProperty("exo:comments")) comment.setComments((commentNode.getProperty("exo:comments").getValue().getString())) ;
    if(commentNode.hasProperty("exo:commentBy")) comment.setCommentBy((commentNode.getProperty("exo:commentBy").getValue().getString())) ;		
    if(commentNode.hasProperty("exo:dateComment")) comment.setDateComment((commentNode.getProperty("exo:dateComment").getValue().getDate().getTime())) ;
    if(commentNode.hasProperty("exo:fullName")) comment.setFullName((commentNode.getProperty("exo:fullName").getValue().getString())) ;
    if(commentNode.hasProperty("exo:postId")) comment.setPostId(commentNode.getProperty("exo:postId").getString()) ;
    return comment;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getCommentById(java.lang.String, java.lang.String)
   */
  public Comment getCommentById(String questionId, String commentId) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node commentNode =	getFAQServiceHome(sProvider).getNode(questionId + "/" + Utils.COMMENT_HOME + "/" + commentId);
      return getCommentByNode(commentNode);
    } catch (Exception e){
      log.error("Failed to get comment by id: "+commentId, e);
      return null;
    }
  }

  private Node getLanguageNodeByLanguage(Node questionNode, String languge) throws Exception{
    NodeIterator nodeIterator = questionNode.getNode(Utils.LANGUAGE_HOME).getNodes();
    Node languageNode = null;
    while(nodeIterator.hasNext()){
      languageNode = nodeIterator.nextNode();
      if(languageNode.getProperty("exo:language").getString().equals(languge)) return languageNode;
    }
    return null;
  }


  @SuppressWarnings("static-access")
  private void saveQuestion(Node questionNode, Question question, boolean isNew, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
    //boolean isMoveQuestion = false;		
    questionNode.setProperty("exo:id", questionNode.getName()) ;
    questionNode.setProperty("exo:name", question.getDetail()) ;
    questionNode.setProperty("exo:author", question.getAuthor()) ;
    questionNode.setProperty("exo:email", question.getEmail()) ;
    questionNode.setProperty("exo:title", question.getQuestion()) ;
    if(isNew){
      GregorianCalendar cal = new GregorianCalendar() ;
      cal.setTime(question.getCreatedDate()) ;
      questionNode.setProperty("exo:createdDate", cal) ;
      questionNode.setProperty("exo:language", question.getLanguage()) ;
    } 
    String catId = question.getCategoryId() ;
    if(!question.getCategoryId().equals(Utils.CATEGORY_HOME)) {
      catId = catId.substring(catId.lastIndexOf("/") + 1) ;
    }
    questionNode.setProperty("exo:categoryId", catId) ;
    questionNode.setProperty("exo:isActivated", question.isActivated()) ;
    questionNode.setProperty("exo:isApproved", question.isApproved()) ;
    //		TODO: not need to save
    //		questionNode.setProperty("exo:relatives", question.getRelations()) ;
    questionNode.setProperty("exo:usersVote", question.getUsersVote()) ;
    questionNode.setProperty("exo:markVote", question.getMarkVote()) ;
    questionNode.setProperty("exo:link", question.getLink()) ;
    List<FileAttachment> listFileAtt = question.getAttachMent() ;

    List<String> listNodeNames = new ArrayList<String>() ;
    if(!listFileAtt.isEmpty()) {
      for(FileAttachment att : listFileAtt) {
        listNodeNames.add(att.getNodeName()) ;
        try {
          Node nodeFile = null;
          if (questionNode.hasNode(att.getNodeName())) nodeFile = questionNode.getNode(att.getNodeName());
          else nodeFile = questionNode.addNode(att.getNodeName(), "exo:faqAttachment");
          // fix permission to download file in ie 6:
          FAQServiceUtils.reparePermissions(nodeFile, "any");
          Node nodeContent = null;
          if (nodeFile.hasNode("jcr:content")) nodeContent = nodeFile.getNode("jcr:content");
          else	nodeContent = nodeFile.addNode("jcr:content", "exo:faqResource") ;
          nodeContent.setProperty("exo:fileName", att.getName()) ;
          nodeContent.setProperty("exo:categoryId", catId) ;
          nodeContent.setProperty("jcr:mimeType", att.getMimeType());
          nodeContent.setProperty("jcr:data", att.getInputStream());
          nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
        } catch (Exception e) {
          log.error("Failed to save question: ", e);
        }
      }
    }
    // remove attachments
    NodeIterator nodeIterator = questionNode.getNodes() ;
    Node node = null ;
    while(nodeIterator.hasNext()){
      node = nodeIterator.nextNode() ;
      if(node.isNodeType("exo:faqAttachment") && !listNodeNames.contains(node.getName())) node.remove() ;
    }

    question.setId(questionNode.getName());
		String catePath = questionNode.getParent().getParent().getPath();
		question.setCategoryId(catePath.substring(catePath.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
		// send email notification
		sendNotifyWatcher(question, faqSetting, isNew);
		
   /* if(!isNew) {
      String catePath = questionNode.getParent().getParent().getPath() ;
      question.setCategoryId(catePath.substring(catePath.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
    }
    if(faqSetting.getDisplayMode().equals("approved")) {
      // Send notification when question response or edited or watching
      if(question.isApproved() && question.isActivated()) {
        sendNotifyForQuestionWatcher (question, faqSetting) ;
      }
    } else {
      //Send notification when add new question in watching category
      if(isNew || question.isActivated()) {
        sendNotifyForCategoryWatcher(question, faqSetting, isNew) ;
      }
    }*/

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#saveQuestion(org.exoplatform.faq.service.Question, boolean, org.exoplatform.faq.service.FAQSetting)
   */
  public Node saveQuestion(Question question, boolean isAddNew, FAQSetting faqSetting) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();	 
    try {
      Node questionNode;
      Node questionHome ;
      Node category ;
      if(isAddNew) {
        category = getFAQServiceHome(sProvider).getNode(question.getCategoryId()) ;				
        try {
          questionHome = category.getNode(Utils.QUESTION_HOME) ;
        }catch(PathNotFoundException ex) {
          //					TODO: JUnit test is fall
          questionHome = category.addNode(Utils.QUESTION_HOME, "exo:faqQuestionHome") ;
          addRSSListener(questionHome) ;
        }
        questionNode = questionHome.addNode(question.getId(), "exo:faqQuestion");
        /*if(!question.getCategoryId().equals(Utils.CATEGORY_HOME)) {
					String catId = question.getCategoryId() ;
					question.setCategoryId(catId.substring(catId.lastIndexOf("/") + 1)) ;
				}*/
      }else {
        questionNode = getFAQServiceHome(sProvider).getNode(question.getPath()) ;
      }	
      //			System.out.println("questionNode ==>" + questionNode.getPath());
      saveQuestion(questionNode, question, isAddNew, sProvider, faqSetting);
      if (questionNode.isNew())	{
        questionNode.getSession().save();
        registerQuestionNodeListener(questionNode);
      }
      else questionNode.save();

      return questionNode;
    }catch (Exception e) {
      log.error("Failed to save question ", e);
    }
    return null ;
  }


  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#removeQuestion(java.lang.String)
   */
  public void removeQuestion(String questionId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
      Node questionHome = questionNode.getParent() ;
      questionNode.remove();
      questionHome.save();
    }catch (Exception e) {
      log.error("Fail ro remove question: ",e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getCommentById(javax.jcr.Node, java.lang.String)
   */
  public Comment getCommentById(Node questionNode, String commentId) throws Exception{
    try{
      Comment comment = new Comment();
      Node commentNode = questionNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
      comment.setId(commentNode.getName()) ;
      if(commentNode.hasProperty("exo:comments")) comment.setComments((commentNode.getProperty("exo:comments").getValue().getString())) ;
      if(commentNode.hasProperty("exo:commentBy")) comment.setCommentBy((commentNode.getProperty("exo:commentBy").getValue().getString())) ;		
      if(commentNode.hasProperty("exo:dateComment")) comment.setDateComment((commentNode.getProperty("exo:dateComment").getValue().getDate().getTime())) ;
      if(commentNode.hasProperty("exo:postId")) comment.setPostId(commentNode.getProperty("exo:postId").getString()) ;
      return comment;
    } catch (Exception e){
      log.error("Failed to get comment through id: ", e);
      return null;
    }
  }


  private Question getQuestion(Node questionNode) throws Exception {
    Question question = new Question() ;
    question.setId(questionNode.getName()) ;
    if(questionNode.hasProperty("exo:language")) question.setLanguage(questionNode.getProperty("exo:language").getString()) ;
    if(questionNode.hasProperty("exo:name")) question.setDetail(questionNode.getProperty("exo:name").getString()) ;
    if(questionNode.hasProperty("exo:author")) question.setAuthor(questionNode.getProperty("exo:author").getString()) ;
    if(questionNode.hasProperty("exo:email")) question.setEmail(questionNode.getProperty("exo:email").getString()) ;
    if(questionNode.hasProperty("exo:title")) question.setQuestion(questionNode.getProperty("exo:title").getString()) ;
    if(questionNode.hasProperty("exo:createdDate")) question.setCreatedDate(questionNode.getProperty("exo:createdDate").getDate().getTime()) ;
    if(questionNode.hasProperty("exo:categoryId")) question.setCategoryId(questionNode.getProperty("exo:categoryId").getString()) ;
    /*String catePath = questionNode.getParent().getParent().getPath() ;
		question.setCategoryId(catePath.substring(catePath.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;*/
    if(questionNode.hasProperty("exo:isActivated")) question.setActivated(questionNode.getProperty("exo:isActivated").getBoolean()) ;
    if(questionNode.hasProperty("exo:isApproved")) question.setApproved(questionNode.getProperty("exo:isApproved").getBoolean()) ;
    if(questionNode.hasProperty("exo:relatives")) question.setRelations(Utils.valuesToArray(questionNode.getProperty("exo:relatives").getValues())) ;		
    if(questionNode.hasProperty("exo:nameAttachs")) question.setNameAttachs(Utils.valuesToArray(questionNode.getProperty("exo:nameAttachs").getValues())) ;		
    if(questionNode.hasProperty("exo:usersVote")) question.setUsersVote(Utils.valuesToArray(questionNode.getProperty("exo:usersVote").getValues())) ;		
    if(questionNode.hasProperty("exo:markVote")) question.setMarkVote(questionNode.getProperty("exo:markVote").getValue().getDouble()) ;
    if(questionNode.hasProperty("exo:emailWatching")) question.setEmailsWatch(Utils.valuesToArray(questionNode.getProperty("exo:emailWatching").getValues())) ;
    if(questionNode.hasProperty("exo:userWatching")) question.setUsersWatch(Utils.valuesToArray(questionNode.getProperty("exo:userWatching").getValues())) ;
    if(questionNode.hasProperty("exo:topicIdDiscuss")) question.setTopicIdDiscuss(questionNode.getProperty("exo:topicIdDiscuss").getString()) ;
    if(questionNode.hasProperty("exo:link")) question.setLink(questionNode.getProperty("exo:link").getString()) ;
    if (questionNode.hasProperty("exo:lastActivity")) question.setLastActivity(questionNode.getProperty("exo:lastActivity").getString());
    if (questionNode.hasProperty("exo:numberOfPublicAnswers")) question.setNumberOfPublicAnswers(questionNode.getProperty("exo:numberOfPublicAnswers").getLong());
    String path = questionNode.getPath() ;
    question.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;

    List<FileAttachment> listFile = new ArrayList<FileAttachment>() ;
    NodeIterator nodeIterator = questionNode.getNodes() ;
    Node nodeFile ;
    Node node ;
    FileAttachment attachment =	null;
    String workspace = questionNode.getSession().getWorkspace().getName() ;;
    while(nodeIterator.hasNext()){
      node = nodeIterator.nextNode() ;
      if(node.isNodeType("exo:faqAttachment")) {
        attachment = new FileAttachment() ;
        nodeFile = node.getNode("jcr:content") ;
        attachment.setId(node.getPath());
        attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
        attachment.setNodeName(node.getName());
        attachment.setName(nodeFile.getProperty("exo:fileName").getValue().getString());				
        attachment.setWorkspace(workspace) ;
        attachment.setPath("/" + workspace + node.getPath()) ;
        try{
          if(nodeFile.hasProperty("jcr:data")) attachment.setSize(nodeFile.getProperty("jcr:data").getStream().available());
          else attachment.setSize(0) ;
        } catch (Exception e) {
          attachment.setSize(0) ;
          log.error("Failed to get question: ", e);
        }
        listFile.add(attachment);
      }
    }
    question.setAttachMent(listFile) ;
    question.setAnswers(getAnswers(questionNode));
    question.setComments(getComment(questionNode));
    return question ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getQuestionById(java.lang.String)
   */
  public Question getQuestionById(String questionId) throws Exception {
    return getQuestion(getQuestionNodeById(questionId));
  }

  private List<String> getViewableCategoryIds(SessionProvider sessionProvider) throws Exception{
    List<String> listId = new ArrayList<String>();
    Node cateHomeNode = getCategoryHome(sessionProvider, null);
    StringBuffer queryString = new StringBuffer("/jcr:root").append(cateHomeNode.getPath()). 
    append("//element(*,exo:faqCategory)[@exo:isView='true'] order by @exo:createdDate descending");
    QueryManager qm = cateHomeNode.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();		
    NodeIterator iter = result.getNodes() ;
    while(iter.hasNext()) {
      listId.add(iter.nextNode().getName());
    }
    listId.add(Utils.CATEGORY_HOME);
    return listId;
  }

  private List<String> getRetrictedCategories(String userId, List<String> usermemberships) throws Exception{
    List<String> categoryList = new ArrayList<String>();
    SessionProvider sessionProvider = CommonUtils.createSystemProvider();
    try{
      Node faqHome = getFAQServiceHome(sessionProvider);
      StringBuffer queryString = new StringBuffer("/jcr:root").append(faqHome.getPath()). 
      append("//element(*,exo:faqCategory)[@exo:userPrivate != ''] order by @exo:createdDate descending");
      QueryManager qm = faqHome.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();		
      NodeIterator iter = result.getNodes() ;
      boolean isAudience = false ;
      while(iter.hasNext()) {
        if(usermemberships.size() > 0) {
          Node cat = iter.nextNode() ;
          try {
            String[] audiences = Utils.valuesToArray(cat.getProperty("exo:userPrivate").getValues()) ;
            isAudience = false ;
            for(String id : usermemberships) {							
              for(String audien : audiences) {
                if(id.equals(audien)) {
                  isAudience = true ;
                  break ;
                }							
              }
              if(isAudience) break ;
            }
            if(!isAudience) categoryList.add(cat.getName()) ;							
          }catch (Exception e) {
            log.error("Failed to check audience ", e);
          }					
        }else {
          categoryList.add(iter.nextNode().getName());
        }				
      }
    }catch(Exception e) {
      log.error("Failed to get restricte category: ", e);
    }
    return categoryList;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getAllQuestions()
   */
  public QuestionPageList getAllQuestions() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null) ;
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
      append("//element(*,exo:faqQuestion)[");
      List<String> listIds = getViewableCategoryIds(sProvider);
      for(int i = 0; i < listIds.size(); i ++){
        if(i > 0) queryString.append(" or ");
        queryString.append("(exo:categoryId='").append(listIds.get(i)).append("')");
      }
      queryString.append("]order by @exo:createdDate ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
      return pageList ;
    }catch (Exception e) {
      log.error("Failed to get all questions: ", e);
    }
    return null ;
  }


  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getQuestionsNotYetAnswer(java.lang.String, boolean)
   */
  public QuestionPageList getQuestionsNotYetAnswer(String categoryId, boolean isApproved) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null) ;
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      String qr = "";
      boolean isOpenQs = false;
      if(categoryId.indexOf(" ") > 0){
        String []strs = categoryId.split(" ");
        categoryId = strs[0];
				if(strs.length == 3) {
					qr = strs[1];
					isOpenQs = Boolean.parseBoolean(strs[2]);
				} else {
					isOpenQs = Boolean.parseBoolean(strs[1]);
				}
      }
      StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:faqQuestion)[") ;
      if(categoryId.equals(Utils.ALL)){
        List<String> listIds = getViewableCategoryIds(sProvider);
        for(int i = 0; i < listIds.size(); i ++){
          if(i > 0) queryString.append(" or ");
          queryString.append("(exo:categoryId='").append(listIds.get(i)).append("')");
        }				
      } else {
        queryString.append("((@exo:categoryId='").append(categoryId).append("')").
        append((categoryId.indexOf("/") > 0)?(" or (@exo:categoryId='"+categoryId.substring(categoryId.lastIndexOf("/") + 1)+"'))"):")");
      }			
      if(isApproved)	queryString.append(" and (@exo:isApproved='true')");
      if(qr.length() > 0) queryString.append(" and ((@exo:isApproved='true') or ").append(qr).append(")");
      queryString.append("] order by @exo:createdDate ascending");

      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
      pageList.setNotYetAnswered(true);
      pageList.setOpenQuestion(isOpenQs);
      return pageList ;
    }catch (Exception e) {
      log.error("Get question not yet answer failed: ", e);
    }
    return null ;
  }
  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getPendingQuestionsByCategory(java.lang.String, org.exoplatform.faq.service.FAQSetting)
   */
  public QuestionPageList getPendingQuestionsByCategory(String categoryId, FAQSetting faqSetting) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null) ;
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = null;
      if(categoryId == null || categoryId.trim().length() < 1) categoryId = "null";
      queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
      append("//element(*,exo:faqQuestion)[((@exo:categoryId='").append(categoryId).append("')").
      append((categoryId.indexOf("/") > 0)?(" or (@exo:categoryId='"+categoryId.substring(categoryId.lastIndexOf("/") + 1)+"'))"):")").
      append(" and (@exo:isActivated='true') and (@exo:isApproved='false')").append("]");		
      queryString.append("order by ");		
      if(faqSetting.isSortQuestionByVote()){
        queryString.append("@exo:markVote descending, ");
      }		
      // order by and ascending or descending
      if(faqSetting.getOrderBy().equals("created")){
        queryString.append("@exo:createdDate ");
      } else {
        queryString.append("@exo:title ");
      }
      if(faqSetting.getOrderType().equals("asc")){
        queryString.append("ascending");
      } else {
        queryString.append("descending");
      }		
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
      return pageList ;
    }catch (Exception e) {
      log.error("Get pedding question through category failed: ", e);
    }
    return null ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getQuestionsByCatetory(java.lang.String, org.exoplatform.faq.service.FAQSetting)
   */
  public QuestionPageList getQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
    SessionProvider sProvider =	CommonUtils.createSystemProvider();
    try {
      String id ;
      Node categoryNode ;
      if(categoryId == null || Utils.CATEGORY_HOME.equals(categoryId)) {
        id = Utils.CATEGORY_HOME ;
        categoryId = Utils.CATEGORY_HOME ;
        categoryNode = getCategoryHome(sProvider, null);
      }else{
        id = categoryId.substring(categoryId.lastIndexOf("/") + 1) ;
        categoryNode = getFAQServiceHome(sProvider).getNode(categoryId) ;
      } 
      categoryNode = getFAQServiceHome(sProvider).getNode(categoryId) ;
      QueryManager qm = categoryNode.getSession().getWorkspace().getQueryManager();
      //if(categoryId == null || categoryId.trim().length() < 1) categoryId = "null";
      String userId = faqSetting.getCurrentUser();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryNode.getPath()).append("/").append(Utils.QUESTION_HOME). 
      append("/element(*,exo:faqQuestion)[(@exo:categoryId='").append(id).append("') and (@exo:isActivated='true')");
      if(!faqSetting.isCanEdit()) {
        queryString.append(" and (@exo:isApproved='true'");
        if(userId != null && userId.length() > 0 && faqSetting.getDisplayMode().equals("both")){
          queryString.append(" or @exo:author='").append(userId).append("')");
        }else{ queryString.append(")");}
      } else {
        if(faqSetting.getDisplayMode().equals("approved")){
          queryString.append(" and (@exo:isApproved='true')");
        }
      }
      queryString.append("] order by ");

      if(faqSetting.isSortQuestionByVote()){
        queryString.append("@exo:markVote descending, ");
      }		
      // order by and ascending or descending
      if(faqSetting.getOrderBy().equals("created")){
        queryString.append("@exo:createdDate ");
      } else {
        queryString.append("@exo:title ");
      }
      if(faqSetting.getOrderType().equals("asc")){
        queryString.append("ascending");
      } else {
        queryString.append("descending");
      }
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();		
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
      return pageList ;
    }catch (Exception e) {
      log.debug("Getting question through category failed: ", e);
    }
    return null ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getAllQuestionsByCatetory(java.lang.String, org.exoplatform.faq.service.FAQSetting)
   */
  public QuestionPageList getAllQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null) ;
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = null;
      if(faqSetting.getDisplayMode().equals("approved")){
        queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
        append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')").
        append(" and (@exo:isApproved='true')").
        append("]");
      } else {
        queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
        append("//element(*,exo:faqQuestion)[@exo:categoryId='").append(categoryId).append("'").
        append("]");
      }			
      queryString.append("order by ");			
      if(faqSetting.isSortQuestionByVote()){
        queryString.append("@exo:markVote descending, ");
      }			
      //	order by and ascending or descending
      if(faqSetting.getOrderBy().equals("created")){
        queryString.append("@exo:createdDate ");
      } else {
        queryString.append("@exo:title ");
      }
      if(faqSetting.getOrderType().equals("asc")){
        queryString.append("ascending");
      } else {
        queryString.append("descending");
      }
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;		
      return pageList ;
    }catch (Exception e) {
      log.debug("Failed to get all question through category: ", e);
    }
    return null ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getQuestionsByListCatetory(java.util.List, boolean)
   */
  public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null) ;

      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root")
      .append(categoryHome.getPath()).append("//element(*,exo:faqQuestion) [");			
      queryString.append(" (");
      int i = 0 ;
      for(String categoryId : listCategoryId) {
        if ( i > 0) queryString.append(" or ") ;
        queryString.append("(@exo:categoryId='").append(categoryId).append("')");
        i ++ ;
      }
      queryString.append(")]");				
      queryString.append(" order by @exo:createdDate ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = null;
      pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
      pageList.setNotYetAnswered(isNotYetAnswer);
      return pageList ;
    } catch ( Exception e) {
      log.debug("Failed get questions through list of category: ", e);
    }
    return null ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getQuickQuestionsByListCatetory(java.util.List, boolean)
   */
  public List<Question> getQuickQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<Question> questions = new ArrayList<Question> () ;
    try {
      Node categoryHome = getCategoryHome(sProvider, null) ;
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
      append("//element(*,exo:faqQuestion)[(");
      int i = 0 ;
      for(String categoryId : listCategoryId) {
        if ( i > 0) queryString.append(" or ") ;
        queryString.append("(@exo:categoryId='").append(categoryId).append("')");				
        i ++ ;
      }
      queryString.append(")]order by @exo:createdDate ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes() ;
      while(iter.hasNext()) {
        questions.add(getQuickQuestion(iter.nextNode())) ;
      }
    } catch ( Exception e) {
      log.debug("Getting quick questions through list of category failed: ", e);
    }
    return questions ;
  }

  private Question getQuickQuestion(Node questionNode) throws Exception {
    Question question = new Question() ;
    question.setId(questionNode.getName()) ;
    question.setCategoryId(questionNode.getProperty("exo:categoryId").getString()) ;
    String path = questionNode.getPath() ;
    question.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
    question.setQuestion(questionNode.getProperty("exo:title").getString()) ;
    return question ; 
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getCategoryPathOfQuestion(java.lang.String)
   */
  public String getCategoryPathOfQuestion(String questionPath) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    String path = "";
    Node faqHome = null;
    try {
      faqHome = getFAQServiceHome(sProvider);
      Node question = faqHome.getNode(questionPath) ;
      Node subCat = question.getParent().getParent() ; 
      String pathName = "";
      while(!subCat.getName().equals(Utils.CATEGORY_HOME)) {
        pathName = "/" + subCat.getProperty("exo:name").getString() + pathName;
        subCat = subCat.getParent() ;
      }
      try {
        pathName = faqHome.getProperty("exo:name").getString() + pathName ;
      }catch (Exception e) {
        pathName = "home" + pathName ;
      }
    }catch( Exception e) {
      log.debug("Getting category path of the question failed: ", e);
    }
    return path;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#moveQuestions(java.util.List, java.lang.String)
   */
  public void moveQuestions(List<String> questions, String destCategoryId, String questionLink, FAQSetting faqSetting) throws Exception {

    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqHome = getFAQServiceHome(sProvider) ;
      String homePath = faqHome.getPath() ;
      Node destQuestionHome;
      try {
        destQuestionHome = (Node)faqHome.getNode(destCategoryId + "/" + Utils.QUESTION_HOME);
      } catch (Exception e) {
        destQuestionHome = faqHome.getNode(destCategoryId).addNode(Utils.QUESTION_HOME, "exo:faqQuestionHome");
        faqHome.getSession().save();
      }
      for(String id : questions) {
        try{
          Node destCateNode =	faqHome.getNode(id).getParent();
          faqHome.getSession().move(homePath+ "/" + id, destQuestionHome.getPath() + id.substring(id.lastIndexOf("/"))) ;
          faqHome.getSession().save() ;
          Node questionNode = faqHome.getNode(destCategoryId + "/" + Utils.QUESTION_HOME + id.substring(id.lastIndexOf("/"))) ;
          String catId = destCategoryId.substring(destCategoryId.lastIndexOf("/")+ 1) ;
          questionNode.setProperty("exo:categoryId", catId) ;
          NodeIterator iter = questionNode.getNodes() ;
          Node attNode ;
          while(iter.hasNext()) {
            attNode = iter.nextNode() ;
            if(attNode.isNodeType("exo:faqAttachment")) {
              attNode.getNode("jcr:content").setProperty("exo:categoryId", catId) ;
            }
          }
          updateAnswers(questionNode, catId) ;
          updateComments(questionNode,catId) ;
          questionNode.save() ;
          //				send email notify to author question. by Duy Tu	
          try {
            sendNotifyMoveQuestion(destCateNode, questionNode, catId, questionLink, faqSetting);
          } catch (Exception e) {}


        }catch(ItemNotFoundException ex){
        }
      }			
    }catch (Exception e) {
      log.error("Failed to remove question: ", e);
    }

  } 

  private void sendNotifyMoveQuestion(Node destCateNode, Node questionNode,String cateId, String link, FAQSetting faqSetting) throws Exception {
    String contentMail = faqSetting.getEmailMoveQuestion();
    String categoryName = null;
    try {
      categoryName = questionNode.getParent().getParent().getProperty("exo:name").getString();
    } catch (Exception e){
      categoryName = "Root";
    }
    Message message = new Message();
    message.setMimeType(MIMETYPE_TEXTHTML) ;
    message.setFrom(questionNode.getProperty("exo:author").getString());
    message.setSubject(faqSetting.getEmailSettingSubject() + ": " + questionNode.getProperty("exo:title").getString());
    if(categoryName == null || categoryName.trim().length() < 1) categoryName = "Root";
    String questionDetail = questionNode.getProperty("exo:title").getString();
    if(questionNode.hasProperty("exo:name")){
      questionDetail = questionDetail + "<br/> <span style=\"font-weight:normal\"> " + questionNode.getProperty("exo:name").getString() + "</span>";
    }
    contentMail = StringUtils.replace(contentMail, "&questionContent_", questionDetail);
    contentMail = StringUtils.replace(contentMail, "&categoryName_", categoryName);
    contentMail = StringUtils.replace(contentMail, "&questionLink_", link);
    message.setBody(contentMail);
    Set<String>emails = new HashSet<String>();
    emails.addAll(calculateMoveEmail(destCateNode));
    emails.addAll(calculateMoveEmail(questionNode.getParent()));
    emails.add(questionNode.getProperty("exo:email").getString());
    sendEmailNotification(new ArrayList<String>(emails), message) ;
  }

  private Set<String> calculateMoveEmail(Node node) throws Exception {
    Set<String> set = new HashSet<String>();
    while(!node.getName().equals(Utils.CATEGORY_HOME)) {
      if(node.isNodeType("exo:faqWatching")){
        set.addAll(Utils.valuesToList(node.getProperty("exo:emailWatching").getValues()));
      }
      node = node.getParent();
    }
    return set;
  }

  private void updateComments(Node question, String catId) throws Exception {
    try {
      QueryManager qm = question.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + question.getPath() + "//element(*,exo:comment)");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      while(iter.hasNext()) {
        iter.nextNode().setProperty("exo:categoryId", catId) ;
      }			
    }catch (Exception e) {
      log.error("Updating comments failed: ", e);
    }
  }

  private void updateAnswers(Node question, String catId) throws Exception {
    try {
      QueryManager qm = question.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + question.getPath() + "//element(*,exo:answer)");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      while(iter.hasNext()) {
        iter.nextNode().setProperty("exo:categoryId", catId) ;
      }			
    }catch (Exception e) {
      log.error("Updating answers failed: ", e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#changeStatusCategoryView(java.util.List)
   */
  public void changeStatusCategoryView(List<String> listCateIds) throws Exception{
    if(listCateIds == null || listCateIds.size() < 1) return;
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqHome = getFAQServiceHome(sProvider);
      Node cat ;
      for(String id : listCateIds){
        cat = faqHome.getNode(id) ;
        cat.setProperty("exo:isView", !cat.getProperty("exo:isView").getBoolean()) ;
      }
      faqHome.save();
    }catch (Exception e) {
      log.error("Changing status category view failed: ", e);
    }
  }


  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getMaxindexCategory(java.lang.String)
   */
  public long getMaxindexCategory(String parentId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    long max = 0 ;
    try {
      NodeIterator iter = getFAQServiceHome(sProvider).getNode((parentId == null)?Utils.CATEGORY_HOME:parentId).getNodes();
      while (iter.hasNext()) {
        Node node = iter.nextNode();
        if(node.isNodeType("exo:faqCategory")) max = max + 1;
      }
    }catch (Exception e) {
      log.error("Failed to get max index category", e);
    }
    return max ;
  }

  private void saveCategory(Node categoryNode, Category category, boolean isNew, SessionProvider sProvider) throws Exception {
    Map<String, String> moderators = new HashMap<String, String> () ;
    if(!categoryNode.getName().equals(Utils.CATEGORY_HOME)) {
      Node parentCategory = categoryNode.getParent() ;
      if(parentCategory.hasProperty("exo:moderators")) {
        for(Value vl : parentCategory.getProperty("exo:moderators").getValues()) {
          moderators.put(vl.getString(), vl.getString()) ;
        }
      }
    }

    if(category.getId() != null){
      categoryNode.setProperty("exo:id", category.getId()) ;			
      categoryNode.setProperty("exo:createdDate", GregorianCalendar.getInstance()) ;
      categoryNode.setProperty("exo:isView", category.isView());
    }
    categoryNode.setProperty("exo:index", category.getIndex()) ;
    categoryNode.setProperty("exo:name", category.getName()) ;
    categoryNode.setProperty("exo:description", category.getDescription()) ;
    for(String mod : category.getModerators()) {
      moderators.put(mod, mod) ;
    }
    categoryNode.setProperty("exo:moderators", moderators.values().toArray(new String[]{})) ;
    categoryNode.setProperty("exo:isModerateQuestions", category.isModerateQuestions()) ;
    categoryNode.setProperty("exo:viewAuthorInfor", category.isViewAuthorInfor()) ;
    categoryNode.setProperty("exo:isModerateAnswers", category.isModerateAnswers());
    categoryNode.setProperty("exo:userPrivate", category.getUserPrivate());
    if(!isNew) {
      try {
        updateModeratorForChildCategories (categoryNode, moderators) ;
      }catch (Exception e){
        log.debug("Updating moderator for child category failed: ", e);
      }
    }
    if(categoryNode.isNew()) categoryNode.getSession().save() ;
    else categoryNode.save() ;		
  }

  private void updateModeratorForChildCategories(Node currentCategory, Map<String, String> moderators) throws Exception{		
    Map<String, String> modMap = new HashMap<String, String>();
    Node cat ;
    NodeIterator iter = currentCategory.getNodes() ;
    while(iter.hasNext()) {
      cat = iter.nextNode() ;
      if(cat.isNodeType("exo:faqCategory")) {
        modMap.clear() ;
        modMap.putAll(moderators) ;
        for(Value vl : cat.getProperty("exo:moderators").getValues()) {
          modMap.put(vl.getString(), vl.getString()) ;
        }
        cat.setProperty("exo:moderators", modMap.values().toArray(new String[]{})) ;
        cat.save() ;
        if(cat.hasNodes()) {
          updateModeratorForChildCategories(cat, modMap) ;
        }
      }			
    }
  }

  private NodeIterator getCategoriesIterator(Node parentCategory) throws Exception {
    QueryManager qm = parentCategory.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + parentCategory.getPath());
    queryString.append("/element(*,exo:faqCategory) order by @exo:index ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  private void resetIndex(Node goingCategory, long index, long gindex) throws Exception {
    Node parent = goingCategory.getParent();
    Node node;
    NodeIterator iter = getCategoriesIterator(parent);
    if (index <= iter.getSize()) {
      if (index < 1) {
        goingCategory.setProperty("exo:index", 1);
      }
      long l = 1;
      while (iter.hasNext()) {
        node = iter.nextNode();
        if (index < 1) {// move up to top
          if (node.getName().equals(goingCategory.getName()))
            continue;
          else {
            l++;
            node.setProperty("exo:index", l);
          }
        } else if (index > gindex) {// move down to index
          l = node.getProperty("exo:index").getLong();
          if (l >= gindex && l <= index) {
            if (l == gindex) {
              goingCategory.setProperty("exo:index", index);
            } else {
              node.setProperty("exo:index", l - 1);
            }
          }
          if (l > index)
            break;
        } else {// move up to index
          l = node.getProperty("exo:index").getLong();
          if (l > index) {
            if (l == gindex) {
              goingCategory.setProperty("exo:index", index + 1);
            } else {
              node.setProperty("exo:index", l + 1);
            }
          }
        }
      }
      parent.save();
    } else {
      goingCategory.setProperty("exo:index", iter.getSize());
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
      node.setProperty("exo:index", i);
      i++;
    }
    parentCateNode.save();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#saveCategory(java.lang.String, org.exoplatform.faq.service.Category, boolean)
   */
  public void saveCategory(String parentId, Category cat, boolean isAddNew) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node newCategory ;
      if(isAddNew) {
        Node parentNode = getFAQServiceHome(sProvider).getNode(parentId) ;
        newCategory = parentNode.addNode(cat.getId(), "exo:faqCategory") ;
        newCategory.addMixin("mix:faqSubCategory") ;
        //				TODO: JUnit test is fall
        Node questionHome = newCategory.addNode(Utils.QUESTION_HOME, "exo:faqQuestionHome") ;
        addRSSListener(questionHome) ;
      } else {
        newCategory = getFAQServiceHome(sProvider).getNode(cat.getPath()) ;
      }
      long index = cat.getIndex();
      long oldIndex = new PropertyReader(newCategory).l("exo:index", newCategory.getParent().getNodes().getSize());
      boolean isResetIndex = (isAddNew || (oldIndex != index));
      if (isResetIndex) {
        cat.setIndex(oldIndex);
      }
      saveCategory(newCategory, cat, isAddNew, sProvider);
      if (isResetIndex) {
        index = (index < oldIndex) ? index - 1 : index;
        resetIndex(newCategory, index, oldIndex);
      }	
    }catch (Exception e) {
      log.error("Failed to save category: ", e);
    }

  }

  private List<Cate> listingSubTree(Node currentCategory, int i) throws Exception{		
    Node cat ;
    int j = i;
    j = j + 1 ;
    List<Cate> cateList = new ArrayList<Cate>() ;
    Cate cate;		
    NodeIterator iter = currentCategory.getNodes() ;
    while(iter.hasNext()) {
      cat = iter.nextNode() ;
      if(cat.isNodeType("exo:faqCategory")) {
        cate = new Cate() ;
        cate.setCategory(getCategory(cat)) ;
        cate.setDeft(i) ;
        cateList.add(cate) ;
        if(cat.hasNodes()) {
          cateList.addAll(listingSubTree(cat, j)) ; ;
        }
      }			
    }
    return cateList ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#listingCategoryTree()
   */
  public List<Cate> listingCategoryTree() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
      Node cateHome = getCategoryHome(sProvider, null) ;
      int i = 1 ;
      List<Cate> cateList = new ArrayList<Cate>() ;		
      cateList.addAll(listingSubTree(cateHome, i)) ;
      return cateList	;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#removeCategory(java.lang.String)
   */
  public void removeCategory(String categoryId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqHome = getFAQServiceHome(sProvider) ;
      Node node = faqHome.getNode(categoryId);
      Node parent = node.getParent();
      node.remove() ;
      faqHome.save() ;
      //update index
      reUpdateIndex(parent);
    }catch (Exception e) {
      log.error("Can not remove category has id: " + categoryId);
    }	
  }

  private Category getCategory(Node categoryNode) throws Exception {
  	if(categoryNode == null) return null;
    Category category = new Category() ;
    category.setId(categoryNode.getName()) ;
    if(categoryNode.hasProperty("exo:name")) category.setName(categoryNode.getProperty("exo:name").getString()) ;
    if(categoryNode.hasProperty("exo:description")) category.setDescription(categoryNode.getProperty("exo:description").getString()) ;
    if(categoryNode.hasProperty("exo:createdDate")) category.setCreatedDate(categoryNode.getProperty("exo:createdDate").getDate().getTime()) ;
    if(categoryNode.hasProperty("exo:moderators")) category.setModerators(Utils.valuesToArray(categoryNode.getProperty("exo:moderators").getValues())) ;
    if(categoryNode.hasProperty("exo:userPrivate")) category.setUserPrivate(Utils.valuesToArray(categoryNode.getProperty("exo:userPrivate").getValues())) ;
    if(categoryNode.hasProperty("exo:isModerateQuestions")) category.setModerateQuestions(categoryNode.getProperty("exo:isModerateQuestions").getBoolean()) ;
    if(categoryNode.hasProperty("exo:isModerateAnswers")) category.setModerateAnswers(categoryNode.getProperty("exo:isModerateAnswers").getBoolean()) ;
    if(categoryNode.hasProperty("exo:viewAuthorInfor")) category.setViewAuthorInfor(categoryNode.getProperty("exo:viewAuthorInfor").getBoolean()) ;
    if(categoryNode.hasProperty("exo:index")) category.setIndex(categoryNode.getProperty("exo:index").getLong()) ;
    if(categoryNode.hasProperty("exo:isView")) category.setView(categoryNode.getProperty("exo:isView").getBoolean()) ;
    String path = categoryNode.getPath() ;
    category.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;		
    return category;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getCategoryById(java.lang.String)
   */
  public Category getCategoryById(String categoryId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      return getCategory(getCategoryNodeById(sProvider, categoryId)) ;
    }catch (Exception e) {
      log.debug("Category not found " + categoryId);
    }
    return null ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#findCategoriesByName(java.lang.String)
   */
  public List<Category> findCategoriesByName(String categoryName) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null) ;
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
      append("//element(*,exo:faqCategory)[@exo:name='").
      append(categoryName).append("']") ;
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult queryResult = query.execute();
      NodeIterator iter = queryResult.getNodes() ;
      List<Category> result = new ArrayList<Category>() ;
      while(iter.hasNext()) {
        result.add(getCategory(iter.nextNode())) ;
      }
      return result ;
    }catch(Exception e) {
      log.error("Could not retrieve categories by name " + categoryName, e);
    }
    return null ;	 
  }


  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getListCateIdByModerator(java.lang.String)
   */
  public List<String> getListCateIdByModerator(String user) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      List<String> allOfUser = UserHelper.getAllGroupAndMembershipOfUser(user);
      StringBuilder builder = new StringBuilder();
      int i = 0;
      if (allOfUser != null && allOfUser.size() > 0) {
        builder.append(" ( ");
        for (String strUser : allOfUser) {
          strUser = strUser.trim();
          if (strUser.length() > 0) {
            if (i > 0) {
              builder.append(" or ");
              i = 1;
            }
            builder.append("@").append("exo:moderators").append("= '").append(strUser).append("' ");
          }
          i++;
        }
        builder.append(" ) ");
        builder.append(" and @exo:isView='true'  ");
      }
      
      Node categoryHome = getCategoryHome(sProvider, null) ;
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getParent() .getPath())
                                                           .append("//element(*,exo:faqCategory)[ " + builder.toString() + " ]");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes() ;
      List<String> listCateId = new ArrayList<String>() ;
      while(iter.hasNext()) {
        Node cate = iter.nextNode() ;
        try{
          listCateId.add(cate.getName() + SEMICOLON + cate.getProperty("exo:name").getString());
        }catch(Exception e) {
          log.debug("Getting property of " + cate + " node failed: ", e);
        }				
      }
      return listCateId ;
    }catch(Exception e) {
      log.error("Failed to get list of CateID through Moderator: ", e);
    }
    return null ;		
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getAllCategories()
   */
  public List<Category> getAllCategories() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null) ;
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:faqCategory)") ;
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes() ;
      List<Category> catList = new ArrayList<Category>() ;
      while(iter.hasNext()) {
        catList.add(getCategory(iter.nextNode())) ;
      }
      return catList ;
    }catch (Exception e) {
      log.error("Getting all category failed: ", e);
    }
    return null ;

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#existingCategories()
   */
  public long existingCategories() throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null) ;
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:faqCategory)") ;
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      result.getNodes().getSize() ;			
    }catch (Exception e) {
      log.error("Failed to check existing categories", e);
    }
    return 0 ;		
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getCategoryNodeById(java.lang.String)
   */
  public Node getCategoryNodeById(String categoryId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      return getCategoryNodeById(sProvider, categoryId);
    }catch (Exception e) {
      log.error("Getting node failed: ", e);
    }
    return null ;
  }

  private Node getCategoryNodeById(SessionProvider sProvider, String categoryId) throws Exception {
    try {
      Node faqHome = getFAQServiceHome (sProvider) ;
      return faqHome.getNode(categoryId) ;
    }catch (PathNotFoundException e) {
      Node categoryHome = getCategoryHome(sProvider, null) ;
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:faqCategory)").
      append("[fn:name()='").append(categoryId).append("']") ;
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			if(iter.getSize() != 0)
				return iter.nextNode();
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getSubCategories(java.lang.String, org.exoplatform.faq.service.FAQSetting, boolean, java.util.List)
   */
  public List<Category> getSubCategories(String categoryId, FAQSetting faqSetting, boolean isGetAll, List<String> limitedUsers) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<Category> catList = new ArrayList<Category>() ;
    try {			
      Node parentCategory ;			
      if(categoryId == null || categoryId.equals(Utils.CATEGORY_HOME)) {
        parentCategory = getCategoryHome(sProvider, null) ;
      } else parentCategory = getFAQServiceHome(sProvider).getNode(categoryId) ;
      if(!faqSetting.isAdmin()){
        PropertyReader reader = new PropertyReader(parentCategory);
        List<String> userPrivates = reader.list("exo:userPrivate", new ArrayList<String>());
        if(!userPrivates.isEmpty() && !Utils.hasPermission(limitedUsers, userPrivates)) return catList;
      }

      StringBuffer queryString = new StringBuffer("/jcr:root").append(parentCategory.getPath());
      if(faqSetting.isAdmin()) 
        queryString.append("/element(*,exo:faqCategory) [@exo:isView='true'] order by @exo:index ascending");				
      else {
        queryString.append("/element(*,exo:faqCategory)[@exo:isView='true' and ( not(@exo:userPrivate) or @exo:userPrivate=''") ;
        if(limitedUsers != null){
          for(String id : limitedUsers) {
            queryString.append(" or @exo:userPrivate = '").append(id).append("' ") ;
            queryString.append(" or @exo:moderators = '").append(id).append("' ") ;
          }
        }
        queryString.append(" )] order by @exo:index");				
      }
      QueryManager qm = parentCategory.getSession().getWorkspace().getQueryManager();
      String qString = queryString.toString();
      Query query = qm.createQuery(qString, Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes() ;
      while(iter.hasNext()) {
        catList.add(getCategory(iter.nextNode())) ;
      } 
    }catch (Exception e) {
      throw e;
    }
    return catList ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getCategoryInfo(java.lang.String, org.exoplatform.faq.service.FAQSetting)
   */
  public long[] getCategoryInfo( String categoryId, FAQSetting faqSetting) throws Exception	{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    long[] cateInfo = new long[]{0, 0, 0, 0};// categories, all, open, pending
    try {
      Node parentCategory ;
      String id ;
      parentCategory = getFAQServiceHome(sProvider).getNode(categoryId) ;
      if(categoryId.indexOf("/") > 0) id = categoryId.substring(categoryId.lastIndexOf("/") + 1) ;
      else id = categoryId ;
      NodeIterator iter = parentCategory.getNodes() ;
      cateInfo[0] = iter.getSize() ;
      //			if(parentCategory.hasNode(FAQ_RSS)) cateInfo[0]--;	
      QueryManager qm = parentCategory.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(parentCategory.getPath()). 
      append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(id).
      append("') and (@exo:isActivated='true')").
      append("]").append("order by @exo:createdDate ascending");
      //System.out.println("Infor queryString ==> " + queryString);			
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator nodeIterator = result.getNodes() ;
      cateInfo[1] = nodeIterator.getSize() ;// all

      Node questionNode = null;
      boolean onlyGetApproved, questionIsApproved = true, isShow = true;
      onlyGetApproved = (faqSetting.getDisplayMode().equals("approved"));
      while(nodeIterator.hasNext()) {
        questionNode = nodeIterator.nextNode() ;
        questionIsApproved = questionNode.getProperty("exo:isApproved").getBoolean();
        isShow = (questionIsApproved || ((faqSetting.isCanEdit() || questionNode.getProperty("exo:author").getString().equals(faqSetting.getCurrentUser()))
            && !onlyGetApproved));
        if(!questionIsApproved){
          cateInfo[3] ++ ;// pending
          if(!isShow) cateInfo[1] --;
        }
        if(isShow) {
					if(!hasAnswerInQuestion(qm, questionNode))
						cateInfo[2] ++;// open
        }
      }
    }catch(Exception e) {
      log.error("Failed to get category info: ", e);
    }
    return cateInfo ;
  }

	private boolean hasAnswerInQuestion(QueryManager qm, Node questionNode) throws Exception {
		StringBuffer queryString = new StringBuffer("/jcr:root").append(questionNode.getPath()).append("//element(*,exo:answer)[(@exo:approveResponses='true') and (@exo:activateResponses='true')] order by @exo:dateResponse ascending");
	    QueryImpl impl = (QueryImpl) qm.createQuery(queryString.toString(), Query.XPATH);
	    impl.setOffset(0);
	    impl.setLimit(1);
	    QueryResult result = impl.execute();
		NodeIterator iter = result.getNodes();
		return (iter.getSize() > 0)?true:false;
	}
	
  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#moveCategory(java.lang.String, java.lang.String)
   */
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
      destNode.setProperty("exo:index", destNode.getParent().getNodes().getSize());
      destNode.save();
      // update index for children categories of parent category moved. 
      reUpdateIndex(parentNode);
    }catch (ItemExistsException e){ 
      throw e ;
    } catch (Exception e) {
      if(log.isDebugEnabled()) log.debug(e.getMessage());
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#addWatchCategory(java.lang.String, org.exoplatform.faq.service.Watch)
   */
  public void addWatchCategory(String id, Watch watch)throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqHome = getFAQServiceHome (sProvider) ;
      Map<String, String> watchs = new HashMap<String, String> () ;
      Node watchingNode = faqHome.getNode(id) ; ;
      if(watchingNode.isNodeType("exo:faqWatching")) {
        Value[] emails = watchingNode.getProperty("exo:emailWatching").getValues() ;
        Value[] users = watchingNode.getProperty("exo:userWatching").getValues() ;
        if(emails != null && users != null) {
          for(int i = 0; i < users.length; i ++) {
            watchs.put(users[i].getString(), emails[i].getString()) ;
          }
        }
        watchs.put(watch.getUser(), watch.getEmails()) ;
        watchingNode.setProperty("exo:emailWatching", watchs.values().toArray(new String[]{})) ;
        watchingNode.setProperty("exo:userWatching", watchs.keySet().toArray(new String[]{})) ;
      }else {
        watchingNode.addMixin("exo:faqWatching") ;
        watchingNode.setProperty("exo:emailWatching", new String[]{watch.getEmails()}) ;
        watchingNode.setProperty("exo:userWatching", new String[]{watch.getUser()}) ;
      }
      watchingNode.save() ;
    }catch (Exception e) {
      log.error("Failed to add watch category: " , e);
    }
  }

  //TODO Going to remove
  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getListMailInWatch(java.lang.String)
   */
  public QuestionPageList getListMailInWatch(String categoryId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null) ;	
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
      append("//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']") ;
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 5, queryString.toString(), true) ;
      return pageList;
    }catch(Exception e) {
      log.error("Failed to get list of mail watch: ", e);
    }
    return null ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getWatchByCategory(java.lang.String)
   */
  public List<Watch> getWatchByCategory(String categoryId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<Watch> listWatches = new ArrayList<Watch>();
    try {
      Node category = getFAQServiceHome(sProvider).getNode(categoryId) ;
      String[] userWatch = null;
      String[] emails = null;
      if(category.hasProperty("exo:emailWatching")) emails = Utils.valuesToArray(category.getProperty("exo:emailWatching").getValues());
      if(category.hasProperty("exo:userWatching")) userWatch = Utils.valuesToArray(category.getProperty("exo:userWatching").getValues());
      if(userWatch != null && userWatch.length > 0) {
        Watch watch = new Watch();
        for(int i = 0; i < userWatch.length; i ++){
          watch = new Watch();
          watch.setEmails(emails[i]);
          watch.setUser(userWatch[i]);
          listWatches.add(watch);
        }
      }
      return listWatches;
    }catch(Exception e) {
      log.error("Failed to get watch through category: ",e);
    }
    return listWatches ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#hasWatch(java.lang.String)
   */
  public boolean hasWatch(String categoryPath) {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node cat = getFAQServiceHome(sProvider).getNode(categoryPath) ;
      if(new PropertyReader(cat).strings("exo:userWatching", new String[]{}).length > 0) return true ;
    }catch (Exception e) {
      log.error("Failed to check has watch", e);
    }
    return false ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#addWatchQuestion(java.lang.String, org.exoplatform.faq.service.Watch, boolean)
   */
  public void addWatchQuestion(String questionId, Watch watch, boolean isNew) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    Map<String, String> watchMap = new HashMap<String, String>() ;
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
      if(questionNode.isNodeType("exo:faqWatching")){					
        Value[] values = questionNode.getProperty("exo:emailWatching").getValues() ;
        Value[] users = questionNode.getProperty("exo:userWatching").getValues() ;
        for(int i = 0; i < users.length; i ++) {
          watchMap.put(users[i].getString(), values[i].getString()) ;
        }
        watchMap.put(watch.getUser(), watch.getEmails()) ;

        questionNode.setProperty("exo:emailWatching", watchMap.values().toArray(new String[]{})) ;
        questionNode.setProperty("exo:userWatching", watchMap.keySet().toArray(new String[]{})) ;
        questionNode.save() ;
      } else {
        questionNode.addMixin("exo:faqWatching");
        questionNode.setProperty("exo:emailWatching", new String[]{watch.getEmails()}) ;
        questionNode.setProperty("exo:userWatching", new String[]{watch.getUser()}) ;
        questionNode.save() ;
      }			
      //questionHome.save();
    }catch (Exception e) {
      log.error("Failed to add a watch question: ", e );
    }

  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getWatchByQuestion(java.lang.String)
   */
  public List<Watch> getWatchByQuestion(String questionId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<Watch> listWatches = new ArrayList<Watch>();
    try {
      Node category = getFAQServiceHome(sProvider).getNode(questionId) ;
      if(category.isNodeType("exo:faqWatching")) {
        String[] userWatch = null;
        String[] emails = null;
        if(category.hasProperty("exo:emailWatching")) emails = Utils.valuesToArray(category.getProperty("exo:emailWatching").getValues());
        if(category.hasProperty("exo:userWatching")) userWatch = Utils.valuesToArray(category.getProperty("exo:userWatching").getValues());
        if(userWatch != null && userWatch.length > 0) {
          Watch watch = new Watch();
          for(int i = 0; i < userWatch.length; i ++){
            watch = new Watch();
            watch.setEmails(emails[i]);
            watch.setUser(userWatch[i]);
            listWatches.add(watch);
          }
        }
      }
    }catch(Exception e) {
      log.error("Failed to get watch through question: ", e);
    }
    return listWatches ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getWatchedCategoryByUser(java.lang.String)
   */
  public QuestionPageList getWatchedCategoryByUser(String userId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null);
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = null;
      queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
      append("//element(*,exo:faqCategory)[(@exo:userWatching='").append(userId).append("')]");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();			
      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
      return pageList ;
    }catch ( Exception e) {
      log.error("Failed to get watched category through user: ", e);
    }
    return null ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#isUserWatched(java.lang.String, java.lang.String)
   */
  public boolean isUserWatched(String userId, String cateId) {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node faqHome = getFAQServiceHome(sProvider);
      Node cate = faqHome.getNode(cateId) ;
      List<String> list = new PropertyReader(cate).list("exo:userWatching", new ArrayList<String>());
      for(String vl : list) {
        if (vl.equals(userId)) return true ;
      }
    }catch (Exception e) {
      log.error("Failed to check user watched", e);
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getWatchedSubCategory(java.lang.String, java.lang.String)
   */
  public List<String> getWatchedSubCategory(String userId, String cateId) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<String> watchedSub = new ArrayList<String> () ;
    try {
      Node faqHome = getFAQServiceHome(sProvider);
      Node category = faqHome.getNode(cateId) ;
      QueryManager qm = faqHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(category.getPath()).
      append("/element(*,exo:faqCategory)[(@exo:userWatching='").append(userId).append("')]");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes() ;
      while(iter.hasNext()) {
        watchedSub.add(iter.nextNode().getName()) ;
      }
    }catch (Exception e) {
      log.error("Getting watched sub category failed: ", e);
    }
    return watchedSub ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getListQuestionsWatch(org.exoplatform.faq.service.FAQSetting, java.lang.String)
   */
  public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node categoryHome = getCategoryHome(sProvider, null) ;
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
      append("//element(*,exo:faqQuestion)[(@exo:userWatching='").append(currentUser).append("')");
      if(faqSetting.getDisplayMode().equals("approved")) {
        queryString.append(" and (@exo:isApproved='true')");
      }
      if(!faqSetting.isAdmin()) queryString.append(" and (@exo:isActivated='true')");
      queryString.append("] order by ");
      if(faqSetting.isSortQuestionByVote()){
        queryString.append("@exo:markVote descending, ");
      }
      // order by and ascending or descending
      if(faqSetting.getOrderBy().equals("created")){
        queryString.append("@exo:createdDate ");
      } else {
        queryString.append("@exo:title ");
      }
      if(faqSetting.getOrderType().equals("asc")){
        queryString.append("ascending");
      } else {
        queryString.append("descending");
      }

      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();

      QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
      return pageList ;
    }catch (Exception e) {
      log.error("Failed to get list of question watch: ", e);
    }
    return null ;
  }

  // Going to remove
  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#deleteCategoryWatch(java.lang.String, java.lang.String)
   */
  public void deleteCategoryWatch(String categoryId, String user) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node category = getFAQServiceHome(sProvider).getNode(categoryId) ;
      Map<String, String> emailMap = new HashMap<String, String>() ;
      Value[] emailValues = category.getProperty("exo:emailWatching").getValues() ;
      Value[] userValues = category.getProperty("exo:userWatching").getValues() ;
      for(int i = 0; i < emailValues.length ; i++) {
        emailMap.put(userValues[i].getString(), emailValues[i].getString()) ;
      }
      emailMap.remove(user) ;
      category.setProperty("exo:userWatching", emailMap.keySet().toArray(new String[]{})) ;
      category.setProperty("exo:emailWatching", emailMap.values().toArray(new String[]{})) ;
      category.save() ;		
    }catch (Exception e) {
      log.error("Failed to deleted category watch", e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#unWatchCategory(java.lang.String, java.lang.String)
   */
  public void unWatchCategory(String categoryId, String user) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node category = getFAQServiceHome(sProvider).getNode(categoryId) ;
      Map<String, String> userMap = new HashMap<String, String>() ;
      Value[] emailValues = category.getProperty("exo:emailWatching").getValues() ;
      Value[] userValues = category.getProperty("exo:userWatching").getValues() ;
      for(int i = 0; i < userValues.length ; i++) {
        userMap.put(userValues[i].getString(), emailValues[i].getString()) ;
      }
      userMap.remove(user) ;
      category.setProperty("exo:emailWatching", userMap.values().toArray(new String[]{})) ;
      category.setProperty("exo:userWatching", userMap.keySet().toArray(new String[]{})) ;
      category.save() ;		
    }catch (Exception e) {
      log.error("Failed to unWatch category", e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#unWatchQuestion(java.lang.String, java.lang.String)
   */
  public void unWatchQuestion(String questionId, String user) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node question = getFAQServiceHome(sProvider).getNode(questionId) ;
      Map<String, String> userMap = new HashMap<String, String>() ;
      Value[] emailValues = question.getProperty("exo:emailWatching").getValues() ;
      Value[] userValues = question.getProperty("exo:userWatching").getValues() ;
      for(int i = 0; i < userValues.length ; i++) {
        userMap.put(userValues[i].getString(), emailValues[i].getString()) ;
      }
      userMap.remove(user) ;
      question.setProperty("exo:emailWatching", userMap.values().toArray(new String[]{})) ;
      question.setProperty("exo:userWatching", userMap.keySet().toArray(new String[]{})) ;
      question.save() ;
    }catch (Exception e) {
      log.error("Unwatching question failed: ", e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getSearchResults(org.exoplatform.faq.service.FAQEventQuery)
   */
  public List<ObjectSearchResult> getSearchResults(FAQEventQuery eventQuery) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();

    eventQuery.setViewingCategories(getViewableCategoryIds(sProvider)) ;
    List<String> retrictedCategoryList = new ArrayList<String>() ;
    if(!eventQuery.isAdmin()) retrictedCategoryList = getRetrictedCategories(eventQuery.getUserId(), eventQuery.getUserMembers()) ;

    Node categoryHome = getCategoryHome(sProvider, null) ;
    eventQuery.setPath(categoryHome.getPath()) ;
    try {
      QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager() ;
      //System.out.println("Query ====>" + eventQuery.getQuery());
      Query query = qm.createQuery(eventQuery.getQuery(), Query.XPATH) ;
      QueryResult result = query.execute() ;
      NodeIterator iter = result.getNodes() ;
      //System.out.println("size ====>" + iter.getSize());
      Node nodeObj = null;
      if(eventQuery.getType().equals(FAQEventQuery.FAQ_CATEGORY)){ // Category search
        List<ObjectSearchResult> results = new ArrayList<ObjectSearchResult> () ;
        while (iter.hasNext()) {
          if(eventQuery.isAdmin()) {
            Node cat = iter.nextNode() ;
            //for retricted audiences
            if(retrictedCategoryList.size() > 0) {
              String path = cat.getPath() ;
              for(String id : retrictedCategoryList) {
                if(path.indexOf(id) > 0) {
                  results.add(getResultObj(cat));
                  break ;
                }
              }							
            }else {
              results.add(getResultObj(cat));
            }						
          }else {						
            results.add(getResultObj(iter.nextNode()));
          }

        }
        return results ;
      } else if(eventQuery.getType().equals(FAQEventQuery.FAQ_QUESTION)){ // Question search
        List<ObjectSearchResult> results = new ArrayList<ObjectSearchResult> () ;
        Map<String, Node> mergeQuestion = new HashMap<String, Node>();
        Map<String, Node> mergeQuestion2 = new HashMap<String, Node>();
        List<Node> listQuestion = new ArrayList<Node>();
        List<Node> listLanguage = new ArrayList<Node>();
        Map<String,Node> listAnswerandComment = new HashMap<String, Node>();
        while(iter.hasNext()){
          nodeObj = iter.nextNode();
          if(!eventQuery.isAdmin()) {
            try {
              if(nodeObj.isNodeType("exo:faqQuestion")){
                if((nodeObj.getProperty("exo:isApproved").getBoolean() == true 
                    && nodeObj.getProperty("exo:isActivated").getBoolean() == true ) 
                    || (nodeObj.getProperty("exo:author").getString().equals(eventQuery.getUserId()) 
                        && nodeObj.getProperty("exo:isActivated").getBoolean() == true))
                  //for retricted audiences
                  if(retrictedCategoryList.size() > 0) {
                    String path = nodeObj.getPath() ;
                    boolean isCanView = true ;
                    for(String id : retrictedCategoryList) {
                      if(path.indexOf(id) > 0) {
                        isCanView = false ;
                        break ;
                      }
                    }
                    if(isCanView) listQuestion.add(nodeObj) ;
                  }else {
                    listQuestion.add(nodeObj) ;
                  }

              }

              if(nodeObj.isNodeType("exo:faqResource")){
                Node nodeQuestion = nodeObj.getParent().getParent() ; 
                if((nodeQuestion.getProperty("exo:isApproved").getBoolean() == true 
                    && nodeQuestion.getProperty("exo:isActivated").getBoolean() == true ) 
                    || (nodeQuestion.getProperty("exo:author").getString().equals(eventQuery.getUserId())
                        && nodeQuestion.getProperty("exo:isActivated").getBoolean() == true))
                  //for retricted audiences
                  if(retrictedCategoryList.size() > 0) {
                    boolean isCanView = true ;
                    String path = nodeObj.getPath() ;
                    for(String id : retrictedCategoryList) {
                      if(path.indexOf(id) > 0) {
                        isCanView = false ;
                        break ;
                      }
                    }
                    if(isCanView) listQuestion.add(nodeQuestion) ;
                  }else {
                    listQuestion.add(nodeQuestion) ;
                  }

              }

              if(nodeObj.isNodeType("exo:faqLanguage")){
                Node nodeQuestion = nodeObj.getParent().getParent() ; 
                if((nodeQuestion.getProperty("exo:isApproved").getBoolean() == true 
                    && nodeQuestion.getProperty("exo:isActivated").getBoolean() == true ) 
                    || (nodeQuestion.getProperty("exo:author").getString().equals(eventQuery.getUserId())
                        && nodeQuestion.getProperty("exo:isActivated").getBoolean() == true))
                  //for retricted audiences
                  if(retrictedCategoryList.size() > 0) {
                    boolean isCanView = true ;
                    String path = nodeObj.getPath() ;
                    for(String id : retrictedCategoryList) {
                      if(path.indexOf(id) > 0) {
                        isCanView = false ;
                        break ;
                      }
                    }
                    if(isCanView) listLanguage.add(nodeObj) ;
                  }else {
                    listLanguage.add(nodeObj) ;
                  }									
              }

              if(nodeObj.isNodeType("exo:answer") || nodeObj.isNodeType("exo:comment")){ //answers of default language
                String quesId = nodeObj.getProperty("exo:questionId").getString() ;
                if(!listAnswerandComment.containsKey(quesId)) {
                  Node nodeQuestion = nodeObj.getParent().getParent();
                  if(nodeQuestion.isNodeType("exo:faqQuestion")) {
                    if((nodeQuestion.getProperty("exo:isApproved").getBoolean() == true 
                        && nodeQuestion.getProperty("exo:isActivated").getBoolean() == true ) 
                        || (nodeQuestion.getProperty("exo:author").getString().equals(eventQuery.getUserId())
                            && nodeQuestion.getProperty("exo:isActivated").getBoolean() == true))
                      //for retricted audiences
                      if(retrictedCategoryList.size() > 0) {
                        boolean isCanView = true ;
                        String path = nodeObj.getPath() ;
                        for(String id : retrictedCategoryList) {
                          if(path.indexOf(id) > 0) {
                            isCanView = false ;
                            break ;
                          }
                        }
                        if(isCanView) listAnswerandComment.put(quesId, nodeObj) ;
                      }else {
                        listAnswerandComment.put(quesId, nodeObj) ;
                      }										
                  }else { // answers of other languages									
                    nodeQuestion = nodeObj.getParent().getParent().getParent().getParent() ;
                    if((nodeQuestion.getProperty("exo:isApproved").getBoolean() == true 
                        && nodeQuestion.getProperty("exo:isActivated").getBoolean() == true ) 
                        || (nodeQuestion.getProperty("exo:author").getString().equals(eventQuery.getUserId())
                            && nodeQuestion.getProperty("exo:isActivated").getBoolean() == true)){
                      //for retricted audiences 
                      if(retrictedCategoryList.size() > 0) {
                        boolean isCanView = true;
                        String path = nodeObj.getPath() ;
                        for(String id : retrictedCategoryList) {
                          if(path.indexOf(id) > 0) {
                            isCanView = false ;
                            break ;
                          }
                        }											
                        if(isCanView) listAnswerandComment.put(quesId, nodeObj) ;
                      }else {
                        listAnswerandComment.put(quesId, nodeObj) ;
                      }
                    }																		
                  }

                }

              }							
            } catch (Exception e) {
              log.error("Failed to add item in list search", e);
            }

          }else {
            if(nodeObj.isNodeType("exo:faqQuestion")) listQuestion.add(nodeObj) ;
            if(nodeObj.isNodeType("exo:faqResource")) listQuestion.add(nodeObj.getParent().getParent()) ;
            if(nodeObj.isNodeType("exo:faqLanguage")) listLanguage.add(nodeObj) ;
            if(nodeObj.isNodeType("exo:answer") || nodeObj.isNodeType("exo:comment")) 
              listAnswerandComment.put(nodeObj.getProperty("exo:questionId").getString(),nodeObj) ;
          }					
        }

        //System.out.println("eventQuery.isQuestionLevelSearch() ==>" +	eventQuery.isQuestionLevelSearch() + " - " + listQuestion.size());
        //System.out.println("eventQuery.isAnswerCommentLevelSearch() ==>" +	eventQuery.isAnswerCommentLevelSearch() + " - " + listAnswerandComment.size());
        //System.out.println("eventQuery.isLanguageLevelSearch() ==>" +	eventQuery.isLanguageLevelSearch() + " - " + listLanguage.size());
        //if(eventQuery.isQuestionLevelSearch() && listQuestion.isEmpty()) return results ;
        //if(eventQuery.isAnswerCommentLevelSearch() && listAnswerandComment.isEmpty()) return results ;
        //if(eventQuery.isLanguageLevelSearch() && listLanguage.isEmpty()) return results ;

        boolean isInitiated = false ;
        if(eventQuery.isQuestionLevelSearch()) {
          //directly return because there is only one this type of search
          if(!eventQuery.isLanguageLevelSearch() && !eventQuery.isAnswerCommentLevelSearch()) {
            List<String> list = new ArrayList<String>();
            for(Node node : listQuestion) {
              if(list.contains(node.getName())) continue;
              else list.add(node.getName());
              results.add(getResultObj(node)) ;
            }
            return results ;
          }
          // merging results
          if(!listQuestion.isEmpty()) {
            isInitiated = true ;
            for(Node node : listQuestion) {
              mergeQuestion.put(node.getName(), node) ;
            }
          }
        } 
        if(eventQuery.isLanguageLevelSearch()) {
          //directly return because there is only one this type of search 
          if(!eventQuery.isQuestionLevelSearch() && !eventQuery.isAnswerCommentLevelSearch()) {
            for(Node node : listLanguage) {
              results.add(getResultObj(node)) ;
            }
            return results ;
          }

          // merging results
          if(isInitiated) {
            for(Node node : listLanguage) {
              String id = node.getProperty("exo:questionId").getString() ;
              if(mergeQuestion.containsKey(id)) {
                mergeQuestion2.put(id, mergeQuestion.get(id)) ;
              }							
            }
          }else {
            for(Node node : listLanguage) {
              mergeQuestion2.put(node.getProperty("exo:questionId").getString(), node) ;
            }						
            isInitiated = true ;
          }					
        } 

        if(eventQuery.isAnswerCommentLevelSearch()) {
          //directly return because there is only one this type of search
          if(!eventQuery.isLanguageLevelSearch() && !eventQuery.isQuestionLevelSearch()) {
            for(Node node : listAnswerandComment.values()) {
              results.add(getResultObj(node)) ;
            }
            return results ;
          }
          // merging results
          if(isInitiated) {
            if(eventQuery.isLanguageLevelSearch()) {
              if(mergeQuestion2.isEmpty()) return results ;
              for(Node node : listAnswerandComment.values()) {
                String id = node.getProperty("exo:questionId").getString() ;
                if(mergeQuestion2.containsKey(id)) {
                  results.add(getResultObj(node));
                }							
              }
            }else { // search on question level 
              if(mergeQuestion.isEmpty()) return results ;
              for(Node node : listAnswerandComment.values()) {
                String id = node.getProperty("exo:questionId").getString() ;
                if(mergeQuestion.containsKey(id)) {
                  results.add(getResultObj(node));
                }							
              }
            }						
          }else {
            for(Node node : listAnswerandComment.values()) {
              results.add(getResultObj(node));
            }
          }					
        }
        // mix all result for fultext search on questions
        if (!eventQuery.isQuestionLevelSearch() && !eventQuery.isAnswerCommentLevelSearch() 
            && !eventQuery.isLanguageLevelSearch()) {
          Map<String, ObjectSearchResult> tmpResult = new HashMap<String, ObjectSearchResult>() ;
          ObjectSearchResult rs ;
          for(Node node : listAnswerandComment.values()) {
            rs = getResultObj(node) ;
            tmpResult.put(rs.getId(), rs);
          }
          for(Node node : listQuestion) {
            rs = getResultObj(node) ;
            tmpResult.put(rs.getId(), rs);
          }
          for(Node node : listLanguage) {
            rs = getResultObj(node) ;
            tmpResult.put(rs.getId(), rs);
          }
          results.addAll(tmpResult.values()) ;
        }
        return results ;

      } else if(eventQuery.getType().equals(FAQEventQuery.CATEGORY_AND_QUESTION)){ // Quick search
        String nodePath = "";
        Session session = categoryHome.getSession();
        Map<String, ObjectSearchResult> searchMap = new HashMap<String, ObjectSearchResult>();

        while (iter.hasNext()) {
          boolean isResult = true ;
          nodeObj = iter.nextNode();
          nodePath = nodeObj.getPath();
          if(nodePath.indexOf("/Question") > 0 && nodePath.lastIndexOf("/") >= nodePath.indexOf("/Question")){
            nodePath = nodePath.substring(0, nodePath.indexOf("/Question") + 41);
            nodeObj = (Node) session.getItem(nodePath);
            if(!eventQuery.isAdmin()) {
              try{
                if((nodeObj.getProperty("exo:isApproved").getBoolean() == true 
                    && nodeObj.getProperty("exo:isActivated").getBoolean() == true ) 
                    || (nodeObj.getProperty("exo:author").getString().equals(eventQuery.getUserId())
                        && nodeObj.getProperty("exo:isActivated").getBoolean() == true)) {
                  //for retricted audiences

                  if(retrictedCategoryList.size() > 0) {
                    String path = nodeObj.getPath() ;
                    for(String id : retrictedCategoryList) {
                      if(path.indexOf(id) > 0) {
                        isResult = false ;
                        break ;
                      }
                    }										
                  }
                }else {
                  isResult = false ;
                }								
              }catch(Exception e) { 
                log.debug(nodeObj + " node must exist: ", e);
                isResult = false ;
              }
            }						
          } else if(nodeObj.isNodeType("exo:faqCategory")){
            if(!eventQuery.isAdmin()) {
              //for restricted audiences
              if(retrictedCategoryList.size() > 0) {
                String path = nodeObj.getPath() ;
                for(String id : retrictedCategoryList) {
                  if(path.indexOf(id) > 0) {
                    isResult = false ;
                    break ;
                  }
                }								
              }
            }
            //nodePath = nodePath.substring(0, nodePath.indexOf("/Category") + 41);
            //nodeObj = (Node) session.getItem(nodePath);
          }	
          //System.out.println("node path >>" + nodeObj.getPath());
          if(!searchMap.containsKey(nodeObj.getName()) && isResult)	{						
            searchMap.put(nodeObj.getName(), getResultObj(nodeObj)) ;
          }					
        }
        return	new ArrayList<ObjectSearchResult> (searchMap.values());
      }			
    } catch (Exception e) {
      throw e;
    }
    return new ArrayList<ObjectSearchResult> () ;
  }

  private ObjectSearchResult getResultObj(Node node) throws Exception {
    ObjectSearchResult objectResult = new ObjectSearchResult() ;
    if(node.isNodeType("exo:faqCategory")){
      objectResult.setIcon("FAQCategorySearch") ;
      objectResult.setName(node.getProperty("exo:name").getString()) ;
      objectResult.setType("faqCategory");
      String path = node.getPath() ;
      objectResult.setId(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
      objectResult.setCreatedDate(node.getProperty("exo:createdDate").getDate().getTime()) ;
    } else {
      if(node.isNodeType("exo:faqQuestion")) {
        if(questionHasAnswer(node)) {
          objectResult.setIcon("QuestionSearch") ;
        } else {
          objectResult.setIcon("NotResponseSearch") ;
        }
        objectResult.setName(node.getProperty("exo:title").getString()) ;
        String path = node.getPath() ;
        objectResult.setId(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
        objectResult.setCreatedDate(node.getProperty("exo:createdDate").getDate().getTime()) ;
      }else{
        objectResult.setIcon("QuestionSearch") ;
        String nodePath = node.getPath() ;
        //if(nodePath.indexOf("/Question") > 0 && nodePath.lastIndexOf("/") > nodePath.indexOf("/Question")){
        nodePath = nodePath.substring(0, nodePath.indexOf("/Question") + 41);
        Node questionNode = (Node) node.getSession().getItem(nodePath);
        objectResult.setName(questionNode.getProperty("exo:title").getString()) ;
        String path = questionNode.getPath() ;
        objectResult.setId(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
        objectResult.setCreatedDate(questionNode.getProperty("exo:createdDate").getDate().getTime()) ;
        //}				
      }
      objectResult.setType("faqQuestion");			
    }

    return objectResult ; 
  }
  /*public List<Question> searchQuestionWithNameAttach(FAQEventQuery eventQuery) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<Question> questionList = new ArrayList<Question>() ;
		Map<String, Question> newMap = new HashMap<String, Question>();
		try {
			Node faqServiceHome = getFAQServiceHome(sProvider) ;			
			QueryManager qm = faqServiceHome.getSession().getWorkspace().getQueryManager() ;
			String path = eventQuery.getPath() ;
			if(path == null || path.length() <= 0) {
				path = faqServiceHome.getPath() ;
			}
			eventQuery.setPath(path) ;
			String type = eventQuery.getType() ;
			String queryString = eventQuery.getQuery() ;
			Query query = qm.createQuery(queryString, Query.XPATH) ;
			QueryResult result = query.execute() ;
			NodeIterator iter = result.getNodes() ;
			while (iter.hasNext()) {
				try {
					if(type.equals("faqAttachment")) {
						Node nodeObj = (Node) iter.nextNode();
						nodeObj = (Node) nodeObj.getParent() ;
						Question question = getQuestion(nodeObj) ;
						newMap.put(question.getId(), question);
					}
				}catch (Exception e) {}
		} catch (Exception e) {
		}
		questionList.addAll(Arrays.asList(newMap.values().toArray(new Question[]{})));
		return questionList ;
	}*/

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getCategoryPath(java.lang.String)
   */
  public List<String> getCategoryPath(String categoryId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    List<String> breadcums = new ArrayList<String>() ;
    try {
      Node category = getFAQServiceHome(sProvider).getNode(categoryId) ;
      while(!category.getName().equals(Utils.CATEGORY_HOME)) {
        breadcums.add(category.getName()) ;
        category = category.getParent() ;
      }
    }catch(Exception e) {
      log.error("Failed to get category: ", e);
    }
    return breadcums;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getParentCategoriesName(java.lang.String)
   */
  public String getParentCategoriesName(String path) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    StringBuilder names = new StringBuilder();
    List<String> list = new ArrayList<String>();
    try {
      Node category = getFAQServiceHome(sProvider).getNode(path) ;
      while(category.isNodeType("exo:faqCategory")) {
        if(category.hasProperty("exo:name")){
          list.add(category.getProperty("exo:name").getString());
        } else {
          list.add(category.getName());
        }
        category = category.getParent() ;
      }
      for (int i = list.size()-1; i >= 0; i--) {
        if(i != list.size()-1)names.append(" > ");
        names.append(list.get(i));
      }
    }catch (Exception e) {
      log.error("Failed to get parent categories name", e);
    }
    return names.toString();
  }

  private void sendEmailNotification(List<String> addresses, Message message) throws Exception {
    /*Calendar cal = new GregorianCalendar();
		PeriodInfo periodInfo = new PeriodInfo(cal.getTime(), null, 1, 86400000);
		String name = String.valueOf(cal.getTime().getTime()) ;
		JobInfo info = new JobInfo(name, "KnowledgeSuite-faq", Class.forName("org.exoplatform.faq.service.notify.NotifyJob"));
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		JobSchedulerService schedulerService = 
			(JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
		messagesInfoMap_.put(name, new NotifyInfo(addresses, message)) ;
		schedulerService.addPeriodJob(info, periodInfo);*/
    pendingMessagesQueue.add(new NotifyInfo(addresses, message)) ;
  }

  public Iterator<NotifyInfo> getPendingMessages() throws Exception {
    Iterator<NotifyInfo>pending = new ArrayList<NotifyInfo>(pendingMessagesQueue).iterator();
    pendingMessagesQueue.clear() ;
    return pending;
  } 

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getMessageInfo(java.lang.String)
   */
  public NotifyInfo getMessageInfo(String name) throws Exception {
    NotifyInfo messageInfo = messagesInfoMap_.get(name) ;
    messagesInfoMap_.remove(name) ;
    return	messageInfo ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#swapCategories(java.lang.String, java.lang.String)
   */
  public void swapCategories(String cateId1, String cateId2) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      String[] strs = Utils.splitForFAQ(cateId2);
      boolean isTop = (strs.length > 1 && strs[1].trim().length() > 0);
      Node faqHome = getFAQServiceHome(sProvider);
      Node goingCategory = faqHome.getNode(cateId1);
      Node mockCategory = faqHome.getNode(strs[0]);
      long index = (isTop) ? 0 : mockCategory.getProperty("exo:index").getLong();
      if (goingCategory.getParent().getPath().equals(mockCategory.getParent().getPath())) {
        long gindex = goingCategory.getProperty("exo:index").getLong();
        resetIndex(goingCategory, index, gindex);
      } else {
        String id = goingCategory.getName();
        Node parentNode = goingCategory.getParent();
        mockCategory.getSession().move(goingCategory.getPath(), mockCategory.getParent().getPath() + "/" + id);
        faqHome.getSession().save();
        Node parent = mockCategory.getParent();
        Node destCat = parent.getNode(id);
        long l = 1;
        if (!isTop) {
          l = parent.getNodes().getSize();
          destCat.setProperty("exo:index", l);
          parent.save();
        }
        resetIndex(destCat, index, l);
        // update index for children categories of parent category moved.
        reUpdateIndex(parentNode);
      }
    } catch (Exception e) {
      log.error("Failed to swap categories", e);
    } 
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#saveTopicIdDiscussQuestion(java.lang.String, java.lang.String)
   */
  public void saveTopicIdDiscussQuestion(String questionId, String topicId) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
      questionNode.setProperty("exo:topicIdDiscuss", topicId);
      questionNode.save() ;
    } catch (Exception e) {
      log.error("Failed to save topic discuss question: ", e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#exportData(java.lang.String, boolean)
   */
  public InputStream exportData(String categoryId, boolean createZipFile) throws Exception{
    Node categoryNode = getCategoryNodeById(categoryId);
    Session session = categoryNode.getSession();
    ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
    File file = null;
    List<File> listFiles = new ArrayList<File>();
    Calendar date = GregorianCalendar.getInstance() ;
    session.exportSystemView(categoryNode.getPath(), bos, false, false ) ;
    listFiles.add(org.exoplatform.ks.common.Utils.getXMLFile(bos, "eXo Knowledge Suite - Answers", "Category", date.getTime(), categoryNode.getName()));
    ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream("exportCategory.zip"));
    try {
      int byteReads;
      byte[] buffer = new byte[4096]; // Create a buffer for copying
      FileInputStream inputStream = null;
      ZipEntry zipEntry = null;
      for(File f : listFiles){
        try {
          inputStream = new FileInputStream(f);
          zipEntry = new ZipEntry(f.getPath());
          zipOutputStream.putNextEntry(zipEntry);
          while((byteReads = inputStream.read(buffer)) != -1)
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


  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#importData(java.lang.String, java.io.InputStream, boolean)
   */
  public boolean importData(String parentId, InputStream inputStream, boolean isZip) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
    	List<String> patchNodeImport = new ArrayList<String>();
    	Node categoryNode = getFAQServiceHome(sProvider).getNode(parentId);
    	Session session = categoryNode.getSession();
    	NodeIterator iter = categoryNode.getNodes();
    	while (iter.hasNext()) {
    		patchNodeImport.add(iter.nextNode().getName());
    	}
      if(isZip){ // Import from zipfile
        ZipInputStream zipStream = new ZipInputStream(inputStream) ;
        ZipEntry entry ;
        while((entry = zipStream.getNextEntry()) != null) {
          ByteArrayOutputStream out= new ByteArrayOutputStream();
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
				iter = getQuestionsIterator(nodeParentQuestion, "", true);
				// Update number answers and regeister question node listener
				while (iter.hasNext()) {
					Node node = iter.nextNode();
					reUpdateNumberOfPublicAnswers(node);
					registerQuestionNodeListener(node);
				}
			}
    }catch(Exception e) {
      log.error("Failed to import data in category " + parentId, e);
      return false ;
    }
    return true ;
  }
  //

  private void calculateImportRootCategory(Node categoryRootNode) throws Exception {
    try {
      NodeIterator iterator0 = categoryRootNode.getNodes();
      int i = 0;
      while (iterator0.hasNext()) {
        if(iterator0.nextNode().isNodeType("exo:faqCategory")){ i = i + 1;}
      }
      Node categoryNode = categoryRootNode.getNode(Utils.CATEGORY_HOME);
      NodeIterator iterator = categoryNode.getNodes();
      String rootPath = categoryRootNode.getPath();
      Session session = categoryRootNode.getSession();
      Workspace workspace = session.getWorkspace();
      while (iterator.hasNext()) {
        Node node = iterator.nextNode();
        try {
          if(node.isNodeType("exo:faqCategory")){
            node.setProperty("exo:index", i);
            i = i + 1;
            workspace.move(node.getPath(), rootPath+"/"+node.getName());
          } else if(node.isNodeType("exo:faqQuestionHome")){
            if(categoryRootNode.hasNode(Utils.QUESTION_HOME)){
              NodeIterator iter = node.getNodes();
              while (iter.hasNext()) {
                Node node_ = iter.nextNode();
                workspace.move(node_.getPath(), rootPath+"/"+Utils.QUESTION_HOME+"/"+node_.getName());
              }
            } else {
              workspace.move(node.getPath(), rootPath+"/"+node.getName());
            }
          }
        } catch (Exception e) {}
      }
      categoryNode.remove();
      session.save();
    } catch (Exception e) {}
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#isExisting(java.lang.String)
   */
  public boolean isExisting(String path) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    	return getFAQServiceHome(sProvider).hasNode(path);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getCategoryPathOf(java.lang.String)
   */
  public String getCategoryPathOf(String id) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node node = getFAQServiceHome(sProvider).getNode(id) ;
      String path;	
      if(node.isNodeType("exo:faqQuestion"))path = node.getParent().getParent().getPath() ;
      else if(node.isNodeType("exo:faqCategory")) path =	node.getPath() ;
      else return null ;
      return path.substring(path.indexOf(Utils.CATEGORY_HOME)) ;
    }catch (Exception e) {			
      log.error("Failed to get category of path: "+id, e);
    }
    return null ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#isModerateAnswer(java.lang.String)
   */
  public boolean isModerateAnswer(String id) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node node = getFAQServiceHome(sProvider).getNode(id) ;
      if(node.isNodeType("exo:faqQuestion")) node = node.getParent().getParent() ;
      return node.getProperty("exo:isModerateAnswers").getBoolean() ;
    }catch (PathNotFoundException e) {
      return false;
    }catch (Exception e) {
      log.error("Failed to check moderate answer", e);
    }
    return false ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#isModerateQuestion(java.lang.String)
   */
  public boolean isModerateQuestion(String id) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node node = getFAQServiceHome(sProvider).getNode(id) ;
      if(node.isNodeType("exo:faqQuestion")) node = node.getParent().getParent() ;
      return node.getProperty("exo:isModerateQuestions").getBoolean() ;
    } catch (PathNotFoundException e) {
      return false;
    } catch (Exception e) {
      log.error("Failed to moderate question", e);
    }
    return false ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#isViewAuthorInfo(java.lang.String)
   */
  public boolean isViewAuthorInfo(String id) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node node ;
      if(id == null) node = getCategoryHome(sProvider, null) ;
      else node = getCategoryNodeById(sProvider, id); ;
      if(node.isNodeType("exo:faqQuestion")) node = node.getParent().getParent() ;
      return new PropertyReader(node).bool("exo:viewAuthorInfor", false);
    }catch (Exception e) {
      log.error("Failed to check view author infor", e);
    }
    return false ;
  }

  public boolean isCategoryModerator(String categoryId, String user) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    boolean isCalMod = false;
    try{
      List<String> userGroups = UserHelper.getAllGroupAndMembershipOfUser(user);
      Node node = getCategoryNodeById(sProvider, categoryId);

      PropertyReader reader = new PropertyReader(node);
      List<String> values = reader.list("exo:moderators", new ArrayList<String>()) ;
      if(!values.isEmpty())
        isCalMod =	Utils.hasPermission(userGroups, values);
    }catch(Exception e) {
      log.error("Cheking whether category moderator failed: ", e); 
    }
    return isCalMod ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#isCategoryExist(java.lang.String, java.lang.String)
   */
  @Override
  public boolean isCategoryExist(String name, String path) {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    if (StringUtils.isEmpty(path)) {
      path = Utils.CATEGORY_HOME;
    }
    try {
      NodeIterator iter = getFAQServiceHome(sProvider).getNode(path).getNodes();
      while (iter.hasNext()) {
        Node catNode = iter.nextNode();
        if (new PropertyReader(catNode).string("exo:name", "").equalsIgnoreCase(name)) {
          return true;
        }
      }
    } catch (Exception e) {
      if (log.isWarnEnabled()) {
        log.warn("Cheking whether catagory is exist: ", e);
      }
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getQuestionContents(java.util.List)
   */
  public List<String> getQuestionContents(List<String> paths) throws Exception {
    List<String> contents = new ArrayList<String>() ;
    for(String path : paths) {
      try{
        contents.add(getQuestionNodeById(path).getProperty("exo:title").getString()) ;
      }catch (Exception e) {}
    }
    return contents ;
  }

  //will be remove
  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getQuestionNodeById(java.lang.String)
   */
  public Node getQuestionNodeById(String path) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node serviceHome = getFAQServiceHome(sProvider) ;
      try {
        return serviceHome.getNode(path) ;
      } catch (PathNotFoundException e) {
        StringBuffer queryString = new StringBuffer("/jcr:root").append(serviceHome.getPath()). 
        append("//element(*,exo:faqQuestion)[fn:name()='").append(path).append("']");
        QueryManager qm = serviceHome.getSession().getWorkspace().getQueryManager();
        Query query = qm.createQuery(queryString.toString(), Query.XPATH);
        QueryResult result = query.execute();		
        NodeIterator iter = result.getNodes() ;
        if(iter.getSize() > 0)
          return iter.nextNode();
      }
    }catch (Exception e) {
      log.error("Failed to get question node by path:"+path, e);
    }
    return null ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getModeratorsOf(java.lang.String)
   */
  public String[] getModeratorsOf(String path) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node node = getFAQServiceHome(sProvider).getNode(path) ;
      if(node.isNodeType("exo:faqQuestion")) {
        return Utils.valuesToArray(node.getParent().getParent().getProperty("exo:moderators").getValues()) ;
      }else if(node.isNodeType("exo:faqCategory")){
        return Utils.valuesToArray(node.getProperty("exo:moderators").getValues()) ;
      }
    }catch (Exception e) {			
      log.error("Failed to get moderators of path: " + path, e);
    }
    return new String[]{} ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getCategoryNameOf(java.lang.String)
   */
  public String getCategoryNameOf(String categoryPath) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node node = getFAQServiceHome(sProvider).getNode(categoryPath) ;
      if(node.hasProperty("exo:name")) return node.getProperty("exo:name").getString() ;
      return node.getName() ;
    }catch (Exception e) {			
      log.error("Failed to get category name of path: " + categoryPath, e);
    }
    return null ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#getCategoryInfo(java.lang.String, java.util.List)
   */
  public CategoryInfo getCategoryInfo(String categoryPath, List<String> categoryIdScoped) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    CategoryInfo categoryInfo = new CategoryInfo() ;
    try {
      Node categoryNode = getFAQServiceHome(sProvider).getNode(categoryPath) ;
      categoryInfo.setId(categoryNode.getName()) ;
      String path = categoryNode.getPath() ;
      categoryInfo.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
      if(categoryNode.hasProperty("exo:name"))
        categoryInfo.setName(categoryNode.getProperty("exo:name").getString()) ;
      else categoryInfo.setName(categoryNode.getName());
      // set Path Name
      Node node = categoryNode;
      List<String> pathName = new ArrayList<String>();
      String categoryName ;
      while (node.isNodeType("exo:faqCategory")) {
        if(node.hasProperty("exo:name"))
          categoryName = node.getProperty("exo:name").getString();
        else categoryName = node.getName();
        pathName.add(categoryName);
        node = node.getParent();
      }
      categoryInfo.setPathName(pathName);
      //declare question info			
      categoryInfo.setQuestionInfos(getQuestionInfo(categoryNode)) ;

      //declare category info
      if(categoryNode.hasNodes()) {
        List<SubCategoryInfo> subList = new ArrayList<SubCategoryInfo>() ;
        NodeIterator subIter = categoryNode.getNodes() ;
        Node sub ;
        SubCategoryInfo subCat ;
        while (subIter.hasNext()){
          sub = subIter.nextNode() ;
          if(categoryIdScoped.isEmpty() || categoryIdScoped.contains(sub.getName())){
            if(sub.isNodeType("exo:faqCategory")) {
              subCat = new SubCategoryInfo() ;
              subCat.setId(sub.getName());
              subCat.setName(sub.getProperty("exo:name").getString()) ;						
              subCat.setPath(categoryInfo.getPath()+ "/" + sub.getName()) ;
              subCat.setSubCateInfos(getSubCategoryInfo(sub, categoryIdScoped)) ;
              subCat.setQuestionInfos(getQuestionInfo(sub)) ;
              subList.add(subCat) ;
            }
          }
        }
        categoryInfo.setSubCateInfos(subList) ;
      }
    }catch(Exception e) {
      return null;
    }
    return categoryInfo ;
  }


  private void registerQuestionNodeListener(Node questionNode) {
    try {
      ObservationManager observation = questionNode.getSession().getWorkspace().getObservationManager();
      QuestionNodeListener listener = new QuestionNodeListener();
      observation.addEventListener(listener,
                                   Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_ADDED
                                   | Event.PROPERTY_CHANGED,
                                   questionNode.getPath(),
                                   true,
                                   null,
                                   null,
                                   false);
    } catch (Exception e) {
      log.error("can not add listener to question node", e);
    } 

  }

  public void reCalculateInfoOfQuestion(String absPathOfProp) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try {
      Item item = null;
      Node quesNode = null;
      Node serviceHomeNode = getFAQServiceHome(sProvider);

      // ----- get Question Node -------------
      int quesNameIndex = absPathOfProp.lastIndexOf(Utils.QUESTION_HOME)
      + Utils.QUESTION_HOME.length() + 2;
      String quesPath = absPathOfProp.substring(0, absPathOfProp.indexOf("/", quesNameIndex));
      try {
        quesNode = (Node) serviceHomeNode.getSession().getItem(quesPath);
      } catch (PathNotFoundException pe) {
        return;
      }
      String lastActivityInfo = null;
      if (quesNode.hasProperty("exo:lastActivity"))
        lastActivityInfo = quesNode.getProperty("exo:lastActivity").getString();
      long timeOfLastActivity = Utils.getTimeOfLastActivity(lastActivityInfo);
      long numberOfAnswers = 0;
      if (quesNode.hasProperty("exo:numberOfPublicAnswers")) {
        numberOfAnswers = quesNode.getProperty("exo:numberOfPublicAnswers").getLong();
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
        if (prop.getName().equalsIgnoreCase("exo:activateResponses")
            || prop.getName().equalsIgnoreCase("exo:approveResponses")) {
          // if activate or approve property has been changed.
          boolean value = prop.getBoolean();
          Node answerNode = prop.getParent();
          boolean isActivated = false, isApproved = false;
          if (answerNode.hasProperty("exo:activateResponses"))
            isActivated = answerNode.getProperty("exo:activateResponses").getBoolean();
          if (answerNode.hasProperty("exo:approveResponses"))
            isApproved = answerNode.getProperty("exo:approveResponses").getBoolean();
          long answerTime = 0;
          if (answerNode.hasProperty("exo:dateResponse"))
            answerTime = answerNode.getProperty("exo:dateResponse").getDate().getTimeInMillis();
          if (isActivated && isApproved) {
            numberOfAnswers++;
            quesNode.setProperty("exo:numberOfPublicAnswers", numberOfAnswers);
            // admin changed this answer to public ...
            if (timeOfLastActivity < answerTime) {
              String author = answerNode.getProperty("exo:responseBy").getString();
              quesNode.setProperty("exo:lastActivity", author + "-" + String.valueOf(answerTime));
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
        if (node.getPrimaryNodeType().getName().equalsIgnoreCase("exo:comment")) {
          long commentTime = node.getProperty("exo:dateComment").getDate().getTimeInMillis();
          if (commentTime > timeOfLastActivity) {
            String author = node.getProperty("exo:commentBy").getString();
            quesNode.setProperty("exo:lastActivity", author + "-" + String.valueOf(commentTime));
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
    sb.append("/jcr:root").append(questionNode.getPath()).append("//element(*, exo:answer)[@exo:activateResponses='true' and @exo:approveResponses='true']");
    Query query =	qm.createQuery(sb.toString(), Query.XPATH);
    QueryResult result = query.execute();
    long size = result.getNodes().getSize();
    size = size < 0 ? 0 : size;
    questionNode.setProperty("exo:numberOfPublicAnswers", size);
    questionNode.save();

  }

  private void reUpdateLastActivityOfQuestion(Node quesNode) throws RepositoryException {
    QueryManager qm = quesNode.getSession().getWorkspace().getQueryManager();

    StringBuilder sb = new StringBuilder();
    sb.append("/jcr:root").append(quesNode.getPath()).append("//element(*, exo:answer)[@exo:activateResponses='true' and @exo:approveResponses='true'] order by @exo:dateResponse descending");
    QueryImpl query = (QueryImpl) qm.createQuery(sb.toString(), Query.XPATH);
    query.setLimit(1);
    QueryResult result = query.execute();
    NodeIterator iter = result.getNodes();

    String author = null;
    long lastTime = -1;
    if (iter.hasNext()) {
      Node node = iter.nextNode();

      if (node.hasProperty("exo:dateResponse")) {
        lastTime = node.getProperty("exo:dateResponse").getDate().getTimeInMillis();
      }
      if (node.hasProperty("exo:responseBy")) {
        author = node.getProperty("exo:responseBy").getString();
      }

    }

    sb = new StringBuilder();
    sb.append("/jcr:root").append(quesNode.getPath()).append("//element(*, exo:comment) order by @exo:dateComment descending");
    query = (QueryImpl) qm.createQuery(sb.toString(), Query.XPATH);
    query.setLimit(1);
    result = query.execute();
    iter = result.getNodes();
    if (iter.hasNext()) {
      Node commentNode = iter.nextNode();
      if (commentNode.hasProperty("exo:dateComment") && commentNode.hasProperty("exo:commentBy")) {
        long commentTime = commentNode.getProperty("exo:dateComment").getDate().getTimeInMillis();
        if (lastTime < commentTime) {
          lastTime = commentTime;
          author = commentNode.getProperty("exo:commentBy").getString();
        }
      }
    }
    if (lastTime > 0) {
      quesNode.setProperty("exo:lastActivity", author + "-" + String.valueOf(lastTime));
      quesNode.save() ;
    } else {
      quesNode.setProperty("exo:lastActivity", (String) null);
    }
  }

  private List<SubCategoryInfo> getSubCategoryInfo(Node category, List<String> categoryIdScoped) throws Exception {
    List<SubCategoryInfo> subList = new ArrayList<SubCategoryInfo>() ;
    if(category.hasNodes()) {
      NodeIterator iter = category.getNodes() ;
      Node sub ;
      SubCategoryInfo cat ; 
      while(iter.hasNext()) {
        try{
          sub = iter.nextNode() ;
          if(sub.isNodeType("exo:faqCategory")) {
            if(categoryIdScoped.isEmpty() || categoryIdScoped.contains(sub.getName())){
              cat = new SubCategoryInfo() ;
              cat.setName(sub.getProperty("exo:name").getString()) ;
              String path = sub.getPath() ;
              cat.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
              cat.setId(sub.getName()) ;
              subList.add(cat) ;
            }
          }
        }catch(Exception e) {
          log.error("Failed to get sub category info: ", e);
        }
      }
    }
    return subList ;
  }
  
  private NodeIterator getNodeIteratorAnswerAccess(Node answerHome) throws Exception {
		StringBuffer queryString = new StringBuffer("/jcr:root").append(answerHome.getPath()).append("/element(*,exo:answer)[@exo:approveResponses='true' and @exo:activateResponses='true']");
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
					questionInfo.setQuestion(question.getProperty("exo:title").getString());
					questionInfo.setDetail(question.getProperty("exo:name").getString());
					questionInfo.setId(question.getName());
					if (question.hasNode(Utils.ANSWER_HOME)) {
						List<String> answers = new ArrayList<String>();
						NodeIterator ansIter = getNodeIteratorAnswerAccess(question.getNode(Utils.ANSWER_HOME));
						while (ansIter.hasNext()) {
							answers.add(ansIter.nextNode().getProperty("exo:responses").getString());
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

  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.impl.DataStorage#updateQuestionRelatives(java.lang.String, java.lang.String[])
   */
  public void updateQuestionRelatives( String questionPath, String[] relatives) throws Exception{
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node question = getFAQServiceHome(sProvider).getNode(questionPath) ;
      question.setProperty("exo:relatives", relatives) ;
      question.save() ;
    }catch (Exception e) {
      log.error("Failed to update question relatives: ", e);
    }
  }


  public InputStream createAnswerRSS(String cateId) throws Exception {
    SessionProvider sProvider = CommonUtils.createSystemProvider();
    try{
      Node cateNode = getCategoryNodeById(sProvider, cateId);
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      StringBuilder queryString = new StringBuilder("/jcr:root").append(cateNode.getPath()). 
      append("//element(*,exo:faqQuestion)");
      List<String> list = getListCategoryIdPublic(sProvider, cateNode);
      if (!list.isEmpty()) queryString.append("[");
      PropertyReader reader = new PropertyReader(cateNode);
      if(reader.list("exo:userPrivate", new ArrayList<String>()).isEmpty()){
        if(!list.isEmpty())list.add(cateNode.getName());
      }
      boolean isOr = false;
      for (String id : list) {
        if(isOr){
          queryString.append(" or (@exo:categoryId='").append(id).append("')");
        } else {
          queryString.append("(@exo:categoryId='").append(id).append("')");
          isOr = true;
        }
      }
      if (!list.isEmpty()) queryString.append("]");

      QueryManager qm = cateNode.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes() ;
      Node nodeQs;

      while (iter.hasNext()) {
        nodeQs = iter.nextNode();
        if(nodeQs.getParent().getParent().isNodeType("exo:faqCategory")) {
          entries.add(createQuestionEntry(sProvider, nodeQs));
        }
      }

      SyndFeed feed = createNewFeed(cateNode, "http://www.exoplatform.com");
      feed.setEntries(entries);

      SyndFeedOutput output = new SyndFeedOutput();
      String s = output.outputString(feed);
      s = StringUtils.replace(s,"&amp;","&");
      s = s.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
      s = StringUtils.replace(s,"ST[CDATA[","<![CDATA[");
      s = StringUtils.replace(s,"END]]","]]>");

      return new ByteArrayInputStream(s.getBytes());
    }catch (Exception e) {
      log.error("Failed to create answer RSS ", e);
    }
    return null;
  }

  private List<String> getListCategoryIdPublic(SessionProvider sProvider, Node cateNode) throws Exception {
    List<String> list = new ArrayList<String>();

    StringBuilder queryString = new StringBuilder("/jcr:root").append(cateNode.getPath()). 
    append("//element(*,exo:faqCategory)[@exo:isView='true' and ( not(@exo:userPrivate) or @exo:userPrivate='')]") ;

    QueryManager qm = cateNode.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator iter = result.getNodes() ;
    while (iter.hasNext()) {
      list.add(iter.nextNode().getName());
    }
    return list;
  }

  private SyndEntry createQuestionEntry(SessionProvider sProvider, Node questionNode) throws Exception	{
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

  private List<String> getStrAnswers(Node questionNode) throws Exception{
    List<String> listAnswers = new ArrayList<String>();
    try{
      Node answerHome = questionNode.getNode(Utils.ANSWER_HOME);
      QueryManager qm = answerHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(answerHome.getPath()). 
      append("//element(*,exo:answer)[(@exo:approveResponses='true') and (@exo:activateResponses='true')]").append("order by @exo:dateResponse ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator nodeIterator = result.getNodes();
      Node answerNode = null;
      while(nodeIterator.hasNext()){
        answerNode = nodeIterator.nextNode();
        if(answerNode.hasProperty("exo:responses"))
          listAnswers.add("<b><u>Answer:</u></b> "+(answerNode.getProperty("exo:responses").getValue().getString())+"<br/>") ;
      }
    } catch (Exception e){
      log.error("Failed to get answers for " + questionNode.getName());
    }
    return listAnswers;
  }

  private List<String> getComments(Node questionNode) throws Exception{
    List<String> listComment = new ArrayList<String>();
    try{
      Node commentHome = questionNode.getNode(Utils.COMMENT_HOME);
      QueryManager qm = commentHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(commentHome.getPath()). 
      append("//element(*,exo:comment)").append(" order by @exo:dateComment ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator nodeIterator = result.getNodes();
      Node commentNode = null;
      while(nodeIterator.hasNext()){
        commentNode = nodeIterator.nextNode();
        if(commentNode.hasProperty("exo:comments")) 
          listComment.add("<b><u>Comment:</u></b>"+(commentNode.getProperty("exo:comments").getValue().getString())+"<br/>") ;
      }
    } catch (Exception e){
      log.error("Failed to get comments for " + questionNode.getName());
    }
    return listComment;
  }

  private SyndFeed createNewFeed(Node node, String link) throws Exception {
    PropertyReader reader = new PropertyReader(node);
    String desc = reader.string("exo:description", " ");
    SyndFeed feed = new SyndFeedImpl();
    feed.setFeedType("rss_2.0");
    feed.setTitle("ST[CDATA["+reader.string("exo:name", "Root")+"END]]");
    feed.setPublishedDate(reader.date("exo:createdDate", new Date()));
    feed.setLink("ST[CDATA["+link+"END]]");					
    feed.setDescription("ST[CDATA["+desc+"END]]");	
    feed.setEncoding("UTF-8");
    return feed;
  }

  private SyndEntry createNewEntry(Node questionNode, List<String> listContent) throws Exception{
    PropertyReader question = new PropertyReader(questionNode);
    SyndContent description = new SyndContentImpl();
    description.setType("text/plain");
    description.setValue("ST[CDATA["+question.string("exo:title", "") + "<br/>" + (listContent.isEmpty()?"":listContent.get(0)) +"END]]"); 
    SyndEntry entry = new SyndEntryImpl();
    entry.setUri("ST[CDATA["+questionNode.getName()+"END]]");
    entry.setTitle("ST[CDATA["+question.string("exo:title")+"END]]");
    entry.setLink("ST[CDATA["+question.string("exo:link", "http://www.exoplatform.com")+"END]]");
    entry.setContributors(listContent);
    entry.setDescription(description);
    entry.setPublishedDate(question.date("exo:createdDate", new Date()));
    entry.setAuthor("ST[CDATA["+question.string("exo:author")+"END]]");
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
}

