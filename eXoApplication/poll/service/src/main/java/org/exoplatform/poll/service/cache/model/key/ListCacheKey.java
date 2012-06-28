package org.exoplatform.poll.service.cache.model.key;

import org.apache.commons.collections.list.UnmodifiableList;
import org.exoplatform.ks.common.cache.ScopeCacheKey;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ListCacheKey <T extends Serializable> extends ScopeCacheKey {
  
  private final List<T> l;

  public ListCacheKey(List<T> l) {
    if (l == null) {
      this.l = Collections.emptyList();
    } else {
      this.l = UnmodifiableList.decorate(l);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ListCacheKey)) return false;
    if (!super.equals(o)) return false;

    ListCacheKey that = (ListCacheKey) o;

    if (l != null ? !l.equals(that.l) : that.l != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (l != null ? l.hashCode() : 0);
    return result;
  }

}
