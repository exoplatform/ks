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
package org.exoplatform.forum.service.conf;

import org.exoplatform.forum.service.ForumService;
import org.exoplatform.ks.common.job.MultiTenancyJob;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.JobExecutionContext;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class DelayWritesJob extends MultiTenancyJob {

  private static Log log_ = ExoLogger.getLogger(DelayWritesJob.class);
  
  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return UpdateViewTask.class;
  }

  public class UpdateViewTask extends MultiTenancyTask {

    public UpdateViewTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
    }

    @Override
    public void run() {
      super.run();
      try {
        ForumService forumService = (ForumService) container.getComponentInstanceOfType(ForumService.class);
        forumService.writeViews();
        forumService.writeReads();
      } catch (Exception e) {
        log_.warn("Update view can not execute ...");
      }
      if (log_.isDebugEnabled()) {
        log_.info("\n\nUpdate view has been updated by a period login job");
      }
    }
  }
  
}
