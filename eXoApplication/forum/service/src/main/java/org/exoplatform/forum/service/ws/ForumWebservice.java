/**
 * @author Uoc Nguyen
 * 
 */
package org.exoplatform.forum.service.ws;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("ks/forum")
public class ForumWebservice implements ResourceContainer {

  protected final static String JSON_CONTENT_TYPE = MediaType.APPLICATION_JSON;

  final public static String    APP_TYPE          = "forum".intern();

  private String                strQuery;

  private List<BanIP>           ipsToJson         = new ArrayList<BanIP>();

  private static Log            log               = ExoLogger.getLogger(ForumWebservice.class);

  private static final CacheControl         cc;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cc = new CacheControl();
    cc.setNoCache(true);
    cc.setNoStore(true);
  }
  
  public ForumWebservice() {
  }

  private MessageBean getNewPosts(String userName, int maxcount) throws Exception {
    ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    List<Post> list = forumService.getRecentPostsForUser(userName, maxcount);
    if (list != null) {
      for (Post post : list) {
        post.setLink(post.getLink() + "/" + post.getId());
      }
    }
    MessageBean data = new MessageBean();
    data.setData(list);
    return data;
  }
  
  private String getUserId(SecurityContext sc, UriInfo uriInfo) {
    try {
      return sc.getUserPrincipal().getName();
    } catch (NullPointerException e) {
      return getViewerId(uriInfo);
    } catch (Exception e) {
      log.debug("Failed to get user id", e);
      return null;
    }
  }
  
  private String getViewerId(UriInfo uriInfo) {
    URI uri = uriInfo.getRequestUri();
    String requestString = uri.getQuery();
    if (requestString == null) return null;
    String[] queryParts = requestString.split("&");
    for (String queryPart : queryParts) {
      if (queryPart.startsWith("opensocial_viewer_id")) {
        return queryPart.substring(queryPart.indexOf("=") + 1, queryPart.length());
      }
    }
    return null;
  }


  /**
   * Return a list of recent posts of the current user limited by posts number.
   * 
   * @param maxcount Limitation of returned posts.
   * @param sc SecurityContext - used to get current user.
   * @param uriInfo UriInfo - used to get current user.
   * @return Response in JSON format.
   * @throws Exception The exception
   * 
   * @anchor KSref.DevelopersReferences.PublicRestAPIs.ForumWebservice.getMessage
   */

  @GET
  @Path("getmessage/{maxcount}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getMessage(@PathParam("maxcount") int maxcount, 
                             @Context SecurityContext sc,
                             @Context UriInfo uriInfo) throws Exception {
    try {
      String userName = getUserId(sc, uriInfo);
      MessageBean data = getNewPosts(userName, maxcount);
      return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cc).build();
    } catch (Exception e) {
      log.debug("Failed to get new post by user.");
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cc).build();
    }
  }


  /**
   * Return a list of recent public posts limited by posts number.
   * 
   * @param maxcount Limitation of returned posts.
   * @return Response in JSON format.
   * @throws Exception The exception
   * 
   * @anchor KSref.DevelopersReferences.PublicRestAPIs.ForumWebservice.getPulicMessage
   */

  @GET
  @Path("getpublicmessage/{maxcount}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPulicMessage(@PathParam("maxcount") int maxcount) throws Exception {
    MessageBean data = getNewPosts(null, maxcount);
    return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cc).build();
  }


  /**
   * Return a list of banned IPs filtered by the input string.
   * 
   * @param str Filter a IPs list. If _strIP_ is set to "_all_", this function will get all banned IPs.
   * @return Response in JSON format.
   * @throws Exception The exception
   * 
   * @anchor KSref.DevelopersReferences.PublicRestAPIs.ForumWebservice.filterIps
   */

  @GET
  @Path("filter/{strIP}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response filterIps(@PathParam("strIP") String str) throws Exception {
    ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    if (str.equals("all")) {
      ipsToJson.clear();
      List<String> banIps = forumService.getBanList();
      for (String ip : banIps) {
        ipsToJson.add(new BanIP(ip));
      }
    } else if (!str.equals(strQuery)) {
      ipsToJson.clear();
      List<String> banIps = forumService.getBanList();
      for (String ip : banIps) {
        if (ip.startsWith(str))
          ipsToJson.add(new BanIP(ip));
      }
      strQuery = str;
    }
    return Response.ok(new BeanToJsons<BanIP>(ipsToJson), JSON_CONTENT_TYPE).cacheControl(cc).build();
  }


  /**
   * Return a list of banned IPs for a forum filtered by the input string.
   * 
   * @param forumId The forum Id.
   * @param str Filter a IPs list. If _strIP_ is set to "_all_", this function will get all banned IPs.
   * @return Response in JSON format.
   * @throws Exception The exception
   * 
   * @anchor KSref.DevelopersReferences.PublicRestAPIs.ForumWebservice.filterIpBanForum
   */

  @GET
  @Path("filterIpBanforum/{strForumId}/{strIP}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response filterIpBanForum(@PathParam("strForumId") String forumId, @PathParam("strIP") String str) throws Exception {
    ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    if (str.equals("all")) {
      ipsToJson.clear();
      List<String> banIps = forumService.getForumBanList(forumId);
      for (String ip : banIps) {
        ipsToJson.add(new BanIP(ip));
      }
    } else if (!str.equals(strQuery)) {
      ipsToJson.clear();
      List<String> banIps = forumService.getForumBanList(forumId);
      for (String ip : banIps) {
        if (ip.startsWith(str))
          ipsToJson.add(new BanIP(ip));
      }
      strQuery = str;
    }
    return Response.ok(new BeanToJsons<BanIP>(ipsToJson), JSON_CONTENT_TYPE).cacheControl(cc).build();
  }


  /**
   * Return a list of tags in a topic of a user filtered by the input string.
   * 
   * @param str Filter a tags list. {example}{example}
   *   If _strTagName_ is " ", the function will return an empty list. {example}{example}
   *   If _strTagName_ is "_onclickForm_", the function will return all tags of the topic.  {example}{example}
   *   If _strTagName_str is any, the function will return the tags name based on this filter.
   * @param userAndTopicId The Id of the current user and topic that has the form of {{{ {userId,topicId} }}}
   * @return Response in JSON format.
   * @throws Exception The exception
   * 
   * @anchor KSref.DevelopersReferences.PublicRestAPIs.ForumWebservice.filterTagNameForum
   */

  @GET
  @Path("filterTagNameForum/{userAndTopicId}/{strTagName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response filterTagNameForum(@PathParam("strTagName") String str, @PathParam("userAndTopicId") String userAndTopicId) throws Exception {
    ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    if (str.equals(" ")) {
      ipsToJson.clear();
    } else if (str.equals("onclickForm")) {
      ipsToJson.clear();
      List<String> banIps = forumService.getTagNameInTopic(userAndTopicId);
      for (String ip : banIps) {
        ipsToJson.add(new BanIP(ip));
      }
    } else {
      ipsToJson.clear();
      List<String> banIps = forumService.getAllTagName(str, userAndTopicId);
      for (String ip : banIps) {
        if (ip.startsWith(str))
          ipsToJson.add(new BanIP(ip));
      }
    }
    return Response.ok(new BeanToJsons<BanIP>(ipsToJson), JSON_CONTENT_TYPE).cacheControl(cc).build();
  }


  /**
   * Get public RSS of a given resource via its Id.
   * 
   * @param resourceid The resource Id, such as Category, Forum, Topic, and more.
   * @return RSSFeed file in .xml format.
   * @throws Exception The exception
   * 
   * @anchor KSref.DevelopersReferences.PublicRestAPIs.ForumWebservice.viewrss
   */

  @GET
  @Path("rss/{resourceid}")
  @Produces(MediaType.APPLICATION_XML)
  public Response viewrss(@PathParam("resourceid") String resourceid) throws Exception {
    try {
      ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      InputStream is = forumService.createForumRss(resourceid, "http://www.exoplatform.com");
      return Response.ok(is, MediaType.APPLICATION_XML).cacheControl(cc).build();
    } catch (Exception e) {
      log.trace("\nView RSS fail: " + e.getMessage() + "\n" + e.getCause());
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }


  /**
   * Get public RSS of a given resource of the current user via this resource's Id.
   * 
   * @param resourceid The user Id.
   * @return RSSFeed file in .xml format.
   * @throws Exception The exception
   * 
   * @anchor KSref.DevelopersReferences.PublicRestAPIs.ForumWebservice.userrss
   */

  @GET
  @Path("rss/user/{resourceid}")
  @Produces(MediaType.TEXT_XML)
  public Response userrss(@PathParam("resourceid") String resourceid) throws Exception {
    try {
      ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      InputStream is = forumService.createUserRss(resourceid, "http://www.exoplatform.com");
      return Response.ok(is, MediaType.APPLICATION_XML).cacheControl(cc).build();
    } catch (Exception e) {
      log.trace("\nGet UserRSS fail: ", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

}
