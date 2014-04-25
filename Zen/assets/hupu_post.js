var hupu_title;
var hupu_date;

var getImageURLs = function(){
    var images = document.getElementsByTagName("img");
    return "" + images.length;
};

var imageURLAtIndex = function(position){
    var images = document.getElementsByTagName("img");
    return "" + images[position].getAttribute('data-url');
};

var addPostInfo = function(title, postInfo, date, uname, position){
    hupu_title = title;
    hupu_date = date;
    var mainPost = "<div class=\"title\"><h1>" + title + "</h1><div><span class=\"date\">" + date + "</span><span class=\"info\" id=\"jdb_postInfo\">" + postInfo + "</div></div><div class=\"about\"><span class=\"name\">" + uname + "</span> <span class=\"time\">" + position + "</span>";
    document.getElementById("hupu_mainPost").innerHTML = mainPost;
};

var addMainPost = function(title, postInfo, date, uname, position, content){
    hupu_title = title;
    hupu_date = date;
    var mainPost = "<div class=\"title\"><h1>" + title + "</h1><div><span class=\"date\">" + date + "</span><span class=\"info\" id=\"jdb_postInfo\">" + postInfo + "</div></div><div class=\"about\"><span class=\"name\">" + uname + "</span> <span class=\"time\">" + position + "</span> </div><div class=\"detail\"><p>" + content + "</p></div>";
    document.getElementById("hupu_mainPost").innerHTML = mainPost;
};

var scrollToTop = function() {
    window.scrollTo(0, 0); 
};

var clearPost = function(postInfo){
    document.getElementById("hupu_mainPost").innerHTML = "<div class=\"title\"><h1>" + hupu_title + "</h1><div><span class=\"date\">" + hupu_date + "</span><span class=\"info\">" + postInfo + "</div></div>";
    document.getElementById("hupu_lightPost").innerHTML = "";
    document.getElementById("hupu_allPost").innerHTML = "<ul id=\"hupu_all\"></ul>";
    scrollToTop();
};

var updatePostInfo = function(postInfo){
    document.getElementById("jdb_postInfo").innerHTML = postInfo;
};

var addLightTitle = function(title, hasLight){
	if(hasLight){
    	document.getElementById("hupu_lightPost").innerHTML = "<h2 class=\"commentH2\">" + title + "</h2><div class=\"hotComment\"><ul id=\"hupu_lights\" class=\"allPostTop\"></ul></div>";
	}
	else{
   	 	document.getElementById("hupu_lightPost").innerHTML = "";
	}
};

var addLightPost = function(uname, position, light, content, index, pid){
    var html = document.getElementById("hupu_lights").innerHTML;
    var l_c = "";
    if(index == 0)
		l_c = "top";
    document.getElementById("hupu_lights").innerHTML = html + "<li ontouchstart=\"touchStart();\" ontouchmove=\"touchMove();\" ontouchend=\"replyOnTouchUp(" + index + ", " + pid + ", 0);\" class=\"" + l_c + "\"><div class=\"about\"><span class=\"name\">" +
    												 uname + "</span> <span class=\"time\"><em id=\"hupu_" + pid + "_0\">亮了(" + light + ")</em></span> </div><div class=\"text\">" + content + "</div></li>";
};

var addAllTitle = function(title, hasReply){
	if(hasReply){
		if(title != ""){
    		document.getElementById("hupu_allPost").innerHTML = "<h2 class=\"commentH2\">" + title + "</h2><ul id=\"hupu_all\" class=\"allPostTop\"></ul>";
    	}
    	else{
    		document.getElementById("hupu_allPost").innerHTML = "<ul id=\"hupu_all\"></ul>";
    	}
	}
	else{
		document.getElementById("hupu_allPost").innerHTML = "";
	}
};

var addAllPost = function(uname, position, light, content, index, pid){
    addReply(uname, position, light, content, index, pid);
};

var loadPublish = function(uname, position, light, content, index, pid){
	addReply(uname, position, light, content, index, pid);
    reply.loadPublishEnd();
};

function addReply(uname, position, light, content, index, pid){
	var cls = "";
    if(index == 0){
    	cls = "style=\"border-top:0px;\"";
    }
	var html = document.getElementById("hupu_all").innerHTML;
    document.getElementById("hupu_all").innerHTML = html + "<li ontouchstart=\"touchStart();\" ontouchmove=\"touchMove();\" " + cls + " ontouchend=\"replyOnTouchUp(" + index + ", " + pid + ", 1);\"><div class=\"about\"> <span class=\"name\">" +
    											 uname + "</span> <span class=\"time\"><em id=\"hupu_" + pid + "_1\">亮了(" + light + ")</em>" + position + "</span> </div><div class=\"text\">" + content + "</div></li>";
};

