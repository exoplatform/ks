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

eXo.require("eXo.core.EventManager");

if (!eXo.wiki)
  eXo.wiki = {};

function UIWikiPortlet() {
};


UIWikiPortlet.prototype.init = function(portletId, linkId) {
  var me = eXo.wiki.UIWikiPortlet;
  me.wikiportlet = document.getElementById(portletId);
  me.changeWindowTite(me.wikiportlet);
  me.changeModeLink = document.getElementById(linkId);
  var head = document.getElementsByTagName('head')[0];
  var newStyle = document.createElement('style');
  newStyle.setAttribute('type', 'text/css');
  newStyle.setAttribute('media', 'print');
  newStyle.appendChild(document.createTextNode('body { position:relative;overflow:visible  !important; } body * {       visibility: hidden;  }'));
  head.appendChild(newStyle);
}

UIWikiPortlet.prototype.changeWindowTite = function(elm) {
  if(elm) {
    var breadCrumb = eXo.core.DOMUtil.findFirstDescendantByClass(elm, 'div', 'UIWikiBreadCrumb');
    var selected = eXo.core.DOMUtil.findFirstDescendantByClass(breadCrumb, 'a', 'Selected');
    if(selected) {
      top.document.title = selected.innerHTML;
    }
  }
};

UIWikiPortlet.prototype.onMouseUp = function(evt) {
  var me = eXo.wiki.UIWikiPortlet;
  var evt = evt || window.event;
  var target = evt.target || evt.srcElement;
  if (evt.button == 2)
    return;
  var searchPopup = eXo.core.DOMUtil.findFirstDescendantByClass(me.wikiportlet, "div", "SearchPopup");
  if (searchPopup)
    searchPopup.style.display = 'none';
  var breadCrumbPopup = eXo.wiki.UIWikiPortlet.getBreadcrumbPopup();
  if (breadCrumbPopup) {
    breadCrumbPopup.style.display = 'none';
  }
  if (target.tagName == "A" || (target.tagName == "INPUT" && target.type == "button") || target.tagName == "SELECT"
      || target.tagName == "DIV" && target.className.indexOf("RefreshModeTarget") > 0) {
    eXo.wiki.UIWikiPortlet.changeMode(evt);
  }
}

UIWikiPortlet.prototype.onKeyUp = function(evt) {
  var evt = evt || window.event;
  var target = evt.target || evt.srcElement;
  if (target.tagName == "INPUT" && target.type == "text")
    if (evt.keyCode == 13)
      eXo.wiki.UIWikiPortlet.changeMode(evt);
}

UIWikiPortlet.prototype.changeMode = function(event) {
  setTimeout("eXo.wiki.UIWikiPortlet.timeChangeMode()", 200);
};

UIWikiPortlet.prototype.timeChangeMode = function() {
  var me = eXo.wiki.UIWikiPortlet;
  var currentURL = document.location.href;
  var mode = "";
   if(currentURL.indexOf("action=AddPage") > 0) {
    mode = "AddPage";
  } else if (currentURL.indexOf("#") > 0) {
    mode = currentURL.substring(currentURL.indexOf("#") + 1, currentURL.length);
    if (mode && mode.length > 0 && mode.charAt(0) == 'H') {
      mode = "";
    }
    if (mode.indexOf("/") > 0)
      mode = mode.substring(0, mode.indexOf("/"));
  } 
  var link = me.changeModeLink;
  var endParamIndex = link.href.lastIndexOf("')");
  var modeIndex = link.href.indexOf("&mode");
  if (modeIndex < 0)
    link.href = link.href.substring(0, endParamIndex) + "&mode=" + mode + "')";
  else
    link.href = link.href.substring(0, modeIndex) + "&mode=" + mode + "')";
  window.location = link.href;
};

UIWikiPortlet.prototype.showPopup = function(elevent, e) {
  var strs = [ "AddTagId", "goPageTop", "goPageBottom", "SearchForm" ];
  for ( var t = 0; t < strs.length; t++) {
    var elm = document.getElementById(strs[t]);
    if (elm)
      elm.onclick = eXo.wiki.UIWikiPortlet.cancel;
  }
  if (!e)
    e = window.event;
  e.cancelBubble = true;
  var parend = eXo.core.DOMUtil.findAncestorByTagName(elevent, "div");
  var popup = eXo.core.DOMUtil.findFirstDescendantByClass(parend, "div", "UIPopupCategory");
  if (popup.style.display === "none") {
    popup.style.display = "block";
    eXo.core.DOMUtil.listHideElements(popup);
  } else {
    popup.style.display = "none";
  }
};

