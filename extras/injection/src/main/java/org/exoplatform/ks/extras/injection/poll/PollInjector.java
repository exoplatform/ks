package org.exoplatform.ks.extras.injection.poll;

import java.util.HashMap;
import java.util.Random;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ks.extras.injection.utils.LoremIpsum4J;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.PollNodeTypes;
import org.exoplatform.poll.service.Utils;

public class PollInjector extends AbstractPollInjector {

  private static final String PARENT_PATH_GROUP = "/" + PollNodeTypes.APPLICATION_DATA + "/" + PollNodeTypes.EXO_POLLS;

  private static final String POLL_PREFIX       = "pollPrefix";

  private static final String POLL_TYPE         = "pollType";

  private static final String GROUP_PREFIX      = "groupPrefix";

  private static final String NUMBER            = "number";

  private static final String TO_GROUP          = "toGroup";

  private boolean              isPublic          = true;

  private LoremIpsum4J        ipsum4j;

  public PollInjector() {
    super();
    ipsum4j = new LoremIpsum4J();
  }
  
  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    int number = getIntParam(params, NUMBER);
    //private | public
    String pollType = params.get(POLL_TYPE);
    String pollPrefix = params.get(POLL_PREFIX);
    String groupPrefix = params.get(GROUP_PREFIX);
    String toGroup = params.get(TO_GROUP);
    
    //
    if (isPublic = "public".equals(pollType)) {
      init(null, pollPrefix, null, null, null);
      
      //
      String parentPath = ExoContainerContext.getCurrentContainer().getContext().getName();
      if (Utils.isEmpty(parentPath)) {
        parentPath = "portal";
      }
      parentPath += "/" + PollNodeTypes.POLLS;
      injectPoll("", parentPath, number);
    } else {
      
      if (Utils.isEmpty(toGroup)) {
        getLog().info("Private poll: groupPrefix or toGroup value is wrong! Please set it exactly with 'groupPrefix' or 'toGroup' value. Aborting injection ...");
        return;
      }
      
      init(groupPrefix, null, pollPrefix, toGroup, null);
      
      //
      String injectToGroupName = groupBase + toGroup;
      String parentPath = injectToGroupName + PARENT_PATH_GROUP;
      injectPoll(injectToGroupName, parentPath, number);
    }
  }

  private void injectPoll(String toGroupName, String parentPath, int number) throws Exception {
    String[] options = createOption(5);
    String[] votes = {"0.0", "0.0", "0.0", "0.0", "0.0"};
    
    //
    for (int i = 0; i < number; i++) {
      Poll poll = new Poll();
      poll.setParentPath(parentPath);
      poll.setQuestion(isPublic ? pollPublicName() : pollPrivateName());
      poll.setOption(options);
      poll.setVote(votes);
      poll.setOwner("root");
      poll.setIsMultiCheck(new Random().nextBoolean());
      
      pollService.savePoll(poll, true, false);
      
      //
      if (isPublic) {
        getLog().info("Public poll '" + poll.getQuestion() + "' generated.");
      } else {
        getLog().info("Private poll '" + poll.getQuestion() + "' generated into '"  + toGroupName + "' group");
      }
      
      
      //
      if (isPublic) {
        pollPublicNumber++;
      } else {
        pollPrivateNumber++;
      }
    }

  }
  
  private String[] createOption(int size) {
    String[] options = new String[size];
    for (int i = 0; i < size; i++) {
      options[i] = (isPublic ? pollPublicName() : pollPrivateName()) + "_option" + (i+1) + " " + ipsum4j.getWords(10);
    }
    return options;
  }

  
}
