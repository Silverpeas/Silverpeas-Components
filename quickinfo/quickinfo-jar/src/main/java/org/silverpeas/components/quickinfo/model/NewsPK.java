package org.silverpeas.components.quickinfo.model;

import com.stratelia.webactiv.util.WAPrimaryKey;

public class NewsPK extends WAPrimaryKey {

  private static final long serialVersionUID = 4717882636348623370L;

  public NewsPK(String id, String componentId) {
    super(id, componentId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof NewsPK)) {
      return false;
    }
    NewsPK other = (NewsPK) obj;
    if (id == null) {
      return other.id == null;
    }
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

}
