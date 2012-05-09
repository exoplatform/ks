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
package org.exoplatform.wiki.mock;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * May 3, 2012
 */
public class MockUserACL extends UserACL {

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.UserACL#hasPermission(org.exoplatform.portal.config.model.Page)
   */
  @Override
  public boolean hasPermission(Page page) {
    return true;
  }

  /**
   * @param params
   */
  public MockUserACL(InitParams params) {
    super(params);
  }

}
