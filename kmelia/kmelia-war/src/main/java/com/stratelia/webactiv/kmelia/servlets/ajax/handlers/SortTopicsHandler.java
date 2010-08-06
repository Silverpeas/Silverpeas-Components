package com.stratelia.webactiv.kmelia.servlets.ajax.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.AjaxHandler;
import com.stratelia.webactiv.util.node.model.NodePK;

public class SortTopicsHandler implements AjaxHandler {

  @Override
  public String handleRequest(HttpServletRequest request, ComponentSessionController controller) {   
    KmeliaSessionController kmelia = ((KmeliaSessionController) controller);
    
    String orderedList = request.getParameter("OrderedList");
    String componentId = kmelia.getComponentId();

    StringTokenizer tokenizer = new StringTokenizer(orderedList, ",");
    List<NodePK> pks = new ArrayList<NodePK>();
    while (tokenizer.hasMoreTokens()) {
      pks.add(new NodePK(tokenizer.nextToken(), componentId));
    }

    // Save order
    try {
      kmelia.getNodeBm().sortNodes(pks);
      return "ok";
    } catch (Exception e) {
      SilverTrace.error("kmelia", "SortTopicsHandler.handleRequest", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

}
