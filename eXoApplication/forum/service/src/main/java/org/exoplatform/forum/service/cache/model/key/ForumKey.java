package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.cache.model.ScopeCacheKey;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ForumKey extends ScopeCacheKey {

  private final String categoryId;
  private final String forumId;

  public ForumKey(String categoryId, String forumId) {
    this.categoryId = categoryId;
    this.forumId = forumId;
  }

  public ForumKey(Forum forum) {
    this.categoryId = forum.getCategoryId();
    this.forumId = forum.getId();
  }

  public String getCategoryId() {
    return categoryId;
  }

  public String getForumId() {
    return forumId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ForumKey)) return false;
    if (!super.equals(o)) return false;

    ForumKey forumKey = (ForumKey) o;

    if (categoryId != null ? !categoryId.equals(forumKey.categoryId) : forumKey.categoryId != null) return false;
    if (forumId != null ? !forumId.equals(forumKey.forumId) : forumKey.forumId != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (categoryId != null ? categoryId.hashCode() : 0);
    result = 31 * result + (forumId != null ? forumId.hashCode() : 0);
    return result;
  }
  
}
