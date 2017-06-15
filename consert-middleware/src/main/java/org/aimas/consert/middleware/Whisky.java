package org.aimas.consert.middleware;

/**
 * Bean class for example with whisky
 */
public class Whisky {

	private static int counter = 0;
	private final int id;
	private String name;
	private String origin;
	
	public Whisky() {
		this(null, null);
	}
	
	public Whisky(String name, String origin) {
		super();
		this.id = Whisky.counter;
		Whisky.counter++;
		this.name = name;
		this.origin = origin;
	}
	

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getOrigin() {
		return origin;
	}
	
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	
	public int getId() {
		return id;
	}
}
