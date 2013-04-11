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
package org.exoplatform.faq.webui.viewer;

import javax.portlet.PortletMode;


import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.popup.UIFAQSettingForm;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS 
 * Author : Vu Duy Tu 
 *           tu.duy@exoplatform.com
 * Jun 24, 2009 - 2:26:16 AM
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/templates/faq/webui/UIFAQPortlet.gtmpl"
)
public class UIFAQPortlet extends UIPortletApplication {
  private final static String SLASH     = "/".intern();

  public UIFAQPortlet() throws Exception {
    addChild(UIViewer.class, null, null);
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext portletReqContext = (PortletRequestContext) context;
    if (portletReqContext.getApplicationMode() == PortletMode.VIEW) {
      removeChild(UIFAQSettingForm.class);
      UIViewer uiViewer = getChild(UIViewer.class);
      if (uiViewer == null) {
        uiViewer = addChild(UIViewer.class, null, null).setRendered(true);
      }
      if (FAQUtils.isFieldEmpty(context.getRequestParameter(OBJECTID)) && 
                       !context.getParentAppRequestContext().useAjax() && StringUtils.EMPTY.equals(uiViewer.getPath())) {
        uiViewer.setPath(getPathOfCateSpace());
      }
    } else if (portletReqContext.getApplicationMode() == PortletMode.EDIT) {
      removeChild(UIViewer.class);
      if (getChild(UIFAQSettingForm.class) == null) {
        UIFAQSettingForm settingForm = addChild(UIFAQSettingForm.class, null, null);
        settingForm.defaulValue();
      }
    }
    super.processRender(app, context);
  }
  
  public String getPathOfCateSpace() {
    String url;
    try {
      url = org.exoplatform.social.webui.Utils.getSpaceUrlByContext();
      if (url  != null &&  url != "") {
        SpaceService sService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
        FAQService fService = (FAQService) getApplicationComponent(FAQService.class);
        Space space = sService.getSpaceByUrl(url);
        String pathOfCateSpace = Utils.CATEGORY_HOME + SLASH + Utils.CATE_SPACE_ID_PREFIX + space.getPrettyName();
        if (fService.isExisting(pathOfCateSpace)) {
          return pathOfCateSpace;
        }
      }
      return Utils.CATEGORY_HOME;
    } catch (Exception e) {
      return Utils.CATEGORY_HOME;
    }    
  }
}
