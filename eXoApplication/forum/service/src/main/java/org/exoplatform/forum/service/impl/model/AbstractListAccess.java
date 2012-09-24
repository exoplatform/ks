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
package org.exoplatform.forum.service.impl.model;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.forum.service.DataStorage;

/**
 * Created by The eXo Platform SAS Author : thanh_vucong
 * thanh_vucong@exoplatform.com Sep 17, 2012
 * @since 2.2.11
 */
public abstract class AbstractListAccess<E> implements ListAccess<E> {

  protected DataStorage storage     = null;

  private int           currentPage = 1;

  private int           totalPage   = 1;

  private int           pageSize    = 0;

  protected int         size        = -1;
  
  public abstract E[] load(int pageSelect) throws Exception, IllegalArgumentException;

  public void reCalculate(int offset, int limit) {
    if (offset >= 0) {
      currentPage = (offset + 1) / limit;
      if ((offset + 1) % limit > 0)
        currentPage++;
    }
  }

  public void initialize(int pageSize, int pageSelect) throws Exception {
    this.setPageSize(pageSize);
    this.getTotalPages();   
    this.setCurrentPage(pageSelect);  
  }
  
  public int getTotalPages() throws Exception {
    this.totalPage = getSize() / pageSize;
    if (getSize() % pageSize > 0)
      this.totalPage++;
    return this.totalPage;
  }

  public int getPageSize() {
    return this.pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public int getCurrentPage() throws Exception {
    return currentPage;
  }

  public void setCurrentPage(int page) throws Exception {
    if (page > totalPage && totalPage > 0) {
      currentPage = totalPage;
    } else if (page <= 0) {
      currentPage = 1;
    } else {
      currentPage = page;
    }
  }

  public int getOffset(int currentPage) {
    return (currentPage - 1) * pageSize;
  }

  public int getOffset(int currentPage, int pageSize) {
    return (this.currentPage - 1) * this.pageSize;
  }

  public int getOffset() {
    return (this.currentPage - 1) * this.pageSize;
  }
}