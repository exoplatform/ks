package org.exoplatform.forum.service.cache.model;

import org.exoplatform.forum.service.cache.loader.CacheLoader;
import org.exoplatform.forum.service.cache.loader.ServiceContext;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.future.FutureExoCache;

import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public enum CacheType {
  CATEGORY_DATA("forum.CategoryData"),
  CATEGORY_LIST("forum.CategoryList"),
  FORUM_DATA("forum.ForumData"),
  FORUM_LIST("forum.ForumList"),
  TOPIC_DATA("forum.TopicData"),
  TOPIC_LIST("forum.TopicList"),
  WATCH_LIST_DATA("forum.WatchListData"),
  LINK_LIST_DATA("forum.LinkListData"),
  OBJECT_NAME_DATA("forum.ObjectNameData"),
  MISC_DATA("forum.MiscData"),
  
  FAQ_MARKUP("faq.Markup"),

  POLL_DATA("poll.PollData"),
  POLL_LIST("poll.PollList"),
  POLL_SUMMARY_DATA("poll.PollSummaryData")

  ;
  
  private final String name;

  private CacheType(final String name) {
    this.name = name;
  }

  public <K extends ScopeCacheKey, V extends Serializable> ExoCache<K, V> getFromService(CacheService service) {
    return service.getCacheInstance(name);
  }

  public <K extends ScopeCacheKey, V extends Serializable> FutureExoCache<K, V, ServiceContext<V>> createFutureCache(
      ExoCache<K, V> cache) {

    return new FutureExoCache<K, V, ServiceContext<V>>(new CacheLoader<K, V>(), cache);

  }
}
