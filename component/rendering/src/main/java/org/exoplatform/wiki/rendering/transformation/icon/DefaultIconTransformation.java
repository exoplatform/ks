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
package org.exoplatform.wiki.rendering.transformation.icon;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.SpecialSymbolBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.block.ProtectedBlockFilter;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;
import org.xwiki.rendering.transformation.icon.IconTransformationConfiguration;
import org.xwiki.rendering.util.ParserUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Dec 2, 2011  
 */
@Component("icon")
public class DefaultIconTransformation extends AbstractTransformation implements Initializable {
  /**
   * Used to get the icon mapping information (suite of characters mapped to an icon name).
   */
  @Inject
  private IconTransformationConfiguration configuration;
  
  /**
   * Used to get the current syntax parser.
   */
  @Inject
  private ComponentManager componentManager;

  /**
   * Used to remove the top level paragraph since we don't currently have an inline parser.
   */
  private ParserUtils parserUtils = new ParserUtils();  
  
  /**
   * The logger to log.
   */
  @Inject
  private Logger logger;
  
  /**
   * The computed tree used to perform the fast mapping for each syntax
   */
  private Map<String, XDOM>               mappingDOMs = new HashMap<String, XDOM>();

  /**
   * Used to filter protected blocks (code macro marker block, etc).
   */
  private ProtectedBlockFilter filter = new ProtectedBlockFilter();

  /**
   * {@inheritDoc}
   * @see org.xwiki.component.phase.Initializable#initialize()
   */
  public void initialize() throws InitializationException {
  }

  /**
   * {@inheritDoc}
   * @see AbstractTransformation#transform(Block, TransformationContext)
   */
  public void transform(Block source, TransformationContext context) throws TransformationException {
    try {
      XDOM mappingTree = this.mappingDOMs.get(context.getSyntax().toIdString());
      if (mappingTree == null) {
        Parser syntaxParser = componentManager.getInstance(Parser.class, context.getSyntax().toIdString());
        mappingTree = new XDOM(Collections.<Block> emptyList());
        for (Map.Entry<Object, Object> entry : this.configuration.getMappings().entrySet()) {
          try {
            XDOM xdom = syntaxParser.parse(new StringReader((String) entry.getKey()));
            // Remove top level paragraph
            this.parserUtils.removeTopLevelParagraph(xdom.getChildren());
            mergeTree(mappingTree, convertToDeepTree(xdom, (String) entry.getValue()));
            // Put constructed XDOM to map to use later
            this.mappingDOMs.put(context.getSyntax().toIdString(), mappingTree);
          } catch (ParseException e) {
            if (logger.isDebugEnabled()) {
              logger.debug("Failed to parse icon symbols [" + entry.getKey() + "]. Reason = [" + e.getMessage() + "]");
            }
          }
        }
      }
      List<Block> filteredBlocks = this.filter.filter(source.getChildren());
      if (!mappingTree.getChildren().isEmpty() && !filteredBlocks.isEmpty()) {
        parseTree(mappingTree, filteredBlocks);
      }
    } catch (ComponentLookupException e) {
      logger.error("Failed to transform icon symbols in syntax [" + context.getSyntax().toIdString() + "]. Reason = ["
          + e.getMessage() + "]");
    }
  }

  /**
   * Converts a standard XDOM tree into a deep tree: sibling are transformed into parent/child relationships and
   * the leaf node is an Image node referencing the passed icon name.
   *
   * @param sourceTree the source tree to modify
   * @param iconName the name of the icon to display when a match is found
   * @return the modified tree
   */
  private Block convertToDeepTree(Block sourceTree, String iconName)
  {
      XDOM targetTree = new XDOM(Collections.<Block>emptyList());
      Block pointer = targetTree;
      for (Block block : sourceTree.getChildren()) {
          pointer.addChild(block);
          pointer = block;
      }
      // Add an image block as the last block
      pointer.addChild(new ImageBlock(new ResourceReference(iconName, ResourceType.ICON), true));
      return targetTree;
  }

