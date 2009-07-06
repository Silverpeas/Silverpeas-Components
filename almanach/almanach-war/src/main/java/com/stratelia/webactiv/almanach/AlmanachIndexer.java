package com.stratelia.webactiv.almanach;

//import com.stratelia.webactiv.beans.admin.*;
import com.stratelia.webactiv.almanach.control.AlmanachSessionController;
import com.stratelia.webactiv.applicationIndexer.control.*;
import com.stratelia.webactiv.almanach.control.*;
import com.stratelia.webactiv.almanach.model.*;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import java.util.Iterator;
import com.stratelia.silverpeas.silvertrace.*;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;

public class AlmanachIndexer implements ComponentIndexerInterface {
   
	private AlmanachSessionController scc = null;

    public void index(MainSessionController mainSessionCtrl, ComponentContext context) throws Exception {

		scc = new AlmanachSessionController(mainSessionCtrl, context);

		indexEvents();
    }

	private void indexEvents() throws Exception {
		SilverTrace.info("almanach", "AlmanachIndexer.indexEvents()", "root.MSG_GEN_ENTER_METHOD");

		Iterator it = scc.getAllEvents().iterator();
		while(it.hasNext()){
			EventDetail event = (EventDetail)(it.next());
			
			//index event itself
			scc.indexEvent(event);
			
			//index possible attachments to the event
			AttachmentController.attachmentIndexer(event.getPK());
		}
	}
}