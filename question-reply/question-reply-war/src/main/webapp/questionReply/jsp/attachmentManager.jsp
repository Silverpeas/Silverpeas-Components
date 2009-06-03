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
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
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