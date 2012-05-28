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
package org.exoplatform.wiki.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ks.common.image.ResizeImageService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.impl.RenderingServiceImpl;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.service.Relations;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiResource;
import org.exoplatform.wiki.service.WikiRestService;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.related.JsonRelatedData;
import org.exoplatform.wiki.service.related.RelatedUtil;
import org.exoplatform.wiki.service.rest.model.Attachment;
import org.exoplatform.wiki.service.rest.model.Attachments;
import org.exoplatform.wiki.service.rest.model.Link;
import org.exoplatform.wiki.service.rest.model.ObjectFactory;
import org.exoplatform.wiki.service.rest.model.PageSummary;
import org.exoplatform.wiki.service.rest.model.Pages;
import org.exoplatform.wiki.service.rest.model.Space;
import org.exoplatform.wiki.service.rest.model.Spaces;
import org.exoplatform.wiki.service.search.TitleSearchResult;
import org.exoplatform.wiki.service.search.WikiSearchData;
import org.exoplatform.wiki.tree.JsonNodeData;
import org.exoplatform.wiki.tree.TreeNode;
import org.exoplatform.wiki.tree.WikiTreeNode;
import org.exoplatform.wiki.tree.TreeNode.TREETYPE;
import org.exoplatform.wiki.tree.utils.TreeUtils;
import org.exoplatform.wiki.utils.Utils;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 20, 2010  
 */
@Path("/wiki")
public class WikiRestServiceImpl implements WikiRestService, ResourceContainer {

  private final WikiService      wikiService;

  private final RenderingService renderingService;

  private static Log             log = ExoLogger.getLogger("wiki:WikiRestService");

  private final CacheControl     cc;
  
  private ObjectFactory  objectFactory = new ObjectFactory();
  
  public WikiRestServiceImpl(WikiService wikiService, RenderingService renderingService) {
    this.wikiService = wikiService;
    this.renderingService = renderingService;
    cc = new CacheControl();
    cc.setNoCache(true);
    cc.setNoStore(true);
  }

  /**
   * {@inheritDoc}
   */
  @POST
  @Path("/content/")
  @Produces(MediaType.TEXT_HTML)
  public Response getWikiPageContent(@QueryParam("sessionKey") String sessionKey,
                                     @QueryParam("wikiContext") String wikiContextKey,
                                     @QueryParam("markup") boolean isMarkup,
                                     @FormParam("html") String data) {
    EnvironmentContext env = EnvironmentContext.getCurrent();
    WikiContext wikiContext = new WikiContext();
    String currentSyntax = Syntax.XWIKI_2_0.toIdString();
    HttpServletRequest request = (HttpServletRequest) env.get(HttpServletRequest.class);
    try {
      if (data == null) {
        if (sessionKey != null && sessionKey.length() > 0) {
          data = (String) request.getSession().getAttribute(sessionKey);
        }
      }
      if (wikiContextKey != null && wikiContextKey.length() > 0) {
        wikiContext = (WikiContext) request.getSession().getAttribute(wikiContextKey);
        if (wikiContext != null)
          currentSyntax = wikiContext.getSyntax();
      }
      Execution ec = ((RenderingServiceImpl) renderingService).getExecution();
      if (ec.getContext() == null) {
        ec.setContext(new ExecutionContext());
      }
      ec.getContext().setProperty(WikiContext.WIKICONTEXT, wikiContext);
      ServletContext wikiServletContext = PortalContainer.getInstance()
                                                         .getPortalContext()
                                                         .getContext("/wiki");
      InputStream is = wikiServletContext.getResourceAsStream("/templates/wiki/webui/xwiki/wysiwyginput.html");
      byte[] b = new byte[is.available()];
      is.read(b);
      is.close();
     
      data = renderingService.render(data,
                                     Syntax.XHTML_1_0.toIdString(),
                                     currentSyntax,
                                     false);
      data = renderingService.render(data,
                                     currentSyntax,
                                     Syntax.ANNOTATED_XHTML_1_0.toIdString(),
                                     false);
      data = new String(b).replace("$content", data);

    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return Response.serverError().entity(e.getMessage()).cacheControl(cc).build();
    }
    return Response.ok(data, MediaType.TEXT_HTML).cacheControl(cc).build();
  }

