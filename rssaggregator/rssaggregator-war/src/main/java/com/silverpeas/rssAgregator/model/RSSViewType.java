package com.silverpeas.rssAgregator.model;

public enum RSSViewType {
  
  /**
   * The AGREGATED RSS view.
   */
  AGREGATED("agregated"),
  /**
   * The SEPARATED RSS view
   */
  SEPARATED("separated");

  /**
   * Constructs a view type with the specified view mode.
   * @param modeView the view mode as defined in the underlying calendar renderer.
   */
  private RSSViewType(final String viewMode) {
    this.rssView = viewMode;
  }
  private String rssView;
  
  /**
   * Converts this view type in a string representation.
   * The value of the string depends on the RSS view rendering engine. It should be a value
   * that matches the view mode supported by the underlying RSS renderer.
   * @return
   */
  @Override
  public String toString() {
    return this.rssView;
  }
}
