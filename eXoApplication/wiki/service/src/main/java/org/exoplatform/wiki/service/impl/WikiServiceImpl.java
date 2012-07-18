package org.exoplatform.wiki.service.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.deployment.plugins.XMLDeploymentPlugin;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWiki;
import org.exoplatform.wiki.mow.core.api.wiki.HelpPage;
import org.exoplatform.wiki.mow.core.api.wiki.LinkEntry;
import org.exoplatform.wiki.mow.core.api.wiki.LinkRegistry;
import org.exoplatform.wiki.mow.core.api.wiki.MovedMixin;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.RemovedMixin;
import org.exoplatform.wiki.mow.core.api.wiki.RenamedMixin;
import org.exoplatform.wiki.mow.core.api.wiki.Template;
import org.exoplatform.wiki.mow.core.api.wiki.TemplateContainer;
import org.exoplatform.wiki.mow.core.api.wiki.Trash;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.mow.core.api.wiki.WikiImpl;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.BreadcrumbData;
import org.exoplatform.wiki.service.IDType;
import org.exoplatform.wiki.service.MetaDataPage;
import org.exoplatform.wiki.service.Permission;
import org.exoplatform.wiki.service.PermissionEntry;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.listener.PageWikiListener;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.service.search.TemplateSearchData;
import org.exoplatform.wiki.service.search.TemplateSearchResult;
import org.exoplatform.wiki.service.search.TitleSearchResult;
import org.exoplatform.wiki.service.search.WikiSearchData;
import org.exoplatform.wiki.template.plugin.WikiTemplatePagePlugin;
import org.exoplatform.wiki.utils.Utils;
import org.picocontainer.Startable;
import org.xwiki.rendering.syntax.Syntax;

public class WikiServiceImpl implements WikiService, Startable {

  final static private String          PREFERENCES          = "preferences";

  final static private String          DEFAULT_SYNTAX       = "defaultSyntax";

  final static private int             CIRCULAR_RENAME_FLAG   = 1000;

  private ConfigurationManager  configManager;

  private JCRDataStorage        jcrDataStorage;

  private Iterator<ValuesParam> syntaxHelpParams;

  private PropertiesParam           preferencesParams;
  
  private List<ComponentPlugin> plugins_ = new ArrayList<ComponentPlugin>();
  
  private List<WikiTemplatePagePlugin> templatePagePlugins_ = new ArrayList<WikiTemplatePagePlugin>();

  private static final Log      log               = ExoLogger.getLogger(WikiServiceImpl.class);

  public WikiServiceImpl(ConfigurationManager configManager,
                         JCRDataStorage jcrDataStorage,
                         InitParams initParams) {
    this.configManager = configManager;
    this.jcrDataStorage = jcrDataStorage;
    if (initParams != null) {
      syntaxHelpParams = initParams.getValuesParamIterator();
      preferencesParams = initParams.getPropertiesParam(PREFERENCES);
    }
  }
  
  public void initDefaultTemplatePage(String path) {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    ChromatticSession session = wStore.getSession();
    jcrDataStorage.initDefaultTemplatePage(session, configManager, path);
  }

  public Page createPage(String wikiType, String wikiOwner, String title, String parentId) throws Exception {
    String pageId = TitleResolver.getId(title, false);
    if(isExisting(wikiType, wikiOwner, pageId)) throw new Exception();
    Model model = getModel();
    WikiImpl wiki = (WikiImpl) getWiki(wikiType, wikiOwner, model);
    PageImpl page = wiki.createWikiPage();
    PageImpl parentPage = null;
    parentPage = (PageImpl) getPageById(wikiType, wikiOwner, parentId);
    if (parentPage == null)
      throw new IllegalArgumentException(String.format("[%s]:[%s]:[%s] is not [wikiType]:[wikiOwner]:[pageId] of an existed page!", wikiType, wikiOwner, parentId));    
    page.setName(pageId);
    parentPage.addWikiPage(page);
    ConversationState conversationState = ConversationState.getCurrent();
    String creator = null;
    if (conversationState != null && conversationState.getIdentity() != null) {
      creator = conversationState.getIdentity().getUserId();
    }
    page.setOwner(creator);
    page.setTitle(title);
    page.getContent().setText("");
    page.makeVersionable();
    
    //update LinkRegistry
    LinkRegistry linkRegistry = wiki.getLinkRegistry();
    String newEntryName = getLinkEntryName(wikiType, wikiOwner, pageId);
    String newEntryAlias = getLinkEntryAlias(wikiType, wikiOwner, pageId);
    LinkEntry newEntry = linkRegistry.getLinkEntries().get(newEntryName);
    if (newEntry == null) {
      newEntry = linkRegistry.createLinkEntry();
      linkRegistry.getLinkEntries().put(newEntryName, newEntry);
      newEntry.setAlias(newEntryAlias);
      newEntry.setTitle(title);
    }
    //This line must be outside if statement to break chaining list when add new page with name that was used in list.
    newEntry.setNewLink(newEntry);
    
    model.save();
    
    return page;
  }
  
  public void createDraftNewPage(String draftNewPageId) throws Exception {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    PageImpl draftNewPagesContainer = wStore.getDraftNewPagesContainer();
    PageImpl oldDraftPage = draftNewPagesContainer.getChildPages().get(draftNewPageId);
    if (oldDraftPage != null) {
      oldDraftPage.remove();
    }
    PageImpl draftNewPage = wStore.createPage();
    draftNewPage.setName(draftNewPageId);
    draftNewPagesContainer.addPublicPage(draftNewPage);
  }
  
