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
package org.exoplatform.forum.service.cache;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.DataStorage;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.impl.model.PostFilter;
import org.exoplatform.ks.common.jcr.JCRSessionManager;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.test.BasicTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Oct 2, 2012  
 */
public class TestCacheDataStrorage extends BasicTestCase {

  protected static Log                  log                    = ExoLogger.getLogger("sample.services.test");

  protected static RepositoryService    repositoryService;

  protected static StandaloneContainer  container;

  protected final static String         REPO_NAME              = "repository".intern();

  protected final static String         SYSTEM_WS              = "system".intern();

  protected final static String         KNOWLEDGE_WS           = "knowledge".intern();

  protected static Node                 root_                  = null;

  protected SessionProvider             sProvider;

  private static SessionProviderService sessionProviderService = null;

  static {
    // we do this in static to save a few cycles
    initContainer();
    initJCR();
  }

  private DataStorage cacheDataStorage;

  private KSDataLocation dataLocation;

  private String       categoryId;

  private String       forumId;

  private String       topicId;
  
  public TestCacheDataStrorage() throws Exception {
  }

  public void setUp() throws Exception {
    startSystemSession();
    cacheDataStorage = (CachedDataStorage) container.getComponentInstanceOfType(DataStorage.class);
    dataLocation = (KSDataLocation) container.getComponentInstanceOfType(KSDataLocation.class);
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    sProvider = sessionProviderService.getSystemSessionProvider(null);
    
    //
    setData();
  }

  public void tearDown() throws Exception {
    super.tearDown();
    killData();

  }
  
  public void testPostListAccess() throws Exception {
    // set Data
    setData();

    //create 26 posts
    List<Post> posts = new ArrayList<Post>();
    for (int i = 0; i < 25; ++i) {
      Post post = createdPost();
      posts.add(post);
      cacheDataStorage.savePost(categoryId, forumId, topicId, post, true, new MessageBuilder());
    }
    // getPost
    assertNotNull(cacheDataStorage.getPost(categoryId, forumId, topicId, posts.get(0).getId()));
    assertEquals(25, cacheDataStorage.getTopic(categoryId, forumId, topicId, "").getPostCount());

    // get Page 1
    List<Post> gotList = cacheDataStorage.getPosts(new PostFilter(categoryId, forumId, topicId, "", "", "", "root"), 0, 10);   
    assertEquals(10, gotList.size());// size = 10: page 1
    
    
    //Page 2
    gotList = cacheDataStorage.getPosts(new PostFilter(categoryId, forumId, topicId, "", "", "", "root"), 10, 10);   
    assertEquals(10, gotList.size());// size = 10: page 2
    
    //Page 3
    gotList = cacheDataStorage.getPosts(new PostFilter(categoryId, forumId, topicId, "", "", "", "root"), 20, 10);   
    assertEquals(6, gotList.size());// size = 6: page 2
  }

  
  public void testPostListCount() throws Exception {
    // set Data
    setData();

    //create 25 + 1 post default post when created topic
    List<Post> posts = new ArrayList<Post>();
    for (int i = 0; i < 25; ++i) {
      Post post = createdPost();
      posts.add(post);
      cacheDataStorage.savePost(categoryId, forumId, topicId, post, true, new MessageBuilder());
    }
    // getPost
    assertNotNull(cacheDataStorage.getPost(categoryId, forumId, topicId, posts.get(0).getId()));
    
    //isApproved = true
    assertEquals(26, cacheDataStorage.getPostsCount(new PostFilter(this.categoryId, this.forumId, topicId, "true", "false", "false", "root")));
    
  //isApproved = false
    assertEquals(0, cacheDataStorage.getPostsCount(new PostFilter(this.categoryId, this.forumId, topicId, "false", "false", "false", "root")));
    
    //isHidden = true
    assertEquals(0, cacheDataStorage.getPostsCount(new PostFilter(this.categoryId, this.forumId, topicId, "false", "true", "false", "root")));
    //isWaiting = true
    assertEquals(0, cacheDataStorage.getPostsCount(new PostFilter(this.categoryId, this.forumId, topicId, "false", "false", "true", "root")));
    
    { //add more posts
      //add more 25 posts
      for (int i = 0; i < 25; ++i) {
        Post post = createdPost();
        posts.add(post);
        cacheDataStorage.savePost(categoryId, forumId, topicId, post, true, new MessageBuilder());
      }
      
      //isApproved = true
      assertEquals(51, cacheDataStorage.getPostsCount(new PostFilter(this.categoryId, this.forumId, topicId, "true", "false", "false", "root")));
      
    //isApproved = false
      assertEquals(0, cacheDataStorage.getPostsCount(new PostFilter(this.categoryId, this.forumId, topicId, "false", "false", "false", "root")));
      
      //isHidden = true
      assertEquals(0, cacheDataStorage.getPostsCount(new PostFilter(this.categoryId, this.forumId, topicId, "false", "true", "false", "root")));
      //isWaiting = true
      assertEquals(0, cacheDataStorage.getPostsCount(new PostFilter(this.categoryId, this.forumId, topicId, "false", "false", "true", "root")));
    
    }
  }
  
  
  protected void startSystemSession() {
    sProvider = sessionProviderService.getSystemSessionProvider(null);
  }

