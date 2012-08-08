/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.ks.upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.query.QueryResult;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * Created by The eXo Platform SAS
 * Author : phongth
 *          phongth@exoplatform.com
 * Aug 08, 2012  
 */
public class WikiGroupPermissionRepairPlugin extends UpgradeProductPlugin {
  private static final Log LOG = ExoLogger.getLogger(WikiGroupPermissionRepairPlugin.class);

  public WikiGroupPermissionRepairPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    LOG.info("\n\nStart check and fix group permission of wiki pages...\n");
    try {
      fixGroupPermissionOfWikiPages();
    } catch (Exception e) {
      LOG.warn("Exception when fix  group permission of wiki pages:", e);
    }
    LOG.info("\n\nFinish check and fix group permission of wiki pages...\n");
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return VersionComparator.isBefore(previousVersion, newVersion);
  }
  
  private void fixGroupPermissionOfWikiPages() {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MOWService.class);
    ChromatticSession session = mowService.getSession();
    
    // Select all the wiki pages
    QueryResult<PageImpl> pageIterator = session.createQueryBuilder(PageImpl.class).where("jcr:path LIKE '/%'").get().objects();
    
    LOG.info("\nTotal pages found: {}\n", pageIterator.size());
    int checkedPage = 0;
    int fixedPage = 0;
    while (pageIterator.hasNext()) {
      PageImpl page = pageIterator.next();
      try {
        HashMap<String, String[]> permissions = page.getPermission();
        boolean isEditedPermission = false;
        
        Set<String> permissionsKey = permissions.keySet();
        List<String> permissionKeyList = new ArrayList<String>(permissionsKey);
        
        for (String id : permissionKeyList) {
          if ((id.indexOf('/') > -1) && (id.indexOf(':') == -1)) {
            String newId = "*:" + id;
            String[] value = permissions.get(id);
            permissions.remove(id);
            permissions.put(newId, value);
            LOG.info("\nRepaired: {} to {}\n", id, newId);
            isEditedPermission = true;
          }
        }
        
        if (isEditedPermission) {
          page.setPermission(permissions);
          fixedPage++;
          LOG.info("\nFixed pages: {}\n", fixedPage);
        }
        checkedPage++;
        LOG.info("\nChecked pages: {}/{}\n", checkedPage, pageIterator.size());
      } catch (Exception e) {
        LOG.warn(String.format("Can not repair the permission for page %s", page.getName()), e);
      }
    }
    RequestLifeCycle.end();
  }
}
