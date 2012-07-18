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

import java.io.Serializable;

import org.exoplatform.wiki.service.WikiPageParams;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * May 16, 2012  
 */
public class MarkupKey implements Serializable {
  
  private WikiPageParams pageParams;

  private String         source;

  private String         sourceSyntax;

  private String         targetSyntax;

  private boolean        supportSectionEdit;

  /**
   * Instance new markup key
   *
   * @param pageParams the identity params of page
   * @param source the source markup
   * @param sourceSyntax the source syntax
   * @param targetSyntax the target syntax
   * @param supportSectionEdit the content supports section editing or not
   */
  public MarkupKey(WikiPageParams pageParams, String source, String sourceSyntax, String targetSyntax, boolean supportSectionEdit) {
    this.pageParams = pageParams;
    this.source = source;
    this.sourceSyntax = sourceSyntax;
    this.targetSyntax = targetSyntax;
    this.supportSectionEdit = supportSectionEdit;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((pageParams == null) ? 0 : pageParams.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
    result = prime * result + ((sourceSyntax == null) ? 0 : sourceSyntax.hashCode());
    result = prime * result + (supportSectionEdit ? 1231 : 1237);
    result = prime * result + ((targetSyntax == null) ? 0 : targetSyntax.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MarkupKey other = (MarkupKey) obj;
    if (pageParams == null) {
      if (other.pageParams != null)
        return false;
    } else if (!pageParams.equals(other.pageParams))
      return false;
    if (source == null) {
      if (other.source != null)
        return false;
    } else if (!source.equals(other.source))
      return false;
    if (sourceSyntax == null) {
      if (other.sourceSyntax != null)
        return false;
    } else if (!sourceSyntax.equals(other.sourceSyntax))
      return false;
    if (supportSectionEdit != other.supportSectionEdit)
      return false;
    if (targetSyntax == null) {
      if (other.targetSyntax != null)
        return false;
    } else if (!targetSyntax.equals(other.targetSyntax))
      return false;
    return true;
  }

  /**
   * @return the supportSectionEdit
   */
  public boolean isSupportSectionEdit() {
    return supportSectionEdit;
  }

  /**
   * @param supportSectionEdit the supportSectionEdit to set
   */
  public void setSupportSectionEdit(boolean supportSectionEdit) {
    this.supportSectionEdit = supportSectionEdit;
  }
  
}
