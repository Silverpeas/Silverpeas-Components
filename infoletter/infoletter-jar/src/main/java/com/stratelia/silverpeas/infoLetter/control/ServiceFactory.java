/*******************************
 *** ServiceFactory          ***
 *** cree par Franck Rageade ***
 *** le 26 Fevrier 2002      ***
 *******************************/

package com.stratelia.silverpeas.infoLetter.control;

// Bibliotheques

import com.stratelia.silverpeas.infoLetter.implementation.*;
import com.stratelia.silverpeas.infoLetter.model.*;

/**
 * Cette classe est reponsable de la fabrication des services.
 * 
 * @author frageade
 * @since February 2002
 */
public class ServiceFactory {

  // Membres

  /**
   * Constructeur sans parametres
   * 
   * @author frageade
   * @since February 2002
   */
  public ServiceFactory() {
  }

  // Methodes

  public static InfoLetterDataInterface getInfoLetterData() {
    return new InfoLetterDataManager();
  }

}

/*************************
 *** Fin du fichier ***
 ************************/
