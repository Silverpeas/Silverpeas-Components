<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.permissions.*" %>
<%@ page import="com.ecyrd.jspwiki.attachment.*" %>
<%@ page import="java.security.Permission" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<fmt:setLocale value="${userLanguage}"/>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="silverpeas_icons" />
<view:setBundle basename="templates.default"/>
<%
  WikiContext c = WikiContext.findContext(pageContext);
  WikiPage wikiPage = c.getPage();
  int attCount = c.getEngine().getAttachmentManager().listAttachments( c.getPage() ).size();
  String attTitle = LocaleSupport.getLocalizedMessage(pageContext, "attach.tab");
  if( attCount != 0 ) attTitle += " (" + attCount + ")";

  String creationAuthor ="";

  //FIXME -- seems not to work correctly for attachments !!
  WikiPage firstPage = c.getEngine().getPage( wikiPage.getName(), 1 );
  if( firstPage != null )
  {
    creationAuthor = firstPage.getAuthor();
  }

  int itemcount = 0;  //number of page versions
  try
  {
    itemcount = wikiPage.getVersion(); /* highest version */
  }
  catch( Exception  e )  { /* dont care */ }

  int pagesize = 20;
  int startitem = itemcount;
  String parm_start = (String)request.getParameter( "start" );
  if( parm_start != null ) startitem = Integer.parseInt( parm_start ) ;
  /*round to start of a pagination block */
  if( startitem > -1 ) startitem = ( (startitem/pagesize) * pagesize );

%>
<wiki:PageExists>

