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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.utils.Utils;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiSidePanelArea.gtmpl"
)
public class UIWikiSidePanelArea extends UIContainer {  
  
  private String syntaxName;
  
  private String syntaxFullPageUrl;

  private String htmlOutput;

  public String getHtmlOutput() {
    return htmlOutput;
  }
  
  public void setHtmlOutput(String output) {
    this.htmlOutput = output;
  }
  
  public String getSyntaxName() {
    return syntaxName;
  }  
  public void setSyntaxName(String syntaxName) {
    this.syntaxName = syntaxName;
  }
  
  public String getSyntaxFullPageUrl() {
    return syntaxFullPageUrl;
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
    WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
    String syntaxId = org.exoplatform.wiki.commons.Utils.getDefaultSyntax();
    
    UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
    WikiContext wikiContext= org.exoplatform.wiki.commons.Utils.setUpWikiContext(wikiPortlet);
    
    PageImpl syntaxHelpPage = wikiService.getHelpSyntaxPage(syntaxId);
    this.syntaxName = syntaxId.replace("/", " ").toUpperCase();
    if (syntaxHelpPage != null) {
      String markup = syntaxHelpPage.getContent().getText();
      this.htmlOutput = renderingService.render(markup, syntaxId, Syntax.XHTML_1_0.toIdString(), false);
      this.syntaxFullPageUrl = "/" + PortalContainer.getInstance().getRestContextName() + "/wiki/help/"
          + syntaxId.replace("/", Utils.SLASH).replace(".", Utils.DOT)
          + "?portalUrl=" + wikiContext.getPortalURL();
    } else {
      this.htmlOutput = null;
    }
    super.processRender(context);
  }
}
