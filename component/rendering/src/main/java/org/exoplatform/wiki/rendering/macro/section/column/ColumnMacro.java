/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.macro.section.column;

import java.io.StringReader;
import java.util.List;

import javax.inject.Inject;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 15 Mar 2011  
 */
@Component("column")
public class ColumnMacro<P extends ColumnMacroParameters> extends AbstractMacro<P> {

  @Inject
  private ComponentManager    componentManager;

  private static final String MACRO_NAME  = "Column";

  private static final String DESCRIPTION = "declares a column in a columned section";

  public ColumnMacro() {
    super(MACRO_NAME, DESCRIPTION, ColumnMacroParameters.class);
  }

  public List<Block> execute(P parameters, String content, MacroTransformationContext context) throws MacroExecutionException {
    Parser parser = getSyntaxParser(context.getSyntax().toIdString());
    XDOM parsedDom;
    try {
      parsedDom = parser.parse(new StringReader(content));
    } catch (ParseException e) {
      throw new MacroExecutionException("Failed to parse content [" + content
          + "] with Syntax parser [" + parser.getSyntax() + "]", e);
    }
    List<Block> result = parsedDom.getChildren();
    return result;
  }

  protected Parser getSyntaxParser(String syntaxId) throws MacroExecutionException {
    try {
      return (Parser) this.componentManager.getInstance(Parser.class, syntaxId);
    } catch (ComponentLookupException e) {
      throw new MacroExecutionException("Failed to find source parser", e);
    }
  }

  public boolean supportsInlineMode() {
    return false;
  }

  public int getPriority() {
    return 750;
  }
}
