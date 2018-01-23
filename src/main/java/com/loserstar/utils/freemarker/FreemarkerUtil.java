package com.loserstar.utils.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

public class FreemarkerUtil {
/*	Freemarker提供了3种加载模板目录的方法。 它使用Configuration类加载模板。
	三种方法分别是：
	public void setClassForTemplateLoading(Class clazz, String pathPrefix);
	public void setDirectoryForTemplateLoading(File dir) throws IOException;
	public void setServletContextForTemplateLoading(Object servletContext, String path);

	第一种：基于类路径，HttpWeb包下的framemaker.ftl文件
	  configuration.setClassForTemplateLoading(this.getClass(), "/HttpWeb");
	configuration.getTemplate("framemaker.ftl"); //framemaker.ftl为要装载的模板 
	第二种：基于文件系统
	configuration.setDirectoryForTemplateLoading(new File("/template"))
	configuration.getTemplate("framemaker.ftl"); //framemaker.ftl为要装载的模板
	第三种：基于Servlet Context，指的是基于WebRoot下的template下的framemaker.ftl文件
	HttpServletRequest request = ServletActionContext.getRequest();
	configuration.setServletContextForTemplateLoading(request.getSession().getServletContext(), "/template");
	configuration.getTemplate("framemaker.ftl"); //framemaker.ftl为要装载的模板
*/
	

	public static void main(String[] args) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
		cfg.setDefaultEncoding("UTF-8");
		/**
		 * https://www.cnblogs.com/yasepix/p/6283726.html
		 * 其实这个方法是根据类加载路径来判断的，最终会执行以下代码：
			FreemarkerUtil.class.getClassLoader().getResource("/template/");
			打出出来发现路径是
			file:/E:/loserStarWorkSpace/loserStarUtils/target/classes/
			所以第二个参数得加上包路径
		 */
		cfg.setClassForTemplateLoading(FreemarkerUtil.class, "/com/loserstar/utils/freemarker");
		System.out.println(FreemarkerUtil.class.getResource("/"));
//		cfg.setDirectoryForTemplateLoading(new File("E:\\loserStarWorkSpace\\loserStarUtils\\src\\main\\java\\com\\loserstar\\utils\\freemarker"));
		Template temp =cfg.getTemplate("test.ftl");
		Map<String, Object> map = new HashMap<>();
		map.put("data", "loserStar!");
		temp.process(map, new OutputStreamWriter(System.out));
	}
}