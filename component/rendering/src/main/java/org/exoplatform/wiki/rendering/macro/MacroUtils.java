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
package org.exoplatform.wiki.rendering.macro;

import java.io.StringReader;
import java.util.List;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.parameter.MacroParameterException;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.util.ParserUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 12 Jan 2011  
 */
public class MacroUtils {
  /**
   * Parse provided content with the parser of the current wiki syntax.
   * 
   * @param content the content to parse.
   * @param context the context of the macro transformation.
   * @return an XDOM containing the parser content.
   * @throws MacroExecutionException failed to parse content
   */
  public static List<Block> parseSourceSyntax(ComponentManager componentManager,
                                              String content,
                                              MacroTransformationContext context) throws MacroExecutionException {
    Parser parser = getSyntaxParser(componentManager, context);
    try {
      List<Block> blocks = parser.parse(new StringReader(content)).getChildren();

      if (context.isInline()) {
        ParserUtils parseUtils = new ParserUtils();
        parseUtils.removeTopLevelParagraph(blocks);
      }

      if (blocks.size() == 1 && blocks.get(0) instanceof ParagraphBlock) {
        List<Block> children = blocks.get(0).getChildren();
        if (children.size() > 0) {
          blocks = children;
        }
      }

      return blocks;
    } catch (ParseException e) {
      throw new MacroExecutionException("Failed to parse content [" + content
          + "] with Syntax parser [" + parser.getSyntax() + "]", e);
    }
  }
  
  public static void validateNumberParam(String param) throws MacroExecutionException {
    if (param.length() > 0) {
      try {
        if (Integer.valueOf(param) < 0) {
          throw new MacroExecutionException("The value is too low. The lowest allowed value is 0");
        }
      } catch (NumberFormatException e) {
        throw new MacroParameterException("Parameter is not a number");
      }
    }
  }

  /**
   * Get the parser for the current syntax.
   * 
   * @param componentManager manager of all services
   * @param context the context of the macro transformation (from which to get the current syntax)
   * @return the parser for the current syntax
   * @throws MacroExecutionException Failed to find source parser.
   */
  private static Parser getSyntaxParser(ComponentManager componentManager, MacroTransformationContext context) throws MacroExecutionException {
    try {
      return componentManager.getInstance(Parser.class, context.getSyntax().toIdString());
    } catch (ComponentLookupException e) {
      throw new MacroExecutionException("Failed to find source parser for syntax ["
          + context.getSyntax() + "]", e);
    }
  }
}
