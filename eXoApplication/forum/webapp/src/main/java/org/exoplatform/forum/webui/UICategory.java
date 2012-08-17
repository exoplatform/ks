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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UICategoryForm;
import org.exoplatform.forum.webui.popup.UIExportForm;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIImportForm;
import org.exoplatform.forum.webui.popup.UIMoveForumForm;
import org.exoplatform.forum.webui.popup.UIWatchToolsForm;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */


@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template = "app:/templates/forum/webui/UICategory.gtmpl",
    events = {
        @EventConfig(listeners = UICategory.SearchFormActionListener.class),
        @EventConfig(listeners = UICategory.EditCategoryActionListener.class),
        @EventConfig(listeners = UICategory.WatchOptionActionListener.class),
        @EventConfig(listeners = UICategory.ExportCategoryActionListener.class),
        @EventConfig(listeners = UICategory.ImportForumActionListener.class),
        @EventConfig(listeners = UICategory.DeleteCategoryActionListener.class),
        @EventConfig(listeners = UICategory.AddForumActionListener.class),
        @EventConfig(listeners = UICategory.EditForumActionListener.class),
        @EventConfig(listeners = UICategory.SetLockedActionListener.class),
        @EventConfig(listeners = UICategory.SetUnLockActionListener.class),
        @EventConfig(listeners = UICategory.SetOpenActionListener.class),
        @EventConfig(listeners = UICategory.SetCloseActionListener.class),
        @EventConfig(listeners = UICategory.MoveForumActionListener.class),
        @EventConfig(listeners = UICategory.RemoveForumActionListener.class),
        @EventConfig(listeners = UICategory.OpenForumLinkActionListener.class),
        @EventConfig(listeners = UICategory.OpenLastTopicLinkActionListener.class),
        @EventConfig(listeners = UICategory.OpenLastReadTopicActionListener.class),
        @EventConfig(listeners = UICategory.AddBookMarkActionListener.class),
        @EventConfig(listeners = UICategory.AddWatchingActionListener.class),
        @EventConfig(listeners = UICategory.UnWatchActionListener.class),
        @EventConfig(listeners = UICategory.RSSActionListener.class),
        @EventConfig(listeners = UICategory.AdvancedSearchActionListener.class)
    }
)
public class UICategory extends BaseForumForm {
  private String             categoryId;

  private Category           category;

  private boolean            isEditCategory  = false;

  private boolean            isEditForum     = false;

  private boolean            useAjax         = true;
  
  private boolean            isCategorySpace = true;
  
  private int                dayForumNewPost = 0;

  private List<Forum>        forums          = new ArrayList<Forum>();

  private Map<String, Topic> MaptopicLast    = new HashMap<String, Topic>();

  static public boolean      isUnWatch       = false;

  public UICategory() throws Exception {
    addUIFormInput(new UIFormStringInput(ForumUtils.SEARCHFORM_ID, null));
    setActions(new String[] { "EditCategory", "ExportCategory", "ImportForum", "DeleteCategory", "WatchOption", "AddForum", "EditForum", "SetLocked", "SetUnLock", "SetOpen", "SetClose", "MoveForum", "RemoveForum" });
  }

  public void initForm() throws Exception {
    UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
    useAjax = forumPortlet.isUseAjax();
    dayForumNewPost = forumPortlet.getDayForumNewPost();
    setListWatches();
  }

  protected boolean useAjax() {
    return useAjax;
  }

  protected String getActionViewInfoUser(String linkType, String userName) {
    return getAncestorOfType(UIForumPortlet.class).getPortletLink(linkType, userName);
  }
  
  protected String getConfirm(String confirm) {
    confirm = confirm.replace("'", "\\47").replace("\"", "\\42");
    return confirm;
  }

  public String getRSSLink(String cateId) {
    PortalContainer pcontainer = PortalContainer.getInstance();
    return CommonUtils.getRSSLink("forum", pcontainer.getPortalContainerInfo().getContainerName(), cateId);
  }

  protected int getDayForumNewPost() {
    return dayForumNewPost;
  }

  protected String getLastReadPostOfForum(String forumId) throws Exception {
    return userProfile.getLastPostIdReadOfForum(forumId);
  }