<%-- part 1 : normal wiki pages --%>
<wiki:PageType type="page">

  <view:tabs>  
	<c:set var="tabContentTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "view.tab")%></c:set>
    <c:set var="viewAction" value="<%=c.getURL(WikiContext.VIEW, c.getPage().getName())%>" />
	<view:tab label="${tabContentTitle}" action="${viewAction}" selected="false" />
  
  <c:set var="tabCommentTitle"><%=LocaleSupport.getLocalizedMessage(pageContext,
                    "comment.tab.addcomment")%></c:set>
    <c:set var="commentAction" value="<%=c.getURL(WikiContext.COMMENT, c.getPage().getName())%>" />
    <view:tab label="${tabCommentTitle}" action="${commentAction}" selected="false" />
  

	<c:set var="tabAttachTitle"><%=attTitle%></c:set>
    <c:set var="attachAction" value="<%=c.getURL(WikiContext.VIEW, c.getPage().getName())%>" />
      <view:tab label="${tabAttachTitle}" action="${attachAction}&attach=true" selected="false" />

      <c:set var="tabInfoTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "info.tab")%></c:set>
    <view:tab label="${tabInfoTitle}" action="${'#'}" selected="true" />	
  </view:tabs>
  <view:frame>
  <view:board>
  <fmt:message key='info.lastmodified'>
    <fmt:param><wiki:PageVersion >1</wiki:PageVersion></fmt:param>
    <fmt:param>
      <a href="<wiki:DiffLink format='url' version='latest' newVersion='previous' />"
        title="<fmt:message key='info.pagediff.title' />" >
        <fmt:formatDate value="<%= wikiPage.getLastModified() %>" pattern="${prefs['DateFormat']}" />
      </a>
    </fmt:param>
    <fmt:param><wiki:Author /></fmt:param>
  </fmt:message>

  <a href="<wiki:Link format='url' jsp='rss.jsp'>
             <wiki:Param name='page' value='<%=wikiPage.getName()%>'/>
             <wiki:Param name='mode' value='wiki'/>
           </wiki:Link>"
    title="<fmt:message key='info.rsspagefeed.title'>
             <fmt:param><wiki:PageName /></fmt:param>
           </fmt:message>" >
    <img src="<wiki:Link jsp='images/xml.png' format='url'/>" alt="[RSS]"/>
  </a>
  </p>

  <wiki:CheckVersion mode="notfirst">
    <p>
    <fmt:message key='info.createdon'>
      <fmt:param>
        <wiki:Link version="1">
          <fmt:formatDate value="<%= firstPage.getLastModified() %>" pattern="${prefs['DateFormat']}" />
        </wiki:Link>
      </fmt:param>
      <fmt:param><%= creationAuthor %></fmt:param>
    </fmt:message>
    </p>
  </wiki:CheckVersion>
  </view:board>

  <wiki:Permission permission="rename">
    <form action="<wiki:Link format='url' jsp='Rename.jsp'/>"
           class="wikiform"
              id="renameform"
        onsubmit="return Wiki.submitOnce(this);"
          method="post" accept-charset="<wiki:ContentEncoding />" >
		<table style="vertical-align: middle;">
		<tr>
		<td>
			<input type="hidden" name="page" value="<wiki:Variable var='pagename' />" />
		</td>
		<td>
			<c:set var="renameButtonLabel"><fmt:message key='info.rename.submit'/></c:set>
			<input type="text" name="renameto" value="<wiki:Variable var='pagename' />" size="40" />
		</td>
		<td>
			<view:button label="${renameButtonLabel}" action="javascript:document.forms['renameform'].submit()" disabled="false"/>
			&nbsp;&nbsp;
		</td>
		<td>
			<input type="checkbox" name="references" checked="checked" />
			<fmt:message key="info.updatereferrers"/>
		</td>
		</tr>
		</table>
    </form>
  </wiki:Permission>
  <wiki:Permission permission="!rename">
      <p><fmt:message key="info.rename.permission"/></p>
  </wiki:Permission>

  <div class="collapsebox-closed" id="incomingLinks">
  <h4><fmt:message key="info.tab.incoming" /></h4>
    <wiki:LinkTo><wiki:PageName /></wiki:LinkTo>
    <wiki:Plugin plugin="ReferringPagesPlugin" args="before='*' after='\n' " />
  </div>

  <div class="collapsebox-closed" id="outgoingLinks">
  <h4><fmt:message key="info.tab.outgoing" /></h4>
    <wiki:Plugin plugin="ReferredPagesPlugin" args="depth='1' type='local'" />
  </div>

  <div class="clearbox"></div>

  <%-- DIFF section --%>
  <wiki:CheckRequestContext context='diff'>
     <wiki:Include page="DiffTab.jsp"/>
  </wiki:CheckRequestContext>
  <%-- DIFF section --%>


    <wiki:CheckVersion mode="first"><fmt:message key="info.noversions"/></wiki:CheckVersion>
    <wiki:CheckVersion mode="notfirst">
    <%-- if( itemcount > 1 ) { --%>

    <wiki:SetPagination start="<%=startitem%>" total="<%=itemcount%>" pagesize="<%=pagesize%>" maxlinks="9"
                       fmtkey="info.pagination"
                         href='<%=c.getURL(WikiContext.INFO, c.getPage().getName(), "start=%s")%>' />

    <div>
      <table  class="tableArrayPane" width="100%" cellspacing="2" cellpadding="2" border="0">
        <tr>
          <th class="ArrayColumn"><fmt:message key="info.version"/></th>
          <th class="ArrayColumn"><fmt:message key="info.date"/></th>
          <th class="ArrayColumn"><fmt:message key="info.size"/></th>
          <th class="ArrayColumn"><fmt:message key="info.author"/></th>
          <th class="ArrayColumn"><fmt:message key="info.changes"/></th>
          <th class="ArrayColumn"><fmt:message key="info.changenote"/></th>
      </tr>

      <wiki:HistoryIterator id="currentPage">
      <% if( ( startitem == -1 ) ||
             (  ( currentPage.getVersion() >= startitem )
             && ( currentPage.getVersion() < startitem + pagesize ) ) )
         {
       %>
      <tr>
        <td class="ArrayCell">
          <wiki:LinkTo version="<%=Integer.toString(currentPage.getVersion())%>">
            <wiki:PageVersion/>
          </wiki:LinkTo>
        </td>

        <td class="ArrayCell"><fmt:formatDate value="<%= currentPage.getLastModified() %>" /></td>
        <td class="ArrayCell">
          <%--<fmt:formatNumber value='<%=Double.toString(currentPage.getSize()/1000.0)%>' groupingUsed='false' maxFractionDigits='1' minFractionDigits='1'/>&nbsp;Kb--%>
          <wiki:PageSize />
        </td>
        <td class="ArrayCell"><wiki:Author /></td>

        <td class="ArrayCell">
          <wiki:CheckVersion mode="notfirst">
            <wiki:DiffLink version="current" newVersion="previous"><fmt:message key="info.difftoprev"/></wiki:DiffLink>
            <wiki:CheckVersion mode="notlatest"> | </wiki:CheckVersion>
          </wiki:CheckVersion>

          <wiki:CheckVersion mode="notlatest">
            <wiki:DiffLink version="latest" newVersion="current"><fmt:message key="info.difftolast"/></wiki:DiffLink>
          </wiki:CheckVersion>
        </td>

         <td class="ArrayCell">
           <%
              String changeNote = (String)currentPage.getAttribute( WikiPage.CHANGENOTE );
              changeNote = (changeNote != null) ? TextUtil.replaceEntities( changeNote ) : "" ;
           %>
           <%= changeNote %>
         </td>

      </tr>
      <% } %>
      </wiki:HistoryIterator>

    </table>
    </div>
     ${pagination}
    <%-- } /* itemcount > 1 */ --%>
    </wiki:CheckVersion>
    </view:frame>
