package com.silverpeas.mailinglist.service.util;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import com.silverpeas.mailinglist.service.util.Html2Text;

import junit.framework.TestCase;

public class TestHtml2Text extends TestCase {
	Html2Text parser = new Html2Text(200);

	public void testParse() throws Exception {
		String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">"
				+ "<html><head><meta content=\"text/html;charset=ISO-8859-1\" "
				+ "http-equiv=\"Content-Type\"><title></title></head><body "
				+ "bgcolor=\"#ffffff\" text=\"#000000\">Hello World <i>Salut les "
				+ "copains </i></body></html>";
		Reader reader = new StringReader(html);
		parser.parse(reader);
		String summary = parser.getSummary();
		assertNotNull(summary);
		assertEquals("Hello World Salut les copains", summary);
	}

	public void testParseBigContent() throws Exception {
		Reader reader = new InputStreamReader(TestHtml2Text.class
				.getResourceAsStream("lemonde.html"));
		parser.parse(reader);
		String summary = parser.getSummary();
		assertNotNull(summary);
		assertEquals("Politique Recherchez depuis  sur Le Monde.fr A la Une "
				+ "Le Desk Vidéos International *Elections américaines Europe "
				+ "Politique *Municipales & Cantonales 2008 Société Carnet "
				+ "Economie Médias Météo Rendez-vo", summary);

	}
}
