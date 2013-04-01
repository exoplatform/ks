/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wiki.mow.core.api.wiki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.chromattic.api.UndeclaredRepositoryException;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.Property;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.service.WikiService;
import org.xwiki.rendering.syntax.Syntax;
/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public abstract class WikiImpl implements Wiki {
  
  @Create
  public abstract PageImpl createWikiPage();

  public abstract WikiType getWikiType();
  
  
  private WikiService wService;

  public WikiService getWikiService() {
    return this.wService;
  }

  public void setWikiService(WikiService wService) {
    this.wService = wService;
  }

  public void initTemplate() {
    String path = getPreferences().getPath();
    wService.initDefaultTemplatePage(path);
  }
  
  public WikiHome getWikiHome() {
    WikiHome home = getHome();
    if (home == null) {
      home = createWikiHome();
      setHome(home);
      home.makeVersionable();
      home.setOwner(getOwner());
      AttachmentImpl content = home.getContent();
      home.setTitle(WikiNodeType.Definition.WIKI_HOME_TITLE);
      home.setSyntax(Syntax.XWIKI_2_0.toIdString());
      StringBuilder sb = new StringBuilder("{{tip}}\nWelcome to Wiki Home of ");
      sb.append(getOwner()).append(" ");
      if (WikiType.PORTAL.equals(getWikiType())) {
        sb.append("portal");
      } else if (WikiType.GROUP.equals(getWikiType())) {
        sb.append("group");
      }
      sb.append(".").append("\n* See **[[Sandbox space>>group:sandbox.WikiHome]]** for an example wiki with sample content.\n{{/tip}}");
      content.setText(sb.toString());
      try {
        initPermisionForWikiHome(home);
        home.checkin();
        home.checkout();
      } catch (Exception e) {
        throw new UndeclaredRepositoryException("Can't create new version for WikiHome");
      }
    }
    return home;
  }
  
  private void initPermisionForWikiHome(WikiHome home) throws Exception {
	    List<String> wikiPermission = getWikiPermissions();
	    if (wikiPermission == null) {
	      home.setNonePermission();
	      return;
	    }
	    
	    HashMap<String, String[]> permMap = new HashMap<String, String[]>();
	    for (String perm : wikiPermission) {
	      String[] actions = perm.substring(0, perm.indexOf(":")).split(",");
	      perm = perm.substring(perm.indexOf(":") + 1);
	      String id = perm.substring(perm.indexOf(":") + 1);
	      List<String> jcrActions = new ArrayList<String>();
	      for (String action : actions) {
	        if (PermissionType.VIEWPAGE.toString().equals(action)) {
	          jcrActions.add(org.exoplatform.services.jcr.access.PermissionType.READ);
	        } else if (PermissionType.EDITPAGE.toString().equals(action)) {
	          jcrActions.add(org.exoplatform.services.jcr.access.PermissionType.ADD_NODE);
	          jcrActions.add(org.exoplatform.services.jcr.access.PermissionType.REMOVE);
	          jcrActions.add(org.exoplatform.services.jcr.access.PermissionType.SET_PROPERTY);
	        }
	      }
	      permMap.put(id, jcrActions.toArray(new String[jcrActions.size()]));
	    }
	    home.setPermission(permMap);
	  }


  public LinkRegistry getLinkRegistry() {
    LinkRegistry linkRegistry = getLinkRegistryByChromattic();
    if (linkRegistry == null) {
      linkRegistry = createLinkRegistry();
      setLinkRegistryByChromattic(linkRegistry);
    }
    return linkRegistry;
  }

  public Trash getTrash() {
    Trash trash = getTrashByChromattic();
    if (trash == null) {
      trash = createTrash();
      setTrashByChromattic(trash);
    }
    return trash;
  }
  
  public Preferences getPreferences()
  {
    Preferences preferences = getPreferencesByChromattic();
    if (preferences == null) {
      preferences = createPreferences();
      setPreferencesByChromattic(preferences);
      preferences.getPreferencesSyntax().setDefaultSyntax(wService.getDefaultWikiSyntaxId());
    }
    return preferences;
  }

  @Name
  public abstract String getName();

  @Property(name = WikiNodeType.Definition.OWNER)
  public abstract String getOwner();

  public abstract void setOwner(String wikiOwner);

  @Path
  public abstract String getPath();
  
  @Property(name = WikiNodeType.Definition.WIKI_PERMISSIONS)
  public abstract List<String> getWikiPermissions();
  public abstract void setWikiPermissions(List<String> permissions);
  
  @Property(name = WikiNodeType.Definition.DEFAULT_PERMISSIONS_INITED)
  public abstract boolean getDefaultPermissionsInited();
  public abstract void setDefaultPermissionsInited(boolean isInited);

  public PageImpl getPageByID(String id) {
    throw new UnsupportedOperationException();
  }

  public PageImpl getPageByURI(String uri) {
    throw new UnsupportedOperationException();
  }
  
  public abstract String getType();
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.WIKI_HOME_NAME)
  protected abstract WikiHome getHome();
  protected abstract void setHome(WikiHome homePage);
  
  @Create
  protected abstract WikiHome createWikiHome();

  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.LINK_REGISTRY)
  protected abstract LinkRegistry getLinkRegistryByChromattic();
  protected abstract void setLinkRegistryByChromattic(LinkRegistry linkRegistry);

  @Create
  protected abstract LinkRegistry createLinkRegistry();
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.TRASH_NAME)
  protected abstract Trash getTrashByChromattic();
  protected abstract void setTrashByChromattic(Trash trash);

  @Create
  protected abstract Trash createTrash();
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.PREFERENCES)
  protected abstract Preferences getPreferencesByChromattic();
  protected abstract void setPreferencesByChromattic(Preferences preferences);
  
  @Create
  protected abstract Preferences createPreferences();
  
}
