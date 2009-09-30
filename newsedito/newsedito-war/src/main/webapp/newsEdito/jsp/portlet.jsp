<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
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