/**
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
 **/
package org.exoplatform.forum.service.conf;

import org.exoplatform.forum.service.ForumService;
import org.exoplatform.job.MultiTenancyJob;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.JobExecutionContext;

public class LoginJob extends MultiTenancyJob {
  private static Log log_ = ExoLogger.getLogger(LoginJob.class);

  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return LoginTask.class;
  }

  public class LoginTask extends MultiTenancyTask {

    public LoginTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
    }

    @Override
    public void run() {
      super.run();
      try {
        ForumService forumService = (ForumService) container.getComponentInstanceOfType(ForumService.class);
        forumService.updateLoggedinUsers();
      } catch (Exception e) {
        log_.warn("Period login job can not execute ...");
      }
      if (log_.isDebugEnabled()) {
        log_.info("\n\nForum Statistic has been updated for logged in user by a period login job");
      }
    }
  }
}
