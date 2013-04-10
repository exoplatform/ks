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
/**
 * 
 */

package org.exoplatform.forum;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Dec 21, 2007 5:35:54 PM 
 */

public class ForumUtils {
  protected static Log       log                     = ExoLogger.getLogger(ForumUtils.class);

  public static final String FIELD_EXOFORUM_LABEL    = "eXoForum".intern();

  public static final String FIELD_SEARCHFORUM_LABEL = "SearchForum".intern();

  public static final String UPLOAD_FILE_SIZE        = "uploadFileSizeLimitMB".intern();

  public static final String UPLOAD_AVATAR_SIZE      = "uploadAvatarSizeLimitMB".intern();

  public static final String SEARCHFORM_ID           = "SearchForm".intern();

  public static final String GOPAGE_ID_T             = "goPageTop".intern();

  public static final String GOPAGE_ID_B             = "goPageBottom".intern();

  public static final String CATEGORIES              = "Categories".intern();

  public static final String CATEGORY                = "category".intern();

  public static final String FORUM                   = "forum".intern();

  public static final String TOPIC                   = "topic".intern();

  public static final String POST                    = "post".intern();

  public static final String TAG                     = "Tag".intern();

  public static final String POLL                    = "Poll".intern();

  public static final String COMMA                   = ",".intern();

  public static final String SLASH                   = "/".intern();

  public static final String EMPTY_STR               = "".intern();

  public static final String SPACE_GROUP_ID          = SpaceUtils.SPACE_GROUP.replace(SLASH, EMPTY_STR);
  
  public static final int    MAXSIGNATURE            = 300;

  public static final int    MAXTITLE                = 100;

  public static final int DEFAULT_VALUE_UPLOAD_PORTAL = -1;  

  public static final long   MAXMESSAGE              = 10000;

  private static String buildForumLink(String url, String type, String id) {
    StringBuilder link = new StringBuilder(url);
    if (!isEmpty(type) && !isEmpty(id)) {
      if (link.lastIndexOf(SLASH) == (link.length() - 1))
        link.append(type);
      else
        link.append(SLASH).append(type);
      if (!id.equals(Utils.FORUM_SERVICE))
        link.append(SLASH).append(id);
    }
    return link.toString();
  }

  public static String createdForumLink(String type, String id, boolean isPrivate) throws Exception {    
    String containerName = ((ExoContainerContext) ExoContainerContext.getCurrentContainer()
                           .getComponentInstanceOfType(ExoContainerContext.class)).getPortalContainerName();
    String pageNodeSelected = Util.getUIPortal().getSelectedUserNode().getURI();
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    return buildLink((portalContext.getPortalURI()), containerName , pageNodeSelected, type, id, isPrivate);
  }

  public static String buildLink(String portalURI, String containerName, String selectedNode, String type, String id, boolean isPrivate){
    StringBuilder sb = new StringBuilder();
    portalURI = portalURI.concat(selectedNode).concat(SLASH);
    if (!isPrivate) {
      sb.append(buildForumLink(portalURI, type, id));
    } else {
      String host = portalURI.substring(0, portalURI.indexOf(containerName) -1);
      sb.append(host)
        .append(SLASH)
        .append(containerName)
        .append(SLASH)
        .append("login?&initialURI=")
        .append(buildForumLink(portalURI.replaceFirst(host, EMPTY_STR), type, id))
        .toString();
    }
    return sb.toString();
  }

