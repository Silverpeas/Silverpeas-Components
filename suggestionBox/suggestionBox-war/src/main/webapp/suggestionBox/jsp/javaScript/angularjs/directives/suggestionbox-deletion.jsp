<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>

<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.components.suggestionbox.multilang.SuggestionBoxBundle"/>

<fmt:message var="deleteSuggestionConfirmMessage" key="suggestionBox.message.suggestion.remove.confirm">
  <fmt:param value="@name@"/>
</fmt:message>

<div style="display: none">
  <span id="deleteSuggestionConfirmMessage" style="display: none">${deleteSuggestionConfirmMessage}</span>

  <div id="suggestionDeletion">
    <span id="suggestionDeletionMessage"></span>

    <form id="suggestionDeletionForm" action="#" method="POST"></form>
  </div>
</div>
