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

import java.util.List;

import org.exoplatform.forum.service.DataStorage;
import org.exoplatform.forum.service.Post;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Sep 13, 2012  
 * @since 2.2.11
 */
public class PostListAccess extends AbstractListAccess<Post> {

  private PostFilter  filter = null;
  
  private Type type;
  
  public enum Type {
    POSTS
  }
  
  public PostListAccess(Type type, DataStorage  storage, PostFilter filter) {
    this.storage = storage;
    this.filter = filter;
    this.type = type;
  }
  
  @Override
  public Post[] load(int offset, int limit) throws Exception, IllegalArgumentException {
    List<Post> got = null;
    
    switch(type) {
      case POSTS :
        got = storage.getPosts(filter, offset, limit);
        break;     
    }
    //
    reCalculate(offset, limit);
    
    return got.toArray(new Post[got.size()]);
  
  }
  
  @Override
  public int getSize() throws Exception {
    if (size < 0) {
      switch(type) {
        case POSTS :
          size = storage.getPostsCount(filter);
          break;
      }
    }
    return size;
  }

  public PostFilter getFilter() {
    return filter;
  }

  @Override
  public Post[] load(int pageSelect) throws Exception, IllegalArgumentException {
    int offset = getOffset(pageSelect);
    int limit = getPageSize();
    return load(offset, limit);
  }
  
}