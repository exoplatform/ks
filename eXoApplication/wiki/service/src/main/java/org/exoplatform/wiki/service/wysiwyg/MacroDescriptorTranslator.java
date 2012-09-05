/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.service.wysiwyg;

import org.xwiki.component.annotation.Role;
import org.xwiki.gwt.wysiwyg.client.plugin.macro.MacroDescriptor;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 18, 2010  
 */
/**
 * Translates a {@link MacroDescriptor} into the execution context language.
 */
@Role
public interface MacroDescriptorTranslator {
  /**
   * Translates the given macro descriptor into the execution context language.
   * 
   * @param macroDescriptor a macro descriptor
   * @return the given macro descriptor translated into the execution context language
   */
  MacroDescriptor translate(MacroDescriptor macroDescriptor);

}
