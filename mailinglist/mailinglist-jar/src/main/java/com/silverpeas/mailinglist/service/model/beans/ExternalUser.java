package com.silverpeas.mailinglist.service.model.beans;

public class ExternalUser extends IdentifiedObject implements
    Comparable<ExternalUser> {

  private String email;

  private String componentId;

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public int hashCode() {
    final int prime = 31;
    int result = prime + ((componentId == null) ? 0 : componentId.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    return result;
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ExternalUser))
      return false;
    final ExternalUser other = (ExternalUser) obj;
    if (componentId == null) {
      if (other.componentId != null)
        return false;
    } else if (!componentId.equals(other.componentId))
      return false;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    return true;
  }

  public int compareTo(ExternalUser user) {
    if (this.equals(user)) {
      return 0;
    }
    if (this.email == null) {
      return -1;
    }
    return this.email.compareToIgnoreCase(user.getEmail());
  }
}
