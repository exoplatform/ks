package org.exoplatform.forum.service.cache.model.data;

import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.cache.model.CachedData;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class LinkData implements CachedData<ForumLinkData> {

  private final String id;
  private final String name;
  private final String path;
  private final String type;
  private final boolean isClosed;
  private final boolean isLock;

  public LinkData(ForumLinkData link) {
    this.id = link.getId();
    this.name = link.getName();
    this.path = link.getPath();
    this.type = link.getType();
    this.isClosed = link.getIsClosed();
    this.isLock = link.getIsLock();
  }

  public ForumLinkData build() {

    ForumLinkData link = new ForumLinkData();
    link.setId(this.id);
    link.setName(this.name);
    link.setPath(this.path);
    link.setType(this.type);
    link.setIsClosed(this.isClosed);
    link.setIsLock(this.isLock);
    return link;
    
  }
}
