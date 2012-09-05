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
package org.exoplatform.wiki.rendering.macro.table;


import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.block.TableRowBlock;
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
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Sep 21, 2010  
 */
@Component("table")
public class TableMacro extends AbstractMacro<Object> {

  /**
   * The description of the macro.
   */
  private static final String DESCRIPTION = "Inserts a table.";

  @Inject
  private ComponentManager componentManager;

  /**
   * Create and initialize the descriptor of the macro.
   */
  public TableMacro() {
    super("Table", DESCRIPTION);
    setDefaultCategory(DEFAULT_CATEGORY_FORMATTING);
  }

  @Override
  public List<Block> execute(Object parameters, String content, MacroTransformationContext context) throws MacroExecutionException {
    XDOM parsedDom;
    // get a parser for the desired syntax identifier
    Parser parser = getSyntaxParser(context.getSyntax().toIdString());

    try {
      // parse the content of the wiki macro that has been injected by the component manager the content of the macro call itself is ignored.
      parsedDom = parser.parse(new StringReader(content));
    } catch (ParseException e) {
      throw new MacroExecutionException("Failed to parse content [" + content + "] with Syntax parser [" + parser.getSyntax() + "]", e);
    }

    List<MacroBlock> potentialRows = parsedDom.getBlocks(new ClassBlockMatcher(MacroBlock.class), Axes.CHILD);
    int count = this.countRows(potentialRows);
    if (count == 0) {
      throw new MacroExecutionException("Table macro expect at least one row macro as first-level children");
    }

    // Make the actual rows, injecting <tr> tags around rows macros
    this.makeRows(potentialRows);

    Map<String, String> tableParameters = new LinkedHashMap<String, String>();
    Block tableRoot = new TableBlock(parsedDom.getChildren(), tableParameters);

    return Collections.singletonList(tableRoot);
  }

  @Override
  public boolean supportsInlineMode() {
    return true;
  }
  
  protected Parser getSyntaxParser(String syntaxId) throws MacroExecutionException {
    try {
      return (Parser) this.componentManager.getInstance(Parser.class, syntaxId);
    } catch (ComponentLookupException e) {
      throw new MacroExecutionException("Failed to find source parser", e);
    }
  }

  private int countRows(List<MacroBlock> blocks) {
    int result = 0;
    for (MacroBlock maybeRow : blocks) {
      if (maybeRow.getId().equals("table-row") || maybeRow.getId().equals("thead")
          || maybeRow.getId().equals("tr")) {
        result++;
      }
    }
    return result;
  }

  private void makeRows(List<MacroBlock> blocks) {
    Iterator<MacroBlock> it = blocks.iterator();
    while (it.hasNext()) {
      MacroBlock probablyRow = it.next();
      if (probablyRow.getId().equals("table-row") || probablyRow.getId().equals("thead")
          || probablyRow.getId().equals("tr")) {
        Map<String, String> params = new LinkedHashMap<String, String>();
        Block rowBlock = new TableRowBlock(Collections.singletonList(probablyRow.clone()), params);
        probablyRow.getParent().replaceChild(rowBlock, probablyRow);
      }
    }

  }

}
