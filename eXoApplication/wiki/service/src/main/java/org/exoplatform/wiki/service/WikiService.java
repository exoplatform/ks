/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.Template;
import org.exoplatform.wiki.mow.core.api.wiki.TemplateContainer;
import org.exoplatform.wiki.service.listener.PageWikiListener;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.service.search.TemplateSearchData;
import org.exoplatform.wiki.service.search.TemplateSearchResult;
import org.exoplatform.wiki.service.search.TitleSearchResult;
import org.exoplatform.wiki.service.search.WikiSearchData;
import org.exoplatform.wiki.template.plugin.WikiTemplatePagePlugin;

/**
 * Created by The eXo Platform SARL.
 * <p>
 * WikiService is interface provide functions for processing database
 * with wikis and pages include: add, edit, remove and searching data
 * 
 * @author  exoplatform
 * @since   Mar 04, 2010
 */
public interface WikiService {

  /**
   * Create a wiki page that specify by parameters
   * 
   * @param wikiType wiki type
   * @param wikiOwner The owner of page
   * @param title The title of new page
   * @param parentId The pageIf of parent page
   * @return new wiki page
   * @throws Exception
   */
  public Page createPage(String wikiType, String wikiOwner, String title, String parentId) throws Exception;

  /**
   * Create new template that specify by parameters
   * 
   * @param title The title of template
   * @param params The param to specify the place to create draft
   * @return New draft page
   * @throws Exception
   */
  public Template createTemplatePage(String title, WikiPageParams params) throws Exception;

  /**
   * Init deafault templates of wiki 
   * 
   * @param path The jcr path where create default template on
   */
  public void initDefaultTemplatePage(String path) ;
  
  /**
   * Create a draft page for new wiki page
   * 
   * @param draftNewPageId The id of draft page
   * @throws Exception
   */
  public void createDraftNewPage(String draftNewPageId) throws Exception;

  /**
   * Delete the wiki page that specify by the parameters
   * 
   * @param wikiType The wiki type
   * @param wikiOwner The wiki owner
   * @param pageId The id of page to delete
   * @return delete page is successful or not
   * @throws Exception
   */
  public boolean deletePage(String wikiType, String wikiOwner, String pageId) throws Exception;

  /**
   * Delete a template that specify by the parameters
   * 
   * @param wikiType The wiki type
   * @param wikiOwner The owner
   * @param templateId Template id of template to delete
   * @throws Exception
   */
  public void deleteTemplatePage(String wikiType, String wikiOwner, String templateId) throws Exception;

  /**
   * Delete a draft page that specify by the parameter
   * 
   * @param draftNewPageId The id of draft page to delete
   * @throws Exception
   */
  public void deleteDraftNewPage(String draftNewPageId) throws Exception;

  /**
   * Rename the wiki page that specify by the parameters 
   *  
   * @param wikiType The wiki type
   * @param wikiOwner The owner
   * @param pageName The name of wiki page to rename
   * @param newName New name of wiki page
   * @param newTitle New title of wiki page
   * @return Rename page is successful for not
   * @throws Exception
   */
  public boolean renamePage(String wikiType, String wikiOwner, String pageName, String newName, String newTitle) throws Exception;

  /**
   * Move a wiki page to another location
   * 
   * @param currentLocationParams Specify the current location of wiki page
   * @param newLocationParams Specify the new locataion to move to
   * @return Move page is successful or not
   * @throws Exception
   */
  public boolean movePage(WikiPageParams currentLocationParams, WikiPageParams newLocationParams) throws Exception;

  /**
   * Get the permission of wiki
   * 
   * @param wikiType The wiki type
   * @param wikiOwner The wiki owner
   * @return List of permssion entry
   * @throws Exception
   */
  public List<PermissionEntry> getWikiPermission(String wikiType, String wikiOwner) throws Exception;

