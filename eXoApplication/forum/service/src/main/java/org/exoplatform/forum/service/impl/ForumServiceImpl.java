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
package org.exoplatform.forum.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jcr.NodeIterator;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.forum.service.CacheUserProfile;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.DataStorage;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumEventLifeCycle;
import org.exoplatform.forum.service.ForumEventListener;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.ForumStatisticsService;
import org.exoplatform.forum.service.ForumSubscription;
import org.exoplatform.forum.service.InitializeForumPlugin;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.LazyPageList;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.service.SendMessageInfo;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.service.UserLoginLogEntry;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.conf.RoleRulesPlugin;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.picocontainer.Startable;
import org.quartz.JobDetail;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
@ManagedBy(ForumServiceManaged.class)
public class ForumServiceImpl implements ForumService, Startable {

  private static final Log           log             = ExoLogger.getLogger(ForumServiceImpl.class);

  private DataStorage                storage;

  private ForumServiceManaged        managementView;                                                  // will be automatically set at @ManagedBy processing

  private Map<String, List<String>>  onlineUserMap   = new HashMap<String, List<String>>();

  final Queue<UserLoginLogEntry>     queue           = new ConcurrentLinkedQueue<UserLoginLogEntry>();

//  private String                     lastLogin_      = "";
  private Map<String, String>        lastLoginMap      = new HashMap<String, String>();

  private ForumStatisticsService     forumStatisticsService;

  private JobSchedulerService        jobSchedulerService;

  protected List<ForumEventListener> listeners_      = new ArrayList<ForumEventListener>(3);
  
  public ForumServiceImpl(InitParams params, ExoContainerContext context, DataStorage dataStorage, ForumStatisticsService forumStatisticsService, JobSchedulerService jobSchedulerService) {
    this.storage = dataStorage;
    this.forumStatisticsService = forumStatisticsService;
    this.jobSchedulerService = jobSchedulerService;
  }

  /**
   * {@inheritDoc}
   */
  public void addPlugin(ComponentPlugin plugin) throws Exception {
    storage.addPlugin(plugin);
  }

  /**
   * {@inheritDoc}
   */
  public void addRolePlugin(ComponentPlugin plugin) throws Exception {
    storage.addRolePlugin(plugin);
  }

  /**
   * {@inheritDoc}
   */
  public void addInitialDefaultDataPlugin(ComponentPlugin plugin) throws Exception {
    storage.addInitialDefaultDataPlugin(plugin);
  }

  public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception {
    storage.addInitialDataPlugin(plugin);
  }

  public void start() {

    try {
      log.info("initializing category listeners...");
      storage.initCategoryListener();
    } catch (Exception e) {
      log.error("Error while updating category listeners " + e.getMessage());
    }

    try {
      log.info("initializing default data...");
      storage.initDefaultData();

    } catch (Exception e) {
      log.error("Error while initializing default data: " + e.getMessage());
    }

    try {
      log.info("initializing data...");
      storage.initDataPlugin();
    } catch (Exception e) {
      log.error("Error while initializing data plugin: " + e.getMessage());
    }

    try {
      log.info("Calculating active users...");
      storage.evaluateActiveUsers("");
    } catch (Exception e) {
      log.error("Error while calculating active users: " + e.getMessage());
    }

    // init Calculate Moderators listeners
    try {
      log.info("initializing Calculate Moderators listeners...");
      storage.addCalculateModeratorEventListener();
    } catch (Exception e) {
      log.error("Error while initializing Moderators listeners: " + e.getMessage());
    }

    // initialize auto prune schedules
    try {
      log.info("initializing prune schedulers...");
      storage.initAutoPruneSchedules();
    } catch (Exception e) {
      log.error("Error while initializing Prune schedulers: " + e.getMessage());
    }

    // init deleted user listeners
    try {
      log.info("initializing deleted user listener...");
      storage.addDeletedUserCalculateListener();
    } catch (Exception e) {
      log.error("Error while initializing Prune schedulers: " + e.getMessage());
    }

    // management views
    try {
      log.info("initializing management view...");
      managePlugins();
      manageStorage();
      manageJobs();
    } catch (Exception e) {
      log.error("Error while initializing Management view: " + e.getMessage());
    }
  }

  private void manageStorage() {
    managementView.registerStorageManager(storage);
  }

