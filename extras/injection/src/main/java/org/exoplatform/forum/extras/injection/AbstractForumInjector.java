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

import java.util.HashMap;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.impl.JCRDataStorage;
import org.exoplatform.forum.extras.injection.utils.LoremIpsum4J;
import org.exoplatform.forum.extras.injection.utils.NameGenerator;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;

/**
 * @author <a href="mailto:thanhvc@exoplatform.com">ThanhVu</a>
 * @version $Revision$
 */
public abstract class AbstractForumInjector extends DataInjector {

  /** . */
  private static Log LOG = ExoLogger.getLogger(ForumInjector.class);
  
  /** . */
  private final static String DEFAULT_USER_BASE = "bench.user";
  
  /** . */
  private final static String DEFAULT_CATEGORY_BASE = "bench.cat";

  /** . */
  private final static String DEFAULT_FORUM_BASE = "bench.forum";
  
  /** . */
  private final static String DEFAULT_TOPIC_BASE = "bench.topic";
  
  /** . */
  private final static String DEFAULT_POST_BASE = "bench.post";
  
  /** . */
  private final static int DEFAULT_BYTE_SIZE_BASE = 100;
  

  /** . */
  protected final static String PASSWORD = "exo";

  /** . */
  protected final static String DOMAIN = "exoplatform.int";

  /** . */
  protected String userBase;
  
  /** . */
  protected String categoryBase;

  /** . */
  protected String forumBase;
  
  /** . */
  protected String topicBase;
  
  /** . */
  protected String postBase;
  
  /** . */
  protected int byteSizeBase;

  /** . */
  protected int userNumber;
  
  /** . */
  protected int categoryNumber;

  /** . */
  protected int forumNumber;
  
  /** . */
  protected int topicNumber;
  
  /** . */
  protected int postNumber;

  /** . */
  protected final OrganizationService organizationService;

  /** . */
  protected final ForumService forumService;
  
  /** . */
  protected final KSDataLocation locator;

  /** . */
  protected final UserHandler userHandler;

  /** . */
  protected final Random random;

  /** . */
  protected NameGenerator nameGenerator;

  /** . */
  protected LoremIpsum4J lorem;

  public AbstractForumInjector() {

    PortalContainer c = PortalContainer.getInstance();
    this.forumService = (ForumService) c.getComponentInstanceOfType(ForumService.class);
    this.organizationService = (OrganizationService) c.getComponentInstanceOfType(OrganizationService.class);
    this.locator = (KSDataLocation) c.getComponentInstanceOfType(KSDataLocation.class);

    //
    this.userHandler = organizationService.getUserHandler();
    this.nameGenerator = new NameGenerator();
    this.random = new Random();
    this.lorem = new LoremIpsum4J();

  }

  public void init(String userPrefix, String categoryPrefix, String forumPrefix, String topicPrefix, String postPrefix, int byteSize) {

    //
    userBase = (userPrefix == null ? DEFAULT_USER_BASE : userPrefix);
    categoryBase = (categoryPrefix == null ? DEFAULT_CATEGORY_BASE : categoryPrefix);
    forumBase = (forumPrefix == null ? DEFAULT_FORUM_BASE : forumPrefix);
    topicBase = (topicPrefix == null ? DEFAULT_TOPIC_BASE : topicPrefix);
    postBase = (postPrefix == null ? DEFAULT_POST_BASE : postPrefix);
    byteSizeBase = (byteSize == 0 ? DEFAULT_BYTE_SIZE_BASE : byteSize);

    //
    categoryNumber = 0;
    forumNumber = 0;
    topicNumber = 0;
    postNumber = 0;

    try {
      userNumber = userNumber(userBase);
      categoryNumber = categoryNumber(categoryBase);
      forumNumber = forumNumber(forumBase);
      topicNumber = topicNumber(topicBase);
      postNumber = postNumber(postBase);
    }
    catch (Exception e) {
      // If no user is existing, set keep 0 as value.
    }

    //
    LOG.info("Initial user number : " + userNumber);
    LOG.info("Initial category number : " + categoryNumber);
    LOG.info("Initial forum number : " + forumNumber);
    LOG.info("Initial topic number : " + topicNumber);
    LOG.info("Initial post number : " + postNumber);
  }
  
  @Override
  public Log getLog() {
    return ExoLogger.getExoLogger(this.getClass());
  }

  @Override
  public Object execute(HashMap<String, String> stringStringHashMap) throws Exception {
    return null;
  }

  @Override
  public void reject(HashMap<String, String> stringStringHashMap) throws Exception {
  }

