package com.silverpeas.scheduleevent.view;

import java.util.Set;

import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.view.AvailabilityFactoryVO.Availability;

public interface ContributorVO {
  String getName();

  String getHtmlClassAttribute();

  boolean hasAnswered();

  Set<Response> match(Set<Response> responses);

  AvailableVO makeAvailabilty(Availability availability);

}
