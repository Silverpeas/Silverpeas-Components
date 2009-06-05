package com.stratelia.silverpeas.infoLetter.model;

import com.stratelia.silverpeas.infoLetter.*;
import com.stratelia.webactiv.util.WAPrimaryKey;
import java.sql.Connection;
import java.util.Vector;

/**
 * Interface declaration
 * 
 * 
 * @author
 */
public interface InfoLetterDataInterface
{

  /**
   * Ouverture de la connection vers
   * la source de donnees
   * @return Connection la connection
   * @exception InfoLetterException
   * @author frageade
   * @since 26 Fevrier 2002
   */
  public Connection openConnection() throws InfoLetterException;

	// Creation d'une lettre d'information
	public void createInfoLetter(InfoLetter ie);

	// Suppression d'une lettre d'information
	public void deleteInfoLetter(WAPrimaryKey pk);

	// Mise a jour d'une lettre d'information
	public void updateInfoLetter(InfoLetter ie);

	// Recuperation de la liste des lettres
	public Vector getInfoLetters(String instanceId);

	// Recuperation de la liste des publications
	public Vector getInfoLetterPublications(WAPrimaryKey letterPK);

	// Creation d'une publication
	public void createInfoLetterPublication(InfoLetterPublicationPdC ilp, String userId);

	// Suppression d'une publication
	public void deleteInfoLetterPublication(WAPrimaryKey pk, String componentId);

	// Mise a jour d'une publication
	public void updateInfoLetterPublication(InfoLetterPublicationPdC ilp);

	// Validation d'une publication
	public void validateInfoLetterPublication(InfoLetterPublication ilp);

	// Recuperation d'une lettre par sa clef
	public InfoLetter getInfoLetter(WAPrimaryKey letterPK);

	// Recuperation d'une publication par sa clef
	public InfoLetterPublicationPdC getInfoLetterPublication(WAPrimaryKey publiPK);

	// Creation de la lettre par defaut a l'instanciation
	public InfoLetter createDefaultLetter(String spaceId, String componentId);

	// Recuperation de la liste des abonnes internes
	public Vector getInternalSuscribers(WAPrimaryKey letterPK);

	// Mise a jour de la liste des abonnes internes
	public void setInternalSuscribers(WAPrimaryKey letterPK, Vector abonnes);

	// Recuperation de la liste des emails externes
	public Vector getExternalsSuscribers(WAPrimaryKey letterPK);

	// Sauvegarde de la liste des emails externes
	public void setExternalsSuscribers(WAPrimaryKey letterPK, Vector emails);

	// abonnement ou desabonnement d'un utilisateur interne
	public void toggleSuscriber(String userId, WAPrimaryKey letterPK, boolean flag);

	// test d'abonnement d'un utilisateur interne
	public boolean isSuscriber(String userId, WAPrimaryKey letterPK);

	// initialisation du template
	public void initTemplate(String spaceId, String componentId, WAPrimaryKey letterPK);

	public int getSilverObjectId(String pubId, String componentId);
}
