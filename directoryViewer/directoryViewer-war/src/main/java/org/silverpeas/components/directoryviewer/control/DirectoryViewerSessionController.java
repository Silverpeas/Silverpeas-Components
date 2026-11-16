package org.silverpeas.components.directoryviewer.control;

import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

public class DirectoryViewerSessionController extends AbstractComponentSessionController {
  private static final long serialVersionUID = 1L;

  public DirectoryViewerSessionController(MainSessionController main, ComponentContext context) {
    super(main, context);
  }

  public String parameter(String name) {
    String value = getComponentParameterValue(name);
    return value == null ? "" : value.trim();
  }
}
