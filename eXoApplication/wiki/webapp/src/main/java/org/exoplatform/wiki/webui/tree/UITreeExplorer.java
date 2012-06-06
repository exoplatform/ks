/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.wiki.webui.tree ;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.commons.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 6, 2010  
 */
@ComponentConfig(
                 lifecycle = Lifecycle.class, 
                 template = "app:/templates/wiki/webui/tree/UITreeExplorer.gtmpl",
                 events = {
                     @EventConfig(listeners = UITreeExplorer.SelectNodeActionListener.class)
                     }
)
public class UITreeExplorer extends UIContainer {
  
  final static public String SELECT_NODE = "SelectNode";

  private String             initParam;

  private String             initURL;

  private String             childrenURL;

  private EventUIComponent   eventComponent;
  
  private boolean            isRenderLink;
  
  public UITreeExplorer() {
    super();
  }

  public void init(String initURL,
                   String childrenURL,
                   String initParam,
                   EventUIComponent eventComponent,
                   boolean isRenderLink) throws Exception {
    this.initURL = initURL;
    this.childrenURL = childrenURL;
    this.initParam = initParam;
    this.eventComponent = eventComponent;
    this.isRenderLink = isRenderLink;
  }

  public String getInitURL() {
    return initURL;
  }

  public void setInitURL(String initURL) {
    this.initURL = initURL;
  }
   
  public String getChildrenURL() {
    return childrenURL;
  }

  public void setChildrenURL(String childrenURL) {
    this.childrenURL = childrenURL;
  }

  public String getInitParam() {
    return initParam;
  }

  public void setInitParam(String initParam) {
    this.initParam = initParam;
  }

  public EventUIComponent getEventComponent() {
    return eventComponent;
  }

  public void setEventComponent(EventUIComponent eventComponent) {
    this.eventComponent = eventComponent;
  }
  
  public boolean isRenderLink() {
    return isRenderLink;
  }

  public String getBaseLink() throws Exception {
    return Utils.getBaseUrl();
  }

  static public class SelectNodeActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {

      WebuiRequestContext context = event.getRequestContext();
      UITreeExplorer tree = event.getSource();
      UIPortletApplication root = tree.getAncestorOfType(UIPortletApplication.class);
      EventUIComponent eventComponent = tree.getEventComponent();
      UIComponent uiComponent = null;
      if (eventComponent.getId() != null) {
        uiComponent = (UIComponent) root.findComponentById(eventComponent.getId());
      } else {
        uiComponent = root;
      }
      String eventName = eventComponent.getEventName();
      Event<UIComponent> xEvent = uiComponent.createEvent(eventName, Event.Phase.PROCESS, context);
      if (xEvent != null) {
        xEvent.broadcast();
      }
    }
  }
}
