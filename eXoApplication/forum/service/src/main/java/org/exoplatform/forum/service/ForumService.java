/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.jcr.NodeIterator;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.forum.service.impl.model.PostFilter;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SARL.
 */
public interface ForumService extends ForumServiceLegacy {

  /**
   * Adds the plugin.
   * 
   * @param plugin the plugin
   * @throws Exception the exception
   */
  void addPlugin(ComponentPlugin plugin) throws Exception;

  /**
   * Adds the plugin.
   * 
   * @param plugin the plugin
   * @throws Exception the exception
   */
  void addRolePlugin(ComponentPlugin plugin) throws Exception;

  /**
   * Adds the plugin.
   * 
   * @param plugin the plugin
   * @throws Exception the exception
   */
  void addInitialDataPlugin(ComponentPlugin plugin) throws Exception;

  void addInitialDefaultDataPlugin(ComponentPlugin plugin) throws Exception;

  /**
   * Gets the categories.
   * 
   * @return the list category
   */
  List<Category> getCategories();

  /**
   * Gets the category.
   * 
   * @param categoryId is the id of category.
   * @return the category
   * @throws Exception the exception
   */
  Category getCategory(String categoryId) throws Exception;

  /**
   * Get user and group have edit permission in a category
   * 
   * @param categoryId id of category
   * @param type type of category
   * @throws Exception the exception
   */
  String[] getPermissionTopicByCategory(String categoryId, String type) throws Exception;

  /**
   * Save category. Check exists category, if not to create new else update
   * exists category
   * 
   * @param category is the category
   * @param isNew is the true when add new category or false when update
   *          category.
   * @throws Exception the exception
   */
  void saveCategory(Category category, boolean isNew) throws Exception;

  /**
   * Save moderator information for category
   * 
   * @param categoryPath path of category
   * @param isNew is calculate new or not
   * @throws Exception the exception
   */
  void calculateModerator(String categoryPath, boolean isNew) throws Exception;

  /**
   * Adds the plugin.
   * 
   * @param plugin the plugin
   * @throws Exception the exception
   */
  void saveModOfCategory(List<String> moderatorCate, String userId, boolean isAdd);

  /**
   * Removes the category. Check exists of category and remove it
   * 
   * @param categoryId is the id of category removed
   * @return the category
   * @throws Exception the exception
   */
  Category removeCategory(String categoryId) throws Exception;

  /**
   * Gets the forums in the category identify.
   * 
   * @param categoryId is the id of category have list forum
   * @return the list forum
   * @throws Exception the exception
   */
  List<Forum> getForums(String categoryId, String strQuery) throws Exception;

  /**
   * Gets the forum in the category identify.
   * 
   * @param categoryId is the id of category identify.
   * @param forumId is the id of forum identify.
   * @return the forum
   */
  Forum getForum(String categoryId, String forumId);

  /**
   * Modify this forum identify.
   * 
   * @param forum is the object forum that should be modified
   * @param type is choose when modify this forum.
   * @throws Exception the exception
   */
  void modifyForum(Forum forum, int type) throws Exception;

  /**
   * Save forum.Check exists forum, if not to create new else update exists
   * forum
   * 
   * @param categoryId is the id of category identify.
   * @param forum is the object forum need save.
   * @param isNew is the new
   * @throws Exception the exception
   */
  void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception;

  /**
   * Save user is moderator of list forum
   * 
   * @param forumPaths is the list path of forums, one forum have only path.
   * @param userName is the userId of Account login of portal system.
   * @param isDelete is false when you want to add userId into list moderator of
   *          forums isDelete is true when you want to remove userId from list
   *          moderator of forums.
   * @throws Exception the exception
   */
  void saveModerateOfForums(List<String> forumPaths, String userName, boolean isDelete) throws Exception;

  /**
   * Remove the forum in category identify.
   * 
   * @param categoryId is the id of category.
   * @param forumId is the id of forum need remove.
   * @return the forum
   * @throws Exception the exception
   */
  Forum removeForum(String categoryId, String forumId) throws Exception;

  /**
   * Move forum. Move list forum to category by path of category
   * 
   * @param forums is the list object forum
   * @param destCategoryPath is the destination path of category
   * @throws Exception the exception
   */
  void moveForum(List<Forum> forums, String destCategoryPath) throws Exception;

