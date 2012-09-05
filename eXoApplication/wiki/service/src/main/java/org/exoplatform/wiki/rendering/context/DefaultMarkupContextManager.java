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
package org.exoplatform.wiki.rendering.context;

import javax.inject.Inject;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.rendering.reference.ObjectReferenceConverter;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiContext;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.ObjectReferenceResolver;
import org.xwiki.rendering.listener.reference.ResourceType;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Oct 27, 2011  
 */
public class DefaultMarkupContextManager implements MarkupContextManager {
  
  private static final Log    LOG           = ExoLogger.getLogger(DefaultMarkupContextManager.class);

  /**
   * Used to get the current syntax parser.
   */
  @Inject
  private ComponentManager    componentManager;

  /**
   * Used to get the current context
   */
  @Inject
  private Execution           execution;

  private static final String DEFAULT_WIKI  = "xwiki";

  private static final String DEFAULT_SPACE = "Main";

  private static final String DEFAULT_PAGE  = "WebHome";

  private static final String PORTAL        = "portal";

  private static final String CLASSIC       = "classic";

  private static final String WIKIHOME      = "Wiki_Home";

  @Override
  public WikiContext getMarkupContext(String objectName, ResourceType type) {

    WikiContext wikiMarkupContext = new WikiContext();
    try {
      DocumentReferenceResolver<String> stringDocumentReferenceResolver = 
        componentManager.getInstance(DocumentReferenceResolver.TYPE_STRING);
      AttachmentReferenceResolver<String> stringAttachmentReferenceResolver = 
        componentManager.getInstance(AttachmentReferenceResolver.TYPE_STRING);
      ObjectReferenceResolver<String> stringObjectReferenceResolver = 
        componentManager.getInstance(ObjectReferenceResolver.TYPE_STRING);
      ExecutionContext ec = execution.getContext();
      WikiContext wikiContext = null;
      if (ec != null) {
        wikiContext = (WikiContext) ec.getProperty(WikiContext.WIKICONTEXT);
        try {
          org.exoplatform.wiki.rendering.reference.ObjectReferenceConverter converter = 
            componentManager.getInstance(ObjectReferenceConverter.class, wikiContext.getSyntax());
          objectName = converter.convert(objectName);
        } catch (ComponentLookupException e) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Syntax %s doesn't have any object reference converter", wikiContext.getSyntax()));
          }
        }
      }   
      boolean isConfluenceSyntax = (objectName.indexOf('^') > 0) ? true : false;
      EntityReference entityReference = null;
      if (ResourceType.DOCUMENT.equals(type)) {
        entityReference = stringDocumentReferenceResolver.resolve(objectName);
      } else if (ResourceType.ATTACHMENT.equals(type) || ResourceType.ICON.equals(type)) {
        entityReference = (isConfluenceSyntax) ? stringObjectReferenceResolver.resolve(objectName)
                                              : stringAttachmentReferenceResolver.resolve(objectName);
      }
      
      if (entityReference != null) {
        wikiMarkupContext.setType(entityReference.extractReference(EntityType.WIKI).getName());
        wikiMarkupContext.setOwner(entityReference.extractReference(EntityType.SPACE).getName());
        wikiMarkupContext.setPageTitle(entityReference.extractReference(EntityType.DOCUMENT).getName());
        wikiMarkupContext.setPageId(wikiMarkupContext.getPageTitle());
        wikiMarkupContext.setPageId(TitleResolver.getId(wikiMarkupContext.getPageId(), false));
        EntityReference attachmentReference = (isConfluenceSyntax) ? entityReference.extractReference(EntityType.OBJECT)
                                                                  : entityReference.extractReference(EntityType.ATTACHMENT);
        if (attachmentReference != null) {
          wikiMarkupContext.setAttachmentName(attachmentReference.getName());
        }
        if (ResourceType.ICON.equals(type)) {
          wikiMarkupContext.setAttachmentName(wikiMarkupContext.getAttachmentName() + ".gif");
        }

        if (wikiContext != null) {
          wikiMarkupContext.setPortalURL(wikiContext.getPortalURL());
          wikiMarkupContext.setPortletURI(wikiContext.getPortletURI());
        } else {
          wikiContext = new WikiContext();
          wikiContext.setType(PORTAL);
          wikiContext.setOwner(CLASSIC);
          wikiContext.setPageId(WIKIHOME);
        }
        if (DEFAULT_WIKI.equals(wikiMarkupContext.getType())) {
          wikiMarkupContext.setType(wikiContext.getType());
        }
        if (DEFAULT_SPACE.equals(wikiMarkupContext.getOwner())) {
          wikiMarkupContext.setOwner(wikiContext.getOwner());
        }
        if (DEFAULT_PAGE.equals(wikiMarkupContext.getPageId())) {
          wikiMarkupContext.setPageId(wikiContext.getPageId());
        }
      }
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Couldn't get wiki context for markup: " + objectName, e);
      }
    }
    return wikiMarkupContext;
  }

}
