package org.exoplatform.poll.webservice;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.ws.PollWebservice;
import org.exoplatform.services.rest.impl.ContainerResponse;

public class PollWebserviceTestCase extends AbstractResourceTest {

  protected PollWebservice pollWebservice;

  private final String     RESOURCE_URL = "/ks/poll";

  public void setUp() throws Exception {
    super.setUp();
    pollWebservice = new PollWebservice();
    addResource(pollWebservice, null);
  }

  public void tearDown() throws Exception {
    removeResource(pollWebservice);
    super.tearDown();
  }

  public void testPollService() throws Exception {
    assertNotNull(pollService);
  }

  /**
   * Test {@link PollWebservice#viewPoll(String,SecurityContext,UriInfo)}
   */
  public void testViewPoll() throws Exception {
    Poll poll = new Poll();
    poll.setParentPath(topicPath);
    pollService.savePoll(poll, true, false);
    String resourceUrl = RESOURCE_URL + "/viewpoll/" + poll.getId();
    startSessionAs("root");
    ContainerResponse response = service("GET", resourceUrl, "", null, null);
    assertEquals("containerResponse1.getStatus() must return: " + 200, 200, response.getStatus());
  }

  /**
   * Test {@link PollWebservice#votePoll(String,String,SecurityContext,UriInfo)}
   */
  public void testVotePoll() throws Exception {

    /********************** Single vote **********************/
    Poll poll = new Poll();
    poll.setParentPath(topicPath);
    String[] options = { "abc", "def", "ghi" };
    poll.setOption(options);
    pollService.savePoll(poll, true, false);

    // Single vote for option at index 1 ==> "def"
    String resourceUrl = RESOURCE_URL + "/votepoll/" + poll.getId() + "/1";
    startSessionAs("root");
    ContainerResponse response = service("GET", resourceUrl, "", null, null);

    // Test the status of response (must be 200)
    assertEquals("containerResponse1.getStatus() must return: " + 200, 200, response.getStatus());

    Poll p = (Poll) response.getEntity();

    // Test percent of each vote
    assertEquals("0.0", p.getVote()[0]);
    assertEquals("100.0", p.getVote()[1]);
    assertEquals("0.0", p.getVote()[2]);

    // Test the number of vote, here is 1
    assertEquals(1, Integer.parseInt(p.getInfoVote()[p.getInfoVote().length-1]));

    /***************************************************/

    // Update vote for option at index 0 ==> "abc"
    resourceUrl = RESOURCE_URL + "/votepoll/" + poll.getId() + "/0";
    startSessionAs("root");
    response = service("GET", resourceUrl, "", null, null);

    // Test the status of response (must be 200)
    assertEquals("containerResponse1.getStatus() must return: " + 200, 200, response.getStatus());

    Poll pUpdate = (Poll) response.getEntity();

    // Test percent of each vote
    assertEquals("100.0", pUpdate.getVote()[0]);
    assertEquals("0.0", pUpdate.getVote()[1]);
    assertEquals("0.0", pUpdate.getVote()[2]);

    /***************************************************/

    // Add new vote with new user "demo" for option at index 2 ==> "ghi"
    resourceUrl = RESOURCE_URL + "/votepoll/" + poll.getId() + "/2";
    startSessionAs("demo");
    response = service("GET", resourceUrl, "", null, null);

    // Test the status of response (must be 200)
    assertEquals("containerResponse1.getStatus() must return: " + 200, 200, response.getStatus());

    Poll pNew = (Poll) response.getEntity();

    // Test percent of each vote
    assertEquals("50.0", pNew.getVote()[0]);
    assertEquals("0.0", pNew.getVote()[1]);
    assertEquals("50.0", pNew.getVote()[2]);

    // Test the number of vote, here is 2
    assertEquals(2, Integer.parseInt(pNew.getInfoVote()[pNew.getInfoVote().length-1]));

    /********************** Multi vote *****************************/

    Poll pollMulti = new Poll();
    pollMulti.setParentPath(topicPath);
    String[] optionsMulti = { "abc", "def", "ghi", "jqk" };
    pollMulti.setOption(optionsMulti);
    pollMulti.setIsMultiCheck(true);
    pollService.savePoll(pollMulti, true, false);

    // Add new multi vote with new user "mary" for option at index 1 and 2 ==>
    // "def" and "ghi"
    String resourceUrlMulti = RESOURCE_URL + "/votepoll/" + pollMulti.getId() + "/1:2";
    startSessionAs("mary");
    ContainerResponse responseMulti = service("GET", resourceUrlMulti, "", null, null);

    // Test the status of response (must be 200)
    assertEquals("containerResponse1.getStatus() must return: " + 200,
                 200,
                 responseMulti.getStatus());

    Poll pMulti = (Poll) responseMulti.getEntity();

    // Test percent of each vote
    assertEquals("0.0", pMulti.getVote()[0]);
    assertEquals("50.0", pMulti.getVote()[1]);
    assertEquals("50.0", pMulti.getVote()[2]);
    assertEquals("0.0", pMulti.getVote()[3]);

    // Test the number of vote, here is 1 (one vote but multi options)
    assertEquals(1, Integer.parseInt(pMulti.getInfoVote()[pMulti.getInfoVote().length-1]));

    /***************************************************/

    // Add update multi vote by "mary" for option at index 0 and 2 ==> "abc" and
    // "ghi"
    resourceUrlMulti = RESOURCE_URL + "/votepoll/" + pollMulti.getId() + "/0:2";
    startSessionAs("mary");
    responseMulti = service("GET", resourceUrlMulti, "", null, null);

    // Test the status of response (must be 200)
    assertEquals("containerResponse1.getStatus() must return: " + 200,
                 200,
                 responseMulti.getStatus());

    Poll pMultiUpdate = (Poll) responseMulti.getEntity();

    // Test percent of each vote
    assertEquals("50.0", pMultiUpdate.getVote()[0]);
    assertEquals("0.0", pMultiUpdate.getVote()[1]);
    assertEquals("50.0", pMultiUpdate.getVote()[2]);
    assertEquals("0.0", pMultiUpdate.getVote()[3]);

    // Test the number of vote, here is 1 (one vote but multi options)
    assertEquals(1, Integer.parseInt(pMulti.getInfoVote()[pMulti.getInfoVote().length-1]));

    /***************************************************/

    // Add new multi vote by "demo" for option at index 1 and 3 ==> "def" and
    // "jqk"
    resourceUrlMulti = RESOURCE_URL + "/votepoll/" + pollMulti.getId() + "/1:3";
    startSessionAs("demo");
    responseMulti = service("GET", resourceUrlMulti, "", null, null);

    // Test the status of response (must be 200)
    assertEquals("containerResponse1.getStatus() must return: " + 200,
                 200,
                 responseMulti.getStatus());

    Poll pMultiNew = (Poll) responseMulti.getEntity();

    // Test percent of each vote 
    assertEquals("25.0", pMultiNew.getVote()[0]);
    assertEquals("25.0", pMultiNew.getVote()[1]);
    assertEquals("25.0", pMultiNew.getVote()[2]);
    assertEquals("25.0", pMultiNew.getVote()[3]);

    // Test the number of vote, here is 2
    assertEquals(2, Integer.parseInt(pMultiNew.getInfoVote()[pMultiNew.getInfoVote().length-1]));
  }
}
