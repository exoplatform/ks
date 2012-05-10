/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.ks.bench;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.NodeIterator;

import org.apache.commons.collections.ListUtils;
import org.exoplatform.forum.service.BufferAttachment;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * <p>
 * Plugin for injecting Forum data.
 * </p>
 * 
 * Created by The eXo Platform SAS
 * @Author : <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a>
 * Jul 21, 2011  
 */
public class ForumDataInjector extends DataInjector {

  private static Log         log         = ExoLogger.getLogger(ForumDataInjector.class);

  private static final String ARRAY_SPLIT = ",";
  
  private static final String UNDER_SCORE = "_";

  private static final String PREFIX_ID = "datainjectorprefixid";

  private ForumService       forumService;
  
  private KSDataLocation     dataLocation;
  
  private enum ENTITY{
    CATEGORY, FORUM, TOPIC, POST, ATTACHMENT
  }

  public ForumDataInjector(ForumService forumService, KSDataLocation dataLocation) {
    this.forumService = forumService;
    this.dataLocation = dataLocation;
  }
  
  @Override
  public Log getLog() {
    return log;
  }
  
  @Override
  public void inject(HashMap<String, String> queryParams) throws Exception {
    String type = queryParams.get("type");
    if ("data".equals(type)) {
      log.info("Injecting data ...");
      injectData(queryParams);
    } else if ("perm".equals(type)) {
      log.info("Injecting permissions ...");
      injectPermission(queryParams);
    } else {
      log.info(String.format("Do not support type %s for injector...", type));
    }
  }
  
  
  public void injectData(HashMap<String, String> queryParams) throws Exception {
    long categoriesCount = 0;
    long forumsCount = 0;
    long topicsCount = 0;
    long postCount = 0;
    long categoriesWeight = 0;
    List<Integer> itemsQu = readQuantities(queryParams);
    List<String> itemsPre = readPrefixes(queryParams);    
    if (itemsPre.size() == 0 || itemsQu.size() == 0) {
      throw new RuntimeException("The size of parametes is zero");
    }
    // itemsQu = getTotalItemQu(itemsPre, itemsQu);
    List<Category> categories = generatesCategories(itemsPre.get(0), itemsQu.get(0));
    for (Category category : categories) {
      if (forumService.getCategory(category.getId()) == null) {
        forumService.saveCategory(category, true);
        categoriesCount++;
      }
      String categoryId = category.getId();
      long forumsWeight = 0;
      int forumNum = 0;
      if (itemsQu.size() > 1) {
        List<Forum> forums = generateForums(itemsPre.get(1), itemsQu.get(1));
        log.info("Category " + categoriesCount + "/" + categories.size() + " with " + forums.size() + " forums");
        for (Forum forum : forums) {
          if (forumService.getForum(categoryId, forum.getId()) == null) {
            forumService.saveForum(categoryId, forum, true);
            forumsCount++;
          }
          String forumId = forum.getId();
          long topicsWeight = 0;
          int topicNum = 0;
          if (itemsQu.size() > 2) {

            List<Topic> topics = generateTopics(itemsPre.get(2), itemsQu.get(2));
            log.info("\tForum " + (++forumNum) + "/" + forums.size() + " with " + topics.size() + " topics");
            for (Topic topic : topics) {
              if (forumService.getTopic(categoryId, forumId, topic.getId(), "root") == null) {
                forumService.saveTopic(categoryId, forumId, topic, true, false, new MessageBuilder());
                // log.info("Created topic " + topic.getTopicName());
                topicsCount++;
              }
              String topicId = topic.getId();
              long postsWeight = 0;
              long t1 = System.currentTimeMillis();
              if (itemsQu.size() > 3) {
                List<Post> posts = generatePosts(itemsPre.get(3), itemsQu.get(3));
                // log.info("Initializing new topic with "+ posts.size()+
                // " posts");

                for (Post post : posts) {
                  if (forumService.getPost(categoryId, forumId, topicId, post.getId()) == null) {
                    forumService.savePost(categoryId, forumId, topicId, post, true, new MessageBuilder());
                    long messageWeight = post.getMessage().length() * 2; // in
                    postCount++;
                    postsWeight += messageWeight;                   
                  }
                  if (itemsQu.size() > 4) {
                    int attSize = Integer.parseInt(queryParams.get("attSize"));
                    List<ForumAttachment> atts = generateAttachments(itemsPre.get(4), itemsQu.get(4), attSize);
                    post.setAttachments(atts);
                    forumService.savePost(categoryId, forumId, topicId, post, false, new MessageBuilder());
                  }
                }
                double elapsed = (System.currentTimeMillis() - t1);
                double rate = ((postsWeight / 1024) / (elapsed / 1000));
                String srate = MessageFormat.format("({0,number,#.#} KB/s)", rate);
                log.info("\t\tTopic " + (++topicNum) + "/" + topics.size() + "\t" + posts.size() + " posts in " + elapsed + "ms "
                    + srate);

                topicsWeight += postsWeight;
              }
            } // end topics loop

            log.info("\t\t " + topics.size() + " topics " + MessageFormat.format("({0,number,#.#} KB)", (topicsWeight / 1024))
                + " total posts=" + postCount);
            forumsWeight += topicsWeight;

          }
        }
        log.info("\t" + forums.size() + " forums " + MessageFormat.format("({0,number,#.#} K)", (forumsWeight / 1024))
            + " total posts=" + postCount);
        categoriesWeight += forumsWeight;
      }
    }
    log.info("INITIALIZED : categories=" + categories.size() + " / forums=" + forumsCount + " / topics=" + topicsCount
        + " / posts=" + postCount + MessageFormat.format(" ({0,number,#.#} KB)", (categoriesWeight / 1024)));
  }
  