</wiki:PageType>


<%-- part 2 : attachments --%>
<wiki:PageType type="attachment">
<%
  int MAXATTACHNAMELENGTH = 30;
  String progressId = c.getEngine().getProgressManager().getNewProgressIdentifier();
%>

<view:tabs>
  <c:set var="tabContentTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "info.parent")%></c:set>
  <c:set var="viewAction" value="<%=c.getURL(WikiContext.VIEW, ((Attachment)wikiPage).getParentName()) %>" />
	<view:tab label="${tabContentTitle}" action="${viewAction}" selected="false" />

  <c:set var="tabAttachTitle"><%=LocaleSupport.getLocalizedMessage(pageContext, "info.attachment.tab")%></c:set>
  <view:tab label="${tabAttachTitle}" action="${'#'}" selected="true" />	
</view:tabs>
<view:frame>

  <h3><fmt:message key="info.uploadnew"/></h3>

  <wiki:Permission permission="upload">
  <form action="<wiki:Link jsp='attach' format='url' absolute='true'><wiki:Param name='progressid' value='<%=progressId%>'/></wiki:Link>"
         class="wikiform"
            id="uploadform"
      onsubmit="return Wiki.submitUpload(this, '<%=progressId%>');"
        method="post" accept-charset="<wiki:ContentEncoding/>"
       enctype="multipart/form-data" >

  <%-- Do NOT change the order of wikiname and content, otherwise the
       servlet won't find its parts. --%>

  <table>
  <tr>
    <td colspan="2"><div class="formhelp"><fmt:message key="info.uploadnew.help" /></div></td>
  </tr>
  <tr>
    <td><label for="content"><fmt:message key="info.uploadnew.filename" /></label></td>
    <td><input type="file" name="content" size="60"/></td>
  </tr>
  <tr>
    <td><label for="changenote"><fmt:message key="info.uploadnew.changenote" /></label></td>
    <td>
    <input type="text" name="changenote" maxlength="80" size="60" />
    </td>
  </tr>
  <tr>
    <td></td>
    <td>
    <input type="hidden" name="page" value="<wiki:Variable var='pagename' />" />
    <c:set var="addAttachButtonLabel"><fmt:message key='attach.add.submit'/></c:set>
	  <view:button label="${addAttachButtonLabel}" action="javascript:document.forms['uploadform'].submit()" disabled="false"/>
	  <input type="hidden" name="action"  value="upload" />
    <input type="hidden" name="nextpage" value="<wiki:PageInfoLink format='url'/>" />
        <div id="progressbar"><div class="ajaxprogress"></div></div>
    </td>
  </tr>
  </table>

  </form>
  </wiki:Permission>
  <wiki:Permission permission="!upload">
    <div class="formhelp"><fmt:message key="attach.add.permission"/></div>
  </wiki:Permission>

  <wiki:Permission permission="delete">
    <h3><fmt:message key="info.deleteattachment"/></h3>
    <form action="<wiki:Link format='url' context='<%=WikiContext.DELETE%>' />"
           class="wikiform"
              id="deleteForm"
          method="post" accept-charset="<wiki:ContentEncoding />"
        onsubmit="return( confirm('<fmt:message key="info.confirmdelete"/>') && Wiki.submitOnce(this) );" >
     <div>
	<c:set var="deleteAllButtonLabel"><fmt:message key='info.deleteattachment.submit'/></c:set>
	<view:button label="${deleteAllButtonLabel}" action="javascript:document.forms['deleteForm'].submit()" disabled="false"/>
     </div>
    </form>
  </wiki:Permission>

  <%-- FIXME why not add pagination here - no need for large amounts of attach versions on one page --%>
  <h3><fmt:message key='info.attachment.history' /></h3>

  <div>
  <table class="tableArrayPane" width="100%" cellspacing="2" cellpadding="2" border="0">
    <tr>
      <th class="ArrayColumn"><fmt:message key="info.attachment.type"/></th>
      <th class="ArrayColumn"><fmt:message key="info.version"/></th>
      <th class="ArrayColumn"><fmt:message key="info.size"/></th>
      <th class="ArrayColumn"><fmt:message key="info.date"/></th>
      <th class="ArrayColumn"><fmt:message key="info.author"/></th>
      <th class="ArrayColumn"><fmt:message key="info.changenote"/></th>
    </tr>

    <wiki:HistoryIterator id="att"><%-- <wiki:AttachmentsIterator id="att"> --%>
    <%
      String name = att.getName(); //att.getFileName();
      int dot = name.lastIndexOf(".");
      String attachtype = ( dot != -1 ) ? name.substring(dot+1) : "";

      String sname = name;
      if( sname.length() > MAXATTACHNAMELENGTH ) sname = sname.substring(0,MAXATTACHNAMELENGTH) + "...";
    %>

    <tr>
      <td class="ArrayCell"><view:mimeTypeIcon divId="<%="attach-" + attachtype%>" ><%= attachtype%></view:mimeTypeIcon></td>
      <td class="ArrayCell"><a href="<wiki:Link version='<%=Integer.toString(att.getVersion())%>' format='url' />"
                       title="<%= name %>"
                       class="attachment" ><wiki:PageVersion /></a></td>
      <td class="ArrayCell">
        <fmt:formatNumber value='<%=Double.toString(att.getSize()/1000.0) %>' groupingUsed='false' maxFractionDigits='1' minFractionDigits='1'/>&nbsp;<fmt:message key="info.kilobytes"/>
      </td>
	    <td class="ArrayCell" style="white-space:nowrap;"><fmt:formatDate value="<%= att.getLastModified() %>" /></td>
      <td class="ArrayCell"><wiki:Author /></td>
      <td class="ArrayCell">
      <%
         String changeNote = (String)att.getAttribute(WikiPage.CHANGENOTE);
         if( changeNote != null ) {
             changeNote = TextUtil.replaceEntities(changeNote);
         %><%=changeNote%><%
         }
      %>
      </td>
    </tr>
    </wiki:HistoryIterator><%-- </wiki:AttachmentsIterator> --%>

  </table>
  </div>

  </view:frame>
</wiki:PageType>

</wiki:PageExists>

<wiki:NoSuchPage>
  <fmt:message key="common.nopage">
    <fmt:param><wiki:EditLink><fmt:message key="common.createit"/></wiki:EditLink></fmt:param>
  </fmt:message>
</wiki:NoSuchPage>
