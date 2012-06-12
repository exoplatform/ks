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
package org.exoplatform.wiki.commons;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.Preferences;
import org.exoplatform.wiki.mow.core.api.wiki.WikiImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.impl.RenderingServiceImpl;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.service.Permission;
import org.exoplatform.wiki.service.PermissionEntry;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.impl.SessionManager;
import org.exoplatform.wiki.tree.utils.TreeUtils;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiRichTextArea;
import org.exoplatform.wiki.webui.WikiMode;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 22, 2010  
 */
public class Utils {  
 
  public static final int DEFAULT_VALUE_UPLOAD_PORTAL = -1;
  
  public static String getCurrentRequestURL() throws Exception {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    HttpServletRequest request = portalRequestContext.getRequest();
    String requestURL = request.getRequestURL().toString();
    UIPortal uiPortal = Util.getUIPortal();
    String pageNodeSelected = uiPortal.getSelectedUserNode().getURI();
    if (!requestURL.contains(pageNodeSelected)) {
      // Happens at the first time processRender() called when add wiki portlet manually
      requestURL = portalRequestContext + pageNodeSelected;
    }      
    return requestURL;
  }

  public static WikiPageParams getCurrentWikiPageParams() throws Exception {
    String requestURL = getCurrentRequestURL();
    PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
    WikiPageParams params = pageResolver.extractWikiPageParams(requestURL, Util.getUIPortal().getSelectedUserNode());
    HttpServletRequest request = Util.getPortalRequestContext().getRequest();
    Map<String, String[]> paramsMap = request.getParameterMap();
    params.setParameters(paramsMap);
    return params;
  }

  public static Page getCurrentWikiPage() throws Exception {
    String requestURL = Utils.getCurrentRequestURL();
    PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
    Page page = pageResolver.resolve(requestURL, Util.getUIPortal().getSelectedUserNode());
    return page;
  }
  
  public static String getURLFromParams(WikiPageParams params) throws Exception {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    String requestURL = portalRequestContext.getRequest().getRequestURL().toString();
    String portalURI = portalRequestContext.getPortalURI();
    String domainURL = requestURL.substring(0, requestURL.indexOf(portalURI));

    UIPortal uiPortal = Util.getUIPortal();
    String pageNodeSelected = uiPortal.getSelectedUserNode().getURI();
    StringBuilder sb = new StringBuilder(domainURL);
    sb.append(portalURI);
    sb.append(pageNodeSelected);
    sb.append("/");
    if (params == null) {
      return sb.toString();
    }
    if (params.getType() != null && !PortalConfig.PORTAL_TYPE.equalsIgnoreCase(params.getType())) {
      sb.append(params.getType().toLowerCase());
      sb.append("/");
      sb.append(org.exoplatform.wiki.utils.Utils.validateWikiOwner(params.getType(), params.getOwner()));
      sb.append("/");
    }
    
    if (params.getPageId() != null) {
      sb.append(URLEncoder.encode(params.getPageId(), "UTF-8"));
    }
    return sb.toString();
  }
  
  public static Page getCurrentNewDraftWikiPage() throws Exception {
    WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
    String sessionId = Util.getPortalRequestContext().getRequest().getSession(false).getId();
    return wikiService.getExsitedOrNewDraftPageById(null, null, sessionId);
  }
  
  public static String getExtension(String filename)throws Exception {
    MimeTypeResolver mimeResolver = new MimeTypeResolver() ;
    try{
      return mimeResolver.getExtension(mimeResolver.getMimeType(filename)) ;
    }catch(Exception e) {
      return mimeResolver.getDefaultMimeType() ;
    }
  }
  
  public static Wiki getCurrentWiki() throws Exception {
    MOWService mowService = (MOWService) PortalContainer.getComponent(MOWService.class);
    WikiStoreImpl store = (WikiStoreImpl) mowService.getModel().getWikiStore();
    String wikiType=  Utils.getCurrentWikiPageParams().getType();
    String owner=  Utils.getCurrentWikiPageParams().getOwner();
    return store.getWiki(WikiType.valueOf(wikiType.toUpperCase()), owner);    
  }

