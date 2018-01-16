package org.fastquery.tcpserver;

import java.lang.reflect.Method;

/**
 * 
 * @author xixifeng
 * 
 */
public class MethodObj {

	private Method method;
	private Object object;

	public MethodObj(Method method, Object object) {
		this.method = method;
		this.object = object;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

}
