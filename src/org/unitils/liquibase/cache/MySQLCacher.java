package org.unitils.liquibase.cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class MySQLCacher {
	
	public String createCache() throws CachingException {
		try {
			ProcessBuilder pb = new ProcessBuilder(
	        	Arrays.asList("/usr/local/bin/mysqldump", "--routines", "-u", "genohm", "-pgenohm", "fromscratch"));
	    	
			Process p = pb.start();
	
	    	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    	StringBuilder sb = new StringBuilder();
	    	String line;
	    	while ((line = bufferedReader.readLine()) != null) {
				sb.append(line + '\n');
			}
	    	return sb.toString();
		} catch (IOException ioex) {
			throw new CachingException(ioex);
		}
	}

}
