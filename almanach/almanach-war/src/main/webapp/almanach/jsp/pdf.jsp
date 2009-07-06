<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkAlmanach.jsp" %>
	<%
	//Recuperation des parametres
	String fileName = (String) request.getAttribute("FileName");

	if (fileName != null) {
		%>
		<html>
		<head>
		<script language="JavaScript">
		function compileResult(fileName) {
			window.location.replace(fileName);
		}
		</script>
		</HEAD>
		<body onLoad="compileResult('<%=fileName%>')">
		</body>
		</html>
		<%
	} else {
		out.print(almanach.getString("errorPdf"));
	}
	%>
