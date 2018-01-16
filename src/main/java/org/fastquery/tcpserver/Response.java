package org.fastquery.tcpserver;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * @author xixifeng
 * 
 */
public class Response {

	private OutputStream outputStream;

	public Response(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public void write(Transmission t) throws IOException {
		outputStream.write(Convert.toTransmission(t));
	}
}
