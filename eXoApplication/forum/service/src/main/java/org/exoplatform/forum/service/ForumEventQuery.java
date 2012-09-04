package org.exoplatform.forum.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.exoplatform.commons.utils.ISO8601;

public class ForumEventQuery implements ForumNodeTypes {
  public static final String VALUE_IN_ENTIRE = "entire";

  public static final String VALUE_IN_TITLE  = "title";

  long             userPermission = 0;

  List<String>     listOfUser     = null;

  private String   type;

  private String   keyValue;

  private String   valueIn;

  private String   topicType;

  private String   path;

  private String   byUser;

  private String   isLock;

  private String   isClosed;

  private String   topicCountMin  = "0";

  private String   postCountMin   = "0";

  private String   viewCountMin   = "0";

  private String   moderator;

  private String   remain;

  private Calendar fromDateCreated;

  private Calendar toDateCreated;

  private Calendar fromDateCreatedLastPost;

  private Calendar toDateCreatedLastPost;

  private boolean  isAnd          = false;

  private boolean  isEmpty        = true;

  public void setListOfUser(List<String> listOfUser) {
    this.listOfUser = new ArrayList<String>();
    this.listOfUser.addAll(listOfUser);
  }

  public List<String> getListOfUser() {
    return listOfUser;
  }

  public long getUserPermission() {
    return userPermission;
  }

  public void setUserPermission(long userPermission) {
    this.userPermission = userPermission;
  }

