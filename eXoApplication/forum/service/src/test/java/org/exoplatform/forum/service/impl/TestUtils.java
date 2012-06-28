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
package org.exoplatform.forum.service.impl;

import static org.exoplatform.commons.testing.AssertUtils.assertContains;
import static org.exoplatform.commons.testing.AssertUtils.assertEmpty;
import static org.exoplatform.commons.testing.AssertUtils.assertNotContains;
import static org.exoplatform.commons.testing.mock.JCRMockUtils.stubValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Value;

import junit.framework.TestCase;

import org.exoplatform.forum.service.Utils;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestUtils extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testRemoveCharterStrange() {

    assertEquals("", Utils.removeCharterStrange(null));
    assertEquals("", Utils.removeCharterStrange(""));

    String s = "aaa" + new String(Character.toChars(31)) + "bbb";
    assertEquals("aaabbb", Utils.removeCharterStrange(s));

    String s2 = "aaa" + new String(Character.toChars(32)) + "bbb";
    assertEquals("aaa" + s2.charAt(3) + "bbb", Utils.removeCharterStrange(s2));

  }

  public void testArraysHaveDifferentContent() {

    // if 2 arrays are the same, then it's false
    String[] a = new String[] { "foo", "bar", "zed" };
    String[] b = new String[] { "foo", "bar", "zed" };
    assertFalse(Utils.arraysHaveDifferentContent(a, b));

    // order is not important
    a = new String[] { "foo", "bar", "zed" };
    b = new String[] { "zed", "foo", "bar" };
    assertFalse(Utils.arraysHaveDifferentContent(a, b));

    // if there is a difference in size, it's true
    a = new String[0];
    b = new String[] { "foo", "bar", "zed" };
    assertTrue(Utils.arraysHaveDifferentContent(a, b));

    // if there is a difference in content, it's true
    a = new String[] { "foo", "bar", "zed*" };
    b = new String[] { "foo", "bar", "zed" };
    assertTrue(Utils.arraysHaveDifferentContent(a, b));

    // if there is a difference in content, it's true
    a = new String[] { "foo", "bar", "zed" };
    b = new String[] { "foo", "bar", "zed*" };
    assertTrue(Utils.arraysHaveDifferentContent(a, b));

  }

  public void testListsHaveDifferentContent() {
    // if 2 arrays are the same, then it's false
    List<String> a = Arrays.asList("foo", "bar", "zed");
    List<String> b = Arrays.asList("foo", "bar", "zed");
    assertFalse(Utils.listsHaveDifferentContent(a, b));

    // order is not important
    a = Arrays.asList("foo", "bar", "zed");
    b = Arrays.asList("zed", "foo", "bar");
    assertFalse(Utils.listsHaveDifferentContent(a, b));

    // if there is a difference in size, it's true
    a = Arrays.asList(new String[0]);
    b = Arrays.asList("foo", "bar", "zed");
    assertTrue(Utils.listsHaveDifferentContent(a, b));

    // if there is a difference in content, it's true
    a = Arrays.asList("foo", "bar", "zed*");
    b = Arrays.asList("foo", "bar", "zed");
    assertTrue(Utils.listsHaveDifferentContent(a, b));

    // if there is a difference in content, it's true
    a = Arrays.asList("foo", "bar", "zed");
    b = Arrays.asList("foo", "bar", "zed*");
    assertTrue(Utils.listsHaveDifferentContent(a, b));
  }

  public void testMapToArray() {
    Map<String, String> map = new HashMap<String, String>();
    String[] actual = Utils.mapToArray(map);
    assertContains(actual, " ");

    map.put("foo", "foo");
    map.put("bar", "bar");
    map.put("zed", "zed");
    actual = Utils.mapToArray(map);
    assertContains(actual, "foo,foo", "bar,bar", "zed,zed");
  }

  public void testArrayToMap() {
    String[] s = new String[] { "foo,foo", "bar,bar", "zed,zed" };
    Map<String, String> actual = Utils.arrayToMap(s);
    assertContains(actual.keySet(), "foo", "bar", "zed");
    assertEquals("foo", actual.get("foo"));
    assertEquals("bar", actual.get("bar"));
    assertEquals("zed", actual.get("zed"));

    s = new String[] { "foo", "bar,bar", "zed,zed,zed" };
    actual = Utils.arrayToMap(s);
    assertContains(actual.keySet(), "bar");
    assertEquals(null, actual.get("foo"));
    assertEquals("bar", actual.get("bar"));
    assertEquals(null, actual.get("zed"));
  }

  public void testGetQueryInList() {
    List<String> list = new ArrayList<String>();
    String actual = Utils.propertyMatchAny("prop", list);
    assertEquals("", actual);

    list = Arrays.asList("foo");
    actual = Utils.propertyMatchAny("prop", list);
    assertEquals("(not(prop) or prop='' or prop='foo')", actual);

    list = Arrays.asList("foo", "bar");
    actual = Utils.propertyMatchAny("prop", list);
    assertEquals("(not(prop) or prop='' or prop='foo' or prop='bar')", actual);

    list = Arrays.asList("foo", "bar", "zed");
    actual = Utils.propertyMatchAny("prop", list);
    assertEquals("(not(prop) or prop='' or prop='foo' or prop='bar' or prop='zed')", actual);

  }

  public void testIsListContentItemList() {
    List<String> list = Arrays.asList(" ");
    List<String> list1 = null;
    boolean actual = Utils.isListContentItemList(list, list1);
    assertFalse(actual);

    list1 = Arrays.asList(" ");
    actual = Utils.isListContentItemList(list, list1);
    assertFalse(actual);

    list = Arrays.asList("bar", "zed");
    list1 = Arrays.asList("foo");
    actual = Utils.isListContentItemList(list, list1);
    assertFalse(actual);

    list = Arrays.asList("bar", "zed", "dog");
    list1 = Arrays.asList("foo", "dog");
    actual = Utils.isListContentItemList(list, list1);
    assertFalse(!actual);

  }

  public void testGetStringsInList() throws Exception {
    List<String> list = new ArrayList<String>();
    String[] actual = Utils.getStringsInList(list);
    assertEquals(0, actual.length);
    list.add("foo");
    list.add(" ");
    list.add("bar");
    actual = Utils.getStringsInList(list);
    assertNotContains(Arrays.asList(actual), " ");
    assertContains(actual, "foo", "bar");
  }

  public void testExtractSameItems() throws Exception {
    List<String> pList = Arrays.asList("foo", "bar", "zed");
    List<String> cList = Arrays.asList("foo", " ", "bar");
    List<String> actual = Utils.extractSameItems(pList, cList);
    assertContains(actual, "foo", "bar");

    // verify behaviour if first list is empty
    pList = new ArrayList<String>();
    cList = Arrays.asList("foo", " ", "bar");
    actual = Utils.extractSameItems(pList, cList);
    assertEmpty(actual);

    // verify behaviour if no common elements
    pList = Arrays.asList("foo", "bar", "zed");
    cList = Arrays.asList("foo*", "bar*", "zed*");
    actual = Utils.extractSameItems(pList, cList);
    assertEmpty(actual);

  }

  public void testValuesToArray() throws Exception {
    Value[] values = new Value[0];
    String[] actual = Utils.valuesToArray(values);
    assertEquals(0, actual.length);

    values = new Value[] { stubValue("foo") };
    actual = Utils.valuesToArray(values);
    assertContains(actual, "foo");
    assertEquals(1, actual.length);

    values = new Value[] { stubValue("foo"), stubValue("bar"), stubValue("zed") };
    actual = Utils.valuesToArray(values);
    assertContains(actual, "foo", "bar", "zed");
    assertEquals(3, actual.length);
  }

  public void testValuesToList() throws Exception {
    Value[] values = new Value[0];
    List<String> actual = Utils.valuesToList(values);
    assertEmpty(actual);

    values = new Value[] { stubValue("foo") };
    actual = Utils.valuesToList(values);
    assertContains(actual, "foo");
    assertEquals(1, actual.size());

    values = new Value[] { stubValue("foo"), stubValue("bar"), stubValue("zed") };
    actual = Utils.valuesToList(values);
    assertContains(actual, "foo", "bar", "zed");
    assertEquals(3, actual.size());
  }

  public void testArrayCopy() {

    // null in, null out
    String[] source = null;
    String[] actual = Utils.arrayCopy(source);
    assertNull(actual);

    // empty array
    source = new String[0];
    actual = Utils.arrayCopy(source);
    assertNotNull(actual);
    assertEquals(0, actual.length);

    source = new String[] { "foo", "bar", "zed" };
    actual = Utils.arrayCopy(source);
    assertEquals("copied arrays should have same size", source.length, actual.length);
    assertNotSame("a new array should have been created", source, actual);
    assertContains(actual, "foo", "bar", "zed"); // should contain all elements
  }

  public void testGetQueryByProperty() throws Exception {
    String actual = Utils.getQueryByProperty("", "", "").toString();
    String expected = "";
    assertEquals(expected, actual);
    
    actual = Utils.getQueryByProperty("and", "", "").toString();
    expected = "";
    assertEquals(expected, actual);

    actual = Utils.getQueryByProperty("", "exo:test", "").toString();
    expected = "";
    assertEquals(expected, actual);

    actual = Utils.getQueryByProperty("", "", "the value").toString();
    expected = "";
    assertEquals(expected, actual);

    actual = Utils.getQueryByProperty("", "exo:test", "the value").toString();
    expected = "(@exo:test='the value')";
    assertEquals(expected, actual);
    
    actual = Utils.getQueryByProperty("and", "", "the value").toString();
    expected = "";
    assertEquals(expected, actual);

    actual = Utils.getQueryByProperty("and", "exo:test", "").toString();
    expected = "";
    assertEquals(expected, actual);

    actual = Utils.getQueryByProperty("and", "exo:test", "the value").toString();
    expected = " and (@exo:test='the value')";
    assertEquals(expected, actual);

    actual = Utils.getQueryByProperty("or", "exo:test", "the value").toString();
    expected = " or (@exo:test='the value')";
    assertEquals(expected, actual);
  }
  
  public void testInsertBuilder() throws Exception {
    StringBuilder qr = Utils.getPathQuery("true", "", "", "");
    String strQuery = "exo:test='test value'";
    if(Utils.isEmpty(qr.toString())) {
      qr.append("[").append(strQuery).append("]");
    } else {
      qr.insert(qr.lastIndexOf("]"), " and (" + strQuery + ")");
    }
    assertEquals("[(@exo:isApproved='true') and (exo:test='test value')]", qr.toString());
    qr = new StringBuilder();
    if(Utils.isEmpty(qr.toString())) {
      qr.append("[(").append(strQuery).append(")]");
    } else {
      qr.insert(qr.lastIndexOf("]"), " and (" + strQuery + ")");
    }
    assertEquals("[(exo:test='test value')]", qr.toString());
  }
  
  public void testGetPathQuery() throws Exception {
    // test for value is empty and true or false.
    String actual = Utils.getPathQuery("", "", "", "").toString();
    String expected = "";
    assertEquals(expected, actual.trim());
    actual = Utils.getPathQuery("true", "", "", "").toString();
    expected = "[(@exo:isApproved='true')]";
    assertEquals(expected, actual);

    actual = Utils.getPathQuery("", "true", "", "").toString();
    expected = "[(@exo:isHidden='true')]";
    assertEquals(expected, actual);

    actual = Utils.getPathQuery("", "", "true", "").toString();
    expected = "[(@exo:isWaiting='true')]";
    assertEquals(expected, actual);

    actual = Utils.getPathQuery("", "", "", "User").toString();
    expected = "[((@exo:userPrivate='User') or (@exo:userPrivate='exoUserPri'))]";
    assertEquals(expected, actual);

    actual = Utils.getPathQuery("", "", "true", "User").toString();
    expected = "[((@exo:userPrivate='User') or (@exo:userPrivate='exoUserPri')) and (@exo:isWaiting='true')]";
    assertEquals(expected, actual);

    actual = Utils.getPathQuery("", "true", "", "User").toString();
    expected = "[((@exo:userPrivate='User') or (@exo:userPrivate='exoUserPri')) and (@exo:isHidden='true')]";
    assertEquals(expected, actual);

    actual = Utils.getPathQuery("true", "", "", "User").toString();
    expected = "[((@exo:userPrivate='User') or (@exo:userPrivate='exoUserPri')) and (@exo:isApproved='true')]";
    assertEquals(expected, actual);

    actual = Utils.getPathQuery("true", "true", "", "").toString();
    expected = "[(@exo:isApproved='true') and (@exo:isHidden='true')]";
    assertEquals(expected, actual);

    actual = Utils.getPathQuery("true", "", "true", "").toString();
    expected = "[(@exo:isApproved='true') and (@exo:isWaiting='true')]";
    assertEquals(expected, actual);

    actual = Utils.getPathQuery("true", "true", "true", "").toString();
    expected = "[(@exo:isApproved='true') and (@exo:isHidden='true') and (@exo:isWaiting='true')]";
    assertEquals(expected, actual);

    actual = Utils.getPathQuery("true", "true", "", "User").toString();
    expected = "[((@exo:userPrivate='User') or (@exo:userPrivate='exoUserPri')) and (@exo:isApproved='true') and (@exo:isHidden='true')]";
    assertEquals(expected, actual);

    actual = Utils.getPathQuery("true", "", "true", "User").toString();
    expected = "[((@exo:userPrivate='User') or (@exo:userPrivate='exoUserPri')) and (@exo:isApproved='true') and (@exo:isWaiting='true')]";
    assertEquals(expected, actual);
    
    actual = Utils.getPathQuery("", "true", "true", "User").toString();
    expected = "[((@exo:userPrivate='User') or (@exo:userPrivate='exoUserPri')) and (@exo:isHidden='true') and (@exo:isWaiting='true')]";
    assertEquals(expected, actual);
    
    actual = Utils.getPathQuery("true", "true", "true", "User").toString();
    expected = "[((@exo:userPrivate='User') or (@exo:userPrivate='exoUserPri')) and (@exo:isApproved='true') and (@exo:isHidden='true') and (@exo:isWaiting='true')]";
    assertEquals(expected, actual);
  }

  public void testHasPermission() {
    List<String> l1 = Arrays.asList(" ");
    List<String> l2 = null;

    boolean condition = Utils.hasPermission(l1, l2);
    assertFalse(condition);

    condition = Utils.hasPermission(l2, l1);
    assertFalse(condition);

    condition = Utils.hasPermission(l2, l2);
    assertFalse(condition);

    l1 = Arrays.asList("g1", "g2");
    l2 = Arrays.asList("g3");
    condition = Utils.hasPermission(l1, l2);
    assertFalse(condition);

    condition = Utils.hasPermission(l2, l1);
    assertFalse(condition);

    l1 = Arrays.asList("g1", "g2", "g3");
    l2 = Arrays.asList("g1", "g4");
    condition = Utils.hasPermission(l1, l2);
    assertTrue(condition);

    condition = Utils.hasPermission(l2, l1);
    assertTrue(condition);
  }
  
  public void testGetCategoryId() {
    assertEquals("forumCategorya77608b97f0001012e0f6c254a761b67",
                 Utils.getCategoryId("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya77608b97f0001012e0f6c254a761b67/foruma77608da7f0001017c1031ba8c6197c2"));
    assertEquals("forumCategorya779f0d57f00010108ac0b087a59082f",
                 Utils.getCategoryId("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya779f0d57f00010108ac0b087a59082f/foruma779f0ea7f00010162d38dbc5ae76721/topica779f2877f0001011393a0953f0d2e00"));
    assertEquals("forumCategorya77c1bd07f0001013c4096e6275324ea",
                 Utils.getCategoryId("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya77c1bd07f0001013c4096e6275324ea/foruma77c1be27f000101712b773a1be93fc9/topica77c1c017f0001012b4161eb9eaed484/posta77c1c237f00010145419cee3ac29fe5"));
  }

  public void testGetCategoryPath() {
    assertEquals("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya77608b97f0001012e0f6c254a761b67",
                 Utils.getCategoryPath("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya77608b97f0001012e0f6c254a761b67/foruma77608da7f0001017c1031ba8c6197c2"));
    assertEquals("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya779f0d57f00010108ac0b087a59082f",
                 Utils.getCategoryPath("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya779f0d57f00010108ac0b087a59082f/foruma779f0ea7f00010162d38dbc5ae76721/topica779f2877f0001011393a0953f0d2e00"));
    assertEquals("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya77c1bd07f0001013c4096e6275324ea",
                 Utils.getCategoryPath("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya77c1bd07f0001013c4096e6275324ea/foruma77c1be27f000101712b773a1be93fc9/topica77c1c017f0001012b4161eb9eaed484/posta77c1c237f00010145419cee3ac29fe5"));
  }

  public void testGetForumId() {
    assertEquals("foruma77608da7f0001017c1031ba8c6197c2",
                 Utils.getForumId("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya77608b97f0001012e0f6c254a761b67/foruma77608da7f0001017c1031ba8c6197c2"));
    assertEquals("foruma779f0ea7f00010162d38dbc5ae76721",
                 Utils.getForumId("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya779f0d57f00010108ac0b087a59082f/foruma779f0ea7f00010162d38dbc5ae76721/topica779f2877f0001011393a0953f0d2e00"));
    assertEquals("foruma77c1be27f000101712b773a1be93fc9",
                 Utils.getForumId("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya77c1bd07f0001013c4096e6275324ea/foruma77c1be27f000101712b773a1be93fc9/topica77c1c017f0001012b4161eb9eaed484/posta77c1c237f00010145419cee3ac29fe5"));
  }

  public void testGetForumPath() {
    assertEquals("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya77608b97f0001012e0f6c254a761b67/foruma77608da7f0001017c1031ba8c6197c2",
                 Utils.getForumPath("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya77608b97f0001012e0f6c254a761b67/foruma77608da7f0001017c1031ba8c6197c2"));
    assertEquals("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya779f0d57f00010108ac0b087a59082f/foruma779f0ea7f00010162d38dbc5ae76721",
                 Utils.getForumPath("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya779f0d57f00010108ac0b087a59082f/foruma779f0ea7f00010162d38dbc5ae76721/topica779f2877f0001011393a0953f0d2e00"));
    assertEquals("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya77c1bd07f0001013c4096e6275324ea/foruma77c1be27f000101712b773a1be93fc9",
                 Utils.getForumPath("/exo:applications/ForumService/ForumData/CategoryHome/forumCategorya77c1bd07f0001013c4096e6275324ea/foruma77c1be27f000101712b773a1be93fc9/topica77c1c017f0001012b4161eb9eaed484/posta77c1c237f00010145419cee3ac29fe5"));
  }
}
