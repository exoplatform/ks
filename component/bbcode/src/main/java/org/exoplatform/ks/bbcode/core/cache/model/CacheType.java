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
package org.exoplatform.ks.bbcode.core.cache.model;

import java.io.Serializable;

import org.exoplatform.ks.common.cache.CacheLoader;
import org.exoplatform.ks.common.cache.ServiceContext;
import org.exoplatform.ks.common.cache.model.ScopeCacheKey;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.future.FutureExoCache;

public enum CacheType {
  BBCODE_DATA("forum.BBCodeData"),
  LIST_BBCODE_DATA("forum.ListBBCodeData");
  
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
