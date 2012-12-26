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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 30-10-2008 - 10:35:05  
 */
@ComponentConfig()
public class UIForumKeepStickPageIterator extends BaseForumForm {

  public int                         pageSelect      = 1;

  public int                         maxPage         = 1;

  @SuppressWarnings("unchecked")
  public JCRPageList                 pageList;

  public int                         totalCheked     = 0;

  private int                        endTabPage      = 0;

  private int                        beginTabPage    = 0;

  public boolean                     isUseAjax       = true;

  public boolean                     isLink          = false;

  public String                      objectId        = ForumUtils.EMPTY_STR;

  private Map<Integer, List<String>> pageCheckedList = new HashMap<Integer, List<String>>();

  public UIForumKeepStickPageIterator() throws Exception {
  }

  public List<String> getListChecked(int page) {
    return pageCheckedList.get(page);
  }

  public int getTotalChecked() {
    return totalCheked;
  }

  public void cleanCheckedList() {
    totalCheked = 0;
    this.pageCheckedList.clear();
  }

  public boolean isUseAjax() {
    return isUseAjax;
  }

  public void setUseAjax(boolean isUseAjax) {
    this.isUseAjax = isUseAjax;
  }

  public String getURLGopage(String number) throws Exception {
    String type = (objectId.indexOf(Utils.FORUM_SERVICE) >= 0) ? Utils.FORUM_SERVICE : 
                  ((objectId.indexOf(Utils.CATEGORY) >= 0) ? ForumUtils.CATEGORY : 
                    ((objectId.indexOf(Utils.FORUM) >= 0) ? ForumUtils.FORUM : 
                      ((objectId.indexOf(Utils.TOPIC) >= 0) ? ForumUtils.TOPIC : 
                        (ForumUtils.EMPTY_STR))));
    String link = ForumUtils.createdForumLink(type, objectId + ForumUtils.SLASH + number, false);
    return link;
  }

  public List<String> getTotalpage() throws Exception {
    int max_Page = maxPage;
    if (this.pageSelect > max_Page)
      this.pageSelect = max_Page;
    int page = this.pageSelect;
    if (page <= 3) {
      beginTabPage = 1;
      if (max_Page <= 7)
        endTabPage = max_Page;
      else
        endTabPage = 7;
    } else {
      if (max_Page > (page + 3)) {
        endTabPage = (int) (page + 3);
        beginTabPage = (int) (page - 3);
      } else {
        endTabPage = max_Page;
        if (max_Page > 7)
          beginTabPage = max_Page - 6;
        else
          beginTabPage = 1;
      }
    }
    List<String> temp = new ArrayList<String>();
    for (int i = beginTabPage; i <= endTabPage; i++) {
      temp.add(ForumUtils.EMPTY_STR + i);
    }
    return temp;
  }

  public List<Integer> getInfoPage() throws Exception {
    List<Integer> temp = new ArrayList<Integer>();
    try {
      temp.add(pageList.getPageSize());
      temp.add(pageList.getCurrentPage());
      temp.add(pageList.getAvailable());
      temp.add(maxPage);
    } catch (Exception e) {
      temp.add(1);
      temp.add(1);
      temp.add(1);
      temp.add(1);
    }
    return temp;
  }

  public void setPageSelect(int page) {
    this.pageSelect = page;
  }

  public int getPageSelect() {
    return this.pageSelect;
  }

  public List<String> getIdSelected() throws Exception {
    List<UIComponent> children = this.getChildren();
    List<String> ids = new ArrayList<String>();
    for (int i = 0; i <= this.maxPage; i++) {
      if (pageCheckedList.get(i) != null)
        ids.addAll(pageCheckedList.get(i));
    }
    for (UIComponent child : children) {
      if (child instanceof UICheckBoxInput) {
        if (((UICheckBoxInput) child).isChecked()) {
          if (!ids.contains(child.getName()))
            ids.add(child.getName());
        }
      }
    }
    this.cleanCheckedList();
    return ids;
  }

  static public class GoPageActionListener extends EventListener<UIForumKeepStickPageIterator> {
    public void execute(Event<UIForumKeepStickPageIterator> event) throws Exception {
      UIForumKeepStickPageIterator keepStickPageIter = event.getSource();
      UIComponent component = keepStickPageIter;
      List<String> checkedList = new ArrayList<String>();
      try {
        List<UIComponent> children = keepStickPageIter.getChildren();
        for (UIComponent child : children) {
          if (child instanceof UICheckBoxInput) {
            if (((UICheckBoxInput) child).isChecked()) {
              checkedList.add(child.getId());
            }
          }
        }
      } catch (Exception e) {
        keepStickPageIter.log.warn("\nPage that reached is not exist: " + e.getMessage() + "\n" + e.getCause());
      }
      if (component instanceof UITopicDetail) {
        UITopicDetail topicDetail = (UITopicDetail) component;
        topicDetail.setIdPostView("top");
      }
      String stateClick = event.getRequestContext().getRequestParameter(OBJECTID).trim();
      int maxPage = keepStickPageIter.maxPage;
      int presentPage = keepStickPageIter.pageSelect;
      if (stateClick.equalsIgnoreCase("next")) {
        if (presentPage < maxPage) {
          keepStickPageIter.pageSelect = presentPage + 1;
        }
      } else if (stateClick.equalsIgnoreCase("previous")) {
        if (presentPage > 1) {
          keepStickPageIter.pageSelect = presentPage - 1;
        }
      } else if (stateClick.equalsIgnoreCase("last")) {
        if (presentPage != maxPage) {
          keepStickPageIter.pageSelect = maxPage;
        }
      } else if (stateClick.equalsIgnoreCase("first")) {
        if (presentPage != 1) {
          keepStickPageIter.pageSelect = 1;
        }
      } else {
        int temp = Integer.parseInt(stateClick);
        if (temp > 0 && temp <= maxPage && temp != presentPage) {
          keepStickPageIter.pageSelect = temp;
        }
      }
      keepStickPageIter.pageCheckedList.put(presentPage, checkedList);
      int checked = 0;
      for (int i = 1; i <= maxPage; i++) {
        if (keepStickPageIter.pageCheckedList.get(i) != null) {
          checked = checked + ((List<String>) keepStickPageIter.pageCheckedList.get(i)).size();
        }
      }
      keepStickPageIter.totalCheked = checked;
      event.getRequestContext().addUIComponentToUpdateByAjax(component);
    }
  }
}
