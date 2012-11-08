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
package org.exoplatform.ks.rendering.base;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/**
 * Created by The eXo Platform SAS
 * Author : quangpld
 *          quangpld@exoplatform.com
 * Nov 6, 2012  
 */
public class AssertUtils {

  public static void assertException(Class<? extends Exception> exceptionType, Closure code) {
    try {
      code.dothis();
    } catch (Exception e) {
      Assert.assertEquals("Wrong exception type", exceptionType, e.getClass());
      return;
    }
    throw new AssertionFailedError("An exception should have been thrown.");
  }
  
  public static void assertException(Closure code) {
    try {
      code.dothis();
    } catch (Exception e) {
      return;
    }
    throw new AssertionFailedError("An exception should have been thrown.");
  }
}
