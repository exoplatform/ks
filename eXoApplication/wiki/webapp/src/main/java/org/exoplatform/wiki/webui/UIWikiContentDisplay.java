/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.webui;

import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.utils.WikiNameValidator;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Sep 16, 2011  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiContentDisplay.gtmpl"
)
public class UIWikiContentDisplay extends UIContainer {
  private String htmlOutput;
  
  protected static final String INVALID_CHARACTERS = WikiNameValidator.INVALID_CHARACTERS;

  public String getHtmlOutput() {
    return htmlOutput;
  }

  public void setHtmlOutput(String htmlOutput) {
    this.htmlOutput = htmlOutput;
  }
  
  public boolean isWelcomePage() throws Exception {
    PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
    if(wikipage instanceof WikiHome) {
      int versionTotals = wikipage.getVersionableMixin().getVersionHistory().getChildren().size() - 1;
      if(versionTotals == 1) return true;
    }
    return false;    
  }
  
  public String getWelcomeMessage() throws Exception {
    String welcomeMessage = "";
    PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
    String portalOwner = wikipage.getWiki().getOwner();
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    StringBuilder sb = new StringBuilder("{{tip}}\n" + res.getString("UIWikiWelcomePage.label.welcome") +" ");
    sb.append(portalOwner).append(" ");
    if (WikiType.PORTAL.toString().equals(wikipage.getWiki().getType().toUpperCase())) {
      sb.append("portal.");
    } else {
      sb.append("group.");
    }
    String sandboxMessage = res.getString("UIWikiWelcomePage.label.sandbox");
    sandboxMessage = sandboxMessage.replaceFirst("sandbox", "**[[Sandbox space>>group:sandbox.WikiHome]]**");
    sb.append("\n* " + sandboxMessage + "\n{{/tip}}");
    RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
    //Setup wiki context
    UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
    Utils.setUpWikiContext(wikiPortlet);
    welcomeMessage = renderingService.render(sb.toString(), wikipage.getSyntax(), Syntax.XHTML_1_0.toIdString(), false);  	
    return welcomeMessage;
  }
}
