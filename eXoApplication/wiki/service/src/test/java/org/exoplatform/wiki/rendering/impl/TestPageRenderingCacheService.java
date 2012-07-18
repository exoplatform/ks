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
package org.exoplatform.wiki.rendering.impl;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.cache.PageRenderingCacheService;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * May 17, 2012  
 */
public final class TestPageRenderingCacheService extends AbstractRenderingTestCase {
  
  private PageRenderingCacheService renderingCacheService;
  
  private WikiService               wikiService;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    renderingCacheService = (PageRenderingCacheService) container.getComponentInstanceOfType(PageRenderingCacheService.class);
    wikiService = (WikiService) container.getComponentInstanceOfType(WikiService.class);
  }
  
  public void testRenderingCache() throws Exception{
    PageImpl cladicHome = (PageImpl) wikiService.getPageById(PortalConfig.PORTAL_TYPE, "cladic", "WikiHome");
    cladicHome.getContent().setText("Sample content");
    renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE, "cladic", "WikiHome"),
                                             Syntax.XHTML_1_0.toIdString());
    assertEquals(1, renderingCacheService.getRenderingCache().getCacheSize());
    
    PageImpl ameHome = (PageImpl) wikiService.getPageById(PortalConfig.PORTAL_TYPE, "ame", "WikiHome");
    ameHome.getContent().setText("Sample content");
    renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE, "ame", "WikiHome"),
                                             Syntax.XHTML_1_0.toIdString());
    assertEquals(0, renderingCacheService.getRenderingCache().getCacheHit());
    
    
    renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE, "cladic", "WikiHome"),
                                             Syntax.XHTML_1_0.toIdString());
    assertEquals(1, renderingCacheService.getRenderingCache().getCacheHit());
    
    // Change the content of page
    cladicHome.getContent().setText("Another text");
    renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE, "cladic", "WikiHome"),
                                                       Syntax.XHTML_1_0.toIdString());
    assertEquals(1, renderingCacheService.getRenderingCache().getCacheHit());
    
    PageImpl cladicChild = (PageImpl) wikiService.createPage(PortalConfig.PORTAL_TYPE, "cladic","cladicChild" , "WikiHome");
    cladicHome.getContent().setText("{{children/}}");
    ameHome.getContent().setText("{{children/}}");
    cladicChild.getContent().setText("{{children/}}");
    setupWikiContext(new WikiPageParams(PortalConfig.PORTAL_TYPE, "cladic", "WikiHome"));
    String cladicHomeContent =  renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE, "cladic", "WikiHome"),
                                                                          Syntax.XHTML_1_0.toIdString());
    setupWikiContext(new WikiPageParams(PortalConfig.PORTAL_TYPE, "ame", "WikiHome"));
    String ameHomeContent =  renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE, "ame", "WikiHome"),
                                                                          Syntax.XHTML_1_0.toIdString());
    assertEquals(
        "<div><ul><li><span class=\"wikilink\"><a href=\"cladicChild\">cladicChild</a></span><ul></ul></li></ul></div>",
        cladicHomeContent); 
    assertTrue(!cladicHomeContent.equals(ameHomeContent));    
  }
  
  public void testInvalidateCache1() throws Exception {
    PageImpl ksdemoHome = (PageImpl) wikiService.getPageById(PortalConfig.PORTAL_TYPE, "ksdemo", "WikiHome");
    PageImpl intranetHome = (PageImpl) wikiService.getPageById(PortalConfig.PORTAL_TYPE, "intranet", "WikiHome");
    ksdemoHome.getContent().setText("{{children/}}");
    intranetHome.getContent().setText("{{children/}}");

    wikiService.createPage(PortalConfig.PORTAL_TYPE, "ksdemo", "cladicChild2", "WikiHome");
    setupWikiContext(new WikiPageParams(PortalConfig.PORTAL_TYPE, "ksdemo", "WikiHome"));
    String ksdemoHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                            "ksdemo",
                                                                                            "WikiHome"),
                                                                         Syntax.XHTML_1_0.toIdString());
    assertEquals("<div><ul><li><span class=\"wikilink\"><a href=\"cladicChild2\">cladicChild2</a></span><ul></ul></li></ul></div>",
                 ksdemoHomeContent);

    wikiService.renamePage(PortalConfig.PORTAL_TYPE, "ksdemo", "cladicChild2", "cladicChild3", "cladicChild3");
    ksdemoHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                     "ksdemo",
                                                                                     "WikiHome"), Syntax.XHTML_1_0.toIdString());
    assertEquals("<div><ul><li><span class=\"wikilink\"><a href=\"cladicChild3\">cladicChild3</a></span><ul></ul></li></ul></div>",
                 ksdemoHomeContent);

    wikiService.movePage(new WikiPageParams(PortalConfig.PORTAL_TYPE, "ksdemo", "cladicChild3"),
                         new WikiPageParams(PortalConfig.PORTAL_TYPE, "intranet", "WikiHome"));
    ksdemoHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                     "ksdemo",
                                                                                     "WikiHome"), Syntax.XHTML_1_0.toIdString());
    assertEquals("<div><ul></ul></div>",
                 ksdemoHomeContent);

    setupWikiContext(new WikiPageParams(PortalConfig.PORTAL_TYPE, "intranet", "WikiHome"));
    String intranetHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                         "intranet",
                                                                                         "WikiHome"),
                                                                      Syntax.XHTML_1_0.toIdString());
    assertEquals("<div><ul><li><span class=\"wikilink\"><a href=\"cladicChild3\">cladicChild3</a></span><ul></ul></li></ul></div>",
                 intranetHomeContent);

    wikiService.deletePage(PortalConfig.PORTAL_TYPE, "intranet", "cladicChild3");
    setupWikiContext(new WikiPageParams(PortalConfig.PORTAL_TYPE, "intranet", "WikiHome"));
    intranetHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                     "intranet",
                                                                                     "WikiHome"), Syntax.XHTML_1_0.toIdString());
    assertEquals("<div><ul></ul></div>", intranetHomeContent);
  }
  
  public void testInvalidateCache2() throws Exception {
    PageImpl acaHome = (PageImpl) wikiService.getPageById(PortalConfig.PORTAL_TYPE, "aca", "WikiHome");
    acaHome.getContent().setText("[[childaca]]");
    setupWikiContext(new WikiPageParams(PortalConfig.PORTAL_TYPE, "aca", "WikiHome"));
    String acaHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                             "aca",
                                                                                             "WikiHome"),
                                                                          Syntax.XHTML_1_0.toIdString());
    assertEquals("<p><span class=\"wikicreatelink\"><a href=\"WikiHome?action=AddPage&amp;pageTitle=childaca&amp;wiki=aca&amp;wikiType=portal\"><span class=\"wikigeneratedlinkcontent\">childaca</span></a></span></p>",
                 acaHomeContent);
    
    wikiService.createPage(PortalConfig.PORTAL_TYPE, "aca", "childaca", "WikiHome");
    acaHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                            "aca",
                                                                                            "WikiHome"),
                                                                         Syntax.XHTML_1_0.toIdString());
    assertEquals("<p><span class=\"wikilink\"><a href=\"childaca\"><span class=\"wikigeneratedlinkcontent\">childaca</span></a></span></p>",
                 acaHomeContent);
    wikiService.renamePage(PortalConfig.PORTAL_TYPE, "aca", "childaca", "childaca1", "childac1");

    acaHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                            "aca",
                                                                                            "WikiHome"),
                                                                         Syntax.XHTML_1_0.toIdString());
    assertEquals("<p><span class=\"wikilink\"><a href=\"childaca1\"><span class=\"wikigeneratedlinkcontent\">childaca</span></a></span></p>",
                 acaHomeContent);
    
    wikiService.movePage(new WikiPageParams(PortalConfig.PORTAL_TYPE, "aca", "childaca1"),
                         new WikiPageParams(PortalConfig.PORTAL_TYPE, "intranet", "WikiHome"));
    acaHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                 "aca",
                                                                                 "WikiHome"),
                                                              Syntax.XHTML_1_0.toIdString());
    assertEquals("<p><span class=\"wikicreatelink\"><a href=\"WikiHome?action=AddPage&amp;pageTitle=childaca&amp;wiki=aca&amp;wikiType=portal\"><span class=\"wikigeneratedlinkcontent\">childaca</span></a></span></p>",
                 acaHomeContent);
    
    wikiService.createPage(PortalConfig.PORTAL_TYPE, "aca", "childaca10", "WikiHome");
    acaHome.getContent().setText("[[childaca10]]");

    acaHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                 "aca",
                                                                                 "WikiHome"),
                                                              Syntax.XHTML_1_0.toIdString());
    assertEquals("<p><span class=\"wikilink\"><a href=\"childaca10\"><span class=\"wikigeneratedlinkcontent\">childaca10</span></a></span></p>",
                 acaHomeContent);
    
    wikiService.deletePage(PortalConfig.PORTAL_TYPE, "aca", "childaca10");
    acaHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                 "aca",
                                                                                 "WikiHome"),
                                                              Syntax.XHTML_1_0.toIdString());
    assertEquals("<p><span class=\"wikicreatelink\"><a href=\"WikiHome?action=AddPage&amp;pageTitle=childaca10&amp;wiki=aca&amp;wikiType=portal\"><span class=\"wikigeneratedlinkcontent\">childaca10</span></a></span></p>",
                 acaHomeContent);
    
  }
  
  public void testInvalidateCache3() throws Exception {
    PageImpl includePageHome = (PageImpl) wikiService.getPageById(PortalConfig.PORTAL_TYPE, "includepage", "WikiHome");
    includePageHome.getContent().setText("{{includepage page=\"child\"/}}");
    setupWikiContext(new WikiPageParams(PortalConfig.PORTAL_TYPE, "includepage", "WikiHome"));
    String includePageHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                                "includepage",
                                                                                                "WikiHome"),
                                                                             Syntax.XHTML_1_0.toIdString());
    assertEquals("", includePageHomeContent);
    
    PageImpl child = (PageImpl) wikiService.createPage(PortalConfig.PORTAL_TYPE, "includepage", "child", "WikiHome");
    child.getContent().setText("child content");
    child.checkin();
    child.checkout();
    includePageHomeContent = renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE,
                                                                                            "includepage",
                                                                                            "WikiHome"),
                                                                         Syntax.XHTML_1_0.toIdString());
    assertEquals("<div class=\"IncludePage \" ><p>child content</p></div>", includePageHomeContent);
  }
  
  
  @Override
  protected void tearDown() throws Exception {
    renderingCacheService.getRenderingCache().clearCache();
    super.tearDown();
  }
  
  private void setupWikiContext(WikiPageParams params) throws ComponentLookupException, ComponentRepositoryException {
    Execution ec = renderingService.getExecution();
    ec.setContext(new ExecutionContext());
    WikiContext wikiContext = new WikiContext();
    wikiContext.setType(params.getType());
    wikiContext.setOwner(params.getOwner());
    wikiContext.setPageId(params.getPageId());
    ec.getContext().setProperty(WikiContext.WIKICONTEXT, wikiContext);
  }

}
