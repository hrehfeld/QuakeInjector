/*
Copyright 2009 Hauke Rehfeld


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
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import javax.xml.ws.http.HTTPException;

public class Download {
	private final URL url;
	private InputStream stream;
	private HttpURLConnection connection; 

	public static Download create(String urlString) throws IOException {
		URL url;
		try {
			url = new URL(urlString);
		}
		catch (java.net.MalformedURLException e) {
			throw new RuntimeException("Something is wrong with the way we construct URLs: "
			                           + e.getMessage());
		}
		return new Download(url);
	}

	public Download(URL url) {
		this.url = url;
	}

	public InputStream init() throws Installer.CancelledException, IOException, HTTPException {
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setFollowRedirects(true);
			connection.setRequestProperty("Accept-Encoding","gzip, deflate");
			connection.connect();
			String encoding = connection.getContentEncoding();
			String contentType = connection.getContentType();

			int response = connection.getResponseCode();
			if (response != HttpURLConnection.HTTP_OK) {
				throw new HTTPException(response);
			}			

			//make appropriate stream
			if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
				stream = new GZIPInputStream(connection.getInputStream());
			}
			else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
				stream = new InflaterInputStream(connection.getInputStream(),
				                                 new Inflater(true));
			}
			else {
				stream = connection.getInputStream();
			}
		}
		catch (FileNotFoundException e) {
			throw new OnlineFileNotFoundException(e.getMessage());
		}


		return stream;
	}

	public InputStream getStream() {
		return stream;
	}

	public int getSize() {
		return connection.getContentLength();
	}

}