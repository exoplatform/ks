/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIPostForm;
import org.exoplatform.forum.webui.popup.UIPrivateMessageForm;
import org.exoplatform.forum.webui.popup.UISettingEditModeForm;
import org.exoplatform.forum.webui.popup.UIViewPostedByUser;
import org.exoplatform.forum.webui.popup.UIViewTopicCreatedByUser;
import org.exoplatform.forum.webui.popup.UIViewUserProfile;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.WebUIUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletApplication;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.mortbay.cometd.AbstractBayeux;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

/**
 * Author : Nguyen Quang Hung
 *          hung.nguyen@exoplatform.com
 * Aug 01, 2007
 */
@ComponentConfig(
                 lifecycle = UIApplicationLifecycle.class,
                 template = "app:/templates/forum/webui/UIForumPortlet.gtmpl",
                 events = {
                   @EventConfig(listeners = UIForumPortlet.ReLoadPortletEventActionListener.class),
                   @EventConfig(listeners = UIForumPortlet.ViewPublicUserInfoActionListener.class ) ,
                   @EventConfig(listeners = UIForumPortlet.ViewPostedByUserActionListener.class ),
                   @EventConfig(listeners = UIForumPortlet.PrivateMessageActionListener.class ),
                   @EventConfig(listeners = UIForumPortlet.ViewThreadByUserActionListener.class ),
                   @EventConfig(listeners = UIForumPortlet.OpenLinkActionListener.class)
                 }
)
public class UIForumPortlet extends UIPortletApplication {
  
  public static String QUICK_REPLY_EVENT_PARAMS    = "UIForumPortlet.QuickReplyEventParams";

  public static String FORUM_POLL_EVENT_PARAMS     = "UIForumPortlet.ForumPollEventParams";

  public static String RULE_EVENT_PARAMS           = "UIForumPortlet.RuleEventParams";

  public static String FORUM_MODERATE_EVENT_PARAMS = "UIForumPortlet.ForumModerateEvent";

  public static String FORUM_LINK_EVENT_PARAMS     = "UIForumPortlet.ForumLinkEvent";
  
  private ForumService forumService;

  private boolean      isCategoryRendered  = true;

  private boolean      isForumRendered     = false;

  private boolean      isTagRendered       = false;

  private boolean      isSearchRendered    = false;

  private boolean      isJumpRendered      = false;

  private boolean      isShowForumJump     = false;

  private boolean      isShowPoll          = false;

  private boolean      isShowModerators    = false;

  private boolean      isShowRules         = false;

  private boolean      isShowIconsLegend   = false;

  private boolean      isShowStatistics    = false;

  private boolean      isShowQuickReply    = false;

  private UserProfile  userProfile         = null;

  private boolean      enableIPLogging     = false;

  private boolean      prefForumActionBar  = false;

  private boolean      isRenderActionBar   = false;

  private boolean      enableBanIP         = false;

  private boolean      useAjax             = true;

  protected boolean    forumSpDeleted      = false;

  private int          dayForumNewPost     = 0;
  
  private String       categorySpId        = "";

  private String       forumSpId           = null;

  private String       spaceGroupId        = null;

  protected String       spaceDisplayName  = null;

  private List<String> invisibleForums     = new ArrayList<String>();

  private List<String> invisibleCategories = new ArrayList<String>();

  private PortletMode portletMode;
  public UIForumPortlet() throws Exception {
    forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    addChild(UIBreadcumbs.class, null, null);
    isRenderActionBar = !UserHelper.isAnonim();
    addChild(UIForumActionBar.class, null, null).setRendered(isRenderActionBar);
    addChild(UICategoryContainer.class, null, null).setRendered(isCategoryRendered);
    addChild(UIForumContainer.class, null, null).setRendered(isForumRendered);
    addChild(UITopicsTag.class, null, null).setRendered(isTagRendered);
    addChild(UISearchForm.class, null, null).setRendered(isSearchRendered);
    addChild(UIForumLinks.class, null, null).setRendered(isJumpRendered);
    UIPopupAction popupAction = addChild(UIPopupAction.class, null, "UIForumPopupAction");
    popupAction.getChild(UIPopupWindow.class).setId("UIForumPopupWindow");
    try {
      loadPreferences();
    } catch (Exception e) {
      log.warn("Failed to load portlet preferences", e);
    }
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext portletReqContext = (PortletRequestContext) context;
    portletMode = portletReqContext.getApplicationMode();
    if (portletMode == PortletMode.VIEW) {
      isRenderActionBar = !UserHelper.isAnonim();
      if (getChild(UIBreadcumbs.class) == null) {
        if (getChild(UISettingEditModeForm.class) != null)
          removeChild(UISettingEditModeForm.class);
        addChild(UIBreadcumbs.class, null, null);
        addChild(UIForumActionBar.class, null, null).setRendered(isRenderActionBar);
        UICategoryContainer categoryContainer = addChild(UICategoryContainer.class, null, null).setRendered(isCategoryRendered);
        addChild(UIForumContainer.class, null, null).setRendered(isForumRendered);
        addChild(UITopicsTag.class, null, null).setRendered(isTagRendered);
        addChild(UISearchForm.class, null, null).setRendered(isSearchRendered);
        addChild(UIForumLinks.class, null, null).setRendered(isJumpRendered);
        updateIsRendered(ForumUtils.CATEGORIES);
        categoryContainer.updateIsRender(true);
      }
      updateCurrentUserProfile();
    } else if (portletMode == PortletMode.EDIT) {
      if (getChild(UISettingEditModeForm.class) == null) {
        UISettingEditModeForm editModeForm = addChild(UISettingEditModeForm.class, null, null);
        editModeForm.setInitComponent();
        removeAllChildPorletView();
      }
    }
    try {
      renderComponentByURL(context);
    } catch (Exception e) {
      log.error("Can not open component by url, view exception: ", e);
    }
    super.processRender(app, context);
  }

