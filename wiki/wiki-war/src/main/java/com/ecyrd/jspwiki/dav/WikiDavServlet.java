/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ecyrd.jspwiki.dav;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.dav.methods.DavMethod;
import com.ecyrd.jspwiki.dav.methods.GetMethod;
import com.ecyrd.jspwiki.dav.methods.PropFindMethod;

/**
 * @since
 */
public class WikiDavServlet extends WebdavServlet {
  private static final long serialVersionUID = 1L;

  private WikiEngine m_engine;
  Logger log = Logger.getLogger(this.getClass().getName());
  private DavProvider m_rawProvider;
  private DavProvider m_rootProvider;
  private DavProvider m_htmlProvider;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    m_engine = WikiEngine.getInstance(config);

    m_rawProvider = new RawPagesDavProvider(m_engine);
    m_rootProvider = new WikiRootProvider(m_engine);
    m_htmlProvider = new HTMLPagesDavProvider(m_engine);
  }

  private DavProvider pickProvider(String context) {
    if (context.equals("raw"))
      return m_rawProvider;
    else if (context.equals("html"))
      return m_htmlProvider;

    return m_rootProvider;
  }

  @Override
  public void doPropFind(HttpServletRequest req, HttpServletResponse res)
      throws IOException, ServletException {
    StopWatch sw = new StopWatch();
    sw.start();

    // Do the "sanitize url" trick
    String p = new String(req.getPathInfo().getBytes("ISO-8859-1"), "UTF-8");

    DavPath path = new DavPath(p);
    if (path.isRoot()) {
      DavMethod dm = new PropFindMethod(m_rootProvider);
      dm.execute(req, res, path);
    } else {
      String context = path.get(0);

      PropFindMethod m = new PropFindMethod(pickProvider(context));
      m.execute(req, res, path.subPath(1));
    }

    sw.stop();

    log.debug("Propfind done for path " + path + ", took " + sw);
  }

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse res) {
    log.debug("DAV doOptions for path " + req.getPathInfo());

    res.setHeader("DAV", "1"); // We support only Class 1
    res.setHeader("Allow",
        "GET, PUT, POST, OPTIONS, PROPFIND, PROPPATCH, MOVE, COPY, DELETE");
    res.setStatus(HttpServletResponse.SC_OK);
  }

  @Override
  public void doMkCol(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if (request.getContentLength() > 0) {
      response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
          "Message may contain no body");
    } else {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
          "JSPWiki is read-only.");
    }
  }

  @Override
  public void doPropPatch(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    // DavMethod dm = new PropPatchMethod( m_rawProvider );

    // dm.execute( request, response );
  }

  @Override
  public void doCopy(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "JSPWiki is read-only.");
  }

  @Override
  public void doMove(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "JSPWiki is read-only.");

  }

  @Override
  protected void doDelete(HttpServletRequest arg0, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "JSPWiki is read-only.");
  }

  @Override
  protected void doPost(HttpServletRequest arg0, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "JSPWiki is read-only.");
  }

  @Override
  protected void doPut(HttpServletRequest arg0, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "JSPWiki is read-only.");
  }

  /*
   * GET /dav/raw/WikiPage.txt GET /dav/html/WikiPage.html GET /dav/pdf/WikiPage.pdf GET
   * /dav/raw/WikiPage/attachment1.png
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    // Do the "sanitize url" trick
    // String p = new String(req.getPathInfo().getBytes("ISO-8859-1"), "UTF-8");

    String p = req.getPathInfo();

    DavPath path = new DavPath(p);

    if (path.isRoot()) {
      DavMethod dm = new GetMethod(m_rootProvider);
      dm.execute(req, res, path);
    } else {
      DavMethod dm = new GetMethod(pickProvider(path.get(0)));

      dm.execute(req, res, path.subPath(1));
    }
  }
}
