/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.impl;

import java.net.URLEncoder;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.cache.PageRenderingCacheService;
import org.exoplatform.wiki.rendering.context.MarkupContextManager;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.MetaDataPage;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.utils.Utils;
import org.exoplatform.wiki.utils.WikiNameValidator;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.wiki.WikiModel;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */
@Component
public class DefaultWikiModel implements WikiModel {
  
  private static final Log    LOG           = ExoLogger.getLogger(DefaultWikiModel.class);
  
  /**
   * Used to get the current context
   */
  @Inject
  private Execution execution;
  
  /**
   * Used to get the build context for document
   */
  @Inject
  private MarkupContextManager markupContextManager;
  
  @Override
  public String getDocumentEditURL(ResourceReference documentReference) {
    WikiContext wikiContext = getWikiContext();
    WikiContext wikiMarkupContext = markupContextManager.getMarkupContext(documentReference.getReference(),ResourceType.DOCUMENT);
    if (wikiContext != null) {
      StringBuilder sb = new StringBuilder();
      String pageTitle = wikiMarkupContext.getPageTitle();
      String wikiType = wikiMarkupContext.getType();
      String wiki = wikiMarkupContext.getOwner();
      try {
        WikiNameValidator.validate(pageTitle);
        sb.append(getDocumentViewURL(wikiContext));
        sb.append("?")
          .append(WikiContext.ACTION)
          .append("=")
          .append(WikiContext.ADDPAGE)
          .append("&")
          .append(WikiContext.PAGETITLE)
          .append("=")
          .append(pageTitle)
          .append("&")
          .append(WikiContext.WIKI)
          .append("=")
          .append(wiki)
          .append("&")
          .append(WikiContext.WIKITYPE)
          .append("=")
          .append(wikiType);
      } catch (IllegalNameException ex) {
        sb.append(String.format("javascript:void(0);"));
      }
      return sb.toString();
    }
    return "";
  }

  @Override
  public String getDocumentViewURL(ResourceReference documentReference) {    
    WikiContext wikiMarkupContext = markupContextManager.getMarkupContext(documentReference.getReference(), ResourceType.DOCUMENT);   
    return getDocumentViewURL(wikiMarkupContext);
  }

