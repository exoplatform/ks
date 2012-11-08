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
package org.exoplatform.faq.service;

import junit.framework.TestCase;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.faq.base.AssertUtils;
import org.exoplatform.faq.base.KernelUtils;

/**
 * Unit Tests for {@link InitialDataPlugin}
 */
public class InitialDataPluginSimpleTestCase extends TestCase {

  private static final String DATAZIP_LOCATION = "jar:/conf/Category752b877dc0a8000d0011d6845cfe7ad9.xml";

  public void testInitialDataPlugin() {

    // check params parsing
    InitParams params = new InitParams();
    KernelUtils.addValueParam(params, "location", DATAZIP_LOCATION);
    KernelUtils.addValueParam(params, "forceXML", "true");
    KernelUtils.addValueParam(params, "category", "Foo");
    InitialDataPlugin plugin = new InitialDataPlugin(params);
    AssertUtils.assertTrue(plugin.isForceXML());
    AssertUtils.assertEquals(DATAZIP_LOCATION, plugin.getLocation());

    // check defaults
    params = new InitParams();
    KernelUtils.addValueParam(params, "location", DATAZIP_LOCATION);
    KernelUtils.addValueParam(params, "forceXML", "tqsqdqsrue");
    plugin = new InitialDataPlugin(params);
    assertEquals(false, plugin.isForceXML()); // check defaults to true

  }

  public void testIsZip() {
    InitParams params = new InitParams();
    KernelUtils.addValueParam(params, "location", DATAZIP_LOCATION);
    KernelUtils.addValueParam(params, "forceXML", "true");
    final InitialDataPlugin plugin = new InitialDataPlugin(params);

    assertTrue(plugin.isZip("toto.zip"));
    assertFalse(plugin.isZip("toto.xml"));

    AssertUtils.assertException(new InitialDataPluginTestCase.Closure() {
      public void dothis() {
        plugin.isZip("toto.unsupported");
      }
    });
  }
}
