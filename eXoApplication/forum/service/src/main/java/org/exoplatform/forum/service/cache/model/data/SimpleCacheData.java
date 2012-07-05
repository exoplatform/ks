package org.exoplatform.forum.service.cache.model.data;


import org.exoplatform.forum.service.cache.model.CachedData;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class SimpleCacheData<T> implements CachedData<T> {

  private final T t;

  public SimpleCacheData(final T t) {
    this.t = t;
  }

  public T build() {
    return t;
  }
}