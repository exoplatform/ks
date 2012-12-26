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
package org.exoplatform.ks.bbcode.core.cache.data;

import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.common.cache.model.CachedData;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Oct 4, 2012  
 */
public class BBCodeCacheData implements CachedData<BBCode> {

  public final static BBCodeCacheData NULL = new BBCodeCacheData(new BBCode());
  
  private final String             id;

  private final String             tagName;

  private final String             replacement;

  private final String             description;

  private final String             example;

  private final boolean            isActive;

  private final boolean            isOption;
  
  public BBCodeCacheData(BBCode bbCode) {
    this.id = bbCode.getId();
    this.tagName = bbCode.getTagName();
    this.replacement = bbCode.getReplacement();
    this.description = bbCode.getDescription();
    this.example = bbCode.getExample();
    this.isActive = bbCode.isActive();
    this.isOption = bbCode.isOption();
  }
  
  
  @Override
  public BBCode build() {
    if (this == NULL) {
      return null;
    }
    BBCode bbCode = new BBCode();
    
    bbCode.setId(this.id); 
    bbCode.setTagName(this.tagName);
    bbCode.setReplacement(this.replacement);
    bbCode.setDescription(this.description);
    bbCode.setExample(this.example);
    bbCode.setOption(this.isOption);
    bbCode.setActive(this.isActive);
        
    return bbCode;
  }


  public String getId() {
    return id;
  }
  
  

}