var isMoving = false;

var imgUri = "";
var videoUri = "";
var uri = "";

var clientX = -1;
var clientY = -1;

function getImgUri(){
    return imgUri;
};

function getVideoUri(){
    return videoUri;
};

function getUri(){
    return uri;
};

document.ontouchstart = function(){
    isMoving = false;    
    if(window.event.srcElement.tagName == "A"){
        uri = window.event.srcElement.href;
    }
};

document.onclick = function(){
    if(window.event.srcElement.tagName == "A" && uri == window.event.srcElement.href){
        event.stopPropagation();
        event.preventDefault();
        window.ZenBridge.OnClick(uri);
    }
}

var imgMove = function(){
	isMoving = true;
};

var imgEnd = function(){
    if(isMoving){
        isMoving = false;
        return;
    }
	event.stopPropagation();
	event.preventDefault();
	
	var doc = window.event.srcElement;
	if(doc.tagName == "IMG"){
        
        var centerNode = doc.parentNode;
        if (centerNode.tagName.toLowerCase() == "a") {
            
            if(centerNode.getAttribute("href") != null){
                videoUri = centerNode.getAttribute("href");
                window.ZenBridge.OnOpenVideo(videoUri);
                return;
            }
        }
        if (centerNode.tagName == "CENTER") {
            var videoNode = centerNode.parentNode;
            if (videoNode.tagName == "A") {
                if(videoNode.getAttribute("href") != null){
                    videoUri = videoNode.getAttribute("href");
                    window.ZenBridge.OnOpenVideo(videoUri);
                    return;
                }
            }
                
        }
        if(doc.getAttribute("data-url") != null){
            imgUri = doc.getAttribute("data-url");
            window.ZenBridge.OnOpenImage(imgUri);
        }
        else if(doc.getAttribute("data-videourl") != null){
            videoUri = doc.getAttribute("data-videourl");
            window.ZenBridge.OnOpenVideo(videoUri);
        }
        
    }
};

var touchStart = function(){
    clientX = event.touches[0].clientX;
    clientY = event.touches[0].pageY - window.scrollY;
	isMoving = false;
    if(window.event.srcElement.tagName == "A"){
        uri = window.event.srcElement.href;
    }
};

var touchMove = function(){
	isMoving = true;
};

var replyOnTouchUp = function(index, pid, area){
    if(isMoving){
        isMoving = false;
        return;
    }
    event.stopPropagation();
	event.preventDefault();
	
    if(window.event.srcElement.tagName == "A" && uri == window.event.srcElement.href){
        return;
    }
    
    if(window.event.srcElement.tagName == "IMG"){
        //show large image
        return;
    }
    window.ZenBridge.OnReplyTouchUp(index, area);
    return;
    if(window.getSelection().rangeCount == 0){
        //window.location = "clientX:" + clientX + ":clientY:" + clientY + ":index:" + index + ":area:" + area;
        window.ZenBridge.OnReplyTouchUp(index, area);
    }
};

var scrollToVisible = function(pid) {
    var obj = document.getElementById("hupu_" + pid + "_1");
    var curleft = curtop = 0;
    if (obj.offsetParent) {
        do {
            curleft += obj.offsetLeft;
            curtop += obj.offsetTop;
        } while (obj = obj.offsetParent);
    }
    var totalH = document.body.scrollHeight;
    var windowH = window.innerHeight;
    var offset = curtop;
    
    if (offset > windowH) {
        if ((offset - windowH/3) < (totalH - windowH)) {
            window.scrollTo(0, offset - windowH/3);
        }
        else {
            window.scrollTo(0, totalH - windowH);
        }
    }
    
};

var getHTML = function() {
    return document.body.innerHTML;
};

//function getRectForSelectedWord() {
//    var selection = window.getSelection();
//    var range = selection.getRangeAt(0);
//    var rect = range.getBoundingClientRect();
//    return "{{" + rect.left + "," + rect.top + "}, {" + rect.width + "," + rect.height + "}}";
//}

var lightSuccess = function(light, pid, area){
    document.getElementById("hupu_" + pid + "_" + area).innerText = "亮了(" + light + ")";
    document.getElementById("hupu_" + pid + "_" + ((area == 0 ) ? 1 : 0)).innerText = "亮了(" + light + ")";
};

var zenAlert = function() {
    alert("hello world!");
};