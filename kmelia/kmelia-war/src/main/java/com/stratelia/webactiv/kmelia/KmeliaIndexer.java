package com.stratelia.webactiv.kmelia;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;

public class KmeliaIndexer implements ComponentIndexerInterface {
     
    private KmeliaSessionController scc = null;
    
    public void index(MainSessionController mainSessionCtrl, ComponentContext context) throws Exception {
		scc = new KmeliaSessionController(mainSessionCtrl, context);
	
		scc.indexKmelia();
	}
}