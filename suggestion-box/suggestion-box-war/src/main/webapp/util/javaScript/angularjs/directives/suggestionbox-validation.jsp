<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>
<c:url var="formValidator" value="/util/javaScript/checkForm.js"/>

<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<fmt:message var="approveSuggestionConfirmMessage" key="GML.contribution.validation.approve.confirm">
  <fmt:param value="@name@"/>
</fmt:message>
<fmt:message var="refuseSuggestionConfirmMessage" key="GML.contribution.validation.refuse.confirm">
  <fmt:param value="@name@"/>
</fmt:message>
<fmt:message var="commentLabel" key="GML.contribution.validation.comment"/>

<script type="text/javascript" src="${formValidator}"></script>

<span id="suggestionValidationApproveMsg" style="display: none">${approveSuggestionConfirmMessage}</span>
<span id="suggestionValidationRefuseMsg" style="display: none">${refuseSuggestionConfirmMessage}</span>
<span id="commentMandatoryErrorMessageMsg" style="display: none"><b>${commentLabel}</b> <fmt:message key='GML.MustBeFilled'/></span>

<div id="suggestionValidation" style="display: none">
  <span id="suggestionValidationMessage"></span><br/><br/>

  <form id="suggestionValidationForm" action="#" method="POST">
    <div>
      <span class="txtlibform">${commentLabel}</span>
      <textarea id="suggestionValidationComment" name="comment" rows="5" cols="60"></textarea>&nbsp;<img border="0" src="${mandatoryIcons}" width="5" height="5"/>
    </div>
  </form>
</div>
<view:includePlugin name="tkn"/>