  public static WikiContext setUpWikiContext(UIWikiPortlet wikiPortlet) throws Exception {
    RenderingService renderingService = (RenderingService) ExoContainerContext.getCurrentContainer()
                                                                              .getComponentInstanceOfType(RenderingService.class);
    Execution ec = ((RenderingServiceImpl) renderingService).getExecution();
    if (ec.getContext() == null) {
      ec.setContext(new ExecutionContext());
    }
    WikiContext wikiContext = createWikiContext(wikiPortlet);
    ec.getContext().setProperty(WikiContext.WIKICONTEXT, wikiContext);
    return wikiContext;
  }
  
  public static void feedDataForWYSIWYGEditor(UIWikiPageEditForm pageEditForm, String markup) throws Exception {
    UIWikiPortlet wikiPortlet = pageEditForm.getAncestorOfType(UIWikiPortlet.class);
    UIWikiRichTextArea richTextArea = pageEditForm.getChild(UIWikiRichTextArea.class);
    RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
    HttpSession session = Util.getPortalRequestContext().getRequest().getSession(false);
    UIFormTextAreaInput markupInput = pageEditForm.getUIFormTextAreaInput(UIWikiPageEditForm.FIELD_CONTENT);
    String markupSyntax = getDefaultSyntax();
    WikiContext wikiContext= Utils.setUpWikiContext(wikiPortlet);
    if (markup == null) {
      markup = (markupInput.getValue() == null) ? "" : markupInput.getValue();
    }
    String xhtmlContent = renderingService.render(markup, markupSyntax, Syntax.ANNOTATED_XHTML_1_0.toIdString(), false);
    richTextArea.getUIFormTextAreaInput().setValue(xhtmlContent);
    session.setAttribute(UIWikiRichTextArea.SESSION_KEY, xhtmlContent);
    session.setAttribute(UIWikiRichTextArea.WIKI_CONTEXT, wikiContext);
    SessionManager sessionManager = (SessionManager) RootContainer.getComponent(SessionManager.class);
    sessionManager.addSessionContext(session.getId(), Utils.createWikiContext(wikiPortlet));
  }

  public static String getCurrentWikiPagePath() throws Exception {
    return TreeUtils.getPathFromPageParams(getCurrentWikiPageParams());
  }
  
  public static String getDefaultSyntax() throws Exception {
    String currentDefaultSyntaxt = Utils.getCurrentPreferences().getPreferencesSyntax().getDefaultSyntax();
    if (currentDefaultSyntaxt == null) {
      WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
      currentDefaultSyntaxt = wservice.getDefaultWikiSyntaxId();
    }
    return currentDefaultSyntaxt;
  }
  
  public static Preferences getCurrentPreferences() throws Exception {
    WikiImpl currentWiki = (WikiImpl) getCurrentWiki();
    return currentWiki.getPreferences();
  }
 
  public static WikiContext createWikiContext(UIWikiPortlet wikiPortlet) throws Exception {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    WikiMode currentMode = wikiPortlet.getWikiMode();
    List<WikiMode> editModes = Arrays.asList(new WikiMode[] { WikiMode.EDITPAGE, WikiMode.ADDPAGE, WikiMode.EDITTEMPLATE,
        WikiMode.ADDTEMPLATE });
    UIPortal uiPortal = Util.getUIPortal();
    String requestURL = portalRequestContext.getRequest().getRequestURL().toString();
    String portalURI = portalRequestContext.getPortalURI();
    String domainURL = requestURL.substring(0, requestURL.indexOf(portalURI));
    String portalURL = domainURL + portalURI;
    String pageNodeSelected = uiPortal.getSelectedUserNode().getURI();
    String treeRestURL = getCurrentRestURL().concat("/wiki/tree/children/");
    
    WikiContext wikiContext = new WikiContext();
    wikiContext.setPortalURL(portalURL);
    wikiContext.setTreeRestURI(treeRestURL);
    wikiContext.setPageTreeId(IdGenerator.generate());
    wikiContext.setRestURI(getCurrentRestURL());
    wikiContext.setRedirectURI(wikiPortlet.getRedirectURL());
    wikiContext.setPortletURI(pageNodeSelected);
    WikiPageParams params = Utils.getCurrentWikiPageParams();    
    wikiContext.setType(params.getType());
    wikiContext.setOwner(params.getOwner());
    if (editModes.contains(currentMode)) {
      wikiContext.setSyntax(getDefaultSyntax());
    } else {
      WikiService service = (WikiService) PortalContainer.getComponent(WikiService.class);
      Page currentPage = service.getPageById(params.getType(), params.getOwner(), params.getPageId());
      if (currentPage != null) {
        wikiContext.setSyntax(currentPage.getSyntax());
      }
    }
    if (wikiPortlet.getWikiMode() == WikiMode.ADDPAGE) {
      String sessionId = Util.getPortalRequestContext().getRequest().getSession(false).getId();
      wikiContext.setPageId(sessionId);
    } else {
      wikiContext.setPageId(params.getPageId());
    }
    wikiContext.setBaseUrl(getBaseUrl());

    return wikiContext;
  }
  