  protected int userNumber(String base) throws Exception {
    StringBuffer sb = new StringBuffer(Utils.JCR_ROOT);
    sb.append("/").append(locator.getUserProfilesLocation()).append("/element(*,");
    sb.append(Utils.USER_PROFILES_TYPE).append(")[jcr:like(exo:userId, '%").append(base).append("%')]");
    
    return (int)forumService.search(sb.toString()).getSize();
  }
  
  protected int categoryNumber(String base) throws Exception {
    StringBuffer sb = new StringBuffer(Utils.JCR_ROOT);
    sb.append("/").append(locator.getForumCategoriesLocation()).append("/element(*,");
    sb.append(Utils.EXO_FORUM_CATEGORY).append(")[jcr:like(exo:name, '%").append(base).append("%')]");

    return (int)forumService.search(sb.toString()).getSize();
  }
  
  protected int forumNumber(String base) throws Exception {
    StringBuffer sb = new StringBuffer(Utils.JCR_ROOT);
    sb.append("/").append(locator.getForumCategoriesLocation()).append("//element(*,");
    sb.append(Utils.EXO_FORUM).append(")[jcr:like(exo:name, '%").append(base).append("%')]");

    return (int)forumService.search(sb.toString()).getSize();

  }
  
  
  protected int topicNumber(String base) throws Exception {
    StringBuffer sb = new StringBuffer(Utils.JCR_ROOT);
    sb.append("/").append(locator.getForumCategoriesLocation()).append("//element(*,");
    sb.append(Utils.EXO_TOPIC).append(")[jcr:like(exo:name, '%").append(base).append("%')]");

    return (int)forumService.search(sb.toString()).getSize();

  }
  
  protected int postNumber(String base) throws Exception {
    StringBuffer sb = new StringBuffer(Utils.JCR_ROOT);
    sb.append("/").append(locator.getForumCategoriesLocation()).append("//element(*,");
    sb.append(Utils.EXO_POST).append(")[jcr:like(exo:name, '%").append(base).append("%')]");

    return (int)forumService.search(sb.toString()).getSize();

  }

  protected Category getCategoryByName(String catName) throws Exception {
    StringBuffer sb = new StringBuffer(Utils.JCR_ROOT);
    sb.append("/").append(locator.getForumCategoriesLocation()).append("/element(*,");
    sb.append(Utils.EXO_FORUM_CATEGORY).append(")[jcr:like(exo:name, '%").append(catName).append("%')]");

    NodeIterator iter =  forumService.search(sb.toString());
    if (iter.hasNext()) {
      Node cateNode = (Node)iter.next();
      Category cat = new Category(cateNode.getName());
      cat.setPath(cateNode.getPath());
      PropertyReader reader = new PropertyReader(cateNode);
      cat.setOwner(reader.string(Utils.EXO_OWNER));
      cat.setCategoryName(reader.string(Utils.EXO_NAME));
      
      return cat;
    }
    
    
    return null;
  }
  
  protected Forum getForumByName(String forumName) throws Exception {
    StringBuffer sb = new StringBuffer(Utils.JCR_ROOT);
    sb.append("/").append(locator.getForumCategoriesLocation()).append("//element(*,");
    sb.append(Utils.EXO_FORUM).append(")[jcr:like(exo:name, '%").append(forumName).append("%')]");

    NodeIterator iter =  forumService.search(sb.toString());
    if (iter.hasNext()) {
      Node forumNode = (Node)iter.next();

      Forum forum = new Forum();
      PropertyReader reader = new PropertyReader(forumNode);
      forum.setId(forumNode.getName());
      forum.setPath(forumNode.getPath());
      forum.setOwner(reader.string(Utils.EXO_OWNER));
      forum.setForumName(reader.string(Utils.EXO_NAME));

      return forum;
    }
    
    
    return null;
  }
  
  protected Category getCategoryByForumName(String forumName) throws Exception {
    StringBuffer sb = new StringBuffer(Utils.JCR_ROOT);
    sb.append("/").append(locator.getForumCategoriesLocation()).append("//element(*,");
    sb.append(Utils.EXO_FORUM).append(")[jcr:like(exo:name, '%").append(forumName).append("%')]");
    
    NodeIterator iter =  forumService.search(sb.toString());
    if (iter.hasNext()) {
      Node forumNode = (Node)iter.next();
      if (forumNode.getParent() != null) {
        Node cateNode =  forumNode.getParent();
        Category cat = new Category(cateNode.getName());
        cat.setPath(cateNode.getPath());
        PropertyReader reader = new PropertyReader(cateNode);
        cat.setOwner(reader.string(Utils.EXO_OWNER));
        cat.setCategoryName(reader.string(Utils.EXO_NAME));
        return cat;
      }
    }
    
    return null;
  }
  
