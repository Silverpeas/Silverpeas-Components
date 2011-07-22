package com.silverpeas.scheduleevent.view;

import java.util.Map;
import java.util.Set;

import com.silverpeas.scheduleevent.service.model.beans.Response;

public interface TimeVO {

  boolean equals(TimeVO time);
  DateVO getDate();
  String getId();
  String getMultilangLabel();
  String getHtmlClassAttribute();
  Map<ContributorVO, AvailableVO> getAvailabilities();
  Set<Response> match(Set<Response> responses);
  void addAvailability(ContributorVO contributor, AvailableVO availability);
  AnswerVO getPresents();
  AvailabilityVisitorPresenceCounter getPresentsCount();
}