  /**
   * Gets the page topic in forum identify.
   * 
   * @param categoryId is the id of category
   * @param forumId is the id of forum
   * @param isApproved is a string that presents status isApproved of object
   *          Topic. if it equal "true" then this function return page topic
   *          have isApproved equal true if it equal "false" then this function
   *          return page topic have isApproved equal false if it is empty then
   *          this function return page topic, not check isApproved.
   * @param isWaiting is a string that presents status isWaiting of object
   *          Topic. if it equal "true" then this function return page topic
   *          have isWaiting equal true if it equal "false" then this function
   *          return page topic have isWaiting equal false if it is empty then
   *          this function return page topic, not check isWaiting.
   * @param strQuery is a string. It's content have command Query. This function
   *          will return page topic suitable to content of that strQuery
   * @return the page topic
   * @throws Exception the exception
   */
  JCRPageList getPageTopic(String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception;

  /**
   * Gets the page topic by user.
   * 
   * @param userName the user name
   * @param strOrderBy is a string. It's content have command to set 'order by' of Query. This function will return page topic has 'order by'
   *        by strOrderby. 
   * @return the page topic by user
   * @throws Exception the exception
   */
  JCRPageList getPageTopicByUser(String userName, boolean isMod, String strOrderBy) throws Exception;

  /**
   * Gets the page topic old.
   * 
   * @param date the date
   * @param forumPatch the path of forum
   * @return the page topic old
   * @throws Exception the exception
   */
  JCRPageList getPageTopicOld(long date, String forumPatch) throws Exception;

  /**
   * Gets the list topic old.
   * 
   * @param date the date
   * @param forumPatch path of forum
   * @return list of topics
   * @throws Exception the exception
   */
  List<Topic> getAllTopicsOld(long date, String forumPatch) throws Exception;

  /**
   * Gets number of the topics old.
   * 
   * @param date the date
   * @param forumPatch path of forum
   * @return number of topics old
   * @throws Exception the exception
   */
  long getTotalTopicOld(long date, String forumPatch);

  /**
   * Gets the topics.
   * 
   * @param categoryId the category id
   * @param forumId the forum id
   * @return the topics
   * @throws Exception the exception
   */
  List<Topic> getTopics(String categoryId, String forumId) throws Exception;

  /**
   * Gets the topic.
   * 
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @param userRead the user read
   * @return the topic
   * @throws Exception the exception
   */
  Topic getTopic(String categoryId, String forumId, String topicId, String userRead) throws Exception;

  /**
   * Update number of topic viewers 
   * 
   * @param path path of topic
   * @param userRead the user read
   */
  void setViewCountTopic(String path, String userRead);

  /**
   * Gets the topic by path.
   * 
   * @param topicPath the topic path
   * @param isLastPost is the last post
   * @return the topic by path
   * @throws Exception the exception
   */
  Topic getTopicByPath(String topicPath, boolean isLastPost) throws Exception;

  /**
   * Get main informations of topic
   * 
   * @param topicPath the topic path
   * @return the topic by path
   * @throws Exception the exception
   */
  Topic getTopicSummary(String topicPath) throws Exception;

  /**
   * Gets the updated topic
   * 
   * @param topic input topic
   * @param isSummary get main informations or not
   * @return the updated topic
   * @throws Exception the exception
   */
  Topic getTopicUpdate(Topic topic, boolean isSummary) throws Exception;

  /**
   * Modify topic.
   * 
   * @param topics the topics
   * @param type the type
   */
  void modifyTopic(List<Topic> topics, int type);

  /**
   * Save topic.
   * 
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topic the topic
   * @param isNew is the new
   * @param isMove is the move
   * @throws Exception the exception
   */
  void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, MessageBuilder messageBuilder) throws Exception;

  /**
   * Removes the topic.
   * 
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @return the topic
   */
  Topic removeTopic(String categoryId, String forumId, String topicId);

