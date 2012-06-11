/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.upgrade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Oct 4, 2011  
 */
public class UpgradeForumPlugin extends UpgradeProductPlugin {

  private static final Log    log               = ExoLogger.getLogger(UpgradeForumPlugin.class);

  private static final String NEW_DOAMIN_FORUM  = "new.domain.forum";

  private static final String GROUP_SPACE_ID    = "/spaces";

  private static final String CATEGORY_SPACE_ID = Utils.CATEGORY + "spaces";

  private static final String UNKNOW_VERSION    = "0";

  private String              newDomain         = "";

  private KSDataLocation      dataLocation;
  
  public UpgradeForumPlugin(InitParams initParams) {
    super(initParams);
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    this.dataLocation = ((KSDataLocation) container.getComponentInstance(KSDataLocation.class));
    getNewDomain();
  }

  public void processUpgrade(String oldVersion, String newVersion) {
    log.info(String.format("Migrating data from %s to %s for Forum......\n\n", oldVersion, newVersion));
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      // register new nodeTypes
      log.info("\n\nRegister new nodeTypes...\n");
      UpgradeUtils.registerNodeTypes("jar:/conf/portal/forum-nodetypes.xml", ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
      UpgradeUtils.registerNodeTypes("jar:/conf/portal/forum-migrate-nodetypes.xml", ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      migrationForumData(sProvider, oldVersion, newVersion);
      migrationSpaceOfPLF(sProvider, oldVersion, newVersion);
    } catch (Exception e) {
      log.warn("[UpgradeForumPlugin] Exception when migrate data for Forum.", e);
    } finally {
      sProvider.close();
    }
    log.info("The end Forum Migration......\n\n");
  }

  private void getNewDomain() {
    try {
      log.info("\nGet new domain for migration forum datas...");
      Properties props = new Properties(System.getProperties());
      newDomain = props.getProperty(NEW_DOAMIN_FORUM);
    } catch (Exception e) {
      log.warn("Failed to get new domain in system configation.... ");
    }
  }

  private void migrationForumData(SessionProvider sProvider, String oldVersion, String newVersion) throws Exception {
    // Migration 2.1.x to 2.2.3
    // properties new: exo:isWatting in nodetype: exo:post
    if (oldVersion.indexOf("2.1.") > 0 || UNKNOW_VERSION.equals(oldVersion)) {
      log.info("[UpgradeForumPlugin] Start migrate forum data from " + oldVersion + " to " + newVersion);
      Node forumHome = getForumHomeNode(sProvider);
      NodeIterator pIter = getNodeIterator(sProvider, forumHome, Utils.EXO_POST, new StringBuilder(""));
      long i = 0, total = pIter.getSize();
      log.info("\nThe size of list post migration: " + total);
      while (pIter.hasNext()) {
        Node pNode = pIter.nextNode();
        try {
          pNode.getProperty(Utils.EXO_IS_WAITING);
        } catch (Exception e) {
          pNode.addMixin("exo:forumMigrate");
          pNode.setProperty(Utils.EXO_IS_WAITING, false);
          forumHome.getSession().save();
          log.info(String.format("Migration node %s/%s with node name %s property: exo:isWatting=false", ++i, total, pNode.getName()));
        }
        // set new link for this post.
        if (!Utils.isEmpty(newDomain)) {
          saveLink(pNode);
        }
      }
      // set new link for topics.
      if (!Utils.isEmpty(newDomain)) {
        NodeIterator tIter = getNodeIterator(sProvider, forumHome, Utils.EXO_TOPIC, new StringBuilder(""));
        log.info("The size of list topic migration: " + tIter.getSize());
        while (tIter.hasNext()) {
          saveLink(tIter.nextNode());
        }
      }
    }
  }

  private void saveLink(Node node) throws Exception {
    try {
      String link = node.getProperty(Utils.EXO_LINK).getString(); 
      if (!Utils.isEmpty(link) && link.indexOf(newDomain) < 0) {
        link = new StringBuilder(newDomain).append(link.substring(link.indexOf("/", 8))).toString();
        node.setProperty(Utils.EXO_LINK, link);
        node.save();
        log.info(String.format("Migration node %s property: exo:link=%s", node.getName(), link));
      }
    } catch (Exception e) {
      log.warn(String.format("Can not migration node %s with property exo:link", node.getName()));
    }
  }

  private void migrationSpaceOfPLF(SessionProvider sProvider, String oldVersion, String newVersion) throws Exception {
    if(oldVersion.indexOf("2.1.") > 0 || UNKNOW_VERSION.equals(oldVersion)) {
      log.info("[UpgradeForumPlugin] Start migrate forum data space in platform from version " + oldVersion + " to " + newVersion);
      Node forumHome = getForumHomeNode(sProvider);
      NodeIterator cIter = getOldAllNodeCateSpace(sProvider, forumHome);
      log.info("\nNumber categories space migration: " + cIter.getSize());
      if (cIter.getSize() > 0) {
        NodeIterator fIter;
        String[] permission;
        PropertyReader reader;
        String nodeName = null, fName;
        Node newSpNode = getCatNSPNode(sProvider);
        String newSpPath = newSpNode.getPath();
        log.info("\nPath of category Spaces: " + newSpPath);
        Session session = forumHome.getSession();
        List<String> groups = getAllGroupOfSpaces();
        List<String> lastGroupIds = new ArrayList<String>();
        for (String string : groups) {
          lastGroupIds.add(string.replaceFirst(GROUP_SPACE_ID+"/", ""));
        }
        log.info("\nGet all groups of spaces: " + lastGroupIds.toString());
        while (cIter.hasNext()) {
          Node cNode = cIter.nextNode();
          reader = new PropertyReader(cNode);
          permission = reader.strings(Utils.EXO_USER_PRIVATE, new String[] { "" });
          log.info("\nMigration category : " + cNode.getName());
          fIter = cNode.getNodes();
          while (fIter.hasNext()) {
            Node fNode = fIter.nextNode();
            nodeName = fNode.getName();
            log.info("\nMigration forum : " + nodeName);
            try {
              if (fNode.isNodeType(Utils.EXO_FORUM)) {
                fName = getGroupId(permission);
                log.info("\n Forum migration has group: " + fName);
                if (lastGroupIds.contains(fName)) {
                  fNode.setProperty(Utils.EXO_POSTER, permission);
                  fNode.setProperty(Utils.EXO_CREATE_TOPIC_ROLE, permission);
                  fNode.setProperty(Utils.EXO_VIEWER, permission);
                  fNode.save();
                  fName = Utils.FORUM_SPACE_ID_PREFIX + fName;
                } else {
                  fNode.setProperty(Utils.EXO_FORUM_ORDER, 100);
                  fNode.save();
                  fName = nodeName;
                }
                if (!hasNodeInSP(sProvider, fName)) {
                  session.move(fNode.getPath(), newSpPath + "/" + fName);
                  session.save();
                  log.info(String.format("Move forum %s in to category Spaces with new node name %s", nodeName, fName));
                } else {
                  moveTopicsToForumSpace(session, fNode, newSpPath + "/" + fName);
                  log.info(String.format("Move all topics from %s in to forum category spaces %s", nodeName, fName));
                }
              }
            } catch (Exception e) {
              log.info(String.format("Can not moving the forum %s in to category Spaces...", nodeName));
            }
          }
          log.info(String.format("Remove old category space: %s ", cNode.getName()));
          cNode.remove();
          session.save();
        }
        newSpNode.setProperty(Utils.EXO_USER_PRIVATE, groups.toArray(new String[groups.size()]));
        forumHome.save();
      }
    }
  }

  private void moveTopicsToForumSpace(Session session, Node fNode, String fpatch) throws Exception {
    try {
      NodeIterator tIter = fNode.getNodes();
      while (tIter.hasNext()) {
        Node tNode = tIter.nextNode();
        if (tNode.isNodeType(Utils.EXO_TOPIC)) {
          session.move(fNode.getPath(), fpatch + "/" + tNode.getName());
        }
      }
      session.save();
    } catch (Exception e) {
      log.info(String.format("Failed to move all topics from %s in to forum category spaces %s", fNode.getName(), fpatch));
    }
  }

  private String getGroupId(String[] grs) throws Exception {
    String s;
    for (int i = 0; i < grs.length; i++) {
      s = grs[i];
      if(s.indexOf("/spaces/") >= 0) {
        s = s.substring(s.lastIndexOf("/")+1);
        if(!Utils.isEmpty(s)) return s;
      }
    }
    return "";
  }

  private boolean hasNodeInSP(SessionProvider sProvider, String id) throws Exception {
    Node categoryHome = getNodeByPath(dataLocation.getForumCategoriesLocation(), sProvider);
    return categoryHome.hasNode(id);
  }
  
  private Node getForumHomeNode(SessionProvider sProvider) throws Exception {
    return getNodeByPath(dataLocation.getForumHomeLocation(), sProvider);
  }

  private Node getCatNSPNode(SessionProvider sProvider) throws Exception {
    Node categoryHome = getNodeByPath(dataLocation.getForumCategoriesLocation(), sProvider);
    Session session = categoryHome.getSession();
    Node catNode = null;
    try {
      catNode = categoryHome.getNode(CATEGORY_SPACE_ID);
    } catch (PathNotFoundException e) {
      catNode = categoryHome.addNode(CATEGORY_SPACE_ID, Utils.EXO_FORUM_CATEGORY);
      catNode.setProperty(Utils.EXO_ID, CATEGORY_SPACE_ID);
      catNode.setProperty(Utils.EXO_OWNER, "");
      catNode.setProperty(Utils.EXO_CREATED_DATE, GregorianCalendar.getInstance());
      catNode.setProperty(Utils.EXO_NAME, "Spaces");
      catNode.setProperty(Utils.EXO_CATEGORY_ORDER, 100);
      catNode.setProperty(Utils.EXO_DESCRIPTION, "The category sotorage all forums of spaces.");
      catNode.setProperty(Utils.EXO_MODIFIED_BY, "");
      catNode.setProperty(Utils.EXO_MODIFIED_DATE, GregorianCalendar.getInstance());
      catNode.setProperty(Utils.EXO_USER_PRIVATE, new String[]{""});
      session.save();
    }
    return catNode;
  }

  private NodeIterator getOldAllNodeCateSpace(SessionProvider sProvider, Node forumHome) throws Exception {
    StringBuilder strQuery = new StringBuilder("[((@").append(Utils.EXO_NAME).append("='spaces') or (jcr:contains(@")
        .append(Utils.EXO_USER_PRIVATE).append(", 'spaces'))) and (@").append(Utils.EXO_ID).append("!='").append(CATEGORY_SPACE_ID).append("')]");
    return getNodeIterator(sProvider, forumHome, Utils.EXO_FORUM_CATEGORY, strQuery);
  }

  private NodeIterator getNodeIterator(SessionProvider sProvider, Node node, String nodeType, StringBuilder strQuery) throws Exception {
    try {
      StringBuilder pathQuery = new StringBuilder(Utils.JCR_ROOT).append(node.getPath()).append("//element(*,").append(nodeType).append(")").append(strQuery).append(" order by @").append(Utils.EXO_NAME).append(" descending");
      strQuery = pathQuery;
      QueryManager qm = node.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(pathQuery.toString(), Query.XPATH);
      QueryResult result = query.execute();
      return result.getNodes();
    } catch (Exception e) {
      log.error(String.format("\n Get not by query %s is failed.", strQuery), e);
      return null;
    }
  }

  public Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception {
    if (nodePath.indexOf("/") == 0) {
      nodePath = nodePath.substring(1);
    }
    return getSession(sessionProvider).getRootNode().getNode(nodePath);
  }

  public boolean shouldProceedToUpgrade(String newVersion, String oldVersion) {
    return VersionComparator.isBefore(oldVersion, newVersion);
  }

  @SuppressWarnings("deprecation")
  private Session getSession(SessionProvider sProvider) throws Exception {
    return dataLocation.getSessionManager().getSession(sProvider);
  }

  @SuppressWarnings("unchecked")
  private List<String> getAllGroupOfSpaces() throws Exception {
    List<String> groupIds = new ArrayList<String>();
    try {
      PortalContainer container = PortalContainer.getInstance();
      OrganizationService organizationService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
      ((ComponentRequestLifecycle)organizationService).startRequest(container);
      Group group = organizationService.getGroupHandler().findGroupById(GROUP_SPACE_ID);
      if (group != null) {
        Collection<Group> groups = organizationService.getGroupHandler().findGroups(group);
        for (Group gr : groups) {
          groupIds.add(gr.getId());
        }
      }
      ((ComponentRequestLifecycle)organizationService).endRequest(container);
    } catch (Exception e) {
      log.warn("\nFailed to get all groups in spaces.", e);
    }
    return groupIds;
  }

}
