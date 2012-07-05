package org.exoplatform.forum.service.cache.model.selector;

import org.exoplatform.forum.service.cache.model.ScopeCacheKey;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ScopeCacheSelector<T extends ScopeCacheKey, U> implements CachedObjectSelector<T, U> {

  public boolean select(final T key, final ObjectCacheInfo<? extends U> ocinfo) {
    return ScopeCacheKey.getCurrentRepositoryName().equals(key.getScope());
  }

  public void onSelect(final ExoCache<? extends T, ? extends U> exoCache, final T key, final ObjectCacheInfo<? extends U> ocinfo) throws Exception {
    exoCache.remove(key);
  }

}