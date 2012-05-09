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
package org.exoplatform.wiki;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.service.WikiPageParams;
import org.mockito.Mockito;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * May 4, 2012  
 */
public class TestUtils extends TestCase {

  public void testGetURLFromParams() throws Exception {
    // Resolve a url of a site wiki page
    String requestURI = "http://hostname/portal/classic/wiki";
    injectWebRequestContext(requestURI, "/portal/classic/", SiteType.PORTAL, "classic", "wiki");
    WikiPageParams params = new WikiPageParams(PortalConfig.PORTAL_TYPE, "classic", "test");
    String expectedURL = "http://hostname/portal/classic/wiki/test";
    String actualURL = Utils.getURLFromParams(params);
    assertEquals(expectedURL, actualURL);

    requestURI = "http://hostname/portal/acme/wiki";
    injectWebRequestContext(requestURI, "/portal/acme/", SiteType.PORTAL, "acme", "wiki");
    params = new WikiPageParams(PortalConfig.PORTAL_TYPE, "classic", "test");
    expectedURL = "http://hostname/portal/acme/wiki/portal/classic/test";
    actualURL = Utils.getURLFromParams(params);
    assertEquals(expectedURL, actualURL);

    requestURI = "http://hostname/portal/u/demo/wiki";
    injectWebRequestContext(requestURI, "/portal/u/demo/", SiteType.USER, "demo", "wiki");
    params = new WikiPageParams(PortalConfig.GROUP_TYPE, ":spaces:test", "test");
    expectedURL = "http://hostname/portal/u/demo/wiki/group/:spaces:test/test";
    actualURL = Utils.getURLFromParams(params);
    assertEquals(expectedURL, actualURL);

    requestURI = "http://hostname/portal/g/:spaces:test/test/wiki";
    injectWebRequestContext(requestURI, "/portal/g/:spaces:test/", SiteType.GROUP, ":spaces:test", "test/wiki");
    params = new WikiPageParams(PortalConfig.PORTAL_TYPE, "classic", "test");
    expectedURL = "http://hostname/portal/g/:spaces:test/test/wiki/portal/classic/test";
    actualURL = Utils.getURLFromParams(params);
    assertEquals(expectedURL, actualURL);

    // Resolve a url of a group wiki page
    requestURI = "http://hostname/portal/classic/wiki";
    injectWebRequestContext(requestURI, "/portal/classic/", SiteType.PORTAL, "classic", "wiki");
    params = new WikiPageParams(PortalConfig.GROUP_TYPE, "guest", "test");
    expectedURL = "http://hostname/portal/classic/wiki/group/guest/test";
    actualURL = Utils.getURLFromParams(params);
    assertEquals(expectedURL, actualURL);

    requestURI = "http://hostname/portal/u/demo/wiki";
    injectWebRequestContext(requestURI, "/portal/u/demo/", SiteType.USER, "demo", "wiki");
    params = new WikiPageParams(PortalConfig.PORTAL_TYPE, "classic", "test");
    expectedURL = "http://hostname/portal/u/demo/wiki/portal/classic/test";
    actualURL = Utils.getURLFromParams(params);
    assertEquals(expectedURL, actualURL);

    requestURI = "http://hostname/portal/g/:spaces:test/test/wiki";
    injectWebRequestContext(requestURI, "/portal/g/:spaces:test/", SiteType.GROUP, ":spaces:test", "test/wiki");
    params = new WikiPageParams(PortalConfig.GROUP_TYPE, ":spaces:test", "test");
    expectedURL = "http://hostname/portal/g/:spaces:test/test/wiki/test";
    actualURL = Utils.getURLFromParams(params);
    assertEquals(expectedURL, actualURL);

    requestURI = "http://hostname/portal/g/:spaces:test/test/wiki";
    injectWebRequestContext(requestURI, "/portal/g/:spaces:test/", SiteType.GROUP, ":spaces:test", "test/wiki");
    params = new WikiPageParams(PortalConfig.GROUP_TYPE, "guest", "test");
    expectedURL = "http://hostname/portal/g/:spaces:test/test/wiki/group/guest/test";
    actualURL = Utils.getURLFromParams(params);
    assertEquals(expectedURL, actualURL);

    // Resolve a url of a user wiki page
    requestURI = "http://hostname/portal/classic/wiki";
    injectWebRequestContext(requestURI, "/portal/classic/", SiteType.PORTAL, "classic", "wiki");
    params = new WikiPageParams(PortalConfig.USER_TYPE, "demo", "test");
    expectedURL = "http://hostname/portal/classic/wiki/user/demo/test";
    actualURL = Utils.getURLFromParams(params);
    assertEquals(expectedURL, actualURL);
    
    requestURI = "http://hostname/portal/u/demo/wiki";
    injectWebRequestContext(requestURI, "/portal/u/demo/", SiteType.USER, "demo", "wiki");
    params = new WikiPageParams(PortalConfig.USER_TYPE, "demo", "test");
    expectedURL = "http://hostname/portal/u/demo/wiki/test";
    actualURL = Utils.getURLFromParams(params);
    assertEquals(expectedURL, actualURL);
    
    requestURI = "http://hostname/portal/u/demo/wiki";
    injectWebRequestContext(requestURI, "/portal/u/demo/", SiteType.USER, "demo", "wiki");
    params = new WikiPageParams(PortalConfig.USER_TYPE, "mary", "test");
    expectedURL = "http://hostname/portal/u/demo/wiki/user/mary/test";
    actualURL = Utils.getURLFromParams(params);
    assertEquals(expectedURL, actualURL);
    
    requestURI = "http://hostname/portal/g/:spaces:test/test/wiki";
    injectWebRequestContext(requestURI, "/portal/g/:spaces:test/", SiteType.GROUP, ":spaces:test", "test/wiki");
    params = new WikiPageParams(PortalConfig.USER_TYPE, "demo", "test");
    expectedURL = "http://hostname/portal/g/:spaces:test/test/wiki/user/demo/test";
    actualURL = Utils.getURLFromParams(params);
    assertEquals(expectedURL, actualURL);
    
  }
  
  private void injectWebRequestContext(String requestURI, String getPortalURI, SiteType siteType, String siteName, String pageURI) throws Exception {
    PortalRequestContext webuiContext = Mockito.mock(PortalRequestContext.class);
    ControllerContext controllerContext = Mockito.mock(ControllerContext.class);
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    UIPortalApplication uiApplication = Mockito.mock(UIPortalApplication.class);
    UIPortal uiPortal = Mockito.mock(UIPortal.class);
    SiteKey siteKey = new SiteKey(siteType, siteName);
    UserNode userNode = Mockito.mock(UserNode.class);

    Mockito.when(userNode.getURI()).thenReturn(pageURI);
    Mockito.when(uiPortal.getSiteKey()).thenReturn(siteKey);
    Mockito.when(uiPortal.getSelectedUserNode()).thenReturn(userNode);
    Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer(requestURI));
    Mockito.when(uiApplication.getCurrentSite()).thenReturn(uiPortal);
    Mockito.when(webuiContext.getUIApplication()).thenReturn(uiApplication);

    Mockito.when(controllerContext.getRequest()).thenReturn(request);
    Mockito.when(webuiContext.getControllerContext()).thenReturn(controllerContext);
    Mockito.when(webuiContext.getPortalURI()).thenReturn(getPortalURI);
    WebuiRequestContext.setCurrentInstance(webuiContext);
  }
}
