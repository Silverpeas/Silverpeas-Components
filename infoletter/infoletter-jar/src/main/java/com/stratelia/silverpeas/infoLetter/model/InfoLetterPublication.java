/*******************************
 *** InfoLetterPublication	 ***
 *** cree par Franck Rageade ***
 *** le 28 Février 2002      ***
 *******************************/

package com.stratelia.silverpeas.infoLetter.model;

// Bibliotheques
import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * 
 * @author frageade
 * @since February 2002
 */
public class InfoLetterPublication extends SilverpeasBean implements Comparable {

		public final static int PUBLICATION_EN_REDACTION = 1;
		public final static int PUBLICATION_VALIDEE = 2;

		public static final String TEMPLATE_ID = "template";

  // Membres

  		/** id de l'instance */
		private String instanceId;
  
		/** titre de la publication */
		private String title;

		/** description de la publication */
		private String description;

		/** date de parution */
		private String parutionDate;

		/** etat de la publication */
		private int publicationState;

		/** id de la lettre */
		private int letterId;

	// Constructeurs

  /**
   * Constructeur sans parametres
   * @author frageade
   * @since February 2002
   */
  public InfoLetterPublication () {
		super();
		title = "";
		description = "";
		parutionDate = "";
		publicationState = PUBLICATION_EN_REDACTION;
		letterId = 0;
  }

  /**
   * Constructeur a 6 parametres
   * @param WAPrimaryKey pk
   * @param String title
   * @param String description
   * @param String parutionDate
   * @param int publicationState
   * @param String letterId
   * @author frageade
   * @since February 2002
   */
  public InfoLetterPublication (WAPrimaryKey pk, String title, String description, String parutionDate, int publicationState, 
			int letterId) {
		super();
		setPK(pk);
		this.title = title;
		this.description = description;
		this.parutionDate = parutionDate;
		this.publicationState = publicationState;
		this.letterId = letterId;
  }

  // Assesseurs

  public String getInstanceId() {
	return instanceId;
  }

  public void setInstanceId(String instanceId) {
	this.instanceId = instanceId;
  }
  
  public String getTitle() {
	return title;
  }

  public void setTitle(String title) {
	this.title = title;
  }

  public String getDescription() {
	return description;
  }

  public void setDescription(String description) {
	this.description = description;
  }

  public String getParutionDate() {
	return parutionDate;
  }

  public void setParutionDate(String parutionDate) {
	this.parutionDate = parutionDate;
  }

  public int getPublicationState() {
	return publicationState;
  }

  public void setPublicationState(int publicationState) {
	this.publicationState = publicationState;
  }

  public int getLetterId() {
	return letterId;
  }

  public void setLetterId(int letterId) {
	this.letterId = letterId;
  }
  
  public void setLetterId(String letterId) {
	this.letterId = Integer.parseInt(letterId);
  }

  // Methodes
  
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public int compareTo(Object obj) {
    if (! (obj instanceof InfoLetter))
      return 0;
    return (String.valueOf(getPK().getId())).compareTo(String.valueOf(((InfoLetter)obj).getPK().getId()));
  }

  public String _getTableName() {
	return "SC_IL_Publication";
  }

  public boolean _isValid() {
	return (publicationState == PUBLICATION_VALIDEE);
  }
}
/*************************
 *** Fin du fichier    ***
 ************************/