/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.TransformationContext;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * Feb 27, 2012
 */
public class ConfluenceToXWiki2Transformer {

  private static Log logger = ExoLogger.getExoLogger(ConfluenceToXWiki2Transformer.class);

  public static String transformContent(String content, ComponentManager componentManager) {
    Syntax sourceSyntax = Syntax.CONFLUENCE_1_0;
    Syntax targetSyntax = Syntax.XWIKI_2_0;
    
    if (componentManager == null) {
      componentManager = new EmbeddableComponentManager();
    }
    
    try {
      Parser parser = componentManager.getInstance(Parser.class, sourceSyntax.toIdString());
      XDOM xdom = parser.parse(new StringReader(content));
      WikiPrinter printer = convert(xdom, sourceSyntax, targetSyntax, componentManager);
      
      return printer.toString();
    } catch (ComponentLookupException e) {
      logger.warn("Transformation failure: ", e);
    } catch (ConversionException e) {
      logger.warn("Transformation failure: ", e);
    } catch (Exception e) {
      logger.warn("Transformation failure: ", e);
    }
    return content;
  }
  
  private static WikiPrinter convert(XDOM xdom, Syntax sourceSyntax, Syntax targetSyntax, ComponentManager componentManager) throws Exception {
    ConfluenceToXWiki2DeepTransformation transformation = ConfluenceToXWiki2DeepTransformation.getInstance();
    transformation.setComponentManager(componentManager);
    transformation.transform(xdom, new TransformationContext(xdom, sourceSyntax));
    
    WikiPrinter printer = new DefaultWikiPrinter();
    BlockRenderer renderer;
    try {
      renderer = componentManager.getInstance(BlockRenderer.class, targetSyntax.toIdString());
    } catch (ComponentLookupException e) {
      throw new ConversionException("Failed to locate Renderer for syntax [" + targetSyntax + "]", e);
    }

    renderer.render(xdom, printer);
    return printer;
  }
}
