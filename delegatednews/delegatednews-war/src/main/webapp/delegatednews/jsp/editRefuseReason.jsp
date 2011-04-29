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
<%@ include file="check.jsp"%>
  <c:set var="pubId" value="${requestScope.PubId}"/>
  <fmt:setLocale value="${sessionScope[sessionController].language}" />
  <view:setBundle basename="com.stratelia.webactiv.kmelia.multilang.kmeliaBundle" var="KML"/>
  <view:setBundle basename="com.stratelia.webactiv.multilang.generalMultilang" var="GML"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <title><fmt:message key="RefusalMotive" bundle="${KML}"/></title>
    <view:looknfeel />
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
    <script type="text/javascript">
    function refuseDelegatedNews()
  {
    window.opener.document.listDelegatedNews.action = "RefuseDelegatedNews";
    window.opener.document.listDelegatedNews.PubId.value = document.refuseReasonForm.PubId.value;
    window.opener.document.listDelegatedNews.RefuseReasonText.value = stripInitialWhitespace(document.refuseReasonForm.refuseReasonText.value);
    window.opener.document.listDelegatedNews.submit();
    window.close();
    }
    </script>
  </head>  
  <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onload="document.refuseReasonForm.refuseReasonText.focus()">
      <view:frame>
        <view:board>
         
    <table class="intfdcolor4" border="0" cellpadding="0" cellspacing="0" width="98%">
    <form name="refuseReasonForm">
    <input type="hidden" name="PubId" value="<c:out value='${pubId}'/>">
    <tr align="center">
      <td valign="top" align="center">
      <table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%">
        <tr>
          <td valign="top"><span class="txtlibform"><fmt:message key="RefusalMotive" bundle="${KML}"/> :</span></td>
                    <td valign="top"><font size="1"><textarea name="refuseReasonText" id="refuseReasonText"></textarea></font></td>
        </tr>
      </table>
               </td>
            </tr>
        </form>
        </table>   
              
        <br/>
        <center>
        <fmt:message key="GML.validate" var="validate" bundle="${GML}"/>
        <fmt:message key="GML.cancel" var="cancel" bundle="${GML}"/>
          <view:buttonPane>
            <view:button action="javascript:onClick=refuseDelegatedNews()" label="${validate}" disabled="false" />
            <view:button action="javascript:onClick=window.close()" label="${cancel}" disabled="false" />
          </view:buttonPane>
        </center>
  
        </view:board>
      </view:frame>
  </body>
</html>