  @SuppressWarnings("unchecked")
  private void manageJobs() {
    try {
      List<JobDetail> jobs = jobSchedulerService.getAllJobs();
      for (JobDetail jobDetail : jobs) {
        managementView.registerJobManager(new JobManager(jobDetail));
      }
    } catch (Exception e) {
      log.error("failed to register jobs manager", e);
    }
  }

  private void managePlugins() {
    List<RoleRulesPlugin> plugins = storage.getRulesPlugins();
    for (RoleRulesPlugin plugin2 : plugins) {
      managementView.registerPlugin(plugin2);
    }

    List<InitializeForumPlugin> defaultPlugins = storage.getDefaultPlugins();
    for (InitializeForumPlugin plugin2 : defaultPlugins) {
      managementView.registerPlugin(plugin2);
    }

  }

  public void stop() {
  }

  public void addMember(User user, UserProfile profileTemplate) throws Exception {
    boolean added = storage.populateUserProfile(user, profileTemplate, true);
    if (added) {
      forumStatisticsService.addMember(user.getUserName());
    }
  }

  public void calculateDeletedUser(String userName) throws Exception {
    storage.calculateDeletedUser(userName);
  }

  /**
   * {@inheritDoc}
   */
  public void removeMember(User user) throws Exception {
    String userName = user.getUserName();
    if (storage.deleteUserProfile(userName))
      forumStatisticsService.removeMember(userName);
    UserProfile userProfile = CacheUserProfile.getFromCache(userName);
    if (userProfile != null) {
      UserProfile profile = new UserProfile();
      profile.setUserId(userName);
      profile.setUserTitle(UserProfile.USER_REMOVED);
      profile.setUserRole(UserProfile.USER_DELETED);
      profile.setIsBanned(true);
      CacheUserProfile.storeInCache(userName, profile);
    }
  }

  public void createUserProfile(User user) throws Exception {

  }

  
  public void calculateDeletedGroup(String groupId, String groupName) throws Exception {
    storage.calculateDeletedGroup(groupId, groupName);
  }
  
  /**
   * {@inheritDoc}
   */
  public void updateUserProfile(User user) throws Exception {
    storage.populateUserProfile(user, null, false);
  }

  /**
   * @deprecated use {@link #updateUserProfile(User)}
   */
  public void saveEmailUserProfile(String userId, String email) throws Exception {
  }

