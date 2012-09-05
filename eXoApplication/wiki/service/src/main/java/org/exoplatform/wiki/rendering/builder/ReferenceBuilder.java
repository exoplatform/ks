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
package org.exoplatform.wiki.rendering.builder;

import org.exoplatform.wiki.service.WikiPageParams;
import org.xwiki.component.annotation.Role;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Oct 27, 2011  
 */
@Role
public interface ReferenceBuilder {

  public static final String wikiSpaceSeparator = ":";

  public static final String spacePageSeparator = ".";
  
  /**
   * Build reference for a wiki page up on a given param
   * @param params the wiki page param
   * @return the document reference
   */
  public String build(WikiPageParams params);

}