  @POST
  @Path("/upload/{wikiType}/{wikiOwner:.+}/{pageId}/")
  public Response upload(@PathParam("wikiType") String wikiType,
                         @PathParam("wikiOwner") String wikiOwner,
                         @PathParam("pageId") String pageId) {
    EnvironmentContext env = EnvironmentContext.getCurrent();
    HttpServletRequest req = (HttpServletRequest) env.get(HttpServletRequest.class);
    boolean isMultipart = FileUploadBase.isMultipartContent(req);
    if (isMultipart) {
      DiskFileUpload upload = new DiskFileUpload();
      // Parse the request
      try {
        List<FileItem> items = upload.parseRequest(req);
        for (FileItem fileItem : items) {
          InputStream inputStream = fileItem.getInputStream();
          byte[] imageBytes;
          if (inputStream != null) {
            imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);
          } else {
            imageBytes = null;
          }
          String fileName = fileItem.getName();
          if (fileName != null)
            // It's necessary because IE posts full path of uploaded files
            fileName = FilenameUtils.getName(fileName);          
          String mimeType = new MimeTypeResolver().getMimeType(StringUtils.lowerCase(fileName));
          WikiResource attachfile = new WikiResource(mimeType, "UTF-8", imageBytes);
          attachfile.setName(fileName);
          if (attachfile != null) {
            WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
            Page page = wikiService.getExsitedOrNewDraftPageById(wikiType, wikiOwner, pageId);
            if (page != null && page.hasPermission(PermissionType.EDITPAGE)) {
              AttachmentImpl att = ((PageImpl) page).createAttachment(attachfile.getName(), attachfile);
              ConversationState conversationState = ConversationState.getCurrent();
              String creator = null;
              if (conversationState != null && conversationState.getIdentity() != null) {
                creator = conversationState.getIdentity().getUserId();
              }
              att.setCreator(creator);
            }
          }
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        return Response.status(HTTPStatus.BAD_REQUEST).entity(e.getMessage()).build();
      }
    }
    return Response.ok().build();
  }

  @GET
  @Path("/tree/{type}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTreeData(@PathParam("type") String type,
                              @QueryParam(TreeNode.PATH) String path,
                              @QueryParam(TreeNode.CURRENT_PATH) String currentPath,
                              @QueryParam(TreeNode.SHOW_EXCERPT) Boolean showExcerpt,
                              @QueryParam(TreeNode.DEPTH) String depth) {
    try {
      List<JsonNodeData> responseData = new ArrayList<JsonNodeData>();
      HashMap<String, Object> context = new HashMap<String, Object>();
      path = URLDecoder.decode(path, "utf-8");
      if (currentPath != null){
        currentPath = URLDecoder.decode(currentPath, "utf-8");
        context.put(TreeNode.CURRENT_PATH, currentPath);
      }   
      context.put(TreeNode.SHOW_EXCERPT, showExcerpt);
      WikiPageParams pageParam = TreeUtils.getPageParamsFromPath(path);
      if (type.equalsIgnoreCase(TREETYPE.ALL.toString())) {
      
        PageImpl page = (PageImpl) wikiService.getPageById(pageParam.getType(),
                                                           pageParam.getOwner(),
                                                           pageParam.getPageId());
        
        Stack<WikiPageParams> stk = Utils.getStackParams(page);
        context.put(TreeNode.STACK_PARAMS, stk);
        responseData = getJsonTree(pageParam, context);
      } else if (type.equalsIgnoreCase(TREETYPE.CHILDREN.toString())) {
        // Get children only
        if (depth == null)
          depth = "1";
        context.put(TreeNode.DEPTH, depth);
        responseData = getJsonDescendants(pageParam, context);
      }
      return Response.ok(new BeanToJsons(responseData), MediaType.APPLICATION_JSON)
                     .cacheControl(cc)
                     .build();
    } catch (Exception e) {
      log.error("Failed for get tree data by rest service.", e);
      return Response.serverError().entity(e.getMessage()).cacheControl(cc).build();
    }
  }
  
