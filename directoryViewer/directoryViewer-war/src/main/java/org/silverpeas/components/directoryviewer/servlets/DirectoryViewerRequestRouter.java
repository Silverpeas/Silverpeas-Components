package org.silverpeas.components.directoryviewer.servlets;

import org.silverpeas.components.directoryviewer.control.DirectoryViewerSessionController;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DirectoryViewerRequestRouter
    extends ComponentRequestRouter<DirectoryViewerSessionController> {
  private static final long serialVersionUID = 1L;

  @Override
  public String getSessionControlBeanName() {
    return "directoryViewer";
  }

  @Override
  public DirectoryViewerSessionController createComponentSessionController(
      MainSessionController main, ComponentContext context) {
    return new DirectoryViewerSessionController(main, context);
  }

  @Override
  public String getDestination(String function, DirectoryViewerSessionController controller,
      HttpRequest request) {
    if (!function.startsWith("Main") && !function.startsWith("portlet")) {
      return "/Rdirectory/jsp/" + function;
    }
    List<String> parameters = new ArrayList<>();
    // The native directory accepts one primary scope at a time.
    String[] scopes = {"GroupIds", "SpaceId", "DomainIds", "ComponentId"};
    for (String scope : scopes) {
      if (!controller.parameter(scope).isEmpty()) {
        add(parameters, scope, controller.parameter(scope));
        break;
      }
    }
    for (String name : new String[]{"InWholeSpaceTree", "Sort", "View",
        "DoNotUseContacts", "DoNotUseExtraForm"}) {
      if (!controller.parameter(name).isEmpty()) {
        add(parameters, name, controller.parameter(name));
      }
    }
    return "/Rdirectory/jsp/Main" + (parameters.isEmpty() ? "" :
        "?" + String.join("&", parameters));
  }

  private void add(List<String> parameters, String name, String value) {
    parameters.add(name + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
  }
}