UIWikiPortlet.prototype.cancel = function(evt) {
  var _e = window.event || evt;
  _e.cancelBubble = true;
};

/*
 * Render the breadcrumb again to fit with a half of screen width
 */
UIWikiPortlet.prototype.renderBreadcrumbs = function(uicomponentid, isLink) {
  var me = eXo.wiki.UIWikiPortlet;
  var component = document.getElementById(uicomponentid);
  var DOMUtil = eXo.core.DOMUtil;
  var breadcrumb = DOMUtil.findFirstDescendantByClass(component, 'div', 'BreadcumbsInfoBar');
  var breadcrumbPopup = DOMUtil.findFirstDescendantByClass(component, 'div', 'SubBlock');
  // breadcrumbPopup = DOMUtil.findFirstDescendantByClass(breadcrumbPopup, 'div', 'SubBlock');
  var itemArray = DOMUtil.findDescendantsByTagName(breadcrumb, "a");
  var shortenFractor = 3 / 4;
  itemArray.shift();
  var ancestorItem = itemArray.shift();
  var lastItem = itemArray.pop();
  if (lastItem == undefined){
    return;
  }
  var parentLastItem = itemArray.pop();
  if(parentLastItem == undefined) {
    return;
  }
  var popupItems = new Array();
  var firstTime = true;
  var content = String(lastItem.innerHTML);
  while (breadcrumb.offsetWidth > shortenFractor * breadcrumb.parentNode.offsetWidth) {
    if (itemArray.length > 0) {
      var arrayLength = itemArray.length;
      var item = itemArray.pop();
      popupItems.push(item);
      if (firstTime) {
        firstTime = false;
        var newItem = item.cloneNode(true);
        newItem.innerHTML = ' ... ';
        if (isLink) {
          newItem.href = '#';
          eXo.core.Browser.eventListener(newItem, 'mouseover', me.showBreadcrumbPopup);
        }
        breadcrumb.replaceChild(newItem, item);
      } else {
        var leftBlock = DOMUtil.findPreviousElementByTagName(item, 'div');
        breadcrumb.removeChild(leftBlock);
        breadcrumb.removeChild(item);
      }
    } else {
      break;
    }
  }

  if (content.length != lastItem.innerHTML.length) {
    lastItem.innerHTML = '<span title="' + content + '">' + lastItem.innerHTML + '...' + '</span>';
  }
  me.createPopup(popupItems, isLink, breadcrumbPopup);
};

UIWikiPortlet.prototype.createPopup = function(popupItems, isLink, breadcrumbPopup){
  if (isLink) {
    var popupItemDepth = -1;
    for (var index = popupItems.length - 1; index >= 0; index--) {
      popupItems[index].className = 'ItemIcon MenuIcon';
      popupItemDepth++;
      var menuItem = document.createElement('div');
      menuItem.className = 'MenuItem';
      var previousDiv = menuItem;
      for (var i = 0; i < popupItemDepth; i++) {
        var marginLeftDiv = document.createElement('div');
        marginLeftDiv.className = 'MarginLeftDiv';
        previousDiv.appendChild(marginLeftDiv);
        previousDiv = marginLeftDiv;
        if (i == popupItemDepth - 1) {
          previousDiv.appendChild(popupItems[index]);
        }
      }
      if (popupItemDepth == 0) {
        menuItem.appendChild(popupItems[index]);
      }
      breadcrumbPopup.appendChild(menuItem);
    }
    
  }
};

/*
 * Remove last characters of item until a condition happen
 */
UIWikiPortlet.prototype.shortenUntil = function(item, condition) {
  var isShortent = false;
  while (!condition() && item.innerHTML.length > 3) {
    item.innerHTML = item.innerHTML.substring(0, item.innerHTML.length - 1);
    isShortent = true;
  }
  if (isShortent) {
    if(item.innerHTML.length > 6) {
      item.innerHTML = item.innerHTML.substring(0, item.innerHTML.length - 3);
    }
    item.innerHTML = item.innerHTML + ' ... ';
  }
};

UIWikiPortlet.prototype.getBreadcrumbPopup = function() {
  var breadcrumb = document.getElementById("UIWikiBreadCrumb");
  var breadcrumbPopup = eXo.core.DOMUtil.findFirstDescendantByClass(breadcrumb, 'div', 'BreadcumPopup');
  return breadcrumbPopup;
};

