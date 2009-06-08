String.extend({deCamelize:function(){
return this.replace(/([a-z])([A-Z])/g,"$1 $2");
}});
function $getText(el){
return el.innerText||el.textContent||"";
}
Element.extend({injectWrapper:function(el){
while(el.firstChild){
this.appendChild(el.firstChild);
}
el.appendChild(this);
return this;
},visible:function(){
var el=this;
while($type(el)=="element"){
if(el.getStyle("visibility")=="hidden"){
return false;
}
if(el.getStyle("display")=="none"){
return false;
}
el=el.getParent();
}
return true;
},hide:function(){
this.style.display="none";
return this;
},show:function(){
this.style.display="";
return this;
},toggle:function(){
this.visible()?this.hide():this.show();
return this;
},scrollTo:function(x,y){
this.scrollLeft=x;
this.scrollTop=y;
},getPosition:function(_6){
_6=_6||[];
var el=this,_8=0,_9=0;
do{
_8+=el.offsetLeft||0;
_9+=el.offsetTop||0;
el=el.offsetParent;
}while(el);
_6.each(function(_a){
_8-=_a.scrollLeft||0;
_9-=_a.scrollTop||0;
});
return {"x":_8,"y":_9};
}});
var Observer=new Class({initialize:function(el,fn,_d){
this.options=Object.extend({event:"keyup",delay:300},_d||{});
this.element=$(el);
this.callback=fn;
this.timeout=null;
this.listener=this.fired.bind(this);
this.value=this.element.getValue();
this.element.setProperty("autocomplete","off").addEvent(this.options.event,this.listener);
},fired:function(){
if(this.value==this.element.value){
return;
}
this.clear();
this.value=this.element.value;
this.timeout=this.callback.delay(this.options.delay,null,[this.element]);
},clear:function(){
this.timeout=$clear(this.timeout);
},stop:function(){
this.element.removeEvent(this.options.event,this.listener);
this.clear();
}});
Element.extend({observe:function(fn,_f){
return new Observer(this,fn,_f);
}});
String.extend({localize:function(){
var s=LocalizedStrings["javascript."+this],_11=arguments;
if(!s){
return ("???"+this+"???");
}
return s.replace(/\{(\d)\}/g,function(m){
return _11[m.charAt(1)]||"???"+m.charAt(1)+"???";
});
}});
Number.REparsefloat=new RegExp("([+-]?\\d+(:?\\.\\d+)?(:?e[-+]?\\d+)?)","i");
function $T(el){
var t=$(el);
return (t&&t.tBodies[0])?$(t.tBodies[0]):t;
}
function getAncestorByTagName(_15,_16){
if(!_15){
return null;
}
if(_15.nodeType==1&&(_15.tagName.toLowerCase()==_16.toLowerCase())){
return _15;
}else{
return getAncestorByTagName(_15.parentNode,_16);
}
}
var Wiki={JSONid:10000,DELIM:"\xa4",init:function(_17){
Object.extend(Wiki,_17||{});
var h=location.host;
this.BasePath=this.BaseUrl.slice(this.BaseUrl.indexOf(h)+h.length,-1);
this.prefs=new Hash.Cookie("JSPWikiUserPrefs",{path:Wiki.BasePath,duration:20});
},getUrl:function(_19){
return this.PageUrl.replace(/%23%24%25/,_19);
},getPageName:function(url){
var s=this.PageUrl.escapeRegExp().replace(/%23%24%25/,"(.+)"),res=url.match(new RegExp(s));
return (res?res[1]:false);
},onPageLoad:function(){
this.PermissionEdit=($E("a.edit")!==undefined);
this.url=null;
this.parseLocationHash.periodical(500);
["editorarea","j_username","loginname","assertedName","query2"].some(function(el){
el=$(el);
if(el&&el.visible()){
el.focus();
return true;
}
return false;
});
if($("morebutton")){
this.replaceMoreBox();
}
},savePrefs:function(){
if($("prefSkin")){
this.prefs.set("SkinName",$("prefSkin").getValue());
}
if($("prefTimeZone")){
this.prefs.set("TimeZone",$("prefTimeZone").getValue());
}
if($("prefTimeFormat")){
this.prefs.set("DateFormat",$("prefTimeFormat").getValue());
}
if($("prefOrientation")){
this.prefs.set("orientation",$("prefOrientation").getValue());
}
if($("editor")){
this.prefs.set("editor",$("editor").getValue());
}
this.prefs.set("FontSize",this.PrefFontSize);
},changeOrientation:function(){
$("wikibody").className=$("prefOrientation").getValue();
},replaceMoreBox:function(){
var _1e=$("morebutton"),_1f=new Element("ul").inject(_1e),_20=_1f.effect("opacity",{wait:false}).set(0),_21=$("actionsMore"),_22="";
$A(_21.options).each(function(o){
if(o.value==""){
return;
}
_22="separator";
new Element("a",{"class":o.className,"href":o.value}).setHTML(o.text).inject(new Element("li").inject(_1f));
});
$("moremenu").inject(new Element("li",{"class":_22}).inject(_1f));
_21.getParent().hide();
_1e.show().addEvent("mouseout",(function(){
_20.start(0);
}).bind(this)).addEvent("mouseover",(function(){
Wiki.locatemenu(_1e,_1f);
_20.start(0.9);
}).bind(this));
},locatemenu:function(_24,el){
var win={"x":window.getWidth(),"y":window.getHeight()},_27={"x":window.getScrollLeft(),"y":window.getScrollTop()},_28=_24.getPosition(),_29={"x":_24.offsetWidth-el.offsetWidth,"y":_24.offsetHeight},_2a={"x":el.offsetWidth,"y":el.offsetHeight},_2b={"x":"left","y":"top"};
for(var z in _2b){
var pos=_28[z]+_29[z];
if((pos+_2a[z]-_27[z])>win[z]){
pos=win[z]-_2a[z]+_27[z];
}
el.setStyle(_2b[z],pos);
}
},parseLocationHash:function(){
if(this.url&&this.url==location.href){
return;
}
this.url=location.href;
var h=location.hash;
if(h==""){
return;
}
h=h.replace(/^#/,"");
var el=$(h);
while($type(el)=="element"){
if(el.hasClass("hidetab")){
TabbedSection.clickTab.apply(el);
}else{
if(el.hasClass("tab")){
}else{
if(el.hasClass("collapsebody")){
}else{
if(!el.visible()){
}
}
}
}
el=el.getParent();
}
location=location.href;
},submitOnce:function(_30){
window.onbeforeunload=null;
(function(){
$A(_30.elements).each(function(e){
if((/submit|button/i).test(e.type)){
e.disabled=true;
}
});
}).delay(10);
return true;
},submitUpload:function(_32,_33){
$("progressbar").setStyle("visibility","visible");
this.progressbar=Wiki.jsonrpc.periodical(1000,this,["progressTracker.getProgress",[_33],function(_34){
if(!_34.code){
$("progressbar").getFirst().setStyle("width",_34+"%").setHTML(_34+"%");
}
}]);
return Wiki.submitOnce(_32);
},JSONid:10000,jsonrpc:function(_35,_36,fn){
new Ajax(Wiki.JsonUrl,{postBody:Json.toString({"id":Wiki.JSONid++,"method":_35,"params":_36}),method:"post",onComplete:function(_38){
var r=Json.evaluate(_38,true);
if(!r){
return;
}
if(r.result){
fn(r.result);
}else{
if(r.error){
fn(r.error);
}
}
}}).request();
}};
var WikiSlimbox={onPageLoad:function(){
var i=0,lnk=new Element("a",{"class":"slimbox"}).setHTML("&raquo;");
$$("*[class^=slimbox]").each(function(_3c){
var _3d="lightbox"+i++,_3e=_3c.className.split("-")[1]||"img ajax",_3f=[];
if(_3e.test("img")){
_3f.extend(["img.inline","a.attachment"]);
}
if(_3e.test("ajax")){
_3f.extend(["a.wikipage","a.external"]);
}
$ES(_3f.join(","),_3c).each(function(el){
var _41=el.src||el.href;
var rel=(el.className.test("inline|attachment"))?"img":"ajax";
if((rel=="img")&&!_41.test("(.bmp|.gif|.png|.jpg|.jpeg)(\\?.*)?$","i")){
return;
}
lnk.clone().setProperties({"href":_41,"rel":_3d+" "+rel,"title":el.alt||el.getText()}).injectBefore(el);
if(el.src){
el.replaceWith(new Element("a",{"class":"attachment","href":el.src}).setHTML(el.alt||el.getText()));
}
});
});
if(i){
Lightbox.init();
}
}};
var Lightbox={init:function(_43){
this.options=$extend({resizeDuration:400,resizeTransition:false,initialWidth:250,initialHeight:250,animateCaption:true,errorMessage:"slimbox.error".localize()},_43||{});
this.anchors=[];
$each(document.links,function(el){
if(el.rel&&el.rel.test(/^lightbox/i)){
el.onclick=this.click.pass(el,this);
this.anchors.push(el);
}
},this);
this.eventKeyDown=this.keyboardListener.bindAsEventListener(this);
this.eventPosition=this.position.bind(this);
this.overlay=new Element("div",{"id":"lbOverlay"}).inject(document.body);
this.center=new Element("div",{"id":"lbCenter","styles":{"width":this.options.initialWidth,"height":this.options.initialHeight,"marginLeft":-(this.options.initialWidth/2),"display":"none"}}).inject(document.body);
new Element("a",{"id":"lbCloseLink","href":"#","title":"slimbox.close.title".localize()}).inject(this.center).onclick=this.overlay.onclick=this.close.bind(this);
this.image=new Element("div",{"id":"lbImage"}).inject(this.center);
this.bottomContainer=new Element("div",{"id":"lbBottomContainer","styles":{"display":"none"}}).inject(document.body);
this.bottom=new Element("div",{"id":"lbBottom"}).inject(this.bottomContainer);
this.caption=new Element("div",{"id":"lbCaption"}).inject(this.bottom);
var _45=new Element("div").inject(this.bottom);
this.prevLink=new Element("a",{"id":"lbPrevLink","href":"#","styles":{"display":"none"}}).setHTML("slimbox.previous".localize()).inject(_45);
this.number=new Element("span",{"id":"lbNumber"}).inject(_45);
this.nextLink=this.prevLink.clone().setProperties({"id":"lbNextLink"}).setHTML("slimbox.next".localize()).inject(_45);
this.prevLink.onclick=this.previous.bind(this);
this.nextLink.onclick=this.next.bind(this);
this.error=new Element("div").setProperty("id","lbError").setHTML(this.options.errorMessage);
new Element("div",{"styles":{"clear":"both"}}).inject(this.bottom);
var _46=this.nextEffect.bind(this);
this.fx={overlay:this.overlay.effect("opacity",{duration:500}).hide(),resize:this.center.effects($extend({duration:this.options.resizeDuration,onComplete:_46},this.options.resizeTransition?{transition:this.options.resizeTransition}:{})),image:this.image.effect("opacity",{duration:500,onComplete:_46}),bottom:this.bottom.effect("margin-top",{duration:400,onComplete:_46})};
this.fxs=new Fx.Elements([this.center,this.image],$extend({duration:this.options.resizeDuration,onComplete:_46},this.options.resizeTransition?{transition:this.options.resizeTransition}:{}));
this.preloadPrev=new Image();
this.preloadNext=new Image();
},click:function(_47){
var rel=_47.rel.split(" ");
if(rel[0].length==8){
return this.open([[url,title,rel[1]]],0);
}
var _49=0,_4a=[];
this.anchors.each(function(el){
var _4c=el.rel.split(" ");
if(_4c[0]!=rel[0]){
return;
}
if((el.href==_47.href)&&(el.title==_47.title)){
_49=_4a.length;
}
_4a.push([el.href,el.title,_4c[1]]);
});
return this.open(_4a,_49);
},open:function(_4d,_4e){
this.images=_4d;
this.position();
this.setup(true);
this.top=window.getScrollTop()+(window.getHeight()/15);
this.center.setStyles({top:this.top,display:""});
this.fx.overlay.start(0.7);
return this.changeImage(_4e);
},position:function(){
this.overlay.setStyles({top:window.getScrollTop(),height:window.getHeight()});
},setup:function(_4f){
var _50=$A(document.getElementsByTagName("object"));
_50.extend(document.getElementsByTagName(window.ie?"select":"embed"));
_50.each(function(el){
if(_4f){
el.lbBackupStyle=el.style.visibility;
}
el.style.visibility=_4f?"hidden":el.lbBackupStyle;
});
var fn=_4f?"addEvent":"removeEvent";
window[fn]("scroll",this.eventPosition)[fn]("resize",this.eventPosition);
document[fn]("keydown",this.eventKeyDown);
this.step=0;
},keyboardListener:function(_53){
switch(_53.keyCode){
case 27:
case 88:
case 67:
this.close();
break;
case 37:
case 38:
case 80:
this.previous();
break;
case 13:
case 32:
case 39:
case 40:
case 78:
this.next();
break;
default:
return;
}
new Event(_53).stop();
},previous:function(){
return this.changeImage(this.activeImage-1);
},next:function(){
return this.changeImage(this.activeImage+1);
},changeImage:function(_54){
if(this.step||(_54<0)||(_54>=this.images.length)){
return false;
}
this.step=1;
this.activeImage=_54;
this.center.style.backgroundColor="";
this.bottomContainer.style.display=this.prevLink.style.display=this.nextLink.style.display="none";
this.fx.image.hide();
this.center.className="lbLoading";
this.preload=new Image();
this.image.empty().setStyle("overflow","hidden");
if(this.images[_54][2]=="img"){
this.preload.onload=this.nextEffect.bind(this);
this.preload.src=this.images[_54][0];
}else{
this.iframeId="lbFrame_"+new Date().getTime();
this.so=new Element("iframe").setProperties({id:this.iframeId,frameBorder:0,scrolling:"auto",src:this.images[_54][0]}).inject(this.image);
this.nextEffect();
}
return false;
},ajaxFailure:function(){
this.ajaxFailed=true;
this.image.setHTML("").adopt(this.error.clone());
this.nextEffect();
},nextEffect:function(){
switch(this.step++){
case 1:
this.center.className="";
this.caption.empty().adopt(new Element("a",{"href":this.images[this.activeImage][0],"title":"slimbox.directLink".localize()}).setHTML(this.images[this.activeImage][1]||""));
var _55=(this.images[this.activeImage][2]=="img")?"slimbox.info":"slimbox.remoteRequest";
this.number.setHTML((this.images.length==1)?"":_55.localize(this.activeImage+1,this.images.length));
this.image.style.backgroundImage="none";
var w=Math.max(this.options.initialWidth,this.preload.width),h=Math.max(this.options.initialHeight,this.preload.height),ww=Window.getWidth()-10,wh=Window.getHeight()-120;
if(this.images[this.activeImage][2]!="img"&&!this.ajaxFailed){
w=6000;
h=3000;
}
if(w>ww){
h=Math.round(h*ww/w);
w=ww;
}
if(h>wh){
w=Math.round(w*wh/h);
h=wh;
}
this.image.style.width=this.bottom.style.width=w+"px";
this.image.style.height=h+"px";
if(this.images[this.activeImage][2]=="img"){
this.image.style.backgroundImage="url("+this.images[this.activeImage][0]+")";
if(this.activeImage){
this.preloadPrev.src=this.images[this.activeImage-1][0];
}
if(this.activeImage!=(this.images.length-1)){
this.preloadNext.src=this.images[this.activeImage+1][0];
}
this.number.setHTML(this.number.innerHTML+"&nbsp;&nbsp;["+this.preload.width+"&#215;"+this.preload.height+"]");
}else{
this.so.style.width=w+"px";
this.so.style.height=h+"px";
}
if(this.options.animateCaption){
this.bottomContainer.setStyles({height:"0px",display:""});
}
this.fxs.start({"0":{height:[this.image.offsetHeight],width:[this.image.offsetWidth],marginLeft:[-this.image.offsetWidth/2]},"1":{opacity:[1]}});
break;
case 2:
this.image.setStyle("overflow","auto");
this.bottomContainer.setStyles({top:(this.top+this.center.clientHeight)+"px",marginLeft:this.center.style.marginLeft});
if(this.options.animateCaption){
this.fx.bottom.set(-this.bottom.offsetHeight);
this.bottomContainer.style.height="";
this.fx.bottom.start(0);
break;
}
this.bottomContainer.style.height="";
case 3:
if(this.activeImage){
this.prevLink.style.display="";
}
if(this.activeImage!=(this.images.length-1)){
this.nextLink.style.display="";
}
this.step=0;
}
},close:function(){
if(this.step<0){
return;
}
this.step=-1;
if(this.preload){
this.preload.onload=Class.empty;
this.preload=null;
}
for(var f in this.fx){
this.fx[f].stop();
}
this.center.style.display=this.bottomContainer.style.display="none";
this.fx.overlay.chain(this.setup.pass(false,this)).start(0);
return false;
}};
var WikiReflection={onPageLoad:function(){
$$("*[class^=reflection]").each(function(w){
var _5c=w.className.split("-");
$ES("img",w).each(function(img){
Reflection.add(img,_5c[1],_5c[2]);
});
});
}};
var Reflection={options:{height:0.33,opacity:0.5},add:function(img,_5f,_60){
_5f=(_5f)?_5f/100:this.options.height;
_60=(_60)?_60/100:this.options.opacity;
var div=new Element("div").injectAfter(img).adopt(img),_62=img.width,_63=img.height,rH=Math.floor(_63*_5f);
div.className=img.className.replace(/\breflection\b/,"");
div.style.cssText=img.backupStyle=img.style.cssText;
div.setStyles({"width":img.width,"height":_63+rH});
img.style.cssText="vertical-align: bottom";
if(window.ie){
new Element("img",{"src":img.src,"styles":{"width":_62,"marginBottom":"-"+(_63-rH)+"px","filter":"flipv progid:DXImageTransform.Microsoft.Alpha(opacity="+(_60*100)+", style=1, finishOpacity=0, startx=0, starty=0, finishx=0, finishy="+(_5f*100)+")"}}).inject(div);
}else{
var r=new Element("canvas",{"width":_62,"height":rH,"styles":{"width":_62,"height":rH}}).inject(div);
if(!r.getContext){
return;
}
var ctx=r.getContext("2d");
ctx.save();
ctx.translate(0,_63-1);
ctx.scale(1,-1);
ctx.drawImage(img,0,0,_62,_63);
ctx.restore();
ctx.globalCompositeOperation="destination-out";
var g=ctx.createLinearGradient(0,0,0,rH);
g.addColorStop(0,"rgba(255, 255, 255, "+(1-_60)+")");
g.addColorStop(1,"rgba(255, 255, 255, 1.0)");
ctx.fillStyle=g;
ctx.rect(0,0,_62,rH);
ctx.fill();
}
}};
var QuickLinks={onPageLoad:function(){
if($("previewcontent")||!Wiki.PrefShowQuickLinks){
return;
}
var _68=$$("#pagecontent *[id^=section]");
if(_68.length==0){
return;
}
var ql,_6a,_6b,_6c;
function qql(_6d,_6e,_6f,_70){
var a=new Element("a",{"title":_6e.localize()}).setHTML(_6f);
if(_70){
a.setProperty("href",_70);
}
ql.adopt(new Element("span",{"class":_6d}).adopt(a));
return (a);
}
ql=new Element("div",{"class":"quicklinks"});
qql("quick2Top","quick.top","&laquo;","#wikibody");
_6a=qql("quick2Prev","quick.previous","&lsaquo;");
if(Wiki.PermissioneEdit){
_6b=qql("quick2Edit","quick.edit","&bull;");
}
_6c=qql("quick2Next","quick.next","&rsaquo;");
qql("quick2Bottom","quick.bottom","&raquo;","#footer");
var _72=_68.length-1;
_68.each(function(s,i){
_6c.setProperty("href",(i==_72)?"#footer":"#"+_68[i+1].id);
_6a.setProperty("href",(i==0)?"#wikibody":"#"+_68[i-1].id);
if(_6b){
_6b.setProperty("href","Edit.jsp?page="+Wiki.PageName+"&section="+(i+1));
}
s.adopt(ql.clone());
});
}};
var TabbedSection={onPageLoad:function(){
$$(".tabmenu a").each(function(el){
if(el.href){
return;
}
var tab=$(el.id.substr(5));
el.addEvent("click",this.clickTab.bind(tab));
},this);
$$(".tabbedSection").each(function(tt){
tt.addClass("tabs");
var _78=new Element("div",{"class":"tabmenu"}).injectBefore(tt);
tt.getChildren().each(function(tab,i){
if(!tab.className.test("^tab-")){
return;
}
if(!tab.id||(tab.id=="")){
tab.id=tab.className;
}
var _7b=tab.className.substr(4).deCamelize();
(i==0)?tab.removeClass("hidetab"):tab.addClass("hidetab");
new Element("div",{"styles":{"clear":"both"}}).inject(tab);
var _7c=new Element("a",{"id":"menu-"+tab.id,"events":{"click":this.clickTab.bind(tab)}}).appendText(_7b).inject(_78);
if(i==0){
_7c.addClass("activetab");
}
},this);
},this);
},clickTab:function(){
var _7d=$("menu-"+this.id);
this.getParent().getChildren().some(function(el){
if(el.id){
var m=$("menu-"+el.id);
if(m&&m.hasClass("activetab")){
if(el.id!=this.id){
m.removeClass("activetab");
_7d.addClass("activetab");
el.addClass("hidetab");
this.removeClass("hidetab");
}
return true;
}
}
return false;
},this);
}};
var WikiAccordion={onPageLoad:function(){
$$(".accordion, .tabbedAccordion").each(function(tt){
var _81=[],_82=[],_83=false;
if(tt.hasClass("tabbedAccordion")){
_83=new Element("div",{"class":"togglemenu"}).injectBefore(tt);
}
tt.getChildren().each(function(tab){
if(!tab.className.test("^tab-")){
return;
}
var _85=tab.className.substr(4).deCamelize();
if(_83){
_81.push(new Element("div",{"class":"toggle"}).inject(_83).appendText(_85));
}else{
_81.push(new Element("div",{"class":"toggle"}).injectBefore(tab).appendText(_85));
}
_82.push(tab.addClass("tab"));
});
new Accordion(_81,_82,{alwaysHide:!_83,onComplete:function(){
var el=$(this.elements[this.previous]);
if(el.offsetHeight>0){
el.setStyle("height","auto");
}
},onActive:function(_87,_88){
_87.addClass("active");
_88.addClass("active");
},onBackground:function(_89,_8a){
_8a.setStyle("height",_8a["offsetHeight"]);
_89.removeClass("active");
_8a.removeClass("active");
}});
});
}};
var SearchBox={onPageLoad:function(){
this.onPageLoadQuickSearch();
this.onPageLoadFullSearch();
},onPageLoadQuickSearch:function(){
var q=$("query");
if(!q){
return;
}
this.query=q;
q.observe(this.ajaxQuickSearch.bind(this));
this.hover=$("searchboxMenu").setProperty("visibility","visible").effect("opacity",{wait:false}).set(0);
$(q.form).addEvent("submit",this.submit.bind(this)).addEvent("mouseout",function(){
this.hover.start(0);
}.bind(this)).addEvent("mouseover",function(){
Wiki.locatemenu(this.query,$("searchboxMenu"));
this.hover.start(0.9);
}.bind(this));
if(window.xwebkit){
q.setProperties({type:"search",autosave:q.form.action,results:"9",placeholder:q.defaultValue});
}else{
$("recentClear").addEvent("click",this.clear.bind(this));
this.recent=Wiki.prefs.get("RecentSearch");
if(!this.recent){
return;
}
var ul=new Element("ul",{"id":"recentItems"}).inject($("recentSearches").show());
this.recent.each(function(el){
new Element("a",{"href":"#","events":{"click":function(){
q.value=el;
q.form.submit();
}}}).setHTML(el).inject(new Element("li").inject(ul));
});
}
},onPageLoadFullSearch:function(){
var q2=$("query2");
if(!q2){
return;
}
this.query2=q2;
var _8f=function(){
var qq=this.query2.value.replace(/^(?:author:|name:|contents:|attachment:)/,"");
this.query2.value=$("scope").getValue()+qq;
this.runfullsearch();
}.bind(this);
q2.observe(this.runfullsearch.bind(this));
$("scope").addEvent("change",_8f);
$("details").addEvent("click",this.runfullsearch.bind(this));
},runfullsearch:function(){
var q2=this.query2.value;
if(!q2||(q2.trim()=="")){
$("searchResult2").empty();
return;
}
$("spin").show();
var _92=$("scope"),_93=q2.match(/^(?:author:|name:|contents:|attachment:)/)||"";
$each(_92.options,function(_94){
if(_94.value==_93){
_94.selected=true;
}
});
new Ajax(Wiki.TemplateDir+"AJAXSearch.jsp",{postBody:$("searchform2").toQueryString(),update:"searchResult2",method:"post",onComplete:function(){
$("spin").hide();
GraphBar.onPageLoad();
Wiki.prefs.set("PrevQuery",q2);
}}).request();
},submit:function(){
var v=this.query.value;
if(v==this.query.defaultValue){
this.query.value="";
}
if(!this.recent){
this.recent=[];
}
if(!this.recent.test(v)){
if(this.recent.length>9){
this.recent.pop();
}
this.recent.unshift(v);
Wiki.prefs.set("RecentSearch",this.recent);
}
},clear:function(){
this.recent=[];
Wiki.prefs.remove("RecentSearch");
$("recentSearches","recentClear").hide();
},ajaxQuickSearch:function(){
var qv=this.query.value;
if((qv==null)||(qv.trim()=="")||(qv==this.query.defaultValue)){
$("searchOutput").empty();
return;
}
$("searchTarget").setHTML("("+qv+") :");
$("searchSpin").show();
Wiki.jsonrpc("search.findPages",[qv,20],function(_97){
$("searchSpin").hide();
if(!_97.list){
return;
}
var _98=new Element("ul");
_97.list.each(function(el){
new Element("li").adopt(new Element("a",{"href":Wiki.getUrl(el.map.page)}).setHTML(el.map.page),new Element("span",{"class":"small"}).setHTML(" ("+el.map.score+")")).inject(_98);
});
$("searchOutput").empty().adopt(_98);
Wiki.locatemenu($("query"),$("searchboxMenu"));
});
},navigate:function(url,_9b,_9c,_9d){
var p=Wiki.PageName,s=this.query.value;
if(s==this.query.defaultValue){
s="";
}
if(s==""){
s=prompt(_9b,(_9c)?p+"sbox.clone.suffix".localize():p);
if(!s||(s=="")){
return false;
}
}
if(_9c&&(s!=p)){
s+="&clone="+p;
}
if(s==""){
return false;
}
location.href=url.replace("__PAGEHERE__",s);
}};
var Color=new Class({_HTMLColors:{black:"000000",green:"008000",silver:"c0c0c0",lime:"00ff00",gray:"808080",olive:"808000",white:"ffffff",yellow:"ffff00",maroon:"800000",navy:"000080",red:"ff0000",blue:"0000ff",purple:"800080",teal:"008080",fuchsia:"ff00ff",aqua:"00ffff"},initialize:function(_a0,_a1){
_a1=_a1||(_a0.push?"rgb":"hex");
if(this._HTMLColors[_a0]){
_a0=this._HTMLColors[_a0];
}
var rgb=(_a1=="rgb")?_a0:_a0.toString().hexToRgb(true);
if(!rgb){
return false;
}
rgb.hex=rgb.rgbToHex();
return $extend(rgb,Color.prototype);
},mix:function(){
var _a3=$A(arguments),rgb=this.copy(),_a5=(($type(_a3[_a3.length-1])=="number")?_a3.pop():50)/100,_a6=1-_a5;
_a3.each(function(_a7){
_a7=new Color(_a7);
for(var i=0;i<3;i++){
rgb[i]=Math.round((rgb[i]*_a6)+(_a7[i]*_a5));
}
});
return new Color(rgb,"rgb");
},invert:function(){
return new Color(this.map(function(_a9){
return 255-_a9;
}));
}});
var GraphBar={onPageLoad:function(){
$$("*[class^=graphBars]").each(function(g){
var _ab=20,_ac=320,_ad=20,_ae=null,_af=null,_b0=false,_b1=false,_b2=true,_b3=g.className.substr(9).split("-"),_b4=_b3.shift();
_b3.each(function(p){
p=p.toLowerCase();
if(p=="vertical"){
_b2=false;
}else{
if(p=="progress"){
_b1=true;
}else{
if(p=="gauge"){
_b0=true;
}else{
if(p.indexOf("min")==0){
_ab=p.substr(3).toInt();
}else{
if(p.indexOf("max")==0){
_ac=p.substr(3).toInt();
}else{
if(p!=""){
p=new Color(p,"hex");
if(!p.hex){
return;
}
if(!_ae){
_ae=p;
}else{
if(!_af){
_af=p;
}
}
}
}
}
}
}
}
});
if(!_af&&_ae){
_af=(_b0||_b1)?_ae.invert():_ae;
}
if(_ab>_ac){
var m=_ac;
_ac=_ab;
_ac=m;
}
var _b7=_ac-_ab;
var _b8=$ES(".gBar"+_b4,g);
if((_b8.length==0)&&_b4&&(_b4!="")){
_b8=this.getTableValues(g,_b4);
}
if(!_b8){
return;
}
var _b9=this.parseBarData(_b8,_ab,_b7),_ba=(_b2?"borderLeft":"borderBottom");
_b8.each(function(b,j){
var _bd=$H().set(_ba+"Width",_b9[j]),_be=$H(),_bf=new Element("span",{"class":"graphBar"}),pb=b.getParent();
if(_b2){
_bf.setHTML("x");
if(_b1){
_be.extend(_bd.obj);
_bd.set(_ba+"Width",_ac-_b9[j]).set("marginLeft","-1ex");
}
}else{
if(pb.getTag()=="td"){
pb=new Element("div").injectWrapper(pb);
}
pb.setStyles({"height":_ac+b.getStyle("lineHeight").toInt(),"position":"relative"});
b.setStyle("position","relative");
if(!_b1){
b.setStyle("top",(_ac-_b9[j]));
}
_bd.extend({"position":"absolute","width":_ad,"bottom":"0"});
if(_b1){
_be.extend(_bd.obj).set(_ba+"Width",_ac);
}
}
if(_b1){
if(_ae){
_bd.set("borderColor",_ae.hex);
}
if(_af){
_be.set("borderColor",_af.hex);
}else{
_bd.set("borderColor","transparent");
}
}else{
if(_ae){
var _c1=_b0?(_b9[j]-_ab)/_b7:j/(_b8.length-1);
_bd.set("borderColor",_ae.mix(_af,100*_c1).hex);
}
}
if(_be.length>0){
_bf.clone().setStyles(_be.obj).injectBefore(b);
}
if(_bd.length>0){
_bf.setStyles(_bd.obj).injectBefore(b);
}
},this);
},this);
},parseBarData:function(_c2,_c3,_c4){
var _c5=[],_c6=Number.MIN_VALUE,_c7=Number.MAX_VALUE,num=date=true;
_c2.each(function(n,i){
var s=n.getText();
_c5.push(s);
if(num){
num=!isNaN(parseFloat(s.match(Number.REparsefloat)));
}
if(date){
date=!isNaN(Date.parse(s));
}
});
_c5=_c5.map(function(b){
if(date){
b=new Date(Date.parse(b)).valueOf();
}else{
if(num){
b=parseFloat(b.match(Number.REparsefloat));
}
}
_c6=Math.max(_c6,b);
_c7=Math.min(_c7,b);
return b;
});
if(_c6==_c7){
_c6=_c7+1;
}
_c4=_c4/(_c6-_c7);
return _c5.map(function(b){
return ((_c4*(b-_c7))+_c3).toInt();
});
},getTableValues:function(_ce,_cf){
var _d0=$E("table",_ce);
if(!_d0){
return false;
}
var _d1=_d0.rows.length;
if(_d1>1){
var r=_d0.rows[0];
for(var h=0;h<r.cells.length;h++){
if($getText(r.cells[h]).trim()==_cf){
var _d4=[];
for(var i=1;i<_d1;i++){
_d4.push(new Element("span").injectWrapper(_d0.rows[i].cells[h]));
}
return _d4;
}
}
}
for(var h=0;h<_d1;h++){
var r=_d0.rows[h];
if($getText(r.cells[0]).trim()==_cf){
var _d4=[];
for(var i=1;i<r.cells.length;i++){
_d4.push(new Element("span").injectWrapper(r.cells[i]));
}
return _d4;
}
}
return false;
}};
var Collapsible={pims:[],onPageLoad:function(){
this.bullet=new Element("div",{"class":"collapseBullet"}).setHTML("&bull;");
this.initialise("favorites","JSPWikiCollapseFavorites");
this.initialise("pagecontent","JSPWikiCollapse"+Wiki.PageName);
this.initialise("previewcontent","JSPWikiCollapse"+Wiki.PageName);
this.initialise("info");
},initialise:function(_d6,_d7){
_d6=$(_d6);
if(!_d6){
return;
}
this.pims.push({"name":_d7,"value":"","initial":(_d7?Cookie.get(_d7):"")});
$ES(".collapse",_d6).each(function(el){
if($E(".collapseBullet",el)){
return;
}
this.collapseNode(el);
},this);
$ES(".collapsebox,.collapsebox-closed",_d6).each(function(el){
this.collapseBox(el);
},this);
},collapseBox:function(el){
var _db=el.getFirst();
if(!_db){
return;
}
var _dc=new Element("div",{"class":"collapsebody"}),_dd=this.bullet.clone(),_de=el.hasClass("collapsebox-closed");
while(_db.nextSibling){
_dc.appendChild(_db.nextSibling);
}
el.appendChild(_dc);
if(_de){
el.removeClass("collapsebox-closed").addClass("collapsebox");
}
_dd.injectTop(_db.addClass("collapsetitle"));
this.newBullet(_dd,_dc,!_de,_db);
},collapseNode:function(_df){
$ES("li",_df).each(function(li){
var _e1=$E("ul",li)||$E("ol",li);
var _e2=true;
for(var n=li.firstChild;n;n=n.nextSibling){
if((n.nodeType==3)&&(n.nodeValue.trim()=="")){
continue;
}
if((n.nodeName=="UL")||(n.nodeName=="OL")){
break;
}
_e2=false;
break;
}
if(_e2){
return;
}
new Element("div",{"class":"collapsebody"}).injectWrapper(li);
var _e4=this.bullet.clone().injectTop(li);
if(_e1){
this.newBullet(_e4,_e1,(_e1.getTag()=="ul"));
}
},this);
},newBullet:function(_e5,_e6,_e7,_e8){
var ck=this.pims.getLast();
_e7=this.parseCookie(_e7);
if(!_e8){
_e8=_e5;
}
var _ea=_e6.setStyle("overflow","hidden").effect("height",{wait:false,onStart:this.renderBullet.bind(_e5),onComplete:function(){
if(_e5.hasClass("collapseOpen")){
_e6.setStyle("height","auto");
}
}});
_e5.className=(_e7?"collapseClose":"collapseOpen");
_e8.addEvent("click",this.clickBullet.bind(_e5,[ck,ck.value.length-1,_ea])).addEvent("mouseenter",function(){
_e8.addClass("collapseHover");
}).addEvent("mouseleave",function(){
_e8.removeClass("collapseHover");
});
_ea.fireEvent("onStart");
if(!_e7){
_ea.set(0);
}
},renderBullet:function(){
if(this.hasClass("collapseClose")){
this.setProperties({"title":"collapse".localize(),"class":"collapseOpen"}).setHTML("-");
}else{
this.setProperties({"title":"expand".localize(),"class":"collapseClose"}).setHTML("+");
}
},clickBullet:function(ck,_ec,_ed){
var _ee=this.hasClass("collapseOpen"),_ef=_ed.element.scrollHeight;
if(_ee){
_ed.start(_ef,0);
}else{
_ed.start(_ef);
}
ck.value=ck.value.substring(0,_ec)+(_ee?"c":"o")+ck.value.substring(_ec+1);
if(ck.name){
Cookie.set(ck.name,ck.value,{path:Wiki.BasePath,duration:20});
}
},parseCookie:function(_f0){
var ck=this.pims.getLast(),_f2=ck.value.length,_f3=(_f0?"o":"c");
if(ck.initial&&(ck.initial.length>_f2)){
var _f4=ck.initial.charAt(_f2);
if((_f0&&(_f4=="c"))||(!_f0&&(_f4=="o"))){
_f3=_f4;
}
if(_f3!=_f4){
ck.initial=null;
}
}
ck.value+=_f3;
return (_f3=="o");
}};
var RoundedCorners={NormalTop:[{margin:"5px",height:"1px",borderSide:"0",borderTop:"1px"},{margin:"3px",height:"1px",borderSide:"2px"},{margin:"2px",height:"1px",borderSide:"1px"},{margin:"1px",height:"2px",borderSide:"1px"}],SmallTop:[{margin:"2px",height:"1px",borderSide:"0",borderTop:"1px"},{margin:"1px",height:"1px",borderSide:"1px"}],registry:{},register:function(_f5,_f6){
this.registry[_f5]=_f6;
return this;
},onPageLoad:function(){
this.NormalBottom=this.NormalTop.slice(0).reverse();
this.SmallBottom=this.SmallTop.slice(0).reverse();
for(selector in this.registry){
var n=$$(selector);
var _f8=this.registry[selector];
this.exec(n,_f8[0],_f8[1],_f8[2],_f8[3]);
}
$$("#pagecontent *[class^=roundedCorners]").each(function(el){
var _fa=el.className.split("-");
if(_fa.length<2){
return;
}
this.exec([el],_fa[1],_fa[2],_fa[3],_fa[4]);
},this);
},exec:function(_fb,_fc,_fd,_fe,_ff){
_fc=(_fc?_fc+"nnnn":"yyyy");
_fd=new Color(_fd,"hex")||"transparent";
if(_fe){
_fe=new Color(_fe);
}
if(_ff){
_ff=new Color(_ff);
}
var c=_fc.split("");
var _101=null;
var _102=null;
if(c[0]+c[1]!="nn"){
_101=document.createElement("b");
_101.className="roundedCorners";
if((c[0]=="y")||(c[1]=="y")){
this.addCorner(_101,this.NormalTop,c[0],c[1],_fd,_fe);
}else{
if((c[0]=="s")||(c[1]=="s")){
this.addCorner(_101,this.SmallTop,c[0],c[1],_fd,_fe);
}
}
}
if(c[2]+c[3]!="nn"){
_102=document.createElement("b");
_102.className="roundedCorners";
if((c[2]=="y")||(c[3]=="y")){
this.addCorner(_102,this.NormalBottom,c[2],c[3],_fd,_fe);
}else{
if((c[2]=="s")||(c[3]=="s")){
this.addCorner(_102,this.SmallBottom,c[2],c[3],_fd,_fe);
}
}
}
if((!_101)&&(!_fe)&&(!_102)){
return;
}
for(var i=0;i<_fb.length;i++){
if(!_fb[i]){
continue;
}
this.addBody(_fb[i],_fd,_fe);
if(_101){
_fb[i].insertBefore(_101.cloneNode(true),_fb[i].firstChild);
}
if(_102){
_fb[i].appendChild(_102.cloneNode(true));
}
}
},addCorner:function(node,arr,left,_107,_108,_109){
for(var i=0;i<arr.length;i++){
var n=document.createElement("div");
n.style.height=arr[i].height;
n.style.overflow="hidden";
n.style.borderWidth="0";
n.style.backgroundColor=_108.hex;
if(_109){
n.style.borderColor=_109.hex;
n.style.borderStyle="solid";
if(arr[i].borderTop){
n.style.borderTopWidth=arr[i].borderTop;
n.style.height="0";
}
}
if(left!="n"){
n.style.marginLeft=arr[i].margin;
}
if(_107!="n"){
n.style.marginRight=arr[i].margin;
}
if(_109){
n.style.borderLeftWidth=(left=="n")?"1px":arr[i].borderSide;
n.style.borderRightWidth=(_107=="n")?"1px":arr[i].borderSide;
}
node.appendChild(n);
}
},addBody:function(node,_10d,_10e){
if(node.passed){
return;
}
var _10f=new Element("div").injectWrapper(node);
_10f.style.padding="0 4px";
_10f.style.backgroundColor=_10d.hex;
if(_10e){
_10f.style.borderLeft="1px solid "+_10e.hex;
_10f.style.borderRight="1px solid "+_10e.hex;
}
node.passed=true;
}};
var Sortable={onPageLoad:function(){
this.DefaultTitle="sort.click".localize();
this.AscendingTitle="sort.ascending".localize();
this.DescendingTitle="sort.descending".localize();
$$(".sortable table").each(function(_110){
if(_110.rows.length<2){
return;
}
$A(_110.rows[0].cells).each(function(th){
th=$(th);
if(th.getTag()!="th"){
return;
}
th.addEvent("click",function(){
Sortable.sort(th);
}).addClass("sort").title=Sortable.DefaultTitle;
});
},this);
},sort:function(th){
var _113=getAncestorByTagName(th,"table"),_114=(_113.filterStack),rows=(_113.sortCache||[]),_116=0,body=$T(_113);
th=$(th);
$A(body.rows[0].cells).each(function(thi,i){
if(thi.getTag()!="th"){
return;
}
if(th==thi){
_116=i;
return;
}
thi.removeClass("sortAscending").removeClass("sortDescending").addClass("sort").title=Sortable.DefaultTitle;
});
if(rows.length==0){
$A(body.rows).each(function(r,i){
if((i==0)||((i==1)&&(_114))){
return;
}
rows.push(r);
});
}
var _11c=Sortable.guessDataType(rows,_116);
if(th.hasClass("sort")){
rows.sort(Sortable.createCompare(_116,_11c));
}else{
rows.reverse();
}
var fl=th.hasClass("sortDescending");
th.removeClass("sort").removeClass("sortAscending").removeClass("sortDescending");
th.addClass(fl?"sortAscending":"sortDescending").title=fl?Sortable.DescendingTitle:Sortable.AscendingTitle;
var frag=document.createDocumentFragment();
rows.each(function(r,i){
frag.appendChild(r);
});
body.appendChild(frag);
_113.sortCache=rows;
if(_113.zebra){
_113.zebra();
}
},guessDataType:function(rows,_122){
var num=date=ip4=euro=true;
rows.each(function(r,i){
var v=$getText(r.cells[_122]).clean().toLowerCase();
if(num){
num=!isNaN(parseFloat(v));
}
if(date){
date=!isNaN(Date.parse(v));
}
if(ip4){
ip4=v.test("(?:\\d{1,3}\\.){3}\\d{1,3}");
}
if(euro){
euro=v.test("^[\xac\xa3$\u201a\xc7\xa8][0-9.,]+");
}
});
return (euro)?"euro":(ip4)?"ip4":(date)?"date":(num)?"num":"string";
},convert:function(val,_128){
switch(_128){
case "num":
return parseFloat(val.match(Number.REparsefloat));
case "euro":
return parseFloat(val.replace(/[^0-9.,]/g,""));
case "date":
return new Date(Date.parse(val));
case "ip4":
var _129=val.split(".");
return parseInt(_129[0])*1000000000+parseInt(_129[1])*1000000+parseInt(_129[2])*1000+parseInt(_129[3]);
default:
return val.toString().toLowerCase();
}
},createCompare:function(i,_12b){
return function(row1,row2){
var val1=Sortable.convert($getText(row1.cells[i]),_12b);
var val2=Sortable.convert($getText(row2.cells[i]),_12b);
if(val1<val2){
return -1;
}else{
if(val1>val2){
return 1;
}else{
return 0;
}
}
};
}};
var TableFilter={onPageLoad:function(){
this.All="filter.all".localize();
this.FilterRow=1;
$$(".table-filter table").each(function(_130){
if(_130.rows.length<2){
return;
}
var r=$(_130.insertRow(TableFilter.FilterRow)).addClass("filterrow");
for(var j=0;j<_130.rows[0].cells.length;j++){
var s=new Element("select",{"events":{"change":TableFilter.filter}});
s.fcol=j;
new Element("th").adopt(s).inject(r);
}
_130.filterStack=[];
TableFilter.buildEmptyFilters(_130);
});
},buildEmptyFilters:function(_134){
for(var i=0;i<_134.rows[0].cells.length;i++){
var ff=_134.filterStack.some(function(f){
return f.fcol==i;
});
if(!ff){
TableFilter.buildFilter(_134,i);
}
}
if(_134.zebra){
_134.zebra();
}
},buildFilter:function(_138,col,_13a){
var _13b=_138.rows[TableFilter.FilterRow].cells[col].firstChild;
if(!_13b){
return;
}
_13b.options.length=0;
var rows=[];
$A(_138.rows).each(function(r,i){
if((i==0)||(i==TableFilter.FilterRow)){
return;
}
if(r.style.display=="none"){
return;
}
rows.push(r);
});
rows.sort(Sortable.createCompare(col,Sortable.guessDataType(rows,col)));
_13b.options[0]=new Option(this.All,this.All);
var _13f;
rows.each(function(r,i){
var v=$getText(r.cells[col]).clean().toLowerCase();
if(v==_13f){
return;
}
_13f=v;
if(v.length>32){
v=v.substr(0,32)+"...";
}
_13b.options[_13b.options.length]=new Option(v,_13f);
});
(_13b.options.length<=2)?_13b.hide():_13b.show();
if(_13a!=undefined){
_13b.value=_13a;
}else{
_13b.options[0].selected=true;
}
},filter:function(){
var col=this.fcol,_144=this.value,_145=getAncestorByTagName(this,"table");
if(!_145||_145.style.display=="none"){
return;
}
if(_145.filterStack.every(function(f,i){
if(f.fcol!=col){
return true;
}
if(_144==TableFilter.All){
_145.filterStack.splice(i,1);
}else{
f.fValue=_144;
}
return false;
})){
_145.filterStack.push({fValue:_144,fcol:col});
}
$A(_145.rows).each(function(r,i){
r.style.display="";
});
_145.filterStack.each(function(f){
var v=f.fValue,c=f.fcol;
TableFilter.buildFilter(_145,c,v);
var j=0;
$A(_145.rows).each(function(r,i){
if((i==0)||(i==TableFilter.FilterRow)){
return;
}
if(v!=$getText(r.cells[c]).clean().toLowerCase()){
r.style.display="none";
}
});
});
TableFilter.buildEmptyFilters(_145);
}};
var Categories={onPageLoad:function(){
this.jsp=Wiki.TemplateDir+"/AJAXCategories.jsp";
$$(".category a.wikipage").each(function(link){
var page=Wiki.getPageName(link.href);
if(!page){
return;
}
var wrap=new Element("span").injectBefore(link).adopt(link),_153=new Element("div",{"class":"categoryPopup"}).inject(wrap),_154=_153.effect("opacity",{wait:false}).set(0);
link.addClass("categoryLink").setProperties({href:"#",title:"category.title".localize(page)}).addEvent("click",function(e){
new Event(e).stop();
new Ajax(Categories.jsp,{postBody:"&page="+page,update:_153,onComplete:function(){
link.setProperty("title","").removeEvent("click");
wrap.addEvent("mouseover",function(e){
_154.start(0.9);
}).addEvent("mouseout",function(e){
_154.start(0);
});
_153.setStyle("left",link.getPosition().x);
_154.start(0.9);
}}).request();
});
});
}};
var WikiTips={onPageLoad:function(){
var tips=[];
$$("*[class^=tip]").each(function(t){
var _15a=t.className.split("-");
if(_15a.length<=0||_15a[0]!="tip"){
return;
}
t.className="tip";
var body=new Element("span").injectWrapper(t).hide(),_15c=(_15a[1])?_15a[1].deCamelize():"tip.default.title".localize();
tips.push(new Element("span",{"class":"tip-anchor","title":_15c+"::"+body.innerHTML}).setHTML(_15c).inject(t));
});
if(tips.length>0){
new Tips(tips,{"className":"tip"});
}
}};
var WikiColumns={onPageLoad:function(){
var tips=[];
$$("*[class^=columns]").each(function(t){
var _15f=t.className.split("-");
t.className="columns";
WikiColumns.buildColumns(t,_15f[1]||"auto");
});
},buildColumns:function(el,_161){
var _162=$ES("hr",el);
if(!_162||_162.length==0){
return;
}
var _163=_162.length+1;
_161=(_161=="auto")?98/_163+"%":_161/_163+"px";
var _164=new Element("div",{"class":"col","styles":{"width":_161}}),col=_164.clone().injectBefore(el.getFirst()),n;
while(n=col.nextSibling){
if(n.tagName&&n.tagName.toLowerCase()=="hr"){
col=_164.clone();
$(n).replaceWith(col);
continue;
}
col.appendChild(n);
}
new Element("div",{"styles":{"clear":"both"}}).inject(el);
}};
var ZebraTable={onPageLoad:function(){
$$("*[class^=zebra]").each(function(z){
var _168=z.className.split("-"),_169=_168[1].test("table"),c1="",c2="";
if(_168[1]){
c1=new Color(_168[1],"hex");
}
if(_168[2]){
c2=new Color(_168[2],"hex");
}
$ES("table",z).each(function(t){
t.zebra=this.zebrafy.pass([_169,c1,c2],t);
t.zebra();
},this);
},this);
},zebrafy:function(_16d,c1,c2){
var j=0;
$A($T(this).rows).each(function(r,i){
if(i==0||(r.style.display=="none")){
return;
}
if(_16d){
(j++%2==0)?$(r).addClass("odd"):$(r).removeClass("odd");
}else{
$(r).setStyle("background-color",(j++%2==0)?c1:c2);
}
});
}};
var HighlightWord={ReQuery:new RegExp("(?:\\?|&)(?:q|query)=([^&]*)","g"),onPageLoad:function(){
var q=Wiki.prefs.get("PrevQuery");
Wiki.prefs.set("PrevQuery","");
if(!q&&this.ReQuery.test(document.referrer)){
q=RegExp.$1;
}
if(!q){
return;
}
var _174=decodeURIComponent(q);
_174=_174.replace(/\+/g," ");
_174=_174.replace(/\s+-\S+/g,"");
_174=_174.replace(/([\(\[\{\\\^\$\|\)\?\*\.\+])/g,"\\$1");
_174=_174.trim().split(/\s+/).join("|");
this.reMatch=new RegExp("("+_174+")","gi");
this.walkDomTree($("pagecontent"));
},walkDomTree:function(node){
if(!node){
return;
}
var nn=null;
for(var n=node.firstChild;n;n=nn){
nn=n.nextSibling;
this.walkDomTree(n);
}
if(node.nodeType!=3){
return;
}
if(node.parentNode.className=="searchword"){
return;
}
var s=node.innerText||node.textContent||"";
if(!this.reMatch.test(s)){
return;
}
var tmp=new Element("span").setHTML(s.replace(this.reMatch,"<span class='searchword'>$1</span>"));
var f=document.createDocumentFragment();
while(tmp.firstChild){
f.appendChild(tmp.firstChild);
}
node.parentNode.replaceChild(f,node);
}};
var WikiPrettify={onPageLoad:function(){
var els=$$(".prettify pre, .prettify code");
if(!els){
return;
}
els.addClass("prettyprint");
prettyPrint();
}};
window.addEvent("load",function(){
Wiki.onPageLoad();
WikiReflection.onPageLoad();
WikiAccordion.onPageLoad();
TabbedSection.onPageLoad();
QuickLinks.onPageLoad();
Collapsible.onPageLoad();
SearchBox.onPageLoad();
Sortable.onPageLoad();
TableFilter.onPageLoad();
RoundedCorners.onPageLoad();
ZebraTable.onPageLoad();
HighlightWord.onPageLoad();
GraphBar.onPageLoad();
Categories.onPageLoad();
WikiSlimbox.onPageLoad();
WikiTips.onPageLoad();
WikiColumns.onPageLoad();
WikiPrettify.onPageLoad();
});

