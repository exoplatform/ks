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
package org.exoplatform.wiki.rendering.cache.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.cache.MarkupData;
import org.exoplatform.wiki.rendering.cache.MarkupKey;
import org.exoplatform.wiki.rendering.cache.PageRenderingCacheService;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * May 17, 2012  
 */
public class PageRenderingCacheServiceImpl implements PageRenderingCacheService {
  
  public static final String              CACHE_NAME = "wiki.PageRenderingCache";
  
  private static final Log                LOG = ExoLogger.getLogger(PageRenderingCacheService.class);

  private RenderingService                renderingService;

  private WikiService                     wikiService;

  private ExoCache<MarkupKey, MarkupData> renderingCache;
  
  private Map<WikiPageParams, List<WikiPageParams>> pageLinksMap = new ConcurrentHashMap<WikiPageParams, List<WikiPageParams>>();

  /**
   * Initialize rendering cache service 
   * @param renderingService the rendering service
   */
  public PageRenderingCacheServiceImpl(RenderingService renderingService, WikiService wikiService, CacheService cacheService) {
    this.renderingService = renderingService;
    this.wikiService = wikiService;
    this.renderingCache = cacheService.getCacheInstance(CACHE_NAME);
  }
  
  @Override
  public String getRenderedContent(WikiPageParams param, String targetSyntax) {
    String renderedContent = StringUtils.EMPTY;
    try {
      PageImpl page = (PageImpl) wikiService.getPageById(param.getType(), param.getOwner(), param.getPageId());
      boolean supportSectionEdit = page.hasPermission(PermissionType.EDITPAGE);
      String markup = page.getContent().getText();
      MarkupKey key = new MarkupKey(new WikiPageParams(param.getType(), param.getOwner(), param.getPageId()),
                                    markup,
                                    page.getSyntax(),
                                    targetSyntax,
                                    supportSectionEdit);
      MarkupData cachedData = renderingCache.get(key);
      if (cachedData != null) {
        return cachedData.build();
      }
      renderedContent = renderingService.render(markup, page.getSyntax(), targetSyntax, supportSectionEdit);
      renderingCache.put(key, new MarkupData(renderedContent));
    } catch (Exception e) {
      LOG.error(String.format("Failed to get rendered content of page [%s:%s:%s] in syntax %s",
                              param.getType(),
                              param.getOwner(),
                              param.getPageId(),
                              targetSyntax), e);
    }
    return renderedContent;
  }

  @Override
  public final ExoCache<MarkupKey, MarkupData> getRenderingCache() {
    return renderingCache;
  }
  
  @Override
  public Map<WikiPageParams, List<WikiPageParams>> getPageLinksMap() {
    return pageLinksMap;
  }

  @Override
  public void addPageLink(WikiPageParams param, WikiPageParams entity) {
    List<WikiPageParams> linkParams = this.pageLinksMap.get(entity);
    if (linkParams == null) {
      linkParams = new ArrayList<WikiPageParams>();
      this.pageLinksMap.put(entity, linkParams);
    }
    linkParams.add(param);
  }

  @Override
  public void invalidateCache(WikiPageParams param) {
    List<WikiPageParams> linkedPages = pageLinksMap.get(param);
    if (linkedPages != null && linkedPages.size() != 0) {
      for (WikiPageParams wikiPageParams : linkedPages) {
        try {
          Page page = wikiService.getPageById(wikiPageParams.getType(), wikiPageParams.getOwner(), wikiPageParams.getPageId());
          MarkupKey key = new MarkupKey(wikiPageParams,
                                        page.getContent().getText(),
                                        page.getSyntax(),
                                        Syntax.XHTML_1_0.toIdString(),
                                        false);
          getRenderingCache().remove(key);
          key.setSupportSectionEdit(true);
          getRenderingCache().remove(key);
        } catch (Exception e) {
          LOG.warn(String.format("Failed to invalidate cache of page [%s:%s:%s]",
                                 wikiPageParams.getType(),
                                 wikiPageParams.getOwner(),
                                 wikiPageParams.getPageId()));
        }
      }
    }
  }

}
