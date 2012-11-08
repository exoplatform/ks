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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.exoplatform.faq.service.InitialDataPluginTestCase.Closure;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tuvd@exoplatform.com
 * Oct 5, 2012  
 */
public abstract class AssertUtils extends Assert {

  private AssertUtils() {
    // hidden
  }

  /**
   * Assert a set of expected items to be all contained in a collection
   * 
   * @param actual containment
   * @param expected items expected to be contained
   */
  public static <T> void assertContains(Collection<T> actual, T... expected) {

    for (T item : expected) {
      boolean found = false;
      for (T obj : actual) {
        if (obj.equals(item)) {
          found = true;
        }
      }
      assertTrue("expected item was not found " + item + "@" + item.hashCode(), found);
    }
  }

  /**
   * Assert a set of expected items NOT to be all contained in a collection
   * 
   * @param actual containment
   * @param expected items expected to be contained
   */
  public static <T> void assertNotContains(Collection<T> actual, T... expected) {
    assertFalse(actual.containsAll(Arrays.asList(expected)));
  }

  /**
   * Assert a set of expected string items to be all contained in a collection
   * 
   * @param actual containment
   * @param expected items expected to be contained
   */
  public static void assertContains(List<String> actual, String... expected) {

    for (String item : expected) {
      boolean found = false;
      for (String obj : actual) {
        if (obj.equals(item)) {
          found = true;
        }
      }
      assertTrue("expected item was not found " + item + "@" + item.hashCode(), found);
    }

  }

  /**
   * Assert a set of expected string items to be all contained in a string array
   * 
   * @param actual containment
   * @param expected items expected to be contained
   */
  public static void assertContains(String[] actual, String... expected) {
    assertTrue(Arrays.asList(actual).containsAll(Arrays.asList(expected)));
  }

  /**
   * Assert a set of expected string items NOT to be all contained in a
   * collection
   * 
   * @param actual containment
   * @param expected items expected to be contained
   */
  public static void assertNotContains(List<String> actual, String... expected) {
    assertFalse(actual.containsAll(Arrays.asList(expected)));
  }

  /**
   * Assert a collection is empty (not null)
   * 
   * @param value
   */
  public static <T> void assertEmpty(Collection<T> value) {
    assertNotNull(value);
    assertEquals(0, value.size());
  }

  /**
   * Assert a collection is not empty and not null
   * 
   * @param <T>
   * @param value
   */
  public static <T> void assertNotEmpty(Collection<T> value) {
    assertNotNull(value);
    assertTrue(value.size() > 0);
  }

  public static <T> void assertEmpty(T[] value) {
    assertNotNull(value);
    assertEquals(0, value.length);
  }

  /**
   * All elements of a list should be contained in the expected array of String
   * 
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertContainsAll(String message, List<String> expected, List<String> actual) {
    assertEquals(message, expected.size(), actual.size());
    assertTrue(message, expected.containsAll(actual));
  }

  /**
   * Assertion method on string arrays
   * 
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertEquals(String message, String[] expected, String[] actual) {
    assertEquals(message, expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(message, expected[i], actual[i]);
    }
  }

  public static void assertException(Closure closure) {
    try {
      closure.dothis();
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }
}
