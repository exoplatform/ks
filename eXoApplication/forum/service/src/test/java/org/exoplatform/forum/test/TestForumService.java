/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;

import org.apache.commons.io.FileUtils;
import org.exoplatform.forum.service.CacheUserProfile;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumNodeTypes;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class TestForumService extends ForumServiceTestCase {
  private static final String USER_ROOT = "root";

  private static final String USER_DEMO = "demo";

  private static final String USER_JOHN = "john";

  private Collection<MembershipEntry> membershipEntries = new ArrayList<MembershipEntry>();
  
  public TestForumService() throws Exception {
    super();
  }

  private ForumService forumService_;

  private KSDataLocation dataLocation;

  private String       categoryId;

  private String       forumId;

  private String       topicId;

  public void setUp() throws Exception {
    super.setUp();
    forumService_ = (ForumService) container.getComponentInstanceOfType(ForumService.class);
    dataLocation = (KSDataLocation) container.getComponentInstanceOfType(KSDataLocation.class);
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    sProvider = sessionProviderService.getSystemSessionProvider(null);

  }

  public void testForumService() throws Exception {
    ForumStatistic forumStatistic = new ForumStatistic();
    forumService_.saveForumStatistic(forumStatistic);
    assertNotNull(forumService_);
  }

  public void testUserProfile() throws Exception {
    String userName = "tu.duy";
    UserProfile userProfile = createdUserProfile(userName);

    // save UserProfile
    forumService_.saveUserProfile(userProfile, true, true);

    // getUserInfo
    userProfile = forumService_.getUserInfo(userName);
    assertNotNull("Get info UserProfile is null", userProfile);

    // get Default and storage this profile in ExoCache
    userProfile = forumService_.getDefaultUserProfile(userName, "");
    assertNotNull("Get default UserProfile is null", userProfile);
    
    // test cache user profile, get this profile is not null
    assertNotNull("Get default UserProfile is null", CacheUserProfile.getFromCache(userName));
    // remove this profile in ExoCache by update this profile
    forumService_.saveUserSettingProfile(userProfile);
    // get by ExoCache is null 
    assertNull("Get default UserProfile is null", CacheUserProfile.getFromCache(userName));
    
    // getUserInformations
    userProfile = forumService_.getUserInformations(userProfile);
    assertNotNull("Get informations UserProfile is null", userProfile);

    // getUserSettingProfile
    userProfile = forumService_.getUserSettingProfile(userName);
    assertNotNull("Get Setting UserProfile is not null", userProfile);

    // saveUserSettingProfile
    assertEquals("Default AutoWatchMyTopics is false", userProfile.getIsAutoWatchMyTopics(), false);
    userProfile.setIsAutoWatchMyTopics(true);
    forumService_.saveUserSettingProfile(userProfile);
    userProfile = forumService_.getUserSettingProfile(userName);
    assertEquals("Edit AutoWatchMyTopics and can't save this property. AutoWatchMyTopics is false", userProfile.getIsAutoWatchMyTopics(), true);
    //
  }

  public void testUserLogin() throws Exception {
    String[] userIds = new String[] { USER_ROOT, USER_JOHN, USER_DEMO };
    for (int i = 0; i < userIds.length; i++) {
      try {
        forumService_.getQuickProfile(userIds[i]);
      } catch (Exception e) {
        forumService_.saveUserProfile(createdUserProfile(userIds[i]), true, true);
      }
    }
    // Add user login
    forumService_.userLogin(USER_ROOT);
    forumService_.userLogin(USER_JOHN);
    forumService_.userLogin(USER_DEMO);

    // Get all user online:
    assertEquals("Get all user online", 3, forumService_.getOnlineUsers().size());

    // isOnline
    assertEquals("John is not Online", forumService_.isOnline(USER_JOHN), true);
    // get Last Login
    assertEquals("Demo can't last Login", forumService_.getLastLogin(), USER_DEMO);
    // userLogout
    forumService_.userLogout(USER_DEMO);
    assertEquals("Demo is online", forumService_.isOnline(USER_DEMO), false);
  }

  public void testForumAdministration() throws Exception {
    ForumAdministration administration = createForumAdministration();
    forumService_.saveForumAdministration(administration);
    administration = forumService_.getForumAdministration();
    assertNotNull(administration);
    assertEquals(administration.getForumSortBy(), "forumName");
  }
  
  private void setData() throws Exception {
    killData();
    Category cat = createCategory(getId(Utils.CATEGORY));
    this.categoryId = cat.getId();
    forumService_.saveCategory(cat, true);
    Forum forum = createdForum();
    this.forumId = forum.getId();
    forumService_.saveForum(categoryId, forum, true);
    Topic topic = createdTopic("root");
    forumService_.saveTopic(categoryId, forumId, topic, true, false, new MessageBuilder());
    this.topicId = topic.getId();
  }

  private void killData() throws Exception {
    List<Category> cats = forumService_.getCategories();
    if (cats.size() > 0) {
      for (Category category : cats) {
        forumService_.removeCategory(category.getId());
      }
    }
  }
  
  public void testCategory() throws Exception {
    String[] catIds = new String[] { getId(Utils.CATEGORY), getId(Utils.CATEGORY), getId(Utils.CATEGORY) };

    // test case failed by KS-4422
    String catId = getId(Utils.CATEGORY);
    Category cat_ = createCategory(catId);
    // check category existing
    assertNull(String.format("The category has ID is %s existed.", catId), forumService_.getCategory(catId));
    // save new category
    forumService_.saveCategory(cat_, true);
    // check again category existing
    assertNotNull(String.format("The category has ID is %s not existing.", catId), forumService_.getCategory(catId));
    
    // test case failed by KS-4427
    // get category new created it same category cat_
    Category catTest = forumService_.getCategory(catId);
    // get userProfiles by jcr node:
    Node nodeCat = root_.getNode(dataLocation.getForumCategoriesLocation().concat("/").concat(catId));
    int privateLength = new PropertyReader(nodeCat).strings(Utils.EXO_USER_PRIVATE).length;
    assertEquals("Two objects userProfiles not same.", privateLength, catTest.getUserPrivate().length);

    // test save/update moderators of category
    List<String> categoriesId = new ArrayList<String>();
    categoriesId.add(catId);
    forumService_.saveModOfCategory(categoriesId, USER_DEMO, true);
    catTest = forumService_.getCategory(catId);
    assertEquals("The moderators of category not contanins user demo.", catTest.getModerators()[0], USER_DEMO);
    
    // add category
    forumService_.saveCategory(createCategory(catIds[0]), true);
    forumService_.saveCategory(createCategory(catIds[1]), true);
    forumService_.saveCategory(createCategory(catIds[2]), true);
    Category category = forumService_.getCategory(catIds[0]);
    assertNotNull("Category is null", category);
    // get categories
    List<Category> categories = forumService_.getCategories();
    assertEquals(categories.size(), 4);
    // update category
    category.setCategoryName("ReName Category");
    forumService_.saveCategory(category, false);
    Category updatedCat = forumService_.getCategory(catIds[0]);
    assertEquals("Category name is not change", "ReName Category", updatedCat.getCategoryName());

    // test removeCategory
    for (int i = 0; i < 3; ++i) {
      forumService_.removeCategory(catIds[i]);
    }
    forumService_.removeCategory(catId);
    categories = forumService_.getCategories();
    assertEquals("Size categories can not equals 0", categories.size(), 0);
  }

  public void testForum() throws Exception {
    String catId = getId(Utils.CATEGORY);
    Category cat = createCategory(catId);
    // create new category
    forumService_.saveCategory(cat, true);

    // create new forum
    Forum forum = createdForum();
    String forumId = forum.getId();

    // add forum
    forumService_.saveForum(catId, forum, true);

    // getForum
    forum = forumService_.getForum(catId, forumId);
    assertNotNull("Forum is null", forum);

    // getList Forum
    // Created 5 new forum, we have total 6 forum.
    List<Forum> forums = new ArrayList<Forum>();
    for (int i = 0; i < 5; i++) {
      forumService_.saveForum(cat.getId(), createdForum(), true);
    }
    forums.addAll(forumService_.getForums(catId, ""));

    // check size of list forum
    assertEquals("List forums size not equals 6", forums.size(), 6);

    // update Forum
    forum.setForumName("Forum update");
    forumService_.saveForum(catId, forum, false);
    assertEquals(forum.getForumName(), forumService_.getForum(catId, forumId).getForumName());

    // modifyForum
    forum.setIsLock(true);
    forumService_.modifyForum(forum, 2);
    forum = forumService_.getForum(catId, forumId);
    assertEquals(forum.getIsLock(), true);

    // saveModerateOfForum
    List<String> list = new ArrayList<String>();
    list.add(catId + "/" + forum.getId());
    forumService_.saveModerateOfForums(list, "demo", false);
    forum = forumService_.getForum(catId, forumId);
    list.clear();
    list.addAll(Arrays.asList(forum.getModerators()));
    assertEquals(list.contains("demo"), true);

    // test moderator of category.
    list.clear();
    list.add(catId);
    forumService_.saveModOfCategory(list, USER_JOHN, true);
    forum = forumService_.getForum(catId, forumId);
    list.clear();
    list.addAll(Arrays.asList(forum.getModerators()));
    assertEquals("Forum in category can not content moderatort user admin", list.contains(USER_JOHN), true);

    // test moveForum, Move list Forum from Category 'cat' to Category 'cate'

    // create new Category
    Category cate = createCategory(getId(Utils.CATEGORY));
    forumService_.saveCategory(cate, true);
    Category cateNew = forumService_.getCategory(cate.getId());

    // move forum
    forumService_.moveForum(forums, cateNew.getPath());

    // get forum in new category
    forum = forumService_.getForum(cate.getId(), forumId);
    assertNotNull(forum);

    // remove Forum and return this Forum
    for (Forum forum2 : forums) {
      forumService_.removeForum(cate.getId(), forum2.getId());
    }

    // check remove
    forums = forumService_.getForumSummaries(catId, "");
    assertEquals("List forums can not equals 0", forums.size(), 0);
  }

  public void testTopic() throws Exception {
    Category cat = createCategory(getId(Utils.CATEGORY));
    forumService_.saveCategory(cat, true);
    Forum forum = createdForum();
    forumService_.saveForum(cat.getId(), forum, true);
    forum = forumService_.getForum(cat.getId(), forum.getId());
    List<String> listTopicId = new ArrayList<String>();
    // add 10 Topics
    List<Topic> list = new ArrayList<Topic>();
    Topic topic;
    for (int i = 0; i < 9; i++) {
      topic = createdTopic(USER_ROOT);
      topic.setCanView(new String[]{USER_ROOT, "*:/foo/zed"});
      list.add(topic);
      listTopicId.add(topic.getId());
      forumService_.saveTopic(cat.getId(), forum.getId(), topic, true, false, new MessageBuilder());
    }
    topic = createdTopic(USER_JOHN);
    listTopicId.add(topic.getId());
    forumService_.saveTopic(cat.getId(), forum.getId(), topic, true, false, new MessageBuilder());
    assertEquals(10, forumService_.getForum(cat.getId(), forum.getId()).getTopicCount());

    // get Topic - topic in position 8
    topic = list.get(8);
    Topic topica = forumService_.getTopic(cat.getId(), forum.getId(), topic.getId(), "");
    assertNotNull(topica);

    // get Topic by path
    topica = forumService_.getTopicByPath(cat.getId() + "/" + forum.getId() + "/" + topic.getId(), false);
    assertNotNull(topica);

    // update Topic
    topica.setIsSticky(true);
    topica.setTopicName("topic 8");
    forumService_.saveTopic(cat.getId(), forum.getId(), topica, false, false, new MessageBuilder());
    assertEquals("This topic name not is 'topic 8'", "topic 8", forumService_.getTopic(cat.getId(), forum.getId(), topic.getId(), "").getTopicName());

    // modifyTopic
    topica.setIsLock(true);
    list.clear();
    list.add(topica);
    forumService_.modifyTopic(list, 2);
    topica = forumService_.getTopic(cat.getId(), forum.getId(), topic.getId(), "");
    assertEquals("This topic is open.", topica.getIsLock(), true);
    // get PageList Topic
    JCRPageList pagelist = forumService_.getPageTopic(cat.getId(), forum.getId(), "", "");
    assertEquals("Available all topics not equals 10.", pagelist.getAvailable(), 10);
    pagelist.setPageSize(5);
    List<Topic> listTopic = pagelist.getPage(1);
    assertEquals("Available page not equals 5", listTopic.size(), 5);
    assertEquals(pagelist.getAvailablePage(), 2);
    
    // get Page topic has check permission.
    setMembershipEntry("/foo/zed", "member", true);
    loginUser(USER_DEMO);
    
    StringBuilder query = new StringBuilder();
    String strQuery = query.append("(").append(Utils.buildXpathHasProperty(Utils.EXO_CAN_VIEW)).append(" or ").toString();
    String buildQueryViewer = Utils.buildXpathByUserInfo(Utils.EXO_CAN_VIEW, UserHelper.getAllGroupAndMembershipOfUser(null));
    buildQueryViewer += " or @" + Utils.EXO_OWNER + "='" + USER_DEMO + "')";
    pagelist = forumService_.getPageTopic(cat.getId(), forum.getId(), strQuery + buildQueryViewer, "");
    assertEquals(10, pagelist.getAvailable());
    
    setMembershipEntry("/foo/bar", "member", true);
    loginUser(USER_JOHN);

    buildQueryViewer = Utils.buildXpathByUserInfo(Utils.EXO_CAN_VIEW, UserHelper.getAllGroupAndMembershipOfUser(null));
    buildQueryViewer += " or @" + Utils.EXO_OWNER + "='" + USER_JOHN + "')";
    pagelist = forumService_.getPageTopic(cat.getId(), forum.getId(), strQuery + buildQueryViewer, "");
    assertEquals(1, pagelist.getAvailable());
    
    // get Topic By User
    topic = createdTopic(USER_DEMO);
    forumService_.saveTopic(cat.getId(), forum.getId(), topic, true, false, new MessageBuilder());
    // We have 11 topic: 9 by root, 1 by john and 1 by demo 
    pagelist = forumService_.getPageTopicByUser(USER_ROOT, true, "");
    assertEquals(9, pagelist.getAvailable());

    // set 5 topics have last post is 2 days.
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(cal.getTimeInMillis() - 2 * 86400000);
    Node topicNode;
    for (Topic topic2 : listTopic) {
      topicNode = root_.getNode(topic2.getPath().replaceFirst("/", ""));
      topicNode.setProperty(ForumNodeTypes.EXO_LAST_POST_DATE, cal);
      topicNode.save();
    }
    // get topics by last post days. Example with 1 day.
    listTopic = forumService_.getAllTopicsOld(1, forum.getPath());
    assertEquals("Failed to run auto prune. List topic has size not equals 5.", listTopic.size(), 5);
    
    // run autoPrune
    PruneSetting pSetting = forumService_.getPruneSetting(forum.getPath());
    // active the pruning this forum.
    pSetting.setActive(true);
    // prune for topics have last post more than 1 day.
    pSetting.setInActiveDay(1);
    forumService_.runPrune(pSetting);
    // check topics pruned.
    // After pruned, the topics is active is 11 - 5 = 6.
    StringBuilder queryBuilder = new StringBuilder();
    // @exo:isActive = 'true'
    queryBuilder.append("@").append(ForumNodeTypes.EXO_IS_ACTIVE).append("='true'");
    int size = forumService_.getTopicList(cat.getId(), forum.getId(), queryBuilder.toString(), "", 20).getAll().size();
    assertEquals("Failed to run autoPrun topics, the size of topics active not equals 6.", size, 6);
    
    // move Topic
    // move topic from forum to forum 1
    Forum forum1 = createdForum();
    forumService_.saveForum(cat.getId(), forum1, true);
    forum1 = forumService_.getForum(cat.getId(), forum1.getId());
    List<Topic> topics = new ArrayList<Topic>();
    topics.add(topica);
    forumService_.moveTopic(topics, forum1.getPath(), "", "");
    assertNotNull("Failed to moved topic, topic is null.", forumService_.getTopic(cat.getId(), forum1.getId(), topica.getId(), ""));
    assertEquals(10, forumService_.getForum(cat.getId(), forum.getId()).getTopicCount());
    assertEquals(1, forumService_.getForum(cat.getId(), forum1.getId()).getTopicCount());

    // test remove Topic return Topic
    // remove id topic moved in list topicIds.
    if (listTopicId.contains(topica.getId()))
      listTopicId.remove(topica.getId());
    for (String topicId : listTopicId) {
      forumService_.removeTopic(cat.getId(), forum.getId(), topicId);
    }
    List<Topic> topics2 = forumService_.getTopics(cat.getId(), forum.getId());
    assertEquals("Topics in forum failed to remove. List topic has size more than 1.", topics2.size(), 1);
    assertEquals(1, forumService_.getForum(cat.getId(), forum.getId()).getTopicCount());
  }

  public void testTopicType() throws Exception {
    // set Data
    setData();
    TopicType topicType = createTopicType("Musics");
    forumService_.saveTopicType(topicType);
    forumService_.saveTopicType(createTopicType("Dance"));
    forumService_.saveTopicType(createTopicType("Sing"));
    topicType = forumService_.getTopicType(topicType.getId());
    assertNotSame("Can not save and get Topic type.", topicType.getId(), TopicType.DEFAULT_ID);
    // Check get All
    List<TopicType> listTopicType = forumService_.getTopicTypes();
    assertEquals("Can not get all topic type. Size of topicTypes list is not 3.", listTopicType.size(), 3);
  }

  public void testPost() throws Exception {
    // set Data
    setData();

    List<Post> posts = new ArrayList<Post>();
    for (int i = 0; i < 25; ++i) {
      Post post = createdPost();
      posts.add(post);
      forumService_.savePost(categoryId, forumId, topicId, post, true, new MessageBuilder());
    }
    // getPost
    assertNotNull(forumService_.getPost(categoryId, forumId, topicId, posts.get(0).getId()));
    assertEquals(25, forumService_.getTopic(categoryId, forumId, topicId, "").getPostCount());

    // get ListPost
    JCRPageList pagePosts = forumService_.getPosts(categoryId, forumId, topicId, "", "", "", "root");
    assertEquals(pagePosts.getAvailable(), posts.size() + 1);// size = 26 (first post and new postList)
    List page1 = pagePosts.getPage(1);
    assertEquals(page1.size(), 10);
    List page3 = pagePosts.getPage(3);
    assertEquals(page3.size(), 6);
    // getPost by Ip
    JCRPageList pageIpPosts = forumService_.getListPostsByIP("192.168.1.11", null);
    assertEquals(pageIpPosts.getAvailable(), 25);// size = 25 (not content first post)
    // update Post First
    Post newPost = (Post) pagePosts.getPage(1).get(1);
    newPost.setMessage("New message");
    forumService_.savePost(categoryId, forumId, topicId, newPost, false, new MessageBuilder());
    assertEquals("New message", forumService_.getPost(categoryId, forumId, topicId, newPost.getId()).getMessage());

    // test movePost
    Topic topicnew = createdTopic("root");
    forumService_.saveTopic(categoryId, forumId, topicnew, true, false, new MessageBuilder());
    topicnew = forumService_.getTopic(categoryId, forumId, topicnew.getId(), "root");

    forumService_.movePost(new String[] { newPost.getPath() }, topicnew.getPath(), false, "test mail content", "");
    assertNotNull(forumService_.getPost(categoryId, forumId, topicnew.getId(), newPost.getId()));

    // test remove Post return post
    assertNotNull(forumService_.removePost(categoryId, forumId, topicnew.getId(), newPost.getId()));
    assertNull(forumService_.getPost(categoryId, forumId, topicnew.getId(), newPost.getId()));
    assertEquals(24, forumService_.getTopic(categoryId, forumId, topicId, "").getPostCount());

    // getViewPost
  }

  // BookMark
  public void testBookMark() throws Exception {
    // set Data
    setData();

    // add bookmark
    String bookMark = Utils.CATEGORY + "//" + categoryId;
    forumService_.saveUserBookmark("root", bookMark, true);
    bookMark = Utils.FORUM + "//" + categoryId + "/" + forumId;
    forumService_.saveUserBookmark("root", bookMark, true);

    // get bookmark
    List<String> bookMarks = new ArrayList<String>();
    bookMarks.addAll(forumService_.getBookmarks("root"));
    assertEquals(bookMarks.size(), 2);
  }

  // Private Message
  public void testPrivateMessage() throws Exception {
    ForumPrivateMessage privateMessage = new ForumPrivateMessage();
    privateMessage.setFrom("demo");
    privateMessage.setIsUnread(false);
    privateMessage.setName("privateMessage Name");
    privateMessage.setMessage("Content privateMessage");
    privateMessage.setSendTo("root");

    // savePtivateMs
    forumService_.savePrivateMessage(privateMessage);

    // get Private Message is SEND_MESSAGE
    JCRPageList pageList = forumService_.getPrivateMessage("demo", Utils.SEND_MESSAGE);
    assertNotNull(pageList);
    assertEquals(pageList.getAvailable(), 1);
    privateMessage = (ForumPrivateMessage) pageList.getPage(1).get(0);
    String privateMessageId_SEND = privateMessage.getId();

    // get Private Message is RECEIVE_MESSAGE
    pageList = forumService_.getPrivateMessage("root", Utils.RECEIVE_MESSAGE);
    assertNotNull(pageList);
    assertEquals(pageList.getAvailable(), 1);
    privateMessage = (ForumPrivateMessage) pageList.getPage(1).get(0);
    String privateMessageId_RECEIVE = privateMessage.getId();
    //
    long t = forumService_.getNewPrivateMessage("root");
    assertEquals(t, 1);

    // Remove PrivateMessage
    forumService_.removePrivateMessage(privateMessageId_SEND, "demo", Utils.SEND_MESSAGE);
    pageList = forumService_.getPrivateMessage("demo", Utils.SEND_MESSAGE);
    assertEquals(pageList.getAvailable(), 0);
    forumService_.removePrivateMessage(privateMessageId_RECEIVE, "root", Utils.RECEIVE_MESSAGE);
    pageList = forumService_.getPrivateMessage("root", Utils.RECEIVE_MESSAGE);
    assertEquals(pageList.getAvailable(), 0);
    //
  }

  public void testGetObjectNameByPath() throws Exception {
    // set Data
    setData();

    // Test get object by path
    String topicPath = forumService_.getForumHomePath();
    topicPath = categoryId + "/" + forumId + "/" + topicId;
    assertNotNull(forumService_.getObjectNameByPath(topicPath));

    // Test get object by path in case the object has been updated
    // by saveForum
    String forumPath = categoryId + "/" + forumId;
    Forum originalForum = convertToForum(forumService_.getObjectNameByPath(forumPath));
    assertNotNull(originalForum);
    originalForum.setForumName("aaa");
    forumService_.saveForum(categoryId, originalForum, false);

    Forum updatedForum = convertToForum(forumService_.getObjectNameByPath(forumPath));
    assertNotNull(updatedForum);
    assertEquals(originalForum.getForumName(), updatedForum.getForumName());

    // by modifyForum
    originalForum.setIsLock(true);
    forumService_.modifyForum(originalForum, Utils.LOCK);
    updatedForum = convertToForum(forumService_.getObjectNameByPath(forumPath));
    assertNotNull(updatedForum);
    assertTrue(updatedForum.getIsLock());

    // by saveModerateOfForums
    List<String> list = new ArrayList<String>();
    list.add(forumPath);
    forumService_.saveModerateOfForums(list, "demo", false);
    updatedForum = convertToForum(forumService_.getObjectNameByPath(forumPath));
    assertNotNull(updatedForum);
    list.clear();
    list.addAll(Arrays.asList(updatedForum.getModerators()));
    assertTrue(list.contains("demo"));

    // by moveForum
    Category cate = createCategory(getId(Utils.CATEGORY));
    forumService_.saveCategory(cate, true);
    Category cateNew = forumService_.getCategory(cate.getId());
    List<Forum> forums = new ArrayList<Forum>();
    forums.add(originalForum);
    forumService_.moveForum(forums, cateNew.getPath());
    originalForum = convertToForum(forumService_.getObjectNameByPath(forumPath));
    assertNull(originalForum);
    updatedForum = convertToForum(forumService_.getObjectNameByPath(cateNew.getId() + "/" + forumId));
    assertNotNull(updatedForum);

    // by removeForum
    forumService_.removeForum(cateNew.getId(), forumId);
    updatedForum = convertToForum(forumService_.getObjectNameByPath(cateNew.getId() + "/" + forumId));
    assertNull(updatedForum);
  }


  public void testGetObjectNameById() throws Exception {
    // set Data
    setData();

    // Test get object by id
    assertNotNull(forumService_.getObjectNameById(forumId, Utils.FORUM));

    // Test get object by id in case the object has been updated
    // by saveForum
    Forum originalForum = convertToForum(forumService_.getObjectNameById(forumId, Utils.FORUM));
    assertNotNull(originalForum);
    originalForum.setForumName("aaa");
    forumService_.saveForum(categoryId, originalForum, false);

    Forum updatedForum = convertToForum(forumService_.getObjectNameById(forumId, Utils.FORUM));
    assertNotNull(updatedForum);
    assertEquals(originalForum.getForumName(), updatedForum.getForumName());

    // by modifyForum
    originalForum.setIsLock(true);
    forumService_.modifyForum(originalForum, Utils.LOCK);
    updatedForum = convertToForum(forumService_.getObjectNameById(forumId, Utils.FORUM));
    assertNotNull(updatedForum);
    assertTrue(updatedForum.getIsLock());

    // by saveModerateOfForums
    List<String> list = new ArrayList<String>();
    list.add(categoryId + "/" + forumId);
    forumService_.saveModerateOfForums(list, "demo", false);
    updatedForum = convertToForum(forumService_.getObjectNameById(forumId, Utils.FORUM));
    assertNotNull(updatedForum);
    list.clear();
    list.addAll(Arrays.asList(updatedForum.getModerators()));
    assertTrue(list.contains("demo"));

    // by moveForum
    Category cate = createCategory(getId(Utils.CATEGORY));
    forumService_.saveCategory(cate, true);
    Category cateNew = forumService_.getCategory(cate.getId());
    List<Forum> forums = new ArrayList<Forum>();
    forums.add(originalForum);
    forumService_.moveForum(forums, cateNew.getPath());
    updatedForum = convertToForum(forumService_.getObjectNameById(forumId, Utils.FORUM));
    assertNotNull(updatedForum);
    assertEquals(cateNew.getPath() + "/" + forumId, updatedForum.getPath());

    // by removeForum
    forumService_.removeForum(cateNew.getId(), forumId);
    updatedForum = convertToForum(forumService_.getObjectNameById(forumId, Utils.FORUM));
    assertNull(updatedForum);

  }

  public void testImportXML() throws Exception {
    Category cat = createCategory(getId(Utils.CATEGORY));
    forumService_.saveCategory(cat, true);
    cat = forumService_.getCategory(cat.getId());
    String pathNode = cat.getPath();
    assertEquals("Before import data, category don't have any forum", forumService_.getForums(cat.getId(), "").size(), 0);
    try {
      File file = new File(System.getProperty("user.dir") + "/src/test/resources/conf/portal/Data.xml");
      String content = FileUtils.readFileToString(file, "UTF-8");
      byte currentXMLBytes[] = content.getBytes();
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentXMLBytes);
      // Import forum into category
      forumService_.importXML(pathNode, byteArrayInputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
      assertEquals("Can't import forum into category", forumService_.getForums(cat.getId(), "").size(), 1);
    } catch (IOException e) {
      log.debug("Failed to test importXML", e);
    }
  }

  public void testExportXML() throws Exception {
    Category cat = createCategory(getId(Utils.CATEGORY));
    forumService_.saveCategory(cat, true);
    cat = forumService_.getCategory(cat.getId());
    Forum forum = createdForum();
    forumService_.saveForum(cat.getId(), forum, true);
    forum = forumService_.getForum(cat.getId(), forum.getId());
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    forumService_.exportXML(cat.getId(), forum.getId(), new ArrayList<String>(), forum.getPath(), bos, false);
    assertEquals("can't export Forum into XML file", bos.size() > 0, true);
  }

  public void testTag() throws Exception {
    // set Data
    setData();
    Tag tag = createTag("Tag1");
    Tag tag2 = createTag("Tag2");
    Tag tag3 = createTag("Tag3");

    // add tag
    List<Tag> tags = new ArrayList<Tag>();
    tags.add(tag);
    tags.add(tag2);
    tags.add(tag3);
    Topic topic = forumService_.getTopic(categoryId, forumId, topicId, "");
    forumService_.addTag(tags, USER_ROOT, topic.getPath());
    // get Tags name in topic by root.
    // List<String> list = forumService_.getTagNameInTopic(USER_ROOT+","+topicId);

    // Test get tag
    String id = Utils.TAG + tag.getName();
    tag = forumService_.getTag(id);
    assertNotNull(tag);

    // Get all tag
    // assertEquals("All tags size is not 3", 3, forumService_.getAllTags().size());

  }

  public void testSearch() throws Exception {
  /*  
    setData(); //getQuickSearch List<String> users = new ArrayList<String>(); users.add("root"); String pathQuery = ""; // from ForumService/ String textQuery = "description"; String type = "true,all"; List<ForumSearch> forumSearchs = forumService_.getQuickSearch(textQuery, type, pathQuery, "root", null, null, null); assertEquals(forumSearchs.isEmpty(), false); //getAdvancedSearch
    ForumEventQuery eventQuery = new ForumEventQuery(); eventQuery.setListOfUser(users); eventQuery.setUserPermission(0); eventQuery.setType(Utils.TOPIC) ; eventQuery.setKeyValue(textQuery) ; eventQuery.setValueIn("entire") ; eventQuery.setPath("") ; eventQuery.setByUser(""); eventQuery.setIsLock("") ; eventQuery.setIsClose("") ; eventQuery.setTopicCountMin("0") ;
    eventQuery.setPostCountMin("0") ; eventQuery.setViewCountMin("0") ; eventQuery.setModerator("") ; forumSearchs = forumService_.getAdvancedSearch(eventQuery, null, null); assertEquals(forumSearchs.isEmpty(), false);
  */   
  }

  public void testWatch() throws Exception {
    // set Data
    setData();
    // addWatch
    String topicPath = categoryId + "/" + forumId;
    List<String> values = new ArrayList<String>();
    values.add("duytucntt@gmail.com");
    forumService_.addWatch(1, topicPath, values, "root");
    // watch by user
    List<Watch> watchs = forumService_.getWatchByUser("root");
    assertEquals(watchs.get(0).getEmail(), values.get(0));
    forumService_.removeWatch(1, topicPath, "/" + values.get(0));
    watchs = forumService_.getWatchByUser("root");
    assertEquals(watchs.size(), 0);
  }

  public void testIpBan() throws Exception {
    // set Data
    setData();
    // set Ip ban
    String ip = "192.168.1.10";
    // save Ip ban
    forumService_.addBanIP(ip);
    // get Ip ban
    List<String> listBans = forumService_.getBanList();
    assertEquals("Ip have adding in listBans", listBans.get(0), ip);
    // addBanIPForum
    forumService_.addBanIPForum(ip, categoryId + "/" + forumId);
    // getForumBanList
    List<String> listIpBanInForum = forumService_.getForumBanList(categoryId + "/" + forumId);
    assertEquals("Ip add in forum", listIpBanInForum.get(0), ip);
    // removeBanIPForum
    forumService_.removeBanIPForum(ip, categoryId + "/" + forumId);
    listIpBanInForum = forumService_.getForumBanList(categoryId + "/" + forumId);
    assertEquals("Ip is removed in listIpBanInForum, size is not 0 ", listIpBanInForum.size(), 0);
    // removeIpBan
    forumService_.removeBan(ip);
    listBans = forumService_.getBanList();
    assertEquals("Ip is removed in listBans, size is not 0 ", listBans.size(), 0);
  }

  public void testCalculateDeletedGroupForSpace() throws Exception {
    killData();
    // test for case in spaces:
    String groupId = "/spaces/new_space";
    String groupName = "new_space";
    String cateId = Utils.CATEGORY + "spaces";
    String forumId = Utils.FORUM_SPACE_ID_PREFIX + groupName;
    Category category = createCategory(cateId);
    category.setCategoryName("spaces");
    category.setUserPrivate(new String[] { groupId });
    forumService_.saveCategory(category, true);
    Forum forum = createdForum();
    forum.setForumName("New Space");
    forum.setId(forumId);
    forumService_.saveForum(cateId, forum, true);
    assertNotNull(String.format("The forum %s in space %s is null", forumId, groupName), forumService_.getForum(cateId, forumId));
    forumService_.calculateDeletedGroup(groupId, groupName);
    assertNull(String.format("The forum %s is not null after deleted the group %s ", forumId, groupId), forumService_.getForum(cateId, forumId));
  }

  public void testCalculateDeletedGroupForNormal() throws Exception {
    killData();
    // set group in categories/forums/topics
    String groupId = "/platform/users";
    String groupName = "users";
    UserProfile profile = createdUserProfile(USER_DEMO);
    profile.setUserRole(UserProfile.USER);
    profile.setUserTitle("User");
    profile.setModerateForums(new String[]{""});
    profile.setModerateCategory(new String[]{""});
    forumService_.saveUserProfile(profile, false, false);
    forumService_.saveUserModerator(USER_DEMO, new ArrayList<String>(), false);
    assertEquals(UserProfile.USER, forumService_.getUserInfo(USER_DEMO).getUserRole());

    String []groupUser = new String[]{groupId, USER_ROOT};
    Category category = createCategory(getId(Utils.CATEGORY));
    category.setUserPrivate(groupUser);
    category.setCreateTopicRole(groupUser);
    category.setModerators(groupUser);
    category.setViewer(groupUser);
    category.setPoster(groupUser);
    forumService_.saveCategory(category, true);
    Forum forum = createdForum();
    forum.setViewer(groupUser);
    forum.setCreateTopicRole(groupUser);
    forum.setPoster(groupUser);
    forum.setModerators(groupUser);
    forumService_.saveForum(category.getId(), forum, true);
    // the user demo in group "/platform/users" is moderator of forum, checking it
    assertEquals(UserProfile.MODERATOR, forumService_.getUserInfo(USER_DEMO).getUserRole());
    
    Topic topic = createdTopic(USER_DEMO);
    topic.setCanView(groupUser);
    topic.setCanPost(groupUser);
    forumService_.saveTopic(category.getId(), forum.getId(), topic, true, false, new MessageBuilder());
    // checking data
    assertEquals(ArrayToString(groupUser), ArrayToString(forumService_.getCategory(category.getId()).getUserPrivate()));
    assertEquals(ArrayToString(groupUser), ArrayToString(forumService_.getForum(category.getId(), forum.getId()).getModerators()));
    assertEquals(ArrayToString(groupUser), ArrayToString(forumService_.getTopic(category.getId(), forum.getId(), topic.getId(), null).getCanView()));
    // deleted group in system
    forumService_.calculateDeletedGroup(groupId, groupName);
    // checking again data
    assertEquals(UserProfile.USER, forumService_.getUserInfo(USER_DEMO).getUserRole());
    assertEquals(USER_ROOT, ArrayToString(forumService_.getCategory(category.getId()).getUserPrivate()));
    assertEquals(USER_ROOT, ArrayToString(forumService_.getForum(category.getId(), forum.getId()).getModerators()));
    assertEquals(USER_ROOT, ArrayToString(forumService_.getTopic(category.getId(), forum.getId(), topic.getId(), null).getCanView()));
  }
  
  private String ArrayToString(String[] strs) {
    List<String> list = Arrays.asList(strs);
    Collections.sort(list);
    return list.toString().replace("[", "").replace("]", "");
  }
  
  private UserProfile createdUserProfile(String userName) {
    UserProfile userProfile = new UserProfile();
    userProfile.setUserId(userName);
    userProfile.setUserRole((long) 0);
    userProfile.setUserTitle(Utils.ADMIN);
    userProfile.setEmail("duytucntt@gmail.com");
    userProfile.setJoinedDate(new Date());
    userProfile.setTimeZone(7.0);
    userProfile.setSignature("signature");
    return userProfile;
  }

  private Post createdPost() {
    Post post = new Post();
    post.setOwner("root");
    post.setCreatedDate(new Date());
    post.setModifiedBy("root");
    post.setModifiedDate(new Date());
    post.setName("SubJect");
    post.setMessage("content description");
    post.setRemoteAddr("192.168.1.11");
    post.setIcon("classNameIcon");
    post.setIsApproved(true);
    post.setIsActiveByTopic(true);
    post.setIsHidden(false);
    post.setIsWaiting(false);
    return post;
  }

  private Topic createdTopic(String owner) {
    Topic topicNew = new Topic();

    topicNew.setOwner(owner);
    topicNew.setTopicName("TestTopic");
    topicNew.setCreatedDate(new Date());
    topicNew.setModifiedBy("root");
    topicNew.setModifiedDate(new Date());
    topicNew.setLastPostBy("root");
    topicNew.setLastPostDate(new Date());
    topicNew.setDescription("Topic description");
    topicNew.setPostCount(0);
    topicNew.setViewCount(0);
    topicNew.setIsNotifyWhenAddPost("");
    topicNew.setIsModeratePost(false);
    topicNew.setIsClosed(false);
    topicNew.setIsLock(false);
    topicNew.setIsWaiting(false);
    topicNew.setIsActive(true);
    topicNew.setIcon("classNameIcon");
    topicNew.setIsApproved(true);
    topicNew.setCanView(new String[] {});
    topicNew.setCanPost(new String[] {});
    return topicNew;
  }

  private Forum createdForum() {
    Forum forum = new Forum();
    forum.setOwner("root");
    forum.setForumName("TestForum");
    forum.setForumOrder(1);
    forum.setCreatedDate(new Date());
    forum.setModifiedBy("root");
    forum.setModifiedDate(new Date());
    forum.setLastTopicPath("");
    forum.setDescription("description");
    forum.setPostCount(0);
    forum.setTopicCount(0);

    forum.setNotifyWhenAddTopic(new String[] {});
    forum.setNotifyWhenAddPost(new String[] {});
    forum.setIsModeratePost(false);
    forum.setIsModerateTopic(false);
    forum.setIsClosed(false);
    forum.setIsLock(false);

    forum.setViewer(new String[] {});
    forum.setCreateTopicRole(new String[] {});
    forum.setModerators(new String[] {});
    return forum;
  }

  private Category createCategory(String id) {
    Category cat = new Category(id);
    cat.setOwner("root");
    cat.setCategoryName("testCategory");
    cat.setCategoryOrder(1);
    cat.setCreatedDate(new Date());
    cat.setDescription("desciption");
    cat.setModifiedBy("root");
    cat.setModifiedDate(new Date());
    return cat;
  }

  private String getId(String type) {
    return type + IdGenerator.generate();
  }

  private Tag createTag(String name) {
    Tag tag = new Tag();
    tag.setName(name);
    tag.setUserTag(new String[] { "root" });
    return tag;
  }

  private ForumAdministration createForumAdministration() {
    ForumAdministration forumAdministration = new ForumAdministration();
    forumAdministration.setForumSortBy("forumName");
    forumAdministration.setForumSortByType("ascending");
    forumAdministration.setTopicSortBy("threadName");
    forumAdministration.setTopicSortByType("ascending");
    forumAdministration.setCensoredKeyword("");
    forumAdministration.setEnableHeaderSubject(false);
    forumAdministration.setHeaderSubject("");
    forumAdministration.setNotifyEmailContent("");
    return forumAdministration;
  }

  private TopicType createTopicType(String name) {
    TopicType topicType = new TopicType();
    topicType.setIcon("BlueIcon");
    topicType.setName(name);
    return topicType;
  }

  private Forum convertToForum(Object object) {
    if (object instanceof Forum) {
      return (Forum) object;
    }
    return null;
  }
  
  private void setMembershipEntry(String group, String membershipType, boolean isNew) {
    MembershipEntry membershipEntry = new MembershipEntry(group, membershipType);
    if(isNew) {
      membershipEntries.clear();
    }
    membershipEntries.add(membershipEntry);
  }

  private void loginUser(String userId) {
    Identity identity = new Identity(userId, membershipEntries);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }
}