/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 2, 2008 - 3:33:33 AM  
 */
public class Utils implements ForumNodeTypes {
  
  private final static Log   LOG                   = ExoLogger.getLogger(Utils.class);

  public final static String TYPE_CATEGORY         = "exo:forumCategory".intern();

  public final static String TYPE_FORUM            = "exo:forum".intern();

  public final static String TYPE_TOPIC            = "exo:topic".intern();

  public final static String USER_PROFILES_TYPE    = "exo:forumUserProfile".intern();

  public final static String FORUM_SERVICE         = "ForumService".intern();

  public final static String USER_PROFILES         = "UserProfile".intern();

  public final static String FORUM_SUBSCRIOTION    = "forumSubscription".intern();

  public final static String NT_UNSTRUCTURED       = "nt:unstructured".intern();

  public final static String FORUMADMINISTRATION   = "forumAdministration".intern();

  public static final String USER_PROFILE_DELETED  = "userProfileDeleted".intern();

  public final static String CATEGORY              = "forumCategory".intern();

  public final static String FORUM                 = "forum".intern();

  public final static String TOPIC                 = "topic".intern();

  public final static String POST                  = "post".intern();
  
  public final static String ATTACHMENT            = "attachment";

  public final static String POLL                  = "poll".intern();

  public final static String TAG                   = "tag".intern();

  public final static String TOPICTYPE             = "topicType".intern();

  public final static String PRUNESETTING          = "pruneSetting".intern();

  public final static String RECEIVE_MESSAGE       = "receive".intern();

  public final static String SEND_MESSAGE          = "send".intern();

  public static final String ADMIN                 = "Administrator".intern();

  public static final String MODERATOR             = "Moderator".intern();

  public static final String USER                  = "User".intern();

  public static final String GUEST                 = "Guest".intern();

  public static final String DELETED               = "_deleted".intern();

  public static final String CACHE_REPO_NAME       = "repositoryName".intern();
  
  public static final String DEFAULT_TEMANT_NAME   = Long.toHexString(System.currentTimeMillis() 
                                                                      + System.identityHashCode("currentTenant"));

  // Type Modify
  public static final int    CLOSE                 = 1;

  public static final int    LOCK                  = 2;

  public static final int    APPROVE               = 3;

  public static final int    STICKY                = 4;

  public static final int    WAITING               = 5;

  public static final int    ACTIVE                = 6;

  public static final int    CHANGE_NAME           = 7;

  public static final int    VOTE_RATING           = 8;

  public static final int    HIDDEN                = 9;

  public static final String SPACE                 = " ".intern();

  /**
   * start with forum prefix.
   */
  public static final String FORUM_SPACE_ID_PREFIX = (FORUM + "Space").intern();

  public static final String ADMIN_ROLE            = "ADMIN".intern();

  public static final String DEFAULT_EMAIL_CONTENT = "Hi,</br> You receive this email because you registered for eXo Forum and Topic Watching notification." + "<br/>We would like to inform you that there is a new $ADD_TYPE in the $OBJECT_WATCH_TYPE <strong>$OBJECT_NAME</strong> with the following content: "
                                                       + "<div>_______________<br/>$POST_CONTENT<br/>_______________</div><div>At $TIME on $DATE, posted by <strong>$POSTER</strong> .</div><div>Go directly to the post: " + "<a target=\"_blank\" href=\"$VIEWPOST_LINK\">Click here.</a> <br/>Or go to reply to the post: <a target=\"_blank\" href=\"$REPLYPOST_LINK\">Click here." + "</a></div>".intern();

  /**
   * Clear characters that have a codepoint < 31 (non printable) from a string
   * @param s string input
   * @return the string with all character whose codepoint<31 removed
   */
  public static String removeCharterStrange(String s) {
    if (s == null || s.length() <= 0)
      return "";
    int i = 0;
    StringBuilder builder = new StringBuilder();
    while (i < s.length()) {
      if (s.codePointAt(i) > 31) {
        builder.append(s.charAt(i));
      }
      ++i;
    }
    return builder.toString();
  }

  static public class DatetimeComparatorDESC implements Comparator<Object> {
    public int compare(Object o1, Object o2) throws ClassCastException {
      Date date1 = ((User) o1).getCreatedDate();
      Date date2 = ((User) o2).getCreatedDate();
      return date2.compareTo(date1);
    }
  }

  static public class DatetimeComparatorPostDESC implements Comparator<Post> {
    public int compare(Post o1, Post o2) throws ClassCastException {
      Date date1 = o2.getCreatedDate();
      Date date2 = o1.getCreatedDate();
      return date2.compareTo(date1);
    }
  }

