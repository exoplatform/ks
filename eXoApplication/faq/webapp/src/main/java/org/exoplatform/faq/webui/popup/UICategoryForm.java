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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.webui.BaseUIFAQForm;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UICategories;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.UISelectComponent;
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
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfigs( 
		{
				@ComponentConfig(
					lifecycle = UIFormLifecycle.class, 
					template = "system:/groovy/webui/form/UIForm.gtmpl", 
					events = {
							@EventConfig(listeners = UICategoryForm.SaveActionListener.class), 
							@EventConfig(listeners = UICategoryForm.SelectPermissionActionListener.class, phase = Phase.DECODE), 
							@EventConfig(listeners = UICategoryForm.CancelActionListener.class, phase = Phase.DECODE), 
							@EventConfig(listeners = UICategoryForm.AddValuesUserActionListener.class, phase = Phase.DECODE) 
				}
			), 
				@ComponentConfig(id = "UICategoryUserPopupWindow", type = UIPopupWindow.class, 
					template = "system:/groovy/webui/core/UIPopupWindow.gtmpl", 
					events = {
							@EventConfig(listeners = UICategoryForm.ClosePopupActionListener.class, name = "ClosePopup"), 
							@EventConfig(listeners = UICategoryForm.AddActionListener.class, name = "Add", phase = Phase.DECODE), 
							@EventConfig(listeners = UICategoryForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE) 
					}
				) 
		}
)
public class UICategoryForm extends BaseUIFAQForm implements UIPopupComponent, UISelector 	{
	private String categoryId_ = "";
	private String parentId_ ;
	//protected long index_ = 0;
	final private static String CATEGORY_DETAIL_TAB = "UIAddCategoryForm";
	final private static String FIELD_NAME_INPUT = "eventCategoryName" ; 
  final private static String FIELD_DESCRIPTION_INPUT = "description" ;
  final private static String FIELD_USERPRIVATE_INPUT = "userPrivate" ;
  final private static String FIELD_MODERATOR_INPUT = "moderator" ;
  final private static String FIELD_INDEX_INPUT = "index" ;
  final private static String FIELD_MODERATEQUESTIONS_CHECKBOX = "moderatequestions" ;
  public static final String VIEW_AUTHOR_INFOR = "ViewAuthorInfor".intern();
  final private static String FIELD_MODERATE_ANSWERS_CHECKBOX = "moderateAnswers" ;
  final private static String USER_SELECTOR_POPUPWINDOW = "UICategoryUserPopupWindow";
  private boolean isAddNew_ = true ;
  private String oldName_ = "";
  private long oldIndex_ = 0;
  private Category currentCategory_ ;
  private long maxIndex = 1;
	public UICategoryForm() throws Exception {
		setActions(new String[] {"Save","Cancel"});
	}
	
