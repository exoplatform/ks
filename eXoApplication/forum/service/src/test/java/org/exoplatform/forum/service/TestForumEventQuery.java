/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.commons.utils.ISO8601;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Dec 28, 2009 - 7:00:29 AM  
 */
public class TestForumEventQuery extends TestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testQuerySearchCategory() {
    List<String> categoryIds = new ArrayList<String>();
    String selector = "/jcr:root/forumPath//element(*,exo:forumCategory)";
    ForumEventQuery eventQuery = new ForumEventQuery();
    String predicate = "";
    eventQuery.setType(Utils.CATEGORY);
    eventQuery.setPath("/forumPath");

    // not conditions
    assertEquals(selector + predicate, eventQuery.getPathQuery(categoryIds));
    // set text search
    eventQuery.setKeyValue("text search");
    // only category name
    eventQuery.setValueIn("title");
    predicate = "[(jcr:contains(@exo:name, 'text search'))";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // all value of category
    eventQuery.setValueIn("all");
    predicate = "[(jcr:contains(., 'text search'))";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    eventQuery.setByUser("root");
    predicate += " and (@exo:owner='root')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    eventQuery.setModerator("demo");
    predicate += " and (@exo:moderators='demo')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    String tempPredicate = predicate;
    // case 1: only from date
    Calendar calendar = GregorianCalendar.getInstance();
    eventQuery.setFromDateCreated(calendar);
    predicate += " and (@exo:createdDate >= xs:dateTime('" + ISO8601.format(calendar) + "'))";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // case 2: only to date
    predicate = tempPredicate;
    eventQuery.setFromDateCreated(null);
    eventQuery.setToDateCreated(calendar);
    predicate += " and (@exo:createdDate <= xs:dateTime('" + ISO8601.format(calendar) + "'))";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // case 3: from date to date
    predicate = tempPredicate;
    eventQuery.setFromDateCreated(calendar);
    predicate += " and ((@exo:createdDate >= xs:dateTime('" + ISO8601.format(calendar) + "')) and (@exo:createdDate <= xs:dateTime('" + ISO8601.format(calendar) + "'))) ";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set category Scoping
    categoryIds.addAll(Arrays.asList(new String[] { "CategoryId1", "CategoryId2", "CategoryId3" }));
    predicate += " and (fn:name()='CategoryId1' or fn:name()='CategoryId2' or fn:name()='CategoryId3')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

  }

  public void testQuerySearchForum() {
    List<String> categoryIds = new ArrayList<String>();
    String selector = "/jcr:root/forumPath//element(*,exo:forum)";
    ForumEventQuery eventQuery = new ForumEventQuery();
    String predicate = "";
    eventQuery.setType(Utils.FORUM);
    eventQuery.setPath("/forumPath");
    // not conditions
    assertEquals(selector + predicate, eventQuery.getPathQuery(categoryIds));

    eventQuery.setKeyValue("text search");
    // only forum name
    eventQuery.setValueIn("title");
    predicate = "[(jcr:contains(@exo:name, 'text search'))";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // all value of forums
    eventQuery.setValueIn("all");
    predicate = "[(jcr:contains(., 'text search'))";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // set User
    eventQuery.setByUser("root");
    predicate += " and (@exo:owner='root')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // Set Close
    String tempPredicate = predicate;
    // Case 1: With Administrator search
    eventQuery.setUserPermission(0);
    eventQuery.setIsClose("true"); // or false, if value is 'all', not new X-path
    predicate += " and (@exo:isClosed='true')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // With normal user or anonim-user not use value isClose.
    // Case 2: With moderator - add moderator search
    eventQuery.setUserPermission(1);
    // sub case 1: close is false;
    eventQuery.setIsClose("false"); // same for case search Administrator with isClose = false.
    predicate = tempPredicate + " and (@exo:isClosed='false')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // sub case 2: close is true
    eventQuery.setIsClose("true");
    List<String> listOfUser = Arrays.asList(new String[] { "john", "/foo/bar", "bez:/foo/dez" });// userName, group and membership of this user.
    eventQuery.setListOfUser(listOfUser);
    predicate = tempPredicate + " and (@exo:isClosed='true' and (@exo:moderators = 'john' or @exo:moderators = '/foo/bar' or @exo:moderators = '*:/foo/bar' or @exo:moderators = 'bez:/foo/dez' or @exo:moderators = '*:/foo/dez'))";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // sub case 3: close is all
    eventQuery.setIsClose("all");
    predicate = tempPredicate + " and (@exo:isClosed='false' or (@exo:moderators = 'john' or @exo:moderators = '/foo/bar' or @exo:moderators = '*:/foo/bar' or @exo:moderators = 'bez:/foo/dez' or @exo:moderators = '*:/foo/dez'))";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set Lock, if isLock = 'all', not build new x-path
    eventQuery.setIsLock("all");
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // isLock != 'all'
    eventQuery.setIsLock("false");
    predicate += " and (@exo:isLock='false')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set moderator
    eventQuery.setModerator("demo");
    predicate += " and (@exo:moderators='demo')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set topic count
    eventQuery.setTopicCountMin("50");
    predicate += " and (@exo:topicCount>=50)";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set post count
    eventQuery.setPostCountMin("100");
    predicate += " and (@exo:postCount>=100)";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set from date
    tempPredicate = predicate;
    Calendar calendar = GregorianCalendar.getInstance();
    eventQuery.setFromDateCreated(calendar);
    predicate += " and (@exo:createdDate >= xs:dateTime('" + ISO8601.format(calendar) + "'))";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set to date but not from date
    eventQuery.setFromDateCreated(null);
    eventQuery.setToDateCreated(calendar);

    // set from date to date
    eventQuery.setFromDateCreated(calendar);
    predicate = tempPredicate + " and ((@exo:createdDate >= xs:dateTime('" + ISO8601.format(calendar) + "')) " + "and (@exo:createdDate <= xs:dateTime('" + ISO8601.format(calendar) + "'))) ";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set category Scoping
    categoryIds.addAll(Arrays.asList(new String[] { "CategoryId1", "CategoryId2", "CategoryId3" }));
    predicate += " and (fn:name()='CategoryId1' or fn:name()='CategoryId2' or fn:name()='CategoryId3')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
  }

  public void testQuerySearchTopic() {
    List<String> categoryIds = new ArrayList<String>();
    String selector = "/jcr:root/forumPath//element(*,exo:topic)";
    ForumEventQuery eventQuery = new ForumEventQuery();
    String predicate = "";
    eventQuery.setType(Utils.TOPIC);
    eventQuery.setPath("/forumPath");
    // not conditions
    assertEquals(selector + predicate, eventQuery.getPathQuery(categoryIds));

    eventQuery.setKeyValue("text search");
    // only forum name
    eventQuery.setValueIn("title");
    predicate = "[(jcr:contains(@exo:name, 'text search'))";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // all value of forums
    eventQuery.setValueIn("all");
    predicate = "[(jcr:contains(., 'text search'))";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // set User
    eventQuery.setByUser("root");
    predicate += " and (@exo:owner='root')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set topic type, with topic: admin
    eventQuery.setTopicType("topicType");
    predicate += " and (@exo:topicType='topicType')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // Set Close, only use for administrator or moderator. they same x-path
    eventQuery.setUserPermission(0);
    // if value is 'all', not new x-path
    eventQuery.setIsClose("all");
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // isClose != 'all'
    eventQuery.setIsClose("true"); // or false.
    predicate += " and (@exo:isClosed='true')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set Lock, if isLock = 'all', not build new x-path
    eventQuery.setIsLock("all");
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
    // isLock != 'all'
    eventQuery.setIsLock("false");
    predicate += " and (@exo:isLock='false')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set post count
    eventQuery.setPostCountMin("100");
    predicate += " and (@exo:postCount>=100)";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set view count
    eventQuery.setViewCountMin("200");
    predicate += " and (@exo:viewCount>=200)";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set from date
    String tempPredicate = predicate;
    Calendar calendar = GregorianCalendar.getInstance();
    eventQuery.setFromDateCreated(calendar);
    predicate += " and (@exo:createdDate >= xs:dateTime('" + ISO8601.format(calendar) + "'))";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set to date but not from date
    eventQuery.setFromDateCreated(null);
    eventQuery.setToDateCreated(calendar);

    // set from date to date
    eventQuery.setFromDateCreated(calendar);
    predicate = tempPredicate + " and ((@exo:createdDate >= xs:dateTime('" + ISO8601.format(calendar) + "')) " + "and (@exo:createdDate <= xs:dateTime('" + ISO8601.format(calendar) + "'))) ";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // check can view for normal user and guest
    List<String> listOfUser = Arrays.asList(new String[] { "john", "/foo/bar", "bez:/foo/dez" });// userName, group and membership of this user.
    eventQuery.setListOfUser(listOfUser);
    eventQuery.setUserPermission(2);
    predicate += " and (@exo:isApproved='true' and @exo:isActive='true' and @exo:isWaiting='false' and @exo:isActiveByForum='true') and ((not(@exo:canView) or @exo:canView='' or @exo:canView=' ') or @exo:canView = 'john' or @exo:canView = '/foo/bar' or @exo:canView = '*:/foo/bar' or @exo:canView = 'bez:/foo/dez' or @exo:canView = '*:/foo/dez' or @exo:owner='john')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));

    // set category Scoping
    categoryIds.addAll(Arrays.asList(new String[] { "CategoryId1", "CategoryId2", "CategoryId3" }));
    predicate += " and (@exo:path='CategoryId1' or @exo:path='CategoryId2' or @exo:path='CategoryId3')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
  }

  public void testQuerySearchPost() {
    List<String> categoryIds = new ArrayList<String>();
    String selector = "/jcr:root/forumPath//element(*,exo:post)";
    String postPrivate = " and (@exo:userPrivate='exoUserPri' or @exo:userPrivate='john' or @exo:userPrivate='/foo/bar' or @exo:userPrivate='bez:/foo/dez') and (@exo:isFirstPost='false')";
    ForumEventQuery eventQuery = new ForumEventQuery();
    String predicate = "";
    eventQuery.setType(Utils.POST);
    eventQuery.setPath("/forumPath");
    List<String> listOfUser = Arrays.asList(new String[] { "john", "/foo/bar", "bez:/foo/dez" });// userName, group and membership of this user.
    eventQuery.setListOfUser(listOfUser);
    eventQuery.setUserPermission(0);
    // not conditions
    assertEquals(selector + predicate, eventQuery.getPathQuery(categoryIds));

    eventQuery.setKeyValue("text search");
    // only forum name
    eventQuery.setValueIn("title");
    predicate = "[(jcr:contains(@exo:name, 'text search'))";
    assertEquals(selector + predicate + postPrivate + "]", eventQuery.getPathQuery(categoryIds));
    // all value of forums
    eventQuery.setValueIn("all");
    predicate = "[(jcr:contains(., 'text search'))";
    assertEquals(selector + predicate + postPrivate + "]", eventQuery.getPathQuery(categoryIds));
    // set User
    eventQuery.setByUser("root");
    predicate += " and (@exo:owner='root')";
    assertEquals(selector + predicate + postPrivate + "]", eventQuery.getPathQuery(categoryIds));

    String tempPredicate = predicate;
    // case 1: only from date
    Calendar calendar = GregorianCalendar.getInstance();
    eventQuery.setFromDateCreated(calendar);
    predicate += " and (@exo:createdDate >= xs:dateTime('" + ISO8601.format(calendar) + "'))";
    assertEquals(selector + predicate + postPrivate + "]", eventQuery.getPathQuery(categoryIds));
    // case 2: only to date
    predicate = tempPredicate;
    eventQuery.setFromDateCreated(null);
    eventQuery.setToDateCreated(calendar);
    predicate += " and (@exo:createdDate <= xs:dateTime('" + ISO8601.format(calendar) + "'))";
    assertEquals(selector + predicate + postPrivate + "]", eventQuery.getPathQuery(categoryIds));
    // case 3: from date to date
    predicate = tempPredicate;
    eventQuery.setFromDateCreated(calendar);
    predicate += " and ((@exo:createdDate >= xs:dateTime('" + ISO8601.format(calendar) + "')) and (@exo:createdDate <= xs:dateTime('" + ISO8601.format(calendar) + "'))) ";
    assertEquals(selector + predicate + postPrivate + "]", eventQuery.getPathQuery(categoryIds));
    // set category Scoping
    categoryIds.addAll(Arrays.asList(new String[] { "CategoryId1", "CategoryId2", "CategoryId3" }));
    predicate += postPrivate + " and (@exo:path='CategoryId1' or @exo:path='CategoryId2' or @exo:path='CategoryId3')";
    assertEquals(selector + predicate + "]", eventQuery.getPathQuery(categoryIds));
  }
}
