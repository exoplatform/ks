package org.exoplatform.poll.service.cache;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.ks.common.cache.CacheType;
import org.exoplatform.ks.common.cache.ScopeCacheKey;
import org.exoplatform.ks.common.cache.loader.ServiceContext;
import org.exoplatform.ks.common.cache.selector.ScopeCacheSelector;
import org.exoplatform.poll.service.DataStorage;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.PollSummary;
import org.exoplatform.poll.service.cache.model.data.ListPollData;
import org.exoplatform.poll.service.cache.model.data.PollData;
import org.exoplatform.poll.service.cache.model.data.PollSumarryData;
import org.exoplatform.poll.service.cache.model.key.ListCacheKey;
import org.exoplatform.poll.service.cache.model.key.PollKey;
import org.exoplatform.poll.service.impl.JCRDataStorage;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.future.FutureExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class CachedDataStorage implements DataStorage {

  private final DataStorage storage;
  private final CacheService service;


  private ExoCache<PollKey, PollData> pollData;
  private ExoCache<ScopeCacheKey, ListPollData> pollList;
  private ExoCache<ListCacheKey<String>, PollSumarryData> pollSummaryData;

  private FutureExoCache<PollKey, PollData, ServiceContext<PollData>> pollDataFuture;
  private FutureExoCache<ScopeCacheKey, ListPollData, ServiceContext<ListPollData>> pollListFuture;
  private FutureExoCache<ListCacheKey<String>, PollSumarryData, ServiceContext<PollSumarryData>> pollSummaryFuture;

  private static final Log LOG = ExoLogger.getLogger(CachedDataStorage.class);

  public CachedDataStorage(JCRDataStorage storage, CacheService service) {

    this.storage = storage;
    this.service = service;

    pollData = CacheType.POLL_DATA.getFromService(service);
    pollList = CacheType.POLL_LIST.getFromService(service);
    pollSummaryData = CacheType.POLL_SUMMARY_DATA.getFromService(service);

    pollDataFuture = CacheType.POLL_DATA.createFutureCache(pollData);
    pollListFuture = CacheType.POLL_LIST.createFutureCache(pollList);
    pollSummaryFuture = CacheType.POLL_SUMMARY_DATA.createFutureCache(pollSummaryData);

  }

  private ListPollData buildPollInput(List<Poll> polls) {
    List<PollKey> keys = new ArrayList<PollKey>();
    for (Poll p : polls) {
      keys.add(new PollKey(p));
    }
    return new ListPollData(keys);
  }

  private List<Poll> buildPollOutput(ListPollData data) {

    if (data == null) {
      return null;
    }

    List<Poll> out = new ArrayList<Poll>();
    for (PollKey k : data.getIds()) {
      try {
        out.add(getPoll(k.getId()));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return out;

  }

  public void addInitialDefaultDataPlugin(ComponentPlugin plugin) throws Exception {
    storage.addInitialDefaultDataPlugin(plugin);
  }

  public void initDefaultData() throws Exception {
    storage.initDefaultData();
  }

  public Poll getPoll(final String pollId) throws Exception {

    PollKey key = new PollKey(pollId);
    
    return pollDataFuture.get(
      new ServiceContext<PollData>() {
        public PollData execute() {
          try {
            return new PollData(storage.getPoll(pollId));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      },
      key
    ).build();

  }

  public void savePoll(Poll poll, boolean isNew, boolean isVote) throws Exception {

    storage.savePoll(poll, isNew, isVote);
    if (!isNew) {
      pollData.put(new PollKey(poll), new PollData(poll));
    }
    pollList.select(new ScopeCacheSelector<ScopeCacheKey, ListPollData>());

  }

  public Poll removePoll(String pollId) {

    pollData.remove(new PollKey(pollId));
    try {
      pollList.select(new ScopeCacheSelector<ScopeCacheKey, ListPollData>());
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return storage.removePoll(pollId);

  }

  public void setClosedPoll(Poll poll) {

    pollData.remove(new PollKey(poll));
    try {
      pollList.select(new ScopeCacheSelector<ScopeCacheKey, ListPollData>());
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    storage.setClosedPoll(poll);

  }

  public List<Poll> getPagePoll() throws Exception {

    return buildPollOutput(pollListFuture.get(
      new ServiceContext<ListPollData>() {
        public ListPollData execute() {
          try {
            return buildPollInput(storage.getPagePoll());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      },
      new ScopeCacheKey()
    ));
    
  }

  public boolean hasPermissionInForum(String pollPath, List<String> allInfoOfUser) throws Exception {
    return storage.hasPermissionInForum(pollPath, allInfoOfUser);
  }

  public PollSummary getPollSummary(final List<String> groupOfUser) throws Exception {

    ListCacheKey<String> key = new ListCacheKey(groupOfUser);

    return pollSummaryFuture.get(
      new ServiceContext<PollSumarryData>() {
        public PollSumarryData execute() {
          try {
            return new PollSumarryData(storage.getPollSummary(groupOfUser));
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      },
      key
    ).build();

  }
  
}
