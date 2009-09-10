<%
	response.setHeader( "Expires", "Tue, 21 Dec 1993 23:59:59 GMT" );
	response.setHeader( "Pragma", "no-cache" );
	response.setHeader( "Cache-control", "no-cache" );
	response.setHeader( "Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT" );
	response.setStatus( HttpServletResponse.SC_CREATED );
%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>

<%
String componentId 	= request.getParameter("ComponentId");
String language 	= request.getParameter("Language");

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>

<html>
<head>
<title></title>
<script language="javascript">
function selectImage(url)
{
	window.opener.choixImageInGallery(url);
	window.close();
}
</script>
</head>
<frameset cols="200,600" rows="*" framespacing="0" frameborder="NO">
  <frame src="<%=m_context%>/GalleryInWysiwyg/dummy?ComponentId=<%=componentId%>&Language=<%=language%>" name="treeview" scrolling="AUTO" frameborder="no">
  <frame src="wysiwygImages.jsp" name="images" scrolling="AUTO" frameborder="NO">
</frameset><noframes></noframes>
</html>