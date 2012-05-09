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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiPermissionForm;
import org.exoplatform.wiki.webui.UIWikiPermissionForm.Scope;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.core.UIExtensionContainer;
import org.exoplatform.wiki.webui.extension.UISyntaxSettingForm;
import org.exoplatform.wiki.webui.extension.UITemplateSettingForm;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 27 Jan 2011  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/popup/UIWikiSettingContainer.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiSettingContainer.ActiveItemActionListener.class)   
  }
)
public class UIWikiSettingContainer extends UIExtensionContainer implements UIPopupComponent {
  private static final Log log = ExoLogger.getExoLogger(UIWikiSettingContainer.class);
  
  private String             activeItem;

  private List<String>       items          = new ArrayList<String>();

  public static final String EXTENSION_TYPE = "org.exoplatform.wiki.webui.popup.UIWikiSettingContainer";

  public static final String ACTION         = "ActiveItem";

  public UIWikiSettingContainer() throws Exception {
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {   
    UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
    HashMap<String, Object> extContext = wikiPortlet.getUIExtContext();
    if (checkModificationContext(extContext)) {
      activeItem = null;
      UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
      List<UIExtension> extensions = manager.getUIExtensions(EXTENSION_TYPE);
      if (!items.isEmpty()) {
        items.clear();
      }
      if (extensions != null) {
        for (int i = 0; i < extensions.size(); i++) {
          UIComponent component = manager.addUIExtension(extensions.get(i), extContext, this);
          items.add(component.getId());
          if (activeItem == null) {
            setActiveItem(component.getId());
          }
        }
      }
    }
    // To prevent active component from being hidden
    getChildById(activeItem).setRendered(true);
    super.processRender(context);
  }
  
  private void checkToUpdateData() throws Exception {
    if (activeItem == null) {
      return;
    }
    UIComponent component = getChildById(activeItem);
    if (component instanceof UIWikiPermissionForm) {
      UIWikiPermissionForm uiWikiPermissionForm = (UIWikiPermissionForm) component;
      uiWikiPermissionForm.setScope(Scope.WIKI);
      WikiService wikiService = uiWikiPermissionForm.getApplicationComponent(WikiService.class);
      WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
      uiWikiPermissionForm.setPermission(wikiService.getWikiPermission(pageParams.getType(), pageParams.getOwner()));
    } else if (component instanceof UITemplateSettingForm) {
      UITemplateSettingForm uiTemplateSettingForm = (UITemplateSettingForm) component;
      uiTemplateSettingForm.initGrid();
    } else if (component instanceof UISyntaxSettingForm) {
      UISyntaxSettingForm uiSyntaxSettingForm = (UISyntaxSettingForm) component;
      uiSyntaxSettingForm.updateData();
    }
  }

  public String getActiveItem() {
    return activeItem;
  }

  public void setActiveItem(String activeItem) {
    if (CommonUtils.isEmpty(this.activeItem) || !this.activeItem.equals(activeItem)) {
      this.activeItem = activeItem;
      try {
        checkToUpdateData();
      } catch (Exception e) {
        log.warn("Can not get permission data", e);
      }
    }
  }

  public List<String> getItems() {
    return items;
  }

  public void setItems(List<String> items) {
    this.items = items;
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }
  
  static public class ActiveItemActionListener extends EventListener<UIWikiSettingContainer> {
    public void execute(Event<UIWikiSettingContainer> event) throws Exception {
      UIWikiSettingContainer container = event.getSource();
      container.setActiveItem(event.getRequestContext().getRequestParameter(OBJECTID));
    }
  }
}
