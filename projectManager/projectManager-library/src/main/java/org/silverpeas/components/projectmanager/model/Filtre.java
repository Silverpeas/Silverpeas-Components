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

/*
 * Created on 8 nov. 2004
 *
 */
package org.silverpeas.components.projectmanager.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import static java.lang.Integer.parseInt;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.silverpeas.kernel.util.StringUtil.likeIgnoreCase;

/**
 * @author neysseri
 */
public class Filtre implements Serializable {

  private static final long serialVersionUID = -5172014425250531057L;
  private static final String FROM_ACTION = "FROM_ACTION";
  private static final String TO_ACTION = "TO_ACTION";
  private static final String PROJECT_CODE = "PROJECT_CODE";
  private static final String PROJECT_DESCRIPTION = "PROJECT_DESCRIPTION";
  private static final String ACTION_NAME = "ACTION_NAME";
  private static final String STATUS = "STATUS";
  private static final String FROM_START_DATE = "FROM_START_DATE";
  private static final String TO_START_DATE = "TO_START_DATE";
  private static final String FROM_END_DATE = "FROM_END_DATE";
  private static final String TO_END_DATE = "TO_END_DATE";
  private static final String WITH_DELAY = "WITH_DELAY";
  private static final String WITHOUT_DELAY = "WITHOUT_DELAY";
  private static final String ENDED = "ENDED";
  private static final String NOT_ENDED = "NOT_ENDED";
  private static final String TASK_MANAGER = "TASK_MANAGER";
  private String actionFrom = null;
  private String actionTo = null;
  private String codeProjet = null;
  private String descProjet = null;
  private String actionNom = null;
  private String statut = null;
  private Date datePVFrom = null;
  private Date datePVTo = null;
  private Date dateDebutFrom = null;
  private Date dateDebutTo = null;
  private Date dateFinFrom = null;
  private Date dateFinTo = null;
  private String datePVFromUI = null;
  private String datePVToUI = null;
  private String dateDebutFromUI = null;
  private String dateDebutToUI = null;
  private String dateFinFromUI = null;
  private String dateFinToUI = null;
  private String retard = null;
  private String avancement = null;
  private String responsableId = null;
  private String responsableName = null;
  private String visibleMOA = null;

  public String getActionFrom() {
    return actionFrom;
  }

  public String getActionNom() {
    return actionNom;
  }

  public String getActionTo() {
    return actionTo;
  }

  public String getAvancement() {
    return avancement;
  }

  public String getCodeProjet() {
    return codeProjet;
  }

  public Date getDateDebutFrom() {
    return dateDebutFrom;
  }

  public Date getDateDebutTo() {
    return dateDebutTo;
  }

  public Date getDateFinFrom() {
    return dateFinFrom;
  }

  public Date getDateFinTo() {
    return dateFinTo;
  }

  public Date getDatePVFrom() {
    return datePVFrom;
  }

  public Date getDatePVTo() {
    return datePVTo;
  }

  public String getDescProjet() {
    return descProjet;
  }

  public String getResponsableId() {
    return responsableId;
  }

  public String getRetard() {
    return retard;
  }

  public String getStatut() {
    return statut;
  }

  public String getVisibleMOA() {
    return visibleMOA;
  }

  public void setActionFrom(String string) {
    actionFrom = string;
  }

  public void setActionNom(String string) {
    actionNom = string;
  }

  public void setActionTo(String string) {
    actionTo = string;
  }

  public void setAvancement(String string) {
    avancement = string;
  }

  public void setCodeProjet(String string) {
    codeProjet = string;
  }

  public void setDateDebutFrom(Date date) {
    dateDebutFrom = date;
  }

  public void setDateDebutTo(Date date) {
    dateDebutTo = date;
  }

  public void setDateFinFrom(Date date) {
    dateFinFrom = date;
  }

  public void setDateFinTo(Date date) {
    dateFinTo = date;
  }

  public void setDatePVFrom(Date date) {
    datePVFrom = date;
  }

  public void setDatePVTo(Date date) {
    datePVTo = date;
  }

  public void setDescProjet(String string) {
    descProjet = string;
  }

  public void setResponsableId(String string) {
    responsableId = string;
  }

  public void setRetard(String string) {
    retard = string;
  }

  public void setStatut(String string) {
    statut = string;
  }

  public void setVisibleMOA(String string) {
    visibleMOA = string;
  }

  public String getDateDebutFromUI() {
    return dateDebutFromUI;
  }

  public String getDateDebutToUI() {
    return dateDebutToUI;
  }

  public String getDateFinFromUI() {
    return dateFinFromUI;
  }

  public String getDateFinToUI() {
    return dateFinToUI;
  }

  public String getDatePVFromUI() {
    return datePVFromUI;
  }

