/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.forum.service.impl.JCRDataStorage;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.ks.common.jcr.SessionManager;

/**
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since July 25, 2007
 **/
public class ForumPageList extends JCRPageList {
  private boolean        isQuery_   = false;

  private String         value_;

  private SessionManager sessionManager;

  private List           listValue_ = null;

  public ForumPageList(int pageSize, int size) {
    super(pageSize);
    setAvailablePage(size);
  };

  public ForumPageList(NodeIterator iter, int pageSize, String value, boolean isQuery) throws Exception {
    super(pageSize);
    value_ = value;
    isQuery_ = isQuery;
    this.sessionManager = ForumServiceUtils.getSessionManager();
    try {
      if (iter == null) {
        iter = setQuery(isQuery_, value_);
      }
      if (iter != null) {
        setAvailablePage((int) iter.getSize());
      }
      iter = null;
    } finally {
      this.sessionManager.closeSession();
    }
  }

  /**
   * Set ForumPageList for search user.
   * @param listResult  result which is returned from search proccessing.
   */
  public ForumPageList(List listResult) {
    super(listResult.size());
    isQuery_ = false;
    listValue_ = listResult;
    this.sessionManager = ForumServiceUtils.getSessionManager();
  }

  @SuppressWarnings("unchecked")
  protected void populateCurrentPage(int page) throws Exception {
    NodeIterator iter = setQuery(isQuery_, value_);
    setAvailablePage((int) iter.getSize());
    if (page == 0)
      currentPage_ = 0; // nasty trick for getAll()
    else
      checkAndSetPage(page);
    page = currentPage_;
    Node currentNode;
    long pageSize = 0;
    if (page > 0) {
      long position = 0;
      pageSize = getPageSize();
      if (page == 1)
        position = 0;
      else {
        position = (page - 1) * pageSize;
        iter.skip(position);
      }
    } else {
      pageSize = iter.getSize();
    }

    currentListPage_ = new ArrayList<Object>();
    for (int i = 0; i < pageSize; i++) {
      if (iter.hasNext()) {
        currentNode = iter.nextNode();
        if (currentNode.isNodeType("exo:post")) {
          currentListPage_.add(getPost(currentNode));
        } else if (currentNode.isNodeType(Utils.TYPE_TOPIC)) {
          currentListPage_.add(getTopic(currentNode));
        } else if (currentNode.isNodeType(Utils.USER_PROFILES_TYPE)) {
          currentListPage_.add(getUserProfile(currentNode));
        } else if (currentNode.isNodeType("exo:privateMessage")) {
          currentListPage_.add(getPrivateMessage(currentNode));
        }
      } else {
        break;
      }
    }
    iter = null;
    if (sessionManager.getCurrentSession() != null && sessionManager.getCurrentSession().isLive()) {
      sessionManager.closeSession();
    }
  }

  @SuppressWarnings("unchecked")
  protected void populateCurrentPage(String valueString) throws Exception {
    NodeIterator nodeIterator = setQuery(isQuery_, value_);
    int pos = 0;
    for (int i = 0; i < nodeIterator.getSize(); i++) {
      if (nodeIterator.nextNode().getName().equals(valueString)) {
        pos = i + 1;
        break;
      }
    }
    int pageSize = getPageSize();
    int page = 1;
    if (pos < pageSize) {
      page = 1;
    } else {
      page = pos / pageSize;
      if (pos % pageSize > 0) {
        page = page + 1;
      }
    }
    NodeIterator iter_ = setQuery(isQuery_, value_);
    this.pageSelected = page;
    iter_.skip((page - 1) * pageSize);
    currentListPage_ = new ArrayList<Object>();
    Node currentNode;
    for (int i = 0; i < pageSize; i++) {
      if (iter_.hasNext()) {
        currentNode = iter_.nextNode();
        if (currentNode.isNodeType(Utils.USER_PROFILES_TYPE)) {
          currentListPage_.add(getUserProfile(currentNode));
        }
      } else {
        break;
      }
    }
    iter_ = null;
    if (sessionManager.getCurrentSession() != null && sessionManager.getCurrentSession().isLive()) {
      sessionManager.closeSession();
    }
  }

