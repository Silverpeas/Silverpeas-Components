<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@ page errorPage="/Error.jsp" %>
<%@ page import="java.util.*" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.*" %>
<%@ page import="com.ecyrd.jspwiki.preferences.*" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<view:setBundle basename="templates.default"/>
<%
  /* see commonheader.jsp */
  String prefDateFormat = Preferences.getPreference(pageContext,"DateFormat");
  String prefTimeZone   = Preferences.getPreference(pageContext,"TimeZone");

  WikiContext c = WikiContext.findContext( pageContext );
  pageContext.setAttribute( "skins", c.getEngine().getTemplateManager().listSkins(pageContext, c.getTemplate() ) );
%>

<h3><fmt:message key="prefs.heading"><fmt:param><wiki:Variable var="applicationname"/></fmt:param></fmt:message></h3>

<c:if test="${param.tab eq 'prefs'}" >
  <div class="formhelp">
    <wiki:Messages div="error" topic="prefs" prefix='<%=LocaleSupport.getLocalizedMessage(pageContext,"prefs.errorprefix.prefs")%>'/>
  </div>
</c:if>

<form action="<wiki:Link jsp='UserPreferences.jsp' format='url'><wiki:Param name='tab' value='prefs'/></wiki:Link>" 
       class="wikiform" 
          id="setCookie"
      method="post" accept-charset="<wiki:ContentEncoding />"
    onsubmit="Wiki.savePrefs(); return Wiki.submitOnce(this);" >
