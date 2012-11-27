/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
 */
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.Post;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tuvd@exoplatform.com
 * Nov 21, 2012  
 */
@ComponentConfig(
   template = "app:/templates/forum/webui/UIAttachmentContainer.gtmpl",
   events = {
     @EventConfig(listeners = UIAttachmentContainer.DownloadAttachActionListener.class)
   }
)
public class UIAttachmentContainer extends UIContainer {
  public static final String    ID_PREFIX     = "Attachment";

  private List<ForumAttachment> attachments   = new ArrayList<ForumAttachment>();

  private boolean               hasAttachment = false;

  public UIAttachmentContainer() {
  }

  public UIAttachmentContainer initContainer(Post post) {
    super.setId(ID_PREFIX.concat(post.getId()));
    this.attachments = post.getAttachments();
    hasAttachment = (this.attachments != null && this.attachments.size() > 0);
    setRendered(hasAttachment);
    return this;
  }

  public boolean isHasAttachment() {
    return hasAttachment;
  }

  public void setHasAttachment(boolean hasAttachment) {
    this.hasAttachment = hasAttachment;
  }

  public void setAttachments(List<ForumAttachment> attachments) {
    this.attachments = attachments;
    this.hasAttachment = (this.attachments != null && this.attachments.size() > 0);
    setRendered(hasAttachment);
  }

  public List<ForumAttachment> getAttachments() {
    return this.attachments;
  }

  static public class DownloadAttachActionListener extends EventListener<UIAttachmentContainer> {
    @Override
    public void execute(Event<UIAttachmentContainer> event) throws Exception {
      UIAttachmentContainer attContainer = event.getSource();
      String attId = event.getRequestContext().getRequestParameter(OBJECTID);
      ForumAttachment attachment = ForumSessionUtils.findAttachmentById(attContainer.getAttachments(), attId);
      if(attachment != null) {
        DownloadService dservice = attContainer.getApplicationComponent(DownloadService.class);
        InputStreamDownloadResource dresource = new InputStreamDownloadResource(attachment.getInputStream(), "application/octet-stream");
        dresource.setDownloadName(attachment.getName());
        String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
        event.getRequestContext().getJavascriptManager()
             .addJavascript("window.open('" + downloadLink.replaceAll("&amp;", "&") + 
                            "', '_self', '', false); setTimeout( function() { eXo.ks.MaskLayerControl.loadImageAgain('" +
                            attContainer.getId() + "');}, 50);");
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(attContainer);
    }
  }
}