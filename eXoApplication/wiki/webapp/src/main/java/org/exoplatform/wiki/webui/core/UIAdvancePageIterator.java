/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 */
package org.exoplatform.wiki.webui.core;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.wiki.webui.UIWikiAdvanceSearchForm;
import org.exoplatform.wiki.webui.UIWikiPortlet;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Oct 1, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIAdvancePageIterator.gtmpl",
  events = {
    @EventConfig(listeners = UIAdvancePageIterator.GoPageActionListener.class),
    @EventConfig(listeners = UIAdvancePageIterator.GoNumberPageActionListener.class)
  }
)
public class UIAdvancePageIterator extends UIForm {

  public static String PREVIOUS = "previous".intern();
  
  public static String NEXT = "next".intern();
  
  public static String FIRST = "first".intern();
  
  public static String LAST = "last".intern();
  
  public static String GOPAGE = "GoPage".intern();
  
  public static String GOTOPPAGE = "goPageTop".intern();
  
  private int beginPageRange = 0;
  
  private int endPageRange = 0;
  
  private int currentPage;

  public UIAdvancePageIterator(){
    addUIFormInput( new UIFormStringInput(GOTOPPAGE, null)) ;
    this.setSubmitAction("return false;");
  }
  
  public List<String> getDisplayedRange() throws Exception {
    UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
    UIWikiAdvanceSearchForm advanceSearchForm = wikiPortlet.findFirstComponentOfType(UIWikiAdvanceSearchForm.class);
    int maxPage = advanceSearchForm.getPageAvailable();
    long page = this.currentPage;
    if (page <= 3) {
      beginPageRange = 1;
      if (maxPage <= 7) {
        endPageRange = maxPage;
      } else {
        endPageRange = 7;
      }
    } else {
      if (maxPage > (page + 3)) {
        endPageRange = (int) (page + 3);
        beginPageRange = (int) (page - 3);
      } else {
        endPageRange = maxPage;
        if (maxPage > 7) {
          beginPageRange = maxPage - 6;
        } else {
          beginPageRange = 1;
        }
      }
    }
    List<String> temp = new ArrayList<String>();
    for (int i = beginPageRange; i <= endPageRange; i++) {
      temp.add(String.valueOf(i));
    }
    return temp;
  }
  
  public void setCurrentPage(int currentPage) {
    this.currentPage = currentPage;
  }
  
  public int getMaxPage() {
    UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
    UIWikiAdvanceSearchForm advanceSearchForm = wikiPortlet.findFirstComponentOfType(UIWikiAdvanceSearchForm.class);
    return advanceSearchForm.getPageAvailable();
  }

  static public class GoPageActionListener extends EventListener<UIAdvancePageIterator> {
    @Override
    public void execute(Event<UIAdvancePageIterator> event) throws Exception {
      UIAdvancePageIterator pageIterator = event.getSource();
      UIWikiPortlet wikiPortlet = pageIterator.getAncestorOfType(UIWikiPortlet.class);
      UIWikiAdvanceSearchForm advanceSearchForm = wikiPortlet.findFirstComponentOfType(UIWikiAdvanceSearchForm.class);
      
      String changeToPage = event.getRequestContext().getRequestParameter(OBJECTID).trim();
      int maxPage = advanceSearchForm.getPageAvailable();
      int presentPage = pageIterator.currentPage;
      if (UIAdvancePageIterator.NEXT.equalsIgnoreCase(changeToPage)) {
        if (presentPage < maxPage) {
          advanceSearchForm.gotoSearchPage(presentPage + 1);         
        }
      } else if (UIAdvancePageIterator.PREVIOUS.equalsIgnoreCase(changeToPage)) {
        if (presentPage > 1) {
          advanceSearchForm.gotoSearchPage(presentPage - 1);         
        }
      } else if (UIAdvancePageIterator.LAST.equalsIgnoreCase(changeToPage)) {
        if (presentPage != maxPage) {
          advanceSearchForm.gotoSearchPage(maxPage);        
        }
      } else if (UIAdvancePageIterator.FIRST.equalsIgnoreCase(changeToPage)) {
        if (presentPage != 1) {
          advanceSearchForm.gotoSearchPage(1);         
        }
      } else {
        int temp = Integer.parseInt(changeToPage);
        if (temp > 0 && temp <= maxPage && temp != presentPage) {
          advanceSearchForm.gotoSearchPage(temp);         
        }       
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(pageIterator.getParent());
    }
  }
  
  static public class GoNumberPageActionListener extends EventListener<UIAdvancePageIterator> {
    @Override
    public void execute(Event<UIAdvancePageIterator> event) throws Exception {
      UIAdvancePageIterator pageIterator = event.getSource();
      UIWikiPortlet wikiPortlet = pageIterator.getAncestorOfType(UIWikiPortlet.class);
      UIWikiAdvanceSearchForm advanceSearchForm = wikiPortlet.findFirstComponentOfType(UIWikiAdvanceSearchForm.class);
      
      UIFormStringInput stringInput = pageIterator.getUIStringInput(UIAdvancePageIterator.GOTOPPAGE) ;
      String numberPage = "" ;
      numberPage = stringInput.getValue() ;
      stringInput.setValue("") ;
      int maxPage = advanceSearchForm.getPageAvailable();
      int presentPage = pageIterator.currentPage;
      int page = 0;
      if (numberPage != null && numberPage.length() > 0) {
        try {
          page = Integer.parseInt(numberPage.trim());
          if (page < 0) {
            event.getRequestContext()
                 .getUIApplication()
                 .addMessage(new ApplicationMessage("NameValidator.msg.Invalid-number",
                                                    new String[] { pageIterator.getLabel(UIAdvancePageIterator.GOPAGE) },
                                                    ApplicationMessage.WARNING));
          } else {
            if (page == 0) {
              page = 1;
            } else if (page > maxPage) {
              page = maxPage;
            }
            advanceSearchForm.gotoSearchPage(page);
          }
        } catch (NumberFormatException e) {
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("NameValidator.msg.Invalid-number",
                                                  new String[] { pageIterator.getLabel(UIAdvancePageIterator.GOPAGE) },
                                                  ApplicationMessage.WARNING));          
        }
      }
      if (page > 0 && page <= maxPage && page != presentPage) {
        advanceSearchForm.gotoSearchPage(page);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(pageIterator.getParent());
    }
  }

}
