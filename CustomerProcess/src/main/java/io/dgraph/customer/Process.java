package io.dgraph.customer;

public class Process {
	String name;
	String lastEventName;
	String nextStep;
	String id;	
	
	public Process() {
		// do nothing
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLastEventName() {
		return lastEventName;
	}
	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	public String getNextStep() {
		return nextStep;
	}
	public void setNextStep(String nextStep) {
		this.nextStep = nextStep;
	}
	
}
