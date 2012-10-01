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
package org.exoplatform.wiki.rendering;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.context.Execution;
import org.xwiki.rendering.block.XDOM;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */
public interface RenderingService {
  
  /**
   * Get the excution from xwiki component manager
   * 
   * @return The excution
   * ComponentLookupException in case the component cannot be found
   */
  public Execution getExecution() throws ComponentLookupException, ComponentRepositoryException;
  
  /**
   * Get xwiki component manager
   * 
   * @return The xwiki coponent manager
   */
  public ComponentManager getComponentManager();

  /**
   * Render the markup from source syntax to target syntax
   * 
   * @param markup The text base to convert
   * @param sourceSyntax The original syntax of markup
   * @param targetSyntax The syntax to convert markup to
   * @param supportSectionEdit is suport the section for user to edit or not
   * @return The markup in target syntax
   * @throws Exception
   */
  public String render(String markup, String sourceSyntax, String targetSyntax, boolean supportSectionEdit) throws Exception;

  /**
   * Get the content of a section that specify by sectionIndex
   * 
   * @param markup The markup that contain sections
   * @param sourceSyntax The syntax of the markup
   * @param sectionIndex The index of section that to get the content
   * @return The content of section
   * @throws Exception
   */
  public String getContentOfSection(String markup, String sourceSyntax, String sectionIndex) throws Exception;

  /**
   * Get the content of a section that specify by sectionIndex
   * 
   * @param markup The markup that contain sections
   * @param sourceSyntax The syntax of the markup
   * @param sectionIndex The index of section that to get the content
   * @param newSectionContent The new content to update to section
   * @return The new content of markup after 
   * @throws Exception
   */
  public String updateContentOfSection(String markup, String sourceSyntax, String sectionIndex, String newSectionContent) throws Exception;
  
  /**
   * Parse the markup to XDOM
   * 
   * @param markup The markup to parse
   * @param The syntax of markup
   * @return the tree representation of the content as {@link org.xwiki.rendering.block.Block}s
   */
  public XDOM parse(String markup, String sourceSyntax) throws Exception;
  
  /**
   * Get CSS url
   * 
   * @return The url of css
   */
  public String getCssURL();

  /**
   * Set CSS url
   * 
   * @param cssURL The url of css
   */
  public void setCssURL(String cssURL);
}
