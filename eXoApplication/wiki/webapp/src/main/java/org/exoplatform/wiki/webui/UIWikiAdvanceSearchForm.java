/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 */
package org.exoplatform.wiki.webui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectOption;
import org.exoplatform.webui.core.model.SelectOptionGroup;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.service.search.WikiSearchData;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiAdvanceSearchForm.gtmpl",
  events = {
      @EventConfig(listeners = UIWikiAdvanceSearchForm.SearchActionListener.class)            
    }
)

public class UIWikiAdvanceSearchForm extends UIForm {
  final static String TEXT  = "text".intern();

  final static String WIKIS = "wikis".intern();
  
  public UIWikiAdvanceSearchForm() throws Exception {
    addChild(new UIFormStringInput(TEXT, TEXT, null));
    List<SelectOptionGroup> wikis = renderWikisOptions();
    UIFormSelectBoxWithGroups selectSpaces = new UIFormSelectBoxWithGroups(WIKIS,
                                                                           WIKIS,
                                                                           new ArrayList<SelectItem>(wikis));
    
    selectSpaces.setValue(getDefaultSelectWikiValue());
    addChild(selectSpaces);
    
    this.setActions(new String[] { "Search" });
  }

  public void resetWikiSpaces() throws Exception {
    getChild(UIFormSelectBoxWithGroups.class).setOptions(new ArrayList<SelectItem>(renderWikisOptions()))
                                             .setValue(getDefaultSelectWikiValue());
  }
  
  public List<SelectOptionGroup> renderWikisOptions() throws Exception {
    List<SelectOptionGroup> listOptions = new ArrayList<SelectOptionGroup>();
    if (getAllWikiOptions().getOptions().size() > 0) {
      listOptions.add(getAllWikiOptions());
    }
    if (getPortalWikiOptions().getOptions().size() > 0) {
    	listOptions.add(getPortalWikiOptions());
    }
    if (getGroupWikiOptions().getOptions().size() > 0) {
		SelectOptionGroup selectOptionGroup = getGroupWikiOptions();
		List<SelectOption> options = selectOptionGroup.getOptions();

		Collections.sort(options, new Comparator<SelectOption>() {
			@Override
			public int compare(SelectOption o1, SelectOption o2) {
				return (o1.getLabel().compareTo(o2.getLabel()));
			}
		});
        listOptions.add(selectOptionGroup);
    }
    
    if (getUserWikiOptions().getOptions().size() > 0) {
    	listOptions.add(getUserWikiOptions());
    }
    return listOptions;
  }

  public SelectOptionGroup getAllWikiOptions() throws Exception {
    SelectOptionGroup allSpaceOptions = new SelectOptionGroup("");
    allSpaceOptions.addOption(new SelectOption(getLabel("AllWikis"), ""));
    allSpaceOptions.addOption(new SelectOption(getLabel("AllPortalWikis"), PortalConfig.PORTAL_TYPE));
    allSpaceOptions.addOption(new SelectOption(getLabel("AllGroupWikis"), PortalConfig.GROUP_TYPE));
    allSpaceOptions.addOption(new SelectOption(getLabel("AllUserWikis"), PortalConfig.USER_TYPE));
    return allSpaceOptions;
  }

  public SelectOptionGroup getPortalWikiOptions() throws Exception {
    SelectOptionGroup portalSpaceOptions = new SelectOptionGroup(getLabel("PortalWikis"));
    Collection<Wiki> portalWikis = Utils.getWikisByType(WikiType.PORTAL);
    for (Wiki wiki : portalWikis) {
      portalSpaceOptions.addOption(new SelectOption(wiki.getOwner(), PortalConfig.PORTAL_TYPE + "/"
          + wiki.getOwner()));
    }
    return portalSpaceOptions;
  }

  public SelectOptionGroup getGroupWikiOptions() throws Exception {
    SelectOptionGroup groupSpaceOptions = new SelectOptionGroup(getLabel("GroupWikis"));
    Collection<Wiki> groupWikis = Utils.getWikisByType(WikiType.GROUP);
    for (Wiki wiki : groupWikis) {
      groupSpaceOptions.addOption(new SelectOption(wiki.getOwner(), PortalConfig.GROUP_TYPE + "/"
          + Utils.validateWikiOwner(wiki.getType(), wiki.getOwner())));
    }
    return groupSpaceOptions;
  }

  public SelectOptionGroup getUserWikiOptions() throws Exception {
    SelectOptionGroup userSpaceOptions = new SelectOptionGroup(getLabel("UserWikis"));
    Collection<Wiki> userWikis = Utils.getWikisByType(WikiType.USER);
    for (Wiki wiki : userWikis) {
      userSpaceOptions.addOption(new SelectOption(wiki.getOwner(), PortalConfig.USER_TYPE + "/"
          + wiki.getOwner()));
    }
    return userSpaceOptions;
  }
  
  public void processSearchAction() throws Exception {
    WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
    String text = getUIStringInput(TEXT).getValue();
    text = (text == null) ? StringUtils.EMPTY : text.trim();
    String path = getChild(UIFormSelectBoxWithGroups.class).getValue();
    String wikiType = null;
    String wikiOwner = null;
    if (!CommonUtils.isEmpty(path)) {
      String[] arrayParams = path.split(CommonUtils.SLASH);
      if (arrayParams.length >= 1) {
        wikiType = arrayParams[0];
        if (arrayParams.length >= 2)
          wikiOwner = StringUtils.replace(path, wikiType + CommonUtils.SLASH, CommonUtils.EMPTY_STR);
      }
    }
    WikiSearchData data = new WikiSearchData(text, null, null, wikiType, wikiOwner);
    PageList<SearchResult> results = wservice.search(data);
    UIWikiAdvanceSearchResult uiSearchResults = getParent().findFirstComponentOfType(UIWikiAdvanceSearchResult.class);
    uiSearchResults.setKeyword(text);
    uiSearchResults.setResult(results);
  }
  
  private String getDefaultSelectWikiValue() throws Exception {
    WikiPageParams currentParams = org.exoplatform.wiki.commons.Utils.getCurrentWikiPageParams();
    String wikiType = currentParams.getType();
    String owner = currentParams.getOwner();
    return wikiType + "/" + Utils.validateWikiOwner(wikiType, owner);
  }
  
  static public class SearchActionListener extends EventListener<UIWikiAdvanceSearchForm> {
    public void execute(Event<UIWikiAdvanceSearchForm> event) throws Exception {
      UIWikiAdvanceSearchForm uiSearch = event.getSource() ;
      uiSearch.processSearchAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearch.getAncestorOfType(UIWikiSearchSpaceArea.class));
    }
  }  

}
