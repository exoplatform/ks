/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.faq.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestSuite;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.component.test.KernelBootstrap;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tuvd@exoplatform.com
 * Oct 5, 2012  
 */

public abstract class BaseTestCase extends AbstractGateInTest {

  /** . */
  private static KernelBootstrap bootstrap;

  private boolean isGetAllConfig = true;
  
  /** . */
  private static final Map<Class<?>, AtomicLong> counters = new HashMap<Class<?>, AtomicLong>();

  protected BaseTestCase()
  {
     super();
  }

  protected BaseTestCase(String name)
  {
     super(name);
  }

  public PortalContainer getContainer()
  {
     return bootstrap != null ? bootstrap.getContainer() : null;
  }

  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }
  
  protected void begin()
  {
     RequestLifeCycle.begin(getContainer());
  }

  protected void end()
  {
     RequestLifeCycle.end();
  }

  public boolean isGetAllConfig() {
    return isGetAllConfig;
  }

  public void setGetAllConfig(boolean isGetAllConfig) {
    this.isGetAllConfig = isGetAllConfig;
  }

  private void getAllClass(List<Class<?>>classes, Class<?> key) throws Exception {
    classes.add(key);
    if(key.getSuperclass() != null) {
      getAllClass(classes, key.getSuperclass());
    }
  }
  
  @Override
  public void beforeRunBare() throws Exception
  {
     Class<?> key = getClass();
     //
     if (!counters.containsKey(key))
     {
        counters.put(key, new AtomicLong(new TestSuite(getClass()).testCount()));

        //
        bootstrap = new KernelBootstrap(Thread.currentThread().getContextClassLoader());

        // Configure ourselves
        if(isGetAllConfig) {
          List<Class<?>>classes = new ArrayList<Class<?>>();
          getAllClass(classes, key);
          for (Class<?> clazz : classes) {
            bootstrap.addConfiguration(clazz);
          }
        } else {
          bootstrap.addConfiguration(key);
        }

        //
        bootstrap.boot();
     }

  }

  @Override
  protected void afterRunBare()
  {
     Class<?> key = getClass();

     //
     if (counters.get(key).decrementAndGet() == 0)
     {
        bootstrap.dispose();

        //
        bootstrap = null;
     }

  }
}
