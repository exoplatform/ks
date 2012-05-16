package org.exoplatform.forum.service.cache.model.data;

import org.exoplatform.forum.service.cache.model.AbstractListData;
import org.exoplatform.forum.service.cache.model.key.ForumKey;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ListWatchData extends AbstractListData<WatchData> {

  public ListWatchData(List<WatchData> ids) {
    super(ids);
  }

}
