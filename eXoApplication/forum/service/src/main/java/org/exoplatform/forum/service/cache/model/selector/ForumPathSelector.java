package org.exoplatform.forum.service.cache.model.selector;

import org.exoplatform.forum.service.cache.model.ScopeCacheKey;
import org.exoplatform.forum.service.cache.model.data.ForumData;
import org.exoplatform.forum.service.cache.model.key.ForumKey;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ForumPathSelector extends ScopeCacheSelector<ScopeCacheKey, Object> {

  private final String[] paths;
  private final ExoCache<ForumKey, ForumData> forumData;

  public ForumPathSelector(String[] paths, ExoCache<ForumKey, ForumData> forumData) {

    if (paths == null) {
      throw new NullPointerException();
    }

    if (forumData == null) {
      throw new NullPointerException();
    }

    this.paths = paths;
    this.forumData = forumData;
  }

  @Override
  public boolean select(ScopeCacheKey key, ObjectCacheInfo<? extends Object> ocinfo) {
    
    if (!super.select(key, ocinfo)) {
      return false;
    }

    ForumData data = forumData.get(key);
    if (data == null || data == ForumData.NULL) {
      return false;
    } else {
      for (String path : paths) {
        if (data.getPath().endsWith(path)) {
          return true;
        }
      }
      return false;
    }

  }
  
}
