package com.silverpeas.mailinglist.service.util.neko;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.cyberneko.html.HTMLEntities;
import org.cyberneko.html.filters.Writer;

public class EntityReplaceWriter extends Writer {

  /** Constructs a writer filter that prints to standard out. */
  public EntityReplaceWriter() {
    super();
  }

  /**
   * Constructs a writer filter using the specified output stream and encoding.
   * 
   * @param outputStream
   *          The output stream to write to.
   * @param encoding
   *          The encoding to be used for the output. The encoding name should
   *          be an official IANA encoding name.
   */
  public EntityReplaceWriter(OutputStream outputStream, String encoding)
      throws UnsupportedEncodingException {
    this(new OutputStreamWriter(outputStream, encoding), encoding);
  }

  /**
   * Constructs a writer filter using the specified Java writer and encoding.
   * 
   * @param writer
   *          The Java writer to write to.
   * @param encoding
   *          The encoding to be used for the output. The encoding name should
   *          be an official IANA encoding name.
   */
  public EntityReplaceWriter(java.io.Writer writer, String encoding) {
    super(writer, encoding);
  }

  protected void printEntity(String name) {
    char entity = (char) HTMLEntities.get(name);
    if (Character.isWhitespace(entity) || "nbsp".equalsIgnoreCase(name)) {
      entity = ' ';
    }
    super.fPrinter.print(entity);
    super.fPrinter.flush();
  }
}
