/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.hyperlink.servlets;

import org.silverpeas.components.hyperlink.control.HyperlinkSessionController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.silverpeas.core.util.StringUtil.isDefined;

public class HyperlinkRequestRouter extends ComponentRequestRouter<HyperlinkSessionController> {

  private static final long serialVersionUID = -4334545961842905383L;
  // Keyword for the parsing
  private static final String USER_LOGIN = "%ST_USER_LOGIN%";
  private static final String USER_PASSWORD = "%ST_USER_PASSWORD%";
  private static final String USER_EMAIL = "%ST_USER_EMAIL%";
  private static final String USER_FIRST_NAME = "%ST_USER_FIRSTNAME%";
  private static final String USER_LAST_NAME = "%ST_USER_LASTNAME%";
  private static final String USER_ENCODED_PASSWORD = "%ST_USER_ENCODED_PASSWORD%";
  private static final String USER_FULL_NAME = "%ST_USER_FULLNAME%";
  private static final String USER_ID = "%ST_USER_ID%";
  private static final String SESSION_ID = "%ST_SESSION_ID%";
  private static final String USER_PROPERTY_PREFIX = "%ST_USER_PROPERTY_";
  private static final String ENCODING = "UTF-8";
  private static final String SSO_DEST = "/hyperlink/jsp/sso.jsp";
  private static final String CURRENT_TAB_DEST = "/hyperlink/jsp/internal.jsp";
  private static final String NEW_TAB_DEST = "/hyperlink/jsp/redirect.jsp";
  private static final String AUTHENTICATION_DEST = "/RwebConnections/jsp/Connection";
  public static final String GO_TO_URL_ACTION = "GoToURL";

