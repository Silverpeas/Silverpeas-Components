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
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<%@ include file="checkQuickInfo.jsp" %>
<%@ page import="com.stratelia.silverpeas.wysiwyg.control.WysiwygController" %>
<%@ page import="com.stratelia.silverpeas.util.SilverpeasSettings" %>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager" %>
<%@ page import="com.stratelia.webactiv.util.fileFolder.FileFolderManager" %>
<%@ page import="com.stratelia.webactiv.util.fileFolder.FileFolderManager" %>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager" %>

<%
  PublicationDetail quickInfoDetail = (PublicationDetail) request.getAttribute("info");
  String pubId   = (String)request.getAttribute("Id");
	
  String routerUrl = URLManager.getApplicationURL() + URLManager.getURL("quickinfo", quickinfo.getSpaceId(), quickinfo.getComponentId());

  boolean isNewSubscription = true;
  String codeHtml = "";
  if (pubId != null && pubId != "-1") {
       isNewSubscription = false;
			if (quickInfoDetail.getWysiwyg() != null && !"".equals(quickInfoDetail.getWysiwyg()))
	       codeHtml = quickInfoDetail.getWysiwyg();
      else if (quickInfoDetail.getDescription() != null)
	       codeHtml = Encode.javaStringToHtmlParagraphe(quickInfoDetail.getDescription());
  }

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>QuickInfo - Edition</title>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<script type="text/javascript">
function isCorrectForm() {
 	var errorMsg = "";
 	var errorNb = 0;
 	var beginDate = $("#BeginDate").val();
    var endDate = $("#EndDate").val();
    var yearBegin = extractYear(beginDate, '<%=quickinfo.getLanguage()%>');
    var monthBegin = extractMonth(beginDate, '<%=quickinfo.getLanguage()%>');
	var dayBegin = extractDay(beginDate, '<%=quickinfo.getLanguage()%>');
	var yearEnd = extractYear(endDate, '<%=quickinfo.getLanguage()%>'); 
	var monthEnd = extractMonth(endDate, '<%=quickinfo.getLanguage()%>');
	var dayEnd = extractDay(endDate, '<%=quickinfo.getLanguage()%>'); 
	var beginDateOK = false;
	var endDateOK = false;

	if (isWhitespace($("#Name").val())) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.title")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++; 
    }
       
    if (! isWhitespace(beginDate)) {
    	if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
            	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("dateDebut")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
             	errorNb++;
    	}
    	else beginDateOK = true;
    }	
  
    if (! isWhitespace(endDate)) {
    	if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
            	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("dateFin")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
             	errorNb++;
    	}
    	else endDateOK = true;
    }
    
    if (beginDateOK && endDateOK) {
    		if (isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin)==false) {
    			errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("dateFin")%>' <%=resources.getString("MustContainsPostDateToBeginDate")%>\n";
                            errorNb++;	
    		}
    }       	       

 	switch(errorNb)
 	{
    	case 0 :
        	result = true;
        	break;
    	case 1 :
        	errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
        	window.alert(errorMsg);
        	result = false;
        	break;
    	default :
    	    errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
    	    window.alert(errorMsg);
    	    result = false;
    	    break;
 	}
 	return result;
}

function reallyAddQuickInfo() {
	if (isCorrectForm()) {
		document.quickInfoEditForm.Action.value = "ReallyAdd";
		document.quickInfoEditForm.submit();
	}
}

function updateQuickInfo() {
	if (isCorrectForm()) {
		document.quickInfoEditForm.Action.value = "ReallyUpdate";
		document.quickInfoEditForm.submit();
	}
}

function quickInfoDeleteConfirm() {
	if (window.confirm("<%=resources.getString("supprimerQIConfirmation")%>")) {
      document.quickInfoEditForm.Action.value = "ReallyRemove";
      document.quickInfoEditForm.submit();
	}
}

function ClipboardCopyOne() {
	document.quickInfoForm.action = "<%=m_context%><%=quickinfo.getComponentUrl()%>copy.jsp";
	document.quickInfoForm.target = "IdleFrame";
	document.quickInfoForm.submit();
}
</script>
</head>
<body id="quickinfo">
<div id="<%=componentId %>">
<%
    	Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
       	browseBar.setPath(resources.getString("edition"));
			
        if (quickInfoDetail != null) {
        	OperationPane operationPane = window.getOperationPane();
            operationPane.addOperation(m_context+"/util/icons/quickInfo_to_del.gif", resources.getString("suppression"), "javascript:onClick=quickInfoDeleteConfirm()");
            operationPane.addOperation(m_context+"/util/icons/copy.gif", generalMessage.getString("GML.copy"), "javascript:onClick=ClipboardCopyOne()");
        }

        out.println(window.printBefore());

        TabbedPane tabbedPane = gef.getTabbedPane();
        tabbedPane.addTab(resources.getString("GML.head"), routerUrl + "quickInfoEdit.jsp?Action=changePage&amp;Id="+pubId+"&amp;page=1",
            quickinfo.getPageId() == QuickInfoSessionController.PAGE_HEADER, !isNewSubscription );
        if (!isNewSubscription && quickinfo.isPdcUsed()) {
            tabbedPane.addTab( resources.getString("GML.PDC") , routerUrl + "quickInfoEdit.jsp?Action=changePage&amp;Id="+pubId
            +"&amp;page=2", quickinfo.getPageId() != QuickInfoSessionController.PAGE_HEADER, true);
        }

        out.println(tabbedPane.print());
