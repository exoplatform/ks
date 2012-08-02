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
package org.exoplatform.wiki.webui.control.action;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.webui.EditMode;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiRichTextArea;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.action.core.AbstractEventActionComponent;
import org.exoplatform.wiki.webui.control.filter.EditPagesPermissionFilter;
import org.exoplatform.wiki.webui.control.filter.IsViewModeFilter;
import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/action/AbstractActionComponent.gtmpl",
  events = {
    @EventConfig(listeners = EditPageActionComponent.EditPageActionListener.class)
  }
)
public class EditPageActionComponent extends AbstractEventActionComponent {
  
  public static final String                   ACTION  = "EditPage";

  public static final String                   SECTION = "section";

  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] {
      new IsViewModeFilter(), new EditPagesPermissionFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }  

  @Override
  public String getActionName() {
    return ACTION;
  }

  @Override
  public boolean isAnchor() {
    return true;
  }
  
  public static class EditPageActionListener extends UIPageToolBarActionListener<EditPageActionComponent> {
    @Override
    protected void processEvent(Event<EditPageActionComponent> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      String sectionIndex = event.getRequestContext().getRequestParameter(SECTION);
      if (sectionIndex == null || sectionIndex.length() == 0) {
        wikiPortlet.changeEditMode(EditMode.ALL);
      } else {
        wikiPortlet.changeEditMode(EditMode.SECTION);
        wikiPortlet.setSectionIndex(sectionIndex);
      }
      UIWikiPageEditForm pageEditForm = wikiPortlet.findFirstComponentOfType(UIWikiPageEditForm.class);
      UIFormStringInput titleInput = pageEditForm.getChild(UIWikiPageTitleControlArea.class).getUIStringInput();
      UIFormTextAreaInput markupInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);
      UIFormStringInput commentInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_COMMENT);
      
      PageImpl page = (PageImpl)Utils.getCurrentWikiPage();
      String title = page.getTitle();
      //Check if current page is welcome page
      int versionTotals = page.getVersionableMixin().getVersionHistory().getChildren().size() - 1;
      String content = "";
      if(page instanceof WikiHome && versionTotals ==1) {
      	 String portalOwner = page.getWiki().getOwner();
      	 WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      	 ResourceBundle res = context.getApplicationResourceBundle();
      	 StringBuilder sb = new StringBuilder("{{tip}}\n" + res.getString("UIWikiWelcomePage.label.welcome") +" ");
      	 sb.append(portalOwner).append(" ");
         if (WikiType.PORTAL.toString().equals(page.getWiki().getType().toUpperCase())) {
           sb.append("portal.");
         } else {
           sb.append("group.");
         }
         String sandboxMessage = res.getString("UIWikiWelcomePage.label.sandbox");
         sandboxMessage = sandboxMessage.replaceFirst("sandbox", "**[[Sandbox space>>group:sandbox.WikiHome]]**");
         sb.append("\n* " + sandboxMessage + "\n{{/tip}}");
         content = sb.toString();
      } else content = page.getContent().getText();
      
      titleInput.setEditable(true);
      if (wikiPortlet.getEditMode() == EditMode.SECTION) {
        RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
        content = renderingService.getContentOfSection(content, page.getSyntax(), sectionIndex);
        titleInput.setEditable(false);
      }
      titleInput.setValue(title);
      pageEditForm.setTitle(title) ;
      markupInput.setValue(content);
      commentInput.setValue("");
      commentInput.setRendered(true);
      UIWikiRichTextArea wikiRichTextArea = pageEditForm.getChild(UIWikiRichTextArea.class);
      if (wikiRichTextArea.isRendered()) {
        Utils.feedDataForWYSIWYGEditor(pageEditForm, null);
      }      
      wikiPortlet.changeMode(WikiMode.EDITPAGE);
      super.processEvent(event);
    }
  }
}
