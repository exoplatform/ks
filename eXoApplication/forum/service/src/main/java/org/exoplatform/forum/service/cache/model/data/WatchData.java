package org.exoplatform.forum.service.cache.model.data;

import org.exoplatform.forum.service.Watch;
import org.exoplatform.forum.service.cache.model.CachedData;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class WatchData implements CachedData<Watch> {

  private final String id;
  private final String userId;
  private final String email;
  private final String nodePath;
  private final String path;
  private final String typeNode;
  private final boolean isRSS;
  private final boolean isEmail;

  public WatchData(Watch watch) {
    this.id = watch.getId();
    this.userId = watch.getUserId();
    this.email = watch.getEmail();
    this.nodePath = watch.getNodePath();
    this.path = watch.getPath();
    this.typeNode = watch.getTypeNode();
    this.isRSS = watch.isAddWatchByRS();
    this.isEmail = watch.isAddWatchByEmail();
  }

  public Watch build() {
    Watch watch = new Watch();
    watch.setId(this.id);
    watch.setUserId(this.userId);
    watch.setEmail(this.email);
    watch.setNodePath(this.nodePath);
    watch.setPath(this.path);
    watch.setTypeNode(this.typeNode);
    watch.setIsAddWatchByRSS(this.isRSS);
    watch.setIsAddWatchByEmail(this.isEmail);
    return watch;
  }
}
