package org.exoplatform.forum.service.cache.model;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;

import javax.jcr.RepositoryException;
import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ScopeCacheKey implements Serializable {

  public final static ScopeCacheKey NULL = new ScopeCacheKey();

  private final String scope;

  public ScopeCacheKey() {
    scope = getCurrentRepositoryName();
  }

  public String getScope() {
    return scope;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ScopeCacheKey)) {
      return false;
    }

    ScopeCacheKey that = (ScopeCacheKey) o;

    if (scope != null ? !scope.equals(that.scope) : that.scope != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return scope != null ? scope.hashCode() : 0;
  }

  public static String getCurrentRepositoryName() {
    RepositoryService repositoryService = (RepositoryService)
                                          ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    try {
      return repositoryService.getCurrentRepository().getConfiguration().getName();
    }
    catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

}
