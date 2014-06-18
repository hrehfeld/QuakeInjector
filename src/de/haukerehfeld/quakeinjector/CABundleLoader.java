/*
Copyright 2014 Eric Wasylishen


This file is part of QuakeInjector.

QuakeInjector is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

QuakeInjector is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with QuakeInjector.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.haukerehfeld.quakeinjector;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Replace the default SSL socket factory one that uses the Mozilla CA bundle.
 * This include the StartSSL CA, which Quaddicted uses.
 * 
 * @author ericw
 */
public abstract class CABundleLoader {
	private static KeyStore getKeystore() throws IOException, GeneralSecurityException {
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		
		// The Mozilla CA bundle, downloaded from https://github.com/bagder/ca-bundle/ca-bundle.crt
		InputStream inputStream = CABundleLoader.class.getResource("/ca-bundle.crt").openStream();
		
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		
		// Read all of the CA certs in the ca-bundle.crt file
		while (true) {
			try {
				X509Certificate  certificate = (X509Certificate) factory.generateCertificate(inputStream);
				ks.setCertificateEntry(certificate.getIssuerDN().getName(), certificate);
			} catch (CertificateException e) {	
				break; // End of file
			}
		}
		
		return ks;
	}

	public static void loadCertificateAuthorities() throws GeneralSecurityException, IOException {
		KeyStore ks = getKeystore();
		
		TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		factory.init(ks);
		
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, factory.getTrustManagers(), null);
		
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}
}
