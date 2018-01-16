package org.fastquery.tcpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author xixifeng
 * 
 */
public class Server {

	private static final Logger LOG = LoggerFactory.getLogger(Server.class);

	private Server() {
	}

	public static void start(Conf conf) {
		try (ServerSocket serverSocket = new ServerSocket(conf.getPort());) {
			while (!Thread.currentThread().isInterrupted()) {
				// 等待客户端来链接,没有客户来链接,就处于阻塞状态
				Socket socket = serverSocket.accept();
				new Thread(new Service(socket, conf)).start();
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
