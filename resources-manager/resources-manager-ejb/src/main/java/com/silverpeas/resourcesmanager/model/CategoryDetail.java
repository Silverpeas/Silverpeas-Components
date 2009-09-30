package com.silverpeas.resourcesmanager.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class CategoryDetail implements Serializable {

  private String id;
  private String instanceId;
  private String name;
  private Date creationDate;
  private Date updateDate;
  private boolean bookable;
  private String form;
  private String responsibleId;
  private String createrId;
  private String updaterId;
  private String description;
  private ArrayList resources;

  public boolean getBookable() {
    return bookable;
  }

  public void setBookable(boolean bookable) {
    this.bookable = bookable;
  }

  public String getCreaterId() {
    return createrId;
  }

  public void setCreaterId(String createrId) {
    this.createrId = createrId;
  }

  public String getForm() {
    return form;
  }

  public void setForm(String form) {
    this.form = form;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ArrayList getResources() {
    return resources;
  }

  public void setResources(ArrayList resources) {
    this.resources = resources;
  }

  public String getResponsibleId() {
    return responsibleId;
  }

  public void setResponsibleId(String responsibleId) {
    this.responsibleId = responsibleId;
  }

  public String getUpdaterId() {
    return updaterId;
  }

  public void setUpdaterId(String updaterId) {
    this.updaterId = updaterId;
  }

  public CategoryDetail(String id, String instanceId, String name,
      Date creationDate, Date updateDate, boolean bookable, String form,
      String responsibleId, String createrId, String updaterId,
      String description, ArrayList resources) {
    super();
    this.id = id;
    this.instanceId = instanceId;
    this.name = name;
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.bookable = bookable;
    this.form = form;
    this.responsibleId = responsibleId;
    this.createrId = createrId;
    this.updaterId = updaterId;
    this.description = description;
    this.resources = resources;
  }

  public CategoryDetail(String name, boolean bookable, String form,
      String responsibleId, String description) {
    super();
    this.name = name;
    this.bookable = bookable;
    this.form = form;
    this.responsibleId = responsibleId;
    this.description = description;
  }

  public CategoryDetail(String id, String instanceId, String name,
      Date creationDate, Date updateDate, boolean bookable, String form,
      String responsibleId, String createrId, String updaterId,
      String description) {
    super();
    this.id = id;
    this.instanceId = instanceId;
    this.name = name;
    this.creationDate = creationDate;
    this.updateDate = updateDate;
    this.bookable = bookable;
    this.form = form;
    this.responsibleId = responsibleId;
    this.createrId = createrId;
    this.updaterId = updaterId;
    this.description = description;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public CategoryDetail(String id, String name, boolean bookable, String form,
      String responsibleId, String description) {
    super();
    this.id = id;
    this.name = name;
    this.bookable = bookable;
    this.form = form;
    this.responsibleId = responsibleId;
    this.description = description;
  }

}
