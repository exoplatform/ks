/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.rendering.macro.pagetree;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.wiki.rendering.macro.MacroUtils;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.rendering.macro.MacroExecutionException;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Jan 06, 2011  
 */
public class PageTreeMacroParameters {

  /**
   * Title of root page
   */
  private String root       = StringUtils.EMPTY;

  /**
   * Depth of tree page
   */
  private String startDepth = StringUtils.EMPTY;

  /**
   * Show excerpt or not
   */
  private boolean excerpt = false;  

  /**
   * @return root page of children
   */
  public String getRoot() {
    return root;
  }

  /**
   * @param root root of page tree
   */
  @PropertyDescription("Root of page tree. If not specified, current page is applied")
  public void setRoot(String root) {
    this.root = root;
  }

  /**
   * @return the depth of children
   */
  public String getStartDepth() {
    return startDepth;
  }

  /**
   * @param depth depth of children to set
   * @throws MacroExecutionException if parameter is not empty and not a number
   */
  @PropertyDescription("Start depth of children. If not specified, first level is applied")
  public void setStartDepth(String depth) throws MacroExecutionException {
    MacroUtils.validateNumberParam(depth);
    this.startDepth = depth;
  }

  /**
   * @return the value allow hide excerpt or not
   */
  public boolean isExcerpt() {
    return excerpt;
  }

  /**
   * @param hidden the value show excerpt to set
   */
  @PropertyDescription("Include Excerpts")
  public void setExcerpt(boolean excerpt) {
    this.excerpt = excerpt;
  }
}
