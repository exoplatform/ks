/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.ks.common.webui;

import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormInputContainer;
import org.exoplatform.webui.form.validator.Validator;

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@exoplatform.com
 * Sep 14, 2006
 * 
 * Represents a multi value selector
 */
@ComponentConfig(
    events = { 
        @EventConfig(listeners = UIFormMultiValueInputSet.AddActionListener.class, phase = Phase.DECODE), 
        @EventConfig(listeners = UIFormMultiValueInputSet.RemoveActionListener.class, phase = Phase.DECODE) 
    }
)
@SuppressWarnings("unchecked")
public class UIFormMultiValueInputSet extends UIFormInputContainer<List> {
  protected Log log = ExoLogger.getLogger(this.getClass());
  /**
   * A list of validators
   */
  protected List<Validator>            validators_;

  /**
   * The type of items in the selector
   */
  private Class<? extends UIFormInput> clazz_;

  private Constructor                  constructor_         = null;

  private List<Integer>                listIndexItemRemoved = new ArrayList<Integer>();

  private int                          maxOld               = 0;

  /**
   * Whether this field is enabled
   */
  protected boolean                    enable_              = true;

  /**
   * Whether this field is in read only mode
   */
  protected boolean                    readonly_            = false;

  public UIFormMultiValueInputSet() throws Exception {
    super(null, null);
  }

  public UIFormMultiValueInputSet(String name, String bindingField) throws Exception {
    super(name, bindingField);
    setComponentConfig(getClass(), null);
  }

  public Class<List> getTypeValue() {
    return List.class;
  }
  
  public <E extends Validator> UIFormInput addValidator(Class<E> clazz, Object... params) throws Exception {
    if (validators_ == null)
      validators_ = new ArrayList<Validator>(3);
    if (params.length > 0) {
      Class<?>[] classes = new Class[params.length];
      for (int i = 0; i < params.length; i++) {
        classes[i] = params[i].getClass();
      }
      Constructor<E> constructor = clazz.getConstructor(classes);
      validators_.add(constructor.newInstance(params));
      return this;
    }
    validators_.add(clazz.newInstance());
    return this;
  }

  public void setType(Class<? extends UIFormInput> clazz) {
    this.clazz_ = clazz;
    Constructor[] constructors = clazz_.getConstructors();
    if (constructors.length > 0) {
      constructor_ = constructors[0];
      if (constructor_.getParameterTypes().length == 0)
        constructor_ = constructors[constructors.length - 1];
    }
  }

  public Class<? extends UIFormInput> getUIFormInputBase() {
    return clazz_;
  }

  /**
   * @return the selected items in the selector
   */
  public List<?> getValue() {
    List<Object> values = new ArrayList<Object>();
    for (UIComponent child : getChildren()) {
      UIFormInputBase uiInput = (UIFormInputBase) child;
      if (uiInput.getValue() == null)
        continue;
      values.add(uiInput.getValue());
    }
    return values;
  }

  public UIFormInput setValue(List<?> values) throws Exception {
    getChildren().clear();
    for (int i = 0; i < values.size(); i++) {
      UIFormInputBase uiInput = createUIFormInput(i);
      uiInput.setValue(values.get(i));
    }
    return this;
  }

  public boolean isEnable() {
    return enable_;
  }

  public UIFormMultiValueInputSet setEnable(boolean enable) {
    enable_ = enable;
    return this;
  }

  public boolean isEditable() {
    return !readonly_;
  }

  public UIFormMultiValueInputSet setEditable(boolean editable) {
    readonly_ = !editable;
    return this;
  }

  public void processDecode(WebuiRequestContext context) throws Exception {
    super.processDecode(context);
    UIForm uiForm = getAncestorOfType(UIForm.class);
    String action = uiForm.getSubmitAction();
    Event<UIComponent> event = createEvent(action, Event.Phase.DECODE, context);
    if (event == null)
      return;
    event.broadcast();
  }
  