  public static String getBaseUrl() throws Exception {
    WikiPageParams params = getCurrentWikiPageParams();
    params.setPageId(null);
    return getURLFromParams(params);
  }
  
  public static String getCurrentWikiNodeUri() throws Exception {    
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    StringBuilder sb = new StringBuilder(portalRequestContext.getPortalURI());
    UIPortal uiPortal = Util.getUIPortal();
    String pageNodeSelected = uiPortal.getSelectedUserNode().getURI();
    sb.append(pageNodeSelected);   
    return sb.toString();
  }

  public static void redirect(WikiPageParams pageParams, WikiMode mode) throws Exception {
    redirect(pageParams, mode, null);
  }

  public static void redirect(WikiPageParams pageParams, WikiMode mode, Map<String, String[]> params) throws Exception {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    portalRequestContext.setResponseComplete(true);
    portalRequestContext.sendRedirect(createURLWithMode(pageParams, mode, params));
  }
  
  public static void ajaxRedirect(Event<? extends UIComponent> event,
                                  WikiPageParams pageParams,
                                  WikiMode mode,
                                  Map<String, String[]> params) throws Exception {
    String redirectLink = Utils.createURLWithMode(pageParams, mode, params);
    event.getRequestContext().getJavascriptManager().addCustomizedOnLoadScript("ajaxRedirect('"
        + redirectLink + "');");
  }
  
