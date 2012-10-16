package com.silverpeas.scheduleevent.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.silverpeas.scheduleevent.service.model.beans.DateOption;
import com.silverpeas.scheduleevent.service.model.beans.Response;

public class HalfDayTime implements TimeVO {
  private final static String HMTL_CLASS_ATTRIBUTE = "titreCouleur";

  private DateVO parent;
  private DateOption date;
  private PartOfDay partOfDay;
  private Map<ContributorVO, AvailableVO> availabilities = new HashMap<ContributorVO, AvailableVO>();
  
  public HalfDayTime(DateVO parent, DateOption date) {
    this.parent = parent;
    this.date = date;
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
    return date.getId();
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

  public void addContributorAvailibility(ContributorVO contributor, AvailableVO availability) {
    availabilities.put(contributor, availability);
  }

  @Override
  public Set<Response> match(Set<Response> responses) {
    Set<Response> match = new HashSet<Response>();
    for(Response response: responses) {
      if (isMatchedDateId(response.getOptionId())) {
        match.add(response);
      }
    }
    return match;
  }

  private boolean isMatchedDateId(String optionId) {
    return optionId.equals(date.getId());
  }

  @Override
  public void addAvailability(ContributorVO contributor, AvailableVO availability) {
    availabilities.put(contributor, availability);
  }

  @Override
  public AnswerVO getPresents() {
    return new PresentAnswer(getPresentsCount());
  }

  @Override
  public AvailabilityVisitorPresenceCounter getPresentsCount() {
    AvailabilityVisitorPresenceCounter presenceCounter = new AvailabilityVisitorPresenceCounter();
    for(AvailableVO availability: getAvailabilities().values()) {
      availability.accept(presenceCounter);
    }
    return presenceCounter;
  }

  @Override
  public DateVO getDate() {
    return parent;
  }
}
