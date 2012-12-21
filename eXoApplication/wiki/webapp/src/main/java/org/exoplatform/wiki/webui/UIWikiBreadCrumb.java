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
package org.exoplatform.wiki.webui;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.service.BreadcrumbData;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiBreadCrumb.gtmpl"
)
public class UIWikiBreadCrumb extends UIContainer {

  private List<BreadcrumbData> breadCumbs = new ArrayList<BreadcrumbData>();

  private String               actionLabel;

  private boolean              isLink     = true;

  public List<BreadcrumbData> getBreadCumbs() {
    return breadCumbs;
  }

  public void setBreadCumbs(List<BreadcrumbData> breadCumbs) {
    this.breadCumbs = breadCumbs;
  }
  
  public String getActionLabel() {
    return actionLabel;
  }

  public void setActionLabel(String actionLabel) {
    this.actionLabel = actionLabel;
  }  

  public String getParentURL() throws Exception {
    if(breadCumbs.size() > 1) {
      return createActionLink(breadCumbs.get(breadCumbs.size() - 2)) ;
    }else {
      return createActionLink(breadCumbs.get(0)) ;
    }     
  }
  
  public boolean isLink() {
    return isLink;
  }

  public void setLink(boolean isLink) {
    this.isLink = isLink;
  }
  
  public WikiPageParams getPageParam() throws Exception {
    if (this.breadCumbs != null && this.breadCumbs.size() > 0) {
      WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
      return wservice.getWikiPageParams(breadCumbs.get(breadCumbs.size() - 1));
    }
    return null;
  }

  public String getWikiType() throws Exception {
    if (getPageParam() != null) {
      return getPageParam().getType();
    }
    return null;
  }

  public String getWikiName() throws Exception {
    if (getPageParam() != null) {
      return getPageParam().getOwner();
    }
    return null;
  }
  
  private String fillPortalName(String url) {
  	RequestContext ctx = RequestContext.getCurrentInstance();
  	NodeURL nodeURL =  ctx.createURL(NodeURL.TYPE);
  	NavigationResource resource = new NavigationResource(SiteType.PORTAL, Util.getPortalRequestContext().getPortalOwner(), url);
  	return nodeURL.setResource(resource).toString(); 
  }
  
  public String createActionLink(BreadcrumbData breadCumbData) throws Exception {
  	String wikiWebappUri = "wiki";
  	StringBuilder sb = new StringBuilder(wikiWebappUri);
  	sb.append("/");
  	if (!WikiType.PORTAL.toString().equalsIgnoreCase(breadCumbData.getWikiType())) {
  		sb.append(breadCumbData.getWikiType().toLowerCase());
  		sb.append("/");
  		sb.append(org.exoplatform.wiki.utils.Utils.validateWikiOwner(breadCumbData.getWikiType(), breadCumbData.getWikiOwner()));
  		sb.append("/");
  	}
  	if (breadCumbData.getId() != null) {
  		sb.append(URLEncoder.encode(breadCumbData.getId(), "UTF-8"));
	  }
  	return fillPortalName(sb.toString());
	}
}
