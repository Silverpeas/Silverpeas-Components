package com.silverpeas.whitePages.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.stratelia.silverpeas.peasCore.URLManager;

/**
 * Le filtre LoginFilter a pour effet de contrôler que l'utilisateur courant n'a
 * pas une fiche à remplir dans une instance de whitePages. Si c'est le cas, 2
 * attributs sont mis en sessions : - RedirectToComponentId : componentId de
 * l'instance pour que le mecanisme de redirection le renvoie sur le composant -
 * - forceCardCreation : componentId de l'instance
 * 
 * Le filtre RequestRouterFilter verifie la présence
 * 
 * @author Ludovic Bertin
 * 
 */
public class ComponentRequestRouterFilter implements Filter {

  /**
   * Configuration du filtre, permettant de récupérer les paramètres.
   */
  FilterConfig config = null;

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   * javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    HttpSession session = ((HttpServletRequest) request).getSession(true);

    HttpServletRequest hsRequest = (HttpServletRequest) request;
    String sURI = hsRequest.getRequestURI();

    if (sURI.startsWith(URLManager.getApplicationURL() + "/R")) {
      /*
       * Retrieve main session controller
       */
      String componentId = (String) session
          .getAttribute(LoginFilter.ATTRIBUTE_FORCE_CARD_CREATION);
      // String sServletPath = hsRequest.getServletPath();
      // String sPathInfo = hsRequest.getPathInfo();
      String sRequestURL = hsRequest.getRequestURL().toString();

      /*
       * If a user must be redirected to a card creation, just do it.
       */
      if ((sRequestURL.indexOf("RpdcClassify") == -1)
          && (sRequestURL.indexOf("RwhitePages") == -1)
          && (sRequestURL.indexOf("Rclipboard") == -1)
          && (sRequestURL.indexOf("importCalendar") == -1)
          && (componentId != null)) {
        StringBuffer redirectURL = new StringBuffer();
        redirectURL.append(URLManager.getURL(null, componentId));
        redirectURL.append("ForceCardCreation");
        RequestDispatcher dispatcher = request.getRequestDispatcher(redirectURL
            .toString());
        dispatcher.forward(request, response);
      } else {
        chain.doFilter(request, response);
      }
    } else {
      chain.doFilter(request, response);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#getFilterConfig()
   */
  public FilterConfig getFilterConfig() {
    return config;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Filter#setFilterConfig(javax.servlet.FilterConfig)
   */
  public void setFilterConfig(FilterConfig arg0) {
    // this.config = config;
  }

  public void init(FilterConfig arg0) {
    // this.config = config;
  }

  public void destroy() {

  }
}
