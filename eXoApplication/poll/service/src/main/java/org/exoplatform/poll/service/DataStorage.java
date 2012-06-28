package org.exoplatform.poll.service;

import org.exoplatform.container.component.ComponentPlugin;

import java.util.List;

public interface DataStorage {

  void addInitialDefaultDataPlugin(ComponentPlugin plugin) throws Exception;
  
  void initDefaultData() throws Exception;

  Poll getPoll(String pollId) throws Exception;

  void savePoll(Poll poll, boolean isNew, boolean isVote) throws Exception;

  Poll removePoll(String pollId);

  void setClosedPoll(Poll poll);

  List<Poll> getPagePoll() throws Exception;

  boolean hasPermissionInForum(String pollPath, List<String> allInfoOfUser) throws Exception;

  PollSummary getPollSummary(List<String> groupOfUser) throws Exception;
}
