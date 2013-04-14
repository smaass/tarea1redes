package cl.srtocino.gsmmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {
	
	static public String getStringFromStream(InputStreamReader ip) throws IOException {
		BufferedReader rd = new BufferedReader(ip);
	      String line = "";
	      String total = "";
	      while ((line = rd.readLine()) != null) {
	        total += line + "\n";
	      }
	      return total;
	}
}
