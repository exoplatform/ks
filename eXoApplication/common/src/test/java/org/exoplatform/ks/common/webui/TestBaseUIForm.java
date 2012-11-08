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
package org.exoplatform.ks.common.webui;

import org.exoplatform.ks.common.base.AbstractUIComponentTestCase;



/**
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestBaseUIForm extends AbstractUIComponentTestCase<BaseUIForm> {

  
  public TestBaseUIForm() throws Exception {
    super();
  }
  
  public void testGenerateComponentId() {
     assertEquals("FakeForm", component.generateComponentId(UIFakeForm.class));
  }

  public void testI18n() {
    assertEquals("DoesNotExist", component.i18n("DoesNotExist"));
    assertEquals("Does.not.exist", component.i18n("Does.not.exist"));
    setResourceBundleEntry("key","value");
    assertEquals("value", component.i18n("key"));
  }

  public void testOpenPopupOverPopup() throws Exception {
    
    // fixture
    new SamplePortlet(component);
    SamplePortlet parent = component.getAncestorOfType(SamplePortlet.class) ;   
    
    // first open a popup with first form
    SampleForm form = component.openPopup(parent, SampleForm.class, "UIAddPostContainer", 900, 460);

    // then  attempt to open a child popup on top with legacy method
    UIPopupContainer popupContainer = form.getAncestorOfType(UIPopupContainer.class) ;
    SamplePopupAction popupAction = popupContainer.getChild(SamplePopupAction.class);
    assertNotNull("UIPopupAction not found", popupAction);
    
  }
  
  
  public void testGetLabel() {

    assertEquals("NonExisting", component.getLabel("NonExisting"));
    
    setResourceBundleEntry("SampleForm.label.Label","value");
    assertEquals("value", component.getLabel("Label"));
    
    setResourceBundleEntry("SampleForm.label.Key", "whatever");
    assertEquals(component.getLabel("Key"), component.i18n("SampleForm.label.Key"));
  }

  @Override
  protected BaseUIForm createComponent() {
    BaseUIForm sampleForm = new BaseUIForm();
    sampleForm.setId("SampleForm");  
    return sampleForm;
  }

}
