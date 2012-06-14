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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.common.webui;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.commons.utils.SerializablePageList;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
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
  @EventConfig(listeners = UIUserSelect.SearchActionListener.class, phase = Phase.DECODE),
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

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    String keyword = getUIStringInput(FIELD_KEYWORD).getValue();
    if (CommonUtils.isEmpty(keyword) && !CommonUtils.isEmpty(spaceGroupId) && uiIterator_ != null) {
      OrganizationService service = getApplicationComponent(OrganizationService.class);
      ListAccess<User> listAccess = service.getUserHandler().findUsersByGroupId(spaceGroupId);
      List<User> results = Arrays.asList(listAccess.load(0, listAccess.getSize()));
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
  
  static public class SearchActionListener extends EventListener<UIUserSelect> {
    public void execute(Event<UIUserSelect> event) throws Exception {
      UIUserSelect uiForm = event.getSource();
      String keyword = uiForm.getUIStringInput(FIELD_KEYWORD).getValue();
      String filter = uiForm.getUIFormSelectBox(FIELD_FILTER).getValue();
      String groupId = uiForm.getSpaceGroupId();
      uiForm.search(keyword, filter, groupId);
      if (filter == null || filter.trim().length() == 0){
        return;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
}
