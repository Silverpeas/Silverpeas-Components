/*******************************
 *** InfoLetterPublication	 ***
 *** cree par Franck Rageade ***
 *** le 28 Février 2002      ***
 *******************************/

package com.stratelia.silverpeas.infoLetter.model;

// Bibliotheques
import java.util.Iterator;

import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * 
 * @author lbertin
 * @since February 2002
 */
public class InfoLetterPublicationPdC extends InfoLetterPublication implements SilverContentInterface {

	/** icone d'une publication */
	private String iconUrl="infoLetterSmall.gif";

  /**
   * Constructeur sans parametres
   * @author frageade
   * @since February 2002
   */
  public InfoLetterPublicationPdC () {
		super();
  }

  /**
   * Constructeur pour convertir une InfoLetterPublication en InfoLetterPublicationPdc
   * @param ilp		InfoLetterPublication
   * @author lbertin
   * @since February 2002
   */
  public InfoLetterPublicationPdC (InfoLetterPublication ilp) {
		super(ilp.getPK(), ilp.getTitle(), ilp.getDescription(), ilp.getParutionDate(), ilp.getPublicationState(), 
			ilp.getLetterId());
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
  public InfoLetterPublicationPdC (WAPrimaryKey pk, String title, String description, String parutionDate, int publicationState, 
			int letterId) {
		super(pk, title, description, parutionDate, publicationState, letterId);
  }

	//methods to be implemented by SilverContentInterface
	
	public String getName()
	{
		return getTitle();
	}

	public String getURL() 
	{
		return "searchResult?Type=Publication&Id="+getId();
	}

	public String getId() 
	{
		return getPK().getId();
	}

    public String getDate()
	{
		return getParutionDate();
	}

	public String getCreatorId() 
	{
		return null;
	}

	public String getIconUrl() 
	{
		return iconUrl;
	}
	
	public String getSilverCreationDate()
	{
		return getParutionDate();
	}
	
	public String getDescription(String language) {
		return getDescription();
	}

	public String getName(String language) {
		return getName();
	}
	
	public Iterator getLanguages()
	{
		return null;
	}
}
/*************************
 *** Fin du fichier    ***
 ************************/