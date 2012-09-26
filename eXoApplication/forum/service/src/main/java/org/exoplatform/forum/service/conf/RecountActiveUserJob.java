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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.job.MultiTenancyJob;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

public class RecountActiveUserJob extends MultiTenancyJob {
  private static Log log_ = ExoLogger.getLogger("job.forum.RecountActiveUserJob");

  @Override
  public Class<? extends MultiTenancyTask> getTask() {
    return RecountActiveUserTask.class;
  }

  public class RecountActiveUserTask extends MultiTenancyTask {

    public RecountActiveUserTask(JobExecutionContext context, String repoName) {
      super(context, repoName);
    }

    @Override
    public void run() {
      super.run();
      try {
        ForumService forumService = (ForumService) container.getComponentInstanceOfType(ForumService.class);
        if (forumService != null) {
          JobDataMap jdatamap = context.getJobDetail().getJobDataMap();
          String lastPost = jdatamap.getString("lastPost");
          if (lastPost != null && lastPost.length() > 0) {
            int days = Integer.parseInt(lastPost);
            if (days > 0) {
              long oneDay = 86400000; // milliseconds of one day
              Calendar calendar = GregorianCalendar.getInstance();
              long currentDay = calendar.getTimeInMillis();
              currentDay = currentDay - (days * oneDay);
              calendar.setTimeInMillis(currentDay);
              StringBuilder stringBuilder = new StringBuilder();
              stringBuilder.append("//element(*,")
                           .append(Utils.USER_PROFILES_TYPE)
                           .append(")[")
                           .append("@exo:lastPostDate >= xs:dateTime('")
                           .append(ISO8601.format(calendar))
                           .append("')]");
              forumService.evaluateActiveUsers(stringBuilder.toString());
              if (log_.isDebugEnabled()) {
                log_.debug("\n\n The RecoundActiveUserJob have been done");
              }
            }
          }
        }
      } catch (NumberFormatException nfe) {
        log_.debug("Value of days is not Integer number.", nfe);
      } catch (Exception e) {
        if (log_.isDebugEnabled()) {
          log_.debug("\n\n The have exception " + e.getMessage());
        }
      }
    }
  }
}
