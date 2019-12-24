package org.silverpeas.components.kmelia.servlets.ajax.handlers;

import org.silverpeas.components.kmelia.KmeliaConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

public class AbstractBindingToPubliHandler {

  protected Set<String> getLinksFromSession(HttpServletRequest request) {
    @SuppressWarnings("unchecked")
    Set<String> list = (Set<String>) request.getSession().getAttribute(
        KmeliaConstants.PUB_TO_LINK_SESSION_KEY);
    if (list == null) {
      list = new HashSet<>(0);
      request.getSession().setAttribute(KmeliaConstants.PUB_TO_LINK_SESSION_KEY, list);
    }
    return list;
  }
}