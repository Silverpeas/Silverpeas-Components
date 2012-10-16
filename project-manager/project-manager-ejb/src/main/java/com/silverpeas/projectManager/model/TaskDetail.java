/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.projectManager.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.stratelia.webactiv.calendar.backbone.TodoDetail;
import com.stratelia.webactiv.calendar.model.Attendee;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import java.util.ArrayList;

/**
 * @author neysseri
 */
public class TaskDetail implements Serializable {
  private static final long serialVersionUID = -1211845237822053494L;
  public final static int IN_PROGRESS = 0;
  public final static int STOPPED = 1;
  public final static int CANCELLED = 2;
  public final static int COMPLETE = 3;
  public final static int IN_ALERT = 4;
  public final static int NOT_STARTED = 5;
  private static final String TYPE = "Task";
  private int id;
  private int mereId = -1;
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
  private Collection<TaskResourceDetail> resources = null;
  private String responsableFullName;
  private String organisateurFullName;
  private String uiDateDebut;
  private String uiDateFin;
  private List<AttachmentDetail> attachments = null;
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
    this.id = id;
    this.mereId = mereId;
    this.chrono = chrono;
    this.nom = nom;
    this.description = description;
    this.organisateurId = organisateurId;
    this.responsableId = responsableId;
    this.charge = charge;
    this.consomme = consomme;
    this.raf = raf;
    this.statut = statut;
    this.dateDebut = dateDebut;
    this.dateFin = dateFin;
    this.codeProjet = codeProjet;
    this.descriptionProjet = descriptionProjet;
    this.estDecomposee = estDecomposee;
    this.instanceId = instanceId;
    this.path = path;
    // Initialize level because level has never been set before.
    this.level = StringUtils.countMatches(this.path, "/") - 2;
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

  public Collection<TaskResourceDetail> getResources() {
    return resources;
  }

  public void setResources(Collection<TaskResourceDetail> resources) {
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
    if (f != null && f.length() > 0) {
      charge = Float.valueOf(f);
    } else {
      charge = 0;
    }
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
    if (i != null && i.length() > 0) {
      codeProjet = i;
    } else {
      codeProjet = "-1";
    }
  }

  /**
   * @param f
   */
  public void setConsomme(float f) {
    consomme = f;
  }

  public void setConsomme(String f) {
    if (f != null && f.length() > 0) {
      consomme = Float.valueOf(f);
    } else {
      consomme = 0;
    }
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
    organisateurId = Integer.valueOf(s);
  }

  /**
   * @param f
   */
  public void setRaf(float f) {
    raf = f;
  }

  public void setRaf(String f) {
    if (f != null && f.length() > 0) {
      raf = Float.valueOf(f);
    } else {
      raf = 0;
    }
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
    todo.setDelegatorId(Integer.toString(getOrganisateurId()));
    Attendee attendee = new Attendee(String.valueOf(getResponsableId()));
    if (attendee != null) {
      List<Attendee> attendees = new ArrayList<Attendee>();
      attendees.add(attendee);
      todo.setAttendees(attendees);
    }
    todo.setExternalId(Integer.toString(getId()));
    todo.setStartDate(getDateDebut());
    todo.setEndDate(getDateFin());
    todo.setPercentCompleted(getAvancement());
    return todo;
  }

  public void setResourceIds(Collection<TaskResourceDetail> resources) {
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
    if (getCodeProjet().equals("-1")) {
      return "";
    }
    return getCodeProjet();
  }

  public String getUiDescriptionProjet() {
    if (getDescriptionProjet() == null) {
      return "";
    }
    return getDescriptionProjet();
  }

  public String getUiDescription() {
    if (getDescription() == null) {
      return "";
    }
    return getDescription();
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
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
  public List<AttachmentDetail> getAttachments() {
    return attachments;
  }

  /**
   * @param vector
   */
  public void setAttachments(List<AttachmentDetail> vector) {
    attachments = vector;
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

  public String getContributionType() {
    return TYPE;
  }
  
  /**
   * The type of this resource
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return TYPE;
  }
}
