/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.ks.extras.injection;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

/**
 * @author <a href="mailto:thanhvc@exoplatform.com">Thanh Vu</a>
 * @version $Revision$
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.forum.component.core.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.forum.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.forum.test.portal-configuration.xml")
})
public abstract class BaseTestCase extends AbstractKernelTest {
 
  
  @Override
  public void setUp() throws Exception {
    //
    begin();
    
    
  }

  @Override
  public void tearDown() throws Exception {
    
    //
    end();
  }
  
  
}
