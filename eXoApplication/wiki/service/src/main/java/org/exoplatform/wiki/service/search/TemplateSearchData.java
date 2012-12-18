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
package org.exoplatform.wiki.service.search;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 28 Jan 2011  
 */
public class TemplateSearchData extends SearchData {
  private Log log = ExoLogger.getLogger(this.getClass());
  
  public static String TEMPLATE_PATH    = WikiNodeType.Definition.PREFERENCES + "/"
                                            + WikiNodeType.Definition.TEMPLATE_CONTAINER + "/%";

  public static String ALL_TEMPLATESPATH    = ALL_PATH + TEMPLATE_PATH;

  public static String PORTAL_TEMPLATESPATH = PORTAL_PATH + TEMPLATE_PATH;

  public static String GROUP_TEMPLATESPATH  = GROUP_PATH + TEMPLATE_PATH;

  public TemplateSearchData(String title, String wikiType, String wikiOwner) {
    super(null, title, null, wikiType, wikiOwner, null);
    createJcrQueryPath();
  }
 
  public void createJcrQueryPath() {
    if (wikiType == null && wikiOwner == null) {
      this.jcrQueryPath = "jcr:path LIKE '" + ALL_TEMPLATESPATH + "'";
    }
    if (wikiType != null) {
      if (wikiType.equals(PortalConfig.USER_TYPE))
        this.jcrQueryPath = "jcr:path LIKE '" + USER_PATH + TEMPLATE_PATH + "'";
      else {
        if (wikiType.equals(PortalConfig.PORTAL_TYPE)) {
          this.jcrQueryPath = "jcr:path LIKE '" + PORTAL_TEMPLATESPATH + "'";
        } else if (wikiType.equals(PortalConfig.GROUP_TYPE))
          this.jcrQueryPath = "jcr:path LIKE '" + GROUP_TEMPLATESPATH + "'";

        if (wikiOwner != null) {
          this.jcrQueryPath = this.jcrQueryPath.replaceFirst("%", wikiOwner);
        }
      }
    }
  }

  @Override
  public String getStatement() {
    StringBuilder statement = new StringBuilder();
    try {
      String title = Utils.escapeIllegalCharacterInQuery(getTitle());
      statement.append("SELECT title, jcr:primaryType, path,"+WikiNodeType.Definition.DESCRIPTION)
               .append(" FROM ")
               .append(WikiNodeType.WIKI_PAGE)
               .append(" WHERE ");
      statement.append(this.jcrQueryPath);
      if (title != null && title.length() > 0) {
        statement.append(" AND ")
                 .append(" CONTAINS(" + WikiNodeType.Definition.TITLE + ", '")
                 .append(title)
                 .append("') ");
      }
    } catch (Exception e) {
      log.debug("Failed to get statement ", e);
    }
    return statement.toString();
  }
}