  private void removeAllChildPorletView() {
    if (getChild(UIBreadcumbs.class) != null) {
      removeChild(UIBreadcumbs.class);
      removeChild(UIForumActionBar.class);
      removeChild(UICategoryContainer.class);
      removeChild(UIForumContainer.class);
      removeChild(UITopicsTag.class);
      removeChild(UISearchForm.class);
      removeChild(UIForumLinks.class);
    }
  }

  public void renderComponentByURL(WebuiRequestContext context) throws Exception {
    forumSpDeleted = false;
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    String isAjax = portalContext.getRequestParameter("ajaxRequest");
    if (isAjax != null && Boolean.parseBoolean(isAjax))
      return;

    String url = ((HttpServletRequest) portalContext.getRequest()).getRequestURL().toString();
    String pageNodeSelected = ForumUtils.SLASH + Util.getUIPortal().getSelectedUserNode().getURI();
    String portalName = Util.getUIPortal().getName();
    if(url.contains(portalName + pageNodeSelected)) {
      url = url.substring(url.lastIndexOf(portalName + pageNodeSelected)+ (portalName + pageNodeSelected).length());
    } else if(url.contains(pageNodeSelected)) {
      url = url.substring(url.lastIndexOf(pageNodeSelected)+ pageNodeSelected.length());
    }
    if(!ForumUtils.isEmpty(url)) {
      url = (url.contains(ForumUtils.SLASH+Utils.FORUM_SERVICE)) ? url.substring(url.lastIndexOf(Utils.FORUM_SERVICE)) :
           ((url.contains(ForumUtils.SLASH+Utils.CATEGORY)) ? url.substring(url.lastIndexOf(ForumUtils.SLASH+Utils.CATEGORY)+1) :
           ((url.contains(ForumUtils.SLASH+Utils.TOPIC)) ? url.substring(url.lastIndexOf(ForumUtils.SLASH+Utils.TOPIC)+1) :
           ((url.contains(ForumUtils.SLASH+Utils.FORUM)) ? url.substring(url.lastIndexOf(ForumUtils.SLASH+Utils.FORUM)+1) : url)));
    } else {
      if (!ForumUtils.isEmpty(getForumIdOfSpace())){
        url = getForumIdOfSpace();
      }
    }
    if (!ForumUtils.isEmpty(url) && url.length() > Utils.FORUM.length()) {
      calculateRenderComponent(url, context);
      context.addUIComponentToUpdateByAjax(this);
    }
  }

  public String getForumIdOfSpace() {
    
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences pref = pcontext.getRequest().getPreferences();
    if (pref.getValue("SPACE_URL", null) != null && ForumUtils.isEmpty(forumSpId)) {
      String url = pref.getValue("SPACE_URL", null);
      SpaceService sService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
      Space space = sService.getSpaceByUrl(url);
      spaceGroupId = space.getGroupId();
      forumSpId = Utils.FORUM_SPACE_ID_PREFIX + space.getPrettyName();
      spaceDisplayName = space.getDisplayName();
      try {
        OrganizationService service = (OrganizationService) PortalContainer.getInstance()
                                                                           .getComponentInstanceOfType(OrganizationService.class);
        String parentGrId = service.getGroupHandler().findGroupById(spaceGroupId).getParentId();
        categorySpId = Utils.CATEGORY + parentGrId.replaceAll(CommonUtils.SLASH, CommonUtils.EMPTY_STR);
      } catch (Exception e) {
        if (log.isDebugEnabled()){
          log.debug("Failed to set category id of space " + space.getPrettyName(), e);
        }
      }
    }
    return forumSpId;
  }

  public void updateIsRendered(String selected){
    if (selected.equals(ForumUtils.CATEGORIES)) {
      isCategoryRendered = true;
      isForumRendered = false;
      isTagRendered = false;
      isSearchRendered = false;
    } else if (selected.equals(ForumUtils.FORUM)) {
      isForumRendered = true;
      isCategoryRendered = false;
      isTagRendered = false;
      isSearchRendered = false;
    } else if (selected.equals(ForumUtils.TAG)) {
      isTagRendered = true;
      isForumRendered = false;
      isCategoryRendered = false;
      isSearchRendered = false;
    } else {
      isTagRendered = false;
      isForumRendered = false;
      isCategoryRendered = false;
      isSearchRendered = true;
    }
    if (!prefForumActionBar) {
      if (!isCategoryRendered || isSearchRendered) {
        isRenderActionBar = false;
      }
    }
    getChild(UIForumActionBar.class).setRendered(isRenderActionBar);
    setRenderForumLink();
    getChild(UIForumContainer.class).setRendered(isForumRendered);
    getChild(UITopicsTag.class).setRendered(isTagRendered);
    getChild(UISearchForm.class).setRendered(isSearchRendered);
    if (!isForumRendered) {
      this.setRenderQuickReply();
    }
  }

