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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.UIForumQuickReplyPortlet;
import org.exoplatform.forum.rendering.RenderHelper;
import org.exoplatform.forum.rendering.RenderingException;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * October 2, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIViewPost.gtmpl",
    events = {
      @EventConfig(listeners = UIViewPost.CloseActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIViewPost.ApproveActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIViewPost.DeletePostActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIViewPost.OpenTopicLinkActionListener.class),
      @EventConfig(listeners = UIViewPost.DownloadAttachActionListener.class, phase = Phase.DECODE)
    }
)
public class UIViewPost extends UIForm implements UIPopupComponent {
  private Post         post;

  private boolean      isViewUserInfo = true;

  private ForumService forumService;

  private UserProfile  userProfile;

  RenderHelper         renderHelper   = new RenderHelper();

  private static Log   log            = ExoLogger.getLogger(UIViewPost.class);

  public UIViewPost() {
    forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
  }

  public void setActionForm(String[] actions) {
    this.setActions(actions);
  }

  protected UserProfile getUserProfile() {
    userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
    return userProfile;
  }

  public String renderPost(Post post) throws RenderingException {
    return renderHelper.renderPost(post);
  }

  public String getImageUrl(String imagePath) throws Exception {
    String url = ForumUtils.EMPTY_STR;
    try {
      url = CommonUtils.getImageUrl(imagePath);
    } catch (Exception e) {
      log.warn(imagePath + " is not exist: " + e.getMessage());
    }
    return url;
  }

  public void setPostView(Post post) throws Exception {
    this.post = post;
  }

  protected Post getPostView() throws Exception {
    return post;
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void setViewUserInfo(boolean isView) {
    this.isViewUserInfo = isView;
  }

  public boolean getIsViewUserInfo() {
    return this.isViewUserInfo;
  }

  static public class DownloadAttachActionListener extends EventListener<UIViewPost> {
    public void execute(Event<UIViewPost> event) throws Exception {
      UIViewPost viewPost = event.getSource();
      String attId = event.getRequestContext().getRequestParameter(OBJECTID);
      ForumAttachment attachment = ForumSessionUtils.findAttachmentById(viewPost.getPostView().getAttachments(), attId);
      DownloadService dservice = viewPost.getApplicationComponent(DownloadService.class);
      InputStreamDownloadResource dresource = new InputStreamDownloadResource(attachment.getInputStream(), "application/octet-stream");
      dresource.setDownloadName(attachment.getName());
      String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
      event.getRequestContext().getJavascriptManager()
      .addJavascript("window.open('" + downloadLink.replaceAll("&amp;", "&") + 
                     "', '_self', '', false); setTimeout( function() { eXo.ks.MaskLayerControl.loadImageAgain('" +
                     viewPost.getId() + "');}, 50);");
      event.getRequestContext().addUIComponentToUpdateByAjax(viewPost);
    }
  }

  static public class ApproveActionListener extends EventListener<UIViewPost> {
    public void execute(Event<UIViewPost> event) throws Exception {
      UIViewPost uiForm = event.getSource();
      Post post = uiForm.post;
      post.setIsApproved(true);
      post.setIsHidden(false);
      post.setIsWaiting(false);
      List<Post> posts = new ArrayList<Post>();
      posts.add(post);
      try {
        uiForm.forumService.modifyPost(posts, Utils.APPROVE);
        uiForm.forumService.modifyPost(posts, Utils.HIDDEN);
        uiForm.forumService.modifyPost(posts, Utils.WAITING);
      } catch (Exception e) {
        log.debug("\nModify post fail: ", e);
      }
      UIViewTopic.closePopup(event.getRequestContext(), uiForm);
    }
  }

  static public class DeletePostActionListener extends EventListener<UIViewPost> {
    public void execute(Event<UIViewPost> event) throws Exception {
      UIViewPost uiForm = event.getSource();
      Post post = uiForm.post;
      try {
        String[] path = post.getPath().split(ForumUtils.SLASH);
        int l = path.length;
        uiForm.forumService.removePost(path[l - 4], path[l - 3], path[l - 2], post.getId());
      } catch (Exception e) {
        log.debug("Removing " + post.getId() + " post fail: ", e);
      }
      UIViewTopic.closePopup(event.getRequestContext(), uiForm);
    }
  }

  static public class OpenTopicLinkActionListener extends EventListener<UIViewPost> {
    public void execute(Event<UIViewPost> event) throws Exception {
      UIViewPost uiForm = event.getSource();
      Post post = uiForm.post;
      if (post == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));
        return;
      }
      String path = post.getPath();
      path = path.substring(path.lastIndexOf(Utils.TOPIC));
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.calculateRenderComponent(path, event.getRequestContext());
      // close popup
      uiForm.closeAction(event.getRequestContext());
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  private void closeAction(WebuiRequestContext context) throws Exception {
    UIPopupContainer popupContainer = getAncestorOfType(UIPopupContainer.class);
    if (popupContainer != null) {
      UIPopupAction popupAction;
      if (((UIComponent) getParent()).getId().equals(popupContainer.getId())) {
        popupAction = popupContainer.getAncestorOfType(UIPopupAction.class);
      } else {
        popupAction = popupContainer.getChild(UIPopupAction.class);
      }
      popupAction.deActivate();
      context.addUIComponentToUpdateByAjax(popupAction);
    } else {
      try {
        UIForumPortlet forumPortlet = getAncestorOfType(UIForumPortlet.class);
        forumPortlet.cancelAction();
      } catch (Exception e) {
        UIForumQuickReplyPortlet forumPortlet = getAncestorOfType(UIForumQuickReplyPortlet.class);
        forumPortlet.cancelAction();
      }
    }
  }

  static public class CloseActionListener extends EventListener<UIViewPost> {
    public void execute(Event<UIViewPost> event) throws Exception {
      UIViewPost uiForm = event.getSource();
      uiForm.closeAction(event.getRequestContext());
    }
  }

}
