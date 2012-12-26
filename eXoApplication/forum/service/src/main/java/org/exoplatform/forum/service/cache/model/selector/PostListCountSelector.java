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
package org.exoplatform.forum.service.cache.model.selector;

import org.exoplatform.ks.common.cache.model.ScopeCacheKey;
import org.exoplatform.ks.common.cache.model.selector.ScopeCacheSelector;
import org.exoplatform.forum.service.cache.model.key.PostListCountKey;
import org.exoplatform.services.cache.ObjectCacheInfo;


/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Oct 2, 2012  
 */
public class PostListCountSelector extends ScopeCacheSelector<ScopeCacheKey, Object> {

  private String topicId;

  public PostListCountSelector(final String topicId) {

    if (topicId == null) {
      throw new NullPointerException();
    }

    this.topicId = topicId;
  }

  @Override
  public boolean select(final ScopeCacheKey key, final ObjectCacheInfo<? extends Object> ocinfo) {
    if (!super.select(key, ocinfo)) {
      return false;
    }

    if (key instanceof PostListCountKey) {
      return ((PostListCountKey)key).getTopicId().equals(this.topicId);
    }
    
    return false;
  }

}

