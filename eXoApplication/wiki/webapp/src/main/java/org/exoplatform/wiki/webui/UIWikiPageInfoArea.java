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
package org.exoplatform.wiki.webui;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.webui.control.UIAttachmentContainer;
import org.exoplatform.wiki.webui.core.UIWikiContainer;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageInfoArea.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiPageInfoArea.CompareRevisionActionListener.class),
    @EventConfig(listeners = UIWikiPageInfoArea.ShowRevisionActionListener.class),
    @EventConfig(listeners = UIWikiPageInfoArea.ToggleAttachmentsActionListener.class)
  }
)
public class UIWikiPageInfoArea extends UIWikiContainer {

  private static final Log log = ExoLogger.getLogger("wiki:UIWikiPageInfoArea");

  public static String TOGGLE_ATTACHMENTS_ACTION = "ToggleAttachments";
  
  public static String SHOW_REVISION = "ShowRevision";
  
  public static String COMPARE_REVISION = "CompareRevision";

  public UIWikiPageInfoArea() {
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW });
  }

  protected PageImpl getCurrentWikiPage() {
    PageImpl currentPage = null;
    try {
      currentPage = (PageImpl) Utils.getCurrentWikiPage();
    } catch (Exception e) {
      log.warn("An error happened when getting current wiki page", e);
    }
    return currentPage;
  }
  
  protected boolean isCurrentPagePublic() throws Exception {
    return Utils.isCurrentPagePublic();
  }

  public static class ToggleAttachmentsActionListener extends EventListener<UIWikiPageInfoArea> {
    @Override
    public void execute(Event<UIWikiPageInfoArea> event) throws Exception {
      UIWikiBottomArea wikiBottomArea = event.getSource().getAncestorOfType(UIWikiBottomArea.class);
      UIAttachmentContainer attachform = wikiBottomArea.findFirstComponentOfType(UIAttachmentContainer.class);
      if (attachform.isRendered()) {
        attachform.setRendered(false);
      } else {
        attachform.setRendered(true);
        UIWikiPageVersionsList pageVersions = wikiBottomArea.findFirstComponentOfType(UIWikiPageVersionsList.class);
        if (pageVersions.isRendered()) {
          pageVersions.setRendered(false);
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(wikiBottomArea.getParent());
    }
  }

  public static class ShowRevisionActionListener extends EventListener<UIWikiPageInfoArea> {
    public void execute(Event<UIWikiPageInfoArea> event) throws Exception {
      UIWikiPageInfoArea infoArea = event.getSource();
      UIWikiBottomArea bottomArea = infoArea.getParent();
      UIWikiPageVersionsList pageVersions = bottomArea.getChild(UIWikiPageVersionsList.class);
      if (pageVersions.isRendered()) {
        pageVersions.setRendered(false);
      } else {
        UIAttachmentContainer attachform = bottomArea.getChild(UIAttachmentContainer.class);
        if (attachform.isRendered()) {
          attachform.setRendered(false);
        }
        pageVersions.setRendered(true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(bottomArea.getParent());
    }
  }

  public static class CompareRevisionActionListener
                                                   extends
                                                   org.exoplatform.wiki.webui.control.action.CompareRevisionActionListener {
    public void execute(Event<UIComponent> event) throws Exception {
      ArrayList<NTVersion> lstVersion = new ArrayList<NTVersion>((Utils.getCurrentPageRevisions()));
      this.setVersionToCompare(lstVersion);
      WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
      String verName = pageParams.getParameter(org.exoplatform.wiki.utils.Utils.VER_NAME);
      if (!StringUtils.isEmpty(verName)) {
        for (int i = 0; i < lstVersion.size(); i++) {
          NTVersion ver = lstVersion.get(i);
          if (ver.getName().equals(verName) && i < lstVersion.size() + 1) {
            this.setFrom(i);
            this.setTo(i + 1);
            break;
          }
        }
      }
      super.execute(event);
    }
  }

}