  @Override
  public HyperlinkSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return new HyperlinkSessionController(mainSessionCtrl, context);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "hyperlinkScc";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param hyperlinkSCC The component Session Control, build and initialised.
   * @param request the HttpRequest
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, HyperlinkSessionController hyperlinkSCC,
      HttpRequest request) {
    final String destination;
    if (function.startsWith("Main") || function.startsWith("portlet")) {
      final String winParam = hyperlinkSCC.getComponentParameterValue("openNewWindow");
      if (StringUtil.getBooleanValue(winParam)) {
        if (hyperlinkSCC.isClientSSOWithoutDefinedConnection()) {
          return getDestination(GO_TO_URL_ACTION, hyperlinkSCC, request);
        }
        return NEW_TAB_DEST;
      }
      return getDestination(GO_TO_URL_ACTION, hyperlinkSCC, request);
    } else if (function.startsWith(GO_TO_URL_ACTION)) {
      destination = getHyperlinkTarget(request, hyperlinkSCC);
    } else {
      destination = "";
    }
    return destination;
  }

  private String getHyperlinkTarget(final HttpRequest request,
      final HyperlinkSessionController hyperlinkSCC) {
    final String destination;
    final String urlParameter = hyperlinkSCC.getURL();
    final String sso = hyperlinkSCC.getComponentParameterValue("SSO");
    final String internalLinkParameter = hyperlinkSCC.getComponentParameterValue("isInternalLink");
    boolean isInternalLink = StringUtil.getBooleanValue(internalLinkParameter);
    if (isDefined(urlParameter)) {
      final String aimedUrl = parseDestination(urlParameter, isInternalLink, request);
      if (StringUtil.getBooleanValue(sso)) {
        request.setAttribute("Login", hyperlinkSCC.getUserDetail().getLogin());
        request.setAttribute("Domain", hyperlinkSCC.getComponentParameterValue("domain"));
        final HttpSession session = request.getSession(false);
        request.setAttribute("Password", session.getAttribute("Silverpeas_pwdForHyperlink"));
        request.setAttribute("URL", aimedUrl);
        destination = SSO_DEST;
      } else if (hyperlinkSCC.isClientSSO()) {
        request.setAttribute("ComponentId", hyperlinkSCC.getComponentId());
        final String methodType = hyperlinkSCC.getMethodType();
        request.setAttribute("Method", methodType);
        destination = AUTHENTICATION_DEST;
      } else {
        final String finalAimedUrl = formatAimedUrl(hyperlinkSCC, request, aimedUrl, isInternalLink);
        request.setAttribute("IsInternalLink", isInternalLink);
        if(request.getParameterAsBoolean("fromRedirect")) {
          destination = finalAimedUrl;
        } else {
          request.setAttribute("URL", finalAimedUrl);
          destination = CURRENT_TAB_DEST;
        }
      }
    } else {
      destination = "";
    }
    return destination;
  }

  private String formatAimedUrl(final HyperlinkSessionController hyperlinkSCC,
      final HttpRequest request, final String originalAimedUrl, boolean internalLink) {
    final User user = hyperlinkSCC.getUserDetail();
    String aimedUrl = originalAimedUrl;
    aimedUrl = getParsedDestination(aimedUrl, USER_LOGIN, encode(user.getLogin()));
    aimedUrl = getParsedDestination(aimedUrl, USER_EMAIL, encode(user.geteMail()));
    aimedUrl = getParsedDestination(aimedUrl, USER_FIRST_NAME, encode(user.getFirstName()));
    aimedUrl = getParsedDestination(aimedUrl, USER_LAST_NAME, encode(user.getLastName()));
    aimedUrl = getParsedDestination(aimedUrl, USER_FULL_NAME, encode(user.getDisplayedName()));
    aimedUrl = getParsedDestination(aimedUrl, USER_ID, encode(hyperlinkSCC.getUserId()));
    aimedUrl = getParsedDestination(aimedUrl, SESSION_ID, encode(request.getSession().getId()));
    if (hyperlinkSCC.getSettings().getBoolean("PasswordKeyEnable", true)) {
      // !!!! Add the password : this is an ugly patch that use a
      // session variable set in the "AuthenticationServlet" servlet
      final HttpSession session = request.getSession(false);
      final String clearPassword = (String) session.getAttribute("Silverpeas_pwdForHyperlink");
      aimedUrl = getParsedDestination(aimedUrl, USER_PASSWORD, encode(clearPassword));
      aimedUrl = getParsedDestination(aimedUrl, USER_ENCODED_PASSWORD,encode(hyperlinkSCC.getUserFull().getPassword()));
    } else {
      aimedUrl = getParsedDestination(aimedUrl, USER_PASSWORD, encode("??????"));
    }
    aimedUrl = getParsedDestinationWithExtraInfos(aimedUrl, hyperlinkSCC);
    UriBuilder uriBuilder = UriBuilder.fromUri(aimedUrl);
    if (internalLink) {
      uriBuilder.queryParam("Referer", hyperlinkSCC.getComponentId());
    }
    return uriBuilder.build().toString();
  }

  /**
   * Parse destination to manage Silverpeas internal links
   * @param urlParameter the target URL as mentioned in instance parameter
   * @param isInternalLink flag to indicate if the URL is insternal or not
   * @param request the HTTP servlet request
   * @return the computed destination
   */
  private String parseDestination(String urlParameter, boolean isInternalLink,
      HttpServletRequest request) {
    String destination;
    // Special case : www, just add http://
    if (urlParameter.startsWith("www")) {
      destination = "http://" + urlParameter;
    } else {
      //Http://xxxx url
      if (isInternalLink) {
        //internal link : must retain only uri
        String applicationURL =
            ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
        if (applicationURL.endsWith("/")) {
          applicationURL = applicationURL.substring(0, applicationURL.length() - 1);
        }
        String requestURL = request.getRequestURL().toString();
        String pertinentURL = urlParameter.substring(urlParameter.indexOf(applicationURL));
        destination = requestURL.substring(0, requestURL.indexOf(applicationURL)) + pertinentURL;
      } else {
        // external link : nothing to do
        destination = urlParameter;
      }
    }

    return destination;
  }

  private String getParsedDestination(String sDestination, String sKeyword, String sValue) {

    String destination = sDestination;
    int nLoginIndex = sDestination.indexOf(sKeyword);
    while (nLoginIndex != -1) {
      // Replace the keyword with the actual value
      String sParsed = destination.substring(0, nLoginIndex);
      sParsed += sValue;
      if (destination.length() > nLoginIndex + sKeyword.length()) {
        sParsed += destination.substring(nLoginIndex + sKeyword.length(), destination.length());
      }
      destination = sParsed;
      nLoginIndex = destination.indexOf(sKeyword);
    }

    return destination;
  }

  private String getParsedDestinationWithExtraInfos(String sDestination,
      HyperlinkSessionController hyperlinkSC) {
    int i = sDestination.indexOf(USER_PROPERTY_PREFIX);
    String destination = sDestination;
    while (i != -1) {
      String keyword = destination.substring(i, destination.indexOf('%', i + 1) + 1);
      String property = extractPropertyName(keyword);
      destination = getParsedDestination(destination, keyword,
          encode(hyperlinkSC.getUserFull().getValue(property)));
      i = destination.indexOf(USER_PROPERTY_PREFIX);
    }
    return destination;
  }

  private String extractPropertyName(String str) {
    return str.substring(USER_PROPERTY_PREFIX.length(), str.length() - 1);
  }

  private String encode(String str) {
    if (!isDefined(str)) {
      return "";
    }
    try {
      return URLEncoder.encode(str, ENCODING);
    } catch (UnsupportedEncodingException e) {
      SilverLogger.getLogger(this).error("str = " + str, e);
    }
    return "";
  }
}