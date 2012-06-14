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
package org.exoplatform.faq.webui;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.popup.UISettingForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.form.UIFormInputInfo;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com 
 * Mar 04, 2008
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class, 
    template = "app:/templates/faq/webui/UIAnswersPortlet.gtmpl"
)
public class UIAnswersPortlet extends UIPortletApplication {
  private final static String SPACE_URL   = "SPACE_URL".intern();

  private final static String SLASH       = "/".intern();

  private String              spaceGroupId = null;

  private boolean             isFirstTime = true;

  /**
   * ui component for displaying message when changing mode.
   */
  private UIFormInputInfo changeModeMessage;

  private PortletMode     portletMode;
  public UIAnswersPortlet() throws Exception {
    changeModeMessage = new UIFormInputInfo("UIMessageEditMode", "UIMessageEditMode", "");
    changeModeMessage.setRendered(false);
    addChild(changeModeMessage);
    addChild(UIAnswersContainer.class, null, null);
    UIPopupAction uiPopup = addChild(UIPopupAction.class, null, null);
    uiPopup.setId("UIAnswersPopupAction");
    uiPopup.getChild(UIPopupWindow.class).setId("UIAnswersPopupWindow");
  }

  public String getSpaceCategoryId() {
    try {
      PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
      PortletPreferences pref = pcontext.getRequest().getPreferences();
      if (pref.getValue(SPACE_URL, null) != null) {
        SpaceService sService = (SpaceService) getApplicationComponent(SpaceService.class);
        FAQService fService = (FAQService) getApplicationComponent(FAQService.class);
        String url = pref.getValue(SPACE_URL, null);
        Space space = sService.getSpaceByUrl(url);
        spaceGroupId = space.getGroupId();
        String categoryId = Utils.CATE_SPACE_ID_PREFIX + space.getPrettyName();
        if (fService.isExisting(Utils.CATEGORY_HOME + SLASH + categoryId)) {
          return categoryId;
        }
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext portletReqContext = (PortletRequestContext) context;
    portletMode = portletReqContext.getApplicationMode();
    if (portletMode == PortletMode.VIEW) {
      changeModeMessage.setRendered(false);
      isFirstTime = true;
      if (getChild(UIAnswersContainer.class) == null) {
        if (getChild(UISettingForm.class) != null) {
          removeChild(UISettingForm.class);
        }
        addChild(UIAnswersContainer.class, null, null);
      }
      renderPortletByURL();
    } else if (portletMode == PortletMode.EDIT) {
      try {
        changeModeMessage.setValue(context.getApplicationResourceBundle().getString("UIAnswersPortlet.label.deny-access-edit-mode"));
        if (isFirstTime) {
          isFirstTime = false;
          UIQuestions questions = getChild(UIAnswersContainer.class).getChild(UIQuestions.class);
          FAQSetting faqSetting = questions.getFAQSetting();
          removeChild(UIAnswersContainer.class);
          if (getChild(UISettingForm.class) == null) {
            if (faqSetting.isAdmin()) {
              UISettingForm settingForm = addChild(UISettingForm.class, null, "FAQPortletSetting");
              settingForm.setRendered(true);
              settingForm.setIsEditPortlet(true);
              settingForm.init();
            } else {
              changeModeMessage.setRendered(true);
            }

          }
        }
      } catch (Exception e) {
        log.error("\nFail to render a WebUIApplication\n", e);
      }
    }

    super.processRender(app, context);
  }

  public void renderPortletByURL() throws Exception {
    try {
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      String cateId = getSpaceCategoryId();
      if (portalContext.getRequestParameter(OBJECTID) == null && !portalContext.useAjax()) {
        if (!FAQUtils.isFieldEmpty(cateId)) {
          UIBreadcumbs uiBreadcums = findFirstComponentOfType(UIBreadcumbs.class);
          UIQuestions uiQuestions = findFirstComponentOfType(UIQuestions.class);
          UICategories categories = findFirstComponentOfType(UICategories.class);
          uiBreadcums.setUpdataPath(Utils.CATEGORY_HOME + "/" + cateId);
          uiBreadcums.setRenderSearch(true);
          uiQuestions.setCategoryId(Utils.CATEGORY_HOME + "/" + cateId);
          categories.setPathCategory(Utils.CATEGORY_HOME + "/" + cateId);
        } else {
          String questionId = portalContext.getRequestParameter(Utils.QUESTION_ID_PARAM);
          String asn = portalContext.getRequestParameter(Utils.ANSWER_NOW_PARAM);
          if (!FAQUtils.isFieldEmpty(questionId)) {
            viewQuestionById(portalContext, questionId, Boolean.valueOf(asn), false);
          }
        }
      }
    } catch (Exception e) {
      log.error("can not render the selected category", e);
    }
  }

  public String getSpaceGroupId() {
    return spaceGroupId;
  }

  public void cancelAction() throws Exception {
    WebuiRequestContext context = RequestContext.getCurrentInstance();
    UIPopupAction popupAction = getChild(UIPopupAction.class);
    popupAction.deActivate();
    context.addUIComponentToUpdateByAjax(popupAction);
  }

  public static String getPreferenceDisplay() {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    String repository = portletPref.getValue("display", "");
    return repository;
  }
  
  public void viewQuestionById(WebuiRequestContext context, String questionId, boolean isAnswerNow, boolean isAction) throws Exception {
    UIQuestions uiQuestions = this.findFirstComponentOfType(UIQuestions.class);
    FAQService faqService_ = (FAQService) getApplicationComponent(FAQService.class);
    uiQuestions.isSortAnswerUp = null;
    try {
      boolean isRelation = false;
      boolean isSetLang = true;
      if (questionId.indexOf(UIQuestions.OBJECT_LANGUAGE) > 0) {
        String[] array = questionId.split(UIQuestions.OBJECT_LANGUAGE);
        questionId = array[0];
        if (array[1].indexOf(UIQuestions.OBJECT_RELATION) > 0) { // click on relation
          isRelation = true;
          if (!FAQUtils.isFieldEmpty(uiQuestions.viewingQuestionId_)) {
            uiQuestions.backPath_ = uiQuestions.viewingQuestionId_ + UIQuestions.OBJECT_LANGUAGE + uiQuestions.language_ + UIQuestions.OBJECT_BACK;
          }
        } else { // Click on back
          if (array[1].indexOf(UIQuestions.OBJECT_BACK) > 0) {
            isRelation = true;
            array[1] = array[1].replaceFirst(UIQuestions.OBJECT_BACK, "");
          }
          isSetLang = false;
          uiQuestions.language_ = array[1];
          uiQuestions.backPath_ = "";
        }
      }
      Question question = faqService_.getQuestionById(questionId);
      if (uiQuestions.checkQuestionToView(question, context)) {
        String questionPath = question.getPath();
        UIBreadcumbs breadcumbs = this.findFirstComponentOfType(UIBreadcumbs.class);
        String categoryPath = question.getCategoryPath();
        breadcumbs.setUpdataPath(categoryPath);
        UICategories categories = this.findFirstComponentOfType(UICategories.class);
        categories.setPathCategory(breadcumbs.getPaths());
        uiQuestions.setCategoryId(categoryPath);
        uiQuestions.updateCurrentQuestionList();
        uiQuestions.pageList.setObjectId(questionPath);
        if(!isAction){
          uiQuestions.viewQuestion(question);
        }
        uiQuestions.viewingQuestionId_ = questionPath;
        if (isRelation){
          uiQuestions.updateLanguageMap();
        }
        if(isSetLang){
          uiQuestions.language_ = question.getLanguage();
        } else {
          uiQuestions.updateCurrentLanguage();
        }
        uiQuestions.updateQuestionLanguageByLanguage(questionPath, question.getLanguage());
        if (isAnswerNow) {
          uiQuestions.processResponseQuestionAction(context, questionPath);
        }
        context.addUIComponentToUpdateByAjax(getChild(UIAnswersContainer.class));
      }
      return;
    } catch (Exception e) {
      log.debug("Failed to view question by id: " + questionId, e);
      uiQuestions.showMessageDeletedQuestion(context);
    }
  }
}
