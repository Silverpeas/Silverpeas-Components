package com.silverpeas.mailinglist.service.model.beans;

public class InternalSubscriber extends IdentifiedObject {
  protected String externalId;

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((externalId == null) ? 0 : externalId.hashCode());
    result = prime * result + this.getClass().hashCode();
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final InternalSubscriber other = (InternalSubscriber) obj;
    if (externalId == null) {
      if (other.externalId != null) {
        return false;
      }
    } else if (externalId.equals(other.externalId)) {
      return true;
    }
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

}
