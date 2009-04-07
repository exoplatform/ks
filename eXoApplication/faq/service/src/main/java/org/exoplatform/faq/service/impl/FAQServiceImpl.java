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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.faq.service.impl;

import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.mail.Message;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 04, 2008  
 */
public class FAQServiceImpl implements FAQService, Startable{	
  
	public static final int CATEGORY = 1 ;
	public static final int QUESTION = 2 ;
	public static final int SEND_EMAIL = 1 ;
	private JCRDataStorage jcrData_ ;
	private MultiLanguages multiLanguages_ ;
	public static long maxUploadSize_ ;
	//private EmailNotifyPlugin emailPlugin_ ;
	
	public FAQServiceImpl(NodeHierarchyCreator nodeHierarchy, InitParams params) throws Exception {
		jcrData_ = new JCRDataStorage(nodeHierarchy) ;
		multiLanguages_ = new MultiLanguages() ;
		try {
			PropertiesParam proParams = params.getPropertiesParam("upload-limit-config");
			if (proParams != null) {
				String maximum = proParams.getProperty("maximum.upload");
				if (maximum != null && maximum.length() > 0) {
						maxUploadSize_ = Long.parseLong(maximum)*1048576;
	    	}
	    }
		} catch (Exception e) {
			maxUploadSize_ = 0;
		}
	}
	
	public void addPlugin(ComponentPlugin plugin) throws Exception {
		jcrData_.addPlugin(plugin) ;
	}
	
	public void addRolePlugin(ComponentPlugin plugin) throws Exception {
		jcrData_.addRolePlugin(plugin) ;
	}
	
	/**
	 * This method get all admin in FAQ
	 * @return userName list
	 * @throws Exception the exception
	 */
	public List<String> getAllFAQAdmin() throws Exception {
		return jcrData_.getAllFAQAdmin() ;
	}
	
	/**
	 * This method get all the category
	 * @param	 sProvider is session provider
	 * @return Category list
	 * @throws Exception the exception
	 */
	public List<Category> getAllCategories(SessionProvider sProvider) throws Exception {
		return jcrData_.getAllCategories(sProvider);
	}
	
	/**
	 * This method get all the question 
	 * and convert to list of question object (QuestionPageList)
	 * @param		sProvider
	 * @return 	QuestionPageList
	 * @throws Exception the exception
	 */
	public QuestionPageList getAllQuestions(SessionProvider sProvider) throws Exception {
		return jcrData_.getAllQuestions(sProvider);
	}
  
	/**
	 * This method get all question node have not yet answer 
	 * and convert to list of question object (QuestionPageList)
	 * @param		sProvider
	 * @return	QuestionPageList
	 * @throws Exception the exception
	 */
	public QuestionPageList getQuestionsNotYetAnswer(SessionProvider sProvider, String categoryId, FAQSetting setting) throws Exception {
	  return jcrData_.getQuestionsNotYetAnswer(sProvider, categoryId, setting);
	}
	
	/**
	 *This method get some informations of category: 
	 *to count sub-categories, to count questions, to count question have not yet answer,
	 *to count question is not approved are contained in this category
   * 
   * @param   categoryId
   * @param   sProvider
   * @return  number of (sub-categories, questions, questions is not approved,question is have not yet answered)
   * @throws Exception the exception
	 */
	public long[] getCategoryInfo(String categoryId, SessionProvider sProvider, FAQSetting setting) throws Exception {
		return jcrData_.getCategoryInfo(categoryId, sProvider, setting);
	}
  
	/**
	 * Returns an category that can then be get property of this category.
	 * <p>
	 *
	 * @param  	categoryId is address id of the category so you want get
	 * @param  	sProvider
	 * @return  category is id equal categoryId
	 * @see     current category
	 * @throws Exception the exception
	 */
	public Category getCategoryById(String categoryId, SessionProvider sProvider) throws Exception {
	  return jcrData_.getCategoryById(categoryId, sProvider);
	}
	
	/**
	 * This method should get question node via identify
   * @param 	question identify
   * @param		sProvider
   * @return 	Question
   * @throws Exception the exception 
	 */
	public Question getQuestionById(String questionId, SessionProvider sProvider) throws Exception {
		return jcrData_.getQuestionById(questionId, sProvider);
	}

