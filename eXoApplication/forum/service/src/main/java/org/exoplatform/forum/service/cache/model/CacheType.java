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
  CATEGORY_DATA("CategoryData"),
  CATEGORY_LIST("CategoryList"),
  FORUM_DATA("ForumData"),
  FORUM_LIST("ForumList"),
  TOPIC_DATA("TopicData"),
  TOPIC_LIST("TopicList"),
  WATCH_LIST_DATA("WatchListData"),
  LINK_LIST_DATA("LinkListData"),
  OBJECT_NAME_DATA("ObjectNameData"),
  MISC_DATA("MiscData")

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