  /**
   * Compare two arrays and to verify there is a difference in content between two string arrays. The elements may not appear in the same order in both arrays.
   * @param a first string array to compare
   * @param b second string array to compare
   * @return true if there is a difference in content or size between the two arrays, false otherwise.
   */
  public static boolean arraysHaveDifferentContent(String[] a, String[] b) {
    if (a.length == b.length) {
      List<String> list = Arrays.asList(b);
      for (int i = 0; i < a.length; i++) {
        if (!list.contains(a[i])) {
          return true;
        }
      }
    } else {
      return true;
    }
    return false;
  }

  /**
   * Compare two lists and to verify there is a difference in content between two string lists. The elements may not appear in the same order in both lists.
   * @param a first string list to compare
   * @param b second string list to compare
   * @return true if there is a difference in content or size between the two lists, false otherwise.
   * @see #arraysHaveDifferentContent(String[], String[])
   */
  public static boolean listsHaveDifferentContent(List<String> a, List<String> b) {
    if (a.size() == b.size()) {
      for (String s : b) {
        if (!a.contains(s)) {
          return true;
        }
      }
    } else {
      return true;
    }
    return false;
  }

  /**
   * Converts a map to a string array representation. Each map entry is converted into a string item. String items will be of the form "key,value".
   * This is the reverse operation of {@link #arrayToMap(String[])}
   * @param map map to convert
   * @return converted list of string
   */
  public static String[] mapToArray(Map<String, String> map) {
    if (map.isEmpty())
      return new String[] { " " };
    String str = map.toString().replace(" ", "").replace("{", "").replace("}", "");
    str = str.replace(",", ";").replace("=", ",");
    return str.split(";");
  }

  /**
   * Convert a String array to a map. key and values must be comma separated. For example : "color,blue" will create an entry "blue" with key "color" in the map.
   * A string not conforming to the pattern "key,value" is ignored
   * @param strs List of string to scan
   * @return map representation
   */
  public static Map<String, String> arrayToMap(String[] strs) {
    Map<String, String> map = new HashMap<String, String>();
    String[] arr;
    for (int i = 0; i < strs.length; i++) {
      arr = strs[i].split(",");
      if (arr.length == 2)
        map.put(arr[0], arr[1]);
    }
    return map;
  }

  /**
   * Create a JCR xpath condition that match a property against a list of values with an or condition. 
   * Note that if the property does not exist, the condition is matched too (means : values are combined with not(property))
   * @param property property to match
   * @param list list of possible values
   * @return the JCR xpath predicate condition to match the property against values.
   */
  public static String propertyMatchAny(String property, List<String> list) {
    StringBuilder builder = new StringBuilder();
    if (!list.isEmpty()) {
      int t = 0;
      for (String string : list) {
        if (t == 0)
          builder.append("(not(").append(property).append(") or ").append(property).append("='' or ").append(property).append("='").append(string).append("'");
        else
          builder.append(" or ").append(property).append("='").append(string).append("'");
        t = 1;
      }
      if (t == 1)
        builder.append(")");
    }
    return builder.toString();
  }

  /**
   * Note that
   * @param list
   * @param list1
   * @return
   */
  public static boolean isListContentItemList(List<String> list, List<String> list1) {
    if (list1 == null || (list1.size() == 1 && isEmpty(list1.get(0))))
      return false;
    for (String string : list1) {
      if (list.contains(string))
        return true;
    }
    return false;
  }

  /**
   * Transforms a List of strings into a string array and clear any  blank entry. 
   * A blank entry is the 'space' value (aka " ").
   * @param list List of Strings to transform
   * @return String array cleared of blanks
   */
  public static String[] getStringsInList(List<String> list){
    if (list.size() > 1) {
      while (list.contains(" ")) {
        list.remove(" ");
      }
    }
    return list.toArray(new String[list.size()]);
  }

  /**
   * Extract the items two lists have in common.
   * @param pList first list
   * @param cList second list
   * @return a new list containing only the common elements between the two lists in input
   * @throws Exception
   */
  public static List<String> extractSameItems(List<String> pList, List<String> cList) throws Exception {
    List<String> list = new ArrayList<String>();
    for (String string : pList) {
      if (cList.contains(string))
        list.add(string);
    }
    return list;
  }

  /**
   * Transforms a jcr Value array into a string array . 
   * Calls {@link Value#getString()} on each item.
   * @see javax.jcr.Value
   * @param values array of values to transform
   * @return string array for the Value array
   * @throws Exception
   */

  public static String[] valuesToArray(Value[] Val) throws Exception {
    if (Val.length < 1)
      return new String[] {};
    List<String> list = new ArrayList<String>();
    String s;
    for (int i = 0; i < Val.length; ++i) {
      s = Val[i].getString();
      if (!isEmpty(s))
        list.add(s);
    }
    return list.toArray(new String[list.size()]);
  }

  /**
   * Transforms a jcr Value array into a string list . 
   * Calls {@link Value#getString()} on each item.
   * @see javax.jcr.Value
   * @param values array of values to transform
   * @return string list for the Value array
   * @throws Exception
   */

