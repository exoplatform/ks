/*
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
 */
package org.exoplatform.faq.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM
 */

@ComponentConfig(
    template = "app:/templates/faq/webui/UIAnswersContainer.gtmpl"
)
public class UIAnswersContainer extends UIContainer {
  private FAQSetting   faqSetting_   = null;

  private String       currentUser_;

  private FAQService   faqService_;

  private boolean      isRenderChild = true;

  private boolean      hasPermission = true;

  private List<String> propetyOfUser = new ArrayList<String>();

  public UIAnswersContainer() throws Exception {
    faqService_ = (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);
    UIBreadcumbs uiBreadcumbs = addChild(UIBreadcumbs.class, null, null);
    UIQuestions uiQuestions = addChild(UIQuestions.class, null, null);
    UICategories uiCategories = addChild(UICategories.class, null, null);

    currentUser_ = FAQUtils.getCurrentUser();
    faqSetting_ = new FAQSetting();
    FAQUtils.getPorletPreference(faqSetting_);
    faqSetting_.setCurrentUser(currentUser_);
    if (!FAQUtils.isFieldEmpty(currentUser_)) {
      if (faqService_.isAdminRole(null))
        faqSetting_.setIsAdmin("TRUE");
      else
        faqSetting_.setIsAdmin("FALSE");
      faqService_.getUserSetting(currentUser_, faqSetting_);
    } else {
      faqSetting_.setIsAdmin("FALSE");
    }
    String cateIdView = Utils.CATEGORY_HOME;
    if (!faqSetting_.isAdmin() && !faqSetting_.isPostQuestionInRootCategory()) {
      propetyOfUser = UserHelper.getAllGroupAndMembershipOfUser(null);
      List<Category> cates = faqService_.getSubCategories(cateIdView, faqSetting_, true, propetyOfUser);
      if (cates != null && cates.size() > 0)
        cateIdView = cateIdView + "/" + cates.get(0).getId();
    }
    isRenderChild = isRenderCategory(cateIdView);
    hasPermission = isRenderChild;
    uiBreadcumbs.setUpdataPath(cateIdView);
    uiQuestions.setFAQService(faqService_);
    uiQuestions.setFAQSetting(faqSetting_);
    uiQuestions.setCategoryId(cateIdView);
    uiBreadcumbs.setRenderSearch(uiQuestions.isViewRootCate());

    uiCategories.setFAQSetting(faqSetting_);
    uiCategories.setFAQService(faqService_);
    if (uiCategories.getCategoryPath() == null)
      uiCategories.setPathCategory(cateIdView);
  }

  public void setViewRootCate() {
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    String isAjax = portalContext.getRequestParameter("ajaxRequest");
    if (isAjax != null && Boolean.parseBoolean(isAjax))
      return;
    FAQUtils.getPorletPreference(faqSetting_);
      UIQuestions questions = getChild(UIQuestions.class);
      questions.setViewRootCate();
      boolean b = questions.isViewRootCate();
      if (b != hasPermission) {
        hasPermission = b;
      }
      if (Utils.CATEGORY_HOME.equals(questions.getCategoryId()))
        isRenderChild = hasPermission;
  }

  public boolean isRenderCategory(String categoryId) {
    try {
      Category category = faqService_.getCategoryById(categoryId);
      if (currentUser_ != null && currentUser_.trim().length() > 0) {
        faqSetting_.setCurrentUser(currentUser_);
        faqSetting_.setCanEdit(false);
        if (faqSetting_.getIsAdmin() != null && faqSetting_.getIsAdmin().equals("TRUE")) {
          faqSetting_.setCanEdit(true);
        } else if (category.getModerators() != null && category.getModerators().length > 0 && category.getModerators()[0].trim().length() > 0) {
          if (propetyOfUser.isEmpty())
            propetyOfUser = UserHelper.getAllGroupAndMembershipOfUser(null);
          faqSetting_.setCanEdit(Utils.hasPermission(Arrays.asList(category.getModerators()), propetyOfUser));
        }
      }
      if (!faqSetting_.isCanEdit() && category.getUserPrivate() != null && category.getUserPrivate().length > 0 && category.getUserPrivate()[0].trim().length() > 0) {
        if (propetyOfUser.isEmpty())
          propetyOfUser = UserHelper.getAllGroupAndMembershipOfUser(null);
        return Utils.hasPermission(Arrays.asList(category.getUserPrivate()), propetyOfUser);        
      }
      return true;
    } catch (Exception e) {
      return true;
    }
  }

  public FAQSetting getFAQSetting() {
    return faqSetting_;
  }

  public boolean getRenderChild() {
    return hasPermission;
  }

  public void updateIsRender(boolean isRender) throws Exception {
    getChild(UICategories.class).setRendered(isRender);
    getChild(UIBreadcumbs.class).setRendered(isRender);
    getChild(UIQuestions.class).setRendered(isRender);
  }
}
