<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki"%>
<%@ page import="com.ecyrd.jspwiki.*"%>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ page import="javax.servlet.jsp.jstl.fmt.*"%>
<fmt:setLocale value="${userLanguage}" />
<fmt:setBundle basename="templates.default" />
<%
  WikiContext c = WikiContext.findContext(pageContext);
  int attCount = c.getEngine().getAttachmentManager().listAttachments(
      c.getPage()).size();
  String attTitle = LocaleSupport.getLocalizedMessage(pageContext,
      "attach.tab");
  if (attCount != 0)
    attTitle += " (" + attCount + ")";
%>
<view:tabs>
  <c:set var="tabContentTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "view.tab")%></c:set>
  <c:set var="viewAction" value="<%=c.getURL(WikiContext.VIEW, c.getPage().getName())%>" />
  <view:tab label="${tabContentTitle}" action="${viewAction}" selected="false" />

  <wiki:PageExists>
    <c:set var="tabCommentTitle"><%=LocaleSupport.getLocalizedMessage(pageContext,
                    "comment.tab.addcomment")%></c:set>
    <c:set var="commentAction" value="<%=c.getURL(WikiContext.COMMENT, c.getPage().getName())%>" />
    <view:tab label="${tabCommentTitle}" action="${commentAction}" selected="true" />

    <c:set var="tabAttachTitle"><%=attTitle%></c:set>
    <c:set var="attachAction" value="<%=c.getURL(WikiContext.VIEW, c.getPage().getName())%>" />
    <view:tab label="${tabAttachTitle}" action="${attachAction}&attach=true" selected="false" />

    <c:set var="tabInfoTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "info.tab")%></c:set>
    <c:set var="infoAction" value="<%=c.getURL(WikiContext.INFO, c.getPage().getName())%>" />
    <view:tab label="${tabInfoTitle}" action="${infoAction}" selected="false" />
  </wiki:PageExists>
</view:tabs>
<view:frame>
  <wiki:Editor />
</view:frame>