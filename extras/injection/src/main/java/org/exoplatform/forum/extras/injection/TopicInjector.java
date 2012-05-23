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
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Topic;

/**
 * @author <a href="mailto:thanhvc@exoplatform.com">Thanh Vu</a>
 * @version $Revision$
 */
public class TopicInjector extends AbstractForumInjector {

  /** . */
  private static final String NUMBER = "number";

  /** . */
  private static final String TOPIC_PREFIX = "topicPrefix";
  
  /** . */
  private static final String FROM_USER = "fromUser";

  /** . */
  private static final String TO_USER = "toUser";

  /** . */
  private static final String USER_PREFIX = "userPrefix";

  /** . */
  private static final String TO_FORUM = "toForum";

  /** . */
  private static final String FORUM_PREFIX = "forumPrefix";

  @Override
  public void inject(HashMap<String, String> params) throws Exception {

    //
    int number = param(params, NUMBER);
    String topicPrefix = params.get(TOPIC_PREFIX);
    
    //
    int fromUser = param(params, FROM_USER);
    int toUser = param(params, TO_USER);
    String userPrefix = params.get(USER_PREFIX);
    
    //
    int toForum = param(params, TO_FORUM);
    String forumPrefix = params.get(FORUM_PREFIX);
    init(userPrefix, null, forumPrefix, topicPrefix, null, 0);
    
    String forumName = forumBase + toForum;
    Forum forum = getForumByName(forumName);
    if (forum == null) {
      getLog().info("forum name is '" + forumName + "' wrong. Please set it exactly. Aborting injection ..." );
      return;
    }

    Category cat = getCategoryByForumName(forumName);
    
    //
    for (int i = fromUser; i <= toUser; ++i) {
      String owner = userBase + i;
      
      //
      for (int j = 0; j < number; ++j) {
        String topicName = topicName();

        Topic topicNew = new Topic();

        topicNew.setOwner(owner);
        topicNew.setTopicName(topicName);
        topicNew.setCreatedDate(new Date());
        topicNew.setModifiedBy(owner);
        topicNew.setModifiedDate(new Date());
        topicNew.setLastPostBy(owner);
        topicNew.setLastPostDate(new Date());
        topicNew.setDescription(topicName + " description");
        topicNew.setPostCount(0);
        topicNew.setViewCount(0);
        topicNew.setIsNotifyWhenAddPost("");
        topicNew.setIsModeratePost(false);
        topicNew.setIsClosed(false);
        topicNew.setIsLock(false);
        topicNew.setIsWaiting(false);
        topicNew.setIsActive(true);
        topicNew.setIcon("classNameIcon");
        topicNew.setIsApproved(true);
        topicNew.setCanView(new String[] {});
        topicNew.setCanPost(new String[] {});

        //
        forumService.saveTopic(cat.getId(),
                               forum.getId(),
                               topicNew,
                               true,
                               false,
                               new MessageBuilder());
        ++topicNumber;
        //
        getLog().info("Topic '" + topicName + "' in forum '" + forumName + "' created by " + owner);

      }

    }

  }

  
}
