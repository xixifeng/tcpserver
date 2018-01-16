package org.fastquery.tcpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;

import org.fastquery.bytes.CRC16;
import org.fastquery.bytes.ShortByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

/**
 * 
 * @author xixifeng
 * 
 */
public class Service implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Service.class);

	private static final ZMQ.Context context = ZMQ.context(1);
	private ZMQ.Socket subscriber = context.socket(ZMQ.SUB);

	private Socket socket;
	private Conf conf;

	private boolean sub;

	public Service(Socket socket, Conf conf) {
		this.socket = socket;
		this.conf = conf;
	}

	@Override
	public void run() {

		subscriber.connect(conf.getMqSubConnectAddr());

		try (InputStream inputStream = socket.getInputStream(); OutputStream outputStream = socket.getOutputStream()) {

			while (!socket.isClosed()) {
				// 第一次读
				List<Byte> bytes = Convert.readFixed12(inputStream);
				int idLength = bytes.get(6).byteValue() & 0XFF;
				int jsonLength = ShortByteUtil.getShort(new byte[] { bytes.get(7).byteValue(), bytes.get(8).byteValue() });
				int attachmentType = bytes.get(9).byteValue() & 0XFF;
				int attachmentLength = ShortByteUtil.getShort(new byte[] { bytes.get(10).byteValue(), bytes.get(11).byteValue() });
				int nextLen = idLength + jsonLength + attachmentLength + 2;
				List<Byte> nextBytes = Convert.readFixed(inputStream, nextLen);
				bytes.addAll(nextBytes);
				// 截至这里一个完整的包就读完了

				int len = bytes.size();
				// 看看CRC校验是否正确
				byte[] crc = CRC16.updateCRC(bytes.subList(0, len - 2));
				if (crc[0] == bytes.get(len - 2) && crc[1] == bytes.get(len - 1)) {
					Transmission t = Convert.toTransmission(bytes, idLength, jsonLength, attachmentType, attachmentLength);
					ProcessingRequest.receive(t, new Response(outputStream), conf);

					// 订阅
					if (!sub) {
						// 主题
						byte[] topic = t.getId().getBytes(Charset.forName("gb2312"));
						subscriber.subscribe(topic);
						// 超时
						subscriber.setReceiveTimeOut(conf.getMqSubReceiveTimeOut());
						sub = true;

						Thread thread = new Thread(() -> {
							while (!socket.isClosed()) {
								try {
									byte[] recv = subscriber.recv(0);
									if (recv != null && recv[0] == 0X7F) {
										outputStream.write(recv);
									} else {
										LOG.debug(t.getId() + "订阅超时");
										outputStream.write(0XFF);
									}
								} catch (Exception e) {
									LOG.error(e.getMessage(), e);
									sub = false;
									subscriber.unsubscribe(topic);
									// subscriber.close();
									break;
								}
							}
							LOG.info("订阅器结束");
						});
						thread.start();
					}
					// 订阅 end

				} else {
					LOG.error("校验失败了,msg长度: " + jsonLength + " , attachmentLength=" + attachmentLength + ",idLength=" + idLength);
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				socket.close();
				LOG.info("会话结束");
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}

	}
}
