package org.exoplatform.forum.service.cache.model.data;

import org.exoplatform.forum.service.cache.model.AbstractListData;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ListLinkData extends AbstractListData<LinkData> {

  public ListLinkData(List<LinkData> ids) {
    super(ids);
  }

}
