package org.fastquery.tcpserver;

import java.util.Arrays;

/**
 * 
 * @author xixifeng
 * 
 */
public class Transmission {

	private int version;
	private String id = ""; // 通信id
	private String msg = ""; // 以 "{" 开头表示是一个JSONObject字符串, 以 "[" 开头表示是一个JSONArray字符串,反之是一个普通字符串
	private int attachmentType; // 附件类型
	private byte[] attachmentBytes = new byte[0]; // 附件
	private int timeStamp = (int) System.currentTimeMillis() / 1000; // 时间戳,精确到秒
	private boolean msgJSON;

	public Transmission() {
	}

	public Transmission(String msg) {
		this.msg = msg;
	}

	public Transmission(String id, String msg) {
		this.id = id;
		this.msg = msg;
	}

	public Transmission(int version, String id, String msg, int attachmentType, byte[] attachmentBytes, int timeStamp, boolean msgJSON) {
		this.version = version;
		this.id = id;
		this.msg = msg;
		this.attachmentType = attachmentType;
		this.attachmentBytes = attachmentBytes;
		this.timeStamp = timeStamp;
		this.msgJSON = msgJSON;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getAttachmentType() {
		return attachmentType;
	}

	public void setAttachmentType(int attachmentType) {
		this.attachmentType = attachmentType;
	}

	public byte[] getAttachmentBytes() {
		return attachmentBytes;
	}

	public void setAttachmentBytes(byte[] attachmentBytes) {
		this.attachmentBytes = attachmentBytes;
	}

	public int getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(int timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public boolean isMsgJSON() {
		return msgJSON;
	}

	public void setMsgJSON(boolean msgJSON) {
		this.msgJSON = msgJSON;
	}

	@Override
	public String toString() {
		return "Transmission [version=" + version + ", id=" + id + ", msg=" + msg + ", attachmentType=" + attachmentType + ", attachmentBytes="
				+ Arrays.toString(attachmentBytes) + ", timeStamp=" + timeStamp + ", msgJSON=" + msgJSON + "]";
	}

}
