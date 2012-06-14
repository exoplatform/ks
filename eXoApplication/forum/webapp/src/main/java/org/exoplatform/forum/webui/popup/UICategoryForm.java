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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIGroupSelector;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.UISelector;
import org.exoplatform.ks.common.webui.UIUserSelect;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfigs ( {
        @ComponentConfig(
            lifecycle = UIFormLifecycle.class,
            template = "app:/templates/forum/webui/popup/UICategoryForm.gtmpl",
            events = {
              @EventConfig(listeners = UICategoryForm.SaveActionListener.class), 
              @EventConfig(listeners = UICategoryForm.AddPrivateActionListener.class, phase=Phase.DECODE),
              @EventConfig(listeners = UICategoryForm.CancelActionListener.class, phase=Phase.DECODE),
              @EventConfig(listeners = UICategoryForm.AddValuesUserActionListener.class, phase=Phase.DECODE),
              @EventConfig(listeners = UICategoryForm.SelectTabActionListener.class, phase=Phase.DECODE)
            }
        )
      ,
        @ComponentConfig(
             id = "UICategoryUserPopupWindow",
             type = UIPopupWindow.class,
             template = "system:/groovy/webui/core/UIPopupWindow.gtmpl",
             events = {
               @EventConfig(listeners = UICategoryForm.ClosePopupActionListener.class, name = "ClosePopup")  ,
               @EventConfig(listeners = UICategoryForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
               @EventConfig(listeners = UICategoryForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
             }
        )
    }
)
public class UICategoryForm extends BaseForumForm implements UIPopupComponent, UISelector {
  public static final String CATEGORY_DETAIL_TAB          = "DetailTab";

  public static final String CATEGORY_PERMISSION_TAB      = "PermissionTab";

  public static final String FIELD_CATEGORYTITLE_INPUT    = "CategoryTitle";

  public static final String FIELD_CATEGORYORDER_INPUT    = "CategoryOrder";

  public static final String FIELD_DESCRIPTION_INPUT      = "Description";

  public static final String FIELD_USERPRIVATE_MULTIVALUE = "UserPrivate";

  public static final String FIELD_MODERAROR_MULTIVALUE   = "moderators";

  public static final String FIELD_VIEWER_MULTIVALUE      = "Viewer";

  public static final String FIELD_POSTABLE_MULTIVALUE    = "Postable";

  public static final String FIELD_TOPICABLE_MULTIVALUE   = "Topicable";

  public static final String USER_SELECTOR_POPUPWINDOW    = "UICategoryUserPopupWindow";

  private String             categoryId                   = ForumUtils.EMPTY_STR;

  private int                id                           = 0;

  private boolean            isDoubleClickSubmit          = false;

  public UICategoryForm() throws Exception {
    isDoubleClickSubmit = false;
    UIFormInputWithActions detailTab = new UIFormInputWithActions(CATEGORY_DETAIL_TAB);
    UIFormInputWithActions permissionTab = new UIFormInputWithActions(CATEGORY_PERMISSION_TAB);

    UIFormStringInput categoryTitle = new UIFormStringInput(FIELD_CATEGORYTITLE_INPUT, FIELD_CATEGORYTITLE_INPUT, null);
    categoryTitle.addValidator(MandatoryValidator.class);
    UIFormStringInput categoryOrder = new UIFormStringInput(FIELD_CATEGORYORDER_INPUT, FIELD_CATEGORYORDER_INPUT, "0");
    categoryOrder.addValidator(PositiveNumberFormatValidator.class);
    UIFormTextAreaInput description = new UIFormTextAreaInput(FIELD_DESCRIPTION_INPUT, FIELD_DESCRIPTION_INPUT, null);

    UIFormTextAreaInput userPrivate = new UIFormTextAreaInput(FIELD_USERPRIVATE_MULTIVALUE, FIELD_USERPRIVATE_MULTIVALUE, null);

    UIFormTextAreaInput moderators = new UIFormTextAreaInput(FIELD_MODERAROR_MULTIVALUE, FIELD_MODERAROR_MULTIVALUE, null);
    UIFormTextAreaInput viewer = new UIFormTextAreaInput(FIELD_VIEWER_MULTIVALUE, FIELD_VIEWER_MULTIVALUE, null);
    UIFormTextAreaInput postable = new UIFormTextAreaInput(FIELD_POSTABLE_MULTIVALUE, FIELD_POSTABLE_MULTIVALUE, null);
    UIFormTextAreaInput topicable = new UIFormTextAreaInput(FIELD_TOPICABLE_MULTIVALUE, FIELD_TOPICABLE_MULTIVALUE, null);

    detailTab.addUIFormInput(categoryTitle);
    detailTab.addUIFormInput(categoryOrder);
    detailTab.addUIFormInput(userPrivate);
    detailTab.addUIFormInput(description);

    permissionTab.addUIFormInput(moderators);
    permissionTab.addUIFormInput(topicable);
    permissionTab.addUIFormInput(postable);
    permissionTab.addUIFormInput(viewer);

    String[] strings = new String[] { "SelectUser", "SelectMemberShip", "SelectGroup" };
    List<ActionData> actions = new ArrayList<ActionData>();

    ActionData ad;
    int i = 0;
    for (String string : strings) {
      ad = new ActionData();
      if (i == 0)
        ad.setActionListener("AddValuesUser");
      else
        ad.setActionListener("AddPrivate");
      ad.setActionParameter(String.valueOf(i) + ForumUtils.COMMA + FIELD_USERPRIVATE_MULTIVALUE);
      ad.setCssIconClass(string + "Icon");
      ad.setActionName(string);
      actions.add(ad);
      ++i;
    }
    detailTab.setActionField(FIELD_USERPRIVATE_MULTIVALUE, actions);
    for (int j = 0; j < getChildIds().length; j++) {
      String field = getChildIds()[j];
      actions = new ArrayList<ActionData>();
      i = 0;
      for (String string : strings) {
        ad = new ActionData();
        if (i == 0) {
          ad.setActionListener("AddValuesUser");
        } else {
          ad.setActionListener("AddPrivate");
        }
        ad.setActionParameter(String.valueOf(i) + ForumUtils.COMMA + field);
        ad.setCssIconClass(string + "Icon");
        ad.setActionName(string);
        actions.add(ad);
        ++i;
      }
      permissionTab.setActionField(field, actions);
    }

    addUIFormInput(detailTab);
    addUIFormInput(permissionTab);
    this.setActions(new String[] { "Save", "Cancel" });
  }

  protected boolean getIsSelected(int id) {
    if (this.id == id)
      return true;
    return false;
  }

  private String[] getChildIds() {
    return new String[] { FIELD_MODERAROR_MULTIVALUE, FIELD_TOPICABLE_MULTIVALUE, FIELD_POSTABLE_MULTIVALUE, FIELD_VIEWER_MULTIVALUE };
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void setCategoryValue(Category category, boolean isUpdate) throws Exception {
    if (isUpdate) {
      this.categoryId = category.getId();
      getUIStringInput(FIELD_CATEGORYTITLE_INPUT).setValue(CommonUtils.decodeSpecialCharToHTMLnumber(category.getCategoryName()));
      getUIStringInput(FIELD_CATEGORYORDER_INPUT).setValue(Long.toString(category.getCategoryOrder()));
      getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).setDefaultValue(CommonUtils.decodeSpecialCharToHTMLnumber(category.getDescription()));
      String userPrivate = ForumUtils.unSplitForForum(category.getUserPrivate());
      String moderator = ForumUtils.unSplitForForum(category.getModerators());
      String topicAble = ForumUtils.unSplitForForum(category.getCreateTopicRole());
      String poster = ForumUtils.unSplitForForum(category.getPoster());
      String viewer = ForumUtils.unSplitForForum(category.getViewer());
      getUIFormTextAreaInput(FIELD_USERPRIVATE_MULTIVALUE).setValue(userPrivate);
      getUIFormTextAreaInput(FIELD_MODERAROR_MULTIVALUE).setValue(moderator);
      getUIFormTextAreaInput(FIELD_TOPICABLE_MULTIVALUE).setValue(topicAble);
      getUIFormTextAreaInput(FIELD_POSTABLE_MULTIVALUE).setValue(poster);
      getUIFormTextAreaInput(FIELD_VIEWER_MULTIVALUE).setValue(viewer);
    }
  }

  public void updateSelect(String selectField, String value) throws Exception {
    UIFormTextAreaInput fieldInput = getUIFormTextAreaInput(selectField);
    String values = fieldInput.getValue();
    fieldInput.setValue(ForumUtils.updateMultiValues(value, values));
  }

  static public class SaveActionListener extends BaseEventListener<UICategoryForm> {
    public void onEvent(Event<UICategoryForm> event, UICategoryForm uiForm, String objectId) throws Exception {
      if (uiForm.isDoubleClickSubmit)
        return;
      String categoryTitle = uiForm.getUIStringInput(FIELD_CATEGORYTITLE_INPUT).getValue();
      int maxText = ForumUtils.MAXTITLE;
      if (categoryTitle.length() > maxText) {
        warning("NameValidator.msg.warning-long-text", new String[] { uiForm.getLabel(FIELD_CATEGORYTITLE_INPUT), String.valueOf(maxText) });
        return;
      }
      categoryTitle = CommonUtils.encodeSpecialCharInTitle(categoryTitle);
      String description = uiForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).getValue();
      if (!ForumUtils.isEmpty(description) && description.length() > maxText) {
        warning("NameValidator.msg.warning-long-text", new String[] { uiForm.getLabel(FIELD_DESCRIPTION_INPUT), String.valueOf(maxText) });
        return;
      }
      description = CommonUtils.encodeSpecialCharInTitle(description);
      String categoryOrder = uiForm.getUIStringInput(FIELD_CATEGORYORDER_INPUT).getValue();
      if (ForumUtils.isEmpty(categoryOrder))
        categoryOrder = "0";
      categoryOrder = ForumUtils.removeZeroFirstNumber(categoryOrder);
      if (categoryOrder.length() > 3) {
        warning("NameValidator.msg.erro-large-number", new String[] { uiForm.getLabel(FIELD_CATEGORYORDER_INPUT) });
        return;
      }
      String moderator = uiForm.getUIFormTextAreaInput(FIELD_MODERAROR_MULTIVALUE).getValue();
      moderator = ForumUtils.removeSpaceInString(moderator);
      moderator = ForumUtils.removeStringResemble(moderator);
      String[] moderators = ForumUtils.splitForForum(moderator);
      if (!ForumUtils.isEmpty(moderator)) {
        String erroUser = UserHelper.checkValueUser(moderator);
        if (!ForumUtils.isEmpty(erroUser)) {
          warning("NameValidator.msg.erroUser-input", new String[] { uiForm.getLabel(FIELD_MODERAROR_MULTIVALUE), erroUser });
          return;
        }
      } else {
        moderators = new String[] { "" };
      }

      String userPrivate = uiForm.getUIFormTextAreaInput(FIELD_USERPRIVATE_MULTIVALUE).getValue();
      if (!ForumUtils.isEmpty(userPrivate) && !ForumUtils.isEmpty(moderator)) {
        userPrivate = userPrivate + ForumUtils.COMMA + moderator;
      }
      userPrivate = ForumUtils.removeSpaceInString(userPrivate);
      userPrivate = ForumUtils.removeStringResemble(userPrivate);
      String[] userPrivates = ForumUtils.splitForForum(userPrivate);
      if (!ForumUtils.isEmpty(userPrivate)) {
        String erroUser = UserHelper.checkValueUser(userPrivate);
        if (!ForumUtils.isEmpty(erroUser)) {
          warning("NameValidator.msg.erroUser-input", new String[] { uiForm.getLabel(FIELD_USERPRIVATE_MULTIVALUE), erroUser });
          return;
        }
      } else {
        userPrivates = new String[] { "" };
      }

      UIFormInputWithActions catPermission = uiForm.getChildById(CATEGORY_PERMISSION_TAB);
      String topicable = catPermission.getUIFormTextAreaInput(FIELD_TOPICABLE_MULTIVALUE).getValue();
      String postable = catPermission.getUIFormTextAreaInput(FIELD_POSTABLE_MULTIVALUE).getValue();
      String viewer = catPermission.getUIFormTextAreaInput(FIELD_VIEWER_MULTIVALUE).getValue();

      topicable = ForumUtils.removeSpaceInString(topicable);
      postable = ForumUtils.removeSpaceInString(postable);
      viewer = ForumUtils.removeSpaceInString(viewer);

      String erroUser = UserHelper.checkValueUser(topicable);
      erroUser = UserHelper.checkValueUser(topicable);
      if (!ForumUtils.isEmpty(erroUser)) {
        warning("NameValidator.msg.erroUser-input", new String[] { uiForm.getLabel(FIELD_TOPICABLE_MULTIVALUE), erroUser });
        return;
      }
      erroUser = UserHelper.checkValueUser(postable);
      if (!ForumUtils.isEmpty(erroUser)) {
        warning("NameValidator.msg.erroUser-input", new String[] { uiForm.getLabel(FIELD_POSTABLE_MULTIVALUE), erroUser });
        return;
      }
      erroUser = UserHelper.checkValueUser(viewer);
      if (!ForumUtils.isEmpty(erroUser)) {
        warning("NameValidator.msg.erroUser-input", new String[] { uiForm.getLabel(FIELD_VIEWER_MULTIVALUE), erroUser });
        return;
      }

      String[] setTopicable = ForumUtils.splitForForum(topicable);
      String[] setPostable = ForumUtils.splitForForum(postable);
      String[] setViewer = ForumUtils.splitForForum(viewer);

      String userName = uiForm.getUserProfile().getUserId();
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      boolean isNew = true;
      Category cat = new Category();
      if (!ForumUtils.isEmpty(uiForm.categoryId)) {
        cat = uiForm.getForumService().getCategory(uiForm.categoryId);
        if(cat == null) {
          warning("UIForumPortlet.msg.catagory-deleted", false);
          forumPortlet.cancelAction();
          forumPortlet.renderForumHome();
          event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
          return;
        }
        isNew = false;
      }
      cat.setOwner(userName);
      cat.setCategoryName(categoryTitle.trim());
      cat.setCategoryOrder(Long.parseLong(categoryOrder));
      cat.setCreatedDate(new Date());
      cat.setDescription(description);
      cat.setModifiedBy(userName);
      cat.setModifiedDate(new Date());
      cat.setUserPrivate(userPrivates);
      cat.setModerators(moderators);
      cat.setCreateTopicRole(setTopicable);
      cat.setPoster(setPostable);
      cat.setViewer(setViewer);

      UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class);
      try {
        uiForm.getForumService().saveCategory(cat, isNew);
        List<String> invisibleCategories = forumPortlet.getInvisibleCategories();
        if (!invisibleCategories.isEmpty()) {
          List<String> invisibleForums = forumPortlet.getInvisibleForums();
          invisibleCategories.add(cat.getId());
          String listForumId = UICategoryForm.listToString(invisibleForums);
          String listCategoryId = UICategoryForm.listToString(invisibleCategories);
          ForumUtils.savePortletPreference(listCategoryId, listForumId);
          forumPortlet.loadPreferences();
        }
        UICategory uiCategory = categoryContainer.getChild(UICategory.class);
        uiCategory.setIsEditForum(true);
        uiCategory.updateByBreadcumbs(cat.getId());
        categoryContainer.updateIsRender(false);
        forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
        forumPortlet.findFirstComponentOfType(UIBreadcumbs.class).setUpdataPath(cat.getId());
        UIForumLinks forumLinks = forumPortlet.getChild(UIForumLinks.class);
        forumLinks.setUpdateForumLinks();
        forumLinks.setValueOption(cat.getId());
      } catch (Exception e) {
        warning("UIForumPortlet.msg.catagory-deleted", false);
        forumPortlet.renderForumHome();
      }
      forumPortlet.cancelAction();
      uiForm.isDoubleClickSubmit = true;
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public String listToString(List<String> list) {
    if (list == null) return ForumUtils.EMPTY_STR;
    String s = list.toString().substring(1);
    return s.substring(0, s.length() - 1).replaceAll(Utils.SPACE, ForumUtils.EMPTY_STR);
  }
  
  static public class SelectTabActionListener extends BaseEventListener<UICategoryForm> {
    public void onEvent(Event<UICategoryForm> event, UICategoryForm uiForm, String id) throws Exception {
      uiForm.id = Integer.parseInt(id);
      UIPopupWindow popupWindow = uiForm.getAncestorOfType(UIPopupWindow.class);
      if (uiForm.id == 1) {
        popupWindow.setWindowSize(550, 440);
      } else {
        popupWindow.setWindowSize(550, 380);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class AddPrivateActionListener extends BaseEventListener<UICategoryForm> {
    public void onEvent(Event<UICategoryForm> event, UICategoryForm categoryForm, String objectId) throws Exception {
      ;
      String[] objects = objectId.split(ForumUtils.COMMA);
      String type = objects[0];
      String param = objects[1];
      UIPopupContainer popupContainer = categoryForm.getAncestorOfType(UIPopupContainer.class);
      UIUserSelect uiUserSelect = popupContainer.findFirstComponentOfType(UIUserSelect.class);
      if (uiUserSelect != null) {
        UIPopupWindow popupWindow = uiUserSelect.getParent();
        closePopupWindow(popupWindow);
      }
      UIGroupSelector uiGroupSelector = null;
      if (type.equals(UIGroupSelector.TYPE_MEMBERSHIP)) {
        uiGroupSelector = openPopup(popupContainer, UIGroupSelector.class, "UIMemberShipSelector", 600, 0);
      } else if (type.equals(UIGroupSelector.TYPE_GROUP)) {
        uiGroupSelector = openPopup(popupContainer, UIGroupSelector.class, "GroupSelector", 600, 0);
      }
      uiGroupSelector.getAncestorOfType(UIPopupWindow.class).setRendered(true);
      uiGroupSelector.setType(type);
      uiGroupSelector.setSpaceGroupId(categoryForm.getAncestorOfType(UIForumPortlet.class).getSpaceGroupId());
      uiGroupSelector.setComponent(categoryForm, new String[] { param });
      uiGroupSelector.getChild(UITree.class).setId(UIGroupSelector.TREE_GROUP_ID);
      uiGroupSelector.getChild(org.exoplatform.webui.core.UIBreadcumbs.class).setId(UIGroupSelector.BREADCUMB_GROUP_ID);
    }
  }

  static public class CancelActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIForumPortlet.class).cancelAction();
    }
  }

  static public class CloseActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupWindow popupWindow = uiUserSelector.getParent();
      closePopupWindow(popupWindow);
    }
  }

  static public class ClosePopupActionListener extends EventListener<UIPopupWindow> {
    public void execute(Event<UIPopupWindow> event) throws Exception {
      UIPopupWindow popupWindow = event.getSource();
      closePopupWindow(popupWindow);
    }
  }

  private void setValueField(UIFormInputWithActions withActions, String field, String values) throws Exception {
    try {
      UIFormTextAreaInput textArea = withActions.getUIFormTextAreaInput(field);
      String vls = textArea.getValue();
      if (!ForumUtils.isEmpty(vls)) {
        values = values + ForumUtils.COMMA + vls;
        values = ForumUtils.removeStringResemble(values.replaceAll(",,", ForumUtils.COMMA));
      }
      textArea.setValue(values);
    } catch (Exception e) {
      log.debug("Set Value into field " + field + " is fall.", e);
    }
  }

  static public class AddActionListener extends EventListener<UIUserSelect> {
    public void execute(Event<UIUserSelect> event) throws Exception {
      UIUserSelect uiUserSelector = event.getSource();
      String values = uiUserSelector.getSelectedUsers();
      UIForumPortlet forumPortlet = uiUserSelector.getAncestorOfType(UIForumPortlet.class);
      UICategoryForm categoryForm = forumPortlet.findFirstComponentOfType(UICategoryForm.class);
      UIPopupWindow popupWindow = uiUserSelector.getParent();
      String id = uiUserSelector.getPermisionType();
      if (id.equals(FIELD_USERPRIVATE_MULTIVALUE)) {
        UIFormInputWithActions catDetail = categoryForm.getChildById(CATEGORY_DETAIL_TAB);
        categoryForm.setValueField(catDetail, FIELD_USERPRIVATE_MULTIVALUE, values);
      } else {
        UIFormInputWithActions catPermission = categoryForm.getChildById(CATEGORY_PERMISSION_TAB);
        String[] array = categoryForm.getChildIds();
        for (int i = 0; i < array.length; i++) {
          if (id.equals(array[i])) {
            categoryForm.setValueField(catPermission, array[i], values);
            break;
          }
        }
      }
      closePopupWindow(popupWindow);
      event.getRequestContext().addUIComponentToUpdateByAjax(categoryForm);
    }
  }

  static public class AddValuesUserActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
      UICategoryForm categoryForm = event.getSource();
      String id = event.getRequestContext().getRequestParameter(OBJECTID).replace("0,", ForumUtils.EMPTY_STR);
      UIPopupContainer uiPopupContainer = categoryForm.getAncestorOfType(UIPopupContainer.class);
      categoryForm.showUIUserSelect(uiPopupContainer, USER_SELECTOR_POPUPWINDOW, id);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
    }
  }
}
