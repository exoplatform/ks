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
package org.exoplatform.wiki.rendering.macro.code;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.box.AbstractBoxMacro;
import org.xwiki.rendering.macro.code.CodeMacroParameters;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.parser.HighlightParser;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Created by The eXo Platform SAS Author : Lai Trung Hieu
 * hieu.lai@exoplatform.com 4 Mar 2011
 */
@Component("code")
public class CodeMacro extends AbstractBoxMacro<CodeMacroParameters> {
  @Inject
  private ComponentManager componentManager;
  
  /**
   * The logger to log.
   */
  @Inject
  private Logger logger;

  /**
   * @return the component manager.
   */
  public ComponentManager getComponentManager() {
    return this.componentManager;
  }

  /**
   * The description of the macro.
   */
  private static final String DESCRIPTION         = "Highlights code snippets of various programming languages";

  /**
   * Used to indicate that content should not be highlighted.
   */
  private static final String LANGUAGE_NONE       = "none";

  /**
   * The description of the macro content.
   */
  private static final String CONTENT_DESCRIPTION = "the content to highlight";

  /**
   * Used to parse content when language="none".
   */
  @Inject
  @Named("plain/1.0")
  private Parser              plainTextParser;

  /**
   * Create and initialize the descriptor of the macro.
   */
  public CodeMacro() {
    super("Code",
          DESCRIPTION,
          new DefaultContentDescriptor(CONTENT_DESCRIPTION),
          CodeMacroParameters.class);
    setDefaultCategory(DEFAULT_CATEGORY_FORMATTING);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.internal.macro.box.DefaultBoxMacro#parseContent(org.xwiki.rendering.macro.box.BoxMacroParameters,
   *      java.lang.String,
   *      org.xwiki.rendering.transformation.MacroTransformationContext)
   */
  @Override
  protected List<Block> parseContent(CodeMacroParameters parameters,
                                     String content,
                                     MacroTransformationContext context) throws MacroExecutionException {
    if (!LANGUAGE_NONE.equalsIgnoreCase(parameters.getLanguage())) {
      try {
        List<Block> result;
        result = highlight(parameters, content);
        return result;
      } catch (Exception e) {
        if (logger.isDebugEnabled()) {
          logger.debug("Can not highlight code", e);
        }
      }
    }
    return parsePlainTextContent(content);
  }

  /**
   * Return a highlighted version of the provided content.
   * 
   * @param parameters the code macro parameters.
   * @param content the content to highlight.
   * @return the highlighted version of the provided content.
   * @throws ParseException the highlight parser failed.
   * @throws ComponentLookupException failed to find highlight parser for
   *           provided language.
   */
  protected List<Block> highlight(CodeMacroParameters parameters, String content) throws ParseException,
                                                                                 ComponentLookupException {
    HighlightParser parser = null;
    parser = getComponentManager().getInstance(HighlightParser.class);
    return parser.highlight(parameters.getLanguage(), new StringReader(content));
  }
  
  private List<Block> parsePlainTextContent(String content) {
    List<Block> result;
    if (StringUtils.isEmpty(content)) {
      result = Collections.emptyList();
    } else {
      try {
        result = this.plainTextParser.parse(new StringReader(content))
                                     .getChildren()
                                     .get(0)
                                     .getChildren();
      } catch (Exception e) {
        result = new ArrayList<Block>();
        content = content.replaceAll("\n", "<br/>");
        result.add(new GroupBlock(Arrays.<Block> asList(new WordBlock(content)), new LinkedHashMap<String, String>()));
      }
    }
    return result;
  }

  @Override
  public boolean supportsInlineMode() {
    return false;
  }
  
}
