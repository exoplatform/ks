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
package org.exoplatform.wiki.mow.api;

import java.util.Collection;

import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public interface WikiStore {

  /**
   * Get all wikis available
   * 
   * @return
   */
  Collection<Wiki> getWikis();

  /**
   * Get a wiki of a given type
   * 
   * @param wikiType
   * @param name
   * @return
   */
  Wiki getWiki(WikiType wikiType, String name);

  /**
   * Add a new wiki of a given type
   * 
   * @param wikiType
   * @param name
   */
  void addWiki(WikiType wikiType, String name);
  
  /** 
   * get wiki container
   * 
   * @param wikiType The wiki type
   * @return Wiki container
   */
  public  <W extends Wiki>WikiContainer<W> getWikiContainer(WikiType wikiType);

}
