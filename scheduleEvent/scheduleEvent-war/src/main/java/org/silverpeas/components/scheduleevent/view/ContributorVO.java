package org.silverpeas.components.scheduleevent.view;

import java.util.Set;

import org.silverpeas.components.scheduleevent.service.model.beans.Response;
import org.silverpeas.components.scheduleevent.view.AvailabilityFactoryVO.Availability;
import org.silverpeas.components.scheduleevent.service.model.beans.Response;

public interface ContributorVO {
  String getName();

  String getHtmlClassAttribute();

  boolean hasAnswered();

  Set<Response> match(Set<Response> responses);

  AvailableVO makeAvailabilty(Availability availability);

}
