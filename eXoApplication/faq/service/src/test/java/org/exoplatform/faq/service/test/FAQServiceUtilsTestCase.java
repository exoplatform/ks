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
package org.exoplatform.faq.service.test;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.faq.base.FAQServiceBaseTestCase;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.ks.common.jcr.SessionManager;

/**
 * Created by The eXo Platform SAS
 * Author : quangpld
 *          quangpld@exoplatform.com
 * Oct 5, 2012  
 */
public class FAQServiceUtilsTestCase extends FAQServiceBaseTestCase {

  public FAQServiceUtilsTestCase() throws Exception {
    super();
  }
  
  public void setUp() throws Exception {
    super.setUp();
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testIsGroupExpression() throws Exception {
    assertTrue(FAQServiceUtils.isGroupExpression("/element(*,exoAnswer)"));
    assertFalse(FAQServiceUtils.isGroupExpression("/element(*,exo:answer)"));
  }
  
  public void testIsMembershipExpression() throws Exception {
    assertTrue(FAQServiceUtils.isMembershipExpression("/element(*,exo:answer)"));
    assertFalse(FAQServiceUtils.isMembershipExpression("/element(*,exoanswer)"));
    assertFalse(FAQServiceUtils.isMembershipExpression("element(*,exo:answer)"));
  }
  
  public void testGetUserPermission() throws Exception {
    List<String> list = new ArrayList<String>();
    list = FAQServiceUtils.getUserPermission(list.toArray(new String[] {}));
    assertEquals(0, list.size());
  }
  
  public void testGetSessionManager() throws Exception {
    SessionManager sessionManager = FAQServiceUtils.getSessionManager();
    assertNotNull(sessionManager);
    assertTrue(sessionManager.openSession().isLive());
  }
}
