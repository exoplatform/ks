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
package org.exoplatform.forum.webui.popup;

import org.exoplatform.forum.base.AbstractUIComponentTestCase;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestUIBanIPForumManagerForm extends AbstractUIComponentTestCase<UIBanIPForumManagerForm> {

  public TestUIBanIPForumManagerForm() throws Exception {
    super();    
  }
  
  public void testCheckIpAddress() throws Exception {
    assertNull(component.checkIpAddress(new String []{"aaa","bbb","ccc","ddd"}));
    assertNull(component.checkIpAddress(new String []{"255","255","255","255"}));
    assertNull(component.checkIpAddress(new String []{"0","0","0","0"}));
    assertNull(component.checkIpAddress(new String []{"192","168","0"}));
    assertEquals("192.168.0.13", component.checkIpAddress(new String []{"192","168","0","13"}));
  }

  @Override
  protected UIBanIPForumManagerForm createComponent() throws Exception {
    return new UIBanIPForumManagerForm();
  }

}
