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
package org.exoplatform.wiki.rendering.macro.pagetree;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.rendering.context.MarkupContextManager;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.tree.TreeNode;
import org.exoplatform.wiki.tree.utils.TreeUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.wiki.WikiModel;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Jan 11, 2011  
 */

@Component("pagetree")
public class PageTreeMacro extends AbstractMacro<PageTreeMacroParameters> {
  private Log log = ExoLogger.getLogger(this.getClass());
  /**
   * The description of the macro
   */
  private static final String DESCRIPTION = "Display a hierachy descendants tree of a specific page";
  
  private static final Syntax XHTML_SYNTAX = new Syntax(SyntaxType.XHTML, "1.0");
  
  /**
   * Used to get the current syntax parser.
   */
  @Inject
  private ComponentManager componentManager;
  
  @Inject
  private Execution execution;
  
  /**
   * Used to get the build context for document
   */
  @Inject
  private MarkupContextManager markupContextManager;
  
  private boolean excerpt;
  
  public PageTreeMacro() {
    super("Page Tree", DESCRIPTION, PageTreeMacroParameters.class);
    setDefaultCategory(DEFAULT_CATEGORY_NAVIGATION);
  }

  @Override
  public List<Block> execute(PageTreeMacroParameters parameters,
                             String content,
                             MacroTransformationContext context) throws MacroExecutionException {
    String documentName = parameters.getRoot();
    String startDepth = parameters.getStartDepth();
    excerpt = parameters.isExcerpt();
    if (StringUtils.isEmpty(startDepth)) {
      startDepth = "1";
    }
    WikiPageParams params = markupContextManager.getMarkupContext(documentName, ResourceType.DOCUMENT);
    if (StringUtils.EMPTY.equals(documentName)) {
      WikiContext wikiContext = getWikiContext();
      if (wikiContext != null)
        params = wikiContext;
    }
    Block root;
    try {
      root = generateTree(params, startDepth);
      return Collections.singletonList(root);
    } catch (Exception e) {
      log.debug("Failed to execute page tree macro", e);
      return Collections.emptyList();
    }
  }

  @Override
  public boolean supportsInlineMode() {

    return true;
  }
  
  /**
   * @return the component manager.
   */
  public ComponentManager getComponentManager() {
    return this.componentManager;
  }

  protected WikiModel getWikiModel(MacroTransformationContext context) throws MacroExecutionException {
    try {
      return getComponentManager().lookup(WikiModel.class);
    } catch (ComponentLookupException e) {
      throw new MacroExecutionException("Failed to find wiki model", e);
    }
  }
  
  private Block generateTree(WikiPageParams params, String startDepth) throws Exception {
    StringBuilder treeSb = new StringBuilder();
    StringBuilder initSb = new StringBuilder();
    TreeNode node = TreeUtils.getTreeNode(params);
    WikiContext wikiContext = getWikiContext();   
    String treeID = "PageTree"+ wikiContext.getPageTreeId();
    String treeRestURI = wikiContext.getTreeRestURI();
    String redirectURI = wikiContext.getRedirectURI();
    String baseUrl = wikiContext.getBaseUrl();
    
    initSb.append("?")
          .append(TreeNode.PATH)
          .append("=")
          .append(node.getPath())
          .append("&")
          .append(TreeNode.SHOW_EXCERPT)
          .append("=")
          .append(excerpt)
          .append("&")
          .append(TreeNode.DEPTH)
          .append("=")
          .append(startDepth);
    treeSb.append("<div class=\"UITreeExplorer\" id =\"").append(treeID).append("\">")
          .append("  <div>")
          .append("    <input class=\"ChildrenURL\" title=\"hidden\" type=\"hidden\" value=\"").append(treeRestURI).append("\" />")
          .append("    <a class=\"SelectNode\" style=\"display:none\" href=\"").append(redirectURI).append("\" ></a>")
          .append(buildHierachyNode(treeID, initSb.toString(), baseUrl))                
          .append("  </div>")
          .append("</div>");
    RawBlock testRaw = new RawBlock(treeSb.toString(), XHTML_SYNTAX);
    return testRaw;
  }  

  public String buildHierachyNode(String treeId, String initParam, String baseUrl) throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("    <div class=\"NodeGroup\">")
      .append("      <script type=\"text/javascript\">")
      .append("        function initTree(){eXo.wiki.UITreeExplorer.init(\"" + treeId + "\",\"" + initParam + "\",false, true,\"" + baseUrl + "\");}")
      .append("        var isInIFrame = (window.location != window.parent.location) ? true : false;")
      .append("        if (isInIFrame) {")
      .append("          if (window.attachEvent) {window.attachEvent('onload', initTree);}")
      .append("          else if (window.addEventListener) {window.addEventListener('load', initTree, false);}")
      .append("            else {document.addEventListener('load', initTree, false);}")      
      .append("        }")
      .append("        else { eXo.core.Browser.addOnLoadCallback(\"init" + treeId + "\",initTree);}")
      .append("      </script>");
    sb.append("    </div>");
    return sb.toString();
  }
 
  private WikiContext getWikiContext() {
    ExecutionContext ec = execution.getContext();
    if (ec != null) {
      WikiContext wikiContext = (WikiContext) ec.getProperty(WikiContext.WIKICONTEXT);
      return wikiContext;
    }
    return null;
  }
}
