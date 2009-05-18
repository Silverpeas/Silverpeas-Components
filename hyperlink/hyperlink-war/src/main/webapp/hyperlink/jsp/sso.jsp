<%@ include file="check.jsp" %>
<%
String login 	= (String) request.getAttribute("Login");
String password = (String) request.getAttribute("Password");
String domain 	= (String) request.getAttribute("Domain");
String url 		= (String) request.getAttribute("URL");
%>

<html>
<head>
<script language="JavaScript">
function getHTTPObject() {
	if (typeof XMLHttpRequest != 'undefined') 
	{ 
		return new XMLHttpRequest(); 
	} 
	try 
	{ 
		return new ActiveXObject("Msxml2.XMLHTTP"); 
	} 
	catch (e) 
	{ 
		try 
		{ 
			return new ActiveXObject("Microsoft.XMLHTTP"); 
		} 
		catch (e) {} 
	} 
	return false; 
}

window.onload = function() 
{ 
	var http = getHTTPObject(); 
	if (http) 
	{
		http.open("get", "<%=Encode.javaStringToJsString(url)%>", false, "<%=domain%>\\<%=login%>", "<%=Encode.javaStringToJsString(password)%>");
		http.send(""); 
		if (http.status == 200) 
		{
			document.location = "<%=Encode.javaStringToJsString(url)%>"; 
		} 
		else 
		{ 
			alert("Login et/ou Mot de passe incorrect !");
		} 
		return false;
	}
}
</script>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body>
<H1>Connexion en cours, merci de patienter...</H1>
</body>
</html>