  public static String createURLWithMode(WikiPageParams pageParams,
                                         WikiMode mode,
                                         Map<String, String[]> params) throws Exception {
    StringBuffer sb = new StringBuffer();
    sb.append(getURLFromParams(pageParams));
    if (!mode.equals(WikiMode.VIEW)) {
      sb.append("#").append(Utils.getActionFromWikiMode(mode));
    }
    if (params != null) {
      Iterator<Entry<String, String[]>> iter = params.entrySet().iterator();
      while (iter.hasNext()) {
        Entry<String, String[]> entry = iter.next();
        sb.append("&");
        sb.append(entry.getKey()).append("=").append(entry.getValue()[0]);
      }
    }
    return sb.toString();
  }

  
  public static String createFormActionLink(UIComponent uiComponent,
                                          String action,
                                          String beanId) throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    boolean isForm = UIForm.class.isInstance(uiComponent);
    UIForm form = isForm ? (UIForm) uiComponent : uiComponent.getAncestorOfType(UIForm.class);
    if (form != null) {
      String formId = form.getId();
      if (context instanceof PortletRequestContext) {
        formId = ((PortletRequestContext) context).getWindowId() + "#" + formId;
      }
      StringBuilder b = new StringBuilder();

      b.append("javascript:eXo.wiki.UIForm.submitPageEvent('").append(formId).append("','");
      b.append(action).append("','");
      if (!isForm) {
        b.append("&amp;").append(UIForm.SUBCOMPONENT_ID).append("=").append(uiComponent.getId());
        if (beanId != null) {
          b.append("&amp;").append(UIComponent.OBJECTID).append("=").append(beanId);
        }
      }
      b.append("')");
      return b.toString();
    } else {
      return form.event(action, uiComponent.getId(), action);
    }
  }

  public static String getActionFromWikiMode(WikiMode mode) {
    switch (mode) {
    case EDITPAGE:
      return "EditPage";
    case ADDPAGE:
      return "AddPage";
    case DELETEPAGE:
      return "DeletePage";
    case ADDTEMPLATE:
      return "AddTemplate";
    case EDITTEMPLATE:
      return "EditTemplate";
    case SPACESETTING:
      return "SpaceSetting";
    default:
      return "";
    }
  }

  public static String getCurrentRestURL() {
    StringBuilder sb = new StringBuilder();
    sb.append("/").append(PortalContainer.getCurrentPortalContainerName()).append("/");
    sb.append(PortalContainer.getCurrentRestContextName());
    return sb.toString();
  }
  
  public static boolean hasPermission(String[] permissions) throws Exception {
    UserACL userACL = Util.getUIPortalApplication().getApplicationComponent(UserACL.class);
    /*// If an user is the super user or in the administration group or has the
    // create portal permission then he has all permissions
    if (userACL.hasCreatePortalPermission()) {
      return true;
    }
    String expAdminGroup = userACL.getAdminGroups();
    if (expAdminGroup != null) {
      expAdminGroup = expAdminGroup.startsWith("/") ? expAdminGroup : "/" + expAdminGroup;
      if (userACL.isUserInGroup(expAdminGroup)) {
        return true;
      }
    }*/
    WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
    WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
    List<PermissionEntry> permissionEntries = wikiService.getWikiPermission(pageParams.getType(), pageParams.getOwner());
    ConversationState conversationState = ConversationState.getCurrent();
    Identity user = null;
    if (conversationState != null) {
      user = conversationState.getIdentity();
    } else {
      user = new Identity(IdentityConstants.ANONIM);
    }
    List<AccessControlEntry> aces = new ArrayList<AccessControlEntry>();
    for (PermissionEntry permissionEntry : permissionEntries) {
      Permission[] perms = permissionEntry.getPermissions();
      for (Permission perm : perms) {
        if (perm.isAllowed()) {
          AccessControlEntry ace = new AccessControlEntry(permissionEntry.getId(), perm.getPermissionType().toString());
          aces.add(ace);
        }
      }
    }
    AccessControlList acl = new AccessControlList(userACL.getSuperUser(), aces);
    return org.exoplatform.wiki.utils.Utils.hasPermission(acl, permissions, user);
  }
  
  public static WikiMode getModeFromAction(String actionParam) {
    String[] params = actionParam.split(WikiConstants.WITH);
    String name = params[0];
    if (name != null) {
      try {
        WikiMode mode = WikiMode.valueOf(name.toUpperCase());
        if (mode != null)
          return mode;
      } catch (IllegalArgumentException e) {
        return null;
      }
    }
    return null;
  }
  
  /**
   * render macro to XHtml string.
   * @param uiComponent - component that contain the macro.
   * @param macroName - name of macro
   * @param wikiSyntax - wiki syntax referred from {@link Syntax}
   * @return String in format {@link Syntax#XHTML_1_0}
   */
  public static String renderMacroToXHtml(UIComponent uiComponent, String macroName, String wikiSyntax) {
    try {
      RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
      setUpWikiContext(uiComponent.getAncestorOfType(UIWikiPortlet.class));
      String content= renderingService.render(macroName,
                                     wikiSyntax,
                                     Syntax.XHTML_1_0.toIdString(),
                                     false);      
      return content;
    } catch (Exception e) {
      return "";
    }
  }

  public static void removeWikiContext() throws Exception {
    RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
    Execution ec = ((RenderingServiceImpl) renderingService).getExecution();
    if (ec != null) {
      ec.removeContext();
    }
  }
  
  public static List<NTVersion> getCurrentPageRevisions() throws Exception {
    PageImpl wikipage = (PageImpl) getCurrentWikiPage();
    Iterator<NTVersion> iter = wikipage.getVersionableMixin().getVersionHistory().iterator();
    List<NTVersion> versionsList = new ArrayList<NTVersion>();
    while (iter.hasNext()) {
      NTVersion version = iter.next();
      if (!(WikiNodeType.Definition.ROOT_VERSION.equals(version.getName()))) {
        versionsList.add(version);
      }
    }
    Collections.sort(versionsList, new VersionNameComparatorDesc());
    return versionsList;
  }
  
  public static int getLimitUploadSize() {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    int limitMB = DEFAULT_VALUE_UPLOAD_PORTAL;
    try {
      limitMB = Integer.parseInt(portletPref.getValue("uploadFileSizeLimitMB", "").trim());
    } catch (Exception e) {
      limitMB = 10;
    }
    return limitMB;
  }
  
  public static String getFullName(String userId) {
    try {
      OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
      User user = organizationService.getUserHandler().findUserByName(userId);
      return user.getFullName();
    } catch (Exception e) {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      ResourceBundle res = context.getApplicationResourceBundle();
      return res.getString("UIWikiPortlet.label.Anonymous");
    }
  }
}
