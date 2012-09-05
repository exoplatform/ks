/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.impl;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.converter.BlockConverter;
import org.picocontainer.Startable;
import org.w3c.dom.Document;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.context.Execution;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.SectionBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.Block.Axes;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.TransformationException;
import org.xwiki.rendering.transformation.TransformationManager;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLUtils;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */
public class RenderingServiceImpl implements RenderingService, Startable {
  
  private String cssURL; 

  private Log LOG = ExoLogger.getExoLogger(RenderingServiceImpl.class);
  
  private EmbeddableComponentManager componentManager = null;

  public Execution getExecution() throws ComponentLookupException, ComponentRepositoryException{
    return componentManager.getInstance(Execution.class);
  }
  
  public ComponentManager getComponentManager() {
    return componentManager;
  }

  public <T> T getComponent(Type clazz) {
    T component = this.<T>getComponent(clazz, "default");
    return component;
  }
  
  /*
   * (non-Javadoc)
   * @see org.exoplatform.wiki.rendering.RenderingService#render(java.lang.String, java.lang.String, java.lang.String)
   */
  public String render(String markup, String sourceSyntax, String targetSyntax, boolean supportSectionEdit) throws Exception {

    XDOM xdom = parse(markup, sourceSyntax);
    Syntax sSyntax = (sourceSyntax == null) ? Syntax.XWIKI_2_0 : getSyntax(sourceSyntax);
    Syntax tSyntax = (targetSyntax == null) ? Syntax.XHTML_1_0 : getSyntax(targetSyntax);
    
    
    try {
      BlockConverter refiner = componentManager.getInstance(BlockConverter.class, sSyntax.toIdString());
      refiner.convert(xdom);
    } catch (ComponentLookupException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(String.format("Syntax %s doesn't have any refiner", sSyntax));
      }
    } catch (ConversionException e) {
      throw new ConversionException("Failed to refine input source", e);
    }

