/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

package org.exoplatform.ks.bbcode.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.api.BBCodeService;
import org.exoplatform.ks.bbcode.spi.BBCodeData;
import org.exoplatform.ks.bbcode.spi.BBCodePlugin;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.SessionManager;
import org.exoplatform.management.ManagementAware;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

/**
 * Managed service implementation for {@link BBCodeService}. 
 * Stores BBCodes in JCR at {@link KSDataLocation#getBBCodesLocation()} 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
@Managed
@NameTemplate(@Property(key = "service", value = "bbcode"))
@ManagedDescription("BBCodes management")
public class BBCodeServiceImpl implements Startable, BBCodeService, ManagementAware {

  public final static String BBCODE_NODE_TYPE      = "exo:forumBBCode".intern();

  public final static String BBCODE_HOME_NODE_TYPE = "exo:forumBBCodeHome".intern();

  private List<BBCodePlugin> plugins;

  private KSDataLocation     dataLocator;

  private SessionManager     sessionManager;

  private List<String>       activeBBCodesCache;

  private ManagementContext  context;

  private static Log         log                   = ExoLogger.getLogger(BBCodeServiceImpl.class);

  public BBCodeServiceImpl() {
    activeBBCodesCache = new ArrayList<String>();
    plugins = new ArrayList<BBCodePlugin>();
  }

  public BBCodeServiceImpl(KSDataLocation dataLocator) {
    this();
    setDataLocator(dataLocator);
  }

  private Node getBBcodeHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getBBCodesLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
  }

  /**
   * {@inheritDoc}
   */
  public void registerBBCodePlugin(BBCodePlugin plugin) throws Exception {
    plugins.add(plugin);
  }

  /**
   * {@inheritDoc}
   */
  public void initDefaultBBCodes() throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node bbCodeHome = getBBcodeHome(sProvider);
      NodeIterator iter = bbCodeHome.getNodes();
      if (iter.getSize() <= 0) {
        List<BBCode> bbCodes = new ArrayList<BBCode>();
        for (BBCodePlugin pln : plugins) {
          List<BBCodeData> codeDatas = pln.getBBCodeData();
          for (BBCodeData codeData : codeDatas) {
            BBCode bbCode = new BBCode();
            bbCode.setTagName(codeData.getTagName());
            bbCode.setReplacement(codeData.getReplacement());
            bbCode.setDescription(codeData.getDescription());
            bbCode.setExample(codeData.getExample());
            bbCode.setOption(Boolean.parseBoolean(codeData.getIsOption()));
            bbCode.setActive(Boolean.parseBoolean(codeData.getIsActive()));
            bbCodes.add(bbCode);
            if (log.isDebugEnabled()) {
              log.debug("Registered " + bbCode);
            }
          }

          managePlugin(pln);

        }

        if (!bbCodes.isEmpty()) {
          this.save(bbCodes);
        }

      }
    } finally {
      sProvider.close();
    }
  }

  /**
   * 
   * @param pln
   */
  private void managePlugin(BBCodePlugin pln) {
    try {
      if (context != null) {
        context.register(pln);
      } else {
        log.warn("No Management context is available for " + getClass());
      }
    } catch (Exception e) {
      log.error("Failed to register BBCode plugin " + pln.getName(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void save(List<BBCode> bbcodes) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node bbCodeHome = getBBcodeHome(sProvider);
      Node bbcNode;
      for (BBCode bbcode : bbcodes) {
        bbcNode = createNode(bbCodeHome, bbcode);
        bbcNode.setProperty("exo:tagName", bbcode.getTagName());
        bbcNode.setProperty("exo:replacement", bbcode.getReplacement());
        bbcNode.setProperty("exo:example", bbcode.getExample());
        bbcNode.setProperty("exo:description", bbcode.getDescription());
        bbcNode.setProperty("exo:isActive", bbcode.isActive());
        bbcNode.setProperty("exo:isOption", bbcode.isOption());
      }
      if (bbCodeHome.isNew()) {
        bbCodeHome.getSession().save();
      } else {
        bbCodeHome.save();
      }

      synchronized (activeBBCodesCache) {
        activeBBCodesCache.clear();
      }

    } catch (Exception e) {
      log.error("Error saving BBCodes", e);
    } finally {
      sProvider.close();
    }
  }

  private Node createNode(Node bbCodeHome, BBCode bbcode) throws Exception {
    Node bbcNode;
    String name = getNodeName(bbcode);
    try {
      bbcNode = bbCodeHome.getNode(bbcode.getId());
      if (!name.equals(bbcode.getId())) {
        bbcNode.remove();
        bbcNode = bbCodeHome.addNode(name, BBCODE_NODE_TYPE);
      }
    } catch (Exception e) {
      bbcNode = bbCodeHome.addNode(name, BBCODE_NODE_TYPE);
    }
    return bbcNode;
  }

  /**
   * create a suitable node name for a given bbcode
   * @param bbcode
   * @return
   */
  private String getNodeName(BBCode bbcode) {
    String id = bbcode.getTagName() + ((bbcode.isOption()) ? "=" : "");
    return id;
  }

  /**
   * {@inheritDoc}
   */
  private BBCode nodeToBBCode(Node bbcNode) throws Exception {
    BBCode bbCode = new BBCode();
    bbCode.setId(bbcNode.getName());
    bbCode.setTagName(bbcNode.getProperty("exo:tagName").getString());
    bbCode.setReplacement(bbcNode.getProperty("exo:replacement").getString());
    bbCode.setExample(bbcNode.getProperty("exo:example").getString());
    if (bbcNode.hasProperty("exo:description"))
      bbCode.setDescription(bbcNode.getProperty("exo:description").getString());
    bbCode.setActive(bbcNode.getProperty("exo:isActive").getBoolean());
    bbCode.setOption(bbcNode.getProperty("exo:isOption").getBoolean());
    return bbCode;
  }

  /**
   * {@inheritDoc}
   */
  public List<BBCode> getAll() throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    List<BBCode> bbcodes = new ArrayList<BBCode>();
    try {
      Node bbCodeHome = getBBcodeHome(sProvider);
      NodeIterator iter = bbCodeHome.getNodes();
      while (iter.hasNext()) {
        try {
          Node bbcNode = iter.nextNode();
          bbcodes.add(nodeToBBCode(bbcNode));
        } catch (Exception e) {
          log.error("Error loading BBCodes", e);
        }
      }
    } finally {
      sProvider.close();
    }
    return bbcodes;
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getActive() throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    if (activeBBCodesCache.isEmpty()) {
      try {
        Node bbCodeHome = getBBcodeHome(sProvider);
        if (bbCodeHome == null) {
          return activeBBCodesCache;
        }
        QueryManager qm = bbCodeHome.getSession().getWorkspace().getQueryManager();
        Query query = qm.createQuery(buildQueryBBCodeActive(bbCodeHome), Query.XPATH);
        QueryResult result = query.execute();
        NodeIterator iter = result.getNodes();
        String tagName = "";

        synchronized (activeBBCodesCache) {
          while (iter.hasNext()) {
            Node bbcNode = iter.nextNode();
            tagName = bbcNode.getName();
            activeBBCodesCache.add(tagName);
          }
        }

      } finally {
        sProvider.close();
      }
    }
    return activeBBCodesCache;
  }
  
  private String buildQueryBBCodeActive(Node bbCodeHome) throws RepositoryException {
    StringBuilder pathQuery = new StringBuilder();
    // new Query().path(bbCodeHome.getPath()).type(BBCODE_NODE_TYPE).predicate("@exo:isActive='true'").toString();
    pathQuery.append("/jcr:root").append(bbCodeHome.getPath()).append("/element(*,").append(BBCODE_NODE_TYPE).append(")[@exo:isActive='true']");
    return pathQuery.toString();
  }
  
  /**
   * {@inheritDoc}
   */
  public List<BBCode> getBBCodeActive() throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    List<BBCode> bbcodes = null;

    try {
      Node bbCodeHome = getBBcodeHome(sProvider);
      if (bbCodeHome == null) {
        return Collections.emptyList();
      }
      QueryManager qm = bbCodeHome.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(buildQueryBBCodeActive(bbCodeHome), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      
      bbcodes = new ArrayList<BBCode>((int)iter.getSize());

      while (iter.hasNext()) {
        try {
          Node bbcNode = iter.nextNode();
          bbcodes.add(nodeToBBCode(bbcNode));
          
        } catch (Exception e) {
          log.error("Error loading BBCodes", e);
        }
      }

    } finally {
      sProvider.close();
    }

    return bbcodes;
  }

  /**
   * {@inheritDoc}
   */
  public BBCode findById(String id) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    BBCode bbCode = new BBCode();
    Node bbcNode;
    try {
      Node bbCodeHome = getBBcodeHome(sProvider);
      try {
        bbcNode = bbCodeHome.getNode(id);
        bbCode = nodeToBBCode(bbcNode);
      } catch (Exception e) {
        log.error("Error loading BBCode" + id, e);
      }
    } finally {
      sProvider.close();
    }
    return bbCode;
  }

  /**
   * {@inheritDoc}
   */
  public void delete(String bbcodeId) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node bbCodeHome = getBBcodeHome(sProvider);
    try {
      bbCodeHome.getNode(bbcodeId).remove();
      bbCodeHome.save();
      synchronized (activeBBCodesCache) {
        activeBBCodesCache.clear();
      }
    } catch (Exception e) {
      log.error("Error removing BBCode" + bbcodeId, e);
    } finally {
      sProvider.close();
    }
  }

  public void start() {
    try {
      initDefaultBBCodes();
    } catch (Exception e) {
      log.error("Default BBCodes failed to initialize", e);
    }
  }

  public void stop() {
  }

  public void setContext(ManagementContext context) {
    this.context = context;
  }

  public KSDataLocation getDataLocator() {
    return dataLocator;
  }

  public void setDataLocator(KSDataLocation dataLocator) {
    this.dataLocator = dataLocator;
    this.sessionManager = dataLocator.getSessionManager();
  }

  public List<BBCodePlugin> getPlugins() {
    return plugins;
  }

  public void setPlugins(List<BBCodePlugin> plugins) {
    this.plugins = plugins;
  }
}
