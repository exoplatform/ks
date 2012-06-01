package org.exoplatform.forum.service.cache.model.data;

import java.util.Date;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.cache.model.CachedData;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class CategoryData implements CachedData<Category> {

  private static final long serialVersionUID = 1L;

  public final static CategoryData NULL = new CategoryData(new Category());

  private final String id;
  private final String owner;
  private final String path;
  private final long categoryOrder;
  private final Date createdDate;
  private final String modifiedBy;
  private final Date modifiedDate;
  private final String name;
  private final String description;
  private final String[] moderators;
  private final String[] userPrivate;
  private final String[] createTopicRole;
  private final String[] viewer;
  private final String[] poster;
  private final long forumCount;
  private final String[] emailNotification;

  public CategoryData(Category category) {

    this.id = category.getId();
    this.owner = category.getOwner();
    this.path = category.getPath();
    this.categoryOrder = category.getCategoryOrder();
    this.createdDate = category.getCreatedDate();
    this.modifiedBy = category.getModifiedBy();
    this.modifiedDate = category.getModifiedDate();
    this.name = category.getCategoryName();
    this.description = category.getDescription();
    this.moderators = category.getModerators();
    this.userPrivate = category.getUserPrivate();
    this.createTopicRole = category.getCreateTopicRole();
    this.viewer = category.getViewer();
    this.poster = category.getPoster();
    this.forumCount = category.getForumCount();
    this.emailNotification = category.getEmailNotification();

  }

  public Category build() {

    //
    if (this == NULL) {
      return null;
    }

    //
    Category category = new Category();
    category.setId(this.id);
    category.setOwner(this.owner);
    category.setPath(this.path);
    category.setCategoryOrder(this.categoryOrder);
    category.setCreatedDate(this.createdDate);
    category.setModifiedBy(this.modifiedBy);
    category.setModifiedDate(this.modifiedDate);
    category.setCategoryName(this.name);
    category.setDescription(this.description);
    category.setModerators(this.moderators);
    category.setCreateTopicRole(this.createTopicRole);
    category.setViewer(this.viewer);
    category.setPoster(this.poster);
    category.setForumCount(this.forumCount);
    category.setEmailNotification(this.emailNotification);
    if (this.userPrivate != null) {
      category.setUserPrivate(userPrivate);
    }
    return category;

  }
  
  public String getId() {
    return this.id;
  }
}
