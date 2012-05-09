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
package org.exoplatform.wiki.webui.popup;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.tree.TreeNode;
import org.exoplatform.wiki.tree.TreeNode.TREETYPE;
import org.exoplatform.wiki.webui.UIWikiBreadCrumb;
import org.exoplatform.wiki.webui.UIWikiLocationContainer;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.tree.EventUIComponent;
import org.exoplatform.wiki.webui.tree.EventUIComponent.EVENTTYPE;
import org.exoplatform.wiki.webui.tree.UITreeExplorer;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 2, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class, 
  template = "app:/templates/wiki/webui/popup/UIWikiMovePageForm.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiMovePageForm.CloseActionListener.class),
    @EventConfig(listeners = UIWikiMovePageForm.MoveActionListener.class)
    }
)
public class UIWikiMovePageForm extends UIForm implements UIPopupComponent {
  
  final static public String PAGENAME_INFO      = "pageNameInfo";

  final static public String LOCATION_CONTAINER = "UIWikiLocationContainer";

  final static public String UITREE             = "UIMoveTree";

  public String              MOVE               = "Move";
  
  public UIWikiMovePageForm() throws Exception {
    addChild(new UIFormInputInfo(PAGENAME_INFO, PAGENAME_INFO, null));
    addChild(UIWikiLocationContainer.class, null, LOCATION_CONTAINER);
    UITreeExplorer uiTree = addChild(UITreeExplorer.class, null, UITREE);

    EventUIComponent eventComponent = new EventUIComponent(LOCATION_CONTAINER,
                                                           UIWikiLocationContainer.CHANGE_NEWLOCATION,
                                                           EVENTTYPE.EVENT);
    StringBuilder initURLSb = new StringBuilder(Utils.getCurrentRestURL());
    initURLSb.append("/wiki/tree/").append(TREETYPE.ALL.toString());
    StringBuilder childrenURLSb = new StringBuilder(Utils.getCurrentRestURL());
    childrenURLSb.append("/wiki/tree/").append(TREETYPE.CHILDREN.toString());
    uiTree.init(initURLSb.toString(), childrenURLSb.toString(), getInitParam(), eventComponent, false);
  }  
  
  static public class CloseActionListener extends EventListener<UIWikiMovePageForm> {
    public void execute(Event<UIWikiMovePageForm> event) throws Exception {  
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      popupContainer.cancelPopupAction();    
    }
  } 
  
  static public class MoveActionListener extends EventListener<UIWikiMovePageForm> {
    public void execute(Event<UIWikiMovePageForm> event) throws Exception {   
      WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
      UIWikiPortlet uiWikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiMovePageForm movePageForm = event.getSource();
      UIWikiLocationContainer locationContainer = movePageForm.findFirstComponentOfType(UIWikiLocationContainer.class);
      UIWikiBreadCrumb currentLocation = (UIWikiBreadCrumb) locationContainer.getChildById(UIWikiLocationContainer.CURRENT_LOCATION);
      UIWikiBreadCrumb newLocation = (UIWikiBreadCrumb) locationContainer.getChildById(UIWikiLocationContainer.NEW_LOCATION);
      WikiPageParams currentLocationParams = currentLocation.getPageParam();
      WikiPageParams newLocationParams = newLocation.getPageParam();
      
      if (newLocationParams==null) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIWikiMovePageForm.msg.new-location-can-not-be-empty",
                                                null,
                                                ApplicationMessage.WARNING));
        org.exoplatform.wiki.commons.Utils.redirect(currentLocationParams, uiWikiPortlet.getWikiMode());
        return;
      }
      //If exist page same with move page name in new location
      PageImpl movepage = (PageImpl) wservice.getPageById(currentLocationParams.getType(),
                                                          currentLocationParams.getOwner(),
                                                          currentLocationParams.getPageId());
      PageImpl existPage = (PageImpl) wservice.getPageById(newLocationParams.getType(),
                                                           newLocationParams.getOwner(),
                                                           currentLocationParams.getPageId());
      if (existPage != null && !existPage.equals(movepage)) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIWikiMovePageForm.msg.same-name-in-new-location-space",
                                                null,
                                                ApplicationMessage.WARNING));
        org.exoplatform.wiki.commons.Utils.redirect(currentLocationParams, uiWikiPortlet.getWikiMode());
        return;
      }
      boolean isMoved = wservice.movePage(currentLocationParams, newLocationParams);      
      if (!isMoved) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIWikiMovePageForm.msg.no-permission-at-destination", null, ApplicationMessage.WARNING));
        org.exoplatform.wiki.commons.Utils.redirect(currentLocationParams, uiWikiPortlet.getWikiMode());
        return;
      }
      UIPopupContainer popupContainer = uiWikiPortlet.getPopupContainer(PopupLevel.L1);    
      popupContainer.cancelPopupAction();
      newLocationParams.setPageId(currentLocationParams.getPageId());
      org.exoplatform.wiki.commons.Utils.redirect(newLocationParams, WikiMode.VIEW);
    }
  }

  public void activate() throws Exception {

    
  }
  public void deActivate() throws Exception {

    
  }
  
  private String getInitParam() throws Exception {
    StringBuilder sb = new StringBuilder();
    String currentPath = Utils.getCurrentWikiPagePath();
    sb.append("?")
      .append(TreeNode.PATH)
      .append("=")
      .append(currentPath)
      .append("&")
      .append(TreeNode.CURRENT_PATH)
      .append("=")
      .append(currentPath);
    return sb.toString();
  }
  
}
