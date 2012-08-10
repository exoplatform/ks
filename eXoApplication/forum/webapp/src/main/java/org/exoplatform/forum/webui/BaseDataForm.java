/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jan 31, 2012  
 */
public class BaseDataForm extends BaseForumForm {

  public String       topicId;

  public String       forumId;

  public boolean      isMoveTopic  = false;

  public boolean      isMovePost   = false;

  public String       pathTopic    = ForumUtils.EMPTY_STR;

  public String       pathPost     = ForumUtils.EMPTY_STR;

  public List<String> canAddTopics = new ArrayList<String>();

  public List<String> canAddPosts  = new ArrayList<String>();

  public List<String> canViewPosts  = new ArrayList<String>();

  public BaseDataForm() {
  }

  public List<Category> getCategories() throws Exception {
    List<Category> categories = new ArrayList<Category>();
    for (Category category : getForumService().getCategories()) {
      if (getUserProfile().getUserRole() == 1) {
        if (!ForumUtils.isArrayEmpty(category.getUserPrivate()) && 
            !ForumServiceUtils.hasPermission(category.getUserPrivate(), userProfile.getUserId())) {
          continue;
        }
        if (isMoveTopic == true && ForumServiceUtils.hasPermission(category.getCreateTopicRole(), userProfile.getUserId())) {
          canAddTopics.add(category.getId());
        }
        if (isMovePost == true && ForumServiceUtils.hasPermission(category.getPoster(), userProfile.getUserId())) {
          canAddPosts.add(category.getId());
        }
        if (isMovePost == true && ForumServiceUtils.hasPermission(category.getViewer(), userProfile.getUserId())) {
          canViewPosts.add(category.getId());
        }
      }
      categories.add(category);
    }
    return categories;
  }

  public List<Forum> getForums(String categoryId) throws Exception {
    List<Forum> forums = new ArrayList<Forum>();
    String strQueryForum = ForumUtils.EMPTY_STR;
    if(categoryId.indexOf(ForumUtils.SPACE_GROUP_ID) > 0) {
      strQueryForum = ForumSessionUtils.getQueryForumInSpace(getUserProfile().getUserId());
    }
    for (Forum forum : getForumService().getForumSummaries(categoryId, strQueryForum)) {
      if (forum.getId().equals(forumId)) {
        if (pathTopic.indexOf(categoryId) >= 0) {
          continue;
        }
      }
      if (getUserProfile().getUserRole() == UserProfile.MODERATOR) {
        if (forum.getIsClosed()) {
          continue;
        }
        if (!ForumServiceUtils.hasPermission(forum.getModerators(), userProfile.getUserId())) {
          if (forum.getIsLock()) {
            continue;
          }
          if (isMoveTopic == true && !canAddTopics.contains(categoryId) && !ForumUtils.isArrayEmpty(forum.getCreateTopicRole()) && 
              !ForumServiceUtils.hasPermission(forum.getCreateTopicRole(), userProfile.getUserId())) {
            continue;
          }
        }
        if (isMovePost == true && ForumServiceUtils.hasPermission(forum.getPoster(), userProfile.getUserId())) {
          canAddPosts.add(forum.getId());
        }
        if (isMovePost == true && ForumServiceUtils.hasPermission(forum.getViewer(), userProfile.getUserId())) {
          canViewPosts.add(forum.getId());
        }
      } else if (userProfile.getUserRole() > 1) {
        continue;
      }
      forums.add(forum);
    }
    return forums;
  }

  public List<Topic> getTopics(String categoryId, String forumId, boolean isMode) throws Exception {
    List<Topic> topics = new ArrayList<Topic>();
    for (Topic topic : getForumService().getTopics(categoryId, forumId)) {
      if (topic.getId().equalsIgnoreCase(topicId)) {
        if (pathPost.indexOf(categoryId) >= 0 && pathPost.indexOf(forumId) > 0)
          continue;
      }
      if (getUserProfile().getUserRole() == UserProfile.MODERATOR) {
        if (!isMode) {
          if (!topic.getIsActive() || !topic.getIsActiveByForum() || !topic.getIsApproved() ||
               topic.getIsClosed() || topic.getIsLock() || topic.getIsWaiting())
            continue;
          if (!canViewPosts.contains(categoryId) && canViewPosts.contains(forumId) && topic.getCanView().length > 0 && 
              !ForumServiceUtils.hasPermission(topic.getCanView(), userProfile.getUserId()))
            continue;
          if (!canAddPosts.contains(categoryId) && canAddPosts.contains(forumId) && topic.getCanPost().length > 0 && 
              !ForumServiceUtils.hasPermission(topic.getCanPost(), userProfile.getUserId()))
            continue;
        }
      }
      topics.add(topic);
    }
    return topics;
  }
}