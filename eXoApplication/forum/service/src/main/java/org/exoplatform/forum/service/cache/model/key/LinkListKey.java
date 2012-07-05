package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.forum.service.cache.model.ScopeCacheKey;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class LinkListKey extends ScopeCacheKey {

  private final String strQueryCate;
  private final String strQueryForum;

  public LinkListKey(String strQueryCate, String strQueryForum) {
    this.strQueryCate = strQueryCate;
    this.strQueryForum = strQueryForum;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LinkListKey)) return false;
    if (!super.equals(o)) return false;

    LinkListKey that = (LinkListKey) o;

    if (strQueryCate != null ? !strQueryCate.equals(that.strQueryCate) : that.strQueryCate != null) return false;
    if (strQueryForum != null ? !strQueryForum.equals(that.strQueryForum) : that.strQueryForum != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (strQueryCate != null ? strQueryCate.hashCode() : 0);
    result = 31 * result + (strQueryForum != null ? strQueryForum.hashCode() : 0);
    return result;
  }
  
}
