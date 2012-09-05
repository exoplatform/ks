/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.converter;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.converter.ConversionException;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Jul 15, 2011  
 */
@Role
public interface BlockConverter {

  /**
   * Convert macro blocks to syntax-unsupported blocks(eg: Format block)
   * @param xdom xdom block to refine
   */
  public void convert(XDOM xdom) throws ConversionException;

}