  public void renderForumHome() throws Exception{
    updateIsRendered(ForumUtils.CATEGORIES);
    UICategoryContainer categoryContainer = getChild(UICategoryContainer.class);
    categoryContainer.updateIsRender(true);
    categoryContainer.getChild(UICategories.class).setIsRenderChild(false);
    getChild(UIForumLinks.class).setUpdateForumLinks();
    getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
  }
  
  public void setRenderForumLink() {
    if (isShowForumJump) {
      if (!ForumUtils.isEmpty(getForumIdOfSpace())) {
        isJumpRendered = false;
      } else {
        isJumpRendered = getUserProfile().getIsShowForumJump();
      }
    } else {
      isJumpRendered = false;
    }
    UICategoryContainer categoryContainer = getChild(UICategoryContainer.class).setRendered(isCategoryRendered);
    categoryContainer.setIsRenderJump(isJumpRendered);
    if (!isCategoryRendered) {
      getChild(UIForumLinks.class).setRendered(isJumpRendered);
    }
  }

  public void setRenderQuickReply() {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletSession portletSession = pcontext.getRequest().getPortletSession();
    ActionResponse actionRes = null;
    if (pcontext.getResponse() instanceof ActionResponse) {
      actionRes = (ActionResponse) pcontext.getResponse();
    }
    ForumParameter param = new ForumParameter();
    param.setRenderQuickReply(false);
    param.setRenderPoll(false);
    param.setRenderModerator(false);
    param.setRenderRule(false);
    if (actionRes != null) {
      actionRes.setEvent(new QName("QuickReplyEvent"), param);
      actionRes.setEvent(new QName("ForumPollEvent"), param);
      actionRes.setEvent(new QName("ForumModerateEvent"), param);
      actionRes.setEvent(new QName("ForumRuleEvent"), param);
    } else {
      portletSession.setAttribute(UIForumPortlet.QUICK_REPLY_EVENT_PARAMS, param, PortletSession.APPLICATION_SCOPE);
      portletSession.setAttribute(UIForumPortlet.FORUM_POLL_EVENT_PARAMS, param, PortletSession.APPLICATION_SCOPE);
      portletSession.setAttribute(UIForumPortlet.FORUM_MODERATE_EVENT_PARAMS, param, PortletSession.APPLICATION_SCOPE);
      portletSession.setAttribute(UIForumPortlet.RULE_EVENT_PARAMS, param, PortletSession.APPLICATION_SCOPE);
    }
  }

