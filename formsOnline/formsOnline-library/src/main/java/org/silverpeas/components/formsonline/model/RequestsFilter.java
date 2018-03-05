package org.silverpeas.components.formsonline.model;

import java.util.ArrayList;
import java.util.List;

public class RequestsFilter {

  private String componentId;

  private boolean allRequests;

  private List<String> formIds = new ArrayList<>();

  public RequestsFilter(String componentId, boolean allRequests) {
    this.componentId = componentId;
    this.allRequests = allRequests;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(final String componentId) {
    this.componentId = componentId;
  }

  public boolean isAllRequests() {
    return allRequests;
  }

  public void setAllRequests(final boolean allRequests) {
    this.allRequests = allRequests;
  }

  public List<String> getFormIds() {
    return formIds;
  }

}
