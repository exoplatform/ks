/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.service;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.RepositoryServiceConfiguration;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class FakeRepositoryService implements RepositoryService {

  public boolean canRemoveRepository(String repositoryName) throws RepositoryException {
    
    return false;
  }

  public void createRepository(RepositoryEntry repositoryEntry) throws RepositoryConfigurationException,
                                                               RepositoryException {
  }

  public RepositoryServiceConfiguration getConfig() {
    
    return null;
  }

  public ManageableRepository getCurrentRepository() throws RepositoryException {
    
    return null;
  }

  public ManageableRepository getDefaultRepository() throws RepositoryException,
                                                    RepositoryConfigurationException {
    
    return null;
  }

  public ManageableRepository getRepository() throws RepositoryException,
                                             RepositoryConfigurationException {
    
    return null;
  }

  public ManageableRepository getRepository(String name) throws RepositoryException,
                                                        RepositoryConfigurationException {
    
    return null;
  }

  public void removeRepository(String repositoryName) throws RepositoryException {
  }

  public void setCurrentRepositoryName(String repositoryName) throws RepositoryConfigurationException {
  }
  
  public void removeRepository(String repositoryName, boolean forceRemove) throws RepositoryException{
  }

}
