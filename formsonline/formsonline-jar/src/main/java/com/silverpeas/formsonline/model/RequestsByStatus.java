package com.silverpeas.formsonline.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Nicolas Eysseric
 */
public class RequestsByStatus {

  List<FormInstance> toValidateList = new ArrayList<FormInstance>();
  List<FormInstance> validatedList = new ArrayList<FormInstance>();
  List<FormInstance> deniedList = new ArrayList<FormInstance>();
  List<FormInstance> archivedList = new ArrayList<FormInstance>();

  public void add(List<FormInstance> requests, FormDetail form) {
    for (FormInstance request : requests) {
      request.setForm(form);
      add(request);
    }
  }

  public void add(FormInstance request) {
    switch (request.getState()) {
      case FormInstance.STATE_REFUSED :
        deniedList.add(request);
        break;
      case FormInstance.STATE_VALIDATED :
        validatedList.add(request);
        break;
      case FormInstance.STATE_ARCHIVED :
        archivedList.add(request);
        break;
      default:
        toValidateList.add(request);
    }
  }

  public List<FormInstance> getToValidate() {
    sortByRequestDate(toValidateList);
    return toValidateList;
  }

  public List<FormInstance> getDenied() {
    return deniedList;
  }

  public List<FormInstance> getValidated() {
    return validatedList;
  }

  public List<FormInstance> getArchived() {
    return archivedList;
  }

  public boolean isEmpty() {
    return getValidated().isEmpty() && getToValidate().isEmpty() && getDenied().isEmpty() &&
        getArchived().isEmpty();
  }

  public List<FormInstance> getAll() {
    List<FormInstance> all = new ArrayList<FormInstance>();
    all.addAll(getToValidate());
    all.addAll(getValidated());
    all.addAll(getDenied());
    all.addAll(getArchived());
    sortByRequestDate(all);
    return all;
  }

  private void sortByRequestDate(List<FormInstance> requests) {
    Collections.sort(requests, new Comparator<FormInstance>() {
      public int compare(FormInstance a, FormInstance b) {
        int c = b.getCreationDate().compareTo(a.getCreationDate());
        if (c == 0) {
          c = Integer.valueOf(b.getId()) - Integer.valueOf(a.getId());
        }
        return c;
      }
    });
  }

}