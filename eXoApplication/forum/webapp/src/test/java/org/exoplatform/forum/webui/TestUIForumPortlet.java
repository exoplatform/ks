/***************************************************************************
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.forum.webui;

import org.exoplatform.forum.base.AbstractUIComponentTestCase;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Feb 3, 2010 - 4:33:01 AM  
 */
public class TestUIForumPortlet extends AbstractUIComponentTestCase<UIForumPortlet> {
  protected Log log = ExoLogger.getLogger(this.getClass());
  public TestUIForumPortlet() throws Exception {
    super();
  }

  public void testCheckCanView() throws Exception {
    try {
      component.checkCanView(new Category("categoryId"), new Forum(), new Topic());
    } catch (Exception e) {
      log.error("Can not check can view, unit test can not suppost IdGenerator.generate()");
    }
  }

  public void testCalculateRenderCompoRnent() throws Exception {
    try {
      WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
      component.calculateRenderComponent(Utils.FORUM_SERVICE, requestContext);
    } catch (Exception e) {
    }
  }

  public void testRenderComponentByURL() throws Exception {
    try {
      WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
      component.renderComponentByURL(requestContext);
    } catch (Exception e) {
    }
  }
  
  @Override
  protected UIForumPortlet createComponent() throws Exception {
    return new UIForumPortlet();
  }

}
