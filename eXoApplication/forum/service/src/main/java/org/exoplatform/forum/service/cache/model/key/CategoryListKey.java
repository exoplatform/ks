package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.forum.service.cache.model.ScopeCacheKey;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class CategoryListKey extends ScopeCacheKey {

  private final String foo;

  public CategoryListKey(String foo) {
    this.foo = foo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CategoryListKey)) return false;
    if (!super.equals(o)) return false;

    CategoryListKey that = (CategoryListKey) o;

    if (foo != null ? !foo.equals(that.foo) : that.foo != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (foo != null ? foo.hashCode() : 0);
    return result;
  }

}
