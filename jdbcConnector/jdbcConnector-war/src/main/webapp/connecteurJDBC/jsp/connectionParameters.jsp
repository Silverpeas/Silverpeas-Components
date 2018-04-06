<%@ include file="head.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<view:setConstant var="adminRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.admin"/>

<c:if test="${not requestScope.highestUserRole.isGreaterThanOrEquals(adminRole)}">
  <c:redirect url="/Error403.jsp"/>
</c:if>

<c:set var="currentConnectionInfo" value="${requestScope.currentConnectionInfo}"/>
<c:set var="availableDataSources" value="${requestScope.availableDataSources}"/>

<fmt:message var="msgTooLongField"        key='erreurChampsTropLong'/>
<fmt:message var="msgNotNumberField"      key='erreurChampsNonEntier'/>
<fmt:message var="connectionSettingTitle" key="titreParametrageConnection"/>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><fmt:message key="windowTitleParametrageConnection"/></title>
  <view:looknfeel withCheckFormScript="true" withFieldsetStyle="true"/>
  <script type="application/javascript">
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
      notyError('${msgTooLongField}');
    } else if (isValidTextField($('#password').val()) == false) {
      $('#password').focus();
      notyError('${msgTooLongField}');
    } else if (isFinite($('#rowLimit').val()) == false) {
      $('#rowLimit').focus();
      notyError('${msgNotNumberField}');
    } else {
      $('#processForm').submit();
    }
  }

  function cancel() {
    $('#processForm').attr('action', 'Main');
    $('#processForm').attr('method', 'GET');
    $('#processForm').submit();
  }
</script>
</head>
<body>
<view:browseBar extraInformations="${connectionSettingTitle}"/>
<view:window>
  <fmt:message var="consultation" key="tabbedPaneConsultation"/>
  <fmt:message var="request" key="tabbedPaneRequete"/>
  <fmt:message var="settings" key="tabbedPaneParametresJDBC"/>
  <view:tabs>
    <view:tab label="${consultation}" action="Main" selected="false"/>
    <view:tab label="${request}" action="ParameterRequest" selected="false"/>
    <view:tab label="${settings}" action="ParameterConnection" selected="true"/>
  </view:tabs>
  <view:frame>
    <form id="processForm" name="processForm" action="UpdateConnection" method="post">
      <div class="fields">
        <div class="field entireWidth">
          <label class="txtlibform" for="dataSource"><fmt:message key="dataSourceField"/></label>
          <div class="champs">
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
          </div>
        </div>
        <div class="field entireWidth">
          <label class="txtlibform" for="description"><fmt:message key="champsDescription"/></label>
          <div class="champs">
            <input type="text" id="description" name="Description" size="50" disabled value="${description}"/>
          </div>
        </div>
        <div class="field">
          <label class="txtlibform" for="login"><fmt:message key="champIdentifiant"/></label>
          <div class="champs">
            <input type="text" id="login" name="Login" size="50" value="${currentConnectionInfo.login}"/>
          </div>
        </div>
        <div class="field">
          <label class="txtlibform" for="password"><fmt:message key="champMotDePasse"/></label>
          <div class="champs">
            <input type="password" id="password" name="Password" size="50" value="${currentConnectionInfo.password}"/>
          </div>
        </div>
        <div class="field">
          <label class="txtlibform" for="rowLimit"><fmt:message key="champLignesMax"/></label>
          <div class="champs">
            <input type="text" id="rowLimit" name="RowLimit" size="50" value="${currentConnectionInfo.dataMaxNumber}"/>
            <i><fmt:message key="champLignesMaxExplanation"/></i>
          </div>
        </div>
      </div>
    </form>
    <div class="break-line"></div>
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