  public static List<String> valuesToList(Value[] values) throws Exception {
    List<String> list = new ArrayList<String>();
    if (values.length < 1)
      return list;
    String s;
    for (int i = 0; i < values.length; ++i) {
      s = values[i].getString();
      if (!isEmpty(s))
        list.add(s);
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] arrayCopy(final T[] source) {
    // null in, null out
    if (source == null) {
      return null;
    }
    // empty in, empty out
    if (source.length == 0) {

      try {
        return (T[]) Array.newInstance(source.getClass().getComponentType(), 0);
      } catch (Exception e) {
        return null;
        // should never occur;
      }
    }

    // instanciate a new array based on first item
    T[] dest = (T[]) Array.newInstance(source[0].getClass(), source.length);
    System.arraycopy(source, 0, dest, 0, source.length);
    return dest;
  }

  /**
   * Check string is null or empty 
   * @param String s
   * @return boolean
   */
  public static boolean isEmpty(String s) {
    return (s == null || s.trim().length() <= 0) ? true : false;
  }

  /**
   * check string array is whether empty or not
   * @param array
   * @return false if at least one element of array is not empty, true in the opposite case.
   */
  public static boolean isEmpty(String[] array) {
    if (array != null && array.length > 0) {
      for (String s : array) {
        if (s != null && s.trim().length() > 0)
          return false;
      }
    }
    return true;
  }
  
  /**
   * get Xpath query by one property. 
   * @param String typeAdd
   * @param String property
   * @param String value
   * @return String
   */  
  public static String getQueryByProperty(String typeAdd, String property, String value) {
    StringBuilder strBuilder = new StringBuilder();
    if (!isEmpty(value) && !isEmpty(property)) {
      if (!isEmpty(typeAdd)) {
        strBuilder.append(SPACE).append(typeAdd).append(SPACE);
      }
      strBuilder.append("(@").append(property).append("='").append(value).append("')");
    } 
    return strBuilder.toString();
  }

  /**
   * get Xpath query when get list post. 
   * @param String isApproved
   * @param String isHidden
   * @param String isWaiting
   * @param String userLogin
   * @return StringBuilder
   */
  public static StringBuilder getPathQuery(String isApproved, String isHidden, String isWaiting, String userLogin) throws Exception {
    StringBuilder strBuilder = new StringBuilder();
    String typeAdd = null;
    String str = getQueryByProperty(typeAdd, EXO_USER_PRIVATE, userLogin);
    if (!isEmpty(str)) {
      strBuilder.append("(").append(str);
      typeAdd = "or";
    }
    if ("or".equals(typeAdd)) {
      strBuilder.append(getQueryByProperty(typeAdd, EXO_USER_PRIVATE, EXO_USER_PRI)).append(")");
      typeAdd = "and";
    }
    str = getQueryByProperty(typeAdd, EXO_IS_APPROVED, isApproved);
    if (!isEmpty(str)) {
      strBuilder.append(str);
      typeAdd = "and";
    }
    str = getQueryByProperty(typeAdd, EXO_IS_HIDDEN, isHidden);
    if (!isEmpty(str)) {
      strBuilder.append(str);
      typeAdd = "and";
    }
    str = getQueryByProperty(typeAdd, EXO_IS_WAITING, isWaiting);
    if (!isEmpty(str)) {
      strBuilder.append(str);
    }
    if (strBuilder.length() > 0) {
      return new StringBuilder("[").append(strBuilder).append("]");
    }
    return new StringBuilder();
  }

  /**
   * Checking a user who whether contained Users/MemberShip/Group ? 
   * @param listOfCanviewrs
   * @param listOfBoundUsers
   * @return boolean
   */
  static public boolean hasPermission(List<String> listOfCanviewrs, List<String> listOfBoundUsers) {
    if (listOfBoundUsers == null || listOfCanviewrs == null)
      return false;
    List<String> tem = new ArrayList<String>();
    for (String str : listOfCanviewrs) {
      if (str == null)
        continue;
      if (listOfBoundUsers.contains(str))
        return true;
      if (str.contains("*")) {
        str = str.substring(str.indexOf("/"), str.length());
        tem.add(str);
        if (listOfBoundUsers.contains(str))
          return true;
      }
    }
    for (String s : listOfBoundUsers) {
      if (tem.contains(s))
        return true;
    }
    return false;
  }
  
  static public String getCurrentTenantName() {
    RepositoryService repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    try {
      return repositoryService.getCurrentRepository().getConfiguration().getName();
    } catch (RepositoryException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Can not get current repository", e);
      }
    }
    return DEFAULT_TEMANT_NAME;
  }

  static public List<String> getOnlineUserByTenantName(Map<String, List<String>> onlineUserMap) {
    List<String> onlinUsers = new ArrayList<String>();
    String currentTenant = getCurrentTenantName();
    if (onlineUserMap != null && onlineUserMap.get(currentTenant) != null) {
      onlinUsers.addAll(onlineUserMap.get(currentTenant));
    }
    return onlinUsers;
  }
}
