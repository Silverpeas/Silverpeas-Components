package com.stratelia.webactiv.kmelia.model.updatechain;

public class Fields {
  private String title;
  private String libelle;

  private FieldUpdateChainDescriptor name;
  private FieldUpdateChainDescriptor description;
  private FieldUpdateChainDescriptor keywords;
  private FieldUpdateChainDescriptor tree;

  private String helper;

  private String[] topics;

  public FieldUpdateChainDescriptor getName() {
    return name;
  }

  public void setName(FieldUpdateChainDescriptor name) {
    this.name = name;
  }

  public FieldUpdateChainDescriptor getDescription() {
    return description;
  }

  public void setDescription(FieldUpdateChainDescriptor description) {
    this.description = description;
  }

  public FieldUpdateChainDescriptor getKeywords() {
    return keywords;
  }

  public void setKeywords(FieldUpdateChainDescriptor keywords) {
    this.keywords = keywords;
  }

  public FieldUpdateChainDescriptor getTree() {
    return tree;
  }

  public void setTree(FieldUpdateChainDescriptor tree) {
    this.tree = tree;
  }

  public String[] getTopics() {
    return topics;
  }

  public void setTopics(String[] topics) {
    this.topics = topics;
  }

  public String getHelper() {
    return helper;
  }

  public void setHelper(String helper) {
    this.helper = helper;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getLibelle() {
    return libelle;
  }

  public void setLibelle(String libelle) {
    this.libelle = libelle;
  }

}