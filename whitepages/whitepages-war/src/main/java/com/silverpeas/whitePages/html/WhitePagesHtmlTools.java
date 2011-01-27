package com.silverpeas.whitePages.html;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.pdc.model.SearchAxis;
import com.stratelia.silverpeas.pdc.model.Value;

public class WhitePagesHtmlTools {

public static String generateHtmlForPdc(List<SearchAxis> axis, String language, HttpServletRequest request) { 
  StringBuffer result = new StringBuffer("");
  for (SearchAxis searchAxis : axis) {
    result.append("<div>");
      int axisId = searchAxis.getAxisId();
      String    valueInContext    = request.getAttribute("Axis" + String.valueOf(axisId)) != null ? (String)request.getAttribute("Axis" + String.valueOf(axisId)) : null;
      String    increment     = "";
      String    selected      = "";
      String axisName = searchAxis.getAxisName();
      StringBuffer buffer = new StringBuffer("<select name=\"Axis"+axisId+"\" size=\"1\">");
      buffer.append("<option value=\"\"></option>");
      List<Value> values = searchAxis.getValues();
      for (Value value : values) {
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