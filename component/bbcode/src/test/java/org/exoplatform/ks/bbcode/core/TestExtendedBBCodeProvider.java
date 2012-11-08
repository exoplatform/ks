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
package org.exoplatform.ks.bbcode.core;

import java.util.Collection;

import junit.framework.TestCase;

import org.exoplatform.ks.bbcode.api.BBCode;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class TestExtendedBBCodeProvider extends TestCase {

  private BBCodeRenderer         renderer;

  private MemoryBBCodeService    bbcodeService;

  private ExtendedBBCodeProvider provider;

  protected void setUp() throws Exception {
    super.setUp();
    bbcodeService = new MemoryBBCodeService();
    provider = new ExtendedBBCodeProvider();
    provider.setBBCodeService(bbcodeService);
    renderer = new BBCodeRenderer();
    renderer.setBbCodeProvider(provider);
  }

  protected void tearDown() throws Exception {
    clearAllBBcode();
    super.tearDown();
  }

  public void testGetBBCodes() throws Exception {

    // active BBCodes are cached
    registerBBCode("FOO", "");

    assertEquals("FOO", provider.getBBCode("FOO").getTagName());

    // = prefix for options
    registerOptBBCode("BAR", "");
    BBCode alt = provider.getBBCode("BAR=");
    assertNotNull(alt);
    assertTrue(alt.isOption());
    assertEquals("BAR=", alt.getId());
    assertEquals("BAR", alt.getTagName());
  }

  public void testGetSupportedBBCodes() {
    assertEmpty(provider.getSupportedBBCodes());

    registerBBCode("FOO", "FOO");
    registerOptBBCode("FOO", "FOO-OPT");
    registerBBCode("BAR", "BAR");

    assertContains(provider.getSupportedBBCodes(), "FOO", "BAR");

    BBCode code = provider.getBBCode("FOO=");
    assertNotNull(code.getTagName());

  }

  private void assertContains(Collection<String> actual, String... strs) {
    for (int i = 0; i < strs.length; i++) {
      if (!actual.contains(strs[i])) {
        assertTrue(false);
      }
    }
    assertTrue(true);
  }

  private void assertEmpty(Collection<String> actual) {
    if (actual == null || actual.size() == 0) {
      assertTrue(true);
    } else {
      assertTrue(false);
    }
  }

  private void registerBBCode(String tagName, String replacement) {
    BBCode foo = new BBCode();
    foo.setReplacement(replacement);
    foo.setTagName(tagName);
    foo.setActive(true);
    foo.setOption(false);
    bbcodeService.addBBCode(foo);
  }

  private void registerOptBBCode(String tagName, String replacement) {
    BBCode foo = new BBCode();
    foo.setReplacement(replacement);
    foo.setTagName(tagName);
    foo.setActive(true);
    foo.setOption(true);
    bbcodeService.addBBCode(foo);
  }

  private void clearAllBBcode() throws Exception {
    for (BBCode bbcode : bbcodeService.getAll()) {
      bbcodeService.delete(bbcode.getId());
    }
  }

}
