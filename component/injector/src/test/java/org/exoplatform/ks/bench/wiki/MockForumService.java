/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.bench.wiki;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.jcr.NodeIterator;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumEventListener;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.ForumSubscription;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.LazyPageList;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.service.SendMessageInfo;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * May 28, 2012  
 */
public class MockForumService implements ForumService {

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#addPlugin(org.exoplatform.container.component.ComponentPlugin)
   */
  @Override
  public void addPlugin(ComponentPlugin plugin) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#addRolePlugin(org.exoplatform.container.component.ComponentPlugin)
   */
  @Override
  public void addRolePlugin(ComponentPlugin plugin) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#addInitialDataPlugin(org.exoplatform.container.component.ComponentPlugin)
   */
  @Override
  public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#addInitialDefaultDataPlugin(org.exoplatform.container.component.ComponentPlugin)
   */
  @Override
  public void addInitialDefaultDataPlugin(ComponentPlugin plugin) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getCategories()
   */
  @Override
  public List<Category> getCategories() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getCategory(java.lang.String)
   */
  @Override
  public Category getCategory(String categoryId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPermissionTopicByCategory(java.lang.String, java.lang.String)
   */
  @Override
  public String[] getPermissionTopicByCategory(String categoryId, String type) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveCategory(org.exoplatform.forum.service.Category, boolean)
   */
  @Override
  public void saveCategory(Category category, boolean isNew) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#calculateModerator(java.lang.String, boolean)
   */
  @Override
  public void calculateModerator(String categoryPath, boolean isNew) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveModOfCategory(java.util.List, java.lang.String, boolean)
   */
  @Override
  public void saveModOfCategory(List<String> moderatorCate, String userId, boolean isAdd) {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#removeCategory(java.lang.String)
   */
  @Override
  public Category removeCategory(String categoryId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getForums(java.lang.String, java.lang.String)
   */
  @Override
  public List<Forum> getForums(String categoryId, String strQuery) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getForum(java.lang.String, java.lang.String)
   */
  @Override
  public Forum getForum(String categoryId, String forumId) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#modifyForum(org.exoplatform.forum.service.Forum, int)
   */
  @Override
  public void modifyForum(Forum forum, int type) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveForum(java.lang.String, org.exoplatform.forum.service.Forum, boolean)
   */
  @Override
  public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveModerateOfForums(java.util.List, java.lang.String, boolean)
   */
  @Override
  public void saveModerateOfForums(List<String> forumPaths, String userName, boolean isDelete) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#removeForum(java.lang.String, java.lang.String)
   */
  @Override
  public Forum removeForum(String categoryId, String forumId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#moveForum(java.util.List, java.lang.String)
   */
  @Override
  public void moveForum(List<Forum> forums, String destCategoryPath) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPageTopic(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public JCRPageList getPageTopic(String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPageTopicByUser(java.lang.String, boolean, java.lang.String)
   */
  @Override
  public JCRPageList getPageTopicByUser(String userName, boolean isMod, String strOrderBy) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPageTopicOld(long, java.lang.String)
   */
  @Override
  public JCRPageList getPageTopicOld(long date, String forumPatch) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getAllTopicsOld(long, java.lang.String)
   */
  @Override
  public List<Topic> getAllTopicsOld(long date, String forumPatch) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getTotalTopicOld(long, java.lang.String)
   */
  @Override
  public long getTotalTopicOld(long date, String forumPatch) {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getTopics(java.lang.String, java.lang.String)
   */
  @Override
  public List<Topic> getTopics(String categoryId, String forumId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getTopic(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Topic getTopic(String categoryId, String forumId, String topicId, String userRead) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#setViewCountTopic(java.lang.String, java.lang.String)
   */
  @Override
  public void setViewCountTopic(String path, String userRead) {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getTopicByPath(java.lang.String, boolean)
   */
  @Override
  public Topic getTopicByPath(String topicPath, boolean isLastPost) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getTopicSummary(java.lang.String)
   */
  @Override
  public Topic getTopicSummary(String topicPath) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getTopicUpdate(org.exoplatform.forum.service.Topic, boolean)
   */
  @Override
  public Topic getTopicUpdate(Topic topic, boolean isSummary) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#modifyTopic(java.util.List, int)
   */
  @Override
  public void modifyTopic(List<Topic> topics, int type) {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveTopic(java.lang.String, java.lang.String, org.exoplatform.forum.service.Topic, boolean, boolean, org.exoplatform.forum.service.MessageBuilder)
   */
  @Override
  public void saveTopic(String categoryId,
                        String forumId,
                        Topic topic,
                        boolean isNew,
                        boolean isMove,
                        MessageBuilder messageBuilder) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#removeTopic(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Topic removeTopic(String categoryId, String forumId, String topicId) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#moveTopic(java.util.List, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void moveTopic(List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#mergeTopic(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void mergeTopic(String srcTopicPath, String destTopicPath, String mailContent, String link) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPosts(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public JCRPageList getPosts(String categoryId,
                              String forumId,
                              String topicId,
                              String isApproved,
                              String isHidden,
                              String strQuery,
                              String userLogin) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPostForSplitTopic(java.lang.String)
   */
  @Override
  public JCRPageList getPostForSplitTopic(String topicPath) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getAvailablePost(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public long getAvailablePost(String categoryId,
                               String forumId,
                               String topicId,
                               String isApproved,
                               String isHidden,
                               String userLogin) throws Exception {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getLastReadIndex(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public long getLastReadIndex(String path, String isApproved, String isHidden, String userLogin) throws Exception {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPagePostByUser(java.lang.String, java.lang.String, boolean, java.lang.String)
   */
  @Override
  public JCRPageList getPagePostByUser(String userName, String userId, boolean isMod, String strOrderBy) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPost(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#savePost(java.lang.String, java.lang.String, java.lang.String, org.exoplatform.forum.service.Post, boolean, org.exoplatform.forum.service.MessageBuilder)
   */
  @Override
  public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew, MessageBuilder messageBuilder) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#modifyPost(java.util.List, int)
   */
  @Override
  public void modifyPost(List<Post> posts, int type) {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#removePost(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Post removePost(String categoryId, String forumId, String topicId, String postId) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#movePost(java.lang.String[], java.lang.String, boolean, java.lang.String, java.lang.String)
   */
  @Override
  public void movePost(String[] postPaths, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getObjectNameByPath(java.lang.String)
   */
  @Override
  public Object getObjectNameByPath(String path) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getObjectNameById(java.lang.String, java.lang.String)
   */
  @Override
  public Object getObjectNameById(String id, String type) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getAllLink(java.lang.String, java.lang.String)
   */
  @Override
  public List<ForumLinkData> getAllLink(String strQueryCate, String strQueryForum) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getForumHomePath()
   */
  @Override
  public String getForumHomePath() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#addTag(java.util.List, java.lang.String, java.lang.String)
   */
  @Override
  public void addTag(List<Tag> tags, String userName, String topicPath) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#unTag(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void unTag(String tagId, String userName, String topicPath) {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getTag(java.lang.String)
   */
  @Override
  public Tag getTag(String tagId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getAllTagName(java.lang.String, java.lang.String)
   */
  @Override
  public List<String> getAllTagName(String strQuery, String userAndTopicId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getTagNameInTopic(java.lang.String)
   */
  @Override
  public List<String> getTagNameInTopic(String userAndTopicId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getAllTags()
   */
  @Override
  public List<Tag> getAllTags() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getMyTagInTopic(java.lang.String[])
   */
  @Override
  public List<Tag> getMyTagInTopic(String[] tagIds) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getTopicByMyTag(java.lang.String, java.lang.String)
   */
  @Override
  public JCRPageList getTopicByMyTag(String userIdAndtagId, String strOrderBy) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveTag(org.exoplatform.forum.service.Tag)
   */
  @Override
  public void saveTag(Tag newTag) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveUserProfile(org.exoplatform.forum.service.UserProfile, boolean, boolean)
   */
  @Override
  public void saveUserProfile(UserProfile userProfile, boolean isOption, boolean isBan) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#updateUserProfile(org.exoplatform.services.organization.User)
   */
  @Override
  public void updateUserProfile(User user) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveUserModerator(java.lang.String, java.util.List, boolean)
   */
  @Override
  public void saveUserModerator(String userName, List<String> ids, boolean isModeCate) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#searchUserProfile(java.lang.String)
   */
  @Override
  public JCRPageList searchUserProfile(String userSearch) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getUserInfo(java.lang.String)
   */
  @Override
  public UserProfile getUserInfo(String userName) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getUserModerator(java.lang.String, boolean)
   */
  @Override
  public List<String> getUserModerator(String userName, boolean isModeCate) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveUserBookmark(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public void saveUserBookmark(String userName, String bookMark, boolean isNew) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveLastPostIdRead(java.lang.String, java.lang.String[], java.lang.String[])
   */
  @Override
  public void saveLastPostIdRead(String userId, String[] lastReadPostOfForum, String[] lastReadPostOfTopic) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveCollapsedCategories(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public void saveCollapsedCategories(String userName, String categoryId, boolean isAdd) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPageListUserProfile()
   */
  @Override
  public JCRPageList getPageListUserProfile() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getQuickSearch(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, java.util.List, java.util.List)
   */
  @Override
  public List<ForumSearch> getQuickSearch(String textQuery,
                                          String type,
                                          String pathQuery,
                                          String userId,
                                          List<String> listCateIds,
                                          List<String> listForumIds,
                                          List<String> forumIdsOfModerator) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getScreenName(java.lang.String)
   */
  @Override
  public String getScreenName(String userName) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getAdvancedSearch(org.exoplatform.forum.service.ForumEventQuery, java.util.List, java.util.List)
   */
  @Override
  public List<ForumSearch> getAdvancedSearch(ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveForumStatistic(org.exoplatform.forum.service.ForumStatistic)
   */
  @Override
  public void saveForumStatistic(ForumStatistic forumStatistic) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getForumStatistic()
   */
  @Override
  public ForumStatistic getForumStatistic() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveForumAdministration(org.exoplatform.forum.service.ForumAdministration)
   */
  @Override
  public void saveForumAdministration(ForumAdministration forumAdministration) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getForumAdministration()
   */
  @Override
  public ForumAdministration getForumAdministration() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#updateStatisticCounts(long, long)
   */
  @Override
  public void updateStatisticCounts(long topicCoutn, long postCount) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#userLogin(java.lang.String)
   */
  @Override
  public void userLogin(String userId) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#userLogout(java.lang.String)
   */
  @Override
  public void userLogout(String userId) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#isOnline(java.lang.String)
   */
  @Override
  public boolean isOnline(String userId) throws Exception {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getOnlineUsers()
   */
  @Override
  public List<String> getOnlineUsers() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getLastLogin()
   */
  @Override
  public String getLastLogin() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPrivateMessage(java.lang.String, java.lang.String)
   */
  @Override
  public JCRPageList getPrivateMessage(String userName, String type) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getNewPrivateMessage(java.lang.String)
   */
  @Override
  public long getNewPrivateMessage(String userName) throws Exception {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#savePrivateMessage(org.exoplatform.forum.service.ForumPrivateMessage)
   */
  @Override
  public void savePrivateMessage(ForumPrivateMessage privateMessage) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveReadMessage(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void saveReadMessage(String messageId, String userName, String type) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#removePrivateMessage(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void removePrivateMessage(String messageId, String userName, String type) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getForumSubscription(java.lang.String)
   */
  @Override
  public ForumSubscription getForumSubscription(String userId) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveForumSubscription(org.exoplatform.forum.service.ForumSubscription, java.lang.String)
   */
  @Override
  public void saveForumSubscription(ForumSubscription forumSubscription, String userId) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#addWatch(int, java.lang.String, java.util.List, java.lang.String)
   */
  @Override
  public void addWatch(int watchType, String path, List<String> values, String currentUser) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#removeWatch(int, java.lang.String, java.lang.String)
   */
  @Override
  public void removeWatch(int watchType, String path, String values) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getJobWattingForModerator(java.lang.String[])
   */
  @Override
  public List<ForumSearch> getJobWattingForModerator(String[] paths) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getJobWattingForModeratorByUser(java.lang.String)
   */
  @Override
  public int getJobWattingForModeratorByUser(String userId) throws Exception {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getMessageInfo(java.lang.String)
   */
  @Override
  public SendMessageInfo getMessageInfo(String name) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPendingMessages()
   */
  @Override
  public Iterator<SendMessageInfo> getPendingMessages() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#isAdminRole(java.lang.String)
   */
  @Override
  public boolean isAdminRole(String userName) throws Exception {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getNewPosts(int)
   */
  @Override
  public List<Post> getNewPosts(int number) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getRecentPostsForUser(java.lang.String, int)
   */
  @Override
  public List<Post> getRecentPostsForUser(String userName, int number) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#search(java.lang.String)
   */
  @Override
  public NodeIterator search(String queryString) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#evaluateActiveUsers(java.lang.String)
   */
  @Override
  public void evaluateActiveUsers(String query) {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#createUserProfile(org.exoplatform.services.organization.User)
   */
  @Override
  public void createUserProfile(User user) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#updateTopicAccess(java.lang.String, java.lang.String)
   */
  @Override
  public void updateTopicAccess(String userId, String topicId) {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#updateForumAccess(java.lang.String, java.lang.String)
   */
  @Override
  public void updateForumAccess(String userId, String forumId) {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#exportXML(java.lang.String, java.lang.String, java.util.List, java.lang.String, java.io.ByteArrayOutputStream, boolean)
   */
  @Override
  public Object exportXML(String categoryId,
                          String forumId,
                          List<String> objectIds,
                          String nodePath,
                          ByteArrayOutputStream bos,
                          boolean isExportAll) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#importXML(java.lang.String, java.io.ByteArrayInputStream, int)
   */
  @Override
  public void importXML(String nodePath, ByteArrayInputStream bis, int typeImport) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getQuickProfiles(java.util.List)
   */
  @Override
  public List<UserProfile> getQuickProfiles(List<String> userList) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getQuickProfile(java.lang.String)
   */
  @Override
  public UserProfile getQuickProfile(String userName) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getUserInformations(org.exoplatform.forum.service.UserProfile)
   */
  @Override
  public UserProfile getUserInformations(UserProfile userProfile) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getDefaultUserProfile(java.lang.String, java.lang.String)
   */
  @Override
  public UserProfile getDefaultUserProfile(String userName, String ip) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#updateUserProfileSetting(org.exoplatform.forum.service.UserProfile)
   */
  @Override
  public UserProfile updateUserProfileSetting(UserProfile userProfile) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getBookmarks(java.lang.String)
   */
  @Override
  public List<String> getBookmarks(String userName) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getUserSettingProfile(java.lang.String)
   */
  @Override
  public UserProfile getUserSettingProfile(String userName) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getUserProfileManagement(java.lang.String)
   */
  @Override
  public UserProfile getUserProfileManagement(String userName) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveUserSettingProfile(org.exoplatform.forum.service.UserProfile)
   */
  @Override
  public void saveUserSettingProfile(UserProfile userProfile) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#updateForum(java.lang.String)
   */
  @Override
  public void updateForum(String path) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getBanList()
   */
  @Override
  public List<String> getBanList() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#addBanIP(java.lang.String)
   */
  @Override
  public boolean addBanIP(String ip) throws Exception {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#removeBan(java.lang.String)
   */
  @Override
  public void removeBan(String ip) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getForumBanList(java.lang.String)
   */
  @Override
  public List<String> getForumBanList(String forumId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#addBanIPForum(java.lang.String, java.lang.String)
   */
  @Override
  public boolean addBanIPForum(String ip, String forumId) throws Exception {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#removeBanIPForum(java.lang.String, java.lang.String)
   */
  @Override
  public void removeBanIPForum(String ip, String forumId) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getListPostsByIP(java.lang.String, java.lang.String)
   */
  @Override
  public JCRPageList getListPostsByIP(String ip, String strOrderBy) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#registerListenerForCategory(java.lang.String)
   */
  @Override
  public void registerListenerForCategory(String categoryId) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#unRegisterListenerForCategory(java.lang.String)
   */
  @Override
  public void unRegisterListenerForCategory(String path) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getUserAvatar(java.lang.String)
   */
  @Override
  public ForumAttachment getUserAvatar(String userName) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveUserAvatar(java.lang.String, org.exoplatform.forum.service.ForumAttachment)
   */
  @Override
  public void saveUserAvatar(String userId, ForumAttachment fileAttachment) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#setDefaultAvatar(java.lang.String)
   */
  @Override
  public void setDefaultAvatar(String userName) {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getWatchByUser(java.lang.String)
   */
  @Override
  public List<Watch> getWatchByUser(String userId) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#updateEmailWatch(java.util.List, java.lang.String, java.lang.String)
   */
  @Override
  public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getAllPruneSetting()
   */
  @Override
  public List<PruneSetting> getAllPruneSetting() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPruneSetting(java.lang.String)
   */
  @Override
  public PruneSetting getPruneSetting(String forumPath) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#savePruneSetting(org.exoplatform.forum.service.PruneSetting)
   */
  @Override
  public void savePruneSetting(PruneSetting pruneSetting) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#runPrune(org.exoplatform.forum.service.PruneSetting)
   */
  @Override
  public void runPrune(PruneSetting pSetting) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#runPrune(java.lang.String)
   */
  @Override
  public void runPrune(String forumPath) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#checkPrune(org.exoplatform.forum.service.PruneSetting)
   */
  @Override
  public long checkPrune(PruneSetting pSetting) throws Exception {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getTopicTypes()
   */
  @Override
  public List<TopicType> getTopicTypes() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getTopicType(java.lang.String)
   */
  @Override
  public TopicType getTopicType(String Id) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#saveTopicType(org.exoplatform.forum.service.TopicType)
   */
  @Override
  public void saveTopicType(TopicType topicType) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#removeTopicType(java.lang.String)
   */
  @Override
  public void removeTopicType(String topicTypeId) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getPageTopicByType(java.lang.String)
   */
  @Override
  public JCRPageList getPageTopicByType(String type) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getTopicList(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
   */
  @Override
  public LazyPageList<Topic> getTopicList(String categoryId, String forumId, String string, String strOrderBy, int pageSize) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#getForumSummaries(java.lang.String, java.lang.String)
   */
  @Override
  public List<Forum> getForumSummaries(String categoryId, String strQuery) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#updateUserProfileInfo(java.lang.String)
   */
  @Override
  public void updateUserProfileInfo(String name) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#addMember(org.exoplatform.services.organization.User, org.exoplatform.forum.service.UserProfile)
   */
  @Override
  public void addMember(User user, UserProfile profileTemplate) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#removeMember(org.exoplatform.services.organization.User)
   */
  @Override
  public void removeMember(User user) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#updateLoggedinUsers()
   */
  @Override
  public void updateLoggedinUsers() throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#calculateDeletedUser(java.lang.String)
   */
  @Override
  public void calculateDeletedUser(String userName) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#calculateDeletedGroup(java.lang.String, java.lang.String)
   */
  @Override
  public void calculateDeletedGroup(String groupId, String groupName) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#createForumRss(java.lang.String, java.lang.String)
   */
  @Override
  public InputStream createForumRss(String objectId, String link) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#createUserRss(java.lang.String, java.lang.String)
   */
  @Override
  public InputStream createUserRss(String userId, String link) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#addListenerPlugin(org.exoplatform.forum.service.ForumEventListener)
   */
  @Override
  public void addListenerPlugin(ForumEventListener listener) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.forum.service.ForumService#removeCacheUserProfile(java.lang.String)
   */
  @Override
  public void removeCacheUserProfile(String userName) throws Exception {

  }

}
