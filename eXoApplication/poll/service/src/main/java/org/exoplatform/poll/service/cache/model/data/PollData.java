package org.exoplatform.poll.service.cache.model.data;

import org.exoplatform.ks.common.cache.CachedData;
import org.exoplatform.poll.service.Poll;

import java.util.Date;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class PollData implements CachedData<Poll> {

  private final String id;
  private final String parentPath;
  private final String oldParentPath;
  private final String owner;
  private final Date createdDate;
  private final String modifiedBy;
  private final Date modifiedDate;
  private final Date lastVote;
  private final long timeOut;
  private final String question;
  private final String[] option;
  private final String[] vote;
  private final String[] userVote;
  private final boolean isMultiCheck;
  private final boolean isClosed;
  private final boolean isAgainVote;
  private final boolean showVote;
  private final String expire;
  private final String isAdmin;

  public PollData(Poll poll) {

    this.id = poll.getId();
    this.parentPath = poll.getParentPath();
    this.oldParentPath = poll.getOldParentPath();
    this.owner = poll.getOwner();
    this.createdDate = poll.getCreatedDate();
    this.modifiedBy = poll.getModifiedBy();
    this.modifiedDate = poll.getModifiedDate();
    this.lastVote = poll.getLastVote();
    this.timeOut = poll.getTimeOut();
    this.question = poll.getQuestion();
    this.option = poll.getOption();
    this.vote = poll.getVote();
    this.userVote = poll.getUserVote();
    this.isMultiCheck = poll.getIsMultiCheck();
    this.isClosed = poll.getIsClosed();
    this.isAgainVote = poll.getIsAgainVote();
    this.showVote = poll.getShowVote();
    this.expire = poll.getExpire();
    this.isAdmin = poll.getIsAdmin();

  }

  public Poll build() {

    Poll poll = new Poll();
    poll.setId(this.id);
    poll.setParentPath(this.parentPath);
    poll.setOldParentPath(this.oldParentPath);
    poll.setOwner(this.owner);
    poll.setCreatedDate(this.createdDate);
    poll.setModifiedBy(this.modifiedBy);
    poll.setModifiedDate(this.modifiedDate);
    poll.setLastVote(this.lastVote);
    poll.setTimeOut(this.timeOut);
    poll.setQuestion(this.question);
    poll.setOption(this.option);
    poll.setVote(this.vote);
    poll.setUserVote(this.userVote);
    poll.setIsMultiCheck(this.isMultiCheck);
    poll.setIsClosed(this.isClosed);
    poll.setIsAgainVote(this.isAgainVote);
    poll.setShowVote(this.showVote);
    poll.setExpire(this.expire);
    poll.setIsAdmin(this.isAdmin);
    return poll;

  }
}
