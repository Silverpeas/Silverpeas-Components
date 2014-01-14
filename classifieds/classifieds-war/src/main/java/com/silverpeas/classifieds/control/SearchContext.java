package com.silverpeas.classifieds.control;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.form.XmlSearchForm;

public class SearchContext {
  
  private XmlSearchForm form;
  private DataRecord data;
  
  public SearchContext(XmlSearchForm form, DataRecord data) {
    setForm(form);
    setData(data);
  }
  
  public void setData(DataRecord data) {
    this.data = data;
  }
  public DataRecord getData() {
    return data;
  }
  
  public void setForm(XmlSearchForm form) {
    this.form = form;
  }
  public XmlSearchForm getForm() {
    return form;
  }
}
