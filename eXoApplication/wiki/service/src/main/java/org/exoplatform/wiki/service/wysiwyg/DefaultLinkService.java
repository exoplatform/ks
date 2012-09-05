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
package org.exoplatform.wiki.service.wysiwyg;

import javax.inject.Inject;
import javax.inject.Named;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.utils.Utils;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.gwt.wysiwyg.client.wiki.EntityConfig;
import org.xwiki.gwt.wysiwyg.client.wiki.URIReference;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.renderer.reference.ResourceReferenceSerializer;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 12, 2010  
 */
@Component
public class DefaultLinkService implements LinkService {
  
  /** Execution context handler, needed for accessing the WikiContext. */
  @Inject
  private Execution execution;
  
  /**
   * The component used to serialize XWiki document references.
   */
  @Inject
  @Named("compact")
  private EntityReferenceSerializer<String>        entityReferenceSerializer;

  /**
   * The component used to resolve an entity reference relative to another
   * entity reference.
   */
  @Inject
  @Named("explicit")
  private EntityReferenceResolver<EntityReference> explicitReferenceEntityReferenceResolver;

  /**
   * The component used to resolve a string entity reference relative to another
   * entity reference.
   */
  @Inject
  @Named("explicit")
  private EntityReferenceResolver<String>          explicitStringEntityReferenceResolver;

  /**
   * The component used to serialize link references.
   * <p>
   * Note: The link reference syntax is independent of the syntax of the edited
   * document. The current hint should be replaced with a generic one to avoid
   * confusion.
   */
  @Inject
  @Named("xhtmlmarker")
  private ResourceReferenceSerializer              linkReferenceSerializer;

  /**
   * The component used to parser link references.
   * <p>
   * Note: The link reference syntax is independent of the syntax of the edited
   * document. The current hint should be replaced with a generic one to avoid
   * confusion.
   */
  @Inject
  @Named("xhtmlmarker")
  private ResourceReferenceParser                  linkReferenceParser;

  /**
   * The object used to convert between client and server entity reference.
   */
  private final EntityReferenceConverter           entityReferenceConverter = new EntityReferenceConverter();
  
  /**
   * Log exception.
   */  
  private static final Log      log               = ExoLogger.getLogger(EntityReferenceConverter.class);

  /**
   * {@inheritDoc}
   * 
   * @see LinkService#getEntityConfig(org.xwiki.gwt.wysiwyg.client.wiki.EntityReference,
   *      org.xwiki.gwt.wysiwyg.client.wiki.ResourceReference)
   */
  public EntityConfig getEntityConfig(org.xwiki.gwt.wysiwyg.client.wiki.EntityReference origin,
                                      org.xwiki.gwt.wysiwyg.client.wiki.ResourceReference destination) {
    String url;
    String destRelativeStrRef;
    
    if (org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType.EXTERNAL == destination.getEntityReference()
                                                                                            .getType()) {
      url = new URIReference(destination.getEntityReference()).getURI();
      destRelativeStrRef = url;
    } else {
      EntityReference originRef = entityReferenceConverter.convert(origin);
      EntityReference destRef = entityReferenceConverter.convert(destination.getEntityReference());
      destRef = explicitReferenceEntityReferenceResolver.resolve(destRef,
                                                                 destRef.getType(),
                                                                 originRef);
      destRelativeStrRef = entityReferenceSerializer.serialize(destRef, originRef);
      url = getEntityURL(destRef);
    }

    EntityConfig entityConfig = new EntityConfig();
    entityConfig.setUrl(url);
    entityConfig.setReference(getLinkReference(destination.getType(),
                                               destination.isTyped(),
                                               destRelativeStrRef));
    return entityConfig;
  }

