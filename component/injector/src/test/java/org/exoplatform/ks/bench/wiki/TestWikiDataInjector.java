/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.ks.bench.wiki;

import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import junit.framework.TestCase;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.ks.bench.WikiDataInjector;
import org.exoplatform.ks.bench.WikiDataInjector.CONSTANTS;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.WikiService;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * May 24, 2012  
 */
public class TestWikiDataInjector extends TestCase {

  private static StandaloneContainer container;

  private final static String        KNOWLEDGE_WS = "knowledge".intern();

  private WikiDataInjector           injector;
  
  private WikiService wikiService;

  protected void begin() {
    RequestLifeCycle.begin(container);
  }

  protected void end() {
    RequestLifeCycle.end();
  }

  protected void setUp() throws Exception {
    initContainer();
    initJCR();
    begin();
    Identity systemIdentity = new Identity(IdentityConstants.SYSTEM);
    ConversationState.setCurrent(new ConversationState(systemIdentity));
    this.wikiService = (WikiService) container.getComponentInstanceOfType(WikiService.class);
    this.injector = new WikiDataInjector(wikiService, null);    
  }

  protected void tearDown() throws Exception {
    end();
  }

  private static void initContainer() {
    try {
      String containerConf = Thread.currentThread().getContextClassLoader().getResource("conf/standalone/configuration.xml").toString();
      StandaloneContainer.addConfigurationURL(containerConf);
      //
      String loginConf = Thread.currentThread().getContextClassLoader().getResource("conf/standalone/login.conf").toString();
      System.setProperty("java.security.auth.login.config", loginConf);
      //
      container = StandaloneContainer.getInstance();
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize standalone container: " + e.getMessage(), e);
    }
  }
  
  private static void initJCR() {
    try {
      RepositoryService  repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
      // Initialize datas
      Session session = repositoryService.getCurrentRepository().getSystemSession(KNOWLEDGE_WS);
      // Remove old data before to starting test case.
      StringBuffer stringBuffer = new StringBuffer();
      stringBuffer.append("/jcr:root").append("//*[fn:name() = 'eXoWiki' or fn:name() = 'ApplicationData']");
      QueryManager qm = session.getWorkspace().getQueryManager();
      Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      while (iter.hasNext()) {
        Node node = iter.nextNode();
        try {
          node.remove();
        } catch (Exception e) {}
      }
      session.save();
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize JCR: ", e);
    }
  }
  
  private HashMap<String, String> createInjectPageParam(String quantity,
                                                        String prefix,
                                                        String totalPage,
                                                        String attSize,
                                                        String wikiOwner,
                                                        String wikiType) {
    HashMap<String, String> queryParams = new HashMap<String, String>();
    queryParams.put(CONSTANTS.TYPE.getName(), CONSTANTS.DATA.getName());
    queryParams.put(WikiDataInjector.QUANTITY, quantity);
    queryParams.put(WikiDataInjector.PREFIX, prefix);
    queryParams.put(WikiDataInjector.PAGE_SIZE, totalPage);
    queryParams.put(WikiDataInjector.ATTACH_SIZE, attSize);
    queryParams.put(WikiDataInjector.WIKI_OWNER, wikiOwner);
    queryParams.put(WikiDataInjector.WIKI_TYPE, wikiType);
    return queryParams;
  }
  
  private HashMap<String, String> createRejectPageParam(String quantity,
                                                        String prefix,
                                                        String wikiOwner,
                                                        String wikiType) {
    HashMap<String, String> queryParams = new HashMap<String, String>();
    queryParams.put(CONSTANTS.TYPE.getName(), CONSTANTS.DATA.getName());
    queryParams.put(WikiDataInjector.QUANTITY, quantity);
    queryParams.put(WikiDataInjector.PREFIX, prefix);
    queryParams.put(WikiDataInjector.WIKI_OWNER, wikiOwner);
    queryParams.put(WikiDataInjector.WIKI_TYPE, wikiType);
    return queryParams;
  }
  
  public void testInjectData() throws Exception {
    PageImpl wikiHome = (PageImpl) wikiService.getPageById(PortalConfig.PORTAL_TYPE, "classic", null);
    HashMap<String, String> injectParams = createInjectPageParam("2", "a", "100", "100", "classic", PortalConfig.PORTAL_TYPE);
    injector.inject(injectParams);
    assertTrue(injector.getPagesByPrefix("a", wikiHome).size() == 2);
    
    injectParams = createInjectPageParam("2,3", "a,b", "100", "100", "classic", PortalConfig.PORTAL_TYPE);
    injector.inject(injectParams);
    org.chromattic.api.query.QueryResult<PageImpl> iter = injector.getPagesByPrefix("a", wikiHome);
    assertTrue(iter.size() == 2);
    assertTrue(injector.getPagesByPrefix("b", iter.next()).size() == 3);
    
    injectParams = createInjectPageParam("1,2", "c,b", "100", "100", "classic", PortalConfig.PORTAL_TYPE);
    injector.inject(injectParams);
    iter = injector.getPagesByPrefix("c", wikiHome);
    assertTrue(injector.getPagesByPrefix("b", iter.next()).size() == 2);
    
    iter = injector.getPagesByPrefix("a", wikiHome);
    assertTrue(injector.getPagesByPrefix("b", iter.next()).size() == 3);
  }
  
  public void testRejectData() throws Exception {
    PageImpl wikiHome = (PageImpl) wikiService.getPageById(PortalConfig.PORTAL_TYPE, "classic", null);
    HashMap<String, String> injectParams = createInjectPageParam("2", "e", "100", "100", "classic", PortalConfig.PORTAL_TYPE);
    injector.inject(injectParams);
    assertTrue(injector.getPagesByPrefix("e", wikiHome).size() == 2);
    HashMap<String, String> rejectParams = createRejectPageParam("2", "e", "classic", PortalConfig.PORTAL_TYPE);
    injector.reject(rejectParams);
    assertTrue(injector.getPagesByPrefix("e", wikiHome).size() == 0);
  }

}
