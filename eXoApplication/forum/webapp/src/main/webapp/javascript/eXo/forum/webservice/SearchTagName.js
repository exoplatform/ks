/**
 * Search Ip Ban using REST service
 * 
 */

eXo.require("eXo.core.JSON");
eXo.require("eXo.core.Keyboard");


function AjaxHandler(callbackObject, action) {
  this.LOADING_STATE = 'LOADING';
  this.SUCCESS_STATE = 'SUCCESS';
  this.ERROR_STATE = 'ERROR';
  this.TIMEOUT_STATE = 'TIMEOUT';
  this.action = action;
  if (callbackObject &&
      callbackObject._ajaxUpdate) {
    this.callbackObject = callbackObject;
  } else {
    this.callbackObject = false;
  }
}

AjaxHandler.prototype.onLoading = function(requestObj) {
//  window.console.info('[' + this.handler.action + '] ' + this.handler.LOADING_STATE);
  if (!this.handler.callbackObject) return;
  this.handler.callbackObject._ajaxUpdate(this.handler, this.handler.LOADING_STATE, requestObj, this.handler.action);
};

AjaxHandler.prototype.onSuccess = function(requestObj) {
//  window.console.info('[' + this.handler.action + '] ' + this.handler.SUCCESS_STATE);
  if (!this.handler.callbackObject) return;
  this.handler.callbackObject._ajaxUpdate(this.handler, this.handler.SUCCESS_STATE, requestObj, this.handler.action);
};

AjaxHandler.prototype.onError = function(requestObj) {
//  window.console.info('[' + this.handler.action + '] ' + this.handler.ERROR_STATE);
  if (!this.handler.callbackObject) return;
  this.handler.callbackObject._ajaxUpdate(this.handler, this.handler.ERROR_STATE, requestObj, this.handler.action);
};

AjaxHandler.prototype.onTimeout = function(requestObj) {
//  window.console.info('[' + this.handler.action + '] ' + this.handler.TIMEOUT_STATE);
  if (!this.handler.callbackObject) return;
  this.handler.callbackObject._ajaxUpdate(this.handler, this.handler.TIMEOUT_STATE, requestObj, this.handler.action);
};

function SearchTagName() {
	this.searchTagNameNode = null;
	this.uiGridNode = null;
  this.SEARCH_IP_BAN = 'Search tag name ajax action';
  this.data = null;
}

SearchTagName.prototype.init = function(userName) {
	var DOMUtil = eXo.core.DOMUtil;
	this.parentNode = document.getElementById('searchTagName');
	if(!this.parentNode) return;
	this.parentNode.style.visibility = "hidden";
	var searchInputId =  this.parentNode.getAttribute("inputId");
	this.searchTagNameNode = document.getElementById(searchInputId);
	if (!this.searchTagNameNode) {
		return;
	}
	this.searchTagNameNode.value = "";
  this.searchTagNameNode.onkeydown = this.searchIpBanWrapper;
  this.searchTagNameNode.onclick = this.submitInput;
  var buttonSearch = document.getElementById('ButtonSearch');
  if(buttonSearch){buttonSearch.onclick = this.submitInput;}
};

SearchTagName.prototype.submitInput = function(event) {
	var str = String(eXo.forum.webservice.SearchTagName.searchTagNameNode.value)
	if(eXo.forum.webservice.SearchTagName.parentNode.style.visibility === "hidden" && str.trim().length === 0) {
		eXo.forum.webservice.SearchTagName.searchTagName('onclickForm');
	}
};