  public boolean isExisting(String wikiType, String wikiOwner, String pageId) throws Exception {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    String statement = new WikiSearchData(wikiType, wikiOwner, pageId).getPageConstraint();
    if (statement != null) {
      Iterator<PageImpl> result = wStore.getSession().createQueryBuilder(PageImpl.class)
      .where(statement).get().objects();
      boolean isExisted = result.hasNext();
      if (!isExisted) {
        Page page = getWikiHome(wikiType, wikiOwner);
        if (page != null) {
          String wikiHomeId = TitleResolver.getId(page.getTitle(), true);
          if (wikiHomeId.equals(pageId)) {
            isExisted = true;
          }
        }
      }
      return isExisted;
    }
    return false;
  }
  
  public boolean deletePage(String wikiType, String wikiOwner, String pageId) throws Exception {
    if (WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageId) || pageId == null)
      return false;
    try {
      PageImpl page = (PageImpl) getPageById(wikiType, wikiOwner, pageId);
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      ChromatticSession session = wStore.getSession();
      RemovedMixin mix = session.create(RemovedMixin.class);
      session.setEmbedded(page, RemovedMixin.class, mix);
      mix.setRemovedBy(Utils.getCurrentUser());
      Calendar calendar = GregorianCalendar.getInstance();
      calendar.setTimeInMillis(new Date().getTime()) ;
      mix.setRemovedDate(calendar.getTime());
      mix.setParentPath(page.getParentPage().getPath());
      WikiImpl wiki = (WikiImpl) getWiki(wikiType, wikiOwner, model);
      Trash trash = wiki.getTrash();
      if(trash.isHasPage(page.getName())) {
        PageImpl oldDeleted = trash.getPage(page.getName()) ;
        String removedDate = oldDeleted.getRemovedMixin().getRemovedDate().toGMTString() ;
        String newName = page.getName()+ "_" + removedDate.replaceAll(" ", "-").replaceAll(":", "-");
        trash.addChild(newName, oldDeleted) ;        
      }      
      trash.addRemovedWikiPage(page);      
      
      //update LinkRegistry
      LinkRegistry linkRegistry = wiki.getLinkRegistry();
      if (linkRegistry.getLinkEntries().get(getLinkEntryName(wikiType, wikiOwner, pageId)) != null) {
        linkRegistry.getLinkEntries().get(getLinkEntryName(wikiType, wikiOwner, pageId)).setNewLink(null);
      }
      
      session.save();
    } catch (Exception e) {
      log.error("Can't delete page '" + pageId + "' ", e) ;
      return false;
    }
    return true;    
  }

  @Override
  public void deleteTemplatePage(String wikiType, String wikiOwner, String templateId) throws Exception {

   WikiPageParams params = new WikiPageParams(wikiType, wikiOwner, templateId);
   getTemplatePage(params, templateId).remove();
  }  
  
  public void deleteDraftNewPage(String newDraftPageId) throws Exception {
    RepositoryService repoService = (RepositoryService) PortalContainer.getInstance()
                                                                       .getComponentInstanceOfType(RepositoryService.class);
    try {
      repoService.getCurrentRepository();
    } catch (Exception e) {
      log.info("Can not get current repository. Drap page will removed in next starting service");
      return;
    }
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    PageImpl draftNewPagesContainer = wStore.getDraftNewPagesContainer();
    PageImpl draftPage = (PageImpl) draftNewPagesContainer.getChild(newDraftPageId);
    if (draftPage != null){
      draftPage.remove();
    }
  }

  public boolean renamePage(String wikiType,
                            String wikiOwner,
                            String pageName,
                            String newName,
                            String newTitle) throws Exception {
    if (WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageName) || pageName == null)
      return false;
    PageImpl currentPage = (PageImpl) getPageById(wikiType, wikiOwner, pageName);
    PageImpl parentPage = currentPage.getParentPage();

    RenamedMixin mix = currentPage.getRenamedMixin();
    if (mix == null) {        
      mix = parentPage.getChromatticSession().create(RenamedMixin.class);
      currentPage.setRenamedMixin(mix);
      List<String> ids = new ArrayList<String>() ;
      ids.add(pageName);
      mix.setOldPageIds(ids.toArray(new String[]{}));
    }
    List<String> ids = new ArrayList<String>();
    for (String id : mix.getOldPageIds()) {
      ids.add(id);
    }
    mix.setOldPageIds(ids.toArray(new String[] {}));    
    currentPage.setName(newName);
    getModel().save();
    currentPage.setTitle(newTitle) ;
    
    //update LinkRegistry
    WikiImpl wiki = (WikiImpl) parentPage.getWiki();
    LinkRegistry linkRegistry = wiki.getLinkRegistry();
    String newEntryName = getLinkEntryName(wikiType, wikiOwner, newName);
    String newEntryAlias = getLinkEntryAlias(wikiType, wikiOwner, newName);
    LinkEntry newEntry = linkRegistry.getLinkEntries().get(newEntryName);
    LinkEntry entry = linkRegistry.getLinkEntries().get(getLinkEntryName(wikiType, wikiOwner, pageName));
    if (newEntry == null) {
      newEntry = linkRegistry.createLinkEntry();
      linkRegistry.getLinkEntries().put(newEntryName, newEntry);
      newEntry.setAlias(newEntryAlias);
      newEntry.setNewLink(newEntry);
      newEntry.setTitle(newTitle);
      entry.setNewLink(newEntry);
    } else {
      processCircularRename(entry, newEntry);
    }
    parentPage.getChromatticSession().save() ;
    return true ;    
  }

  public boolean movePage(WikiPageParams currentLocationParams, WikiPageParams newLocationParams) throws Exception {
    try {
      PageImpl destPage = (PageImpl) getPageById(newLocationParams.getType(),
                                                 newLocationParams.getOwner(),
                                                 newLocationParams.getPageId());
      if (destPage == null || !destPage.hasPermission(PermissionType.EDITPAGE))
        return false;
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      ChromatticSession session = wStore.getSession();
      PageImpl movePage = (PageImpl) getPageById(currentLocationParams.getType(),
                                                 currentLocationParams.getOwner(),
                                                 currentLocationParams.getPageId());
      WikiImpl sourceWiki = (WikiImpl) movePage.getWiki();
      MovedMixin mix = movePage.getMovedMixin();
      if (mix == null) {        
        movePage.setMovedMixin(session.create(MovedMixin.class));
        mix = movePage.getMovedMixin();
        mix.setTargetPage(movePage.getParentPage());
        session.save();
      }
      mix.setTargetPage(destPage);
   
      WikiImpl destWiki = (WikiImpl) destPage.getWiki();
      movePage.setParentPage(destPage);
      
      //update LinkRegistry
      if (!newLocationParams.getType().equals(currentLocationParams.getType())) {
        LinkRegistry sourceLinkRegistry = sourceWiki.getLinkRegistry();
        LinkRegistry destLinkRegistry = destWiki.getLinkRegistry();
        String newEntryName = getLinkEntryName(newLocationParams.getType(), newLocationParams.getOwner(), currentLocationParams.getPageId());
        String newEntryAlias = getLinkEntryAlias(newLocationParams.getType(), newLocationParams.getOwner(), currentLocationParams.getPageId());
        LinkEntry newEntry = destLinkRegistry.getLinkEntries().get(newEntryName);
        LinkEntry entry = sourceLinkRegistry.getLinkEntries().get(getLinkEntryName(currentLocationParams.getType(), currentLocationParams.getOwner(), currentLocationParams.getPageId()));
        if (newEntry == null) {
          newEntry = destLinkRegistry.createLinkEntry();
          destLinkRegistry.getLinkEntries().put(newEntryName, newEntry);
          newEntry.setAlias(newEntryAlias);
          newEntry.setNewLink(newEntry);
          newEntry.setTitle(destPage.getTitle());
          entry.setNewLink(newEntry);
        } else {
          processCircularRename(entry, newEntry);
        }
      }
    } catch (Exception e) {
      log.error("Can't move page '" + currentLocationParams.getPageId() + "' ", e);
      return false;
    }
    return true;
  }

  public List<PermissionEntry> getWikiPermission(String wikiType, String wikiOwner) throws Exception {
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>();
    Model model = getModel();
    WikiImpl wiki = (WikiImpl) getWiki(wikiType, wikiOwner, model);
    if (wiki == null) {
      return permissionEntries;
    }
    if (!wiki.getDefaultPermissionsInited()) {
      List<String> permissions = getWikiDefaultPermissions(wikiType, wikiOwner);
      wiki.setWikiPermissions(permissions);
      wiki.setDefaultPermissionsInited(true);
      HashMap<String, String[]> permMap = new HashMap<String, String[]>();
      for (String perm : permissions) {
        String[] actions = perm.substring(0, perm.indexOf(":")).split(",");
        perm = perm.substring(perm.indexOf(":") + 1);
        String id = perm.substring(perm.indexOf(":") + 1);
        List<String> jcrActions = new ArrayList<String>();
        for (String action : actions) {
          if (PermissionType.VIEWPAGE.toString().equals(action)) {
            jcrActions.add(org.exoplatform.services.jcr.access.PermissionType.READ);
          } else if (PermissionType.EDITPAGE.toString().equals(action)) {
            jcrActions.add(org.exoplatform.services.jcr.access.PermissionType.ADD_NODE);
            jcrActions.add(org.exoplatform.services.jcr.access.PermissionType.REMOVE);
            jcrActions.add(org.exoplatform.services.jcr.access.PermissionType.SET_PROPERTY);
          }
        }
        permMap.put(id, jcrActions.toArray(new String[jcrActions.size()]));
      }
      updateAllPagesPermissions(wikiType, wikiOwner, permMap);
    }
    List<String> permissions = wiki.getWikiPermissions();
    for (String perm : permissions) {
      String[] actions = perm.substring(0, perm.indexOf(":")).split(",");
      perm = perm.substring(perm.indexOf(":") + 1);
      String idType = perm.substring(0, perm.indexOf(":"));
      String id = perm.substring(perm.indexOf(":") + 1);

      PermissionEntry entry = new PermissionEntry();
      if (IDType.USER.toString().equals(idType)) {
        entry.setIdType(IDType.USER);
      } else if (IDType.GROUP.toString().equals(idType)) {
        entry.setIdType(IDType.GROUP);
      } else if (IDType.MEMBERSHIP.toString().equals(idType)) {
        entry.setIdType(IDType.MEMBERSHIP);
      }
      entry.setId(id);
      Permission[] perms = new Permission[4];
      perms[0] = new Permission();
      perms[0].setPermissionType(PermissionType.VIEWPAGE);
      perms[1] = new Permission();
      perms[1].setPermissionType(PermissionType.EDITPAGE);
      perms[2] = new Permission();
      perms[2].setPermissionType(PermissionType.ADMINPAGE);
      perms[3] = new Permission();
      perms[3].setPermissionType(PermissionType.ADMINSPACE);
      for (String action : actions) {
        if (PermissionType.VIEWPAGE.toString().equals(action)) {
          perms[0].setAllowed(true);
        } else if (PermissionType.EDITPAGE.toString().equals(action)) {
          perms[1].setAllowed(true);
        } else if (PermissionType.ADMINPAGE.toString().equals(action)) {
          perms[2].setAllowed(true);
        } else if (PermissionType.ADMINSPACE.toString().equals(action)) {
          perms[3].setAllowed(true);
        }
      }
      entry.setPermissions(perms);

      permissionEntries.add(entry);
    }
    return permissionEntries;
  }
  
  public void setWikiPermission(String wikiType, String wikiOwner, List<PermissionEntry> permissionEntries) throws Exception {
    Model model = getModel();
    WikiImpl wiki = (WikiImpl) getWiki(wikiType, wikiOwner, model);
    List<String> permissions = new ArrayList<String>();
    HashMap<String, String[]> permMap = new HashMap<String, String[]>();
    for (PermissionEntry entry : permissionEntries) {
      StringBuilder actions = new StringBuilder();
      Permission[] pers = entry.getPermissions();
      List<String> permlist = new ArrayList<String>();
      // Permission strings has the format:
      // VIEWPAGE,EDITPAGE,ADMINPAGE,ADMINSPACE:USER:john
      // VIEWPAGE:GROUP:/platform/users
      // VIEWPAGE,EDITPAGE,ADMINPAGE,ADMINSPACE:MEMBERSHIP:manager:/platform/administrators
      for (int i = 0; i < pers.length; i++) {
        Permission perm = pers[i];
        if (perm.isAllowed()) {
          actions.append(perm.getPermissionType().toString());
          if (i < pers.length - 1) {
            actions.append(",");
          }
          
          if (perm.getPermissionType().equals(PermissionType.VIEWPAGE)) {
            permlist.add(org.exoplatform.services.jcr.access.PermissionType.READ);
          } else if (perm.getPermissionType().equals(PermissionType.EDITPAGE)) {
            permlist.add(org.exoplatform.services.jcr.access.PermissionType.ADD_NODE);
            permlist.add(org.exoplatform.services.jcr.access.PermissionType.REMOVE);
            permlist.add(org.exoplatform.services.jcr.access.PermissionType.SET_PROPERTY);
          }
        }
      }
      if (actions.toString().length() > 0) {
        actions.append(":").append(entry.getIdType()).append(":").append(entry.getId());
        permissions.add(actions.toString());
      }
      if (permlist.size() > 0) {
        permMap.put(entry.getId(), permlist.toArray(new String[permlist.size()]));
      }
    }
    wiki.setWikiPermissions(permissions);
    // TODO: study performance
    updateAllPagesPermissions(wikiType, wikiOwner, permMap);
  }

  public Page getPageById(String wikiType, String wikiOwner, String pageId) throws Exception {
    PageImpl page = null;

    if (WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageId) || pageId == null) {
      page = getWikiHome(wikiType, wikiOwner);
    } else {
      String statement = new WikiSearchData(wikiType, wikiOwner, pageId).getPageConstraint();
      if (statement != null) {
        Model model = getModel();
        WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
        page = searchPage(statement, wStore.getSession());
        if (page == null && (page = getWikiHome(wikiType, wikiOwner)) != null) {
          String wikiHomeId = TitleResolver.getId(page.getTitle(), true);
          if (!wikiHomeId.equals(pageId)) {
            page = null;
          }
        }
      }
    }
    
    if (page != null && page.hasPermission(PermissionType.VIEWPAGE)) {
      return page;
    }
    return null;
  }

  public Page getRelatedPage(String wikiType, String wikiOwner, String pageId) throws Exception {
    Model model = getModel();
    WikiImpl wiki = (WikiImpl) getWiki(wikiType, wikiOwner, model);
    LinkRegistry linkRegistry = wiki.getLinkRegistry();
    LinkEntry oldLinkEntry = linkRegistry.getLinkEntries().get(getLinkEntryName(wikiType, wikiOwner, pageId));
    LinkEntry newLinkEntry = null;
    if (oldLinkEntry != null) {
      newLinkEntry = oldLinkEntry.getNewLink();
    }
    int circularFlag = CIRCULAR_RENAME_FLAG;// To deal with old circular data if it is existed
    while (newLinkEntry != null && !newLinkEntry.equals(oldLinkEntry) && circularFlag > 0) {
      oldLinkEntry = newLinkEntry;
      newLinkEntry = oldLinkEntry.getNewLink();
      circularFlag--;
    }
    if (newLinkEntry == null) {
      return null;
    }
    if (circularFlag == 0) {
      // Find link entry mapped with an existed page in old circular data
      circularFlag = CIRCULAR_RENAME_FLAG;
      while (circularFlag > 0) {
        if (getPageWithLinkEntry(newLinkEntry) != null) {
          break;
        }
        newLinkEntry = newLinkEntry.getNewLink();
        circularFlag--;
      }
      // Break old circular data
      if (circularFlag > 0) {
        newLinkEntry.setNewLink(newLinkEntry);
      }
    }
    return getPageWithLinkEntry(newLinkEntry);
  }
  
  public Page getExsitedOrNewDraftPageById(String wikiType, String wikiOwner, String pageId) throws Exception {
    Page existedPage = getPageById(wikiType, wikiOwner, pageId);
    if (existedPage != null) {
      return existedPage;
    }
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    PageImpl draftNewPagesContainer = wStore.getDraftNewPagesContainer();
    return draftNewPagesContainer.getChildPages().get(pageId);
  }
  
  public Page getPageByUUID(String uuid) throws Exception {

    return null;
  }
  
  @Override
  public Template getTemplatePage(WikiPageParams params, String templateId) throws Exception {
    return getTemplatesContainer(params).getTemplate(templateId);
  }

  @Override
  public Map<String,Template> getTemplates(WikiPageParams params) throws Exception {
    return getTemplatesContainer(params).getTemplates();
  }
  
  @Override
  public TemplateContainer getTemplatesContainer(WikiPageParams params) throws Exception {
    Model model = getModel();
    WikiImpl wiki = (WikiImpl) getWiki(params.getType(), params.getOwner(), model);
    return wiki.getPreferences().getTemplateContainer();
  }
  
  @Override
  public void modifyTemplate(WikiPageParams params,
                             Template template,
                             String newTitle,
                             String newDescription,
                             String newContent,
                             String newSyntaxId) throws Exception {
    if (newTitle != null) {
      template = getTemplatesContainer(params).addPage(TitleResolver.getId(newTitle,false), template);
      template.setDescription(newDescription);
      template.setTitle(newTitle);
      template.getContent().setText(newContent);
      template.setSyntax(newSyntaxId);
    }
  }

  public PageList<SearchResult> searchContent(WikiSearchData data) throws Exception {
    List<SearchResult> results = search(data).getAll();
    for (SearchResult result : results) {
      if (WikiNodeType.WIKI_ATTACHMENT.equals(result.getType())) {
        results.remove(result);
      }
    }
    return new ObjectPageList<SearchResult>(results, 10);
  }
  
  public PageList<SearchResult> search(WikiSearchData data) throws Exception {

    Model model = getModel();
    try {
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      PageList<SearchResult> result = jcrDataStorage.search(wStore.getSession(), data);
      
      if ((data.getTitle() != null) && (data.getWikiType() != null) && (data.getWikiOwner() != null)) {
        PageImpl homePage = getWikiHome(data.getWikiType(), data.getWikiOwner());
        if (data.getTitle().equals("") || homePage != null && homePage.getTitle().contains(data.getTitle())) {
          Calendar wikiHomeCreateDate = Calendar.getInstance();
          wikiHomeCreateDate.setTime(homePage.getCreatedDate());
          
          Calendar wikiHomeUpdateDate = Calendar.getInstance();
          wikiHomeUpdateDate.setTime(homePage.getUpdatedDate());
          
          SearchResult wikiHomeResult = new SearchResult(null, homePage.getTitle(), homePage.getPath(), WikiNodeType.WIKI_HOME.toString(), wikiHomeUpdateDate, wikiHomeCreateDate);
          wikiHomeResult.setPageName(homePage.getName());          
          List<SearchResult> tempSearchResult = result.getAll();
          tempSearchResult.add(wikiHomeResult);
          result = new ObjectPageList<SearchResult>(tempSearchResult, result.getPageSize());
        }
      }
      
      return result;
    } catch (Exception e) {
      log.error("Can't search", e);
    }
    return null;
  }
  
  @Override
  public List<TemplateSearchResult> searchTemplate(TemplateSearchData data) throws Exception {

    Model model = getModel();
    try {
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      List<TemplateSearchResult> result = jcrDataStorage.searchTemplate(wStore.getSession(),
                                                                            data);
      return result;
    } catch (Exception e) {
      log.error("Can't search", e);
    }
    return null;
  }

  public List<SearchResult> searchRenamedPage(String wikiType, String wikiOwner, String pageId) throws Exception {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiSearchData data = new WikiSearchData(wikiType, wikiOwner, pageId);
    return jcrDataStorage.searchRenamedPage(wStore.getSession(), data);
  }

  public Object findByPath(String path, String objectNodeType) {    
    String relPath = path;
    if (relPath.startsWith("/"))
      relPath = relPath.substring(1);
    try {
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      if (WikiNodeType.WIKI_PAGE.equals(objectNodeType)) {
        return wStore.getSession().findByPath(PageImpl.class, relPath);
      } else if (WikiNodeType.WIKI_ATTACHMENT.equals(objectNodeType)) {
        return wStore.getSession().findByPath(AttachmentImpl.class, relPath);
      } else if (WikiNodeType.WIKI_TEMPLATE.equals(objectNodeType)) {   
        return wStore.getSession().findByPath(Template.class, relPath);
      }
    } catch (Exception e) {
      log.error("Can't find Object", e);
    }  
    return null;
  }

  public String getPageTitleOfAttachment(String path) throws Exception {
    try {
      String relPath = path;
      if (relPath.startsWith("/"))
        relPath = relPath.substring(1);
      String temp = relPath.substring(0, relPath.lastIndexOf("/"));
      relPath = temp.substring(0, temp.lastIndexOf("/"));
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      PageImpl page = wStore.getSession().findByPath(PageImpl.class, relPath);
      return page.getTitle();
    } catch (Exception e) {
      return null;
    }
  }

  public InputStream getAttachmentAsStream(String path) throws Exception {
    Model model = getModel();
    try {
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      return jcrDataStorage.getAttachmentAsStream(path, wStore.getSession());
    } catch (Exception e) {
      return null;
    }
  }

  public List<BreadcrumbData> getBreadcumb(String wikiType, String wikiOwner, String pageId) throws Exception {
    return getBreadcumb(null, wikiType, wikiOwner, pageId);
  }

  public PageImpl getHelpSyntaxPage(String syntaxId) throws Exception {
    Model model = getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    if (wStore.getHelpPagesContainer().getChildPages().size() == 0) {
      createHelpPages(wStore);
    }
    Iterator<PageImpl> syntaxPageIterator = wStore.getHelpPagesContainer()
                                                  .getChildPages()
                                                  .values()
                                                  .iterator();
    while (syntaxPageIterator.hasNext()) {
      PageImpl syntaxPage = syntaxPageIterator.next();
      if (syntaxPage.getSyntax().equals(syntaxId)) {
        return syntaxPage;
      }
    }
    return null;
  }
  
  public Page getMetaDataPage(MetaDataPage metaPage) throws Exception {
    if (MetaDataPage.EMOTION_ICONS_PAGE.equals(metaPage)) {
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      return wStore.getEmotionIconsPage();
    }
    return null;
  }
  
  public String getDefaultWikiSyntaxId() {
    if (preferencesParams != null) {
      return preferencesParams.getProperty(DEFAULT_SYNTAX);
    }
    return Syntax.XWIKI_2_0.toIdString();
  }
  
  public WikiPageParams getWikiPageParams(BreadcrumbData data) {
    if (data != null) {
      return new WikiPageParams(data.getWikiType(), data.getWikiOwner(), data.getId());
    }
    return null;
  }
  
  public List<TitleSearchResult> searchDataByTitle(WikiSearchData data) throws Exception {
    try {
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      return jcrDataStorage.searchDataByTitle(wStore.getSession(), data);
    } catch (Exception e) {
      log.error("Can't search content", e);
    }
    return null;
  }
  
  private Model getModel() {
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer()
                                                            .getComponentInstanceOfType(MOWService.class);
    return mowService.getModel();
  }

  private PageImpl searchPage(String statement, ChromatticSession session) throws Exception {
    PageImpl wikiPage = null;
    if (statement != null) {
      Iterator<PageImpl> result = session.createQueryBuilder(PageImpl.class)
                                         .where(statement)
                                         .get()
                                         .objects();
      if (result.hasNext())
        wikiPage = result.next();
    }
    // TODO: still don't know reason but following code is necessary.
    if (wikiPage != null) {
      String path = wikiPage.getPath();
      if (path.startsWith("/")) {
        path = path.substring(1, path.length());
      }
      wikiPage = session.findByPath(PageImpl.class, path);
    }
    if (wikiPage != null) {
    }
    return wikiPage;
  }

  private Wiki getWiki(String wikiType, String owner, Model model) {
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiImpl wiki = null;
    try {
      if (PortalConfig.PORTAL_TYPE.equals(wikiType)) {
        WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
        wiki = portalWikiContainer.getWiki(owner, true);
      } else if (PortalConfig.GROUP_TYPE.equals(wikiType)) {
        WikiContainer<GroupWiki> groupWikiContainer = wStore.getWikiContainer(WikiType.GROUP);
        boolean hasPermission = hasPermission(wikiType, owner);
        wiki = groupWikiContainer.getWiki(owner, hasPermission);
      } else if (PortalConfig.USER_TYPE.equals(wikiType)) {
        boolean hasEditWiki = hasPermission(wikiType, owner);
        WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
        wiki = userWikiContainer.getWiki(owner, hasEditWiki);

      }
      model.save();
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("[WikiService] Cannot get wiki " + wikiType + ":" + owner, e);
      }
    }
    return wiki;
  }
  
  private List<AccessControlEntry> getAccessControls(String wikiType, String wikiOwner) throws Exception{
    List<AccessControlEntry> aces = new ArrayList<AccessControlEntry>();
    try {
      List<String> permissions = getWikiDefaultPermissions(wikiType, wikiOwner);
      for (String perm : permissions) {
        String[] actions = perm.substring(0, perm.indexOf(":")).split(",");
        perm = perm.substring(perm.indexOf(":") + 1);
        String id = perm.substring(perm.indexOf(":") + 1);
        for (String action : actions) {
          aces.add(new AccessControlEntry(id, action));
        }
      }
    } catch (Exception e) {
      log.debug("failed in method getDefaultPermission:", e);
    }
    return aces;
  }
  
  private  boolean hasPermission(String wikiType, String owner) throws Exception {
    ConversationState conversationState = ConversationState.getCurrent();
    Identity user = null;
    if (conversationState != null) {
      user = conversationState.getIdentity();
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserACL acl = (UserACL)container.getComponentInstanceOfType(UserACL.class);
      if(acl != null && acl.getSuperUser().equals(user.getUserId())){
        return true;
      }
    } else {
      user = new Identity(IdentityConstants.ANONIM);
    }
    List<AccessControlEntry> aces = getAccessControls(wikiType, owner);
    AccessControlList acl = new AccessControlList(owner, aces);
    String []permission = new String[]{PermissionType.ADMINSPACE.toString()};
    return Utils.hasPermission(acl, permission, user);
  }

  private WikiHome getWikiHome(String wikiType, String owner) throws Exception {
    Model model = getModel();
    WikiImpl wiki = (WikiImpl) getWiki(wikiType, owner, model);
    if (wiki != null) {
      WikiHome wikiHome = wiki.getWikiHome();
      return wikiHome;
    } else {
      return null;
    }

  }

  private List<BreadcrumbData> getBreadcumb(List<BreadcrumbData> list,
                                           String wikiType,
                                           String wikiOwner,
                                           String pageId) throws Exception {
    if (list == null) {
      list = new ArrayList<BreadcrumbData>(5);
    }
    if (pageId == null) {
      return list;
    }
    PageImpl page = (PageImpl) getPageById(wikiType, wikiOwner, pageId);
    if (page == null) {
      return list;
    }
    list.add(0, new BreadcrumbData(page.getName(), page.getPath(), page.getTitle(), wikiType, wikiOwner));
    PageImpl parentPage = page.getParentPage();
    if (parentPage != null) {
      getBreadcumb(list, wikiType, wikiOwner, parentPage.getName());
    }

    return list;
  }

  private void createHelpPages(WikiStoreImpl wStore) {
    PageImpl helpPage = wStore.getHelpPagesContainer();
    while (syntaxHelpParams.hasNext()) {
      try {
        ValuesParam syntaxhelpParam = syntaxHelpParams.next();
        String syntaxName = syntaxhelpParam.getName();
        ArrayList<String> syntaxValues = syntaxhelpParam.getValues();
        String shortFile = syntaxValues.get(0);
        String fullFile = syntaxValues.get(1);
        HelpPage syntaxPage = addSyntaxPage(wStore, helpPage, syntaxName, shortFile, " Short help Page");
        addSyntaxPage(wStore, syntaxPage, syntaxName, fullFile, " Full help Page");
      } catch (Exception e) {
        log.error("Can not create Help page", e);
      }
    }
  }
  
  @Override
  public Template createTemplatePage(String title, WikiPageParams params) throws Exception {
    Model model = getModel();
    TemplateContainer templContainer = getTemplatesContainer(params);
    ConversationState conversationState = ConversationState.getCurrent();
    try {
      Template template = templContainer.createTemplatePage();
      String pageId = TitleResolver.getId(title, false);
      template.setName(pageId);
      templContainer.addPage(template.getName(), template);
      String creator = null;
      if (conversationState != null && conversationState.getIdentity() != null) {
        creator = conversationState.getIdentity().getUserId();
      }
      template.setOwner(creator);
      template.setTitle(title);
      template.getContent().setText("");
      model.save();
      return template;
    } catch (Exception e) {
      log.error("Can not create Template page", e);
    }
    return null;
  }

  private HelpPage addSyntaxPage(WikiStoreImpl wStore,
                                 PageImpl parentPage,
                                 String name,
                                 String path,
                                 String type) throws Exception {
    StringBuffer stringContent = new StringBuffer();
    InputStream inputContent = null;
    BufferedReader bufferReader = null;
    String tempLine;
    inputContent = configManager.getInputStream(path);
    bufferReader = new BufferedReader(new InputStreamReader(inputContent));
    while ((tempLine = bufferReader.readLine()) != null) {
      stringContent.append(tempLine + "\n");
    }

    HelpPage syntaxPage = wStore.createHelpPage();
    String realName = name.replace("/", "");
    syntaxPage.setName(realName + type);
    parentPage.addPublicPage(syntaxPage);
    AttachmentImpl content = syntaxPage.getContent();
    syntaxPage.setTitle(realName + type);
    content.setText(stringContent.toString());
    syntaxPage.setSyntax(name);
    syntaxPage.setNonePermission();
    inputContent.close();
    bufferReader.close();
    return syntaxPage;
  }
  
  private void addEmotionIcons() {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {
      Model model = getModel();
      WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
      if (wStore.getEmotionIconsPage() == null) {
        model.save();
        XMLDeploymentPlugin emotionIconsPlugin = getEmotionIconsPlugin();
        if (emotionIconsPlugin != null) {
          emotionIconsPlugin.deploy(sessionProvider);
        }
      }
    } catch (Exception e) {
      log.warn("Can not init emotion icons...");
    } finally {
      sessionProvider.close();
    }
  }

  private XMLDeploymentPlugin getEmotionIconsPlugin() {
    for (ComponentPlugin c : plugins_) {
      if (c instanceof XMLDeploymentPlugin) {
        return (XMLDeploymentPlugin) c;
      }
    }
    return null;
  }

  private String getLinkEntryName(String wikiType, String wikiOwner, String pageId) {
    if (PortalConfig.GROUP_TYPE.equals(wikiType)) {
      wikiOwner = wikiOwner.replace("/", "-");
    }
    return wikiType + "@" + wikiOwner + "@" + pageId;
  }
  
  private String getLinkEntryAlias(String wikiType, String wikiOwner, String pageId) {
    return wikiType + "@" + wikiOwner + "@" + pageId;
  }
  
  private void processCircularRename(LinkEntry entry, LinkEntry newEntry) {
    // Check circular rename
    boolean isCircular = true;
    int circularFlag = CIRCULAR_RENAME_FLAG;// To deal with old circular data if it is existed
    LinkEntry checkEntry = newEntry;
    while (!checkEntry.equals(entry) && circularFlag > 0) {
      checkEntry = checkEntry.getNewLink();
      if (checkEntry.getNewLink().equals(checkEntry) && !checkEntry.equals(entry)) {
        isCircular = false;
        break;
      }
      circularFlag--;
    }
    if (!isCircular || circularFlag == 0) {
      entry.setNewLink(newEntry);
    } else {
      LinkEntry nextEntry = newEntry.getNewLink();
      while (!nextEntry.equals(newEntry)) {
        LinkEntry deletedEntry = nextEntry;
        nextEntry = nextEntry.getNewLink();
        if (!nextEntry.equals(deletedEntry)) {
          deletedEntry.remove();
        } else {
          deletedEntry.remove();
          break;
        }
      }
    }
    newEntry.setNewLink(newEntry);
  }
  
  private Page getPageWithLinkEntry(LinkEntry entry) throws Exception {
    String linkEntryAlias = entry.getAlias();
    String[] splits = linkEntryAlias.split("@");
    String wikiType = splits[0];
    String wikiOwner = splits[1];
    String pageId = linkEntryAlias.substring((wikiType + "@" + wikiOwner + "@").length());
    return getPageById(wikiType, wikiOwner, pageId);
  }
  
  private void updateAllPagesPermissions(String wikiType, String wikiOwner, HashMap<String, String[]> permMap) throws Exception {    
    PageImpl page = getWikiHome(wikiType, wikiOwner);
    Queue<PageImpl> queue = new LinkedList<PageImpl>();
    queue.add(page);
    while (queue.peek() != null) {
      PageImpl p = (PageImpl) queue.poll();
      if (!p.getOverridePermission()) {
        p.setPermission(permMap);
      }
      Iterator<PageImpl> iter = p.getChildPages().values().iterator();
      while (iter.hasNext()) {
        queue.add(iter.next());
      }
    }
  }
  
  private List<String> getWikiDefaultPermissions(String wikiType, String wikiOwner) throws Exception {
    String view = new StringBuilder().append(PermissionType.VIEWPAGE).toString();
    String viewEdit = new StringBuilder().append(PermissionType.VIEWPAGE).append(",").append(PermissionType.EDITPAGE).toString();
    String all = new StringBuilder().append(PermissionType.VIEWPAGE)
                                    .append(",")
                                    .append(PermissionType.EDITPAGE)
                                    .append(",")
                                    .append(PermissionType.ADMINPAGE)
                                    .append(",")
                                    .append(PermissionType.ADMINSPACE)
                                    .toString();
    List<String> permissions = new ArrayList<String>();
    Iterator<Entry<String, IDType>> iter = Utils.getACLForAdmins().entrySet().iterator();
    while (iter.hasNext()) {
      Entry<String, IDType> entry = iter.next();
      permissions.add(new StringBuilder(all).append(":").append(entry.getValue()).append(":").append(entry.getKey()).toString());
    }
    if (PortalConfig.PORTAL_TYPE.equals(wikiType)) {
      UserPortalConfigService service = (UserPortalConfigService) ExoContainerContext.getCurrentContainer()
                                                                                     .getComponentInstanceOfType(UserPortalConfigService.class);
      PortalConfig portalConfig = service.getUserPortalConfig(wikiOwner, null).getPortalConfig();
      String portalEditClause = new StringBuilder(all).append(":")
                                                      .append(IDType.MEMBERSHIP)
                                                      .append(":")
                                                      .append(portalConfig.getEditPermission())
                                                      .toString();
      if (!permissions.contains(portalEditClause)) {
        permissions.add(portalEditClause);
      }
      permissions.add(new StringBuilder(viewEdit).append(":").append(IDType.USER).append(":any").toString());
    } else if (PortalConfig.GROUP_TYPE.equals(wikiType)) {
      UserACL userACL = (UserACL) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);
      String makableMTClause = new StringBuilder(all).append(":")
                                                     .append(IDType.MEMBERSHIP)
                                                     .append(":")
                                                     .append(userACL.getMakableMT())
                                                     .append(":")
                                                     .append(wikiOwner)
                                                     .toString();
      if (!permissions.contains(makableMTClause)) {
        permissions.add(makableMTClause);
      }
      String ownerClause = new StringBuilder(viewEdit).append(":")
                                                      .append(IDType.MEMBERSHIP)
                                                      .append(":*:")
                                                      .append(wikiOwner)
                                                      .toString();
      if (!permissions.contains(ownerClause)) {
        permissions.add(ownerClause);
      }
      permissions.add(new StringBuilder(view).append(":").append(IDType.USER).append(":any").toString());
    } else if (PortalConfig.USER_TYPE.equals(wikiType)) {
      String ownerClause = new StringBuilder(all).append(":").append(IDType.USER).append(":").append(wikiOwner).toString();
      if (!permissions.contains(ownerClause)) {
        permissions.add(ownerClause);
      }
      permissions.add(new StringBuilder(view).append(":").append(IDType.USER).append(":any").toString());
    }
    return permissions;
  }

  @Override
  public void addComponentPlugin(ComponentPlugin plugin) {
    if (plugin != null) {
      plugins_.add(plugin);
    }
  }

  @Override
  public void addWikiTemplatePagePlugin(WikiTemplatePagePlugin plugin) {
    if (plugin != null) {
      templatePagePlugins_.add(plugin);
    }
  }

  @Override
  public List<PageWikiListener> getPageListeners() {
    List<PageWikiListener> pageListeners = new ArrayList<PageWikiListener>();
    for (ComponentPlugin c : plugins_) {
      if (c instanceof PageWikiListener) {
        pageListeners.add((PageWikiListener) c);
      }
    }
    return pageListeners;
  }

  public void setTemplatePagePlugin() {
    for (WikiTemplatePagePlugin plugin : templatePagePlugins_) {
      jcrDataStorage.setTemplatePagePlugin(plugin);
    }
  }

  @Override
  public boolean addRelatedPage(WikiPageParams orginaryPageParams, WikiPageParams relatedPageParams) throws Exception {
    
    PageImpl orginary = (PageImpl) getPageById(orginaryPageParams.getType(), orginaryPageParams.getOwner(), orginaryPageParams.getPageId());
    PageImpl related = (PageImpl) getPageById(relatedPageParams.getType(), relatedPageParams.getOwner(), relatedPageParams.getPageId());
    
    return orginary.addRelatedPage(related) != null;
  }

  @Override
  public List<Page> getRelatedPage(WikiPageParams pageParams) throws Exception {
    PageImpl page = (PageImpl) getPageById(pageParams.getType(), pageParams.getOwner(), pageParams.getPageId());
    List<PageImpl> pages = page.getRelatedPages();
    return new ArrayList<Page>(pages);
  }

  @Override
  public boolean removeRelatedPage(WikiPageParams orginaryPageParams,
                                   WikiPageParams relatedPageParams) throws Exception {
    PageImpl orginary = (PageImpl) getPageById(orginaryPageParams.getType(), orginaryPageParams.getOwner(), orginaryPageParams.getPageId());
    PageImpl related = (PageImpl) getPageById(relatedPageParams.getType(), relatedPageParams.getOwner(), relatedPageParams.getPageId());
    return orginary.removeRelatedPage(related) != null;
  }
  
  private void removeDraftPages() {
    try {
      Model model = getModel();
      WikiStoreImpl wikiStore = (WikiStoreImpl) model.getWikiStore();
      PageImpl draftPages = wikiStore.getDraftNewPagesContainer();
      draftPages.remove();
    } catch (Exception e) {
      log.warn("Can not remove draft pages ...");
    }
  }
  
  private void removeHelpPages() {
    try {
      Model model = getModel();
      WikiStoreImpl wikiStore = (WikiStoreImpl) model.getWikiStore();
      PageImpl helpPages = wikiStore.getHelpPagesContainer();
      helpPages.remove();
    } catch (Exception e) {
      log.warn("Can not remove help pages ...");
    }
  }
  
  @Override
  public void start() {
    try {
      ChromatticManager chromatticManager = (ChromatticManager) ExoContainerContext.getCurrentContainer()
                                                                                   .getComponentInstanceOfType(ChromatticManager.class);
      RequestLifeCycle.begin(chromatticManager);
      try {
        setTemplatePagePlugin();
      } catch (Exception e) {
        log.warn("Can not init page templates ...");
      }
      addEmotionIcons();
      removeDraftPages();
      removeHelpPages();
      try {
        getWikiHome(PortalConfig.GROUP_TYPE, "sandbox");
      } catch (Exception e) {
        log.warn("Can not init sandbox wiki ...");
      }
    } catch (Exception e) {
      log.warn("Can not start WikiService ...", e);
    } finally {
      RequestLifeCycle.end();
    }
  }

  @Override
  public void stop() {
    
  }
  
}
