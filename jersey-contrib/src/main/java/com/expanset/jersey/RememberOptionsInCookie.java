package com.expanset.jersey;

import java.net.URI;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.ws.rs.container.ContainerRequestContext;

import org.apache.commons.lang3.StringUtils;

import com.expanset.common.RememberOptions;

/**
 * Settings for store data in a HTTP cookie.
 */
public class RememberOptionsInCookie extends RememberOptions {

	protected final String path;
	
	protected final String domain;
	
	protected final boolean secure; 

	/**
	 * @param maxAge Cookie time to live in seconds. If null, time to live is limited to the browser session.
	 */
	public RememberOptionsInCookie(@Nullable Integer maxAge) {
		super(maxAge);
		
		this.path = null;
		this.domain = null;
		this.secure = false;
	}

	/**
	 * @param maxAge Cookie time to live in seconds. If null, time to live is limited to the browser session.
	 * @param secure true - send only through secured HTTP channel (HTTPS).
	 */
	public RememberOptionsInCookie(@Nullable Integer maxAge, boolean secure) {
		super(maxAge);
		
		this.path = null;
		this.domain = null;
		this.secure = secure;
	}

	/**
	 * @param maxAge Cookie time to live in seconds. If null, time to live is limited to the browser session.
	 * @param path URL path to store cookie.
	 * @param domain Domain to store cookie.
	 * @param secure true - send only through secured HTTP channel (HTTPS).
	 */
	public RememberOptionsInCookie(
			@Nullable Integer maxAge, 
			@Nullable String path, 
			@Nullable String domain, 
			boolean secure) {
		super(maxAge);
		
		this.path = path;
		this.domain = domain;
		this.secure = secure;
	}

	/**
	 * Convert {@link RememberOptions} to {@link RememberOptionsInCookie}.
	 * @param rememberOptions {@link RememberOptions} for convert to {@link RememberOptionsInCookie}.
	 * @return {@link RememberOptionsInCookie} from {@link RememberOptions}.
	 */
	public static RememberOptionsInCookie convert(RememberOptions rememberOptions) {
		final RememberOptionsInCookie rememberOptionsInCookie;
		if(rememberOptions == null) {
			rememberOptionsInCookie = new RememberOptionsInCookie(null);
		} else if (rememberOptions instanceof RememberOptionsInCookie) {
			rememberOptionsInCookie = (RememberOptionsInCookie)rememberOptions;
		} else {
			rememberOptionsInCookie = new RememberOptionsInCookie(rememberOptions.getMaxAge());
		}
		return rememberOptionsInCookie;		
	}
	
	/**
	 * Setup cookie by remember options.
	 * @param request Current web request.
	 * @param cookie Cookie to setup.
	 */
	public void setupCookie(ContainerRequestContext request, Cookie cookie) {
		cookie.setSecure(isSecure());
		if(getMaxAge() != null) {
			cookie.setMaxAge(getMaxAge().intValue());
		}		
		if(StringUtils.isNotEmpty(getPath())) {
			cookie.setPath(getPath());
		} else {
			final URI baseUri = request.getUriInfo().getBaseUri();
			final String basePath = baseUri.getPath();
			
			cookie.setPath(StringUtils.isNotEmpty(basePath) ? basePath : "/");
		}
		if(StringUtils.isNotEmpty(getDomain())) {
			cookie.setPath(getDomain());
		}	
	}
	
	/**
	 * 
	 * @return URL path to store cookie.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return Domain to store cookie.
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @return true - send only through secured HTTP channel (HTTPS).
	 */
	public boolean isSecure() {
		return secure;
	}
}
