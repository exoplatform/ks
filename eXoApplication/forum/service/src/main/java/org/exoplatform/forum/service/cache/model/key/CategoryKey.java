package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.cache.model.ScopeCacheKey;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class CategoryKey extends ScopeCacheKey {

  private final String id;

  public CategoryKey(String id) {
    this.id = id;
  }

  public CategoryKey(Category category) {
    this.id = category.getId();
  }



  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CategoryKey)) return false;
    if (!super.equals(o)) return false;

    CategoryKey that = (CategoryKey) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (id != null ? id.hashCode() : 0);
    return result;
  }
  
}