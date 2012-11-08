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
package org.exoplatform.ks.common.base;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.application.URLBuilder;
import org.exoplatform.web.url.PortalURL;
import org.exoplatform.web.url.ResourceType;
import org.exoplatform.web.url.URLFactory;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : quangpld
 *          quangpld@exoplatform.com
 * Nov 6, 2012  
 */
public abstract class AbstractWebUITestCase extends TestCase {
  
  protected MockWebuiApplication webuiApplication;
  
  public AbstractWebUITestCase() throws Exception {
    webuiApplication = new MockWebuiApplication();
    webuiApplication.setResourceBundle(new MockResourceBundle(new HashMap<String, Object>()));
  }
  
  public final void setUp() throws Exception {
    // init request
    MockWebUIRequestContext context = new MockWebUIRequestContext(new MockParentRequestContext(null, null), webuiApplication);
    WebuiRequestContext.setCurrentInstance(context);
    
    //
    doSetUp();
  }
  
  protected void setResourceBundleEntry(String key, String value) {
    //
    MockResourceBundle mockResourceBundle = null;
    try {
      mockResourceBundle = (MockResourceBundle) webuiApplication.getResourceBundle(null);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    
    //
    if (mockResourceBundle != null) mockResourceBundle.put(key, value);
  }
  
  protected abstract void doSetUp();
  
  /**
   * MockWebuiApplication class.
   * 
   */
  protected class MockWebuiApplication extends WebuiApplication {
    
    ResourceBundle rb;
    
    @Override
    public String getApplicationInitParam(String name) {
      return null;
    }

    @Override
    public String getApplicationGroup() {
      return null;
    }

    @Override
    public String getApplicationId() {
      return null;
    }

    @Override
    public String getApplicationName() {
      return null;
    }

    @Override
    public String getApplicationType() {
      return null;
    }

    @Override
    public ResourceBundle getOwnerResourceBundle(String username, Locale locale) throws Exception {
      return null;
    }

    @Override
    public ResourceBundle getResourceBundle(Locale locale) throws Exception {
      return rb;
    }
    
    public void setResourceBundle(ResourceBundle rb) {
      this.rb = rb;
    }
    
    public <T extends UIComponent> T createUIComponent(Class<T> type, String configId, String id, WebuiRequestContext context) throws Exception {
      return type.getConstructor().newInstance();
    }
  }
  
  /**
   * MockResourceBundle class.
   * 
   */
  protected class MockResourceBundle extends ResourceBundle {

    protected Map<String, Object> content;

    public MockResourceBundle(Map<String, Object> content) {
      this.content = new HashMap<String, Object>(content);
    }

    protected Object handleGetObject(String key) {
      if (key == null) throw new IllegalArgumentException("null hey is not allowed");
      return content.get(key);
    }

    public Enumeration<String> getKeys() {
      return Collections.enumeration(content.keySet());
    }

    public void put(String key, String value) {
      content.put(key, value);
    }
  }
  
  /**
   * MockWebUIRequestContext class.
   *
   */
  protected class MockWebUIRequestContext extends WebuiRequestContext {

    public MockWebUIRequestContext(RequestContext parentAppRequestContext, Application app) {
      super(parentAppRequestContext, app);
    }

    @Override
    public <T> T getRequest() throws Exception {
      return null;
    }

    public String getPortalContextPath(){
      return null ;
    }
    
    @Override
    public String getRequestContextPath() {
      return null;
    }

    @Override
    public <T> T getResponse() throws Exception {
      return null;
    }

    @Override
    public void sendRedirect(String url) throws Exception {
    }

    @Override
    public Orientation getOrientation() {
      return null;
    }

    @Override
    public String getRequestParameter(String name) {
      return null;
    }

    @Override
    public String[] getRequestParameterValues(String name) {
      return new String[0];
    }

    @Override
    public URLBuilder<UIComponent> getURLBuilder() {
      return null;
    }

    @Override
    public boolean useAjax() {
      return false;
    }

    @Override
    public URLFactory getURLFactory() {
      return null;
    }

    @Override
    public <R, U extends PortalURL<R, U>> U newURL(ResourceType<R, U> resourceType, URLFactory urlFactory) {
      return null;
    }

    @Override
    public UserPortal getUserPortal() {
      return null;
    }
  }
  
  /**
   * MockParentRequestContext class.
   *
   */
  protected class MockParentRequestContext extends RequestContext {

    public MockParentRequestContext(RequestContext parentAppRequestContext, Application app) {
      super(parentAppRequestContext, app);
    }

    @Override
    public Orientation getOrientation() {
      return null;
    }

    @Override
    public String getRequestParameter(String name) {
      return null;
    }

    @Override
    public String[] getRequestParameterValues(String name) {
      return new String[0];
    }

    @Override
    public URLBuilder<?> getURLBuilder() {
      return null;
    }
    
    public Locale getLocale() {
       return null;
    }

    @Override
    public boolean useAjax() {
      return false;
    }

    @Override
    public URLFactory getURLFactory() {
      return null;
    }

    @Override
    public <R, U extends PortalURL<R, U>> U newURL(ResourceType<R, U> resourceType, URLFactory urlFactory) {
      return null;
    }

    @Override
    public UserPortal getUserPortal() {
      return null;
    }
  }
}
