package com.silverpeas.whitePages.html;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.pdc.model.SearchAxis;
import com.stratelia.silverpeas.pdc.model.Value;

public class WhitePagesHtmlTools {

public static String generateHtmlForPdc(List axis, String language, HttpServletRequest request) { 
  StringBuffer result = new StringBuffer("");
  Iterator iter = axis.iterator();
  while(iter.hasNext()){
    result.append("<div>");
    SearchAxis searchAxis = (SearchAxis)iter.next();
      int axisId = searchAxis.getAxisId();
      String    valueInContext    = request.getAttribute("Axis" + String.valueOf(axisId)) != null ? (String)request.getAttribute("Axis" + String.valueOf(axisId)) : null;
      Value   value       = null;
      String    increment     = "";
      String    selected      = "";
      String axisName = searchAxis.getAxisName();
      StringBuffer buffer = new StringBuffer("<select name=\"Axis"+axisId+"\" size=\"1\">");
      buffer.append("<option value=\"\"></option>");
      List values = searchAxis.getValues();
      for (int v=0; v<values.size(); v++)
      {
        value = (Value) values.get(v);
        
        for (int inc=0; inc<value.getLevelNumber(); inc++)
        {
          increment += "&nbsp;&nbsp;&nbsp;&nbsp;";
        }
        
        if (value.getFullPath().equals(valueInContext))
        {
          selected = " selected";
        }
        
        buffer.append("<option value=\""+value.getFullPath()+"\""+selected+">"+increment+value.getName(language));
        buffer.append("</option>");
        
        increment   = "";
        selected  = "";
      }
      buffer.append("</select>");
      result.append("<label class=\"txtlibform\" for=\"Axis");
      result.append(axisId);
      result.append("\">");
      result.append(axisName);
      result.append("</label>");
      result.append(buffer.toString()); 
      result.append("</div>");  
    }
  return result.toString();
}

}