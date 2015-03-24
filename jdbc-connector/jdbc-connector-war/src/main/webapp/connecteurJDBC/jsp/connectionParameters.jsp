<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.connecteurJDBC.multilang.connecteurJDBC"/>
<c:set var="currentConnectionInfo" value="${requestScope.currentConnectionInfo}"/>
<c:set var="availableDataSources" value="${requestScope.availableDataSources}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><fmt:message key="windowTitleParametrageConnection"/></title>
  <view:looknfeel/>
<script type="text/javascript" src="<c:url value='/util/javaScript/checkForm.js'/>"></script>
<script type="text/javascript">
  var dataSources = [];
  <c:forEach items="${availableDataSources}" var="dataSource">
  <c:set var="dataSourceLogin" value=""/>
  <c:set var="dataSourcePassword" value=""/>
  <c:set var="rowLimit" value="0"/>
  <c:if test="${dataSource.dataSourceName == currentConnectionInfo.dataSourceName}">
    <c:set var="dataSourceLogin" value="${currentConnectionInfo.login}"/>
    <c:set var="dataSourcePassword" value="${currentConnectionInfo.password}"/>
    <c:set var="rowLimit" value="${currentConnectionInfo.dataMaxNumber}"/>
  </c:if>
  dataSources.push({
    name : '${dataSource.dataSourceName}',
    description : '${dataSource.description}',
    login : '${dataSourceLogin}',
    password : '${dataSourcePassword}',
    rowLimit : ${rowLimit}
  });
  </c:forEach>

  function updateForm() {
    var selectedDataSourceName = $('#dataSource').val();
    var selectedDataSource;
    for (i = 0; i < dataSources.length; i++) {
      if (dataSources[i].name === selectedDataSourceName) {
        selectedDataSource = dataSources[i];
        break;
      }
    }
    $('#description').val(selectedDataSource.description);
    $('#login').val(selectedDataSource.login);
    $('#password').val(selectedDataSource.password);
    $('#rowLimit').val(selectedDataSource.rowLimit);
  }

  function processUpdate() {
    if (isValidTextField($('#login').val()) === false) {
      $('#login').focus()
      alert("<fmt:message key='erreurChampsTropLong'/>");
    } else if (isValidTextField($('#password').val()) == false) {
      $('#password').focus();
      alert("<fmt:message key='erreurChampsTropLong'/>");
    } else if (isFinite($('#rowLimit').val()) == false) {
      $('#rowLimit').focus();
      alert("<fmt:message key='erreurChampsNonEntier'/>");
    } else {
      $('#processForm').attr('action', 'UpdateConnection');
      $('#processForm').submit();
    }
  }

  function cancel() {
    $('#processForm').attr('action', 'connecteurJDBC');
    $('#processForm').submit();
  }
</script>
</head>
<body>
<fmt:message var="connectionSettingTitle" key="titreParametrageConnection"/>
<view:browseBar extraInformations="${connectionSettingTitle}"/>

<view:window>
  <fmt:message var="consultation" key="tabbedPaneConsultation"/>
  <fmt:message var="request" key="tabbedPaneRequete"/>
  <fmt:message var="settings" key="tabbedPaneParametresJDBC"/>
  <view:tabs>
    <view:tab label="${consultation}" action="DoRequest" selected="false"/>
    <view:tab label="${request}" action="ParameterRequest" selected="false"/>
    <view:tab label="${settings}" action="ParameterConnection" selected="true"/>
  </view:tabs>
  <view:frame>
    <form id="processForm" name="processForm" action="">
      <table CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
        <TR>
          <TD>
            <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
              <TR>
                <TD class="txtlibform"><fmt:message key="dataSourceField"/></TD>
                <TD>
                  <select id="dataSource" name="DataSource" onchange="javascript:updateForm();">
                    <c:set var="description" value="${availableDataSources[0].description}"/>
                    <c:forEach items="${availableDataSources}" var="dataSource">
                      <c:choose>
                        <c:when test="${currentConnectionInfo != null && currentConnectionInfo.dataSourceName == dataSource.dataSourceName}">
                          <option value="${dataSource.dataSourceName}" selected="selected">${dataSource.dataSourceName}</option>
                          <c:set var="description" value="${dataSource.description}"/>
                        </c:when>
                        <c:otherwise>
                          <option value="${dataSource.dataSourceName}">${dataSource.dataSourceName}</option>
                        </c:otherwise>
                      </c:choose>
                    </c:forEach>
                  </select>
                </TD>
              </TR>
              <TR>
                <TD class="txtlibform"><fmt:message key="champsDescription"/></TD>
                <TD>
                  <input type="text" id="description" name="Description" size="50" disabled value="${description}"/>
                </TD>
              </TR>
              <TR>
                <TD class="txtlibform"><fmt:message key="champIdentifiant"/></TD>
                <TD>
                  <input type="text" id="login" name="Login" size="50" value="${currentConnectionInfo.login}"/>
                </TD>
              </TR>
              <TR>
                <TD class="txtlibform"><fmt:message key="champMotDePasse"/></TD>
                <TD>
                  <input type="password" id="password" name="Password" size="50" value="${currentConnectionInfo.password}"/>
                </TD>
              </TR>
              <TR>
                <TD class="txtlibform"><fmt:message key="champLignesMax"/></TD>
                <TD>
                  <input type="text" id="rowLimit" name="RowLimit" size="50" value="${currentConnectionInfo.dataMaxNumber}"/>
                  <i><fmt:message key="champLignesMaxExplanation"/></i>
                </TD>
              </TR>
            </table>
          </TD>
        </TR>
      </table>
    </form>
    <view:buttonPane>
      <fmt:message var="validate" key="boutonValider"/>
      <fmt:message var="cancel" key="boutonAnnuler"/>
      <view:button label="${validate}" action="javascript:onclick=processUpdate();"/>
      <view:button label="${cancel}" action="javascript:onclick=cancel();"/>
    </view:buttonPane>
  </view:frame>
</view:window>
</body>
</html>
