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
package org.exoplatform.wiki.mow.api;

import java.util.HashMap;

import javax.jcr.Node;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.services.security.Identity;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.service.PermissionType;


/**
 * Created by The eXo Platform SAS
 * Author : phongth
 *          phongth@exoplatform.com
 * October 27, 2011  
 */
public abstract class Permission {
  protected MOWService mowService;
  
  public void setMOWService(MOWService mowService) {
    this.mowService = mowService;
  }
  
  public MOWService getMOWService() {
    return mowService;
  }

  protected ChromatticSession getChromatticSession() {
    return mowService.getSession();
  }
  
  protected Node getJCRNode(String path) throws Exception {
    return (Node) getChromatticSession().getJCRSession().getItem(path);
  }
  
  public abstract HashMap<String, String[]> getPermission(String path) throws Exception;
  
  public abstract boolean hasPermission(PermissionType permissionType, String path) throws Exception;
  
  public abstract boolean hasPermission(PermissionType permissionType, String path, Identity user) throws Exception;
  
  public abstract void setPermission(HashMap<String, String[]> permissions, String path) throws Exception;
}
