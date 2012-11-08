/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.stratelia.webactiv.hyperlink.servlets;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.hyperlink.control.HyperlinkSessionController;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

  @Override
  public HyperlinkSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return new HyperlinkSessionController(mainSessionCtrl, context);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   * @return 
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
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, HyperlinkSessionController hyperlinkSCC,
      HttpServletRequest request) {
    SilverTrace.info("hyperlink", "HyperlinkRequestRooter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "function = " + function);
    String destination = "";

    if (function.startsWith("Main") || function.startsWith("portlet")) {

      // Retrieves first the openNewWindow parameter
      String winParam = hyperlinkSCC.getComponentParameterValue("openNewWindow");

      // Test the parameter
      if (StringUtil.isDefined(winParam)) {
        if ("yes".equals(winParam)) {
          return "/hyperlink/jsp/redirect.jsp";
        }
        return getDestination("GoToURL", hyperlinkSCC, request);
      }
      return getDestination("GoToURL", hyperlinkSCC, request);
    } else if (function.startsWith("GoToURL")) {
      // Get the URL Parameter
      String urlParameter = hyperlinkSCC.getComponentParameterValue("URL");
      String sso = hyperlinkSCC.getComponentParameterValue("SSO");

      String internalLinkParameter = hyperlinkSCC.getComponentParameterValue("isInternalLink");
      boolean isInternalLink =
          ((StringUtil.isDefined(internalLinkParameter)) && (internalLinkParameter.equals("yes")));

      if (StringUtil.isDefined(urlParameter)) {
        destination = parseDestination(urlParameter, isInternalLink, request);
        UserDetail userDetail = hyperlinkSCC.getUserDetail();
        if ("yes".equalsIgnoreCase(sso)) {
          request.setAttribute("Login", userDetail.getLogin());
          request.setAttribute("Domain", hyperlinkSCC.getComponentParameterValue("domain"));
          HttpSession session = request.getSession(false);
          request.setAttribute("Password", session.getAttribute("Silverpeas_pwdForHyperlink"));
          request.setAttribute("URL", destination);
          return "/hyperlink/jsp/sso.jsp";
        } else if (hyperlinkSCC.isClientSSO()) {
          request.setAttribute("ComponentId", hyperlinkSCC.getComponentId());
          String methodType = hyperlinkSCC.getMethodType();
          request.setAttribute("Method", methodType);
          destination = "/RwebConnections/jsp/Connection";
        } else {
          try {
            destination = getParsedDestination(destination, USER_LOGIN, encode(userDetail.getLogin()));
            destination =
                getParsedDestination(destination, USER_EMAIL, encode(userDetail.geteMail()));
            destination =
                getParsedDestination(destination, USER_FIRST_NAME,
                    encode(userDetail.getFirstName()));
            destination =
                getParsedDestination(destination, USER_LAST_NAME, encode(userDetail.getLastName()));
            destination =
                getParsedDestination(destination, USER_FULL_NAME,
                    encode(userDetail.getDisplayedName()));
            destination =
                getParsedDestination(destination, USER_ID, encode(hyperlinkSCC.getUserId()));
            destination =
                getParsedDestination(destination, SESSION_ID,
                    encode(request.getSession().getId()));

            if (hyperlinkSCC.getSettings().getBoolean("PasswordKeyEnable", true)) {
              // !!!! Add the password : this is an ugly patch that use a
              // session variable set in the "AuthenticationServlet" servlet
              HttpSession session = request.getSession(false);
              String clearPassword = (String) session.getAttribute("Silverpeas_pwdForHyperlink");
              destination =
                  getParsedDestination(destination, USER_PASSWORD, encode(clearPassword));
              destination =
                  getParsedDestination(destination, USER_ENCODED_PASSWORD, encode(hyperlinkSCC
                      .getUserFull().getPassword()));
            } else {
              destination = getParsedDestination(destination, USER_PASSWORD, encode("??????"));
            }
            destination = getParsedDestinationWithExtraInfos(destination, hyperlinkSCC);
          } catch (UnsupportedEncodingException e) {
            SilverTrace.error("hyperlink",
                "HyperlinkRequestRooter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "destination = " + destination);
          }
        }
      } else {
        SilverTrace.error("hyperlink", "HyperlinkRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "destination = " + destination);
      }
    }
    SilverTrace.info("hyperlink", "HyperlinkRequestRooter.getDestination()",
        "root.MSG_GEN_RETURN_VALUE", "destination = " + destination);
    return destination;
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
    String destination = null;
    if (urlParameter.startsWith("www")) { // Special case : www, just add http://
      destination = "http://" + urlParameter;
    } else { //Http://xxxx url
      if (isInternalLink) { //internal link : must retain only uri
        String applicationURL = GeneralPropertiesManager.getString("ApplicationURL");
        if (applicationURL.endsWith("/")) {
          applicationURL = applicationURL.substring(0, applicationURL.length() - 1);
        }
        String requestURL = request.getRequestURL().toString();
        String pertinentURL = urlParameter.substring(urlParameter.indexOf(applicationURL));
        destination = requestURL.substring(0, requestURL.indexOf(applicationURL)) + pertinentURL;
      } else { // external link : nothing to do
        destination = urlParameter;
      }
    }

    return destination;
  }

  private String getParsedDestination(String sDestination, String sKeyword, String sValue) {
    SilverTrace.info("hyperlink", "HyperlinkRequestRooter.getParsedDestination()",
        "root.MSG_GEN_PARAM_VALUE", "sDestination = " + sDestination);
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
    SilverTrace.info("hyperlink", "HyperlinkRequestRooter.getParsedDestination()",
        "root.MSG_GEN_RETURN_VALUE", "sDestination = " + destination);
    return destination;
  }

  private String getParsedDestinationWithExtraInfos(String sDestination,
      HyperlinkSessionController hyperlinkSC) throws UnsupportedEncodingException {
    int i = sDestination.indexOf(USER_PROPERTY_PREFIX);
    String destination = sDestination;
    while (i != -1) {
      String keyword = destination.substring(i, destination.indexOf('%', i + 1) + 1);
      String property = extractPropertyName(keyword);
      destination = getParsedDestination(destination, keyword, encode(hyperlinkSC.getUserFull().getValue(property)));
      i = destination.indexOf(USER_PROPERTY_PREFIX);
    }
    return destination;
  }

  private String extractPropertyName(String str) {
    return str.substring(USER_PROPERTY_PREFIX.length(), str.length() - 1);
  }
  
  private String encode(String str) {
    if (!StringUtil.isDefined(str)) {
      return "";
    }
    try {
      return URLEncoder.encode(str, ENCODING);
    } catch (UnsupportedEncodingException e) {
      SilverTrace.error("hyperlink", "HyperlinkRequestRooter.encode()",
          "root.CANT_ENCODE_STRING", "str = " + str, e);
    }
    return "";
  }
}