SearchTagName.prototype.searchIpBanWrapper = function(event) {
	var key = eXo.core.Keyboard.getKeynum(event);
	if(key == 13){
		var object = eXo.forum.webservice.SearchTagName;
		var str = String(object.searchTagNameNode.value);
		if(object.parentNode.style.visibility === "visible"){
			object.searchTagNameNode.focus();
			object.parentNode.style.visibility = "hidden";
			object.searchTagName(' ');
		} else if(str.trim().length > 0){
			var linkSubmit = String(object.parentNode.getAttribute('linkSubmit'));
			linkSubmit = linkSubmit.replace("javascript:", "");
			eval(linkSubmit);
		}
		return;
	}
	if(key == 38 || key == 40){
		var DOMUtil = eXo.core.DOMUtil;
		var items = DOMUtil.findDescendantsByClass(this.parentNode, "div", "TagNameItem");
		if(items && items.length > 0) {
			var itemSl =  DOMUtil.findFirstDescendantByClass(this.parentNode, "div", "Selected");
			if(itemSl) {
				var t = items.length;
				for (var i = 0; i < t; i++) {
	        if(items[i] === itemSl){
	        	items[i].className = "TagNameItem";
	        	if(i == 0 && key == 38) {
	        		eXo.forum.webservice.SearchTagName.setValueInput(items[t-1]);
	        	}else if(i == (t-1) && key == 40){
	        		eXo.forum.webservice.SearchTagName.setValueInput(items[0]);
	        	} else if(key == 38){
	        		eXo.forum.webservice.SearchTagName.setValueInput(items[i-1]);
	        	} else if(key == 40) {
	        		eXo.forum.webservice.SearchTagName.setValueInput(items[i+1]);
	        	}
	        }
        }
			} else {
				eXo.forum.webservice.SearchTagName.setValueInput(items[0]);
			}
		}
	}else if(key > 40 || key == 8) {
		var str = String(eXo.forum.webservice.SearchTagName.searchTagNameNode.value)
		if(key == 8 && (str.trim().length === 0 ||str.trim().length === 1)){
			eXo.forum.webservice.SearchTagName.searchTagName('onclickForm');
		} else {
			window.setTimeout(eXo.forum.webservice.SearchTagName.searchIpBanTimeout, 50);
		}
	}
};

SearchTagName.prototype.setValueInput = function(elm) {
	elm.className = "TagNameItem Selected";
	var str = String(this.searchTagNameNode.value);
	str = str.substring(0, str.lastIndexOf(" "));
	var value = String(elm.innerHTML);
	value = value.substring(0, value.indexOf(" "));
	if(str.length == 0) str = value ;
	else str = str + " " + value;
	this.searchTagNameNode.value = str;
};

SearchTagName.prototype.searchIpBanTimeout = function() {
	eXo.forum.webservice.SearchTagName.searchTagName(eXo.forum.webservice.SearchTagName.searchTagNameNode.value);
};

SearchTagName.prototype.searchTagName = function(keyword) {
	// Get data from service, url: /ks/forum/filterTagNameForum/{strTagName}/
	keyword = String(keyword);
	var strs = keyword.split(" ");
	if(strs.length >= 1)keyword = strs[strs.length-1];
	keyword = keyword || ' ';
	var userAndTopicId = this.parentNode.getAttribute("userAndTopicId");
	var restPath = this.parentNode.getAttribute("restPath");
	if(userAndTopicId){
		var url = restPath + '/ks/forum/filterTagNameForum/' + userAndTopicId + '/' + keyword + '/';
		this.url_ = url;
	  var handler = new AjaxHandler(this, this.SEARCH_IP_BAN);
	  this.ajaxWrapper(handler, url, 'GET');
	}
}

SearchTagName.prototype._ajaxUpdate = function(ajaxHandler, state, requestObject, action) {
  switch (state) {
    case ajaxHandler.LOADING_STATE:
      break;
    case ajaxHandler.SUCCESS_STATE:
      if (action == this.STORE_DATA_AJAX_ACTION) {
        return;
      }
      var _data;
      if (requestObject.responseText) {
        try {
          _data = eXo.core.JSON.parse(requestObject.responseText);
//          window.console.dir(_data);
        } catch (e) {
//          window.console.error('JSON parser exception');
        }
      }
      if (_data) {
        this.data = _data;
        if (action == this.SEARCH_IP_BAN) {
          this.updateIpBanList();
        }
      }
      break;
    case ajaxHandler.ERROR_STATE:
      break;
    case ajaxHandler.TIMEOUT_STATE:
      break;
    default:
      break;
  }
};