  @SuppressWarnings("unchecked")
  public void injectPermission(HashMap<String, String> queryParams) throws Exception {
    List<String> prefixes = readPrefixes(queryParams);
    if (prefixes.size() == 3) {
      boolean isCanView = false;
      boolean isCanPost = false;
      String permString = queryParams.get("perm");
      List<String> member = readUsersIfExist(queryParams);
      List<String> groups = readGroupsIfExist(queryParams);
      List<String> memberships = readMembershipIfExist(queryParams);
      List<String> mergeMembers = ListUtils.union(ListUtils.union(member, groups), memberships);
      String[] memberArray = new String[mergeMembers.size()];
      for (int i = 0; i < mergeMembers.size(); i++) {
        memberArray[i] = mergeMembers.get(i);
      }
      List<String> itemIds = search(prefixes.get(2), ENTITY.TOPIC);
      if (Integer.parseInt(permString.substring(0, 1)) > 0) {
        isCanView = true;
      }
      if (Integer.parseInt(permString.substring(1, 2)) > 0) {
        isCanPost = true;
      }
      for (int i = 0; i < itemIds.size(); i++) {
        String[] ids = itemIds.get(i).split("/");
        int l = ids.length;
        Topic topic = forumService.getTopic(ids[l - 3], ids[l - 2], ids[l - 1], "root");
        if (isCanView) {
          topic.setCanView(memberArray);
        }
        if (isCanPost) {
          topic.setCanPost(memberArray);
        }
        forumService.saveTopic(ids[l - 3], ids[l - 2], topic, false, false, new MessageBuilder());
      }
    } else {
      throw new IllegalArgumentException("Prefix item is not a topic prefix. It should be [category_prefix],[forum_prefix],[topic_prefix]");
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void reject(HashMap<String, String> queryParams) throws Exception {
    List<String> prefixes = readPrefixes(queryParams);
    List<String> itemIds = new ArrayList<String>();
    switch (prefixes.size()) {
    case 5:
      itemIds = search(prefixes.get(prefixes.size()-1), ENTITY.ATTACHMENT);
      for (int i = 0; i < itemIds.size(); i++) {
        String[] ids = itemIds.get(i).split("/");
        int l = ids.length;
        Post post = forumService.getPost(ids[l - 6], ids[l - 5], ids[l - 4], ids[l - 3]);
        List<ForumAttachment> atts = post.getAttachments();
        List<ForumAttachment> toRemoveAtts = new ArrayList<ForumAttachment>();
        for (ForumAttachment att : atts) {
          if (att.getId().equals(ids[l - 2]))
            toRemoveAtts.add(att);
        }
        atts = ListUtils.subtract(atts, toRemoveAtts);
        post.setAttachments(atts);
        forumService.savePost(ids[l - 6], ids[l - 5], ids[l - 4], post, false, new MessageBuilder());
      }
      break;
    case 4:
      itemIds = search(prefixes.get(prefixes.size()-1), ENTITY.POST);
      for (int i = 0; i < itemIds.size(); i++) {
        String[] ids = itemIds.get(i).split("/");
        int l = ids.length;
        forumService.removePost(ids[l - 4], ids[l - 3], ids[l - 2], ids[l - 1]);
      }
      break;
    case 3:
      itemIds = search(prefixes.get(prefixes.size()-1), ENTITY.TOPIC);
      for (int i = 0; i < itemIds.size(); i++) {
        String[] ids = itemIds.get(i).split("/");
        int l = ids.length;
        forumService.removeTopic(ids[l - 3], ids[l - 2], ids[l - 1]);
      }
      break;
    case 2:
      itemIds = search(prefixes.get(prefixes.size()-1), ENTITY.FORUM);
      for (int i = 0; i < itemIds.size(); i++) {
        String[] ids = itemIds.get(i).split("/");
        int l = ids.length;
        forumService.removeForum(ids[l - 2], ids[l - 1]);
      }
      break;
    case 1:
      itemIds = search(prefixes.get(prefixes.size()-1), ENTITY.CATEGORY);
      for (int i = 0; i < itemIds.size(); i++) {
        String[] ids = itemIds.get(i).split("/");
        int l = ids.length;
        forumService.removeCategory(ids[l - 1]);
      }
      break;
    default:
      break;
    }
  }
  
  private List<Integer> readQuantities(HashMap<String, String> queryParams) {
    String quantitiesString = queryParams.get("q");
    List<Integer> quantities = new LinkedList<Integer>();
    for (String s : quantitiesString.split(ARRAY_SPLIT)) {
      if (s.length() > 0) {
        int quantity = Integer.parseInt(s.trim());
        quantities.add(quantity);
      }
    }
    return quantities;
  }
  
  private List<String> readPrefixes(HashMap<String, String> queryParams) {
    String prefixesString = queryParams.get("pre");
    List<String> prefixes = new LinkedList<String>();
    for (String s : prefixesString.split(ARRAY_SPLIT)) {
      if (s.length() > 0) {
        prefixes.add(s);
      }
    }
    return prefixes;
  }

  private List<Category> generatesCategories(String prefix, int cateQu) {
    List<Category> result = new ArrayList<Category>();
    try {
      for (int i = 0; i < cateQu; i++) {
        Category category = new Category();
        category.setId(generateParam(prefix, Utils.CATEGORY, i, true));
        category.setCategoryName(generateParam(prefix, "Category", i, false));
        category.setCategoryOrder(i);
        category.setCreatedDate(new Date());
        category.setDescription(randomWords(10));
        category.setModifiedBy(randomUser());
        category.setModifiedDate(new Date());
        category.setOwner(randomUser());
        result.add(category);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  private List<Forum> generateForums(String prefix, int forumQu) {
    List<Forum> result = new ArrayList<Forum>();
    try {
      for (int i = 0; i < forumQu; i++) {
        Forum forum = new Forum();
        forum.setId(generateParam(prefix, Utils.FORUM, i, true));
        forum.setForumName(generateParam(prefix, Utils.FORUM, i, false));
        forum.setCreatedDate(new Date());
        forum.setDescription(randomWords(10));        
        forum.setForumOrder(i);
        forum.setOwner(randomUser());
        result.add(forum);
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<Topic> generateTopics(String prefix, int topicQu) {
    List<Topic> result = new ArrayList<Topic>();
    try {
      for (int i = 0; i < topicQu; i++) {
        Topic topic = new Topic();
        topic.setId(generateParam(prefix, Utils.TOPIC, i, true));
        topic.setTopicName(generateParam(prefix, Utils.TOPIC, i, false));
        topic.setCreatedDate(new Date());
        topic.setDescription(randomWords(10));
        topic.setOwner(randomUser());
        topic.setCanPost(new String[] { "" });
        topic.setIcon(ForumDataRandom.getClassIcon());
        result.add(topic);
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<Post> generatePosts(String prefix, int postQu) {
    List<Post> result = new ArrayList<Post>();
    for (int i = 0; i < postQu; i++) {
      Post post = new Post();
      post.setId(generateParam(prefix, Utils.POST, i, true));
      post.setName(generateParam(prefix, Utils.POST, i, false));
      post.setMessage(randomParagraphs(5));
      post.setOwner(randomUser());
      post.setIcon(ForumDataRandom.getClassIcon());
      result.add(post);
    }
    return result;
  }
  
  private String generateParam(String prefix, String entity, int order, boolean isId) {
    StringBuilder sb = new StringBuilder();
    sb.append(entity);
    if (isId) {
      sb.append(PREFIX_ID).append(UNDER_SCORE)
        .append(prefix.replace(" ", "").replace("-", "_"));
    } else {
      sb.append(UNDER_SCORE).append(prefix);
    }
    sb.append(UNDER_SCORE).append(order);
    return sb.toString();
  }
  
  private List<String> search(String prefix, ENTITY entity) {
    StringBuffer sb = new StringBuffer();
    String nodeType = "nt:base";
    List<String> result = new ArrayList<String>();
    switch (entity) {
    case CATEGORY:
      nodeType = Utils.TYPE_CATEGORY;
      break;
    case FORUM:
      nodeType = Utils.TYPE_FORUM;
      break;
    case TOPIC:
      nodeType = Utils.TYPE_TOPIC;
      break;
    case POST:
      nodeType = Utils.EXO_POST;
      break;
    case ATTACHMENT:
      nodeType = Utils.EXO_FORUM_RESOURCE;
      break;
    default:
      break;
    }   
    prefix = new StringBuilder().append("%").append(UNDER_SCORE).append(prefix).append(UNDER_SCORE).append("%").toString();
    sb.append(dataLocation.getForumCategoriesLocation())
      .append("//element(*,")
      .append(nodeType)
      .append(")[jcr:like(").append(Utils.EXO_NAME).append(",'")
      .append(prefix)
      .append("') or jcr:like(").append(Utils.EXO_FILE_NAME).append(",'")
      .append(prefix)
      .append("')]");
    try {
      NodeIterator iter = forumService.search(sb.toString());
      while (iter.hasNext()) {
        result.add(iter.nextNode().getPath().toString());
      }
    } catch (Exception e) {
      log.debug("Failure when search for prefix", e);
    }
    return result;
  }

  private List<ForumAttachment> generateAttachments(String prefix, int quantity, int capacity) throws Exception {
    List<ForumAttachment> listAttachments = new ArrayList<ForumAttachment>();
    String rs = createTextResource(capacity);    
    for (int i = 0; i < quantity; i++) {
      BufferAttachment att = new BufferAttachment();
      att.setId(generateParam(prefix, Utils.ATTACHMENT, i, true));
      att.setName(generateParam(prefix, Utils.ATTACHMENT, i, false));
      att.setInputStream(new ByteArrayInputStream(rs.getBytes("UTF-8")));
      att.setMimeType("text/plain");
      long fileSize = (long) capacity * 1024;
      att.setSize(fileSize);
      listAttachments.add(att);
    }
    return listAttachments;
  }

  @Override
  public Object execute(HashMap<String, String> params) throws Exception {
    return new Object();
  }

}

