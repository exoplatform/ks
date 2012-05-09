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
package org.exoplatform.wiki.resolver;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * May 3, 2012
 */
public class MockUserPortalConfigService extends UserPortalConfigService {

  /**
   * @param userACL
   * @param storage
   * @param orgService
   * @param navService
   * @param descriptionService
   * @param params
   * @throws Exception
   */
  public MockUserPortalConfigService(UserACL userACL,
                                     DataStorage storage,
                                     OrganizationService orgService,
                                     NavigationService navService,
                                     DescriptionService descriptionService,
                                     InitParams params) throws Exception {
    super(userACL, storage, orgService, navService, descriptionService, params);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.UserPortalConfigService#getPage(java.lang.String, java.lang.String)
   */
  @Override
  public Page getPage(String pageId, String accessUser) throws Exception {
    return super.getPage(pageId, accessUser);
  }

}
