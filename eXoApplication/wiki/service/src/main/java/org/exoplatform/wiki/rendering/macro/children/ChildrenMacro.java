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
package org.exoplatform.wiki.rendering.macro.children;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.builder.ReferenceBuilder;
import org.exoplatform.wiki.rendering.cache.PageRenderingCacheService;
import org.exoplatform.wiki.rendering.context.MarkupContextManager;
import org.exoplatform.wiki.rendering.macro.ExcerptUtils;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.tree.TreeNode;
import org.exoplatform.wiki.tree.utils.TreeUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Jan 06, 2011  
 */

@Component("children")
public class ChildrenMacro extends AbstractMacro<ChildrenMacroParameters> {
  private static final Log    log         = ExoLogger.getLogger(ChildrenMacro.class);

  /**
   * The description of the macro
   */
  private static final String DESCRIPTION = "Display children and descendants of a specific page";
  
  /**
   * Used to get the current syntax parser.
   */
  @Inject
  private ComponentManager    componentManager;
  
  /**
   * Used to get the current context
   */
  @Inject
  private Execution           execution;
  
  /**
   * Used to get the build context for document
   */
  @Inject
  private MarkupContextManager markupContextManager;
  
  private boolean excerpt;
  
  public ChildrenMacro() {
    super("Chilren", DESCRIPTION, ChildrenMacroParameters.class);
    setDefaultCategory(DEFAULT_CATEGORY_NAVIGATION);
  }

  @Override
  public List<Block> execute(ChildrenMacroParameters parameters,
                             String content,
                             MacroTransformationContext context) throws MacroExecutionException {
    
    boolean descendant = parameters.isDescendant();
    excerpt = parameters.isExcerpt();
    String documentName = parameters.getParent();
    String childrenNum = parameters.getChildrenNum();
    String depth = parameters.getDepth();
    
    WikiPageParams params = markupContextManager.getMarkupContext(documentName, ResourceType.DOCUMENT);
    if (StringUtils.EMPTY.equals(documentName)) {
      ExecutionContext ec = execution.getContext();
      if (ec != null) {
        WikiContext wikiContext = (WikiContext) ec.getProperty(WikiContext.WIKICONTEXT);
        params = wikiContext;
      }
    }
    
    Block root;
    try {
      WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
      
      // Check if root page exist
      PageImpl wikiPage = (PageImpl) wikiService.getPageById(params.getType(), params.getOwner(), params.getPageId());
      if (wikiPage == null) {
        // if root page was renamed then find it
        wikiPage = (PageImpl) wikiService.getRelatedPage(params.getType(), params.getOwner(), params.getPageId());
        if (wikiPage != null) {
          params = new WikiPageParams(wikiPage.getWiki().getType(), wikiPage.getWiki().getOwner(), wikiPage.getName());
        }
      }
      
      root = generateTree(params, descendant, childrenNum, depth, context);
      PageRenderingCacheService renderingCacheService = (PageRenderingCacheService) ExoContainerContext.getCurrentContainer()
          .getComponentInstanceOfType(PageRenderingCacheService.class);
      WikiContext wikiContext = getWikiContext();
      renderingCacheService.addPageLink(new WikiPageParams(wikiContext.getType(), wikiContext.getOwner(), wikiContext.getPageId()), 
          new WikiPageParams(params.getType(), params.getOwner(), params.getPageId()));
      return Collections.singletonList(root);
    } catch (Exception e) {
      log.debug("Failed to ", e);
      return Collections.emptyList();
    }
  }

  private Block generateTree(WikiPageParams params,
                             boolean descendant,
                             String childrenNum,
                             String depth,MacroTransformationContext context) throws Exception {
    Block block = new GroupBlock();
    HashMap<String, Object> treeContext = new HashMap<String, Object>();
    treeContext.put(TreeNode.SHOW_DESCENDANT, descendant);
    treeContext.put(TreeNode.CHILDREN_NUMBER, childrenNum);
    treeContext.put(TreeNode.DEPTH, depth);
    TreeNode node = TreeUtils.getDescendants(params, treeContext);    
    addBlock(block, node,context);
    return block;
  }

  public ListItemBlock trankformToBlock(TreeNode node, MacroTransformationContext context) throws Exception {
    WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer()
                                                               .getComponentInstanceOfType(WikiService.class);
    List<Block> blocks = new ArrayList<Block>();
    
    WikiPageParams params = TreeUtils.getPageParamsFromPath(node.getPath());
    PageImpl page = (PageImpl) wikiService.getPageById(params.getType(),
                                                       params.getOwner(),
                                                       params.getPageId());
    DocumentResourceReference link = new DocumentResourceReference(getReferenceBuilder(context).build(params));
    List<Block> content = new ArrayList<Block>();
    content.add(new WordBlock(page.getTitle()));

    LinkBlock linkBlock = new LinkBlock(content, link, true);
    blocks.add(linkBlock);
    if (excerpt) {
      String excerpts = ExcerptUtils.getExcerpts(params);
      if (!StringUtils.EMPTY.equals(excerpts)) {
        blocks.add(new RawBlock(excerpts, Syntax.XHTML_1_0));
      }
    }
    return new ListItemBlock(blocks);
  }

  public void addBlock(Block block, TreeNode node, MacroTransformationContext context) throws Exception {
    List<TreeNode> children = node.getChildren();
    Block childrenBlock = new BulletedListBlock(Collections.<Block> emptyList());
    int size = children.size();
    for (int i = 0; i < size; i++) {
      Block listBlock = trankformToBlock(children.get(i), context);
      addBlock(listBlock, children.get(i), context);
      childrenBlock.addChild(listBlock);
    }
    block.addChild(childrenBlock);
  }

  @Override
  public boolean supportsInlineMode() {
    return true;
  }

  private ReferenceBuilder getReferenceBuilder(MacroTransformationContext context) throws MacroExecutionException {
    try {
      return componentManager.getInstance(ReferenceBuilder.class, context.getSyntax().toIdString());
    } catch (ComponentLookupException e) {
      throw new MacroExecutionException(String.format("Failed to find reference builder for syntax %s", context.getSyntax()
                                                                                                               .toIdString()), e);
    }
  }
  
  private WikiContext getWikiContext() {
    ExecutionContext ec = execution.getContext();
    WikiContext wikiContext = null;
    if (ec != null) {
      wikiContext = (WikiContext) ec.getProperty(WikiContext.WIKICONTEXT);
    }
    return wikiContext;
  }
}
