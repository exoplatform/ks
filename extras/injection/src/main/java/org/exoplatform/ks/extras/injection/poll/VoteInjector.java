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
package org.exoplatform.ks.extras.injection.poll;

import java.util.HashMap;
import java.util.Random;

import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.Utils;

/**
 * Created by The eXo Platform SAS 
 * Author : Vu Duy Tu tuvd@exoplatform.com 
 * Aug 30, 2012
 */
public class VoteInjector extends AbstractPollInjector {

  private static final String FROM_POLL      = "fromPoll";

  private static final String TO_POLL      = "toPoll";

  private static final String POLL_PREFIX = "pollPrefix";

  private static final String FROM_USER   = "fromUser";

  private static final String TO_USER     = "toUser";

  private static final String USER_PREFIX = "userPrefix";

  private Random random = new Random();
  
  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    // pollVote?fromPoll=1&toPoll=100&pollPrefix=bench.poll&fromUser=10&toUser=50&userPrefix=abc.user

    //
    int fromPoll = getIntParam(params, FROM_POLL);
    int toPoll = getIntParam(params, TO_POLL);
    String pollPrefix = params.get(POLL_PREFIX);

    //
    int fromUser = getIntParam(params, FROM_USER);
    int toUser = getIntParam(params, TO_USER);
    String userPrefix = params.get(USER_PREFIX);
    init(null, pollPrefix, null, null, userPrefix);
    Poll poll;
    for (int i = fromPoll; i <= toPoll; i++) {
      poll = getFullPublicPollByName(pollPublicBase + String.valueOf(i));
      for (int j = fromUser; j <= toUser; j++) {
        String userName = userBase + String.valueOf(j);
        poll = Utils.calculateVote(poll, userName, getRandomVoteOption(poll.getIsMultiCheck()));
        pollService.savePoll(poll, false, true);
      }
    }
  }
  
  private String getRandomVoteOption(boolean isMultiChoice) {
    StringBuilder builder = new StringBuilder();
    if(isMultiChoice) {
      for (int i = 0; i < random.nextInt(4) + 1; i++) {
        if(builder.length() > 0) {
          builder.append(":");
        }
        builder.append(getVoteValue(builder.toString()));
      }
    } else {
      builder.append(random.nextInt(5));
    }
    return builder.toString();
  }
  
  private String getVoteValue(String str) {
    String s = String.valueOf(random.nextInt(5));
    if(str.indexOf(s) >= 0) {
      return getVoteValue(str);
    }
    return s;
  }
  
  

}
