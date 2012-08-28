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
package org.exoplatform.ks.extras.injection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.exoplatform.ks.extras.injection.poll.GroupInjector;
import org.exoplatform.ks.extras.injection.poll.PollInjector;
import org.exoplatform.poll.service.PollService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SEA 
 * Author : Vu Duy Tu 
 * tu.duy@exoplatform.com 
 * Jun 15, 2012
 */

public class InjectorPollTestCase extends BaseTestCase {
  GroupInjector                   groupInjector;

  PollInjector                    pollInjector;

  PollService                     pollService;

  OrganizationService             organizationService;

  GroupHandler                    groupHandler;

  private HashMap<String, String> params;

  private Set<String>             pollPublicTeadown;

  private Map<String, String>             pollPrivateTeadown;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    groupInjector = (GroupInjector) getContainer().getComponentInstanceOfType(GroupInjector.class);
    pollInjector = (PollInjector) getContainer().getComponentInstance(PollInjector.class);
    organizationService = (OrganizationService) getContainer().getComponentInstance(OrganizationService.class);
    pollService = (PollService) getContainer().getComponentInstance(PollService.class);
    
    assertNotNull(groupInjector);
    assertNotNull(pollInjector);
    assertNotNull(organizationService);
    assertNotNull(pollService);
    //
    groupHandler = organizationService.getGroupHandler();
    params = new HashMap<String, String>();
    pollPublicTeadown = new HashSet<String>();
    pollPrivateTeadown = new HashMap<String, String>();
  }

  @Override
  public void tearDown() throws Exception {
    for(String pollName : pollPublicTeadown) {
      pollService.removePoll(pollInjector.getPublicPollByName(pollName).getId());
    }
    for(String pollName : pollPrivateTeadown.keySet()) {
      pollService.removePoll(pollInjector.getPrivatePollByName(pollPrivateTeadown.get(pollName), pollName).getId());
    }
    removeAllGroup();
    super.tearDown();
  }

  private void removeAllGroup() throws Exception {
    for (Object gr : groupHandler.findGroups(null)) {
      groupHandler.removeGroup((Group) gr, false);
    }
  }

  private void clearPollPrivate(String groupBaseName, String pollBaseName, int number) {
    for (int i = 0; i < number; i++) {
      pollPrivateTeadown.put(pollBaseName + i, groupBaseName);
    }
  }

  private void clearPollPublic(String pollBaseName, int number) {
    for (int i = 0; i < number; i++) {
      pollPublicTeadown.add(pollBaseName + i);
    }
  }

  public void testDefaultGroup() throws Exception {
    performGroupTest(null);
  }

  public void testPrefixGroup() throws Exception {
    performGroupTest("foo");
  }

  public void testDefaultPollPublic() throws Exception {
    performPollPublicTest(null);
  }

  public void testPrefixPollPublic() throws Exception {
    performPollPublicTest("foo");
  }

  public void testDefaultPollPrivate() throws Exception {
    performPollPrivateTest(null, null);
  }

  public void testPrefixPollPrivate() throws Exception {
    performPollPrivateTest("foo", "bar");
  }

  public void testDefaultPollPrivateFail() throws Exception {
    performPollPrivateFailTest(null, null);
  }

  public void testPrefixPollPrivateFail() throws Exception {
    performPollPrivateFailTest("foo", "bar");
  }

  private void performGroupTest(String prefix) throws Exception {
    params.put(GroupInjector.NUMBER, "5");
    String groupBaseName = (prefix == null ? "bench.group" : prefix);

    if (prefix != null) {
      params.put("prefix", prefix);
    }

    //
    groupInjector.inject(params);

    //
    assertEquals(5, groupInjector.groupNumber(groupBaseName));

    assertNotNull(groupInjector.getGroupByName(groupBaseName + "0"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "1"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "3"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "4"));

    //
    params.clear();
    params.put(GroupInjector.NUMBER, "5");

    if (prefix != null) {
      params.put("prefix", prefix);
    }
    //
    groupInjector.inject(params);

    assertEquals(10, groupInjector.groupNumber(groupBaseName));

    assertNotNull(groupInjector.getGroupByName(groupBaseName + "5"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "6"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "7"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "8"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "9"));
  }

  private void performPollPublicTest(String pollPrefix) throws Exception {
    params.put(GroupInjector.NUMBER, "5");
    String pollBaseName = (pollPrefix == null ? "public.poll" : pollPrefix);

    if (pollPrefix != null) {
      params.put("pollPrefix", pollPrefix);
    }

    params.put("toGroup", "0");
    params.put("pollType", "public");
    pollInjector.inject(params);

    assertEquals(5, pollInjector.pollNumberForPublic(pollBaseName));

    assertNotNull(pollInjector.getPublicPollByName(pollBaseName + "0"));
    assertNotNull(pollInjector.getPublicPollByName(pollBaseName + "1"));
    assertNotNull(pollInjector.getPublicPollByName(pollBaseName + "2"));
    assertNotNull(pollInjector.getPublicPollByName(pollBaseName + "3"));
    assertNotNull(pollInjector.getPublicPollByName(pollBaseName + "4"));

    //
    pollInjector.inject(params);
    assertEquals(10, pollInjector.pollNumberForPublic(pollBaseName));

    assertNotNull(pollInjector.getPublicPollByName(pollBaseName + "5"));
    assertNotNull(pollInjector.getPublicPollByName(pollBaseName + "6"));
    assertNotNull(pollInjector.getPublicPollByName(pollBaseName + "7"));
    assertNotNull(pollInjector.getPublicPollByName(pollBaseName + "8"));
    assertNotNull(pollInjector.getPublicPollByName(pollBaseName + "9"));

    clearPollPublic(pollBaseName, 10);
  }

  private void performPollPrivateTest(String groupPrefix, String pollPrefix) throws Exception {
    params.put(GroupInjector.NUMBER, "5");
    String groupBaseName = (groupPrefix == null ? "bench.group" : groupPrefix);

    if (groupPrefix != null) {
      params.put("prefix", groupPrefix);
    }

    //
    groupInjector.inject(params);

    //
    assertEquals(5, groupInjector.groupNumber(groupBaseName));

    assertNotNull(groupInjector.getGroupByName(groupBaseName + "0"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "1"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "3"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "4"));

    //
    params.clear();
    params.put(GroupInjector.NUMBER, "5");
    String pollBaseName = (pollPrefix == null ? "private.poll" : pollPrefix);

    if (groupPrefix != null) {
      params.put("groupPrefix", groupPrefix);
    }

    if (pollPrefix != null) {
      params.put("pollPrefix", pollPrefix);
    }

    params.put("toGroup", "0");
    params.put("pollType", "private");
    pollInjector.inject(params);

    assertEquals(5, pollInjector.pollNumberForPrivate(groupBaseName + "0", pollBaseName));

    assertNotNull(pollInjector.getPrivatePollByName(groupBaseName + "0", pollBaseName + "0"));
    assertNotNull(pollInjector.getPrivatePollByName(groupBaseName + "0", pollBaseName + "1"));
    assertNotNull(pollInjector.getPrivatePollByName(groupBaseName + "0", pollBaseName + "2"));
    assertNotNull(pollInjector.getPrivatePollByName(groupBaseName + "0", pollBaseName + "3"));
    assertNotNull(pollInjector.getPrivatePollByName(groupBaseName + "0", pollBaseName + "4"));

    //
    pollInjector.inject(params);
    assertEquals(10, pollInjector.pollNumberForPrivate(groupBaseName + "0", pollBaseName));

    assertNotNull(pollInjector.getPrivatePollByName(groupBaseName + "0", pollBaseName + "5"));
    assertNotNull(pollInjector.getPrivatePollByName(groupBaseName + "0", pollBaseName + "6"));
    assertNotNull(pollInjector.getPrivatePollByName(groupBaseName + "0", pollBaseName + "7"));
    assertNotNull(pollInjector.getPrivatePollByName(groupBaseName + "0", pollBaseName + "8"));
    assertNotNull(pollInjector.getPrivatePollByName(groupBaseName + "0", pollBaseName + "9"));

    clearPollPrivate(groupBaseName + "0", pollBaseName, 10);
  }

  private void performPollPrivateFailTest(String groupPrefix, String pollPrefix) throws Exception {
    params.put(GroupInjector.NUMBER, "5");
    String groupBaseName = (groupPrefix == null ? "bench.group" : groupPrefix);

    if (groupPrefix != null) {
      params.put("prefix", groupPrefix);
    }

    //
    groupInjector.inject(params);

    //
    assertEquals(5, groupInjector.groupNumber(groupBaseName));

    assertNotNull(groupInjector.getGroupByName(groupBaseName + "0"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "1"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "3"));
    assertNotNull(groupInjector.getGroupByName(groupBaseName + "4"));

    //
    params.clear();
    params.put(GroupInjector.NUMBER, "5");
    String pollBaseName = (pollPrefix == null ? "private.poll" : pollPrefix);

    if (groupPrefix != null) {
      params.put("groupPrefix", groupPrefix);
    }

    if (pollPrefix != null) {
      params.put("pollPrefix", pollPrefix);
    }

    params.put("toGroup", "");
    params.put("pollType", "private");
    pollInjector.inject(params);

    assertEquals(0, pollInjector.pollNumberForPrivate(groupBaseName + "0", pollBaseName));

    //
    params.put("toGroup", null);
    pollInjector.inject(params);
    assertEquals(0, pollInjector.pollNumberForPrivate(groupBaseName + "0", pollBaseName));

  }

}