  /**
   * Set the permisson for wiki
   * 
   * @param wikiType The wiki type
   * @param wikiOwner The wiki owner
   * @param permissionEntries The list of permission entry to set to permissions of wiki
   * @throws Exception
   */
  public void setWikiPermission(String wikiType, String wikiOwner, List<PermissionEntry> permissionEntries) throws Exception;

  /**
   * Get the wiki page that specify by parameters
   *  
   * @param wikiType The wiki type
   * @param wikiOwner The wiki owner
   * @param pageId The id of wiki page
   * @return The wiki page
   * @throws Exception
   */
  public Page getPageById(String wikiType, String wikiOwner, String pageId) throws Exception;

  /**
   * Get a page that renamed by the old id
   * After rename, the wiki page was changed the page id. The old id of wiki page is stored to the link registry of wiki.
   * This function provides the way to get the wiki page by its old id.
   * 
   * @param wikiType The wiki type
   * @param wikiOwner The wiki owner
   * @param pageId The old page id of wiki page
   * @return The wiki page
   * @throws Exception
   */
  public Page getRelatedPage(String wikiType, String wikiOwner, String pageId) throws Exception;

  /**
   * Get a wiki page or create a draft for it if it's not exist
   * 
   * @param wikiType The wiki type
   * @param wikiOwner The wiki owner
   * @param pageId The page Id
   * @return The wiki page for the draft page
   * @throws Exception
   */
  public Page getExsitedOrNewDraftPageById(String wikiType, String wikiOwner, String pageId) throws Exception;

  /**
   * Get a template that specify by parameters
   * 
   * @param params The location of template
   * @param templateId The id of template
   * @return The template
   * @throws Exception
   */
  public Template getTemplatePage(WikiPageParams params, String templateId) throws Exception;

  /**
   * Search through the content of wiki pages and return search result
   * 
   * @param data The wiki search data that contain search query
   * @return The search result
   * @throws Exception
   */
  public PageList<SearchResult> searchContent(WikiSearchData data) throws Exception;

  /**
   * Get list of breadcrum data of wiki page
   * 
   * @param wikiType Wiki type
   * @param wikiOwner Wiki owner
   * @param pageId The page id
   * @return The list of breadcrum data
   * @throws Exception
   */
  public List<BreadcrumbData> getBreadcumb(String wikiType, String wikiOwner, String pageId) throws Exception;

  /**
   * Get the wiki page param from Breadcrum data
   * 
   * @param data The breadcrum data
   * @return The wiki page param
   * @throws Exception
   */
  public WikiPageParams getWikiPageParams(BreadcrumbData data) throws Exception;

  /**
   * Search for wiki pages that match the search query in input search data
   * 
   * @param data The Wiki Search data that contain the search query
   * @return The search result
   * @throws Exception
   */
  public PageList<SearchResult> search(WikiSearchData data) throws Exception;

  /**
   * Search for wiki templates that match the search query in input search data
   * 
   * @param data The Wiki Search data that contain the search query
   * @return The search result
   * @throws Exception
   */
  public List<TemplateSearchResult> searchTemplate(TemplateSearchData data) throws Exception;

  /**
   * Get the list of wiki page that renamed
   * 
   * @param wikiType The wiki type
   * @param wikiOwner The wiki owner
   * @param pageId The wiki page id
   * @return The list of wiki page that renamed
   * @throws Exception
   */
  @Deprecated
  public List<SearchResult> searchRenamedPage(String wikiType, String wikiOwner, String pageId) throws Exception;

  /**
   * Search through the title of wiki pages and return search result
   * 
   * @param data The wiki search data that contain search query
   * @return The sreach result
   * @throws Exception
   */
  public List<TitleSearchResult> searchDataByTitle(WikiSearchData data) throws Exception;

  /**
   * Get a wiki object (PageImpl, AttachmentImpl, Template) by jcr path
   * 
   * @param path The jcr node path to get the object
   * @param objectNodeType The node type of jcr node
   * @return The Object that's gotten from jcr node 
   * @throws Exception
   */
  public Object findByPath(String path, String objectNodeType) throws Exception;

