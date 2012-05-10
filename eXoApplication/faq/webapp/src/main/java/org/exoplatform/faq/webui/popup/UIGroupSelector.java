/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 **/
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.UISelectComponent;
import org.exoplatform.ks.common.webui.UISelector;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.organization.UIGroupMembershipSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Aug 29, 2007 11:57:56 AM 
 */
@ComponentConfigs({ 
  @ComponentConfig(
      template = "app:/templates/faq/webui/popup/UIGroupSelector.gtmpl",
      events = {
          @EventConfig(listeners = UIGroupSelector.ChangeNodeActionListener.class),
          @EventConfig(listeners = UIGroupSelector.SelectMembershipActionListener.class),
          @EventConfig(listeners = UIGroupSelector.SelectPathActionListener.class)  
      }  
  ),
  @ComponentConfig(
      type = UITree.class, id = "UITreeGroupSelector",
      template = "system:/groovy/webui/core/UITree.gtmpl",
      events = @EventConfig(listeners = UITree.ChangeNodeActionListener.class)
  ),
  @ComponentConfig(
      type = UIBreadcumbs.class, id = "BreadcumbGroupSelector",
      template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl",
      events = @EventConfig(listeners = UIBreadcumbs.SelectPathActionListener.class)
  )
})

public class UIGroupSelector extends UIGroupMembershipSelector implements UIPopupComponent, UISelectComponent {

  private UIComponent         uiComponent;

  private String              type_           = null;

  private List<Group>         selectedGroup_;

  private String              returnFieldName = null;

  private String              spaceId         = null;

  private String              spaceParentId   = null;

  private OrganizationService service;

  public UIGroupSelector() throws Exception {
    service = UserHelper.getOrganizationService();
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    if(!FAQUtils.isFieldEmpty(spaceId)) {
      UITree uiTree = getChild(UITree.class);
      Group parentGroup = (Group)uiTree.getParentSelected();
      if(parentGroup == null) {
        uiTree.setParentSelected(null);
      }
    }
    super.processRender(context);
  }
  
  public UIComponent getReturnComponent() {
    return uiComponent;
  }

  public String getReturnField() {
    return returnFieldName;
  }

  public void setComponent(UIComponent uicomponent, String[] initParams) {
    uiComponent = uicomponent;
    if (initParams == null || initParams.length <= 0)
      return;
    for (int i = 0; i < initParams.length; i++) {
      if (initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=");
        returnFieldName = array[1];
        break;
      }
      returnFieldName = initParams[0];
    }
  }

  public List<Group> getChildGroup() throws Exception {
    List<Group> children = UserHelper.findGroups(getCurrentGroup());
    return children;
  }

  public boolean isSelectGroup() {
    return TYPE_GROUP.equals(type_);
  }

  public boolean isSelectUser() {
    return TYPE_USER.equals(type_);
  }

  public boolean isSelectMemberShip() {
    return TYPE_MEMBERSHIP.equals(type_);
  }

  @SuppressWarnings("unchecked")
  public List<String> getList() throws Exception {
    List<String> children = new ArrayList<String>();
    if (TYPE_USER.equals(type_)) {
      ListAccess<User> userPageList = service.getUserHandler().findUsersByGroupId(getCurrentGroup().getId());
      User users[] = userPageList.load(0, userPageList.getSize());
      for (int i = 0; i < userPageList.getSize(); i++) {
        children.add(users[i].getUserName());
      }
    } else if (TYPE_MEMBERSHIP.equals(type_)) {
      for (String child : getListMemberhip()) {
        children.add(child);
      }
    } else if (TYPE_GROUP.equals(type_) && getCurrentGroup() != null) {
      if (!FAQUtils.isFieldEmpty(spaceId) && getCurrentGroup().getId().equals(spaceParentId)) {
        Group group = service.getGroupHandler().findGroupById(spaceId);
        children.add(group.getGroupName());
      } else {
        Collection<Group> groups = service.getGroupHandler().findGroups(getCurrentGroup());
        for (Group child : groups) {
          children.add(child.getGroupName());
        }
      }
    }
    return children;
  }

  public void setSelectedGroups(List<Group> groups) {
    if (groups != null) {
      selectedGroup_ = groups;
      getChild(UITree.class).setSibbling(selectedGroup_);
    }
  }

  public void setSpaceGroupId(String groupId) throws Exception {
    if (!FAQUtils.isFieldEmpty(groupId)) {
      Group group = service.getGroupHandler().findGroupById(groupId);
      if (group != null) {
        this.spaceId = groupId;
        selectedGroup_ = new ArrayList<Group>();
        selectedGroup_.add(group);
        spaceParentId = group.getParentId();
        changeGroup(spaceParentId);
      }
    } else {
      setSelectedGroups(null);
    }
  }

  public void changeGroup(String groupId) throws Exception {
    super.changeGroup(groupId);
    if (selectedGroup_ != null) {
      UITree tree = getChild(UITree.class);
      tree.setSibbling(selectedGroup_);
      tree.setChildren(null);
    }
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void setType(String type) {
    this.type_ = type;
  }

  public String getType() {
    return type_;
  }

  static public class SelectMembershipActionListener extends EventListener<UIGroupSelector> {
    public void execute(Event<UIGroupSelector> event) throws Exception {
      String user = event.getRequestContext().getRequestParameter(OBJECTID);
      UIGroupSelector uiGroupSelector = event.getSource();
      String returnField = uiGroupSelector.getReturnField();
      ((UISelector) uiGroupSelector.getReturnComponent()).updateSelect(returnField, user);
      try {
        UIPopupContainer popupContainer = uiGroupSelector.getAncestorOfType(UIPopupContainer.class);
        UIPopupAction popupAction;
        if (((UIComponent) uiGroupSelector.getParent()).getId().equals(popupContainer.getId())) {
          popupAction = popupContainer.getAncestorOfType(UIPopupAction.class);
        } else {
          popupAction = popupContainer.getChild(UIPopupAction.class);
        }
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupSelector.getReturnComponent());
      } catch (NullPointerException e) {
        UIPopupAction uiPopup = uiGroupSelector.getAncestorOfType(UIPopupAction.class);
        uiPopup.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupSelector.getReturnComponent());
      }
    }
  }

  static public class ChangeNodeActionListener extends EventListener<UITree> {
    public void execute(Event<UITree> event) throws Exception {
      UIGroupSelector uiGroupSelector = event.getSource().getAncestorOfType(UIGroupSelector.class);
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiGroupSelector.changeGroup(groupId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupSelector);
    }
  }

  static public class SelectPathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIGroupSelector uiGroupSelector = event.getSource().getParent();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiGroupSelector.changeGroup(objectId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupSelector);
    }
  }
}
