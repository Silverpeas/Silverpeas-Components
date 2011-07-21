package com.silverpeas.crm.model;

import java.sql.Connection;
import java.util.ArrayList;

import com.silverpeas.crm.CrmException;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Interface declaration
 *
 *
 * @author
 */
public interface CrmDataInterface
{

  /**
   * Ouverture de la connection vers
   * la source de donnees
   * @return Connection la connection
   * @exception CrmException
   */
  public Connection openConnection() throws CrmException;

	// Creation du crm par defaut a l'instanciation
	public Crm createDefaultCrm(String spaceId, String componentId);

	// Creation d'un crm
	public void createCrm(Crm crm);

	// Suppression d'un crm
	public void deleteCrm(WAPrimaryKey pk);

	// Mise a jour d'un crm
	public void updateCrm(Crm crm);

	// Recuperation d'un crm par sa clef
	public Crm getCrm(WAPrimaryKey crmPK);

	// Recuperation de la liste des crm
	public ArrayList<Crm> getCrms(String instanceId);

	// Recuperation de la liste des contatcs
	public ArrayList<CrmContact> getCrmContacts(WAPrimaryKey crmPK);

	// Creation d'un contact
	public void createCrmContact(CrmContact contact);

	// Suppression d'un contact
	public void deleteCrmContact(WAPrimaryKey pk, String componentId);

	// Mise a jour d'un contact
	public void updateCrmContact(CrmContact contact);

	// Recuperation d'un contact par sa clef
	public CrmContact getCrmContact(WAPrimaryKey contactPK);

	// Recuperation de la liste participants
	public ArrayList<CrmParticipant> getCrmParticipants(WAPrimaryKey crmPK);

	// Recuperation d'un participant par sa clef
	public CrmParticipant getCrmParticipant(WAPrimaryKey participantPK);

	// Creation d'un participant
	public void createCrmParticipant(CrmParticipant participant);

	// Suppression d'un participant
	public void deleteCrmParticipant(WAPrimaryKey participantPK, String componentId);

	// Mise a jour d'un contact
	public void updateCrmParticipant(CrmParticipant participant);

    // Recuperation d'un événement par sa clef
    public CrmDelivery getCrmDelivery(WAPrimaryKey deliveryPK);

	// Recuperation de la liste délivrables
	public ArrayList<CrmDelivery> getCrmDeliverys(WAPrimaryKey crmPK);

	// Creation d'un délivrable
	public void createCrmDelivery(CrmDelivery delivery);

	// Suppression d'un délivrable
	public void deleteCrmDelivery(WAPrimaryKey deliveryPK, String componentId);

	// Mise a jour d'un délivrable
	public void updateCrmDelivery(CrmDelivery delivery);

    // Recuperation d'un événement par sa clef
    public CrmEvent getCrmEvent(WAPrimaryKey eventPK);

	// Recuperation de la liste événements
	public ArrayList<CrmEvent> getCrmEvents(WAPrimaryKey crmPK);

	// Creation d'un événement
	public void createCrmEvent(CrmEvent event);

	// Suppression d'un événement
	public void deleteCrmEvent(WAPrimaryKey eventPK, String componentId);

	// Mise a jour d'un événement
	public void updateCrmEvent(CrmEvent event);


	//public int getSilverObjectId(String pubId, String componentId);
}
