<%@ include file="check.jsp" %>

<% 
	// récupération des paramètres :
	List 		photos	= (List) request.getAttribute("Photos");
	Integer		rang	= (Integer) request.getAttribute("Rang");
	Collection 	path 	= (Collection) request.getAttribute("Path");
	Integer		wait	= (Integer) request.getAttribute("Wait");

	// déclaration des variables :
	String 		nomRep ; 		
	String 		name ;
	String 		namePreview ;
	String 		preview_url ="";
	String 		photoId = "";
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript" type="text/JavaScript">
	// (C) 2000 www.CodeLifter.com
	// http://www.codelifter.com
	// Free for all users, but leave in this  header
	// NS4-6,IE4-6
	// Fade effect only in IE; degrades gracefully
	
	// $Id$
	
	// Set slideShowSpeed (milliseconds)
	var slideShowSpeed = <%=wait.intValue()*1000%>
	// Agent sniffer shamelessly 'stolen' from the excellent X library from cross-browser.com
	var xOp7=false,xOp5or6=false,xIE4Up=false,xNN4=false,xUA=navigator.userAgent.toLowerCase();
	if(window.opera){
	  xOp7=(xUA.indexOf('opera 7')!=-1 || xUA.indexOf('opera/7')!=-1);
	  if (!xOp7) xOp5or6=(xUA.indexOf('opera 5')!=-1 || xUA.indexOf('opera/5')!=-1 || xUA.indexOf('opera 6')!=-1 || xUA.indexOf('opera/6')!=-1);
	}
	else if(document.layers) xNN4=true;
	else {xIE4Up=document.all && xUA.indexOf('msie')!=-1 && parseInt(navigator.appVersion)>=4;}
	
	// Duration of crossfade (seconds)
	var crossFadeDuration = 3
	
	// Specify the image files
	var Pic = new Array() 	// don't touch this
	// to add more images, just continue
	// the pattern, adding to the array below
	
	<%
		// recherche de l'url des photos
		int p = 0;
		Iterator it = (Iterator) photos.iterator();
		while (it.hasNext()) 
		{
			PhotoDetail unePhoto = (PhotoDetail) it.next();
			if (unePhoto != null)
			{
				nomRep 		= resource.getSetting("imagesSubDirectory") + unePhoto.getPhotoPK().getId();
				name 		= unePhoto.getImageName();
				//namePreview	= name.substring(0,name.indexOf(".")) + "_preview.jpg";
				namePreview	= unePhoto.getPhotoPK().getId() + "_preview.jpg";
				preview_url	= FileServer.getUrl(null, componentId, namePreview, unePhoto.getImageMimeType(), nomRep);
				photoId 	= new Integer(unePhoto.getPhotoPK().getId()).toString();
				out.println("Pic["+p+"] = '"+preview_url+"'");
			}
			p++;
		}
	%>
	
	var t
	var j 	= <%=rang.intValue()%>
	var p 	= Pic.length
	var pos = j
	
	var preLoad = new Array()
	
	function preLoadPic(index)
	{
		if (Pic[index] != ''){
            window.status='Loading : '+Pic[index]
            preLoad[index] = new Image()
            preLoad[index].src = Pic[index]
            Pic[index] = ''
            window.status=''
        }
	}
	
	function runSlideShow()
	{
		if (xIE4Up){
	    	document.images.SlideShow.style.filter="blendTrans(duration=2)"
	        document.images.SlideShow.style.filter= "blendTrans(duration=crossFadeDuration)"
	      	document.images.SlideShow.filters.blendTrans.Apply()
	    }
	    document.images.SlideShow.src = preLoad[j].src
	    if (xIE4Up){
	    	document.images.SlideShow.filters.blendTrans.Play()
	    }
	
	    pos = j
	
        j = j + 1
        if (j > (p-1)) j=0
        t = setTimeout('runSlideShow()', slideShowSpeed)
        preLoadPic(j)
	}
	
	function endSlideShow()
	{
		self.document.location = 'StopDiaporama?Rang='+pos
	}
	
	preLoadPic(j)

</script>
		</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(displayPath(path));
	
   	// fin du diaporama
 	operationPane.addOperation(resource.getIcon("gallery.stopDiaporama"), resource.getString("gallery.diaporama"), "javaScript:endSlideShow();");
   
	out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<table CELLPADDING=5 WIDTH="100%">
	<tr>
      	<td>
			<center><img src="<%=preview_url%>" name="SlideShow"></center>
			<input type="hidden" name="PhotoId" value="<%=photoId%>">
		</td>
	</tr>
</table>
<script language="JavaScript" type="text/JavaScript">
	runSlideShow()
</script>
<% 
  	out.println(frame.printAfter());
	out.println(window.printAfter());
%>	
</body>
</html>