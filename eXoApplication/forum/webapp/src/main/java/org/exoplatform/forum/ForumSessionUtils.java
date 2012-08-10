/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.user.CommonContact;
import org.exoplatform.ks.common.user.ContactProvider;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 */

public class ForumSessionUtils {

  static private final Log   LOG            = ExoLogger.getLogger(ForumSessionUtils.class);

  public final static String DEFAULT_AVATAR = "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";

  /**
   * create an avatar link for user. 
   * Firstly, the function tries to load avatar resource from user profile of forum.
   * if the resource is not found, try to get it from {@link ContactProvider}
   * else, return default url: <a>/forum/skin/DefaultSkin/webui/background/Avatar1.gif</a>.
   * @param userName
   * @param forumService
   * @return
   */
  public static String getUserAvatarURL(String userName, ForumService forumService) {
    String url = null;
    try {
      ForumAttachment attachment = forumService.getUserAvatar(userName);
      url = CommonUtils.getImageUrl(attachment.getPath()) + "?size=" + attachment.getSize();
    } catch (Exception e) {
      if (LOG.isDebugEnabled())
        LOG.debug(String.format("can not load avatar of [%s] as file resource", userName), e);
    }
    if (url == null || url.trim().length() < 1) {
      CommonContact contact = getPersonalContact(userName);
      if (!ForumUtils.isEmpty(contact.getAvatarUrl())) {
        url = contact.getAvatarUrl();
      }
      url = (url == null || url.trim().length() < 1) ? DEFAULT_AVATAR : url;
    }
    return url;
  }

  public static String getFileSource(InputStream input, String fileName, DownloadService dservice) throws Exception {
    byte[] imageBytes = null;
    if (input != null) {
      imageBytes = new byte[input.available()];
      input.read(imageBytes);
      ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
      InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image");
      dresource.setDownloadName(fileName);
      return dservice.getDownloadLink(dservice.addDownloadResource(dresource));
    }
    return null;
  }

  public static CommonContact getPersonalContact(String userId) {
    try {
      if (userId.indexOf(Utils.DELETED) > 0)
        return new CommonContact();
      ContactProvider provider = (ContactProvider) PortalContainer.getComponent(ContactProvider.class);
      return provider.getCommonContact(userId);
    } catch (Exception e) {
      return new CommonContact();
    }
  }

  public static String getBreadcumbUrl(String link, String componentName, String actionName, String objectId) throws Exception {
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    String url = portalContext.getRequest().getRequestURL().toString();
    url = url.substring(0, url.indexOf(ForumUtils.SLASH, 8));
    link = link.replaceFirst(componentName, "UIBreadcumbs").replaceFirst(actionName, "ChangePath").replace("pathId", objectId).replaceAll("&amp;", "&");
    return (url + link);
  }
  
  public static List<String> getGroupIdInSpaceOfUser(String userId) throws Exception {
    List<String> groupIds = new ArrayList<String>();
    for (Object group : UserHelper.getOrganizationService().getGroupHandler().findGroupsOfUser(userId)) {
      if (((Group) group).getId().indexOf(ForumUtils.SPACE_GROUP_ID) >= 0) {
        groupIds.add(((Group) group).getGroupName());
      }
    }
    return groupIds;
  }
  
  public static String getQueryForumInSpace(String userId) throws Exception {
    if (!ForumUtils.isEmpty(userId)) {
      StringBuilder builder = new StringBuilder();
      for (String groupId : getGroupIdInSpaceOfUser(userId)) {
        if (builder.length() > 0) {
          builder.append(" or ");
        }
        builder.append("(jcr:like(@").append(Utils.EXO_ID).append(",'%").append(groupId).append("%'))");
      }
      return builder.toString();
    }
    return ForumUtils.EMPTY_STR;
  }
  
}
