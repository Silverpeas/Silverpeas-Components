package com.silverpeas.components.organizationchart.model;

public class OrganizationalRole {
	
	private String label = null; // label or label to print in front
	private String ldapKey = null; // key wich is part of the "Title" ldap filed
	
	public OrganizationalRole(String label, String ldapKey){
		this.label = label;
		this.ldapKey = ldapKey;
	}

	public String getLabel() {
		return label;
	}

	public String getLdapKey() {
		return ldapKey;
	}

}
