package org.exoplatform.ks.ext.impl;

import org.apache.commons.lang.StringEscapeUtils;

import junit.framework.TestCase;

public class AnswerSpaceActivityPublisherTest extends TestCase {

	protected void setUp() throws Exception {};
	
	protected void tearDown() throws Exception {};
	
	public void testActivityUnescapseHtml() throws Exception {	  
	  testActivityEscapseHtml("escapse test $ & <html>", "escapse test $ &amp; &lt;html&gt;");
	  
	  testActivityEscapseHtml("\" <html>", "&quot; &lt;html&gt;");
	}
	
	/**
	 * Using for testing with escapse and unescapse HTML with what Activity expectation is.
	 * @param unescapseStr unscapse string: ex $<> ' &
	 * @param escapseStr escapse string: &amp; &lt; &gt;
	 */
	private void testActivityEscapseHtml(String unescapseStr, String escapseStr) {
	  String gotEscapse = StringEscapeUtils.escapeHtml(unescapseStr);
	  System.out.println(gotEscapse);
    //&lt; < and &gt; >
    assertEquals(escapseStr, gotEscapse);
    
    String gotUnescapse = StringEscapeUtils.unescapeHtml(escapseStr);
    //&lt; < and &gt; >
    assertEquals(unescapseStr, gotUnescapse);

	}

}
