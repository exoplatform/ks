/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.forum.service;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Nov 23, 2007 3:09:21 PM
 */
public class AuthenticationLogoutListener extends Listener<ConversationRegistry, ConversationState> {

  private ExoContainerContext context;
  private final static Log   LOG                   = ExoLogger.getLogger(Utils.class);

  public AuthenticationLogoutListener(ExoContainerContext context) throws Exception {
    this.context = context;
  }

  @Override
  public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    ForumService fservice = (ForumService) container.getComponentInstanceOfType(ForumService.class);
    if (fservice == null) {
      LOG.warn("Can not get forum service!");
      return;
    }
    fservice.userLogout(event.getData().getIdentity().getUserId());

  }
}
