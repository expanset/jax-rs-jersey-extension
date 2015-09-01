package com.expanset.jersey.assets;

/**
 * Settings for serve static assets. 
 */
public class AssetsBundle {

	protected final String mapping;
	
	protected final String cacheControl;
	
	/**
	 * @param mapping Mapping of static asset bundle (like /css/*).
	 */
	public AssetsBundle(String mapping) {
		this.mapping = mapping;
		this.cacheControl = null;
	}	
	
	/**
	 * @param mapping Mapping of static asset bundle (like /css/*).
	 * @param cacheControl Cache-Control HTTP header to control cache on client for this bundle. 
	 */
	public AssetsBundle(String mapping, String cacheControl) {
		this.mapping = mapping;
		this.cacheControl = cacheControl;
	}

	/**
	 * @return Mapping of static asset bundle (like /css/*).
	 */
	public String getMapping() {
		return mapping;
	}

	/**
	 * @return Cache-Control HTTP header to control cache on client for this bundle. 
	 */
	public String getCacheControl() {
		return cacheControl;
	}
}
