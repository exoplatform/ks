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

package org.exoplatform.forum.extras.injection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.services.organization.OrganizationService;

/**
 * @author <a href="mailto:thanhvc@exoplatform.com">Thanh Vu</a>
 * @version $Revision$
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.forum.component.core.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.forum.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.forum.test.portal-configuration.xml")
})
public class InjectorTestCase extends AbstractKernelTest {

  private OrganizationService organizationService;
  private ForumService forumService;
 
  //
  private ProfileInjector profileInjector;
  private CategoryInjector categoryInjector;
  private ForumInjector forumInjector;
  private TopicInjector topicInjector;
  private PostInjector postInjector;
  private AttachmentInjector attachmentInjector;
  
  
  
  private HashMap<String, String> params;
  private List<String> users;
  private List<String> forums;
  private List<String> topics;
  private List<String> posts;
  
  
  @Override
  public void setUp() throws Exception {

    //
    begin();
    
    //
    profileInjector = (ProfileInjector) getContainer().getComponentInstanceOfType(ProfileInjector.class);
    categoryInjector = (CategoryInjector) getContainer().getComponentInstanceOfType(CategoryInjector.class);
    forumInjector = (ForumInjector) getContainer().getComponentInstanceOfType(ForumInjector.class);
    topicInjector = (TopicInjector) getContainer().getComponentInstanceOfType(TopicInjector.class);
    postInjector = (PostInjector) getContainer().getComponentInstanceOfType(PostInjector.class);
    attachmentInjector = (AttachmentInjector) getContainer().getComponentInstanceOfType(AttachmentInjector.class);
    
    //
    organizationService = (OrganizationService) getContainer().getComponentInstanceOfType(OrganizationService.class);
    forumService = (ForumService) getContainer().getComponentInstanceOfType(ForumService.class);
    
    
    assertNotNull(profileInjector);
    assertNotNull(attachmentInjector);
    assertNotNull(topicInjector);
    assertNotNull(forumInjector);
    assertNotNull(categoryInjector);
    assertNotNull(organizationService);
    assertNotNull(forumService);
    
    //
    params = new HashMap<String, String>();
    users = new ArrayList<String>();
    forums = new ArrayList<String>();
    topics = new ArrayList<String>();
    posts = new ArrayList<String>();
  }

  @Override
  public void tearDown() throws Exception {

    
    for(String topicName : topics) {
      Topic topic = topicInjector.getTopicByName(topicName);
      Forum forum = topicInjector.getForumByTopicName(topicName);
      Category category = categoryInjector.getCategoryByForumName(forum.getForumName());
      
      forumService.removeTopic(category.getId(), forum.getId(), topic.getId());
    }
    
    //
    for(String forumName : forums) {
      Forum forum = forumInjector.getForumByName(forumName);
      Category cat = forumInjector.getCategoryByForumName(forumName);
      forumService.removeForum(cat.getId(), forum.getId());
    }
    //
    List<Category> list =  forumService.getCategories();
    for(Category cat : list) {
      forumService.removeCategory(cat.getId());
    }
    
    //
    for(String user : users) {
      organizationService.getUserHandler().removeUser(user, true);
    }
    
    //
    end();
  }
  
  public void testDefaultProfile() throws Exception {
    performProfileTest(null);
  }
  
  public void testPrefixProfile() throws Exception {
    performProfileTest("foo");
  }
  
  public void testDefaultCategory() throws Exception {
    performCategoryTest(null, null);
  }
  
  public void testPrefixCategory() throws Exception {
    performCategoryTest("foo", "bar");
  }
  
  public void testDefaultForum() throws Exception {
    performForumTest(null, null, null);
  }
  
  public void testPrefixForum() throws Exception {
    performForumTest("foo", "bar", "forum");
  }
  
  public void testDefaultTopic() throws Exception {
    performTopicTest(null, null, null, null);
  }
  
  public void testPrefixTopic() throws Exception {
    performTopicTest("foo", "bar", "forum", "topic");
  }
  
  public void testDefaultPost() throws Exception {
    performPostTest(null, null, null, null, null);
  }
  
  public void testPrefixPost() throws Exception {
    performPostTest("foo", "bar", "forum", "topic", "post");
  }
  
  public void testDefaultAttachment() throws Exception {
    performAttachmentTest(null, null, null, null, null);
  }
  
  public void testPrefixAttachment() throws Exception {
    performAttachmentTest("foo", "bar", "forum", "topic", "post");
  }
  
  private void performProfileTest(String prefix) throws Exception {
    //
    String baseName = (prefix == null ? "bench.user" : prefix);
    assertClean(baseName, null, null);
    
    //
    params.put("number", "5");
    if (prefix != null) {
      params.put("prefix", prefix);      
    }

    profileInjector.inject(params);
    
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "0"));
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "1"));
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "2"));
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "3"));
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "4"));
    
    //
    assertNotNull(forumService.getUserProfileManagement(baseName + "0"));
    assertNotNull(forumService.getUserProfileManagement(baseName + "1"));
    assertNotNull(forumService.getUserProfileManagement(baseName + "2"));
    assertNotNull(forumService.getUserProfileManagement(baseName + "3"));
    assertNotNull(forumService.getUserProfileManagement(baseName + "4"));
    
    assertEquals(5, profileInjector.userNumber(baseName));
    
    //
    cleanProfile(baseName, 5);
  }
  
  private void performCategoryTest(String userPrefix, String catPrefix) throws Exception {

    //
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String catBaseName = (catPrefix == null ? "bench.cat" : catPrefix);
    assertClean(userBaseName, catBaseName, null);

    //
    params.put("number", "3");
    if (userPrefix != null) {
      params.put("prefix", userPrefix);      
    }
    
    profileInjector.inject(params);
    
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "0"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "1"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "2"));
    

    //
    params.put("number", "2");
    params.put("fromUser", "0");
    params.put("toUser", "2");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    categoryInjector.inject(params);

    //
    assertEquals(6, categoryInjector.categoryNumber(catBaseName));
    
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "0"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "1"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "2"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "3"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "4"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "5"));
    
    categoryInjector.inject(params);
    
    //
    assertEquals(12, categoryInjector.categoryNumber(catBaseName));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "6"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "7"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "8"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "9"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "10"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "11"));
    

    //
    cleanProfile(userBaseName, 3);
  }
  
  private void performForumTest(String userPrefix, String catPrefix, String forumPrefix) throws Exception {

    //
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String catBaseName = (catPrefix == null ? "bench.cat" : catPrefix);
    String forumBaseName = (catPrefix == null ? "bench.forum" : forumPrefix);
    assertClean(userBaseName, catBaseName, forumBaseName);

    //
    params.put("number", "3");
    if (userPrefix != null) {
      params.put("prefix", userPrefix);      
    }
    
    profileInjector.inject(params);
    
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "0"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "1"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "2"));

    //
    params.clear();
    params.put("number", "1");
    params.put("fromUser", "0");
    params.put("toUser", "2");
    if (userPrefix != null) {
      params.put("userPrefix", userBaseName);
    }
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    categoryInjector.inject(params);

    //
    assertEquals(3, categoryInjector.categoryNumber(catBaseName));
    
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "0"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "1"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "5");
    if (forumPrefix != null) {
      params.put("forumPrefix", forumPrefix);
    }
    params.put("toCat", "0");
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    forumInjector.inject(params);

    assertEquals(5, forumInjector.forumNumber(forumBaseName));
    
    assertNotNull(forumInjector.getForumByName(forumBaseName + "0"));
    assertNotNull(forumInjector.getForumByName(forumBaseName + "1"));
    assertNotNull(forumInjector.getForumByName(forumBaseName + "2"));
    assertNotNull(forumInjector.getForumByName(forumBaseName + "3"));
    assertNotNull(forumInjector.getForumByName(forumBaseName + "4"));
    
    
    
    forumInjector.inject(params);
    assertEquals(10, forumInjector.forumNumber(forumBaseName));
    
    assertNotNull(forumInjector.getForumByName(forumBaseName + "5"));
    assertNotNull(forumInjector.getForumByName(forumBaseName + "6"));
    assertNotNull(forumInjector.getForumByName(forumBaseName + "7"));
    assertNotNull(forumInjector.getForumByName(forumBaseName + "8"));
    assertNotNull(forumInjector.getForumByName(forumBaseName + "9"));
    
    
    //
    cleanProfile(userBaseName, 3);
    cleanForum(forumBaseName, 10);
    
  }
  
  private void performTopicTest(String userPrefix, String catPrefix, String forumPrefix, String topicPrefix) throws Exception {
    //
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String catBaseName = (catPrefix == null ? "bench.cat" : catPrefix);
    String forumBaseName = (catPrefix == null ? "bench.forum" : forumPrefix);
    String topicBaseName = (catPrefix == null ? "bench.topic" : topicPrefix);
    assertClean(userBaseName, catBaseName, forumBaseName);

    //
    params.put("number", "3");
    if (userPrefix != null) {
      params.put("prefix", userPrefix);      
    }
    
    profileInjector.inject(params);
    
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "0"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "1"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "2"));

    //
    params.clear();
    params.put("number", "1");
    params.put("fromUser", "0");
    params.put("toUser", "2");
    if (userPrefix != null) {
      params.put("userPrefix", userBaseName);
    }
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    categoryInjector.inject(params);

    //
    assertEquals(3, categoryInjector.categoryNumber(catBaseName));
    
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "0"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "1"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "1");
    params.put("toCat", "1");
    
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    if (forumPrefix != null) {
      params.put("forumPrefix", forumPrefix);
    }

    
    forumInjector.inject(params);

    assertEquals(1, forumInjector.forumNumber(forumBaseName));
    assertNotNull(forumInjector.getForumByName(forumBaseName + "0"));
    
    //
    params.clear();
    params.put("number", "2");
    params.put("fromUser", "0");
    params.put("toUser", "2");
    params.put("toForum", "0");
    
    if (topicPrefix != null) {
      params.put("topicPrefix", topicPrefix);
    }
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (forumPrefix != null) {
      params.put("forumPrefix", forumPrefix);
    }
    
    topicInjector.inject(params);
    
    assertEquals(6, topicInjector.topicNumber(topicBaseName));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "0"));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "1"));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "2"));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "3"));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "4"));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "5"));
    
    topicInjector.inject(params);
    
    assertEquals(12, topicInjector.topicNumber(topicBaseName));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "6"));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "7"));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "8"));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "9"));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "10"));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "11"));
    //
    cleanForum(forumBaseName, 1);
    cleanProfile(userBaseName, 3);
    cleanTopic(topicBaseName, 12);
    
  }
  
  private void performPostTest(String userPrefix, String catPrefix, String forumPrefix, String topicPrefix, String postPrefix) throws Exception {

    //
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String catBaseName = (catPrefix == null ? "bench.cat" : catPrefix);
    String forumBaseName = (forumPrefix == null ? "bench.forum" : forumPrefix);
    String topicBaseName = (topicPrefix == null ? "bench.topic" : topicPrefix);
    String postBaseName = (postPrefix == null ? "bench.post" : postPrefix);
    assertClean(userBaseName, catBaseName, forumBaseName);

    //
    params.put("number", "3");
    if (userPrefix != null) {
      params.put("prefix", userPrefix);      
    }
    
    profileInjector.inject(params);
    
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "0"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "1"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "2"));
   

    //
    params.clear();
    params.put("number", "1");
    params.put("fromUser", "0");
    params.put("toUser", "2");
    if (userPrefix != null) {
      params.put("userPrefix", userBaseName);
    }
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    categoryInjector.inject(params);

    //
    assertEquals(3, categoryInjector.categoryNumber(catBaseName));
    
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "0"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "1"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "1");
    params.put("toCat", "1");
    
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    if (forumPrefix != null) {
      params.put("forumPrefix", forumPrefix);
    }

    
    forumInjector.inject(params);

    assertEquals(1, forumInjector.forumNumber(forumBaseName));
    assertNotNull(forumInjector.getForumByName(forumBaseName + "0"));
    
    //
    params.clear();
    params.put("number", "1");
    params.put("fromUser", "1");
    params.put("toUser", "1");
    params.put("toForum", "0");
    if (topicPrefix != null) {
      params.put("topicPrefix", topicPrefix);
    }
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (forumPrefix != null) {
      params.put("forumPrefix", forumPrefix);
    }
    
    topicInjector.inject(params);
    
    assertEquals(1, topicInjector.topicNumber(topicBaseName));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "0"));
    
    //
    params.clear();
    params.put("number", "6");
    if (postPrefix != null) {
      params.put("postPrefix", postPrefix);
    }
    params.put("fromUser", "0");
    params.put("toUser", "2");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    params.put("toTopic", "0");
    if (topicPrefix != null) {
      params.put("topicPrefix", topicPrefix);
    }
    
    postInjector.inject(params);
    
    assertEquals(18, postInjector.postNumber(postBaseName));
    assertNotNull(postInjector.getPostByName(postBaseName + "0"));
    assertNotNull(postInjector.getPostByName(postBaseName + "1"));
    assertNotNull(postInjector.getPostByName(postBaseName + "2"));
    assertNotNull(postInjector.getPostByName(postBaseName + "3"));
    assertNotNull(postInjector.getPostByName(postBaseName + "4"));
    assertNotNull(postInjector.getPostByName(postBaseName + "5"));
    assertNotNull(postInjector.getPostByName(postBaseName + "6"));
    assertNotNull(postInjector.getPostByName(postBaseName + "7"));
    assertNotNull(postInjector.getPostByName(postBaseName + "8"));
    assertNotNull(postInjector.getPostByName(postBaseName + "9"));
    assertNotNull(postInjector.getPostByName(postBaseName + "10"));
    assertNotNull(postInjector.getPostByName(postBaseName + "11"));
    assertNotNull(postInjector.getPostByName(postBaseName + "12"));
    assertNotNull(postInjector.getPostByName(postBaseName + "13"));
    assertNotNull(postInjector.getPostByName(postBaseName + "14"));
    assertNotNull(postInjector.getPostByName(postBaseName + "15"));
    assertNotNull(postInjector.getPostByName(postBaseName + "16"));
    assertNotNull(postInjector.getPostByName(postBaseName + "17"));
    
    postInjector.inject(params);
    
    assertEquals(36, postInjector.postNumber(postBaseName));
    assertNotNull(postInjector.getPostByName(postBaseName + "18"));
    assertNotNull(postInjector.getPostByName(postBaseName + "19"));
    assertNotNull(postInjector.getPostByName(postBaseName + "20"));
    assertNotNull(postInjector.getPostByName(postBaseName + "21"));
    assertNotNull(postInjector.getPostByName(postBaseName + "22"));
    assertNotNull(postInjector.getPostByName(postBaseName + "23"));
    assertNotNull(postInjector.getPostByName(postBaseName + "24"));
    assertNotNull(postInjector.getPostByName(postBaseName + "25"));
    assertNotNull(postInjector.getPostByName(postBaseName + "26"));
    assertNotNull(postInjector.getPostByName(postBaseName + "27"));
    assertNotNull(postInjector.getPostByName(postBaseName + "28"));
    assertNotNull(postInjector.getPostByName(postBaseName + "29"));
    assertNotNull(postInjector.getPostByName(postBaseName + "30"));
    assertNotNull(postInjector.getPostByName(postBaseName + "31"));
    assertNotNull(postInjector.getPostByName(postBaseName + "32"));
    assertNotNull(postInjector.getPostByName(postBaseName + "33"));
    assertNotNull(postInjector.getPostByName(postBaseName + "34"));
    assertNotNull(postInjector.getPostByName(postBaseName + "35"));
    
    //
    cleanForum(forumBaseName, 1);
    cleanProfile(userBaseName, 3);
    cleanTopic(topicBaseName, 1);
    cleanPost(postBaseName, 36);
    
  }
  
  private void performAttachmentTest(String userPrefix, String catPrefix, String forumPrefix, String topicPrefix, String postPrefix) throws Exception {

    //
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String catBaseName = (catPrefix == null ? "bench.cat" : catPrefix);
    String forumBaseName = (forumPrefix == null ? "bench.forum" : forumPrefix);
    String topicBaseName = (topicPrefix == null ? "bench.topic" : topicPrefix);
    String postBaseName = (postPrefix == null ? "bench.post" : postPrefix);
    assertClean(userBaseName, catBaseName, forumBaseName);

    //
    params.put("number", "3");
    if (userPrefix != null) {
      params.put("prefix", userPrefix);      
    }
    
    profileInjector.inject(params);
    
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "0"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "1"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "2"));
   

    //
    params.clear();
    params.put("number", "1");
    params.put("fromUser", "0");
    params.put("toUser", "2");
    if (userPrefix != null) {
      params.put("userPrefix", userBaseName);
    }
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    
    categoryInjector.inject(params);

    //
    assertEquals(3, categoryInjector.categoryNumber(catBaseName));
    
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "0"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "1"));
    assertNotNull(categoryInjector.getCategoryByName(catBaseName + "2"));
    
    //
    params.clear();
    params.put("number", "1");
    params.put("toCat", "1");
    
    if (catPrefix != null) {
      params.put("catPrefix", catPrefix);
    }
    if (forumPrefix != null) {
      params.put("forumPrefix", forumPrefix);
    }
    
    forumInjector.inject(params);

    assertEquals(1, forumInjector.forumNumber(forumBaseName));
    
    assertNotNull(forumInjector.getForumByName(forumBaseName + "0"));

    
    //
    params.clear();
    params.put("number", "1");
    params.put("fromUser", "1");
    params.put("toUser", "1");
    params.put("toForum", "0");
    
    if (topicPrefix != null) {
      params.put("topicPrefix", topicPrefix);
    }
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }

    if (forumPrefix != null) {
      params.put("forumPrefix", forumPrefix);
    }
    
    topicInjector.inject(params);
    
    assertEquals(1, topicInjector.topicNumber(topicBaseName));
    assertNotNull(topicInjector.getTopicByName(topicBaseName + "0"));

    
    
    //
    params.clear();
    params.put("number", "5");
    params.put("fromUser", "0");
    params.put("toUser", "0");
    params.put("toTopic", "0");
    
    if (postPrefix != null) {
      params.put("postPrefix", postPrefix);
    }
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (topicPrefix != null) {
      params.put("topicPrefix", topicPrefix);
    }
    
    postInjector.inject(params);
    
    assertEquals(5, postInjector.postNumber(postBaseName));
    assertNotNull(postInjector.getPostByName(postBaseName + "0"));
    assertNotNull(postInjector.getPostByName(postBaseName + "1"));
    assertNotNull(postInjector.getPostByName(postBaseName + "2"));
    assertNotNull(postInjector.getPostByName(postBaseName + "3"));
    assertNotNull(postInjector.getPostByName(postBaseName + "4"));
    
    
    //
    params.clear();
    params.put("number", "2");
    if (postPrefix != null) {
      params.put("postPrefix", postPrefix);
    }
    
    params.put("fromPost", "0");
    params.put("toPost", "4");
    params.put("byteSize", "50");
    
    attachmentInjector.inject(params);

    assertEquals(2, attachmentInjector.getPostByName(postBaseName + "0").getAttachments().size());
    assertEquals(2, attachmentInjector.getPostByName(postBaseName + "1").getAttachments().size());
    assertEquals(2, attachmentInjector.getPostByName(postBaseName + "2").getAttachments().size());
    assertEquals(2, attachmentInjector.getPostByName(postBaseName + "3").getAttachments().size());
    assertEquals(2, attachmentInjector.getPostByName(postBaseName + "4").getAttachments().size());
    
    
    attachmentInjector.inject(params);

    assertEquals(4, attachmentInjector.getPostByName(postBaseName + "0").getAttachments().size());
    assertEquals(4, attachmentInjector.getPostByName(postBaseName + "1").getAttachments().size());
    assertEquals(4, attachmentInjector.getPostByName(postBaseName + "2").getAttachments().size());
    assertEquals(4, attachmentInjector.getPostByName(postBaseName + "3").getAttachments().size());
    assertEquals(4, attachmentInjector.getPostByName(postBaseName + "4").getAttachments().size());
    
    
    
    //
    cleanForum(forumBaseName, 1);
    cleanProfile(userBaseName, 3);
    cleanTopic(topicBaseName, 1);
    cleanPost(postBaseName, 5);
    
  }
  
  
  private void assertClean(String userBaseName, String categoryBaseName, String forumBaseName) throws Exception {
    if (userBaseName != null) {
      assertEquals(null, organizationService.getUserHandler().findUserByName(userBaseName + "0"));
      assertEquals(null, categoryInjector.getCategoryByName(categoryBaseName + "0"));
      assertEquals(null, forumInjector.getForumByName(forumBaseName + "0"));
    }
  }
  
  private void cleanProfile(String prefix, int number) {

    for (int i = 0; i < number; ++i) {
      users.add(prefix + i);
    }

  }
  
  private void cleanForum(String prefix, int number) {

    for (int i = 0; i < number; ++i) {
      forums.add(prefix + i);
    }

  }
  
  private void cleanTopic(String prefix, int number) {

    for (int i = 0; i < number; ++i) {
      topics.add(prefix + i);
    }

  }
  
  private void cleanPost(String prefix, int number) {

    for (int i = 0; i < number; ++i) {
      posts.add(prefix + i);
    }

  }
}
