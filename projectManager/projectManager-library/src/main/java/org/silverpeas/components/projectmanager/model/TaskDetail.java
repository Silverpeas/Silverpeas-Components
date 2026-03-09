/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.projectmanager.model;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.personalorganizer.model.Attendee;
import org.silverpeas.core.personalorganizer.model.TodoDetail;
import org.silverpeas.kernel.exception.NotSupportedException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author neysseri
 */
public class TaskDetail implements Serializable {
  private static final long serialVersionUID = -1211845237822053494L;
  public static final int IN_PROGRESS = 0;
  public static final int STOPPED = 1;
  public static final int CANCELLED = 2;
  public static final int COMPLETE = 3;
  public static final int IN_ALERT = 4;
  public static final int NOT_STARTED = 5;
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
  private List<SimpleDocument> attachments = null;
  private boolean isUnfold = false;
  private int level = 0;
  private boolean updateAvailable = false;
  private boolean deletionAvailable = false;
  private String uiDateDebutPlus1;
  private String previousTaskName = null;

  public static TaskDetailBuilder builder() {
    return new TaskDetailBuilder();
  }

  private TaskDetail() {
  }

  public int getAvancement() {
    return Math.round((consomme / (consomme + raf)) * 100);
  }

  public float getCharge() {
    return charge;
  }

  public int getChrono() {
    return chrono;
  }

  public String getCodeProjet() {
    return codeProjet;
  }

  public float getConsomme() {
    return consomme;
  }

  public Date getDateDebut() {
    return dateDebut;
  }

  public Date getDateFin() {
    return dateFin;
  }

  public String getDescription() {
    return description;
  }

  public String getDescriptionProjet() {
    return descriptionProjet;
  }

  public int getEstDecomposee() {
    return estDecomposee;
  }

  public int getId() {
    return id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public int getMereId() {
    return mereId;
  }

  public String getNom() {
    return nom;
  }

  public int getOrganisateurId() {
    return organisateurId;
  }

  public float getRaf() {
    return raf;
  }

  public int getResponsableId() {
    return responsableId;
  }

  public Collection<TaskResourceDetail> getResources() {
    return resources;
  }

  public void setResources(Collection<TaskResourceDetail> resources) {
    this.resources = resources;
  }

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
    if (f != null && !f.isEmpty()) {
      charge = Float.parseFloat(f);
    } else {
      charge = 0;
    }
  }

  public void setChrono(int i) {
    chrono = i;
  }

  public void setCodeProjet(String i) {
    if (i != null && !i.isEmpty()) {
      codeProjet = i;
    } else {
      codeProjet = "-1";
    }
  }

  public void setConsomme(float f) {
    consomme = f;
  }

  public void setConsomme(String f) {
    if (f != null && !f.isEmpty()) {
      consomme = Float.parseFloat(f);
    } else {
      consomme = 0;
    }
  }

  public void setDateDebut(Date string) {
    dateDebut = string;
  }

  public void setDateFin(Date string) {
    dateFin = string;
  }

  public void setDescription(String string) {
    description = string;
  }

  public void setDescriptionProjet(String string) {
    descriptionProjet = string;
  }

  public void setEstDecomposee(int i) {
    estDecomposee = i;
  }

  public void setId(int i) {
    id = i;
  }

  public void setInstanceId(String string) {
    instanceId = string;
  }

  public void setMereId(int i) {
    mereId = i;
  }

  public void setNom(String string) {
    nom = string;
  }

  public void setOrganisateurId(int i) {
    organisateurId = i;
  }

  public void setOrganisateurId(String s) {
    organisateurId = Integer.parseInt(s);
  }

  public void setRaf(float f) {
    raf = f;
  }

  public void setRaf(String f) {
    if (f != null && !f.isEmpty()) {
      raf = Float.parseFloat(f);
    } else {
      raf = 0;
    }
  }

  public void setResponsableId(int i) {
    responsableId = i;
  }

  public void setStatut(int i) {
    statut = i;
  }