  /**
   * {@inheritDoc}
   */
  public void saveCategory(Category category, boolean isNew) throws Exception {
    storage.saveCategory(category, isNew);
    for (ForumEventLifeCycle f : listeners_) {
      f.saveCategory(category);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void calculateModerator(String categoryPath, boolean isNew) throws Exception {
    storage.calculateModerator(categoryPath, false);
  }

  /**
   * {@inheritDoc}
   */
  public Category getCategory(String categoryId) throws Exception {
    return storage.getCategory(categoryId);
  }

  /**
   * {@inheritDoc}
   */
  public String[] getPermissionTopicByCategory(String categoryId, String type) throws Exception {
    return storage.getPermissionTopicByCategory(categoryId, type);
  }

  /**
   * {@inheritDoc}
   */
  public List<Category> getCategories() {
    return storage.getCategories();
  }

  /**
   * {@inheritDoc}
   */
  public Category removeCategory(String categoryId) throws Exception {
    return storage.removeCategory(categoryId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveModOfCategory(List<String> moderatorCate, String userId, boolean isAdd) {
    storage.saveModOfCategory(moderatorCate, userId, isAdd);
  }

  /**
   * {@inheritDoc}
   */
  public void modifyForum(Forum forum, int type) throws Exception {
    storage.modifyForum(forum, type);
  }

  /**
   * {@inheritDoc}
   */
  public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception {
    storage.saveForum(categoryId, forum, isNew);
    for (ForumEventLifeCycle f : listeners_) {
      f.saveForum(forum);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void saveModerateOfForums(List<String> forumPaths, String userName, boolean isDelete) throws Exception {
    storage.saveModerateOfForums(forumPaths, userName, isDelete);
  }

  /**
   * {@inheritDoc}
   */
  public void moveForum(List<Forum> forums, String destCategoryPath) throws Exception {
    storage.moveForum(forums, destCategoryPath);
  }

  /**
   * {@inheritDoc}
   */
  public Forum getForum(String categoryId, String forumId){
    return storage.getForum(categoryId, forumId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Forum> getForums(String categoryId, String strQuery) throws Exception {
    return storage.getForums(categoryId, strQuery);
  }

  /**
   * {@inheritDoc}
   */
  public List<Forum> getForumSummaries(String categoryId, String strQuery) throws Exception {
    return storage.getForumSummaries(categoryId, strQuery);
  }

  /**
   * {@inheritDoc}
   */
  public Forum removeForum(String categoryId, String forumId) throws Exception {
    return storage.removeForum(categoryId, forumId);
  }

  /**
   * {@inheritDoc}
   */
  public void modifyTopic(List<Topic> topics, int type) {
    storage.modifyTopic(topics, type);
  }

  /**
   * 
   * @deprecated use {@link #saveTopic(String, String, Topic, boolean, boolean, MessageBuilder)}
   */
  @Deprecated
  public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent) throws Exception {
    saveTopic(categoryId, forumId, topic, isNew, isMove, new MessageBuilder());
  }

  public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, MessageBuilder messageBuilder) throws Exception {
    storage.saveTopic(categoryId, forumId, topic, isNew, isMove, messageBuilder);
    for (ForumEventLifeCycle f : listeners_) {
      if (isNew)
        f.addTopic(topic, categoryId, forumId);
      else
        f.updateTopic(topic, categoryId, forumId);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Topic getTopic(String categoryId, String forumId, String topicId, String userRead) throws Exception {
    return storage.getTopic(categoryId, forumId, topicId, userRead);
  }

  /**
   * {@inheritDoc}
   */
  public void setViewCountTopic(String path, String userRead){
    storage.setViewCountTopic(path, userRead);
  }

  /**
   * {@inheritDoc}
   */
  public Topic getTopicSummary(String topicPath) throws Exception {
    return storage.getTopicSummary(topicPath, true);
  }

  /**
   * {@inheritDoc}
   */
  public Topic getTopicByPath(String topicPath, boolean isLastPost) throws Exception {
    return storage.getTopicByPath(topicPath, isLastPost);
  }

  public Topic getTopicUpdate(Topic topic, boolean isSummary) throws Exception {
    return storage.getTopicUpdate(topic, isSummary);
  }

  /**
   * {@inheritDoc}
   */
  public LazyPageList<Topic> getTopicList(String categoryId, String forumId, String strQuery, String strOrderBy, int pageSize) throws Exception {
    return storage.getTopicList(categoryId, forumId, strQuery, strOrderBy, pageSize);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPageTopic(String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception {
    return storage.getPageTopic(categoryId, forumId, strQuery, strOrderBy);
  }

  /**
   * {@inheritDoc}
   */
  public List<Topic> getTopics(String categoryId, String forumId) throws Exception {
    return storage.getTopics(categoryId, forumId);
  }

  /**
   * {@inheritDoc}
   */
  public void moveTopic(List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception {
    storage.moveTopic(topics, destForumPath, mailContent, link);
    CacheUserProfile.clearCache();
  }

  /**
   * {@inheritDoc}
   */
  public Topic removeTopic(String categoryId, String forumId, String topicId) {
    CacheUserProfile.clearCache();
    return storage.removeTopic(categoryId, forumId, topicId);
  }

  /**
   * {@inheritDoc}
   */
  public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage.getPost(categoryId, forumId, topicId, postId);
  }

  /**
   * {@inheritDoc}
   */
  public long getLastReadIndex(String path, String isApproved, String isHidden, String userLogin) throws Exception {
    return storage.getLastReadIndex(path, isApproved, isHidden, userLogin);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPostForSplitTopic(String topicPath) throws Exception {
    return storage.getPostForSplitTopic(topicPath);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPosts(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception {
    return storage.getPosts(categoryId, forumId, topicId, isApproved, isHidden, strQuery, userLogin);
  }

  /**
   * {@inheritDoc}
   */
  public long getAvailablePost(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception {
    return storage.getAvailablePost(categoryId, forumId, topicId, isApproved, isHidden, userLogin);
  }

  /**
   * 
   * @deprecated use {@link #savePost(String, String, String, Post, boolean, MessageBuilder)}
   */
  @Deprecated
  public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew, String defaultEmailContent) throws Exception {
    savePost(categoryId, forumId, topicId, post, isNew, new MessageBuilder());
  }

  public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew, MessageBuilder messageBuilder) throws Exception {
    storage.savePost(categoryId, forumId, topicId, post, isNew, messageBuilder);
    for (ForumEventLifeCycle f : listeners_) {
      if (isNew)
        f.addPost(post, categoryId, forumId, topicId);
      else
        f.updatePost(post, categoryId, forumId, topicId);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void modifyPost(List<Post> posts, int type){
    storage.modifyPost(posts, type);
  }

  /**
   * {@inheritDoc}
   */
  public void movePost(List<Post> posts, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception {
    String[] postPaths = new String[posts.size()];
    int i = 0;
    for (Post p : posts) {
      postPaths[i] = p.getPath();
      ++i;
    }
    movePost(postPaths, destTopicPath, isCreatNewTopic, mailContent, link);
  }

  /**
   * {@inheritDoc}
   */
  public void movePost(String[] postPaths, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception {
    storage.movePost(postPaths, destTopicPath, isCreatNewTopic, mailContent, link);
    CacheUserProfile.clearCache();
  }

  /**
   * {@inheritDoc}
   */
  public void mergeTopic(String srcTopicPath, String destTopicPath, String mailContent, String link) throws Exception {
    storage.mergeTopic(srcTopicPath, destTopicPath, mailContent, link);
    CacheUserProfile.clearCache();
  }

  /**
   * {@inheritDoc}
   */
  public Post removePost(String categoryId, String forumId, String topicId, String postId) {
    CacheUserProfile.clearCache();
    return storage.removePost(categoryId, forumId, topicId, postId);
  }

  /**
   * {@inheritDoc}
   */
  public Object getObjectNameByPath(String path) throws Exception {
    return storage.getObjectNameByPath(path);
  }

  /**
   * {@inheritDoc}
   */
  public Object getObjectNameById(String path, String type) throws Exception {
    return storage.getObjectNameById(path, type);
  }

  /**
   * {@inheritDoc}
   */
  public List<ForumLinkData> getAllLink(String strQueryCate, String strQueryForum) throws Exception {
    return storage.getAllLink(strQueryCate, strQueryForum);
  }

  /**
   * {@inheritDoc}
   */
  public String getForumHomePath() throws Exception {
    return storage.getDataLocation().getForumHomeLocation();
  }

  /**
   * {@inheritDoc}
   */
  /*
   * public Poll getPoll(String categoryId, String forumId, String topicId) throws Exception { return storage.getPoll(categoryId, forumId, topicId) ; }
   *//**
     * {@inheritDoc}
     */
  /*
   * public Poll removePoll(String categoryId, String forumId, String topicId) throws Exception { return storage.removePoll(categoryId, forumId, topicId); }
   *//**
     * {@inheritDoc}
     */
  /*
   * public void savePoll(String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception { storage.savePoll(categoryId, forumId, topicId, poll, isNew, isVote) ; }
   *//**
     * {@inheritDoc}
     */
  /*
   * public void setClosedPoll(String categoryId, String forumId, String topicId, Poll poll) throws Exception { storage.setClosedPoll(categoryId, forumId, topicId, poll) ; }
   */
  /**
   * {@inheritDoc}
   */
  public void addTag(List<Tag> tags, String userName, String topicPath) throws Exception {
    storage.addTag(tags, userName, topicPath);
  }

  /**
   * {@inheritDoc}
   */
  public List<Tag> getAllTags() throws Exception {
    return storage.getAllTags();
  }

  /**
   * {@inheritDoc}
   */
  public List<Tag> getMyTagInTopic(String[] tagIds) throws Exception {
    return storage.getMyTagInTopic(tagIds);
  }

  /**
   * {@inheritDoc}
   */
  public Tag getTag(String tagId) throws Exception {
    return storage.getTag(tagId);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getAllTagName(String strQuery, String userAndTopicId) throws Exception {
    return storage.getAllTagName(strQuery, userAndTopicId);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getTagNameInTopic(String userAndTopicId) throws Exception {
    return storage.getTagNameInTopic(userAndTopicId);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getTopicByMyTag(String userIdAndtagId, String strOrderBy) throws Exception {
    return storage.getTopicByMyTag(userIdAndtagId, strOrderBy);
  }

  /**
   * {@inheritDoc}
   */
  public void saveTag(Tag newTag) throws Exception {
    storage.saveTag(newTag);
  }

  /**
   * {@inheritDoc}
   */
  public void unTag(String tagId, String userName, String topicPath) {
    storage.unTag(tagId, userName, topicPath);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserModerator(String userName, List<String> ids, boolean isModeCate) throws Exception {
    storage.saveUserModerator(userName, ids, isModeCate);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserProfile(UserProfile userProfile, boolean isOption, boolean isBan) throws Exception {
    String userId = userProfile.getUserId();
    storage.saveUserProfile(userProfile, isOption, isBan);
    removeCacheUserProfile(userId);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile getUserInfo(String userName) throws Exception {
    return storage.getUserInfo(userName);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getUserModerator(String userName, boolean isModeCate) throws Exception {
    return storage.getUserModerator(userName, isModeCate);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile getUserProfileManagement(String userName) throws Exception {
    return storage.getUserProfileManagement(userName);
  }

  /**
   * {@inheritDoc}
   */
  public void saveLastPostIdRead(String userId, String[] lastReadPostOfForum, String[] lastReadPostOfTopic) throws Exception {
    storage.saveLastPostIdRead(userId, lastReadPostOfForum, lastReadPostOfTopic);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserBookmark(String userName, String bookMark, boolean isNew) throws Exception {
    storage.saveUserBookmark(userName, bookMark, isNew);
    removeCacheUserProfile(userName);
  }

  /**
   * {@inheritDoc}
   */
  public void saveCollapsedCategories(String userName, String categoryId, boolean isAdd) throws Exception {
    storage.saveCollapsedCategories(userName, categoryId, isAdd);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPageListUserProfile() throws Exception {
    return storage.getPageListUserProfile();
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPrivateMessage(String userName, String type) throws Exception {
    return storage.getPrivateMessage(userName, type);
  }

  /**
   * {@inheritDoc}
   */
  public long getNewPrivateMessage(String userName) throws Exception {
    return storage.getNewPrivateMessage(userName);
  }

  /**
   * {@inheritDoc}
   */
  public void removePrivateMessage(String messageId, String userName, String type) throws Exception {
    storage.removePrivateMessage(messageId, userName, type);
  }

  /**
   * {@inheritDoc}
   */
  public void saveReadMessage(String messageId, String userName, String type) throws Exception {
    storage.saveReadMessage(messageId, userName, type);
  }

  /**
   * {@inheritDoc}
   */
  public void savePrivateMessage(ForumPrivateMessage privateMessage) throws Exception {
    storage.savePrivateMessage(privateMessage);
  }

  /**
   * {@inheritDoc}
   */
  public ForumSubscription getForumSubscription(String userId) {
    return storage.getForumSubscription(userId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveForumSubscription(ForumSubscription forumSubscription, String userId) throws Exception {
    storage.saveForumSubscription(forumSubscription, userId);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPageTopicOld(long date, String forumPatch) throws Exception {
    return storage.getPageTopicOld(date, forumPatch);
  }

  /**
   * {@inheritDoc}
   */
  public List<Topic> getAllTopicsOld(long date, String forumPatch) throws Exception {
    return storage.getAllTopicsOld(date, forumPatch);
  }

  /**
   * {@inheritDoc}
   */
  public long getTotalTopicOld(long date, String forumPatch) {
    return storage.getTotalTopicOld(date, forumPatch);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPageTopicByUser(String userName, boolean isMod, String strOrderBy) throws Exception {
    return storage.getPageTopicByUser(userName, isMod, strOrderBy);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPagePostByUser(String userName, String userId, boolean isMod, String strOrderBy) throws Exception {
    return storage.getPagePostByUser(userName, userId, isMod, strOrderBy);
  }

  /**
   * {@inheritDoc}
   */
  public ForumStatistic getForumStatistic() throws Exception {
    return storage.getForumStatistic();
  }

  /**
   * {@inheritDoc}
   */
  public void saveForumStatistic(ForumStatistic forumStatistic) throws Exception {
    storage.saveForumStatistic(forumStatistic);
  }

  /**
   * {@inheritDoc}
   */
  public void updateStatisticCounts(long topicCount, long postCount) throws Exception {
    storage.updateStatisticCounts(topicCount, postCount);
  }

  /**
   * {@inheritDoc}
   */
  public List<ForumSearch> getQuickSearch(String textQuery, String type, String pathQuery, String userId, List<String> listCateIds, List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception {
    return storage.getQuickSearch(textQuery, type, pathQuery, userId, listCateIds, listForumIds, forumIdsOfModerator);
  }

  /**
   * {@inheritDoc}
   */
  public List<ForumSearch> getAdvancedSearch(ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds){
    return storage.getAdvancedSearch(eventQuery, listCateIds, listForumIds);
  }

  /**
   * {@inheritDoc}
   */
  public ForumAdministration getForumAdministration() throws Exception {
    return storage.getForumAdministration();
  }

  /**
   * {@inheritDoc}
   */
  public void saveForumAdministration(ForumAdministration forumAdministration) throws Exception {
    storage.saveForumAdministration(forumAdministration);
  }

  /**
   * {@inheritDoc}
   */
  public void addWatch(int watchType, String path, List<String> values, String currentUser) throws Exception {
    storage.addWatch(watchType, path, values, currentUser);
  }

  /**
   * {@inheritDoc}
   */
  public void removeWatch(int watchType, String path, String values) throws Exception {
    storage.removeWatch(watchType, path, values);
  }

  /**
   * {@inheritDoc}
   */
  public List<ForumSearch> getJobWattingForModerator(String[] paths){
    return storage.getJobWattingForModerator(paths);
  }

  /**
   * {@inheritDoc}
   */
  public int getJobWattingForModeratorByUser(String userId) throws Exception {
    return storage.getJobWattingForModeratorByUser(userId);
  }

  /**
   * {@inheritDoc}
   */
  public void updateLoggedinUsers() throws Exception {
    UserLoginLogEntry loginEntry = queue.poll();
    if (loginEntry == null)
      return;
    int maxOnline = 1;
    Calendar timestamp = loginEntry.loginTime;
    while (loginEntry != null) {
      if(Utils.getCurrentTenantName().equals(loginEntry.tenantName)) {
        storage.updateLastLoginDate(loginEntry.userName);
        if (loginEntry.totalOnline > maxOnline) {
          maxOnline = loginEntry.totalOnline;
          timestamp = loginEntry.loginTime;
        }
      }
      loginEntry = queue.poll();
    }

    ForumStatistic stats = storage.getForumStatistic();
    int mostOnline = 0;
    String mostUsersOnline = stats.getMostUsersOnline();
    if (mostUsersOnline != null && mostUsersOnline.length() > 0) {
      String[] array = mostUsersOnline.split(","); // OMG responsible of this should loose a finger!
      try {
        mostOnline = Integer.parseInt(array[0].trim());
      } catch (NumberFormatException e) {
        mostOnline = 0;
      }
    }
    if (maxOnline > mostOnline) {
      stats.setMostUsersOnline(maxOnline + ", at " + timestamp.getTimeInMillis());
    } else if (mostOnline == 0) {
      // this case is expected to appear when the first user logins to system and the statistic is not activated before.
      // the maximum number of online users will jump from N/A to 1
      stats.setMostUsersOnline("1, at " + timestamp.getTimeInMillis());
    }
    storage.saveForumStatistic(stats);

  }

  /**
   * {@inheritDoc}
   */
  public void userLogin(String userId) throws Exception {
    // Note: login and onlineUserlist shoudl be anaged by forumStatisticsService.memberIn();
    String currentTenant = Utils.getCurrentTenantName();
    lastLoginMap.put(currentTenant, userId);
    List<String> onlinUsers = Utils.getOnlineUserByTenantName(onlineUserMap);
    if (!onlinUsers.contains(userId)) {
      onlinUsers.add(userId);
      onlineUserMap.put(currentTenant, onlinUsers);
    }
    UserLoginLogEntry loginEntry = new UserLoginLogEntry(currentTenant, userId, onlinUsers.size(), 
                                                         CommonUtils.getGreenwichMeanTime());
    queue.add(loginEntry);
    CacheUserProfile.storeInCache(userId, storage.getDefaultUserProfile(userId, null));
  }

  /**
   * {@inheritDoc}
   */
  public void userLogout(String userId) throws Exception {
    List<String> onlinUsers = Utils.getOnlineUserByTenantName(onlineUserMap);
    if (onlinUsers.contains(userId)) {
      onlinUsers.remove(userId);
      onlineUserMap.put(Utils.getCurrentTenantName(), onlinUsers);
    }
    removeCacheUserProfile(userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isOnline(String userId) throws Exception {
    return Utils.getOnlineUserByTenantName(onlineUserMap).contains(userId);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getOnlineUsers() throws Exception {
    return Utils.getOnlineUserByTenantName(onlineUserMap);
  }

  /**
   * {@inheritDoc}
   */
  public String getLastLogin() throws Exception {
    return lastLoginMap.get(Utils.getCurrentTenantName());
  }

  /**
   * {@inheritDoc}
   */
  public SendMessageInfo getMessageInfo(String name) throws Exception {
    return storage.getMessageInfo(name);
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<SendMessageInfo> getPendingMessages() throws Exception {
    return storage.getPendingMessages();
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList searchUserProfile(String userSearch) throws Exception {
    return storage.searchUserProfile(userSearch);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAdminRole(String userName) throws Exception {
    return storage.isAdminRole(userName);
  }

  /**
   * {@inheritDoc}
   */
  public List<Post> getNewPosts(int number) throws Exception {
    return storage.getNewPosts(number);
  }

  /**
   * {@inheritDoc}
   */
  public List<Post> getRecentPostsForUser(String userName, int number) throws Exception {
    return storage.getRecentPostsForUser(userName, number);
  }

  public NodeIterator search(String queryString) throws Exception {
    return storage.search(queryString);
  }

  /**
   * {@inheritDoc}
   */
  public void evaluateActiveUsers(String query) {
    storage.evaluateActiveUsers(query);
  }

  /**
   * {@inheritDoc}
   */
  public void updateTopicAccess(String userId, String topicId) {
    storage.updateTopicAccess(userId, topicId);
  }

  /**
   * {@inheritDoc}
   */
  public void updateForumAccess(String userId, String forumId){
    storage.updateForumAccess(userId, forumId);
  }

  /**
   * {@inheritDoc}
   */
  public Object exportXML(String categoryId, String forumId, List<String> objectIds, String nodePath, ByteArrayOutputStream bos, boolean isExportAll) throws Exception {
    return storage.exportXML(categoryId, forumId, objectIds, nodePath, bos, isExportAll);
  }

  /**
   * {@inheritDoc}
   */
  public List<UserProfile> getQuickProfiles(List<String> userList) throws Exception {
    return storage.getQuickProfiles(userList);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile getQuickProfile(String userName) throws Exception {
    return storage.getQuickProfile(userName);
  }

  /**
   * {@inheritDoc}
   */
  public String getScreenName(String userName) throws Exception {
    return storage.getScreenName(userName);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile getUserInformations(UserProfile userProfile) throws Exception {
    return storage.getUserInformations(userProfile);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile getDefaultUserProfile(String userName, String ip) throws Exception {
    UserProfile userProfile = CacheUserProfile.getFromCache(userName);
    if (userProfile == null) {
      userProfile = storage.getDefaultUserProfile(userName, null);
      CacheUserProfile.storeInCache(userName, userProfile);
    }
    if (!userProfile.getIsBanned() && ip != null) {
      userProfile.setIsBanned(storage.isBanIp(ip));
    }
    return userProfile;
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile updateUserProfileSetting(UserProfile userProfile) throws Exception {
    return storage.updateUserProfileSetting(userProfile);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getBookmarks(String userName) throws Exception {
    return storage.getBookmarks(userName);
  }

  /**
   * {@inheritDoc}
   */
  public UserProfile getUserSettingProfile(String userName) throws Exception {
    return storage.getUserSettingProfile(userName);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserSettingProfile(UserProfile userProfile) throws Exception {
    storage.saveUserSettingProfile(userProfile);
    removeCacheUserProfile(userProfile.getUserId());
  }

  /**
   * {@inheritDoc}
   */
  public void importXML(String nodePath, ByteArrayInputStream bis, int typeImport) throws Exception {
    storage.importXML(nodePath, bis, typeImport);
  }

  /**
   * {@inheritDoc}
  public void updateDataImported() throws Exception{
    storage.updateDataImported();
  }
   */

  /**
   * {@inheritDoc}
   */
  public void updateForum(String path) throws Exception {
    storage.updateForum(path);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getBanList() throws Exception {
    return storage.getBanList();
  }

  /**
   * {@inheritDoc}
   */
  public boolean addBanIP(String ip) throws Exception {
    return storage.addBanIP(ip);
  }

  /**
   * {@inheritDoc}
   */
  public void removeBan(String ip) throws Exception {
    storage.removeBan(ip);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getListPostsByIP(String ip, String strOrderBy) throws Exception {
    return storage.getListPostsByIP(ip, strOrderBy);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getForumBanList(String forumId) throws Exception {
    return storage.getForumBanList(forumId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean addBanIPForum(String ip, String forumId) throws Exception {
    return storage.addBanIPForum(ip, forumId);
  }

  /**
   * {@inheritDoc}
   */
  public void removeBanIPForum(String ip, String forumId) throws Exception {
    storage.removeBanIPForum(ip, forumId);
  }

  /**
   * {@inheritDoc}
   */
  public void registerListenerForCategory(String categoryId) throws Exception {
    storage.registerListenerForCategory(categoryId);
  }

  /**
   * {@inheritDoc}
   */
  public void unRegisterListenerForCategory(String path) throws Exception {
    storage.unRegisterListenerForCategory(path);
  }

  /**
   * {@inheritDoc}
   */
  public ForumAttachment getUserAvatar(String userName) throws Exception {
    return storage.getUserAvatar(userName);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserAvatar(String userId, ForumAttachment fileAttachment) throws Exception {
    storage.saveUserAvatar(userId, fileAttachment);
  }

  /**
   * {@inheritDoc}
   */
  public void setDefaultAvatar(String userName) {
    storage.setDefaultAvatar(userName);
  }

  /**
   * {@inheritDoc}
   */
  public List<Watch> getWatchByUser(String userId) throws Exception {
    return storage.getWatchByUser(userId);
  }

  /**
   * {@inheritDoc}
   */
  public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId) throws Exception {
    storage.updateEmailWatch(listNodeId, newEmailAdd, userId);
  }

  /**
   * {@inheritDoc}
   */
  public List<PruneSetting> getAllPruneSetting() throws Exception {
    return storage.getAllPruneSetting();
  }

  /**
   * {@inheritDoc}
   */
  public void savePruneSetting(PruneSetting pruneSetting) throws Exception {
    storage.savePruneSetting(pruneSetting);
  }

  /**
   * {@inheritDoc}
   */
  public PruneSetting getPruneSetting(String forumPath) throws Exception {
    return storage.getPruneSetting(forumPath);
  }

  /**
   * {@inheritDoc}
   */
  public void runPrune(PruneSetting pSetting) throws Exception {
    storage.runPrune(pSetting);
  }

  /**
   * {@inheritDoc}
   */
  public void runPrune(String forumPath) throws Exception {
    storage.runPrune(forumPath);
  }

  /**
   * {@inheritDoc}
   */
  public long checkPrune(PruneSetting pSetting) throws Exception {
    return storage.checkPrune(pSetting);
  }

  /**
   * {@inheritDoc}
   */
  public JCRPageList getPageTopicByType(String type) throws Exception {
    return storage.getPageTopicByType(type);
  }

  /**
   * {@inheritDoc}
   */
  public TopicType getTopicType(String Id) throws Exception {
    return storage.getTopicType(Id);
  }

  /**
   * {@inheritDoc}
   */
  public List<TopicType> getTopicTypes(){
    return storage.getTopicTypes();
  }

  /**
   * {@inheritDoc}
   */
  public void removeTopicType(String topicTypeId) throws Exception {
    storage.removeTopicType(topicTypeId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveTopicType(TopicType topicType) throws Exception {
    storage.saveTopicType(topicType);
  }

  /**
   * {@inheritDoc}
   */
  public void updateUserProfileInfo(String name) throws Exception {
    storage.updateUserProfileInfo(name);
  }

  public DataStorage getStorage() {
    return storage;
  }

  public void setStorage(DataStorage storage) {
    this.storage = storage;
  }

  public ForumServiceManaged getManagementView() {
    return managementView;
  }

  public void setManagementView(ForumServiceManaged managementView) {
    this.managementView = managementView;
  }

  public ForumStatisticsService getForumStatisticsService() {
    return forumStatisticsService;
  }

  public void setForumStatisticsService(ForumStatisticsService forumStatisticsService) {
    this.forumStatisticsService = forumStatisticsService;
  }

  public JobSchedulerService getJobSchedulerService() {
    return jobSchedulerService;
  }

  public void setJobSchedulerService(JobSchedulerService jobSchedulerService) {
    this.jobSchedulerService = jobSchedulerService;
  }

  public InputStream createForumRss(String objectId, String link) throws Exception {
    return storage.createForumRss(objectId, link);
  }

  public InputStream createUserRss(String userId, String link) throws Exception {
    return storage.createUserRss(userId, link);
  }

  public void addListenerPlugin(ForumEventListener listener) throws Exception {
    listeners_.add(listener);
  }
  
  public void removeCacheUserProfile(String userName) throws Exception {
    UserProfile userProfile = CacheUserProfile.getFromCache(userName);
    if (userProfile != null && UserProfile.USER_DELETED != userProfile.getUserRole()) {
      CacheUserProfile.removeInCache(userName);
    }
  }
}
