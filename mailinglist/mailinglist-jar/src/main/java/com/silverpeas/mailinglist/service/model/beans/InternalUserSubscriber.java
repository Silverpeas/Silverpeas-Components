package com.silverpeas.mailinglist.service.model.beans;

public class InternalUserSubscriber extends InternalSubscriber {
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
    final InternalUserSubscriber other = (InternalUserSubscriber) obj;
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
