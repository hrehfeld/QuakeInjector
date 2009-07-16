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
	private URLConnection connection;

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

	public Download(URL url) throws IOException, HTTPException, java.net.UnknownHostException {
		this.url = url;

		try {
			HttpURLConnection con = null;
			connection = url.openConnection();

			//http stuff, but url might be something different
			if (connection instanceof HttpURLConnection) {
				con = (HttpURLConnection) connection;
				con.setFollowRedirects(true);
				con.setRequestProperty("Accept-Encoding","gzip, deflate");
			}

			connection.connect();

			if (con != null) {
				int response = con.getResponseCode();
				if (response != HttpURLConnection.HTTP_OK) {
					throw new HTTPException(response);
				}
			}
			
			//try getting the stream
			connection.getInputStream();
		}
		catch (FileNotFoundException e) {
			throw new OnlineFileNotFoundException(e.getMessage());
		}
		
	}

	public InputStream getStream() throws IOException {
		return getStream(null);
	}
	
	public InputStream getStream(ProgressListener progress) throws IOException {
		if (stream == null) {
			String encoding = connection.getContentEncoding();


			stream = connection.getInputStream();
			if (progress != null) {
				stream = new ProgressListenerInputStream(stream, progress);
			}

			/** @todo 2009-07-15 18:55 hrehfeld    check what this does for non-http connections */
			if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
				stream = new GZIPInputStream(stream);
			}
			else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
				stream = new InflaterInputStream(stream, new Inflater(true));
			}
		}
		
		return stream;
	}

	public int getSize() {
		return connection.getContentLength();
	}

}