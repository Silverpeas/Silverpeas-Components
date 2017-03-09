package org.silverpeas.components.scheduleevent.view;

import org.silverpeas.components.scheduleevent.service.model.beans.Response;
import org.silverpeas.components.scheduleevent.view.AvailabilityFactoryVO.Availability;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DisableTime extends AbstractTimeVO {
  private final static String COMPONENT_ID_SUFFIX = "disabled";
  private final static String HMTL_CLASS_ATTRIBUTE = "titreCouleur inactif";
  private DateVO parent;
  private Map<ContributorVO, AvailableVO> availabilities = new HashMap<ContributorVO, AvailableVO>();

  public DisableTime(DateVO date) {
    parent = date;
  }

  @Override
  public String getId() {
    return getPartOfDay().getPrefixId() + COMPONENT_ID_SUFFIX;
  }

  @Override
  public String getHtmlClassAttribute() {
    return HMTL_CLASS_ATTRIBUTE;
  }

  @Override
  public Map<ContributorVO, AvailableVO> getAvailabilities() {
    return Collections.unmodifiableMap(availabilities);
  }

  private AvailableVO getDisabledAvaibility(ContributorVO contributor) {
    return contributor.makeAvailabilty(Availability.DISABLE);
  }

  @Override
  public Set<Response> match(Set<Response> responses) {
    return new HashSet<Response>(0);
  }

  @Override
  public void addAvailability(ContributorVO contributor, AvailableVO availability) {
    availabilities.put(contributor, getDisabledAvaibility(contributor));
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
}
