/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.services.organization.Group;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 *  Mar 3, 2008 2:12:29 PM
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/forum/webui/popup/UISelectItemForumForm.gtmpl",
  events = {
    @EventConfig(listeners = UISelectItemForum.AddActionListener.class), 
    @EventConfig(listeners = UISelectItemForum.CancelActionListener.class,phase = Phase.DECODE)
  }
)
public class UISelectItemForum extends BaseForumForm implements UIPopupComponent {
  List<ForumLinkData>                      forumLinks       = null;

  private Map<String, List<ForumLinkData>> mapListForum     = new HashMap<String, List<ForumLinkData>>();

  private Map<String, List<ForumLinkData>> mapListTopic     = new HashMap<String, List<ForumLinkData>>();

  private List<String>                     listIdIsSelected = new ArrayList<String>();
  
  private String                           userId           = null;
  private String                           categorySpId     = "forumCategoryspaces";

  public UISelectItemForum() {
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void setForumLinks(List<String> listIds) throws Exception {
    UIForumPortlet forumPortlet = getAncestorOfType(UIForumPortlet.class);
    UIForumLinks uiForumLinks = forumPortlet.getChild(UIForumLinks.class);
    listIdIsSelected = new ArrayList<String>();
    listIdIsSelected.addAll(listIds);
    if (ForumUtils.isEmpty(forumPortlet.getCategorySpaceId()) && uiForumLinks != null) {
      forumLinks = uiForumLinks.getForumLinks();
    } else {
      categorySpId = forumPortlet.getCategorySpaceId();
    }
    if (forumLinks == null || forumLinks.size() <= 0) {
      if (getForumService().getCategory(categorySpId) != null) {
        forumLinks = new ArrayList<ForumLinkData>();
        StringBuilder strQueryCate = new StringBuilder();
        strQueryCate.append("[").append(Utils.EXO_ID).append("!='").append(categorySpId).append("']");
        forumLinks.addAll(getForumService().getAllLink(strQueryCate.toString(), ForumUtils.EMPTY_STR));
        String strQuryForum = getQueryForum();
        if (!ForumUtils.isEmpty(strQuryForum)) {
          strQueryCate = new StringBuilder();
          strQueryCate.append("[").append(Utils.EXO_ID).append("='").append(categorySpId).append("']");
          forumLinks.addAll(getForumService().getAllLink(strQueryCate.toString(), strQuryForum));
        }
      } else {
        forumLinks.addAll(getForumService().getAllLink(ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR));
      }
    }
  }

  @SuppressWarnings("unchecked")
  private List<String> getGroupSpaceOfUser(String userId) {
    List<String> groupId = new ArrayList<String>();
    try {
      Collection<Group> groups = UserHelper.getOrganizationService().getGroupHandler().findGroupsOfUser(userId);
      for (Group group : groups) {
        if (group.getId().indexOf(SpaceUtils.SPACE_GROUP) >= 0) {
          groupId.add(group.getGroupName());
        }
      }
    } catch (Exception e) {
      log.warn("The method findGroupsOfUser() cannot access the database.");
    }
    return groupId;
  }

  private String getQueryForum() {
    if (!ForumUtils.isEmpty(userId)) {
      StringBuilder queryForum = new StringBuilder("[");
      List<String> groupIds = getGroupSpaceOfUser(userId);
      for (String groupId : groupIds) {
        if (queryForum.length() > 10) {
          queryForum.append(" or ");
        }
        queryForum.append("(jcr:like(@" + Utils.EXO_ID + ",'%" + groupId + "%'))");
      }
      queryForum.append("]");
      if (groupIds.size() > 0) {
        return queryForum.toString();
      }
    }
    return null;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  protected List<ForumLinkData> getForumLinks() throws Exception {
    List<ForumLinkData> linkForum = new ArrayList<ForumLinkData>();
    String cateId = ForumUtils.EMPTY_STR;
    for (ForumLinkData forumLink : this.forumLinks) {
      if (forumLink.getType().equals(Utils.CATEGORY)) {
        cateId = forumLink.getId();
        for (ForumLinkData forumlist : this.forumLinks) {
          if (forumlist.getType().equals(Utils.FORUM) && forumlist.getPath().indexOf(cateId) >= 0) {
            linkForum.add(forumlist);
            if (getUICheckBoxInput(forumlist.getPath()) == null) {
              if (listIdIsSelected.contains(forumlist.getId()))
                addUIFormInput((new UICheckBoxInput(forumlist.getPath(), forumlist.getPath(), false)).setChecked(true));
              else
                addUIFormInput((new UICheckBoxInput(forumlist.getPath(), forumlist.getPath(), false)).setChecked(false));
            }
          }
        }
        mapListForum.put(cateId, linkForum);
        linkForum = new ArrayList<ForumLinkData>();
      }
    }
    return this.forumLinks;
  }

  protected List<ForumLinkData> getForums(String categoryId) {
    return mapListForum.get(categoryId);
  }

  protected List<ForumLinkData> getTopics(String forumId) {
    return mapListTopic.get(forumId);
  }

  private String getNameForumLinkData(String id) throws Exception {
    for (ForumLinkData linkData : this.forumLinks) {
      if (linkData.getPath().equals(id))
        return linkData.getName();
    }
    return null;
  }

  static public class AddActionListener extends EventListener<UISelectItemForum> {
    public void execute(Event<UISelectItemForum> event) throws Exception {
      UISelectItemForum uiForm = event.getSource();
      List<String> listIdSelected = new ArrayList<String>();
      List<UIComponent> children = uiForm.getChildren();
      for (UIComponent child : children) {
        if (child instanceof UICheckBoxInput) {
          if (((UICheckBoxInput) child).isChecked()) {
            listIdSelected.add(uiForm.getNameForumLinkData(child.getName()) + "(" + child.getName());
          }
        }
      }
      UIModeratorManagementForm managementForm = uiForm.getAncestorOfType(UIForumPortlet.class).findFirstComponentOfType(UIModeratorManagementForm.class);
      managementForm.setModForunValues(listIdSelected);
      event.getRequestContext().addUIComponentToUpdateByAjax(managementForm);
      uiForm.cancelChildPopupAction();
    }
  }

  static public class CancelActionListener extends EventListener<UISelectItemForum> {
    public void execute(Event<UISelectItemForum> event) throws Exception {
      event.getSource().cancelChildPopupAction();
    }
  }
}