  protected Topic getTopicByName(String topicName) throws Exception {
    StringBuffer sb = new StringBuffer(Utils.JCR_ROOT);
    sb.append("/").append(locator.getForumCategoriesLocation()).append("//element(*,");
    sb.append(Utils.EXO_TOPIC).append(")[jcr:like(exo:name, '%").append(topicName).append("%')]");

    NodeIterator iter =  forumService.search(sb.toString());
    if (iter.hasNext()) {
      Node topicNode = (Node)iter.next();

      Topic topicNew = new Topic();
      PropertyReader reader = new PropertyReader(topicNode);
      topicNew.setId(topicNode.getName());
      topicNew.setPath(topicNode.getPath());
      topicNew.setTopicName(reader.string(Utils.EXO_NAME));
      return topicNew;
    }
    
    
    return null;
  }
  
  protected Post getPostByName(String postName) throws Exception {
    StringBuffer sb = new StringBuffer(Utils.JCR_ROOT);
    sb.append("/").append(locator.getForumCategoriesLocation()).append("//element(*,");
    sb.append(Utils.EXO_POST).append(")[jcr:like(exo:name, '%").append(postName).append("%')]");

    NodeIterator iter =  forumService.search(sb.toString());
    if (iter.hasNext()) {
      Node postNode = (Node)iter.next();

      Post post = new Post();
      PropertyReader reader = new PropertyReader(postNode);
      post.setId(postNode.getName());
      post.setPath(postNode.getPath());
      post.setName(reader.string(Utils.EXO_NAME));
      post.setNumberAttach(reader.l(Utils.EXO_NUMBER_ATTACH));
      if (post.getNumberAttach() > 0) {
        post.setAttachments(JCRDataStorage.getAttachmentsByNode(postNode));
      }
      return post;
    }
    return null;
  }
  
  protected Topic getTopicByPostName(String postName) throws Exception {
    StringBuffer sb = new StringBuffer(Utils.JCR_ROOT);
    sb.append("/").append(locator.getForumCategoriesLocation()).append("//element(*,");
    sb.append(Utils.EXO_POST).append(")[jcr:like(exo:name, '%").append(postName).append("%')]");

    NodeIterator iter =  forumService.search(sb.toString());
    if (iter.hasNext()) {
      Node postNode = (Node)iter.next();

      if (postNode.getParent() != null) {
        Node topicNode = postNode.getParent();
        
        Topic topicNew = new Topic();
        PropertyReader reader = new PropertyReader(topicNode);
        topicNew.setId(topicNode.getName());
        topicNew.setPath(topicNode.getPath());
        topicNew.setTopicName(reader.string(Utils.EXO_NAME));
        return topicNew;
      }
      
    }
    return null;
  }
  
  protected Forum getForumByTopicName(String topicName) throws Exception {
    StringBuffer sb = new StringBuffer(Utils.JCR_ROOT);
    sb.append("/").append(locator.getForumCategoriesLocation()).append("//element(*,");
    sb.append(Utils.EXO_TOPIC).append(")[jcr:like(exo:name, '%").append(topicName).append("%')]");

    NodeIterator iter =  forumService.search(sb.toString());
    if (iter.hasNext()) {
      Node topicNode = (Node)iter.next();
      if (topicNode.getParent() != null) {
        //
        Node forumNode = topicNode.getParent();
        Forum forum = new Forum();
        PropertyReader reader = new PropertyReader(forumNode);
        forum.setId(forumNode.getName());
        forum.setPath(forumNode.getPath());
        forum.setOwner(reader.string(Utils.EXO_OWNER));
        
        forum.setForumName(reader.string(Utils.EXO_NAME));

        return forum;
        
      }

    }
    
    
    return null;
  }
  
  protected String userName() {
    return userBase + userNumber;
  }
  
  protected String categoryName() {
    return categoryBase + categoryNumber;
  }

  protected String forumName() {
    return forumBase + forumNumber;
  }
  
  protected String topicName() {
    return topicBase + topicNumber;
  }
  
  protected String postName() {
    return postBase + postNumber;
  }
  
  protected String getId(String type) {
    return type + IdGenerator.generate();
  }
  
  protected int param(HashMap<String, String> params, String name) {

    //
    if (params == null) {
      throw new NullPointerException();
    }

    //
    if (name == null) {
      throw new NullPointerException();
    }

    //
    try {
      String value = params.get(name);
      if (value != null) {
        return Integer.valueOf(value);
      }
    } catch (NumberFormatException e) {
      LOG.warn("Integer number expected for property " + name);
    }
    return 0;
    
  }
  
}
