/**
 * 
 */
package org.exoplatform.poll.service.ws;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.PollNodeTypes;
import org.exoplatform.poll.service.PollService;
import org.exoplatform.poll.service.PollSummary;
import org.exoplatform.poll.service.Utils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.IdentityConstants;

/**
 * @author Vu Duy Tu
 * 
 */
@Path("ks/poll")
public class PollWebservice implements ResourceContainer {
  private static final Log   log      = ExoLogger.getLogger(PollWebservice.class);

  final public static String APP_TYPE = "poll".intern();
  private OrganizationService organizationService = null;
  
  public PollWebservice() {
    getOrganizationService();
  }
  
  private static final CacheControl         cc;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cc = new CacheControl();
    cc.setNoCache(true);
    cc.setNoStore(true);
  }
  
  private OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer()
                                .getComponentInstance(OrganizationService.class);
    }
    return organizationService;
  }
  
  private String getUserId(SecurityContext sc, UriInfo uriInfo) {
    if (sc != null && sc.getUserPrincipal() != null) {
      return sc.getUserPrincipal().getName();
    } else if (uriInfo != null) {
      return getViewerId(uriInfo);
    }
    return StringUtils.EMPTY;
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

  @GET
  @Path("/viewpoll/{resourceid}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response viewPoll(@PathParam("resourceid") String pollId,
                           @Context SecurityContext sc,
                           @Context UriInfo uriInfo) throws Exception {
    PollService pollService = (PollService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
    String username = getUserId(sc, uriInfo);
    if (!Utils.isEmpty(pollId)) {
      try {
        Poll poll = pollService.getPoll(pollId);
        if (poll != null) {
          // poll.setIsAdmin(String.valueOf(hasGroupAdminOfGatein()));
          poll.setIsAdmin("true");
          String group = poll.getParentPath();
          boolean hasPerminsion = false;
          if (group.indexOf(PollNodeTypes.APPLICATION_DATA) > 0 && poll.getIsAdmin().equals("false")) {
            group = group.substring(group.indexOf(PollNodeTypes.GROUPS + "/") + PollNodeTypes.GROUPS.length(), group.indexOf("/" + PollNodeTypes.APPLICATION_DATA));
            for (String group_ : getGroupsOfUser(username)) {
              if (group_.indexOf(group) >= 0) {
                hasPerminsion = true;
                break;
              }
            }
          } else if (group.indexOf(PollNodeTypes.POLLS) < 0) {
            hasPerminsion = pollService.hasPermissionInForum(group + "/" + poll.getId(), getAllGroupAndMembershipOfUser(username));
          } else {
            hasPerminsion = true;
          }
          if (!hasPerminsion) {
            poll = new Poll();
            poll.setId("DoNotPermission");
            return Response.ok(poll, MediaType.APPLICATION_JSON).cacheControl(cc).build();
          }
          poll.setVotes();
          poll.setInfoVote();
          poll.setShowVote(isGuestPermission(poll, username));
          return Response.ok(poll, MediaType.APPLICATION_JSON).cacheControl(cc).build();
        }
      } catch (Exception e) {
        log.error("Can not get poll by id: " + pollId, e);
      }
    }
    PollSummary pollSummary = new PollSummary();
    pollSummary = pollService.getPollSummary(getAllGroupAndMembershipOfUser(username));
    pollSummary.setIsAdmin("true");
    return Response.ok(pollSummary, MediaType.APPLICATION_JSON).cacheControl(cc).build();
  }

  @GET
  @Path("/votepoll/{pollId}/{indexVote}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response votePoll(@PathParam("pollId") String pollId,
                           @PathParam("indexVote") String indexVote,
                           @Context SecurityContext sc,
                           @Context UriInfo uriInfo) throws Exception {
    if (!Utils.isEmpty(pollId) && !Utils.isEmpty(indexVote)) {
      try {
        PollService pollService = (PollService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
        Poll poll = pollService.getPoll(pollId.trim());
        String username = getUserId(sc, uriInfo);
        if (poll != null && !IdentityConstants.ANONIM.equals(username) && 
            validateIndexVote(indexVote, poll.getOption().length)) {
          poll = Utils.calculateVote(poll, username, indexVote);
          pollService.savePoll(poll, false, true);
          poll.setVotes();
          poll.setInfoVote();
          poll.setShowVote(isGuestPermission(poll, username));
          poll.setIsAdmin(String.valueOf(hasGroupAdminOfGatein(username)));
          return Response.ok(poll, MediaType.APPLICATION_JSON).cacheControl(cc).build();
        }
      } catch (Exception e) {
        log.debug("Failed to vote poll.", e);
      }
      return Response.ok("You do not have permission to vote this poll; or some options have been removed from the poll.", MediaType.TEXT_PLAIN).cacheControl(cc).build();
    }
    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
  }

  private boolean validateIndexVote(String indexVote, int max) {
    String[] ivArr = indexVote.split(Utils.COLON);
    for (int i = 1; i < ivArr.length; i++) {
      try {
        int t = Integer.parseInt(ivArr[i]);
        if (t >= max) {
          return false;
        }
      } catch (Exception e) {
        return false;
      }
    }
    return true;
  }

  private boolean isGuestPermission(Poll poll_, String username) throws Exception {
    if (poll_.getIsClosed())
      return true;
    if (poll_.getTimeOut() > 0) {
      Date today = Utils.getGreenwichMeanTime().getTime();
      if ((today.getTime() - poll_.getCreatedDate().getTime()) >= poll_.getTimeOut() * 86400000)
        return true;
    }
    if (Utils.isEmpty(username) || IdentityConstants.ANONIM.equals(username))
      return true;
    String[] userVotes = poll_.getUserVote();
    for (String string : userVotes) {
      string = string.substring(0, string.indexOf(":"));
      if (string.equalsIgnoreCase(username))
        return true;
    }
    return false;
  }

  private boolean hasGroupAdminOfGatein(String username) {
    try {
      UserACL userACL = (UserACL) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);
      List<String> list = new ArrayList<String>();
      list.add(username);
      list.addAll(getGroupsOfUser(username));
      for (String str : list) {
        if (str.equals(userACL.getSuperUser()) || str.equals(userACL.getAdminGroups()))
          return true;
      }
    } catch (Exception e) {
      log.debug("Failed to check has group admin of gatein." + e.getCause());
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private List<String> getAllGroupAndMembershipOfUser(String username) {
    List<String> listOfUser = new ArrayList<String>();
    try {
      listOfUser.add(username);// himself
      Set<String> list = new HashSet<String>();
      Collection<Membership> memberships = organizationService.getMembershipHandler().findMembershipsByUser(username);
      for (Membership membership : memberships) {
        listOfUser.add(membership.getGroupId()); // its groups
        listOfUser.add(membership.getMembershipType() + ":" + membership.getGroupId()); // its memberships
      }
      listOfUser.addAll(list);
    } catch (Exception e) {
      log.warn("Failed to get all info of user.");
    }
    return listOfUser;
  }

  private List<String> getGroupsOfUser(String username) {
    try {
      List<String> grIds = new ArrayList<String>();
      for (Object gr : getOrganizationService().getGroupHandler().findGroupsOfUser(username)) {
        grIds.add(((Group) gr).getId());
      }
      return grIds;
    } catch (Exception e) {
      log.warn("Failed to get groupId of user.");
      return new ArrayList<String>();
    }
  }
}
