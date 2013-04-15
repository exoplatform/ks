/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

import javax.jcr.Node;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.SessionManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 */

public class ForumServiceUtils {

  private static final String ANY   = "*".intern();

  private static final String COLON = ":".intern();

  private static final String SLASH = "/".intern();
  
  /**
   * 
   * Verify if a user match user, group, membership expressions
   * @param userGroupMembership ist that may contain usernames or group names or membership expressions in the form MEMBERSHIPTYPE:GROUP
   * @param userId username to match against the expressions
   * @return true if the user match at least one of the expressions
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public static boolean hasPermission(String[] userGroupMembership, String userId) throws Exception {
    IdentityRegistry identityRegistry = (IdentityRegistry) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityRegistry.class);
    Identity identity = identityRegistry.getIdentity(userId);
    if (identity == null) {
      OrganizationService oService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
      Collection<Membership> memberships = oService.getMembershipHandler().findMembershipsByUser(userId);
      ArrayList<MembershipEntry> membershipEntryList = new ArrayList<MembershipEntry>();
      if (memberships != null) {
        for (Membership membership : memberships) {
          membershipEntryList.add(new MembershipEntry(membership.getGroupId() ,membership.getMembershipType()));
        }
      }
      identity = new Identity(userId, membershipEntryList);
    }

    if (userGroupMembership == null || userGroupMembership.length <= 0 || userGroupMembership[0].equals(" "))
      return false;

    for (String item : userGroupMembership) {
      String expr = item.trim();

      if (isMembershipExpression(expr)) {
        String[] array = expr.split(COLON);
        String membershipType = array[0];
        String group = array[1];
        if (identity.isMemberOf(group, membershipType)) {
          return true;
        }
      } else if (isGroupExpression(expr)) {
        String group = expr;
        if (identity.isMemberOf(group)) {
          return true;
        }
      } else {
        String username = expr;
        if (username.equals(userId)) {
          return true;
        }
      }

    }
    return false; // no match found
  }

  /**
   * Is the expression a group expression
   * @param expr
   * @return
   */
  public static boolean isGroupExpression(String expr) {
    return ((expr.indexOf(SLASH) >= 0) && !(expr.indexOf(COLON) >= 0));
  }

  /**
   * Is the expression a membership expression (MEMBERSHIPTYPE:GROUP)
   * @param expr
   * @return
   */
  public static boolean isMembershipExpression(String expr) {
    return ((expr.indexOf(SLASH) >= 0) && (expr.indexOf(COLON) >= 0));
  }

  private static ListAccess<User> getUserByGroup(UserHandler userHandler, String group){
    try {
      return userHandler.findUsersByGroupId(group);
    } catch (Exception e) {
      return null;
    }
  }
  
  /**
   * Find usernames matching membership expressions
   * @param organizationService 
   * @param memberShip the membership, ex: member:/platform/users , *:/platform/users. 
   * @return list of users that mach at least one of the membership
   * @throws Exception
   */
  
  private static List<String> getUserByMembershipType(OrganizationService organizationService, String memberShip) throws Exception {
    List<String> users = getFromCache(new String[] { memberShip });
    if (users != null) {
      return users;
    }
    users = new ArrayList<String>();
    String[] array = memberShip.trim().split(COLON);
    UserHandler userHandler = organizationService.getUserHandler();
    if (array[0].length() > 1) {
      List<String> usersOfGroup = getUserByGroupId(userHandler, array[1]);
      MembershipHandler membershipHandler = organizationService.getMembershipHandler();
      for (String userName : usersOfGroup) {
        if (membershipHandler.findMembershipByUserGroupAndType(userName, array[1], array[0]) != null) {
          users.add(userName);
        }
      }
    } else {
      if (ANY.equals(array[0])) {
        users.addAll(getUserByGroupId(userHandler, array[1]));
      }
    }
    storeInCache(new String[] { memberShip }, users);
    return users;
  }
  
