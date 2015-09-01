package com.expanset.jersey.utils;

import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.process.internal.RequestScope.Instance;

public final class RequestScopeUtils {
	
	public static boolean isInRequestScope(RequestScope requestScope) {
		// NOTE Because there is no method to direct check active scope. 
		final Instance scope = requestScope.suspendCurrent();
		if(scope == null) {
			return false;
		}
		scope.release();
		return true;
	}
	
	private RequestScopeUtils() {}
}
