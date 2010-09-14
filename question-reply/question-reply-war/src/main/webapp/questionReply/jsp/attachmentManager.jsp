<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkQuestionReply.jsp" %>

<%
Reply 	reply 		= (Reply) request.getAttribute("CurrentReply");
String	currentLang = (String) request.getAttribute("Language");
	
String replyId = reply.getPK().getId();
String	pIndexIt	= "1";
String url = scc.getComponentUrl()+"ViewAttachments";
boolean openUrl = false;
%>
<HTML>
<HEAD>
<TITLE></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">

</script>
</HEAD>
<BODY>
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel);
	browseBar.setExtraInformation(resource.getString("questionReply.reponse"));
	
	tabbedPane.addTab(resource.getString("GML.head"), "UpdateRQuery", false);
	tabbedPane.addTab(resource.getString("GML.attachments"), "#", true, false);
	
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
	out.flush();
	
	try
	{
		if (scc.isVersionControlled()) 
		{
			//Versioning links
			getServletConfig().getServletContext().getRequestDispatcher("/versioningPeas/jsp/documents.jsp?Id="+URLEncoder.encode(replyId)+"&SpaceId="+URLEncoder.encode(spaceId)+"&ComponentId="+URLEncoder.encode(componentId)+"&Context=Images&IndexIt="+pIndexIt+"&Url="+URLEncoder.encode(url)+"&SL="+URLEncoder.encode(scc.getSpaceLabel())+"&CL="+URLEncoder.encode(scc.getComponentLabel())).include(request, response);
		} 
		else
		{
			//Attachments links
			getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/editAttFiles.jsp?Id="+replyId+"&ComponentId="+componentId+"&Context=Images&IndexIt="+pIndexIt+"&Url="+url+"&UserId="+scc.getUserId()+"&OpenUrl="+openUrl+"&Profile="+scc.getUserProfil()+"&Language="+currentLang).include(request, response);
		}
	}
	catch (Exception e)
	{
		
	}
	out.println(frame.printAfter());
	out.println(window.printAfter());
	
%>
</BODY>
</HTML>