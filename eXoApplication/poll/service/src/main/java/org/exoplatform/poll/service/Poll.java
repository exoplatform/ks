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
package org.exoplatform.poll.service;

import java.util.Date;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *         tu.duy@exoplatform.com
 * Octo 25, 2007
 */
public class Poll {
  private String   id;

  private String   parentPath;

  private String   oldParentPath;

  private String   owner;

  private Date     createdDate;

  private String   modifiedBy;

  private Date     modifiedDate;

  private Date     lastVote;

  private long     timeOut      = 0;

  private String   question;

  private String[] option;

  private String[] vote;

  private String[] userVote;

  private boolean  isMultiCheck = false;

  private boolean  isClosed     = false;

  private boolean  isAgainVote  = false;

  private boolean  showVote     = true;

  private String   votes;

  private String[] infoVote;

  private String   expire;

  private String   isAdmin      = "false";

  public Poll() {
    id = PollNodeTypes.POLL + IdGenerator.generate();
    createdDate = new Date();
    option = new String[] {};
    vote = new String[] {};
    userVote = new String[] {};
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setIsAdmin(String isAdmin) {
    this.isAdmin = isAdmin;
  }

  public String getIsAdmin() {
    return isAdmin;
  }

  public String getParentPath() {
    return parentPath;
  }

  public void setParentPath(String path) {
    this.parentPath = path;
  }

  public void setOldParentPath(String oldParentPath) {
    this.oldParentPath = oldParentPath;
  }

  public String getOldParentPath() {
    return oldParentPath;
  }

  /**
   * This method should calculate the id of the topic base on the id of the post
   * @return
   */
  public String getTopicId() {
    return null;
  }

  /**
   * This method should calculate the id of the forum base on the id of the post
   * @return
   */
  public String getForumId() {
    return null;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
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

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public long getTimeOut() {
    return timeOut;
  }

  public void setTimeOut(long timeOut) {
    this.timeOut = timeOut;
  }

  public String[] getOption() {
    return this.option;
  }

  public void setOption(String[] option) {
    this.option = option;
  }

  public String[] getVote() {
    return vote;
  }

  public void setVote(String[] vote) {
    this.vote = vote;
  }

  public String[] getUserVote() {
    return userVote;
  }

  public void setUserVote(String[] userVote) {
    this.userVote = userVote;
  }

  public boolean getIsMultiCheck() {
    return isMultiCheck;
  }

  public void setIsMultiCheck(boolean isMultiCheck) {
    this.isMultiCheck = isMultiCheck;
  }

  public boolean getIsClosed() {
    return isClosed;
  }

  public void setIsClosed(boolean isClosed) {
    this.isClosed = isClosed;
  }

  public boolean getIsAgainVote() {
    return isAgainVote;
  }

  public void setIsAgainVote(boolean isAgainVote) {
    this.isAgainVote = isAgainVote;
  }

  public Date getLastVote() {
    return lastVote;
  }

  public void setLastVote(Date lastVote) {
    this.lastVote = lastVote;
  }

  public void setShowVote(boolean showVote) {
    this.showVote = showVote;
  }

  public boolean getShowVote() {
    return showVote;
  }

  public void setVotes(String votes) {
    this.votes = votes;
  }

  public String getVotes() {
    int size = 0;
    if (!isMultiCheck)
      size = userVote.length;
    else
      for (int i = 0; i < userVote.length; i++)
        size += userVote[i].split(":").length - 1;
    votes = String.valueOf(size);
    return votes;
  }

  public void setInfoVote() {
    String[] userVotes = userVote;
    long size = 0, temp = 1;
    if (!this.getIsMultiCheck()) {
      size = userVote.length;
    } else {
      for (int i = 0; i < userVote.length; i++) {
        size += userVote[i].split(":").length - 1;// root:1:2:4 --> user root checked option 1, 2 and 4 --> checked time is 3
      }
    }
    temp = size;
    if (size == 0)
      size = 1;
    int l = vote.length;
    infoVote = new String[(l + 1)];
    for (int j = 0; j < l; j++) {
      String string = vote[j];
      double tmp = Double.parseDouble(string);
      double k = (tmp * size) / 100;
      double t = (double) Math.round(k);
      string = String.valueOf(t * 100 / size);
      int dotPos = string.indexOf(".");
      int numberAfterDot = string.length() - dotPos - 1;
      if (dotPos > 0 && numberAfterDot > 2)
        string = string.substring(0, dotPos + 3);
      infoVote[j] = string + ":" + t;
    }
    infoVote[l] = "" + temp;
    if (this.getIsMultiCheck()) {
      infoVote[l] = String.valueOf(userVotes.length);
    }
  }

  public String[] getInfoVote() {
    return infoVote;
  }

  public void setExpire(String expire) {
    this.expire = expire;
  }

  public String getExpire() {
    return expire;
  }
}
