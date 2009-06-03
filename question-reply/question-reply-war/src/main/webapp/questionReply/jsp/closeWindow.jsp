<%@ page import="java.util.*"%>

<%@ include file="checkQuestionReply.jsp" %>
<%
	String url = (String) request.getAttribute("urlToReload");
%>

<SCRIPT LANGUAGE="JavaScript">
<!--
	self.opener.location = '<%=routerUrl+url%>';
	self.close();
//-->
</SCRIPT>
