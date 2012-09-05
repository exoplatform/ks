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
package org.exoplatform.wiki.rendering.macro.section;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.Block.Axes;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
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
@Component("section")
public class SectionMacro extends AbstractMacro<SectionMacroParameters> {
  private static final double TOTAL_WIDTH                = 99.900000000000006D;

  private static final double COLUMN_RIGHT_PADDING_RATE  = 1.5D;

  private static final String STYLE_TEXT_ALIGN_JUSTIFY   = "text-align:justify;";

  private static final String STYLE_CLEAR_BOTH           = "clear:both";

  private static final String PARAMETER_STYLE            = "style";

  private static final String COLUMN_RIGHT_PADDING_STYLE = "1.5%";

  private static final String DESCRIPTION                = "A macro to enclose columned text";

  private static final String MACRO_NAME                 = "Section";

  @Inject
  private ComponentManager    componentManager;

  public SectionMacro() {
    super(MACRO_NAME, DESCRIPTION, SectionMacroParameters.class);
  }

  public List<Block> execute(SectionMacroParameters parameters,
                             String content,
                             MacroTransformationContext context) throws MacroExecutionException {
    Parser parser = getSyntaxParser(context.getSyntax().toIdString());
    XDOM parsedDom;
    try {
      parsedDom = parser.parse(new StringReader(content));
    } catch (ParseException e) {
      throw new MacroExecutionException("Failed to parse content [" + content
          + "] with Syntax parser [" + parser.getSyntax() + "]", e);
    }

    List<MacroBlock> potentialColumns = parsedDom.getBlocks(new ClassBlockMatcher(MacroBlock.class), Axes.CHILD);

    int count = countColumns(potentialColumns);

    if (count == 0) {
      throw new MacroExecutionException("Section macro expect at least one column macro as first-level children");
    }

    double computedColumnWidth = (TOTAL_WIDTH - COLUMN_RIGHT_PADDING_RATE * (count - 1)) / count;

    makeColumns(potentialColumns, computedColumnWidth);

    Map<String, String> clearFloatsParams = new HashMap<String, String>();
    clearFloatsParams.put(PARAMETER_STYLE, STYLE_CLEAR_BOTH);

    parsedDom.addChild(new GroupBlock(clearFloatsParams));

    Map<String, String> sectionParameters = new HashMap<String, String>();
    if (parameters.isJustify()) {
      sectionParameters.put(PARAMETER_STYLE, STYLE_TEXT_ALIGN_JUSTIFY);
    }

    Block sectionRoot = new GroupBlock(sectionParameters);
    sectionRoot.addChildren(parsedDom.getChildren());

    return Collections.singletonList(sectionRoot);
  }

  private int countColumns(List<MacroBlock> blocks) {
    int result = 0;
    for (MacroBlock maybeColumn : blocks) {
      if (maybeColumn.getId().equals("column")) {
        result++;
      }
    }
    return result;
  }

  private void makeColumns(List<MacroBlock> blocks, double columnWidth) {
    Iterator<MacroBlock> it = blocks.iterator();
    while (it.hasNext()) {
      MacroBlock probablyColumn = (MacroBlock) it.next();
      if (probablyColumn.getId().equals("column")) {
        ColumnStyle style = new ColumnStyle();
        style.setWidth(columnWidth + "%");
        if (it.hasNext()) {
          style.setPaddingRight(COLUMN_RIGHT_PADDING_STYLE);
        }
        Map<String, String> params = Collections.singletonMap(PARAMETER_STYLE,
                                                              style.getStyleAsString());
        Block colParent = new GroupBlock(new HashMap<String, String>(params));
        colParent.addChild(probablyColumn.clone());
        probablyColumn.getParent().replaceChild(colParent, probablyColumn);
      }
    }
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
