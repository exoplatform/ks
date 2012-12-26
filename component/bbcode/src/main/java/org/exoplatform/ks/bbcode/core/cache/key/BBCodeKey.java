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
package org.exoplatform.ks.bbcode.core.cache.key;

import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.common.cache.model.ScopeCacheKey;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Oct 4, 2012  
 */
public class BBCodeKey extends ScopeCacheKey {
  private final String bbCodeId;
  
  public BBCodeKey(String bbCodeId) {
    this.bbCodeId = bbCodeId;
  }
  
  public BBCodeKey(BBCode bbCode) {
    this.bbCodeId = bbCode.getId();
  }

  public String getBbCodeId() {
    return bbCodeId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BBCodeKey)) return false;
    if (!super.equals(o)) return false;

    BBCodeKey bbCodeKey = (BBCodeKey) o;

    if (bbCodeId != null ? !bbCodeId.equals(bbCodeKey.bbCodeId) : bbCodeKey.bbCodeId != null) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (bbCodeId != null ? bbCodeId.hashCode() : 0);
    return result;
  }
  

}
