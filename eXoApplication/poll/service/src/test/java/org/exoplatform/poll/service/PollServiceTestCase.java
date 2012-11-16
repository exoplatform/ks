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
package org.exoplatform.poll.service;

import java.util.List;

import org.exoplatform.poll.base.BaseTestCase;

/**
 * Created by The eXo Platform SAS 
 * Author : Vu Duy Tu 
 *          tuvd@exoplatform.com 
 *   Oct 8, 2012
 */

public class PollServiceTestCase extends BaseTestCase {

  public void setUp() throws Exception {
    super.setUp();
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testPollService() throws Exception {
    assertNotNull(getPollService());
  }

  /**
   * testSavePoll
   * 
   * @throws Exception
   */
  public void testSavePoll() throws Exception {
    // if poll of topic : parentPath = topic.getPath();
    Poll pollTopic = new Poll();
    pollTopic.setParentPath(topicPath);
    pollService.savePoll(pollTopic, true, false);
    assertNotNull(pollService.getPoll(pollTopic.getId()));

    // if poll of Group : parentPath =
    // $GROUP/${PollNodeTypes.APPLICATION_DATA}/${PollNodeTypes.EXO_POLLS}
    Poll pollGroup = new Poll();
    String parentPathGroup = "platform/users/" + PollNodeTypes.APPLICATION_DATA + "/"
        + PollNodeTypes.EXO_POLLS;
    pollGroup.setParentPath(parentPathGroup);
    pollService.savePoll(pollGroup, true, false);
    assertNotNull(pollService.getPoll(pollGroup.getId()));

    // if poll of public: parentPath = $PORTAL/${PollNodeTypes.POLLS}
    Poll pollPulic = new Poll();
    String parentPathPublic = "portal" + PollNodeTypes.POLLS;
    pollPulic.setParentPath(parentPathPublic);
    pollService.savePoll(pollPulic, true, false);
    assertNotNull(pollService.getPoll(pollPulic.getId()));
  }

  /**
   * testGetPoll
   * 
   * @throws Exception
   */
  public void testGetPoll() throws Exception {
    String pollId = pollService.getPagePoll().get(1).getId();
    String path = pollService.getPagePoll().get(1).getParentPath();
    Poll p = pollService.getPoll(pollId);
    assertNotNull(p);
    assertEquals(path, p.getParentPath());
  }

  /**
   * testGetPagePoll
   * 
   * @throws Exception
   */
  public void testGetPagePoll() throws Exception {
    List<Poll> listPoll = pollService.getPagePoll();
    assertEquals(listPoll.size(), 2);
  }

  /**
   * testRemovePoll
   * 
   * @throws Exception
   */
  public void testRemovePoll() throws Exception {
    String pollId = pollService.getPagePoll().get(1).getId();
    pollService.removePoll(pollId);
    assertEquals(1, pollService.getPagePoll().size());
  }

}