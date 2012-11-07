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
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.utils.WikiNameValidator;
import org.exoplatform.wiki.webui.EditMode;
import org.exoplatform.wiki.webui.UIWikiPageControlArea;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiRichTextArea;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.filter.IsEditAddModeFilter;
import org.exoplatform.wiki.webui.control.filter.IsEditAddPageModeFilter;
import org.exoplatform.wiki.webui.control.listener.UISubmitToolBarActionListener;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/action/SavePageActionComponent.gtmpl",
  events = {
    @EventConfig(listeners = SavePageActionComponent.SavePageActionListener.class, phase = Phase.DECODE)
  }
)
public class SavePageActionComponent extends UIComponent {

  public static final String                   ACTION   = "SavePage";  
  
  private static final Log log = ExoLogger.getLogger("wiki:SavePageActionComponent");
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] {
      new IsEditAddModeFilter(), new IsEditAddPageModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }  

  protected boolean isNewMode() {
    return (WikiMode.ADDPAGE.equals(getAncestorOfType(UIWikiPortlet.class).getWikiMode()));
  }
  
  protected String getPageTitleInputId() {
    return UIWikiPageTitleControlArea.FIELD_TITLEINPUT;
  }
  
  protected String getActionLink() throws Exception {
    return Utils.createFormActionLink(this, ACTION, ACTION);
  }  
  
  public static class SavePageActionListener extends
                                            UISubmitToolBarActionListener<SavePageActionComponent> {
    @Override
    protected void processEvent(Event<SavePageActionComponent> event) throws Exception {
      WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
      UIWikiPageTitleControlArea pageTitleControlForm = wikiPortlet.findComponentById(UIWikiPageControlArea.TITLE_CONTROL);
      UIWikiPageEditForm pageEditForm = wikiPortlet.findFirstComponentOfType(UIWikiPageEditForm.class);
      UIWikiRichTextArea wikiRichTextArea = pageEditForm.getChild(UIWikiRichTextArea.class);
      UIFormStringInput titleInput = pageEditForm.getChild(UIWikiPageTitleControlArea.class)
                                                 .getUIStringInput();      
      UIFormTextAreaInput markupInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);
      UIFormStringInput commentInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_COMMENT);
      String syntaxId = Utils.getDefaultSyntax();
      RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
      Page page = Utils.getCurrentWikiPage();
      Utils.setUpWikiContext(wikiPortlet);
      try {
        WikiNameValidator.validate(titleInput.getValue());
      } catch (IllegalNameException ex) {
    	  if (wikiRichTextArea.isRendered()) {
    	        String htmlContent = wikiRichTextArea.getUIFormTextAreaInput().getValue();
    	        String markupContent = renderingService.render(htmlContent,
    	                                                       Syntax.XHTML_1_0.toIdString(),
    	                                                       syntaxId,
    	                                                       false);
    	        markupInput.setValue(markupContent);
    	        wikiPortlet.changeMode(WikiMode.VIEW);
    	        super.processEvent(event);
    	      }
        String msg = ex.getMessage();
        ApplicationMessage appMsg = new ApplicationMessage("WikiPageNameValidator.msg.EmptyTitle",
                                                           null,
                                                           ApplicationMessage.WARNING);
        if (msg != null) {
          Object[] arg = { msg };
          appMsg = new ApplicationMessage("WikiPageNameValidator.msg.Invalid-char",
                                          arg,
                                          ApplicationMessage.WARNING);
        }
        event.getRequestContext().getUIApplication().addMessage(appMsg);
        event.getRequestContext().setProcessRender(true);
      }
      if (event.getRequestContext().getProcessRender()) {
        Utils.redirect(pageParams, wikiPortlet.getWikiMode());
        return;
      }

      String title = titleInput.getValue().trim();
      if (wikiRichTextArea.isRendered()) {
        String htmlContent = wikiRichTextArea.getUIFormTextAreaInput().getValue();
        String markupContent = renderingService.render(htmlContent,
                                                       Syntax.XHTML_1_0.toIdString(),
                                                       syntaxId,
                                                       false);
        markupInput.setValue(markupContent);
      }
      String markup = (markupInput.getValue() == null) ? "" : markupInput.getValue();
      markup = markup.trim();
      
      
      String newPageId = TitleResolver.getId(title, false);
      if (WikiNodeType.Definition.WIKI_HOME_NAME.equals(page.getName()) && wikiPortlet.getWikiMode() == WikiMode.EDITPAGE) {
        // as wiki home page has fixed name (never edited anymore), every title changing is accepted. 
        ;
      } else if (newPageId.equals(page.getName()) && wikiPortlet.getWikiMode() == WikiMode.EDITPAGE) {
        // if page title is not changed in editing phase, do not need to check its existence.
        ;
      } else if (wikiService.isExisting(pageParams.getType(), pageParams.getOwner(), newPageId)) {
        // if new page title is duplicated with existed page's.
        if (log.isDebugEnabled()) log.debug("The title '" + title + "' is already existing!");
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("SavePageAction.msg.warning-page-title-already-exist",
                                                null,
                                                ApplicationMessage.WARNING));
        Utils.redirect(pageParams, wikiPortlet.getWikiMode());
        return;
      }
      
      try {
        if (wikiPortlet.getWikiMode() == WikiMode.EDITPAGE) {
          if (wikiPortlet.getEditMode() == EditMode.SECTION) {
            newPageId = page.getName();
            title = page.getTitle();
            markup = renderingService.updateContentOfSection(page.getContent().getText(),
                                                             page.getSyntax(),
                                                             wikiPortlet.getSectionIndex(),
                                                             markup);
          }
          if (!page.getName().equals(newPageId)) {
            wikiService.renamePage(pageParams.getType(),
                                   pageParams.getOwner(),
                                   page.getName(),
                                   newPageId,
                                   title);
          }
          Object minorAtt = event.getRequestContext().getAttribute(MinorEditActionComponent.ACTION);
          if (minorAtt != null) {
            ((PageImpl) page).setMinorEdit(Boolean.parseBoolean(minorAtt.toString()));
          }
          synchronized (((PageImpl) page).getJCRPageNode().getUUID()) {
            page.setComment(StringEscapeUtils.escapeHtml(commentInput.getValue()));
            page.setSyntax(syntaxId);
            pageTitleControlForm.getUIFormInputInfo().setValue(title);
            pageParams.setPageId(page.getName());
            ((PageImpl) page).setURL(Utils.getURLFromParams(pageParams));
            page.getContent().setText(markup);

            if (!pageEditForm.getTitle().equals(title)) {
              page.setTitle(title);
              ((PageImpl) page).checkin();
              ((PageImpl) page).checkout();
              pageParams.setPageId(newPageId);
            } else {
              ((PageImpl) page).checkin();
              ((PageImpl) page).checkout();
            }
          }
        } else if (wikiPortlet.getWikiMode() == WikiMode.ADDPAGE) {
          String sessionId = Util.getPortalRequestContext().getRequest().getSession(false).getId();
          Page draftPage = wikiService.getExsitedOrNewDraftPageById(null, null, sessionId);
          Collection<AttachmentImpl> attachs = ((PageImpl) draftPage).getAttachments();

          Page subPage = wikiService.createPage(pageParams.getType(),
                                                pageParams.getOwner(),
                                                title,
                                                page.getName());
          pageParams.setPageId(newPageId);
          ((PageImpl) subPage).setURL(Utils.getURLFromParams(pageParams));
          subPage.getContent().setText(markup);
          subPage.setSyntax(syntaxId);
          ((PageImpl) subPage).getAttachments().addAll(attachs);
          ((PageImpl) subPage).checkin();
          ((PageImpl) subPage).checkout();         
          ((PageImpl) draftPage).remove();
          return;
        }
      } catch (Exception e) {
        log.error("An exception happens when saving the page with title:" + title, e);
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIPageToolBar.msg.Exception",
                                                                                       null,
                                                                                       ApplicationMessage.ERROR));
      } finally {
        wikiPortlet.changeMode(WikiMode.VIEW);
        Utils.redirect(pageParams, WikiMode.VIEW);
        super.processEvent(event);
      }
    }
  }

}
