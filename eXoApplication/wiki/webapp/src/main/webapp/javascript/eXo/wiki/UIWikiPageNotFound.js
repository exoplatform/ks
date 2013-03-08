/**
 * Copyright (C) 2012 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */(function(base, uiForm, webuiExt, $) {

if (!eXo.wiki) {
  eXo.wiki = {};
};

function UIWikiPageNotFound() {
};

UIWikiPageNotFound.prototype.init = function() {
  $(window).load(function() {
    setTimeout(eXo.wiki.UIWikiPageNotFound.hidePopup(), 1000);
  });
};

UIWikiPageNotFound.prototype.hidePopup = function() {
  // Find and hide all popup
  var popupL1 = document.getElementById('UIWikiPopupWindowL1');
  var popupL2 = document.getElementById('UIWikiPopupWindowL2');
  
  if (popupL1) {
    var closeLink = $(popupL1).find('a.CloseButton')[0];
    var event = closeLink["onclick"];
    if (typeof(event) == "function") {
        event.call(closeLink);
    }
  }
    
  if (popupL2) {
    var closeLink = $(popupL2).find('a.CloseButton')[0];
    var event = closeLink["onclick"];
    if (typeof(event) == "function") {
        event.call(closeLink);
    }
  }
}

eXo.wiki.UIWikiPageNotFound = new UIWikiPageNotFound();
return eXo.wiki.UIWikiPageNotFound;

})(base, uiForm, webuiExt, $);

