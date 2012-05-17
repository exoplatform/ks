import javax.ws.rs.Path
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response
import javax.ws.rs.FormParam

import java.util.Map;
import java.util.HashMap;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.api.Attachment;
import org.exoplatform.wiki.mow.api.Wiki;

//List wikis
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.WikiStore;
import org.exoplatform.wiki.resolver.TitleResolver;

import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.tree.utils.TreeUtils;

//upload
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.wiki.service.WikiResource;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.commons.utils.MimeTypeResolver;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.exoplatform.common.http.HTTPStatus;
import org.apache.commons.io.FilenameUtils;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.utils.Utils;

//Wiki Transformation
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;


@Path("/wikiloader")
public class WikiLoader {

  public Map pages = new HashMap();
  
  private Page getPageByPath(String path) {
   WikiPageParams pageParam = TreeUtils.getPageParamsFromPath(path.replaceAll("//", "/"))
        
   WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class)
   Page page = (Page) wikiService.getPageById(pageParam.getType(), pageParam.getOwner(), pageParam.getPageId())

   return  page
  }
  
  @GET
  @Path("check")
  public Response checkPath(@QueryParam("path") String path) {
  if ( getPageByPath(path) != null)
     return Response.status(200).entity("OK : Page exists").build();
   return Response.status(404).entity("KO : Page not found").build();
  }

  @GET
  @Path("/checkPath/{wikiType}/{wikiOwner:.+}/{pageId}/")
  public Response checkPath(@PathParam("wikiType") String wikiType,
                         @PathParam("wikiOwner") String wikiOwner,
                         @PathParam("pageId") String pageId) {
    Page page = getPageByPath(wikiType + "/" + wikiOwner + "/" + pageId);
    if ( page != null) {
      page.getAttachments()
      return Response.status(200).entity("OK : Page exists").build();
    } else {
      return Response.status(404).entity("KO : Page not found").build();
    }
  }

  @GET
  @Path("pageurl")
  @Produces("text/plain")
  public String getPageURL(@QueryParam("path") String path) {
     def page = getPageByPath(path);
     if (page.getName() != null) {
       return page.getName();
     }
     return "Page URL not Found";
  }
  
  @GET
  @Path("atturl")
  @Produces("text/plain")
  public String getAttachmentURL(@QueryParam("path") String path) {
   WikiPageParams pageParam = TreeUtils.getPageParamsFromPath(path.replaceAll("//", "/"))
     return "/upload/" + pageParam.getType() + "/" + pageParam.getOwner() + "/" + pageParam.getPageId();
  }
  
  @GET
  @Path("pages")
  public Response listPages(@QueryParam("path") String path) {
   def page = getPageByPath(path);
   def list = "";
   if ( page != null) {
      Map<String,PageImpl> childPages = page.getChildPages();
      childPages.values().each {
        list += it.getName() + "\n";
      }
      return Response.status(200).entity("Pages : \n" + list).build();
   }
   return Response.status(404).entity("KO : Page not found").build();
  }

  @GET
  @Path("create")
  public String createPage(@QueryParam("path") String path, @QueryParam("name") String pageName, @QueryParam("syntax") String syntax) {
   Page wikipage = null;
   
   //getLogger().info("Page creation requested : " + path + "/" + pageName);

   Page parentPage = getPageByPath(path)
   String creator = null;

   try {
   
    if (parentPage != null && parentPage.getWiki() != null) {
     // Check if not exists
     WikiPageParams pageParam = TreeUtils.getPageParamsFromPath(path.replaceAll("//", "/"))
     WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class)
     Page page = (Page) wikiService.getPageById(pageParam.getType(), pageParam.getOwner(), pageName)

     if (page == null) {
         // Create if not exists 
         def pageTitle = pageName;
         pageName = pageName.replace(' ', '_');
         
         wikipage = wikiService.createPage(pageParam.getType(), pageParam.getOwner(), pageName, parentPage.getName());
         if (syntax != null && syntax.equals("xwiki2")) {
            wikipage.setSyntax("xwiki/2.0");
         } else {
            wikipage.setSyntax("confluence/1.0");
         }
         wikipage.setTitle(pageTitle);
         wikipage.getContent().setText("Page created by WikiLoader author " + wikipage.getAuthor() + ", parent author " + parentPage.getAuthor());
         ConversationState conversationState = ConversationState.getCurrent();
         if (conversationState != null && conversationState.getIdentity() != null) {
            creator = conversationState.getIdentity().getUserId();
         }
         if (creator != null)
            wikipage.setOwner(creator);
         wikipage.checkin();
         wikipage.checkout();
         System.out.println("WikiLoaderService created page : " + path + "/" + pageName + " on demand of " + creator);
     
         return wikipage.getName();
     } else {
         return "%% Page exists : " + wikipage.getName();
    }
     
    }
   } catch (Exception e) {
     return "%%Page creation failed : " + pageName + ", cause: " + e.getMessage();
   }
   
  }

  @POST
  @Path("content")
  public String setContent(@FormParam("path") String path, @FormParam("content") String content) {
   String message = "Please provide a valid path";
   if (path != null) {
      Page wikipage = getPageByPath(path)

      if (wikipage != null) {
         wikipage.getContent().setText(content);
      }   
      return "Content replaced in Page path";
    }
    return message;
  }

  @GET
  @Path("content/text")
  public String contentAsText(@QueryParam("path") String path) {
    Page wikipage = getPageByPath(path)

    if (wikipage != null) {
       return wikipage.getContent().getText();
    }
   
    return wikipage != null;
  }

  /**
   * Switch the page content from Confluence to XWiki2 (and correctly set the syntax)
   **/
  @GET
  @Path("toxwiki2")
  public String convertToXWiki2(@QueryParam("path") String path) {
    if (path != null) {
      Page wikipage = getPageByPath(path)
      String currentSyntax = wikipage.getSyntax();
      
      //TODO Check that the syntax is available in the space
      
      if (wikipage != null && !"xwiki/2.0".equals(currentSyntax)) {
        String content = wikipage.getContent().getText();
        return transform_CFL1_XWK2(content);
        
        //wikipage.setSyntax("xwiki/2.0");
        //wikipage.getContent().setText(newContent);
        //return "transformed to xwiki2"
      }   
    }
    return "no change performed"
  } 

  public static String transform_CFL1_XWK2(String content) {
    EmbeddableComponentManager ecm = new EmbeddableComponentManager();
    ecm.initialize(Thread.currentThread().getContextClassLoader());

    try {
      Converter converter = ecm.lookup(Converter.class);
      WikiPrinter printer = new DefaultWikiPrinter();
      converter.convert(new StringReader(content), Syntax.CONFLUENCE_1_0, Syntax.XWIKI_2_0, printer);
      return printer.toString();
    } catch (ComponentLookupException e) {
      return "TRANSFORMATION FAILURE: " + e.getMessage();
    } catch (ConversionException e) {
      return "TRANSFORMATION FAILURE: " + e.getMessage();
    }

    return content;
  }


  @GET
  @Path("list")
  public String listWikis() {
    def mowService = (MOWService) PortalContainer.getComponent(MOWService.class);
    def model = mowService.getModel();
    def wStore = (WikiStore) model.getWikiStore();
    def wikis = wStore.getWikis();
    
    def list = new ArrayList();
    wikis.each {
      def path = it.getType() + "/" + it.getOwner() + "/" + it.getWikiHome().getName();
      path = path.replaceAll("//", "/");
      list.add(path);
    }
    java.util.Collections.sort(list);
    def content = "";
    list.each {
      content += it + "\n";
    }
    //WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
    return content;
  }
   
  @GET
  @Path("/attachments/{wikiType}/{wikiOwner:.+}/{pageId}/")
  public String attachmentList(@PathParam("wikiType") String wikiType,
                       @PathParam("wikiOwner") String wikiOwner,
                       @PathParam("pageId") String pageId) {
                         
    Page page = getPageByPath(wikiType + "/" + wikiOwner + "/" + pageId);
    String attachmentList = "";

    if (page != null) {
      Iterator<AttachmentImpl> attIter= ((PageImpl) page).getAttachments().iterator();
      while (attIter.hasNext()) {
        AttachmentImpl att = attIter.next();
        attachmentList += att.getName() + "|";
      }
    } else {
      attachmentList = "%%Page " + wikiType + "/" + wikiOwner + "/" + pageId + " not found";
    }

    return attachmentList;
  }
  
  @POST
  @Path("/upload/{wikiType}/{wikiOwner:.+}/{pageId}/")
  public String upload(@PathParam("wikiType") String wikiType,
                         @PathParam("wikiOwner") String wikiOwner,
                         @PathParam("pageId") String pageId) {
 
    EnvironmentContext env = EnvironmentContext.getCurrent();
    HttpServletRequest req = (HttpServletRequest) env.get(HttpServletRequest.class);
    boolean isMultipart = FileUploadBase.isMultipartContent(req);

    Page page = getPageByPath(wikiType + "/" + wikiOwner + "/" + pageId);
    if (page == null) {
      return "%%Page not found : " + wikiType + "/" + wikiOwner + "/" + pageId; 
    }

    if (isMultipart) {      
      DiskFileUpload upload = new DiskFileUpload();
      // Parse the request
      try {
        List<FileItem> items = upload.parseRequest(req);
        for (FileItem fileItem : items) {
          InputStream inputStream = fileItem.getInputStream();
          byte[] fileBytes;
          if (inputStream != null) {
            fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
          } else {
            fileBytes = null;
          }
          String fileName = fileItem.getName();
          String fileType = fileItem.getContentType();
          if (fileName != null) {
            // It's necessary because IE posts full path of uploaded files
            fileName = FilenameUtils.getName(fileName);
            fileType = FilenameUtils.getExtension(fileName);
          }

          String mimeType = new MimeTypeResolver().getMimeType(fileName);
          WikiResource attachfile = new WikiResource(mimeType, "UTF-8", fileBytes);
          attachfile.setName(fileName);
          if (attachfile != null) {
              AttachmentImpl att = ((PageImpl) page).createAttachment(attachfile.getName(), attachfile);
              
              ConversationState conversationState = ConversationState.getCurrent();
              String creator = null;
              if (conversationState != null && conversationState.getIdentity() != null) {
                creator = conversationState.getIdentity().getUserId();
              }
              att.setCreator(creator);
              //Utils.reparePermissions(att);
              return "File uploaded : " + fileName + " in WikiType:" + wikiType+ " WikiOwner:" + wikiOwner+ " pageId:" + pageId;
            } else {
              return "Not found page to attach the file to " + pageId;
            } 
        }
      } catch (Exception e) {
        return "Error:" + e.getMessage();
      }
    } else {
       return "Not multipart";
    }
    return "Nothing done";
  }

}