  public ForumEventQuery() {
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getKeyValue() {
    return keyValue;
  }

  public void setKeyValue(String keyValue) {
    this.keyValue = keyValue;
  }

  public String getValueIn() {
    return valueIn;
  }

  public void setValueIn(String valueIn) {
    this.valueIn = valueIn;
  }

  public String getTopicType() {
    return topicType;
  }

  public void setTopicType(String topicType) {
    this.topicType = topicType;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getByUser() {
    return byUser;
  }

  public void setByUser(String byUser) {
    this.byUser = byUser;
  }

  public String getIsLock() {
    return isLock;
  }

  public void setIsLock(String isLock) {
    this.isLock = isLock;
  }

  public String getIsClose() {
    return isClosed;
  }

  public void setIsClose(String isClosed) {
    this.isClosed = isClosed;
  }

  public String getTopicCountMin() {
    return topicCountMin;
  }

  public void setTopicCountMin(String topicCountMin) {
    this.topicCountMin = topicCountMin;
  }

  public String getPostCountMin() {
    return postCountMin;
  }

  public void setPostCountMin(String postCountMin) {
    this.postCountMin = postCountMin;
  }

  public String getViewCountMin() {
    return viewCountMin;
  }

  public void setViewCountMin(String viewCountMin) {
    this.viewCountMin = viewCountMin;
  }

  public String getModerator() {
    return moderator;
  }

  public void setModerator(String moderator) {
    this.moderator = moderator;
  }

  public String getRemain() {
    return remain;
  }

  public void setRemain(String remain) {
    this.remain = remain;
  }

  public Calendar getFromDateCreated() {
    return fromDateCreated;
  }

  public void setFromDateCreated(Calendar fromDateCreated) {
    this.fromDateCreated = fromDateCreated;
  }

  public Calendar getToDateCreated() {
    return toDateCreated;
  }

  public void setToDateCreated(Calendar toDateCreated) {
    this.toDateCreated = toDateCreated;
  }

  public Calendar getFromDateCreatedLastPost() {
    return fromDateCreatedLastPost;
  }

  public void setFromDateCreatedLastPost(Calendar fromDateCreatedLastPost) {
    this.fromDateCreatedLastPost = fromDateCreatedLastPost;
  }

  public Calendar getToDateCreatedLastPost() {
    return toDateCreatedLastPost;
  }

  public void setToDateCreatedLastPost(Calendar toDateCreatedLastPost) {
    this.toDateCreatedLastPost = toDateCreatedLastPost;
  }

  public boolean getIsEmpty() {
    return this.isEmpty;
  }

  public String getPathQuery(List<String> listIds) {
    isAnd = false;
    isEmpty = true;
    String nodeType = (Utils.CATEGORY.equals(type)) ? EXO_FORUM_CATEGORY :
                      (Utils.FORUM.equals(type)) ? EXO_FORUM :
                      (Utils.TOPIC.equals(type)) ? EXO_TOPIC : EXO_POST;

    StringBuffer queryString = new StringBuffer();
    if (path != null && path.length() > 0)
      queryString.append(JCR_ROOT).append(path).append("//element(*,").append(nodeType).append(")");
    else
      queryString.append("//element(*,").append(nodeType).append(")");
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("[");
    if (keyValue != null && keyValue.length() > 0) {
      if (VALUE_IN_TITLE.equals(valueIn)) {
        stringBuffer.append("(jcr:contains(@").append(EXO_NAME).append(", '").append(keyValue).append("'))");
      } else {
        stringBuffer.append("(jcr:contains(., '").append(keyValue).append("'))");
      }
      isAnd = true;
    }

    if (byUser != null && byUser.length() > 0) {
      String temp = setArrays(byUser, EXO_OWNER);
      if (temp != null && temp.length() > 0) {
        stringBuffer.append(temp);
      }
    }
    if (type.equals(Utils.TOPIC)) {
      if (topicType != null && topicType.length() > 0 && !topicType.equals("all")) {
        if (isAnd)
          stringBuffer.append(" and ");
        stringBuffer.append("(@").append(EXO_TOPIC_TYPE).append("='").append(topicType).append("')");
        isAnd = true;
      }
    }
    if (isAnd){
      isEmpty = false;
    }
    if(type.equals(Utils.FORUM) || type.equals(Utils.TOPIC)) {
      if (isClosed != null && isClosed.length() > 0) {
        if (userPermission == 1) {
          if (type.equals(Utils.FORUM)) {
            if (isAnd)
              stringBuffer.append(" and ");
            if (isClosed.equals("all")) {
              stringBuffer.append("(@").append(EXO_IS_CLOSED).append("='false' or (")
                          .append(Utils.buildXpathByUserInfo(EXO_MODERATORS, listOfUser))
                          .append("))");
            } else if (isClosed.equals("false")) {
              stringBuffer.append("(@").append(EXO_IS_CLOSED).append("='").append(isClosed).append("')");
              isEmpty = false;
            } else if (isClosed.equals("true")) {
              stringBuffer.append("(@").append(EXO_IS_CLOSED).append("='").append(isClosed).append("' and (")
                                       .append(Utils.buildXpathByUserInfo(EXO_MODERATORS, listOfUser));
              stringBuffer.append("))");
              isEmpty = false;
            }
            isAnd = true;
          } else {
            if (!isClosed.equals("all")) {
              if (isAnd)
                stringBuffer.append(" and ");
              stringBuffer.append("(@").append(EXO_IS_CLOSED).append("='").append(isClosed).append("')");
              isAnd = true;
              isEmpty = false;
            }
          }
        } else {
          if (!isClosed.equals("all")) {
            if (isAnd)
              stringBuffer.append(" and ");
            stringBuffer.append("(@").append(EXO_IS_CLOSED).append("='").append(isClosed).append("')");
            isAnd = true;
            isEmpty = false;
          }
        }
      }
      if (isLock != null && isLock.length() > 0) {
        if (!isLock.equals("all")) {
          if (isAnd)
            stringBuffer.append(" and ");
          stringBuffer.append("(@").append(EXO_IS_LOCK).append("='").append(isLock).append("')");
          isAnd = true;
          isEmpty = false;
        }
      }
    }

    if (remain != null && remain.length() > 0) {
      if (isAnd)
        stringBuffer.append(" and ");
      stringBuffer.append("(").append(remain).append(")");
      isAnd = true;
    }
    
    if (moderator != null && moderator.length() > 0 && (Utils.FORUM.equals(type) || Utils.CATEGORY.equals(type))) {
      String temp = setArrays(moderator, EXO_MODERATORS);
      if (temp != null && temp.length() > 0) {
        stringBuffer.append(temp);
        isEmpty = false;
      }
    }
    
    String temp;
    if(Utils.FORUM.equals(type) || Utils.TOPIC.equals(type)) {
      temp = setValueMin(topicCountMin, EXO_TOPIC_COUNT);
      if (temp != null && temp.length() > 0) {
        stringBuffer.append(temp);
      }
      temp = setValueMin(postCountMin, EXO_POST_COUNT);
      if (temp != null && temp.length() > 0) {
        stringBuffer.append(temp);
      }
      if(Utils.TOPIC.equals(type)) {
        temp = setValueMin(viewCountMin, EXO_VIEW_COUNT);
        if (temp != null && temp.length() > 0) {
          stringBuffer.append(temp);
        }
        temp = setDateFromTo(fromDateCreatedLastPost, toDateCreatedLastPost, EXO_LAST_POST_DATE);
        if (temp != null && temp.length() > 0) {
          stringBuffer.append(temp);
        }
      }
    }

    temp = setDateFromTo(fromDateCreated, toDateCreated, EXO_CREATED_DATE);
    if (temp != null && temp.length() > 0) {
      stringBuffer.append(temp);
    }
    // add to search for user and moderator:
    if (type.equals(Utils.TOPIC) && userPermission > 1) {
      if (isAnd) {
        stringBuffer.append(" and ");
      }
      stringBuffer.append("(@").append(EXO_IS_APPROVED).append("='true' and @").append(EXO_IS_ACTIVE).append("='true' and @")
                  .append(EXO_IS_WAITING).append("='false' and @").append(EXO_IS_ACTIVE_BY_FORUM).append("='true')");
      
      String str = Utils.buildXpathByUserInfo(EXO_CAN_VIEW, listOfUser);
      if(!Utils.isEmpty(str)) {
        if (isAnd) {
          stringBuffer.append(" and ");
        }
        stringBuffer.append("(").append(Utils.buildXpathHasProperty(EXO_CAN_VIEW)).append(" or ")
        .append(str).append(" or @").append(EXO_OWNER).append("='").append(listOfUser.get(0)).append("'").append(")");
      }

    } 
    
    if (type.equals(Utils.POST)) {
      if (isAnd)
        stringBuffer.append(" and ");
      stringBuffer.append("(@").append(EXO_USER_PRIVATE).append("='").append(EXO_USER_PRI).append("'");
      for (String currentUser : listOfUser) {
        stringBuffer.append(" or @").append(EXO_USER_PRIVATE).append("='").append(currentUser).append("'");
      }
      stringBuffer.append(") and (@").append(EXO_IS_FIRST_POST).append("='false')");
      if (userPermission > 1) {
        stringBuffer.append(" and (@").append(EXO_IS_APPROVED).append("='true' and @").append(EXO_IS_ACTIVE_BY_TOPIC)
                    .append("='true' and @").append(EXO_IS_HIDDEN).append("='false')");
      }
    }

    if (listIds != null && listIds.size() > 0) {
      stringBuffer.append(" and (");
      int size = listIds.size();
      String searchBy = null;
      if (type.equals(Utils.CATEGORY) || type.equals(Utils.FORUM))
        searchBy = "fn:name()";
      else
        searchBy = "@" + EXO_PATH;
      for (int i = 0; i < size; i++) {
        if (i > 0)
          stringBuffer.append(" or ");
        stringBuffer.append(searchBy).append("='").append(listIds.get(i)).append("'");
      }
      stringBuffer.append(")");
    }

    stringBuffer.append("]");
    if (isAnd)
      queryString.append(stringBuffer.toString());
    return queryString.toString();
  }

  private String setArrays(String values, String property) {
    StringBuffer stringBuffer = new StringBuffer();
    StringBuilder builder = new StringBuilder();
    values = values.replaceAll(";", ",");
    if (values.indexOf(",") > 0) {
      String[] vls = values.split(",");
      int i = 0;
      for (String string : vls) {
        string = string.trim();
        if (string.length() > 0) {
          if (i > 0)
            builder.append(" or ");
          builder.append("(@").append(property).append("='").append(string).append("')");
          ++i;
        }
      }
    } else if (values.trim().length() > 0) {
      builder.append("@").append(property).append("='").append(values).append("'");
    }
    if (builder.length() > 0) {
      if (isAnd)
        stringBuffer.append(" and ");
      stringBuffer.append("(").append(builder).append(")");
      isAnd = true;
    }
    return stringBuffer.toString();
  }

  private String setValueMin(String min, String property) {
    StringBuffer queryString = new StringBuffer();
    if (Integer.parseInt(min) > 0) {
      if (isAnd)
        queryString.append(" and ");
      queryString.append("(@").append(property).append(">=").append(min).append(")");
      isAnd = true;
      isEmpty = false;
    }
    return queryString.toString();
  }

  private String setDateFromTo(Calendar fromDate, Calendar toDate, String property) {
    StringBuffer queryString = new StringBuffer();
    if (fromDate != null && toDate != null) {
      if (isAnd)
        queryString.append(" and ");
      queryString.append("((@").append(property).append(" >= xs:dateTime('").append(ISO8601.format(fromDate)).append("')) and ");
      queryString.append("(@").append(property).append(" <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))) ");
      isAnd = true;
      isEmpty = false;
    } else if (fromDate != null) {
      if (isAnd)
        queryString.append(" and ");
      queryString.append("(@").append(property).append(" >= xs:dateTime('").append(ISO8601.format(fromDate)).append("'))");
      isAnd = true;
      isEmpty = false;
    } else if (toDate != null) {
      if (isAnd)
        queryString.append(" and ");
      queryString.append("(@").append(property).append(" <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))");
      isAnd = true;
      isEmpty = false;
    }
    return queryString.toString();
  }
}
