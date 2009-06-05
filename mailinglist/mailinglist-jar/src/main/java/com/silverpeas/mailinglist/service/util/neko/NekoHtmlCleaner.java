package com.silverpeas.mailinglist.service.util.neko;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.HTMLTagBalancer;
import org.cyberneko.html.filters.ElementRemover;
import org.cyberneko.html.filters.Writer;

import com.silverpeas.mailinglist.service.util.HtmlCleaner;

public class NekoHtmlCleaner implements HtmlCleaner {
  private StringWriter content;
  private XMLParserConfiguration parser;
  private int maxSize = 0;

  public NekoHtmlCleaner() {
    ElementRemover remover = new NekoElementRemover();
    remover.removeElement("script");
    remover.removeElement("title");
    remover.removeElement("link");
    remover.removeElement("meta");
    remover.removeElement("select");
    remover.acceptElement("br", null);
    content = new StringWriter();
    
    Writer writer = new EntityReplaceWriter(content, "UTF-8");
    XMLDocumentFilter[] filters = {new HTMLTagBalancer(), remover, writer, };
    parser = new HTMLConfiguration();
    parser.setProperty("http://cyberneko.org/html/properties/filters", filters);
    parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
  }

  @Override
  public String getSummary() {
    String buffer = content.toString();
    buffer = buffer.trim();
    buffer = buffer.replaceAll("<[B,b][R,r]>", " ");
    buffer = buffer.replaceAll("<[B,b][R,r]/>", " ");
    buffer = buffer.replaceAll("\\s[\\s]*", " ");
    if (buffer.length() <= maxSize) {
      return buffer;
    }
    return buffer.substring(0, maxSize);
  }

  @Override
  public void parse(Reader in) throws IOException {
    XMLInputSource source = new XMLInputSource("-//W3C//DTD HTML 4.01", null,
        null, in, "ISO-8859-1");
    parser.parse(source);    
  }

  @Override
  public void setSummarySize(int size) {
    this.maxSize = size;
  }

}
