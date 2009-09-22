package com.stratelia.webactiv.kmelia.model.updatechain;

import java.util.ArrayList;
import java.util.List;



public class UpdateChainDescriptor 
{
 
	private String title;
	private String libelle;
	
	private String helper;
	
	private List<FieldUpdateChainDescriptor> fields = new ArrayList<FieldUpdateChainDescriptor>();


	public UpdateChainDescriptor(String title, List<FieldUpdateChainDescriptor> fields) {
		this.title = title;
		this.fields = fields;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<FieldUpdateChainDescriptor> getFields() {
		return fields;
	}

	public void setFields(List<FieldUpdateChainDescriptor> fields) {
		this.fields = fields;
	}

	public void add(FieldUpdateChainDescriptor field) {
        fields.add(field);
}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public String getHelper() {
		return helper;
	}

	public void setHelper(String helper) {
		this.helper = helper;
	}


	
}