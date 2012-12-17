/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.faq.webui.viewer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.rendering.RenderHelper;
import org.exoplatform.faq.service.CategoryInfo;
import org.exoplatform.faq.service.DataStorage.LoadMoreType;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionInfo;
import org.exoplatform.faq.service.SubCategoryInfo;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS 
 * Author : Vu Duy Tu 
 *           tu.duy@exoplatform.com 
 * Jun 24, 2009 - 4:32:48 AM
 */

@ComponentConfig(
    events = { 
        @EventConfig(listeners = UIViewer.ChangePathActionListener.class),
        @EventConfig(listeners = UIViewer.LoadMoreCategoryActionListener.class),
        @EventConfig(listeners = UIViewer.LoadMoreQuestionActionListener.class)
    }
)
public class UIViewer extends UIContainer {
  private FAQService   fAqService;

  private String       path         = Utils.CATEGORY_HOME;

  protected boolean    useAjax      = false;

  private boolean      isInSpace    = false;

  private RenderHelper renderHelper = new RenderHelper();
  
  private CategoryInfo categoryInfo;

  public UIViewer() throws Exception {
    fAqService = (FAQService) PortalContainer.getComponent(FAQService.class);
    categoryInfo = fAqService.getCategoryInfo(path, FAQUtils.getCategoriesIdFAQPortlet());
  }

  public void processDecode(WebuiRequestContext context) throws Exception {
    super.processDecode(context);
    setPath(StringUtils.EMPTY);
  }
  
  public String getPath() {
    return path;
  }

  public boolean isInSpace() {
    return isInSpace;
  }

  public void setPath(String path) {
    this.path = path;
  }

  protected List<String> arrangeList(List<String> list) {
    List<String> newList = new ArrayList<String>();
    if (list.isEmpty() || list.size() == 0) {
      newList.add("<img src=\"/faq/skin/DefaultSkin/webui/background/HomeIcon.gif\" alt=\"" + Utils.CATEGORY_HOME + "\"/>");
    } else {
      for (int i = (list.size() - 1); i >= 0; i--) {
        if (i == (list.size() - 1)) {
          newList.add("<img src=\"/faq/skin/DefaultSkin/webui/background/HomeIcon.gif\" alt=\"" + list.get(i) + "\"/>");
        } else {
          newList.add(list.get(i));
        }
      }
    }
    return newList;
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return new FAQResourceResolver();
  }

  public String getTemplate() {
    return "FAQViewerTemplate";
  }

  protected CategoryInfo getCategoryInfo() throws Exception {
    useAjax = FAQUtils.getUseAjaxFAQPortlet();
    if(path.indexOf(Utils.CATE_SPACE_ID_PREFIX) >= 0) {
      isInSpace = true;
    } else {
      isInSpace = false;
    }
    return categoryInfo;
  }
  
  protected void setCategoryInfo(CategoryInfo categoryInfo) {
    this.categoryInfo = categoryInfo;
  }

  protected String render(String s) {
    Question question = new Question();
    question.setDetail(s);
    return renderHelper.renderQuestion(question);
  }

  static public class ChangePathActionListener extends EventListener<UIViewer> {
    public void execute(Event<UIViewer> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      UIViewer viewer = event.getSource();
      viewer.setCategoryInfo(viewer.fAqService.getCategoryInfo(path, FAQUtils.getCategoriesIdFAQPortlet()));
      event.getRequestContext().addUIComponentToUpdateByAjax(viewer);
    }
  }
  
  static public class LoadMoreCategoryActionListener extends EventListener<UIViewer> {
    public void execute(Event<UIViewer> event) throws Exception {
      UIViewer viewer = event.getSource();
      List<SubCategoryInfo> subCategoryInfos = viewer.getCategoryInfo().getSubCateInfos();
      CategoryInfo categoryInfo = viewer.fAqService.loadMore(LoadMoreType.CATEGORY,
                                                             viewer.getCategoryInfo().getPath(),
                                                             FAQUtils.getCategoriesIdFAQPortlet(),
                                                             viewer.getCategoryInfo().getSubCateInfos().size(),
                                                             Utils.LIMIT_OF_CATEGORIES_PER_LOADING);
      subCategoryInfos.addAll(categoryInfo.getSubCateInfos());
      viewer.getCategoryInfo().setSubCateInfos(subCategoryInfos);
      viewer.getCategoryInfo().setLoadMore(categoryInfo.hasLoadMore());
      event.getRequestContext().addUIComponentToUpdateByAjax(viewer);
    }
  }

  static public class LoadMoreQuestionActionListener extends EventListener<UIViewer> {
    public void execute(Event<UIViewer> event) throws Exception {
      WebuiRequestContext context = event.getRequestContext();
      String categoryId = context.getRequestParameter(OBJECTID);
      UIViewer viewer = event.getSource();
      
      //
      List<SubCategoryInfo> subCategoryInfos = viewer.getCategoryInfo().getSubCateInfos();
      SubCategoryInfo subCategoryInfo = null;
      for (SubCategoryInfo subCateInfo : subCategoryInfos) {
        if(categoryId.equals(subCateInfo.getId())) {
          subCategoryInfo = subCateInfo;
          break;
        }
      }
      List<String> categoryIdScoped = new ArrayList<String>();
      categoryIdScoped.add(categoryId);
      CategoryInfo categoryInfo = null;
      try {
        categoryInfo = viewer.fAqService.loadMore(LoadMoreType.QUESTION,
                                                  viewer.getCategoryInfo().getPath(),
                                                  categoryIdScoped,
                                                  subCategoryInfo.getQuestionInfos().size(),
                                                  Utils.LIMIT_OF_QUESTIONS_PER_LOADING);
      } catch (Exception e) {
        context.getUIApplication().addMessage(new ApplicationMessage("UIViewer.msg.LoadMoreQuestionFail", null, ApplicationMessage.WARNING));
      }
      
      //
      if (categoryInfo != null) {
        List<SubCategoryInfo> moreSubCategoryInfos = categoryInfo.getSubCateInfos();
        if (moreSubCategoryInfos.size() > 0) {
          List<QuestionInfo> questionInfos = subCategoryInfo.getQuestionInfos();
          questionInfos.addAll(moreSubCategoryInfos.get(0).getQuestionInfos());
          subCategoryInfo.setQuestionInfos(questionInfos);
          subCategoryInfo.setLoadMore(moreSubCategoryInfos.get(0).hasLoadMore());
        }
      }
      
      context.addUIComponentToUpdateByAjax(viewer);
    }
  }
}
