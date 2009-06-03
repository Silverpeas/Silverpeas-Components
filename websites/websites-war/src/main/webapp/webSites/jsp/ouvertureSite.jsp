<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="javax.naming.Context"%>
<%@ page import="javax.naming.InitialContext"%>
<%@ page import="javax.rmi.PortableRemoteObject"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%> 
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>

<%@ include file="checkScc.jsp" %>

<%

//CBO : REMOVE String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String url = (String) request.getParameter("URL");

%>

 <!-- ouvertureSite -->          
 
<HTML>
<HEAD>

<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>

<%
out.println(gef.getLookStyleSheet());
%>
<!--CBO : UPDATE-->
<!--<script type="text/javascript" src="<%/*iconsPath*/%>/util/javaScript/animation.js"></script>-->
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">

function openSite(URL) {
    winName = "site";
    larg ="670";
    haut = "500";
    windowParams = "width="+larg+",height="+haut+", toolbar=yes, scrollbars=yes, resizable, alwaysRaised";
    site = window.open(URL,winName,windowParams);    
    location.href("Main.jsp");    
}

</Script>

</HEAD>

<BODY bgcolor="white" topmargin="5" leftmargin="5" onLoad="openSite('<%=url%>')">
</BODY>     
</HTML>
