/*
 * Created on 25 oct. 2004
 *
 */
package com.silverpeas.projectManager.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import com.stratelia.webactiv.calendar.backbone.TodoDetail;
import com.stratelia.webactiv.calendar.model.Attendee;

/**
 * @author neysseri
 * 
 */
public class TaskDetail implements Serializable {

  public final static int IN_PROGRESS = 0;
  public final static int STOPPED = 1;
  public final static int CANCELLED = 2;
  public final static int COMPLETE = 3;
  public final static int IN_ALERT = 4;
  public final static int NOT_STARTED = 5;

  private int id;
  private int mereId;
  private int chrono;
  private String nom;
  private String description;
  private int organisateurId;
  private int responsableId;
  private float charge;
  private float consomme;
  private float raf;
  private int statut;
  private Date dateDebut;
  private Date dateFin;
  private String codeProjet;
  private String descriptionProjet;
  private int estDecomposee;
  private String instanceId;
  private String path = "/";
  private int previousTaskId = -1;

  // les ressources (Collection de TaskResourceDetail)
  private Collection resources = null;

  // Les attributs suivants sont utilisés lors de l'affichage
  private String responsableFullName;
  private String organisateurFullName;
  private String uiDateDebut;
  private String uiDateFin;
  private Vector attachments = null;
  private boolean isUnfold = false;
  private int level = 0;
  private boolean updateAvailable = false;
  private boolean deletionAvailable = false;
  private String uiDateDebutPlus1;
  private String previousTaskName = null;

  public TaskDetail() {
  }

  public TaskDetail(int id, int mereId, int chrono, String nom,
      String description, int organisateurId, int responsableId, float charge,
      float consomme, float raf, int statut, Date dateDebut, Date dateFin,
      String codeProjet, String descriptionProjet, int estDecomposee,
      String instanceId, String path) {
    setId(id);
    setMereId(mereId);
    setChrono(chrono);
    setNom(nom);
    setDescription(description);
    setOrganisateurId(organisateurId);
    setResponsableId(responsableId);
    setCharge(charge);
    setConsomme(consomme);
    setRaf(raf);
    setStatut(statut);
    setDateDebut(dateDebut);
    setDateFin(dateFin);
    setCodeProjet(codeProjet);
    setDescriptionProjet(descriptionProjet);
    setEstDecomposee(estDecomposee);
    setInstanceId(instanceId);
    setPath(path);
  }

  /**
   * @return
   */
  public int getAvancement() {
    return Math.round((consomme / (consomme + raf)) * 100);
  }

  /**
   * @return
   */
  public float getCharge() {
    return charge;
  }

  /**
   * @return
   */
  public int getChrono() {
    return chrono;
  }

  /**
   * @return
   */
  public String getCodeProjet() {
    return codeProjet;
  }

  /**
   * @return
   */
  public float getConsomme() {
    return consomme;
  }

  /**
   * @return
   */
  public Date getDateDebut() {
    return dateDebut;
  }

  /**
   * @return
   */
  public Date getDateFin() {
    return dateFin;
  }

  /**
   * @return
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return
   */
  public String getDescriptionProjet() {
    return descriptionProjet;
  }

  /**
   * @return
   */
  public int getEstDecomposee() {
    return estDecomposee;
  }

  /**
   * @return
   */
  public int getId() {
    return id;
  }

  /**
   * @return
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * @return
   */
  public int getMereId() {
    return mereId;
  }

  /**
   * @return
   */
  public String getNom() {
    return nom;
  }

  /**
   * @return
   */
  public int getOrganisateurId() {
    return organisateurId;
  }

  /**
   * @return
   */
  public float getRaf() {
    return raf;
  }

  /**
   * @return
   */
  public int getResponsableId() {
    return responsableId;
  }

  public Collection getResources() {
    return resources;
  }

  public void setResources(Collection resources) {
    this.resources = resources;
  }

  /**
   * @return
   */
  public int getStatut() {
    return statut;
  }

  /**
   * @param f
   */
  public void setCharge(float f) {
    charge = f;
  }

  public void setCharge(String f) {
    if (f != null && f.length() > 0)
      charge = new Float(f).floatValue();
    else
      charge = 0;
  }

  /**
   * @param i
   */
  public void setChrono(int i) {
    chrono = i;
  }

  /**
   * @param i
   */
  public void setCodeProjet(String i) {
    if (i != null && i.length() > 0)
      codeProjet = i;
    else
      codeProjet = "-1";
  }

  /**
   * @param f
   */
  public void setConsomme(float f) {
    consomme = f;
  }

  public void setConsomme(String f) {
    if (f != null && f.length() > 0)
      consomme = new Float(f).floatValue();
    else
      consomme = 0;
  }

  /**
   * @param string
   */
  public void setDateDebut(Date string) {
    dateDebut = string;
  }

  /**
   * @param string
   */
  public void setDateFin(Date string) {
    dateFin = string;
  }

  /**
   * @param string
   */
  public void setDescription(String string) {
    description = string;
  }

  /**
   * @param string
   */
  public void setDescriptionProjet(String string) {
    descriptionProjet = string;
  }

  /**
   * @param i
   */
  public void setEstDecomposee(int i) {
    estDecomposee = i;
  }

  /**
   * @param i
   */
  public void setId(int i) {
    id = i;
  }

  /**
   * @param string
   */
  public void setInstanceId(String string) {
    instanceId = string;
  }

  /**
   * @param i
   */
  public void setMereId(int i) {
    mereId = i;
  }

  /**
   * @param string
   */
  public void setNom(String string) {
    nom = string;
  }

  /**
   * @param i
   */
  public void setOrganisateurId(int i) {
    organisateurId = i;
  }