  public static boolean isValidEmailAddresses(String addressList){
    if (isEmpty(addressList))
      return true;
    addressList = StringUtils.remove(addressList, " ");
    addressList = StringUtils.replace(addressList, ";", COMMA);
    try {
      InternetAddress[] iAdds = InternetAddress.parse(addressList, true);
      String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}";
      for (int i = 0; i < iAdds.length; i++) {
        if (!iAdds[i].getAddress().matches(emailRegex))
          return false;
      }
    } catch (AddressException e) {
      return false;
    }
    return true;
  }

  public static String getSizeFile(long size) {
    String sizeStr = String.valueOf(size);
    String unit = " Byte";
    if (size >= 1024) {
      DecimalFormat df = new DecimalFormat("#,###.#", new DecimalFormatSymbols(Locale.ENGLISH));
      double convertedSize = (double) size / 1024;
      unit = " Kb";
      if (convertedSize >= 1024) {
        convertedSize = convertedSize / 1024;
        unit = " Mb";
      }
      sizeStr = df.format(convertedSize);
    }
    return (sizeStr + unit);
  }

  public static String getTimeZoneNumberInString(String string) {
    if (!isEmpty(string)) {
      StringBuffer stringBuffer = new StringBuffer();
      for (int i = 0; i < string.length(); ++i) {
        char c = string.charAt(i);
        if (c == ')')
          break;
        if (Character.isDigit(c) || c == '-' || c == '+' || c == ':') {
          if (c == ':')
            c = '.';
          if (c == '3' && string.charAt(i - 1) == ':')
            c = '5';
          stringBuffer.append(c);
        }
      }
      return stringBuffer.toString();
    }
    return null;
  }

  public static String[] getStarNumber(double voteRating) {
    int star = (int) voteRating;
    String[] className = new String[6];
    float k = 0;
    for (int i = 0; i < 5; i++) {
      if (i < star)
        className[i] = "star";
      else if (i == star) {
        k = (float) (voteRating - i);
        if (k < 0.25)
          className[i] = "notStar";
        if (k >= 0.25 && k < 0.75)
          className[i] = "halfStar";
        if (k >= 0.75)
          className[i] = "star";
      } else {
        className[i] = "notStar";
      }
      className[5] = String.valueOf(voteRating);
      if (className[5].length() >= 3)
        className[5] = className[5].substring(0, 3);
      if (k == 0)
        className[5] = String.valueOf(star);
    }
    return className;
  }

  public static String getOrderBy(String strOrderBy, String param) {
    // In case : user have sort before
    if (!isEmpty(strOrderBy)) {
      // If user want to reverse sort of a property
      if (strOrderBy.indexOf(param) >= 0) {
        if (strOrderBy.indexOf("descending") > 0) {
          strOrderBy = param + " ascending";
        } else {
          strOrderBy = param + " descending";
        }
        // User sort in another property
      } else {
        strOrderBy = param + " ascending";
      }
      // In case : The first time user sorting
    } else {
      strOrderBy = param + " ascending";
    }
    return strOrderBy;
  }
  
  public static String updateMultiValues(String value, String values) {
    if (!isEmpty(values)) {
      values = removeSpaceInString(values);
      if (!isStringInStrings(values.split(COMMA), value)) {
        if (values.lastIndexOf(COMMA) != (values.length() - 1))
          values = values + COMMA;
        values = values + value;
      }
    } else
      values = value;
    return removeStringResemble(values);
  }
  
  public static String[] getCensoredKeyword(ForumService forumService) throws Exception {
    ForumAdministration forumAdministration = forumService.getForumAdministration();
    return getCensoredKeyword(forumAdministration.getCensoredKeyword());
  }

  public static String[] getCensoredKeyword(String stringKey) {
    if (!isEmpty(stringKey)) {
      String str = EMPTY_STR;
      while (!stringKey.equals(str)) {
        str = stringKey;
        stringKey = stringKey.toLowerCase().replaceAll(";", COMMA).replaceAll(COMMA + " ", COMMA).replaceAll(" " + COMMA, COMMA).replaceAll(COMMA + COMMA, COMMA);
        if (stringKey.indexOf(COMMA) == 0) {
          stringKey = stringKey.replaceFirst(COMMA, EMPTY_STR);
        }
      }
      return stringKey.trim().split(COMMA);
    }
    return new String[] {};
  }

  public static String[] splitForForum(String str) {
    if (!isEmpty(str)) {
      str = StringUtils.remove(str, " ");
      if (str.contains(COMMA)) {
        str = str.replaceAll(";", COMMA);
        return str.trim().split(COMMA);
      } else {
        str = str.replaceAll(COMMA, ";");
        return str.trim().split(";");
      }
    } else
      return new String[] { EMPTY_STR };
  }

  public static String unSplitForForum(String[] str) {
    if (str == null || str.length == 0)
      return EMPTY_STR;
    StringBuilder rtn = new StringBuilder();
    if (!str[0].equals(" ")) {
      for (String temp : str) {
        if (rtn.length() > 1)
          rtn.append(COMMA).append(temp.trim());
        else
          rtn.append(temp.trim());
      }
    }
    return rtn.toString();
  }

  public static String removeSpaceInString(String str) {
    if (!isEmpty(str)) {
      String strs[] = new String[] { ";", COMMA+" ", " "+COMMA, COMMA+COMMA};
      for (int i = 0; i < strs.length; i++) {
        while (str.indexOf(strs[i]) >= 0) {
          str = str.replaceAll(strs[i], COMMA);
        }
      }
      if (str.lastIndexOf(COMMA) == str.length() - 1) {
        str = str.substring(0, str.length() - 1);
      }
      if (str.indexOf(COMMA) == 0) {
        str = str.substring(1, str.length());
      }
      return str;
    } else
      return EMPTY_STR;
  }

  public static String removeZeroFirstNumber(String str) {
    if (!isEmpty(str)) {
      str = str.trim();
      StringBuilder s = new StringBuilder();
      int i = 0;
      while ((i + 1) < str.length() && (str.charAt(i) == '0' || str.charAt(i) == ' ')) {
        s.append(str.charAt(i));
        ++i;
      }
      str = str.replaceFirst(s.toString(), EMPTY_STR);
    }
    return str;
  }

  public static String removeStringResemble(String s) {
    List<String> list = new ArrayList<String>();
    if (!isEmpty(s)) {
      String temp[] = splitForForum(s);
      StringBuilder builder = new StringBuilder();
      int l = temp.length;
      for (int i = 0; i < l; ++i) {
        if (list.contains(temp[i]) || temp[i].trim().length() == 0)
          continue;
        list.add(temp[i]);
        if (i == (l - 1))
          builder.append(temp[i]);
        else
          builder.append(temp[i]).append(COMMA);
      }
      return builder.toString();
    } else
      return EMPTY_STR;
  }

  public static boolean isEmpty(String str) {
    if (str == null || str.trim().length() == 0)
      return true;
    else
      return false;
  }

  public static boolean isArrayEmpty(String[] strs) {
    if (strs == null || strs.length == 0 || (strs.length == 1 && strs[0].trim().length() <= 0))
      return true;
    return false;
  }

  public static String[] addStringToString(String input, String output) {
    List<String> list = new ArrayList<String>();
    if (!isEmpty(output)) {
      if (!isEmpty(input)) {
        if (input.lastIndexOf(COMMA) != (input.length() - 1))
          input = input + COMMA;
        output = input + output;
        String temp[] = splitForForum(output);
        for (String string : temp) {
          if (list.contains(string) || string.length() == 0)
            continue;
          list.add(string);
        }
      }
    }
    if (list.size() == 0)
      list.add(" ");
    return list.toArray(new String[list.size()]);
  }

  public static String[] arraysMerge(String[] strs1, String[] strs2) {
    if(isArrayEmpty(strs1)) return strs2;
    if(isArrayEmpty(strs2)) return strs1;
    Set<String> set = new HashSet<String>();
    for (int i = 0; i < strs1.length; i++) {
      set.add(strs1[i]);
    }
    for (int i = 0; i < strs2.length; i++) {
      set.add(strs2[i]);
    }
    return set.toArray(new String[set.size()]);
  }
  
  public static boolean isStringInStrings(String[] strings, String string) {
    if (isEmpty(string)) {
      return false;
    }
    if (isArrayEmpty(strings)) {
      return false;
    }
    return isStringInList(Arrays.asList(strings), string.trim());
  }

  public static boolean isStringInList(List<String> list, String string) {
    for (String str : list) {
      if (str.trim().equals(string)) {
        return true;
      }
    }
    return false;
  }

  public static String getSubString(String str, int max) {
    if (!isEmpty(str)) {
      int l = str.length();
      if (l > max) {
        str = str.substring(0, max);
        int space = str.lastIndexOf(" ");
        if (space > (max - 6))
          str = str.substring(0, space) + "...";
        else
          str = str + "...";
      }
    }
    return str;
  }

  public static List<String> addArrayToList(List<String> list, String[] array) {
    if (array == null)
      return list;
    if (list.isEmpty() && !isArrayEmpty(array))
      list.addAll(Arrays.asList(array));
    else {
      for (int i = 0; i < array.length; i++) {
        if (array[i] != null && !list.contains(array[i]) && array[i].trim().length() > 0)
          list.add(array[i]);
      }
    }
    return list;
  }

  public static String getLabel(String label, String key) {
    if (isEmpty(key))
      key = " ";
    try {
      return label.replaceFirst("<keyWord>", key);
    } catch (Exception e) {
      String s = label.substring(0, label.indexOf("<keyWord>") - 1);
      return s + "'" + key + "'" + label.substring(label.indexOf("<keyWord>"));
    }
  }

  public static String[] getColor() {
    return new String[] { "blue", "DarkGoldenRod", "green", "yellow", "BlueViolet", "orange", "darkBlue", "IndianRed", "DarkCyan", "lawnGreen" };
  }

  public static MessageBuilder getDefaultMail() {
    MessageBuilder messageBuilder = new MessageBuilder();
    try {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      ResourceBundle res = context.getApplicationResourceBundle();
      messageBuilder.setContent(res.getString("UINotificationForm.label.notifyEmailContentDefault"));
      String header = res.getString("UINotificationForm.label.notifyEmailHeaderSubjectDefault");
      messageBuilder.setHeaderSubject((isEmpty(header)) ? EMPTY_STR : header);

      messageBuilder.setTypes(res.getString("UIForumPortlet.label.category"), res.getString("UIForumPortlet.label.forum"), res.getString("UIForumPortlet.label.topic"), res.getString("UIForumPortlet.label.post"));
    } catch (Exception e) {
      log.debug("Failed to get resource bundle for default content email notification !", e);
    }
    return messageBuilder;
  }

  public static boolean enableIPLogging() {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    return Boolean.parseBoolean(portletPref.getValue("enableIPFiltering", EMPTY_STR));
  }

  public static void savePortletPreference(String listCategoryId, String listForumId) throws Exception {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    portletPref.setValue("invisibleCategories", listCategoryId);
    portletPref.setValue("invisibleForums", listForumId);
    portletPref.store();
  }

  public static SettingPortletPreference getPorletPreference() {
    SettingPortletPreference preference = new SettingPortletPreference();
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    preference.setShowForumActionBar(Boolean.parseBoolean(portletPref.getValue("showForumActionBar", EMPTY_STR)));
    preference.setForumNewPost(Integer.parseInt(portletPref.getValue("forumNewPost", EMPTY_STR)));
    preference.setUseAjax(Boolean.parseBoolean(portletPref.getValue("useAjax", EMPTY_STR)));
    preference.setEnableIPLogging(Boolean.parseBoolean(portletPref.getValue("enableIPLogging", EMPTY_STR)));
    preference.setEnableIPFiltering(Boolean.parseBoolean(portletPref.getValue("enableIPFiltering", EMPTY_STR)));
    preference.setInvisibleCategories(getListInValus(portletPref.getValue("invisibleCategories", EMPTY_STR)));
    preference.setInvisibleForums((getListInValus(portletPref.getValue("invisibleForums", EMPTY_STR))));
    // Show porlet
    preference.setShowForumJump(Boolean.parseBoolean(portletPref.getValue("isShowForumJump", EMPTY_STR)));
    preference.setShowIconsLegend(Boolean.parseBoolean(portletPref.getValue("isShowIconsLegend", EMPTY_STR)));
    preference.setShowModerators(Boolean.parseBoolean(portletPref.getValue("isShowModerators", EMPTY_STR)));
    preference.setShowPoll(Boolean.parseBoolean(portletPref.getValue("isShowPoll", EMPTY_STR)));
    preference.setShowQuickReply(Boolean.parseBoolean(portletPref.getValue("isShowQuickReply", EMPTY_STR)));
    preference.setShowRules(Boolean.parseBoolean(portletPref.getValue("isShowRules", EMPTY_STR)));
    preference.setShowStatistics(Boolean.parseBoolean(portletPref.getValue("isShowStatistics", EMPTY_STR)));
    return preference;
  }

  public static void savePortletPreference(SettingPortletPreference sPreference) throws Exception {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    String listForumId = EMPTY_STR, listCategoryId = EMPTY_STR;
    List<String> invisibleForums = sPreference.getInvisibleForums();
    List<String> invisibleCategories = sPreference.getInvisibleCategories();

    if (!invisibleCategories.isEmpty()) {
      listForumId = invisibleForums.toString().replace('[' + EMPTY_STR, EMPTY_STR).replace(']' + EMPTY_STR, EMPTY_STR).replaceAll(" ", EMPTY_STR);
      listCategoryId = invisibleCategories.toString().replace('[' + EMPTY_STR, EMPTY_STR).replace(']' + EMPTY_STR, EMPTY_STR).replaceAll(" ", EMPTY_STR);
    }

    portletPref.setValue("isShowForumJump", sPreference.isShowForumJump() + EMPTY_STR);
    portletPref.setValue("isShowIconsLegend", sPreference.isShowIconsLegend() + EMPTY_STR);
    portletPref.setValue("isShowModerators", sPreference.isShowModerators() + EMPTY_STR);
    portletPref.setValue("isShowPoll", sPreference.isShowPoll() + EMPTY_STR);
    portletPref.setValue("isShowQuickReply", sPreference.isShowQuickReply() + EMPTY_STR);
    portletPref.setValue("isShowRules", sPreference.isShowRules() + EMPTY_STR);
    portletPref.setValue("isShowStatistics", sPreference.isShowStatistics() + EMPTY_STR);
    portletPref.setValue("useAjax", sPreference.isUseAjax() + EMPTY_STR);
    portletPref.setValue("invisibleCategories", listCategoryId);
    portletPref.setValue("invisibleForums", listForumId);
    portletPref.store();
  }
  
  public static boolean isAjaxRequest() {
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    return portalContext.useAjax();
  }

  public static List<String> getListInValus(String value) {
    List<String> list = new ArrayList<String>();
    if (!ForumUtils.isEmpty(value)) {
      list.addAll(Arrays.asList(ForumUtils.addStringToString(value, value)));
    }
    return list;
  }

  public static int getLimitUploadSize(boolean isAvatar) {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    int limitMB;
    try {
      if (isAvatar) {
        limitMB = Integer.parseInt(portletPref.getValue(UPLOAD_AVATAR_SIZE, EMPTY_STR).trim());
      } else {
        limitMB = Integer.parseInt(portletPref.getValue(UPLOAD_FILE_SIZE, EMPTY_STR).trim());
      }
    } catch (NumberFormatException e) {
      limitMB = DEFAULT_VALUE_UPLOAD_PORTAL;
    }
    return limitMB;
  }

  static public class DatetimeComparatorDESC implements Comparator<Object> {
    public int compare(Object o1, Object o2) throws ClassCastException {
      Date date1 = ((Post) o1).getCreatedDate();
      Date date2 = ((Post) o2).getCreatedDate();
      return date1.compareTo(date2);
    }
  }

  static public class SortComparatorDESC implements Comparator<Object> {
    public int compare(Object o1, Object o2) throws ClassCastException {
      String str1 = ((TopicType) o1).getName();
      String str2 = ((TopicType) o2).getName();
      return str1.compareTo(str2);
    }
  }

  static public String getCalculateListEmail(String s) throws Exception {
    String[] strs = splitForForum(s);
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < strs.length; i++) {
      if (isEmpty(strs[i]))
        continue;
      if (i > 0)
        builder.append(",<br/>");
      builder.append("<span title='").append(strs[i]).append("'>").append(getSubString(strs[i], 15)).append("</span>");
    }
    return builder.toString();
  }
}
