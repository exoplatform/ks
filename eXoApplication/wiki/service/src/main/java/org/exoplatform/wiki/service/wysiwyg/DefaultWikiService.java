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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.service.search.WikiSearchData;
import org.exoplatform.wiki.utils.Utils;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.gwt.wysiwyg.client.wiki.Attachment;
import org.xwiki.gwt.wysiwyg.client.wiki.AttachmentReference;
import org.xwiki.gwt.wysiwyg.client.wiki.EntityConfig;
import org.xwiki.gwt.wysiwyg.client.wiki.EntityReference;
import org.xwiki.gwt.wysiwyg.client.wiki.ResourceReference;
import org.xwiki.gwt.wysiwyg.client.wiki.WikiPage;
import org.xwiki.gwt.wysiwyg.client.wiki.WikiPageReference;
import org.xwiki.gwt.wysiwyg.client.wiki.WikiService;
import org.xwiki.model.reference.DocumentReference;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 24, 2010  
 */
@Component
public class DefaultWikiService implements WikiService {
  /**
   * Default Wiki logger to report errors correctly.
   */
  private static Log                     log                      = ExoLogger.getLogger("wiki:GWTWikiService");

  /** Execution context handler, needed for accessing the WikiContext. */
  @Inject
  private Execution                      execution;

  /**
   * The service used to create links.
   */
  @Inject
  private LinkService                    linkService;

  /**
   * The object used to convert between client and server entity reference.
   */
  private final EntityReferenceConverter entityReferenceConverter = new EntityReferenceConverter();

  private WikiContext getWikiContext() {
    return (WikiContext) execution.getContext().getProperty(WikiContext.WIKICONTEXT);
  }

