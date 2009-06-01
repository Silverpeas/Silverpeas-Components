package com.stratelia.silverpeas.connecteurJDBC.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

public class ConnecteurJDBCConnectionInfoPK extends WAPrimaryKey implements Serializable {

  /**
	* Constructor which set only the id
	* @since 1.0
	*/
  public ConnecteurJDBCConnectionInfoPK (String id) {
	super(id);
  }

   /**
	* Constructor which set the id
	* The WAPrimaryKey provides space and component name
	* @since 1.0
	*/
  public ConnecteurJDBCConnectionInfoPK (String id, String space, String componentName) {
	super(id, space, componentName);
  }

  /**
	* Constructor which set the id
	* The WAPrimaryKey provides space and component name
	* @since 1.0
	*/
  public ConnecteurJDBCConnectionInfoPK (String id, WAPrimaryKey pk) {
	super(id, pk);
  }

  /**
	* Return the object root table name
	* @return the root table name of the object
	* @since 1.0
	*/
  public String getRootTableName() {
	return "ConnecteurJDBCConnectionInfo";
  }

  public String getTableName() {
	return "SC_ConnecteurJDBC_ConnectInfo";
  }

  /**
	* Check if an another object is equal to this object
	* @return true if other is equals to this object
	* @param other the object to compare to this NodePK
	* @since 1.0
	*/
  public boolean equals(Object other) {
	if (!(other instanceof ConnecteurJDBCConnectionInfoPK)) return false;
	return (id.equals( ((ConnecteurJDBCConnectionInfoPK) other).getId()) ) &&
	   (space.equals(((ConnecteurJDBCConnectionInfoPK) other).getSpace()) ) &&
	   (componentName.equals(((ConnecteurJDBCConnectionInfoPK) other).getComponentName()) );
  }

  /**
	* Returns a hash code for the key
	* @return A hash code for this object
	*/
  public int hashCode() {
		return toString().hashCode();
  }

}