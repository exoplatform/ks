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
package org.exoplatform.faq.service;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tuvd@exoplatform.com
 * Aug 1, 2012  
 */
public class TestUtils extends TestCase{
  
  public TestUtils() {
  }
  
  public void testGetQueryListOfUser() {
    // the property and listOfUser always not null.
    String property = "exo:foo";
    List<String> listOfUser = new ArrayList<String>();
    String actual = Utils.buildQueryListOfUser(property, listOfUser);
    String expected = "";
    assertEquals(expected, actual);
    listOfUser.add("demo");
    actual = Utils.buildQueryListOfUser(property, listOfUser);
    expected = "@exo:foo = 'demo'";
    assertEquals(expected, actual);
    listOfUser.add("/foo/bar");
    actual = Utils.buildQueryListOfUser(property, listOfUser);
    expected = "@exo:foo = 'demo' or @exo:foo = '/foo/bar' or @exo:foo = '*:/foo/bar'";
    listOfUser.add("member:/zed/bar");
    actual = Utils.buildQueryListOfUser(property, listOfUser);
    expected = "@exo:foo = 'demo' or @exo:foo = '/foo/bar' or @exo:foo = '*:/foo/bar'" +
    		       " or @exo:foo = 'member:/zed/bar' or @exo:foo = '*:/zed/bar'";
    assertEquals(expected, actual);
  }

}
