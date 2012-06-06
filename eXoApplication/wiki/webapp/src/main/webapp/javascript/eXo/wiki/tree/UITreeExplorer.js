
eXo.require("eXo.core.EventManager");

if(eXo.wiki.UITreeExplorer ==  null) {
  eXo.wiki.UITreeExplorer = {};
};

function UITreeExplorer() {};

UITreeExplorer.prototype.init = function( componentid, initParam, isFullRender, isRenderLink, baseLink ) {
  var me = eXo.wiki.UITreeExplorer;
  var component = document.getElementById(componentid);
  if (component == null) {
    var editForm = document.getElementById('UIWikiPageEditForm');
    var ifm = eXo.core.DOMUtil.findFirstDescendantByClass(editForm, 'iframe', 'gwt-RichTextArea');
    // Store current iframe element
    me.innerDoc = ifm.contentDocument || ifm.contentWindow.document;

    component = me.innerDoc.getElementById(componentid);
  }
  
  this.isRenderLink = isRenderLink;
  this.baseLink = baseLink;
  var initURL = eXo.core.DOMUtil.findFirstDescendantByClass(component, "input", "InitURL");
  var initNode = eXo.core.DOMUtil.findFirstDescendantByClass(component, "div", "NodeGroup");
  initParam = me.cleanParam(initParam);
  me.render(initParam, initNode, isFullRender);
};

UITreeExplorer.prototype.collapseExpand = function(element) {
  var node = element.parentNode;
  var subGroup = eXo.core.DOMUtil.findFirstChildByClass(node, "div", "NodeGroup");
  if (element.className == "EmptyIcon")
    return true;
  if (!subGroup)
    return false;
  if (subGroup.style.display == "none") {
    if (element.className == "ExpandIcon")
      element.className = "CollapseIcon";
    subGroup.style.display = "block";
  } else {
    if (element.className == "CollapseIcon")
      element.className = "ExpandIcon";
    subGroup.style.display = "none";
  }
  return true;
};

UITreeExplorer.prototype.onNodeClick = function(node, absPath) {
  var me = eXo.wiki.UITreeExplorer;
  var selectableObj = eXo.core.DOMUtil.findDescendantsByTagName(node, "a");
  if (selectableObj.length > 0) {
    var component = eXo.core.DOMUtil.findAncestorByClass(node,"UITreeExplorer");
    var selectedNode = eXo.core.DOMUtil.findFirstDescendantByClass(component, "div", "Hover");
    if (selectedNode)    
    eXo.core.DOMUtil.removeClass(selectedNode, "Hover");
    if (!eXo.core.DOMUtil.hasClass(node, "Hover"))
      eXo.core.DOMUtil.addClass(node, "Hover");
    me.selectNode(node, absPath);
  }
};

UITreeExplorer.prototype.selectNode = function(node, nodePath) {
  var me = eXo.wiki.UITreeExplorer;
  var component = eXo.core.DOMUtil.findAncestorByClass(node, "UITreeExplorer");
  var root =  eXo.core.DOMUtil.findAncestorByClass(node, "UITreeExplorer");
  var link = eXo.core.DOMUtil.findFirstDescendantByClass(component, "a",
      "SelectNode");

  var endParamIndex = link.href.lastIndexOf("')");
  var param = "&objectId";
  var modeIndex = link.href.indexOf(param);
  if (endParamIndex > 0) {
    if (modeIndex < 0)
      link.href = link.href.substring(0, endParamIndex) + param + "="
          + nodePath + "')";
    else
      link.href = link.href.substring(0, modeIndex) + param + "=" + nodePath
          + "')";
  } else {
    if (modeIndex < 0)
      link.href = link.href.substring(0, link.href.length) + param + "="
          + nodePath;
    else
      link.href = link.href.substring(0, modeIndex) + param + "=" + nodePath;
  }
  window.location = link.href;

};

UITreeExplorer.prototype.render = function(param, element, isFullRender) {
  var me = eXo.wiki.UITreeExplorer;
  var node = element.parentNode;
  var component = eXo.core.DOMUtil.findAncestorByClass(node, "UITreeExplorer");
  var url =  eXo.core.DOMUtil.findFirstDescendantByClass(component, "input", "ChildrenURL").value;
  if (isFullRender){
    url =  eXo.core.DOMUtil.findFirstDescendantByClass(component, "input", "InitURL").value;
  }
  var http = eXo.wiki.UITreeExplorer.getHTTPObject();  
  var restURL = url + param;

  http.open("GET", restURL, true); 
  
  var childBlock = document.createElement("div");
  if (me.innerDoc) {
    childBlock = me.innerDoc.createElement("div");
    me.innerDoc = null;
  }
  childBlock.className = "NodeGroup";
  childBlock.innerHTML = me.loading;
  node.appendChild(childBlock);
  
  http.onreadystatechange = function() {
    if (http.readyState == 4) {
      me.renderTreeNodes(childBlock, http.responseText);      
    }
  }
  
  http.send("");
  element.className = "CollapseIcon";
};

