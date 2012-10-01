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
package org.exoplatform.wiki.service;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.wiki.tree.TreeNode.TREETYPE;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 20, 2010  
 */
public interface WikiRestService {

  /**
   * Get the contain of wiki page
   * 
   * @param sessionKey key is used to retrieve the editor input value from the session.
   * @param isMarkup if <em>true</em> then <em>markup content</em> is returned else <em>html content</em> is returned
   * @return Status.OK and the page contain as TEXT_HTML
   *         Status.INTERNAL_SERVER_ERROR if the exception occur when get the contain of wiki page
   */
  Response getWikiPageContent(String sessionKey, String wikiContextKey, boolean isMarkup, String data);
  
  /**
   * Update a file to the wiki page that specify by the parameters
   * 
   * @param wikiType The wiki type
   * @param wikiOwner The wiki owner
   * @param pageId The page id of wiki page
   * @return Status.OK if upload success 
   *         HTTPStatus.BAD_REQUEST if upload fail
   */
  Response upload(String wikiType, String wikiOwner, String pageId);
  
  /**
   * Get data to create page tree
   * 
   * @param type Tree type {@link TREETYPE}
   * @param path Jcr node path of root node
   * @param currentPath Jcr node path of current selected node
   * @param showExcerpt Is show the summary of page or not
   * @param depth Start depth to show the tree node
   * @return Status.OK and the tree data as APPLICATION_JSON
   *         Status.INTERNAL_SERVER_ERROR if the exception occur when get tree data
   */
  Response getTreeData(String type, String path, String currentPath, Boolean showExcerpt, String depth);
  
  /**
   * Get the list of relate page of wiki page
   * 
   * @param path Jcr path of wiki page that will get the related page from
   * @return Status.OK and JsonRelatedData as APPLICATION_JSON
   *         Status.NOT_FOUND if can not find the wiki page by input jcr path
   *         Status.INTERNAL_SERVER_ERROR if the exception occur when get related page
   */
  Response getRelated(String path);
  
  /**
   * Search through the title of wiki pages by keyword
   * 
   * @param keyword The keyword to search
   * @param wikiType The wiki type
   * @param wikiOwner The wiki owner
   * @return Status.OK and search result as APPLICATION_JSON
   *         Status.INTERNAL_SERVER_ERROR if the exception occur when search page
   */
  Response searchData(String keyword, String wikiType, String wikiOwner) throws Exception;
  
  /**
   * Get an image form wiki page
   *  
   * @param uriInfo The base url info
   * @param wikiType The wiki type
   * @param wikiOwner The wiki owner
   * @param pageId Wiki page id
   * @param imageId The image id
   * @param width The width of image
   * @return Status.OK and the image after resize
   *         Status.INTERNAL_SERVER_ERROR if the exception occur when get image
   */
  Response getImage(UriInfo uriInfo, String wikiType, String wikiOwner, String pageId, String imageId, Integer width);
  
  /**
   * Return the help syntax page.
   * The syntax id have to replaced all special characters: 
   *  Character '/' have to replace to "SLASH"
   *  Character '.' have to replace to "DOT"
   *
   * Sample:
   * "confluence/1.0" will be replaced to "confluenceSLASH1DOT0"
   *  
   * @param syntaxId The id of syntax to show in help page
   * @return Status.OK and help content as TEXT_HTML
   *         Status.INTERNAL_SERVER_ERROR if the exception occur when get help content
   */
  Response getHelpSyntaxPage(String syntaxId);
}
