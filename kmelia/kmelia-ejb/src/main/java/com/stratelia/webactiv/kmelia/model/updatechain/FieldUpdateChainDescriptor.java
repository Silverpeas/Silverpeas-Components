package com.stratelia.webactiv.kmelia.model.updatechain;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.jsp.JspWriter;

import com.silverpeas.form.FormException;


public class FieldUpdateChainDescriptor 
{
	private String type;
	
	private String name;
	private boolean lastValue;
	private boolean suggestion;
	private String value;
	private List<String> values;
	private int size;
	
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}

	private List<FieldParameter> parameters; 
	
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean getLastValue() {
		return lastValue;
	}
	public void setLastValue(boolean lastValue) {
		this.lastValue = lastValue;
	}
	
	public boolean getSuggestion() {
		return suggestion;
	}
	public void setSuggestion(boolean suggestion) {
		this.suggestion = suggestion;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	 
	public List<String> getValues() {
		return values;
	}
	public void setValues(List<String> values) {
		this.values = values;
	}
	public List<FieldParameter> getParams() {
		return parameters;
	}
	public void setParams(List<FieldParameter> params) {
		this.parameters = params;
	}
	
	public void display(JspWriter jw, FieldsContext fieldsContext, boolean mandatory) throws IOException, FormException
    {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw, true);
		if (type.equals("text"))
		{
			TextFieldDisplayer fieldDisplayer = new TextFieldDisplayer();
			fieldDisplayer.display(out, this, fieldsContext, mandatory);
		}
		else if (type.equals("jdbc"))
		{
			JdbcFieldDisplayer fieldDisplayer = new JdbcFieldDisplayer();
			fieldDisplayer.display(out, this, fieldsContext, mandatory);
		}
		out.flush(); 
		jw.write(sw.toString());
    }

}