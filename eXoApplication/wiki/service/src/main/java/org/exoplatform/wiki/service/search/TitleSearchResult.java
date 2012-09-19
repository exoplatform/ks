/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.service.search;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Sep 22, 2010  
 */
public class TitleSearchResult {
  
  private static final Log log = ExoLogger.getLogger(TitleSearchResult.class);
  
  private String fullTitle;

  private String type;

  private String path;

  private String uri;
  
  private String fileType;

  public TitleSearchResult() {
  }

  public TitleSearchResult(String fullTitle, String path, String type, String fileType) {
    this.fullTitle = fullTitle;
    this.type = type;
    this.path = path;
    this.fileType = fileType;
    setUri();
  }

  public String getFullTitle() {
    return fullTitle;
  }

  public void setFullTitle(String fullTitle) {
    this.fullTitle = fullTitle;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getFileType() {
    return fileType;
  }
  
  public void setFileType(String fileType) {
    this.fileType = fileType;
  }
  private Wiki getWiki() throws Exception {
    Wiki searchWiki = null;
    if (WikiNodeType.WIKI_PAGE.equals(getType())) {
      PageImpl pageImpl = (PageImpl) org.exoplatform.wiki.utils.Utils.getObject(getPath(),
                                                                                getType());
      searchWiki = pageImpl.getWiki();
    }

    if (WikiNodeType.WIKI_ATTACHMENT.equals(getType())) {
      AttachmentImpl searchContent = (AttachmentImpl) org.exoplatform.wiki.utils.Utils.getObject(getPath(),
                                                                                                 getType());
      searchWiki = searchContent.getParentPage().getWiki();
    }
    return searchWiki;
  }

  private String getWikiType() throws Exception {
    return getWiki().getType();
  }

  public String getUri() {
    return uri;
  }

  private void setUri() {
    StringBuilder sb = new StringBuilder();
    try {
      if (WikiNodeType.WIKI_PAGE.equals(getType())) {
        String wikiType = getWikiType();
        if (!PortalConfig.PORTAL_TYPE.equalsIgnoreCase(wikiType)) {
          sb.append("/");
          sb.append(wikiType);
          sb.append("/");
          sb.append(Utils.validateWikiOwner(wikiType, getWiki().getOwner()));
        }
        sb.append(path.substring(path.lastIndexOf("/")));
      } else if (WikiNodeType.WIKI_ATTACHMENT.equals(getType())) {
        AttachmentImpl searchAtt = (AttachmentImpl) org.exoplatform.wiki.utils.Utils.getObject(getPath(),
                                                                                               getType());
        sb.append(searchAtt.getDownloadURL());
      }
    } catch (Exception e) {
      if (log.isWarnEnabled()) log.warn("failed to make uri for resource " + path, e);
    }
    uri = sb.toString();
  }
}
