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



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ks.ext.impl.ForumTransformHTML;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.impl.ForumEventListener;
import org.exoplatform.ks.bbcode.core.ExtendedBBCodeProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ForumSpaceActivityPublisher extends ForumEventListener {
    
    public static final String FORUM_APP_ID = "ks-forum:spaces";
    public static final String FORUM_ID_KEY = "ForumId";
    public static final String CATE_ID_KEY = "CateId";
    public static final String ACTIVITY_TYPE_KEY = "ActivityType";
    public static final String POST_ADDED = "AddPost";
    public static final String TOPIC_ADDED = "AddTopic";
    public static final String POST_UPDATED = "UpdatePost";
    public static final String TOPIC_UPDATED = "UpdateTopic";
    public static final String POST_ID_KEY = "PostId";
    public static final String POST_OWNER_KEY = "PostOwner";
    public static final String POST_LINK_KEY = "PostLink";
    public static final String POST_NAME_KEY = "PostName";
    public static final String TOPIC_ID_KEY = "TopicId";
    public static final String TOPIC_OWNER_KEY = "TopicOwner";
    public static final String TOPIC_LINK_KEY = "TopicLink";
    public static final String TOPIC_NAME_KEY = "TopicName";
    
    private static Log      LOG = ExoLogger.getExoLogger(ForumSpaceActivityPublisher.class);
    
    private boolean isPublicPost(Post post, Topic topic) {
      if (!isPublicTopic(topic)) {
        return false;
      }
      if (post == null || 
          post.getUserPrivate().length == 2 || post.getIsHidden() || !post.getIsActiveByTopic() || !post.getIsApproved()) {
        // check permission of the post
        // if the post is private or hidden by censored words or not active by topic or waiting for approving, return false. 
        return false;
      }
      return true;
    }
    
    private boolean isPublicTopic(Topic topic) {
      if (topic == null || 
          !topic.getIsActive() || topic.getIsWaiting() || topic.getIsClosed() ||
          !Utils.isEmpty(topic.getCanView())) {
        // check permission of topic
        // if the topic is not active or waiting or closed or restricts users, return false
        return false;
      }
      return true;
    }
    
    @Override
    public void saveCategory(Category category) {
      // TODO Auto-generated method stub

    }

    @Override
    public void saveForum(Forum forum) {
      // TODO Auto-generated method stub

    }


    @Override
    public void addPost(Post post, String categoryId, String forumId, String topicId) {
      try {
        Class.forName("org.exoplatform.social.core.manager.IdentityManager") ;
        if (forumId == null || forumId.indexOf(Utils.FORUM_SPACE_ID_PREFIX) < 0) {
          return;
        }
        ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
        Topic topic = forumService.getTopic(categoryId, forumId, topicId, "");
        if (!isPublicPost(post, topic)) {
          // ignore post which is not public
          return;
        }
        
        String msg = "@"+post.getOwner();
        String body = ForumTransformHTML.getTitleInHTMLCode(post.getMessage(), new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
        IdentityManager identityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class); 
        ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
        SpaceService spaceS = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class); 
        String spaceId = forumId.split(Utils.FORUM_SPACE_ID_PREFIX)[1];
        Space space = spaceS.getSpaceById(spaceId) ;
        Identity spaceIdentity = identityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
        Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, post.getOwner());
        Activity activity = new Activity();
        activity.setUserId(userIdentity.getId());
        activity.setTitle(StringEscapeUtils.unescapeHtml(msg));
        activity.setBody(StringEscapeUtils.unescapeHtml(body));
        activity.setType(FORUM_APP_ID);
        Map<String, String> templateParams = new HashMap<String, String>();
        templateParams.put(FORUM_ID_KEY, forumId);
        templateParams.put(CATE_ID_KEY, categoryId);
        templateParams.put(TOPIC_ID_KEY, topicId);
        templateParams.put(POST_ID_KEY, post.getId());
        templateParams.put(POST_LINK_KEY, post.getLink());
        templateParams.put(POST_NAME_KEY, post.getName());
        templateParams.put(POST_OWNER_KEY, "@" + post.getOwner());
        templateParams.put(ACTIVITY_TYPE_KEY, POST_ADDED);
        activity.setTemplateParams(templateParams);
        
        activityM.saveActivity(spaceIdentity, activity);

      } catch (ClassNotFoundException e) {
        if(LOG.isDebugEnabled()) LOG.debug("Please check the integrated project does the social deploy? " +e.getMessage());
      } catch (Exception e) {
        LOG.error("Can not record Activity for space when post " +e.getMessage());
      }

    }

    @Override
    public void addTopic(Topic topic, String categoryId, String forumId) {
      try {
        Class.forName("org.exoplatform.social.core.manager.IdentityManager") ;
        if (forumId == null || forumId.indexOf(Utils.FORUM_SPACE_ID_PREFIX) < 0) {
          return;
        }
        
        if (!isPublicTopic(topic)) {
          // ignore topic which is not public
          return;
        }
        
        String msg = "@"+topic.getOwner();
        String body = ForumTransformHTML.getTitleInHTMLCode(topic.getDescription(), new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
        IdentityManager identityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class); 
        ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
        Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, topic.getOwner());
        SpaceService spaceS = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class); 
        String spaceId = forumId.split(Utils.FORUM_SPACE_ID_PREFIX)[1];
        Space space = spaceS.getSpaceById(spaceId) ;
        Identity spaceIdentity = identityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
        
        Activity activity = new Activity();
        activity.setUserId(userIdentity.getId());
        activity.setTitle(StringEscapeUtils.unescapeHtml(msg));
        activity.setBody(StringEscapeUtils.unescapeHtml(body));
        activity.setType(FORUM_APP_ID);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put(FORUM_ID_KEY, forumId);
        params.put(CATE_ID_KEY, categoryId);
        params.put(TOPIC_ID_KEY, topic.getId());
        params.put(TOPIC_LINK_KEY, topic.getLink());
        params.put(TOPIC_NAME_KEY, topic.getTopicName());
        params.put(TOPIC_OWNER_KEY, topic.getOwner());
        params.put(ACTIVITY_TYPE_KEY, TOPIC_ADDED);
        activity.setTemplateParams(params);
        
        activityM.saveActivity(spaceIdentity, activity);
      } catch (ClassNotFoundException e) {
        if(LOG.isDebugEnabled()) LOG.debug("Please check the integrated project does the social deploy? " +e.getMessage());
      } catch (Exception e) {
        LOG.error("Can not record Activity for space when add topic " +e.getMessage());
      }
    }



    @Override
    public void updatePost(Post post, String categoryId, String forumId, String topicId) {
      try {
        Class.forName("org.exoplatform.social.core.manager.IdentityManager") ;
        if (forumId == null || forumId.indexOf(Utils.FORUM_SPACE_ID_PREFIX) < 0) {
          return;
        }
        ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
        Topic topic = forumService.getTopic(categoryId, forumId, topicId, "");
        if (!isPublicPost(post, topic)) {
          // ignore post which is not public
          return;
        }
        
        String msg = "@" + post.getOwner();
        String body = ForumTransformHTML.getTitleInHTMLCode(post.getMessage(), new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
        IdentityManager identityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class); 
        ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
        Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, post.getOwner());
        SpaceService spaceS = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class); 
        String spaceId = forumId.split(Utils.FORUM_SPACE_ID_PREFIX)[1];
        Space space = spaceS.getSpaceById(spaceId) ;
        Identity spaceIdentity = identityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
        
        Activity activity = new Activity();
        activity.setUserId(userIdentity.getId());
        activity.setTitle(StringEscapeUtils.unescapeHtml(msg));
        activity.setBody(StringEscapeUtils.unescapeHtml(body));
        activity.setType(FORUM_APP_ID);
        Map<String, String> templateParams = new HashMap<String, String>();
        templateParams.put(FORUM_ID_KEY, forumId);
        templateParams.put(CATE_ID_KEY, categoryId);
        templateParams.put(TOPIC_ID_KEY, topicId);
        templateParams.put(POST_ID_KEY, post.getId());
        templateParams.put(POST_LINK_KEY, post.getLink());
        templateParams.put(POST_NAME_KEY, post.getName());
        templateParams.put(POST_OWNER_KEY, post.getOwner());
        templateParams.put(ACTIVITY_TYPE_KEY, POST_UPDATED);
        activity.setTemplateParams(templateParams);
        
        activityM.saveActivity(spaceIdentity, activity);

      } catch (ClassNotFoundException e) {
        if(LOG.isDebugEnabled()) LOG.debug("Please check the integrated project does the social deploy? " +e.getMessage());
      } catch (Exception e) {
        LOG.error("Can not record Activity for space when post " +e.getMessage());
      }
    }

    @Override
    public void updateTopic(Topic topic, String categoryId, String forumId) {
      try {
        Class.forName("org.exoplatform.social.core.manager.IdentityManager") ;
        if (forumId == null || forumId.indexOf(Utils.FORUM_SPACE_ID_PREFIX) < 0) {
          return;
        }
        
        if (!isPublicTopic(topic)) {
          // ignore topic which is not public
          return;
        }
        
        String msg = "@" + topic.getOwner();
        String body = ForumTransformHTML.getTitleInHTMLCode(topic.getDescription(), new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
        IdentityManager identityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class); 
        ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
        Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, topic.getOwner());
        SpaceService spaceS = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class); 
        String spaceId = forumId.split(Utils.FORUM_SPACE_ID_PREFIX)[1];
        Space space = spaceS.getSpaceById(spaceId) ;
        Identity spaceIdentity = identityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
        
        Activity activity = new Activity();
        activity.setUserId(userIdentity.getId());
        activity.setTitle(StringEscapeUtils.unescapeHtml(msg));
        activity.setBody(StringEscapeUtils.unescapeHtml(body));
        activity.setType(FORUM_APP_ID);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put(FORUM_ID_KEY, forumId);
        params.put(CATE_ID_KEY, categoryId);
        params.put(TOPIC_ID_KEY, topic.getId());
        params.put(TOPIC_LINK_KEY, topic.getLink());
        params.put(TOPIC_NAME_KEY, topic.getTopicName());
        params.put(TOPIC_OWNER_KEY, topic.getOwner());
        params.put(ACTIVITY_TYPE_KEY, TOPIC_UPDATED);
        activity.setTemplateParams(params);
        
        activityM.saveActivity(spaceIdentity, activity);
      } catch (ClassNotFoundException e) {
        if(LOG.isDebugEnabled()) LOG.debug("Please check the integrated project does the social deploy? " +e.getMessage());
      } catch (Exception e) {
        LOG.error("Can not record Activity for space when add topic " +e.getMessage());
      }
      
    }

  }
