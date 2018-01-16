package org.fastquery.tcpserver;

import java.io.InputStream;
import java.lang.reflect.Method;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author xixifeng
 * 
 */
public class Xml2Conf {
	public static Conf to() throws Exception {

		try (InputStream inputStream = Xml2Conf.class.getResourceAsStream("/tcp-server.xml")) {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = null;
			Document document = null;
			builder = factory.newDocumentBuilder();
			document = builder.parse(inputStream);

			Conf conf = new Conf();

			NodeList tcpServer = document.getElementsByTagName("tcp:server");
			Element element = (Element) tcpServer.item(0);
			int port = Integer.parseInt(element.getAttribute("port").trim());
			conf.setPort(port);
			NodeList nodeList = element.getChildNodes();
			Node serviceBeansNode = null;
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && "tcp:serviceBeans".equals(node.getNodeName())) {
					serviceBeansNode = node;
				}

				if (node.getNodeType() == Node.ELEMENT_NODE && "property".equals(node.getNodeName())) {
					Element propertyElement = (Element) node;
					if ("mqSubConnectAddr".equals(propertyElement.getAttribute("name"))) {
						conf.setMqSubConnectAddr(propertyElement.getTextContent().trim());
					}
					if ("mqSubReceiveTimeOut".equals(propertyElement.getAttribute("name"))) {
						conf.setMqSubReceiveTimeOut(Integer.parseInt(propertyElement.getTextContent().trim()));
					}
				}
			}

			if (serviceBeansNode == null) {
				throw new RuntimeException("从tcp-server.xml中,没有找到<tcp:serviceBeans>");
			}

			NodeList beanNodeList = serviceBeansNode.getChildNodes();
			for (int i = 0; i < beanNodeList.getLength(); i++) {
				Node node = beanNodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE && "bean".equals(node.getNodeName())) {
					Element beanEle = (Element) node;
					String className = beanEle.getAttribute("class");
					Class<?> clazz = Class.forName(className.trim());
					Object object = clazz.newInstance();
					Method[] methods = clazz.getDeclaredMethods();
					for (Method method : methods) {
						Uri uri = method.getAnnotation(Uri.class);
						if (uri != null) {
							conf.putMethod(uri.value(), new MethodObj(method, object));
						}
					}

				}
			}
			return conf;

		} catch (Exception e) {
			throw e;
		}
	}
}