  @Override
  public String getImageURL(ResourceReference imageReference, Map<String, String> parameters) {
    String imageName = imageReference.getReference();
    StringBuilder sb = new StringBuilder();
    try {

      PageRenderingCacheService renderingCacheService = (PageRenderingCacheService) ExoContainerContext.getCurrentContainer()
                                                                                                       .getComponentInstanceOfType(PageRenderingCacheService.class);
      ResourceType resourceType = ResourceType.ICON.equals(imageReference.getType()) ? ResourceType.ICON : ResourceType.ATTACHMENT;
      WikiContext wikiMarkupContext = markupContextManager.getMarkupContext(imageName, resourceType);
      String portalContainerName = PortalContainer.getCurrentPortalContainerName();
      String portalURL = wikiMarkupContext.getPortalURL();
      String domainURL = portalURL.substring(0, portalURL.indexOf("/"+portalContainerName));
      WikiContext context =getWikiContext();
      renderingCacheService.addPageLink(new WikiPageParams(context.getType(), context.getOwner(), context.getPageId()),
                                        new WikiPageParams(wikiMarkupContext.getType(),
                                                           wikiMarkupContext.getOwner(),
                                                           wikiMarkupContext.getPageId()));
      renderingCacheService.addPageLink(new WikiPageParams(context.getType(), context.getOwner(), context.getPageId()),
                                        new WikiPageParams(wikiMarkupContext.getType(),
                                                           wikiMarkupContext.getOwner(),
                                                           wikiMarkupContext.getPageId(),
                                                           wikiMarkupContext.getAttachmentName()));   
      sb.append(domainURL).append(Utils.getCurrentRepositoryWebDavUri());
      PageImpl page = null;
      WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
      if (ResourceType.ATTACHMENT.equals(resourceType)) {
        page = (PageImpl) wikiService.getExsitedOrNewDraftPageById(wikiMarkupContext.getType(), wikiMarkupContext.getOwner(), wikiMarkupContext.getPageId());
      } else {
        page = (PageImpl) wikiService.getMetaDataPage(MetaDataPage.EMOTION_ICONS_PAGE);
      }
      
      if (page != null) {
        sb.append(page.getWorkspace());
        sb.append(page.getPath());
        sb.append("/");
        AttachmentImpl att = page.getAttachment(TitleResolver.getId(wikiMarkupContext.getAttachmentName(), false));
        if (att != null) {
          sb.append(URLEncoder.encode(att.getName(), "UTF-8"));
        }
      } else {
        // If can not find the resource then return url empty
        return StringUtils.EMPTY;
      }
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Couldn't get attachment URL for attachment: " + imageName, e);
      }
    }
    return sb.toString();
  }

  @Override
  public String getLinkURL(ResourceReference linkReference) {
    return getImageURL(linkReference, null);
  }

  @Override
  public boolean isDocumentAvailable(ResourceReference documentReference) {
    // Should look for pages in the model with the given title
    // (Page.findPageByTitle())
    
    WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer()
    .getComponentInstanceOfType(WikiService.class);
    PageRenderingCacheService renderingCacheService = (PageRenderingCacheService) ExoContainerContext.getCurrentContainer()
                                          .getComponentInstanceOfType(PageRenderingCacheService.class);
    
    Page page = null;
    String documentName = documentReference.getReference();
    ResourceType type = documentReference.getType();
    WikiContext wikiMarkupContext = markupContextManager.getMarkupContext(documentName, type);
    WikiContext wikiContext = getWikiContext();
    try {
      renderingCacheService.addPageLink(new WikiPageParams(wikiContext.getType(), wikiContext.getOwner(), wikiContext.getPageId()),
                                        new WikiPageParams(wikiMarkupContext.getType(),
                                                           wikiMarkupContext.getOwner(),
                                                           wikiMarkupContext.getPageId()));
    } catch (Exception e) {
      LOG.warn(String.format("Failed to link incoming pages for page %s", documentReference.toString()), e);
    }
    try {
      if (!Utils.isWikiAvailable(wikiMarkupContext.getType(), wikiMarkupContext.getOwner())) {
        return false;
      } else {
        page = wikiService.getPageById(wikiMarkupContext.getType(),
                                       wikiMarkupContext.getOwner(),
                                       wikiMarkupContext.getPageId());
        if (page == null) {
          page = wikiService.getRelatedPage(wikiMarkupContext.getType(), wikiMarkupContext.getOwner(), wikiMarkupContext.getPageId());
          if (page!=null) {
            renderingCacheService.addPageLink(new WikiPageParams(wikiContext.getType(), wikiContext.getOwner(), wikiContext.getPageId()),
                                              new WikiPageParams(wikiMarkupContext.getType(),
                                                                 wikiMarkupContext.getOwner(),
                                                                 page.getName()));
          }
          return page != null;
        } 
      }

    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("An exception happened when checking available status of document: "
            + documentName, e);
      }
      return false;
    }
    return true;
  }

  private String getDocumentViewURL(WikiContext context) {
    try {
      WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
      PageImpl page = (PageImpl) wikiService.getPageById(context.getType(), context.getOwner(), context.getPageId());
      if (page == null) {
        page = (PageImpl) wikiService.getRelatedPage(context.getType(), context.getOwner(), context.getPageId());
      }
      if (page != null) {
        Wiki wiki = page.getWiki();
        context.setType(wiki.getType());
        context.setOwner(wiki.getOwner());
        context.setPageId(page.getName());
      }
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("An exception happened when process broken link.", e);
      }
    }
    return Utils.getDocumentURL(context);
  }
  
  private WikiContext getWikiContext() {
    ExecutionContext ec = execution.getContext();
    if (ec != null) {
      WikiContext wikiContext = (WikiContext) ec.getProperty(WikiContext.WIKICONTEXT);
      return wikiContext;
    }
    return null;
  }
}
