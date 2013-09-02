package com.stratelia.webactiv.yellowpages;

import java.util.ArrayList;
import java.util.List;

public class ImportReport {
  
  private int nbAdded;
  private List<String> errors = new ArrayList<String>();
  
  public void setNbAdded(int nbAdded) {
    this.nbAdded = nbAdded;
  }
  public int getNbAdded() {
    return nbAdded;
  }
  
  public void addError(String error) {
    errors.add(error);
  }
  public List<String> getErrors() {
    return errors;
  }
  
  public boolean isInError() {
    return !errors.isEmpty();
  }
  
}