/*
 * IconDetail.java
 *
 * Created on 19 avril 2001, 09:12
 */
 
package com.stratelia.webactiv.webSites.siteManage.model;

/** 
 *
 * @author  cbonin
 * @version 
 */

import java.io.Serializable;

public class IconDetail implements Serializable  {

        /*-------------- Attributs ------------------*/
        private SitePK iconPk = new SitePK("", "", "");
        private String name; 
        private String description;
        private String address;
        
        
        /*-------------- Methodes des attributs ------------------*/
//iconPk
  public SitePK getIconPK() {
		return iconPk;}
	public void setIconPK(SitePK val) {
		iconPk = new SitePK(val.getId(), val.getSpace(), val.getComponentName());}

//name
	public String getName() {
		return name;}
	public void setName(String val) {
		name = val;}
                
//description
	public String getDescription() {
		return description;}
	public void setDescription(String val) {
		description = val;}     

//address
	public String getAddress() {
		return address;}
	public void setAddress(String val) {
		address = val;}  
                              
                 
  /*-------------- Methodes ------------------*/

 /**
   SiteDetail
 */
 public IconDetail() {
    init("","","","");
}

 /**
   IconDetail
 */
 public IconDetail(String idIcon, String name, String description, String address){
      init(idIcon, name, description, address);
}

 /**
   init
 */
 public void init(String idIcon, String name, String description, String address){
    this.iconPk.setId(idIcon);
    this.name = name;
    this.description = description;
    this.address = address;
 }


 /**
   toString
*/
 public String toString() {
	return iconPk+"|"+name+"|"+description+"|"+address;
 }
}