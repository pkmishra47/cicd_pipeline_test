package org.apache.nifi.processors.daxoperation.utils;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.JEditorPane;
import javax.swing.text.EditorKit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RtfToHtml {
	private static Logger log = LoggerFactory.getLogger(RtfToHtml.class);
	
	public static String convertRtfToHtml(String rtfString) {
		StringReader rtf = new StringReader(rtfString);
		JEditorPane p = new JEditorPane();
		p.setContentType("text/rtf");
		EditorKit kitRtf = p.getEditorKitForContentType("text/rtf");
		try {
			kitRtf.read(rtf, p.getDocument(), 0);
			kitRtf = null;
			EditorKit kitHtml = p.getEditorKitForContentType("text/html");
			Writer writer = new StringWriter();
			kitHtml.write(writer, p.getDocument(), 0, p.getDocument().getLength());
			return writer.toString();
		} catch (Exception e) {
			log.error("RTF error: ", e);
		}
		return null;
	}
}