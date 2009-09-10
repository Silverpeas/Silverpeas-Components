package com.silverpeas.gallery.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * It's the Node PrimaryKey object
 * It identify a Node
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class PhotoPK extends WAPrimaryKey implements Serializable {

  // to apply the fat key pattern
  transient public PhotoDetail photoDetail = null;

  /**
	* Constructor which set only the id
	* @since 1.0
	*/
  public PhotoPK (String id) {
    super(id);
  }

  /**
	* Constructor which set id, space and component name
	* @since 1.0
	*/
  public PhotoPK (String id, String space, String componentName) {
    super(id, space, componentName);
  }
  
  public PhotoPK(String id, String componentId)
  {
	  super(id, componentId);
  }
  
  /**
	* Constructor which set the id
	* The WAPrimaryKey provides space and component name
	* @since 1.0
	*/
  public PhotoPK (String id, WAPrimaryKey pk) {
    super(id, pk);
  }
  
  /**
	* Return the object root table name
	* @return the root table name of the object
	* @since 1.0
	*/
  public String getRootTableName() {
    return "Gallery";
  }

  /**
	* Return the object table name
	* @return the table name of the object
	* @since 1.0
	*/
  public String getTableName() {
    return "SC_Gallery_Photo";
  }
  
  /**
	* Check if an another object is equal to this object
	* @return true if other is equals to this object
	* @param other the object to compare to this NodePK
	* @since 1.0
	*/
  public boolean equals(Object other) {
    if (!(other instanceof PhotoPK)) return false;
    return (id.equals( ((PhotoPK) other).getId()) ) &&
       (componentName.equals(((PhotoPK) other).getComponentName()) );
  }

	/**
	* Returns a hash code for the key
	* @return A hash code for this object
	*/
	public int hashCode() {
		return this.id.hashCode() ^ this.componentName.hashCode();
		//return toString().hashCode();
	}
}