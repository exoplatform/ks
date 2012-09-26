package org.exoplatform.ks.extras.injection.forum;

import java.util.Arrays;
import java.util.HashMap;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Topic;

public class MembershipInjector extends AbstractForumInjector {

  /** . */
  private static final String TYPE        = "type";

  /** . */
  private static final String TO_TYPE     = "toType";
  
  /** . */
  private static final String TYPE_PREFIX      = "typePrefix";

  /** . */
  private static final String FROM_USER   = "fromUser";

  /** . */
  private static final String TO_USER     = "toUser";

  /** . */
  private static final String USER_PREFIX = "userPrefix";
  
  private int      toType;
  private String   typePrefix;
  private int      fromUser;
  private int      toUser;
  private String   userPrefix;

  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    String type = params.get(TYPE);
    if (type == null | type.length() <= 0) {
      getLog().info("type value is wrong! Please set it exactly with 'category', 'forum', or 'topic' value. Aborting injection ...");
      return;
    }
    
    //
    this.toType = param(params, TO_TYPE);
    this.typePrefix = params.get(TYPE_PREFIX);
    this.fromUser = param(params, FROM_USER);
    this.toUser = param(params, TO_USER);
    this.userPrefix = params.get(USER_PREFIX);

    if ("category".equals(type)) {
      init(userPrefix, typePrefix, null, null, null, 0);
      
      //
      injectCategory();
    } else if ("forum".equals(type)) {
      init(userPrefix, null, typePrefix, null, null, 0);
      
      //
      injectForum();
    } else if ("topic".equals(type)) {
      init(userPrefix, null, null, typePrefix, null, 0);
      
      //
      injectTopic();
    } else {
      getLog().info("type value is wrong! Please set it exactly with 'category', 'forum', or'topic'. Aborting injection ...");
      return;
    }

  }
 
  private void injectCategory() throws Exception {
    //
    String categoryName = categoryBase + toType;
    Category cat = getCategoryByName(categoryName);
    if (cat == null) {
      getLog().info("category name is '" + categoryName
          + "' is wrong. Please set it exactly. Aborting injection ...");
      return;
    }
    
    //
    String[] userNames = getUserNames();
    if (userNames == null | userNames.length <= 0) {
      getLog().info("Don't assign permission any user to '" + categoryName + "' forum. Please set it exactly. Aborting injection ...");
      return;
    }

    //
    cat = forumService.getCategory(cat.getId());
    cat.setModerators(userNames);
    cat.setCreateTopicRole(userNames);
    cat.setViewer(userNames);
    cat.setPoster(userNames);
    
    
    forumService.saveCategory(cat, false);
    getLog().info("Assign permission '" + Arrays.toString(userNames) + "' user(s) into '" + categoryName + "' category.");
  }

  private void injectForum() throws Exception {
    //
    String forumName = forumBase + toType;
    Forum forum = getForumByName(forumName);
    if (forum == null) {
      getLog().info("forum name is '" + forumName + "' is wrong. Please set it exactly. Aborting injection ...");
      return;
    }
    Category cat = getCategoryByForumName(forumName);
    
    //
    String[] userNames = getUserNames();
    if (userNames == null | userNames.length <=0) {
      getLog().info("Don't assign permission any user to '" + forumName + "' forum. Please set it exactly. Aborting injection ...");
      return;
    }

    //
    forum = forumService.getForum(cat.getId(), forum.getId());
    forum.setModerators(userNames);
    forum.setCreateTopicRole(userNames);
    forum.setViewer(userNames);
    forum.setPoster(userNames);
    forumService.saveForum(cat.getId(), forum, false);
    
    getLog().info("Assign permission '" + Arrays.toString(userNames) + "' user(s) into '" + forumName + "' forum in '" + cat.getCategoryName() + "' category.");

  }

  private void injectTopic() throws Exception {
    //
    String topicName = topicBase + toType;
    Topic topic = getTopicByName(topicName);
    if (topic == null) {
      getLog().info("topic name is '" + topicName + "' is wrong. Please set it exactly. Aborting injection ...");
      return;
    }
    Forum forum = getForumByTopicName(topicName);
    Category cat = getCategoryByForumName(forum.getForumName());
    
    //
    String[] userNames = getUserNames();
    if (userNames == null | userNames.length <= 0) {
      getLog().info("Don't assign permission any user to '" + topicName + "' topic. Please set it exactly. Aborting injection ...");
      return;
    }
    
    //
    topic = forumService.getTopic(cat.getId(), forum.getId(), topic.getId(), null);
    topic.setCanPost(userNames);
    topic.setCanView(userNames);
    topic.setEmailNotification(userNames);
    forumService.saveTopic(cat.getId(),
                           forum.getId(),
                           topic,
                           false,
                           false,
                           new MessageBuilder());
    getLog().info("Assign permission '" + Arrays.toString(userNames) + "' user(s) into '" + topicName
                  + "' topic in '" + forum.getForumName() + "' forum.");
  }
  
  private String[] getUserNames() throws Exception {
    //
    String[] result = new String[toUser - fromUser + 1];
    int userIdx = 0;
    for(int i = fromUser; i <= toUser; i++)  {
      String username = userBase + i;
      if (userHandler.findUserByName(username) != null) {
        result[userIdx] = username;
        userIdx++;
      }
    }
    
    return result;
  }
}
