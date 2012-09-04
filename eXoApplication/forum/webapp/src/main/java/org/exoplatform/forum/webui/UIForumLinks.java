/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionResponse;
import javax.xml.namespace.QName;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.forum.info.UIForumLinkPortlet;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/UIForumLinks.gtmpl",
    events = {
      @EventConfig(listeners = UIForumLinks.SelectActionListener.class)      
    }
)
public class UIForumLinks extends BaseForumForm {
  public static final String  FIELD_FORUMLINK_SELECTBOX = "forumLink";

  public static final String  FIELD_FORUMHOMEPAGE_LABEL = "forumHomePage";

  private String              path                      = Utils.FORUM_SERVICE;

  private List<ForumLinkData> forumLinks                = null;

  public UIForumLinks() throws Exception {
  }

  private StringBuffer getStrQuery(List<String> list, String property) {
    StringBuffer strQuery = new StringBuffer();
    String AC = (property.indexOf("fn") < 0) ? "@" : ForumUtils.EMPTY_STR;
    for (String string : list) {
      if (strQuery.length() == 0) {
        strQuery.append("(").append(AC).append(property).append("='").append(string).append("'");
      } else {
        strQuery.append(" or ").append(AC).append(property).append("='").append(string).append("'");
      }
    }
    if (strQuery.length() > 0) {
      strQuery.append(")");
    }
    return strQuery;
  }

  public void setUpdateForumLinks() throws Exception {
    UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
    try {
      this.userProfile = forumPortlet.getUserProfile();
    } catch (Exception e) {
      String userName = UserHelper.getCurrentUser();
      if (userName != null) {
        try {
          userProfile = getForumService().getQuickProfile(userName);
        } catch (Exception ex) {
          userProfile = new UserProfile();
        }
      }
    }
    StringBuffer buffQueryCate = new StringBuffer();
    StringBuffer buffQueryForum = new StringBuffer();
    List<String> listUser = UserHelper.getAllGroupAndMembershipOfUser(null);
    if (this.userProfile.getUserRole() > 0) {
      // set Query for Forum
      StringBuffer mods = getStrQuery(listUser, Utils.EXO_MODERATORS);
      if (mods.length() > 0) {
        buffQueryForum.append("(@").append(Utils.EXO_IS_CLOSED).append("='false' or ").append(mods).append(")");
      } else {
        buffQueryForum.append("(@").append(Utils.EXO_IS_CLOSED).append("='false')");
      }
      // set Query for Category
      listUser.add(" ");
      buffQueryCate = getStrQuery(listUser, Utils.EXO_USER_PRIVATE);
    }
    List<String> listCateIdScope = forumPortlet.getInvisibleCategories();
    List<String> listForumIdScope = forumPortlet.getInvisibleForums();
    if (!listForumIdScope.isEmpty() && !listForumIdScope.get(0).equals(" ")) {
      if (buffQueryForum.length() > 0) {
        buffQueryForum.append(" and ").append(getStrQuery(listForumIdScope, "fn:name()"));
      } else {
        buffQueryForum.append(getStrQuery(listForumIdScope, "fn:name()"));
      }
    }

    if (!listCateIdScope.isEmpty() && !listCateIdScope.get(0).equals(" ")) {

      if (buffQueryCate.length() > 0) {
        buffQueryCate.append(" and ").append(getStrQuery(listCateIdScope, "fn:name()"));
      } else {
        buffQueryCate.append(getStrQuery(listCateIdScope, "fn:name()"));
      }
    }
    if (buffQueryForum.length() > 0) {
      buffQueryForum = new StringBuffer("[").append(buffQueryForum).append("]");
    }
    if (buffQueryCate.length() > 0) {
      buffQueryCate = new StringBuffer("[").append(buffQueryCate).append("]");
    }

    this.forumLinks = getForumService().getAllLink(buffQueryCate.toString(), buffQueryForum.toString());
    List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>();
    list.add(new SelectItemOption<String>(this.getLabel(FIELD_FORUMHOMEPAGE_LABEL) + ForumUtils.SLASH + FIELD_FORUMHOMEPAGE_LABEL, Utils.FORUM_SERVICE));
    String space = "&nbsp; &nbsp; ", type = "/categoryLink";
    for (ForumLinkData linkData : forumLinks) {
      if (linkData.getType().equals(Utils.FORUM)) {
        type = ForumUtils.SLASH + FIELD_FORUMLINK_SELECTBOX;
        space = "&nbsp; &nbsp; &nbsp; &nbsp; ";
      }
      if (linkData.getType().equals(Utils.CATEGORY)) {
        type = "/categoryLink";
        space = "&nbsp; &nbsp; ";
      }
      if (linkData.getType().equals(Utils.TOPIC))
        continue;
      list.add(new SelectItemOption<String>(space + linkData.getName() + type, linkData.getPath()));
    }
    UIFormSelectBoxForum forumLink;
    if (getChild(UIFormSelectBoxForum.class) != null) {
      forumLink = this.getChild(UIFormSelectBoxForum.class).setOptions(list);
      if (ForumUtils.isEmpty(path))
        forumLink.setValue(Utils.FORUM_SERVICE);
      else
        forumLink.setValue(path.trim());
    } else {
      forumLink = new UIFormSelectBoxForum(FIELD_FORUMLINK_SELECTBOX, FIELD_FORUMLINK_SELECTBOX, list);
      if (ForumUtils.isEmpty(path))
        forumLink.setValue(Utils.FORUM_SERVICE);
      else
        forumLink.setValue(path.trim());
      addUIFormInput(forumLink);
    }
  }

