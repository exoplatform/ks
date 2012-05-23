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
import org.exoplatform.forum.service.Forum;

/**
 * @author <a href="mailto:thanhvc@exoplatform.com">Thanh Vu</a>
 * @version $Revision$
 */
public class ForumInjector extends AbstractForumInjector {

  /** . */
  private static final String NUMBER = "number";

  /** . */
  private static final String TO_CAT = "toCat";

  /** . */
  private static final String FORUM_PREFIX = "forumPrefix";

  /** . */
  private static final String CATEGORY_PREFIX = "catPrefix";
  

  @Override
  public void inject(HashMap<String, String> params) throws Exception {

    //
    int number = param(params, NUMBER);
    int to = param(params, TO_CAT);
    String forumPrefix = params.get(FORUM_PREFIX);
    String categoryPrefix = params.get(CATEGORY_PREFIX);
    init(null, categoryPrefix, forumPrefix, null, null, 0);
    
    //
    String categoryName = categoryBase + to;
    Category cat = getCategoryByName(categoryName);
    if (cat == null) {
      getLog().info("category name is '" + categoryName + "' is wrong. Please set it exactly. Aborting injection ..." );
      return;
    }
    
    //
    for (int j = 0; j < number; ++j) {
      //
      String forumName = forumName();

      //
      Forum forum = new Forum();
      forum.setOwner(cat.getOwner());
      forum.setForumName(forumName);
      forum.setForumOrder(1);
      forum.setCreatedDate(new Date());
      forum.setModifiedBy(cat.getOwner());
      forum.setModifiedDate(new Date());
      forum.setLastTopicPath("");
      forum.setDescription(forumName + " description");
      forum.setPostCount(0);
      forum.setTopicCount(0);

      forum.setNotifyWhenAddTopic(new String[] {});
      forum.setNotifyWhenAddPost(new String[] {});
      forum.setIsModeratePost(false);
      forum.setIsModerateTopic(false);
      forum.setIsClosed(false);
      forum.setIsLock(false);

      forum.setViewer(new String[] {});
      forum.setCreateTopicRole(new String[] {});
      forum.setModerators(new String[] {});

      forumService.saveForum(cat.getId(), forum, true);
      ++forumNumber;

      //
      getLog().info("Forum '" + forumName + "' for catefgory '" + categoryName + "' created by "
          + cat.getOwner());

    }
    
  }

}
