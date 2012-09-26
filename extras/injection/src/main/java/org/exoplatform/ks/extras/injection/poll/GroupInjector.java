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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.extras.injection.poll;

import java.util.HashMap;

import org.exoplatform.services.organization.Group;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 13, 2012  
 */

public class GroupInjector extends AbstractPollInjector {

  public static final String NUMBER = "number";

  public static final String PREFIX = "prefix";

  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    int number = getIntParam(params, NUMBER);
    String prefix = getStringValueParam(params, PREFIX, DEFAULT_GROUP_PREFIX);
    init(prefix, null, null, null, null);

    for (int i = 1; i <= number; i++) {
      //
      String groupName = groupName();
      Group group = groupHandler.createGroupInstance();
      group.setGroupName(groupName);
      group.setLabel(groupName);
      group.setDescription(groupName);
      try {
        // Save group
        groupHandler.addChild(null, group, true);
        //
        ++groupNumber;

      } catch (Exception e) {
        getLog().error(e);
      }

      //
      getLog().info("Group '" + groupName + "' generated");
    }
  }

}
