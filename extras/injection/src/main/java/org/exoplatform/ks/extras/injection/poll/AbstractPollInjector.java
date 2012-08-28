package org.exoplatform.ks.extras.injection.poll;

import java.util.HashMap;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ks.extras.injection.utils.LoremIpsum4J;
import org.exoplatform.ks.extras.injection.utils.ExoNameGenerator;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.PollNodeTypes;
import org.exoplatform.poll.service.PollService;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;

public abstract class AbstractPollInjector extends DataInjector {

  public static final String           POLL_DEFAULT_TYPE           = "public";

  public static final String           DEFAULT_POLL_PRIVATE_PREFIX = "private.poll";

  public static final String           DEFAULT_POLL_PUBLIC_PREFIX  = "public.poll";

  public static final String           DEFAULT_GROUP_PREFIX        = "bench.group";

  protected static final int           OPTION_DEFAULT_SIZE         = 5;

  protected static PollService         pollService;

  protected static ForumService        forumService;

  protected final KSDataLocation       locator;

  protected static OrganizationService organizationService;

  protected GroupHandler               groupHandler;

  protected String                     groupBase;

  protected int                        groupNumber                 = 0;

  protected String                     pollPublicBase;

  protected String                     pollPrivateBase;

  protected int                        pollPrivateNumber;

  protected int                        pollPublicNumber;

  protected String                     optionBase;

  protected int                        optionNumber;

  protected Random                     random                      = new Random();

  protected ExoNameGenerator exoNameGenerator = new ExoNameGenerator();

  protected LoremIpsum4J               lorem                       = new LoremIpsum4J();

  private static final Log             LOG                         = ExoLogger.getLogger(AbstractPollInjector.class);

  public static final String           POLL_TYPE                   = "type";

  public AbstractPollInjector() {
    PortalContainer container = PortalContainer.getInstance();
    pollService = (PollService) container.getComponentInstance(PollService.class);
    forumService = (ForumService) container.getComponentInstance(ForumService.class);
    locator = (KSDataLocation) container.getComponentInstance(KSDataLocation.class);
    organizationService = (OrganizationService) container.getComponentInstance(OrganizationService.class);

    groupHandler = organizationService.getGroupHandler();
  }

  @Override
  public Log getLog() {
    return ExoLogger.getExoLogger(this.getClass());
  }

  @Override
  public Object execute(HashMap<String, String> stringHashMap) throws Exception {
    return null;
  }

  @Override
  public void reject(HashMap<String, String> stringHashMap) throws Exception {

  }

  protected String pollPublicName() {
    return pollPublicBase + pollPublicNumber;
  }

  protected String pollPrivateName() {
    return pollPrivateBase + pollPrivateNumber;
  }

  protected String groupName() {
    return groupBase + groupNumber;
  }

  public void init(String groupPrefix,
                   String pollPublicPrefix,
                   String pollPrivatePrefix,
                   String toGroup) {
    //
    groupBase = (groupPrefix == null ? DEFAULT_GROUP_PREFIX : groupPrefix);
    pollPrivateBase = (pollPrivatePrefix == null ? DEFAULT_POLL_PRIVATE_PREFIX : pollPrivatePrefix);
    pollPublicBase = (pollPublicPrefix == null ? DEFAULT_POLL_PUBLIC_PREFIX : pollPublicPrefix);

    try {
      groupNumber = groupNumber(groupBase);
      pollPrivateNumber = pollNumberForPrivate(groupBase + (toGroup == null ? "" : toGroup), pollPrivateBase);
      pollPublicNumber = pollNumberForPublic(pollPublicBase);
    } catch (Exception e) {
      // If no user is existing, set keep 0 as value.
    }

    //
    LOG.info("Initial group number : " + groupNumber);
    LOG.info("Initial poll private number : " + pollPrivateNumber);
    LOG.info("Initial poll public number : " + pollPublicNumber);

  }

  public int groupNumber(String base) throws Exception {
    int count = 0;
    for (Object gr : groupHandler.findGroups(null)) {
      if (((Group) gr).getGroupName().contains(base)) {
        count++;
      }
    }
    return count;
  }

