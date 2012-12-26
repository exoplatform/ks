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
import org.exoplatform.ks.bbcode.api.BBCodeService;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.services.jcr.RepositoryService;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Oct 4, 2012  
 */
@ConfiguredBy({ 
                @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"), 
                @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/bbcodes-configuration.xml") })
public class TestBBcodeCacheService extends AbstractJCRTestCase {

  private BBCodeService bbcodeService;

  private String            bbcodesPath;
  KSDataLocation locator = null;

  @BeforeMethod
  protected void setUp() throws Exception {
    RepositoryService repos = getComponent(RepositoryService.class);
    locator = new KSDataLocation(getWorkspace(), repos);
   
    
    BBCodeServiceImpl impl = getComponent(BBCodeServiceImpl.class);
    impl.setDataLocator(locator);
    bbcodeService = getComponent(BBCodeService.class);
    
    bbcodesPath = locator.getBBCodesLocation();
    addNode(bbcodesPath, BBCodeServiceImpl.BBCODE_HOME_NODE_TYPE); // create node /bbcodes
  }

  @AfterMethod
  protected void tearDown() throws Exception {
    deleteNode(bbcodesPath); // create node /bbcodes
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
