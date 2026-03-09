package org.silverpeas.components.scheduleevent.view;

import org.silverpeas.components.scheduleevent.service.model.beans.Response;
import org.silverpeas.components.scheduleevent.view.AvailabilityFactoryVO.Availability;

import java.util.Set;

public interface ContributorVO {
  String getName();

  String getHtmlClassAttribute();

  boolean hasAnswered();

  Set<Response> match(Set<Response> responses);

  AvailableVO makeAvailability(Availability availability);

}

