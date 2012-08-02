/**
 * Copyright (C) 2010 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * @author Lai Trung Hieu
 */

eXo.require("eXo.core.Keyboard");

function UIWikiSearchBox() {
  this.restURL = null;
  this.input = null;
  this.searchPopup = null;
  this.searchType = "";
  this.menu = null;
  this.wikiNodeURI = null;
  this.searchMsg = "Search for";
};

UIWikiSearchBox.prototype.init = function(componentId, searchInputName, searchLabel, wikiNodeURI) {
  this.wikiNodeURI = wikiNodeURI;
  var uiComponent = document.getElementById(componentId);
  var restInput = uiComponent["restURL"];
  this.input = uiComponent[searchInputName];
  this.input.setAttribute('autocomplete', 'off');
  this.input.setAttribute('title', searchLabel);
  
  this.restURL = restInput.value;
  this.input.onkeyup = function(evt) {
    evt = window.event || evt;
    eXo.wiki.UIWikiSearchBox.pressHandler(evt, this);
  }
  this.input.form.onsubmit = function() {
    return false;
  }
  this.input.value = searchLabel;
  this.input.style.color = "Gray";
  this.input.onfocus = function(evt) {
    this.value = "";
    this.style.color = "Black";
  }
  this.input.onblur = function(evt) {
    if (this.value == '') {
      this.value = searchLabel;
      this.style.color = "Gray";
    }
  }
};

UIWikiSearchBox.prototype.pressHandler = function(evt, textbox) {
  var me = eXo.wiki.UIWikiSearchBox;
  evt = window.event || evt;
  var keyNum = eXo.core.Keyboard.getKeynum(evt);
  if (evt.altKey || evt.ctrlKey || evt.shiftKey)
    return;
  switch (keyNum) {
  case 13:
    if (textbox.value.trim() != "")
      me.enterHandler(evt);
    break;
  case 27:
    me.escapeHandler();
    break;
  case 38:
    me.arrowUpHandler();
    break;
  case 40:
    me.arrowDownHandler();
    break;
  default:
    if (me.typeTimeout)
      clearTimeout(me.typeTimeout);
    if (me.currentItem)
      me.currentItem = null;
    me.typeTimeout = setTimeout(function() {
      if (me.xhr) {
        me.xhr.abort();
        me.xhr = null;
        delete me.xhr;
      }
      me.typeHandler(textbox);
      clearTimeout(me.typeTimeout);
    }, 500)
  }
  return;
};

/**
 * Handler key press
 */

UIWikiSearchBox.prototype.enterHandler = function(evt) {
  var me = eXo.wiki.UIWikiSearchBox;
  if (me.currentItem) {
    var link = me.currentItem.firstChild.firstChild;
    if (link.href) {
      evt.cancelBubble = true;
      if (evt.stopPropagation)
        evt.stopPropagation();
      this.searchPopup.style.display = "none";
      window.location = link.href;
    }
  } else {
    me.doAdvanceSearch();
  }
};

UIWikiSearchBox.prototype.escapeHandler = function() {
  var me = eXo.wiki.UIWikiSearchBox;
  if (me.currentItem)
    me.currentItem = null;
  eXo.wiki.UIWikiSearchBox.hideMenu();
};

UIWikiSearchBox.prototype.arrowUpHandler = function() {
  var me = eXo.wiki.UIWikiSearchBox;
  if (!me.currentItem) {
    me.currentItem = this.menu.lastChild;
    eXo.core.DOMUtil.addClass(me.currentItem, "ItemOver");
    return;
  }
  eXo.core.DOMUtil.replaceClass(me.currentItem, "ItemOver", "");
  if (me.currentItem.previousSibling)
    me.currentItem = me.currentItem.previousSibling;
  else
    me.currentItem = this.menu.lastChild;
  eXo.core.DOMUtil.addClass(me.currentItem, "ItemOver");
};

UIWikiSearchBox.prototype.arrowDownHandler = function() {
  var me = eXo.wiki.UIWikiSearchBox;
  if (!me.currentItem) {
    me.currentItem = this.menu.firstChild;
    eXo.core.DOMUtil.addClass(me.currentItem, "ItemOver");
    return;
  }
  eXo.core.DOMUtil.replaceClass(me.currentItem, "ItemOver", "");
  if (me.currentItem.nextSibling)
    me.currentItem = me.currentItem.nextSibling;
  else
    me.currentItem = this.menu.firstChild;
  eXo.core.DOMUtil.addClass(me.currentItem, "ItemOver");
};

UIWikiSearchBox.prototype.typeHandler = function(textbox) {
  var keyword = this.createKeyword(textbox.value);
  if (keyword == '') {
    eXo.wiki.UIWikiSearchBox.hideMenu();
    return;
  }
  var url = this.restURL + keyword;
  this.makeRequest(url, this.typeCallback);

};

UIWikiSearchBox.prototype.makeRequest = function(url, callback) {
  var me = eXo.wiki.UIWikiSearchBox;
  this.xhr = eXo.core.Browser.createHttpRequest();
  this.xhr.open('GET', url, true);
  this.xhr.setRequestHeader("Cache-Control", "max-age=86400");
  this.xhr.onreadystatechange = function() {
    if (me.xhr.readyState == 4 && me.xhr.status == 200) {
      if (callback)
        callback(me.xhr.responseText);
    }
  }
  me.xhr.send(null);
};