  public void setOrganisateurId(String s) {
    organisateurId = new Integer(s).intValue();
  }

  /**
   * @param f
   */
  public void setRaf(float f) {
    raf = f;
  }

  public void setRaf(String f) {
    if (f != null && f.length() > 0)
      raf = new Float(f).floatValue();
    else
      raf = 0;
  }

  /**
   * @param i
   */
  public void setResponsableId(int i) {
    responsableId = i;
  }

  /**
   * @param i
   */
  public void setStatut(int i) {
    statut = i;
  }

  /**
   * @return
   */
  public String getPath() {
    return path;
  }

  /**
   * @param string
   */
  public void setPath(String string) {
    path = string;
  }

  public TodoDetail toTodoDetail() {
    TodoDetail todo = new TodoDetail();

    todo.setComponentId(getInstanceId());
    todo.setSpaceId("useless");
    todo.setName(getNom());
    todo.setDescription(getDescription());
    todo.setDelegatorId(new Integer(getOrganisateurId()).toString());
    Attendee attendee = new Attendee(new Integer(getResponsableId()).toString());
    if (attendee != null) {
      Vector attendees = new Vector();
      attendees.add(attendee);
      todo.setAttendees(attendees);
    }
    todo.setExternalId(new Integer(getId()).toString());
    todo.setStartDate(getDateDebut());
    todo.setEndDate(getDateFin());
    todo.setPercentCompleted(getAvancement());
    return todo;
  }

  public void setResourceIds(Collection resources) {
    this.resources = resources;
  }

  /**
   * @return
   */
  public String getResponsableFullName() {
    return responsableFullName;
  }

  /**
   * @param string
   */
  public void setResponsableFullName(String string) {
    responsableFullName = string;
  }

  /**
   * @return
   */
  public String getUiDateDebut() {
    return uiDateDebut;
  }

  /**
   * @return
   */
  public String getUiDateFin() {
    return uiDateFin;
  }

  /**
   * @param string
   */
  public void setUiDateDebut(String string) {
    uiDateDebut = string;
  }

  /**
   * @param string
   */
  public void setUiDateFin(String string) {
    uiDateFin = string;
  }

  public String getUiCodeProjet() {
    if (getCodeProjet().equals("-1"))
      return "";
    else
      return new Integer(getCodeProjet()).toString();
  }

  public String getUiDescriptionProjet() {
    if (getDescriptionProjet() == null)
      return "";
    else
      return getDescriptionProjet();
  }

  public String getUiDescription() {
    if (getDescription() == null)
      return "";
    else
      return getDescription();
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("TaskDetail {").append("\n");
    result.append("  id = ").append(getId()).append("\n");
    result.append("  mereId = ").append(getMereId()).append("\n");
    result.append("  chrono = ").append(getChrono()).append("\n");
    result.append("  nom = ").append(getNom()).append("\n");
    result.append("  organisateurId = ").append(getOrganisateurId()).append(
        "\n");
    result.append("  responsableId = ").append(getResponsableId()).append("\n");
    result.append("  charge = ").append(getCharge()).append("\n");
    result.append("  consomme  = ").append(getConsomme()).append("\n");
    result.append("  raf = ").append(getRaf()).append("\n");
    result.append("  avancement  = ").append(getAvancement()).append("\n");
    result.append("  statut = ").append(getStatut()).append("\n");
    result.append("  dateDebut = ").append(getDateDebut()).append("\n");
    result.append("  dateFin = ").append(getDateFin()).append("\n");
    result.append("  codeProjet = ").append(getCodeProjet()).append("\n");
    result.append("  estDecomposee = ").append(getEstDecomposee()).append("\n");
    result.append("  instanceId = ").append(getInstanceId()).append("\n");
    result.append("  path = ").append(getPath()).append("\n");
    result.append("}");
    return result.toString();
  }

  /**
   * @return
   */
  public String getOrganisateurFullName() {
    return organisateurFullName;
  }

  /**
   * @param string
   */
  public void setOrganisateurFullName(String string) {
    organisateurFullName = string;
  }

  /**
   * @return
   */
  public Vector getAttachments() {
    return attachments;
  }

  /**
   * @param vector
   */
  public void setAttachments(Collection vector) {
    attachments = new Vector(vector);
  }

  /**
   * @return
   */
  public boolean isUnfold() {
    return isUnfold;
  }

  /**
   * @param b
   */
  public void setUnfold(boolean b) {
    isUnfold = b;
  }

  /**
   * @return
   */
  public int getLevel() {
    return level;
  }

  /**
   * @param i
   */
  public void setLevel(int i) {
    level = i;
  }

  /**
   * @return
   */
  public boolean isDeletionAvailable() {
    return deletionAvailable;
  }

  /**
   * @return
   */
  public boolean isUpdateAvailable() {
    return updateAvailable;
  }

  /**
   * @param b
   */
  public void setDeletionAvailable(boolean b) {
    deletionAvailable = b;
  }

  /**
   * @param b
   */
  public void setUpdateAvailable(boolean b) {
    updateAvailable = b;
  }

  /**
   * @return
   */
  public int getPreviousTaskId() {
    return previousTaskId;
  }

  /**
   * @param i
   */
  public void setPreviousTaskId(int i) {
    previousTaskId = i;
  }

  /**
   * @return
   */
  public String getUiDateDebutPlus1() {
    return uiDateDebutPlus1;
  }

  /**
   * @param string
   */
  public void setUiDateDebutPlus1(String string) {
    uiDateDebutPlus1 = string;
  }

  /**
   * @return
   */
  public String getPreviousTaskName() {
    return previousTaskName;
  }

  /**
   * @param string
   */
  public void setPreviousTaskName(String string) {
    previousTaskName = string;
  }

}