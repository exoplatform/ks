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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.exoplatform.wiki.mow.api.Attachment;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.services.security.Identity;
import org.exoplatform.wiki.service.PermissionType;

/**
 * 
 * Simple {@link Page} implementation, includes only getter and setter methods.
 * <p> 
 * Created by The eXo Platform SAS
 * @Author <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a>
 * Apr 25, 2011
 * </p>  
 */
public class SimplePageImpl implements Page {
  
  private Collection<? extends Attachment> attachments;
  private String author;
  private String comment;
  private Attachment content;
  private Date createDate;
  private String name;
  private String owner;
  private HashMap<String, String[]> permission;
  private String syntax;
  private String title;
  private Date updateDate;
  private boolean hasPermission;
  private String url;
  
  public SimplePageImpl(String name, String title, String owner) {
    this.name = name;
    this.title = title;
    this.owner = owner;
  }
  
  public SimplePageImpl author(String author) {
    this.author = author;
    return this;
  }
  
  public SimplePageImpl comment(String comment) {
    this.comment = comment;
    return this;
  }
  
  public SimplePageImpl url(String url) {
    this.url = url;
    return this;
  }
  
  public SimplePageImpl attachments(Collection<? extends Attachment> attachments) {
    this.attachments = attachments;
    return this;
  }
  
  public SimplePageImpl createDate(Date date) {
    this.createDate = date;
    return this;
  }
  
  public SimplePageImpl updateDate(Date date) {
    this.updateDate = date;
    return this;
  }
  
  public SimplePageImpl hasPermission(boolean hasPermission) {
    this.hasPermission = hasPermission;
    return this;
  }
  
  public SimplePageImpl permission(HashMap<String, String[]> permission) {
    this.permission = permission;
    return this;
  }
  
  public SimplePageImpl syntax(String syntax) {
    this.syntax = syntax;
    return this;
  }
  
  @Override
  public Collection<? extends Attachment> getAttachments() {
    return attachments;
  }

  @Override
  public String getAuthor() {
    return author;
  }

  @Override
  public String getComment() {
    return comment;
  }

  @Override
  public Attachment getContent() {
    return content;
  }

  @Override
  public Date getCreatedDate() {
    return createDate;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getOwner() {
    return owner;
  }

  @Override
  public HashMap<String, String[]> getPermission() throws Exception {
    return permission;
  }

  @Override
  public String getSyntax() {
    return syntax;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public Date getUpdatedDate() {
    return updateDate;
  }

  @Override
  public boolean hasPermission(PermissionType permissionType) throws Exception {
    return hasPermission;
  }
  
  @Override
  public boolean hasPermission(PermissionType permissionType, Identity user) throws Exception {
    return hasPermission;
  }

  @Override
  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public void setPermission(HashMap<String, String[]> permissions) throws Exception {
    if (permissions != null)
      this.permission = new HashMap<String, String[]>(permissions); 
    
  }

  @Override
  public void setSyntax(String syntax) {
    this.syntax = syntax;
    
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getURL() {
    return this.url;
  }

  
}