%>
<form name="quickInfoEditForm" action="quickInfoEdit" method="post">
<view:frame>
<view:board>
	<table width="100%" border="0" cellspacing="0" cellpadding="3">
	    <tr>
        	<td nowrap="nowrap" class="txtlibform"><%=resources.getString("GML.title")%>:</td>
            <td><input type="text" size="97" id="Name" name="Name" maxlength="<%=DBUtil.TextFieldLength%>" <%
                        if (quickInfoDetail != null)
                          out.println("value=\""+Encode.javaStringToHtmlString(quickInfoDetail.getName())+"\"");
                      %>/>
                      &nbsp;<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5" alt=""/></td>
         </tr>
         <tr>
         	<td nowrap="nowrap" class="txtlibform" valign="top"><%= resources.getString("GML.description")%>:</td>
	        <td><textarea name="Description" id="Description" rows="50" cols="10"><%=codeHtml%></textarea></td>
         </tr>
		 <tr>
             <td class="txtlibform" nowrap="nowrap"><%=resources.getString("dateDebut")%> :</td>
             <td><input class="dateToPick" type="text" id="BeginDate" name="BeginDate" size="14" maxlength="<%=DBUtil.DateFieldLength%>" <%
                        if (quickInfoDetail != null)
                          if (quickInfoDetail.getBeginDate() != null)
                            out.println("value=\""+resources.getInputDate(quickInfoDetail.getBeginDate())+"\"");
                      %>/>                      
                      <span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span></td>
         </tr>
         <tr>
         	<td class="txtlibform" nowrap="nowrap"><%=resources.getString("dateFin")%> :</td>
            <td><input class="dateToPick" type="text" id="EndDate" name="EndDate" size="14" maxlength="<%=DBUtil.DateFieldLength%>" <%
                        if (quickInfoDetail != null)
                          if (quickInfoDetail.getEndDate() != null)
                            out.println("value=\""+resources.getInputDate(quickInfoDetail.getEndDate())+"\"");
                      %>/>
                      <span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span></td>
         </tr>
         <tr><td colspan="2" align="left"><span class="txtnote">(<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5" alt=""/> = <%=resources.getString("GML.requiredField")%></span>)</td></tr>
     </table>
</view:board>
<%
	
	ButtonPane	buttonPane		= gef.getButtonPane();

%>
  
        <table width="100%" border="0" cellspacing="0" cellpadding="5">
          <tr>
            <td align="right">
              <%
                String link = "javascript:onClick=updateQuickInfo()";
                if (quickInfoDetail == null)
                  link = "javascript:onClick=reallyAddQuickInfo()";
                Button button = gef.getFormButton(resources.getString("GML.validate"), link, false);
								buttonPane.addButton(button);
                button = gef.getFormButton(resources.getString("GML.cancel"), "Main", false);
								buttonPane.addButton(button);
              %>
            <br/><center><%=buttonPane.print()%></center><br/>
            </td>
          </tr>
        </table>
    
	<input type="hidden" name="Action"/>
    <% if (quickInfoDetail != null) { %>
    	<input type="hidden" name="Id" value="<%=quickInfoDetail.getPK().getId()%>"/>
  	<% } %>
</view:frame>
</form>

<%
        out.println(window.printAfter());
%>

<form name="quickInfoForm" action="quickInfoEdit.jsp" method="post">
  <input type="hidden" name="Action"/>
  <% if (quickInfoDetail != null) { %>
      <input type="hidden" name="Id" value="<%=quickInfoDetail.getPK().getId()%>"/>
  <% } %>
</form>
<%                    
out.println("<script type=\"text/javascript\">");
out.println("var oFCKeditor = new FCKeditor('Description');");
out.println("oFCKeditor.Width = \"500\";");
out.println("oFCKeditor.Height = \"300\";");
out.println("oFCKeditor.BasePath = \""+URLManager.getApplicationURL()+"/wysiwyg/jsp/FCKeditor/\" ;");
out.println("oFCKeditor.DisplayErrors = true;");
out.println("oFCKeditor.Config[\"AutoDetectLanguage\"] = false");
out.println("oFCKeditor.Config[\"DefaultLanguage\"] = \""+quickinfo.getLanguage()+"\";");
String configFile = SilverpeasSettings.readString(settings, "configFile", URLManager.getApplicationURL() +"/wysiwyg/jsp/javaScript/myconfig.js");
out.println("oFCKeditor.Config[\"CustomConfigurationsPath\"] = \""+configFile+"\";");
out.println("oFCKeditor.ToolbarSet = 'quickinfo';");
out.println("oFCKeditor.Config[\"ToolbarStartExpanded\"] = true;");
out.println("oFCKeditor.ReplaceTextarea();");
out.println("</script>");
%>
</div>
</body>
</html>