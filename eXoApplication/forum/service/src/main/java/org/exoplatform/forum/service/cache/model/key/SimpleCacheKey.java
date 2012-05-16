package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.forum.service.cache.model.ScopeCacheKey;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class SimpleCacheKey extends ScopeCacheKey {

  private final String type;
  private final String key;

  public SimpleCacheKey(String key) {
    this(null, key);
  }

  public SimpleCacheKey(String type, String key) {
    this.type = type;
    this.key = key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SimpleCacheKey)) return false;
    if (!super.equals(o)) return false;

    SimpleCacheKey that = (SimpleCacheKey) o;

    if (key != null ? !key.equals(that.key) : that.key != null) return false;
    if (type != null ? !type.equals(that.type) : that.type != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (key != null ? key.hashCode() : 0);
    return result;
  }

}
