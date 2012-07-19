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
package org.exoplatform.wiki.mow.api;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Mar 31, 2010  
 */
public interface WikiNodeType {

  public final static String WIKI_STORE              = "wiki:store";

  public final static String PORTAL_WIKI_CONTAINER   = "wiki:portalwikis";

  public final static String GROUP_WIKI_CONTAINER    = "wiki:groupwikis";

  public final static String USER_WIKI_CONTAINER     = "wiki:userwikis";

  public final static String PORTAL_WIKI             = "wiki:portalwiki";
  
  public final static String GROUP_WIKI              = "wiki:groupwiki";

  public final static String WIKI                    = "wiki:wiki";

  public final static String USER_WIKI               = "wiki:userwiki";

  public final static String LINK_REGISTRY           = "wiki:linkregistry";
  
  public final static String LINK_ENTRY              = "wiki:linkentry";
  
  public final static String WIKI_HOME               = "exo:wikihome";

  public final static String WIKI_PAGE               = "wiki:page";

  public final static String WIKI_ATTACHMENT         = "wiki:attachment";

  public final static String WIKI_ATTACHMENT_CONTENT = "nt:resource";

  public final static String WIKI_PAGE_CONTENT       = "wiki:content";

  public final static String WIKI_CONTENT_ITEM       = "wiki:contentItem";

  public final static String WIKI_PARAGRAPH          = "wiki:paragraph";

  public final static String WIKI_LINK               = "wiki:link";

  public final static String WIKI_ANNOTATION         = "wiki:annotation";

  public final static String WIKI_MARKUP             = "wiki:markup";

  public final static String WIKI_TRASH              = "wiki:trash";

  public final static String WIKI_REMOVED            = "wiki:removed";

  public final static String WIKI_PREFERENCES        = "wiki:preferences";
  
  public final static String WIKI_PREFERENCES_SYNTAX = "wiki:preferencessyntax";
  
  public final static String WIKI_TEMPLATE_CONTAINER = "wiki:templatecontainer";

  public final static String WIKI_TEMPLATE           = "wiki:template";

  public final static String WIKI_WATCHED            = "wiki:watched";
  
  public final static String WIKI_HELP_PAGE            = "wiki:helppage";
  
  public interface Definition {

    public final static String WIKI_APPLICATION           = "eXoWiki";

    public final static String WIKIS                      = "wikis";

    public final static String WIKI_STORE_NAME            = "wikimetadata";

    public final static String PORTAL_WIKI_CONTAINER_NAME = "portalwikis";

    public final static String GROUP_WIKI_CONTAINER_NAME  = "groupwikis";

    public final static String USER_WIKI_CONTAINER_NAME   = "userwikis";

    public final static String WIKI_CONTAINER_REFERENCE   = "ref";

    public final static String LINK_REGISTRY              = "LinkRegistry";
    
    public final static String WIKI_PERMISSIONS           = "wikiPermissions";
    
    public final static String DEFAULT_PERMISSIONS_INITED = "isDefaultPermissionsInited";
    
    public final static String NEW_LINK                   = "newlink";
    
    public final static String WIKI_HOME_NAME             = "WikiHome";

    public final static String TRASH_NAME                 = "Trash";

    public final static String WIKI_HOME_TITLE            = "Wiki Home";

    public final static String OWNER                      = "owner";

    public final static String AUTHOR                     = "author";

    public final static String CONTENT                    = "content";

    public final static String ALIAS                      = "alias";

    public final static String TARGET                     = "target";

    public final static String TITLE                      = "title";

    public final static String TEXT                       = "text";
    
    public final static String COMMENT                    = "comment";

    public final static String SYNTAX                     = "syntax";
    
    public final static String CREATED_DATE               = "createdDate";

    public final static String UPDATED_DATE               = "updatedDate";
    
    public final static String OVERRIDEPERMISSION         = "isOverridePermission";
    
    public final static String ROOT_VERSION               = "rootVersion";

    public final static String CREATED                    = "jcr:created";

    public final static String PATH                       = "jcr:path";

    public final static String MIMETYPE                   = "jcr:mimeType";

    public final static String PRIMARY_TYPE               = "jcr:primaryType";

    public final static String DATA                       = "jcr:data";

    public final static String ATTACHMENT_CONTENT         = "jcr:content";

    public final static String UPDATED                    = "jcr:lastModified";

    public final static String CREATOR                    = "creator";

    public final static String SIZE                       = "size";

    public final static String HELP_PAGES                 = "helppages";

    public final static String DRAFT_NEW_PAGES            = "draftNewPages";
    
    public final static String EMOTION_ICONS_PAGE         = "EmotionIconsPage";

    public final static String ALLOW_MULTIPLE_SYNTAXES    = "allowMutipleSyntaxes";

    public final static String DEFAULT_SYNTAX             = "defaultSyntax";

    public final static String PREFERENCES                = "Preferences";

    public final static String PREFERENCES_SYNTAX         = "PreferencesSyntax";
    
    public final static String TEMPLATE_CONTAINER         = "TemplateContainer";
    
    public final static String FILE_TYPE                  = "fileType";
    
    public final static String WATCHER                    = "watcher";
    
    public final static String RELATION                    = "exo:relation";
    
    public final static String DESCRIPTION                = "description";
    
    public final static String URL                        = "url";

    public final static String OLD_PAGE_IDS               = "oldPageIds";

    public final static String TARGET_PAGE                = "targetPage";
    
  }
}
