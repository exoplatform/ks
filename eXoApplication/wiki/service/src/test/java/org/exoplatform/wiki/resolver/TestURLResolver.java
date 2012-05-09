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
package org.exoplatform.wiki.resolver;


import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.wiki.mock.MockDataStorage;
import org.exoplatform.wiki.service.WikiPageParams;


public class TestURLResolver extends AbstractResolverTestcase {
  private URLResolver resolver ;
  
  public void setUp() throws Exception{
    super.setUp() ;
    OrganizationService orgservice = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class) ;
    resolver = new URLResolver(orgservice);
  }
  
  public void testResolvePortalURL() throws Exception{
    //http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/[$OWNER_TYPE/$OWNER]/$WIKI_PAGE_URI
    UserNode usernode = createUserNode(MockDataStorage.PORTAL_CLASSIC__WIKI[0], "wiki");
    String url = "http://hostname/$CONTAINER/$ACCESS/classic/wiki" ;
    WikiPageParams params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.PORTAL_TYPE, params.getType()) ;
    assertEquals("classic", params.getOwner()) ;
    assertEquals("WikiHome", params.getPageId()) ;
    
    url = "http://hostname/$CONTAINER/$ACCESS/classic/wiki/WikiHome" ;
    params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.PORTAL_TYPE, params.getType()) ;
    assertEquals("classic", params.getOwner()) ;
    assertEquals("WikiHome", params.getPageId()) ;
    
    url = "http://hostname/$CONTAINER/$ACCESS/classic/wiki/pageId" ;
    params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.PORTAL_TYPE, params.getType()) ;
    assertEquals("classic", params.getOwner()) ;
    assertEquals("pageId", params.getPageId()) ;
  }
  
  public void testResolveGroupURL() throws Exception{
    //http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/[$OWNER_TYPE/$OWNER]/$WIKI_PAGE_URI
    UserNode usernode = createUserNode(MockDataStorage.PORTAL_CLASSIC__WIKI[0], "wiki");
    String url = "http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/group/platform/" ;
    WikiPageParams params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.GROUP_TYPE, params.getType()) ;
    assertEquals("/platform", params.getOwner()) ;
    assertEquals("WikiHome", params.getPageId()) ;  
    
    
    url = "http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/group/platform" ;
    params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.GROUP_TYPE, params.getType()) ;
    assertEquals("/platform", params.getOwner()) ;
    assertEquals("WikiHome", params.getPageId()) ;
    
    
    url = "http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/group/platform/users/pageId/" ;
    params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.GROUP_TYPE, params.getType()) ;
    assertEquals("/platform/users", params.getOwner()) ;
    assertEquals("pageId", params.getPageId()) ;
    
    url = "http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/group/platform/users/pageId" ;
    params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.GROUP_TYPE, params.getType()) ;
    assertEquals("/platform/users", params.getOwner()) ;
    assertEquals("pageId", params.getPageId()) ;
    
    url = "http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/group/platform/users/WikiHome" ;
    params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.GROUP_TYPE, params.getType()) ;
    assertEquals("/platform/users", params.getOwner()) ;
    assertEquals("WikiHome", params.getPageId()) ;
    
    url = "http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/group/platform/users" ;
    params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.GROUP_TYPE, params.getType()) ;
    assertEquals("/platform/users", params.getOwner()) ;
    assertEquals("WikiHome", params.getPageId()) ;
  }
  
  public void testResolveUserURL() throws Exception{
    //http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/[$OWNER_TYPE/$OWNER]/$WIKI_PAGE_URI
    UserNode usernode = createUserNode(MockDataStorage.PORTAL_CLASSIC__WIKI[0], "wiki");
    String url = "http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/user/john" ;
    WikiPageParams params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.USER_TYPE, params.getType()) ;
    assertEquals("john", params.getOwner()) ;
    assertEquals("WikiHome", params.getPageId()) ;  
    
    url = "http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/user/john/" ;
    params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.USER_TYPE, params.getType()) ;
    assertEquals("john", params.getOwner()) ;
    assertEquals("WikiHome", params.getPageId()) ;
    
    url = "http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/user/john/WikiHome" ;
    params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.USER_TYPE, params.getType()) ;
    assertEquals("john", params.getOwner()) ;
    assertEquals("WikiHome", params.getPageId()) ;
    
    url = "http://hostname/$CONTAINER/$ACCESS/$SITE/wiki/user/john/WikiHome/" ;
    params = resolver.extractPageParams(url, usernode) ;
    assertEquals(PortalConfig.USER_TYPE, params.getType()) ;
    assertEquals("john", params.getOwner()) ;
    assertEquals("WikiHome", params.getPageId()) ;
    
  }
}
