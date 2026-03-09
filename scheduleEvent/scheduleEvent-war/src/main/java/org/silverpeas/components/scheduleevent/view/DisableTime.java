package org.silverpeas.components.scheduleevent.view;

import org.silverpeas.components.scheduleevent.service.model.beans.Response;
import org.silverpeas.components.scheduleevent.view.AvailabilityFactoryVO.Availability;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DisableTime extends AbstractTimeVO {
  private static final String COMPONENT_ID_SUFFIX = "disabled";
  private static final String HTML_CLASS_ATTRIBUTE = "titreCouleur inactif";
  private final DateVO parent;
  private final Map<ContributorVO, AvailableVO> availabilities = new HashMap<>();

  public DisableTime(DateVO date) {
    parent = date;
  }

  @Override
  public String getId() {
    return getPartOfDay().getPrefixId() + COMPONENT_ID_SUFFIX;
  }

  @Override
  public String getHtmlClassAttribute() {
    return HTML_CLASS_ATTRIBUTE;
  }

  @Override
  public Map<ContributorVO, AvailableVO> getAvailabilities() {
    return Collections.unmodifiableMap(availabilities);
  }

  private AvailableVO getDisabledAvailability(ContributorVO contributor) {
    return contributor.makeAvailability(Availability.DISABLE);
  }

  @Override
  public Set<Response> match(Set<Response> responses) {
    return new HashSet<>(0);
  }

  @Override
  public void addAvailability(ContributorVO contributor, AvailableVO availability) {
    availabilities.put(contributor, getDisabledAvailability(contributor));
  }

  @Override
  public AnswerVO getPresents() {
    return DisableAnswer.getInstance();
  }

  @Override
  public AvailabilityVisitorPresenceCounter getPresentsCount() {
    return new AvailabilityVisitorPresenceCounter();
  }

  @Override
  public DateVO getDate() {
    return parent;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
