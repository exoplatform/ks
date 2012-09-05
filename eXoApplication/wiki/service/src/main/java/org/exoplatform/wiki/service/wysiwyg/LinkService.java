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
package org.exoplatform.wiki.service.wysiwyg;

import org.xwiki.component.annotation.Role;
import org.xwiki.gwt.wysiwyg.client.wiki.EntityConfig;
import org.xwiki.gwt.wysiwyg.client.wiki.EntityReference;
import org.xwiki.gwt.wysiwyg.client.wiki.ResourceReference;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 12, 2010  
 */
@Role
public interface LinkService {
  /**
   * Creates an entity link configuration object (URL, link reference) for a link with the specified origin and
   * destination. The link reference in the returned {@link EntityConfig} is relative to the link origin.
   * 
   * @param origin the origin of the link
   * @param destination the destination of the link
   * @return the link configuration object that can be used to insert the link in the origin page
   */
  EntityConfig getEntityConfig(EntityReference origin, ResourceReference destination);

  /**
   * Parses the given link reference and extracts a reference to the linked entity. The returned entity reference is
   * resolved relative to the given base entity reference.
   * 
   * @param linkReference a link reference pointing to an entity of the specified type
   * @param baseReference the entity reference used to resolve the linked entity reference
   * @return a reference to the linked entity
   */
  ResourceReference parseLinkReference(String linkReference, EntityReference baseReference);

}
