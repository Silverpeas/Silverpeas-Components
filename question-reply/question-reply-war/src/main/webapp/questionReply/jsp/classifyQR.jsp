<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>

<%
	String   contentId = (String) request.getAttribute("contentId");
	URLIcone classify = containerContext.getClassifyURLIcone();
	String classifyURL = null;
	if (contentId != null)
	{
		classifyURL = containerContext.getClassifyURLWithParameters(
			componentId, contentId);
	}

%>

<SCRIPT LANGUAGE="JavaScript">
<!--
function classify()
{
	SP_openWindow('<%=m_context+classifyURL%>','classify','500', '300', 'menubar=no,scrollbars=no,statusbar=no');
}
//-->
classify();
self.location="<%=routerUrl%>ConsultQuestion";
</SCRIPT>