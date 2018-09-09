package rebar.gizmo.mustache;


import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mustache.MustacheResourceTemplateLoader;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.google.common.collect.Lists;

public class CustomMustacheTemplateLoader extends MustacheResourceTemplateLoader {

	List<File> searchPathList = Lists.newCopyOnWriteArrayList();
	Logger logger = org.slf4j.LoggerFactory.getLogger(CustomMustacheTemplateLoader.class);
	
	private ResourceLoader resourceLoader = new DefaultResourceLoader();
	private String charSet = "UTF-8";

	
	@Autowired
	org.springframework.context.ApplicationContext applicationContext;

	public CustomMustacheTemplateLoader(String prefix, String suffix) {
		super(prefix,CustomMustacheViewResolver.SUFFIX);
		searchPathList.add(new File("./src/main/resources"));
	}

	public List<File> getTemplatePaths() {
		return searchPathList;
	}

	Resource tridentResolveResource(final String url) {
		String name = url;
		
		if (name.startsWith("classpath:")) {
			name =name.replace("classpath:", "");
		}
		else if (name.startsWith("file:")) {
			name = name.replace("file:","");
		}
		if (!name.endsWith(CustomMustacheViewResolver.SUFFIX)) {
			name = name+CustomMustacheViewResolver.SUFFIX;
		}
		
		
		for (File searchPath: this.searchPathList) {
			File file = new File(searchPath, name);
			Resource r = null;
			if (file.exists()) {
				r = new FileSystemResource(file);
				// if inputstream exists return resource else 
				 /*try {
					 r.getInputStream();
				 }catch (Exception e) {
					 logger.info("Input stream not found for resource");
					 continue;
				 }*/
				logger.debug("{} => {} => {} in {}",url,name,r, searchPath);
				return r;
			} else {
				logger.debug("{} doesn't exist in {}", name, searchPath);
			}
		}

		Resource r = applicationContext.getResource(url);
		logger.debug("{} => {} => {}",url,name,r);
		return r;
	}

	@Override
	public Reader getTemplate(String viewName) throws Exception {
		while (viewName.startsWith("/")) {
			viewName = viewName.substring(1);
		}
		String fqName = String.format("/templates/%s%s", viewName,CustomMustacheViewResolver.SUFFIX);		
		//Resource resource = tridentResolveResource(fqName);
		//return new InputStreamReader(resource.getInputStream());
		return new InputStreamReader(resourceLoader.getResource(fqName).getInputStream(), charSet);
	}
}
