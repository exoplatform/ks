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

package org.exoplatform.poll.service.impl;

import java.util.List;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.poll.service.DataStorage;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.PollService;
import org.exoplatform.poll.service.PollSummary;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

@NameTemplate(@Property(key = "service", value = "poll"))
public class PollServiceImpl implements Startable, PollService {
  private DataStorage storage_;

  private KSDataLocation   dataLocator;

  private static final Log log = ExoLogger.getLogger(PollServiceImpl.class);

  public PollServiceImpl(InitParams params, KSDataLocation locator, DataStorage storage) throws Exception {
    this.dataLocator = locator;
    storage_ = storage;
  }

  public void addInitialDefaultDataPlugin(ComponentPlugin plugin) throws Exception {
    storage_.addInitialDefaultDataPlugin(plugin);
  }

  public void start() {
    try {
      log.info("initializing Poll default data...");
      storage_.initDefaultData();
    } catch (Exception e) {
      log.error("Failed to initializing default data for poll: ", e);
    }
  }

  public void stop() {
  }

  public Poll getPoll(String pollId) throws Exception {
    return storage_.getPoll(pollId);
  }

  public Poll removePoll(String pollId) {
    return storage_.removePoll(pollId);
  }

  public void savePoll(Poll poll, boolean isNew, boolean isVote) throws Exception {
    storage_.savePoll(poll, isNew, isVote);
  }

  public void setClosedPoll(Poll poll){
    storage_.setClosedPoll(poll);
  }

  public List<Poll> getPagePoll() throws Exception {
    return storage_.getPagePoll();
  }

  public boolean hasPermissionInForum(String pollPath, List<String> allInfoOfUser) throws Exception {
    return storage_.hasPermissionInForum(pollPath, allInfoOfUser);
  }
  
  public PollSummary getPollSummary(List<String> groupOfUser) throws Exception {
    return storage_.getPollSummary(groupOfUser);
  }

}
