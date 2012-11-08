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
package org.exoplatform.faq.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : quangpld
 *          quangpld@exoplatform.com
 * Oct 5, 2012  
 */
public class UtilsTestCase extends TestCase {
  
  public UtilsTestCase() throws Exception {
    super();
  }

  public void testHasPermission() throws Exception {
    List<String> listOfUsers = new ArrayList<String>();

    assertFalse(Utils.hasPermission(listOfUsers, listOfUsers));

    listOfUsers.add("demo");
    listOfUsers.add("member:/platform/users");
    listOfUsers.add("*:/platform/newgroup");
    listOfUsers.add("/platform/test");
    
    List<String> listPlugin = Arrays.asList(new String[]{});
    assertFalse(Utils.hasPermission(listPlugin, listOfUsers));
    
    listPlugin = Arrays.asList(new String[]{" "});
    assertFalse(Utils.hasPermission(listPlugin, listOfUsers));

    listPlugin = Arrays.asList(new String[]{"demo", "/abc/zzz"});
    assertTrue(Utils.hasPermission(listPlugin, listOfUsers));

    listPlugin = Arrays.asList(new String[]{"marry", "/abc/zzz"});
    assertFalse(Utils.hasPermission(listPlugin, listOfUsers));

    listPlugin = Arrays.asList(new String[]{"marry", "member:/platform/users"});
    assertTrue(Utils.hasPermission(listPlugin, listOfUsers));

    listPlugin = Arrays.asList(new String[]{"marry", "*:/platform/users"});
    assertTrue(Utils.hasPermission(listPlugin, listOfUsers));
    
    listPlugin = Arrays.asList(new String[]{"marry", "admin:/platform/users"});
    assertFalse(Utils.hasPermission(listPlugin, listOfUsers));
    
    listPlugin = Arrays.asList(new String[]{"marry", "/platform/newgroup"});
    assertTrue(Utils.hasPermission(listPlugin, listOfUsers));
    
    listPlugin = Arrays.asList(new String[]{"marry", "*:/platform/newgroup"});
    assertTrue(Utils.hasPermission(listPlugin, listOfUsers));
    
    listPlugin = Arrays.asList(new String[]{"marry", "member:/platform/newgroup"});
    assertTrue(Utils.hasPermission(listPlugin, listOfUsers));

    listPlugin = Arrays.asList(new String[]{"marry", "*:/platform/test"});
    assertTrue(Utils.hasPermission(listPlugin, listOfUsers));
    
    listPlugin = Arrays.asList(new String[]{"marry", "/platform/test"});
    assertTrue(Utils.hasPermission(listPlugin, listOfUsers));

    assertFalse(Utils.hasPermission(listPlugin, new ArrayList<String>()));
  }
  
  public void testGetTimeOfLastActivity() throws Exception {
    assertEquals(1315131135, Utils.getTimeOfLastActivity("root-1315131135"));
    assertEquals(-1, Utils.getTimeOfLastActivity("root1315131135"));
    assertEquals(-1, Utils.getTimeOfLastActivity("root-1dsaf315131135"));
  }
  
  public void testGetAuthorOfLastActivity() throws Exception {
    assertEquals("root", Utils.getAuthorOfLastActivity("root-1315131135"));
    assertEquals(null, Utils.getAuthorOfLastActivity("root1315131135"));
    assertEquals("root", Utils.getAuthorOfLastActivity("root-1dsaf315131135"));
  }
  
  public void testGetOderBy() throws Exception {
    FAQSetting faqSetting = new FAQSetting();
    faqSetting.setDisplayMode("both");
    faqSetting.setOrderBy("created");
    faqSetting.setOrderType("asc");
    faqSetting.setSortQuestionByVote(true);
    faqSetting.setIsAdmin("TRUE");
    faqSetting.setEmailMoveQuestion("content email move question");
    faqSetting.setEmailSettingSubject("Send notify watched");
    faqSetting.setEmailSettingContent("Question content: &questionContent_ <br/>Response: &questionResponse_ <br/> link: &questionLink_");
    assertEquals("@exo:markVote descending, @exo:createdDate ascending", Utils.getOderBy(faqSetting));;
  }
  
  public void testBuildQueryListOfUser() throws Exception {
    List<String> users = new ArrayList<String>();
    users.add("root");
    users.add("demo");
    assertEquals("@exo:isAdmin = 'root' or @exo:isAdmin = 'demo'", Utils.buildQueryListOfUser("exo:isAdmin", users));
  }
}