UIWikiPortlet.prototype.showBreadcrumbPopup = function(evt) {
  var breadcrumbPopup = eXo.wiki.UIWikiPortlet.getBreadcrumbPopup();
  var ellipsis = evt.target || evt.srcElement;
  var isRTL = eXo.core.I18n.isRT();
  var offsetLeft = eXo.core.Browser.findPosX(ellipsis, isRTL) - 20;
  var offsetTop = eXo.core.Browser.findPosY(ellipsis) + 20;
  breadcrumbPopup.style.zIndex= '100';
  breadcrumbPopup.style.display = 'block';
  breadcrumbPopup.style.left = offsetLeft + 'px';
  breadcrumbPopup.style.top = offsetTop + 'px';
};


UIWikiPortlet.prototype.highlightEditSection = function (header, highlight) {
  var sectionContainer = eXo.core.DOMUtil.findAncestorByClass(header, 'section-container');
  var section = eXo.core.DOMUtil.findFirstDescendantByClass(header, 'span', 'EditSection');
  if (highlight == true) {
    section.style.display = 'block';
    sectionContainer.style.backgroundColor = '#F7F7F7';
  }
  else {
    section.style.display = 'none';
    sectionContainer.style.backgroundColor = '';
  }
};

UIWikiPortlet.prototype.createURLHistory = function (uicomponentId, isShow) {
  if(isShow || isShow === 'true'){
    setTimeout("eXo.wiki.UIWikiPortlet.urlHistory('"+uicomponentId+"')", 500);
  }
};

UIWikiPortlet.prototype.urlHistory = function (uicomponentId) {
  var component = document.getElementById(uicomponentId);
  if(component) {
    var local = String(window.location);
    if(local.indexOf('#') < 0 || local.indexOf('#') === (local.length-1)) {
      window.location = local.replace('#', '') + '#ShowHistory';
    }
  }
};

UIWikiPortlet.prototype.makeRenderingErrorsExpandable = function (uicomponentId) {
  var uicomponent = document.getElementById(uicomponentId);
  var DOMUtil = eXo.core.DOMUtil;
  if(uicomponent) {
  	var renderingErrors = DOMUtil.findDescendantsByClass(uicomponent,"span","xwikirenderingerror" );
    for (i=0;i<renderingErrors.length;i++) {
    var renderingError = renderingErrors[i];
    var descriptionError = renderingError.nextSibling;
    if (descriptionError.innerHTML !== "" && DOMUtil.hasClass(descriptionError,"xwikirenderingerrordescription")) {
      renderingError.style.cursor="pointer";            
    	eXo.core.EventManager.addEvent(renderingError,"click",function(event){
    		if(!DOMUtil.hasClass(this.nextSibling,"hidden")) {
    		  this.nextSibling.className += ' ' + "hidden";
        }
        else {
            DOMUtil.removeClass(this.nextSibling, "hidden");
          }
        });
      }
    }
  }
};

UIWikiPortlet.prototype.decorateSpecialLink = function(uicomponentId) {
  var DOMUtil = eXo.core.DOMUtil;
  var uicomponent = document.getElementById(uicomponentId);
  var invalidChars = DOMUtil.findFirstDescendantByClass(uicomponent, "div",
      "InvalidChars");
  var invalidCharsMsg = invalidChars.innerText;
  if (!invalidCharsMsg || typeof(invalidCharsMsg) == "undefined") {
    invalidCharsMsg = invalidChars.textContent;
  }
  if (uicomponent) {
    var linkSpans = DOMUtil.findDescendantsByClass(uicomponent, "span", "wikicreatelink");
    for (i = 0; i < linkSpans.length; i++) {
      var linkSpan = linkSpans[i];
      var pageLink = linkSpan.childNodes[0];
      if (typeof(pageLink) != "undefined" && pageLink.href == "javascript:void(0);") {
        eXo.core.EventManager.addEvent(pageLink, "click", function(event) {
          alert(invalidCharsMsg);
          return;
        });
      }
    }
  }
};

UIWikiPortlet.prototype.keepSessionAlive = function(isKeepSessionAlive) {
  if (isKeepSessionAlive == true) {
    eXo.session.itvInit();
  } else {
    eXo.session.destroyItv();
    eXo.session.initialized = false;
    eXo.session.openUrl = null;
  }
}

eXo.wiki.UIWikiPortlet = new UIWikiPortlet();

/** ******************* Other functions ***************** */

String.prototype.trim = function() {
  return this.replace(/^\s+|\s+$/g, '');
};

String.prototype.replaceAll = function(oldText, newText) {
  return this.replace(new RegExp(oldText, "g"), newText);
}
