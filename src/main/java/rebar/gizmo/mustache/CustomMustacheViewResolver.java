package rebar.gizmo.mustache;



import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;

import org.slf4j.Logger;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.boot.web.servlet.view.MustacheViewResolver;
import org.springframework.core.io.Resource;

import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Template;

public class CustomMustacheViewResolver extends MustacheViewResolver {

	public static final String SUFFIX=".mustache";
	
	Logger logger = org.slf4j.LoggerFactory.getLogger(CustomMustacheViewResolver.class);

	private String charset;

	CustomMustacheTemplateLoader templateLoader;

	Compiler compilerRef;
	

	public CustomMustacheViewResolver(Compiler compiler) {
		super(compiler);
		compilerRef = compiler;
		setViewClass(requiredViewClass());
	
		
	}

	@Override
	protected Class<?> requiredViewClass() {
		return CustomMustacheView.class;
	}



	protected Resource resolveFromLocale(String viewName, String locale) {
		String name = viewName + locale + getSuffix();

		Resource resource = this.templateLoader.tridentResolveResource(name);

		if (resource == null || !resource.exists()) {
			if (locale.isEmpty()) {
				
				resource = null;
			} else {
				int index = locale.lastIndexOf("_");
				resource = resolveFromLocale(viewName, locale.substring(0, index));
			}
		}
		
		return resource;
	}

	private String getLocale(Locale locale) {
		if (locale == null) {
			return "";
		}
		LocaleEditor localeEditor = new LocaleEditor();
		localeEditor.setValue(locale);
		return "_" + localeEditor.getAsText();
	}

	private Template createTemplate(Resource resource) throws IOException {

		Reader reader = getReader(resource);

		try {
			Template t = compilerRef.compile(reader);

			return t;
		} finally {
			reader.close();
		}
	}

	private Reader getReader(Resource resource) throws IOException {
		if (this.charset != null) {
			return new InputStreamReader(resource.getInputStream(), this.charset);
		}
		return new InputStreamReader(resource.getInputStream());
	}

	public CustomMustacheTemplateLoader getTemplateLoader() {
		return templateLoader;
	}

	public void setTemplateLoader(CustomMustacheTemplateLoader tl) {
		this.templateLoader = tl;
	}
}
