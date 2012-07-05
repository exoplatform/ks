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
package org.exoplatform.forum.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * March 2, 2007  
 */
public class Forum {
  private String       id;

  private String       owner;

  private String       path;

  private int          forumOrder           = 0;

  private Date         createdDate;

  private String       modifiedBy;

  private Date         modifiedDate;

  private String       lastTopicPath        = "";

  private String       name;

  private String       description;

  private long         postCount            = 0;

  private long         topicCount           = 0;

  private String[]     notifyWhenAddTopic;

  private String[]     notifyWhenAddPost;

  private boolean      isAutoAddEmailNotify = true;

  private boolean      isModerateTopic      = false;

  private boolean      isModeratePost       = false;

  private boolean      isClosed             = false;

  private boolean      isLock               = false;

  private String[]     moderators;

  private String[]     createTopicRole;

  private String[]     viewer;

  private String[]     poster;

  private String[]     emailNotification;

  private List<String> banIPs = new ArrayList<String>();

  public Forum() {
    notifyWhenAddTopic = new String[] {};
    notifyWhenAddPost = new String[] {};
    viewer = new String[] {""};
    createTopicRole = new String[] {""};
    moderators = new String[] {""};
    poster = new String[] {""};
    emailNotification = new String[] {};
    id = Utils.FORUM + IdGenerator.generate();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * This method should:
   * Calculate the category id  base on the forum id
   * @return The category id
   */
  public String getCategoryId() {
    if (path != null && path.length() > 0) {
      String[] arr = path.split("/");
      return arr[arr.length - 2];
    }
    return null;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getForumOrder() {
    return forumOrder;
  }

  public void setForumOrder(int forumOrder) {
    this.forumOrder = forumOrder;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
  }

  public String getLastTopicPath() {
    return lastTopicPath;
  }

  public void setLastTopicPath(String lastTopicPath) {
    this.lastTopicPath = lastTopicPath;
  }

  public String getForumName() {
    return name;
  }

  public void setForumName(String forumName) {
    this.name = forumName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getPostCount() {
    return postCount;
  }

  public void setPostCount(long postCount) {
    this.postCount = postCount;
  }

  public long getTopicCount() {
    return topicCount;
  }

  public void setTopicCount(long topicCount) {
    this.topicCount = topicCount;
  }

  public String[] getNotifyWhenAddTopic() {
    return notifyWhenAddTopic;
  }

  public void setNotifyWhenAddTopic(String[] notifyWhenAddTopic) {
    this.notifyWhenAddTopic = notifyWhenAddTopic;
  }

  public String[] getNotifyWhenAddPost() {
    return notifyWhenAddPost;
  }

  public void setNotifyWhenAddPost(String[] notifyWhenAddPost) {
    this.notifyWhenAddPost = notifyWhenAddPost;
  }

  public boolean getIsModerateTopic() {
    return isModerateTopic;
  }

  public void setIsModerateTopic(boolean isModerateTopic) {
    this.isModerateTopic = isModerateTopic;
  }

  public boolean getIsModeratePost() {
    return isModeratePost;
  }

  public void setIsModeratePost(boolean isModeratePost) {
    this.isModeratePost = isModeratePost;
  }

  public boolean getIsClosed() {
    return isClosed;
  }

  public void setIsClosed(boolean isClosed) {
    this.isClosed = isClosed;
  }

  public boolean getIsLock() {
    return isLock;
  }

  public void setIsLock(boolean isLock) {
    this.isLock = isLock;
  }

  public String[] getCreateTopicRole() {
    return createTopicRole;
  }

  public void setCreateTopicRole(String[] createTopicRole) {
    this.createTopicRole = createTopicRole;
  }

  public String[] getPoster() {
    return poster;
  }

  public void setPoster(String[] poster) {
    this.poster = poster;
  }

  public String[] getViewer() {
    return viewer;
  }

  public void setViewer(String[] viewer) {
    this.viewer = viewer;
  }

  public String[] getModerators() {
    return moderators;
  }

  public void setModerators(String[] moderators) {
    this.moderators = moderators;
  }

  public String[] getEmailNotification() {
    return emailNotification;
  }

  public void setEmailNotification(String[] emailNotification) {
    this.emailNotification = emailNotification;
  }

  public List<String> getBanIP() {
    return banIPs;
  }

  public void setBanIP(List<String> banIPs) {
    this.banIPs = banIPs;
  }

  public boolean getIsAutoAddEmailNotify() {
    return isAutoAddEmailNotify;
  }

  public void setIsAutoAddEmailNotify(boolean isAutoAddEmailNotify) {
    this.isAutoAddEmailNotify = isAutoAddEmailNotify;
  }
}
