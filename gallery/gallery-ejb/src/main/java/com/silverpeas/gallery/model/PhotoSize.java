/**
 * 
 */
package com.silverpeas.gallery.model;

/**
 * A set a different sized photo is generated for each photo.
 * This Enum provides the differents size with associated prefix.
 * 
 * @author Ludovic Bertin
 */
public enum PhotoSize {
  TINY("tiny", "_66x50.jpg"),
  SMALL("small", "_133x100.jpg"),
  NORMAL("normal", "_266x150.jpg"),
  PREVIEW("preview", "_266x150.jpg"),
  ORIGINAL("original", ".jpg");
  
  private String prefix = null;
  private String name = null;

  private PhotoSize(String name, String prefix) {
    this.name = name;
    this.prefix = prefix;
  }

  /**
   * @return the prefix
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Get photo site with given size name
   * 
   * @param name  the size name
   * 
   * @return PhotoSize object
   */
  static public PhotoSize get(String name) {
    for (PhotoSize size : PhotoSize.values()) {
      if (size.name.equals(name)) {
        return size;
      }
    }
    
    return PREVIEW;
  }
}
