package com.silverpeas.whitePages.service;

import java.util.Comparator;

import com.silverpeas.whitePages.model.SearchField;

public class SearchFieldComparator implements Comparator<SearchField> {

  public int compare(SearchField o1, SearchField o2) {
    return o1.getFieldId().compareTo(o2.getFieldId()) ;
  }

}
