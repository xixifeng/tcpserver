package org.fastquery.tcpserver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xixifeng
 * 
 */
public class ProcessingRequest {

	private ProcessingRequest() {
	}

	/**
	 * 接受器.
	 * 
	 * @param t 客户端发送过来的实体
	 * @param response 响应头
	 * @param conf 配置
	 * @throws IOException IO异常
	 * @throws IllegalAccessException 非法访问
	 * @throws InvocationTargetException 调用异常
	 */
	public static void receive(Transmission t, Response response, Conf conf) throws IOException, IllegalAccessException, InvocationTargetException {
		if (t.isMsgJSON()) {
			JSONObject json = JSON.parseObject(t.getMsg());
			if (json.containsKey("uri")) {
				String path = json.getString("uri");
				Transmission ts = conf.invokeResource(path, t);
				if (ts == null) {
					ts = new Transmission();
				}
				response.write(ts);
			}
		} else {
			response.write(new Transmission());
		}

	}

}
