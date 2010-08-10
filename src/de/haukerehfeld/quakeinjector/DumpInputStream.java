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
import java.io.OutputStream;

/**
 * Dump everything that's read to an OutputStream

 * Note that skipping doesn't add to the stream, and marking isn't supported
 */
public class DumpInputStream extends InputStream {
	private final InputStream in;
	private final OutputStream out;

	public DumpInputStream(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	@Override
	public int read() throws IOException {
		int r = in.read();
		out.write(r);
		return r;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int readcount = in.read(b, off, len);

		if (readcount > 0) {
			out.write(b, off, len);
		}
		return readcount;
	}

	@Override
	public long skip(long n) throws IOException	{
		return in.skip(n);
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void mark(int readlimit) {}

	@Override
	public void reset() throws IOException {
		throw new IOException("Reset not supported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}
}

