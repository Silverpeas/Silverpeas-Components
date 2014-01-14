package com.silverpeas.components.organizationchart.control;

import java.io.Serializable;
import java.util.Comparator;

import com.silverpeas.components.organizationchart.view.OrganizationBox;

public class UnitComparator implements Comparator<OrganizationBox>, Serializable {

  public static final UnitComparator comparator = new UnitComparator();
  private static final long serialVersionUID = 1L;

  @Override
  public int compare(OrganizationBox p1, OrganizationBox p2) {
    return p1.getName().compareTo(p2.getName());
  }

}