  @SuppressWarnings("unchecked")
  protected void populateCurrentPageSearch(int page, List list, boolean isWatch, boolean isSearchUser) {
    int pageSize = getPageSize();
    int position = 0;
    if (page == 1)
      position = 0;
    else {
      position = (page - 1) * pageSize;
    }
    int endIndex = pageSize * page;
    if (!isSearchUser) {
      if (!isWatch)
        currentListPage_ = new ArrayList<ForumSearch>();
      else
        currentListPage_ = new ArrayList<Watch>();
    } else {
      currentListPage_ = new CopyOnWriteArrayList();
      list = listValue_;
    }
    endIndex = (endIndex < list.size()) ? endIndex : list.size();
    if (endIndex > position) {
      currentListPage_.addAll(list.subList(position, endIndex));
    }
  }

  private NodeIterator setQuery(boolean isQuery, String value) throws Exception {
    NodeIterator iter = null;
    Session session = sessionManager.getCurrentSession();
    if (session == null || !session.isLive()) {
      sessionManager.openSession();
      session = sessionManager.getCurrentSession();
    }
    if (isQuery) {
      QueryManager qm = session.getWorkspace().getQueryManager();
      Query query = qm.createQuery(value, Query.XPATH);
      QueryResult result = query.execute();
      iter = result.getNodes();
    } else {
      Node node = (Node) session.getItem(value);
      iter = node.getNodes();
    }
    return iter;
  }

  public Post getPost(Node postNode) throws Exception {
    Post postNew = new Post();
    PropertyReader reader = new PropertyReader(postNode);
    postNew.setId(postNode.getName());
    postNew.setPath(postNode.getPath());

    postNew.setOwner(reader.string(ForumNodeTypes.EXO_OWNER));
    postNew.setCreatedDate(reader.date(ForumNodeTypes.EXO_CREATED_DATE));
    postNew.setModifiedBy(reader.string(ForumNodeTypes.EXO_MODIFIED_BY));
    postNew.setModifiedDate(reader.date(ForumNodeTypes.EXO_MODIFIED_DATE));
    postNew.setEditReason(reader.string(ForumNodeTypes.EXO_EDIT_REASON));
    postNew.setName(reader.string(ForumNodeTypes.EXO_NAME));
    postNew.setMessage(reader.string(ForumNodeTypes.EXO_MESSAGE));
    postNew.setRemoteAddr(reader.string(ForumNodeTypes.EXO_REMOTE_ADDR));
    postNew.setIcon(reader.string(ForumNodeTypes.EXO_ICON));
    postNew.setLink(reader.string(ForumNodeTypes.EXO_LINK));
    postNew.setIsApproved(reader.bool(ForumNodeTypes.EXO_IS_APPROVED));
    postNew.setIsHidden(reader.bool(ForumNodeTypes.EXO_IS_HIDDEN));
    postNew.setIsWaiting(reader.bool(ForumNodeTypes.EXO_IS_WAITING));
    postNew.setIsActiveByTopic(reader.bool(ForumNodeTypes.EXO_IS_ACTIVE_BY_TOPIC));
    postNew.setUserPrivate(reader.strings(ForumNodeTypes.EXO_USER_PRIVATE));
    postNew.setNumberAttach(reader.l(ForumNodeTypes.EXO_NUMBER_ATTACH));
    if (postNew.getNumberAttach() > 0) {
      postNew.setAttachments(JCRDataStorage.getAttachmentsByNode(postNode));
    }
    return postNew;
  }

