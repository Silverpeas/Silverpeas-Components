<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.permissions.*" %>
<%@ page import="com.ecyrd.jspwiki.attachment.*" %>
<%@ page import="java.security.Permission" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
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

  <wiki:TabbedSection defaultTab="info">

  <wiki:Tab id="pagecontent"
         title='<%=LocaleSupport.getLocalizedMessage(pageContext, "actions.view")%>'
     accesskey="v"
	       url="<%=c.getURL(WikiContext.VIEW, c.getPage().getName())%>">
      <%--<wiki:Include page="PageTab.jsp"/> --%>
  </wiki:Tab>

  <wiki:Tab id="attach" title="<%= attTitle %>" accesskey="a">
    <wiki:Include page="AttachmentTab.jsp"/>
  </wiki:Tab>

  <%-- actual infopage content --%>
  <wiki:Tab id="info" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "info.tab")%>' accesskey="i" >
  <p>
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

  <wiki:Permission permission="rename">
    <form action="<wiki:Link format='url' jsp='Rename.jsp'/>"
           class="wikiform"
              id="renameform"
        onsubmit="return Wiki.submitOnce(this);"
          method="post" accept-charset="<wiki:ContentEncoding />" >
      <p>
      <input type="hidden" name="page" value="<wiki:Variable var='pagename' />" />
      <input type="submit" name="rename" value="<fmt:message key='info.rename.submit' />" />
      <input type="text" name="renameto" value="<wiki:Variable var='pagename' />" size="40" />
      &nbsp;&nbsp;
      <input type="checkbox" name="references" checked="checked" />
      <fmt:message key="info.updatereferrers"/>
      </p>
    </form>
  </wiki:Permission>
  <wiki:Permission permission="!rename">
      <p><fmt:message key="info.rename.permission"/></p>
  </wiki:Permission>

  <wiki:Permission permission="delete">
    <form action="<wiki:Link format='url' context='<%=WikiContext.DELETE%>' />"
           class="wikiform"
              id="deleteForm"
          method="post" accept-charset="<wiki:ContentEncoding />"
        onsubmit="return( confirm('<fmt:message key="info.confirmdelete"/>') && Wiki.submitOnce(this) );">
      <p>
      <input type="submit" name="delete-all" id="delete-all"
            value="<fmt:message key='info.delete.submit'/>" >
      </p>
    </form>
  </wiki:Permission>
  <wiki:Permission permission="!delete">
      <p><fmt:message key="info.delete.permission"/></p>
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

    <div class="zebra-table sortable table-filter">
    <table class="wikitable" >
      <tr>
        <th><fmt:message key="info.version"/></th>
        <th><fmt:message key="info.date"/></th>
        <th><fmt:message key="info.size"/></th>
        <th><fmt:message key="info.author"/></th>
        <th><fmt:message key="info.changes"/></th>
        <th class='changenote'><fmt:message key="info.changenote"/></th>
      </tr>

      <wiki:HistoryIterator id="currentPage">
      <% if( ( startitem == -1 ) ||
             (  ( currentPage.getVersion() >= startitem )
             && ( currentPage.getVersion() < startitem + pagesize ) ) )
         {
       %>
      <tr>
        <td>
          <wiki:LinkTo version="<%=Integer.toString(currentPage.getVersion())%>">
            <wiki:PageVersion/>
          </wiki:LinkTo>
        </td>

        <td><fmt:formatDate value="<%= currentPage.getLastModified() %>" pattern="${prefs['DateFormat']}" /></td>
        <td>
          <%--<fmt:formatNumber value='<%=Double.toString(currentPage.getSize()/1000.0)%>' groupingUsed='false' maxFractionDigits='1' minFractionDigits='1'/>&nbsp;Kb--%>
          <wiki:PageSize />
        </td>
        <td><wiki:Author /></td>

        <td>
          <wiki:CheckVersion mode="notfirst">
            <wiki:DiffLink version="current" newVersion="previous"><fmt:message key="info.difftoprev"/></wiki:DiffLink>
            <wiki:CheckVersion mode="notlatest"> | </wiki:CheckVersion>
          </wiki:CheckVersion>

          <wiki:CheckVersion mode="notlatest">
            <wiki:DiffLink version="latest" newVersion="current"><fmt:message key="info.difftolast"/></wiki:DiffLink>
          </wiki:CheckVersion>
        </td>

         <td class="changenote">
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
  </wiki:Tab>

  </wiki:TabbedSection>

</wiki:PageType>


<%-- part 2 : attachments --%>
<wiki:PageType type="attachment">
<%
  int MAXATTACHNAMELENGTH = 30;
  String progressId = c.getEngine().getProgressManager().getNewProgressIdentifier();
%>

  <wiki:TabbedSection defaultTab="info">
  <wiki:Tab id="pagecontent"
         title='<%=LocaleSupport.getLocalizedMessage(pageContext, "info.parent")%>'
     accesskey="v"
	       url="<%=c.getURL(WikiContext.VIEW, ((Attachment)wikiPage).getParentName()) %>">
  </wiki:Tab>

  <wiki:Tab id="info" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "info.attachment.tab")%>' accesskey="i" >

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
    <input type="submit" name="upload" value="<fmt:message key='attach.add.submit'/>" id="upload" /> <input type="hidden" name="action"  value="upload" />
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
     <input type="submit" name="delete-all" id="delete-all"
           value="<fmt:message key='info.deleteattachment.submit' />" />
     </div>
    </form>
  </wiki:Permission>

  <%-- FIXME why not add pagination here - no need for large amounts of attach versions on one page --%>
  <h3><fmt:message key='info.attachment.history' /></h3>

  <div class="zebra-table"><div class="slimbox-img sortable">
  <table class="wikitable">
    <tr>
      <th><fmt:message key="info.attachment.type"/></th>
      <%--<th><fmt:message key="info.attachment.name"/></th>--%>
      <th><fmt:message key="info.version"/></th>
      <th><fmt:message key="info.size"/></th>
      <th><fmt:message key="info.date"/></th>
      <th><fmt:message key="info.author"/></th>
      <%--
      <wiki:Permission permission="upload">
         <th><fmt:message key="info.actions"/></th>
      </wiki:Permission>
      --%>
      <th  class='changenote'><fmt:message key="info.changenote"/></th>
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
      <td><div id="attach-<%= attachtype %>" class="attachtype"><%= attachtype %></div></td>
      <%--<td><wiki:LinkTo title="<%= name %>" ><%= sname %></wiki:LinkTo></td>--%>
      <%--FIXME classs parameter throws java exception
      <td><wiki:Link version='<%=Integer.toString(att.getVersion())%>'
                       title="<%= name %>"
                       class="attachment" ><wiki:PageVersion /></wiki:Link></td>
      --%>
      <td><a href="<wiki:Link version='<%=Integer.toString(att.getVersion())%>' format='url' />"
                       title="<%= name %>"
                       class="attachment" ><wiki:PageVersion /></a></td>
      <td style="white-space:nowrap;text-align:right;">
        <fmt:formatNumber value='<%=Double.toString(att.getSize()/1000.0) %>' groupingUsed='false' maxFractionDigits='1' minFractionDigits='1'/>&nbsp;<fmt:message key="info.kilobytes"/>
      </td>
	  <td style="white-space:nowrap;"><fmt:formatDate value="<%= att.getLastModified() %>" pattern="${prefs['DateFormat']}" /></td>
      <td><wiki:Author /></td>
      <%--
      // FIXME: This needs to be added, once we figure out what is going on.
      <wiki:Permission permission="upload">
         <td>
            <input type="button"
                   value="Restore"
                   url="<wiki:Link format='url' context='<%=WikiContext.UPLOAD%>'/>"/>
         </td>
      </wiki:Permission>
      --%>
      <td class='changenote'>
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
  </div></div>
  </wiki:Tab>

  </wiki:TabbedSection> <%-- end of .tabs --%>

</wiki:PageType>

</wiki:PageExists>

<wiki:NoSuchPage>
  <fmt:message key="common.nopage">
    <fmt:param><wiki:EditLink><fmt:message key="common.createit"/></wiki:EditLink></fmt:param>
  </fmt:message>
</wiki:NoSuchPage>
