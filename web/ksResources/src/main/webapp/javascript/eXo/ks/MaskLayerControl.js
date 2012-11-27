
function MaskLayerControl() {
  this.domUtil = eXo.core.DOMUtil ;
}

MaskLayerControl.prototype.init = function(root){
  root = (typeof(root) == 'string') ? document.getElementById(root) : root ;
  var nodeList = this.domUtil.findDescendantsByClass(root, 'span', 'ViewDownloadIcon') ;
  for (var i=0; i<nodeList.length; i++) {
    var linkNode = nodeList[i].getElementsByTagName('a')[0] ;
    linkNode.onclick = this.showPictureWrapper ;
  }
} ;

MaskLayerControl.prototype.showPictureWrapper = function() {
  eXo.ks.MaskLayerControl.showPicture(this) ;
  return false ;
} ;

/**
 * 
 * @param {Element} node
 */
MaskLayerControl.prototype.showPicture = function(node) {
	if(typeof(node) == "string"){
		var imgSrcNode = new Image();
		imgSrcNode.src = node;
	}else{
	  var attachmentContent = this.domUtil.findAncestorByClass(node, 'AttachmentContent') ;
	  var imgSrcNode = this.domUtil.findDescendantsByClass(attachmentContent, 'img', 'AttachmentFile')[0] ;		
	}
	if(!document.getElementById("UIPictutreContainer")){		
	  var containerNode = document.createElement('div') ;
		containerNode.id = "UIPictutreContainer";
	  with (containerNode.style) {
			position = "absolute";
			top = "0px";
	    width = '100%' ;
	    height = '100%' ;
	    textAlign = 'center' ;
	  }
	  containerNode.setAttribute('title', 'Click to close') ;
	  containerNode.onclick = this.hidePicture ;
		document.getElementById("UIPortalApplication").appendChild(containerNode)
	}else containerNode = document.getElementById("UIPictutreContainer");
	containerNode.appendChild(imgSrcNode);
	imgSrcNode.onload = function() {
		var imgSize = {'height':this.height, 'width' : this.width};
		var windowHeight = document.documentElement.clientHeight;
		var windowWidth = document.documentElement.clientWidth;
		
		var marginTop = (windowHeight < parseInt(imgSize.height))?0:parseInt((windowHeight - parseInt(imgSize.height))/2);
		
		var imgHeight = (windowHeight < parseInt(imgSize.height))?windowHeight + "px":"auto";
		var imgWidth = (windowWidth < parseInt(imgSize.width))?windowWidth + "px":"auto";
		var imageNode = "<img id='ShowImage' src='" + imgSrcNode.src +"' style='height:" + imgHeight + ";width:"+ imgWidth +";margin-top:0px;' alt='Click to close'/>";
    containerNode.innerHTML = imageNode;
    var interV = setInterval(function() {
      var showImage = document.getElementById('ShowImage');
      var mrTop = parseInt(showImage.style.marginTop);
      if (mrTop < marginTop) {
        mrTop += 5;
        showImage.style.marginTop = mrTop + 'px';
      } else {
        clearInterval(interV);
      }
    }, 10);
	}
	var maskNode = eXo.core.UIMaskLayer.createMask('UIPortalApplication', containerNode, 30, 'CENTER') ;
	this.scrollHandler();	
} ;

MaskLayerControl.prototype.scrollHandler = function() {	
  eXo.core.UIMaskLayer.object.style.top = document.getElementById("MaskLayer").offsetTop + "px" ;
	eXo.ks.MaskLayerControl.timer = setTimeout(eXo.ks.MaskLayerControl.scrollHandler,1);
} ;

MaskLayerControl.prototype.hidePicture = function() {
  eXo.core.Browser.onScrollCallback.remove('MaskLayerControl') ;
  var maskContent = eXo.core.UIMaskLayer.object ;
  var maskNode = document.getElementById("MaskLayer") || document.getElementById("subMaskLayer") ;
  if (maskContent) maskContent.parentNode.removeChild(maskContent) ;
  if (maskNode) maskNode.parentNode.removeChild(maskNode) ;
	clearTimeout(eXo.ks.MaskLayerControl.timer);
	delete eXo.ks.MaskLayerControl.timer;
} ;

MaskLayerControl.prototype.getImageSize = function(img) {
	var imgNode = new Image();
	imgNode.src = img.src;
	return {"height":imgNode.height,"width":imgNode.width};
};

MaskLayerControl.prototype.loadImageAgain = function(componentId) {
  var cont = document.getElementById(componentId);  
  var imgs = cont.getElementsByTagName('img');
  for(var i = 0; i < imgs.length; ++i) {
    imgs[i].src = imgs[i].src;
    imgs[i].style.opacity = 0;
    imgs[i].style.filter = 'alpha(opacity=0)';
    imgs[i].setAttribute('op', 0);
    imgs[i].onload = eXo.ks.MaskLayerControl.imageEffect;
  }
}

MaskLayerControl.prototype.imageEffect = function() {
  var img = this;
  var interV = setInterval(function() {
    var op = parseInt(img.getAttribute('op'));
    if(op < 100) {
      op += 5;
      img.style.opacity = (op / 100);
      img.style.filter = 'alpha(opacity=' + op + ')';
      img.setAttribute('op', op);
    } else {
      clearInterval(interV);
    }
  }, 50);
};

if (!eXo.ks) eXo.ks = {} ;
eXo.ks.MaskLayerControl = new MaskLayerControl() ;