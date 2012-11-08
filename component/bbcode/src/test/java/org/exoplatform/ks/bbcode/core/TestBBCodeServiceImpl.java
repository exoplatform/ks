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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.bbcode.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.api.BBCodeService;
import org.exoplatform.ks.bbcode.spi.BBCodeData;
import org.exoplatform.ks.bbcode.spi.BBCodePlugin;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.SessionManager;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.bbcode.component.core.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.bbcode.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.bbcode.test.portal-configuration.xml")
})

public class TestBBCodeServiceImpl extends AbstractKernelTest {

  private BBCodeServiceImpl bbcodeServiceImpl;

  private BBCodeService     bbcodeService;

  private KSDataLocation    locator;

  private SessionManager    sessionManager;

  private String            bbcodesPath;
  @Override
  public void setUp() throws Exception {
    super.setUp();
    //
    begin();
    
    bbcodeServiceImpl = new BBCodeServiceImpl();
    locator = getService(KSDataLocation.class);
    bbcodeService = getService(BBCodeService.class);
    
    bbcodeServiceImpl.setDataLocator(locator);
    sessionManager = locator.getSessionManager();
    bbcodesPath = bbcodeServiceImpl.getDataLocator().getBBCodesLocation();
    
  }

  @Override
  public void tearDown() throws Exception {
    for (BBCode bbcode : bbcodeService.getAll()) {
      bbcodeService.delete(bbcode.getId());
    }
    //
    end();
  }
  
  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }
  
  

  public void testRegisterBBCodePlugin() throws Exception {
    BBCodePlugin plugin = new BBCodePlugin();
    plugin.setName("plugin1");
    bbcodeServiceImpl.registerBBCodePlugin(plugin);
    List<BBCodePlugin> plugins = bbcodeServiceImpl.getPlugins();
    assertEquals(1, plugins.size());
    assertEquals("plugin1", plugins.get(0).getName());

    // registerPlugin() adds elements (does not replace)
    BBCodePlugin plugin2 = new BBCodePlugin();
    plugin2.setName("plugin2");
    bbcodeServiceImpl.registerBBCodePlugin(plugin2);
    List<BBCodePlugin> plugins2 = bbcodeServiceImpl.getPlugins();
    assertEquals("BBCode plugins list size was not incremented", 2, plugins2.size());
    assertEquals("plugin2", plugins.get(1).getName());
  }


  public void testInitDefaultBBCodes() throws Exception {

    BBCodePlugin plugin = new BBCodePlugin();
    plugin.setBbcodeData(Arrays.asList(new BBCodeData("foo", "bar", false, false)));
    bbcodeServiceImpl.registerBBCodePlugin(plugin);
    bbcodeServiceImpl.initDefaultBBCodes();
    String targetPath = bbcodesPath + "/" + "foo";
    assertNodeExists(targetPath);

  }


  public void testSave() throws Exception {
    List<BBCode> bbcodes = Arrays.asList(createBBCode("foo", "replacement", "description", "example", false, false));
    bbcodeService.save(bbcodes);
    String targetPath = bbcodesPath + "/" + "foo";
    assertNodeExists(targetPath);
    Node n = getNode(targetPath);
    assertEquals("foo", n.getProperty("exo:tagName").getString());
    assertEquals("replacement", n.getProperty("exo:replacement").getString());
    assertEquals("description", n.getProperty("exo:description").getString());
    assertEquals("example", n.getProperty("exo:example").getString());
    assertEquals(false, n.getProperty("exo:isOption").getBoolean());
    assertEquals(false, n.getProperty("exo:isActive").getBoolean());
  }


  public void testGetAll() throws Exception {
    List<BBCode> bbcodes = new ArrayList<BBCode>();
    bbcodes.add(createBBCode("foo", "replacement", "description", "example", false, true));
    bbcodes.add(createBBCode("foo", "replacement", "description", "example", true, false));
    bbcodes.add(createBBCode("foo2", "replacement", "description", "example", false, true));
    bbcodes.add(createBBCode("foo3", "replacement", "description", "example", false, true));
    bbcodes.add(createBBCode("foo4", "replacement", "description", "example", false, true));
    bbcodeService.save(bbcodes);
    List<BBCode> actual = bbcodeService.getAll();
    assertEquals(bbcodes.size(), actual.size());
  }


  public void testGetActive() throws Exception {
    List<BBCode> bbcodes = new ArrayList<BBCode>();
    bbcodes.add(createBBCode("foo", "replacement", "description", "example", false, true));
    bbcodes.add(createBBCode("foo", "replacement", "description", "example", true, false));
    bbcodes.add(createBBCode("foo2", "replacement", "description", "example", true, true));
    bbcodes.add(createBBCode("foo3", "replacement", "description", "example", false, true));
    bbcodes.add(createBBCode("foo4", "replacement", "description", "example", false, true));
    bbcodeService.save(bbcodes);
    List<String> actual = bbcodeService.getActive();
    assertEquals(bbcodes.size() - 1, actual.size());
    assertContains(actual, "foo", "foo2=", "foo3", "foo4");
  }


  public void testFindById() throws Exception {
    List<BBCode> bbcodes = Arrays.asList(createBBCode("foo", "replacement", "description", "example", true, true));
    bbcodeService.save(bbcodes);
    assertNodeExists(bbcodesPath + "/" + "foo=");
    BBCode actual = bbcodeServiceImpl.findById("foo=");
    assertNotNull(actual);
    assertEquals("foo", actual.getTagName());
    assertEquals("replacement", actual.getReplacement());
    assertEquals("description", actual.getDescription());
    assertEquals("example", actual.getExample());
    assertEquals(true, actual.isOption());
    assertEquals(true, actual.isActive());
  }


  public void testDelete() throws Exception {
    List<BBCode> bbcodes = Arrays.asList(createBBCode("foo", "replacement", "description", "example", false, false));
    bbcodeService.save(bbcodes);
    String targetPath = bbcodesPath + "/" + "foo";
    assertNodeExists(targetPath);
    bbcodeService.delete("foo");
    assertNodeNotExists(targetPath);
  }

  private BBCode createBBCode(String tag, String replacement, String description, String example, boolean option, boolean active) {
    BBCode bbc = new BBCode();
    bbc.setTagName(tag);
    bbc.setReplacement(replacement);
    bbc.setDescription(description);
    bbc.setExample(example);
    bbc.setOption(option);
    bbc.setActive(active);
    return bbc;
  }
  
  private Session getSession() {
    Session session = sessionManager.getCurrentSession();
    if (session == null) {
      session = sessionManager.openSession();
    }
    return session;
  }
  
  private void assertNodeExists(String path) {
    Session session = getSession();
    if(path.indexOf("/") == 0) path = path.substring(0);
    try {
      session.getRootNode().getNode(path);
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    } finally{
      session.logout();
    }
  }

  private void assertNodeNotExists(String path) {
    Session session = getSession();
    if(path.indexOf("/") == 0) path = path.substring(0);
    try {
      session.getRootNode().getNode(path);
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(true);
    } finally{
      session.logout();
    }
  }
  
  private Node getNode(String path) throws Exception {
    return getSession().getRootNode().getNode(path);
  }
  
  private void assertContains(List<String> actual, String... strs) {
    for (int i = 0; i < strs.length; i++) {
      if(!actual.contains(strs[i])) {
        assertTrue(false);
      }
    }
    assertTrue(true);
  }
}
