package org.silverpeas.components.scheduleevent.view;

import org.silverpeas.components.scheduleevent.service.model.beans.Response;

import java.util.Map;
import java.util.Set;

public interface TimeVO {
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
