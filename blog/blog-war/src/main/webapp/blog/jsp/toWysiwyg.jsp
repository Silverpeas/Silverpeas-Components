<%@ include file="check.jsp" %>

<%
PublicationDetail 	pub			= (PublicationDetail) request.getAttribute("CurrentPublicationDetail");

String 				pubId 		= pub.getPK().getId();
String 				pubName 	= pub.getName();

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
<BODY id="blog" onUnload="closeWindows()">
<div id="<%=instanceId %>">
<form name="toWysiwyg" Action="../../wysiwyg/jsp/htmlEditor.jsp" method="Post">
	<input type="hidden" name="SpaceId" value="<%=spaceId%>">
    <input type="hidden" name="SpaceName" value="<%=spaceLabel%>">
    <input type="hidden" name="ComponentId" value="<%=instanceId%>">
    <input type="hidden" name="ComponentName" value="<%=Encode.javaStringToHtmlString(componentLabel)%>">
    <input type="hidden" name="ObjectId" value="<%=pubId%>">
    <input type="hidden" name="Language" value="<%=resource.getLanguage()%>">
    <input type="hidden" name="ReturnUrl" value="<%=m_context+URLManager.getURL("blog", "useless", instanceId)%>FromWysiwyg?PostId=<%=pubId%>">
    <input type="hidden" name="UserId" value="<%=userId%>">
    <input type="hidden" name="IndexIt" value="false" >
</form>

<script>goToWysiwyg()</script>
</div>
</BODY>
</HTML>