  private String getResourceBundle(WebuiRequestContext context, String key, String dfValue) throws Exception {
    ResourceBundle res = context.getApplicationResourceBundle();
    UIPortletApplication app = getAncestorOfType(UIPortletApplication.class);
    try {
      dfValue = res.getString(app.getId() + key);
    } catch (Exception e) {
      log.warn("Can not find resource bundle for key : " + app.getId() + key);
    }
    return dfValue;
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    if (getChildren() == null || getChildren().size() < 1)
      createUIFormInput(0);

    Writer writer = context.getWriter();

    UIForm uiForm = getAncestorOfType(UIForm.class);
    int size = getChildren().size();
    String rmItem = getResourceBundle(context, ".label.RemoveItem", "Remove Item");
    String addItem = getResourceBundle(context, ".label.AddItem", "Add Item");
    String lbItem = uiForm.getLabel(getId());
    writer.append("<div class=\"UIFormMultiValueInputSet\" id=\"").append(getId()).append("\">");
    for (int i = 0; i < size; i++) {
      UIFormInputBase uiInput = getChild(i);
      writer.append("<div class=\"MultiValueContainer\">");

      uiInput.setReadOnly(readonly_);
      uiInput.setDisabled(!enable_);
      uiInput.setHTMLAttribute("title", lbItem + " " + (i+1));
      renderChild(uiInput.getId());

      if ((size >= 2) || ((size == 1) && (uiInput.getValue() != null))) {
        writer.append("<img onclick=\"");
        writer.append(uiForm.event("Remove", uiInput.getId())).append("\" title=\"").append(rmItem).append("\" alt=\"").append(rmItem).append("\"");
        writer.append(" class=\"MultiFieldAction Remove16x16Icon\" src=\"/eXoResources/skin/sharedImages/Blank.gif\" />");
      }
      if (i == size - 1) {

        writer.append("<img onclick=\"");
        writer.append(uiForm.event("Add", getId())).append("\" title=\"").append(addItem).append("\" alt=\"").append(addItem).append("\"");
        writer.append(" class=\"MultiFieldAction AddNewNodeIcon\" src=\"/eXoResources/skin/sharedImages/Blank.gif\" />");
      }
      writer.append("</div>");
    }
    writer.append("</div>");
  }

  public UIFormInputBase createUIFormInput(int idx) throws Exception {
    Class[] classes = constructor_.getParameterTypes();
    Object[] params = new Object[classes.length];
    for (int i = 0; i < classes.length; i++) {
      if (classes[i].isPrimitive()) {
        if (classes[i] == boolean.class)
          params[i] = false;
        else
          params[i] = 0;
      }
    }
    params[0] = getId() + String.valueOf(idx);
    UIFormInputBase inputBase = (UIFormInputBase) constructor_.newInstance(params);
    if (validators_ != null) {
      for (Validator validator : validators_) {
        inputBase.addValidator(validator.getClass());
      }
    }
    addChild(inputBase);
    return inputBase;
  }

  public void resetListIndexItemRemoved() {
    this.listIndexItemRemoved = new ArrayList<Integer>();
  }

  public List<Integer> getListIndexItemRemoved() {
    return listIndexItemRemoved;
  }

  public void setMaxOld(int maxOld) {
    this.maxOld = maxOld;
  }

  public int getMaxOld() {
    return maxOld;
  }

  static public class AddActionListener extends EventListener<UIFormMultiValueInputSet> {
    public void execute(Event<UIFormMultiValueInputSet> event) throws Exception {
      UIFormMultiValueInputSet uiSet = event.getSource();
      String id = event.getRequestContext().getRequestParameter(OBJECTID);
      if (uiSet.getId().equals(id)) {
        List<UIComponent> children = uiSet.getChildren();
        if (children.size() > 0) {
          UIFormInputBase uiInput = (UIFormInputBase) children.get(children.size() - 1);
          String index = uiInput.getId();
          int maxIndex = Integer.parseInt(index.replaceAll(id, ""));
          if (maxIndex < uiSet.maxOld) {
            maxIndex = uiSet.maxOld;
            while (uiSet.getChildById(id + String.valueOf(maxIndex)) != null) {
              maxIndex = maxIndex + 1;
            }
          }
          uiSet.createUIFormInput(maxIndex + 1);
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSet.getParent());
    }
  }

  static public class RemoveActionListener extends EventListener<UIFormMultiValueInputSet> {
    public void execute(Event<UIFormMultiValueInputSet> event) throws Exception {
      UIFormMultiValueInputSet uiSet = event.getSource();
      String id = event.getRequestContext().getRequestParameter(OBJECTID);
      uiSet.removeChildById(id);
      uiSet.listIndexItemRemoved.add(Integer.parseInt(id.replaceAll(uiSet.getId(), "")));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSet.getParent());
    }
  }

}
