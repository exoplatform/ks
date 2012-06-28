/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.ks.common.cache.loader;

import org.exoplatform.ks.common.cache.ScopeCacheKey;
import org.exoplatform.services.cache.future.Loader;

import java.io.Serializable;

/**
 * Used by FutureCache to execute the ServiceContext.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CacheLoader<K extends ScopeCacheKey, V extends Serializable> implements Loader<K, V, ServiceContext<V>> {

  /**
   * Execute the context.
   *
   * @param cacheContext
   * @param key
   * @return
   * @throws Exception
   */
  public V retrieve(final ServiceContext<V> cacheContext, final K key) throws Exception {
    return cacheContext.execute();
  }
  
}
