package org.exoplatform.forum.service.cache.model.data;

import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.cache.model.CachedData;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class PostData  implements CachedData<Post> {
  
  private final String id;
  private final String path;
  private final String owner;
  private final Date createdDate;
  private final String modifiedBy;
  private final Date modifiedDate;
  private final String editReason;
  private final String name;
  private final String message;
  private final String remoteAddr;
  private final String icon;
  private final String link;
  private final boolean isApproved;
  private final boolean isHidden;
  private final boolean isWaiting;
  private final boolean isActiveByTopic;
  private final String[] userPrivate;
  private final long numberAttach;
  private final ForumAttachment[] attachments;

  public PostData(Post post) {
    this.id = post.getId();
    this.path = post.getPath();
    this.owner = post.getOwner();
    this.createdDate = post.getCreatedDate();
    this.modifiedBy = post.getModifiedBy();
    this.modifiedDate = post.getModifiedDate();
    this.editReason = post.getEditReason();
    this.name = post.getName();
    this.message = post.getMessage();
    this.remoteAddr = post.getRemoteAddr();
    this.icon = post.getIcon();
    this.link = post.getLink();
    this.isApproved = post.getIsApproved();
    this.isHidden = post.getIsHidden();
    this.isWaiting = post.getIsWaiting();
    this.isActiveByTopic = post.getIsActiveByTopic();
    this.userPrivate = post.getUserPrivate();
    this.numberAttach = post.getNumberAttach();
    if (post.getAttachments() != null) {
      this.attachments = post.getAttachments().toArray(new ForumAttachment[]{});
    } else {
      this.attachments = null;
    }
  }

  public Post build() {

    Post post = new Post();

    post.setId(this.id);
    post.setPath(this.path);
    post.setOwner(this.owner);
    post.setCreatedDate(this.createdDate);
    post.setModifiedBy(this.modifiedBy);
    post.setModifiedDate(this.modifiedDate);
    post.setEditReason(this.editReason);
    post.setName(this.name);
    post.setMessage(this.message);
    post.setRemoteAddr(this.remoteAddr);
    post.setIcon(this.icon);
    post.setLink(this.link);
    post.setIsApproved(this.isApproved);
    post.setIsHidden(this.isHidden);
    post.setIsWaiting(this.isWaiting);
    post.setIsActiveByTopic(this.isActiveByTopic);
    post.setUserPrivate(this.userPrivate);
    post.setNumberAttach(this.numberAttach);
    if (this.attachments != null) {
      post.setAttachments(Arrays.asList(this.attachments));
    }

    return post;

  }

}
