package rebar.gizmo;

import org.neo4j.driver.v1.Driver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