  private Topic getTopic(Node topicNode) throws Exception {
    if (topicNode == null)
      return null;
    Topic topicNew = new Topic();
    PropertyReader reader = new PropertyReader(topicNode);
    topicNew.setId(topicNode.getName());
    topicNew.setPath(topicNode.getPath());
    topicNew.setIcon(reader.string(ForumNodeTypes.EXO_ICON));
    topicNew.setTopicType(reader.string(ForumNodeTypes.EXO_TOPIC_TYPE, " "));
    topicNew.setTopicName(reader.string(ForumNodeTypes.EXO_NAME));
    topicNew.setOwner(reader.string(ForumNodeTypes.EXO_OWNER));
    topicNew.setCreatedDate(reader.date(ForumNodeTypes.EXO_CREATED_DATE));
    topicNew.setDescription(reader.string(ForumNodeTypes.EXO_DESCRIPTION));
    topicNew.setLastPostBy(reader.string(ForumNodeTypes.EXO_LAST_POST_BY));
    topicNew.setLastPostDate(reader.date(ForumNodeTypes.EXO_LAST_POST_DATE));
    topicNew.setIsSticky(reader.bool(ForumNodeTypes.EXO_IS_STICKY));
    if (topicNode.getParent().getProperty(ForumNodeTypes.EXO_IS_LOCK).getBoolean())
      topicNew.setIsLock(true);
    else
      topicNew.setIsLock(topicNode.getProperty(ForumNodeTypes.EXO_IS_LOCK).getBoolean());
    topicNew.setIsClosed(reader.bool(ForumNodeTypes.EXO_IS_CLOSED));
    topicNew.setIsApproved(reader.bool(ForumNodeTypes.EXO_IS_APPROVED));
    topicNew.setIsActive(reader.bool(ForumNodeTypes.EXO_IS_ACTIVE));
    topicNew.setIsWaiting(reader.bool(ForumNodeTypes.EXO_IS_WAITING));
    topicNew.setIsActiveByForum(reader.bool(ForumNodeTypes.EXO_IS_ACTIVE_BY_FORUM));
    topicNew.setIsPoll(reader.bool(ForumNodeTypes.EXO_IS_POLL));
    topicNew.setPostCount(reader.l(ForumNodeTypes.EXO_POST_COUNT));
    topicNew.setViewCount(reader.l(ForumNodeTypes.EXO_VIEW_COUNT));
    topicNew.setNumberAttachment(reader.l(ForumNodeTypes.EXO_NUMBER_ATTACHMENTS));
    topicNew.setUserVoteRating(reader.strings(ForumNodeTypes.EXO_USER_VOTE_RATING));
    topicNew.setVoteRating(reader.d(ForumNodeTypes.EXO_VOTE_RATING));
    // update more properties for topicNew.
    topicNew.setModifiedBy(reader.string(ForumNodeTypes.EXO_MODIFIED_BY));
    topicNew.setModifiedDate(reader.date(ForumNodeTypes.EXO_MODIFIED_DATE));
    topicNew.setIsModeratePost(reader.bool(ForumNodeTypes.EXO_IS_MODERATE_POST));
    topicNew.setIsNotifyWhenAddPost(reader.string(ForumNodeTypes.EXO_IS_NOTIFY_WHEN_ADD_POST, null));
    topicNew.setLink(reader.string(ForumNodeTypes.EXO_LINK));
    topicNew.setTagId(reader.strings(ForumNodeTypes.EXO_TAG_ID));
    topicNew.setCanView(reader.strings(ForumNodeTypes.EXO_CAN_VIEW, new String[] {}));
    topicNew.setCanPost(reader.strings(ForumNodeTypes.EXO_CAN_POST, new String[] {}));
    if (topicNode.isNodeType(ForumNodeTypes.EXO_FORUM_WATCHING))
      topicNew.setEmailNotification(reader.strings(ForumNodeTypes.EXO_EMAIL_WATCHING, new String[] {}));
    // try {
    // if (topicNew.getNumberAttachment() > 0) {
    // String idFirstPost = topicNode.getName().replaceFirst(Utils.TOPIC,
    // Utils.POST);
    // Node FirstPostNode = topicNode.getNode(idFirstPost);
    // topicNew.setAttachments(getAttachmentsByNode(FirstPostNode));
    // }
    // } catch (Exception e) {
    // log.debug("Failed to set attachments in topicNew.", e);
    // }
    return topicNew;
  }

