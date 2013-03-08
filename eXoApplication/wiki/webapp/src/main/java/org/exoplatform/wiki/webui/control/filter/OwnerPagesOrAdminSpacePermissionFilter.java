/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.wiki.webui.control.filter;

import java.util.Map;

import org.exoplatform.webui.ext.filter.UIExtensionAbstractFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilterType;
import org.exoplatform.wiki.commons.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : phongth
 *          phongth@exoplatform.com
 * Nov 06, 2012  
 */
public class OwnerPagesOrAdminSpacePermissionFilter extends UIExtensionAbstractFilter {

  public OwnerPagesOrAdminSpacePermissionFilter() {
    this(null);
  }

  public OwnerPagesOrAdminSpacePermissionFilter(String messageKey) {
    super(messageKey, UIExtensionFilterType.MANDATORY);
  }

  @Override
  public boolean accept(Map<String, Object> context) throws Exception {
    return Utils.canModifyPagePermission();
  }

  @Override
  public void onDeny(Map<String, Object> context) throws Exception {
  }
}