<table>

  <tr>
  <td><label for="assertedName"><fmt:message key="prefs.assertedname"/></label></td>
  <td> 
  <input type="text" id="assertedName" name="assertedName" size="20" value="<wiki:UserProfile property='wikiname' />" />
  <%-- CHECK THIS
  <input type="text" id="assertedName" name="assertedName" size="20" value="<wiki:UserProfile property='loginname'/>" />
  --%>
  </td>
  </tr>
  <wiki:UserCheck status="anonymous">
  <tr>
  <td>&nbsp;</td>
  <td>
  <div class="formhelp">
    <fmt:message key="prefs.assertedname.description">
      <fmt:param><wiki:Variable var="applicationname" /></fmt:param>
      <fmt:param>
        <a href="<wiki:Link jsp='Login.jsp' format='url'><wiki:Param name='tab' value='register'/></wiki:Link>">
          <fmt:message key="prefs.assertedname.create"/>
        </a>
      </fmt:param>
    </fmt:message>
  </div>
  </td>
  </tr>
  </wiki:UserCheck>

  <tr>
  <td><label for="editor"><fmt:message key="edit.chooseeditor"/></label></td>
  <td>
    <select id="editor" name="editor">
      <wiki:EditorIterator id="edt">
        <option <%=edt.isSelected()%> value="<%=edt.getName()%>"><%=edt.getName()%></option>
      </wiki:EditorIterator>
  </select>
  </td>
  </tr>
  
  <tr>
  <td><label for="prefSkin"><fmt:message key="prefs.user.skin"/></label></td>
  <td>
  <select id="prefSkin" name="prefSkin">
    <c:forEach items="${skins}" var="i">
      <option value='<c:out value='${i}'/>' <c:if test='${i == prefs["SkinName"]}'>selected="selected"</c:if> ><c:out value="${i}"/></option>
    </c:forEach>
  </select>
  </td>
  </tr>

  <tr>
  <td><label for="prefOrientation"><fmt:message key="prefs.user.orientation"/></label></td>
  <td>
  <select id="prefOrientation" name="prefOrientation" onchange="Wiki.changeOrientation();">
      <option value='fav-left' <c:if test='${"fav-left" == prefs["orientation"]}'>selected="selected"</c:if> ><fmt:message key="prefs.user.orientation.left"/></option>
      <option value='fav-right' <c:if test='${"fav-right" == prefs["orientation"]}'>selected="selected"</c:if> ><fmt:message key="prefs.user.orientation.right"/></option>
      <%--
      <option value='fav-hidden' <c:if test='${"fav-hidden" == prefs["orientation"]}'>selected="selected"</c:if> ><fmt:message key="prefs.user.fav-hide"/></option>
      --%>
  </select>
  </td>
  </tr>

  <tr>
  <td><label for="prefTimeFormat"><fmt:message key="prefs.user.timeformat"/></label></td>
  <td>
  <select id="prefTimeFormat" name="prefTimeFormat" >
    <%
      Properties props = c.getEngine().getWikiProperties();
      ArrayList tfArr = new ArrayList(40);

     /* filter timeformat props */
      for( Enumeration e = props.propertyNames(); e.hasMoreElements(); )
      {
          String name = (String) e.nextElement();
          if( name.startsWith( "jspwiki.defaultprefs.timeformat." ) )
          {
			 tfArr.add(name);
          }
      }

      /* fetch actual formats */
      if( tfArr.size() == 0 )
      {
          tfArr.add( "dd-MMM-yy" );
          tfArr.add( "d-MMM-yyyy" );
          tfArr.add( "EEE, dd-MMM-yyyy, zzzz" );
      } else {
          Collections.sort( tfArr );
          for( int i=0; i < tfArr.size(); i++ )
          {
            tfArr.set(i, props.getProperty( (String)tfArr.get(i) ) );
          }
      }

      Date d = new Date() ;  // Now.

      for( int i=0; i < tfArr.size(); i++ )
      {
        String f = (String)tfArr.get(i);
        String selected = ( prefDateFormat.equals( f ) ? " selected='selected'" : "" ) ;
        try
        {
          java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat( f );
          java.util.TimeZone tz = java.util.TimeZone.getDefault();
          try 
          {
            tz.setRawOffset( Integer.parseInt( prefTimeZone ) );
          }
          catch( Exception e) { /* dont care */ } ;
          fmt.setTimeZone( tz );
    %>
          <option value="<%= f %>" <%= selected%> ><%= fmt.format(d) %></option>
   <%
        }
        catch( IllegalArgumentException e ) { } // skip parameter
      }
    %>
  </select>
  </td>
  </tr>

  <tr>
  <td><label for="prefTimeZone"><fmt:message key="prefs.user.timezone"/></label></td>
  <td>
  <select id='prefTimeZone' name='prefTimeZone' class='select'>
    <% 
       String[][] tzs = 
       { { "-43200000" , "(UTC-12) Enitwetok, Kwajalien" }
       , { "-39600000" , "(UTC-11) Nome, Midway Island, Samoa" }
       , { "-36000000" , "(UTC-10) Hawaii" }
       , { "-32400000" , "(UTC-9) Alaska" }
       , { "-28800000" , "(UTC-8) Pacific Time" }
       , { "-25200000" , "(UTC-7) Mountain Time" }
       , { "-21600000" , "(UTC-6) Central Time, Mexico City" }
       , { "-18000000" , "(UTC-5) Eastern Time, Bogota, Lima, Quito" }
       , { "-14400000" , "(UTC-4) Atlantic Time, Caracas, La Paz" }
       , { "-12600000" , "(UTC-3:30) Newfoundland" }
       , { "-10800000" , "(UTC-3) Brazil, Buenos Aires, Georgetown, Falkland Is." }
       , {  "-7200000" , "(UTC-2) Mid-Atlantic, Ascention Is., St Helena" }
       , {  "-3600000" , "(UTC-1) Azores, Cape Verde Islands" }
       , {         "0" , "(UTC) Casablanca, Dublin, Edinburgh, London, Lisbon, Monrovia" }
       , {   "3600000" , "(UTC+1) Berlin, Brussels, Copenhagen, Madrid, Paris, Rome" }
       , {   "7200000" , "(UTC+2) Helsinki, Athens, Kaliningrad, South Africa, Warsaw" }
       , {  "10800000" , "(UTC+3) Baghdad, Riyadh, Moscow, Nairobi" }
       , {  "12600000" , "(UTC+3.30) Tehran" }
       , {  "14400000" , "(UTC+4) Adu Dhabi, Baku, Muscat, Tbilisi" }
       , {  "16200000" , "(UTC+4:30) Kabul" }
       , {  "18000000" , "(UTC+5) Islamabad, Karachi, Tashkent" }
       , {  "19800000" , "(UTC+5:30) Bombay, Calcutta, Madras, New Delhi" }
       , {  "21600000" , "(UTC+6) Almaty, Colomba, Dhakra" }
       , {  "25200000" , "(UTC+7) Bangkok, Hanoi, Jakarta" }
       , {  "28800000" , "(UTC+8) Beijing, Hong Kong, Perth, Singapore, Taipei" }
       , {  "32400000" , "(UTC+9) Osaka, Sapporo, Seoul, Tokyo, Yakutsk" }
       , {  "34200000" , "(UTC+9:30) Adelaide, Darwin" }
       , {  "36000000" , "(UTC+10) Melbourne, Papua New Guinea, Sydney, Vladivostok" }
       , {  "39600000" , "(UTC+11) Magadan, New Caledonia, Solomon Islands" }
       , {  "43200000" , "(UTC+12) Auckland, Wellington, Fiji, Marshall Island" }
       };
       String servertz = Integer.toString( java.util.TimeZone.getDefault().getRawOffset() ) ;
       String selectedtz = servertz;
       for( int i=0; i < tzs.length; i++ )
       {
         if( prefTimeZone.equals( tzs[i][0] ) ) selectedtz = prefTimeZone;
       }
       for( int i=0; i < tzs.length; i++ )
       {
         String selected = ( selectedtz.equals( tzs[i][0] ) ? " selected='selected'" : "" ) ;
         String server = ( servertz.equals( tzs[i][0] ) ? " [SERVER]" : "" ) ;
    %>
        <option value="<%= tzs[i][0] %>" <%= selected%> ><%= tzs[i][1]+server %></option>
   <%
       }
    %>    
  </select>
  </td>
  </tr>

  <%-- user browser language only ;  why not allow to choose from all installed server languages on jspwiki ??   
  <tr>
  <td><label for="prefLanguage">Select Language</label></td>
  <td>
  <select id="prefLanguage" name="prefLanguage" >
    <option value="">English</option>
  </select>
  </td>
  </tr>
  
  <tr>
  <td><label for="prefShowQuickLinks">Show Quick Links</label></td>
  <td>
  <input class='checkbox' type='checkbox' id='prefShowQuickLinks' name='prefShowQuickLinks' 
         <%= (prefShowQuickLinks.equals("yes") ? "checked='checked'" : "") %> />
         <span class="quicklinks"><span 
               class='quick2Top'><a href='#wikibody' title='Go to Top' >&laquo;</a></span><span 
               class='quick2Prev'><a href='#' title='Go to Previous Section'>&lsaquo;</a></span><span 
               class='quick2Edit'><a href='#' title='Edit this section'>&bull;</a></span><span 
               class='quick2Next'><a href='#' title='Go to Next Section'>&rsaquo;</a></span><span 
               class='quick2Bottom'><a href='#footer' title='Go to Bottom' >&raquo;</a></span></span>
  </td>
  </tr>

  <tr>
  <td><label for="prefShowCalendar">Show Calendar</label></td>
  <td>
    <input class='checkbox' type='checkbox' id='prefShowCalendar' name='prefShowCalendar' 
            <%= (prefShowCalendar.equals("yes") ? "checked='checked'": "") %> >
  </td>
  </tr>
  --%>
 <tr>
  <td>&nbsp;</td>
  <td>
    <input type="submit" name="ok" value="<fmt:message key='prefs.save.prefs.submit'/>" 
      accesskey="s" />
    <input type="hidden" name="redirect" value="<wiki:Variable var='redirect' default='' />" />
    <input type="hidden" name="action" value="setAssertedName" />
    <div class="formhelp"><fmt:message key='prefs.cookies'/></div>
  </td>
  </tr>

</table>
</form>
  
<!-- Clearing the 'asserted name' and other prefs in the cookie -->
<%--wiki:UserCheck status="asserted"--%>

<h3><fmt:message key='prefs.clear.heading'/></h3>

<form action="<wiki:Link format='url' jsp='UserPreferences.jsp'><wiki:Param name='tab' value='prefs'/></wiki:Link>"
          id="clearCookie"
    onsubmit="Wiki.prefs.empty(); return Wiki.submitOnce( this );" 
      method="post" accept-charset="<wiki:ContentEncoding />" >
  <div>
  <input type="submit" name="ok" value="<fmt:message key='prefs.clear.submit'/>" />
  <input type="hidden" name="action" value="clearAssertedName" />
  </div>
  <div class="formhelp"><fmt:message key="prefs.clear.description" /></div>

</form>
<%--/wiki:UserCheck--%>
