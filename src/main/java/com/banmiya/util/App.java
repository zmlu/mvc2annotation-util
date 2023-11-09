package com.banmiya.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Hello world!
 */
public class App {
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	/**
	 * dispatcher-servlet-business.xml路径
	 */
	private static String BUSINESS_XML = "/Users/luzhoumin/Developer/echemi/codeup/echemi_backstage/src/main/resources/dispatcher-servlet-business.xml";
//  private static String BUSINESS_XML = "/Users/luzhoumin/Developer/echemi/codeup/echemi_backstage/src/main/resources/dispatcher-servlet-framework.xml";
	/**
	 * main/java 路径
	 */
	private static String PROJECT_ROOT = "/Users/luzhoumin/Developer/echemi/codeup/echemi_supplier/src/main/java/";
	
	public static void main(String[] args) {
		String business = FileUtil.readString(new File(BUSINESS_XML), StandardCharsets.UTF_8);
		Document document = XmlUtil.parseXml(business);
		Element beans = XmlUtil.getRootElement(document);
		List<Element> beanList = XmlUtil.getElements(beans, "bean");
		for (Element bean : beanList) {
			String id = bean.getAttribute("id");
			String clazz = bean.getAttribute("class");
			String clazzPath = PROJECT_ROOT + clazz.replaceAll("\\.", "/") + ".java";
			System.out.println("===" + clazzPath + "===");
			File clazzFile = new File(clazzPath);
			boolean isFile = clazzFile.isFile();
			if (!isFile) {
				continue;
			}
			String clazzContent = FileUtil.readString(clazzFile, StandardCharsets.UTF_8);
			boolean isHaveImport = clazzContent.contains("import org.springframework.web.bind.annotation.RequestMapping;");
			if (!isHaveImport) {
				clazzContent = clazzContent.replaceAll("import javax.servlet.http.HttpServletRequest;", "import javax.servlet.http.HttpServletRequest;\nimport org.springframework.web.bind.annotation.RequestMapping;");
			}
			boolean isHaveRestController = clazzContent.contains("import org.springframework.web.bind.annotation.RestController;");
			if (!isHaveRestController) {
				clazzContent = clazzContent.replaceAll("import javax.servlet.http.HttpServletRequest;", "import javax.servlet.http.HttpServletRequest;\nimport org.springframework.web.bind.annotation.RestController;");
				clazzContent = clazzContent.replaceAll("public class ", "@RestController\npublic class ");
			}
			if (StrUtil.isNotEmpty(id)) {
				Element property = XmlUtil.getElement(bean, "property");
				Element subBean = XmlUtil.getElement(property, "bean");
				Element subProperty = XmlUtil.getElement(subBean, "property");
				Element props = XmlUtil.getElement(subProperty, "props");
				List<Element> propList = XmlUtil.getElements(props, "prop");
				for (Element element : propList) {
					String key = element.getAttribute("key");
					String textContent = element.getTextContent();
					logger.debug(key + ":" + textContent);
					clazzContent = clazzContent.replace("public ModelAndView " + textContent + "(", "@RequestMapping(\"" + key + "\")\n\tpublic ModelAndView " + textContent + "(");
					clazzContent = clazzContent.replace("public void " + textContent + "(", "@RequestMapping(\"" + key + "\")\n\tpublic void " + textContent + "(");
				}
				FileUtil.writeUtf8String(clazzContent, clazzPath);
				logger.debug(clazzContent);
			}
		}
	}
}
