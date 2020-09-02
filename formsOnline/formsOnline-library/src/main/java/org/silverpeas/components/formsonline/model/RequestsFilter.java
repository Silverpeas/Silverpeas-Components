package org.silverpeas.components.formsonline.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RequestsFilter {

  private String componentId;
  private boolean allRequests;
  private List<String> formIds = new ArrayList<>();
  private int state = -1;
  private FormInstanceValidationType pendingValidationType;

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

  public int getState() {
    return state;
  }

  public void setState(final int state) {
    this.state = state;
  }

  public Optional<FormInstanceValidationType> getPendingValidationType() {
    return Optional.ofNullable(pendingValidationType);
  }

  public void setPendingValidationType(final FormInstanceValidationType pendingValidationType) {
    this.pendingValidationType = pendingValidationType;
  }
}
