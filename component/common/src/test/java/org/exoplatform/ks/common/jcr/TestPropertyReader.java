package org.exoplatform.ks.common.jcr;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;

import junit.framework.TestCase;

import org.jboss.util.property.PropertyException;

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

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class TestPropertyReader extends TestCase {

  private Node           node;

  private Property       prop;

  private PropertyReader reader;

  protected void setUp() throws Exception {
    super.setUp();
    node = mock(Node.class);
    prop = mock(Property.class);
    reader = new PropertyReader(node);
  }

  public void testD() throws Exception {
    bindProperty("foo");
    when(prop.getDouble()).thenReturn(111d);
    assertEquals(111d, reader.d("foo"));
  }

  public void testBool() throws Exception {
    boolean expected = true;
    bindProperty("bool");
    when(prop.getBoolean()).thenReturn(expected);
    boolean actual = reader.bool("bool");
    assertEquals(expected, actual);

    bindProperty("bool2");
    when(prop.getBoolean()).thenReturn(false);
    reader.bool("bool2", true);
    assertEquals(expected, actual);
  }

  public void testDate() throws Exception {
    Calendar cal = new GregorianCalendar();
    Date expected = cal.getTime();
    bindProperty("date");
    when(prop.getDate()).thenReturn(cal);
    Date actual = reader.date("date");
    assertEquals(expected, actual);
  }

  public void testLong() throws Exception {
    long expected = 111L;
    bindProperty("long");
    when(prop.getLong()).thenReturn(expected);
    long actual = reader.l("long");
    assertEquals(expected, actual);

    // test default value
    when(prop.getLong()).thenThrow(new PropertyException());
    expected = 123L;
    actual = reader.l("long", expected);
    assertEquals(expected, actual);
  }

  public void testList() throws Exception {
    String[] expected = new String[] { "foo", "bar", "zed" };
    bindProperty("list");
    Value[] mockValues = new Value[] { value(expected[0]), value(expected[1]), value(expected[2]) };
    when(prop.getValues()).thenReturn(mockValues);
    List<String> actual = reader.list("list");
    assertContains(actual, expected);

    // test default value
    when(prop.getValues()).thenThrow(new PropertyException());
    actual = reader.list("list", Arrays.asList(new String[] { "1", "2", "3" }));
    assertContains(actual, "1", "2", "3");
  }

  public void testString() throws Exception {
    String expected = "foo";
    bindProperty("string");
    when(prop.getString()).thenReturn(expected);

    String actual = reader.string("string");
    assertEquals(actual, expected);

    when(prop.getString()).thenThrow(new PropertyException());
    expected = "bar";
    actual = reader.string("string", expected);
    assertEquals(actual, expected);

  }

  public void testStrings() throws Exception {
    String[] expected = new String[] { "foo", "bar", "zed" };
    bindProperty("strings");
    Value[] mockValues = new Value[] { value(expected[0]), value(expected[1]), value(expected[2]) };
    when(prop.getValues()).thenReturn(mockValues);
    String[] actual = reader.strings("strings");
    assertContains(actual, expected);

    // test default value
    when(prop.getValues()).thenThrow(new PropertyException());
    actual = reader.strings("strings", new String[] { "1", "2", "3" });
    assertContains(actual, "1", "2", "3");
  }

  /**
   * By a {@link Property} to the field Node
   * @param propertyName name of the property to 
   * @throws Exception
   */
  private void bindProperty(String propertyName) throws Exception {
    when(node.getProperty(propertyName)).thenReturn(prop);
  }

  /**
   * Creates a Mock object for a JCR String {@link Value}
   * @param value actual value to be returned
   * @return
   * @throws Exception
   */
  private Value value(String value) throws Exception {
    Value stub = mock(Value.class);
    when(stub.getString()).thenReturn(value);
    return stub;
  }

  /**
   * Assert a set of expected items to be all contained in a collection
   * 
   * @param actual containment
   * @param expected items expected to be contained
   */
  private <T> void assertContains(Collection<T> actual, T... expected) {

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
   * Assert a set of expected string items to be all contained in a string array
   * 
   * @param actual containment
   * @param expected items expected to be contained
   */
  private void assertContains(String[] actual, String... expected) {
    assertTrue(Arrays.asList(actual).containsAll(Arrays.asList(expected)));
  }
}
