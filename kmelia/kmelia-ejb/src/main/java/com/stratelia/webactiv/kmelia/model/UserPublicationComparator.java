package com.stratelia.webactiv.kmelia.model;

import java.util.Comparator;

public class UserPublicationComparator implements Comparator {

  @Override
  public int compare(Object obj1, Object obj2) {
    if(obj1 instanceof UserPublication && obj2 instanceof UserPublication){
      return Integer.parseInt(((UserPublication) obj1).getPublication().getId()) -
          Integer.parseInt(((UserPublication) obj2).getPublication().getId());
    }    
    return 0;
  }

}
