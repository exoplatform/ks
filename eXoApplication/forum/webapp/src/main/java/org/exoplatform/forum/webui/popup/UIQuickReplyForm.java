/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
import java.util.Date;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.portlet.ActionResponse;
import javax.xml.namespace.QName;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.forum.info.UIForumQuickReplyPortlet;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.ks.common.TransformHTML;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.WebUIUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Apr 15, 2009 - 4:34:11 AM  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIQuickReplyForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIQuickReplyForm.PreviewReplyActionListener.class),
      @EventConfig(listeners = UIQuickReplyForm.QuickReplyActionListener.class)
    }
)
public class UIQuickReplyForm extends UIForm {
  private String             categoryId;

  private String             forumId;

  private String             topicId                = ForumUtils.EMPTY_STR;

  private String             userName;

  private Topic              topic;

  private boolean            isModerator            = false;

  public static final String FIELD_MESSAGE_TEXTAREA = "Message";

  public UIQuickReplyForm() {
    addUIFormInput(new UIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA, FIELD_MESSAGE_TEXTAREA, null));
  }

  public void setInitForm(String categoryId, String forumId, String topicId, boolean isModerator) {
    this.categoryId = categoryId;
    this.forumId = forumId;
    this.topicId = topicId;
    this.isModerator = isModerator;
    this.userName = UserHelper.getCurrentUser();
    try {
      ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      this.topic = forumService.getTopic(categoryId, forumId, topicId, userName);
    } catch (Exception e) {
      topic = new Topic();
    }
  }

  static public class QuickReplyActionListener extends EventListener<UIQuickReplyForm> {

    public void execute(Event<UIQuickReplyForm> event) throws Exception {
      Log log = ExoLogger.getLogger(QuickReplyActionListener.class);
      UIQuickReplyForm quickReply = event.getSource();
      ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      UIFormTextAreaInput textAreaInput = quickReply.getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA);
      String message = textAreaInput.getValue();
      String checksms = message;
      if (checksms != null && checksms.trim().length() > 0) {
        boolean isOffend = false;
        boolean hasTopicMod = false;
        if (!quickReply.isModerator) {
          String[] censoredKeyword = ForumUtils.getCensoredKeyword(forumService);
          checksms = checksms.toLowerCase().trim();
          for (String string : censoredKeyword) {
            if (checksms.indexOf(string) >= 0) {
              isOffend = true;
              break;
            }
          }
          if (quickReply.topic != null)
            hasTopicMod = quickReply.topic.getIsModeratePost();
        }
        message = TransformHTML.enCodeHTMLContent(message);
        String remoteAddr = WebUIUtils.getRemoteIP();
        UserProfile userProfile = forumService.getDefaultUserProfile(quickReply.userName, remoteAddr);
        // set link
        String link = ForumUtils.createdForumLink(ForumUtils.TOPIC, quickReply.topicId, false);
        //
        Topic topic = quickReply.topic;
        Post post = new Post();
        post.setName(quickReply.getLabel("Re") + topic.getTopicName());
        post.setMessage(message);
        post.setOwner(quickReply.userName);
        post.setRemoteAddr(remoteAddr);
        post.setIcon(topic.getIcon());
        post.setIsWaiting(isOffend);
        post.setIsApproved(!hasTopicMod);
        post.setLink(link);
        try {
          forumService.savePost(quickReply.categoryId, quickReply.forumId, quickReply.topicId, post, true, ForumUtils.getDefaultMail());
          forumService.updateTopicAccess(quickReply.userName, topic.getId());
          if (userProfile.getIsAutoWatchTopicIPost()) {
            List<String> values = new ArrayList<String>();
            values.add(userProfile.getEmail());
            String path = quickReply.categoryId + ForumUtils.SLASH + quickReply.forumId + ForumUtils.SLASH + quickReply.topicId;
            forumService.addWatch(1, path, values, quickReply.userName);
          }
        } catch (PathNotFoundException e) {
          throw new MessageException(new ApplicationMessage("UIPostForm.msg.isParentDelete", null, ApplicationMessage.WARNING));
		} catch (NullPointerException ne) {          
        }
        textAreaInput.setValue(ForumUtils.EMPTY_STR);
        if (isOffend || hasTopicMod) {
          Object[] args = { ForumUtils.EMPTY_STR };
          if (isOffend)
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("MessagePost.msg.isOffend",
                                                                                           args,
                                                                                           ApplicationMessage.WARNING));
          else {
            event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("MessagePost.msg.isModerate",
                                                                                           null,
                                                                                           ApplicationMessage.WARNING));
          }
        } else {
          try {
            ActionResponse actionRes = event.getRequestContext().getResponse();
            ForumParameter param = new ForumParameter();
            param.setTopicId(topic.getId());
            actionRes.setEvent(new QName("ReLoadPortletEvent"), param);
          } catch (Exception e) {
            log.debug("\nSetting Topic fail: " + e.getMessage() + "\n" + e.getCause());
          }
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(quickReply);
      } else {
        String[] args = new String[] { quickReply.getLabel(FIELD_MESSAGE_TEXTAREA) };
        throw new MessageException(new ApplicationMessage("MessagePost.msg.message-empty", args));
      }
    }
  }

  static public class PreviewReplyActionListener extends EventListener<UIQuickReplyForm> {
    public void execute(Event<UIQuickReplyForm> event) throws Exception {
      UIQuickReplyForm quickReply = event.getSource();
      String message = quickReply.getUIStringInput(FIELD_MESSAGE_TEXTAREA).getValue();
      String checksms = (message);
      if (checksms != null && checksms.trim().length() > 3) {
        message = TransformHTML.enCodeHTMLContent(message);
        Topic topic = quickReply.topic;
        Post post = new Post();
        post.setName(quickReply.getLabel("Re") + topic.getTopicName());
        post.setMessage(message);
        post.setOwner(quickReply.userName);
        post.setRemoteAddr(ForumUtils.EMPTY_STR);
        post.setIcon(topic.getIcon());
        post.setIsApproved(false);
        post.setCreatedDate(new Date());
        UIForumQuickReplyPortlet quickReplyPortlet = quickReply.getAncestorOfType(UIForumQuickReplyPortlet.class);
        UIPopupAction popupAction = quickReplyPortlet.getChild(UIPopupAction.class).setRendered(true);
        UIViewPost viewPost = popupAction.activate(UIViewPost.class, 670);
        viewPost.setPostView(post);
        viewPost.setViewUserInfo(false);
        viewPost.setActionForm(new String[] { "Close" });
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } else {
        String[] args = new String[] { quickReply.getLabel(FIELD_MESSAGE_TEXTAREA) };
        throw new MessageException(new ApplicationMessage("MessagePost.msg.message-empty", args));
      }
    }
  }
}
