package org.exoplatform.poll.service.cache.model.data;

import org.exoplatform.ks.common.cache.AbstractListData;
import org.exoplatform.poll.service.cache.model.key.PollKey;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ListPollData extends AbstractListData<PollKey> {

  public ListPollData(final List<PollKey> ids) {
    super(ids);
  }

}
