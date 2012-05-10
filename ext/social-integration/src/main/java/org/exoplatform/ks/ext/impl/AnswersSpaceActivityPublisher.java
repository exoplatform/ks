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
package org.exoplatform.ks.ext.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.impl.AnswerEventListener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class AnswersSpaceActivityPublisher extends AnswerEventListener {

  public static final String SPACE_APP_ID = "ks-answer:spaces";
  
  public static final String QUESTION_ID_KEY = "QuestionId";
  public static final String ANSWER_ID_KEY = "AnswerId";
  public static final String COMMENT_ID_KEY = "CommentId";
  public static final String ACTIVITY_TYPE_KEY = "ActivityType";
  public static final String AUTHOR_KEY = "Author";
  public static final String LINK_KEY = "Link";
  public static final String QUESTION_NAME_KEY = "Name";
  public static final String LANGUAGE_KEY = "Language";
  public static final String ANSWER = "Answer";
  public static final String QUESTION = "Question";
  public static final String COMMENT = "Comment";
  public static final String ANSWER_ADD = ANSWER + "Add";
  public static final String QUESTION_ADD = QUESTION + "Add";
  public static final String COMMENT_ADD = COMMENT + "Add";
  public static final String ANSWER_UPDATE = ANSWER + "Update";
  public static final String QUESTION_UPDATE = QUESTION + "Update";
  public static final String COMMENT_UPDATE = COMMENT + "Update";
  
  private static Log LOG = ExoLogger.getExoLogger(AnswerEventListener.class);

  @Override
  public void saveAnswer(String questionId, Answer answer, boolean isNew) {
    try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager");
      FAQService faqS = (FAQService) PortalContainer.getInstance()
                                                    .getComponentInstanceOfType(FAQService.class);
      Question q = faqS.getQuestionById(questionId);
      
      String catId = q.getCategoryId();
      
      if (catId == null || catId.indexOf(Utils.CATE_SPACE_ID_PREFIX) < 0) {
        return;
      }
      
      //TODO resource bundle needed 
      String msg = "@"+answer.getResponseBy();
      String body = answer.getResponses();
      String spaceId = catId.split(Utils.CATE_SPACE_ID_PREFIX)[1];
      IdentityManager identityM = (IdentityManager) PortalContainer.getInstance()
                                                                    .getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance()
                                                                   .getComponentInstanceOfType(ActivityManager.class);
      
      Identity spaceIdentity = identityM.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                              spaceId,
                                                              false);
      Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, answer.getResponseBy());
      Activity activity = new Activity();
      activity.setUserId(userIdentity.getId());
      activity.setTitle(StringEscapeUtils.unescapeHtml(msg));
      activity.setBody(StringEscapeUtils.unescapeHtml(ForumTransformHTML.cleanHtmlCode(body, (List<String>) Collections.EMPTY_LIST)));
      activity.setType(SPACE_APP_ID);
      Map<String, String> params = new HashMap<String, String>();
      params.put(QUESTION_ID_KEY, questionId);
      params.put(ACTIVITY_TYPE_KEY, isNew ? ANSWER_ADD : ANSWER_UPDATE);
      params.put(ANSWER_ID_KEY, answer.getId());
      params.put(AUTHOR_KEY, answer.getResponseBy());
      params.put(QUESTION_NAME_KEY, q.getQuestion());
      params.put(LINK_KEY, q.getLink());
      params.put(LANGUAGE_KEY, q.getLanguage());
      activity.setTemplateParams(params);
      
      activityM.recordActivity(spaceIdentity, activity);

    } catch (ClassNotFoundException e) {
      if (LOG.isDebugEnabled())
        LOG.debug("Please check the integrated project does the social deploy? " + e.getMessage());
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when post answer " + e.getMessage());
    }

  }

  @Override
  public void saveComment(String questionId, Comment comment, boolean isNew) {
    /*try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager");
      FAQService faqS = (FAQService) PortalContainer.getInstance()
                                                    .getComponentInstanceOfType(FAQService.class);
      Question q = faqS.getQuestionById(questionId);
      
      String catId = q.getCategoryId();
      if (catId == null || catId.indexOf(Utils.CATE_SPACE_ID_PREFIX) < 0) {
        return;
      }
      
      //TODO resource bundle needed 
//      String msg = "@"+comment.getCommentBy() + " has commented: <a href=" + q.getLink() + ">"
//          + q.getQuestion() + "</a>";
      String msg = "@" + comment.getCommentBy();
      String body = comment.getComments();
      String spaceId = catId.split(Utils.CATE_SPACE_ID_PREFIX)[1];
      IdentityManager indentityM = (IdentityManager) PortalContainer.getInstance()
                                                                    .getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance()
                                                                   .getComponentInstanceOfType(ActivityManager.class);
      // SpaceService spaceS = (SpaceService)
      // PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
      // Space space = spaceS.getSpaceById(spaceId) ;
      Identity spaceIdentity = indentityM.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                              spaceId,
                                                              false);
      Activity activity = new Activity();
      activity.setTitle(msg);
      activity.setBody(body);
      activity.setType(SPACE_APP_ID);
      Map<String, String> params = new HashMap<String, String>();
      params.put(QUESTION_ID_KEY, questionId);
      params.put(ACTIVITY_TYPE_KEY, isNew ? COMMENT_ADD : COMMENT_UPDATE);
      params.put(COMMENT_ID_KEY, comment.getId());
      params.put(AUTHOR_KEY, comment.getCommentBy());
      params.put(QUESTION_NAME_KEY, q.getQuestion());
      params.put(LINK_KEY, q.getLink());
      params.put(LANGUAGE_KEY, q.getLanguage());
      activity.setTemplateParams(params);
      
      activityM.recordActivity(spaceIdentity, activity);

    } catch (ClassNotFoundException e) {
      if (LOG.isDebugEnabled())
        LOG.debug("Please check the integrated project does the social deploy? " + e.getMessage());
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when add comment " + e.getMessage());
    }*/
  }

  @Override
  public void saveQuestion(Question question, boolean isNew) {
    try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager");
      String catId = question.getCategoryId();
      if (catId == null || catId.indexOf(Utils.CATE_SPACE_ID_PREFIX) < 0) {
        return;
      }
      
    //TODO resource bundle needed 
      String msg = "@"+question.getAuthor();
      String body = question.getDetail();
      String spaceId = catId.split(Utils.CATE_SPACE_ID_PREFIX)[1];
      IdentityManager identityM = (IdentityManager) PortalContainer.getInstance()
                                                                    .getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance()
                                                                   .getComponentInstanceOfType(ActivityManager.class);
      Identity spaceIdentity = identityM.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                              spaceId,
                                                              false);
      Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, question.getAuthor());
      Activity activity = new Activity();
      activity.setUserId(userIdentity.getId());
      activity.setTitle(msg);
      activity.setBody(body);
      activity.setType(SPACE_APP_ID);
      Map<String, String> params = new HashMap<String, String>();
      params.put(QUESTION_ID_KEY, question.getId());
      params.put(ACTIVITY_TYPE_KEY, isNew ? QUESTION_ADD : QUESTION_UPDATE);
      params.put(QUESTION_NAME_KEY, question.getQuestion());
      params.put(LINK_KEY, question.getLink());
      params.put(LANGUAGE_KEY, question.getLanguage());
      activity.setTemplateParams(params);
      
      activityM.recordActivity(spaceIdentity, activity);
    } catch (ClassNotFoundException e) {
      if (LOG.isDebugEnabled())
        LOG.debug("Please check the integrated project does the social deploy? " + e.getMessage());
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when add new questin " + e.getMessage());
    }

  }

  @Override
  public void saveAnswer(String questionId, Answer[] answers, boolean isNew) {
    try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager");
      FAQService faqS = (FAQService) PortalContainer.getInstance()
                                                    .getComponentInstanceOfType(FAQService.class);
      Question q = faqS.getQuestionById(questionId);
      
      String catId = q.getCategoryId();
      if (catId == null || catId.indexOf(Utils.CATE_SPACE_ID_PREFIX) < 0) {
        return;
      }
      
      if (answers != null) {
        for (Answer a : answers) {
          saveAnswer(questionId, a, isNew);
        }
      }
      
    } catch (ClassNotFoundException e) {
      if (LOG.isDebugEnabled())
        LOG.debug("Please check the integrated project does the social deploy? " + e.getMessage());
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when post answer " + e.getMessage());
    }
  }

}
