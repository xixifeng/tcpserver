package org.fastquery.tcpserver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.fastquery.bytes.ByteUtil;
import org.fastquery.bytes.CRC16;
import org.fastquery.bytes.IntByteUtil;
import org.fastquery.bytes.ShortByteUtil;

/**
 * 
 * @author xixifeng
 * 
 */
public class Convert {

	private Convert() {

	}

	/**
	 * 将一个包转换成实体Transmission
	 * 
	 * @param bytes 一个完整的包
	 * @return 包转换后的实体
	 */
	static Transmission toTransmission(List<Byte> frame) {
		int idLength = frame.get(6).byteValue() & 0XFF;
		int jsonLength = ShortByteUtil.getShort(new byte[] { frame.get(7).byteValue(), frame.get(8).byteValue() });
		int attachmentType = frame.get(9).byteValue() & 0XFF;
		int attachmentLength = ShortByteUtil.getShort(new byte[] { frame.get(10).byteValue(), frame.get(11).byteValue() });
		return toTransmission(frame, idLength, jsonLength, attachmentType, attachmentLength);
	}

	static Transmission toTransmission(List<Byte> frame, int idLength, int jsonLength, int attachmentType, int attachmentLength) {

		int version = frame.get(1).byteValue() & 0XFF;
		int timeStamp = IntByteUtil.getInt(ByteUtil.toBytes(frame.subList(2, 6)));

		List<Byte> ids = null;
		List<Byte> jsons = null;
		List<Byte> attachments = null;

		if (idLength != 0) {
			ids = frame.subList(12, 12 + idLength);
		}
		if (jsonLength != 0) {
			jsons = frame.subList(12 + idLength, 12 + idLength + jsonLength);
		}
		if (attachmentLength != 0) {
			attachments = frame.subList(12 + idLength + jsonLength, 12 + idLength + jsonLength + attachmentLength);
		}

		String id = new String(ByteUtil.toBytes(ids), Charset.forName("gb2312")); // 这个返回不可能为null
		String msg = "";
		if (jsonLength != 0) {
			msg = new String(ByteUtil.toBytes(jsons), Charset.forName("gb2312"));
		}
		byte[] attachment = ByteUtil.toBytes(attachments);

		return new Transmission(version, id, msg, attachmentType, attachment, timeStamp, jsonLength != 0 ? msg.charAt(0) == '{' : false);
	}

	/**
	 * 将实体Transmission转换成一个字结数组的包
	 * 
	 * @param transmission 实体
	 * @return 一个包完整的字节
	 */
	public static byte[] toTransmission(Transmission transmission) {
		// 范围限定

		/*
		 * private String id 为 null -> "" private String json; 为 null -> "" private byte[]
		 * attachmentBytes; > 为null 则 长度为0
		 */

		// 1). version [1,255]
		String id = transmission.getId();
		List<Byte> idBytes = ByteUtil.toBytes(id.getBytes(Charset.forName("gb2312")));
		if (!idBytes.isEmpty() && idBytes.get(0) == 0X7F) {
			throw new IllegalArgumentException("id的第一个字节不能为0X7F");
		}

		// 3). json gb2312 的字节个数不能大于65536
		// 4). attachmentType [1,255]
		// 5). attachmentBytes 不能为null,长度不能大于65536

		List<Byte> bytes = new ArrayList<>();
		bytes.add(Byte.valueOf((byte) 0X7F)); // 1
		bytes.add((byte) transmission.getVersion()); // 2

		int timeStamp = transmission.getTimeStamp();
		bytes.addAll(ByteUtil.toBytes(IntByteUtil.getBytes(timeStamp))); // 3

		String json = transmission.getMsg();
		List<Byte> jsonBytes = ByteUtil.toBytes(json.getBytes(Charset.forName("gb2312")));
		List<Byte> jsonLen = ByteUtil.toBytes(ShortByteUtil.getBytes((short) jsonBytes.size()));

		int attachmentType = transmission.getAttachmentType();
		byte[] attachmentBytes = transmission.getAttachmentBytes();
		List<Byte> attachmentLen = ByteUtil.toBytes(ShortByteUtil.getBytes((short) attachmentBytes.length));

		bytes.add((byte) idBytes.size()); // 4

		bytes.addAll(jsonLen); // 5

		bytes.add((byte) attachmentType); // 6

		bytes.addAll(attachmentLen); // 7

		bytes.addAll(idBytes); // 8

		bytes.addAll(jsonBytes); // 9

		bytes.addAll(ByteUtil.toBytes(attachmentBytes)); // 10

		byte[] crc = CRC16.updateCRC(bytes); // 11
		bytes.add((byte) crc[0]);
		bytes.add((byte) crc[1]);

		return ByteUtil.doubleByte(bytes, (byte) 0X7F);
	}

	static List<Byte> readFixed12(InputStream inputStream) throws IOException {
		while (inputStream.read() != 0X7F) {
			//
		}
		int limit = 12;
		List<Byte> bytes = new ArrayList<>();
		bytes.add((byte) 0X7F);

		return readFixed(inputStream, limit - 1, bytes);
	}

	static List<Byte> readFixed(InputStream inputStream, int limit) throws IOException {
		List<Byte> bytes = new ArrayList<>();
		return readFixed(inputStream, limit, bytes);
	}

	static List<Byte> readFixed(InputStream inputStream, int limit, List<Byte> bytes) throws IOException {
		int fixed = bytes.size() + limit;
		byte[] bs = new byte[limit];
		inputStream.read(bs);
		bytes.addAll(ByteUtil.toBytes(bs));
		int size = 0;
		List<Byte> ss;
		while ((size = (ss = ByteUtil.unDoubleByte(bytes, (byte) 0X7F)).size()) < fixed) {
			byte[] sub = new byte[fixed - size];
			inputStream.read(sub);
			bytes.addAll(ByteUtil.toBytes(sub));
		}

		return ss;
	}
}
