package org.exoplatform.forum.service.cache.model.data;

import org.exoplatform.forum.service.cache.model.AbstractListData;
import org.exoplatform.forum.service.cache.model.key.CategoryKey;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ListCategoryData extends AbstractListData<CategoryKey> {

  public ListCategoryData(List<CategoryKey> ids) {
    super(ids);
  }

}
