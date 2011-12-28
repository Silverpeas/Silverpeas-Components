package com.silverpeas.components.organizationchart.control;

import java.util.Comparator;

import com.silverpeas.components.organizationchart.view.OrganizationBox;

public class UnitComparator implements Comparator<OrganizationBox> {
  
  static public UnitComparator comparator = new UnitComparator();

  @Override
  public int compare(OrganizationBox p1, OrganizationBox p2) {
    return p1.getName().compareTo(p2.getName());
  }

}
