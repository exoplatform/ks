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

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.Block.Axes;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.ParserUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Jul 15, 2011  
 */
@Component("confluence/1.0")
public class ConfluenceBlockConverter implements BlockConverter {
  
  /**
   * Used to get the current syntax parser.
   */
  @Inject
  private ComponentManager componentManager;

  @Override
  public void convert(XDOM xdom) throws ConversionException {
    List<MacroBlock> blocks = xdom.getBlocks(new ClassBlockMatcher(MacroBlock.class),Axes.DESCENDANT);
    for (MacroBlock block : blocks) {
      transformDivMacro(block);
      transformSpanMacro(block);
    }
  }
  
  private void transformSpanMacro(MacroBlock block) throws ConversionException {
    if (block.getId().equals("span")) {
      Block parent = block.getParent();
      Map<String, String> params = block.getParameters();
      String content = block.getContent().replaceAll("\\{span\\}", StringUtils.EMPTY);
      XDOM xdom = parse(content);      
      (new ParserUtils()).removeTopLevelParagraph((xdom.getChildren()));
      FormatBlock newBlock = new FormatBlock(xdom.getChildren(), Format.NONE, params);
      parent.replaceChild(newBlock, block);
    }
  }

  private void transformDivMacro(MacroBlock block) throws ConversionException{
    if (block.getId().equals("div")) {
      Block parent = block.getParent();
      Map<String, String> params = block.getParameters();
      XDOM xdom = parse(block.getContent());
      List<MacroBlock> children = xdom.getBlocks(new ClassBlockMatcher(MacroBlock.class),Axes.DESCENDANT);

      for (MacroBlock child : children) {
        transformSpanMacro(child);
      }
      (new ParserUtils()).removeTopLevelParagraph(xdom.getChildren());
      ParagraphBlock newBlock = new ParagraphBlock(xdom.getChildren(), params);
      parent.replaceChild(newBlock, block);
    }
  }
  
  private XDOM parse(String content) throws ConversionException {
    try {
      Parser parser = componentManager.getInstance(Parser.class, Syntax.CONFLUENCE_1_0.toIdString());
      return parser.parse(new StringReader(content));
    } catch (ComponentLookupException e) {
      throw new ConversionException("Failed to locate Parser for syntax [Confluence 1.0]", e);
    } catch (ParseException e) {
      throw new ConversionException("Failed to parse input source", e);
    }
  }
  
}
