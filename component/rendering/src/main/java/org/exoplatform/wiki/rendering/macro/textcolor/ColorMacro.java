package org.exoplatform.wiki.rendering.macro.textcolor;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.util.ParserUtils;

@Component("color")
public class ColorMacro extends AbstractMacro<ColorMacroParameters> {
  
  /**
   * Used to get the current syntax parser.
   */
  @Inject
  private ComponentManager componentManager;
  
  /**
   * The description of the macro.
   */
  private static final String DESCRIPTION = "Decorate text with color";
  
  public ColorMacro() {
    super("Color", DESCRIPTION, new DefaultContentDescriptor(), ColorMacroParameters.class);
    setDefaultCategory(DEFAULT_CATEGORY_FORMATTING);
  }

  @Override
  public List<Block> execute(ColorMacroParameters parameters,
                             String content,
                             MacroTransformationContext context) throws MacroExecutionException {
    XDOM parsedDom;
    String color = parameters.getName();
    Parser parser = getSyntaxParser(context.getSyntax().toIdString());
    try {
      // parse the content of the wiki macro that has been injected by the component manager the content of the macro call itself is ignored.
      parsedDom = parser.parse(new StringReader(content));
    } catch (ParseException e) {
      throw new MacroExecutionException("Failed to parse content [" + content + "] with Syntax parser [" + parser.getSyntax() + "]", e);
    }
    
    Map<String, String> params = new HashMap<String, String>();
    params.put("style", "color:" + color + ";");
    List<Block> children = new ArrayList<Block>();
    children.addAll(parsedDom.getChildren());
    (new ParserUtils()).removeTopLevelParagraph(children);
    Block spanBlock = new FormatBlock(children, Format.NONE, params);
    return Collections.singletonList(spanBlock);
  }

  @Override
  public boolean supportsInlineMode() {
    return true;
  }
  
  protected Parser getSyntaxParser(String syntaxId) throws MacroExecutionException {
    try {
      return (Parser) componentManager.getInstance(Parser.class, syntaxId);
    } catch (ComponentLookupException e) {
      throw new MacroExecutionException("Failed to find source parser", e);
    }
  }
  
}
