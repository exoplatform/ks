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
package org.exoplatform.forum.extras.injection;

import java.util.Date;
import java.util.HashMap;

import org.exoplatform.forum.service.Category;

/**
 * @author <a href="mailto:thanhvc@exoplatform.com">Thanh Vu</a>
 * @version $Revision$
 */
public class CategoryInjector extends AbstractForumInjector {
  /** . */
  private static final String NUMBER = "number";

  /** . */
  private static final String FROM_USER = "fromUser";

  /** . */
  private static final String TO_USER = "toUser";

  /** . */
  private static final String USER_PREFIX = "userPrefix";

  /** . */
  private static final String CATEGORY_PREFIX = "catPrefix";

  @Override
  public void inject(HashMap<String, String> params) throws Exception {

    //
    int number = param(params, NUMBER);
    int from = param(params, FROM_USER);
    int to = param(params, TO_USER);
    String userPrefix = params.get(USER_PREFIX);
    String categoryPrefix = params.get(CATEGORY_PREFIX);
    init(userPrefix, categoryPrefix, null, null, null, 0);

    //
    for(int i = from; i <= to; ++i) {
      for (int j = 0; j < number; ++j) {

        //
        String owner = userBase + i;
        String categoryName = categoryName();

        Category cat = new Category(categoryName);
        cat.setOwner(owner);
        cat.setCategoryName(categoryName);
        cat.setCategoryOrder(i);
        cat.setCreatedDate(new Date());
        cat.setDescription(categoryName + " desciption");
        cat.setModifiedBy(owner);
        cat.setModifiedDate(new Date());
        
        forumService.saveCategory(cat, true);
        
        ++categoryNumber;

        //
        getLog().info("Category '" + categoryName + "' created by " + owner);

      }
    }
    
  }
}