    WikiPrinter printer = convert(xdom, sSyntax, tSyntax, supportSectionEdit);
    return printer.toString();
  }
  
  public String getContentOfSection(String markup, String sourceSyntax, String sectionIndex) throws Exception {

    XDOM xdom = parse(markup, sourceSyntax);
    Syntax sSyntax = (sourceSyntax == null) ? Syntax.XWIKI_2_0 : getSyntax(sourceSyntax);

    List<HeaderBlock> headers = getFilteredHeaders(xdom);
    int index = Integer.parseInt(sectionIndex);
    String content = null;
    if (headers.size() >= index) {
      SectionBlock section = headers.get(index - 1).getSection();
      content = renderXDOM(new XDOM(Collections.<Block> singletonList(section)), sSyntax);
    }
    return content;
  }

  public String updateContentOfSection(String markup, String sourceSyntax, String sectionIndex, String newSectionContent) throws Exception {

    XDOM xdom = parse(markup, sourceSyntax);
    Syntax sSyntax = (sourceSyntax == null) ? Syntax.XWIKI_2_0 : getSyntax(sourceSyntax);

    List<HeaderBlock> headers = getFilteredHeaders(xdom);
    int index = Integer.parseInt(sectionIndex);
    String content = null;
    if (headers.size() >= index) {
      HeaderBlock header = headers.get(index - 1);
      List<Block> blocks = parse(newSectionContent, sourceSyntax).getChildren();
      int sectionLevel = header.getLevel().getAsInt();
      for (int level = 1; level < sectionLevel && blocks.size() == 1
          && blocks.get(0) instanceof SectionBlock; ++level) {
        blocks = blocks.get(0).getChildren();
      }
      // replace old current SectionBlock with new Blocks
      Block section = header.getSection();
      section.getParent().replaceChild(blocks, section);
      // render back XDOM to document's content syntax
      content = renderXDOM(xdom, sSyntax);
    }
    return content;
  }

  private String clean(String dirtyHTML)
  {
    HTMLCleaner cleaner = getComponent(HTMLCleaner.class);
    HTMLCleanerConfiguration config = cleaner.getDefaultConfiguration();
    Document document = cleaner.clean(new StringReader(dirtyHTML), config);
    return HTMLUtils.toString(document);
  }
  
  @Override
  public void start() {
    componentManager = new EmbeddableComponentManager();
    componentManager.initialize(this.getClass().getClassLoader());
  }

  @Override
  public void stop() {
  }
  
  private <T> T getComponent(Type clazz, String hint) {
    T component = null;
    if (componentManager != null) {
      try {
        component = componentManager.<T>getInstance(clazz, hint);
      } catch (ComponentLookupException e) {
        throw new RuntimeException("Failed to load component [" + clazz + "] for hint [" + hint + "]", e);
      }
    } else {
      throw new RuntimeException("Component manager has not been initialized before lookup for [" + clazz + "] for hint [" + hint + "]");
    }

    return component;
  }

  private void outputTree(Block parent, int level) {
    StringBuffer buf = new StringBuffer();
    int i = 0;
    while (i++ < level) {
      buf.append("  ");
    }
    buf.append(parent.getClass().getSimpleName());
    if(LOG.isDebugEnabled()){
      LOG.debug(buf.toString());
    }
    List<Block> children = parent.getChildren();
    for (Block block : children) {
      outputTree(block, level + 1);
    }
  }

  /*
   * private XDOM traverseTo(Block parent, int level) { StringBuffer buf = new
   * StringBuffer(); int i = 0; while(i++<level) { buf.append("  "); }
   * buf.append(parent.getClass().getSimpleName());
   * System.out.println(buf.toString()); List<Block> children =
   * parent.getChildren(); for (Block block : children) { outputTree(block,
   * level+1); } }
   */
  private WikiPrinter convert(XDOM xdom, Syntax sourceSyntax, Syntax targetSyntax, boolean supportSectionEdit) throws Exception {

    // Step 2: Run transformations
    try {
      TransformationManager transformationManager = componentManager.getInstance(TransformationManager.class);
      transformationManager.performTransformations(xdom, sourceSyntax);
    } catch (TransformationException e) {
      throw new ConversionException("Failed to execute some transformations", e);
    }

    // Step 3: Locate the Renderer and render the content in the passed printer
    WikiPrinter printer = new DefaultWikiPrinter();
    BlockRenderer renderer;
    try {
      renderer = componentManager.getInstance(BlockRenderer.class, targetSyntax.toIdString());
    } catch (ComponentLookupException e) {
      throw new ConversionException("Failed to locate Renderer for syntax [" + targetSyntax + "]",
                                    e);
    }

    if (supportSectionEdit) {
      List<HeaderBlock> filteredHeaders = getFilteredHeaders(xdom);
      int sectionIndex = 1;
      for (HeaderBlock block : filteredHeaders) {
        SectionBlock section = block.getSection();
        Block parentBlock = section.getParent();
        ResourceReference link = new ResourceReference( "section=" + sectionIndex, ResourceType.URL);       
        sectionIndex++;
        List<Block> emtyList = Collections.emptyList();
        Map<String, String> linkParameters = new LinkedHashMap<String, String>();
        linkParameters.put("title", "Edit section: " + renderXDOM(new XDOM(block.getChildren()), sourceSyntax));
        LinkBlock linkBlock = new LinkBlock(emtyList, link, true, linkParameters);
        Map<String, String> spanParameters = new LinkedHashMap<String, String>();
        spanParameters.put("class", "EditSection");
        FormatBlock spanBlock = new FormatBlock(Collections.singletonList((Block) linkBlock), Format.NONE, spanParameters);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("class", "header-container");
        Block headerContainer = new GroupBlock(params);
        headerContainer.addChild(block);
        headerContainer.addChild(spanBlock);
        section.replaceChild(headerContainer, block);
        
        params.put("class", "section-container");
        Block sectionContainer = new GroupBlock(params);
        sectionContainer.addChild(section);
        parentBlock.replaceChild(sectionContainer, section);
      }
    }
    
    renderer.render(xdom, printer);
    return printer;

  }

  public XDOM parse(String markup, String sourceSyntax) throws Exception {
    XDOM xdom;
    Syntax sSyntax = (sourceSyntax == null) ? Syntax.XWIKI_2_0 : getSyntax(sourceSyntax);
    if (sSyntax == Syntax.XHTML_1_0 || sSyntax == Syntax.ANNOTATED_XHTML_1_0) {
      markup = clean(markup);
    }
    try {
      Parser parser = componentManager.getInstance(Parser.class, sSyntax.toIdString());
      xdom = parser.parse(new StringReader(markup));
    } catch (ComponentLookupException e) {
      throw new ConversionException("Failed to locate Parser for syntax [" + sSyntax + "]", e);
    } catch (ParseException e) {
      throw new ConversionException("Failed to parse input source", e);
    }
    if (LOG.isDebugEnabled()) {
      outputTree(xdom, 0);
    }
    return xdom;
  }

  private String renderXDOM(Block content, Syntax targetSyntax) throws Exception {
    try {
      BlockRenderer renderer = componentManager.getInstance(BlockRenderer.class, targetSyntax.toIdString());
      WikiPrinter printer = new DefaultWikiPrinter();
      renderer.render(content, printer);
      return printer.toString();
    } catch (Exception e) {
      throw new ConversionException("Failed to render document to syntax [" + targetSyntax + "]", e);
    }
  }

  private List<HeaderBlock> getFilteredHeaders(XDOM xdom) {
    List<HeaderBlock> filteredHeaders = new ArrayList<HeaderBlock>();
    // get the headers
    List<HeaderBlock> headers = xdom.getBlocks(new ClassBlockMatcher(HeaderBlock.class), Axes.DESCENDANT); 
    // get the maximum header level
    int sectionDepth = 3;
    // filter the headers
    for (HeaderBlock header : headers) {
      if (header.getLevel().getAsInt() <= sectionDepth) {
        filteredHeaders.add(header);
      }
    }
    return filteredHeaders;
  }
  
  private Syntax getSyntax(String syntaxId) {
    Syntax syntax = Syntax.XWIKI_2_0;
    if (Syntax.XWIKI_2_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.XWIKI_2_0;
    } else if (Syntax.CREOLE_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.CREOLE_1_0;
    } else if (Syntax.CONFLUENCE_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.CONFLUENCE_1_0;
    } else if (Syntax.XHTML_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.XHTML_1_0;
    } else if (Syntax.ANNOTATED_XHTML_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.ANNOTATED_XHTML_1_0;
    } else if (Syntax.MEDIAWIKI_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.MEDIAWIKI_1_0;
    } else if (Syntax.XWIKI_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.XWIKI_1_0;
    } else if (Syntax.JSPWIKI_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.JSPWIKI_1_0;
    } else if (Syntax.TWIKI_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.TWIKI_1_0;
    } else if (Syntax.HTML_4_01.toIdString().equals(syntaxId)) {
      syntax = Syntax.HTML_4_01;
    } else if (Syntax.PLAIN_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.PLAIN_1_0;
    } else if (Syntax.TEX_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.TEX_1_0;
    } else if (Syntax.EVENT_1_0.toIdString().equals(syntaxId)) {
      syntax = Syntax.EVENT_1_0;
    }

    return syntax;
  }

  public String getCssURL() {
    return cssURL;
  }

  public void setCssURL(String cssURL) {
    this.cssURL = cssURL;
  }
}
