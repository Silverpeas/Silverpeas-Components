<%@ page import="java.util.*"%>
<%@ include file="checkQuestionReply.jsp" %>
<SCRIPT LANGUAGE="JavaScript">
<%
   	if (containerContext == null) {
	%>
    		self.location = "Main";
	<%
   	} else {
	%>
      		self.location = "<%=m_context+containerContext.getReturnURL()%>"; 
	<%
   	}

%>
</SCRIPT>
