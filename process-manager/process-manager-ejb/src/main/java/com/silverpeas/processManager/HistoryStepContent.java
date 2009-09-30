package com.silverpeas.processManager;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;

public class HistoryStepContent {

  Form form;
  PagesContext pageContext;
  DataRecord record;

  public HistoryStepContent(Form form, PagesContext pageContext,
      DataRecord record) {
    setForm(form);
    setPageContext(pageContext);
    setRecord(record);
  }

  public Form getForm() {
    return form;
  }

  public void setForm(Form form) {
    this.form = form;
  }

  public PagesContext getPageContext() {
    return pageContext;
  }

  public void setPageContext(PagesContext pageContext) {
    this.pageContext = pageContext;
  }

  public DataRecord getRecord() {
    return record;
  }

  public void setRecord(DataRecord record) {
    this.record = record;
  }

}
