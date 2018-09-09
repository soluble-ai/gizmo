package rebar.gizmo;


import org.neo4j.driver.v1.Session;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mustache.MustacheEnvironmentCollector;
import org.springframework.boot.autoconfigure.mustache.MustacheProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;


import com.samskivert.mustache.Mustache;

import rebar.gizmo.mustache.CustomMustacheTemplateLoader;
import rebar.gizmo.mustache.CustomMustacheViewResolver;
import rebar.gizmo.mustache.JacksonMustacheSupport;


@SpringBootApplication
public class Main {

	static org.slf4j.Logger logger = LoggerFactory.getLogger(Main.class);
	public static void main(String[] args) {
		try {
		ApplicationContext ctx = SpringApplication.run(Main.class, args);
		
		Session session = ctx.getBean(Gizmo.class).getDriver().session();
		
		session.run("match (x:DummyCheck) return x limit 1");
		}
		catch (Exception e) {
			logger.error("fatal",e);
			System.exit(1);
		}
	}

	@Bean
	CustomMustacheViewResolver customMustacheViewResolver(Environment env) {
		CustomMustacheViewResolver r = new CustomMustacheViewResolver(mustacheCompiler(env));
		
		r.setTemplateLoader(customMustacheTemplateLoader());
		r.setSuffix(CustomMustacheViewResolver.SUFFIX);
		r.setPrefix(MustacheProperties.DEFAULT_PREFIX);
	
		return r;
	}
	@Bean
	CustomMustacheTemplateLoader customMustacheTemplateLoader() {
		CustomMustacheTemplateLoader loader = new CustomMustacheTemplateLoader(MustacheProperties.DEFAULT_PREFIX,MustacheProperties.DEFAULT_SUFFIX);
		
	
		loader.setCharset("utf8");
	
		return loader;
	}

	@Bean
	public Gizmo gizmo() {
		return new Gizmo();
	}
	@Bean
	public Mustache.Compiler mustacheCompiler( 
	  Environment environment) {
	 
	    MustacheEnvironmentCollector collector
	      = new MustacheEnvironmentCollector();
	    collector.setEnvironment(environment);
	    
	    
	 
	
	    return JacksonMustacheSupport
				.configure(Mustache.compiler()
	      .defaultValue("")
	
	      .withLoader(customMustacheTemplateLoader())
	      .withCollector(collector));
	}
}
