package org.exoplatform.forum.service.cache.model.key;

import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.cache.model.ScopeCacheKey;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class TopicKey extends ScopeCacheKey {

  private final String topicPath;
  private final boolean isLastPost;

  public TopicKey(String topicPath, boolean lastPost) {
    this.topicPath = topicPath;
    isLastPost = lastPost;
  }

  public TopicKey(Topic topic) {
    this.topicPath = topic.getPath();
    this.isLastPost = false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TopicKey)) return false;
    if (!super.equals(o)) return false;

    TopicKey topicKey = (TopicKey) o;

    if (isLastPost != topicKey.isLastPost) return false;
    if (topicPath != null ? !topicPath.equals(topicKey.topicPath) : topicKey.topicPath != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (topicPath != null ? topicPath.hashCode() : 0);
    result = 31 * result + (isLastPost ? 1 : 0);
    return result;
  }

}
