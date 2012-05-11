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
package org.exoplatform.wiki.service;


import java.util.HashMap;
import java.util.List;

import org.chromattic.api.ChromatticSession;
import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.AbstractMOWTestcase;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWiki;
import org.exoplatform.wiki.mow.core.api.wiki.MovedMixin;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.service.search.TemplateSearchData;
import org.exoplatform.wiki.service.search.TemplateSearchResult;
import org.exoplatform.wiki.service.search.TitleSearchResult;
import org.exoplatform.wiki.service.search.WikiSearchData;
import org.xwiki.rendering.syntax.Syntax;


public class TestWikiService extends AbstractMOWTestcase {
  private WikiService wService ; 
  public void setUp() throws Exception{
    super.setUp() ;
    wService = (WikiService)container.getComponentInstanceOfType(WikiService.class) ;    
  }
  
  public void testWikiService() throws Exception{
    assertNotNull(wService) ;
  }
  
  public void testGetPortalPageById() throws Exception{
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    PortalWiki wikiACME = portalWikiContainer.addWiki("acme");
    wikiACME.getWikiHome() ;
    WikiHome wikiHomePage = wiki.getWikiHome();
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "WikiHome")) ;    
        
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("testGetPortalPageById-001");
    wikiHomePage.addWikiPage(wikipage);
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "testGetPortalPageById-001")) ;    
    
  }
  
  public void testGetGroupPageById() throws Exception{
    
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<GroupWiki> groupWikiContainer = wStore.getWikiContainer(WikiType.GROUP);
    GroupWiki wiki = groupWikiContainer.addWiki("platform/users");
    WikiHome wikiHomePage = wiki.getWikiHome();
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.GROUP_TYPE, "platform/users", "WikiHome")) ;
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("testGetGroupPageById-001");
    wikiHomePage.addWikiPage(wikipage);
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.GROUP_TYPE, "platform/users", "testGetGroupPageById-001")) ;    
    assertNull(wService.getPageById(PortalConfig.GROUP_TYPE, "unknown", "WikiHome"));    
    
  }
  
  public void testGetUserPageById() throws Exception{
    
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
    UserWiki wiki = userWikiContainer.addWiki("john");
    WikiHome wikiHomePage = wiki.getWikiHome();
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.USER_TYPE, "john", "WikiHome")) ;
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("testGetUserPageById-001");
    wikiHomePage.addWikiPage(wikipage);
    model.save() ;
    
    assertNotNull(wService.getPageById(PortalConfig.USER_TYPE, "john", "testGetUserPageById-001")) ;
    assertNull(wService.getPageById(PortalConfig.USER_TYPE, "unknown", "WikiHome"));
    
  }
  
  public void testCreatePageAndSubPage() throws Exception{    
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "parentPage", "WikiHome") ;
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "parentPage")) ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "childPage", "parentPage") ;
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "childPage")) ;
  }

  public void testCreateTemplatePage() throws Exception {
    WikiPageParams params = new WikiPageParams(PortalConfig.PORTAL_TYPE, "classic", null);
    wService.createTemplatePage("SampleChart", params);
    assertNotNull(wService.getTemplatePage(params, "SampleChart"));
  }
  
  public void testGetBreadcumb() throws Exception {
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "Breadcumb1", "WikiHome") ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "Breadcumb2", "Breadcumb1") ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "Breadcumb3", "Breadcumb2") ;
    List<BreadcrumbData> breadCumbs = wService.getBreadcumb(PortalConfig.PORTAL_TYPE, "classic", "Breadcumb3");
    assertEquals(4, breadCumbs.size());
    assertEquals("WikiHome", breadCumbs.get(0).getId());
    assertEquals("Breadcumb1", breadCumbs.get(1).getId());
    assertEquals("Breadcumb2", breadCumbs.get(2).getId());
    assertEquals("Breadcumb3", breadCumbs.get(3).getId());
    wService.createPage(PortalConfig.GROUP_TYPE, "platform/users", "GroupBreadcumb1", "WikiHome") ;
    wService.createPage(PortalConfig.GROUP_TYPE, "platform/users", "GroupBreadcumb2", "GroupBreadcumb1") ;
    wService.createPage(PortalConfig.GROUP_TYPE, "platform/users", "GroupBreadcumb3", "GroupBreadcumb2") ;
    breadCumbs = wService.getBreadcumb(PortalConfig.GROUP_TYPE, "platform/users", "GroupBreadcumb3");
    assertEquals(4, breadCumbs.size());
    assertEquals("WikiHome", breadCumbs.get(0).getId());
    assertEquals("GroupBreadcumb1", breadCumbs.get(1).getId());
    assertEquals("GroupBreadcumb2", breadCumbs.get(2).getId());
    assertEquals("GroupBreadcumb3", breadCumbs.get(3).getId());
    wService.createPage(PortalConfig.USER_TYPE, "john", "UserBreadcumb1", "WikiHome") ;
    wService.createPage(PortalConfig.USER_TYPE, "john", "UserBreadcumb2", "UserBreadcumb1") ;
    wService.createPage(PortalConfig.USER_TYPE, "john", "UserBreadcumb3", "UserBreadcumb2") ;
    breadCumbs = wService.getBreadcumb(PortalConfig.USER_TYPE, "john", "UserBreadcumb3");
    assertEquals(4, breadCumbs.size());
    assertEquals("WikiHome", breadCumbs.get(0).getId());
    assertEquals("UserBreadcumb1", breadCumbs.get(1).getId());
    assertEquals("UserBreadcumb2", breadCumbs.get(2).getId());
    assertEquals("UserBreadcumb3", breadCumbs.get(3).getId());
  }
  
  public void testMovePage() throws Exception{    
    
    //moving page in same space
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "oldParent", "WikiHome") ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "child", "oldParent") ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "newParent", "WikiHome") ;
    
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "oldParent")) ;
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "child")) ;
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "newParent")) ;
    
    WikiPageParams currentLocationParams= new WikiPageParams();
    WikiPageParams newLocationParams= new WikiPageParams();
    currentLocationParams.setPageId("child");
    currentLocationParams.setType(PortalConfig.PORTAL_TYPE);
    currentLocationParams.setOwner("classic");
    newLocationParams.setPageId("newParent");
    newLocationParams.setType(PortalConfig.PORTAL_TYPE);
    newLocationParams.setOwner("classic");    
    assertTrue(wService.movePage(currentLocationParams,newLocationParams)) ;      
    
    //moving page from different spaces
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
    UserWiki wiki = userWikiContainer.addWiki("demo");
    wiki.getWikiHome();
    model.save() ;
    
    wService.createPage(PortalConfig.USER_TYPE, "demo", "acmePage", "WikiHome") ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "classicPage", "WikiHome") ;
    
    assertNotNull(wService.getPageById(PortalConfig.USER_TYPE, "demo", "acmePage")) ;
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "classicPage")) ;
    
    currentLocationParams.setPageId("acmePage");
    currentLocationParams.setType(PortalConfig.USER_TYPE);
    currentLocationParams.setOwner("demo");
    newLocationParams.setPageId("classicPage");
    newLocationParams.setType(PortalConfig.PORTAL_TYPE);
    newLocationParams.setOwner("classic");   
    assertTrue(wService.movePage(currentLocationParams,newLocationParams)) ;
    
    // moving a page to another read-only page
    wService.createPage(PortalConfig.PORTAL_TYPE, "demo", "toMovedPage", "WikiHome");
    PageImpl page = (PageImpl) wService.createPage(PortalConfig.USER_TYPE, "demo", "privatePage", "WikiHome");
    HashMap<String, String[]> permissionMap = new HashMap<String, String[]>();
    permissionMap.put("any", PermissionType.DEFAULT_AC);
    page.setPermission(permissionMap);
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "demo", "toMovedPage"));
    assertNotNull(wService.getPageById(PortalConfig.USER_TYPE, "demo", "privatePage"));

    currentLocationParams.setPageId("toMovedPage");
    currentLocationParams.setType(PortalConfig.PORTAL_TYPE);
    currentLocationParams.setOwner("demo");
    newLocationParams.setPageId("privatePage");
    newLocationParams.setType(PortalConfig.USER_TYPE);
    newLocationParams.setOwner("demo");
    startSessionAs("mary");
    assertFalse(wService.movePage(currentLocationParams, newLocationParams));
  }
  
  public void testAddMixin() throws Exception{
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "mixinPage", "WikiHome") ;
    PageImpl page = (PageImpl)wService.getPageById("portal", "classic", "mixinPage") ;
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    ChromatticSession session = wStore.getSession() ;
    MovedMixin mix = session.create(MovedMixin.class) ;
    session.setEmbedded(page, MovedMixin.class, mix) ;
    assertSame(mix, page.getMovedMixin()) ;    
    assertSame(page, mix.getEntity()) ;
  }
  
  public void testDeletePage() throws Exception{    
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "deletePage", "WikiHome") ;
    assertTrue(wService.deletePage(PortalConfig.PORTAL_TYPE, "classic", "deletePage")) ;
    //wait(10) ;
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "deletePage", "WikiHome") ;
    assertTrue(wService.deletePage(PortalConfig.PORTAL_TYPE, "classic", "deletePage")) ;    
    assertNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "deletePage")) ; 
    assertFalse(wService.deletePage(PortalConfig.PORTAL_TYPE, "classic", "WikiHome")) ;
  }
  
  public void testDeleteTemplatePage() throws Exception{
    Model model = mowService.getModel();
    WikiPageParams params = new WikiPageParams(PortalConfig.PORTAL_TYPE, "classic", null);
    wService.createTemplatePage("SampleExcel", params);
    model.save();
    assertNotNull(wService.getTemplatePage(params, "SampleExcel"));
    wService.deleteTemplatePage(PortalConfig.PORTAL_TYPE, "classic", "SampleExcel");
    model.save();
    assertNull(wService.getTemplatePage(params, "SampleExcel"));
  }
  
  public void testRenamePage() throws Exception{    
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "currentPage", "WikiHome") ;
    assertTrue(wService.renamePage(PortalConfig.PORTAL_TYPE, "classic", "currentPage", "renamedPage", "renamedPage")) ;
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "renamedPage")) ;  
    
  }
  
  public void testSearchRenamedPage() throws Exception{    
    PageImpl page = (PageImpl) wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "Page", "WikiHome");
    page.getContent().setText("This is a rename page test");
    assertTrue(wService.renamePage(PortalConfig.PORTAL_TYPE, "classic", "Page", "Page01", "Page01"));
    assertEquals(1, wService.searchRenamedPage(PortalConfig.PORTAL_TYPE, "classic", "Page").size());

    PageImpl guestPage = (PageImpl) wService.createPage(PortalConfig.GROUP_TYPE, "/platform/guests", "Page", "WikiHome");
    guestPage.getContent().setText("This is a rename guest page test");
    assertTrue(wService.renamePage(PortalConfig.GROUP_TYPE, "/platform/guests", "Page", "Page01", "Page01"));
    assertEquals(1, wService.searchRenamedPage(PortalConfig.GROUP_TYPE, "/platform/guests", "Page").size());
    
    PageImpl demoPage = (PageImpl) wService.createPage(PortalConfig.USER_TYPE, "demo", "Page", "WikiHome");
    demoPage.getContent().setText("This is a rename demo page test");
    assertTrue(wService.renamePage(PortalConfig.USER_TYPE, "demo", "Page", "Page01", "Page01"));
    assertEquals(1, wService.searchRenamedPage(PortalConfig.USER_TYPE, "demo", "Page").size());
  }
  
  public void testSearchContent() throws Exception {

    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
    WikiContainer<PortalWiki> portalContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki portalWiki = portalContainer.addWiki("ext");
    UserWiki userWiki = userWikiContainer.addWiki("demo");
    portalWiki.getWikiHome();
    userWiki.getWikiHome();

    PageImpl kspage = (PageImpl) wService.createPage(PortalConfig.PORTAL_TYPE,
                                                     "classic",
                                                     "knowledge suite",
                                                     "WikiHome");
    kspage.getContent().setText("forum faq wiki");

    PageImpl ksExtpage = (PageImpl) wService.createPage(PortalConfig.PORTAL_TYPE,
                                                        "ext",
                                                        "knowledge suite",
                                                        "WikiHome");
    ksExtpage.getContent().setText("forum faq wiki");

    PageImpl ksSocialpage = (PageImpl) wService.createPage(PortalConfig.USER_TYPE,
                                                           "demo",
                                                           "knowledge suite",
                                                           "WikiHome");
    ksSocialpage.getContent().setText("forum faq wiki");

    PageImpl cspage = (PageImpl) wService.createPage(PortalConfig.PORTAL_TYPE,
                                                     "classic",
                                                     "collaboration suite",
                                                     "WikiHome");
    cspage.getContent().setText("calendar mail contact chat");
    
    PageImpl guestPage = (PageImpl) wService.createPage(PortalConfig.GROUP_TYPE,
                                                     "/platform/guests",
                                                     "Guest page",
                                                     "WikiHome");
    guestPage.getContent().setText("Playground");

    guestPage.getChromatticSession().save();
    // fulltext search
    WikiSearchData data = new WikiSearchData("suite", null, null, "portal", "classic");
    PageList<SearchResult> result = wService.searchContent(data);
    assertEquals(2, result.getAll().size());

    data = new WikiSearchData("suite", null, null, "portal", null);

    result = wService.searchContent(data);
    assertEquals(3, result.getAll().size());

    data = new WikiSearchData("suite", null, null, null, null);
    result = wService.searchContent(data);
    assertEquals(4, result.getAll().size());

    // title search
    data = new WikiSearchData(null, "knowledge", null, "portal", "classic");
    result = wService.searchContent(data);
    assertEquals(1, result.getAll().size());

    data = new WikiSearchData(null, "collaboration", null, "portal", "classic");
    result = wService.searchContent(data);
    assertEquals(1, result.getAll().size());

    data = new WikiSearchData(null, "knowledge", null, "portal", null);
    result = wService.searchContent(data);
    assertEquals(2, result.getAll().size());

    data = new WikiSearchData(null, "knowledge", null, null, null);
    result = wService.searchContent(data);
    assertEquals(3, result.getAll().size());

    // content search
    data = new WikiSearchData(null, null, "forum", "portal", "classic");
    result = wService.searchContent(data);
    assertEquals(1, result.getAll().size());

    data = new WikiSearchData(null, null, "calendar", "portal", "classic");
    result = wService.searchContent(data);
    assertEquals(1, result.getAll().size());

    data = new WikiSearchData(null, null, "forum", "portal", null);
    result = wService.searchContent(data);
    assertEquals(2, result.getAll().size());

    data = new WikiSearchData(null, null, "forum", null, null);
    result = wService.searchContent(data);
    assertEquals(3, result.getAll().size());
    
    data = new WikiSearchData("Playground", null, null, PortalConfig.GROUP_TYPE, "/platform/guests");
    result = wService.searchContent(data);
    assertEquals(1, result.getAll().size());
    
    data = new WikiSearchData("forum", null, null, PortalConfig.USER_TYPE, null);
    result = wService.searchContent(data);
    assertEquals(1, result.getAll().size());
    
    data = new WikiSearchData("forum", null, null, PortalConfig.USER_TYPE, "demo");
    result = wService.searchContent(data);
    assertEquals(1, result.getAll().size());

    // content & title search
    //TODO: search on many node types with AND operator
    /*data = new ContentSearchData(null, "suite", "forum", "portal", "classic");
    result = wService.searchContent(data);
    assertEquals(1, result.getAll().size());

    data = new ContentSearchData(null, "suite", "forum", "portal", null);
    result = wService.searchContent(data);
    assertEquals(2, result.getAll().size());

    data = new ContentSearchData(null, "suite", "forum", null, null);
    result = wService.searchContent(data);
    assertEquals(3, result.getAll().size());*/

  }
  
  public void testSearch() throws Exception {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalContainer = wStore.getWikiContainer(WikiType.PORTAL);
    portalContainer.addWiki("ext");
    
    PageImpl kspage = (PageImpl)wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "knowledge", "WikiHome") ;
    kspage.getContent().setText("forum faq wiki exoplatform") ;
    
    PageImpl ksExtPage = (PageImpl)wService.createPage(PortalConfig.PORTAL_TYPE, "ext", "knowledge ext", "WikiHome") ;
    ksExtPage.getContent().setText("forum faq wiki exoplatform") ;
    
    AttachmentImpl attachment1 = kspage.createAttachment("attachment1.txt", Resource.createPlainText("foo")) ;
    attachment1.setCreator("you") ;    
    assertEquals(attachment1.getName(), "attachment1.txt") ;
    assertNotNull(attachment1.getContentResource()) ;
    attachment1.setContentResource(Resource.createPlainText("exoplatform content mamagement")) ;
    
    PageImpl guestPage = (PageImpl) wService.createPage(PortalConfig.GROUP_TYPE, "/platform/guests", "guest platform", "WikiHome");
    guestPage.getContent().setText("exoplatform");
    
    PageImpl userPage = (PageImpl) wService.createPage(PortalConfig.USER_TYPE, "demo", "demo", "WikiHome");
    userPage.getContent().setText("exoplatform");
    
    kspage.getChromatticSession().save();
    WikiSearchData data = new WikiSearchData("exoplatform", null, null, null, null);

    PageList<SearchResult> result = wService.search(data);
    assertEquals(5, result.getAll().size());
    
    data = new WikiSearchData("exoplatform", null, null,"portal",null) ;
    result = wService.search(data) ;
    assertEquals(3, result.getAll().size()) ;
    
    data = new WikiSearchData("exoplatform", null, null, "portal", "classic");

    result = wService.search(data);
    assertEquals(2, result.getAll().size());    
    
    data = new WikiSearchData("exoplatform", null, null, PortalConfig.GROUP_TYPE, null);
    result = wService.search(data);
    assertEquals(1, result.getAll().size());
    
    data = new WikiSearchData("exoplatform", null, null, PortalConfig.GROUP_TYPE, "/platform/guests");
    result = wService.search(data);
    assertEquals(1, result.getAll().size());
    
    data = new WikiSearchData("exoplatform", null, null, PortalConfig.USER_TYPE, null);
    result = wService.search(data);
    assertEquals(1, result.getAll().size());
    
    data = new WikiSearchData("exoplatform", null, null, PortalConfig.USER_TYPE, "demo");
    result = wService.search(data);
    assertEquals(1, result.getAll().size());
      
  }
  
  public void testSearchTitle() throws Exception {    
    PageImpl kspage = (PageImpl) wService.createPage(PortalConfig.PORTAL_TYPE,
                                                     "classic",
                                                     "dumpPage",
                                                     "WikiHome");
    assertNotNull(wService.getPageById(PortalConfig.PORTAL_TYPE, "classic", "dumpPage"));
    wService.createPage(PortalConfig.GROUP_TYPE, "/platform/guests", "Dump guest Page", "WikiHome");
    
    wService.createPage(PortalConfig.USER_TYPE, "demo", "Dump demo Page", "WikiHome");

    AttachmentImpl attachment1 = kspage.createAttachment("dumpFile.txt",
                                                         Resource.createPlainText("foo"));
    assertEquals(attachment1.getName(), "dumpFile.txt");
    assertNotNull(attachment1.getContentResource());
    kspage.getChromatticSession().save();
    // limit size is 2
    WikiSearchData data = new WikiSearchData(null, "dump", null, null, null);
    data.setLimit(2);
    List<TitleSearchResult> result = wService.searchDataByTitle(data);
    assertEquals(2, result.size());
    // limit size is 10
    data.setLimit(10);
    result = wService.searchDataByTitle(data);
    assertEquals(4, result.size());
    // not limit size
    data= new WikiSearchData(null, "dump", null, "portal", "classic");
    result = wService.searchDataByTitle(data);
    assertEquals(2, result.size());
    
    data = new WikiSearchData(null, "dump", null, PortalConfig.GROUP_TYPE, null);
    result = wService.searchDataByTitle(data);
    assertEquals(1, result.size());

    data = new WikiSearchData(null, "dump", null, PortalConfig.GROUP_TYPE, "/platform/guests");
    result = wService.searchDataByTitle(data);
    assertEquals(1, result.size());
    
    data = new WikiSearchData(null, "dump", null, PortalConfig.USER_TYPE, null);
    result = wService.searchDataByTitle(data);
    assertEquals(1, result.size());

    data = new WikiSearchData(null, "dump", null, PortalConfig.USER_TYPE, "demo");
    result = wService.searchDataByTitle(data);
    assertEquals(1, result.size());
    
    
  }
  
  public void testSearchTemplate() throws Exception {    
    WikiPageParams params= new WikiPageParams(PortalConfig.PORTAL_TYPE,  "classic", null);
    wService.createTemplatePage("Sample Search Template", params);
    assertNotNull(wService.getTemplatePage(params, "Sample_Search_Template"));
    
    params= new WikiPageParams(PortalConfig.GROUP_TYPE,  "/platform/guests", null);
    wService.createTemplatePage("Sample Group Search Template", params);
    assertNotNull(wService.getTemplatePage(params, "Sample_Group_Search_Template"));
    
    params= new WikiPageParams(PortalConfig.USER_TYPE,  "demo", null);
    wService.createTemplatePage("Sample User Search Template", params);
    assertNotNull(wService.getTemplatePage(params, "Sample_User_Search_Template"));
    
    TemplateSearchData data = new TemplateSearchData("Template",PortalConfig.PORTAL_TYPE,  "classic");
    List<TemplateSearchResult> result = wService.searchTemplate(data);
    assertEquals(1, result.size());
    
    data = new TemplateSearchData("Template", PortalConfig.GROUP_TYPE, "/platform/guests");
    result = wService.searchTemplate(data);
    assertEquals(1, result.size());
    
    data = new TemplateSearchData("Template", PortalConfig.USER_TYPE, "demo");
    result = wService.searchTemplate(data);
    assertEquals(1, result.size());
  }
  
  public void testGetPageTitleOfAttachment() throws Exception {
    PageImpl kspage = (PageImpl)wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "GetPageTitleOfAttachment", "WikiHome") ;
    kspage.getContent().setText("forum faq wiki exoplatform") ;
    AttachmentImpl attachment1 = kspage.createAttachment("attachment1.txt", Resource.createPlainText("foo")) ;
    attachment1.setCreator("you") ;    
    assertEquals(attachment1.getName(), "attachment1.txt") ;
    assertNotNull(attachment1.getContentResource()) ;
    attachment1.setContentResource(Resource.createPlainText("exoplatform content mamagement")) ;
    
    assertEquals("GetPageTitleOfAttachment", wService.getPageTitleOfAttachment(attachment1.getJCRContentPath())) ;
    
  }
  
  public void testGetAttachmentAsStream() throws Exception {
    PageImpl kspage = (PageImpl)wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "GetAttachmentAsStream", "WikiHome") ;
    kspage.getContent().setText("forum faq wiki exoplatform") ;
    AttachmentImpl attachment1 = kspage.createAttachment("attachment.txt", Resource.createPlainText("this is a text attachment")) ;
    attachment1.setCreator("john") ;    
    assertEquals(attachment1.getName(), "attachment.txt") ;
    assertNotNull(attachment1.getContentResource()) ;    
    assertNotNull(wService.getAttachmentAsStream(attachment1.getPath()+"/jcr:content")) ;    
  }
  
  public void testGetSyntaxPage() throws Exception {
    PageImpl syntaxPage = wService.getHelpSyntaxPage(Syntax.XWIKI_2_0.toIdString());
    assertNotNull(syntaxPage);
  }

  public void testBrokenLink() throws Exception {
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "OriginalParentPage", "WikiHome");
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "OriginalPage", "OriginalParentPage");
    PageImpl relatedPage = (PageImpl) wService.getRelatedPage(PortalConfig.PORTAL_TYPE, "classic", "OriginalPage");
    assertEquals("OriginalPage", relatedPage.getName());
    wService.renamePage(PortalConfig.PORTAL_TYPE, "classic", "OriginalPage", "RenamedOriginalPage", "RenamedOriginalPage");
    relatedPage = (PageImpl) wService.getRelatedPage(PortalConfig.PORTAL_TYPE, "classic", "OriginalPage");
    assertEquals("RenamedOriginalPage", relatedPage.getName());
    wService.renamePage(PortalConfig.PORTAL_TYPE, "classic", "RenamedOriginalPage", "RenamedOriginalPage2", "RenamedOriginalPage2");
    relatedPage = (PageImpl) wService.getRelatedPage(PortalConfig.PORTAL_TYPE, "classic", "OriginalPage");
    assertEquals("RenamedOriginalPage2", relatedPage.getName());
    WikiPageParams currentPageParams= new WikiPageParams();
    currentPageParams.setPageId("RenamedOriginalPage2");
    currentPageParams.setOwner("classic");
    currentPageParams.setType(PortalConfig.PORTAL_TYPE);
    WikiPageParams newPageParams= new WikiPageParams();
    newPageParams.setPageId("WikiHome");
    newPageParams.setOwner("classic");
    newPageParams.setType(PortalConfig.PORTAL_TYPE);
    wService.movePage(currentPageParams,newPageParams);
    relatedPage = (PageImpl) wService.getRelatedPage(PortalConfig.PORTAL_TYPE, "classic", "OriginalPage");
    assertEquals("RenamedOriginalPage2", relatedPage.getName());
    wService.renamePage(PortalConfig.PORTAL_TYPE, "classic", "RenamedOriginalPage2", "RenamedOriginalPage3", "RenamedOriginalPage3");
    relatedPage = (PageImpl) wService.getRelatedPage(PortalConfig.PORTAL_TYPE, "classic", "OriginalPage");
    assertEquals("RenamedOriginalPage3", relatedPage.getName());
    wService.createPage(PortalConfig.GROUP_TYPE, "platform/users", "OriginalParentPage", "WikiHome");
    // Move RenamedOriginalPage3 from portal type to group type
    currentPageParams.setPageId("RenamedOriginalPage3");
    currentPageParams.setOwner("classic");
    currentPageParams.setType(PortalConfig.PORTAL_TYPE);
    newPageParams.setPageId("OriginalParentPage");
    newPageParams.setOwner("platform/users");
    newPageParams.setType(PortalConfig.GROUP_TYPE);
    //
    wService.movePage(currentPageParams,newPageParams);
    relatedPage = (PageImpl) wService.getRelatedPage(PortalConfig.PORTAL_TYPE, "classic", "OriginalPage");
    assertEquals("RenamedOriginalPage3", relatedPage.getName());
    wService.deletePage(PortalConfig.GROUP_TYPE, "platform/users", "RenamedOriginalPage3");
    assertNull(wService.getRelatedPage(PortalConfig.PORTAL_TYPE, "classic", "OriginalPage"));
  }
  
  public void testCircularRename() throws Exception {
    wService.createPage(PortalConfig.PORTAL_TYPE, "classic", "CircularRename1", "WikiHome");
    PageImpl relatedPage = (PageImpl) wService.getRelatedPage(PortalConfig.PORTAL_TYPE, "classic", "CircularRename1");
    assertEquals("CircularRename1", relatedPage.getName());
    wService.renamePage(PortalConfig.PORTAL_TYPE, "classic", "CircularRename1", "CircularRename2", "CircularRename2");
    relatedPage = (PageImpl) wService.getRelatedPage(PortalConfig.PORTAL_TYPE, "classic", "CircularRename1");
    assertEquals("CircularRename2", relatedPage.getName());
    // Do a circular rename
    wService.renamePage(PortalConfig.PORTAL_TYPE, "classic", "CircularRename2", "CircularRename1", "CircularRename1");
    relatedPage = (PageImpl) wService.getRelatedPage(PortalConfig.PORTAL_TYPE, "classic", "CircularRename1");
    assertEquals("CircularRename1", relatedPage.getName());
    relatedPage = (PageImpl) wService.getRelatedPage(PortalConfig.PORTAL_TYPE, "classic", "CircularRename2");
    assertNull(relatedPage);
  }
  
}