  public String getDatePVToUI() {
    return datePVToUI;
  }

  public void setDateDebutFromUI(String string) {
    dateDebutFromUI = string;
  }

  public void setDateDebutToUI(String string) {
    dateDebutToUI = string;
  }

  public void setDateFinFromUI(String string) {
    dateFinFromUI = string;
  }

  public void setDateFinToUI(String string) {
    dateFinToUI = string;
  }

  public void setDatePVFromUI(String string) {
    datePVFromUI = string;
  }

  public void setDatePVToUI(String string) {
    datePVToUI = string;
  }

  public String getResponsableName() {
    return responsableName;
  }

  public void setResponsableName(String string) {
    responsableName = string;
  }

  private static final Map<String, BiPredicate<Filtre, TaskDetail>> MATCHING_PREDICATES = ofEntries(
      entry(FROM_ACTION, (f, t) -> t.getChrono() >= parseInt(f.getActionFrom())),
      entry(TO_ACTION, (f, t) -> t.getChrono() <= parseInt(f.getActionTo())),
      entry(PROJECT_CODE, (f, t) -> f.getCodeProjet().equals(t.getCodeProjet())),
      entry(PROJECT_DESCRIPTION, (f, t) -> likeIgnoreCase(t.getDescriptionProjet(), "%" + f.getDescProjet() + "%")),
      entry(ACTION_NAME, (f, t) -> likeIgnoreCase(t.getNom(), "%" + f.getActionNom() + "%")),
      entry(STATUS, (f, t) -> parseInt(f.getStatut()) == t.getStatut()),
      entry(FROM_START_DATE, (f, t) -> t.getDateDebut().compareTo(f.getDateDebutFrom()) >= 0),
      entry(TO_START_DATE, (f, t) -> t.getDateDebut().compareTo(f.getDateDebutTo()) <= 0),
      entry(FROM_END_DATE, (f, t) -> t.getDateFin().compareTo(f.getDateFinFrom()) >= 0),
      entry(TO_END_DATE, (f, t) -> t.getDateFin().compareTo(f.getDateFinTo()) <= 0),
      entry(WITH_DELAY, (f, t) -> t.getDateFin().compareTo(new Date()) < 0 && t.getAvancement() == 100),
      entry(WITHOUT_DELAY, (f, t) -> t.getDateFin().compareTo(new Date()) >= 0 && t.getAvancement() == 100),
      entry(ENDED, (f, t) -> t.getAvancement() == 100),
      entry(NOT_ENDED, (f, t) -> t.getAvancement() < 100),
      entry(TASK_MANAGER, (f, t) -> parseInt(f.getResponsableId()) == t.getResponsableId()));

  /**
   * Indicates if the given task matches the registered filters.
   * @param task the task to check.
   * @return true if the filters matches, false otherwise.
   */
  public boolean matches(final TaskDetail task) {
    final List<String> predicates = new ArrayList<>();
    addFromStringFilter(predicates, getActionFrom(), FROM_ACTION);
    addFromStringFilter(predicates, getActionTo(), TO_ACTION);
    addFromStringFilter(predicates, getCodeProjet(), PROJECT_CODE);
    addFromStringFilter(predicates, getDescProjet(), PROJECT_DESCRIPTION);
    addFromStringFilter(predicates, getActionNom(), ACTION_NAME);
    addFromIntegerFilter(predicates, getStatut());
    if (getDateDebutFrom() != null) {
      predicates.add(FROM_START_DATE);
    }
    if (getDateDebutTo() != null) {
      predicates.add(TO_START_DATE);
    }
    if (getDateFinFrom() != null) {
      predicates.add(FROM_END_DATE);
    }
    if (getDateFinTo() != null) {
      predicates.add(TO_END_DATE);
    }
    if (getRetard() != null && !"-1".equals(getRetard())) {
      if ("1".equals(getRetard())) {
        predicates.add(WITH_DELAY);
      } else {
        predicates.add(WITHOUT_DELAY);
      }
    }
    if (getAvancement() != null && !"-1".equals(getAvancement())) {
      if ("1".equals(getAvancement())) {
        predicates.add(ENDED);
      } else {
        predicates.add(NOT_ENDED);
      }
    }
    addFromStringFilter(predicates, getResponsableId(), TASK_MANAGER);
    return predicates.stream().map(MATCHING_PREDICATES::get).allMatch(p -> p.test(this, task));
  }

  private void addFromStringFilter(final List<String> predicates, final String filter,
      final String predicateKey) {
    if (isNotEmpty(filter)) {
      predicates.add(predicateKey);
    }
  }

  private void addFromIntegerFilter(final List<String> predicates, final String filter) {
    if (isNotEmpty(filter) && !"-1".equals(filter)) {
      predicates.add(Filtre.STATUS);
    }
  }
}
