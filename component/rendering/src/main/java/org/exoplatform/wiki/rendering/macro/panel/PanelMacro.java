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
package org.exoplatform.wiki.rendering.macro.panel;

import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.util.ParserUtils;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 26, 2010  
 */
@Component("panel")
public class PanelMacro extends AbstractMacro<PanelMacroParameters> {

  /**
   * The description of the macro.
   */
  private static final String DESCRIPTION = "Embraces a block of text within a fully customizable panel";

  /**
   * The description of the macro content.
   */
  private static final String CONTENT_DESCRIPTION = "The content to put in the panel";
  
  private static final String PANEL_BLOCK = "panel";
  
  private static final String PANEL_HEADER_BLOCK = "panelHeader";
  
  private static final String PANEL_CONTENT_BLOCK = "panelContent";

  /**
   * Used to get the current syntax parser.
   */
  @Inject
  private ComponentManager componentManager;
  
  /**
   * Create and initialize the descriptor of the macro.
   */
  public PanelMacro() {
    super("Panel", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION), PanelMacroParameters.class);
    setDefaultCategory(DEFAULT_CATEGORY_FORMATTING);
  }

  @Override
  public List<Block> execute(PanelMacroParameters parameters, String content, MacroTransformationContext context) throws MacroExecutionException {
    if (content != null) {
      Map<String, String> panelParameters = new LinkedHashMap<String, String>();
      panelParameters.put("class", PANEL_BLOCK);
      Block panelBlock = new GroupBlock(panelParameters);

      // we add the title, if there is one
      String titleParameter = parameters.getTitle();
      if (!StringUtils.isEmpty(titleParameter)) {
        Map<String, String> panelHeaderParameters = new LinkedHashMap<String, String>();
        panelHeaderParameters.put("class", PANEL_HEADER_BLOCK);
        Block panelHeaderBlock = new GroupBlock(panelHeaderParameters);
        Parser parser = getSyntaxParser(context);
        List<Block> titleBlocks = parseTitle(parser, titleParameter);
        panelHeaderBlock.addChildren(titleBlocks);

        panelBlock.addChild(panelHeaderBlock);
      }
      Map<String, String> panelContentParameters = new LinkedHashMap<String, String>();
      panelContentParameters.put("class", PANEL_CONTENT_BLOCK);
      Block panelContentBlock = new GroupBlock(panelContentParameters);
      List<Block> contentBlocks = parseContent(parameters, content, context);
      panelContentBlock.addChildren(contentBlocks);

      panelBlock.addChild(panelContentBlock);

      return Collections.singletonList(panelBlock);
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * @return the component manager.
   */
  public ComponentManager getComponentManager() {
    return this.componentManager;
  }

  @Override
  public boolean supportsInlineMode() {
    return true;
  }
  
  /**
   * Renders the panel's title (which can contain content in the current syntax).
   * 
   * @param parser the appropriate syntax parser
   * @param titleParameter the title which is going to be parsed
   * @return the parsing result
   * @throws MacroExecutionException if the parsing fails
   */
  protected List<Block> parseTitle(Parser parser, String titleParameter) throws MacroExecutionException {
    try {
      List<Block> titleBlocks = parser.parse(new StringReader(titleParameter)).getChildren();

      // we try to simplify a bit the generated XDOM tree
      if (titleBlocks.size() == 1 && titleBlocks.get(0) instanceof ParagraphBlock) {
        List<Block> children = titleBlocks.get(0).getChildren();
        if (children.size() > 0) {
          titleBlocks = children;
        }
      }

      return titleBlocks;
    } catch (ParseException e) {
      throw new MacroExecutionException("Failed to parse the box's title [" + titleParameter + "]",
                                        e);
    }
  }
  
  /**
   * Renders the panel's content (which can contain content in the current syntax).
   * @param parameters
   * @param content
   * @param context
   * @return
   * @throws MacroExecutionException
   */
  protected List<Block> parseContent(PanelMacroParameters parameters,
                                     String content,
                                     MacroTransformationContext context) throws MacroExecutionException {
    return parseSourceSyntax(content, context);
  }

  /**
   * Parse provided content with the parser of the current wiki syntax.
   * 
   * @param content the content to parse.
   * @param context the context of the macro transformation.
   * @return an XDOM containing the parser content.
   * @throws MacroExecutionException failed to parse content
   */
  protected List<Block> parseSourceSyntax(String content, MacroTransformationContext context) throws MacroExecutionException {
    Parser parser = getSyntaxParser(context);

    try {
      List<Block> blocks = parser.parse(new StringReader(content)).getChildren();
      if (context.isInline()) {
        ParserUtils parseUtils = new ParserUtils();
        parseUtils.removeTopLevelParagraph(blocks);
      }

      return blocks;
    } catch (ParseException e) {
      throw new MacroExecutionException("Failed to parse content [" + content
          + "] with Syntax parser [" + parser.getSyntax() + "]", e);
    }
  }
  
  
  /**
   * Get the parser for the current syntax.
   * 
   * @param context the context of the macro transformation (from which to get the current syntax)
   * @return the parser for the current syntax
   * @throws MacroExecutionException Failed to find source parser.
   */
  protected Parser getSyntaxParser(MacroTransformationContext context) throws MacroExecutionException {
    try {
      return getComponentManager().getInstance(Parser.class, context.getSyntax().toIdString());
    } catch (ComponentLookupException e) {
      throw new MacroExecutionException("Failed to find source parser for syntax ["
          + context.getSyntax() + "]", e);
    }
  }
  
}
