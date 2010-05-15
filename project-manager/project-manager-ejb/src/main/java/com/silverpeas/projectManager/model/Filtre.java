/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

/*
 * Created on 8 nov. 2004
 *
 */
package com.silverpeas.projectManager.model;

import java.io.Serializable;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

/**
 * @author neysseri
 */
public class Filtre implements Serializable {

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

  public Filtre(HttpServletRequest request) {
  }

  /**
   * @return
   */
  public String getActionFrom() {
    return actionFrom;
  }

  /**
   * @return
   */
  public String getActionNom() {
    return actionNom;
  }

  /**
   * @return
   */
  public String getActionTo() {
    return actionTo;
  }

  /**
   * @return
   */
  public String getAvancement() {
    return avancement;
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
  public Date getDateDebutFrom() {
    return dateDebutFrom;
  }

  /**
   * @return
   */
  public Date getDateDebutTo() {
    return dateDebutTo;
  }

  /**
   * @return
   */
  public Date getDateFinFrom() {
    return dateFinFrom;
  }

  /**
   * @return
   */
  public Date getDateFinTo() {
    return dateFinTo;
  }

  /**
   * @return
   */
  public Date getDatePVFrom() {
    return datePVFrom;
  }

  /**
   * @return
   */
  public Date getDatePVTo() {
    return datePVTo;
  }

  /**
   * @return
   */
  public String getDescProjet() {
    return descProjet;
  }

  /**
   * @return
   */
  public String getResponsableId() {
    return responsableId;
  }

  /**
   * @return
   */
  public String getRetard() {
    return retard;
  }

  /**
   * @return
   */
  public String getStatut() {
    return statut;
  }

  /**
   * @return
   */
  public String getVisibleMOA() {
    return visibleMOA;
  }

  /**
   * @param string
   */
  public void setActionFrom(String string) {
    actionFrom = string;
  }

  /**
   * @param string
   */
  public void setActionNom(String string) {
    actionNom = string;
  }

  /**
   * @param string
   */
  public void setActionTo(String string) {
    actionTo = string;
  }

  /**
   * @param string
   */
  public void setAvancement(String string) {
    avancement = string;
  }

  /**
   * @param string
   */
  public void setCodeProjet(String string) {
    codeProjet = string;
  }

  /**
   * @param string
   */
  public void setDateDebutFrom(Date date) {
    dateDebutFrom = date;
  }

  /**
   * @param string
   */
  public void setDateDebutTo(Date date) {
    dateDebutTo = date;
  }

  /**
   * @param string
   */
  public void setDateFinFrom(Date date) {
    dateFinFrom = date;
  }

  /**
   * @param string
   */
  public void setDateFinTo(Date date) {
    dateFinTo = date;
  }

  /**
   * @param string
   */
  public void setDatePVFrom(Date date) {
    datePVFrom = date;
  }

  /**
   * @param string
   */
  public void setDatePVTo(Date date) {
    datePVTo = date;
  }

  /**
   * @param string
   */
  public void setDescProjet(String string) {
    descProjet = string;
  }

  /**
   * @param string
   */
  public void setResponsableId(String string) {
    responsableId = string;
  }

  /**
   * @param string
   */
  public void setRetard(String string) {
    retard = string;
  }

  /**
   * @param string
   */
  public void setStatut(String string) {
    statut = string;
  }

  /**
   * @param string
   */
  public void setVisibleMOA(String string) {
    visibleMOA = string;
  }

  /**
   * @return
   */
  public String getDateDebutFromUI() {
    return dateDebutFromUI;
  }

  /**
   * @return
   */
  public String getDateDebutToUI() {
    return dateDebutToUI;
  }

  /**
   * @return
   */
  public String getDateFinFromUI() {
    return dateFinFromUI;
  }

  /**
   * @return
   */
  public String getDateFinToUI() {
    return dateFinToUI;
  }

  /**
   * @return
   */
  public String getDatePVFromUI() {
    return datePVFromUI;
  }

  /**
   * @return
   */
  public String getDatePVToUI() {
    return datePVToUI;
  }

  /**
   * @param string
   */
  public void setDateDebutFromUI(String string) {
    dateDebutFromUI = string;
  }

  /**
   * @param string
   */
  public void setDateDebutToUI(String string) {
    dateDebutToUI = string;
  }

  /**
   * @param string
   */
  public void setDateFinFromUI(String string) {
    dateFinFromUI = string;
  }

  /**
   * @param string
   */
  public void setDateFinToUI(String string) {
    dateFinToUI = string;
  }

  /**
   * @param string
   */
  public void setDatePVFromUI(String string) {
    datePVFromUI = string;
  }

  /**
   * @param string
   */
  public void setDatePVToUI(String string) {
    datePVToUI = string;
  }

  /**
   * @return
   */
  public String getResponsableName() {
    return responsableName;
  }

  /**
   * @param string
   */
  public void setResponsableName(String string) {
    responsableName = string;
  }

}
