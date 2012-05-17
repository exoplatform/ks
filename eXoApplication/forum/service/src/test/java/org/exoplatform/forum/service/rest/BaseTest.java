/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.forum.service.rest;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.services.rest.impl.ProviderBinder;
import org.exoplatform.services.rest.impl.RequestHandlerImpl;
import org.exoplatform.services.rest.impl.ResourceBinder;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration1.xml"), 
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/rest/exo.ks.webservice.test-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/rest/exo.portal.component.portal-configuration1.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/rest/exo.portal.component.portal-configuration2.xml")
  })
public abstract class BaseTest extends AbstractKernelTest {

  protected PortalContainer container;
  
  protected ProviderBinder providers;

  protected ResourceBinder     binder;

  protected RequestHandlerImpl requestHandler;
  
  protected OrganizationService  orgService;
 
  public BaseTest() {
    container = PortalContainer.getInstance();
    orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    binder = (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    requestHandler = (RequestHandlerImpl) container.getComponentInstanceOfType(RequestHandlerImpl.class);
    ProviderBinder.setInstance(new ProviderBinder());
    providers = ProviderBinder.getInstance();
    ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providers));
    binder.clear();
  }

  public void setUp() throws Exception {
  }
  
  protected void start() {
    ((ComponentRequestLifecycle)orgService).startRequest(container);
  }
  
  protected void stop() {
    ((ComponentRequestLifecycle)orgService).endRequest(container);
  }  
  
  public void tearDown() throws Exception {
  }

  public void registry(Object resource) {
    try {
      binder.addResource(resource, null);
    } catch (Exception e) {
      log.debug("Can not add resource for class: " + resource.getClass());
    }
  }

  public void registry(Class<?> resourceClass) throws Exception {
    binder.addResource(resourceClass, null);
  }

  public void unregistry(Object resource) {
    binder.removeResource(resource.getClass());
  }

  public void unregistry(Class<?> resourceClass) {
    binder.removeResource(resourceClass);
  }

}
