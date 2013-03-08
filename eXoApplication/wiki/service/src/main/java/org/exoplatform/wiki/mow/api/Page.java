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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.services.security.Identity;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public interface Page {

  String getName();
  
  /**
   * Get the owner of the page
   * 
   * @return
   */
  String getOwner();

  /**
   * The Author is changed when any part of the document changes (content, attachments).
   */
  String getAuthor();
  
  /**
   * The date when creating page.
   */
  Date getCreatedDate();
  
  /**
   * The date when any part of the document changes (content, attachments).
   */
  Date getUpdatedDate();
  
  /**
   * Get the actual content of the page
   * 
   * @return
   */
  Attachment getContent();
  
  /**
   * Get the syntax used in that page
   * 
   * @return
   */
  String getSyntax();

  void setSyntax(String syntax);

  String getTitle();

  void setTitle(String title);
  
  String getComment();
  
  void setComment(String comment);

  /**
   * Get the attachments of this page
   * 
   * @return
   * @throws Exception 
   */
  Collection<? extends Attachment> getAttachments() throws Exception;
  
  boolean hasPermission(PermissionType permissionType) throws Exception; 
  
  /**
   * Check if user has permisison on page or not
   * 
   * @param permissionType The type of permisison to check {@link PermissionType}}
   * @param user The user to check
   * @return User has permisison on page or not
   * @throws Exception
   */
  boolean hasPermission(PermissionType permissionType, Identity user) throws Exception;
  
  /**
   * 
   * @return
   * @throws Exception
   */
  HashMap<String, String[]> getPermission() throws Exception;
  
  /**
   * 
   * @param permissions
   * @throws Exception
   */
  void setPermission(HashMap<String, String[]> permissions) throws Exception;
  
  /**
   * get URL of page. The domain part of link can be fixed.
   */
  String getURL();
}
