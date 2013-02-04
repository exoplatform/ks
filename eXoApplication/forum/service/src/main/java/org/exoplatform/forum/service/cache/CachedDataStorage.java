package org.exoplatform.forum.service.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.NodeIterator;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.DataStorage;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.ForumSubscription;
import org.exoplatform.forum.service.InitializeForumPlugin;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.LazyPageList;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.service.SendMessageInfo;
import org.exoplatform.forum.service.SortSettings;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.ks.common.cache.ServiceContext;
import org.exoplatform.forum.service.cache.model.CacheType;
import org.exoplatform.ks.common.cache.model.CachedData;
import org.exoplatform.forum.service.cache.model.data.CategoryData;
import org.exoplatform.forum.service.cache.model.data.ForumData;
import org.exoplatform.forum.service.cache.model.data.LinkData;
import org.exoplatform.forum.service.cache.model.data.ListCategoryData;
import org.exoplatform.forum.service.cache.model.data.ListForumData;
import org.exoplatform.forum.service.cache.model.data.ListLinkData;
import org.exoplatform.forum.service.cache.model.data.ListPostData;
import org.exoplatform.forum.service.cache.model.data.ListWatchData;
import org.exoplatform.forum.service.cache.model.data.PostData;
import org.exoplatform.ks.common.cache.model.data.SimpleCacheData;
import org.exoplatform.forum.service.cache.model.data.TagData;
import org.exoplatform.forum.service.cache.model.data.TopicData;
import org.exoplatform.forum.service.cache.model.data.WatchData;
import org.exoplatform.forum.service.cache.model.key.CategoryKey;
import org.exoplatform.forum.service.cache.model.key.CategoryListKey;
import org.exoplatform.forum.service.cache.model.key.ForumKey;
import org.exoplatform.forum.service.cache.model.key.ForumListKey;
import org.exoplatform.forum.service.cache.model.key.LinkListKey;
import org.exoplatform.forum.service.cache.model.key.ObjectNameKey;
import org.exoplatform.forum.service.cache.model.key.PostKey;
import org.exoplatform.forum.service.cache.model.key.PostListCountKey;
import org.exoplatform.forum.service.cache.model.key.PostListKey;
import org.exoplatform.ks.common.cache.model.key.SimpleCacheKey;
import org.exoplatform.forum.service.cache.model.key.TopicKey;
import org.exoplatform.forum.service.cache.model.selector.CategoryIdSelector;
import org.exoplatform.forum.service.cache.model.selector.ForumPathSelector;
import org.exoplatform.forum.service.cache.model.selector.PostListCountSelector;
import org.exoplatform.ks.common.cache.model.selector.ScopeCacheSelector;
import org.exoplatform.forum.service.impl.JCRDataStorage;
import org.exoplatform.forum.service.impl.model.PostFilter;
import org.exoplatform.ks.common.conf.RoleRulesPlugin;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.future.FutureExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class CachedDataStorage implements DataStorage, Startable {

  private static final Log LOG = ExoLogger.getLogger(CachedDataStorage.class);
  private static final String PRIVATE_MESSAGE_COUNT_KEY = "messageCount";

  private DataStorage storage;
  private CacheService service;

  //
  private ExoCache<CategoryKey, CategoryData> categoryData;
  private ExoCache<CategoryListKey, ListCategoryData> categoryList;
  private ExoCache<ForumKey, ForumData> forumData;
  private ExoCache<ForumListKey, ListForumData> forumList;
  private ExoCache<PostKey, PostData> postData;
  private ExoCache<PostListKey, ListPostData> postList;
  private ExoCache<PostListCountKey, SimpleCacheData> postListCount;
  private ExoCache<TopicKey, TopicData> topicData;
  private ExoCache<SimpleCacheKey, ListWatchData> watchListData;
  private ExoCache<LinkListKey, ListLinkData> linkListData;
  private ExoCache<ObjectNameKey, CachedData> objectNameData;
  private ExoCache<SimpleCacheKey, SimpleCacheData> miscData;

  //
  private FutureExoCache<CategoryKey, CategoryData, ServiceContext<CategoryData>> categoryDataFuture;
  private FutureExoCache<CategoryListKey, ListCategoryData, ServiceContext<ListCategoryData>> categoryListFuture;
  private FutureExoCache<ForumKey, ForumData, ServiceContext<ForumData>> forumDataFuture;
  private FutureExoCache<ForumListKey, ListForumData, ServiceContext<ListForumData>> forumListFuture;
  private FutureExoCache<PostKey, PostData, ServiceContext<PostData>> postDataFuture;
  private FutureExoCache<PostListKey, ListPostData, ServiceContext<ListPostData>> postListFuture;
  private FutureExoCache<PostListCountKey, SimpleCacheData, ServiceContext<SimpleCacheData>> postListCountFuture;
  private FutureExoCache<TopicKey, TopicData, ServiceContext<TopicData>> topicDataFuture;
  private FutureExoCache<SimpleCacheKey, ListWatchData, ServiceContext<ListWatchData>> watchListDataFuture;
  private FutureExoCache<LinkListKey, ListLinkData, ServiceContext<ListLinkData>> linkListDataFuture;
  private FutureExoCache<SimpleCacheKey, SimpleCacheData, ServiceContext<SimpleCacheData>> miscDataFuture;

  private ForumStatistic statistic;

  public CachedDataStorage(CacheService service, JCRDataStorage storage) {
    
    this.storage = storage;
    this.service = service;

    
  }

  private void clearForumCache(Forum forum, boolean isPutNewKey) throws Exception {
    if (isPutNewKey) {
      forumData.put(new ForumKey(forum), new ForumData(forum));
    } else {
      forumData.remove(new ForumKey(forum));
    }
    statistic = null;
  }

  private void clearForumCache(String categoryId, String forumId, boolean isPutNewKey) throws Exception {
    Forum forum = getForum(categoryId, forumId);
    if (forum != null) {
      clearForumCache(forum, isPutNewKey);
    }
  }
  
  private void clearForumListCache() throws Exception {
    forumList.select(new ScopeCacheSelector<ForumListKey, ListForumData>());
  }
  
  private void clearPostListCache() throws Exception {
    postList.select(new ScopeCacheSelector<PostListKey, ListPostData>());
  }
  
  private void clearPostListCountCache(String topicId) throws Exception {   
    postListCount.select(new PostListCountSelector(topicId));
  }
  
  private void clearLinkListCache() throws Exception {
    linkListData.select(new ScopeCacheSelector<LinkListKey, ListLinkData>());
  }
  
  private void clearTopicCache(String topicPath) throws Exception {
    topicData.remove(new TopicKey(topicPath, true));
  }

  private void clearTopicCache(String categoryId, String forumId, String topicId) throws Exception {
    Topic topic = getTopic(categoryId, forumId, topicId, null);
    clearTopicCache(topic);
  }

  private void clearTopicCache(Topic topic) throws Exception {
    if (topic != null) {
      clearTopicCache(topic.getPath());
      objectNameData.remove(new ObjectNameKey(topic.getPath()));
      objectNameData.remove(new ObjectNameKey(topic.getId(), Utils.TOPIC));
    }
    
  }
  
  private void clearPostCache(String categoryId, String forumId, String topicId, String postId) throws Exception {
    postData.remove(new PostKey(categoryId, forumId, topicId, postId));
  }
  
  private void clearObjectCache(Forum forum, boolean isPutNewKey) throws Exception {
    if (forum != null) {
      ForumData forumData = new ForumData(forum);
      String categoryId = forum.getCategoryId();
      String forumId = forum.getId();
      if (isPutNewKey) {
        objectNameData.put(new ObjectNameKey(categoryId + "/" + forumId), forumData);
        objectNameData.put(new ObjectNameKey(forumId, Utils.FORUM), forumData);
      } else {
        objectNameData.remove(new ObjectNameKey(categoryId + "/" + forumId));
        objectNameData.remove(new ObjectNameKey(forumId, Utils.FORUM));
      }
    }
  }
  
  private void clearObjectCache(Category category, boolean isNew) throws Exception {
    
    if (isNew) {
      CategoryData categoryData = new CategoryData(category);
      objectNameData.put(new ObjectNameKey(category.getId(), Utils.CATEGORY), categoryData);
    } else {
      objectNameData.remove(new ObjectNameKey(category.getId(), Utils.CATEGORY));
    }
  }
  
  private void clearObjectCache(String categoryId, String forumId, boolean isPutNewKey) throws Exception {
    clearObjectCache(getForum(categoryId, forumId), isPutNewKey);
  }
  
  private void clearWatchingItemCache(String watchingItemPath) throws Exception {
    String categoryId = watchingItemPath.contains("/") ? watchingItemPath.substring(0, watchingItemPath.indexOf("/")) : watchingItemPath;
    String forumId = watchingItemPath.contains("/" + Utils.FORUM) ? watchingItemPath.substring(watchingItemPath.indexOf("/" + Utils.FORUM) + 1) : null;
    forumId = (!Utils.isEmpty(forumId) && forumId.contains("/")) ? forumId.substring(0, forumId.indexOf("/")) : forumId;
    String topicId = watchingItemPath.contains(Utils.TOPIC) ? watchingItemPath.substring(watchingItemPath.indexOf(Utils.TOPIC)) : null;
    
    // Clear watching item data
    if (!Utils.isEmpty(topicId)) {
      topicData.remove(new TopicKey(categoryId + "/" + forumId + "/" + topicId, false));
    } else if (!Utils.isEmpty(forumId)) {
      forumData.remove(new ForumKey(categoryId, forumId));
    } else {
      categoryData.remove(new CategoryKey(categoryId));
    }
  }

  public void start() {

    //
    this.categoryData = CacheType.CATEGORY_DATA.getFromService(service);
    this.categoryList = CacheType.CATEGORY_LIST.getFromService(service);
    this.forumData = CacheType.FORUM_DATA.getFromService(service);
    this.forumList = CacheType.FORUM_LIST.getFromService(service);
    this.postData = CacheType.POST_DATA.getFromService(service);
    this.postList = CacheType.POST_LIST.getFromService(service);
    this.postListCount = CacheType.POST_LIST_COUNT.getFromService(service);
    this.topicData = CacheType.TOPIC_DATA.getFromService(service);
    this.objectNameData = CacheType.OBJECT_NAME_DATA.getFromService(service);
    this.miscData = CacheType.MISC_DATA.getFromService(service);
    this.watchListData = CacheType.WATCH_LIST_DATA.getFromService(service);
    this.linkListData = CacheType.LINK_LIST_DATA.getFromService(service);

    //
    this.categoryDataFuture = CacheType.CATEGORY_DATA.createFutureCache(categoryData);
    this.categoryListFuture = CacheType.CATEGORY_LIST.createFutureCache(categoryList);
    this.forumDataFuture = CacheType.FORUM_DATA.createFutureCache(forumData);
    this.forumListFuture = CacheType.FORUM_LIST.createFutureCache(forumList);
    this.postDataFuture = CacheType.POST_DATA.createFutureCache(postData);
    this.postListFuture = CacheType.POST_LIST.createFutureCache(postList);
    this.postListCountFuture = CacheType.POST_LIST_COUNT.createFutureCache(postListCount);
    this.topicDataFuture = CacheType.TOPIC_DATA.createFutureCache(topicData);
    this.watchListDataFuture = CacheType.WATCH_LIST_DATA.createFutureCache(watchListData);
    this.linkListDataFuture = CacheType.LINK_LIST_DATA.createFutureCache(linkListData);
    this.miscDataFuture = CacheType.MISC_DATA.createFutureCache(miscData);
    
  }

  public void stop() {
  }

  private ListLinkData buildLinkInput(List<ForumLinkData> links) {
    List<LinkData> data = new ArrayList<LinkData>();
    for (ForumLinkData l : links) {
      data.add(new LinkData(l));
    }
    return new ListLinkData(data);
  }

  private List<ForumLinkData> buildLinkOutput(ListLinkData data) {

    if (data == null) {
      return null;
    }

    List<ForumLinkData> out = new ArrayList<ForumLinkData>();
    for (LinkData d : data.getIds()) {
      out.add(d.build());
    }
    return out;

  }
  
  private ListWatchData buildWatchInput(List<Watch> watches) {
    List<WatchData> data = new ArrayList<WatchData>();
    for (Watch w : watches) {
      data.add(new WatchData(w));
    }
    return new ListWatchData(data);
  }

  private List<Watch> buildWatchOutput(ListWatchData data) {

    if (data == null) {
      return null;
    }

    List<Watch> out = new ArrayList<Watch>();
    for (WatchData d : data.getIds()) {
      out.add(d.build());
    }
    return out;

  }

  private ListPostData buildPostInput(List<Post> posts) {
    List<PostKey> data = new ArrayList<PostKey>();
    for (Post p : posts) {
      data.add(new PostKey(p));
    }
    return new ListPostData(data);
  }

  private List<Post> buildPostOutput(ListPostData data) {

    if (data == null) {
      return null;
    }

    List<Post> out = new ArrayList<Post>();
    for (PostKey k : data.getIds()) {
      try {
        out.add(getPost(k.getCategory(), k.getForum(), k.getTopic(), k.getPost()));
      } catch (Exception e) {
        LOG.error(e);
      }
    }
    return out;

  }

  private ListForumData buildForumInput(List<Forum> forums) {
    List<ForumKey> keys = new ArrayList<ForumKey>();
    for (Forum f : forums) {
      keys.add(new ForumKey(f));
    }
    return new ListForumData(keys);
  }

  private List<Forum> buildForumOutput(ListForumData data) {

    if (data == null) {
      return null;
    }

    List<Forum> out = new ArrayList<Forum>();
    for (ForumKey k : data.getIds()) {
      out.add(getForum(k.getCategoryId(), k.getForumId()));
    }
    return out;

  }

  private ListCategoryData buildCategoryInput(List<Category> categories) {
    List<CategoryKey> keys = new ArrayList<CategoryKey>();
    for (Category c : categories) {
      keys.add(new CategoryKey(c));
    }
    return new ListCategoryData(keys);
  }

  private List<Category> buildCategoryOutput(ListCategoryData data) {

    if (data == null) {
      return null;
    }

    List<Category> out = new ArrayList<Category>();
    for (CategoryKey k : data.getIds()) {
      try {
        out.add(getCategory(k.getId()));
      } catch (Exception e) {
        LOG.error(e);
      }
    }
    return out;

  }

  @ManagedDescription("repository for forum storage")
  @Managed
  public String getRepository() throws Exception {
    return storage.getRepository();
  }

  @ManagedDescription("workspace for the forum storage")
  @Managed
  public String getWorkspace() throws Exception {
    return storage.getWorkspace();
  }

  @ManagedDescription("data path for forum storage")
  @Managed
  public String getPath() throws Exception {
    return storage.getPath();
  }

  public void addPlugin(ComponentPlugin plugin) throws Exception {
    storage.addPlugin(plugin);
  }

  public void addRolePlugin(ComponentPlugin plugin) throws Exception {
    storage.addRolePlugin(plugin);
  }

  public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception {
    storage.addInitialDataPlugin(plugin);
  }

  public void addInitialDefaultDataPlugin(ComponentPlugin plugin) throws Exception {
    storage.addInitialDefaultDataPlugin(plugin);
  }

  public void addCalculateModeratorEventListener() throws Exception {
    storage.addCalculateModeratorEventListener();
  }

  public void addDeletedUserCalculateListener() throws Exception {
    storage.addDeletedUserCalculateListener();
  }

  public void initCategoryListener() {
    storage.initCategoryListener();
  }

  public boolean isAdminRole(String userName) throws Exception {
    return storage.isAdminRole(userName);
  }

  public void setDefaultAvatar(String userName) {
    storage.setDefaultAvatar(userName);
  }

  public ForumAttachment getUserAvatar(String userName) throws Exception {
    return storage.getUserAvatar(userName);
  }

  public void saveUserAvatar(String userId, ForumAttachment fileAttachment) throws Exception {
    storage.saveUserAvatar(userId, fileAttachment);
  }

  public void saveForumAdministration(ForumAdministration forumAdministration) throws Exception {
    storage.saveForumAdministration(forumAdministration);
  }

  public ForumAdministration getForumAdministration() throws Exception {
    return storage.getForumAdministration();
  }

  public SortSettings getForumSortSettings() throws Exception {
    return storage.getForumSortSettings();
  }

  public SortSettings getTopicSortSettings() throws Exception {
    return storage.getTopicSortSettings();
  }

  // TODO : need range
  public List<Category> getCategories() {

    return buildCategoryOutput(
        categoryListFuture.get(
            new ServiceContext<ListCategoryData>() {
              public ListCategoryData execute() {
                return buildCategoryInput(storage.getCategories());
              }
            },
            new CategoryListKey(null)
        )
    );
    
  }

  public Category getCategory(final String categoryId) throws Exception {

    return categoryDataFuture.get(
      new ServiceContext<CategoryData>() {
        public CategoryData execute() {
          try {
            Category got = storage.getCategory(categoryId);
            if (got != null) {
              return new CategoryData(got);
            }
            else {
              return CategoryData.NULL;
            }
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      },
      new CategoryKey(categoryId)
    ).build();

  }

  public String[] getPermissionTopicByCategory(String categoryId, String type) throws Exception {
    return storage.getPermissionTopicByCategory(categoryId, type);
  }

  public void saveCategory(Category category, boolean isNew) throws Exception {
    storage.saveCategory(category, isNew);
    categoryData.put(new CategoryKey(category), new CategoryData(category));
    categoryList.select(new ScopeCacheSelector<CategoryListKey, ListCategoryData>());
    clearLinkListCache();
    clearObjectCache(category, isNew);
  }

  public void saveModOfCategory(List<String> moderatorCate, String userId, boolean isAdd) {
    storage.saveModOfCategory(moderatorCate, userId, isAdd);
    try {
      categoryData.select(new CategoryIdSelector(moderatorCate, categoryData));
    } catch (Exception e) {
      LOG.debug("Can not clear list categories in cached.", e);
    } 
  }

  public void calculateModerator(String nodePath, boolean isNew) throws Exception {
    storage.calculateModerator(nodePath, isNew);
    clearForumCache(Utils.getCategoryId(nodePath), Utils.getForumId(nodePath), false);
    clearForumListCache();
  }

  public void registerListenerForCategory(String path) throws Exception {
    storage.registerListenerForCategory(path);
  }

  public void unRegisterListenerForCategory(String path) throws Exception {
    storage.unRegisterListenerForCategory(path);
  }

  public Category removeCategory(String categoryId) throws Exception {
    categoryData.remove(new CategoryKey(categoryId));
    categoryList.select(new ScopeCacheSelector<CategoryListKey, ListCategoryData>());
    clearLinkListCache();
    return storage.removeCategory(categoryId);  
  }

  // TODO : need range
  public List<Forum> getForums(final String categoryId, final String strQuery) throws Exception {

    return buildForumOutput(
        forumListFuture.get(
            new ServiceContext<ListForumData>() {
              public ListForumData execute() {
                try {
                  return buildForumInput(storage.getForums(categoryId, strQuery));
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              }
            },
            new ForumListKey(categoryId, strQuery)
        )
    );

  }

  public List<Forum> getForumSummaries(String categoryId, String strQuery) throws Exception {
    return getForums(categoryId, strQuery);
  }

  public Forum getForum(final String categoryId, final String forumId) {

    return forumDataFuture.get(
        new ServiceContext<ForumData>() {
          public ForumData execute() {
            Forum got = storage.getForum(categoryId, forumId);
            if (got != null) {
              return new ForumData(got);
            }
            else {
              return ForumData.NULL;
            }
          }
        },
        new ForumKey(categoryId, forumId)
    ).build();

  }

  public void modifyForum(Forum forum, int type) throws Exception {
    storage.modifyForum(forum, type);
    clearForumCache(forum, true);
    clearForumListCache();
    clearObjectCache(forum, true);
  }

  public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception {
    storage.saveForum(categoryId, forum, isNew);
    clearForumCache(forum, true);
    clearForumListCache();
    clearLinkListCache();
    clearObjectCache(forum, true);
  }

  public void saveModerateOfForums(List<String> forumPaths, String userName, boolean isDelete) throws Exception {
    storage.saveModerateOfForums(forumPaths, userName, isDelete);
    forumData.select(new ForumPathSelector(forumPaths.toArray(new String[forumPaths.size()]), forumData));
    clearForumListCache();
    for (String forumPath : forumPaths) {
      clearObjectCache(Utils.getCategoryId(forumPath), Utils.getForumId(forumPath), true);
    }
  }

  public Forum removeForum(String categoryId, String forumId) throws Exception {
    clearForumCache(categoryId, forumId, false);
    clearForumListCache();
    clearLinkListCache();
    clearObjectCache(categoryId, forumId, false);
    return storage.removeForum(categoryId, forumId);
  }

  public void moveForum(List<Forum> forums, String destCategoryPath) throws Exception {
    for (Forum forum : forums) {
      clearForumCache(forum, false);
      clearObjectCache(forum, false);
    }
    clearForumListCache();
    clearLinkListCache();
    storage.moveForum(forums, destCategoryPath);
  }

  public JCRPageList getPageTopic(String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception {
    return storage.getPageTopic(categoryId, forumId, strQuery, strOrderBy);
  }

  public LazyPageList<Topic> getTopicList(String categoryId, String forumId, String xpathConditions, String strOrderBy, int pageSize) throws Exception {
    return storage.getTopicList(categoryId, forumId, xpathConditions, strOrderBy, pageSize);
  }

  public List<Topic> getTopics(String categoryId, String forumId) throws Exception {
    return storage.getTopics(categoryId, forumId);
  }

  public Topic getTopic(String categoryId, String forumId, String topicId, String userRead) throws Exception {
    return storage.getTopic(categoryId, forumId, topicId, userRead);
  }

  public Topic getTopicSummary(final String topicPath, final boolean isLastPost) throws Exception {

    return topicDataFuture.get(
        new ServiceContext<TopicData>() {
          public TopicData execute() {
            try {
              Topic got = storage.getTopicSummary(topicPath, isLastPost);
              if (got != null) {
                return new TopicData(got);
              }
              else {
                return TopicData.NULL;
              }
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        },
        new TopicKey(topicPath, isLastPost)
    ).build();

  }

  public Topic getTopicByPath(String topicPath, boolean isLastPost) throws Exception {
    return storage.getTopicByPath(topicPath, isLastPost);
  }

  public Topic getTopicUpdate(Topic topic, boolean isSummary) throws Exception {
    return storage.getTopicUpdate(topic, isSummary);
  }

  public JCRPageList getPageTopicOld(long date, String forumPatch) throws Exception {
    return storage.getPageTopicOld(date, forumPatch);
  }

  public List<Topic> getAllTopicsOld(long date, String forumPatch) throws Exception {
    return storage.getAllTopicsOld(date, forumPatch);
  }

  public long getTotalTopicOld(long date, String forumPatch) {
    return storage.getTotalTopicOld(date, forumPatch);
  }

  public JCRPageList getPageTopicByUser(String userName, boolean isMod, String strOrderBy) throws Exception {
    return storage.getPageTopicByUser(userName, isMod, strOrderBy);
  }

  public void modifyTopic(List<Topic> topics, int type) {
    storage.modifyTopic(topics, type);
  }

  public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, MessageBuilder messageBuilder) throws Exception {
    storage.saveTopic(categoryId, forumId, topic, isNew, isMove, messageBuilder);
    clearForumCache(categoryId, forumId, false);
    clearForumListCache();
    
    // Clear first post cache.
    if (isNew == false) {
      clearPostCache(categoryId, forumId, topic.getId(), topic.getId().replace(Utils.TOPIC, Utils.POST));
      clearTopicCache(topic);
    }
  }

  public Topic removeTopic(String categoryId, String forumId, String topicId) {
    try {
      clearForumCache(categoryId, forumId, false);
      clearForumListCache();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return storage.removeTopic(categoryId, forumId, topicId);
  }

  public void moveTopic(List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception {
    storage.moveTopic(topics, destForumPath, mailContent, link);
    if (topics != null && topics.size() > 0) {
      forumData.select(new ForumPathSelector(new String[] {Utils.getForumPath(topics.get(0).getPath()), destForumPath}, forumData));
      clearForumListCache();
      for (Topic topic : topics) {
        clearTopicCache(topic);
      }
    }
  }

  public long getLastReadIndex(String path, String isApproved, String isHidden, String userLogin) throws Exception {
    return storage.getLastReadIndex(path, isApproved, isHidden, userLogin);
  }

  public JCRPageList getPosts(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception {
    return storage.getPosts(categoryId, forumId, topicId, isApproved, isHidden, strQuery, userLogin);
  }

  public long getAvailablePost(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception {
    return storage.getAvailablePost(categoryId, forumId, topicId, isApproved, isHidden, userLogin);
  }

  public JCRPageList getPagePostByUser(String userName, String userId, boolean isMod, String strOrderBy) throws Exception {
    return storage.getPagePostByUser(userName, userId, isMod, strOrderBy);
  }

  public Post getPost(final String categoryId, final String forumId, final String topicId, final String postId) throws Exception {

    PostKey key = new PostKey(categoryId, forumId, topicId, postId);

    return postDataFuture.get(
        new ServiceContext<PostData>() {

          public PostData execute() {
            try {
              Post got = storage.getPost(categoryId, forumId, topicId, postId);
              if (got == null) {
                return PostData.NULL;
              } else {
                return new PostData(got);
              }
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        },
        key
    ).build();

  }

  public JCRPageList getListPostsByIP(String ip, String strOrderBy) throws Exception {
    return storage.getListPostsByIP(ip, strOrderBy);
  }
  
  public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew, MessageBuilder messageBuilder) throws Exception {
    storage.savePost(categoryId, forumId, topicId, post, isNew, messageBuilder);
    clearForumCache(categoryId, forumId, false);
    clearForumListCache();
    clearTopicCache(categoryId, forumId, topicId);
    clearPostListCache();
    
    //
    if (isNew == false) {
      clearPostCache(categoryId, forumId, topicId, post.getId());
    } else {
      clearPostListCountCache(topicId);
    }
    statistic = null;
  }

  public void modifyPost(List<Post> posts, int type) {
    storage.modifyPost(posts, type);
    //clear caching
    try {
      clearPostListCache();
      //
      for(Post post : posts) {
        String categoryId = Utils.getCategoryId(post.getPath());
        clearPostCache(categoryId, post.getForumId(), post.getTopicId(), post.getId());
        clearPostListCountCache(post.getTopicId());
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  public Post removePost(String categoryId, String forumId, String topicId, String postId) {
    try {
      clearForumCache(categoryId, forumId, false);
      clearForumListCache();
      clearTopicCache(categoryId, forumId, topicId);
      clearPostListCache();
      clearPostCache(categoryId, forumId, topicId, postId);
      clearPostListCountCache(topicId);
      statistic = null;
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return storage.removePost(categoryId, forumId, topicId, postId);
  }

  public void addTag(List<Tag> tags, String userName, String topicPath) throws Exception {
    storage.addTag(tags, userName, topicPath);
  }

  public void unTag(String tagId, String userName, String topicPath) {
    storage.unTag(tagId, userName, topicPath);
  }

  public Tag getTag(String tagId) throws Exception {
    return storage.getTag(tagId);
  }

  public List<String> getTagNameInTopic(String userAndTopicId) throws Exception {
    return storage.getTagNameInTopic(userAndTopicId);
  }

  public List<String> getAllTagName(String keyValue, String userAndTopicId) throws Exception {
    return storage.getAllTagName(keyValue, userAndTopicId);
  }

  public List<Tag> getAllTags() throws Exception {
    return storage.getAllTags();
  }

  public List<Tag> getMyTagInTopic(String[] tagIds) throws Exception {
    return storage.getMyTagInTopic(tagIds);
  }

  public JCRPageList getTopicByMyTag(String userIdAndtagId, String strOrderBy) throws Exception {
    return storage.getTopicByMyTag(userIdAndtagId, strOrderBy);
  }

  public void saveTag(Tag newTag) throws Exception {
    storage.saveTag(newTag);
  }

  public JCRPageList getPageListUserProfile() throws Exception {
    return storage.getPageListUserProfile();
  }

  public JCRPageList searchUserProfile(String userSearch) throws Exception {
    return storage.searchUserProfile(userSearch);
  }

  public UserProfile getDefaultUserProfile(String userName, String ip) throws Exception {
    return storage.getDefaultUserProfile(userName, ip);
  }

  public UserProfile updateUserProfileSetting(UserProfile userProfile) throws Exception {
    return storage.updateUserProfileSetting(userProfile);
  }

  public String getScreenName(final String userName) throws Exception {

    SimpleCacheKey key = new SimpleCacheKey("screen", userName);

    return (String) miscDataFuture.get(
        new ServiceContext<SimpleCacheData>() {
          public SimpleCacheData execute() {
            try {
              String got = storage.getScreenName(userName);
              return new SimpleCacheData(got);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        },
        key
    ).build();

  }

  public UserProfile getUserSettingProfile(String userName) throws Exception {
    return storage.getUserSettingProfile(userName);
  }

  public void saveUserSettingProfile(UserProfile userProfile) throws Exception {
    storage.saveUserSettingProfile(userProfile);
  }

  public UserProfile getLastPostIdRead(UserProfile userProfile, String isOfForum) throws Exception {
    return storage.getLastPostIdRead(userProfile, isOfForum);
  }

  public void saveLastPostIdRead(String userId, String[] lastReadPostOfForum, String[] lastReadPostOfTopic) throws Exception {
    storage.saveLastPostIdRead(userId, lastReadPostOfForum, lastReadPostOfTopic);
  }

  public List<String> getUserModerator(String userName, boolean isModeCate) throws Exception {
    return storage.getUserModerator(userName, isModeCate);
  }

  public void saveUserModerator(String userName, List<String> ids, boolean isModeCate) throws Exception {
    storage.saveUserModerator(userName, ids, isModeCate);
  }

  public UserProfile getUserInfo(String userName) throws Exception {
    return storage.getUserInfo(userName);
  }

  public List<UserProfile> getQuickProfiles(List<String> userList) throws Exception {
    return storage.getQuickProfiles(userList);
  }

  public UserProfile getQuickProfile(String userName) throws Exception {
    return storage.getQuickProfile(userName);
  }

  public UserProfile getUserInformations(UserProfile userProfile) throws Exception {
    return storage.getUserInformations(userProfile);
  }

  public void saveUserProfile(UserProfile newUserProfile, boolean isOption, boolean isBan) throws Exception {
    storage.saveUserProfile(newUserProfile, isOption, isBan);
  }

  public UserProfile getUserProfileManagement(String userName) throws Exception {
    return storage.getUserProfileManagement(userName);
  }

  public void saveUserBookmark(String userName, String bookMark, boolean isNew) throws Exception {
    storage.saveUserBookmark(userName, bookMark, isNew);
  }

  public void saveCollapsedCategories(String userName, String categoryId, boolean isAdd) throws Exception {
    storage.saveCollapsedCategories(userName, categoryId, isAdd);
  }

  public void saveReadMessage(String messageId, String userName, String type) throws Exception {
    storage.saveReadMessage(messageId, userName, type);
    miscData.remove(new SimpleCacheKey(PRIVATE_MESSAGE_COUNT_KEY, userName));
  }

  public JCRPageList getPrivateMessage(String userName, String type) throws Exception {
    return storage.getPrivateMessage(userName, type);
  }

  public long getNewPrivateMessage(final String userName) throws Exception {

    SimpleCacheKey key = new SimpleCacheKey(PRIVATE_MESSAGE_COUNT_KEY, userName);

    return (Long) miscDataFuture.get(
        new ServiceContext<SimpleCacheData>() {
          public SimpleCacheData execute() {
            try {
              Long got = storage.getNewPrivateMessage(userName);
              return new SimpleCacheData(got);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        },
        key
    ).build();

  }

  public void savePrivateMessage(ForumPrivateMessage privateMessage) throws Exception {
    storage.savePrivateMessage(privateMessage);
    miscData.remove(new SimpleCacheKey(PRIVATE_MESSAGE_COUNT_KEY, privateMessage.getSendTo()));
  }

  public void removePrivateMessage(String messageId, String userName, String type) throws Exception {
    storage.removePrivateMessage(messageId, userName, type);
    miscData.remove(new SimpleCacheKey(PRIVATE_MESSAGE_COUNT_KEY, userName));
  }

  public ForumSubscription getForumSubscription(String userId) {
    return storage.getForumSubscription(userId);
  }

  public void saveForumSubscription(ForumSubscription forumSubscription, String userId) throws Exception {
    storage.saveForumSubscription(forumSubscription, userId);
  }

  public ForumStatistic getForumStatistic() throws Exception {
    if (statistic != null) {
      return statistic;
    } else {
      return statistic = storage.getForumStatistic();
    }
  }

  public void saveForumStatistic(ForumStatistic forumStatistic) throws Exception {
    storage.saveForumStatistic(forumStatistic);
  }

  public Object getObjectNameByPath(final String path) throws Exception {

    ObjectNameKey key = new ObjectNameKey(path);
    CachedData data = objectNameData.get(key);

    if (data == null) {
      Object got = storage.getObjectNameByPath(path);
      if (got instanceof Post) {
        objectNameData.put(key, new PostData((Post) got));
      } else if (got instanceof Topic) {
        objectNameData.put(key, new TopicData((Topic) got));
      } else if (got instanceof Forum) {
        objectNameData.put(key, new ForumData((Forum) got));
      } else if (got instanceof Category) {
        objectNameData.put(key, new CategoryData((Category) got));
      } else if (got instanceof Tag) {
        objectNameData.put(key, new TagData((Tag) got));
      } else {
        objectNameData.put(key, TopicData.NULL);
      }
      return got;
    } else {
      return data.build();
    }

  }

  public Object getObjectNameById(String id, String type) throws Exception {

    ObjectNameKey key = new ObjectNameKey(id, type);
    CachedData data = objectNameData.get(key);

    if (data == null) {
      Object got = storage.getObjectNameById(id, type);
      if (got instanceof Post) {
        objectNameData.put(key, new PostData((Post) got));
      } else if (got instanceof Topic) {
        objectNameData.put(key, new TopicData((Topic) got));
      } else if (got instanceof Forum) {
        objectNameData.put(key, new ForumData((Forum) got));
      } else if (got instanceof Category) {
        objectNameData.put(key, new CategoryData((Category) got));
      } else if (got instanceof Tag) {
        objectNameData.put(key, new TagData((Tag) got));
      } else {
        objectNameData.put(key, TopicData.NULL);
      }
      return got;
    } else {
      return data.build();
    }

  }

  // TODO : need range
  public List<ForumLinkData> getAllLink(final String strQueryCate, final String strQueryForum) throws Exception {

    LinkListKey key = new LinkListKey(strQueryCate, strQueryForum);

    return buildLinkOutput(linkListDataFuture.get(
        new ServiceContext<ListLinkData>() {
          public ListLinkData execute() {
            try {
              List<ForumLinkData> got = storage.getAllLink(strQueryCate, strQueryForum);
              return buildLinkInput(got);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        },
        key
    ));

  }

  public List<ForumSearch> getQuickSearch(String textQuery, String type_, String pathQuery, String userId, List<String> listCateIds, List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception {
    return storage.getQuickSearch(textQuery, type_, pathQuery, userId, listCateIds, listForumIds, forumIdsOfModerator);
  }

  public List<ForumSearch> getAdvancedSearch(ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds) {
    return storage.getAdvancedSearch(eventQuery, listCateIds, listForumIds);
  }

  public void addWatch(int watchType, String path, List<String> values, String currentUser) throws Exception {
    storage.addWatch(watchType, path, values, currentUser);
    watchListData.remove(new SimpleCacheKey(null, currentUser));
    clearWatchingItemCache(path);
  }

  public void removeWatch(int watchType, String path, String values) throws Exception {
    storage.removeWatch(watchType, path, values);
    watchListData.select(new ScopeCacheSelector());
    clearWatchingItemCache(path);
  }

  public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId) throws Exception {
    storage.updateEmailWatch(listNodeId, newEmailAdd, userId);
    watchListData.remove(new SimpleCacheKey(null, userId));
    for (String id : listNodeId) {
      if (id.contains(Utils.CATEGORY)) {
        categoryData.remove(new CategoryKey(id));
      } else if (id.contains(Utils.FORUM)) {
        forumData.remove(new ForumKey((Forum) getObjectNameById(id, Utils.FORUM)));
      } else if (id.contains(Utils.TOPIC)) {
        topicData.remove(new TopicKey((Topic) getObjectNameById(id, Utils.TOPIC)));
      }
    }
  }

  // TODO : need range
  public List<Watch> getWatchByUser(final String userId) throws Exception {

    SimpleCacheKey key = new SimpleCacheKey(null, userId);

    return buildWatchOutput(watchListDataFuture.get(
      new ServiceContext<ListWatchData>() {
        public ListWatchData execute() {
          try {
            List<Watch> got = storage.getWatchByUser(userId);
            return buildWatchInput(got);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      },
      key
    ));

  }

  public void updateForum(String path) throws Exception {
    storage.updateForum(path);
  }

  public SendMessageInfo getMessageInfo(String name) throws Exception {
    return storage.getMessageInfo(name);
  }

  public Iterator<SendMessageInfo> getPendingMessages() throws Exception {
    return storage.getPendingMessages();
  }

  public List<ForumSearch> getJobWattingForModerator(String[] paths) {
    return storage.getJobWattingForModerator(paths);
  }

  public int getJobWattingForModeratorByUser(String userId) throws Exception {
    return storage.getJobWattingForModeratorByUser(userId);
  }

  public NodeIterator search(String queryString) throws Exception {
    return storage.search(queryString);
  }

  public void evaluateActiveUsers(String query) {
    storage.evaluateActiveUsers(query);
  }

  public Object exportXML(String categoryId, String forumId, List<String> objectIds, String nodePath, ByteArrayOutputStream bos, boolean isExportAll) throws Exception {
    return storage.exportXML(categoryId, forumId, objectIds, nodePath, bos, isExportAll);
  }

  public void importXML(String nodePath, ByteArrayInputStream bis, int typeImport) throws Exception {
    storage.importXML(nodePath, bis, typeImport);
    categoryList.select(new ScopeCacheSelector<CategoryListKey, ListCategoryData>());
    forumList.select(new ScopeCacheSelector());
    forumData.select(new ScopeCacheSelector());
  }

  public void updateTopicAccess(String userId, String topicId) {
    storage.updateTopicAccess(userId, topicId);
  }
  
  public void writeReads() {
    storage.writeReads();
  }

  public void updateForumAccess(String userId, String forumId) {
    storage.updateForumAccess(userId, forumId);
  }

  public List<String> getBookmarks(String userName) throws Exception {
    return storage.getBookmarks(userName);
  }

  public List<String> getBanList() throws Exception {
    return storage.getBanList();
  }

  public boolean isBanIp(String ip) throws Exception {
    return storage.isBanIp(ip);
  }

  public boolean addBanIP(String ip) throws Exception {
    return storage.addBanIP(ip);
  }

  public void removeBan(String ip) throws Exception {
    storage.removeBan(ip);
  }

  public List<String> getForumBanList(String forumId) throws Exception {
    return storage.getForumBanList(forumId);
  }

  public boolean addBanIPForum(String ip, String forumId) throws Exception {
    return storage.addBanIPForum(ip, forumId);
  }

  public void removeBanIPForum(String ip, String forumId) throws Exception {
    storage.removeBanIPForum(ip, forumId);
  }

  public void updateStatisticCounts(long topicCount, long postCount) throws Exception {
    storage.updateStatisticCounts(topicCount, postCount);
  }

  public PruneSetting getPruneSetting(String forumPath) throws Exception {
    return storage.getPruneSetting(forumPath);
  }

  public List<PruneSetting> getAllPruneSetting() throws Exception {
    return storage.getAllPruneSetting();
  }

  public void savePruneSetting(PruneSetting pruneSetting) throws Exception {
    storage.savePruneSetting(pruneSetting);
  }

  public void runPrune(String forumPath) throws Exception {
    storage.runPrune(forumPath);
  }

  public void runPrune(PruneSetting pSetting) throws Exception {
    storage.runPrune(pSetting);
  }

  public long checkPrune(PruneSetting pSetting) throws Exception {
    return storage.checkPrune(pSetting);
  }

  public List<TopicType> getTopicTypes() {
    return storage.getTopicTypes();
  }

  public TopicType getTopicType(String Id) throws Exception {
    return storage.getTopicType(Id);
  }

  public void saveTopicType(TopicType topicType) throws Exception {
    storage.saveTopicType(topicType);
  }

  public void removeTopicType(String topicTypeId) throws Exception {
    storage.removeTopicType(topicTypeId);
  }

  public JCRPageList getPageTopicByType(String type) throws Exception {
    return storage.getPageTopicByType(type);
  }

  public boolean populateUserProfile(User user, UserProfile profileTemplate, boolean isNew) throws Exception {
    return storage.populateUserProfile(user, profileTemplate, isNew);
  }

  public boolean deleteUserProfile(String userId) throws Exception {
    return storage.deleteUserProfile(userId);
  }

  public void calculateDeletedUser(String userName) throws Exception {
    storage.calculateDeletedUser(userName);
  }

  public void calculateDeletedGroup(String groupId, String groupName) throws Exception {
    storage.calculateDeletedGroup(groupId, groupName);
    forumData.select(new ScopeCacheSelector<ForumKey, ForumData>());
    forumList.select(new ScopeCacheSelector<ForumListKey, ListForumData>());
    categoryData.select(new ScopeCacheSelector<CategoryKey, CategoryData>());
    categoryList.select(new ScopeCacheSelector<CategoryListKey, ListCategoryData>());
  }

  public void initDataPlugin() throws Exception {
    storage.initDataPlugin();
  }

  public void initDefaultData() throws Exception {
    storage.initDefaultData();
  }

  public List<RoleRulesPlugin> getRulesPlugins() {
    return storage.getRulesPlugins();
  }

  public List<InitializeForumPlugin> getDefaultPlugins() {
    return storage.getDefaultPlugins();
  }

  public void initAutoPruneSchedules() throws Exception {
    storage.initAutoPruneSchedules();
  }

  public void updateLastLoginDate(String userId) throws Exception {
    storage.updateLastLoginDate(userId);
  }

  public String getLatestUser() throws Exception {
    return storage.getLatestUser();
  }

  public List<Post> getNewPosts(int number) throws Exception {
    return storage.getNewPosts(number);
  }

  public List<Post> getRecentPostsForUser(String userName, int number) throws Exception {
    return storage.getRecentPostsForUser(userName, number);
  }

  public Map<String, String> getServerConfig() {
    return storage.getServerConfig();
  }

  public KSDataLocation getDataLocation() {
    return storage.getDataLocation();
  }

  public void setViewCountTopic(String path, String userRead) {
    storage.setViewCountTopic(path, userRead);
  }
  
  public void writeViews() {
    storage.writeViews();
  }

  public JCRPageList getPostForSplitTopic(String topicPath) throws Exception {
    return storage.getPostForSplitTopic(topicPath);
  }

  public void movePost(String[] postPaths, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception {
    forumData.select(new ForumPathSelector(new String[] {Utils.getForumPath(postPaths[0]), Utils.getForumPath(destTopicPath)}, forumData));
    clearForumListCache();
    String srcTopicPath = null;
    
    for (String postPath : postPaths) {
      srcTopicPath = postPath.substring(0, postPath.lastIndexOf("/"));
      clearTopicCache(srcTopicPath);
      //
      Topic topic = getTopicByPath(srcTopicPath, false);
      clearPostListCountCache(topic.getId());
    }
    storage.movePost(postPaths, destTopicPath, isCreatNewTopic, mailContent, link);
    clearTopicCache(destTopicPath);
    //clear PostListCount caching.
    Topic topic = getTopicByPath(destTopicPath, false);
    clearPostListCountCache(topic.getId());
    clearPostListCache();
  }

  public void mergeTopic(String srcTopicPath, String destTopicPath, String mailContent, String link) throws Exception {
    storage.mergeTopic(srcTopicPath, destTopicPath, mailContent, link);
  }

  public void updateUserProfileInfo(String name) throws Exception {
    storage.updateUserProfileInfo(name);
  }

  public InputStream createForumRss(String objectId, String link) throws Exception {
    return storage.createForumRss(objectId, link);
  }

  public InputStream createUserRss(String userId, String link) throws Exception {
    return storage.createUserRss(userId, link);
  }

  public List<Post> getPosts(final PostFilter filter, final int offset, final int limit) throws Exception {

    PostListKey key = new PostListKey(filter, offset, limit);

    return buildPostOutput(postListFuture.get(
        new ServiceContext<ListPostData>() {
          public ListPostData execute() {
            try {
              List<Post> got = storage.getPosts(filter, offset, limit);
              return buildPostInput(got);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        },
        key
    ));

  }

  public int getPostsCount(final PostFilter filter) throws Exception {

    PostListCountKey key = new PostListCountKey("postsCount", filter.toString(), filter.getTopicId());

    SimpleCacheData<Integer> data = postListCountFuture.get(new ServiceContext<SimpleCacheData>() {
      public SimpleCacheData execute() {
        try {
          return new SimpleCacheData<Integer>(storage.getPostsCount(filter));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }, key);

    return data.build();

  }

 
}
