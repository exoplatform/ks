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
package org.exoplatform.wiki.mow.api;

import java.util.List;

import org.exoplatform.wiki.mow.core.api.wiki.Preferences;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public interface Wiki {

  /**
   * Wiki name
   * 
   * @return
   */
  String getName();

  /**
   * Name of the owner of this wiki. May be a portal name, a group name or a
   * user name depending on the type of the wiki.
   * 
   * @return
   */
  String getOwner();
  
  /**
   * Type of this wiki. May be a portal type, a group type or a
   * user type.
   * 
   * @return type of wiki
   */
  String getType();

  /**
   * Get the home page of the wiki
   * 
   * @return
   */
  Page getWikiHome();

  /**
   * Get a page by its URO
   * 
   * @param uri
   * @return
   */
  Page getPageByURI(String uri);

  /**
   * Get a Page by id
   * 
   * @param id
   * @return
   */
  Page getPageByID(String id);
  
  /**
   * 
   * @return
   */
  List<String> getWikiPermissions();
  
  /**
   * 
   * @param permissions
   */
  void setWikiPermissions(List<String> permissions);
  
  
  public Preferences getPreferences();
  
  public abstract boolean getDefaultPermissionsInited();
  
  public abstract void setDefaultPermissionsInited(boolean isInited);
  

}