  public void update(Category category, List<Forum> forums) throws Exception {
    this.category = category;
    if (forums == null) {
      this.isEditForum = true;
    } else {
      this.forums = forums;
    }
    this.categoryId = category.getId();
    this.isCategorySpace = (categoryId.indexOf(ForumUtils.SPACE_GROUP_ID) > 0);
    this.getAncestorOfType(UIForumPortlet.class).getChild(UIBreadcumbs.class).setUpdataPath((categoryId));
  }

  public void updateByBreadcumbs(String categoryId) {
    this.categoryId = categoryId;
    this.isEditCategory = true;
    this.isEditForum = true;
    this.isCategorySpace = (categoryId.indexOf(ForumUtils.SPACE_GROUP_ID) > 0);
  }
  
  public void updateByLink(Category category) {
    this.categoryId = category.getId();
    this.isEditCategory = false;
    this.isEditForum = true;
    this.category = category;
    this.isCategorySpace = (categoryId.indexOf(ForumUtils.SPACE_GROUP_ID) > 0);
  }

  public String getCategoryId() {
    return this.categoryId;
  }

  public boolean isCategorySpace() {
    return this.isCategorySpace;
  }

  private Category getCategory() throws Exception {
    if (this.isEditCategory || this.category == null) {
      try {
        this.category = getForumService().getCategory(this.categoryId);
      } catch (Exception e) {
        log.debug("Failed to get category", e);
      }
      this.isEditCategory = false;
    }
    if(category != null) {
      initForm();
    }
    return category;
  }

  private Category refreshCategory() throws Exception {
    try {
      this.category = getForumService().getCategory(this.categoryId);
    } catch (Exception e) {
      log.debug("Failed to get category", e);
    }
    return category;
  }

  private boolean isShowForum(String id) {
    if (this.getAncestorOfType(UIForumPortlet.class).getInvisibleCategories().isEmpty())
      return true;
    List<String> list = new ArrayList<String>();
    list.addAll(this.getAncestorOfType(UIForumPortlet.class).getInvisibleForums());
    return (list.contains(id)) ? true : false;
  }

  protected List<Forum> getForumList() throws Exception {
    if (this.isEditForum) {
      String strQuery = ForumUtils.EMPTY_STR;
      if (this.userProfile.getUserRole() > 0)
        strQuery = "(@exo:isClosed='false') or (exo:moderators='" + this.userProfile.getUserId() + "')";
      try {
        this.forums = getForumService().getForumSummaries(this.categoryId, strQuery);
      } catch (Exception e) {
        log.debug("Failed to get forum summaries", e);
      }
      this.isEditForum = false;
      this.getAncestorOfType(UICategoryContainer.class).getChild(UICategories.class).setIsgetForumList(true);
    }
    List<Forum> listForum = new ArrayList<Forum>();
    UICheckBoxInput checkBoxInput;
    for (Forum forum : this.forums) {
      String forumId = forum.getId();
      if (getUICheckBoxInput(forumId) != null) {
        checkBoxInput = getUICheckBoxInput(forumId).setChecked(false);
      } else {
        checkBoxInput = new UICheckBoxInput(forumId, forumId, false);
      }
      checkBoxInput.setHTMLAttribute("title", forum.getForumName());
      addUIFormInput(checkBoxInput);
      if (isShowForum(forumId))
        listForum.add(forum);
    }
    return listForum;
  }

  public void setIsEditCategory(boolean isEdit) {
    this.isEditCategory = isEdit;
  }

  public void setIsEditForum(boolean isEdit) {
    this.isEditForum = isEdit;
  }

  private Forum getForum(String forumId) throws Exception {
    for (Forum forum : this.forums) {
      if (forum.getId().equals(forumId))
        return forum;
    }
    return null;
  }

  protected Topic getLastTopic(Category cate, Forum forum) throws Exception {
    Topic topic = null;
    String topicPath = forum.getLastTopicPath();
    if (!ForumUtils.isEmpty(topicPath)) {
      topic = getForumService().getTopicSummary(topicPath);
      if (topic != null) {
        String topicId = topic.getId();
        if (getAncestorOfType(UIForumPortlet.class).checkCanView(cate, forum, topic)) {
          this.MaptopicLast.put(topicId, topic);
        } else {
          if (this.MaptopicLast.containsKey(topicId)) {
            this.MaptopicLast.remove(topicId);
          }
          return null;
        }
      }
    }
    return topic;
  }

