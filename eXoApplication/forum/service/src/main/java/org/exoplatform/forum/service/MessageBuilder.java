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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.service;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.services.mail.Message;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          tu.duy@exoplatform.com
 * Jan 24, 2011  
 */
public class MessageBuilder {
  public final static String  CONTEN_EMAIL = Utils.DEFAULT_EMAIL_CONTENT;

  public final static String  SLASH        = "/".intern();

  private String              id;

  private String              owner;

  private String              headerSubject;

  private String              content;

  private String              objName;

  private String              watchType;

  private String              addType;

  private String              addName;

  private String              message;

  private String              catName;

  private String              forumName;

  private String              topicName;

  private String              link;

  private String              privateLink;

  private String              dateFormat;

  private String              timeFormat;

  private String              zoneTime;

  private Date                createdDate;

  private Map<String, String> types;

  public MessageBuilder() {
    content = CONTEN_EMAIL;
    dateFormat = "MM/dd/yyyy";
    timeFormat = "HH:mm";
    zoneTime = "GMT+0";
    types = new HashMap<String, String>();
    types.put(Utils.CATEGORY, "Category");
    types.put(Utils.FORUM, Utils.FORUM);
    types.put(Utils.TOPIC, Utils.TOPIC);
    types.put(Utils.POST, Utils.POST);
    link = "";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getHeaderSubject() {
    return headerSubject;
  }

  public void setHeaderSubject(String headerSubject) {
    this.headerSubject = headerSubject;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getObjName() {
    return objName;
  }

  public void setObjName(String objName) {
    this.objName = objName;
  }

  public String getWatchType() {
    return watchType;
  }

  public void setWatchType(String watchType) {
    this.watchType = watchType;
  }

  public String getAddType() {
    return addType;
  }

  public void setAddType(String addType) {
    this.addType = addType;
  }

  public String getAddName() {
    return addName;
  }
  
  public void setAddName(String addName) {
    this.addName = addName;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getCatName() {
    return catName;
  }

  public void setCatName(String catName) {
    this.catName = catName;
  }

  public String getForumName() {
    return forumName;
  }

  public void setForumName(String forumName) {
    this.forumName = forumName;
  }

  public String getTopicName() {
    return topicName;
  }

  public void setTopicName(String topicName) {
    this.topicName = topicName;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getDateFormat() {
    return dateFormat;
  }

  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  public String getTimeFormat() {
    return timeFormat;
  }

  public void setTimeFormat(String timeFormat) {
    this.timeFormat = timeFormat;
  }

  public String getZoneTime() {
    return zoneTime;
  }

  public void setZoneTime(String zoneTime) {
    this.zoneTime = zoneTime;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Map<String, String> getTypes() {
    return types;
  }

  public void setTypes(String cate, String forum, String topic, String post) {
    types = new HashMap<String, String>();
    types.put(Utils.CATEGORY, cate);
    types.put(Utils.FORUM, forum);
    types.put(Utils.TOPIC, topic);
    types.put(Utils.POST, post);
  }

  public void setPrivateLink() {
    try {
      String host = CommonUtils.getDomainURL();
      if(!CommonUtils.isEmpty(link)) {
        if(link.indexOf("http") == 0) {
          link = link.substring(link.indexOf(SLASH, 8));
        }
        String link = this.link;
        this.link = host + link;
        String ptContainer = link.substring(1, link.indexOf(SLASH, 2));
        privateLink = new StringBuilder(host).append(SLASH).append(ptContainer).append(SLASH).append("login?&initialURI=").append(link).append(SLASH).append(id).toString();
      }
    } catch (Exception e) {
      privateLink = link;
    }
  }

 
  private String getHeaderSubjectOfMessage(){
    String headerSubject = this.headerSubject; 
    if (headerSubject != null && headerSubject.length() > 0) {
      headerSubject = StringUtils.replace(headerSubject, "$CATEGORY", catName);
      headerSubject = StringUtils.replace(headerSubject, "$FORUM", forumName);
      headerSubject = StringUtils.replace(headerSubject, "$TOPIC", topicName);
    } else {
      headerSubject = "[" + catName + "][" + forumName + "]" + topicName;
    }
    return CommonUtils.decodeSpecialCharToHTMLnumber(headerSubject);
  }
  public Message getContentEmail() {
    setPrivateLink();
    Message message = new Message();
    message.setMimeType(ForumNodeTypes.TEXT_HTML);
    message.setFrom(owner);
    message.setSubject(getHeaderSubjectOfMessage());
    String content_ = StringUtils.replace(content, "$OBJECT_NAME", objName);
    content_ = StringUtils.replace(content_, "$OBJECT_WATCH_TYPE", types.get(watchType));
    content_ = StringUtils.replace(content_, "$ADD_TYPE", types.get(addType));
    content_ = StringUtils.replace(content_, "$ADD_NAME", addName);
    content_ = StringUtils.replace(content_, "$POST_CONTENT", this.message);
    Format formatter = new SimpleDateFormat(timeFormat);
    content_ = StringUtils.replace(content_, "$TIME", formatter.format(createdDate) + " " + zoneTime);
    formatter = new SimpleDateFormat(dateFormat);
    content_ = StringUtils.replace(content_, "$DATE", formatter.format(createdDate));
    content_ = StringUtils.replace(content_, "$POSTER", owner);
    content_ = StringUtils.replace(content_, "$VIEWPOST_LINK", link + SLASH + id);
    content_ = StringUtils.replace(content_, "$VIEWPOST_PRIVATE_LINK", privateLink);
    content_ = StringUtils.replace(content_, "$REPLYPOST_LINK", privateLink + "/true");

    content_ = StringUtils.replace(content_, "$CATEGORY", catName);
    content_ = StringUtils.replace(content_, "$FORUM", forumName);
    content_ = StringUtils.replace(content_, "$TOPIC", topicName);
    message.setBody(CommonUtils.convertCodeHTML(content_));
    return message;
  }

  public Message getContentEmailMoved() {
    setPrivateLink();
    Message message = new Message();
    message.setMimeType(ForumNodeTypes.TEXT_HTML);
    message.setFrom(owner);
    message.setSubject(getHeaderSubjectOfMessage());

    String content_ = StringUtils.replace(content, "$OBJECT_NAME", objName);
    content_ = StringUtils.replace(content_, "$OBJECT_PARENT_NAME", addType);
    content_ = StringUtils.replace(content_, "$POSTER", owner);
    content_ = StringUtils.replace(content_, "$VIEWPOST_LINK", link);
    content_ = StringUtils.replace(content_, "$VIEWPOST_PRIVATE_LINK", privateLink);
    content_ = StringUtils.replace(content_, "$REPLYPOST_LINK", privateLink + "/true");

    content_ = StringUtils.replace(content_, "$OBJECT_PARENT_TYPE", types.get(Utils.CATEGORY));
    content_ = StringUtils.replace(content_, "$OBJECT_TYPE", types.get(Utils.FORUM));
    message.setBody(CommonUtils.convertCodeHTML(content_));
    return message;
  }

}
