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
import java.util.List;

import javax.portlet.ActionResponse;
import javax.portlet.PortletSession;
import javax.xml.namespace.QName;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.portal.account.UIAccountSetting;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    template = "app:/templates/forum/webui/UIBreadcumbs.gtmpl" ,
    events = {
        @EventConfig(listeners = UIBreadcumbs.ChangePathActionListener.class),
        @EventConfig(listeners = UIBreadcumbs.AccountSettingsActionListener.class),
        @EventConfig(listeners = UIBreadcumbs.RssActionListener.class)
    }
)
public class UIBreadcumbs extends UIContainer {

  private static Log         log           = ExoLogger.getExoLogger(UIBreadcumbs.class);

  protected boolean            useAjax       = true;

  private ForumService       forumService;

  private List<String>       breadcumbs_   = new ArrayList<String>();

  private List<String>       path_         = new ArrayList<String>();

  private String             QUICK_SEARCH  = "QuickSearchForm";

  public static final String FORUM_SERVICE = Utils.FORUM_SERVICE;

  private boolean            isLink        = false;

  private boolean            isOpen        = true;

  private String             tooltipLink   = Utils.FORUM_SERVICE;
  
  public UIBreadcumbs() throws Exception {
    forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    breadcumbs_.add(ForumUtils.FIELD_EXOFORUM_LABEL);
    path_.add(FORUM_SERVICE);
    addChild(UIQuickSearchForm.class, null, QUICK_SEARCH);
  }

  protected void setIsUseAjax() throws Exception {
    UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
    useAjax = forumPortlet.isUseAjax();
  }
  
  protected UserProfile getUserProfile() {
    UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
    return forumPortlet.getUserProfile();
  }

  public void setUpdataPath(String path) throws Exception {
    isLink = false;
    setRenderForumLink(path);
    String tempPath = ForumUtils.EMPTY_STR;
    String frspId = getAncestorOfType(UIForumPortlet.class).getForumIdOfSpace();
    if(!ForumUtils.isEmpty(frspId)) {
      path_.clear();
      breadcumbs_.clear();
      String temp[] = path.split(ForumUtils.SLASH);
      for (String string : temp) {
        if (!ForumUtils.isEmpty(tempPath))
          tempPath = tempPath + ForumUtils.SLASH + string;
        else
          tempPath = string;
        Object obj = forumService.getObjectNameByPath(tempPath);
        if (obj instanceof Forum) {
          addBreadcumbs(tempPath, ((Forum) obj).getForumName(), ForumUtils.FORUM);
        } else if (obj instanceof Topic) {
          addBreadcumbs(tempPath, ((Topic) obj).getTopicName(), ForumUtils.TOPIC);
        } else if (obj instanceof Tag) {
          Forum forum = (Forum) forumService.getObjectNameById(frspId, Utils.FORUM);
          addBreadcumbs(new StringBuilder(forum.getCategoryId()).append(ForumUtils.SLASH).append(forum.getId()).toString(), 
                                                forum.getForumName(), ForumUtils.FORUM);
          addBreadcumbs(tempPath, ((Tag) obj).getName(), ForumUtils.TAG);
        }
      }
    } else if (!ForumUtils.isEmpty(path) && !path.equals(FORUM_SERVICE)) {
      String temp[] = path.split(ForumUtils.SLASH);
      if (path.indexOf(ForumUtils.FIELD_EXOFORUM_LABEL) >= 0) {
        if (!ForumUtils.FIELD_EXOFORUM_LABEL.equals(path)) {
          clearBreadcumbs();
        }
        if (!breadcumbs_.contains(ForumUtils.FIELD_SEARCHFORUM_LABEL)) {
          addBreadcumbs(ForumUtils.SLASH + ForumUtils.FIELD_EXOFORUM_LABEL, ForumUtils.FIELD_SEARCHFORUM_LABEL, "");
        }
      } else {
        clearBreadcumbs();
        int i = 0;
        try {
          for (String string : temp) {
            if (!ForumUtils.isEmpty(tempPath))
              tempPath = tempPath + ForumUtils.SLASH + string;
            else
              tempPath = string;
            Object obj = forumService.getObjectNameByPath(tempPath);
            if (obj == null) {
              if (i == 0) {
                isLink = true;
              }
              break;
            }
            if (obj instanceof Category) {
              tempPath = string;
              addBreadcumbs(tempPath, ((Category) obj).getCategoryName(), ForumUtils.CATEGORY);
            } else if (obj instanceof Forum) {
              addBreadcumbs(tempPath, ((Forum) obj).getForumName(), ForumUtils.FORUM);
            } else if (obj instanceof Topic) {
              addBreadcumbs(tempPath, ((Topic) obj).getTopicName(), ForumUtils.TOPIC);
            } else if (obj instanceof Tag) {
              addBreadcumbs(tempPath, ((Tag) obj).getName(), ForumUtils.TAG);
            }
            ++i;
          }
        } catch (Exception e) {
          log.warn(String.format("Failed to find object with path %s", path), e);
        }
      }
    } else {
      clearBreadcumbs();
    }
  }
  
  private void clearBreadcumbs() {
    path_.clear();
    breadcumbs_.clear();
    addBreadcumbs(FORUM_SERVICE, ForumUtils.FIELD_EXOFORUM_LABEL, FORUM_SERVICE);
  }

