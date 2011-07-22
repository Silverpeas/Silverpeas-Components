package com.silverpeas.scheduleevent.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.view.AvailabilityFactoryVO.Availability;

public class DisableTime implements TimeVO {
  private final static String COMPONENT_ID_SUFFIX = "disabled";
  private final static String HMTL_CLASS_ATTRIBUTE = "titreCouleur inactif";
  private DateVO parent;
  private PartOfDay partOfDay;
  private Map<ContributorVO, AvailableVO> availabilities = new HashMap<ContributorVO, AvailableVO>();
  
  public DisableTime(DateVO date) {
    parent = date;
  }

  public void setPartOfDay(PartOfDay part) {
    partOfDay = part;
  }

  @Override
  public boolean equals(TimeVO time) {
    return getId().equals(time.getId());
  }

  @Override
  public String getId() {
    return partOfDay.getPrefixId() + COMPONENT_ID_SUFFIX;
  }

  @Override
  public String getMultilangLabel() {
    return partOfDay.getMultilangLabel();
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
