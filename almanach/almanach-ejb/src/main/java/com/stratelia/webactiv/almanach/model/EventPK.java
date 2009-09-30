package com.stratelia.webactiv.almanach.model;

import java.io.Serializable;
import com.stratelia.webactiv.util.*;

/**
 * It's the Event PrimaryKey object It identify a Event
 * 
 * @author Seb
 * @version 1.0
 */
public class EventPK extends WAPrimaryKey implements Serializable {

  /**
   * Constructor which set only the id
   * 
   * @since 1.0
   */
  public EventPK(String id) {
    super(id);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component
   * name
   * 
   * @since 1.0
   */
  public EventPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component
   * name
   * 
   * @since 1.0
   */
  public EventPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Return the object root table name
   * 
   * @return the root table name of the object
   * @since 1.0
   */
  public String getRootTableName() {
    return "Event";
  }

  /**
   * Return the object table name
   * 
   * @return the root table name of the object
   * @since 1.0
   */
  public String getTableName() {
    return "SC_Almanach_Event";
  }

  /**
   * Check if an another object is equal to this object
   * 
   * @return true if other is equals to this object
   * @param other
   *          the object to compare to this NodePK
   * @since 1.0
   */
  public boolean equals(Object other) {
    if (!(other instanceof EventPK))
      return false;
    return (id.equals(((EventPK) other).getId()))
        && (space.equals(((EventPK) other).getSpace()))
        && (componentName.equals(((EventPK) other).getComponentName()));
  }

  /**
   * Returns a hash code for the key
   * 
   * @return A hash code for this object
   */
  public int hashCode() {
    return toString().hashCode();
  }
}