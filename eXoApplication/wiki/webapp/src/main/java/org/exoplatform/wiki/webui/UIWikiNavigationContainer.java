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

import java.util.Arrays;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.tree.TreeNode;
import org.exoplatform.wiki.tree.TreeNode.TREETYPE;
import org.exoplatform.wiki.webui.core.UIWikiForm;
import org.exoplatform.wiki.webui.tree.UITreeExplorer;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 7 Dec 2010  
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "app:/templates/wiki/webui/UIWikiNavigationContainer.gtmpl"
               )
public class UIWikiNavigationContainer extends UIWikiForm {

  public UIWikiNavigationContainer() throws Exception {
    super();    
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW, WikiMode.DELETEPAGE, WikiMode.VIEWREVISION, WikiMode.SHOWHISTORY,
        WikiMode.ADVANCEDSEARCH, WikiMode.PAGEINFO, WikiMode.COMPAREREVISION });
    UITreeExplorer uiTree = addChild(UITreeExplorer.class, null, null);
    StringBuilder initURLSb = new StringBuilder(Utils.getCurrentRestURL());
    initURLSb.append("/wiki/tree/").append(TREETYPE.ALL.toString());
    StringBuilder childrenURLSb = new StringBuilder(Utils.getCurrentRestURL());
    childrenURLSb.append("/wiki/tree/").append(TREETYPE.CHILDREN.toString());
    uiTree.init(initURLSb.toString(), childrenURLSb.toString(), getInitParam(), null, true);
    addChild(UIWikiRelatedPages.class, null, null);
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {

    String initParam = getInitParam();
    if (initParam != null) {
      this.getChild(UITreeExplorer.class).setInitParam(initParam);
    }
    super.processRender(context);
  }
  
  private String getInitParam() throws Exception {
    StringBuilder initParamSb = new StringBuilder();
    String currentPath = Utils.getCurrentWikiPagePath();
    if (currentPath != null) {    
      initParamSb.append("?").append(TreeNode.PATH).append("=").append(currentPath);
      return initParamSb.toString();
    }
    return null;
  }
  
}
