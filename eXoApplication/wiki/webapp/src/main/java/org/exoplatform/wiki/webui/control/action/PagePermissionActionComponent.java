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
package org.exoplatform.wiki.webui.control.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.IDType;
import org.exoplatform.wiki.service.Permission;
import org.exoplatform.wiki.service.PermissionEntry;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.webui.UIWikiPermissionForm;
import org.exoplatform.wiki.webui.UIWikiPermissionForm.Scope;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.control.action.core.AbstractEventActionComponent;
import org.exoplatform.wiki.webui.control.filter.AdminPagesPermissionFilter;
import org.exoplatform.wiki.webui.control.filter.IsViewModeFilter;
import org.exoplatform.wiki.webui.control.listener.MoreContainerActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Dec 29, 2010  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/action/AbstractActionComponent.gtmpl",
  events = {
    @EventConfig(listeners = PagePermissionActionComponent.PagePermissionActionListener.class)
  }
)
public class PagePermissionActionComponent extends AbstractEventActionComponent {
  
  public static final String                   ACTION  = "PagePermission";

  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] {
      new IsViewModeFilter(), new AdminPagesPermissionFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }

  @Override
  public String getActionName() {
    return ACTION;
  }

  @Override
  public boolean isAnchor() {
    return false;
  }

  public static class PagePermissionActionListener extends MoreContainerActionListener<PagePermissionActionComponent> {
    @Override
    protected void processEvent(Event<PagePermissionActionComponent> event) throws Exception {
      UIWikiPortlet uiWikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer uiPopupContainer = uiWikiPortlet.getPopupContainer(PopupLevel.L1);
      UIWikiPermissionForm uiWikiPermissionForm = uiPopupContainer.createUIComponent(UIWikiPermissionForm.class, null, "UIWikiPagePermissionForm");
      uiPopupContainer.activate(uiWikiPermissionForm, 800, 0);
      uiWikiPermissionForm.setScope(Scope.PAGE);
      PageImpl page = (PageImpl) Utils.getCurrentWikiPage();
      HashMap<String, String[]> permissionMap = page.getPermission();
      List<PermissionEntry> permissionEntries = convertToPermissionEntryList(permissionMap);
      uiWikiPermissionForm.setPermission(permissionEntries);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
      super.processEvent(event);
    }

    private List<PermissionEntry> convertToPermissionEntryList(HashMap<String, String[]> permissions) {
      List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>();
      Set<Entry<String, String[]>> entries = permissions.entrySet();
      for (Entry<String, String[]> entry : entries) {
        PermissionEntry permissionEntry = new PermissionEntry();
        String key = entry.getKey();
        IDType idType = IDType.USER;
        if (key.indexOf(":") > 0) {
          idType = IDType.MEMBERSHIP;
        } else if (key.indexOf("/") == 0) {
          idType = IDType.GROUP;
        }
        permissionEntry.setIdType(idType);
        permissionEntry.setId(key);
        Permission[] perms = new Permission[2];
        perms[0] = new Permission();
        perms[0].setPermissionType(PermissionType.VIEWPAGE);
        perms[1] = new Permission();
        perms[1].setPermissionType(PermissionType.EDITPAGE);
        for (String action : entry.getValue()) {
          if (org.exoplatform.services.jcr.access.PermissionType.READ.equals(action)) {
            perms[0].setAllowed(true);
          } else if (org.exoplatform.services.jcr.access.PermissionType.ADD_NODE.equals(action)
              || org.exoplatform.services.jcr.access.PermissionType.REMOVE.equals(action)
              || org.exoplatform.services.jcr.access.PermissionType.SET_PROPERTY.equals(action)) {
            perms[1].setAllowed(true);
          }
        }
        permissionEntry.setPermissions(perms);

        permissionEntries.add(permissionEntry);
      }
      return permissionEntries;
    }
  }
}
