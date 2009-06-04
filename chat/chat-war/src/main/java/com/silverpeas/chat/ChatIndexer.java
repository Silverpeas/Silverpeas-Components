/*
 * Created on 13 avr. 2005
 *
 */
package com.silverpeas.chat;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;

/**
 * @author neysseri
 *
 */
public class ChatIndexer implements ComponentIndexerInterface {

	public void index(MainSessionController mainSessionCtrl, ComponentContext context) throws Exception {
		//Ce composant n'est pas ré-indexable !!!
		//chat.index();
	}
}