  private Topic getTopic(String topicId) throws Exception {
    if (this.MaptopicLast.containsKey(topicId)) {
      return this.MaptopicLast.get(topicId);
    }
    return null;
  }

  private List<Forum> getForumsChecked(boolean isBreak) throws Exception {
    List<UIComponent> children = this.getChildren();
    List<Forum> forums = new ArrayList<Forum>();
    for (UIComponent child : children) {
      if (child instanceof UICheckBoxInput) {
        if (((UICheckBoxInput) child).isChecked()) {
          forums.add(this.getForum(((UICheckBoxInput) child).getName()));
          if(isBreak) break;
        }
      }
    }
    return forums;
  }

  static public class EditCategoryActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      if(uiCategory.getCategory() != null) {
        UICategoryForm categoryForm = uiCategory.openPopup(UICategoryForm.class, "EditCategoryForm", 550, 380);
        categoryForm.setCategoryValue(uiCategory.getCategory(), true);
        uiCategory.isEditCategory = true;
      } else {
        UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.renderForumHome();
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
      }
    }
  }

  static public class DeleteCategoryActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      uiCategory.getForumService().removeCategory(uiCategory.categoryId);
      UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.renderForumHome();
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class AddForumActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      if (uiCategory.getCategory() != null) {
        UIForumForm forumForm = uiCategory.openPopup(UIForumForm.class, "AddNewForumForm", 650, 480);
        forumForm.initForm();
        forumForm.setCategoryValue(uiCategory.categoryId, false);
        forumForm.setForumUpdate(false);
        uiCategory.isEditForum = true;
      } else {
        UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.renderForumHome();
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
      }
    }
  }

  static public class EditForumActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      List<Forum> forums = uiCategory.getForumsChecked(true);
      if (forums.size() > 0) {
        UIForumForm forumForm = uiCategory.openPopup(UIForumForm.class, "EditForumForm", 650, 480);
        forumForm.setMode(false);
        forumForm.initForm();
        forumForm.setCategoryValue(uiCategory.categoryId, false);
        forumForm.setForumValue(forums.get(0), true);
        forumForm.setForumUpdate(false);
        uiCategory.isEditForum = true;
      } else {
        warning("UICategory.msg.notCheck");
        return;
      }
    }
  }

  static public class SetLockedActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      List<Forum> forums = uiCategory.getForumsChecked(false);
      if (forums.size() > 0) {
        try {
          for (Forum forum : forums) {
            if (forum.getIsLock())
              continue;
            forum.setIsLock(true);
            uiCategory.getForumService().modifyForum(forum, Utils.LOCK);
          }
          uiCategory.isEditForum = true;
        } catch (Exception e) {
          warning("UICategory.msg.fail-lock-forum", false);
          event.getSource().log.warn("Failed to lock forums", e);
        }
      } else {
        warning("UICategory.msg.notCheck");
        return;
      }
    }
  }

  static public class SetUnLockActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      List<Forum> forums = uiCategory.getForumsChecked(false);
      if (forums.size() > 0) {
        try {
          for (Forum forum : forums) {
            if (!forum.getIsLock())
              continue;
            forum.setIsLock(false);
            uiCategory.getForumService().modifyForum(forum, Utils.LOCK);
          }
          uiCategory.isEditForum = true;
        } catch (Exception e) {
          warning("UICategory.msg.fail-unlock-forum", false);
          event.getSource().log.warn("Failed to unlock forums", e);
        }
      } else {
        warning("UICategory.msg.notCheck");
        return;
      }
    }
  }

  static public class SetOpenActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      List<Forum> forums = uiCategory.getForumsChecked(false);
      if (forums.size() > 0) {
        try {
          for (Forum forum : forums) {
            forum.setIsClosed(false);
            uiCategory.getForumService().modifyForum(forum, Utils.CLOSE);
          }
          uiCategory.isEditForum = true;
        } catch (Exception e) {
          warning("UICategory.msg.fail-open-forum", false);
          event.getSource().log.warn("Failed to open forums", e);
        }
      } else {
        warning("UICategory.msg.notCheck");
      }
    }
  }

  static public class SetCloseActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      List<Forum> forums = uiCategory.getForumsChecked(false);
      if (forums.size() > 0) {
        try {
          for (Forum forum : forums) {
            forum.setIsClosed(true);
            uiCategory.getForumService().modifyForum(forum, Utils.CLOSE);
          }
          uiCategory.isEditForum = true;
        } catch (Exception e) {
          warning("UICategory.msg.fail-close-forum", false);
          event.getSource().log.warn("Failed to close forums", e);
        }
      } else {
        warning("UICategory.msg.notCheck");
      }
    }
  }

  static public class MoveForumActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      List<Forum> forums = uiCategory.getForumsChecked(false);
      if ((forums.size() > 0)) {
        UIMoveForumForm moveForumForm = uiCategory.openPopup(UIMoveForumForm.class, 315, 365);
        moveForumForm.setListForum(forums, uiCategory.categoryId);
        moveForumForm.setForumUpdate(false);
        uiCategory.isEditForum = true;
      } else {
        warning("UICategory.msg.notCheck");
      }
    }
  }

  static public class RemoveForumActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      List<Forum> forums = uiCategory.getForumsChecked(false);
      if ((forums.size() > 0)) {
        try {
          for (Forum forum : forums) {
            uiCategory.getForumService().removeForum(uiCategory.categoryId, forum.getId());
          }
          uiCategory.getAncestorOfType(UIForumPortlet.class).getChild(UIForumLinks.class).setUpdateForumLinks();
          uiCategory.isEditForum = true;
        } catch (Exception e) {
          warning("UICategory.msg.fail-remove-forum", false);
          event.getSource().log.warn("Failed to remove forums", e);
        }
      } else {
        warning("UICategory.msg.notCheck");
      }
    }
  }

  static public class OpenForumLinkActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String forumId) throws Exception {
      Forum forum = uiCategory.getForumService().getForum(uiCategory.categoryId, forumId);
      if (forum != null) {
        UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.updateIsRendered(ForumUtils.FORUM);
        UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class);
        uiForumContainer.setIsRenderChild(true);
        uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
        UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class);
        uiTopicContainer.setUpdateForum(uiCategory.categoryId, forum, 0);
        forumPortlet.getChild(UIForumLinks.class).setValueOption((uiCategory.categoryId + ForumUtils.SLASH + forumId));
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
      } else {
        uiCategory.isEditForum = true;
        warning("UITopicContainer.msg.forum-deleted", false);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCategory);
      }
    }
  }

  static public class OpenLastTopicLinkActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String Id) throws Exception {
      String[] id = Id.trim().split(ForumUtils.SLASH);
      UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class);
      Topic topic = uiCategory.getForumService().getTopicSummary(uiCategory.categoryId+ForumUtils.SLASH+Id);
      if(topic != null) {
        forumPortlet.updateIsRendered(ForumUtils.FORUM);
        UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class);
        UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class);
        uiForumContainer.setIsRenderChild(false);
        UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class);
        uiForumContainer.getChild(UIForumDescription.class).setForum(uiCategory.getForum(id[0]));
        topic = uiCategory.getForumService().getTopicUpdate(topic, true);
        uiTopicDetail.setUpdateForum(uiCategory.getForum(id[0]));
        uiTopicDetail.initInfoTopic(uiCategory.categoryId, id[0], topic, 0);
        String lastPostId = ForumUtils.EMPTY_STR;
        uiTopicDetail.setLastPostId(lastPostId);
        if (lastPostId == null || lastPostId.length() < 0)
          lastPostId = "lastpost";
        uiTopicDetail.setIdPostView(lastPostId);
        uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(uiCategory.categoryId, id[0], topic.getId());
        forumPortlet.getChild(UIForumLinks.class).setValueOption((uiCategory.categoryId + ForumUtils.SLASH + id[0] + " "));
      } else {
        Object[] args = { ForumUtils.EMPTY_STR };
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty",
                                                                                       args,
                                                                                       ApplicationMessage.WARNING));        
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class OpenLastReadTopicActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, String path) throws Exception {
      WebuiRequestContext context = event.getRequestContext();
      String[] id = path.trim().split(ForumUtils.SLASH);
      Topic topic = uiCategory.getForumService().getTopicSummary(id[0]+ForumUtils.SLASH+id[1]+ForumUtils.SLASH+id[2]);
      UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class);
      if (topic == null) {
        warning("UIForumPortlet.msg.topicEmpty", false);
        forumPortlet.removeCacheUserProfile();
      } else {
        topic = uiCategory.getForumService().getTopicUpdate(topic, true);
        path = topic.getPath();
        Forum forum;
        if (path.indexOf(id[1]) < 0) {
          if (id[id.length - 1].indexOf(Utils.POST) == 0) {
            path = path.substring(path.indexOf(Utils.CATEGORY)) + ForumUtils.SLASH + id[id.length - 1];
          } else {
            path = path.substring(path.indexOf(Utils.CATEGORY));
          }
          id = path.trim().split(ForumUtils.SLASH);
          forum = uiCategory.getForumService().getForum(id[0], id[1]);
          forumPortlet.removeCacheUserProfile();
        } else {
          forum = uiCategory.getForum(id[1]);
        }
        Category category = uiCategory.getCategory();
        if (forumPortlet.checkCanView(category, forum, topic)) {
          forumPortlet.updateIsRendered(ForumUtils.FORUM);
          UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class);
          UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class);
          uiForumContainer.setIsRenderChild(false);
          UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class);
          uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
          uiTopicDetail.setUpdateForum(forum);
          uiTopicDetail.initInfoTopic(id[0], id[1], topic, 0);
          if (id[id.length - 1].indexOf(Utils.POST) == 0) {
            uiTopicDetail.setIdPostView(id[id.length - 1]);
            uiTopicDetail.setLastPostId(id[id.length - 1]);
          } else {
            uiTopicDetail.setIdPostView("lastpost");
          }
          uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1], topic.getId());
          forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0] + ForumUtils.SLASH + id[1] + " "));
          event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
        } else {
          uiCategory.userProfile.addLastPostIdReadOfForum(forum.getId(), ForumUtils.EMPTY_STR);
          uiCategory.getForumService().saveLastPostIdRead(uiCategory.userProfile.getUserId(), uiCategory.userProfile.getLastReadPostOfForum(), uiCategory.userProfile.getLastReadPostOfTopic());
          context.getUIApplication().addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission",
                                                                       null,
                                                                       ApplicationMessage.WARNING));          
        }
      }
      context.addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class SearchFormActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      String path = uiCategory.category.getPath();
      UIFormStringInput formStringInput = uiCategory.getUIStringInput(ForumUtils.SEARCHFORM_ID);
      String text = formStringInput.getValue();
      if (!ForumUtils.isEmpty(text) && !ForumUtils.isEmpty(path)) {
        String special = "\\,.?!`~/][)(;#@$%^&*<>-_+=|:\"'";
        for (int i = 0; i < special.length(); i++) {
          char c = special.charAt(i);
          if (text.indexOf(c) >= 0) {
            warning("UIQuickSearchForm.msg.failure");
            return;
          }
        }
        StringBuffer type = new StringBuffer();
        List<String> forumIdsOfModerator = new ArrayList<String>();
        if (uiCategory.userProfile.getUserRole() == 0) {
          type.append("true,").append(Utils.FORUM).append(ForumUtils.SLASH).append(Utils.TOPIC).append(ForumUtils.SLASH).append(Utils.POST);
        } else {
          type.append("false,").append(Utils.FORUM).append(ForumUtils.SLASH).append(Utils.TOPIC).append(ForumUtils.SLASH).append(Utils.POST);
          if (uiCategory.userProfile.getUserRole() == 1) {
            String[] strings = uiCategory.userProfile.getModerateForums();
            for (int i = 0; i < strings.length; i++) {
              String str = strings[i].substring(strings[i].lastIndexOf(ForumUtils.SLASH) + 1);
              if (str.length() > 0)
                forumIdsOfModerator.add(str);
            }
          }
        }
        UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
        UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class);
        categoryContainer.updateIsRender(true);
        UICategories categories = categoryContainer.getChild(UICategories.class);
        categories.setIsRenderChild(true);
        List<ForumSearch> list = uiCategory.getForumService().getQuickSearch(text, type.toString(), path, uiCategory.userProfile.getUserId(), forumPortlet.getInvisibleCategories(), forumPortlet.getInvisibleForums(), forumIdsOfModerator);
        UIForumListSearch listSearchEvent = categories.getChild(UIForumListSearch.class);
        listSearchEvent.setListSearchEvent(list, uiCategory.category.getId());
        forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL);
        formStringInput.setValue(ForumUtils.EMPTY_STR);
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
      } else {
        warning("UIQuickSearchForm.msg.checkEmpty");
      }
    }
  }

  static public class AddBookMarkActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, String path) throws Exception {
      if (!ForumUtils.isEmpty(path)) {
        int t = path.indexOf("//");
        String type = path.substring(0, t);
        if (type.equals("forum")) {
          path = path.substring(t + 2);
          String forumId = path.substring(path.indexOf(ForumUtils.SLASH) + 1);
          Forum forum = uiCategory.getForum(forumId);
          path = "ForumNormalIcon//" + forum.getForumName() + "//" + forumId;
        } else if (type.equals("category")) {
          path = path.substring(path.indexOf("//") + 2);
          Category category = uiCategory.getCategory();
          path = "CategoryNormalIcon//" + category.getCategoryName() + "//" + path;
        } else {
          path = path.substring(t + 2);
          String topicId = path.substring(path.lastIndexOf(ForumUtils.SLASH) + 1);
          Topic topic = uiCategory.getTopic(topicId);
          path = "ThreadNoNewPost//" + topic.getTopicName() + "//" + topicId;
        }
        String userName = uiCategory.userProfile.getUserId();
        uiCategory.getForumService().saveUserBookmark(userName, path, true);
      }
    }
  }

  static public class AddWatchingActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiform, final String path) throws Exception {
      isUnWatch = !uiform.addWatch(path, uiform.userProfile);
      if(!isUnWatch) uiform.isEditCategory = true;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiform);
    }
  }

  static public class UnWatchActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String path) throws Exception {
      if(uiCategory.unWatch(path, uiCategory.userProfile)) {
        isUnWatch = true;
        uiCategory.isEditCategory = true;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCategory);
    }
  }

  static public class AdvancedSearchActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.updateIsRendered(ForumUtils.FIELD_SEARCHFORUM_LABEL);
      forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL);
      UISearchForm searchForm = forumPortlet.getChild(UISearchForm.class);
      searchForm.setUserProfile(forumPortlet.getUserProfile());
      searchForm.setPath(uiCategory.category.getPath());
      searchForm.setSelectType(Utils.FORUM);
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class ExportCategoryActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String path) throws Exception {
      uiCategory.isEditCategory = true;
      Category category = uiCategory.getCategory();
      if (category == null) {
        UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.renderForumHome();
        warning("UIForumPortlet.msg.catagory-deleted", false);
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
        return;
      }
      uiCategory.isEditForum = true;
      if (uiCategory.getForumList().isEmpty()) {
        warning("UICategory.msg.emptyCategoryExport");
        return;
      }
      UIExportForm exportForm = uiCategory.openPopup(UIExportForm.class, 450, 300);
      exportForm.setObjectId(category);
    }
  }

  static public class ImportForumActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String path) throws Exception {
      Category cate = uiCategory.getCategory();
      if (cate == null) {
        UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.renderForumHome();
        warning("UITopicContainer.msg.forum-deleted", false);
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
        return;
      }
      UIImportForm importForm = uiCategory.openPopup(UIImportForm.class, 450, 160);
      importForm.setPath(cate.getPath());
    }
  }

  static public class WatchOptionActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
      Category category = uiCategory.refreshCategory();
      UIWatchToolsForm watchToolsForm = uiCategory.openPopup(UIWatchToolsForm.class, 500, 365);
      watchToolsForm.setPath(category.getPath());
      watchToolsForm.setEmails(category.getEmailNotification());
    }
  }

  static public class RSSActionListener extends BaseEventListener<UICategory> {
    public void onEvent(Event<UICategory> event, UICategory uiCategory, final String cateId) throws Exception {
      String userId = uiCategory.getUserProfile().getUserId();
      if (!userId.equals(UserProfile.USER_GUEST)) {
        uiCategory.getForumService().addWatch(-1, cateId, null, userId);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCategory);
      }
    }
  }
}
