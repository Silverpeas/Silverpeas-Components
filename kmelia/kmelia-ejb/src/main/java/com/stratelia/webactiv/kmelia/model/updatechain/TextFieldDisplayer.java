package com.stratelia.webactiv.kmelia.model.updatechain;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.form.FormException;
import com.silverpeas.form.Util;
import com.silverpeas.util.EncodeHelper;

public class TextFieldDisplayer 
{
	/**
     * Constructeur
     */
	public TextFieldDisplayer()
	{
		
	}
	
    public void display(PrintWriter out, FieldUpdateChainDescriptor field, FieldsContext fieldsContext, boolean mandatory)
    	throws FormException
    {
    	String mandatoryImg = Util.getIcon("mandatoryField");
    	List values = field.getValues();
    	String value = field.getValue();
    	if (!field.getLastValue())
    		value = "";
  		if (values != null && values.size() > 0) 
  		{ 
  			out.println("<select name=\""+field.getName()+"\">");
		    Iterator it = values.iterator();
		    while (it.hasNext())
		    {
		    	String currentValue = (String) it.next(); 
		    	String selected = "";
				if (currentValue.equals(field.getName()))
					selected = "selected";

				out.println("<option value=\""+EncodeHelper.javaStringToHtmlString(currentValue)+"\" "+selected+">"
            +EncodeHelper.javaStringToHtmlString(currentValue)+"</option>");

  			}
		    out.println("</select>");
  		} 
  		else 
  		{
  			out.println("<input type=\"text\" size=\""+field.getSize() + "\" name=\""+field.getName()+"\" value=\""
            +EncodeHelper.javaStringToHtmlString(value)+"\" size=\"60\" maxlength=\"60\">");
  		}
  		
  		if (mandatory) 
		{
			out.println("<TD><img src=\"" + mandatoryImg + "\" width=\"5\" height=\"5\" border=\"0\" ><TD>");
		}
		
    }
  
}
