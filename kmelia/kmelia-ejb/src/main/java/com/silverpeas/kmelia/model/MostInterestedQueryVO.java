package com.silverpeas.kmelia.model;

import java.io.Serializable;

public class MostInterestedQueryVO implements Serializable {

  private String query;
  private Integer occurrences;

  /**
   * @param query
   * @param occurrences
   */
  public MostInterestedQueryVO(String query, Integer occurrences) {
    this.query = query;
    this.occurrences = occurrences;
  }

  /**
   * @return the query
   */
  public String getQuery() {
    return query;
  }

  /**
   * @param query the query to set
   */
  public void setQuery(String query) {
    this.query = query;
  }

  /**
   * @return the occurrences
   */
  public Integer getOccurrences() {
    return occurrences;
  }

  /**
   * @param occurrences the occurrences to set
   */
  public void setOccurrences(Integer occurrences) {
    this.occurrences = occurrences;
  }

}
