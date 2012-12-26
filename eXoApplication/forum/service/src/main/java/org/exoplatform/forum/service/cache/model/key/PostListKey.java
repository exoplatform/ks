package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.ks.common.cache.model.ScopeCacheKey;
import org.exoplatform.forum.service.impl.model.PostFilter;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class PostListKey extends ScopeCacheKey {

  private String categoryId;
  private String forumId;
  private String topicId;
  private String isApproved;
  private String isHidden;
  private String isWaiting;
  private String topicPath;
  private String userLogin;

  private int offset;
  private int limit;

  public PostListKey(PostFilter filter, int offset, int limit) {

    this.categoryId = filter.getCategoryId();
    this.forumId = filter.getForumId();
    this.topicId = filter.getTopicId();
    this.isApproved = filter.getIsApproved();
    this.isHidden = filter.getIsHidden();
    this.isWaiting = filter.getIsWaiting();
    this.topicPath = filter.getTopicPath();
    this.userLogin = filter.getUserLogin();
    this.offset = offset;
    this.limit = limit;

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PostListKey)) return false;
    if (!super.equals(o)) return false;

    PostListKey that = (PostListKey) o;

    if (limit != that.limit) return false;
    if (offset != that.offset) return false;
    if (categoryId != null ? !categoryId.equals(that.categoryId) : that.categoryId != null) return false;
    if (forumId != null ? !forumId.equals(that.forumId) : that.forumId != null) return false;
    if (isApproved != null ? !isApproved.equals(that.isApproved) : that.isApproved != null) return false;
    if (isHidden != null ? !isHidden.equals(that.isHidden) : that.isHidden != null) return false;
    if (isWaiting != null ? !isWaiting.equals(that.isWaiting) : that.isWaiting != null) return false;
    if (topicId != null ? !topicId.equals(that.topicId) : that.topicId != null) return false;
    if (topicPath != null ? !topicPath.equals(that.topicPath) : that.topicPath != null) return false;
    if (userLogin != null ? !userLogin.equals(that.userLogin) : that.userLogin != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (categoryId != null ? categoryId.hashCode() : 0);
    result = 31 * result + (forumId != null ? forumId.hashCode() : 0);
    result = 31 * result + (topicId != null ? topicId.hashCode() : 0);
    result = 31 * result + (isApproved != null ? isApproved.hashCode() : 0);
    result = 31 * result + (isHidden != null ? isHidden.hashCode() : 0);
    result = 31 * result + (isWaiting != null ? isWaiting.hashCode() : 0);
    result = 31 * result + (topicPath != null ? topicPath.hashCode() : 0);
    result = 31 * result + (userLogin != null ? userLogin.hashCode() : 0);
    result = 31 * result + offset;
    result = 31 * result + limit;
    return result;
  }
  
}