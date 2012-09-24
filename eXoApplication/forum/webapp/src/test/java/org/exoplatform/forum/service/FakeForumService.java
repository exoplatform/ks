/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.forum.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.NodeIterator;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.forum.service.impl.model.PostFilter;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.services.organization.User;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class FakeForumService implements ForumService {
 
  List<String> activeBBCode; 
  
  public boolean addBanIP(String ip) throws Exception {

    return false;
  }

  public boolean addBanIPForum(String ip, String forumId) throws Exception {

    return false;
  }

  public void addInitBBCodePlugin(ComponentPlugin plugin) throws Exception {
  }

  public void addInitRssPlugin(ComponentPlugin plugin) throws Exception {
  }

  public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception {
  }

  public void addInitialDefaultDataPlugin(ComponentPlugin plugin) throws Exception {
  }

  public void addMember(User user, UserProfile profileTemplate) throws Exception {
  }

  public void addPlugin(ComponentPlugin plugin) throws Exception {
  }

  public void addRolePlugin(ComponentPlugin plugin) throws Exception {
  }

  public void addTag(List<Tag> tags, String userName, String topicPath) throws Exception {
  }

  public void addWatch(int watchType, String path, List<String> values, String currentUser) throws Exception {
  }

  public void calculateModerator(String categoryPath, boolean isNew) throws Exception {
  }

  public long checkPrune(PruneSetting pSetting) throws Exception {

    return 0;
  }

  public void createUserProfile(User user) throws Exception {
  }

  public void evaluateActiveUsers(String query) {
  }

  public Object exportXML(String categoryId,
                          String forumId,
                          List<String> objectIds,
                          String nodePath,
                          ByteArrayOutputStream bos,
                          boolean isExportAll) throws Exception {

    return null;
  }

  public List<String> getActiveBBCode() throws Exception {
    return activeBBCode;
  }
  
  public void addActiveBBCodes(String... activesBBCodes) {
    if (activeBBCode == null) {
      activeBBCode = new ArrayList<String>(Arrays.asList(activesBBCodes));
    }
    else {
      this.activeBBCode.addAll(Arrays.asList(activesBBCodes));
    }
    
  }
  
  public BBCode getBBcode(String id) throws Exception {
    return bbcodes.get(id);
  }
  
  public void setBBCode(String id, BBCode bbcode) {
    if (bbcodes == null) {
      this.bbcodes = new HashMap<String, BBCode>();
    }
    this.bbcodes.put(id, bbcode);
  }

  Map<String,BBCode> bbcodes;

  public List<ForumSearch> getAdvancedSearch(ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds) {

    return null;
  }

  public List<BBCode> getAllBBCode() throws Exception {

    return null;
  }

  public List<ForumLinkData> getAllLink(String strQueryCate, String strQueryForum) throws Exception {

    return null;
  }

  public List<PruneSetting> getAllPruneSetting() throws Exception {

    return null;
  }

  public List<String> getAllTagName(String strQuery, String userAndTopicId) throws Exception {

    return null;
  }

  public List<Tag> getAllTags() throws Exception {

    return null;
  }

  public List<Topic> getAllTopicsOld(long date, String forumPatch) throws Exception {

    return null;
  }

  public long getAvailablePost(String categoryId,
                               String forumId,
                               String topicId,
                               String isApproved,
                               String isHidden,
                               String userLogin) throws Exception {

    return 0;
  }

  public List<String> getBanList() throws Exception {

    return null;
  }

  public List<String> getBookmarks(String userName) throws Exception {

    return null;
  }

  public List<Category> getCategories() {

    return null;
  }

  public Category getCategory(String categoryId) throws Exception {

    return null;
  }

  public UserProfile getDefaultUserProfile(String userName, String ip) throws Exception {

    return null;
  }

  public Forum getForum(String categoryId, String forumId) {

    return null;
  }

  public ForumAdministration getForumAdministration() throws Exception {

    return null;
  }

  public List<String> getForumBanList(String forumId) throws Exception {

    return null;
  }

  public String getForumHomePath() throws Exception {

    return null;
  }

  public ForumStatistic getForumStatistic() throws Exception {

    return null;
  }

  public ForumSubscription getForumSubscription(String userId) {

    return null;
  }

  public List<Forum> getForumSummaries(String categoryId, String strQuery) throws Exception {

    return null;
  }

  public List<Forum> getForums(String categoryId, String strQuery) throws Exception {

    return null;
  }

  public List<ForumSearch> getJobWattingForModerator(String[] paths) {

    return null;
  }

  public int getJobWattingForModeratorByUser(String userId) throws Exception {

    return 0;
  }

  public String getLastLogin() throws Exception {

    return null;
  }

  public long getLastReadIndex(String path, String isApproved, String isHidden, String userLogin) throws Exception {

    return 0;
  }

  public JCRPageList getListPostsByIP(String ip, String strOrderBy) throws Exception {

    return null;
  }

  public SendMessageInfo getMessageInfo(String name) throws Exception {

    return null;
  }

  public List<Tag> getMyTagInTopic(String[] tagIds) throws Exception {

    return null;
  }

  public List<Post> getNewPosts(int number) throws Exception {

    return null;
  }

  public long getNewPrivateMessage(String userName) throws Exception {

    return 0;
  }

  public Object getObjectNameById(String id, String type) throws Exception {

    return null;
  }

  public Object getObjectNameByPath(String path) throws Exception {

    return null;
  }

  public List<String> getOnlineUsers() throws Exception {

    return null;
  }

  public JCRPageList getPageListUserProfile() throws Exception {

    return null;
  }

  public JCRPageList getPagePostByUser(String userName,
                                       String userId,
                                       boolean isMod,
                                       String strOrderBy) throws Exception {

    return null;
  }

  public JCRPageList getPageTopic(String categoryId,
                                  String forumId,
                                  String strQuery,
                                  String strOrderBy) throws Exception {

    return null;
  }

  public JCRPageList getPageTopicByType(String type) throws Exception {

    return null;
  }

  public JCRPageList getPageTopicByUser(String userName, boolean isMod, String strOrderBy) throws Exception {

    return null;
  }

  public JCRPageList getPageTopicOld(long date, String forumPatch) throws Exception {

    return null;
  }

  public String[] getPermissionTopicByCategory(String categoryId, String type) throws Exception {

    return null;
  }

  public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {

    return null;
  }

  public JCRPageList getPostForSplitTopic(String topicPath) throws Exception {

    return null;
  }

  public JCRPageList getPosts(String categoryId,
                              String forumId,
                              String topicId,
                              String isApproved,
                              String isHidden,
                              String strQuery,
                              String userLogin) throws Exception {

    return null;
  }

  public JCRPageList getPrivateMessage(String userName, String type) throws Exception {

    return null;
  }

  public PruneSetting getPruneSetting(String forumPath) throws Exception {

    return null;
  }

  public UserProfile getQuickProfile(String userName) throws Exception {

    return null;
  }

  public List<UserProfile> getQuickProfiles(List<String> userList) throws Exception {

    return null;
  }

  public List<ForumSearch> getQuickSearch(String textQuery,
                                          String type,
                                          String pathQuery,
                                          String userId,
                                          List<String> listCateIds,
                                          List<String> listForumIds,
                                          List<String> forumIdsOfModerator) throws Exception {

    return null;
  }

  public String getScreenName(String userName) throws Exception {

    return null;
  }

  public Tag getTag(String tagId) throws Exception {

    return null;
  }

  public List<String> getTagNameInTopic(String userAndTopicId) throws Exception {

    return null;
  }

  public Topic getTopic(String categoryId, String forumId, String topicId, String userRead) throws Exception {

    return null;
  }

  public JCRPageList getTopicByMyTag(String userIdAndtagId, String strOrderBy) throws Exception {

    return null;
  }

  public Topic getTopicByPath(String topicPath, boolean isLastPost) throws Exception {

    return null;
  }

  public LazyPageList<Topic> getTopicList(String categoryId,
                                          String forumId,
                                          String string,
                                          String strOrderBy,
                                          int pageSize) throws Exception {

    return null;
  }

  public Topic getTopicSummary(String topicPath) throws Exception {

    return null;
  }

  public TopicType getTopicType(String Id) throws Exception {

    return null;
  }

  public List<TopicType> getTopicTypes() {

    return null;
  }

  public List<Topic> getTopics(String categoryId, String forumId) throws Exception {

    return null;
  }

  public long getTotalTopicOld(long date, String forumPatch) {

    return 0;
  }

  public ForumAttachment getUserAvatar(String userName) throws Exception {

    return null;
  }

  public UserProfile getUserInfo(String userName) throws Exception {

    return null;
  }

  public UserProfile getUserInformations(UserProfile userProfile) throws Exception {

    return null;
  }

  public List<String> getUserModerator(String userName, boolean isModeCate) throws Exception {

    return null;
  }

  public UserProfile getUserProfileManagement(String userName) throws Exception {

    return null;
  }

  public UserProfile getUserSettingProfile(String userName) throws Exception {

    return null;
  }

  public List<Watch> getWatchByUser(String userId) throws Exception {

    return null;
  }

  public void importXML(String nodePath, ByteArrayInputStream bis, int typeImport) throws Exception {
  }

  public boolean isAdminRole(String userName) throws Exception {

    return false;
  }

  public boolean isOnline(String userId) throws Exception {

    return false;
  }

  public void mergeTopic(String srcTopicPath, String destTopicPath, String mailContent, String link) throws Exception {
  }

  public void modifyForum(Forum forum, int type) throws Exception {
  }

  public void modifyPost(List<Post> posts, int type) {
  }

  public void modifyTopic(List<Topic> topics, int type) {
  }

  public void moveForum(List<Forum> forums, String destCategoryPath) throws Exception {
  }

  public void movePost(String[] postPaths,
                       String destTopicPath,
                       boolean isCreatNewTopic,
                       String mailContent,
                       String link) throws Exception {
  }

  public void moveTopic(List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception {
  }

  public void registerListenerForCategory(String categoryId) throws Exception {
  }

  public void removeBBCode(String bbcodeId) throws Exception {
  }

  public void removeBan(String ip) throws Exception {
  }

  public void removeBanIPForum(String ip, String forumId) throws Exception {
  }

  public Category removeCategory(String categoryId) throws Exception {

    return null;
  }

  public Forum removeForum(String categoryId, String forumId) throws Exception {

    return null;
  }

  public void removeMember(User user) throws Exception {
  }

  public Post removePost(String categoryId, String forumId, String topicId, String postId) {

    return null;
  }

  public void removePrivateMessage(String messageId, String userName, String type) throws Exception {
  }

  public Topic removeTopic(String categoryId, String forumId, String topicId) {

    return null;
  }

  public void removeTopicType(String topicTypeId) throws Exception {
  }

  public void removeWatch(int watchType, String path, String values) throws Exception {
  }

  public void runPrune(PruneSetting pSetting) throws Exception {
  }

  public void runPrune(String forumPath) throws Exception {
  }

  public void saveBBCode(List<BBCode> bbcodes) throws Exception {
  }

  public void saveCategory(Category category, boolean isNew) throws Exception {
  }

  public void saveCollapsedCategories(String userName, String categoryId, boolean isAdd) throws Exception {
  }

  public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception {
  }

  public void saveForumAdministration(ForumAdministration forumAdministration) throws Exception {
  }

  public void saveForumStatistic(ForumStatistic forumStatistic) throws Exception {
  }

  public void saveForumSubscription(ForumSubscription forumSubscription, String userId) throws Exception {
  }

  public void saveLastPostIdRead(String userId,
                                 String[] lastReadPostOfForum,
                                 String[] lastReadPostOfTopic) throws Exception {
  }

  public void saveModOfCategory(List<String> moderatorCate, String userId, boolean isAdd) {
  }

  public void saveModerateOfForums(List<String> forumPaths, String userName, boolean isDelete) throws Exception {
  }

  public void savePost(String categoryId,
                       String forumId,
                       String topicId,
                       Post post,
                       boolean isNew,
                       String defaultEmailContent) throws Exception {
  }

  public void savePrivateMessage(ForumPrivateMessage privateMessage) throws Exception {
  }

  public void savePruneSetting(PruneSetting pruneSetting) throws Exception {
  }

  public void saveReadMessage(String messageId, String userName, String type) throws Exception {
  }

  public void saveTag(Tag newTag) throws Exception {
  }

  public void saveTopic(String categoryId,
                        String forumId,
                        Topic topic,
                        boolean isNew,
                        boolean isMove,
                        String defaultEmailContent) throws Exception {
  }

  public void saveTopicType(TopicType topicType) throws Exception {
  }

  public void saveUserAvatar(String userId, ForumAttachment fileAttachment) throws Exception {
  }

  public void saveUserBookmark(String userName, String bookMark, boolean isNew) throws Exception {
  }

  public void saveUserModerator(String userName, List<String> ids, boolean isModeCate) throws Exception {
  }

  public void saveUserProfile(UserProfile userProfile, boolean isOption, boolean isBan) throws Exception {
  }

  public void saveUserSettingProfile(UserProfile userProfile) throws Exception {
  }

  public JCRPageList searchUserProfile(String userSearch) throws Exception {

    return null;
  }

  public void setDefaultAvatar(String userName) {
  }

  public void setViewCountTopic(String path, String userRead) {
  }

  public void unRegisterListenerForCategory(String path) throws Exception {
  }

  public void unTag(String tagId, String userName, String topicPath) {
  }

  public void updateDataImported() throws Exception {
  }

  public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId) throws Exception {
  }

  public void updateForum(String path) throws Exception {
  }

  public void updateForumAccess(String userId, String forumId) {
  }

  public void updateStatisticCounts(long topicCoutn, long postCount) throws Exception {
  }

  public void updateTopicAccess(String userId, String topicId) {
  }

  public void updateUserProfile(User user) throws Exception {
  }

  public void updateUserProfileInfo(String name) throws Exception {
  }

  public UserProfile updateUserProfileSetting(UserProfile userProfile) throws Exception {

    return null;
  }

  public void userLogin(String userId) throws Exception {
  }

  public void userLogout(String userId) throws Exception {
  }

  public NodeIterator search(String queryString) throws Exception {
    
    return null;
  }

  public void updateLoggedinUsers() throws Exception {
    
  }

  public Iterator<SendMessageInfo> getPendingMessages() throws Exception {
    return null;
  }

  public InputStream createForumRss(String objectId, String link) throws Exception {
    return null;
  }

  public InputStream createUserRss(String userId, String link) throws Exception {
    return null;
  }

  @Override
  public void calculateDeletedUser(String userName) throws Exception {

    
  }

  @Override
  public void addListenerPlugin(ForumEventListener listener) throws Exception {

    
  }

  @Override
  public Topic getTopicUpdate(Topic topic, boolean isSummary) throws Exception {

    return null;
  }

  @Override
  public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew, MessageBuilder messageBuilder) throws Exception {

    
  }

  @Override
  public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, MessageBuilder messageBuilder) throws Exception {

    
  }

  @Override
  public List<Post> getRecentPostsForUser(String userName, int number) throws Exception {
    return null;
  }

  @Override
  public void removeCacheUserProfile(String userName) throws Exception {
    
  }

  @Override
  public void calculateDeletedGroup(String groupId, String groupName) throws Exception {
    
  }

  @Override
  public ListAccess<Post> getPosts(PostFilter filter) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
