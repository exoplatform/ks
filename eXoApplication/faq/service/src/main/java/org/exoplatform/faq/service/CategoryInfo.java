/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.faq.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 24, 2009 - 3:34:08 AM  
 */
public class CategoryInfo {
  private String                id;

  private String                path;

  private String                name;

  private List<String>          pathName;

  private List<QuestionInfo>    questionInfos   = new ArrayList<QuestionInfo>();

  private List<SubCategoryInfo> subCateInfos    = new ArrayList<SubCategoryInfo>();
  
  private boolean               enabledLoadMore = true;

  public CategoryInfo() {
    id = Utils.CATEGORY_HOME;
    path = id;
    name = id;
    pathName = new ArrayList<String>();
    pathName.add(id);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getPathName() {
    return pathName;
  }

  public void setPathName(List<String> pathName) {
    this.pathName = pathName;
  }

  public List<QuestionInfo> getQuestionInfos() {
    return questionInfos;
  }

  public void setQuestionInfos(List<QuestionInfo> questionInfos) {
    this.questionInfos = questionInfos;
  }

  public List<SubCategoryInfo> getSubCateInfos() {
    return subCateInfos;
  }

  public void setSubCateInfos(List<SubCategoryInfo> subCateInfos) {
    this.subCateInfos = subCateInfos;
  }
  
  public boolean hasLoadMore() {
    return enabledLoadMore;
  }

  public void setLoadMore(boolean enabledLoadMore) {
    this.enabledLoadMore = enabledLoadMore;
  }
}
