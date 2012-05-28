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
package org.exoplatform.wiki.mow.core.api.wiki;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.wiki.mow.api.Permission;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : phongth
 *          phongth@exoplatform.com
 * October 27, 2011  
 */
public class PermissionImpl extends Permission {
  @Override
  public HashMap<String, String[]> getPermission(String jcrPath) throws Exception {
    ExtendedNode extendedNode = (ExtendedNode) getJCRNode(jcrPath);
    HashMap<String, String[]> perm = new HashMap<String, String[]>();
    AccessControlList acl = extendedNode.getACL();
    List<AccessControlEntry> aceList = acl.getPermissionEntries();
    for (int i = 0, length = aceList.size(); i < length; i++) {
      AccessControlEntry ace = aceList.get(i);
      String[] nodeActions = perm.get(ace.getIdentity());
      List<String> actions = null;
      if (nodeActions != null) {
        actions = new ArrayList<String>(Arrays.asList(nodeActions));
      } else {
        actions = new ArrayList<String>();
      }
      actions.add(ace.getPermission());
      perm.put(ace.getIdentity(), actions.toArray(new String[actions.size()]));
    }
    return perm;
  }

  @Override
  public boolean hasPermission(PermissionType permissionType, String jcrPath) throws Exception {
    String[] permission = new String[] {};
    if (PermissionType.VIEWPAGE.equals(permissionType) || PermissionType.VIEW_ATTACHMENT.equals(permissionType)) {
      permission = new String[] { org.exoplatform.services.jcr.access.PermissionType.READ };
    } else if (PermissionType.EDITPAGE.equals(permissionType) || PermissionType.EDIT_ATTACHMENT.equals(permissionType)) {
      permission = new String[] { org.exoplatform.services.jcr.access.PermissionType.ADD_NODE,
          org.exoplatform.services.jcr.access.PermissionType.REMOVE,
          org.exoplatform.services.jcr.access.PermissionType.SET_PROPERTY };
    }

    ExtendedNode extendedNode = (ExtendedNode) getJCRNode(jcrPath);
    AccessControlList acl = extendedNode.getACL();
    ConversationState conversationState = ConversationState.getCurrent();
    Identity user = null;
    if (conversationState != null) {
      user = conversationState.getIdentity();
    } else {
      user = new Identity(IdentityConstants.ANONIM);
    }
    return Utils.hasPermission(acl, permission, user);
  }

  @Override
  public void setPermission(HashMap<String, String[]> permissions, String jcrPath) throws Exception {
    getChromatticSession().save();
    ExtendedNode extendedNode = (ExtendedNode) getJCRNode(jcrPath);
    if (extendedNode.canAddMixin("exo:privilegeable")) {
      extendedNode.addMixin("exo:privilegeable");
    }
    
    if (permissions != null && permissions.size() > 0) {
      extendedNode.setPermissions(permissions);
    } else {
      extendedNode.clearACL();
      extendedNode.setPermission(IdentityConstants.ANY, org.exoplatform.services.jcr.access.PermissionType.ALL);
    }
  }
}
