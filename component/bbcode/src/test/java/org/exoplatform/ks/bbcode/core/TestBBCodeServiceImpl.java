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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.testing.AssertUtils;
import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.spi.BBCodeData;
import org.exoplatform.ks.bbcode.spi.BBCodePlugin;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.services.jcr.RepositoryService;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
@ConfiguredBy( { @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"), @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/bbcodes-configuration.xml") })
public class TestBBCodeServiceImpl extends AbstractJCRTestCase {

  private BBCodeServiceImpl bbcodeService;

  private String            bbcodesPath;

  @BeforeMethod
  protected void setUp() throws Exception {
    bbcodeService = new BBCodeServiceImpl();
    RepositoryService repos = getComponent(RepositoryService.class);
    KSDataLocation locator = getComponent(KSDataLocation.class);
    bbcodeService.setDataLocator(locator);

    bbcodesPath = bbcodeService.getDataLocator().getBBCodesLocation();
    addNode(bbcodesPath, BBCodeServiceImpl.BBCODE_HOME_NODE_TYPE); // create node /bbcodes
  }

  @AfterMethod
  protected void tearDown() throws Exception {
    deleteNode(bbcodesPath); // create node /bbcodes
  }

  @Override
  public void beforeContainerStart() {
    String tempDir = "target/temp" + System.nanoTime() + getClass().getCanonicalName();
    System.setProperty("gatein.test.tmp.dir", tempDir);
  }

  @Test
  public void testRegisterBBCodePlugin() throws Exception {
    BBCodePlugin plugin = new BBCodePlugin();
    plugin.setName("plugin1");
    bbcodeService.registerBBCodePlugin(plugin);
    List<BBCodePlugin> plugins = bbcodeService.getPlugins();
    assertEquals(1, plugins.size());
    assertEquals("plugin1", plugins.get(0).getName());

    // registerPlugin() adds elements (does not replace)
    BBCodePlugin plugin2 = new BBCodePlugin();
    plugin2.setName("plugin2");
    bbcodeService.registerBBCodePlugin(plugin2);
    List<BBCodePlugin> plugins2 = bbcodeService.getPlugins();
    assertEquals("BBCode plugins list size was not incremented", 2, plugins2.size());
    assertEquals("plugin2", plugins.get(1).getName());
  }

  @Test
  public void testInitDefaultBBCodes() throws Exception {

    BBCodePlugin plugin = new BBCodePlugin();
    plugin.setBbcodeData(Arrays.asList(new BBCodeData("foo", "bar", false, false)));
    bbcodeService.registerBBCodePlugin(plugin);
    bbcodeService.initDefaultBBCodes();
    String targetPath = bbcodesPath + "/" + "foo";
    assertNodeExists(targetPath);

  }

  @Test
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

  @Test
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

  @Test
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
    AssertUtils.assertContains(actual, "foo", "foo2=", "foo3", "foo4");
  }

  @Test
  public void testFindById() throws Exception {
    List<BBCode> bbcodes = Arrays.asList(createBBCode("foo", "replacement", "description", "example", true, true));
    bbcodeService.save(bbcodes);
    assertNodeExists(bbcodesPath + "/" + "foo=");
    BBCode actual = bbcodeService.findById("foo=");
    assertNotNull(actual);
    assertEquals("foo", actual.getTagName());
    assertEquals("replacement", actual.getReplacement());
    assertEquals("description", actual.getDescription());
    assertEquals("example", actual.getExample());
    assertEquals(true, actual.isOption());
    assertEquals(true, actual.isActive());
  }

  @Test
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

}
