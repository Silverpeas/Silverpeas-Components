package com.silverpeas.mailinglist.service.util;

import java.io.IOException;
import java.io.Reader;

public interface HtmlCleaner {
  public void setSummarySize(int size);

  public String getSummary();

  public void parse(Reader in) throws IOException;
}
