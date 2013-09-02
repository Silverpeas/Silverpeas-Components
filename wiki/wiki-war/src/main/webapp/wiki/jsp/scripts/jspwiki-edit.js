var EditTools={onPageLoad:function(){
this.textarea=$("editorarea");
if(!this.textarea||!this.textarea.visible){
return;
}
window.onbeforeunload=(function(){
if(this.textarea.value!=this.textarea.defaultValue){
return "edit.areyousure".localize();
}
}).bind(this);
this.wikisnippets=this.getWikiSnippets();
this.wikismartpairs=this.getWikiSmartPairs();
this.onPageLoadPostEditor();
var _1=$("toolbar");
var _2=new Fx.Slide(_1,{onStart:function(){
$("editassist").toggleClass("closed");
}});
var e=$("editassist").addEvent("click",function(e){
e=new Event(e);
_2.toggle();
e.stop();
}).getParent().show();
$("tbREDO").addEvent("click",function(e){
EditTools.redoTextarea();
new Event(e).stop();
});
$("tbUNDO").addEvent("click",function(e){
new Event(e).stop();
EditTools.undoTextarea();
});
$("replace").addEvent("click",function(e){
EditTools.doReplace();
new Event(e).stop();
}).getParent().getParent().show();
_1.getElements("a.tool").each(function(el){
el.addEvent("click",this.insertTextArea.pass(el,this));
},this);
var hh=Wiki.prefs.get("EditorSize");
if(hh){
this.textarea.setStyle("height",hh);
}
var h=new Element("div",{"class":"textarea-resizer","title":"edit.resize".localize()}).injectAfter(this.textarea);
this.textarea.makeResizable({handle:h,modifiers:{x:false,y:"height"},onComplete:function(){
Wiki.prefs.set("EditorSize",this.value.now.y);
}});
},onPageLoadPostEditor:function(){
if(window.ie){
return;
}
this.posteditor=new postEditor.create(this.textarea,"changenote");
this.posteditor.onKeyRight=Class.empty;
this.posteditor.value=function(_b){
EditTools.storeTextarea();
this.element.value=_b.join("");
};
["smartpairs","tabcompletion"].each(function(el){
$(el).setProperty("checked",Wiki.prefs.get(el)||false).addEvent("click",function(e){
Wiki.prefs.set(el,this.checked);
EditTools.initPostEditor();
});
},this);
$("smartpairs").getParent().show();
this.initPostEditor();
},initPostEditor:function(){
if(!this.posteditor){
return;
}
this.posteditor.changeSmartTypingPairs($("smartpairs").checked?this.wikismartpairs:{});
this.posteditor.changeSnippets($("tabcompletion").checked?this.wikisnippets:{});
},getWikiSnippets:function(){
return {"toc":{snippet:["","[{TableOfContents }]","\n"],tab:["[{TableOfContents }]",""]},"link":{snippet:["[","link text|pagename","]"],tab:["link text","pagename",""]},"code":{snippet:["%%prettify \n{{{\n","some code block","\n}}}\n/%\n"],tab:["some code block",""]},"pre":{snippet:["{{{\n","some preformatted block","\n}}}\n"],tab:["some preformatted block",""]},"br":{snippet:["\\\\\n","",""],tab:[""]},"bold":{snippet:["__","some bold text","__"],tab:["some bold text",""]},"italic":{snippet:["''","some italic text","''"],tab:["some italic text",""]},"h1":{snippet:["!!! ","Heading 1 title","\n"],tab:["Heading 1 title",""]},"h2":{snippet:["!! ","Heading 2 title","\n"],tab:["Heading 2 title",""]},"h3":{snippet:["! ","Heading 3 title","\n"],tab:["Heading 3 title",""]},"dl":{snippet:["\n",";term:definition text","\n"],tab:["term","definition text",""]},"mono":{snippet:["{{","some monospaced text","}}"],tab:["some monospaced text",""]},"hr":{snippet:["----\n","",""],tab:[""]},"sub":{snippet:["%%sub ","subscript text","/%"],tab:["subscript text",""]},"sup":{snippet:["%%sup ","superscript text","/%"],tab:["superscript text",""]},"strike":{snippet:["%%strike ","strikethrough text","/%"],tab:["strikethrough text",""]},"tab":{snippet:["%%tabbedSection \n","%%tab-tabTitle1\ntab content 1\n/%\n%%tab-tabTitle2\ntab content 2","\n/%\n/%\n"],tab:["tabTitle1","tab content 1","tabTitle2","tab content 2",""]},"table":{snippet:["\n","||heading 1||heading 2\n| cell 1   | cell 2","\n"],tab:["heading 1","heading 2","cell 1","cell 2",""]},"img":{snippet:["","[{Image src='img.jpg' width='..' height='..' align='left|center|right' style='..' class='..' }]","\n"],tab:["img.jpg",""]},"quote":{snippet:["%%quote \n","quoted text","\n/%\n"],tab:["quoted text",""]},"%%":{snippet:["%%","wikistyle\nsome text","\n/%"],tab:["wikistyle","some text",""]},"sign":{snippet:["\\\\\n--",Wiki.UserName+", "+"25 Sep 07","\n"],tab:[Wiki.UserName,"25 Sep 07",""]},"date":{command:function(k){
var _f=["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],_10=["January","February","March","April","May","June","July","August","September","October","November","December"],dt=new Date(),y=dt.getYear();
if(y<1000){
y+=1900;
}
var _13=_f[dt.getDay()]+", "+_10[dt.getMonth()]+" "+dt.getDate()+", "+y;
return {snippet:["",_13," "],tab:[_13,""]};
}}};
},getWikiSmartPairs:function(){
return {"\"":"\"","(":")","{":"}","[":"]","<":">","'":{scope:{"{{{":"}}}"},pair:"'"}};
},insertTextArea:function(el){
var _15=this.wikisnippets[el.getText()];
if(!_15){
return;
}
var s=TextArea.getSelection(this.textarea),t=_15.snippet.join("");
EditTools.storeTextarea();
if((el.rel=="break")&&(!TextArea.isSelectionAtStartOfLine(this.textarea))){
t="\n"+t;
}
if(s){
t=t.replace(_15.tab[0],s);
}
TextArea.replaceSelection(this.textarea,t);
return false;
},doReplace:function(){
var _18=$("tbFIND").value,_19=$("tbREGEXP").checked,_1a=$("tbGLOBAL").checked?"g":"",_1b=$("tbREPLACE").value,_1c=$("tbMatchCASE").checked?"":"i";
if(_18==""){
return;
}
var sel=TextArea.getSelection(this.textarea);
var _1e=(!sel||(sel==""))?this.textarea.value:sel;
if(!_19){
var re=new RegExp("([.*\\?+[^$])","gi");
_18=_18.replace(re,"\\$1");
}
var re=new RegExp(_18,_1a+_1c+"m");
if(!re.exec(_1e)){
alert("edit.findandreplace.nomatch".localize());
return true;
}
_1e=_1e.replace(re,_1b);
this.storeTextarea();
if(!sel||(sel=="")){
this.textarea.value=_1e;
}else{
TextArea.replaceSelection(this.textarea,_1e);
}
if(this.textarea.onchange){
this.textarea.onchange();
}
},CLIPBOARD:null,clipboard:function(_20){
var s=TextArea.getSelection(this.textarea);
if(!s||s==""){
return;
}
this.CLIPBOARD=s;
$("tbPASTE").className=this.ToolbarMarker;
var ss=_20.replace(/\$/,s);
if(s==ss){
return;
}
this.storeTextarea();
TextArea.replaceSelection(this.textarea,ss);
},paste:function(){
if(!this.CLIPBOARD){
return;
}
this.storeTextarea();
TextArea.replaceSelection(this.textarea,this.CLIPBOARD);
},UNDOstack:[],REDOstack:[],UNDOdepth:20,storeTextarea:function(){
this.UNDOstack.push(this.textarea.value);
$("tbUNDO").disabled="";
this.REDOstack=[];
$("tbREDO").disabled="true";
if(this.UNDOstack.length>this.UNDOdepth){
this.UNDOstack.shift();
}
},undoTextarea:function(){
if(this.UNDOstack.length>0){
$("tbREDO").disabled="";
this.REDOstack.push(this.textarea.value);
this.textarea.value=this.UNDOstack.pop();
}
if(this.UNDOstack.length==0){
$("tbUNDO").disabled="true";
}
if(!this.selector){
return;
}
this.onSelectorLoad();
this.onSelectorChanged();
this.textarea.focus();
},redoTextarea:function(){
if(this.REDOstack.length>0){
$("tbUNDO").disabled="";
this.UNDOstack.push(this.textarea.value);
this.textarea.value=this.REDOstack.pop();
}
if(this.REDOstack.length==0){
$("tbREDO").disabled="true";
}
if(!this.selector){
return;
}
this.onSelectorLoad();
this.onSelectorChanged();
this.textarea.focus();
},getSuggestionMenu:function(){
var mID="findSuggestionMenu",m=$(mID);
if(!m){
m=new Element("div",{"id":mID}).injectTop($("favorites"));
}
return m;
}};
var TextArea={getSelection:function(id){
var f=$(id);
if(!f){
return "";
}
if(window.ie){
return document.selection.createRange().text;
}
return f.getValue().substring(f.selectionStart,f.selectionEnd);
},replaceSelection:function(id,_28){
var f=$(id);
if(!f){
return;
}
var _2a=this.scrollTop;
if(window.ie){
f.focus();
var r=document.selection.createRange();
r.text=_28;
r.moveStart("character",-_28.length);
r.select();
f.range.select();
}else{
var _2c=f.selectionStart,end=f.selectionEnd;
f.value=f.value.substring(0,_2c)+_28+f.value.substring(end);
f.replaceSelectionRange(_2c+_28.length,_2c+_28.length);
}
f.focus();
f.scrollTop=_2a;
if(f.onchange){
f.onchange();
}
},isSelectionAtStartOfLine:function(id){
var f=$(id);
if(!f){
return false;
}
if(window.ie){
f.focus();
var r1=document.selection.createRange(),r2=document.selection.createRange();
r2.moveStart("character",-1);
if(r2.text==""){
r2.moveEnd("character",1);
}
if(r1.compareEndPoints("StartToStart",r2)==0){
return true;
}
if(r2.text.charAt(0).match(/[\n\r]/)){
return true;
}
}else{
if(f.selectionStart==0){
return true;
}
if(f.value.charAt(f.selectionStart-1)=="\n"){
return true;
}
}
return false;
}};
var globalCursorPos;
function setCursorPos(id){
if(window.ie){
return;
}
globalCursorPos=getCursorPos($(id));
}
function getCursorPos(_33){
var _34=_33.value;
if(window.ie){
var _35=document.selection.createRange(),_36=_35.text,_37="#%~";
_35.text=_36+_37;
_35.moveStart("character",(0-_36.length-_37.length));
var _38=_33.value;
_35.text=_36;
for(i=0;i<=_38.length;i++){
var _39=_38.substring(i,i+_37.length);
if(_39==_37){
var _3a=(i-_36.length);
return _3a;
}
}
}else{
if(_33.selectionStart||_33.selectionStart=="0"){
return _33.selectionStart;
}else{
return _34.length;
}
}
}
function insertString(_3b){
var _3c=myForm.myTextArea.value.substring(0,globalCursorPos);
var _3d=myForm.myTextArea.value.substring(globalCursorPos,myForm.myTextArea.value.length);
myForm.myTextArea.value=_3c+_3b+_3d;
}
function getSuggestions(id){
if(window.ie){
return;
}
var _3f=$(id),val=_3f.value,_41;
var pos=getCursorPos(_3f);
for(i=pos-1;i>0;i--){
if(val.charAt(i)=="]"){
break;
}
if(val.charAt(i)=="["&&i<val.length-1){
_41=val.substring(i+1,pos);
break;
}
}
if(_41){
jsonrpc.search.getSuggestions(callback,_41,10);
}else{
EditTools.getSuggestionMenu().hide();
}
}
function callback(_43,_44){
if(_44){
alert(_44.message);
return;
}
var _45=EditTools.getSuggestionMenu(),_46=[];
_43.list.each(function(el){
_46.push("<li>"+el+"</li>");
});
_45.setHTML("<ul>",_46.join(""),"</ul>").show();
}
window.addEvent("load",EditTools.onPageLoad.bind(EditTools));

