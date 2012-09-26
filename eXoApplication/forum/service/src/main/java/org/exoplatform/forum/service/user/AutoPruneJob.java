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
package org.exoplatform.forum.service.user;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.job.MultiTenancyJob;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.JobExecutionContext;

public class AutoPruneJob extends MultiTenancyJob {
  private static Log log_ = ExoLogger.getLogger("job.RecordsJob");

  public AutoPruneJob() throws Exception {
  }

  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return AutoPruneTask.class;
  }
  
  public class AutoPruneTask extends MultiTenancyTask {

    public AutoPruneTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
    }
    
    @Override
    public void run() {
      super.run();
      ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
      try {
        ExoContainer container = CommonUtils.getExoContainer(context);
        String desc = context.getJobDetail().getDescription();
        ForumService forumService = (ForumService) container.getComponentInstanceOfType(ForumService.class);
        ExoContainerContext.setCurrentContainer(container);
        RepositoryService repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
        repositoryService.setCurrentRepositoryName(context.getJobDetail().getJobDataMap().getString(Utils.CACHE_REPO_NAME));
        forumService.runPrune(desc);
        if (log_.isDebugEnabled()) {
          log_.debug("\n\nAuto prune has worked on " + desc + " forum");
        }
      } catch (Exception e) {
        log_.debug("\n\n >>>>>> AutoPrune Job error" + e.getMessage());
      } finally {
        ExoContainerContext.setCurrentContainer(oldContainer);
      }
    }
    
  }
  
}
