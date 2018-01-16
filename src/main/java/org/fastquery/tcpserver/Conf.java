package org.fastquery.tcpserver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

/**
 * 
 * @author xixifeng
 * 
 */
public class Conf {

	private int port;
	private String mqSubConnectAddr;
	private int mqSubReceiveTimeOut;

	private Map<String, MethodObj> methods = new HashMap<>();

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getMqSubConnectAddr() {
		return mqSubConnectAddr;
	}

	public void setMqSubConnectAddr(String mqSubConnectAddr) {
		this.mqSubConnectAddr = mqSubConnectAddr;
	}

	public int getMqSubReceiveTimeOut() {
		return mqSubReceiveTimeOut;
	}

	public void setMqSubReceiveTimeOut(int mqSubReceiveTimeOut) {
		this.mqSubReceiveTimeOut = mqSubReceiveTimeOut;
	}

	public void putMethod(String path, MethodObj methodObj) {
		methods.put(path, methodObj);
	}

	public Transmission invokeResource(String path, Transmission t) throws IllegalAccessException, InvocationTargetException {
		MethodObj mo = methods.get(path);
		if (mo == null) {
			throw new IllegalArgumentException("终端传递的path=" + path + ", 没有找到可匹配的方法");
		}

		// 注入 Transmission
		Method method = mo.getMethod();
		Object object = mo.getObject();
		Class<?> clazz = object.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (field.getAnnotation(Resource.class) != null && field.getType() == Transmission.class) {
				field.setAccessible(true);
				field.set(object, t);
				field.setAccessible(false);
				break;
			}
		}

		return (Transmission) method.invoke(object);
	}
}
