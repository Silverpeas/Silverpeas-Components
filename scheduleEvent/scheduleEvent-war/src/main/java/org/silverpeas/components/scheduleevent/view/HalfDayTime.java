package org.silverpeas.components.scheduleevent.view;

import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.components.scheduleevent.service.model.beans.Response;

import java.util.*;

public class HalfDayTime extends AbstractTimeVO {
  private static final String HMTL_CLASS_ATTRIBUTE = "titreCouleur";

  private final DateVO parent;
  private final DateOption date;
  private final Map<ContributorVO, AvailableVO> availabilities = new HashMap<>();

  public HalfDayTime(DateVO parent, DateOption date) {
    this.parent = parent;
    this.date = date;
  }

  @Override
  public String getId() {
    return date.getId();
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
    Set<Response> match = new HashSet<>();
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

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
