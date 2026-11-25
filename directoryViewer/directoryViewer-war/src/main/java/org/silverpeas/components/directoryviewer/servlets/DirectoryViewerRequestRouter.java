/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
import java.util.function.Function;

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
    return destination(function, controller::parameter);
  }

  static String destination(String function, Function<String, String> parameter) {
    if (!function.startsWith("Main") && !function.startsWith("portlet")) {
      return "/Rdirectory/jsp/" + function;
    }
    List<String> parameters = new ArrayList<>();
    // The native directory accepts one primary scope at a time.
    String[] scopes = {"GroupIds", "SpaceId", "DomainIds", "ComponentId"};
    for (String scope : scopes) {
      String value = parameter.apply(scope);
      if (value != null && !value.isBlank()) {
        add(parameters, scope, value.trim());
        break;
      }
    }
    for (String name : new String[]{"InWholeSpaceTree", "Sort", "View",
        "DoNotUseContacts", "DoNotUseExtraForm"}) {
      String value = parameter.apply(name);
      if (value != null && !value.isBlank()) {
        add(parameters, name, value.trim());
      }
    }
    return "/Rdirectory/jsp/Main" + (parameters.isEmpty() ? "" :
        "?" + String.join("&", parameters));
  }

  private static void add(List<String> parameters, String name, String value) {
    parameters.add(name + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
  }
}
