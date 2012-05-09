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
package org.exoplatform.wiki.webui.control;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.service.BreadcrumbData;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiBreadCrumb;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 30 Mar 2011  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/UIRelatedPagesContainer.gtmpl"
)
public class UIRelatedPagesContainer extends UIWikiExtensionContainer {
  
  private static final Log log                     = ExoLogger.getLogger(UIRelatedPagesContainer.class);
  
  public static final String EXTENSION_TYPE = "org.exoplatform.wiki.webui.control.UIRelatedPagesContainer";

  private UIWikiBreadCrumb breadcrumb;

  public UIRelatedPagesContainer() throws Exception {
    super(); 
    breadcrumb = addChild(UIWikiBreadCrumb.class, null, "UIWikiBreadCrumb_PageInfo");
    breadcrumb.setLink(true);
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context);    
  }
  
  protected boolean isHasExtension() {
    return extensionSize > 0;
  }

  @Override
  public String getExtensionType() {
    return EXTENSION_TYPE;
  }

  public List<BreadcrumbData> getBreadcrumbDatas(Page page) {
    WikiService service = getApplicationComponent(WikiService.class);
    WikiPageParams params = org.exoplatform.wiki.utils.Utils.getWikiPageParams(page);
    try {
      return service.getBreadcumb(params.getType(), params.getOwner(), params.getPageId());
    } catch (Exception e) {
      if (log.isWarnEnabled()) {
        log.warn(String.format("can not load BreadcrumbData for page [%s]", page.getName()), e);
      }
      return new ArrayList<BreadcrumbData>();
    }
  }
  
  protected Page getCurrentPage() throws Exception {
    return Utils.getCurrentWikiPage();
  }
}