  /**
   * Get default wiki syntax id
   * 
   * @return The default wiki syntax id
   */
  public String getDefaultWikiSyntaxId();

  /**
   * Get the page title of an attachment
   *  
   * @param path The jcr path of the attachment
   * @return The page title
   * @throws Exception
   */
  public String getPageTitleOfAttachment(String path) throws Exception;

  /**
   * Get the attachment as stream
   * 
   * @param path The path of attachment
   * @return The input stream
   * @throws Exception
   */
  public InputStream getAttachmentAsStream(String path) throws Exception;

  /**
   * Get the wiki page that contain the syntax help content
   * 
   * @param syntaxId The syntax id to get help page
   * @return The wiki page that content the syntax help content
   * @throws Exception
   */
  public PageImpl getHelpSyntaxPage(String syntaxId) throws Exception;
  
  /**
   * Get the wiki page that contain the metadata infomation
   * 
   * @param metaPage The type of Metadata
   * @return The wiki page that contain the metadata infomation
   * @throws Exception
   */
  public Page getMetaDataPage(MetaDataPage metaPage) throws Exception;

  /**
   * Get the map of template that specify by the parameters
   * 
   * @param params The location of templates
   * @return The map of templates 
   * @throws Exception
   */
  public Map<String, Template> getTemplates(WikiPageParams params) throws Exception;

  /**
   * Get template container that specify by the parameters
   * 
   * @param params The location of template container
   * @return The template container
   * @throws Exception
   */
  public TemplateContainer getTemplatesContainer(WikiPageParams params) throws Exception;

  /**
   * Modify the wiki tempate
   * 
   * @param params The location of template
   * @param template The template to modify
   * @param newName The new name of template
   * @param newDescription The new description of template
   * @param newContent The new contain of template
   * @param newSyntaxId The new syntax id of template
   * @throws Exception
   */
  public void modifyTemplate(WikiPageParams params, Template template, String newName, String newDescription, String newContent, String newSyntaxId) throws Exception;

  /**
   * Check if the wiki page exist for not 
   * 
   * @param wikiType The wiki type
   * @param wikiOwner The wiki owner
   * @param pageId The page id
   * @return The wiki page exist or not
   * @throws Exception
   */
  public boolean isExisting(String wikiType, String wikiOwner, String pageId) throws Exception;

  /**
   * register a {@link PageWikiListener} 
   * @param listener
   */
  public void addComponentPlugin(ComponentPlugin plugin);

  /**
   * Add wiki template plugin
   * 
   * @param templatePlugin The template plugin
   */
  public void addWikiTemplatePagePlugin(WikiTemplatePagePlugin templatePlugin);

  /**
   * Get page listener
   * 
   * @return list of {@link PageWikiListener}
   */
  public List<PageWikiListener> getPageListeners();

  /**
   * Add relate page
   * 
   * @param orginaryPageParams The param of the target page to add related page to
   * @param relatedPageParams The param of the related page
   * @return Add related page is success or not
   * @throws Exception
   */
  public boolean addRelatedPage(WikiPageParams orginaryPageParams, WikiPageParams relatedPageParams) throws Exception;

  /**
   * Get the list of related page of wiki page
   * 
   * @param pageParams The param refer to wiki page
   * @return The list of related page
   * @throws Exception
   */
  public List<Page> getRelatedPage(WikiPageParams pageParams) throws Exception;

  /**
   * Remove relate page
   * 
   * @param orginaryPageParams The param of the target page to remove related page from
   * @param relatedPageParams The param of the related page
   * @return Remove related page is success or not
   * @throws Exception
   */
  public boolean removeRelatedPage(WikiPageParams orginaryPageParams, WikiPageParams relatedPageParams) throws Exception;
}
