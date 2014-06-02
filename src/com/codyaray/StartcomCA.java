package com.codyaray;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

/**
 * Replace the default SSL socket factory with one that trusts StartSSL.
 * 
 * @author codyaray, ericw
 * 
 * Based on http://stackoverflow.com/a/16229909/218269
 */
public class StartcomCA {

	private static X509TrustManager getTrustManager(String algorithm, KeyStore keystore)
			throws GeneralSecurityException {
		TrustManagerFactory factory = TrustManagerFactory.getInstance(algorithm);
		factory.init(keystore);

		for (TrustManager tm : factory.getTrustManagers()) {
			if (tm instanceof X509TrustManager) {
				return (X509TrustManager) tm;
			}
		}
		return null;
	}

	private static X509KeyManager getKeyManager(String algorithm, KeyStore keystore, char[] password)
			throws GeneralSecurityException {
		KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm);
		factory.init(keystore, password);

		for (KeyManager km : factory.getKeyManagers()) {
			if (km instanceof X509KeyManager) {
				return (X509KeyManager) km;
			}
		}
		return null;
	}

	private static final char[] PASSWORD = null;

	private static KeyStore customKeystore() throws Exception {
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate) factory.generateCertificate(StartcomCA.class.getResource(
				"/ca.pem").openStream());

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, PASSWORD);
		ks.setCertificateEntry("startcom-ca", certificate);
		return ks;
	}

	private static void setup() throws Exception {
		KeyStore ks = customKeystore();

		String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();

		X509TrustManager customTrustManager = getTrustManager("SunX509", ks);
		X509TrustManager jvmTrustManager = getTrustManager(defaultAlgorithm, null);

		X509KeyManager customKeyManager = getKeyManager("SunX509", ks, PASSWORD);
		X509KeyManager jvmKeyManager = getKeyManager(defaultAlgorithm, null, null);

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(new KeyManager[] { new CompositeX509KeyManager(Arrays.asList(jvmKeyManager, customKeyManager)) },
				new TrustManager[] { new CompositeX509TrustManager(Arrays.asList(jvmTrustManager, customTrustManager)) },
				null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	public static void trustStartcom() {
		try {
			setup();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
