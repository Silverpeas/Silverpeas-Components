<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%@ page import="com.ecyrd.jspwiki.*"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt"%>

<fmt:setLocale value="${userLanguage}" />
<fmt:setBundle basename="templates.default" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" var="wikiMessages" />
<view:operationPane>
  <wiki:CheckRequestContext context='view|info|diff|upload'>
    <wiki:Permission permission="edit">
      <wiki:PageType type="page">
        <fmt:message key="actions.edit" var="editAltText" />
        <fmt:message key="wiki.icons.editPage" var="editIconPath" bundle="${icons}" />
        <c:url var="editIcon" value="${editIconPath}" />
        <c:set var="editLink">
          <wiki:EditLink format='url' />
        </c:set>
        <view:operation altText="${editAltText}" icon="${editIcon}" action="${editLink}" />
      </wiki:PageType>
      <wiki:PageType type="attachment"> 
        <fmt:message key="actions.editparent" var="editParentAltText" />
        <fmt:message key="wiki.icons.editParentPage" var="editParentIconPath" bundle="${icons}" />
        <c:url var="editParentIcon" value="${editParentIconPath}" />
        <c:url var="editParentLink" value="Edit.jsp">
          <c:param name="page">
            <wiki:ParentPageName />
          </c:param>
        </c:url>
        <view:operation altText="${editParentAltText}" icon="${editParentIcon}" action="${editParentLink}" />
      </wiki:PageType>
    </wiki:Permission>
  </wiki:CheckRequestContext>

  <%-- more actions dropdown -- converted to popup by javascript 
       so all basic actions are accessible even if js is not avail --%>

  <wiki:CheckRequestContext context='view|info|diff|upload'>
    <wiki:PageExists>
      <wiki:Permission permission="comment">
        <wiki:PageType type="page">
          <fmt:message key="actions.comment" var="commentAltText" />
          <fmt:message key="wiki.icons.commentPage" var="commentIconPath" bundle="${icons}" />
          <c:url var="commentIcon" value="${commentIconPath}" />
          <c:set var="commentLink">
            <wiki:CommentLink format='url' />
          </c:set>
          <view:operation altText="${commentAltText}" icon="${commentIcon}" action="${commentLink}" />
        </wiki:PageType>
        <wiki:PageType type="attachment">
          <fmt:message key="actions.comment" var="commentAltText" />
          <fmt:message key="wiki.icons.commentPage" var="commentIconPath" bundle="${icons}" />
          <c:url var="commentIcon" value="${commentIconPath}" />
          <c:url var="commentLink" value="Comment.jsp">
            <c:param name="page">
              <wiki:ParentPageName />
            </c:param>
          </c:url>
          <view:operation altText="${commentAltText}" icon="${commentIcon}" action="${commentLink}" />
        </wiki:PageType>
      </wiki:Permission>
    </wiki:PageExists>
  </wiki:CheckRequestContext>

  <wiki:Permission permission="delete">
    <c:set var="deleteAllButtonLabel">
      <fmt:message key='info.delete.submit' />
    </c:set>
    <fmt:message key="wiki.icons.commentPage" var="deleteIconPath" bundle="${icons}" />
    <c:url var="deleteIcon" value="${deleteIconPath}" />
    <view:operation altText="${deleteAllButtonLabel}" icon="${deleteIcon}" action="javascript:document.forms['deleteForm'].submit()" />
  </wiki:Permission>

  <wiki:CheckRequestContext context='view|info|diff|upload|edit|comment|preview'>
    <fmt:message key="actions.rawpage" var="rawpageAltText" />
    <fmt:message key="wiki.icons.rawPage" var="rawIconPath" bundle="${icons}" />
    <c:url var="rawIcon" value="${rawIconPath}" />
    <c:set var="rawpageLink">
      <wiki:Link format='url'>
        <wiki:Param name='skin' value='raw' />
      </wiki:Link>
    </c:set>
    <view:operation altText="${rawpageAltText}" icon="${rawIcon}" action="${rawpageLink}" />
    
    <fmt:message key="prefs.tab" var="prefsPageLabel" />
    <fmt:message key="prefs.tab.title" var="prefsAltText" bundle="${wikiMessages}" /> 
    <fmt:message key="wiki.icons.prefsPage" var="prefsIconPath" bundle="${icons}" />
    <c:url var="prefsIcon" value="${prefsIconPath}" />
     <%WikiContext myPrefsContext = WikiContext.findContext(pageContext);%>
    <c:set var="prefsAction" value="<%=myPrefsContext.getURL(WikiContext.PREFS, myPrefsContext.getPage().getName())%>" />
    <view:operation altText="${prefsAltText}" icon="${prefsIcon}" action="${prefsAction}" />
  </wiki:CheckRequestContext>

  <wiki:CheckRequestContext context='edit|info|diff|upload|comment|preview|prefs'>
    <fmt:message key="view.tab" var="viewPageLabel" />
    <fmt:message key="view.tab.title" var="viewAltText" bundle="${wikiMessages}" /> 
    <fmt:message key="wiki.icons.viewPage" var="viewIconPath" bundle="${icons}" />
    <%WikiContext viewContext = WikiContext.findContext(pageContext);%>
    <c:url var="viewIcon" value="${deleteIconPath}" />
    <c:set var="viewAction" value="<%=viewContext.getURL(WikiContext.VIEW, viewContext.getPage().getName())%>" />
    <view:operation altText="${viewAltText}" icon="${viewIcon}" action="${viewAction}" />   
  </wiki:CheckRequestContext>
</view:operationPane>