  /**
   * Move topic.
   * 
   * @param topics the topics
   * @param destForumPath the target of forum path
   * @param mailContent mail to send notification
   * @param link to topic
   * @throws Exception the exception
   */
  void moveTopic(List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception;

  /**
   * Move topic.
   * 
   * @param srcTopicPath path of moved topic
   * @param destTopicPath the target of topic
   * @param mailContent mail to send notification
   * @param link to topic
   * @throws Exception the exception
   */
  void mergeTopic(String srcTopicPath, String destTopicPath, String mailContent, String link) throws Exception;

  /**
   * Gets the posts.
   * 
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @param isApproved is the approved
   * @param isHidden is the hidden
   * @param strQuery the str query
   * @param userLogin the user login
   * @return the posts
   * @throws Exception the exception
   */
  JCRPageList getPosts(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception;
  
  /**
   * Gets Posts and return ListAccess
   * @param filter
   * @return
   * @throws Exception
   * @since 2.2.11
   */
  ListAccess<Post> getPosts(PostFilter filter) throws Exception;

  /**
   * Gets posts of topic.
   * 
   * @param topicPath path of topic
   * @return the posts
   * @throws Exception the exception
   */
  JCRPageList getPostForSplitTopic(String topicPath) throws Exception;

  /**
   * Gets number of the posts.
   * 
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @param isApproved is the approved
   * @param isHidden is the hidden
   * @param userLogin the user login
   * @return number of the posts
   * @throws Exception the exception
   */
  long getAvailablePost(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception;

  /**
   * Gets index of last read post.
   * 
   * @param path path of post
   * @param isApproved is the approved
   * @param isHidden is the hidden
   * @param userLogin the user login
   * @return index of the post
   * @throws Exception the exception
   */
  long getLastReadIndex(String path, String isApproved, String isHidden, String userLogin) throws Exception;

  /**
   * Gets the page post by user.
   * 
   * @param userName the user name
   * @param userId the poster
   * @param isMod the role of poster
   * @param strQuery is a string. It's content have command Query. This function
   *        will return page post suitable to content of that strQuery
   * @return the page post by user
   * @throws Exception the exception
   */
  JCRPageList getPagePostByUser(String userName, String userId, boolean isMod, String strOrderBy) throws Exception;

  /**
   * This method should: 1. Check the user permission 2. Load the Page Post data
   * from the database
   * 
   * @param postId the post id
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @return the post
   * @throws Exception the exception
   */
  Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception;

  /**
   * This method should: 1. Check the user permission 2. Check the madatory
   * field of the post 3. Save the post data into the database 4. Invalidate the
   * TopicView data cache
   * 
   * @param topicId the topic id
   * @param post the post
   * @param isNew is the new
   * @param categoryId the category id
   * @param forumId the forum id
   * @throws Exception the exception
   */
  void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew, MessageBuilder messageBuilder) throws Exception;

  /**
   * Modify posts.
   * 
   * @param posts the posts
   * @param type type of post
   */
  void modifyPost(List<Post> posts, int type);

  /**
   * Removes the post.
   * 
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @param postId the post id
   * @return the post
   */
  Post removePost(String categoryId, String forumId, String topicId, String postId);

  /**
   * Move post.
   * 
   * @param posts the posts
   * @param destTopicPath the dest topic path
   * @throws Exception the exception
   */
  void movePost(String[] postPaths, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception;

  /**
   * Gets the object name by path.
   * 
   * @param path the path
   * @return the object name by path
   * @throws Exception the exception
   */
  Object getObjectNameByPath(String path) throws Exception;

  /**
   * Gets the object name by path.
   * 
   * @param path the path
   * @return the object name by path
   * @throws Exception the exception
   */
  Object getObjectNameById(String id, String type) throws Exception;

  /**
   * Gets the all link.
   * 
   * @param strQueryCate is a string. It's content have command Query. This function
   *        will return page category suitable to content of that strQueryCate
   * @param strQueryForum is a string. It's content have command Query. This function
   *        will return page forum suitable to content of that strQueryForum
   * @return the all link
   * @throws Exception the exception
   */
  List<ForumLinkData> getAllLink(String strQueryCate, String strQueryForum) throws Exception;

  /**
   * Gets the forum home path.
   * 
   * @return the forum home path
   * @throws Exception the exception
   */
  String getForumHomePath() throws Exception;

  /**
   * Adds the topic in tag.
   * 
   * @param tags the list tag is add
   * @param topicPath the topic path
   * @throws Exception the exception
   */
  void addTag(List<Tag> tags, String userName, String topicPath) throws Exception;

  /**
   * UnTag the topic in tag.
   * 
   * @param tagId the tag id
   * @param userName the user id
   * @param topicPath the topic path
   */
  void unTag(String tagId, String userName, String topicPath);

  /**
   * Gets the tag.
   * 
   * @param tagId the tag id
   * @return the tag
   * @throws Exception the exception
   */
  Tag getTag(String tagId) throws Exception;

  /**
   * Gets all the tag names.
   * 
   * @param strQuery query to get tags
   * @param userAndTopicId input id
   * @return the list names of tags
   * @throws Exception the exception
   */
  List<String> getAllTagName(String strQuery, String userAndTopicId) throws Exception;

  /**
   * Gets all the tag name in topic.
   * 
   * @param userAndTopicId input id
   * @return the list names of tags
   * @throws Exception the exception
   */
  List<String> getTagNameInTopic(String userAndTopicId) throws Exception;

  /**
   * Gets the tags.
   * 
   * @return the tags
   * @throws Exception the exception
   */
  List<Tag> getAllTags() throws Exception;

  /**
   * Gets the tags by user.
   * 
   * @param tagIds the list tag id of user tag in topic.
   * @return the tags by user add in topic
   * @throws Exception the exception
   */
  List<Tag> getMyTagInTopic(String[] tagIds) throws Exception;

  /**
   * Gets the topics by tag.
   * 
   * @param userIdAndtagId the user id and tag id (userId:tagId)
   * @param strOrderBy the topic order by
   * @return the topics by tag of user tag
   * @throws Exception the exception
   */
  JCRPageList getTopicByMyTag(String userIdAndtagId, String strOrderBy) throws Exception;

  /**
   * Save tag.
   * 
   * @param newTag the new tag
   * @throws Exception the exception
   */
  void saveTag(Tag newTag) throws Exception;

  /**
   * Save user profile.
   * 
   * @param userProfile the user profile
   * @param isOption is the option
   * @param isBan is the ban
   * @throws Exception the exception
   */
  void saveUserProfile(UserProfile userProfile, boolean isOption, boolean isBan) throws Exception;

  /**
   * Save user profile.
   * 
   * @param user user want to update
   * @throws Exception the exception
   */
  void updateUserProfile(User user) throws Exception;

  /**
   * Save user moderator.
   * 
   * @param userName username of a user
   * @param ids ids of categories or forums
   * @param isModeCate save for category or not
   * @throws Exception the exception
   */
  void saveUserModerator(String userName, List<String> ids, boolean isModeCate) throws Exception;

  /**
   * Search for user profile
   * 
   * @param userSearch user want to search
   * @return forum page list
   * @throws Exception the exception
   */
  JCRPageList searchUserProfile(String userSearch) throws Exception;

  /**
   * Gets the user info.
   * 
   * @param userName the user name
   * @return the user info
   * @throws Exception the exception
   */
  UserProfile getUserInfo(String userName) throws Exception;

  /**
   * Gets all moderators.
   * 
   * @param userName the user name
   * @param isModeCate is category or not
   * @return list of users
   * @throws Exception the exception
   */
  List<String> getUserModerator(String userName, boolean isModeCate) throws Exception;

  /**
   * Save user bookmark.
   * 
   * @param userName the user name
   * @param bookMark the book mark
   * @param isNew is the new
   * @throws Exception the exception
   */
  void saveUserBookmark(String userName, String bookMark, boolean isNew) throws Exception;

  /**
   * Save to post if this post has an user read
   * 
   * @param userId the user name
   * @param lastReadPostOfForum last post was read
   * @param lastReadPostOfTopic last post was read
   * @throws Exception the exception
   */
  void saveLastPostIdRead(String userId, String[] lastReadPostOfForum, String[] lastReadPostOfTopic) throws Exception;

  /**
   * Save user collapCategories.
   * 
   * @param userName the user name
   * @param categoryId the book mark
   * @param isNew is the new
   * @throws Exception the exception
   */
  void saveCollapsedCategories(String userName, String categoryId, boolean isAdd) throws Exception;

  /**
   * Gets the page list user profile.
   * 
   * @return the page list user profile
   * @throws Exception the exception
   */
  JCRPageList getPageListUserProfile() throws Exception;

  /**
   * Gets the quick search.
   * 
   * @param textQuery the text query
   * @param type is type user and type object(forum, topic and post)
   * @param pathQuery the path query
   * @param forumIdsOfModerator the list of forumId witch user searching has role is 'moderator'.   
   * @return the quick search
   * @throws Exception the exception
   */
  List<ForumSearch> getQuickSearch(String textQuery, String type, String pathQuery, String userId, List<String> listCateIds, List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception;

  /**
   * Gets screen name
   * 
   * @param userName username of user
   * @return screen name
   * @throws Exception the exception
   */
  String getScreenName(String userName) throws Exception;

  /**
   * Gets the advanced search.
   * 
   * @param eventQuery the event query
   * @return the advanced search
   */
  List<ForumSearch> getAdvancedSearch(ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds);

  /**
   * Save forum statistic.
   * 
   * @param forumStatistic the forum statistic
   * @throws Exception the exception
   */
  void saveForumStatistic(ForumStatistic forumStatistic) throws Exception;

  /**
   * Gets the forum statistic.
   * 
   * @return the forum statistic
   * @throws Exception the exception
   */
  ForumStatistic getForumStatistic() throws Exception;

  /**
   * Save forum administration.
   * 
   * @param forumAdministration the forum administration
   * @throws Exception the exception
   */
  void saveForumAdministration(ForumAdministration forumAdministration) throws Exception;

  /**
   * Gets the forum administration.
   * 
   * @return the forum administration
   * @throws Exception the exception
   */
  ForumAdministration getForumAdministration() throws Exception;

  /**
   * Update informations for topics and posts
   * 
   * @param topicCoutn number of  topics
   * @param postCount number of posts
   * @throws Exception the exception
   */
  void updateStatisticCounts(long topicCoutn, long postCount) throws Exception;

  /**
   * User login.
   * 
   * @param userId the user id
   * @throws Exception the exception
   */
  void userLogin(String userId) throws Exception;

  /**
   * User logout.
   * 
   * @param userId the user id
   * @throws Exception the exception
   */
  void userLogout(String userId) throws Exception;

  /**
   * Checks if is online.
   * 
   * @param userId the user id
   * @return true, if is online
   * @throws Exception the exception
   */
  boolean isOnline(String userId) throws Exception;

  /**
   * Gets the online users.
   * 
   * @return the online users
   * @throws Exception the exception
   */
  List<String> getOnlineUsers() throws Exception;

  /**
   * Gets the last login.
   * 
   * @return the last login
   * @throws Exception the exception
   */
  String getLastLogin() throws Exception;

  /**
   * Gets the private message.
   * 
   * @param userName the user name
   * @param type the type
   * @return the private message
   * @throws Exception the exception
   */
  JCRPageList getPrivateMessage(String userName, String type) throws Exception;

  /**
   * Gets new private message.
   * 
   * @param userName the user name
   * @return number of private messages
   * @throws Exception the exception
   */
  long getNewPrivateMessage(String userName) throws Exception;

  /**
   * Save private message.
   * 
   * @param privateMessage the private message
   * @throws Exception the exception
   */
  void savePrivateMessage(ForumPrivateMessage privateMessage) throws Exception;

  /**
   * Save read message.
   * 
   * @param messageId the message id
   * @param userName the user name
   * @param type the type
   * @throws Exception the exception
   */
  void saveReadMessage(String messageId, String userName, String type) throws Exception;

  /**
   * Removes the private message.
   * 
   * @param messageId the message id
   * @param userName the user name
   * @param type the type
   * @throws Exception the exception
   */
  void removePrivateMessage(String messageId, String userName, String type) throws Exception;

  /**
   * Get descriptions of forum
   * 
   * @param userId username of an user
   * @return subscription of forum
   */
  ForumSubscription getForumSubscription(String userId);

  /**
   * Save descriptions of forum
   * 
   * @param forumSubscription informations want to save
   * @param userId username of an user
   * @throws Exception the exception
   */
  void saveForumSubscription(ForumSubscription forumSubscription, String userId) throws Exception;

  /**
   * Adds the watch.
   * 
   * @param watchType the watch type
   * @param path the path
   * @param values the values
   * @throws Exception the exception
   */
  void addWatch(int watchType, String path, List<String> values, String currentUser) throws Exception;

  /**
   * Remove the watch.
   * 
   * @param watchType the watch type
   * @param path the path
   * @param values the values
   * @throws Exception the exception
   */
  void removeWatch(int watchType, String path, String values) throws Exception;

  /**
   * Get job waiting for moderator.
   * 
   * @param paths the paths
   * @return list of forum
   */
  List<ForumSearch> getJobWattingForModerator(String[] paths);

  /**
   * Get number of jobs are waiting for moderator.
   * 
   * @param userId username of an user
   * @return number of jobs
   * @throws Exception the exception
   */
  int getJobWattingForModeratorByUser(String userId) throws Exception;

  /**
   * Get information of message
   * 
   * @param name name of message
   * @return message information
   * @throws Exception the exception
   */
  SendMessageInfo getMessageInfo(String name) throws Exception;

  /**
   * Get messages are pending
   * 
   * @return pending messages
   * @throws Exception the exception
   */
  Iterator<SendMessageInfo> getPendingMessages() throws Exception;

  /**
   * Check admin role
   * 
   * @param userName username of an user
   * @return is admin or not
   * @throws Exception the exception
   */
  boolean isAdminRole(String userName) throws Exception;

  /**
   * Gets recent public posts limited by number post.
   * 
   * @param number is number of post
   * @return the list recent public post.
   * @throws Exception the exception
   */
  List<Post> getNewPosts(int number) throws Exception;
  
  /**
   * Gets recent posts for user and limited by number post.
   * 
   * @param userName is userId for check permission.
   * @param number is number of post
   * @return the list recent post for user.
   * @throws Exception the exception
   */
  List<Post> getRecentPostsForUser(String userName, int number) throws Exception;

  /**
   * Search  node
   * 
   * @param queryString query
   * @return iterator of nodes
   * @throws Exception the exception
   */
  NodeIterator search(String queryString) throws Exception;

  /**
   * evaluate active of users 
   * 
   * @param query input a query
   */
  void evaluateActiveUsers(String query);

  /**
   * create a user profile
   * 
   * @param user saved user
   * @throws Exception the exception
   */
  void createUserProfile(User user) throws Exception;

  /**
   * update user access a topic 
   * 
   * @param userId username of an user
   * @param topicId id of a topic
   */
  void updateTopicAccess(String userId, String topicId);

  /**
   * update user access a forum 
   * 
   * @param userId username of an user
   * @param forumId id of a forum
   */
  void updateForumAccess(String userId, String forumId);

  /**
   * export to xml object 
   * 
   * @param categoryId id of category
   * @param forumId id of forum
   * @param objectIds ids
   * @param nodePath path of node
   * @param bos byte array output stream
   * @param isExportAll is export all or not
   * @throws Exception the exception
   */
  Object exportXML(String categoryId, String forumId, List<String> objectIds, String nodePath, ByteArrayOutputStream bos, boolean isExportAll) throws Exception;

  /**
   * import a stream 
   * 
   * @param nodePath path of node
   * @param bis byte array input stream
   * @param typeImport type of import
   * @throws Exception the exception
   */
  //Note: used updateForum(String path) for update data after imported.
  void importXML(String nodePath, ByteArrayInputStream bis, int typeImport) throws Exception;

  /**
   * get profiles of users
   * 
   * @param userList list of users
   * @return list of profiles
   * @throws Exception the exception
   */
  List<UserProfile> getQuickProfiles(List<String> userList) throws Exception;

  /**
   * get profile of an user
   * 
   * @param userName username
   * @return object user profile
   * @throws Exception the exception
   */
  UserProfile getQuickProfile(String userName) throws Exception;

  /**
   * get more informations of user 
   * 
   * @param userProfile profile of user
   * @return user profile
   * @throws Exception the exception
   */
  UserProfile getUserInformations(UserProfile userProfile) throws Exception;

  /**
   * get default user profile
   * 
   * @param userName username of a user
   * @param ip current ip
   * @return user profile
   * @throws Exception the exception
   */
  UserProfile getDefaultUserProfile(String userName, String ip) throws Exception;

  /**
   * update user profile
   * 
   * @param userProfile input user profile
   * @return user profile
   * @throws Exception the exception
   */
  UserProfile updateUserProfileSetting(UserProfile userProfile) throws Exception;

  /**
   * get bookmarks of user
   * 
   * @param userName username of a user
   * @return bookmarks
   * @throws Exception the exception
   */
  List<String> getBookmarks(String userName) throws Exception;

  /**
   * get user profile
   * 
   * @param userName username of a user
   * @return user profile
   * @throws Exception the exception
   */
  UserProfile getUserSettingProfile(String userName) throws Exception;

  /**
   * get user profile manager
   * 
   * @param userName username of a user
   * @return user profile
   * @throws Exception the exception
   */
  UserProfile getUserProfileManagement(String userName) throws Exception;

  /**
   * save user profile
   * 
   * @param userProfile saved user profile
   * @throws Exception the exception
   */
  void saveUserSettingProfile(UserProfile userProfile) throws Exception;

  /**
   * update forum
   * 
   * @param path path to forum
   * @throws Exception the exception
   */
  void updateForum(String path) throws Exception;

  /**
   * get list of banded ips
   * 
   * @return list banded ips
   * @throws Exception the exception
   */
  List<String> getBanList() throws Exception;

  /**
   * add ip to ban
   * 
   * @param ip add ip
   * @throws Exception the exception
   */
  boolean addBanIP(String ip) throws Exception;

  /**
   * remove banded ip
   * 
   * @param ip removed banded ip
   * @throws Exception the exception
   */
  void removeBan(String ip) throws Exception;

  /**
   * get list of band ips in forum
   * 
   * @param forumId id of forum
   * @return list band ips
   * @throws Exception the exception
   */
  List<String> getForumBanList(String forumId) throws Exception;

  /**
   * add ip to list of band ips in forum
   * 
   * @param ip add ip
   * @param forumId id of forum
   * @throws Exception the exception
   */
  boolean addBanIPForum(String ip, String forumId) throws Exception;

  /**
   * remove ip from list of band ips in forum
   * 
   * @param ip removed ip
   * @param forumId id of forum
   * @throws Exception the exception
   */
  void removeBanIPForum(String ip, String forumId) throws Exception;

  /**
   * get list of posts
   * 
   * @param ip input ip
   * @param strOrderBy input order
   * @return list of posts
   * @throws Exception the exception
   */
  JCRPageList getListPostsByIP(String ip, String strOrderBy) throws Exception;

  /**
   * register listener
   * 
   * @param categoryId id of category
   * @throws Exception the exception
   */
  void registerListenerForCategory(String categoryId) throws Exception;

  /**
   * remove listener
   * 
   * @param path path of category
   * @throws Exception the exception
   */
  void unRegisterListenerForCategory(String path) throws Exception;

  /**
   * get avatar
   * 
   * @param userName username
   * @return avatar
   * @throws Exception the exception
   */
  ForumAttachment getUserAvatar(String userName) throws Exception;

  /**
   * save avatar for user
   * 
   * @param userId username
   * @param fileAttachment avatar
   * @throws Exception the exception
   */
  void saveUserAvatar(String userId, ForumAttachment fileAttachment) throws Exception;

  /**
   * set default avatar
   * 
   * @param userName username
   */
  void setDefaultAvatar(String userName);

  /**
   * get watches
   * 
   * @param userId username
   * @return watches by user
   * @throws Exception the exception
   */
  List<Watch> getWatchByUser(String userId) throws Exception;

  /**
   * update watch email addresss for user 
   * 
   * @param listNodeId watch node
   * @param newEmailAdd watch email address
   * @param userId username
   * @throws Exception the exception
   */
  void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId) throws Exception;

  /**
   * get prune settings
   * @return list of prune settings
   * @throws Exception the exception
   */
  List<PruneSetting> getAllPruneSetting() throws Exception;

  /**
   * get prune setting
   * 
   * @param forumPath path of forum
   * @return prune setting
   * @throws Exception the exception
   */
  PruneSetting getPruneSetting(String forumPath) throws Exception;

  /**
   * save a prune setting
   * 
   * @param pruneSetting input prune setting
   * @throws Exception the exception
   */
  void savePruneSetting(PruneSetting pruneSetting) throws Exception;

  /**
   * run prune setting
   * 
   * @param pSetting input prune setting
   * @throws Exception the exception
   */
  void runPrune(PruneSetting pSetting) throws Exception;

  /**
   * run prune setting
   * 
   * @param forumPath path of forum
   * @throws Exception the exception
   */
  void runPrune(String forumPath) throws Exception;

  /**
   * check prune setting
   * 
   * @param pSetting input prune setting
   * @return prune setting
   * @throws Exception the exception
   */
  long checkPrune(PruneSetting pSetting) throws Exception;

  /**
   * get list types of topic
   * 
   * @return list of topic type
   */
  List<TopicType> getTopicTypes();

  /**
   * get type of a topic
   * 
   * @param Id id of topic
   * @return object topic type
   * @throws Exception the exception
   */
  TopicType getTopicType(String Id) throws Exception;

  /**
   * save type of a topic
   * 
   * @param topicType object topic type
   * @throws Exception the exception
   */
  void saveTopicType(TopicType topicType) throws Exception;

  /**
   * remove type of a topic
   * 
   * @param topicTypeId id of topic type
   * @throws Exception the exception
   */
  void removeTopicType(String topicTypeId) throws Exception;

  /**
   * get page list of topics
   * 
   * @param type type of topic
   * @return page list of topics
   * @throws Exception the exception
   */
  JCRPageList getPageTopicByType(String type) throws Exception;

  /**
   * get list of topics
   * 
   * @param categoryId id of category
   * @param forumId id of forum
   * @param string condition
   * @param strOrderBy input order
   * @return topic page list
   * @throws Exception the exception
   */
  LazyPageList<Topic> getTopicList(String categoryId, String forumId, String string, String strOrderBy, int pageSize) throws Exception;

  /**
   * get summaries
   * 
   * @param categoryId id of category
   * @param strQuery query
   * @return page list of forums
   * @throws Exception the exception
   */
  List<Forum> getForumSummaries(String categoryId, String strQuery) throws Exception;

  /**
   * update user profile
   * 
   * @param name username
   * @throws Exception the exception
   */
  void updateUserProfileInfo(String name) throws Exception;

  /**
   * <p>
   * Add a new member to the forum. The forum profile is created and statistics
   * updated
   * </p>
   * 
   * @param user user that becomes a new forum member
   * @param profileTemplate user profile template to be used for default
   *          settings
   * @throws Exception
   */
  void addMember(User user, UserProfile profileTemplate) throws Exception;

  /**
   * <p>
   * Removes an existing member from the forum. The forum profile is deleted and
   * statistics updated
   * </p>
   * 
   * @param user user that leaves forum
   * @throws Exception
   */
  void removeMember(User user) throws Exception;

  /**
   * <p>
   * Update information of logged in users that records in a queue to statistic and profile
   * </p>
   * 
   * @param
   * @throws Exception
   */
  public void updateLoggedinUsers() throws Exception;

  /**
   * update when delete an user
   * 
   * @param userName username
   * @throws Exception the exception
   */
  public void calculateDeletedUser(String userName) throws Exception;

  /**
   * update data when delete a group
   * 
   * @param groupId the identity of group.
   * @throws Exception the exception
   */
  public void calculateDeletedGroup(String groupId, String groupName) throws Exception;

  /**
   * create RSS
   * 
   * @param objectId id of forum
   * @return input stream
   * @throws Exception the exception
   */
  public InputStream createForumRss(String objectId, String link) throws Exception;

  /**
   * create RSS of user
   * 
   * @param userId username
   * @param link link of RSS
   * @return input stream
   * @throws Exception the exception
   */
  public InputStream createUserRss(String userId, String link) throws Exception;

  /**
   * add listener
   * 
   * @param listener add listener
   * @throws Exception the exception
   */
  public void addListenerPlugin(ForumEventListener listener) throws Exception;
  
  /**
   * remove user-profile of user login cache in service
   * 
   * @param userName 
   * @throws Exception 
   */
  public void removeCacheUserProfile(String userName) throws Exception;
}
