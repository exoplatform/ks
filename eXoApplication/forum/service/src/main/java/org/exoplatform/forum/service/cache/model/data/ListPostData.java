package org.exoplatform.forum.service.cache.model.data;

import org.exoplatform.ks.common.cache.model.AbstractListData;
import org.exoplatform.forum.service.cache.model.key.PostKey;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ListPostData extends AbstractListData<PostKey> {

  public ListPostData(List<PostKey> ids) {
    super(ids);
  }

}