  public UIFormSelectBoxForum getUIFormSelectBoxForum(String name) {
    return findComponentById(name);
  }

  public List<ForumLinkData> getForumLinks() {
    return this.forumLinks;
  }

  public void setValueOption(String path) {
    this.path = path;
  }

  static public class SelectActionListener extends EventListener<UIForumLinks> {
    public void execute(Event<UIForumLinks> event) throws Exception {
      UIForumLinks uiForm = event.getSource();
      UIFormSelectBoxForum selectBoxForum = uiForm.getUIFormSelectBoxForum(FIELD_FORUMLINK_SELECTBOX);
      String path = selectBoxForum.getValue();
      try {
        UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
        boolean isErro = false;
        if (!path.equals(uiForm.path)) {
          if (path.lastIndexOf(Utils.FORUM) > 0) {
            String id[] = path.trim().split(ForumUtils.SLASH);
            Forum forum = uiForm.getForumService().getForum(id[0], id[1]);
            ;
            if (forum != null) {
              UIForumContainer forumContainer = forumPortlet.findFirstComponentOfType(UIForumContainer.class);
              forumContainer.getChild(UIForumDescription.class).setForum(forum);
              forumContainer.getChild(UITopicContainer.class).updateByBreadcumbs(id[0], id[1], true, 0);
              forumContainer.setIsRenderChild(true);
              forumPortlet.updateIsRendered(ForumUtils.FORUM);
            } else
              isErro = true;
          } else if (path.indexOf(Utils.CATEGORY) >= 0) {
            Category category = uiForm.getForumService().getCategory(path.trim());
            if (category != null) {
              UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class);
              categoryContainer.getChild(UICategory.class).update(category, null);
              categoryContainer.updateIsRender(false);
              forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
            } else
              isErro = true;
          }
          if (isErro) {
            uiForm.warning("UIShowBookMarkForm.msg.link-not-found", false);
            path = Utils.FORUM_SERVICE;
          }
          if (path.indexOf(Utils.FORUM_SERVICE) >= 0) {
            UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class);
            categoryContainer.updateIsRender(true);
            categoryContainer.getChild(UICategories.class).setIsRenderChild(false);
            forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
          }
          uiForm.path = path;
          forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(path.trim());
          event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
        }
      } catch (Exception e) {
        try {
          if (!path.equals(uiForm.path)) {
            uiForm.path = path;
            UIForumLinkPortlet linkPortlet = uiForm.getParent();
            if (path.equals(Utils.FORUM_SERVICE)) {
              linkPortlet.setRenderChild(false);
            } else {
              linkPortlet.setRenderChild(true);
            }
            ActionResponse actionRes = event.getRequestContext().getResponse();
            ForumParameter param = new ForumParameter();
            param.setPath(path);
            actionRes.setEvent(new QName("OpenLink"), param);
            event.getRequestContext().addUIComponentToUpdateByAjax(linkPortlet);
          }
        } catch (Exception ex) {
          uiForm.log.error("Rendering " + uiForm.getParent() + " uicomponent fail: " + ex.getMessage() + "\n" + ex.getCause());
        }
      }
    }
  }
}
