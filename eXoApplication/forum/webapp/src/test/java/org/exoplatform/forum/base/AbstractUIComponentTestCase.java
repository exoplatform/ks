/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.base;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;

/**
 * Created by The eXo Platform SAS
 * Author : quangpld
 *          quangpld@exoplatform.com
 * Nov 6, 2012  
 */
public abstract class AbstractUIComponentTestCase<T extends UIComponent> extends AbstractWebUITestCase {
  
  protected T component;

  public AbstractUIComponentTestCase() throws Exception {
    super();
  }
  
  public void doSetUp() {  
    try {
      this.component = createComponent();
      this.component.setParent(new MockUIApplication());
    } catch (Exception e) {
      fail("Failed to initialize UIComponent: " + e.getMessage());
    }
  }
  
  protected void assertApplicationMessage(String key) {
    UIApplication app = component.getAncestorOfType(UIApplication.class);
    boolean found = false;
    for (ApplicationMessage message : app.getUIPopupMessages().getWarnings()) {
      if(key.equals(message.getMessageKey())) {
        found = true;
      }
    }
    
    for (ApplicationMessage message : app.getUIPopupMessages().getInfos()) {
      if(key.equals(message.getMessageKey())) {
        found = true;
      }
    }
    
    for (ApplicationMessage message : app.getUIPopupMessages().getErrors()) {
      if(key.equals(message.getMessageKey())) {
        found = true;
      }
    }
    
    assertTrue("Message not found <" + key + ">", found);
  }
  
  protected abstract T createComponent() throws Exception;
  
  /**
   * MockUIApplication class.
   *
   */
  protected class MockUIApplication extends UIApplication {
    public MockUIApplication() throws Exception {
      super();
    }
  }
}
