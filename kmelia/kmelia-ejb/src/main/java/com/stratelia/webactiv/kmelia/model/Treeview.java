package com.stratelia.webactiv.kmelia.model;

import java.util.List;

public class Treeview {

  private String path = null;
  private List tree = null;
  private int nbAliases = 0;
  private String componentId = null;

  public Treeview(String path, List tree, String componentId) {
    this.path = path;
    this.tree = tree;
    this.componentId = componentId;
  }

  public String getComponentId() {
    return componentId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public List getTree() {
    return tree;
  }

  public void setTree(List tree) {
    this.tree = tree;
  }

  public int getNbAliases() {
    return nbAliases;
  }

  public void setNbAliases(int nbAliases) {
    this.nbAliases = nbAliases;
  }

}
