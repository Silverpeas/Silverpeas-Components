<%@ include file="checkKmelia.jsp" %>
<%@ include file="tabManager.jsp.inc" %>

<%
PublicationDetail 	pubDetail 		= (PublicationDetail) request.getAttribute("CurrentPublicationDetail");
String				contentLanguage = (String) request.getAttribute("Language"); 

String pubId 	= pubDetail.getPK().getId();
String pubName 	= pubDetail.getName(contentLanguage);
String returnURL = "";
/*if (kmaxMode)
	returnURL = m_context + kmeliaScc.getComponentUrl() + "KmaxFromWysiwyg?PubId="+pubId;
else*/
	returnURL = m_context + kmeliaScc.getComponentUrl() + "FromWysiwyg?PubId="+pubId;
	
%>
<HTML>
<HEAD>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">

function goToWysiwyg() {
    document.toWysiwyg.submit();
}

function closeWindows() {
    if (window.publicationWindow != null)
        window.publicationWindow.close();
}
</script>
</HEAD>
<BODY onUnload="closeWindows()">

<form name="toWysiwyg" Action="../../wysiwyg/jsp/htmlEditor.jsp" method="Post">
	<input type="hidden" name="SpaceId" value="<%=spaceId%>">
    <input type="hidden" name="SpaceName" value="<%=spaceLabel%>">
    <input type="hidden" name="ComponentId" value="<%=componentId%>">
    <input type="hidden" name="ComponentName" value="<%=Encode.javaStringToHtmlString(componentLabel)%>">
<% if (kmaxMode) { %>
	<input type="hidden" name="BrowseInfo" value="<%= Encode.javaStringToHtmlString(pubName)%>">
<% } else { %>
	<input type="hidden" name="BrowseInfo" value="<%=kmeliaScc.getSessionPathString()+" > " + Encode.javaStringToHtmlString(pubName)%>">
<% } %>
    <input type="hidden" name="ObjectId" value="<%=pubId%>">
    <input type="hidden" name="Language" value="<%=resources.getLanguage()%>">
    <input type="hidden" name="ContentLanguage" value="<%=contentLanguage%>">
    <input type="hidden" name="ReturnUrl" value="<%=returnURL%>">
    <input type="hidden" name="UserId" value="<%=kmeliaScc.getUserId()%>">
</form>

<script>goToWysiwyg()</script>
</BODY>
</HTML>