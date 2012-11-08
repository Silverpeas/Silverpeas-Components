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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkSurvey.jsp" %>
<%@ include file="surveyUtils.jsp" %>


<%
	//R�cup�ration des param�tres
	QuestionContainerDetail survey 	= (QuestionContainerDetail) request.getAttribute("Survey");
	String profile 					= (String) request.getAttribute("Profile");
	Collection resultsByUser		= (Collection) request.getAttribute("ResultUser");
	String userName					= (String) request.getAttribute("UserName");
	String userId					= (String) request.getAttribute("UserId");
	
	ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.survey.surveySettings", surveyScc.getLanguage());
	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

%>

<html>
<head>
<title></title>
<style>

			html, body, div, dl, dt, dd, ul, ol, li, h1, h2, h3, h4, h5, h6, pre, form, fieldset, textarea, p, blockquote, th, td, img, hr, embed, object {
				margin:0px;
				padding:0px;
				font-family:"Verdana","Arial",sans-serif;
			}
			
			body {
				margin:20px 0 0 20px;
			}
		
			/* tableau Doodle */
			.questionResults th {
				border-top:#CCC 1px solid;
			}
			
			.questionResults th, .questionResults td, .questionResults tr {
				padding:5px;
				font-size:10px;
			}
			
			.questionResults .questionResults-top th {
				background-color:#E4E4E4;
				/*border:#CCC 1px solid;*/
				width:150px;
			}
			
			.questionResults .questionResults-top .questionResults-vide, .questionResults tbody .questionResults-vide {
				background-color:#FFFFFF;
				border:none;
				padding:3px;
			}
			
			.questionResults .displayUserName , .questionResults .displayUserName a {
				/*display:block;*/
				background-color:#E4E4E4;
				/*border:#CCC 1px solid;*/
				margin-right:5px;
				font-size:12px;
				font-weight:bold;
			}
			
			.questionResults tbody tr td {
				background-color:#FFFFFF;
				/*border:#E9E9E9 1px solid;*/
				min-height:33px;
			}
			
			.questionResults tbody .questionResults-Oui {
				background-color:#D5FAC5;
				/*border:1px solid #7CCC24;*/
				text-align:center;
				font-size:12px;
			}
			
			.questionResults tbody .questionResults-Non {
				background-color:#FECBCB;
				/*border:1px solid #FD433E;*/
				text-align:center;
				font-size:12px;
			}
			
			.questionResults .labelAnswer {
				text-align:right;
				width:50%;
			}		
		</style>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=iconsPath%>/util/javaScript/animation.js"></script>
<script language="JavaScript1.2">
        
	function viewUsers(id)
	{
    	url = "ViewListResult?AnswerId="+id;
 		windowName = "users";
 		larg = "550";
 		haut = "250";
 		windowParams = "directories=0,menubar=0,toolbar=0,resizable=1,scrollbars=1,alwaysRaised";
 		suggestions = SP_openWindow(url, windowName, larg , haut, windowParams);
 		suggestions.focus();
    }
 				   
</script>
</head>
<body>
<%     
	Window window = gef.getWindow();
    Frame frame = gef.getFrame();

    String surveyPart = displaySurveyResult(userName, userId, "user", resultsByUser, "C", survey, gef, m_context, surveyScc, resources, false, settings, frame);
    String action = "ViewResult";
    window.addBody(frame.printBefore()+"<center>"+""+"</center><BR>"+surveyPart);

    out.println(window.print());
%>

</body>
</html>