  public String getPath() {
    return path;
  }

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
    List<Attendee> attendees = new ArrayList<>();
    attendees.add(attendee);
    todo.setAttendees(attendees);
    todo.setExternalId(Integer.toString(getId()));
    todo.setStartDate(getDateDebut());
    todo.setEndDate(getDateFin());
    todo.setPercentCompleted(getAvancement());
    return todo;
  }

  public void setResourceIds(Collection<TaskResourceDetail> resources) {
    this.resources = resources;
  }

  public String getResponsableFullName() {
    return responsableFullName;
  }

  public void setResponsableFullName(String string) {
    responsableFullName = string;
  }

  public String getUiDateDebut() {
    return uiDateDebut;
  }

  public String getUiDateFin() {
    return uiDateFin;
  }

  public void setUiDateDebut(String string) {
    uiDateDebut = string;
  }

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
    return "TaskDetail {" + "\n" +
           "  id = " + getId() + "\n" +
           "  mereId = " + getMereId() + "\n" +
           "  chrono = " + getChrono() + "\n" +
           "  nom = " + getNom() + "\n" +
           "  organisateurId = " + getOrganisateurId() + "\n" +
           "  responsableId = " + getResponsableId() + "\n" +
           "  charge = " + getCharge() + "\n" +
           "  consomme  = " + getConsomme() + "\n" +
           "  raf = " + getRaf() + "\n" +
           "  avancement  = " + getAvancement() + "\n" +
           "  statut = " + getStatut() + "\n" +
           "  dateDebut = " + getDateDebut() + "\n" +
           "  dateFin = " + getDateFin() + "\n" +
           "  codeProjet = " + getCodeProjet() + "\n" +
           "  estDecomposee = " + getEstDecomposee() + "\n" +
           "  instanceId = " + getInstanceId() + "\n" +
           "  path = " + getPath() + "\n" +
           "}";
  }

  public String getOrganisateurFullName() {
    return organisateurFullName;
  }

  public void setOrganisateurFullName(String string) {
    organisateurFullName = string;
  }

  public List<SimpleDocument> getAttachments() {
    return attachments;
  }

  public void setAttachments(List<SimpleDocument> vector) {
    attachments = vector;
  }

  public boolean isUnfold() {
    return isUnfold;
  }

  public void setUnfold(boolean b) {
    isUnfold = b;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int i) {
    level = i;
  }

  public boolean isDeletionAvailable() {
    return deletionAvailable;
  }

  public boolean isUpdateAvailable() {
    return updateAvailable;
  }

  public void setDeletionAvailable(boolean b) {
    deletionAvailable = b;
  }

  public void setUpdateAvailable(boolean b) {
    updateAvailable = b;
  }

  public int getPreviousTaskId() {
    return previousTaskId;
  }

  public void setPreviousTaskId(int i) {
    previousTaskId = i;
  }

  public String getUiDateDebutPlus1() {
    return uiDateDebutPlus1;
  }

  public void setUiDateDebutPlus1(String string) {
    uiDateDebutPlus1 = string;
  }

  public String getPreviousTaskName() {
    return previousTaskName;
  }

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

  public Contribution asContribution() {
    return new TaskContribution(this);
  }

  private static class TaskContribution implements Contribution {
    private static final long serialVersionUID = -6909201682577518080L;
    private static final String NOT_SUPPORTED_MESSAGE =
        "TaskContribution is not a real " + "Contribution for now";
    private final TaskDetail task;

    private TaskContribution(final TaskDetail task) {
      this.task = task;
    }

    @Override
    public ContributionIdentifier getIdentifier() {
      return ContributionIdentifier
          .from(task.getInstanceId(), String.valueOf(task.getId()), task.getContributionType());
    }

    @Override
    public User getCreator() {
      throw new NotSupportedException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public Date getCreationDate() {
      throw new NotSupportedException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public User getLastUpdater() {
      throw new NotSupportedException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public Date getLastUpdateDate() {
      throw new NotSupportedException(NOT_SUPPORTED_MESSAGE);
    }
  }

  public static class TaskDetailBuilder {

    private final TaskDetail taskDetail;

    private TaskDetailBuilder() {
      this.taskDetail = new TaskDetail();
    }

    public TaskDetailBuilder setId(int id) {
      taskDetail.setId(id);
      return this;
    }

    public TaskDetailBuilder setMereId(int mereId) {
      taskDetail.setMereId(mereId);
      return this;
    }

    public TaskDetailBuilder setChrono(int chrono) {
      taskDetail.setChrono(chrono);
      return this;
    }

    public TaskDetailBuilder setNom(String nom) {
      taskDetail.setNom(nom);
      return this;
    }

    public TaskDetailBuilder setDescription(String description) {
      taskDetail.setDescription(description);
      return this;
    }

    public TaskDetailBuilder setOrganisateurId(int organisateurId) {
      taskDetail.setOrganisateurId(organisateurId);
      return this;
    }

    public TaskDetailBuilder setResponsableId(int responsableId) {
      taskDetail.setResponsableId(responsableId);
      return this;
    }

    public TaskDetailBuilder setCharge(float charge) {
      taskDetail.setCharge(charge);
      return this;
    }

    public TaskDetailBuilder setConsomme(float consomme) {
      taskDetail.setConsomme(consomme);
      return this;
    }

    public TaskDetailBuilder setRaf(float raf) {
      taskDetail.setRaf(raf);
      return this;
    }

    public TaskDetailBuilder setStatut(int statut) {
      taskDetail.setStatut(statut);
      return this;
    }

    public TaskDetailBuilder setDateDebut(Date dateDebut) {
      taskDetail.setDateDebut(dateDebut);
      return this;
    }

    public TaskDetailBuilder setDateFin(Date dateFin) {
      taskDetail.setDateFin(dateFin);
      return this;
    }

    public TaskDetailBuilder setCodeProjet(String codeProjet) {
      taskDetail.setCodeProjet(codeProjet);
      return this;
    }

    public TaskDetailBuilder setDescriptionProjet(String descriptionProjet) {
      taskDetail.setDescriptionProjet(descriptionProjet);
      return this;
    }

    public TaskDetailBuilder setEstDecomposee(int estDecomposee) {
      taskDetail.setEstDecomposee(estDecomposee);
      return this;
    }

    public TaskDetailBuilder setInstanceId(String instanceId) {
      taskDetail.setInstanceId(instanceId);
      return this;
    }

    public TaskDetailBuilder setPath(String path) {
      taskDetail.setPath(path);
      return this;
    }

    public TaskDetail createTaskDetail() {
      return taskDetail;
    }
  }
}
