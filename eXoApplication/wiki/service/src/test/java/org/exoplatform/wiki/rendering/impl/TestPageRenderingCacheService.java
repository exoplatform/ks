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
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
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
  
  /* (non-Javadoc)
   * @see org.exoplatform.wiki.rendering.impl.AbstractRenderingTestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    renderingCacheService = (PageRenderingCacheService) container.getComponentInstanceOfType(PageRenderingCacheService.class);
    wikiService = (WikiService) container.getComponentInstanceOfType(WikiService.class);
  }
  
  public void testRenderingCache() throws Exception{
    PageImpl page = (PageImpl) wikiService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "WikiHome");
    page.getContent().setText("Sample content");    
    renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE, "classic", "WikiHome"),
                                             Syntax.XHTML_1_0.toIdString());
    assertEquals(1, renderingCacheService.getRenderingCache().getCacheSize());
    renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE, "acme", "WikiHome"),
                                             Syntax.XHTML_1_0.toIdString());
    assertEquals(0, renderingCacheService.getRenderingCache().getCacheHit());
    renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE, "classic", "WikiHome"),
                                             Syntax.XHTML_1_0.toIdString());
    assertEquals(1, renderingCacheService.getRenderingCache().getCacheHit());
    
    // Change the content of page
    page.getContent().setText("Another text");
    renderingCacheService.getRenderedContent(new WikiPageParams(PortalConfig.PORTAL_TYPE, "classic", "WikiHome"),
                                                       Syntax.XHTML_1_0.toIdString());
    assertEquals(1, renderingCacheService.getRenderingCache().getCacheHit());
  }
  
  @Override
  protected void tearDown() throws Exception {
    renderingCacheService.getRenderingCache().clearCache();
    super.tearDown();
  }

}