UITreeExplorer.prototype.getHTTPObject = function() {
  if (typeof XMLHttpRequest != 'undefined') {
    return new XMLHttpRequest();
  }
  try {
    return new ActiveXObject("Msxml2.XMLHTTP");
  } catch (e) {
    try {
      return new ActiveXObject("Microsoft.XMLHTTP");
    } catch (e) {
    }
  }
  return false;
};

UITreeExplorer.prototype.renderTreeNodes = function(node, responseText) {
  var me = eXo.wiki.UITreeExplorer;
  var dataList = JSON.parse(responseText);
  var resultLength = dataList.jsonList.length;

  var str = "";
  for ( var i = 0; i < resultLength; i++) {
    str += me.buildNode(dataList.jsonList[i]);
  }
  node.innerHTML = str;
}

UITreeExplorer.prototype.buildHierachyNode = function(data){
  var me = eXo.wiki.UITreeExplorer; 
  var children = data.children; 
  var childBlock = "<div class=\"NodeGroup\">";
  for ( var i = 0; i < children.length; i++) {   
    childBlock += me.buildNode(children[i]);
  }
  childBlock += "</div>";
  return childBlock
}


UITreeExplorer.prototype.buildNode = function(data) {
  var me = eXo.wiki.UITreeExplorer;   
  var nodeName = data.name; 
  // Change Type for CSS
  var nodeType = data.nodeType;
  var nodeTypeCSS = nodeType.substring(0, 1).toUpperCase()
      + nodeType.substring(1).toLowerCase();
  var iconType = (data.expanded ==true)? "Collapse":"Expand" ;
  var lastNodeClass = "";
  var hoverClass = "";
  var excerptData = data.excerpt;
  var path = data.path.replaceAll("/", ".");
  var param = "?path=" + path;
  if (excerptData!=null) {
    param += "&excerpt=true";
  }
  if (data.extendParam)
    param += "&current=" + data.extendParam.replaceAll("/",".");
  if (data.lastNode == true) {
    lastNodeClass = "LastNode";
  }
  if (data.hasChild == false) {
    iconType = "Empty";
  }
  if (data.selected == true){
    hoverClass = "Hover";
  }
  var childNode = "";
  childNode += " <div  class=\"" + lastNodeClass + " Node\" >";
  childNode += "   <div class=\""+iconType+"Icon\" id=\"" + path + "\" onclick=\"event.cancelBubble=true;  if(eXo.wiki.UITreeExplorer.collapseExpand(this)) return;  eXo.wiki.UITreeExplorer.render('"+ param + "', this)\">";
  if (me.isRenderLink) {
    childNode += "    <div id=\"iconTreeExplorer\" class=\""+ nodeTypeCSS +" TreeNodeType Node "+ hoverClass +" \">";
  } else {
    childNode += "    <div id=\"iconTreeExplorer\"  onclick=\"event.cancelBubble=true; eXo.wiki.UITreeExplorer.onNodeClick(this,'"+path+"', false " + ")\""  + "class=\""+ nodeTypeCSS +" TreeNodeType Node "+ hoverClass +" \">";
  }
  childNode += "      <div class='NodeLabel'>";
  
  if (data.selectable == true) {
    if (me.isRenderLink) {
      var index = path.lastIndexOf("%2F"); // Find the index of character "/"
      var pageId = path.substring(index + 3);
      var link = me.baseLink + pageId;
      childNode += "        <a title=\"" + nodeName + "\" href=\"" + link + "\">" + nodeName + "</a>";
    } else {
      childNode += "        <a title=\"" + nodeName + "\">" + nodeName + "</a>";
    }
  }
  else{
    childNode += "         <span style=\"cursor:auto\" title=\""+nodeName+"\">"+nodeName+"</span>";
  }
  if (excerptData != null) {
    childNode += excerptData;
  }
  childNode += "      </div>";
  childNode += "    </div>";
  childNode += "  </div>";
  if (data.children.length > 0) {
    childNode += me.buildHierachyNode(data);
  }
  childNode += " </div>"; 
  return childNode;
}


UITreeExplorer.prototype.cleanParam = function(data){
  return data.replace(/&amp;/g, "&");
}

eXo.wiki.UITreeExplorer = new UITreeExplorer();