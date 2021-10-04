package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

import java.time.Instant;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Defines the configuration of an agent according to the deployment ontology
 */
public class Request {

	public static void log(String message)
	{
		TimeZone tz = TimeZone.getDefault();
		String tzDiff = ZoneId.of(tz.getID()).getRules().getOffset(Instant.now()).toString();
		String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
		System.out.println("[" + date +  " " + tz.getID() + " " + tzDiff +  " ] " + message);
	}

	public static void logInfo(String host, int port, String type, String body)
	{
		Request.log("From: " + host + ":" + port + "; Type:" + type + "; Body: " + body);
	}

	public static void wait(int sec) {
		try {
			System.out.println("Retry in " + sec + " seconds");
			TimeUnit.SECONDS.sleep(sec);
		} catch (InterruptedException e) {
			System.out.println("Unable to execute sleep function");
		}
	}
}