  /**
   * Merged two XDOM trees.
   *
   * @param targetTree the tree to merge into
   * @param sourceTree the tree to merge
   */
  private void mergeTree(Block targetTree, Block sourceTree)
  {
      for (Block block : sourceTree.getChildren()) {
          // Check if the current block exists in the target tree at the same place in the tree
          int pos = indexOf(targetTree.getChildren(), block);
          if (pos > -1) {
              Block foundBlock = targetTree.getChildren().get(pos);
              mergeTree(foundBlock, block);
          } else {
              targetTree.addChild(block);
          }
      }
  }

  /**
   * Shallow indexOf implementation that only compares nodes based on their data and not their children data.
   *
   * @param targetBlocks the list of blocks to look into
   * @param block the block to look for in the list of passed blocks
   * @return the position of the block in the list of target blocks  and -1 if not found
   */
  private int indexOf(List<Block> targetBlocks, Block block)
  {
      int pos = 0;
      for (Block targetBlock : targetBlocks) {
          // Test a non deep equality
          if (blockEquals(targetBlock, block)) {
              return pos;
          }
          pos++;
      }
      return -1;
  }

  /**
   * Compares two nodes in a shallow manner (children data are not compared).
   *
   * @param target the target node to compare
   * @param source the source node to compare
   * @return true if the two blocks are equals in a shallow manner (children data are not compared)
   */
  private boolean blockEquals(Block target, Block source) {
    boolean found = false;
    if (source instanceof SpecialSymbolBlock && target instanceof SpecialSymbolBlock) {
      if (((SpecialSymbolBlock) target).getSymbol() == ((SpecialSymbolBlock) source).getSymbol()) {
        found = true;
      }
    } else if (source instanceof WordBlock && target instanceof WordBlock) {
      if (((WordBlock) target).getWord().equals(((WordBlock) source).getWord())) {
        found = true;
      }
    } else if (source.equals(target)) {
      found = true;
    }
    return found;
  }

  /**
   * Parse a list of blocks and replace suite of Blocks matching the icon mapping definitions by image blocks.
   *
   * @param sourceBlocks the blocks to parse
   */
  private void parseTree(XDOM mappingTree, List<Block> sourceBlocks)
  {
      Block matchStartBlock = null;
      int count = 0;
      Block mappingCursor =mappingTree.getChildren().get(0);
      Block sourceBlock = sourceBlocks.get(0);
      while (sourceBlock != null) {
          while (mappingCursor != null) {
              if (blockEquals(sourceBlock, mappingCursor)) {
                  if (matchStartBlock == null) {
                      matchStartBlock = sourceBlock;
                  }
                  count++;
                  mappingCursor = mappingCursor.getChildren().get(0);
                  // If we reach the Image Block then we've found a match!
                  if (mappingCursor instanceof ImageBlock) {
                      // Replace the first source block with the image block and remove all other blocks...
                      for (int i = 0; i < count - 1; i++) {
                          matchStartBlock.getParent().removeBlock(matchStartBlock.getNextSibling());
                      }
                      sourceBlock = mappingCursor.clone();
                      matchStartBlock.getParent().replaceChild(sourceBlock, matchStartBlock);
                      mappingCursor = null;
                      matchStartBlock = null;
                      count = 0;
                  } else {
                      // Look for next block match
                      break;
                  }
              } else {
                  mappingCursor = mappingCursor.getNextSibling();
              }
          }
          // Look for a match in children of the source block
          List<Block> filteredSourceBlocks = this.filter.filter(sourceBlock.getChildren());
          if (filteredSourceBlocks.size() > 0) {
              parseTree(mappingTree, filteredSourceBlocks);
          } else if (mappingCursor == null) {
              // No match has been found, reset state variables
              mappingCursor = mappingTree.getChildren().get(0);
              count = 0;
              matchStartBlock = null;
          }
          sourceBlock = this.filter.getNextSibling(sourceBlock);
      }
  }
}