  /**
   * @param entityReference an entity reference
   * @return the URL to access the specified entity
   */
  private String getEntityURL(EntityReference entityReference) {
    org.exoplatform.wiki.service.WikiService wservice = (org.exoplatform.wiki.service.WikiService) PortalContainer.getComponent(org.exoplatform.wiki.service.WikiService.class);
    WikiContext wikiContext = getWikiContext();
    WikiContext context = new WikiContext();
    context.setPortalURL(wikiContext.getPortalURL());
    context.setPortletURI(wikiContext.getPortletURI());
    PageImpl page;
    switch (entityReference.getType()) {
    case DOCUMENT:
      String pageId = TitleResolver.getId(entityReference.getName(), false);
      String wikiOwner = entityReference.getParent().getName();
      String wikiType = entityReference.getParent().getParent().getName();
      context.setType(wikiType);
      context.setOwner(wikiOwner);
      context.setPageId(pageId);
      try {
        boolean isPageExisted = wservice.isExisting(wikiType, wikiOwner, pageId);
        if (isPageExisted) {
          return Utils.getDocumentURL(context);
        }
      } catch (Exception e) {
        log.error("Exception happen when finding page " + pageId, e);
      }
      return null;
    case ATTACHMENT:
      String attachmentId = entityReference.getName();
      pageId = TitleResolver.getId(entityReference.getParent().getName(), false);
      wikiOwner = entityReference.getParent().getParent().getName();
      wikiType = entityReference.getParent().getParent().getParent().getName();
      try {
        page = (PageImpl) wservice.getExsitedOrNewDraftPageById(wikiType, wikiOwner, pageId);
        AttachmentImpl att = page.getAttachment(TitleResolver.getId(attachmentId, false));
        if (att != null) {
          return att.getDownloadURL();
        }
      } catch (Exception e) {
        log.error("Exception happen when finding attachment " + attachmentId, e);
      }
      return null;
    default:
      return null;
    }
  }

  /**
   * @param clientResourceType the type of linked resource
   * @param typed {@code true} to include the resource scheme in the link
   *          reference serialization, {@code false} otherwise
   * @param relativeStringEntityReference a relative string entity reference
   * @return a link reference that can be used to insert a link to the specified
   *         entity
   */
  private String getLinkReference(org.xwiki.gwt.wysiwyg.client.wiki.ResourceReference.ResourceType clientResourceType,
                                  boolean typed,
                                  String relativeStringEntityReference) {
    
    
    ResourceType resourceType = new ResourceType(clientResourceType.getScheme());
    ResourceReference linkReference = new ResourceReference(relativeStringEntityReference,
                                                            resourceType);
    
    linkReference.setTyped(typed);
    return linkReferenceSerializer.serialize(linkReference);
  }
  
  

  /**
   * {@inheritDoc}
   * 
   * @see LinkService#parseLinkReference(String,
   *      org.xwiki.gwt.wysiwyg.client.wiki.EntityReference)
   */
  public org.xwiki.gwt.wysiwyg.client.wiki.ResourceReference parseLinkReference(String linkReferenceAsString,
                                                                                org.xwiki.gwt.wysiwyg.client.wiki.EntityReference baseReference) {
    ResourceReference linkReference = linkReferenceParser.parse(linkReferenceAsString);
    org.xwiki.gwt.wysiwyg.client.wiki.ResourceReference clientLinkReference = new org.xwiki.gwt.wysiwyg.client.wiki.ResourceReference();
    clientLinkReference.setType(org.xwiki.gwt.wysiwyg.client.wiki.ResourceReference.ResourceType.forScheme(linkReference.getType()
                                                                                                                        .getScheme()));
    clientLinkReference.setTyped(linkReference.isTyped());
    clientLinkReference.getParameters().putAll(linkReference.getParameters());
    clientLinkReference.setEntityReference(parseEntityReferenceFromResourceReference(linkReference.getReference(),
                                                                                     clientLinkReference.getType(),
                                                                                     baseReference));
    return clientLinkReference;
  }

  /**
   * Parses a client entity reference from a link/resource reference.
   * 
   * @param stringEntityReference a string entity reference extracted from a
   *          link/resource reference
   * @param resourceType the type of resource the string entity reference was
   *          extracted from
   * @param baseReference the client entity reference that is used to resolve
   *          the parsed entity reference relative to
   * @return an untyped client entity reference
   */
  private org.xwiki.gwt.wysiwyg.client.wiki.EntityReference parseEntityReferenceFromResourceReference(String stringEntityReference,
                                                                                                      org.xwiki.gwt.wysiwyg.client.wiki.ResourceReference.ResourceType resourceType,
                                                                                                      org.xwiki.gwt.wysiwyg.client.wiki.EntityReference baseReference) {
    org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType entityType;
    switch (resourceType) {
    case DOCUMENT:
      entityType = org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType.DOCUMENT;
      break;
    case ATTACHMENT:
      entityType = org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType.ATTACHMENT;
      break;
    default:
      entityType = org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType.EXTERNAL;
      break;
    }
    if (entityType == org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType.EXTERNAL) {
      return new URIReference(stringEntityReference).getEntityReference();
    } else {
      return entityReferenceConverter.convert(explicitStringEntityReferenceResolver.resolve(stringEntityReference,
                                                                                            EntityType.valueOf(entityType.toString()),
                                                                                            entityReferenceConverter.convert(baseReference)));
    }
  }
  
  private WikiContext getWikiContext() {
    return (WikiContext) execution.getContext().getProperty(WikiContext.WIKICONTEXT);
  }

}
