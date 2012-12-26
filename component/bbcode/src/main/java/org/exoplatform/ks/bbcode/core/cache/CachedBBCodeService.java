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
package org.exoplatform.ks.bbcode.core.cache;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.api.BBCodeService;
import org.exoplatform.ks.bbcode.core.BBCodeServiceImpl;
import org.exoplatform.ks.bbcode.core.cache.data.BBCodeCacheData;
import org.exoplatform.ks.bbcode.core.cache.data.ListBBCodeData;
import org.exoplatform.ks.bbcode.core.cache.key.BBCodeKey;
import org.exoplatform.ks.bbcode.core.cache.model.CacheType;
import org.exoplatform.ks.bbcode.spi.BBCodePlugin;
import org.exoplatform.ks.common.cache.ServiceContext;
import org.exoplatform.ks.common.cache.model.key.SimpleCacheKey;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.future.FutureExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Oct 4, 2012  
 */
public class CachedBBCodeService implements Startable, BBCodeService {

  private static Log LOG = ExoLogger.getLogger(CachedBBCodeService.class);
  private CacheService service;
  private BBCodeService bbCodeService;
  
  
  //
  private ExoCache<BBCodeKey, BBCodeCacheData> bbCodeData;
  private ExoCache<SimpleCacheKey, ListBBCodeData> bbCodeListData;
  
  //
  private FutureExoCache<BBCodeKey, BBCodeCacheData, ServiceContext<BBCodeCacheData>> bbCodeDataFuture;
  private FutureExoCache<SimpleCacheKey, ListBBCodeData, ServiceContext<ListBBCodeData>> bbCodeListDataFuture;
  
  public CachedBBCodeService(CacheService service, BBCodeServiceImpl bbCodeService) {
    this.service = service;
    this.bbCodeService = bbCodeService;

  }
  
  private void clearBBCodeCached(String bbCodeId) {
    bbCodeData.remove(new BBCodeKey(bbCodeId));
  }
  
  private void clearBBCodeListCached() {
    SimpleCacheKey key = new SimpleCacheKey("bbcode", "forum.bbcode.list.key");
    bbCodeListData.remove(key);
  }
  
  @Override
  public void registerBBCodePlugin(BBCodePlugin plugin) throws Exception {
    bbCodeService.registerBBCodePlugin(plugin);
  }

  @Override
  public void save(List<BBCode> bbcodes) throws Exception {
    bbCodeService.save(bbcodes);
    
    //
    for(BBCode bb : bbcodes) {
      clearBBCodeCached(bb.getId());
    }
    
    clearBBCodeListCached();
  }

  @Override
  public List<BBCode> getAll() throws Exception {
    return bbCodeService.getAll();
  }

  @Override
  public List<String> getActive() throws Exception {
    
    SimpleCacheKey key = new SimpleCacheKey("bbcode", "forum.bbcode.list.key");

    return buildBBCodeIdOutput(bbCodeListDataFuture.get(
        new ServiceContext<ListBBCodeData>() {
          public ListBBCodeData execute() {
            try {
              List<BBCode> got = getBBCodeActive();
              return buildBBCodeInput(got);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        },
        key
    ));
  }
  
  @Override
  public List<BBCode> getBBCodeActive() throws Exception {
    return this.bbCodeService.getBBCodeActive();
  }

  @Override
  public BBCode findById(final String bbcodeId) throws Exception {
    return bbCodeDataFuture.get(
      new ServiceContext<BBCodeCacheData>() {
        public BBCodeCacheData execute() {
          try {
            BBCode got = bbCodeService.findById(bbcodeId);
            if (got != null) {
              return new BBCodeCacheData(got);
            } else {
              return BBCodeCacheData.NULL;
            }
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }, new BBCodeKey(bbcodeId)).build();
  }

  @Override
  public void delete(String bbcodeId) throws Exception {
    bbCodeService.delete(bbcodeId);
    
    //
    clearBBCodeCached(bbcodeId);   
    clearBBCodeListCached();
  }
  
  private ListBBCodeData buildBBCodeInput(List<BBCode> bbCodes) {
    List<BBCodeKey> data = new ArrayList<BBCodeKey>(bbCodes.size());
    for (BBCode p : bbCodes) {
      data.add(new BBCodeKey(p));
    }
    return new ListBBCodeData(data);
  }
  
  private List<String> buildBBCodeIdOutput(ListBBCodeData data) {

    if (data == null) {
      return null;
    }

    List<String> out = new ArrayList<String>(data.getIds().size());
    for (BBCodeKey k : data.getIds()) {
      try {
        out.add(findById(k.getBbCodeId()).getId());
      } catch (Exception e) {
        LOG.error(e);
      }
    }
    return out;

  }

  @Override
  public void start() {
    this.bbCodeData = CacheType.BBCODE_DATA.getFromService(this.service);
    this.bbCodeListData = CacheType.LIST_BBCODE_DATA.getFromService(this.service);
    
    //
    this.bbCodeDataFuture = CacheType.BBCODE_DATA.createFutureCache(bbCodeData);
    this.bbCodeListDataFuture = CacheType.LIST_BBCODE_DATA.createFutureCache(bbCodeListData);
  }

  @Override
  public void stop() {
    
    
  }

}
