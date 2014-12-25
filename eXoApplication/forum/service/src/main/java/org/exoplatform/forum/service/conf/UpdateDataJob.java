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
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class UpdateDataJob extends MultiTenancyJob {
  private static Log log_ = ExoLogger.getLogger("job.RecordsJob");

  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return UpdateDataTask.class;
  }

  public class UpdateDataTask extends MultiTenancyTask {

    public UpdateDataTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
    }

    @Override
    public void run() {
      super.run();
      try {
        ForumService forumService = (ForumService) container.getComponentInstanceOfType(ForumService.class);
        String name = context.getJobDetail().getName();
        JobDataMap jdatamap = context.getJobDetail().getJobDataMap();
        String path = jdatamap.getString("path");
        forumService.updateForum(path);
        JobSchedulerService schedulerService = (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
        JobInfo info = new JobInfo(name, "KnowledgeSuite-forum", context.getJobDetail().getJobClass());
        if (log_.isDebugEnabled()) {
          log_.debug("\n\nForum statistic updated");
        }
        schedulerService.removeJob(info);
      } catch (Exception e) {
        log_.trace("\nStatistic Forum could not updated: " + "\n" + e.getCause());
      }
    }
  }
}
