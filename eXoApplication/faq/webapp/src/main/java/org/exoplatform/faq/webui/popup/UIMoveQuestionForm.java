/*
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
 */
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.BaseUIFAQForm;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com 
 * Apr 22, 2008 ,5:12:42 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIMoveQuestionForm.gtmpl", 
    events = {
        @EventConfig(listeners = UIMoveQuestionForm.OkActionListener.class), 
        @EventConfig(listeners = UIMoveQuestionForm.CancelActionListener.class) 
    }
)
public class UIMoveQuestionForm extends BaseUIFAQForm implements UIPopupComponent {
  private String            questionId_      = "";

  protected String          homeCategoryName = "";

  private String            categoryId_;

  private FAQSetting        faqSetting_;

  private List<Cate>        listCate         = new ArrayList<Cate>();

  public UIMoveQuestionForm() throws Exception {
    homeCategoryName = getFAQService().getCategoryNameOf(Utils.CATEGORY_HOME);
  }

  public String getCategoryID() {
    return categoryId_;
  }

  public void setCategoryID(String s) {
    categoryId_ = s;
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  protected List<Cate> getListCate() {
    return this.listCate;
  }

  public void setQuestionId(String questionId) throws Exception {
    this.questionId_ = questionId;
    Question question = getFAQService().getQuestionById(questionId_);
    this.categoryId_ = question.getCategoryId();
  }

  public void setFAQSetting(FAQSetting faqSetting) {
    this.faqSetting_ = faqSetting;
    String orderType = faqSetting.getOrderType();
    if (orderType.equals("asc"))
      faqSetting.setOrderType("desc");
    else
      faqSetting.setOrderType("asc");
  }

  public void updateSubCategory() throws Exception {
    FAQSetting faqSetting = new FAQSetting();
    faqSetting.setIsAdmin("False");
    this.listCate.addAll(FAQUtils.listingCategoryTree(null,faqSetting, null, Integer.MAX_VALUE));
    
    String orderType = faqSetting_.getOrderType();
    if (orderType.equals("asc"))
      faqSetting_.setOrderType("desc");
    else
      faqSetting_.setOrderType("asc");
  }

  static public class OkActionListener extends BaseEventListener<UIMoveQuestionForm> {
    public void onEvent(Event<UIMoveQuestionForm> event, UIMoveQuestionForm moveQuestionForm, String catePath) throws Exception {
      UIAnswersPortlet portlet = moveQuestionForm.getAncestorOfType(UIAnswersPortlet.class);
      UIQuestions questions = portlet.getChild(UIAnswersContainer.class).getChild(UIQuestions.class);
      try {
        Category category = questions.getFAQService().getCategoryById(catePath);
        if (category.getUserPrivate().length > 0 && !Utils.hasPermission(Arrays.asList(category.getUserPrivate()),
            UserHelper.getAllGroupAndMembershipOfUser(null))) {
          warning("UIQuestions.msg.can-not-move-question");
          return;
        }
        try {
          Question question = questions.getFAQService().getQuestionById(moveQuestionForm.questionId_);
          String cateId = catePath.substring(catePath.lastIndexOf("/") + 1);
          if (cateId.equals(question.getCategoryId())) {
            warning("UIMoveQuestionForm.msg.choice-orther");
            return;
          }
          question.setCategoryId(cateId);
          question.setCategoryPath(catePath);
          String link = FAQUtils.getQuestionURI(new StringBuffer(catePath).append("/").
                                                append(Utils.QUESTION_HOME).append("/").append(question.getId()).toString(), false);
          FAQUtils.getEmailSetting(moveQuestionForm.faqSetting_, false, false);
          FAQUtils.getEmailMoveQuestion(moveQuestionForm.faqSetting_);
          List<String> questionList = new ArrayList<String>();
          questionList.add(question.getPath());
          questions.getFAQService().moveQuestions(questionList, catePath, link, moveQuestionForm.faqSetting_);
          questions.updateCurrentQuestionList();
        } catch (Exception e) {
          moveQuestionForm.log.warn("Can not move this question. Exception: " + e.getMessage());
          warning("UIQuestions.msg.question-id-deleted", false);
        }
      } catch (Exception e) {
        warning("UIQuestions.msg.category-id-deleted", false);
      }
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      questions.setDefaultLanguage();
      event.getRequestContext().addUIComponentToUpdateByAjax(questions);
      popupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class CancelActionListener extends EventListener<UIMoveQuestionForm> {
    public void execute(Event<UIMoveQuestionForm> event) throws Exception {
      UIMoveQuestionForm moveQuestionForm = event.getSource();
      UIAnswersPortlet portlet = moveQuestionForm.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      popupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }
}