SearchTagName.prototype.updateIpBanList = function() {
	var DOMUtil = eXo.core.DOMUtil;
	// Remove all old items
	var oldTagNameList = DOMUtil.findDescendantsByClass(this.parentNode, 'div', 'TagNameItem');
	for(var i=0; i < oldTagNameList.length; i++) {
		DOMUtil.removeElement(oldTagNameList[i]);		
	}
	// Fill up with new list
	var t = 0;
	var length_ = this.data.jsonList.length ;
	for(var i=0; i < length_; i++) {
		this.parentNode.appendChild(this.buildItemNode(this.data.jsonList[i].ip));
		t = 1;
	}
	if(t==1) this.parentNode.style.visibility = "visible";
	else this.parentNode.style.visibility = "hidden";
};

SearchTagName.prototype.buildItemNode = function(ip) {
	var SearchTagName = eXo.forum.webservice.SearchTagName;
	var itemNode = document.createElement('div');
	itemNode.className = 'TagNameItem';
	itemNode.innerHTML = ip;
	ip = ip.substring(0, ip.indexOf(' '));
	this.searchTagNameNode;
	itemNode.onclick = function() {
		var str = String(SearchTagName.searchTagNameNode.value);
		str = str.substring(0, str.lastIndexOf(' '))
		if(str.length == 0) str = ip ;
		else str = str + " " + ip;
		SearchTagName.searchTagNameNode.value = str;
		SearchTagName.searchTagNameNode.focus();
		SearchTagName.parentNode.style.visibility = "hidden";
		SearchTagName.searchTagName(' ');
	}
	itemNode.onmouseover = SearchTagName.onmouseOver;
	itemNode.onfocus = SearchTagName.onmouseOver;
	itemNode.onmouseout = SearchTagName.onmouseOut;
	itemNode.onblur = SearchTagName.onmouseOut;
	return itemNode;
};


SearchTagName.prototype.onmouseOver = function() {
	if (this.className === 'TagNameItem') {
  	  	this.className = 'TagNameItem OverItem';
	} else {
	  	this.className = 'TagNameItem OverItem Slect';
	}
};

SearchTagName.prototype.onmouseOut = function() {
	if (this.className === 'TagNameItem OverItem') {
		this.className = 'TagNameItem';
	} else {
		this.className = 'TagNameItem Selected';
	}
};

SearchTagName.prototype.ajaxProcessOverwrite = function(manualMode, ajaxRequest) {
  if (ajaxRequest.request == null) return ;
  ajaxRequest.request.open(ajaxRequest.method, ajaxRequest.url, true) ;   
  if (!manualMode) {
    if (ajaxRequest.method == "POST") {
      ajaxRequest.request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8") ;
    } else {
      ajaxRequest.request.setRequestHeader("Content-Type", "text/plain;charset=UTF-8") ;
    }
  } else {
    ajaxRequest.request.setRequestHeader("Content-Type", "text/xml;charset=UTF-8") ;
  }
  
  if (ajaxRequest.timeout > 0) setTimeout(ajaxRequest.onTimeoutInternal, ajaxRequest.timeout) ;
  
  ajaxRequest.request.send(ajaxRequest.queryString) ;
};

/**
 * 
 * @param {AjaxRequest} ajaxRequest
 * @param {Object} handler
 */
SearchTagName.prototype.initAjaxRequest = function(ajaxRequest, handler) {
  ajaxRequest.onSuccess = handler.onSuccess ;
  ajaxRequest.onLoading = handler.onLoading ;
  ajaxRequest.onTimeout = handler.onTimeout ;
  ajaxRequest.onError = handler.onError ;
  ajaxRequest.callBack = handler.callBack ;
  ajaxRequest.handler = handler;
  this.currentRequest = ajaxRequest ;
};

SearchTagName.prototype.ajaxWrapper =function(handler, url, method, data) {
  var request = new eXo.portal.AjaxRequest(method, url, data);
  this.initAjaxRequest(request, handler);
  this.ajaxProcessOverwrite(true, request);
//  request.process() ;
};

eXo.forum.webservice = eXo.forum.webservice || {};
eXo.forum.webservice.SearchTagName = new SearchTagName();
