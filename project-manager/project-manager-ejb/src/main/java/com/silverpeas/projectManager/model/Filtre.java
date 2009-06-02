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
 *
 */
public class Filtre implements Serializable {
	
	private String 	actionFrom 		= null;
	private String 	actionTo		= null;
	private String 	codeProjet		= null;
	private String 	descProjet		= null;
	private String 	actionNom		= null;
	private String 	statut			= null;
	private Date 	datePVFrom		= null;
	private Date 	datePVTo		= null;
	private Date 	dateDebutFrom	= null;
	private Date 	dateDebutTo		= null;
	private Date 	dateFinFrom		= null;
	private Date 	dateFinTo		= null;
	private String 	datePVFromUI	= null;
	private String 	datePVToUI		= null;
	private String 	dateDebutFromUI	= null;
	private String 	dateDebutToUI	= null;
	private String 	dateFinFromUI	= null;
	private String 	dateFinToUI		= null;
	private String 	retard			= null;
	private String 	avancement		= null;
	private String 	responsableId	= null;
	private String	responsableName = null;
	private String 	visibleMOA		= null;
	
	//private Date	today			= new Date();
		
	public Filtre(HttpServletRequest request) 
	{
	}
	
	/*public String getSQL()
	{
		StringBuffer sql = new StringBuffer();
		
		if (getActionFrom() != null && getActionFrom().length()>0)
			sql.append(" chrono >= ").append(getActionFrom());
		
		if (getActionTo() != null && getActionTo().length()>0) {
			if (sql.length() > 0)
				sql.append(" AND ");
			sql.append(" chrono <= ").append(getActionTo());
		}

		if (getCodeProjet() != null && getCodeProjet().length()>0) {
			if (sql.length() > 0)
				sql.append(" AND ");
			sql.append(" codeProjet = ").append(getCodeProjet());
		}
			
		if (getDescProjet() != null && getDescProjet().length()>0) {
			if (sql.length() > 0)
				sql.append(" AND ");
			sql.append(" descriptionProjet like '%").append(getDescProjet()).append("%' ");
		}
			
		if (getActionNom() != null && getActionNom().length()>0) {
			if (sql.length() > 0)
				sql.append(" AND ");
			sql.append(" nom like '%").append(getActionNom()).append("%' ");
		}
			
		if (getStatut() != null && !getStatut().equals("-1"))
		{
			if (sql.length() > 0)
				sql.append(" AND ");
			sql.append(" statut = ").append(getStatut());
		}
			
		if (getDatePVFrom() != null)
		{
			if (sql.length() > 0)
				sql.append(" AND ");
			sql.append(" datePV >= '").append(ProjectManagerDAO.date2DBDate(getDatePVFrom())).append("' ");
		}
		if (getDatePVTo() != null)
		{
			if (sql.length() > 0)
			sql.append(" AND ");
			sql.append(" datePV <= '").append(ProjectManagerDAO.date2DBDate(getDatePVTo())).append("' ");
		}
			
		if (getDateDebutFrom() != null)
		{
			if (sql.length() > 0)
				sql.append(" AND ");
			sql.append(" dateDebut >= '").append(ProjectManagerDAO.date2DBDate(getDateDebutFrom())).append("' ");
		}
			
		if (getDateDebutTo() != null)
		{
			if (sql.length() > 0)
				sql.append(" AND ");
			sql.append(" dateDebut <= '").append(ProjectManagerDAO.date2DBDate(getDateDebutTo())).append("' ");
		}
					
		if (getDateFinFrom() != null)
		{
			if (sql.length() > 0)
				sql.append(" AND ");
			sql.append(" dateFin >= '").append(ProjectManagerDAO.date2DBDate(getDateFinFrom())).append("' ");
		}
			
		if (getDateFinTo() != null)
		{
			if (sql.length() > 0)
				sql.append(" AND ");
			sql.append(" dateFin <= '").append(ProjectManagerDAO.date2DBDate(getDateFinTo())).append("' ");
		}
			
		if (getRetard() != null && !getRetard().equals("-1"))
		{
			if (sql.length() > 0)
				sql.append(" AND ");
			if (getRetard().equals("1"))
			{
				//les tasks en retard
				sql.append(" dateFin < '").append(ProjectManagerDAO.date2DBDate(today)).append("' ");
			}
			else
			{
				//les tasks qui ne sont pas en retard
				sql.append(" dateFin >= '").append(ProjectManagerDAO.date2DBDate(today)).append("' ");
			}
		}
		
		if (getAvancement() != null && !getAvancement().equals("-1"))
		{
			if (sql.length() > 0)
				sql.append(" AND ");
			if (getAvancement().equals("1"))
			{
				//les tasks terminées
				sql.append(" avancement = 100 ");
			}
			else
			{
				//les tasks non terminées
				sql.append(" avancement < 100 ");
			}
		}
		
		if (getResponsableId() != null && getResponsableId().length()>0)
		{
			if (sql.length() > 0)
				sql.append(" AND ");
			sql.append(" responsableId = ").append(getResponsableId());
		}
			
		if (getVisibleMOA() != null && !getVisibleMOA().equals("-1"))
		{
			if (sql.length() > 0)
				sql.append(" AND ");
			if (getVisibleMOA().equals("1"))
			{
				//les tasks visibles par la MOA
				sql.append(" visibleMOA = 1 ");
			}
			else
			{
				//les tasks non visibles par la MOA
				sql.append(" visibleMOA = 0 ");
			}
		}
			
		return sql.toString();
	}*/

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
