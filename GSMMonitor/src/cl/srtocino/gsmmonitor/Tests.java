package cl.srtocino.gsmmonitor;

import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.junit.Test;

public class Tests extends TestCase {
	
	@Test
	public void testRegexMatch() {
		String response = "<cell mnc=\"99\" lac=\"0\" nbSamples=\"57\" lat=\"50.5715642160311\" lon=\"25.2897075399231\" cellId=\"29513\" mcc=\"250\" range=\"6000\"/>";
		Pattern pattern = Pattern.compile("\\w+=\"[0-9.]+\"");
		Matcher matcher = pattern.matcher(response);
		Map<String, String> attributes = new Hashtable<String, String>();
		while (matcher.find()) {
			String match = matcher.group();
			int equalsIndex = match.indexOf('=');
			String attrName = match.substring(0, equalsIndex);
			String attrValue = match.substring(equalsIndex+2, match.length()-1);
			attributes.put(attrName, attrValue);
		}
		assertTrue(attributes.get("mnc").equals("99"));
		assertTrue(attributes.get("lac").equals("0"));
		assertTrue(attributes.get("lat").equals("50.5715642160311"));
		assertTrue(attributes.get("range").equals("6000"));
		assertTrue(attributes.get("cellId").equals("29513"));
	}
}
