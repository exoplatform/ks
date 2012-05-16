package org.exoplatform.forum.service.cache.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class AbstractListData<T> implements Serializable {

  private final List<T> ids;

  public AbstractListData(final List<T> ids) {
    this.ids = ids;
  }

  public List<T> getIds() {
    return ids;
  }
  
}