	public void updateAddNew(boolean isAddNew) throws Exception {
		isAddNew_ = isAddNew ;
    UIFormInputWithActions inputset = new UIFormInputWithActions(CATEGORY_DETAIL_TAB) ;
    inputset.addUIFormInput(new UIFormStringInput(FIELD_NAME_INPUT, FIELD_NAME_INPUT, null).addValidator(MandatoryValidator.class)) ;
    UIFormStringInput index = new UIFormStringInput(FIELD_INDEX_INPUT, FIELD_INDEX_INPUT, null) ;
    index.addValidator(PositiveNumberFormatValidator.class);
    maxIndex = getFAQService().getMaxindexCategory(parentId_) + 1;
    if(isAddNew)index.setValue(String.valueOf(maxIndex));
    inputset.addUIFormInput(index) ;
    inputset.addUIFormInput(new UIFormTextAreaInput(FIELD_USERPRIVATE_INPUT, FIELD_USERPRIVATE_INPUT, null)) ;
    inputset.addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION_INPUT, FIELD_DESCRIPTION_INPUT, null)) ;
    inputset.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_MODERATEQUESTIONS_CHECKBOX, FIELD_MODERATEQUESTIONS_CHECKBOX, false )) ;
    inputset.addUIFormInput(new UIFormCheckBoxInput<Boolean>(VIEW_AUTHOR_INFOR, VIEW_AUTHOR_INFOR, false )) ;
    inputset.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_MODERATE_ANSWERS_CHECKBOX, FIELD_MODERATE_ANSWERS_CHECKBOX, false));
    UIFormTextAreaInput moderator = new UIFormTextAreaInput(FIELD_MODERATOR_INPUT, FIELD_MODERATOR_INPUT, null) ;
    if (isAddNew) moderator.setValue(FAQUtils.getCurrentUser());
		moderator.addValidator(MandatoryValidator.class);
    inputset.addUIFormInput(moderator) ;
    List<ActionData> actionData ;
    String[]strings = new String[] {"SelectUser", "SelectMemberShip", "SelectGroup"}; 
		ActionData ad ;
		String files[] = new String[]{FIELD_USERPRIVATE_INPUT, FIELD_MODERATOR_INPUT};
		for (int i = 0; i < files.length; i++) {
			int j = 0;
			actionData = new ArrayList<ActionData>() ;
			for(String string : strings) {
				ad = new ActionData() ;
				ad.setActionName(string);
				if(j==0) ad.setActionListener("AddValuesUser") ;
	      else ad.setActionListener("SelectPermission") ;
				ad.setActionType(ActionData.TYPE_ICON) ;
				ad.setCssIconClass(string + "Icon") ;
				ad.setActionParameter(files[i] + "," + String.valueOf(j)) ;
				actionData.add(ad) ;
				++j;
			}
			inputset.setActionField(files[i], actionData) ; 
		}
		addChild(inputset) ;
	}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	public String getParentId() { return parentId_; }
	public void setParentId(String s) { parentId_ = s ; }

	public void updateSelect(String selectField, String value) throws Exception {
		UIFormTextAreaInput fieldInput = getUIFormTextAreaInput(selectField) ;
		String oldValue = fieldInput.getValue() ;
		if(oldValue != null && oldValue.trim().length() > 0) {
			oldValue =  oldValue + "," +  value ;
		} else oldValue = value ;
		fieldInput.setValue(oldValue) ;
	} 

	public void setCategoryValue(Category cat, boolean isUpdate) throws Exception{
		if(isUpdate) {
			isAddNew_ = false ;
			categoryId_ = cat.getPath() ; 
			currentCategory_ = cat ;
			oldName_ = cat.getName() ;
			oldIndex_ = cat.getIndex() ;
			if(oldName_ != null && oldName_.trim().length() > 0) getUIStringInput(FIELD_NAME_INPUT).setValue(oldName_) ;
			else getUIStringInput(FIELD_NAME_INPUT).setValue("Root") ;
			String userPrivate = null;
			if(cat.getUserPrivate() != null) {
				for(String str : cat.getUserPrivate()) {
					if(userPrivate != null) userPrivate = userPrivate + ", " + str;
					else userPrivate = str ;
				}
			}					
			getUIFormTextAreaInput(FIELD_USERPRIVATE_INPUT).setDefaultValue(userPrivate) ;			
			getUIStringInput(FIELD_INDEX_INPUT).setValue(String.valueOf(cat.getIndex())) ;
			getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).setDefaultValue(cat.getDescription()) ;
			getUIFormCheckBoxInput(FIELD_MODERATEQUESTIONS_CHECKBOX).setChecked(cat.isModerateQuestions()) ;
			getUIFormCheckBoxInput(FIELD_MODERATE_ANSWERS_CHECKBOX).setChecked(cat.isModerateAnswers()) ;
			getUIFormCheckBoxInput(VIEW_AUTHOR_INFOR).setChecked(cat.isViewAuthorInfor()) ;
			String moderator = "";
			if(cat.getModerators() != null && cat.getModerators().length > 0)
				for(String str : cat.getModerators()) {
					if( moderator!= null && moderator.trim().length() >0 ) moderator += "," ;
					moderator += str ;
				}    
			if(moderator.trim().length() > 0)getUIFormTextAreaInput(FIELD_MODERATOR_INPUT).setValue(moderator) ;
			else getUIFormTextAreaInput(FIELD_MODERATOR_INPUT).setValue(FAQUtils.getCurrentUser()) ;
		} 
	}

	public String cutColonCaret(String name) {
		StringBuffer string = new StringBuffer();
		char c;
		for (int i = 0; i < name.length(); i++) {
			c = name.charAt(i) ;
			if(c == 58 || c == 47) continue ;
			string.append(c) ;
		}
		return string.toString();
	}

	static public class SaveActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategory = event.getSource() ;
      String name = uiCategory.getUIStringInput(FIELD_NAME_INPUT).getValue() ;
      name = FAQUtils.convertTextForTitle(name);
      
      if(uiCategory.isAddNew_) {
      	if(uiCategory.getFAQService().isCategoryExist(name, uiCategory.parentId_)) {
      		uiCategory.warning("UICateforyForm.sms.cate-name-exist") ;
      		return ;
        }
      }else {
      	if(!name.equals(uiCategory.oldName_) && uiCategory.getFAQService().isCategoryExist(name, uiCategory.parentId_)) {
      		uiCategory.warning("UICateforyForm.sms.cate-name-exist") ;
      		return ;
        }
      }
      long index = uiCategory.oldIndex_;
      uiCategory.maxIndex = uiCategory.getFAQService().getMaxindexCategory(uiCategory.parentId_) + 1;
      String strIndex = uiCategory.getUIStringInput(FIELD_INDEX_INPUT).getValue();
      if (!FAQUtils.isFieldEmpty(strIndex)) {
        index = Long.parseLong(strIndex);
        if (index > uiCategory.maxIndex) {
          index = uiCategory.maxIndex;
        }
      } else if (uiCategory.isAddNew_) {
        index = uiCategory.maxIndex;
      }
      
      String description = uiCategory.getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).getValue() ;
      String moderator = uiCategory.getUIFormTextAreaInput(FIELD_MODERATOR_INPUT).getValue() ;
      if (moderator == null || moderator.trim().length() <= 0) {
      	uiCategory.warning("UICategoryForm.msg.moderator-required") ;
        return ; 
      }
      
      String userPrivate = uiCategory.getUIFormTextAreaInput(FIELD_USERPRIVATE_INPUT).getValue() ;
      String erroUser = UserHelper.checkValueUser(userPrivate) ;
      if(!FAQUtils.isFieldEmpty(erroUser)) {
    		uiCategory.warning("UICateforyForm.sms.user-not-found", new String[]{ uiCategory.getLabel(FIELD_USERPRIVATE_INPUT), erroUser }) ;
    		return ;
    	}
      String[] userPrivates = new String[]{""} ;
      if(userPrivate != null && userPrivate.trim().length() > 0) {
      	userPrivates = FAQUtils.splitForFAQ(userPrivate) ;
      }
      /*moderator = uiCategory.removeSpaceInString(moderator) ;
      moderator = uiCategory.filterItemInString(moderator) ;*/
      erroUser = UserHelper.checkValueUser(moderator) ;
      if(!FAQUtils.isFieldEmpty(erroUser)) {
      	uiCategory.warning("UICateforyForm.sms.user-not-found", new String[]{ uiCategory.getLabel(FIELD_MODERATOR_INPUT), erroUser }) ;
      	return ;
      }
      
      Boolean moderatequestion = uiCategory.getUIFormCheckBoxInput(FIELD_MODERATEQUESTIONS_CHECKBOX).isChecked() ;
      Boolean moderateAnswer = uiCategory.getUIFormCheckBoxInput(FIELD_MODERATE_ANSWERS_CHECKBOX).isChecked() ;
      boolean viewAuthorInfor = uiCategory.getUIFormCheckBoxInput(VIEW_AUTHOR_INFOR).isChecked();
      String[] users = FAQUtils.splitForFAQ(moderator) ;
      
			
			UIAnswersPortlet answerPortlet = uiCategory.getAncestorOfType(UIAnswersPortlet.class) ;
			//UIQuestions questions = answerPortlet.findFirstComponentOfType(UIQuestions.class) ;
			//SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			Category cat ;
			if(uiCategory.isAddNew_) {
				cat = new Category();
				cat.setCreatedDate(new Date()) ;
			}else {
				cat = uiCategory.currentCategory_ ;
			}
			cat.setName(name.trim()) ;
			cat.setUserPrivate(userPrivates);
			cat.setDescription(description) ;			
			cat.setModerateQuestions(moderatequestion) ;
			cat.setModerateAnswers(moderateAnswer);
			cat.setViewAuthorInfor(viewAuthorInfor);
			cat.setIndex(index);
			cat.setModerators(users) ;
			uiCategory.getFAQService().saveCategory(uiCategory.parentId_, cat, uiCategory.isAddNew_) ;
			
			if(!uiCategory.isAddNew_) {
				UICategories categories = answerPortlet.findFirstComponentOfType(UICategories.class) ;
				if(uiCategory.categoryId_.equals(categories.getCategoryPath())) {
					UIQuestions questions = answerPortlet.findFirstComponentOfType(UIQuestions.class) ;
					questions.viewAuthorInfor = uiCategory.getFAQService().isViewAuthorInfo(uiCategory.categoryId_) ;
				}
			}
			
			answerPortlet.cancelAction() ;
			//questions.setQuestions() ; //?
			event.getRequestContext().addUIComponentToUpdateByAjax(answerPortlet) ;
		}
	}

	static public class SelectPermissionActionListener extends BaseEventListener<UICategoryForm> {
		public void onEvent(Event<UICategoryForm> event, UICategoryForm categoryForm, String permType) throws Exception {
			String types[] = permType.split(",");
			UIPopupContainer popupContainer = categoryForm.getAncestorOfType(UIPopupContainer.class);
			UIUserSelect uiUserSelect = popupContainer.findFirstComponentOfType(UIUserSelect.class) ;
			if(uiUserSelect != null){
				UIPopupWindow popupWindow = uiUserSelect.getParent();
				popupWindow.setShow(false);
				popupWindow.setUIComponent(null);
				popupWindow.setRendered(false);
				event.getRequestContext().addUIComponentToUpdateByAjax(popupWindow.getParent()) ;
			}
			UIGroupSelector uiGroupSelector = null;
			if (types[1].equals(UISelectComponent.TYPE_GROUP)) {
				uiGroupSelector = openPopup(popupContainer, UIGroupSelector.class, "GroupSelector", 550, 0);
			} else if (types[1].equals(UISelectComponent.TYPE_MEMBERSHIP)) {
				uiGroupSelector = openPopup(popupContainer, UIGroupSelector.class, "UIMemberShipSelector", 550, 0);
			}
			uiGroupSelector.setType(types[1]);
      uiGroupSelector.setSpaceGroupId(categoryForm.getAncestorOfType(UIAnswersPortlet.class).getSpaceGroupId());
			uiGroupSelector.setComponent(categoryForm, new String[] { types[0] });
			uiGroupSelector.getChild(UITree.class).setId(UIGroupSelector.TREE_GROUP_ID);
			uiGroupSelector.getChild(org.exoplatform.webui.core.UIBreadcumbs.class).setId(UIGroupSelector.BREADCUMB_GROUP_ID);
		}
	}

	static public class CancelActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategory = event.getSource() ;			
			UIPopupAction uiPopupAction = uiCategory.getAncestorOfType(UIPopupAction.class) ;
			uiPopupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}

	static public class CloseActionListener extends EventListener<UIUserSelector> {
		public void execute(Event<UIUserSelector> event) throws Exception {
			UIUserSelector uiUserSelector = event.getSource() ;
			UIPopupWindow popupWindow = uiUserSelector.getParent() ;
			popupWindow.setUIComponent(null);
			popupWindow.setShow(false);
			popupWindow.setRendered(false);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupWindow.getParent()) ;
		}
	}

	static public class ClosePopupActionListener extends EventListener<UIPopupWindow> {
		public void execute(Event<UIPopupWindow> event) throws Exception {
			UIPopupWindow popupWindow = event.getSource() ;
			popupWindow.setUIComponent(null);
			popupWindow.setShow(false);
			popupWindow.setRendered(false);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupWindow.getParent()) ;
		}
	}
  
	private String getUserSelect(String vls, String values) throws Exception {
		try {
			if(!FAQUtils.isFieldEmpty(vls)) {
				values = values + "," + vls;
				List<String> list = new ArrayList<String>();
				for (String string : Arrays.asList(values.split(","))) {
					if(!list.contains(string) && string.trim().length() > 0)list.add(string);
        }
				values = list.toString().replace('['+"", "").replace(']'+"", "").replaceAll(", ", ",");
			}
    } catch (Exception e) {
      log.error("Fail to get user selector: ", e);
      }
    return values;
	}
	
	static public class AddActionListener extends EventListener<UIUserSelect> {
		public void execute(Event<UIUserSelect> event) throws Exception {
			UIUserSelect uiUserSelector = event.getSource();
			String values = uiUserSelector.getSelectedUsers();
			UIAnswersPortlet answerPortlet = uiUserSelector.getAncestorOfType(UIAnswersPortlet.class);
			UICategoryForm categoryForm = answerPortlet.findFirstComponentOfType(UICategoryForm.class);
			UIPopupWindow popupWindow = uiUserSelector.getParent();
			String id = uiUserSelector.getPermisionType();
			UIFormInputWithActions inputset = categoryForm.getChildById(CATEGORY_DETAIL_TAB);
			if (id.equals(FIELD_USERPRIVATE_INPUT)) {
				UIFormTextAreaInput textAreaInput = inputset.getUIFormTextAreaInput(FIELD_USERPRIVATE_INPUT);
				textAreaInput.setValue(categoryForm.getUserSelect(textAreaInput.getValue(), values));
			} else {
				UIFormTextAreaInput stringInput = inputset.getUIFormTextAreaInput(FIELD_MODERATOR_INPUT);
				stringInput.setValue(categoryForm.getUserSelect(stringInput.getValue(), values));
			}
			popupWindow.setUIComponent(null);
			popupWindow.setShow(false);
			popupWindow.setRendered(false);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupWindow.getParent());
			event.getRequestContext().addUIComponentToUpdateByAjax(categoryForm);
		}
	}

	static public class AddValuesUserActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm categoryForm = event.getSource();
			String id = event.getRequestContext().getRequestParameter(OBJECTID).replace(",0", "")	;
			UIPopupContainer uiPopupContainer = categoryForm.getAncestorOfType(UIPopupContainer.class) ;
			UIGroupSelector uiGroupSelector = uiPopupContainer.findFirstComponentOfType(UIGroupSelector.class) ;
			if(uiGroupSelector != null){
				UIPopupWindow popupWindow = uiGroupSelector.getAncestorOfType(UIPopupWindow.class);
				popupWindow.setUIComponent(null);
				popupWindow.setShow(false);
				popupWindow.setRendered(false);
				event.getRequestContext().addUIComponentToUpdateByAjax(popupWindow.getParent());
			}
			UIPopupWindow uiPopupWindow = uiPopupContainer.getChildById(USER_SELECTOR_POPUPWINDOW);
			if(uiPopupWindow == null)uiPopupWindow = uiPopupContainer.addChild(UIPopupWindow.class, USER_SELECTOR_POPUPWINDOW, USER_SELECTOR_POPUPWINDOW) ;
			UIUserSelect uiUserSelector = uiPopupContainer.createUIComponent(UIUserSelect.class, null, "UIUserSelector");
			uiUserSelector.setShowSearch(true);
			uiUserSelector.setShowSearchUser(true);
			uiUserSelector.setShowSearchGroup(false);
			uiUserSelector.setSpaceGroupId(categoryForm.getAncestorOfType(UIAnswersPortlet.class).getSpaceGroupId());
			uiPopupWindow.setUIComponent(uiUserSelector);
			uiPopupWindow.setShow(true);
			uiPopupWindow.setWindowSize(740, 400);
			uiPopupWindow.setRendered(true);
			uiUserSelector.setPermisionType(id);
			uiPopupContainer.setRendered(true);
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
		}
	}
}