  /**
   * Find usernames matching group id expressions
   * @param userHandler 
   * @param groupId the group Id, ex: /platform/users . 
   * @return list of users that mach at least one of the group id
   * @throws Exception
   */
  private static List<String> getUserByGroupId(UserHandler userHandler, String groupId) throws Exception {
    List<String> users = getFromCache(new String[]{groupId});
    if (users != null) {
      return users;
    }
    users = new ArrayList<String>();
    ListAccess<User> pageList = getUserByGroup(userHandler, groupId);
    if (pageList == null){
      return users;
    }
    User[] userArray = (User[]) pageList.load(0, pageList.getSize());
    for (int i = 0; i < pageList.getSize(); i++) {
      users.add(userArray[i].getUserName());
    }
    storeInCache(new String[]{groupId}, users);
    return users;
  }
  
  /**
   * Find usernames matching user, group or membership expressions
   * @param userGroupMembership list that may contain usernames or group names or membership expressions in the form MEMBERSHIPTYPE:GROUP
   * @return list of users that mach at least one of the userGroupMembership
   * @throws Exception
   */
  public static List<String> getUserPermission(String[] userGroupMembership) throws Exception {
    if (userGroupMembership == null || userGroupMembership.length <= 0 || (userGroupMembership.length == 1 && userGroupMembership[0].equals(" "))) {
      return new ArrayList<String>();
    }
    List<String> list = getFromCache(userGroupMembership);
    if (list != null) {
      return list;
    }
    OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer()
                                                                                       .getComponentInstanceOfType(OrganizationService.class);
    Set<String> users = new HashSet<String>();
    for (int j = 0; j < userGroupMembership.length; j++) {
      String str = userGroupMembership[j].trim();
      if (isMembershipExpression(str)) {
        users.addAll(getUserByMembershipType(organizationService, str));
      } else if (isGroupExpression(str)) {
        users.addAll(getUserByGroupId(organizationService.getUserHandler(), str));
      } else {
        users.add(str);
      }
    }
    storeInCache(userGroupMembership, new ArrayList<String>(users));
    return new ArrayList<String>(users);
  }

  /**
   * Store the list of user for the permission expressions in cache
   * @param userGroupMembership
   * @param users
   * @throws Exception
   */
  private static void storeInCache(String[] userGroupMembership, List<String> users) throws Exception {
    ExoCache<Serializable, List<String>> cache = getCache();
    Serializable cacheKey = getCacheKey(userGroupMembership);
    cache.put(cacheKey, users);
  }

  /**
   * Load a list of user for the permission expressions in cache
   * @param userGroupMembership
   * @return
   * @throws Exception
   */
  private static List<String> getFromCache(String[] userGroupMembership) {
    ExoCache<Serializable, List<String>> cache = getCache();
    Serializable cacheKey = getCacheKey(userGroupMembership);
    return cache.get(cacheKey);
  }

  private static Serializable getCacheKey(String[] userGroupMembership) {
    StringBuilder sb = new StringBuilder();
    for (String item : userGroupMembership) {
      sb.append("#").append(item);
    }
    return sb.toString();
  }

  private static ExoCache<Serializable, List<String>> getCache(){
    CacheService cacheService = (CacheService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CacheService.class);
    return cacheService.getCacheInstance("org.exoplatform.forum.ForumPermissionsUsers");
  }

  public static void reparePermissions(Node node, String owner) throws Exception {
    ExtendedNode extNode = (ExtendedNode) node;
    if (extNode.canAddMixin("exo:privilegeable"))
      extNode.addMixin("exo:privilegeable");
    String[] arrayPers = { PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE };
    extNode.setPermission(owner, arrayPers);
    List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries();
    for (AccessControlEntry accessControlEntry : permsList) {
      extNode.setPermission(accessControlEntry.getIdentity(), arrayPers);
    }
  }

  public static SessionManager getSessionManager() {
    KSDataLocation location = (KSDataLocation) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(KSDataLocation.class);
    return location.getSessionManager();
  }
}