  /**
   * {@inheritDoc}
   * 
   * @see WikiService#isMultiWiki()
   */
  @Override
  public Boolean isMultiWiki() {
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see WikiService#getVirtualWikiNames()
   */
  @Override
  public List<String> getVirtualWikiNames() {
    List<String> virtualWikiNamesList = new ArrayList<String>();
    return virtualWikiNamesList;
  }

  /**
   * {@inheritDoc}
   * 
   * @see WikiService#getSpaceNames(String)
   */
  @Override
  public List<String> getSpaceNames(String wikiName) {
    List<String> spaceNames = new ArrayList<String>();
    Collection<Wiki> wikis = Utils.getWikisByType(WikiType.valueOf(wikiName.toUpperCase()));
    for (Wiki wiki : wikis) {
      spaceNames.add(wiki.getOwner());
    }
    return spaceNames;
  }

  /**
   * {@inheritDoc}
   * 
   * @see WikiService#getPageNames(String, String)
   */
  @Override
  public List<String> getPageNames(String wikiName, String spaceName) {
    org.exoplatform.wiki.service.WikiService wservice = (org.exoplatform.wiki.service.WikiService) PortalContainer.getComponent(org.exoplatform.wiki.service.WikiService.class);

    try {
      WikiContext wikiContext = getWikiContext();
      WikiSearchData data = new WikiSearchData(null,
                                                     "",
                                                     null,
                                                     wikiContext.getType(),
                                                     wikiContext.getOwner());
      PageList<SearchResult> results = wservice.search(data);
      List<DocumentReference> documentReferences = prepareDocumentReferenceList(results);
      List<WikiPage> wikiPages = getWikiPages(documentReferences);
      List<String> pagesNames = new ArrayList<String>();
      for (WikiPage page : wikiPages) {
        String pageName = page.getTitle();
        if (!pagesNames.contains(pageName)) {
          pagesNames.add(pageName);
        }
      }
      return pagesNames;
    } catch (Exception e) {
      log.error("Exception happened when list pages name", e);
      throw new RuntimeException("Failed to list Wiki pages name.", e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see WikiService#getRecentlyModifiedPages(int, int)
   */
  public List<WikiPage> getRecentlyModifiedPages(String wikiName, int start, int count) {
    // TODO: implement wiki search service by author and sort by date to get
    // recently modified pages
    return new ArrayList<WikiPage>();
  }

  /**
   * {@inheritDoc}
   * 
   * @see WikiService#getMatchingPages(String, int, int)
   */
  public List<WikiPage> getMatchingPages(String wikiName, String keyword, int start, int count) {
    String quote = "'";
    String doubleQuote = "''";
    String escapedKeyword = keyword.replaceAll(quote, doubleQuote).toLowerCase();
    org.exoplatform.wiki.service.WikiService wservice = (org.exoplatform.wiki.service.WikiService) PortalContainer.getComponent(org.exoplatform.wiki.service.WikiService.class);

    try {
      WikiContext wikiContext = getWikiContext();
      WikiSearchData data = new WikiSearchData(null,
                                                     escapedKeyword,
                                                     null,
                                                     wikiContext.getType(),
                                                     wikiContext.getOwner());
      data.setNodeType(WikiNodeType.WIKI_PAGE);
      PageList<SearchResult> results = wservice.search(data);
      List<DocumentReference> documentReferences = prepareDocumentReferenceList(results);
      return getWikiPages(documentReferences);
    } catch (Exception e) {
      log.error("Exception happened when searching pages", e);
      throw new RuntimeException("Failed to search Wiki pages.", e);
    }
  }

  /**
   * Helper function to create a list of {@link WikiPage}s from a list of
   * document references.
   * 
   * @param documentReferences a list of document references
   * @return the list of {@link WikiPage}s corresponding to the given document
   *         references
   * @throws Exception if anything goes wrong while creating the list of
   *           {@link WikiPage}s
   */
  private List<WikiPage> getWikiPages(List<DocumentReference> documentReferences) throws Exception {
    org.exoplatform.wiki.service.WikiService wservice = (org.exoplatform.wiki.service.WikiService) PortalContainer.getComponent(org.exoplatform.wiki.service.WikiService.class);
    List<WikiPage> wikiPages = new ArrayList<WikiPage>();
    for (DocumentReference documentReference : documentReferences) {
      WikiPage wikiPage = new WikiPage();
      wikiPage.setReference(entityReferenceConverter.convert(documentReference)
                                                    .getEntityReference());
      String pageId = documentReference.getName();
      String wikiOwner = documentReference.getParent().getName();
      String wikiType = documentReference.getParent().getParent().getName();
      PageImpl page = (PageImpl) wservice.getPageById(wikiType, wikiOwner, pageId);
      wikiPage.setTitle(page.getTitle());
      wikiPage.setUrl(pageId);
      wikiPages.add(wikiPage);
    }
    return wikiPages;
  }

  /**
   * {@inheritDoc}
   * 
   * @see WikiService#getEntityConfig(org.xwiki.gwt.wysiwyg.client.wiki.EntityReference,
   *      ResourceReference)
   */
  public EntityConfig getEntityConfig(org.xwiki.gwt.wysiwyg.client.wiki.EntityReference origin,
                                      ResourceReference destination) {
    return linkService.getEntityConfig(origin, destination);
  }

  /**
   * {@inheritDoc}
   * 
   * @see WikiService#getAttachment(AttachmentReference)
   */
  @Override
  public Attachment getAttachment(AttachmentReference attachmentReference) {
    // Clean attachment filename to be synchronized with all attachment
    // operations.
    String cleanedFileName = attachmentReference.getFileName();
    WikiPageReference pageReference = attachmentReference.getWikiPageReference();
    org.exoplatform.wiki.service.WikiService wservice = (org.exoplatform.wiki.service.WikiService) PortalContainer.getComponent(org.exoplatform.wiki.service.WikiService.class);
    PageImpl page;
    try {
      cleanedFileName = TitleResolver.encodeSpecialCharacters(cleanedFileName);
      page = (PageImpl) wservice.getExsitedOrNewDraftPageById(pageReference.getWikiName(),
                                                              pageReference.getSpaceName(),
                                                              pageReference.getPageName());
      if (page == null) {
        return null;
      }

      if (page.getAttachment(cleanedFileName) == null) {
        log.warn(String.format("Failed to get attachment: %s not found.", cleanedFileName));
        return null;
      }

      Attachment attach = new Attachment();
      attach.setReference(attachmentReference.getEntityReference());
      attach.setUrl(page.getAttachment(cleanedFileName).getDownloadURL());
      return attach;
    } catch (Exception e) {
      log.error("Failed to get attachment: there was a problem with getting the document on the server.",
                e);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see WikiService#getImageAttachments(WikiPageReference)
   */
  public List<Attachment> getImageAttachments(WikiPageReference reference) {
    List<Attachment> imageAttachments = new ArrayList<Attachment>();
    List<Attachment> allAttachments = getAttachments(reference);
    for (Attachment attachment : allAttachments) {
      if (attachment.getMimeType().startsWith("image/")) {
        imageAttachments.add(attachment);
      }
    }
    return imageAttachments;
  }

  /**
   * {@inheritDoc}
   * 
   * @see WikiService#getAttachments(WikiPageReference)
   */
  @Override
  public List<Attachment> getAttachments(WikiPageReference documentReference) {
    try {
      String wikiName = documentReference.getWikiName();
      String spaceName = documentReference.getSpaceName();
      String pageName = documentReference.getPageName();
      if (log.isTraceEnabled()) {
        log.trace("Getting attachments of page : " + wikiName + "." + spaceName + "." + pageName);
      }
      List<Attachment> attachments = new ArrayList<Attachment>();
      org.exoplatform.wiki.service.WikiService wservice = (org.exoplatform.wiki.service.WikiService) PortalContainer.getComponent(org.exoplatform.wiki.service.WikiService.class);
      Page page = wservice.getExsitedOrNewDraftPageById(wikiName,
                                                        spaceName,
                                                        TitleResolver.getId(pageName, false));
      Collection<AttachmentImpl> attachs = ((PageImpl) page).getAttachmentsExcludeContent();
      for (AttachmentImpl attach : attachs) {
        AttachmentReference attachmentReference = new AttachmentReference(attach.getName(),
                                                                          documentReference);
        EntityReference entityReference = attachmentReference.getEntityReference();
        entityReference.setType(org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType.ATTACHMENT);
        Attachment currentAttach = new Attachment();
        currentAttach.setUrl(attach.getDownloadURL());
        currentAttach.setReference(entityReference);
        currentAttach.setMimeType(attach.getContentResource().getMimeType());
        attachments.add(currentAttach);
      }
      return attachments;
    } catch (Exception e) {
      throw new RuntimeException("Failed to retrieve the list of attachments.", e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see WikiService#getUploadURL(org.xwiki.gwt.wysiwyg.client.wiki.EntityReference)
   */
  @Override
  public String getUploadURL(WikiPageReference documentReference) {
    StringBuilder sb = new StringBuilder();
    sb.append("/").append(PortalContainer.getCurrentPortalContainerName()).append("/");
    sb.append(PortalContainer.getCurrentRestContextName()).append("/wiki/upload/");
    sb.append(documentReference.getWikiName()).append("/").append(documentReference.getSpaceName());
    sb.append("/").append(documentReference.getPageName()).append("/");
    return sb.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see WikiService#parseLinkReference(String,
   *      org.xwiki.gwt.wysiwyg.client.wiki.EntityReference)
   */
  public ResourceReference parseLinkReference(String linkReference,
                                              org.xwiki.gwt.wysiwyg.client.wiki.EntityReference baseReference) {
    return linkService.parseLinkReference(linkReference, baseReference);
  }

  /**
   * Helper function to prepare a list of {@link WikiPage} (with full name,
   * title, etc) from a list of search results.
   * 
   * @param results the list of the search results
   * @return the list of {@link WikiPage}s corresponding to the passed names
   * @throws Exception if anything goes wrong retrieving the documents
   */
  private List<DocumentReference> prepareDocumentReferenceList(PageList<SearchResult> results) throws Exception {
    List<DocumentReference> documentReferences = new ArrayList<DocumentReference>();
    for (SearchResult result : results.getAll()) {
      String nodeName = result.getPageName();
      if (nodeName != null && nodeName.length() > 0 && nodeName.startsWith("/")) {
        nodeName = nodeName.substring(1);
      }
      WikiContext wikiContext = getWikiContext();
      log.info("Prepair DocumentReference : " + wikiContext.getType() + "@"
          + wikiContext.getOwner() + "@" + nodeName);
      documentReferences.add(new DocumentReference(wikiContext.getType(),
                                                   wikiContext.getOwner(),
                                                   nodeName));
    }
    return documentReferences;
  }

}