  @GET
  @Path("/related/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRelated(@QueryParam(TreeNode.PATH) String path) {
    if (path == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    try {
      WikiPageParams params = TreeUtils.getPageParamsFromPath(path);
      PageImpl page = (PageImpl) wikiService.getPageById(params.getType(), params.getOwner(), params.getPageId());
      
      List<PageImpl> relatedPages = page.getRelatedPages();
      List<JsonRelatedData> relatedData = RelatedUtil.pageImplToJson(relatedPages);
      return Response.ok(new BeanToJsons<JsonRelatedData>(relatedData)).cacheControl(cc).build();
    } catch (Exception e) {
      if (log.isErrorEnabled()) log.error(String.format("can not get related pages of [%s]", path), e);
      return Response.serverError().cacheControl(cc).build();
    }
    
  }
  
  @GET
  @Path("/{wikiType}/spaces")
  @Produces("application/xml")
  public Spaces getSpaces(@Context UriInfo uriInfo,
                          @PathParam("wikiType") String wikiType,
                          @QueryParam("start") Integer start,
                          @QueryParam("number") Integer number) {
    Spaces spaces = objectFactory.createSpaces();
    List<String> spaceNames = new ArrayList<String>();
    Collection<Wiki> wikis = Utils.getWikisByType(WikiType.valueOf(wikiType.toUpperCase()));
    for (Wiki wiki : wikis) {
      spaceNames.add(wiki.getOwner());
    }
    for (String spaceName : spaceNames) {
      try {
        Page page = wikiService.getPageById(wikiType, spaceName, WikiNodeType.Definition.WIKI_HOME_NAME);
        spaces.getSpaces().add(createSpace(objectFactory, uriInfo.getBaseUri(), wikiType, spaceName, page));
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
    return spaces;
  }

  @GET
  @Path("/{wikiType}/spaces/{wikiOwner:.+}/")
  @Produces("application/xml")
  public Space getSpace(@Context UriInfo uriInfo,
                        @PathParam("wikiType") String wikiType,
                        @PathParam("wikiOwner") String wikiOwner) {
    Page page;
    try {
      page = wikiService.getPageById(wikiType, wikiOwner, WikiNodeType.Definition.WIKI_HOME_NAME);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return objectFactory.createSpace();
    }
    return createSpace(objectFactory, uriInfo.getBaseUri(), wikiType, wikiOwner, page);
  }

  @GET
  @Path("/{wikiType}/spaces/{wikiOwner:.+}/pages")
  @Produces("application/xml")
  public Pages getPages(@Context UriInfo uriInfo,
                        @PathParam("wikiType") String wikiType,
                        @PathParam("wikiOwner") String wikiOwner,
                        @QueryParam("start") Integer start,
                        @QueryParam("number") Integer number,
                        @QueryParam("parentId") String parentFilterExpression) {
    Pages pages = objectFactory.createPages();
    PageImpl page = null;
    boolean isWikiHome = true;
    try {
      String parentId = WikiNodeType.Definition.WIKI_HOME_NAME;
      if (parentFilterExpression != null && parentFilterExpression.length() > 0
          && !parentFilterExpression.startsWith("^(?!")) {
        parentId = parentFilterExpression;
        if (parentId.indexOf(".") >= 0) {
          parentId = parentId.substring(parentId.indexOf(".") + 1);
        }
        isWikiHome = false;
      }
      page = (PageImpl) wikiService.getPageById(wikiType, wikiOwner, parentId);
      if (isWikiHome) {
        pages.getPageSummaries().add(createPageSummary(objectFactory, uriInfo.getBaseUri(), page));
      } else {
        for (PageImpl childPage : page.getChildPages().values()) {
          pages.getPageSummaries().add(createPageSummary(objectFactory,
                                                       uriInfo.getBaseUri(),
                                                       childPage));
        }
      }
    } catch (Exception e) {
      log.error("Can't get children pages of:" + parentFilterExpression, e);
    }

    return pages;
  }
  
  @GET
  @Path("/{wikiType}/spaces/{wikiOwner:.+}/pages/{pageId}")
  @Produces("application/xml")
  public org.exoplatform.wiki.service.rest.model.Page getPage(@Context UriInfo uriInfo,
                                                              @PathParam("wikiType") String wikiType,
                                                              @PathParam("wikiOwner") String wikiOwner,
                                                              @PathParam("pageId") String pageId) {
    PageImpl page;
    try {
      page = (PageImpl) wikiService.getPageById(wikiType, wikiOwner, pageId);
      if (page != null) {
        return createPage(objectFactory, uriInfo.getBaseUri(), uriInfo.getAbsolutePath(), page);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return objectFactory.createPage();
  }
  
  @GET
  @Path("/{wikiType}/spaces/{wikiOwner:.+}/pages/{pageId}/attachments")
  @Produces("application/xml")
  public Attachments getAttachments(@Context UriInfo uriInfo,
                                    @PathParam("wikiType") String wikiType,
                                    @PathParam("wikiOwner") String wikiOwner,
                                    @PathParam("pageId") String pageId,
                                    @QueryParam("start") Integer start,
                                    @QueryParam("number") Integer number) {
    Attachments attachments = objectFactory.createAttachments();
    PageImpl page;
    try {
      page = (PageImpl) wikiService.getPageById(wikiType, wikiOwner, pageId);
      Collection<AttachmentImpl> pageAttachments = page.getAttachmentsExcludeContent();
      for (AttachmentImpl pageAttachment : pageAttachments) {
        attachments.getAttachments().add(createAttachment(objectFactory, uriInfo.getBaseUri(), pageAttachment, "attachment", "attachment"));
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return attachments;
  }
  
  @GET
  @Path("contextsearch/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response searchData(@QueryParam("keyword") String keyword,
                             @QueryParam("wikiType") String wikiType,
                             @QueryParam("wikiOwner") String wikiOwner) throws Exception {
    try {
      WikiSearchData data = new WikiSearchData(null, keyword.toLowerCase(), null, wikiType, wikiOwner);
      data.setLimit(10);
      List<TitleSearchResult> result = wikiService.searchDataByTitle(data);
      return Response.ok(new BeanToJsons(result), MediaType.APPLICATION_JSON)
                     .cacheControl(cc)
                     .build();
    } catch (Exception e) {
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
    }
  }
  
  @GET
  @Path("/images/{wikiType}/space/{wikiOwner:.+}/page/{pageId}/{imageId}")
  @Produces("image")
  public Response getImage(@Context UriInfo uriInfo,
                           @PathParam("wikiType") String wikiType,
                           @PathParam("wikiOwner") String wikiOwner,
                           @PathParam("pageId") String pageId,
                           @PathParam("imageId") String imageId,
                           @QueryParam("width") Integer width) {
    InputStream result = null;
    try {
      ResizeImageService resizeImgService = (ResizeImageService) ExoContainerContext.getCurrentContainer()
                                                                                    .getComponentInstanceOfType(ResizeImageService.class);
      PageImpl page = (PageImpl) wikiService.getPageById(wikiType, wikiOwner, pageId);
      AttachmentImpl att = page.getAttachment(imageId);
      ByteArrayInputStream bis = new ByteArrayInputStream(att.getContentResource().getData());
      if (width != null) {
        result = resizeImgService.resizeImageByWidth(imageId, bis, width);
      } else {
        result = bis;
      }      
      return Response.ok(result, "image").cacheControl(cc).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug(String.format("Can't get image name: %s of page %s", imageId, pageId), e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
    }
  }

  public Space createSpace(ObjectFactory objectFactory,
                           URI baseUri,
                           String wikiName,
                           String spaceName,
                           Page home) {
    Space space = objectFactory.createSpace();
    space.setId(String.format("%s:%s", wikiName, spaceName));
    space.setWiki(wikiName);
    space.setName(spaceName);
    if (home != null) {
      space.setHome("home");
      space.setXwikiRelativeUrl("home");
      space.setXwikiAbsoluteUrl("home");
    }

    String pagesUri = UriBuilder.fromUri(baseUri)
                                .path("/wiki/{wikiName}/spaces/{spaceName}/pages")
                                .build(wikiName, spaceName)
                                .toString();
    Link pagesLink = objectFactory.createLink();
    pagesLink.setHref(pagesUri);
    pagesLink.setRel(Relations.PAGES);
    space.getLinks().add(pagesLink);

    if (home != null) {
      String homeUri = UriBuilder.fromUri(baseUri)
                                 .path("/wiki/{wikiName}/spaces/{spaceName}/pages/{pageName}")
                                 .build(wikiName, spaceName, home.getName())
                                 .toString();
      Link homeLink = objectFactory.createLink();
      homeLink.setHref(homeUri);
      homeLink.setRel(Relations.HOME);
      space.getLinks().add(homeLink);
    }

    String searchUri = UriBuilder.fromUri(baseUri)
                                 .path("/wiki/{wikiName}/spaces/{spaceName}/search")
                                 .build(wikiName, spaceName)
                                 .toString();
    Link searchLink = objectFactory.createLink();
    searchLink.setHref(searchUri);
    searchLink.setRel(Relations.SEARCH);
    space.getLinks().add(searchLink);

    return space;

  }

  public org.exoplatform.wiki.service.rest.model.Page createPage(ObjectFactory objectFactory,
                                                                 URI baseUri,
                                                                 URI self,
                                                                 PageImpl doc) throws Exception {
    org.exoplatform.wiki.service.rest.model.Page page = objectFactory.createPage();
    fillPageSummary(page, objectFactory, baseUri, doc);

    page.setVersion("current");
    page.setMajorVersion(1);
    page.setMinorVersion(0);
    page.setLanguage(doc.getSyntax());
    page.setCreator(doc.getOwner());

    GregorianCalendar calendar = new GregorianCalendar();
    page.setCreated(calendar);

    page.setModifier(doc.getAuthor());

    calendar = new GregorianCalendar();
    calendar.setTime(doc.getUpdatedDate());
    page.setModified(calendar);

    page.setContent(doc.getContent().getText());

    if (self != null) {
      Link pageLink = objectFactory.createLink();
      pageLink.setHref(self.toString());
      pageLink.setRel(Relations.SELF);
      page.getLinks().add(pageLink);
    }
    return page;
  }

  public PageSummary createPageSummary(ObjectFactory objectFactory, URI baseUri, PageImpl doc) throws IllegalArgumentException, UriBuilderException, Exception {
    PageSummary pageSummary = objectFactory.createPageSummary();
    fillPageSummary(pageSummary, objectFactory, baseUri, doc);
    String wikiName = doc.getWiki().getType();
    String spaceName = doc.getWiki().getOwner();
    String pageUri = UriBuilder.fromUri(baseUri)
                               .path("/wiki/{wikiName}/spaces/{spaceName}/pages/{pageName}")
                               .build(wikiName, spaceName, doc.getName())
                               .toString();
    Link pageLink = objectFactory.createLink();
    pageLink.setHref(pageUri);
    pageLink.setRel(Relations.PAGE);
    pageSummary.getLinks().add(pageLink);

    return pageSummary;
  }
  
  public Attachment createAttachment(ObjectFactory objectFactory,
                                     URI baseUri,
                                     AttachmentImpl pageAttachment,
                                     String xwikiRelativeUrl,
                                     String xwikiAbsoluteUrl) throws Exception {
    Attachment attachment = objectFactory.createAttachment();

    fillAttachment(attachment, objectFactory, baseUri, pageAttachment, xwikiRelativeUrl, xwikiAbsoluteUrl);

    PageImpl page = pageAttachment.getParentPage();

    String attachmentUri = UriBuilder.fromUri(baseUri)
                                     .path("/wiki/{wikiName}/spaces/{spaceName}/pages/{pageName}/attachments/{attachmentName}")
                                     .build(page.getWiki().getType(), page.getWiki().getOwner(), page.getName(), pageAttachment.getName())
                                     .toString();
    Link attachmentLink = objectFactory.createLink();
    attachmentLink.setHref(attachmentUri);
    attachmentLink.setRel(Relations.ATTACHMENT_DATA);
    attachment.getLinks().add(attachmentLink);

    return attachment;
  }  
 
  private List<JsonNodeData> getJsonTree(WikiPageParams params,HashMap<String, Object> context) throws Exception {
    List<JsonNodeData> responseData = new ArrayList<JsonNodeData>();
    String currentPath = (String) context.get(TreeNode.CURRENT_PATH);
    Wiki wiki = Utils.getWiki(params);
    WikiTreeNode wikiNode = new WikiTreeNode(wiki);
    wikiNode.pushDescendants(context);
    responseData = TreeUtils.tranformToJson(wikiNode, context);
    return responseData;
  }

  private List<JsonNodeData> getJsonDescendants(WikiPageParams params,
                                                HashMap<String, Object> context) throws Exception {
    TreeNode treeNode = TreeUtils.getDescendants(params, context);
    return TreeUtils.tranformToJson(treeNode, context);
  }

  private static void fillPageSummary(PageSummary pageSummary,
                                      ObjectFactory objectFactory,
                                      URI baseUri,
                                      PageImpl doc) throws IllegalArgumentException, UriBuilderException, Exception {
    String wikiType = doc.getWiki().getType();
    pageSummary.setWiki(wikiType);
    pageSummary.setFullName(doc.getTitle());
    pageSummary.setId(wikiType + ":" + doc.getWiki().getOwner() + "." + doc.getName());
    pageSummary.setSpace(doc.getWiki().getOwner());
    pageSummary.setName(doc.getName());
    pageSummary.setTitle(doc.getTitle());
    pageSummary.setXwikiRelativeUrl("http://localhost:8080/ksdemo/rest-ksdemo/wiki/portal/spaces/classic/pages/WikiHome");
    pageSummary.setXwikiAbsoluteUrl("http://localhost:8080/ksdemo/rest-ksdemo/wiki/portal/spaces/classic/pages/WikiHome");
    pageSummary.setTranslations(objectFactory.createTranslations());
    pageSummary.setSyntax(doc.getSyntax());

    PageImpl parent = doc.getParentPage();
    // parentId must not be set if the parent document does not exist.
    if (parent != null) {
      pageSummary.setParent(parent.getName());
      pageSummary.setParentId(parent.getName());
    } else {
      pageSummary.setParent("");
      pageSummary.setParentId("");
    }

    String spaceUri = UriBuilder.fromUri(baseUri)
                                .path("/wiki/{wikiName}/spaces/{spaceName}")
                                .build(wikiType, doc.getWiki().getOwner())
                                .toString();
    Link spaceLink = objectFactory.createLink();
    spaceLink.setHref(spaceUri);
    spaceLink.setRel(Relations.SPACE);
    pageSummary.getLinks().add(spaceLink);

    if (parent != null) {
      String parentUri = UriBuilder.fromUri(baseUri)
                                   .path("/wiki/{wikiName}/spaces/{spaceName}/pages/{pageName}")
                                   .build(parent.getWiki().getType(),
                                          parent.getWiki().getOwner(),
                                          parent.getName())
                                   .toString();
      Link parentLink = objectFactory.createLink();
      parentLink.setHref(parentUri);
      parentLink.setRel(Relations.PARENT);
      pageSummary.getLinks().add(parentLink);
    }

    if (!doc.getChildPages().isEmpty()) {
      String pageChildrenUri = UriBuilder.fromUri(baseUri)
                                         .path("/wiki/{wikiName}/spaces/{spaceName}/pages/{pageName}/children")
                                         .build(wikiType,
                                                doc.getWiki().getOwner(),
                                                doc.getName())
                                         .toString();
      Link pageChildrenLink = objectFactory.createLink();
      pageChildrenLink.setHref(pageChildrenUri);
      pageChildrenLink.setRel(Relations.CHILDREN);
      pageSummary.getLinks().add(pageChildrenLink);
    }

    if (!doc.getAttachmentsExcludeContent().isEmpty()) {
      String attachmentsUri;
      attachmentsUri = UriBuilder.fromUri(baseUri)
                                 .path("/wiki/{wikiName}/spaces/{spaceName}/pages/{pageName}/attachments")
                                 .build(wikiType,
                                        doc.getWiki().getOwner(),
                                        doc.getName())
                                 .toString();

      Link attachmentsLink = objectFactory.createLink();
      attachmentsLink.setHref(attachmentsUri);
      attachmentsLink.setRel(Relations.ATTACHMENTS);
      pageSummary.getLinks().add(attachmentsLink);
    }

  }
  
  private void fillAttachment(Attachment attachment,
                              ObjectFactory objectFactory,
                              URI baseUri,
                              AttachmentImpl pageAttachment,
                              String xwikiRelativeUrl,
                              String xwikiAbsoluteUrl) throws Exception {
    PageImpl page = pageAttachment.getParentPage();

    attachment.setId(String.format("%s@%s", page.getName(), pageAttachment.getName()));
    attachment.setName(pageAttachment.getName());
    attachment.setSize((int) pageAttachment.getWeightInBytes());
    attachment.setVersion("current");
    attachment.setPageId(page.getName());
    attachment.setPageVersion("current");
    attachment.setMimeType(pageAttachment.getContentResource().getMimeType());
    attachment.setAuthor(pageAttachment.getCreator());

    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTime(pageAttachment.getCreated());
    attachment.setDate(calendar);

    attachment.setXwikiRelativeUrl(xwikiRelativeUrl);
    attachment.setXwikiAbsoluteUrl(xwikiAbsoluteUrl);

    String pageUri = UriBuilder.fromUri(baseUri)
                               .path("/wiki/{wikiName}/spaces/{spaceName}/pages/{pageName}")
                               .build(page.getWiki().getType(), page.getWiki().getOwner(), page.getName())
                               .toString();
    Link pageLink = objectFactory.createLink();
    pageLink.setHref(pageUri);
    pageLink.setRel(Relations.PAGE);
    attachment.getLinks().add(pageLink);
  }
  
  /**
   * Return the help syntax page.
   * The syntax id have to replaced all special characters: 
   *  Character '/' have to replace to "SLASH"
   *  Character '.' have to replace to "DOT"
   *
   * Sample:
   * "confluence/1.0" will be replaced to "confluenceSLASH1DOT0"
   *  
   * @param syntaxId The id of syntax to show in help page
   * @return The response that contains help page
   */
  @GET
  @Path("/help/{syntaxId}")
  @Produces(MediaType.TEXT_HTML)
  public Response getHelpSyntaxPage(@PathParam("syntaxId") String syntaxId) {
    CacheControl cacheControl = new CacheControl();
    
    syntaxId = syntaxId.replace(Utils.SLASH, "/").replace(Utils.DOT, ".");
    try {
      PageImpl page = wikiService.getHelpSyntaxPage(syntaxId);
      if (page == null) {
        return Response.status(HTTPStatus.NOT_FOUND).cacheControl(cc).build();
      }
      Page fullHelpPage = (Page) page.getChildPages().values().iterator().next();
      
      // Get help page body
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      RenderingService renderingService = (RenderingService) container.getComponentInstanceOfType(RenderingService.class);
      String body = renderingService.render(fullHelpPage.getContent().getText(), fullHelpPage.getSyntax(), Syntax.XHTML_1_0.toIdString(), false);
      
      // Create javascript to load css
      StringBuilder script = new StringBuilder("<script type=\"text/javascript\">")
      .append("var local = String(window.location);")
      .append("var i = local.indexOf('/', local.indexOf('//') + 2);")
      .append("local = (i <= 0) ? local : local.substring(0, i);")
      .append("local = local + '/wiki/skin/DefaultSkin/webui/Stylesheet.css';")
      .append("var link = document.createElement('link');")
      .append("link.rel = 'stylesheet';")
      .append("link.type = 'text/css';")
      .append("link.href = local;")
      .append("document.head = document.head || document.getElementsByTagName(\"head\")[0] || document.documentElement;")
      .append("document.head.appendChild(link);")
      .append("</script>");
      
      // Create help html page
      StringBuilder htmlOutput = new StringBuilder();
      htmlOutput.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">")
      .append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\" dir=\"ltr\">")
      .append("<head id=\"head\">")
      .append("<title>Wiki help page</title>")
      .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>")
      .append(script)
      .append("</head>")
      .append("<body>")
      .append("<div class=\"UIWikiPageContentArea\">")
      .append(body)
      .append("</div>")
      .append("</body>")
      .append("</html>");
      
      return Response.ok(htmlOutput.toString(), MediaType.TEXT_HTML).cacheControl(cacheControl).build();
    } catch (Exception e) {
      if (log.isWarnEnabled()) {
        log.warn("An exception happens when getHelpSyntaxPage", e);
      }
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
    }
  }
}