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
package org.exoplatform.wiki.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.tree.utils.TreeUtils;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 6, 2010  
 */
public class PageTreeNode extends TreeNode {
  private PageImpl page;

  public PageTreeNode(PageImpl page) throws Exception {
    super(page.getTitle(), TreeNodeType.PAGE);
    this.page = page;
    this.path = buildPath();
    this.hasChild = this.page.getChildPages().size() > 0;
  }

  public PageImpl getPage() {
    return page;
  }

  public void setPage(PageImpl page) {
    this.page = page;
  }

  @Override
  protected void addChildren(HashMap<String, Object> context) throws Exception {

  	Collection<PageImpl> pages = page.getChildrenByRootPermission().values();
    Iterator<PageImpl> childPageIterator = pages.iterator();
    int count = 0;
    int size = getNumberOfChildren(context, pages.size());
    PageImpl currentPage = (PageImpl) context.get(TreeNode.SELECTED_PAGE);
    while (childPageIterator.hasNext() && count < size) {
    	PageImpl childPage = childPageIterator.next();
    	if (childPage.hasPermission(PermissionType.VIEWPAGE) ||  (currentPage != null && Utils.isDescendantPage(currentPage, childPage))) {
    		PageTreeNode child = new PageTreeNode(childPage);
    		this.children.add(child);
    	}
      count++;
    }
    super.addChildren(context);
  }

  public PageTreeNode getChildByName(String name) throws Exception {
    for (TreeNode child : children) {
      if (child.getName().equals(name))
        return (PageTreeNode) child;
    }
    return null;
  }
  
  @Override
  public String buildPath() {
    Wiki wiki = (Wiki) this.page.getWiki();
    WikiPageParams params = new WikiPageParams(wiki.getType(), wiki.getOwner(),this.page.getName() );
    return TreeUtils.getPathFromPageParams(params);    
  }

}