  public Group getGroupByName(String groupName) throws Exception {
    return groupHandler.findGroupById("/" + groupName);
  }

  public Poll getPublicPollByName(String pollName) throws Exception {
    try {
      StringBuffer sb = new StringBuffer(PollNodeTypes.JCR_ROOT);
      String rootPath = KSDataLocation.Locations.DEFAULT_APPS_LOCATION;
      sb.append("/")
        .append(rootPath)
        .append("//element(*,")
        .append(PollNodeTypes.EXO_POLL)
        .append(")");
      sb.append("[(@exo:question='").append(pollName).append("')]");
      NodeIterator it = forumService.search(sb.toString());
      if (it.hasNext()) {
        Node pollNode = it.nextNode();
        return getPollNode(pollNode);
      } else {
        return null;
      }
    } catch (Exception e) {
      return null;
    }
  }

  private Poll getPollNode(Node pollNode) throws Exception {
    Poll pollNew = new Poll();
    pollNew.setId(pollNode.getName());
    pollNew.setParentPath(pollNode.getParent().getPath());
    PropertyReader reader = new PropertyReader(pollNode);
    pollNew.setQuestion(reader.string(PollNodeTypes.EXO_QUESTION));
    pollNew.setOption(reader.strings(PollNodeTypes.EXO_OPTION, new String[] {}));
    pollNew.setVote(reader.strings(PollNodeTypes.EXO_VOTE, new String[] {}));
    pollNew.setIsMultiCheck(reader.bool(PollNodeTypes.EXO_IS_MULTI_CHECK));
    return pollNew;
  }

  public Poll getPrivatePollByName(String groupName, String pollName) throws Exception {
    try {
      StringBuffer sb = new StringBuffer(PollNodeTypes.JCR_ROOT);
      String rootPath = PollNodeTypes.GROUPS + "/" + groupName;
      sb.append("/")
        .append(rootPath)
        .append("//element(*,")
        .append(PollNodeTypes.EXO_POLL)
        .append(")");
      sb.append("[(@exo:question='").append(pollName).append("')]");
      NodeIterator it = forumService.search(sb.toString());
      if (it.hasNext()) {
        Node pollNode = it.nextNode();
        return getPollNode(pollNode);
      } else {
        return null;
      }
    } catch (Exception e) {
      return null;
    }
  }

  public int pollNumberForPrivate(String groupName, String pollBase) throws Exception {
    try {
      StringBuffer sb = new StringBuffer(PollNodeTypes.JCR_ROOT);
      String rootPath = PollNodeTypes.GROUPS + "/" + groupName;

      sb.append("/").append(rootPath).append("//element(*,");
      sb.append(PollNodeTypes.EXO_POLL)
        .append(")[jcr:like(exo:question, '%")
        .append(pollBase)
        .append("%')]");
      return (int) forumService.search(sb.toString()).getSize();

    } catch (Exception e) {
      return 0;
    }
  }

  public int pollNumberForPublic(String pollBase) throws Exception {
    try {
      StringBuffer sb = new StringBuffer(PollNodeTypes.JCR_ROOT);
      String rootPath = KSDataLocation.Locations.DEFAULT_APPS_LOCATION;

      sb.append("/").append(rootPath).append("//element(*,");
      sb.append(PollNodeTypes.EXO_POLL)
        .append(")[jcr:like(exo:question, '%")
        .append(pollBase)
        .append("%')]");
      return (int) forumService.search(sb.toString()).getSize();

    } catch (Exception e) {
      return 0;
    }
  }

  protected static String getStringValueParam(HashMap<String, String> params,
                                              String key,
                                              String defaultValue) {
    if (params == null) {
      throw new NullPointerException();
    }
    String vl = params.get(key);
    if (vl == null) {
      return defaultValue;
    }
    return vl;
  }

  protected int getIntParam(HashMap<String, String> params, String name) {

    //
    if (params == null) {
      throw new NullPointerException();
    }

    //
    if (name == null) {
      throw new NullPointerException();
    }

    //
    try {
      String value = params.get(name);
      if (value != null) {
        return Integer.valueOf(value);
      }
    } catch (NumberFormatException e) {
      LOG.warn("Integer number expected for property " + name);
    }
    return 0;

  }

}
