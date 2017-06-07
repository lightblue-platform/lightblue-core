package com.redhat.lightblue.crud;

/**
 *
 */
public class LightblueHealth {
	private final boolean isHealthy;
	private final String details;

	/**
	 * @param isHealthy
	 * @param details
	 */
	public LightblueHealth(boolean isHealthy, String details) {
		this.isHealthy = isHealthy;
		this.details = details;
	}

	/**
	 * @return if the application is healthy
	 */
	public boolean isHealthy() {
		return isHealthy;
	}

	/**
	 * @return details about the health
	 */
	public String details() {
		return details;
	}
}