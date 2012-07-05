/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.forum.service.cache.model.selector;

import java.util.List;

import org.exoplatform.forum.service.cache.model.ScopeCacheKey;
import org.exoplatform.forum.service.cache.model.data.CategoryData;
import org.exoplatform.forum.service.cache.model.key.CategoryKey;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 30, 2012  
 */
public class CategoryIdSelector extends ScopeCacheSelector<ScopeCacheKey, Object> {

  private final List<String>                        cateIds;

  private final ExoCache<CategoryKey, CategoryData> selector;

  public CategoryIdSelector(List<String> cateIds, ExoCache<CategoryKey, CategoryData> selector) {
    if (cateIds == null) {
      throw new NullPointerException();
    }
    if (selector == null) {
      throw new NullPointerException();
    }
    this.cateIds = cateIds;
    this.selector = selector;
  }

  @Override
  public boolean select(ScopeCacheKey key, ObjectCacheInfo<? extends Object> ocinfo) {

    if (!super.select(key, ocinfo)) {
      return false;
    }

    CategoryData data = selector.get(key);
    if (data == null) {
      return false;
    } else {
      for (String path : cateIds) {
        if (data.getId().equals(path)) {
          return true;
        }
      }
      return false;
    }

  }
}