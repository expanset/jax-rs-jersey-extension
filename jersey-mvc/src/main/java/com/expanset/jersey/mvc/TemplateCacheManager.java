package com.expanset.jersey.mvc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Configuration;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.internal.TemplateHelper;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

import com.expanset.common.URLUtils;

/**
 * Utility class for manage template cache.
 */
@Service
@Contract
public class TemplateCacheManager {
	
	@Inject
	protected Configuration webConfig;

	@Inject
	protected ServletContext servletContext;
	
	@Inject
	@Optional
	protected Provider<Locale> localeProvider;
	
	/**
	 * Refresh delay in milliseconds. 
	 */
	protected final long delay = 5000;

	protected final ConcurrentHashMap<String, TemplateInfo> cache = new ConcurrentHashMap<>();

	protected class TemplateInfo {
		
		public long lastChecked = System.currentTimeMillis(); 
		
		public long lastModified = Long.MIN_VALUE; 
	}
		
	/**
	 * Returns reader for template file.
	 * @param engine Template engine name.
	 * @param resourceName Template path and name.
	 * @return Reader for template file.
	 * @throws IOException Error when create reader.
	 */
	public Reader getTemplateReader(String engine, String resourceName) 
			throws IOException {
		final String resolvedResourceName = resolveTemplateName(engine, resourceName);
        final InputStream stream = servletContext.getResourceAsStream(resolvedResourceName);
        if(stream == null) {
        	throw new IllegalStateException(String.format("Template %s not found", resourceName));
        }
        
        cache.computeIfAbsent(resolvedResourceName, (key) -> new TemplateInfo());
        
        final Charset charset = TemplateHelper.getTemplateOutputEncoding(webConfig, engine);
		return new InputStreamReader(stream, charset);		
	}
	
	/**
	 * Returns true if template file is need to be reloaded.
	 * @param engine  Template engine name. 
	 * @param resourceName Template path and name.
	 * @return true if template file is need to be reloaded.
	 * @throws IOException Error when check.
	 */
	public boolean isNeedReloadTemplate(String engine, String resourceName) 
			throws IOException {
        final long now = System.currentTimeMillis();
		final String resolvedResourceName = resolveTemplateName(engine, resourceName);
		final TemplateInfo templateInfo = cache.computeIfAbsent(resolvedResourceName, (key) -> new TemplateInfo());
		
        if (now - templateInfo.lastChecked < delay) {
        	return false;
        }
        
        final long lastModified = getLastModified(servletContext.getResource(resolvedResourceName));
        
        final boolean lastModifiedChanged = lastModified != templateInfo.lastModified;		
        synchronized (templateInfo) {
        	templateInfo.lastChecked = now; 
        	templateInfo.lastModified = lastModified;
		}
        
		return lastModifiedChanged;
	}
		
	/**
	 * Returns template name with localization suffixes.
	 * @param engine  Template engine name. 
	 * @param resourceName Template path and name.
	 * @return Template name with localization suffixes.
	 * @throws IOException Error. 
	 */
	public String resolveTemplateName(String engine, String resourceName) 
			throws IOException {
		String resource;
		
		String templateBasePath = (String)webConfig.getProperty(MvcFeature.TEMPLATE_BASE_PATH + "." + engine);
		resource = URLUtils.combine("/", templateBasePath, resourceName);

		if(localeProvider != null) {
			final Locale locale = localeProvider.get();
			if(locale != null) {
				final String folder = FilenameUtils.getPath(resourceName);
				final String fileNameExt = FilenameUtils.getExtension(resourceName);
				final String fileNameWithoutExt = FilenameUtils.getBaseName(resourceName);
				
				String language = locale.getLanguage();
				while(StringUtils.isNotEmpty(language)) {					
					final String resourceCandidate = URLUtils.combine(
							folder,
							fileNameWithoutExt + "_" + language + "." + fileNameExt);
					if(servletContext.getResource(resourceCandidate) != null) {
						return resourceCandidate;
					}

					final int nextLangIndex = StringUtils.lastIndexOf(language, "_");
					if(nextLangIndex == -1) {
						language = StringUtils.EMPTY;
					} else {
						language = StringUtils.substring(language, nextLangIndex + 1);
					}
				}
			}
		}
		
		return resource;
	}	
	
	protected long getLastModified(URL url) 
			throws IOException {
		final URLConnection connection = url.openConnection();
		if (connection instanceof JarURLConnection) {
			final URL jarURL = ((JarURLConnection) connection).getJarFileURL();
			if (jarURL.getProtocol().equals("file")) {
				return new File(jarURL.getFile()).lastModified();
			} else {
				URLConnection jarConn = null;
				try {
					jarConn = jarURL.openConnection();
					return jarConn.getLastModified();
				} catch (IOException e) {
					return -1;
				} finally {
					try {
						if (jarConn != null)
							jarConn.getInputStream().close();
					} catch (IOException e) {
					}
				}
			}
		} else {
			long lastModified = connection.getLastModified();
			if (lastModified == -1L && url.getProtocol().equals("file")) {
				return new File(url.getFile()).lastModified();
			} else {
				return lastModified;
			}
		}
	}
}
