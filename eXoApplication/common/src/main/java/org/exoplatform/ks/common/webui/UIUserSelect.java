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
package org.exoplatform.ks.common.webui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.commons.utils.SerializablePageList;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Van Hoang
 *          hoangnv01@gmail.com
 * Jun 22, 2010  
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/organization/account/UIUserSelector.gtmpl", events = {
  @EventConfig(listeners = UIUserSelector.AddActionListener.class, phase = Phase.DECODE),
  @EventConfig(listeners = UIUserSelector.AddUserActionListener.class, phase = Phase.DECODE),
  @EventConfig(listeners = UIUserSelector.SearchActionListener.class, phase = Phase.DECODE),
  @EventConfig(listeners = UIUserSelector.SearchGroupActionListener.class, phase = Phase.DECODE),
  @EventConfig(listeners = UIUserSelector.SelectGroupActionListener.class, phase = Phase.DECODE),
  @EventConfig(listeners = UIUserSelector.FindGroupActionListener.class, phase = Phase.DECODE),
  @EventConfig(listeners = UIUserSelector.ShowPageActionListener.class, phase = Phase.DECODE),
  @EventConfig(listeners = UIUserSelector.CloseActionListener.class, phase = Phase.DECODE)})
  
  /**
   * Add a permission type on forum form. Extended from UIUserSelector class
   * */
public class UIUserSelect extends UIUserSelector {
  private String permisionType;
  private String spaceGroupId = null;

  public UIUserSelect() throws Exception {
    super();
  }

  @SuppressWarnings({ "unchecked", "deprecation" })
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    if (spaceGroupId != null && spaceGroupId.length() > 0 && uiIterator_ != null) {
      OrganizationService service = getApplicationComponent(OrganizationService.class);
      List<User> results = new CopyOnWriteArrayList<User>();
      results.addAll(uiIterator_.getPageList().getAll());
      // remove if user doesn't exist in selected group
      MembershipHandler memberShipHandler = service.getMembershipHandler();
      for (User user : results) {
        if (memberShipHandler.findMembershipsByUserAndGroup(user.getUserName(), spaceGroupId).size() == 0) {
          results.remove(user);
        }
      }
      uiIterator_.setPageList(new SerializablePageList<User>(new ListAccessImpl<User>(User.class, results), 10));
    }
    super.processRender(context);
  }
  
  public String getSpaceGroupId() {
    return spaceGroupId;
  }

  public void setSpaceGroupId(String spaceGroupId) {
    this.spaceGroupId = spaceGroupId;
  }

  public String getPermisionType() {
    return permisionType;
  }

  public void setPermisionType(String permisionType) {
    this.permisionType = permisionType;
  }
  
}
