package com.silverpeas.scheduleevent.view;

import java.util.HashSet;
import java.util.Set;

import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.view.AvailabilityFactoryVO.Availability;

public class ContributorImplVO implements ContributorVO {
  private Contributor contributor;
  private String name;
  private AvailabilityFactoryVO availabilityFactory;
  private String htmlClassAttribute;
  
  public ContributorImplVO(String name, Contributor contributor, AvailabilityFactoryVO availabilityFactory) {
    this.contributor = contributor;
    this.name = name;
    this.availabilityFactory = availabilityFactory;
    htmlClassAttribute = "";
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((contributor == null) ? 0 : contributor.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ContributorImplVO other = (ContributorImplVO) obj;
    if (contributor == null) {
      if (other.contributor != null)
        return false;
    } else if (!contributor.equals(other.contributor))
      return false;
    return true;
  }

  @Override
  public boolean hasAnswered() {
    return contributor.getLastValidation() != null;
  }

  @Override
  public Set<Response> match(Set<Response> responses) {
    Set<Response> match = new HashSet<Response>();
    for(Response response: responses) {
      if (isMatchedUserId(response.getUserId())) {
        match.add(response);
      }
    }
    return match;
  }

  private boolean isMatchedUserId(int userId) {
    return contributor.getUserId() == userId;
  }

  @Override
  public AvailableVO makeAvailabilty(Availability availability) {
    return availabilityFactory.makeAvailablity(availability);
  }

  @Override
  public String getHtmlClassAttribute() {
    return htmlClassAttribute;
  }

  public void setHtmlClassAttribute(String attribute) {
    htmlClassAttribute = attribute;
  }
}