UIWikiSearchBox.prototype.typeCallback = function(data) {
  if (!data)
    return;
  eval("var data = " + data.trim());
  if (typeof (data) != "object")
    return;
  eXo.wiki.UIWikiSearchBox.renderMenu(data);
};

UIWikiSearchBox.prototype.doAdvanceSearch = function() {
  var action = eXo.core.DOMUtil.findAncestorByClass(this.input, "SearchForm");
  action = eXo.core.DOMUtil.findFirstChildByClass(action, "a", "AdvancedSearch");
  eXo.wiki.UIWikiAjaxRequest.makeNewHash('#AdvancedSearch');
  action.onclick();
}

/**
 * Render Contextual Search Menu
 */

UIWikiSearchBox.prototype.renderMenu = function(data) {
  var me = eXo.wiki.UIWikiSearchBox;
  var searchBox = eXo.core.DOMUtil.findAncestorByClass(this.input, "UIWikiSearchBox");
  this.searchPopup = eXo.core.DOMUtil.findFirstDescendantByClass(searchBox, "div", "SearchPopup");
  this.searchPopup.style.display = "block";
  this.searchPopup.onmouseup = function(evt) {
    this.style.display = "none";
    evt.cancelBubble = true;
    if (evt.stopPropagation())
      evt.stopPropagation();
  }
  this.menu = eXo.core.DOMUtil.findFirstDescendantByClass(this.searchPopup, "div", "SubBlock");
  var resultLength = data.jsonList.length;
  this.menu.innerHTML = "";

  var textNode = document.createTextNode('');
  this.menu.appendChild(textNode);

  var searchItemNode = document.createElement('div');
  searchItemNode.className = 'MenuItem TextItem Horizon';
  var searchText = document.createElement('div');
  searchText.className = 'MenuText';
  var linkNode = document.createElement("a");
  linkNode.className = 'ItemIcon MenuIcon';
  linkNode.setAttribute('href', 'javascript:eXo.wiki.UIWikiSearchBox.doAdvanceSearch();');
   linkNode.setAttribute('title', this.searchMsg+ " \'" + this.input.value + "\'");
  linkNode.innerHTML = this.searchMsg + " \'" + this.input.value + "\'";
  linkNode.setAttribute('title', this.searchMsg + " \'" + this.input.value + "\'");
  searchText.appendChild(linkNode);  
  searchItemNode.appendChild(searchText);  
  this.menu.insertBefore(searchItemNode, textNode);
  me.shortenWord(linkNode, searchText); 

  for ( var i = 0; i < resultLength; i++) {
    var itemNode = this.buildChild(data.jsonList[i]);
    this.menu.insertBefore(itemNode, searchItemNode);
    // Check if title is outside of the container
    var linkContainer = itemNode.firstChild;
    var link = linkContainer.firstChild;
    var keyword = this.input.value.trim();    
    var origin =  link.innerHTML;   
    var shorten =  me.shortenWord(link, linkContainer);
    if (origin!= shorten && keyword.length >= shorten.length-3)
      link.innerHTML = me.doHighLight(shorten, shorten.substring(0,shorten.length-3));
    else
      link.innerHTML = me.doHighLight(shorten, keyword);
  }
  this.menu.removeChild(this.menu.lastChild);
};

UIWikiSearchBox.prototype.buildChild = function(dataObject) {
  var menuItemNode = document.createElement('div');
  menuItemNode.className = 'MenuItem TextItem';
  // Create Horizon div
  if (this.searchType != dataObject.type) {
    menuItemNode.className = 'MenuItem TextItem Horizon';
  }
  this.searchType = dataObject.type;
  var searchText = document.createElement('div');
  searchText.className = 'MenuText ';

  var linkNode = document.createElement("a");
  linkNode.className = 'ItemIcon MenuIcon';
  if (dataObject.type == "wiki:attachment") {
    linkNode.setAttribute('href', dataObject.uri);
  } else {
    linkNode.setAttribute('href', this.wikiNodeURI + dataObject.uri);
  }
  var keyword = this.input.value.trim();
  var labelResult = dataObject.fullTitle;
  linkNode.setAttribute('title', labelResult);
  linkNode.innerHTML = labelResult;
  searchText.appendChild(linkNode);
  menuItemNode.appendChild(searchText);
  return menuItemNode;
};

/**
 * Other functions
 */

UIWikiSearchBox.prototype.createKeyword = function(str) {
  if (str.indexOf(",") != -1) {
    str = str.substr(str.lastIndexOf(",") + 1, str.length);
  }
  str = str.replace(/^\s*/, "");
  return str;
};

UIWikiSearchBox.prototype.hideMenu = function() {
  if (this.searchPopup)
    this.searchPopup.style.display = "none";
};

UIWikiSearchBox.prototype.doHighLight = function(text, keyword) {
  var hiRE = new RegExp("(" + keyword + ")", "gi");
  text = text.replace(hiRE, "<strong>$1</strong>");  
  return text;
}

UIWikiSearchBox.prototype.shortenWord = function(source, container) {
  var isCut = false;
  while (source.offsetWidth > container.offsetWidth) {
    isCut = true;
    var size = source.innerHTML.length;
    source.innerHTML = source.innerHTML.substring(0, size - 1);
  }
  if (isCut) {    
    source.innerHTML = source.innerHTML.substring(0, source.innerHTML.length - 8) + "...";
  }
  return source.innerHTML;
};

eXo.wiki.webservice = eXo.wiki.webservice || {};

eXo.wiki.UIWikiSearchBox = new UIWikiSearchBox();