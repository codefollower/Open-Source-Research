package com.codefollower.douyu.http.ssl;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.net.ssl.KeyManagerFactory;


import com.codefollower.douyu.http.Constants;
import com.codefollower.douyu.logging.InternalLogger;
import com.codefollower.douyu.logging.InternalLoggerFactory;

public class SSLConfig {
	private static final InternalLogger log = InternalLoggerFactory.getInstance(SSLConfig.class);

	public String adjustRelativePath(String path, String relativeTo) {
		String newPath = path;
		File f = new File(newPath);
		if (!f.isAbsolute()) {
			newPath = relativeTo + File.separator + newPath;
			f = new File(newPath);
		}
		if (!f.exists()) {
			log.warn("configured file:[" + newPath + "] does not exist.");
		}
		return newPath;
	}

	// -------------------- SSL related properties --------------------

	private String algorithm = KeyManagerFactory.getDefaultAlgorithm();

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String s) {
		this.algorithm = s;
	}

	private String clientAuth = "false";

	public String getClientAuth() {
		return clientAuth;
	}

	public void setClientAuth(String s) {
		this.clientAuth = s;
	}

	private String keystoreFile = System.getProperty("user.home") + "/.keystore";

	public String getKeystoreFile() {
		return keystoreFile;
	}

	public void setKeystoreFile(String s) {
		String file = adjustRelativePath(s, System.getProperty(Constants.CATALINA_BASE_PROP));
		this.keystoreFile = file;
	}

	private String keystorePass = null;

	public String getKeystorePass() {
		return keystorePass;
	}

	public void setKeystorePass(String s) {
		this.keystorePass = s;
	}

	private String keystoreType = "JKS";

	public String getKeystoreType() {
		return keystoreType;
	}

	public void setKeystoreType(String s) {
		this.keystoreType = s;
	}

	private String keystoreProvider = null;

	public String getKeystoreProvider() {
		return keystoreProvider;
	}

	public void setKeystoreProvider(String s) {
		this.keystoreProvider = s;
	}

	private String sslProtocol = "TLS";

	public String getSslProtocol() {
		return sslProtocol;
	}

	public void setSslProtocol(String s) {
		sslProtocol = s;
	}

	// Note: Some implementations use the comma separated string, some use
	// the array
	private String ciphers = null;
	private String[] ciphersarr = new String[0];

	public String[] getCiphersArray() {
		return this.ciphersarr;
	}

	public String getCiphers() {
		return ciphers;
	}

	public void setCiphers(String s) {
		ciphers = s;
		if (s == null)
			ciphersarr = new String[0];
		else {
			StringTokenizer t = new StringTokenizer(s, ",");
			ciphersarr = new String[t.countTokens()];
			for (int i = 0; i < ciphersarr.length; i++)
				ciphersarr[i] = t.nextToken();
		}
	}

	private String keyAlias = null;

	public String getKeyAlias() {
		return keyAlias;
	}

	public void setKeyAlias(String s) {
		keyAlias = s;
	}

	private String keyPass = null;

	public String getKeyPass() {
		return keyPass;
	}

	public void setKeyPass(String s) {
		this.keyPass = s;
	}

	private String truststoreFile = System.getProperty("javax.net.ssl.trustStore");

	public String getTruststoreFile() {
		return truststoreFile;
	}

	public void setTruststoreFile(String s) {
		if (s == null) {
			this.truststoreFile = null;
		} else {
			String file = adjustRelativePath(s, System.getProperty(Constants.CATALINA_BASE_PROP));
			this.truststoreFile = file;
		}
	}

	private String truststorePass = System.getProperty("javax.net.ssl.trustStorePassword");

	public String getTruststorePass() {
		return truststorePass;
	}

	public void setTruststorePass(String truststorePass) {
		this.truststorePass = truststorePass;
	}

	private String truststoreType = System.getProperty("javax.net.ssl.trustStoreType");

	public String getTruststoreType() {
		return truststoreType;
	}

	public void setTruststoreType(String truststoreType) {
		this.truststoreType = truststoreType;
	}

	private String truststoreProvider = null;

	public String getTruststoreProvider() {
		return truststoreProvider;
	}

	public void setTruststoreProvider(String truststoreProvider) {
		this.truststoreProvider = truststoreProvider;
	}

	private String truststoreAlgorithm = null;

	public String getTruststoreAlgorithm() {
		return truststoreAlgorithm;
	}

	public void setTruststoreAlgorithm(String truststoreAlgorithm) {
		this.truststoreAlgorithm = truststoreAlgorithm;
	}

	private String trustManagerClassName = null;

	public String getTrustManagerClassName() {
		return trustManagerClassName;
	}

	public void setTrustManagerClassName(String trustManagerClassName) {
		this.trustManagerClassName = trustManagerClassName;
	}

	private String crlFile = null;

	public String getCrlFile() {
		return crlFile;
	}

	public void setCrlFile(String crlFile) {
		this.crlFile = crlFile;
	}

	private String trustMaxCertLength = null;

	public String getTrustMaxCertLength() {
		return trustMaxCertLength;
	}

	public void setTrustMaxCertLength(String trustMaxCertLength) {
		this.trustMaxCertLength = trustMaxCertLength;
	}

	private String sessionCacheSize = null;

	public String getSessionCacheSize() {
		return sessionCacheSize;
	}

	public void setSessionCacheSize(String s) {
		sessionCacheSize = s;
	}

	private String sessionTimeout = "86400";

	public String getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(String s) {
		sessionTimeout = s;
	}

	private String allowUnsafeLegacyRenegotiation = null;

	public String getAllowUnsafeLegacyRenegotiation() {
		return allowUnsafeLegacyRenegotiation;
	}

	public void setAllowUnsafeLegacyRenegotiation(String s) {
		allowUnsafeLegacyRenegotiation = s;
	}

	private String[] sslEnabledProtocolsarr = new String[0];

	public String[] getSslEnabledProtocolsArray() {
		return this.sslEnabledProtocolsarr;
	}

	public void setSslEnabledProtocols(String s) {
		if (s == null) {
			this.sslEnabledProtocolsarr = new String[0];
		} else {
			ArrayList<String> sslEnabledProtocols = new ArrayList<String>();
			StringTokenizer t = new StringTokenizer(s, ",");
			while (t.hasMoreTokens()) {
				String p = t.nextToken().trim();
				if (p.length() > 0) {
					sslEnabledProtocols.add(p);
				}
			}
			sslEnabledProtocolsarr = sslEnabledProtocols.toArray(new String[sslEnabledProtocols.size()]);
		}
	}
}