  public void loadPreferences() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context instanceof PortletRequestContext){
    PortletRequestContext  pContext = (PortletRequestContext) context;
    PortletPreferences portletPref = pContext.getRequest().getPreferences();
    invisibleCategories.clear();
    invisibleForums.clear();
    prefForumActionBar = Boolean.parseBoolean(portletPref.getValue("showForumActionBar", ForumUtils.EMPTY_STR));
    dayForumNewPost = Integer.parseInt(portletPref.getValue("forumNewPost", ForumUtils.EMPTY_STR));
    useAjax = Boolean.parseBoolean(portletPref.getValue("useAjax", ForumUtils.EMPTY_STR));
    enableIPLogging = Boolean.parseBoolean(portletPref.getValue("enableIPLogging", ForumUtils.EMPTY_STR));
    enableBanIP = Boolean.parseBoolean(portletPref.getValue("enableIPFiltering", ForumUtils.EMPTY_STR));
    isShowForumJump = Boolean.parseBoolean(portletPref.getValue("isShowForumJump", ForumUtils.EMPTY_STR));
    isShowPoll = Boolean.parseBoolean(portletPref.getValue("isShowPoll", ForumUtils.EMPTY_STR));
    isShowModerators = Boolean.parseBoolean(portletPref.getValue("isShowModerators", ForumUtils.EMPTY_STR));
    isShowRules = Boolean.parseBoolean(portletPref.getValue("isShowRules", ForumUtils.EMPTY_STR));
    isShowQuickReply = Boolean.parseBoolean(portletPref.getValue("isShowQuickReply", ForumUtils.EMPTY_STR));
    isShowStatistics = Boolean.parseBoolean(portletPref.getValue("isShowStatistics", ForumUtils.EMPTY_STR));
    isShowIconsLegend = Boolean.parseBoolean(portletPref.getValue("isShowIconsLegend", ForumUtils.EMPTY_STR));
    invisibleCategories.addAll(getListInValus(portletPref.getValue("invisibleCategories", ForumUtils.EMPTY_STR)));
    invisibleForums.addAll(getListInValus(portletPref.getValue("invisibleForums", ForumUtils.EMPTY_STR)));
    if (invisibleCategories.size() == 1 && invisibleCategories.get(0).equals(" "))
      invisibleCategories.clear();
    }
  }

  private List<String> getListInValus(String value) throws Exception {
    List<String> list = new ArrayList<String>();
    if (!ForumUtils.isEmpty(value)) {
      list.addAll(Arrays.asList(ForumUtils.addStringToString(value, value)));
    }
    return list;
  }

  public String[] getImportJSTagCode() {
    return new String[] { "shCore", "shBrushBash", "shBrushCpp", "shBrushCSharp", "shBrushCss", "shBrushDelphi", "shBrushGroovy", "shBrushJava", "shBrushJScript", "shBrushPhp", "shBrushPython", "shBrushRuby", "shBrushScala", "shBrushSql", "shBrushVb", "shBrushXml" };
  }

  public List<String> getInvisibleForums() {
    return invisibleForums;
  }

  public List<String> getInvisibleCategories() {
    return invisibleCategories;
  }

  public boolean isEnableIPLogging() {
    return enableIPLogging;
  }

  public boolean isEnableBanIp() {
    return enableBanIP;
  }

  public boolean isShowForumActionBar() {
    return prefForumActionBar;
  }

  public boolean isShowPoll() {
    return isShowPoll;
  }

  public boolean isShowModerators() {
    return isShowModerators;
  }

  public boolean isShowRules() {
    return isShowRules;
  }

  public boolean isShowIconsLegend() {
    return isShowIconsLegend;
  }

  public boolean isShowQuickReply() {
    return isShowQuickReply;
  }

  public boolean isShowStatistics() {
    return isShowStatistics;
  }

  public boolean isUseAjax() {
    return useAjax;
  }

  public int getDayForumNewPost() {
    return dayForumNewPost;
  }

  public String getSpaceGroupId() {
    return spaceGroupId;
  }
  public void cancelAction() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    UIPopupAction popupAction = getChild(UIPopupAction.class);
    popupAction.deActivate();
    context.addUIComponentToUpdateByAjax(popupAction);
  }
  
  public void updateCurrentUserProfile() {
    try {
      String userId = UserHelper.getCurrentUser();
      if (enableBanIP) {
        userProfile = forumService.getDefaultUserProfile(userId, WebUIUtils.getRemoteIP());
      } else {
        userProfile = forumService.getDefaultUserProfile(userId, null);
      }
      if (!ForumUtils.isEmpty(userId))
        userProfile.setEmail(UserHelper.getUserByUserId(userId).getEmail());
      if (userProfile.getIsBanned())
        userProfile.setUserRole((long) 3);
    } catch (Exception e) {
      userProfile = new UserProfile();
    }
    if(UserProfile.USER_DELETED == userProfile.getUserRole() ||
       UserProfile.GUEST == userProfile.getUserRole() ) {
      isRenderActionBar = false;
    }
    UIForumActionBar actionBar = getChild(UIForumActionBar.class);
    if (actionBar != null) {
      actionBar.setRendered(isRenderActionBar);
    }
  }

  public UserProfile getUserProfile() {
    if (userProfile == null) {
      updateCurrentUserProfile();
    }
    return userProfile;
  }

  public void updateAccessTopic(String topicId) throws Exception {
    String userId = userProfile.getUserId();
    if (userId != null && userId.length() > 0) {
      forumService.updateTopicAccess(userId, topicId);
    }
    userProfile.setLastTimeAccessTopic(topicId, CommonUtils.getGreenwichMeanTime().getTimeInMillis());
  }

  public void updateAccessForum(String forumId) throws Exception {
    String userId = userProfile.getUserId();
    if (userId != null && userId.length() > 0) {
      forumService.updateForumAccess(userId, forumId);
    }
    userProfile.setLastTimeAccessForum(forumId, CommonUtils.getGreenwichMeanTime().getTimeInMillis());
  }

  public void removeCacheUserProfile() {
    try {
      forumService.removeCacheUserProfile(userProfile.getUserId());
    } catch (Exception e) {
      log.debug("Failed to remove cache userprofile with user: " + userProfile.getUserId());
    }
  }

  public String getPortletLink(String actionName, String userName) {
    try {
      return event(actionName, userName);
    } catch (Exception e) {
      log.debug("Failed to set link to view info user.", e);
      return null;
    }
  }

  protected String getCometdContextName() {
    EXoContinuationBayeux bayeux = (EXoContinuationBayeux) PortalContainer.getInstance()
                                                                          .getComponentInstanceOfType(AbstractBayeux.class);
    return (bayeux == null ? "cometd" : bayeux.getCometdContextName());
  }

  public String getUserToken() throws Exception {
    try {
      ContinuationService continuation = (ContinuationService) PortalContainer.getInstance()
                                                                         .getComponentInstanceOfType(ContinuationService.class);
      return continuation.getUserToken(userProfile.getUserId());
    } catch (Exception e) {
      log.error("Could not retrieve continuation token for user " + userProfile.getUserId(), e);
    }
    return ForumUtils.EMPTY_STR;
  }

  private boolean isArrayNotNull(String[] strs) {
    if (strs != null && strs.length > 0 && !strs[0].equals(" "))
      return true;// private
    else
      return false;
  }
  
  public boolean checkForumHasAddTopic(String categoryId, String forumId) throws Exception {
    if (getUserProfile().getUserRole() == 0) return true;
    if (getUserProfile().getUserId().contains(UserProfile.USER_GUEST)) return false;
    try {
      Forum forum = (Forum) forumService.getObjectNameById(forumId, Utils.FORUM);
      if (forum.getIsClosed() || forum.getIsLock())
        return false;
      Category cate = (Category) forumService.getObjectNameById(categoryId, Utils.CATEGORY);
      boolean isAdd = true;
      if(!Utils.isEmpty(cate.getUserPrivate())) {
        isAdd = ForumServiceUtils.hasPermission(cate.getUserPrivate(), userProfile.getUserId());
      }
      if(isAdd) {
        if (userProfile.getUserRole() > 1 || (userProfile.getUserRole() == 1 && !ForumServiceUtils.hasPermission(forum.getModerators(), userProfile.getUserId()))) {
          String[] canCreadTopic = ForumUtils.arraysMerge(forum.getCreateTopicRole(), cate.getCreateTopicRole());
          if (!Utils.isEmpty(canCreadTopic) && !canCreadTopic[0].equals(" ")) {
            return ForumServiceUtils.hasPermission(canCreadTopic, userProfile.getUserId());
          }
        }
      } else return false;
    } catch (Exception e) {
      throw e;
    }
    return true;
  }
  
  public boolean checkForumHasAddPost(String categoryId, String forumId, String topicId) throws Exception {
    if (getUserProfile().getUserRole() == 0) return true;
    if (getUserProfile().getUserId().contains(UserProfile.USER_GUEST)) return false;
    try {
      Topic topic = (Topic) forumService.getObjectNameById(topicId, Utils.TOPIC);
      if (topic.getIsClosed() || topic.getIsLock())
        return false;
      Forum forum = (Forum) forumService.getObjectNameById(forumId, Utils.FORUM);
      if (forum.getIsClosed() || forum.getIsLock())
        return false;
      Category cate = (Category) forumService.getObjectNameById(categoryId, Utils.CATEGORY);
      boolean isAdd = true;
      if(!Utils.isEmpty(cate.getUserPrivate())) {
        isAdd = ForumServiceUtils.hasPermission(cate.getUserPrivate(), userProfile.getUserId());
      }
      if(isAdd) {
        if (userProfile.getUserRole() > 1 || (userProfile.getUserRole() == 1 && !ForumServiceUtils.hasPermission(forum.getModerators(), userProfile.getUserId()))) {
          if (!topic.getIsActive() || !topic.getIsActiveByForum())
            return false;
          String[] canCreadPost = ForumUtils.arraysMerge(cate.getCreateTopicRole(), ForumUtils.arraysMerge(topic.getCanPost(), forum.getCreateTopicRole()));
          if (!ForumUtils.isArrayEmpty(canCreadPost)) {
            return ForumServiceUtils.hasPermission(canCreadPost, userProfile.getUserId());
          }
        }
      } else return false;
    } catch (Exception e) {
      throw e;
    }
    return true;
  }

  public boolean checkCanView(Category cate, Forum forum, Topic topic) throws Exception {
    String userId = getUserProfile().getUserId();
    if (userProfile.getUserRole() == 0)
      return true;
    List<String> userBound = UserHelper.getAllGroupAndMembershipOfUser(userId);
    String[] viewer = cate.getUserPrivate();
    if (isArrayNotNull(viewer)) {
      if (!Utils.hasPermission(Arrays.asList(viewer), userBound))
        return false;
    }
    if (forum != null) {
      if (isArrayNotNull(forum.getModerators())) {
        if (Utils.hasPermission(Arrays.asList(forum.getModerators()), userBound))
          return true;
      } else if (forum.getIsClosed())
        return false;
    }
    if (topic != null) {
      List<String> list = new ArrayList<String>();
      list = ForumUtils.addArrayToList(list, topic.getCanView());
      list = ForumUtils.addArrayToList(list, forum.getViewer());
      list = ForumUtils.addArrayToList(list, cate.getViewer());

      if (!list.isEmpty() && topic.getOwner() != null)
        list.add(topic.getOwner());
      if (topic.getIsClosed() || !topic.getIsActive() || !topic.getIsActiveByForum() || !topic.getIsApproved() || topic.getIsWaiting() || (!list.isEmpty() && !Utils.hasPermission(list, userBound)))
        return false;
    }
    return true;
  }

  
  public static void showWarningMessage(WebuiRequestContext context, String key, String... args) {
    context.getUIApplication().addMessage(new ApplicationMessage(key, args, ApplicationMessage.WARNING));
  }
  
  public void calculateRenderComponent(String path, WebuiRequestContext context) throws Exception {
    ResourceBundle res = context.getApplicationResourceBundle();
    if (path.equals(Utils.FORUM_SERVICE)) {
      renderForumHome();
    } else if (path.indexOf(ForumUtils.FIELD_SEARCHFORUM_LABEL) >= 0) {
      updateIsRendered(ForumUtils.FIELD_SEARCHFORUM_LABEL);
      UISearchForm searchForm = getChild(UISearchForm.class);
      searchForm.setUserProfile(getUserProfile());
      searchForm.setPath(ForumUtils.EMPTY_STR);
      searchForm.setSelectType(path.replaceFirst(ForumUtils.FIELD_SEARCHFORUM_LABEL, ""));
      path = ForumUtils.FIELD_EXOFORUM_LABEL;
    } else if (path.lastIndexOf(Utils.TAG) >= 0) {
      updateIsRendered(ForumUtils.TAG);
      getChild(UIForumLinks.class).setValueOption(ForumUtils.EMPTY_STR);
      getChild(UITopicsTag.class).setIdTag(path);
    } else if (path.lastIndexOf(Utils.TOPIC) >= 0) {
      boolean isReply = false, isQuote = false;
      if (path.indexOf("/true") > 0) {
        isQuote = true;
        path = path.replaceFirst("/true", ForumUtils.EMPTY_STR);
      } else if (path.indexOf("/false") > 0) {
        isReply = true;
        path = path.replaceFirst("/false", ForumUtils.EMPTY_STR);
      }
      if(path.indexOf(Utils.CATEGORY) > 0) {
        path = path.substring(path.indexOf(Utils.CATEGORY));
      }
      String[] id = path.split(ForumUtils.SLASH);
      String postId = "top";
      int page = 0;
      if (path.indexOf(Utils.POST) > 0) {
        postId = id[id.length - 1];
        path = path.substring(0, path.lastIndexOf(ForumUtils.SLASH));
        id = path.split(ForumUtils.SLASH);
      } else if (id.length > 1) {
        try {
          page = Integer.parseInt(id[id.length - 1]);
        } catch (NumberFormatException e) {
          if (log.isDebugEnabled()){
            log.debug("Failed to parse number " + id[id.length - 1], e);
          }
        }
        if (page > 0) {
          path = path.replace(ForumUtils.SLASH + id[id.length - 1], ForumUtils.EMPTY_STR);
          id = path.split(ForumUtils.SLASH);
        } else
          page = 0;
      }
      try {
        Topic topic;
        if (id.length > 1) {
          topic = this.forumService.getTopicByPath(path, false);
        } else {
          topic = (Topic) this.forumService.getObjectNameById(path, Utils.TOPIC);
        }
        if (topic != null) {
          if (path.indexOf(ForumUtils.SLASH) < 0) {
            path = topic.getPath();
            path = path.substring(path.indexOf(Utils.CATEGORY));
            id = path.split(ForumUtils.SLASH);
          }
          Category category = this.forumService.getCategory(id[0]);
          Forum forum = this.forumService.getForum(id[0], id[1]);
          if (this.checkCanView(category, forum, topic)) {
            this.updateIsRendered(ForumUtils.FORUM);
            UIForumContainer uiForumContainer = this.getChild(UIForumContainer.class);
            UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class);
            uiForumContainer.setIsRenderChild(false);
            uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
            UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class);
            uiTopicDetail.setIsEditTopic(true);
            uiTopicDetail.setUpdateForum(forum);
            uiTopicDetail.initInfoTopic(id[0], id[1], topic, page);
            uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1], topic.getId());
            this.getChild(UIForumLinks.class).setValueOption((id[0] + ForumUtils.SLASH + id[1] + " "));
            uiTopicDetail.setIdPostView(postId);
            uiTopicDetail.setLastPostId((postId.equals("top")?ForumUtils.EMPTY_STR:postId));
            if (isReply || isQuote) {
              if (uiTopicDetail.getCanPost()) {
                uiTopicDetail.setIdPostView("top");
                try {
                  UIPopupAction popupAction = this.getChild(UIPopupAction.class);
                  UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
                  UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null);
                  boolean isMod = ForumServiceUtils.hasPermission(forum.getModerators(), this.userProfile.getUserId());
                  postForm.setPostIds(id[0], id[1], topic.getId(), topic);
                  postForm.setMod(isMod);
                  if (isQuote) {
                    // uiTopicDetail.setLastPostId(postId) ;
                    Post post = this.forumService.getPost(id[0], id[1], topic.getId(), postId);
                    if (post != null) {
                      postForm.updatePost(postId, true, false, post);
                      popupContainer.setId("UIQuoteContainer");
                    } else {
                      showWarningMessage(context, "UIBreadcumbs.msg.post-no-longer-exist", ForumUtils.EMPTY_STR);
                      uiTopicDetail.setIdPostView("normal");
                    }
                  } else {
                    postForm.updatePost(ForumUtils.EMPTY_STR, false, false, null);
                    popupContainer.setId("UIAddPostContainer");
                  }
                  popupAction.activate(popupContainer, 900, 500);
                  context.addUIComponentToUpdateByAjax(popupAction);
                } catch (Exception e) {
                  log.error(e);
                }
              } else {
                showWarningMessage(context, "UIPostForm.msg.no-permission", ForumUtils.EMPTY_STR);
              }
            }
            if (!UserHelper.isAnonim()) {
              this.forumService.updateTopicAccess(userProfile.getUserId(), topic.getId());
              this.getUserProfile().setLastTimeAccessTopic(topic.getId(), CommonUtils.getGreenwichMeanTime().getTimeInMillis());
            }
          } else {
            showWarningMessage(context, "UIBreadcumbs.msg.do-not-permission", 
                               new String[] { topic.getTopicName(), res.getString("UIForumPortlet.label.topic").toLowerCase() });
            if (!ForumUtils.isEmpty(getForumIdOfSpace())) {
              calculateRenderComponent(forumSpId, context);
            } else {
              renderForumHome();
              path = Utils.FORUM_SERVICE;
            }
          }
        } else if (!ForumUtils.isEmpty(getForumIdOfSpace())) {
          if (forumService.getForum(categorySpId, forumSpId) == null) {
            forumSpDeleted = true;
            removeAllChildPorletView();
            log.info("The forum in space " + spaceDisplayName + " no longer exists.");
            return;
          } else {
            showWarningMessage(context, "UIShowBookMarkForm.msg.link-not-found", ForumUtils.EMPTY_STR);
            calculateRenderComponent(forumSpId, context);
          }
        }
      } catch (Exception e) {
        if (log.isDebugEnabled()){
          log.debug("Failed to render forum link: [" + path + "]. Forum home will be rendered.\nCaused by:", e);
        }
        showWarningMessage(context, "UIShowBookMarkForm.msg.link-not-found", ForumUtils.EMPTY_STR);
        renderForumHome();
        path = Utils.FORUM_SERVICE;
      }
    } else if ((path.lastIndexOf(Utils.FORUM) == 0 && path.lastIndexOf(Utils.CATEGORY) < 0) || (path.lastIndexOf(Utils.FORUM) > 0)) {
      try {
        Forum forum = null;
        String cateId = null;
        int page = 0;
        if (path.indexOf(ForumUtils.SLASH) >= 0) {
          path = path.substring(path.indexOf(Utils.CATEGORY));
          String[] arr = path.split(ForumUtils.SLASH);
          try {
            page = Integer.parseInt(arr[arr.length - 1]);
          } catch (Exception e) {
            if (log.isDebugEnabled()){
              log.debug("Failed to parse number " + arr[arr.length - 1], e);
            }
          }
          if (arr[0].indexOf(Utils.CATEGORY) == 0) {
            cateId = arr[0];
            forum = this.forumService.getForum(cateId, arr[1]);
          } else {
            forum = (Forum) this.forumService.getObjectNameById(arr[0], Utils.FORUM);
          }
        }
        if (forum == null) {
          forum = (Forum) this.forumService.getObjectNameById(path, Utils.FORUM);
          if (forum == null && path.equals(getForumIdOfSpace())) {
            forum = forumService.getForum(this.categorySpId, path);
          }
          if(forum == null) {
            forumSpDeleted = true;
            removeAllChildPorletView();
            log.info("The forum in space " + spaceDisplayName + " no longer exists.");
            return;
          }
        }
        path = forum.getPath();
        if (cateId == null) {
          cateId = path.substring(path.indexOf(Utils.CATEGORY), path.lastIndexOf(Utils.FORUM) - 1);
        }
        path = path.substring(path.indexOf(Utils.CATEGORY));
        Category category = this.forumService.getCategory(cateId);
        if (this.checkCanView(category, forum, null)) {
          this.updateIsRendered(ForumUtils.FORUM);
          UIForumContainer forumContainer = this.findFirstComponentOfType(UIForumContainer.class);
          forumContainer.setIsRenderChild(true);
          forumContainer.getChild(UIForumDescription.class).setForum(forum);
          forumContainer.getChild(UITopicContainer.class).setUpdateForum(cateId, forum, page);
        } else {
          showWarningMessage(context, "UIBreadcumbs.msg.do-not-permission", 
                             new String[] { forum.getForumName(), res.getString("UIForumPortlet.label.forum").toLowerCase() });
          renderForumHome();
          path = Utils.FORUM_SERVICE;
        }
      } catch (Exception e) {
        if (log.isDebugEnabled()){
          log.debug("Failed to render forum link: [" + path + "]. Forum home will be rendered.\nCaused by:", e);
        }
        showWarningMessage(context, "UIShowBookMarkForm.msg.link-not-found", 
                           new String[] { res.getString("UIForumPortlet.label.forum") });
        renderForumHome();
        path = Utils.FORUM_SERVICE;
      }
    } else if (path.indexOf(Utils.CATEGORY) >= 0 && path.indexOf(ForumUtils.SLASH) < 0) {
      UICategoryContainer categoryContainer = this.getChild(UICategoryContainer.class);
      try {
        Category category = this.forumService.getCategory(path);
        if (this.checkCanView(category, null, null)) {
          categoryContainer.getChild(UICategory.class).updateByLink(category);
          categoryContainer.updateIsRender(false);
          this.updateIsRendered(ForumUtils.CATEGORIES);
        } else {
          showWarningMessage(context, "UIBreadcumbs.msg.do-not-permission", 
                             new String[] { category.getCategoryName(), res.getString("UIForumPortlet.label.category").toLowerCase() });
          renderForumHome();
          path = Utils.FORUM_SERVICE;
        }
      } catch (Exception e) {
        if (log.isDebugEnabled()){
          log.debug("Failed to render forum link: [" + path + "]. Forum home will be rendered.\nCaused by:", e);
        }
        showWarningMessage(context, "UIShowBookMarkForm.msg.link-not-found", ForumUtils.EMPTY_STR);
        renderForumHome();
        path = Utils.FORUM_SERVICE;
      }
    } else {
      if (log.isDebugEnabled()){
        log.debug("Failed to render forum link: [" + path + "]. Forum home will be rendered.");
      }
      showWarningMessage(context, "UIShowBookMarkForm.msg.link-not-found", ForumUtils.EMPTY_STR);
      renderForumHome();
      path = Utils.FORUM_SERVICE;
    }
    getChild(UIBreadcumbs.class).setUpdataPath(path);
    getChild(UIForumLinks.class).setValueOption(path);
  }

  static public class ReLoadPortletEventActionListener extends EventListener<UIForumPortlet> {
    public void execute(Event<UIForumPortlet> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource();
      ForumParameter params = (ForumParameter) event.getRequestContext().getAttribute(PortletApplication.PORTLET_EVENT_VALUE);
      if (params.getTopicId() != null) {
        forumPortlet.userProfile.setLastTimeAccessTopic(params.getTopicId(), CommonUtils.getGreenwichMeanTime().getTimeInMillis());
        UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class);
        topicDetail.setIdPostView("lastpost");
      }
      if (params.isRenderPoll()) {
        UITopicDetailContainer topicDetailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class);
        topicDetailContainer.getChild(UITopicDetail.class).setIsEditTopic(true);
        topicDetailContainer.getChild(UITopicPoll.class).setEditPoll(true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }
  
  static public class OpenLinkActionListener extends EventListener<UIForumPortlet> {
    public void execute(Event<UIForumPortlet> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      if (ForumUtils.isEmpty(path)) {
        ForumParameter params = (ForumParameter) event.getRequestContext().getAttribute(PortletApplication.PORTLET_EVENT_VALUE);
        path = params.getPath();
      }
      if (ForumUtils.isEmpty(path))
        return;
      forumPortlet.calculateRenderComponent(path, event.getRequestContext());
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class ViewPublicUserInfoActionListener extends EventListener<UIForumPortlet> {
    public void execute(Event<UIForumPortlet> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource();
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class);
      UIViewUserProfile viewUserProfile = popupAction.createUIComponent(UIViewUserProfile.class, null, null);
      try {
        UserProfile selectProfile = forumPortlet.forumService.getUserInformations(forumPortlet.forumService.getQuickProfile(userId.trim()));
        viewUserProfile.setUserProfileViewer(selectProfile);
      } catch (Exception e) {
        log.debug("Fail to set user profile.", e);
        showWarningMessage(event.getRequestContext(), "UITopicDetail.msg.userIsDeleted", new String[] { userId });
        return;
      }
      popupAction.activate(viewUserProfile, 670, 400, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class PrivateMessageActionListener extends EventListener<UIForumPortlet> {
    public void execute(Event<UIForumPortlet> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource();
      if (forumPortlet.userProfile.getIsBanned()) {
        showWarningMessage(event.getRequestContext(), "UITopicDetail.msg.userIsBannedCanNotSendMail", ForumUtils.EMPTY_STR);
        return;
      }
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      int t = userId.indexOf(Utils.DELETED);
      if (t < 0) {
        try {
          forumPortlet.forumService.getQuickProfile(userId.trim());
        } catch (Exception e) {
          t = 1;
        }
      }
      if (t > 0) {
        showWarningMessage(event.getRequestContext(), "UITopicDetail.msg.userIsDeleted", userId.substring(0, t) );
        return;
      }
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UIPrivateMessageForm messageForm = popupContainer.addChild(UIPrivateMessageForm.class, null, null);
      messageForm.setFullMessage(false);
      messageForm.setUserProfile(forumPortlet.userProfile);
      messageForm.setSendtoField(userId);
      popupContainer.setId("PrivateMessageForm");
      popupAction.activate(popupContainer, 720, 550);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class ViewPostedByUserActionListener extends EventListener<UIForumPortlet> {
    public void execute(Event<UIForumPortlet> event) throws Exception {
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIForumPortlet forumPortlet = event.getSource();
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UIViewPostedByUser viewPostedByUser = popupContainer.addChild(UIViewPostedByUser.class, null, null);
      viewPostedByUser.setUserProfile(userId);
      popupContainer.setId("ViewPostedByUser");
      popupAction.activate(popupContainer, 760, 370);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class ViewThreadByUserActionListener extends EventListener<UIForumPortlet> {
    public void execute(Event<UIForumPortlet> event) throws Exception {
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIForumPortlet forumPortlet = event.getSource();
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UIViewTopicCreatedByUser topicCreatedByUser = popupContainer.addChild(UIViewTopicCreatedByUser.class, null, null);
      topicCreatedByUser.setUserId(userId);
      popupContainer.setId("ViewTopicCreatedByUser");
      popupAction.activate(popupContainer, 760, 450);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }
}