  private void addBreadcumbs(String path, String breadcumb, String tooltipLink) {
    path_.add(path);
    breadcumbs_.add(breadcumb);
    this.tooltipLink = tooltipLink;
  }

  private void setRenderForumLink(String path) throws Exception {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletSession portletSession = pcontext.getRequest().getPortletSession();
    ActionResponse actionRes = null;
    if (pcontext.getResponse() instanceof ActionResponse) {
      actionRes = (ActionResponse) pcontext.getResponse();
    }
    ForumParameter param = new ForumParameter();
    if (getUserProfile().getIsShowForumJump() && !FORUM_SERVICE.equals(path)) {
      if (path.indexOf(Utils.TOPIC) > 0) {
        path = path.substring(0, path.lastIndexOf(ForumUtils.SLASH));
      }
      param.setRenderForumLink(true);
      param.setPath(path);
    } else {
      param.setRenderForumLink(false);
    }
    if (actionRes != null) {
      actionRes.setEvent(new QName("ForumLinkEvent"), param);
    } else {
      portletSession.setAttribute(UIForumPortlet.FORUM_LINK_EVENT_PARAMS, param, PortletSession.APPLICATION_SCOPE);
    }
  }

  public boolean isOpen() {
    return isOpen;
  }

  public void setOpen(boolean isOpen) {
    this.isOpen = isOpen;
  }

  protected String getToolTip() {
    return tooltipLink;
  }

  protected boolean isLink() {
    return this.isLink;
  }

  public String getLastPath() {
    if (this.path_.size() > 0) {
      String str = path_.get(this.path_.size() - 1);
      return ((ForumUtils.SLASH + ForumUtils.FIELD_EXOFORUM_LABEL).equals(str)) ? 
                                                            Utils.FORUM_SERVICE : str;
    } else{
      return Utils.FORUM_SERVICE;
    }
  }

  protected String getPath(int index) {
    return this.path_.get(index);
  }

  protected int getMaxPath() {
    return breadcumbs_.size();
  }

  protected List<String> getBreadcumbs() throws Exception {
    return new ArrayList<String>(breadcumbs_);
  }

  protected String getType(String id) {
    return (id.indexOf(Utils.FORUM_SERVICE) >= 0) ? Utils.FORUM_SERVICE : 
            ((id.indexOf(Utils.CATEGORY) >= 0) ? ForumUtils.CATEGORY : 
              ((id.indexOf(Utils.FORUM) >= 0) ? ForumUtils.FORUM : 
                ((id.indexOf(Utils.TOPIC) >= 0) ? ForumUtils.TOPIC : 
                  (ForumUtils.EMPTY_STR))));
  }

  protected boolean checkLinkPrivate(String id) throws Exception {
    if (id.indexOf(Utils.TOPIC) >= 0) {
      try {
        Topic topic = (Topic) this.forumService.getObjectNameById(id, Utils.TOPIC);
        if (topic != null) {
          if (topic.getIsClosed() || !topic.getIsActiveByForum() || !topic.getIsActive() || topic.getIsWaiting()
              || !(Utils.isEmpty(topic.getCanView()))) {
            return true;
          } else {
            return isForumPrivate(topic.getPath(), true);
          }
        }
      } catch (Exception e) {
        log.warn("\nThe " + id + " must exist");
      }
    } else if (id.indexOf(Utils.CATEGORY) == 0) {
      return isCategoryPrivate(id, false);
    } else if (id.indexOf(Utils.FORUM) == 0) {
      return isForumPrivate(id, false);
    }
    return false;
  }

  private boolean isCategoryPrivate(String id, boolean isForTopic) throws Exception {
    Category cate = (Category) this.forumService.getCategory(id);
    if(cate != null) {
      return !Utils.isEmpty(cate.getUserPrivate()) || (isForTopic && !Utils.isEmpty(cate.getViewer()));
    } else {
      return true;
    }
  }
  
  private boolean isForumPrivate(String id, boolean isForTopic) throws Exception {
    Forum forum = null;
    if (id.indexOf("/") >= 0) {
      String[] arr = id.split("/");
      forum = forumService.getForum(arr[arr.length - 3], arr[arr.length - 2]);
    } else {
      forum = (Forum) this.forumService.getObjectNameById(id, Utils.FORUM);
    }
    if (forum != null) {
      if (forum.getIsClosed() || (isForTopic && !Utils.isEmpty(forum.getViewer()))) {
        return true;
      } else {
        return isCategoryPrivate(forum.getCategoryId(), isForTopic);
      }
    }
    return true;
  }

  static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs breadcums = event.getSource();
      if (breadcums.isOpen()) {
        String path = event.getRequestContext().getRequestParameter(OBJECTID);
        UIForumPortlet forumPortlet = breadcums.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.calculateRenderComponent(path, event.getRequestContext());
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
      } else {
        breadcums.isOpen = true;
      }
    }
  }

  static public class RssActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class);
      categoryContainer.updateIsRender(true);
      forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
      event.getSource().setUpdataPath(FORUM_SERVICE);
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class AccountSettingsActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIPortal uiPortal = Util.getUIPortal();
      UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);

      UIAccountSetting uiAccountForm = uiMaskWS.createUIComponent(UIAccountSetting.class, null, null);
      uiMaskWS.setUIComponent(uiAccountForm);
      uiMaskWS.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
    }
  }
}
