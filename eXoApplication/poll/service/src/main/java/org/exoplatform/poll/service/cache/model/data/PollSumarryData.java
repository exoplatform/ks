package org.exoplatform.poll.service.cache.model.data;

import org.apache.commons.collections.list.UnmodifiableList;
import org.exoplatform.ks.common.cache.CachedData;
import org.exoplatform.poll.service.PollSummary;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class PollSumarryData implements CachedData<PollSummary> {

  private final String id;
  private final List<String> pollId;
  private final List<String> pollName;
  private final List<String> groupPrivate;
  private final String isAdmin;
  
  public PollSumarryData(PollSummary summary) {
    this.id = summary.getId();
    this.pollId = UnmodifiableList.decorate(summary.getPollId());
    this.pollName = UnmodifiableList.decorate(summary.getPollName());
    this.groupPrivate = UnmodifiableList.decorate(summary.getGroupPrivate());
    this.isAdmin = summary.getIsAdmin();
  }

  public PollSummary build() {

    PollSummary summary = new PollSummary();
    summary.setId(this.id);
    summary.setPollId(this.pollId);
    summary.setPollName(this.pollName);
    summary.setGroupPrivate(this.groupPrivate);
    summary.setIsAdmin(this.isAdmin);
    return summary;

  }
  
}
