/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.faq.test;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.log.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * Created by The eXo Platform SAS
 * @author : Hung nguyen
 *          hung.nguyen@exoplatform.com
 * May 7, 2008  
 */
public abstract class FAQServiceTestCase extends FAQTestCase {

  protected static Log          log = ExoLogger.getLogger("faq.test");  

  protected static RepositoryService   repositoryService;
  protected static StandaloneContainer container;
  
  protected final static String REPO_NAME = "repository".intern();
  protected final static String SYSTEM_WS = "system".intern();
  protected final static String KNOWLEDGE_WS = "knowledge".intern();
  protected static Node root_ = null;
  protected SessionProvider sessionProvider;
  private static SessionProviderService sessionProviderService = null;
  
  public FAQServiceTestCase() throws Exception {    
    initContainer();
    initJCR();
  }
  
  public void setUp() throws Exception {
    startSystemSession();
  }
  
  public void tearDown() throws Exception {

  }
  protected void startSystemSession() {
    sessionProvider = sessionProviderService.getSystemSessionProvider(null) ;
  }
  protected void startSessionAs(String user) {
    Identity identity = new Identity(user);
    ConversationState state = new ConversationState(identity);
    sessionProviderService.setSessionProvider(null, new SessionProvider(state));
    sessionProvider = sessionProviderService.getSessionProvider(null);
  }
  protected void endSession() {
    sessionProviderService.removeSessionProvider(null);
    startSystemSession();
  }
  
  
 
  private static void initContainer() {
    try {
      String containerConf = FAQServiceTestCase.class.getResource("/conf/portal/test-configuration.xml").toString();
      StandaloneContainer.addConfigurationURL(containerConf);
      container = StandaloneContainer.getInstance();      
      String loginConf = Thread.currentThread().getContextClassLoader().getResource("conf/portal/login.conf").toString();
      System.setProperty("java.security.auth.login.config", loginConf);
    }
    catch (Exception e) {
      log.error("Fail to init "+ container+" containner: ", e);
      throw new RuntimeException("Failed to initialize standalone container: ",e);
    }
  }

  private static void initJCR() {
    try {
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    
    // Initialize datas
    Session session = repositoryService.getCurrentRepository().getSystemSession(KNOWLEDGE_WS);
    root_ = session.getRootNode();   
    sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;   
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to initialize JCR: ",e);
    }
  }
  
}
