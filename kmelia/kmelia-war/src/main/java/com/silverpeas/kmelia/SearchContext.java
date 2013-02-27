package com.silverpeas.kmelia;

import java.util.List;

import com.stratelia.webactiv.kmelia.model.KmeliaPublication;

public class SearchContext {
  
  public static int NONE = 0;
  public static int GLOBAL = 1;
  public static int LOCAL = 2;
  
  private String query;
  private List<KmeliaPublication> results;
  private int currentIndex = 0;
  
  public SearchContext(String query, List<KmeliaPublication> results) {
    this.query = query;
    this.results = results;
  }
  
  public String getQuery() {
    return query;
  }
  public void setQuery(String query) {
    this.query = query;
  }
  public List<KmeliaPublication> getResults() {
    return results;
  }
  public void setResults(List<KmeliaPublication> results) {
    this.results = results;
  }
  public int getCurrentIndex() {
    return currentIndex;
  }
  public void setCurrentIndex(int currentIndex) {
    this.currentIndex = currentIndex;
  }
  
  public void markPublicationAsRead(KmeliaPublication publication) {
    // store "in session" the publication is read
    int index = results.indexOf(publication);
    if (index != -1) {
      results.get(index).read = true;
    }
  }

}