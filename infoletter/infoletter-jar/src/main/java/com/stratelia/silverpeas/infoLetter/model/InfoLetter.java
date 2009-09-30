/*******************************
 *** InfoLetter				 ***
 *** cree par Franck Rageade ***
 *** le 28 Février 2002      ***
 *******************************/

package com.stratelia.silverpeas.infoLetter.model;

// Bibliotheques
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import java.util.Vector;

/**
 * 
 * @author frageade
 * @since February 2002
 */
public class InfoLetter extends SilverpeasBean implements Comparable {

  // Membres

  /** id de l'instance */
  private String instanceId;

  /** nom de la liste */
  private String name;

  /** description de la liste */
  private String description;

  /** periodicite de la liste */
  private String periode;

  /** abonnes internes */
  private Vector internalSuscribers;

  /** abonnes externes */
  private Vector externalSuscribers;

  /** publications */
  private Vector publications;

  // Constructeurs

  /**
   * Constructeur sans parametres
   * 
   * @author frageade
   * @since February 2002
   */
  public InfoLetter() {
    super();
    instanceId = "";
    name = "";
    description = "";
    periode = "";
    internalSuscribers = new Vector();
    externalSuscribers = new Vector();
    publications = new Vector();
  }

  /**
   * Constructeur a 8 parametres
   * 
   * @param WAPrimaryKey
   *          pk
   * @param String
   *          name
   * @param String
   *          description
   * @param String
   *          periode
   * @param Vector
   *          internalSuscribers
   * @param Vector
   *          externalSuscribers
   * @param Vector
   *          publications
   * @author frageade
   * @since February 2002
   */
  public InfoLetter(WAPrimaryKey pk, String instanceId, String name,
      String description, String periode, Vector internalSuscribers,
      Vector externalSuscribers, Vector publications) {
    super();
    setPK(pk);
    this.instanceId = instanceId;
    this.name = name;
    this.description = description;
    this.periode = periode;
    this.internalSuscribers = internalSuscribers;
    this.externalSuscribers = externalSuscribers;
    this.publications = publications;
  }

  // Assesseurs

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getName() {
    return name;
  }

  public void setName(String n) {
    name = n;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPeriode() {
    return periode;
  }

  public void setPeriode(String periode) {
    this.periode = periode;
  }

  public Vector readInternalSuscribers() {
    return internalSuscribers;
  }

  public void writeInternalSuscribers(Vector internalSuscribers) {
    this.internalSuscribers = internalSuscribers;
  }

  public Vector readExternalSuscribers() {
    return externalSuscribers;
  }

  public void writeExternalSuscribers(Vector externalSuscribers) {
    this.externalSuscribers = externalSuscribers;
  }

  public Vector readPublications() {
    return publications;
  }

  public void writePublications(Vector publications) {
    this.publications = publications;
  }

  // Methodes

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public int compareTo(Object obj) {
    if (!(obj instanceof InfoLetter))
      return 0;
    return (String.valueOf(getPK().getId())).compareTo(String
        .valueOf(((InfoLetter) obj).getPK().getId()));
  }

  public String _getTableName() {
    return "SC_IL_Letter";
  }

}

/*************************
 *** Fin du fichier ***
 ************************/
