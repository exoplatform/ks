/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.tree.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.rendering.macro.ExcerptUtils;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.tree.JsonNodeData;
import org.exoplatform.wiki.tree.PageTreeNode;
import org.exoplatform.wiki.tree.SpaceTreeNode;
import org.exoplatform.wiki.tree.TreeNode;
import org.exoplatform.wiki.tree.TreeNodeType;
import org.exoplatform.wiki.tree.WikiHomeTreeNode;
import org.exoplatform.wiki.tree.WikiTreeNode;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 7 Jan 2011  
 */
public class TreeUtils {
  
  /**
   * Create a tree node with a given {@link WikiPageParams}
   * 
   * @param params is the wiki page parameters
   * @return <code>TreeNode</code>
   * @throws Exception
   */
  public static TreeNode getTreeNode(WikiPageParams params) throws Exception {
    Object wikiObject = Utils.getObjectFromParams(params);
    if (wikiObject instanceof WikiHome) {
      WikiHome wikiHome = (WikiHome) wikiObject;
      WikiHomeTreeNode wikiHomeNode = new WikiHomeTreeNode(wikiHome);
      return wikiHomeNode;
    } else if (wikiObject instanceof Page) {
      PageImpl page = (PageImpl) wikiObject;
      PageTreeNode pageNode = new PageTreeNode(page);
      return pageNode;
    } else if (wikiObject instanceof Wiki) {
      Wiki wiki = (Wiki) wikiObject;
      WikiTreeNode wikiNode = new WikiTreeNode(wiki);
      return wikiNode;
    } else if (wikiObject instanceof String) {
      SpaceTreeNode spaceNode = new SpaceTreeNode((String) wikiObject);
      return spaceNode;
    }
    return new TreeNode();
  }
  
  /**
   * Create a tree node contain all its descendant with a given {@link WikiPageParams} 
   * 
   * @param params is the wiki page parameters
   * @param context is the page tree context
   * @return <code>TreeNode</code>
   * @throws Exception
   */
  public static TreeNode getDescendants(WikiPageParams params, HashMap<String, Object> context) throws Exception {
    TreeNode treeNode = getTreeNode(params);
    treeNode.pushDescendants(context);
    return treeNode;
  }
  
  public static List<JsonNodeData> tranformToJson(TreeNode treeNode, HashMap<String, Object> context) throws Exception {
    WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer()
                                                               .getComponentInstanceOfType(WikiService.class);
    List<JsonNodeData> children = new ArrayList<JsonNodeData>();
    int counter = 1;
    boolean isSelectable = true;
    boolean isLastNode = false;
    Boolean showExcerpt = false;
    String excerpt = null;
    PageImpl page = null;
    PageImpl currentPage = null;
    WikiPageParams currentPageParams = null;
    String currentPath = null;
    if (context != null) {
      currentPath = (String) context.get(TreeNode.CURRENT_PATH);
      showExcerpt = (Boolean) context.get(TreeNode.SHOW_EXCERPT);
    }
    currentPageParams = getPageParamsFromPath(currentPath);

    for (TreeNode child : treeNode.getChildren()) {
      isSelectable = true;
      isLastNode = false;
      if (counter >= treeNode.getChildren().size()) {
        isLastNode = true;
      }
      // if (child.getNodeType().equals(TreeNodeType.WIKIHOME)) { isSelectable =
      // true;}
      if (child.getNodeType().equals(TreeNodeType.WIKI)) {
        isSelectable = false;
      } else if (currentPath != null && child.getNodeType().equals(TreeNodeType.PAGE)) {
        page = ((PageTreeNode) child).getPage();
        currentPage = (PageImpl) wikiService.getPageById(currentPageParams.getType(),
                                                         currentPageParams.getOwner(),
                                                         currentPageParams.getPageId());
        if (currentPage != null
            && (currentPage.equals(page) || Utils.isDescendantPage(page, currentPage)))
          isSelectable = false;
      }
      if (showExcerpt != null && showExcerpt) {
        WikiPageParams params = getPageParamsFromPath(child.getPath());
        excerpt = ExcerptUtils.getExcerpts(params);
      }
      children.add(new JsonNodeData(child, isLastNode, isSelectable, currentPath, excerpt, context));
      counter++;
    }
    return children;
  }
  

  
  public static WikiPageParams getPageParamsFromPath(String path) throws Exception {
    if (path == null) {
      return null;
    }
    WikiPageParams result = new WikiPageParams();
    path = path.trim();
    if (path.indexOf("/") < 0) {
      result.setType(path);
    } else {
      String[] array = path.split("/");
      result.setType(array[0]);
      if (array.length < 3) {
        result.setOwner(array[1]);
      } else if (array.length >= 3) {
        if (array[0].equals(PortalConfig.GROUP_TYPE)) {
          OrganizationService oService = (OrganizationService) ExoContainerContext.getCurrentContainer()
                                                                                  .getComponentInstanceOfType(OrganizationService.class);
          String groupId = path.substring(path.indexOf("/"));
          if (oService.getGroupHandler().findGroupById(groupId) != null) {
            result.setOwner(groupId);
          } else {
            result.setPageId(path.substring(path.lastIndexOf("/") + 1));
            String owner = path.substring(path.indexOf("/"), path.lastIndexOf("/"));
            while (oService.getGroupHandler().findGroupById(owner) == null) {
              owner = owner.substring(0,owner.lastIndexOf("/"));
            }
            result.setOwner(owner);
          }
        } else {
          // if (array[0].equals(PortalConfig.PORTAL_TYPE) || array[0].equals(PortalConfig.USER_TYPE))
          result.setOwner(array[1]);
          result.setPageId(array[array.length-1]);
        }
      }
    }
    return result;
  }
 
  public static String getPathFromPageParams(WikiPageParams param) {
    StringBuilder sb = new StringBuilder();
    if (param.getType() != null) {
      sb.append(param.getType());
    }
    if (param.getOwner() != null) {
      sb.append("/").append(Utils.validateWikiOwner(param.getType(), param.getOwner()));
    }
    if (param.getPageId() != null) {
      sb.append("/").append(param.getPageId());
    }
    return sb.toString();
  }
}
