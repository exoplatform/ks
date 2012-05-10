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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
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
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
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
import org.exoplatform.forum.service.Watch;
import org.exoplatform.forum.webui.popup.UIPostForm;
import org.exoplatform.forum.webui.popup.UIPrivateMessageForm;
import org.exoplatform.forum.webui.popup.UISettingEditModeForm;
import org.exoplatform.forum.webui.popup.UIViewPostedByUser;
import org.exoplatform.forum.webui.popup.UIViewTopicCreatedByUser;
import org.exoplatform.forum.webui.popup.UIViewUserProfile;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.user.CommonContact;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletApplication;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;

/**
 * Author : Nguyen Quang Hung
 *					hung.nguyen@exoplatform.com
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
  private ForumService forumService;
  private boolean isCategoryRendered = true;
  private boolean isForumRendered = false;
  private boolean isTagRendered = false;
  private boolean isSearchRendered = false;
  private boolean isJumpRendered = false;
  private boolean isShowForumJump = false;
  private boolean isShowPoll = false;
  private boolean isShowModerators = false;
  private boolean isShowRules = false;
  private boolean isShowIconsLegend = false;
  private boolean isShowStatistics = false;
  private boolean isShowQuickReply = false;
  private UserProfile userProfile = null;
  private boolean enableIPLogging = false;
  private boolean isShowForumActionBar = false;
  private boolean enableBanIP = false;
  private boolean useAjax = true;
  private int dayForumNewPost = 0;
  private String linkUserInfo = "";
  private String spaceGroupId = null;
  private List<String>invisibleForums = new ArrayList<String>();
  private List<String>invisibleCategories = new ArrayList<String>();
  private List<Watch> listWatches = null;
  public UIForumPortlet() throws Exception {
    forumService = (ForumService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class) ;
    addChild(UIBreadcumbs.class, null, null) ;
    boolean isRenderBar = !UserHelper.isAnonim() ;
    addChild(UIForumActionBar.class, null, null).setRendered(isRenderBar);
    addChild(UICategoryContainer.class, null, null).setRendered(isCategoryRendered) ;
    addChild(UIForumContainer.class, null, null).setRendered(isForumRendered) ;
    addChild(UITopicsTag.class, null, null).setRendered(isTagRendered) ;
    addChild(UISearchForm.class, null, null).setRendered(isSearchRendered) ;
    addChild(UIForumLinks.class, null, null).setRendered(isJumpRendered) ;
    UIPopupAction popupAction = addChild(UIPopupAction.class, null, "UIForumPopupAction") ;
    popupAction.getChild(UIPopupWindow.class).setId("UIForumPopupWindow");
    try {
      loadPreferences();
    } catch (Exception e) {}
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
    if(portletReqContext.getApplicationMode() == PortletMode.VIEW) {
      if(getChild(UIBreadcumbs.class) ==  null) {
        if(getChild(UISettingEditModeForm.class) != null)
          removeChild(UISettingEditModeForm.class);
        addChild(UIBreadcumbs.class, null, null) ;
        addChild(UIForumActionBar.class, null, null).setRendered(!UserHelper.isAnonim());
        UICategoryContainer categoryContainer = addChild(UICategoryContainer.class, null, null).setRendered(isCategoryRendered) ;
        addChild(UIForumContainer.class, null, null).setRendered(isForumRendered) ;
        addChild(UITopicsTag.class, null, null).setRendered(isTagRendered) ;
        addChild(UISearchForm.class, null, null).setRendered(isSearchRendered) ;
        addChild(UIForumLinks.class, null, null).setRendered(isJumpRendered) ;
        updateIsRendered(ForumUtils.CATEGORIES);
        categoryContainer.updateIsRender(true) ;
      }
    }else if(portletReqContext.getApplicationMode() == PortletMode.EDIT) {
      if(getChild(UISettingEditModeForm.class) == null) {
        UISettingEditModeForm editModeForm = addChild(UISettingEditModeForm.class, null, null);
        editModeForm.setUserProfile(getUserProfile());
        if(getChild(UIBreadcumbs.class) != null) {
          removeChild(UIBreadcumbs.class) ;
          removeChild(UIForumActionBar.class) ;
          removeChild(UICategoryContainer.class) ;
          removeChild(UIForumContainer.class) ;
          removeChild(UITopicsTag.class) ;
          removeChild(UISearchForm.class) ;
          removeChild(UIForumLinks.class);
        }
      }
    }
    try {
      renderComponentByURL(context);
    } catch (Exception e) {
      log.error("Can not open component by url, view exception: ", e);
    }
    super.processRender(app, context) ;
  }

  public void renderComponentByURL( WebuiRequestContext context) throws Exception {

    PortalRequestContext portalContext = Util.getPortalRequestContext();

    String url = ((HttpServletRequest)portalContext.getRequest()).getRequestURL().toString();
    String old = url ;
    String isAjax = portalContext.getRequestParameter("ajaxRequest");
    if(isAjax != null && Boolean.parseBoolean(isAjax)) return;
    String portalName = Util.getUIPortal().getName() ;
    url = (url.contains(portalName))? url.substring(url.lastIndexOf(portalName)): url;
    url = (url.contains(Utils.FORUM_SERVICE))? url.substring(url.lastIndexOf(Utils.FORUM_SERVICE)): (
        (url.contains(Utils.CATEGORY))? url.substring(url.lastIndexOf(Utils.CATEGORY)): (
            (url.contains(Utils.TOPIC))? url.substring(url.lastIndexOf(Utils.TOPIC)): (
                (url.contains(Utils.FORUM) && ((url.lastIndexOf(Utils.FORUM)+5) < url.length()))? url.substring(url.lastIndexOf(Utils.FORUM)): url) ));
    if(url.indexOf(portalName) >= 0) return ;

    if(url.equals(old)) {
      if(getForumIdOfSpace() != null) url = getForumIdOfSpace();
      else return;
    }

    calculateRenderComponent(url, context);
    context.addUIComponentToUpdateByAjax(this);
  }

  private String getForumIdOfSpace() {

    try {
      PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
      PortletPreferences pref = pcontext.getRequest().getPreferences();
      if(pref.getValue("SPACE_URL", null) != null) {
        String url = pref.getValue("SPACE_URL", null);
        SpaceService sService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
        Space space = sService.getSpaceByUrl(url) ;
        spaceGroupId = space.getGroupId();
        String forumId = Utils.FORUM_SPACE_ID_PREFIX + space.getId();
        return forumId ;
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  public void updateIsRendered(String selected) throws Exception {
    if(selected.equals(ForumUtils.CATEGORIES)) {
      isCategoryRendered = true ;
      isForumRendered = false ;
      isTagRendered = false ;
      isSearchRendered = false ;
    } else if(selected.equals(ForumUtils.FORUM)) {
      isForumRendered = true ;
      isCategoryRendered = false ;
      isTagRendered = false ;
      isSearchRendered = false ;
    } else if(selected.equals(ForumUtils.TAG)) {
      isTagRendered = true ;
      isForumRendered = false ;
      isCategoryRendered = false ;
      isSearchRendered = false ;
    } else {
      isTagRendered = false ;
      isForumRendered = false ;
      isCategoryRendered = false ;
      isSearchRendered = true ;
    }
    if(!isShowForumActionBar){
      if(!isCategoryRendered || isSearchRendered){
        getChild(UIForumActionBar.class).setRendered(false) ;
      }
    }
    setRenderForumLink();
    getChild(UIForumContainer.class).setRendered(isForumRendered) ;
    getChild(UITopicsTag.class).setRendered(isTagRendered) ;
    getChild(UISearchForm.class).setRendered(isSearchRendered) ;
    if(!isForumRendered) {
      try {
        this.setRenderQuickReply();
      } catch (Exception e) {
      }
    }
  }

  public void setRenderForumLink() throws Exception {
    if(userProfile == null) updateUserProfileInfo();
    if(isShowForumJump){
      isJumpRendered = this.userProfile.getIsShowForumJump() ;
    } else {
      isJumpRendered = false;
    }
    UICategoryContainer categoryContainer = getChild(UICategoryContainer.class).setRendered(isCategoryRendered) ;
    categoryContainer.setIsRenderJump(isJumpRendered);
    if(!isCategoryRendered) {
      getChild(UIForumLinks.class).setRendered(isJumpRendered) ;
    }
  }

  public void setRenderQuickReply() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    ActionResponse actionRes = (ActionResponse)pcontext.getResponse();
    ForumParameter param = new ForumParameter() ;
    param.setRenderQuickReply(false);
    param.setRenderPoll(false);
    param.setRenderModerator(false);
    param.setRenderRule(false);
    actionRes.setEvent(new QName("QuickReplyEvent"), param) ;
    actionRes.setEvent(new QName("ForumPollEvent"), param) ;
    actionRes.setEvent(new QName("ForumModerateEvent"), param) ;
    actionRes.setEvent(new QName("ForumRuleEvent"), param) ;
  }

  public void loadPreferences() throws Exception {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
    invisibleCategories.clear();
    invisibleForums.clear();
    try {
      isShowForumActionBar = Boolean.parseBoolean(portletPref.getValue("showForumActionBar", ""));
      dayForumNewPost = Integer.parseInt(portletPref.getValue("forumNewPost", ""));
      useAjax = Boolean.parseBoolean(portletPref.getValue("useAjax", ""));
      enableIPLogging = Boolean.parseBoolean(portletPref.getValue("enableIPLogging", ""));
      enableBanIP = Boolean.parseBoolean(portletPref.getValue("enableIPFiltering", ""));
      isShowForumJump = Boolean.parseBoolean(portletPref.getValue("isShowForumJump", ""));
      isShowPoll = Boolean.parseBoolean(portletPref.getValue("isShowPoll", ""));
      isShowModerators = Boolean.parseBoolean(portletPref.getValue("isShowModerators", ""));
      isShowRules = Boolean.parseBoolean(portletPref.getValue("isShowRules", ""));
      isShowQuickReply = Boolean.parseBoolean(portletPref.getValue("isShowQuickReply", ""));
      isShowStatistics = Boolean.parseBoolean(portletPref.getValue("isShowStatistics", ""));
      isShowIconsLegend = Boolean.parseBoolean(portletPref.getValue("isShowIconsLegend", ""));
      invisibleCategories.addAll(getListInValus(portletPref.getValue("invisibleCategories", ""))) ;
      invisibleForums.addAll(getListInValus(portletPref.getValue("invisibleForums", ""))) ;
    } catch (Exception e) {
      log.error("Fail to load preference: " + e.getCause());
    }
    if(invisibleCategories.size() == 1 && invisibleCategories.get(0).equals(" ")) invisibleCategories.clear();
  }

  private List<String> getListInValus(String value) throws Exception {
    List<String>list = new ArrayList<String>();
    if(!ForumUtils.isEmpty(value)) {
      list.addAll(Arrays.asList(ForumUtils.addStringToString(value, value)));
    }
    return list;
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
    return isShowForumActionBar;
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

  public boolean isUseAjax(){
    return useAjax;
  }

  public int getDayForumNewPost(){
    return dayForumNewPost;
  }

  public String getSpaceGroupId() {
    return spaceGroupId;
  }

  public void renderPopupMessages() throws Exception {
    UIPopupMessages popupMess = getUIPopupMessages();
    if(popupMess == null)	return ;
    WebuiRequestContext	context =	RequestContext.getCurrentInstance() ;
    popupMess.processRender(context);
  }

  public void cancelAction() throws Exception {
    WebuiRequestContext context = RequestContext.getCurrentInstance() ;
    UIPopupAction popupAction = getChild(UIPopupAction.class) ;
    popupAction.deActivate() ;
    context.addUIComponentToUpdateByAjax(popupAction) ;
  }

  public UserProfile getUserProfile() throws Exception {
    if(this.userProfile == null) updateUserProfileInfo() ;
    return this.userProfile ;
  }

  public void updateWatching() throws Exception {
    listWatches = forumService.getWatchByUser(userProfile.getUserId());
  }

  public List<Watch> getWatchingByCurrentUser() throws Exception {
    if(listWatches == null) updateWatching();
    return listWatches;
  }

  public void updateAccessTopic(String topicId) throws Exception {
    String userId = userProfile.getUserId() ;
    if(userId != null && userId.length() > 0) {
      try{
        forumService.updateTopicAccess(userId, topicId);
      } catch (Exception e) {}
    }
    userProfile.setLastTimeAccessTopic(topicId, ForumUtils.getInstanceTempCalendar().getTimeInMillis());
  }

  public void updateAccessForum(String forumId) throws Exception {
    String userId = userProfile.getUserId() ;
    if(userId != null && userId.length() > 0) {
      try{
        forumService.updateForumAccess(userId, forumId);
      } catch (Exception e) {}
    }
    userProfile.setLastTimeAccessForum(forumId, ForumUtils.getInstanceTempCalendar().getTimeInMillis());
  }

  public void updateUserProfileInfo() throws Exception {
    String userId = "" ;
    try {
      userId = UserHelper.getCurrentUser() ;
    } catch (Exception e) {
      log.error("user is unknown: " + e.getCause());
    }
    try{
      if(enableBanIP) {
        userProfile = forumService.getDefaultUserProfile(userId, org.exoplatform.ks.common.Utils.getRemoteIP()) ;
      }else {
        userProfile = forumService.getDefaultUserProfile(userId, null) ;
      }
      if(!ForumUtils.isEmpty(userId))
        userProfile.setEmail(UserHelper.getUserByUserId(userId).getEmail());
      if(userProfile.getIsBanned())userProfile.setUserRole((long)3);
    }catch (Exception e) {}
  }

  public String getPortletLink() {
  	if(ForumUtils.isEmpty(linkUserInfo))
			try {
				linkUserInfo = this.event("ViewPublicUserInfo", "userName");
			} catch (Exception e) {
				log.debug("Failed to set link to view info user.", e);
			}
  	return linkUserInfo;
  }
  
  private CommonContact getPersonalContact(String userId) throws Exception {
    CommonContact contact  = ForumSessionUtils.getPersonalContact(userId) ;
    if(contact == null) {
      contact = new CommonContact() ;
    }
    return contact ;
  }

  static public class ReLoadPortletEventActionListener extends EventListener<UIForumPortlet> {
    public void execute(Event<UIForumPortlet> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource() ;
      ForumParameter params = (ForumParameter) event.getRequestContext().getAttribute(PortletApplication.PORTLET_EVENT_VALUE);
      if(params.getTopicId() != null){
        forumPortlet.userProfile.setLastTimeAccessTopic(params.getTopicId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
        UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class);
        topicDetail.setIdPostView("lastpost");
      }
      if(params.isRenderPoll()) {
        UITopicDetailContainer topicDetailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class);
        topicDetailContainer.getChild(UITopicDetail.class).setIsEditTopic(true);;
        topicDetailContainer.getChild(UITopicPoll.class).setEditPoll(true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }

  private boolean isArrayNotNull(String []strs){
    if(strs != null && strs.length > 0 && !strs[0].equals(" ")) return true;//private
    else  return false;
  }

  public boolean checkCanView(Category cate, Forum forum, Topic topic) throws Exception {
    if(userProfile == null) updateUserProfileInfo();
    String userId = userProfile.getUserId();
    if(userProfile.getUserRole() == 0) return true;
    List<String> userBound = UserHelper.getAllGroupAndMembershipOfUser(userId);
    String[] viewer = cate.getUserPrivate();
    if(isArrayNotNull(viewer)) {
      if(!Utils.hasPermission(Arrays.asList(viewer), userBound)) return false;
    }
    if(forum != null){
      if(isArrayNotNull(forum.getModerators())) {
        if(Utils.hasPermission(Arrays.asList(forum.getModerators()), userBound))
          return true;
      } else if(forum.getIsClosed()) return false;
    }
    if(topic != null) {
      List<String> list = new ArrayList<String>();
      list = ForumUtils.addArrayToList(list, topic.getCanView());
      list = ForumUtils.addArrayToList(list, forum.getViewer());
      list = ForumUtils.addArrayToList(list, cate.getViewer());

      //			if (topic.getOwner() != null)
      //			  list = ForumUtils.addArrayToList(list, new String[] {topic.getOwner()});

      if(!list.isEmpty() && topic.getOwner() != null)	list.add(topic.getOwner());
      if(topic.getIsClosed() || !topic.getIsActive() || !topic.getIsActiveByForum() || !topic.getIsApproved() ||
          topic.getIsWaiting() || (!list.isEmpty() && !Utils.hasPermission(list, userBound))) return false;
    }
    return true;
  }

  public void calculateRenderComponent(String path, WebuiRequestContext context) throws Exception {
    UIApplication uiApp = (UIApplication)this;
    ResourceBundle res = context.getApplicationResourceBundle() ;
    if(path.equals(Utils.FORUM_SERVICE)) {
      this.updateIsRendered(ForumUtils.CATEGORIES);
      UICategoryContainer categoryContainer = this.getChild(UICategoryContainer.class) ;
      categoryContainer.updateIsRender(true) ;
      categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
    }else	if(path.lastIndexOf(Utils.TOPIC) >= 0) {
      boolean isReply = false, isQuote = false;
      if(path.indexOf("/true") > 0){
        isQuote = true;
        path = path.replaceFirst("/true", "");
      } else	if(path.indexOf("/false") > 0){
        isReply = true;
        path = path.replaceFirst("/false", "");
      }
      String []id = path.split("/") ;
      String postId = "top";
      int page = 0;
      if(path.indexOf(Utils.POST) > 0) {
        postId = id[id.length-1];
        path = path.substring(0, path.lastIndexOf("/")) ;
        id = new String[]{path};
      } else if(id.length > 1) {
        try {
          page = Integer.parseInt(id[id.length-1]);
        } catch (Exception e) {}
        if(page > 0){
          path = path.replace("/"+id[id.length-1], "");
          id = new String[]{path};
        } else page = 0;
      }
      try{
        Topic topic ;
        if(id.length > 1) {
          topic = this.forumService.getTopicByPath(path, false) ;
        } else {
          topic = (Topic)this.forumService.getObjectNameById(path, Utils.TOPIC);
          path = topic.getPath();
          path = path.substring(path.indexOf(Utils.CATEGORY));
          id = path.split("/") ;
        }
        if(topic != null) {
          Category category = this.forumService.getCategory(id[0]);
          Forum forum = this.forumService.getForum(id[0], id[1]) ;
          if(this.checkCanView(category, forum, topic)){
            this.updateIsRendered(ForumUtils.FORUM);
            UIForumContainer uiForumContainer = this.getChild(UIForumContainer.class) ;
            UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
            uiForumContainer.setIsRenderChild(false) ;
            uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
            UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
            uiTopicDetail.setIsEditTopic(true) ;
            uiTopicDetail.setUpdateForum(forum) ;
            uiTopicDetail.setTopicFromCate(id[0], id[1] , topic, page) ;
            uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1] , topic.getId()) ;
            this.getChild(UIForumLinks.class).setValueOption((id[0] + "/" + id[1] + " "));
            uiTopicDetail.setIdPostView(postId) ;
            if(isReply || isQuote){
              if(uiTopicDetail.getCanPost()) {
                uiTopicDetail.setIdPostView("top") ;
                try {
                  UIPopupAction popupAction = this.getChild(UIPopupAction.class) ;
                  UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
                  UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
                  boolean isMod = ForumServiceUtils.hasPermission(forum.getModerators(), this.userProfile.getUserId());
                  postForm.setPostIds(id[0], id[1], topic.getId(), topic) ;
                  postForm.setMod(isMod);
                  if(isQuote){
                    uiTopicDetail.setLastPostId(postId) ;
                    Post post = this.forumService.getPost(id[0], id[1], topic.getId(), postId);
                    if(post != null) {
                      postForm.updatePost(postId, true, false, post) ;
                      popupContainer.setId("UIQuoteContainer") ;
                    } else {
                      uiApp.addMessage(new ApplicationMessage("UIBreadcumbs.msg.post-no-longer-exist", null, ApplicationMessage.WARNING)) ;
                      context.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                      uiTopicDetail.setIdPostView("normal") ;
                    }
                  } else {
                    postForm.updatePost("", false, false, null) ;
                    popupContainer.setId("UIAddPostContainer") ;
                  }
                  popupAction.activate(popupContainer, 900, 500) ;
                  context.addUIComponentToUpdateByAjax(popupAction) ;
                } catch (Exception e) {
                  log.error(e);
                }
              } else {
                uiApp.addMessage(new ApplicationMessage("UIPostForm.msg.no-permission",null, ApplicationMessage.WARNING)) ;
              }
            }
            if (!UserHelper.isAnonim()) {
              this.forumService.updateTopicAccess(UserHelper.getCurrentUser(),  topic.getId()) ;
              this.getUserProfile().setLastTimeAccessTopic(topic.getId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
            }
          } else {
            uiApp.addMessage(new ApplicationMessage("UIBreadcumbs.msg.do-not-permission",  new String[]{topic.getTopicName(), res.getString("UIForumPortlet.label.topic").toLowerCase()}, ApplicationMessage.WARNING)) ;
            this.updateIsRendered(ForumUtils.CATEGORIES);
            UICategoryContainer categoryContainer = this.getChild(UICategoryContainer.class) ;
            categoryContainer.updateIsRender(true) ;
            categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
            path = Utils.FORUM_SERVICE;
          }
        }
      }catch(Exception e) {
        e.printStackTrace();
        uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
        this.updateIsRendered(ForumUtils.CATEGORIES);
        UICategoryContainer categoryContainer = this.getChild(UICategoryContainer.class) ;
        categoryContainer.updateIsRender(true) ;
        categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
        path = Utils.FORUM_SERVICE;
      }
    }else	if((path.lastIndexOf(Utils.FORUM) == 0 && path.lastIndexOf(Utils.CATEGORY) < 0) || (path.lastIndexOf(Utils.FORUM) > 0)) {
      UICategoryContainer categoryContainer = this.getChild(UICategoryContainer.class) ;
      try {
        Forum forum;
        String cateId = null;
        int page = 0;
        if(path.indexOf("/") > 0){
          String []arr = path.split("/");
          try {
            page = Integer.parseInt(arr[arr.length-1]);
          } catch (Exception e) {}
          if(arr[0].indexOf(Utils.CATEGORY) == 0){
            cateId = arr[0];
            forum = this.forumService.getForum(cateId, arr[1]);
          } else {
            forum = (Forum)this.forumService.getObjectNameById(arr[0], Utils.FORUM);
          }
        } else {
          forum = (Forum)this.forumService.getObjectNameById(path, Utils.FORUM);
        }
        path = forum.getPath();
        if(cateId == null){
          cateId = path.substring(path.indexOf(Utils.CATEGORY), path.lastIndexOf(Utils.FORUM)-1);
        }
        path = path.substring(path.indexOf(Utils.CATEGORY));
        Category category = this.forumService.getCategory(cateId);
        if(this.checkCanView(category, forum, null)){
          this.updateIsRendered(ForumUtils.FORUM);
          UIForumContainer forumContainer = this.findFirstComponentOfType(UIForumContainer.class);
          forumContainer.setIsRenderChild(true) ;
          forumContainer.getChild(UIForumDescription.class).setForum(forum) ;
          forumContainer.getChild(UITopicContainer.class).setUpdateForum(cateId, forum, page) ;
        } else {
          uiApp.addMessage(new ApplicationMessage("UIBreadcumbs.msg.do-not-permission", new String[]{forum.getForumName(), res.getString("UIForumPortlet.label.forum").toLowerCase()}, ApplicationMessage.WARNING)) ;
          this.updateIsRendered(ForumUtils.CATEGORIES);
          categoryContainer.updateIsRender(true) ;
          categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
          path = Utils.FORUM_SERVICE;
        }
      }catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", new String[]{res.getString("UIForumPortlet.label.forum")}, ApplicationMessage.WARNING)) ;
        this.updateIsRendered(ForumUtils.CATEGORIES);
        categoryContainer.updateIsRender(true) ;
        categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
        path = Utils.FORUM_SERVICE;
      }
    }else	if(path.indexOf(Utils.CATEGORY) >= 0 && path.indexOf("/") < 0) {
      UICategoryContainer categoryContainer = this.getChild(UICategoryContainer.class) ;
      try {
        Category category = this.forumService.getCategory(path);
        if(this.checkCanView(category, null, null)){
          categoryContainer.getChild(UICategory.class).updateByLink(category) ;
          categoryContainer.updateIsRender(false) ;
          this.updateIsRendered(ForumUtils.CATEGORIES);
        } else {
          uiApp.addMessage(new ApplicationMessage("UIBreadcumbs.msg.do-not-permission", new String[]{category.getCategoryName(), res.getString("UIForumPortlet.label.category").toLowerCase()}, ApplicationMessage.WARNING)) ;
          this.updateIsRendered(ForumUtils.CATEGORIES);
          categoryContainer.updateIsRender(true) ;
          categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
          path = Utils.FORUM_SERVICE;
        }
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
        this.updateIsRendered(ForumUtils.CATEGORIES);
        categoryContainer.updateIsRender(true) ;
        categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
        path = Utils.FORUM_SERVICE;
      }
    }else {
      uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
      this.updateIsRendered(ForumUtils.CATEGORIES);
      UICategoryContainer categoryContainer = this.getChild(UICategoryContainer.class) ;
      categoryContainer.updateIsRender(true) ;
      categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
      path = Utils.FORUM_SERVICE;
    }
    UIBreadcumbs uiBreadcumbs = getChild(UIBreadcumbs.class);
    uiBreadcumbs.setUpdataPath(path);
    getChild(UIForumLinks.class).setValueOption(path);
  }

  static public class OpenLinkActionListener extends EventListener<UIForumPortlet> {
    public void execute(Event<UIForumPortlet> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource() ;
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(ForumUtils.isEmpty(path)){
        ForumParameter params = (ForumParameter) event.getRequestContext().getAttribute(PortletApplication.PORTLET_EVENT_VALUE);
        path = params.getPath();
      }
      if(ForumUtils.isEmpty(path)) return;
      forumPortlet.calculateRenderComponent(path, event.getRequestContext());
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }

  static public class ViewPublicUserInfoActionListener extends EventListener<UIForumPortlet> {
    public void execute(Event<UIForumPortlet> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource() ;
      String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIViewUserProfile viewUserProfile = popupAction.createUIComponent(UIViewUserProfile.class, null, null) ;
      try{
        UserProfile selectProfile = forumPortlet.forumService.getUserInformations(forumPortlet.forumService.getQuickProfile(userId.trim())) ;
        viewUserProfile.setUserProfile(selectProfile) ;
      }catch(Exception e) {
        log.error("Fail to set user profile: \n",e);
      }
      viewUserProfile.setUserProfileLogin(forumPortlet.userProfile) ;
      CommonContact contact = forumPortlet.getPersonalContact(userId.trim());
      viewUserProfile.setContact(contact) ;
      popupAction.activate(viewUserProfile, 670, 400, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

  static public class PrivateMessageActionListener extends EventListener<UIForumPortlet> {
    public void execute(Event<UIForumPortlet> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource() ;;
      if(forumPortlet.userProfile.getIsBanned()){
        String[] args = new String[] { } ;
        throw new MessageException(new ApplicationMessage("UITopicDetail.msg.userIsBannedCanNotSendMail", args, ApplicationMessage.WARNING)) ;
      }
      String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      int t = userId.indexOf(Utils.DELETED);
      if(t > 0) {
        String[] args = new String[] { userId.substring(0, t)} ;
        throw new MessageException(new ApplicationMessage("UITopicDetail.msg.userIsDeleted", args, ApplicationMessage.WARNING)) ;
      }
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIPrivateMessageForm messageForm = popupContainer.addChild(UIPrivateMessageForm.class, null, null) ;
      messageForm.setFullMessage(false);
      messageForm.setUserProfile(forumPortlet.userProfile);
      messageForm.setSendtoField(userId) ;
      popupContainer.setId("PrivateMessageForm") ;
      popupAction.activate(popupContainer, 650, 480) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

  static public class ViewPostedByUserActionListener extends EventListener<UIForumPortlet> {
    public void execute(Event<UIForumPortlet> event) throws Exception {
      String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIForumPortlet forumPortlet = event.getSource() ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIViewPostedByUser viewPostedByUser = popupContainer.addChild(UIViewPostedByUser.class, null, null) ;
      viewPostedByUser.setUserProfile(userId) ;
      popupContainer.setId("ViewPostedByUser") ;
      popupAction.activate(popupContainer, 760, 370) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

  static public class ViewThreadByUserActionListener extends EventListener<UIForumPortlet> {
    public void execute(Event<UIForumPortlet> event) throws Exception {
      String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIForumPortlet forumPortlet = event.getSource() ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIViewTopicCreatedByUser topicCreatedByUser = popupContainer.addChild(UIViewTopicCreatedByUser.class, null, null) ;
      topicCreatedByUser.setUserId(userId) ;
      popupContainer.setId("ViewTopicCreatedByUser") ;
      popupAction.activate(popupContainer, 760, 450) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}