  private UserProfile getUserProfile(Node profileNode) throws Exception {
    UserProfile userProfile = new UserProfile();
    userProfile.setUserId(profileNode.getName());
    PropertyReader reader = new PropertyReader(profileNode);
    userProfile.setScreenName(reader.string(ForumNodeTypes.EXO_SCREEN_NAME, 
                                            reader.string(ForumNodeTypes.EXO_FULL_NAME, profileNode.getName())));
    userProfile.setFullName(reader.string(ForumNodeTypes.EXO_FULL_NAME));
    userProfile.setFirstName(reader.string(ForumNodeTypes.EXO_FIRST_NAME));
    userProfile.setLastName(reader.string(ForumNodeTypes.EXO_LAST_NAME));
    userProfile.setEmail(reader.string(ForumNodeTypes.EXO_EMAIL));
    userProfile.setUserRole(reader.l(ForumNodeTypes.EXO_USER_ROLE));
    userProfile.setUserTitle(reader.string(ForumNodeTypes.EXO_USER_TITLE, ""));
    userProfile.setSignature(reader.string(ForumNodeTypes.EXO_SIGNATURE));
    userProfile.setTotalPost(reader.l(ForumNodeTypes.EXO_TOTAL_POST));
    userProfile.setTotalTopic(reader.l(ForumNodeTypes.EXO_TOTAL_TOPIC));
    userProfile.setBookmark(reader.strings(ForumNodeTypes.EXO_BOOKMARK));
    userProfile.setLastLoginDate(reader.date(ForumNodeTypes.EXO_LAST_LOGIN_DATE));
    userProfile.setJoinedDate(reader.date(ForumNodeTypes.EXO_JOINED_DATE));
    userProfile.setLastPostDate(reader.date(ForumNodeTypes.EXO_LAST_POST_DATE));
    userProfile.setIsDisplaySignature(reader.bool(ForumNodeTypes.EXO_IS_DISPLAY_SIGNATURE));
    userProfile.setIsDisplayAvatar(reader.bool(ForumNodeTypes.EXO_IS_DISPLAY_AVATAR));
    userProfile.setShortDateFormat(reader.string(ForumNodeTypes.EXO_SHORT_DATEFORMAT, userProfile.getShortDateFormat()));
    userProfile.setLongDateFormat(reader.string(ForumNodeTypes.EXO_LONG_DATEFORMAT, userProfile.getLongDateFormat()));
    userProfile.setTimeFormat(reader.string(ForumNodeTypes.EXO_TIME_FORMAT, userProfile.getTimeFormat()));
    userProfile.setMaxPostInPage(reader.l(ForumNodeTypes.EXO_MAX_POST, 10));
    userProfile.setMaxTopicInPage(reader.l(ForumNodeTypes.EXO_MAX_TOPIC, 10));
    userProfile.setIsShowForumJump(reader.bool(ForumNodeTypes.EXO_IS_SHOW_FORUM_JUMP, true));
    userProfile.setModerateForums(reader.strings(ForumNodeTypes.EXO_MODERATE_FORUMS, new String[] {}));
    userProfile.setModerateCategory(reader.strings(ForumNodeTypes.EXO_MODERATE_CATEGORY, new String[] {}));
    userProfile.setNewMessage(reader.l(ForumNodeTypes.EXO_NEW_MESSAGE));
    userProfile.setTimeZone(reader.d(ForumNodeTypes.EXO_TIME_ZONE));
    userProfile.setIsBanned(reader.bool(ForumNodeTypes.EXO_IS_BANNED));
    userProfile.setBanUntil(reader.l(ForumNodeTypes.EXO_BAN_UNTIL));
    userProfile.setBanReason(reader.string(ForumNodeTypes.EXO_BAN_REASON, ""));
    userProfile.setBanCounter(Integer.parseInt(reader.string(ForumNodeTypes.EXO_BAN_COUNTER, "0")));
    userProfile.setBanReasonSummary(reader.strings(ForumNodeTypes.EXO_BAN_REASON_SUMMARY, new String[] {}));
    userProfile.setCreatedDateBan(reader.date(ForumNodeTypes.EXO_CREATED_DATE_BAN));
    return userProfile;
  }

  private ForumPrivateMessage getPrivateMessage(Node messageNode) throws Exception {
    ForumPrivateMessage message = new ForumPrivateMessage();
    message.setId(messageNode.getName());
    PropertyReader reader = new PropertyReader(messageNode);
    message.setFrom(reader.string(ForumNodeTypes.EXO_FROM));
    message.setSendTo(reader.string(ForumNodeTypes.EXO_SEND_TO));
    message.setName(reader.string(ForumNodeTypes.EXO_NAME));
    message.setMessage(reader.string(ForumNodeTypes.EXO_MESSAGE));
    message.setReceivedDate(reader.date(ForumNodeTypes.EXO_RECEIVED_DATE));
    message.setIsUnread(reader.bool(ForumNodeTypes.EXO_IS_UNREAD));
    message.setType(reader.string(ForumNodeTypes.EXO_TYPE));
    return message;
  }

  @Override
  protected void populateCurrentPageList(int page, List list) {
    int pageSize = getPageSize();
    int position = 0;
    if (page == 1)
      position = 0;
    else {
      position = (page - 1) * pageSize;
    }
    pageSize *= page;
    currentListPage_ = new ArrayList<String>();
    for (int i = (int) position; i < pageSize && i < list.size(); i++) {
      currentListPage_.add(list.get(i));
    }
  }

  @Override
  public List getAll() throws Exception {
    currentPage_ = 0;
    populateCurrentPage(currentPage_);// trick allowed by implementation of ForumPageList.populateCurrentPage()
    this.pageSelected = 0;
    return currentListPage_;
  }
}
