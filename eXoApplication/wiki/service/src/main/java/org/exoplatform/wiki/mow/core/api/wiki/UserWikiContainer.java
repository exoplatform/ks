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
package org.exoplatform.wiki.mow.core.api.wiki;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.NoSuchNodeException;
import org.chromattic.api.UndeclaredRepositoryException;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.PrimaryType;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
@PrimaryType(name = WikiNodeType.USER_WIKI_CONTAINER)
public abstract class UserWikiContainer extends WikiContainer<UserWiki> {

  @OneToOne
  @MappedBy(WikiNodeType.Definition.USER_WIKI_CONTAINER_NAME )
  public abstract WikiStoreImpl getMultiWiki();

  public UserWiki addWiki(String wikiOwner) {
    NodeHierarchyCreator nodeHierachyCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer()
                                                                                         .getComponentInstanceOfType(NodeHierarchyCreator.class);    
    wikiOwner = validateWikiOwner(wikiOwner);
    if(wikiOwner == null){
      return null;
    }
    OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer()
                                                                                       .getComponentInstanceOfType(OrganizationService.class);
    try {
      if (organizationService.getUserHandler().findUserByName(wikiOwner) == null) {
        return null;
      }
    } catch (Exception ex) {
      return null;
    }
    ChromatticSession session = getMultiWiki().getSession();
    Node wikiNode = null;
    try {      
      Node tempNode = nodeHierachyCreator.getUserApplicationNode(CommonUtils.createSystemProvider(),
                                                                 wikiOwner);      
      Node userDataNode = (Node) session.getJCRSession().getItem(tempNode.getPath());        
      try {
        wikiNode = userDataNode.getNode(WikiNodeType.Definition.WIKI_APPLICATION);
      } catch (PathNotFoundException e) {
        wikiNode = userDataNode.addNode(WikiNodeType.Definition.WIKI_APPLICATION, WikiNodeType.USER_WIKI);
        userDataNode.save();
      }
    } catch (Exception e) {
      if (e instanceof PathNotFoundException)
        throw new NoSuchNodeException(e);
      else
        throw new UndeclaredRepositoryException(e.getMessage());
    }
    UserWiki uwiki = session.findByNode(UserWiki.class, wikiNode);
    uwiki.setWikiService(getwService());
    uwiki.setOwner(wikiOwner);
    uwiki.setUserWikis(this);
    uwiki.getPreferences();
    session.save();
    return uwiki;
  }
}
