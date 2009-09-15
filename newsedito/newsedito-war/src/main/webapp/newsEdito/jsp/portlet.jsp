<%@ include file="imports.jsp" %>
<%@ include file="declarations.jsp.inc" %>

<%@ include file="publicationUtils.jsp.inc" %>

<%
        String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

		action = "View";
 		news.selectFirstOnLineArchive();
%>

<HTML>
<HEAD>

<% out.println(gef.getLookStyleSheet()); %>

<script type="text/javascript" src="../../util/javaScript/navigation.js"></script>

<Script language="JavaScript">

function selectTitle(titleId)
{
    //document.portletForm.target = "MyMain";
    //document.portletForm.action = "Main.jsp";
    //document.portletForm.submit();	
		gotoComponentPart('<%=m_context + news.getComponentUrl()%>',titleId, 'Node');
}

function selectPublication(publicationId)
{
    //document.portletForm.target = "MyMain";
    //document.portletForm.action = "Main.jsp";
    //document.portletForm.submit();	
		gotoComponentPart('<%=m_context + news.getComponentUrl()%>', publicationId,'Publication');
}


</Script>

<TITLE><%=generalMessage.getString("GML.popupTitle")%></TITLE>

</HEAD>

<BODY>

<%@ include file="init.jsp.inc" %>
<%
archives = null;
%>

<table width="100%" border="0" cellspacing="1" cellpadding="3">
  <tr>

    <td width="25%" valign="top"> 

	<%@ include file="navigationDisplaying.jsp.inc" %>   
    
    </td>

    <td valign="top"> 

		<% 
			Collection editoList = null;
			
			try {
				editoList = news.getArchivePublicationDetails();
			}
	   	catch	(NewsEditoException e) {
			SilverTrace.error("NewsEdito", "portlet_JSP", "NewsEdito.EX_PROBLEM_TO_GET_ARCHIVE",e);
		}   

			if (editoList!=null)
				displayEditorial(out,editoList,archiveDetail.getModelId(),news,true,"selectPublication","headline","healineBody");
     %>

      <p>&nbsp;</p>
      </td>
  </tr>
</table>

<form name="portletForm">
</form>


</BODY>
</HTML>