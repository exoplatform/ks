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

import java.util.Date;
import java.util.List;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * March 2, 2007  
 */
public class Topic {
  private String                id;

  private String                owner;

  private String                path;

  private Date                  createdDate;

  private String                modifiedBy;

  private Date                  modifiedDate;

  private String                editReason;

  private String                lastPostBy;

  private Date                  lastPostDate;

  private String                name;

  private String                description;

  private long                  postCount           = 0;

  private long                  viewCount           = 0;

  private String                icon;

  private String                link                = "";

  private String                remoteAddr          = "";

  private String                topicType           = "";

  private long                  numberAttachments   = 0;

  private boolean               isModeratePost      = false;

  private String                isNotifyWhenAddPost = "";

  private boolean               isClosed            = false;

  private boolean               isLock              = false;

  private boolean               isApproved          = true;

  private boolean               isSticky            = false;

  private boolean               isPoll              = false;

  private boolean               isWaiting           = false;

  private boolean               isActive            = true;

  private boolean               isActiveByForum     = true;

  private String[]              canView;

  private String[]              canPost;

  private String[]              userVoteRating;

  private String[]              tagId;

  private String[]              emailNotification;

  private Double                voteRating          = 0.0;

  private List<ForumAttachment> attachments;

  public Topic() {
    canView = new String[] { "" };
    canPost = new String[] { " " };
    userVoteRating = new String[] {};
    tagId = new String[] {};
    emailNotification = new String[] {};
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

  public String getEditReason() {
    return editReason;
  }

  public void setEditReason(String editReason) {
    this.editReason = editReason;
  }

  public String getLastPostBy() {
    return lastPostBy;
  }

  public void setLastPostBy(String lastPostBy) {
    this.lastPostBy = lastPostBy;
  }

  public Date getLastPostDate() {
    return lastPostDate;
  }

  public void setLastPostDate(Date lastPostDate) {
    this.lastPostDate = lastPostDate;
  }

  public String getTopicName() {
    return name;
  }

  public void setTopicName(String topic) {
    this.name = topic;
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

  public long getViewCount() {
    return viewCount;
  }

  public void setViewCount(long viewCount) {
    this.viewCount = viewCount;
  }

  public boolean getIsModeratePost() {
    return isModeratePost;
  }

  public void setIsModeratePost(boolean isModeratePost) {
    this.isModeratePost = isModeratePost;
  }

  public String getIsNotifyWhenAddPost() {
    return isNotifyWhenAddPost;
  }

  public void setIsNotifyWhenAddPost(String isNotifyWhenAddPost) {
    this.isNotifyWhenAddPost = isNotifyWhenAddPost;
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

  public String getId() {
    if (id == null) {
      id = Utils.TOPIC + IdGenerator.generate();
    }
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public long getNumberAttachment() {
    return numberAttachments;
  }

  public void setNumberAttachment(long numberAttachments) {
    this.numberAttachments = numberAttachments;
  }

  public boolean getIsApproved() {
    return isApproved;
  }

  public void setIsApproved(boolean isApproved) {
    this.isApproved = isApproved;
  }

  public boolean getIsActiveByForum() {
    return isActiveByForum;
  }

  public void setIsActiveByForum(boolean isActiveByForum) {
    this.isActiveByForum = isActiveByForum;
  }

  public boolean getIsSticky() {
    return isSticky;
  }

  public void setIsSticky(boolean isSticky) {
    this.isSticky = isSticky;
  }

  public String[] getCanView() {
    return canView;
  }

  public void setCanView(String[] canView) {
    this.canView = canView;
  }

  public String[] getCanPost() {
    return canPost;
  }

  public void setCanPost(String[] canPost) {
    this.canPost = canPost;
  }

  public boolean getIsPoll() {
    return isPoll;
  }

  public void setIsPoll(boolean isPoll) {
    this.isPoll = isPoll;
  }

  public String[] getUserVoteRating() {
    return userVoteRating;
  }

  public void setUserVoteRating(String[] userVoteRating) {
    this.userVoteRating = userVoteRating;
  }

  public String[] getTagId() {
    return tagId;
  }

  public void setTagId(String[] tagId) {
    this.tagId = tagId;
  }

  public Double getVoteRating() {
    return voteRating;
  }

  public void setVoteRating(Double voteRating) {
    this.voteRating = voteRating;
  }

  public void setAttachments(List<ForumAttachment> attachments) {
    this.attachments = attachments;
  }

  public List<ForumAttachment> getAttachments() {
    return this.attachments;
  }

  public String getCategoryId() {
    return Utils.getCategoryId(path);
  }

  public String getForumId() {
    return Utils.getForumId(path);
  }

  public boolean getIsWaiting() {
    return isWaiting;
  }

  public void setIsWaiting(boolean isWaiting) {
    this.isWaiting = isWaiting;
  }

  public boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(boolean isActive) {
    this.isActive = isActive;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getRemoteAddr() {
    return remoteAddr;
  }

  public void setRemoteAddr(String remoteAddr) {
    this.remoteAddr = remoteAddr;
  }

  public String getTopicType() {
    return topicType;
  }

  public void setTopicType(String topicType) {
    this.topicType = topicType;
  }

  public String[] getEmailNotification() {
    return emailNotification;
  }

  public void setEmailNotification(String[] emailNotification) {
    this.emailNotification = emailNotification;
  }
}