	/**
	 * This method should view questions, only question node is activated and approved  via category identify
   * and convert to list of question object
   * 
   * @param		Category identify
   * @param		sProvider
   * @return 	QuestionPageList
   * @throws Exception the exception
	 */
	public QuestionPageList getQuestionsByCatetory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
		return jcrData_.getQuestionsByCatetory(categoryId, sProvider, faqSetting);
	}
  
	/**
	 * This method get all questions via category identify and convert to list of question list
	 * 
	 * @param 	Category identify
	 * @param		sProvider
	 * @return 	QuestionPageList
	 * @throws Exception the exception
	 */
	public QuestionPageList getAllQuestionsByCatetory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
	  return jcrData_.getAllQuestionsByCatetory(categoryId, sProvider, faqSetting);
	}
  
	/**
	 * This method every category should get list question, all question convert to list of question object
	 * @param 	listCategoryId  is list via identify
	 * @param 	isNotYetAnswer  if isNotYetAnswer equal true then return list question is not yet answer
	 * 					isNotYetAnswer  if isNotYetAnswer equal false then return list all questions
	 * @param		sProvider
	 * @return 	QuestionPageList
	 * @throws Exception the exception
	 */
	public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer, SessionProvider sProvider) throws Exception {
	  return jcrData_.getQuestionsByListCatetory(listCategoryId, isNotYetAnswer, sProvider);
	}
  
	/**
	 * This method should lookup languageNode of question
   * and find all child node of language node
   * 
   * @param 	Question identify
   * @param		sProvider
   * @return 	language list
   * @throws Exception the exception
	 */
  public List<QuestionLanguage>  getQuestionLanguages(String questionId, SessionProvider sProvider) throws Exception {
    return jcrData_.getQuestionLanguages(questionId, sProvider) ;
  }
  
  /**
   * This method should lookup languageNode of question
   * so find child node of language node is searched
   * and find properties of child node, if contain input of user, get this question
   * 
   * @param 	Question list
   * @param 	langage want search
   * @param 	term content want search in all field question
   * @param		sProvider 
   * @return 	Question list
   * @throws Exception the exception
   */
  public List<Question> searchQuestionByLangageOfText(List<Question> listQuestion, String languageSearch, String text, SessionProvider sProvider) throws Exception {
    return jcrData_.searchQuestionByLangageOfText(listQuestion, languageSearch, text, sProvider) ;
  }
  
  /**
   * This method should lookup languageNode of question
   * so find child node of language node is searched
   * and find properties of child node, if contain input of user, get this question
   * 
   * @param 	Question list
   * @param 	langage want search
   * @param 	question's content want search
   * @param 	response's content want search
   * @param		sProvider 
   * @return 	Question list
   * @throws Exception the exception
   */
  public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch, SessionProvider sProvider) throws Exception {
    return jcrData_.searchQuestionByLangage(listQuestion, languageSearch, questionSearch, responseSearch, sProvider) ;
  }

  /**
	 * Returns an list category that can then be view on the screen.
	 * <p>
	 * This method always returns immediately, this view list category in screen.
	 * if categoryId equal null then list category is list parent category
	 * else this view list category of one value parent category that you communicate categoryId 
	 *  
	 * @param  	categoryId is address id of the category 
	 * @param  	sProvider
	 * @return  List parent category or list sub category
	 * @see     list category
	 * @throws Exception the exception
	 */
	public List<Category> getSubCategories(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
		return jcrData_.getSubCategories(categoryId, sProvider ,faqSetting);
	}
	
	/**
	 * This method should lookup questions via question identify and from category identify
   * so lookup destination category and move questions to destination category
   * 
   * @param Question identify list
   * @param destination category identify
   * @param sProvider
   * @throws Exception the exception
	 */
	public void moveQuestions(List<String> questions, String destCategoryId, SessionProvider sProvider) throws Exception {
		jcrData_.moveQuestions(questions, destCategoryId, sProvider) ;
	}
	/**
	 * Remove one category in list
	 * <p>
	 * This function is used to remove one category in list
	 * 
	 * @param  	categoryId is address id of the category need remove 
	 * @param  	sProvider
	 * @throws Exception the exception
	 */
	public void removeCategory(String categoryId, SessionProvider sProvider) throws Exception {
		jcrData_.removeCategory(categoryId, sProvider) ;
	}
	
	/**
	 * Remove one question in list
	 * <p>
	 * This function is used to remove one question in list
	 * 
	 * @param  	question identify
	 * @param  	sProvider
	 * @throws Exception the exception
	 */
	public void removeQuestion(String questionId, SessionProvider sProvider) throws Exception {
		jcrData_.removeQuestion(questionId, sProvider) ;
	}


  /**
	 * Add new or edit category in list.
	 * <p>
	 * This function is used to add new or edit category in list. User will input information of fields need
	 * in form add category, so user save then category will persistent in data
	 * 
	 * @param  	parentId is address id of the category parent where user want add sub category
	 * when paretId equal null so this category is parent category else sub category  
	 * @param  	cat is properties that user input to interface will save on data
	 * @param		isAddNew is true when add new category else update category
	 * @param		sProvider
	 * @return  List parent category or list sub category
	 * @see     list category
	 * @throws Exception the exception
	 */
	public void saveCategory(String parentId, Category cat, boolean isAddNew, SessionProvider sProvider) throws Exception {
		jcrData_.saveCategory(parentId, cat, isAddNew, sProvider) ;
	}
	
	/**
	 * This method should create new question or update exists question
	 * @param question is information but user input or edit to form interface of question 
	 * @param isAddNew equal true then add new question
	 * 				isAddNew equal false then update question
	 * @param	sProvider
	 */
	public Node saveQuestion(Question question, boolean isAddNew, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
		return jcrData_.saveQuestion(question, isAddNew, sProvider, faqSetting) ;
	}
  
  /**
   * This function is used to set some properties of FAQ. 
   * <p>
   * This function is used(Users of FAQ Administrator) choose some properties in object FAQSetting
   * 
   * @param	 newSetting is properties of object FAQSetting that user input to interface will save on data
   * @param	 sProvider
   * @return all value depend FAQSetting will configuration follow properties but user choose
   * @throws Exception the exception
   */
  public void saveFAQSetting(FAQSetting faqSetting,String userName, SessionProvider sProvider) throws Exception {
  	jcrData_.saveFAQSetting(faqSetting,userName, sProvider);
  }
  
  /**
	 * Move one category in list to another plate in project.
	 * <p>
	 * This function is used to move category in list. User will right click on category need
	 * move, so user choose in list category one another category want to put
	 * 
	 * @param  	categoryId is address id of the category that user want plate
	 * @param  	destCategoryId is address id of the category that user want put( destination )
	 * @param		sProvider
	 * @return  category will put new plate
	 * @see     no see category this plate but user see that category at new plate
	 * @throws Exception the exception
	 */
  public void moveCategory(String categoryId, String destCategoryId, SessionProvider sProvider) throws Exception {
  	jcrData_.moveCategory(categoryId, destCategoryId, sProvider);
  }
  
  /**
   * This function is used to allow user can watch a category. 
   * You have to register your email for whenever there is new question is inserted 
   * in the category or new category then there will  a notification sent to you.
   * 
   * @param		id of category with user want add watch on that category 
   * @param		value, this address email (multiple value) with input to interface will save on data
   * @param		sProvider
   * @throws Exception the exception
   *  
   */
  public void addWatch(String id, Watch watch, SessionProvider sProvider)throws Exception {
  	jcrData_.addWatch(id, watch, sProvider) ;
  }
  
  /**
   * This method will get list mail of one category. User see list this mails and 
   * edit or delete mail if need
   * 
   * @param		CategoryId is id of category
   * @param		sProvider
   * @return	list email of current category
   * @see			list email where user manager	
   * @throws Exception the exception				
   */
  public QuestionPageList getListMailInWatch(String categoryId, SessionProvider sProvider) throws Exception {
    return jcrData_.getListMailInWatch(categoryId, sProvider); 
  }
  
  /**
   * This function will delete watch in one category 
   * 
   * @param	 categoryId is id of current category
   * @param	 sProvider
   * @param	 emails is location current of one watch with user want delete 
   * @throws Exception the exception
   */
  public void deleteMailInWatch(String categoryId, SessionProvider sProvider, String emails) throws Exception {
  	jcrData_.deleteMailInWatch(categoryId, sProvider, emails);
  }
  
  /**
   * This function will un watch in one category 
   * 
   * @param	 categoryId is id of current category
   * @param	 sProvider
   * @param	 emails is location current of one watch with user want delete 
   * @throws Exception the exception
   */
  public void UnWatch(String categoryId, SessionProvider sProvider, String userCurrent) throws Exception {
  	jcrData_.UnWatch(categoryId, sProvider, userCurrent);
  }
  
  /**
   * This function will un watch in one category 
   * 
   * @param	 categoryId is id of current category
   * @param	 sProvider
   * @param	 emails is location current of one watch with user want delete 
   * @throws Exception the exception
   */
  public void UnWatchQuestion(String questionID, SessionProvider sProvider, String userCurrent) throws Exception {
  	jcrData_.UnWatchQuestion(questionID, sProvider, userCurrent);
  }
  
  /**
   * This method will return list object FAQFormSearch
   * <p>
   * In instance system filter categories and questions coherent value with user need search
   * 
   * @param  	sProvider
   * @param		text is value user input with search.
   * @param		fromDate, toDate is time user want search 
   * @return	list FAQFormSearch
   * @see		 list categories and question was filter
   * @throws Exception the exception
   */
  public List<FAQFormSearch> getAdvancedEmpty(SessionProvider sProvider, String text, Calendar fromDate, Calendar toDate) throws Exception {
  	return jcrData_.getAdvancedEmpty(sProvider, text, fromDate, toDate); 
  }
  
  /**
   * This method will return list category when user input value search
   * <p>
   * With many categories , it's difficult to find a category which user want to see.
   * So to support to users can find their categories more quickly and accurate,
   *  user can use 'Search Category' function
   * 
   * @param	 sProvider
   * @param	 eventQuery is object save value in form advanced search 
   * @throws Exception the exception
   */
  public List<Category> getAdvancedSearchCategory(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
  	return jcrData_.getAdvancedSearchCategory(sProvider, eventQuery); 
  }
  
  /**
   * This method should lookup all the categories node 
   * so find category have user in moderators
   * and convert to category object and return list of category object
   * 
   * @param  user is name when user login
   * @param	 sProvider  
   * @return Category list
   * @throws Exception the exception
   */
  public List<String> getListCateIdByModerator(String user, SessionProvider sProvider) throws Exception {
    return jcrData_.getListCateIdByModerator(user, sProvider); 
  }
  
  /**
   * This method will return list question when user input value search
   * <p>
   * With many questions , it's difficult to find a question which user want to see.
   * So to support to users can find their questions more quickly and accurate,
   *  user can use 'Search Question' function
   * 
   * @param	 sProvider
   * @param	 eventQuery is object save value in form advanced search 
   * @throws Exception the exception
   */
  public List<Question> getAdvancedSearchQuestion(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
  	return jcrData_.getAdvancedSearchQuestion(sProvider, eventQuery) ;
  }
  
  /**
   * This method will return list question when user input value search
   * <p>
   * With many questions , it's difficult to find a question which user want to see.
   * So to support to users can find their questions more quickly and accurate,
   *  user can use 'Search Question' function
   * 
   * @param	 sProvider
   * @param	 eventQuery is object save value in form advanced search 
   * @throws Exception the exception
   */
  public List<Question> searchQuestionWithNameAttach(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
  	return jcrData_.searchQuestionWithNameAttach(sProvider, eventQuery) ;
  }
  
  /**
   * This method return path of category identify
   * @param  category identify
   * @param	 sProvider
   * @return list category name is sort(path of this category)
   * @throws Exception the exception
   */
  public List<String> getCategoryPath(SessionProvider sProvider, String categoryId) throws Exception {
  	return jcrData_.getCategoryPath(sProvider, categoryId) ;
  }
  
  /**
   * This function will send message to address but you want send
   * 
   * @param	 message is object save content with user want send to one or many address email
   * @throws Exception the exception
   */
  public void sendMessage(Message message) throws Exception {
  	jcrData_.sendMessage(message) ;
  }
  
  /**
   * Adds the file language.
   * 
   * @param node the node
   * @param value the value
   * @param mimeType the mine type
   * @param language the language
   * @param isDefault the is default
   * 
   * @throws Exception the exception
   */
	public void addFileLanguage(Node node, Value value, String mimeType, String language, boolean isDefault) throws Exception {
		multiLanguages_.addFileLanguage(node, value, mimeType, language, isDefault) ;		
	}
	
	/**
   * Adds the file language.
   * 
   * @param node the node
   * @param language the language
   * @param mappings the mappings
   * @param isDefault the is default
   * 
   * @throws Exception the exception
   */
	public void addFileLanguage(Node node, String language, Map mappings, boolean isDefault) throws Exception {
		multiLanguages_.addFileLanguage(node, language, mappings, isDefault) ;
	}
	
	/**
   * Adds the language.
   * 
   * @param node the node
   * @param inputs the inputs
   * @param language the language
   * @param isDefault the is default
   * @throws Exception the exception
   */
	public void addLanguage(Node node, Map inputs, String language, boolean isDefault) throws Exception {
		multiLanguages_.addLanguage(node, inputs, language, isDefault) ;
	}
	
	/**
   * Adds the language.
   * 
   * @param node the node
   * @param inputs the inputs
   * @param language the language
   * @param isDefault the is default
   * @param nodeType the node type
   * @throws Exception the exception
   */
	public void addLanguage(Node node, Map inputs, String language, boolean isDefault, String nodeType) throws Exception {
		multiLanguages_.addLanguage(node, inputs, language, isDefault, nodeType) ;
	}
	
	/**
   * Gets the default.
   * 
   * @param node the node
   * @return the default
   * @throws Exception the exception
   */
	public String getDefault(Node node) throws Exception {
		return multiLanguages_.getDefault(node);
	}
	
	/**
   * Gets the language.
   * 
   * @param node the node
   * @param language the language
   * @return the language
   * @throws Exception the exception
   */
	public Node getLanguage(Node node, String language) throws Exception {
		return multiLanguages_.getLanguage(node, language);
	}
	
	/**
   * Gets the supported languages.
   * 
   * @param node the node
   * @return the supported languages
   * @throws Exception the exception
   */
	public List<String> getSupportedLanguages(Node node) throws Exception {
		return multiLanguages_.getSupportedLanguages(node);
	}
	
	/**
   * Sets the default.
   * 
   * @param node the node
   * @param language the language
   * @throws Exception the exception
   */
	public void setDefault(Node node, String language) throws Exception {
		multiLanguages_.setDefault(node, language) ;
	}
	
	/**
   * Adds the language node, when question have multiple language, 
   * each language is a child node of question node.
   * 
   * @param questionNode  the question node which have multiple language
   * @param language the  language which is added in to questionNode
   * @throws Exception    throw an exception when save a new language node
   */
	public void addLanguage(Node questionNode, QuestionLanguage language) throws Exception {
		multiLanguages_.addLanguage(questionNode, language) ;
	}
	
	public void getUserSetting(SessionProvider sProvider, String userName, FAQSetting faqSetting) throws Exception {
		jcrData_.getUserSetting(sProvider, userName, faqSetting);
	}
	
	public NotifyInfo getMessageInfo(String name) throws Exception {
		return jcrData_.getMessageInfo(name) ;
	}
	
	public boolean isAdminRole(String userName, SessionProvider sessionProvider) throws Exception {
	  return jcrData_.isAdminRole(userName, sessionProvider);
  }
	
	public Node getCategoryNodeById(String categoryId, SessionProvider sProvider) throws Exception {
		return jcrData_.getCategoryNodeById(categoryId, sProvider);
	}
	
	public void addWatchQuestion(String questionId, Watch watch, boolean isNew, SessionProvider sessionProvider) throws Exception{
		jcrData_.addWatchQuestion(questionId, watch, isNew, sessionProvider);
	}
	
	public QuestionPageList getListMailInWatchQuestion(String questionId, SessionProvider sProvider) throws Exception {
		return jcrData_.getListMailInWatchQuestion(questionId, sProvider);
	}
	
	public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser, SessionProvider sProvider) throws Exception {
		return jcrData_.getListQuestionsWatch(faqSetting, currentUser, sProvider);
	}
	
	public List<String> getListPathQuestionByCategory(String categoryId, SessionProvider sProvider) throws Exception{
		return jcrData_.getListPathQuestionByCategory(categoryId, sProvider);
	}
	
	public void importData(String categoryId, Session session, InputStream inputStream, boolean isImportCategory, SessionProvider sProvider) throws Exception{
		jcrData_.importData(categoryId, session, inputStream, isImportCategory, sProvider);
	}
	
	public boolean categoryAlreadyExist(String categoryId, SessionProvider sProvider) throws Exception {
		return jcrData_.categoryAlreadyExist(categoryId, sProvider);
	}
	
	public void swapCategories(String parentCateId, String cateId1, String cateId2, SessionProvider sessionProvider) throws Exception{
		jcrData_.swapCategories(parentCateId, cateId1, cateId2, sessionProvider);
	}
	
	public Node getQuestionNodeById(String questionId, SessionProvider sProvider) throws Exception{
		return jcrData_.getQuestionNodeById(questionId, sProvider);
	}
	
	public void generateRSS(String path, int typeEvent) throws Exception  {
		jcrData_.generateRSS(path, typeEvent) ;
	}

	public void saveTopicIdDiscussQuestion(String questionId, String pathDiscuss, SessionProvider sProvider) throws Exception {
		jcrData_.saveTopicIdDiscussQuestion(questionId, pathDiscuss, sProvider);
	}
	
	public long getMaxindexCategory(String parentId, SessionProvider sProvider) throws Exception {
		return jcrData_.getMaxindexCategory(parentId, sProvider);
	}
	
	public Node getRSSNode(SessionProvider sProvider, String categoryId) throws Exception{
		return jcrData_.getRSSNode(sProvider, categoryId);
	}
	
	public void deleteAnswer(String questionId, String answerId, SessionProvider sProvider) throws Exception{
		jcrData_.deleteAnswer(questionId, answerId, sProvider);
	}
	
	public void deleteComment(String questionId, String commentId, SessionProvider sProvider) throws Exception{
		jcrData_.deleteComment(questionId, commentId, sProvider);
	}
	
	public void saveAnswer(String questionId, Answer answer, boolean isNew, SessionProvider sProvider) throws Exception{
		jcrData_.saveAnswer(questionId, answer, isNew, sProvider);
	}
	
	public void saveComment(String questionId, Comment comment, boolean isNew, SessionProvider sProvider) throws Exception{
		jcrData_.saveComment(questionId, comment, isNew, sProvider);
	}
	
	public Comment getCommentById(SessionProvider sProvider, String questionId, String commentId) throws Exception{
		return jcrData_.getCommentById(questionId, commentId, sProvider);
	}
	
	public Answer getAnswerById(String questionId, String answerid, SessionProvider sProvider) throws Exception{
		return jcrData_.getAnswerById(questionId, answerid, sProvider);
	}
	
	public void saveAnswer(String questionId, Answer[] answers, SessionProvider sProvider) throws Exception{
		jcrData_.saveAnswer(questionId, answers, sProvider);
	}

	public JCRPageList getPageListAnswer(SessionProvider sProvider, String questionId, Boolean isSortByVote) throws Exception {
	  return jcrData_.getPageListAnswer(sProvider, questionId, isSortByVote);
  }

	public JCRPageList getPageListComment(SessionProvider sProvider, String questionId) throws Exception {
	  return jcrData_.getPageListComment(sProvider, questionId);
  }
	
	public QuestionPageList getListCategoriesWatch(String userId, SessionProvider sProvider) throws Exception {
		return jcrData_.getListCategoriesWatch(userId, sProvider);
	}
	
	public FileAttachment getUserAvatar(String userName, SessionProvider sessionProvider) throws Exception{
		return jcrData_.getUserAvatar(userName, sessionProvider);
	}
	
	public void saveUserAvatar(String userId, FileAttachment fileAttachment, SessionProvider sessionProvider) throws Exception{
		jcrData_.saveUserAvatar(userId, fileAttachment, sessionProvider);
	}
	
	public void setDefaultAvatar(String userName, SessionProvider sessionProvider)throws Exception{
		jcrData_.setDefaultAvatar(userName, sessionProvider);
	}
	
	public boolean getWatchByUser(String userId, String cateId, SessionProvider sessionProvider) throws Exception{
		return jcrData_.getWatchByUser(userId, cateId, sessionProvider);
	}
	
	public QuestionPageList getPendingQuestionsByCategory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception{
		return jcrData_.getPendingQuestionsByCategory(categoryId, sProvider, faqSetting);
	}

	public void start() {
		// TODO Auto-generated method stub
		try{
			jcrData_.checkEvenListen();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {}
}