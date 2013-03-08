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
import java.util.List;

import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
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
public class WikiHomeTreeNode extends TreeNode {
  private WikiHome           wikiHome;  

  public WikiHomeTreeNode(WikiHome wikiHome) throws Exception {
    super(wikiHome.getTitle(), TreeNodeType.WIKIHOME);
    this.wikiHome = wikiHome;
    this.path = this.buildPath();
    this.hasChild = wikiHome.getChildPages().size() > 0;
  }

  @Override
  protected void addChildren(HashMap<String, Object> context) throws Exception {
  	Collection<PageImpl> pages = wikiHome.getChildrenByRootPermission().values();
    Iterator<PageImpl> childPageIterator = pages.iterator();
    int count=0;
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

  public WikiHome getWikiHome() {
    return wikiHome;
  }

  public PageTreeNode getChildByName(String name) throws Exception {
    for (TreeNode child : children) {
      if (child.getName().equals(name))
        return (PageTreeNode)child;
    }
    return null;
  }

  public PageTreeNode findDescendantNodeByName(List<TreeNode> listPageTreeNode, String name) throws Exception {
    for (TreeNode pageTreeNode : listPageTreeNode) {
      if (pageTreeNode.getName().equals(name)) {
        return (PageTreeNode)pageTreeNode;
      } else {
        List<TreeNode> listChildPageTreeNode =  pageTreeNode.getChildren();
        if (listChildPageTreeNode.size() > 0) {
          return (PageTreeNode)findDescendantNodeByName(listChildPageTreeNode, name);
        }
      }
    }
    return null;
  }
  
  @Override
  public String buildPath() {
    Wiki wiki = this.wikiHome.getWiki();
    WikiPageParams params = new WikiPageParams(wiki.getType(), wiki.getOwner(),WikiNodeType.Definition.WIKI_HOME_NAME );
    return TreeUtils.getPathFromPageParams(params);
  }
}
