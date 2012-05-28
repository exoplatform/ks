package org.exoplatform.poll.service.cache.model.key;

import org.exoplatform.ks.common.cache.ScopeCacheKey;
import org.exoplatform.poll.service.Poll;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class PollKey extends ScopeCacheKey {

  private final String id;

  public PollKey(String id) {
    this.id = id;
  }

  public PollKey(Poll poll) {
    this.id = poll.getId();
  }

  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PollKey)) return false;
    if (!super.equals(o)) return false;

    PollKey pollKey = (PollKey) o;

    if (id != null ? !id.equals(pollKey.id) : pollKey.id != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (id != null ? id.hashCode() : 0);
    return result;
  }
  
}
