package org.exoplatform.forum.service.cache.model.data;

import org.exoplatform.forum.service.cache.model.AbstractListData;
import org.exoplatform.forum.service.cache.model.key.ForumKey;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ListForumData extends AbstractListData<ForumKey> {

  public ListForumData(List<ForumKey> ids) {
    super(ids);
  }

}
