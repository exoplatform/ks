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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.job.MultiTenancyJob;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.quartz.JobExecutionContext;

public class UpdateUserProfileJob extends MultiTenancyJob {
  private static Log log_ = ExoLogger.getLogger(UpdateUserProfileJob.class);

  public UpdateUserProfileJob() throws Exception {
  }
  
  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return UpdateUserProfileTask.class;
  }
  
  public class UpdateUserProfileTask extends MultiTenancyTask {

    public UpdateUserProfileTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
    }
    
    @Override
    public void run() {
      super.run();
      ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
      try {
        ExoContainer exoContainer = CommonUtils.getExoContainer(context);
        ForumService forumService = (ForumService) exoContainer.getComponentInstanceOfType(ForumService.class);
        ExoContainerContext.setCurrentContainer(exoContainer);
        String name = context.getJobDetail().getName();
        JobSchedulerService schedulerService = (JobSchedulerService) exoContainer.getComponentInstanceOfType(JobSchedulerService.class);
        JobInfo info = new JobInfo(name, "KnowledgeSuite-forum", context.getJobDetail().getJobClass());
        RepositoryService repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
        repositoryService.setCurrentRepositoryName(context.getJobDetail().getJobDataMap().getString(Utils.CACHE_REPO_NAME));
        forumService.updateUserProfileInfo(name);
        if (log_.isDebugEnabled()) {
          log_.debug("\n\nNumber of deleted posts, topics updated to Forum statistics and user's profile");
        }
        schedulerService.removeJob(info);
      } catch (Exception e) {
        log_.trace("User profile could not updated: " + "\n" + e.getCause());
      } finally {
        ExoContainerContext.setCurrentContainer(oldContainer);
      }
    }
    
    
  }

  

  
}
