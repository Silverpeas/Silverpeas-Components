/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ecyrd.jspwiki.url;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.ecyrd.jspwiki.TextUtil;
import com.ecyrd.jspwiki.WikiContext;
import com.silverpeas.wiki.control.WikiMultiInstanceManager;

/**
 * Implements the Silverpeas URL constructor using links directly to the JSP pages.
 * @author X.Delorme
 */
public class SilverpeasURLConstructor extends DefaultURLConstructor {

  /**
   * Constructs the actual URL based on the context.
   */
  private String makeURL(String context, String name, boolean absolute) {
    return doReplacementWithComponentId(getURLPattern(context, name), name,
        absolute);
  }

  /**
   * Constructs the URL with a bunch of parameters.
   * @param parameters If null or empty, no parameters are added. {@inheritDoc}
   */
  public String makeURL(String context, String name, boolean absolute,
      String parameters) {
    if (parameters != null && parameters.length() > 0) {
      if (context.equals(WikiContext.ATTACH)) {
        parameters = "?" + parameters;
      } else if (context.equals(WikiContext.NONE)) {
        parameters = (name.indexOf('?') != -1) ? "&amp;" : "?" + parameters;
      } else {
        parameters = "&amp;" + parameters;
      }
    } else {
      parameters = "";
    }
    return makeURL(context, name, absolute) + parameters;
  }

  protected final String doReplacementWithComponentId(String baseptrn,
      String name, boolean absolute) {
    String baseurl = m_pathPrefix + WikiMultiInstanceManager.getComponentId()
        + '/';
    if (absolute) {
      baseurl = m_engine.getBaseURL()
          + WikiMultiInstanceManager.getComponentId() + '/';
    }
    baseptrn = TextUtil.replaceString(baseptrn, "%u", baseurl);
    baseptrn = TextUtil.replaceString(baseptrn, "%U", m_engine.getBaseURL()
        + WikiMultiInstanceManager.getComponentId() + '/');
    baseptrn = TextUtil.replaceString(baseptrn, "%n",
        encodeURIWithPercent(name));
    baseptrn = TextUtil.replaceString(baseptrn, "%p", m_pathPrefix
        + WikiMultiInstanceManager.getComponentId() + '/');
    return baseptrn;
  }

  /**
   * URLEncoder returns pluses, when we want to have the percent encoding. See
   * http://issues.apache.org/bugzilla/show_bug.cgi?id=39278 for more info. We also convert any
   * %2F's back to slashes to make nicer-looking URLs.
   */
  private final String encodeURIWithPercent(String uri) {
    uri = m_engine.encodeName(uri);
    uri = StringUtils.replace(uri, "+", "%20");
    uri = StringUtils.replace(uri, "%2F", "/");
    return uri;
  }

  /**
   * Should parse the "page" parameter from the actual request. {@inheritDoc}
   */
  public String parsePage(String context, HttpServletRequest request,
      String encoding) throws UnsupportedEncodingException {
    request.setCharacterEncoding(encoding);
    String pagereq = request.getParameter("page");
    if (context.equals(WikiContext.ATTACH)) {
      pagereq = parsePageFromURL(request, encoding);
    }
    return pagereq;
  }

  /**
   * This method is not needed for the DefaultURLConstructor.
   * @param request The HTTP Request that was used to end up in this page.
   * @return "Wiki.jsp", "PageInfo.jsp", etc. Just return the name, JSPWiki will figure out the
   * page.
   */
  public String getForwardPage(HttpServletRequest request) {
    String basePath = request.getPathInfo();
    return "wiki" + basePath;
  }
}