  protected void startSessionAs(String user) {
    Identity identity = new Identity(user);
    ConversationState state = new ConversationState(identity);
    sessionProviderService.setSessionProvider(null, new SessionProvider(state));
    sProvider = sessionProviderService.getSessionProvider(null);
  }

  protected void endSession() {
    sessionProviderService.removeSessionProvider(null);
    startSystemSession();
  }

  /**
   * All elements of a list should be contained in the expected array of String
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertContainsAll(String message, List<String> expected, List<String> actual) {
    assertEquals(message, expected.size(), actual.size());
    assertTrue(message, expected.containsAll(actual));
  }

  /**
   * Assertion method on string arrays
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertEquals(String message, String[] expected, String[] actual) {
    assertEquals(message, expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(message, expected[i], actual[i]);
    }
  }

  private static void initContainer() {
    //ClassLoader parentLoader;
    
    initProperties();
    
    try {
      ExoContainer container_ = RootContainer.getInstance();
      if (container_ != null) {
        container_.stop();
        container_.dispose();
      }
      String containerConf = TestCacheDataStrorage.class.getResource("/conf/portal/configuration.xml").toString();
      StandaloneContainer.addConfigurationURL(containerConf);
      
      container = StandaloneContainer.getInstance();
      String loginConf = Thread.currentThread().getContextClassLoader().getResource("conf/portal/login.conf").toString();

      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", loginConf);
      ExoContainerContext.setCurrentContainer(container);
    } catch (Exception e) {
      log.error("Failed to initialize standalone container: ", e);
    } finally {
      //Thread.currentThread().setContextClassLoader(parentLoader);
    }
  }

  private static void initProperties() {
    //
    URL fileProps = TestCacheDataStrorage.class.getResource("/conf/portal/configuration.properties");
    Map<String, String> props = ContainerUtil.loadProperties(fileProps);
    if (props != null)
    {
       for (Map.Entry<String, String> entry : props.entrySet())
       {
          String propertyName = entry.getKey();
          String propertyValue = entry.getValue();
          PropertyManager.setProperty(propertyName, propertyValue);
       }
    }
  }

  private static void initJCR() {
    try {
      repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);

      // Initialize datas
      Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(KNOWLEDGE_WS);
      root_ = session.getRootNode();
      sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);

      JCRSessionManager sessionManager = new JCRSessionManager(KNOWLEDGE_WS, repositoryService);
      KSDataLocation ksDataLocation = (KSDataLocation) container.getComponentInstanceOfType(KSDataLocation.class);
      ksDataLocation.setSessionManager(sessionManager);
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize JCR: ", e);
    }
  }
  
  private void setData() throws Exception {
    
    Category cat = createCategory(getId(Utils.CATEGORY));
    this.categoryId = cat.getId();
    cacheDataStorage.saveCategory(cat, true);
    Forum forum = createdForum();
    this.forumId = forum.getId();
    cacheDataStorage.saveForum(categoryId, forum, true);
    Topic topic = createdTopic("root");
    cacheDataStorage.saveTopic(categoryId, forumId, topic, true, false, new MessageBuilder());
    this.topicId = topic.getId();
  }
  
  private void killData() throws Exception {
    List<Category> cats = cacheDataStorage.getCategories();
    if (cats.size() > 0) {
      for (Category category : cats) {
        cacheDataStorage.removeCategory(category.getId());
      }
    }
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
  
}