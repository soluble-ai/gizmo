package rebar.gizmo;

import javax.annotation.PostConstruct;

import org.neo4j.driver.v1.Driver;
import org.opencypher.gremlin.neo4j.driver.Config;
import org.opencypher.gremlin.neo4j.driver.Config.ConfigBuilder;
import org.opencypher.gremlin.neo4j.driver.GremlinDatabase;
import org.opencypher.gremlin.translation.translator.TranslatorFlavor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class Gizmo implements ApplicationContextAware {

	static ApplicationContext applicationContext;

	java.util.function.Supplier<Driver> driverSupplier;
	
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		applicationContext = ctx;
		
		driverSupplier = Suppliers.memoize(toGuavaSupplier(new DriverSupplier(applicationContext)));
	}
	private Supplier<Driver> toGuavaSupplier(final java.util.function.Supplier<Driver> d) {
		return new Supplier<Driver>() {

			@Override
			public Driver get() {
				return d.get();
			}
		};
		
	}
	public Driver getDriver() {
		return driverSupplier.get();
		
	}
}
