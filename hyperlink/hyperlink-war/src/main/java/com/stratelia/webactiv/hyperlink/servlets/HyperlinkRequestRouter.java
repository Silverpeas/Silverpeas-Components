package com.stratelia.webactiv.hyperlink.servlets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.hyperlink.control.HyperlinkSessionController;

public class HyperlinkRequestRouter extends ComponentRequestRouter {

  // Keyword for the parsing
  static private String s_sUserLogin = "%ST_USER_LOGIN%";
  static private String s_sUserPassword = "%ST_USER_PASSWORD%";
  static private String s_sUserEmail = "%ST_USER_EMAIL%";
  static private String s_sUserFirstName = "%ST_USER_FIRSTNAME%";
  static private String s_sUserLastName = "%ST_USER_LASTNAME%";
  static private String s_sUserEncodedPassword = "%ST_USER_ENCODED_PASSWORD%";
  static private String s_sUserFullName = "%ST_USER_FULLNAME%";
  static private String s_sUserId = "%ST_USER_ID%";
  static private String s_sSessionId = "%ST_SESSION_ID%";
  static private String s_sUserPropertyPrefix = "%ST_USER_PROPERTY_";

  static private String ENCODING = "ISO-8859-1";

  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    ComponentSessionController component = (ComponentSessionController) new HyperlinkSessionController(
        mainSessionCtrl, context);
    return component;
  }

  /**
   * This method has to be implemented in the component request rooter class.
   * returns the session control bean name to be put in the request object ex :
   * for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "hyperlinkScc";
  }

  /**
   * This method has to be implemented by the component request rooter it has to
   * compute a destination page
   * 
   * @param function
   *          The entering request function (ex : "Main.jsp")
   * @param componentSC
   *          The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   *         "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {

    SilverTrace.info("hyperlink", "HyperlinkRequestRooter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "function = " + function);

    String destination = "";
    HyperlinkSessionController hyperlinkSCC = (HyperlinkSessionController) componentSC;

    if (function.startsWith("Main") || function.startsWith("portlet")) {

      // Retrieves first the openNewWindow parameter
      String winParam = hyperlinkSCC
          .getComponentParameterValue("openNewWindow");

      // Test the parameter
      if (StringUtil.isDefined(winParam)) {
        if (winParam.equals("yes"))
          return "/hyperlink/jsp/redirect.jsp";
        else
          return getDestination("GoToURL", componentSC, request);
      } else
        return getDestination("GoToURL", componentSC, request);
    } else if (function.startsWith("GoToURL")) {
      // Get the URL Parameter
      String urlParameter = hyperlinkSCC.getComponentParameterValue("URL");
      String sso = hyperlinkSCC.getComponentParameterValue("SSO");

      if (urlParameter != null && !urlParameter.equals("")) {
        if (urlParameter.startsWith("www"))
          destination = "http://" + urlParameter;
        else
          destination = urlParameter;

        if ("yes".equalsIgnoreCase(sso)) {
          request
              .setAttribute("Login", hyperlinkSCC.getUserDetail().getLogin());
          request.setAttribute("Domain", hyperlinkSCC
              .getComponentParameterValue("domain"));

          HttpSession session = request.getSession(false);
          request.setAttribute("Password", (String) session
              .getAttribute("Silverpeas_pwdForHyperlink"));

          request.setAttribute("URL", destination);

          return "/hyperlink/jsp/sso.jsp";
        } else {
          try {
            destination = this.getParsedDestination(destination, s_sUserLogin,
                URLEncoder.encode(hyperlinkSCC.getUserDetail().getLogin(),
                    ENCODING));
            destination = this.getParsedDestination(destination, s_sUserEmail,
                URLEncoder.encode(hyperlinkSCC.getUserDetail().geteMail(),
                    ENCODING));
            destination = this.getParsedDestination(destination,
                s_sUserFirstName, URLEncoder.encode(hyperlinkSCC
                    .getUserDetail().getFirstName(), ENCODING));
            destination = this.getParsedDestination(destination,
                s_sUserLastName, URLEncoder.encode(hyperlinkSCC.getUserDetail()
                    .getLastName(), ENCODING));
            destination = this.getParsedDestination(destination,
                s_sUserFullName, URLEncoder.encode(hyperlinkSCC.getUserDetail()
                    .getDisplayedName(), ENCODING));
            destination = this.getParsedDestination(destination, s_sUserId,
                URLEncoder.encode(hyperlinkSCC.getUserId(), ENCODING));
            destination = this.getParsedDestination(destination, s_sSessionId,
                URLEncoder.encode(request.getSession().getId(), ENCODING));

            if ("true".equalsIgnoreCase(hyperlinkSCC.getSettings().getString(
                "PasswordKeyEnable", "true"))) {
              // !!!! Add the password : this is an uggly patch that use a
              // session variable set in the "AuthenticationServlet" servlet
              HttpSession session = request.getSession(false);
              destination = this.getParsedDestination(destination,
                  s_sUserPassword, URLEncoder.encode((String) session
                      .getAttribute("Silverpeas_pwdForHyperlink"), ENCODING));
              destination = this.getParsedDestination(destination,
                  s_sUserEncodedPassword, URLEncoder.encode(hyperlinkSCC
                      .getUserFull().getPassword(), ENCODING));
            } else {
              destination = this.getParsedDestination(destination,
                  s_sUserPassword, URLEncoder.encode("??????", ENCODING));
            }

            destination = getParsedDestinationWithExtraInfos(destination,
                hyperlinkSCC);
          } catch (UnsupportedEncodingException e) {
            SilverTrace.error("hyperlink",
                "HyperlinkRequestRooter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "destination = " + destination);
          }
        }
      } else
        SilverTrace.error("hyperlink",
            "HyperlinkRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "destination = " + destination);
    }

    SilverTrace.info("hyperlink", "HyperlinkRequestRooter.getDestination()",
        "root.MSG_GEN_RETURN_VALUE", "destination = " + destination);
    return destination;
  }

  private String getParsedDestination(String sDestination, String sKeyword,
      String sValue) {
    SilverTrace.info("hyperlink",
        "HyperlinkRequestRooter.getParsedDestination()",
        "root.MSG_GEN_PARAM_VALUE", "sDestination = " + sDestination);

    int nLoginIndex = sDestination.indexOf(sKeyword);
    if (nLoginIndex != -1) {
      // Replace the keyword with the actual value
      String sParsed = sDestination.substring(0, nLoginIndex);
      sParsed += sValue;
      if (sDestination.length() > nLoginIndex + sKeyword.length())
        sParsed += sDestination.substring(nLoginIndex + sKeyword.length(),
            sDestination.length());
      sDestination = sParsed;
    }
    SilverTrace.info("hyperlink",
        "HyperlinkRequestRooter.getParsedDestination()",
        "root.MSG_GEN_RETURN_VALUE", "sDestination = " + sDestination);
    return sDestination;
  }

  private String getParsedDestinationWithExtraInfos(String sDestination,
      HyperlinkSessionController hyperlinkSC)
      throws UnsupportedEncodingException {
    int i = sDestination.indexOf(s_sUserPropertyPrefix);
    while (i != -1) {
      String keyword = sDestination.substring(i, sDestination.indexOf("%",
          i + 1) + 1);
      String property = extractPropertyName(keyword);

      sDestination = getParsedDestination(sDestination, keyword, URLEncoder
          .encode(hyperlinkSC.getUserFull().getValue(property), ENCODING));

      i = sDestination.indexOf(s_sUserPropertyPrefix);
    }
    return sDestination;
  }

  private String extractPropertyName(String str) {
    return str.substring(s_sUserPropertyPrefix.length(), str.length() - 1);
  }
}