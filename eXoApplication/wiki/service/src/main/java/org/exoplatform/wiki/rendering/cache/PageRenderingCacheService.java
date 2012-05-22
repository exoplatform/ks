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
package org.exoplatform.wiki.rendering.cache;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.wiki.service.WikiPageParams;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * May 17, 2012  
 */
public interface PageRenderingCacheService {
  
  /**
   * Get rendered content of a wiki page
   * @param param the parameter to specify the wiki page
   * @param targetSyntax the syntax to be display
   * @return the rendered content
   */
  public String getRenderedContent(WikiPageParams param, String targetSyntax);
  
  /**
   * Get the rendering cache
   * @return the rendering cache
   */
  public ExoCache<MarkupKey, MarkupData> getRenderingCache();  
}
