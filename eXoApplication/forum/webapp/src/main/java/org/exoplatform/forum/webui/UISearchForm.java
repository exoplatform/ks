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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.UISelector;
import org.exoplatform.ks.common.webui.UIUserSelect;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfigs ( {
        @ComponentConfig(
            lifecycle = UIFormLifecycle.class,
            template = "app:/templates/forum/webui/popup/UISearchForm.gtmpl",
            events = {
              @EventConfig(listeners = UISearchForm.SearchActionListener.class),  
              @EventConfig(listeners = UISearchForm.OnchangeActionListener.class, phase = Phase.DECODE),  
              @EventConfig(listeners = UISearchForm.ResetFieldActionListener.class, phase = Phase.DECODE),  
              @EventConfig(listeners = UISearchForm.AddValuesUserActionListener.class, phase = Phase.DECODE),  
              @EventConfig(listeners = UISearchForm.CancelActionListener.class, phase = Phase.DECODE)      
            }
        )
      ,
        @ComponentConfig(
             id = "UIUserSearchPopupWindow",
             type = UIPopupWindow.class,
             template = "system:/groovy/webui/core/UIPopupWindow.gtmpl",
             events = {
               @EventConfig(listeners = UISearchForm.ClosePopupActionListener.class, name = "ClosePopup")  ,
               @EventConfig(listeners = UISearchForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
               @EventConfig(listeners = UISearchForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
             }
        )
    }
)
public class UISearchForm extends BaseForumForm implements UISelector {

  final static private String FIELD_SEARCHVALUE_INPUT     = "SearchValue";

  final static private String FIELD_SCOPE_RADIOBOX        = "Scope";

  final static private String FIELD_SEARCHUSER_INPUT      = "SearchUser";

  final static private String FIELD_SEARCHTYPE_SELECTBOX  = "SearchType";

  final static private String FIELD_TOPICTYPE_SELECTBOX   = "TopicType";

  final static private String FIELD_TOPICCOUNTMIN_SLIDER  = "TopicCountMax";

  final static private String FIELD_POSTCOUNTMIN_SLIDER   = "PostCountMax";

  final static private String FIELD_VIEWCOUNTMIN_SLIDER   = "ViewCountMax";

  final static private String FIELD_ISLOCK_CHECKBOX       = "IsLock";

  final static private String FIELD_ISUNLOCK_CHECKBOX     = "IsUnLock";

  final static private String FIELD_ISCLOSED_CHECKBOX     = "IsClosed";

  final static private String FIELD_ISOPEN_CHECKBOX       = "IsOpen";

  final static private String FIELD_MODERATOR_INPUT       = "Moderator";

  final static private String FROMDATECREATED             = "FromDateCreated";

  final static private String TODATECREATED               = "ToDateCreated";

  final static private String FROMDATECREATEDLASTPOST     = "FromDateCreatedLastPost";

  final static private String TODATECREATEDLASTPOST       = "ToDateCreatedLastPost";

  private boolean             isSearchForum               = false;

  private boolean             isSearchCate                = false;

  private boolean             isSearchTopic               = false;

  private String              path                        = ForumUtils.EMPTY_STR;

  private final static String USER_SEARCH_POPUP_WINDOW_ID = "UIUserSearchPopupWindow";

  private List<TopicType>     listTT                      = new ArrayList<TopicType>();

  private Locale              locale;

  public UISearchForm() throws Exception {
    if (this.getId() == null)
      setId("UISearchForm");
    UIFormStringInput searchValue = new UIFormStringInput(FIELD_SEARCHVALUE_INPUT, FIELD_SEARCHVALUE_INPUT, null);
    UIFormStringInput searchUser = new UIFormStringInput(FIELD_SEARCHUSER_INPUT, FIELD_SEARCHUSER_INPUT, null);
    List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>();
    list.add(new SelectItemOption<String>(getLabel("Category"), Utils.CATEGORY));
    list.add(new SelectItemOption<String>(getLabel("Forum"), Utils.FORUM));
    list.add(new SelectItemOption<String>(getLabel("Topic"), Utils.TOPIC));
    list.add(new SelectItemOption<String>(getLabel("Post"), Utils.POST));
    UIFormSelectBox searchType = new UIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX, FIELD_SEARCHTYPE_SELECTBOX, list);
    searchType.setOnChange("Onchange");

    UIFormSelectBox topicType = new UIFormSelectBox(FIELD_TOPICTYPE_SELECTBOX, FIELD_TOPICTYPE_SELECTBOX, null);
    UIFormRadioBoxInput boxInput = new UIFormRadioBoxInput(FIELD_SCOPE_RADIOBOX, FIELD_SCOPE_RADIOBOX, null);

    UICheckBoxInput isLock = new UICheckBoxInput(FIELD_ISLOCK_CHECKBOX, FIELD_ISLOCK_CHECKBOX, false);
    UICheckBoxInput isUnLock = new UICheckBoxInput(FIELD_ISUNLOCK_CHECKBOX, FIELD_ISUNLOCK_CHECKBOX, false);
    UICheckBoxInput isClosed = new UICheckBoxInput(FIELD_ISCLOSED_CHECKBOX, FIELD_ISCLOSED_CHECKBOX, false);
    UICheckBoxInput isOpent = new UICheckBoxInput(FIELD_ISOPEN_CHECKBOX, FIELD_ISOPEN_CHECKBOX, false);
    UIFormDateTimePicker FromDateCreated = new UIFormDateTimePicker(FROMDATECREATED, FROMDATECREATED, null, false);
    UIFormDateTimePicker ToDateCreated = new UIFormDateTimePicker(TODATECREATED, TODATECREATED, null, false);
    UIFormDateTimePicker FromDateCreatedLastPost = new UIFormDateTimePicker(FROMDATECREATEDLASTPOST, FROMDATECREATEDLASTPOST, null, false);
    UIFormDateTimePicker ToDateCreatedLastPost = new UIFormDateTimePicker(TODATECREATEDLASTPOST, TODATECREATEDLASTPOST, null, false);

    UISliderControl topicCountMin = new UISliderControl(FIELD_TOPICCOUNTMIN_SLIDER, FIELD_TOPICCOUNTMIN_SLIDER, "0");// Sliders
    UISliderControl postCountMin = new UISliderControl(FIELD_POSTCOUNTMIN_SLIDER, FIELD_POSTCOUNTMIN_SLIDER, "0");
    UISliderControl viewCountMin = new UISliderControl(FIELD_VIEWCOUNTMIN_SLIDER, FIELD_VIEWCOUNTMIN_SLIDER, "0");

    UIFormStringInput moderator = new UIFormStringInput(FIELD_MODERATOR_INPUT, FIELD_MODERATOR_INPUT, null);

    addUIFormInput(searchValue);
    addUIFormInput(searchType);
    addUIFormInput(topicType);
    addUIFormInput(boxInput);
    addUIFormInput(searchUser);
    addUIFormInput(isLock);
    addUIFormInput(isUnLock);
    addUIFormInput(isClosed);
    addUIFormInput(isOpent);
    addUIFormInput(FromDateCreated);
    addUIFormInput(ToDateCreated);

    addUIFormInput(FromDateCreatedLastPost);
    addUIFormInput(ToDateCreatedLastPost);
    addUIFormInput(topicCountMin);
    addUIFormInput(postCountMin);
    addUIFormInput(viewCountMin);
    addUIFormInput(moderator);
    setActions(new String[] { "Search", "ResetField", "Cancel" });
  }

  protected void setLocale() throws Exception {
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    Locale locale = portalContext.getLocale();
    if (this.locale == null || !locale.getLanguage().equals(this.locale.getLanguage())) {
      initDefaultContent();
      this.locale = locale;
    }
  }

  public void initDefaultContent() throws Exception {
    getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).setOptions(optionsType(0));

    List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>();
    list.add(new SelectItemOption<String>(getLabel("Full"), ForumEventQuery.VALUE_IN_ENTIRE));
    list.add(new SelectItemOption<String>(getLabel("Titles"), ForumEventQuery.VALUE_IN_TITLE));
    UIFormRadioBoxInput boxInput = this.getUIFormRadioBoxInput(FIELD_SCOPE_RADIOBOX).setOptions(list);
    boxInput.setValue(ForumEventQuery.VALUE_IN_ENTIRE);

    list = new ArrayList<SelectItemOption<String>>();
    list.add(new SelectItemOption<String>(getLabel("All"), "all"));
    for (TopicType topicType : listTT) {
      list.add(new SelectItemOption<String>(topicType.getName(), topicType.getId()));
    }
    UIFormSelectBox topicType = this.getUIFormSelectBox(FIELD_TOPICTYPE_SELECTBOX).setOptions(list);
    topicType.setValue(TopicType.DEFAULT_ID);

    if (userProfile == null)
      setUserProfile(null);
    String showCalendar = getLabel("ShowCalendar");
    getUIFormDateTimePicker(FROMDATECREATED).setInfo(showCalendar, locale, userProfile.getShortDateFormat(), userProfile.getTimeFormat());
    getUIFormDateTimePicker(TODATECREATED).setInfo(showCalendar, locale, userProfile.getShortDateFormat(), userProfile.getTimeFormat());
    getUIFormDateTimePicker(FROMDATECREATEDLASTPOST).setInfo(showCalendar, locale, userProfile.getShortDateFormat(), userProfile.getTimeFormat());
    getUIFormDateTimePicker(TODATECREATEDLASTPOST).setInfo(showCalendar, locale, userProfile.getShortDateFormat(), userProfile.getTimeFormat());
  }

  public boolean getIsSearchCate() {
    return isSearchCate;
  }

  public boolean getIsSearchForum() {
    return isSearchForum;
  }

  public void setIsSearchForum(boolean isSearchForum) {
    this.isSearchForum = isSearchForum;
  }

  public boolean getIsSearchTopic() {
    return isSearchTopic;
  }

  public void setIsSearchTopic(boolean isSearchTopic) {
    this.isSearchTopic = isSearchTopic;
  }

  public void setPath(String path) {
    this.path = path;
    if(ForumUtils.isEmpty(path)) setOptionsType(0);
    else if(path.indexOf(Utils.TOPIC) > 0) setOptionsType(3);
    else if(path.indexOf(Utils.CATEGORY) > 0) setOptionsType(1);
    else if(path.indexOf(Utils.FORUM) > 0) setOptionsType(2);
  }
  
  private  List<SelectItemOption<String>> optionsType(int type) {
    List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>();
    if(type == 0)list.add(new SelectItemOption<String>(getLabel("Category"), Utils.CATEGORY));
    if(type <= 1)list.add(new SelectItemOption<String>(getLabel("Forum"), Utils.FORUM));
    if(type <= 2)list.add(new SelectItemOption<String>(getLabel("Topic"), Utils.TOPIC));
    if(type <= 3)list.add(new SelectItemOption<String>(getLabel("Post"), Utils.POST));
    return list;
  }
  
  private void setOptionsType(int type) {
    getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).setOptions(optionsType(type));
  }

  private void setTopicType() {
    listTT.clear();
    listTT.addAll(getForumService().getTopicTypes());
  }

  public void setUserProfile(UserProfile userProfile) {
    this.userProfile = (userProfile != null) ? userProfile : this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
  }

  private boolean getIsMod() {
    if (this.userProfile != null) {
      if (this.userProfile.getUserRole() < 2)
        return true;
    }
    return false;
  }

  public void setSelectType(String type){
    this.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).setValue(type);
    if (type.equals(Utils.FORUM)) {
      this.isSearchForum = true;
      this.isSearchTopic = false;
      this.isSearchCate = false;
    } else if (type.equals(Utils.TOPIC)) {
      this.isSearchCate = false;
      this.isSearchForum = false;
      this.isSearchTopic = true;
      this.setTopicType();
      List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>();
      list.add(new SelectItemOption<String>(this.getLabel("All"), "all"));
      for (TopicType topicType : this.listTT) {
        list.add(new SelectItemOption<String>(topicType.getName(), topicType.getId()));
      }
      this.getUIFormSelectBox(FIELD_TOPICTYPE_SELECTBOX).setOptions(list);
    } else if (type.equals(Utils.CATEGORY)) {
      this.isSearchCate = true;
      this.isSearchForum = false;
      this.isSearchTopic = false;
    } else {
      this.isSearchCate = false;
      this.isSearchForum = false;
      this.isSearchTopic = false;
    }
    this.getAncestorOfType(UIForumPortlet.class).getChild(UIForumLinks.class).setValueOption(ForumUtils.EMPTY_STR);
  }

  public UIFormRadioBoxInput getUIFormRadioBoxInput(String name) {
    return (UIFormRadioBoxInput) findComponentById(name);
  }

  public String getLabel(ResourceBundle res, String id) throws Exception {
    String label = getId() + ".label." + id;
    try {
      return res.getString(label);
    } catch (Exception e) {
      return id;
    }
  }

  private String checkValue(String input) throws Exception {
    if (!ForumUtils.isEmpty(input)) {
      try {
        Integer.parseInt(input.trim());
        return input.trim();
      } catch (NumberFormatException e) {
        return null;
      }
    } else
      return null;
  }

  public void updateSelect(String selectField, String value) throws Exception {
    UIFormStringInput fieldInput = getUIStringInput(selectField);
    String values = fieldInput.getValue();
    fieldInput.setValue(ForumUtils.updateMultiValues(value, values));
  }

  private Calendar getCalendar(UIFormDateTimePicker dateTimeInput, String faled) throws Exception {
    Calendar calendar = dateTimeInput.getCalendar();
    if (!ForumUtils.isEmpty(dateTimeInput.getValue())) {
      if (calendar == null) {
        Object[] args = { faled };
        throw new MessageException(new ApplicationMessage("NameValidator.msg.erro-format-date", args, ApplicationMessage.WARNING));
      }
    }
    return calendar;
  }

  public UIFormDateTimePicker getUIFormDateTimePicker(String name) {
    return (UIFormDateTimePicker) findComponentById(name);
  }

  static public class SearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiForm = event.getSource();
      Log log = ExoLogger.getLogger(SearchActionListener.class);
      String keyValue = uiForm.getUIStringInput(FIELD_SEARCHVALUE_INPUT).getValue();
      keyValue = CommonUtils.encodeSpecialCharInSearchTerm(keyValue);
      String type = uiForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).getValue();
      String topicType = uiForm.getUIFormSelectBox(FIELD_TOPICTYPE_SELECTBOX).getValue();

      String valueIn = uiForm.getUIFormRadioBoxInput(FIELD_SCOPE_RADIOBOX).getValue();
      if (valueIn == null || valueIn.length() == 0)
        valueIn = ForumEventQuery.VALUE_IN_ENTIRE;
      String byUser = uiForm.getUIStringInput(FIELD_SEARCHUSER_INPUT).getValue();

      String isLock = "all";
      boolean isL = (Boolean) uiForm.getUICheckBoxInput(FIELD_ISLOCK_CHECKBOX).getValue();
      boolean isUL = (Boolean) uiForm.getUICheckBoxInput(FIELD_ISUNLOCK_CHECKBOX).getValue();
      if (isL && !isUL)
        isLock = "true";
      if (!isL && isUL)
        isLock = "false";
      String isClosed = "all";
      String remain = ForumUtils.EMPTY_STR;
      boolean isCl = (Boolean) uiForm.getUICheckBoxInput(FIELD_ISCLOSED_CHECKBOX).getValue();
      boolean isOp = (Boolean) uiForm.getUICheckBoxInput(FIELD_ISOPEN_CHECKBOX).getValue();
      if (uiForm.getIsMod()) {
        if (isCl && !isOp)
          isClosed = "true";
        if (!isCl && isOp)
          isClosed = "false";
      } else {
        if (type.equals(Utils.FORUM)) {
          isClosed = "false";
        } else if (type.equals(Utils.TOPIC)) {
          isClosed = "false";
          remain = "@exo:isActiveByForum='true'";
        } else if (type.equals(Utils.POST))
          remain = "@exo:isActiveByTopic='true'";
      }
      String topicCountMin = uiForm.getUISliderControl(FIELD_TOPICCOUNTMIN_SLIDER).getValue();
      String postCountMin = uiForm.getUISliderControl(FIELD_POSTCOUNTMIN_SLIDER).getValue();
      String viewCountMin = uiForm.getUISliderControl(FIELD_VIEWCOUNTMIN_SLIDER).getValue();

      String moderator = uiForm.getUIStringInput(FIELD_MODERATOR_INPUT).getValue();
      Calendar fromDateCreated = uiForm.getCalendar(uiForm.getUIFormDateTimePicker(FROMDATECREATED), FROMDATECREATED);
      Calendar toDateCreated = uiForm.getCalendar(uiForm.getUIFormDateTimePicker(TODATECREATED), TODATECREATED);
      Calendar fromDateCreatedLastPost = uiForm.getCalendar(uiForm.getUIFormDateTimePicker(FROMDATECREATEDLASTPOST), FROMDATECREATEDLASTPOST);
      Calendar toDateCreatedLastPost = uiForm.getCalendar(uiForm.getUIFormDateTimePicker(TODATECREATEDLASTPOST), TODATECREATEDLASTPOST);
      if (fromDateCreated != null && toDateCreated != null && fromDateCreated.after(toDateCreated)) {
        uiForm.warning("UISearchForm.msg.erro-from-less-then-to");
        return;
      }
      if (type.equals(Utils.TOPIC) && 
          (fromDateCreatedLastPost != null && toDateCreatedLastPost != null && fromDateCreatedLastPost.after(toDateCreatedLastPost))) {
        uiForm.warning("UISearchForm.msg.erro-from-less-then-to");
        return;
      }      
      ForumEventQuery eventQuery = new ForumEventQuery();
      eventQuery.setListOfUser(UserHelper.getAllGroupAndMembershipOfUser(null));
      eventQuery.setUserPermission(uiForm.userProfile.getUserRole());
      eventQuery.setType(type);
      eventQuery.setKeyValue(keyValue);
      eventQuery.setValueIn(valueIn);
      eventQuery.setTopicType(topicType);
      eventQuery.setPath(uiForm.path);
      eventQuery.setByUser(CommonUtils.encodeSpecialCharInSearchTerm(byUser));
      eventQuery.setIsLock(isLock);
      eventQuery.setIsClose(isClosed);
      eventQuery.setTopicCountMin(uiForm.checkValue(topicCountMin));
      eventQuery.setPostCountMin(uiForm.checkValue(postCountMin));
      eventQuery.setViewCountMin(uiForm.checkValue(viewCountMin));
      eventQuery.setModerator(CommonUtils.encodeSpecialCharInSearchTerm(moderator));
      eventQuery.setFromDateCreated(fromDateCreated);
      eventQuery.setToDateCreated(toDateCreated);
      eventQuery.setFromDateCreatedLastPost(fromDateCreatedLastPost);
      eventQuery.setToDateCreatedLastPost(toDateCreatedLastPost);

      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      if (type.equals(Utils.CATEGORY)) {
        eventQuery.getPathQuery(forumPortlet.getInvisibleCategories());
      } else {
        eventQuery.getPathQuery(new ArrayList<String>(forumPortlet.getInvisibleForums()));
      }
      if (eventQuery.getIsEmpty()) {
        uiForm.warning("NameValidator.msg.erro-empty-search");
        return;
      }
      eventQuery.setRemain(remain);
      List<ForumSearch> list = null;
      try {
        list = uiForm.getForumService().getAdvancedSearch(eventQuery, forumPortlet.getInvisibleCategories(), new ArrayList<String>(forumPortlet.getInvisibleForums()));
      } catch (Exception e) {
        log.warn("\nGetting advance search fail:\n " + e.getCause());
        uiForm.warning("UIQuickSearchForm.msg.failure");
        return;
      }

      forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
      UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class);
      categoryContainer.updateIsRender(true);
      UICategories categories = categoryContainer.getChild(UICategories.class);
      categories.setIsRenderChild(true);
      UIForumListSearch listSearchEvent = categories.getChild(UIForumListSearch.class);
      listSearchEvent.setListSearchEvent(list, ForumUtils.FIELD_SEARCHFORUM_LABEL+type);
      forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL);
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class OnchangeActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiForm = event.getSource();
      String type = uiForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).getValue();
      uiForm.getUIFormRadioBoxInput(FIELD_SCOPE_RADIOBOX).setValue(ForumEventQuery.VALUE_IN_ENTIRE);
      uiForm.setSelectType(type);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class ResetFieldActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiForm = event.getSource();
      uiForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).setValue(Utils.CATEGORY);
      uiForm.getUIFormSelectBox(FIELD_TOPICTYPE_SELECTBOX).setValue("all");
      uiForm.getUIFormRadioBoxInput(FIELD_SCOPE_RADIOBOX).setValue(ForumEventQuery.VALUE_IN_ENTIRE);
      uiForm.getUIFormDateTimePicker(FROMDATECREATEDLASTPOST).setValue(ForumUtils.EMPTY_STR);
      uiForm.getUIFormDateTimePicker(TODATECREATEDLASTPOST).setValue(ForumUtils.EMPTY_STR);
      uiForm.getUICheckBoxInput(FIELD_ISLOCK_CHECKBOX).setValue(false);
      uiForm.getUICheckBoxInput(FIELD_ISUNLOCK_CHECKBOX).setValue(false);
      uiForm.getUICheckBoxInput(FIELD_ISCLOSED_CHECKBOX).setValue(false);
      uiForm.getUICheckBoxInput(FIELD_ISOPEN_CHECKBOX).setValue(false);
      uiForm.getUIStringInput(FIELD_MODERATOR_INPUT).setValue(ForumUtils.EMPTY_STR);
      uiForm.getUIStringInput(FIELD_SEARCHVALUE_INPUT).setValue(ForumUtils.EMPTY_STR);
      uiForm.getUIFormDateTimePicker(FROMDATECREATED).setValue(ForumUtils.EMPTY_STR);
      uiForm.getUIFormDateTimePicker(TODATECREATED).setValue(ForumUtils.EMPTY_STR);
      uiForm.getUIStringInput(FIELD_SEARCHUSER_INPUT).setValue(ForumUtils.EMPTY_STR);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class CancelActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiForm = event.getSource();
      UIForumPortlet forumPortlet = uiForm.getParent();
      forumPortlet.renderForumHome();
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class CloseActionListener extends EventListener<UIUserSelect> {
    public void execute(Event<UIUserSelect> event) throws Exception {
      UIUserSelect uiUserSelector = event.getSource();
      UIForumPortlet forumPortlet = uiUserSelector.getAncestorOfType(UIForumPortlet.class);
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class);
      popupAction.removeChild(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class ClosePopupActionListener extends EventListener<UIPopupWindow> {
    public void execute(Event<UIPopupWindow> event) throws Exception {
      UIPopupWindow popupWindow = event.getSource();
      UIForumPortlet forumPortlet = popupWindow.getAncestorOfType(UIForumPortlet.class);
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class);
      popupAction.removeChild(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class AddActionListener extends EventListener<UIUserSelect> {
    public void execute(Event<UIUserSelect> event) throws Exception {
      UIUserSelect uiUserSelector = event.getSource();
      String values = uiUserSelector.getSelectedUsers();
      UIForumPortlet forumPortlet = uiUserSelector.getAncestorOfType(UIForumPortlet.class);
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class);
      UISearchForm searchForm = forumPortlet.findFirstComponentOfType(UISearchForm.class);
      String id = uiUserSelector.getPermisionType();
      if (id.equals(FIELD_SEARCHUSER_INPUT)) {
        UIFormStringInput searchUser = searchForm.getUIStringInput(FIELD_SEARCHUSER_INPUT);
        String vls = searchUser.getValue();
        if (!ForumUtils.isEmpty(vls)) {
          values = values + ForumUtils.COMMA + vls;
          values = ForumUtils.removeStringResemble(values.replaceAll(",,", ForumUtils.COMMA));
        }
        searchUser.setValue(values);
      } else {
        UIFormStringInput moderators = searchForm.getUIStringInput(FIELD_MODERATOR_INPUT);
        String vls = moderators.getValue();
        if (!ForumUtils.isEmpty(vls)) {
          values = values + ForumUtils.COMMA + vls;
          values = ForumUtils.removeStringResemble(values.replaceAll(",,", ForumUtils.COMMA));
        }
        moderators.setValue(values);
      }
      popupAction.removeChild(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      event.getRequestContext().addUIComponentToUpdateByAjax(searchForm);
    }
  }

  static public class AddValuesUserActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      String id = event.getRequestContext().getRequestParameter(OBJECTID);
      UISearchForm searchForm = event.getSource();
      UIForumPortlet forumPortlet = searchForm.getAncestorOfType(UIForumPortlet.class);
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class).setRendered(true);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      searchForm.showUIUserSelect(popupContainer, USER_SEARCH_POPUP_WINDOW_ID, id);
      popupAction.addChild(popupContainer);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }
}
