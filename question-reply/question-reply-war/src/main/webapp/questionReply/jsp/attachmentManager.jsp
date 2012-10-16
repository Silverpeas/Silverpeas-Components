<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ include file="checkQuestionReply.jsp" %>

<%
Reply 	reply 		= (Reply) request.getAttribute("CurrentReply");
String	currentLang = (String) request.getAttribute("Language");
	
String replyId = reply.getPK().getId();
String	pIndexIt	= "1";
String url = scc.getComponentUrl()+"ViewAttachments";
boolean openUrl = false;
%>
<html>
<head>
<title></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">

</script>
</head>
<body>
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
			getServletConfig().getServletContext().getRequestDispatcher("/versioningPeas/jsp/documents.jsp?Id="+URLEncoder.encode(replyId)+"&SpaceId="+URLEncoder.encode(spaceId)+"&ComponentId="+URLEncoder.encode(componentId, "UTF-8")+"&Context=Images&IndexIt="+pIndexIt+"&Url="+URLEncoder.encode(url, "UTF-8")+"&SL="+URLEncoder.encode(scc.getSpaceLabel(), "UTF-8")+"&CL="+URLEncoder.encode(scc.getComponentLabel(), "UTF-8")).include(request, response);
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
</body>
</html>