
<!--
/* Permet d'imprimer la page 
en cours de consultation */
function Print_page() {
     window.print();
}

/* Permet d'afficher une page 
dans une fenetre de type PopUp */
function MM_openBrWindow(theURL,winName,features) { 
     window.open(theURL,winName,features);
} 

//date courante au format jj/mm/aa

function aujourdhui(){
	var date = new Date();
	var day = date.getDate();
	var month = date.getMonth()+1;
	var day_ok = (day <= 9) ? '0'+day : day;
	var month_ok = (month <= 9) ? '0'+month : month; 
	Year = date.getFullYear(); 
	return (day_ok+'/'+month_ok+'/'+Year) ;
}

function MM_swapImgRestore() { //v3.0
  var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}

function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function MM_findObj(n, d) { //v3.0
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document); return x;
}

function MM_swapImage() { //v3.0
  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}

function MM_showHideLayers() { //v3.0
  var i,p,v,obj,args=MM_showHideLayers.arguments;
  for (i=0; i<(args.length-2); i+=3) if ((obj=MM_findObj(args[i]))!=null) { v=args[i+2];
    if (obj.style) { obj=obj.style; v=(v=='show')?'visible':(v='hide')?'hidden':v; }
    obj.visibility=v; }
}

/* Permet d'afficher des informations 
sous forme de bulles.
Attention ne fonctionne pas avec Netscape 6 */

var IB=new Object;
var nsx=0;
nsy=0;

function ns(e) {
	nsx=e.x;
    nsy=e.y;
}

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

function InitBulle(ColTexte,ColFond,ColContour,NbPixel,TbTaille) {
	IB.ColTexte=ColTexte;
    IB.ColFond=ColFond;
    IB.ColContour=ColContour;
    IB.NbPixel=NbPixel;
    IB.TbTaille=TbTaille;

	if (document.layers) {
		window.captureEvents(Event.MOUSEMOVE);
        window.onMouseMove=ns;
		document.write("<LAYER name='bulle' top='0' left='0' visibility='hide' index='1'></LAYER>");
	}
	if (document.all) {
		document.write("<DIV id='bulle' style='position:absolute; top:0; left:0; visibility:hidden; z-index=1'></DIV>");
	}
}

function AffBulle(texte) {
contenu="<TABLE border=0 cellspacing=0 cellpadding="+IB.NbPixel+" bgcolor="+IB.ColFond+" width="+IB.TbTaille+"><TR><TD valign='top'><TABLE border=0 cellpadding=2 cellspacing=0 class="+IB.ColContour+" width='100%'><TR><TD><span class="+IB.ColTexte+">"+texte+"</span></TD></TR></TABLE></TD></TR></TABLE>";

	if (document.layers) {
		document.layers["bulle"].document.write(contenu);
		document.layers["bulle"].document.close();
		document.layers["bulle"].top=nsy+20;
		document.layers["bulle"].left=nsx-100;
		document.layers["bulle"].visibility="show";
		document.layers["bulle"].index=1;
}
	if (document.all) {
		var f=window.event;
		bulle.innerHTML=contenu;
		document.all["bulle"].style.top=f.clientY+20;
		document.all["bulle"].style.left=f.clientX-100;
		document.all["bulle"].style.visibility="visible";
		document.all["bulle"].style.index=1;
	}
}

function HideBulle() {
	if (document.layers) {document.layers["bulle"].visibility="hide";}
	if (document.all) {document.all["bulle"].style.visibility="hidden";}
}


/* Permet d'afficher des informations 
sous forme d'une fenetre DHTML de Scrolling */
function verifyCompatibleBrowser(){ 
    this.ver=navigator.appVersion 
    this.dom=document.getElementById?1:0 
    this.ie5=(this.ver.indexOf("MSIE 5")>-1 && this.dom)?1:0; 
    this.ie4=(document.all && !this.dom)?1:0; 
    this.ns5=(this.dom && parseInt(this.ver) >= 5) ?1:0; 
 
    this.ns4=(document.layers && !this.dom)?1:0; 
    this.bw=(this.ie5 || this.ie4 || this.ns4 || this.ns5) 
    return this 
} 
bw=new verifyCompatibleBrowser() 
 
 
var speed=50 
 
var loop, timer 
 
function ConstructObject(obj,nest){ 
    nest=(!nest) ? '':'document.'+nest+'.' 
    this.el=bw.dom?document.getElementById(obj):bw.ie4?document.all[obj]:bw.ns4?eval(nest+'document.'+obj):0; 
    this.css=bw.dom?document.getElementById(obj).style:bw.ie4?document.all[obj].style:bw.ns4?eval(nest+'document.'+obj):0; 
    this.scrollHeight=bw.ns4?this.css.document.height:this.el.offsetHeight 
    this.clipHeight=bw.ns4?this.css.clip.height:this.el.offsetHeight 
    this.up=MoveAreaUp;this.down=MoveAreaDown; 
    this.MoveArea=MoveArea; this.x; this.y; 
    this.obj = obj + "Object" 
    eval(this.obj + "=this") 
    return this 
} 
function MoveArea(x,y){ 
    this.x=x;this.y=y 
    this.css.left=this.x 
    this.css.top=this.y 
} 
 
function MoveAreaDown(move){ 
        if(this.y>-this.scrollHeight+objContainer.clipHeight){ 
    this.MoveArea(0,this.y-move) 
    if(loop) setTimeout(this.obj+".down("+move+")",speed) 
        } 
} 
function MoveAreaUp(move){ 
        if(this.y<0){ 
    this.MoveArea(0,this.y-move) 
    if(loop) setTimeout(this.obj+".up("+move+")",speed) 
        } 
} 
 
function PerformScroll(speed){ 
        if(initialised){ 
                loop=true; 
                if(speed>0) objScroller.down(speed) 
                else objScroller.up(speed) 
        } 
} 
 
function CeaseScroll(){ 
    loop=false 
    if(timer) clearTimeout(timer) 
} 
var initialised; 
function InitialiseScrollableArea(){ 
    objContainer=new ConstructObject('divContainer') 
    objScroller=new ConstructObject('divContent','divContainer') 
    objScroller.MoveArea(0,0) 
    objContainer.css.visibility='visible' 
    initialised=true; 
} 

//-->
