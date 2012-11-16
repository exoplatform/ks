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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.poll.service;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Apr 28, 2011  
 */
public class UtilsTestCase extends TestCase{
  
  public UtilsTestCase() {
    super();
  }

  public void setUp() throws Exception {
    super.setUp();
  }

  public void testIsEmpty() {
    assertEquals(true, Utils.isEmpty(null));
    assertEquals(true, Utils.isEmpty(""));
    assertEquals(true, Utils.isEmpty(" "));
    assertEquals(false, Utils.isEmpty("abc"));
  }

  public void testIsListEmpty() {
    List<String> list = null;
    assertEquals(true, Utils.isListEmpty(list));
    list = new ArrayList<String>();
    assertEquals(true, Utils.isListEmpty(list));
    list.add("");
    list.add(" ");
    list.add("");
    assertEquals(true, Utils.isListEmpty(list));
    list.add("");
    list.add("foo");
    list.add("");
    assertEquals(false, Utils.isListEmpty(list));
  }
  
  /**
   * testCalculateSingleVote
   * @throws Exception
   */
  public void testCalculateSingleVote() throws Exception{
  //Test the first vote
    Poll p2 = new Poll("pollSingleVote");
    String[] options = {"rgrtgr:thrth","ry4yy5","hrtutyut","retgerhtrh"};
    String[] userVote2 = {};
    p2.setOption(options);
    p2.setUserVote(userVote2);
    
    Poll pFirstVote = Utils.calculateVote(p2, "root", "0");
    String[] sVotes = pFirstVote.getVote();
    assertEquals(sVotes[0],"100.0");
    assertEquals(sVotes[1],"0.0");
    assertEquals(sVotes[2],"0.0");
    assertEquals(sVotes[3],"0.0");
    
    //Test the second vote that is the update vote
    Poll pFirstUpdate = Utils.calculateVote(p2, "root", "3");
    sVotes=pFirstUpdate.getVote();
    assertEquals(sVotes[0],"0.0");
    assertEquals(sVotes[1],"0.0");
    assertEquals(sVotes[2],"0.0");
    assertEquals(sVotes[3],"100.0");
    
    //Test the second vote that is the new vote
    Poll pFirstNew = Utils.calculateVote(p2, "john", "2");
    sVotes=pFirstNew.getVote();
    assertEquals(sVotes[0],"0.0");
    assertEquals(sVotes[1],"0.0");
    assertEquals(sVotes[2],"50.0");
    assertEquals(sVotes[3],"50.0");
    
    
    
    //New poll with single-vote
    Poll p = new Poll("NewPollSingleVote ");
    String[] userVote = {"root:1","john:0","demo:3","mary:1"};
    p.setOption(options);
    p.setUserVote(userVote);
    
    //Test update vote
    Poll pUpdate = Utils.calculateVote(p, "root", "2");
    sVotes=pUpdate.getVote();
    assertEquals(sVotes[0],"25.0");
    assertEquals(sVotes[1],"25.0");
    assertEquals(sVotes[2],"25.0");
    assertEquals(sVotes[3],"25.0");
    
    //Test new vote
    Poll pnew = Utils.calculateVote(p, "binh", "3");
    sVotes = pnew.getVote();
    assertEquals(sVotes[0],"20.0");
    assertEquals(sVotes[1],"20.0");
    assertEquals(sVotes[2],"20.0");
    assertEquals(sVotes[3],"40.0");
  }

  /**
   * testCalculateMultiVote
   * @throws Exception
   */
  public void testCalculateMultiVote() throws Exception{
    
    //Test the first vote
    Poll p2 = new Poll("PollMultiVote");
    String[] options = {"rgrtgr:thrth","ry4yy5","hrtutyut","retgerhtrh"};
    String[] userVote2 = {};
    p2.setOption(options);
    p2.setUserVote(userVote2);
    
    Poll pFirstVote = Utils.calculateVote(p2, "root", "0:2");
    String[] sVotes = pFirstVote.getVote();
    assertEquals(sVotes[0],"50.0");
    assertEquals(sVotes[1],"0.0");
    assertEquals(sVotes[2],"50.0");
    assertEquals(sVotes[3],"0.0");
    
    //Test the second vote that is the update vote
    Poll pSecondUpdate = Utils.calculateVote(p2, "root", "1:3");
    sVotes=pSecondUpdate.getVote();
    assertEquals(sVotes[0],"0.0");
    assertEquals(sVotes[1],"50.0");
    assertEquals(sVotes[2],"0.0");
    assertEquals(sVotes[3],"50.0");
    
    //Test the second vote that is the new vote
    Poll pSecondNew = Utils.calculateVote(p2, "john", "0:2");
    sVotes=pSecondNew.getVote();
    assertEquals(sVotes[0],"25.0");
    assertEquals(sVotes[1],"25.0");
    assertEquals(sVotes[2],"25.0");
    assertEquals(sVotes[3],"25.0");
    
    
    
    //New poll with multi-votes
    Poll p = new Poll("NewPollMultiVote");
    String[] userVote = {"root:1:3","john:0:2","demo:2:3","mary:2:1"};
    p.setOption(options);
    p.setUserVote(userVote);
    
    //Test update vote
    Poll pUpdate = Utils.calculateVote(p, "root", "1:2");
    sVotes=pUpdate.getVote();
    assertEquals(sVotes[0],"12.5");
    assertEquals(sVotes[1],"25.0");
    assertEquals(sVotes[2],"50.0");
    assertEquals(sVotes[3],"12.5");
    
    //Test new vote
    Poll pnew = Utils.calculateVote(p, "binh", "2:3");
    sVotes = pnew.getVote();
    assertEquals(sVotes[0],"10.0");
    assertEquals(sVotes[1],"20.0");
    assertEquals(sVotes[2],"50.0");
    assertEquals(sVotes[3],"20.0");
  }
  
}