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

import java.util.HashMap;

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
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * Created by The eXo Platform SAS
 * Author : phongth
 *          phongth@exoplatform.com
 * May 25, 2012  
 */
public class WikiPermissionRepairPlugin extends UpgradeProductPlugin {
  private static final Log Log = ExoLogger.getLogger(WikiPermissionRepairPlugin.class);

  public WikiPermissionRepairPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    Log.info("\n\nStart check and fix null entry permission of attachments...\n");
    try {
      fixPermissionEntryNull();
    } catch (Exception e) {
      Log.warn("[WikiPermissionRepairPlugin] Exception when fix null entry permission of attachments for wiki:", e);
    }
    Log.info("\n\nFinish check and fix null entry permission of attachments...\n");
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return VersionComparator.isBefore(previousVersion, newVersion);
  }
  
  public void fixPermissionEntryNull() {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MOWService.class);
    ChromatticSession session = mowService.getSession();
    QueryResult<AttachmentImpl> attachmentIterator = session.createQueryBuilder(AttachmentImpl.class).where("jcr:path LIKE '/%' AND not(fn:name()='content')").get().objects();
    
    Log.info("\nTotal attachments found: {}\n", attachmentIterator.size());
    int fixedAttachment = 0;
    while (attachmentIterator.hasNext()) {
      AttachmentImpl attachment = attachmentIterator.next();
      try {
        PageImpl parent = attachment.getParentPage();
        if (parent == null) {
          continue;
        }
        HashMap<String, String[]> permissions = attachment.getParentPage().getPermission();
        if (attachment.getCreator() != null) {
          permissions.put(attachment.getCreator(), org.exoplatform.services.jcr.access.PermissionType.ALL);
        }
        attachment.setPermission(permissions);
        fixedAttachment++;
        Log.info("\nFixed attachment: {}/{}\n", fixedAttachment, attachmentIterator.size());
      } catch (Exception e) {
        Log.warn(String.format("Can not repair the permission for attachment %s", attachment.getName()), e);
      }
    }
    RequestLifeCycle.end();
  }
}
