package rebar.gizmo;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.opencypher.gremlin.neo4j.driver.Config;
import org.opencypher.gremlin.neo4j.driver.GremlinDatabase;
import org.opencypher.gremlin.neo4j.driver.GremlinGraphDriver;
import org.opencypher.gremlin.neo4j.driver.Config.ConfigBuilder;
import org.opencypher.gremlin.translation.translator.TranslatorFlavor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.amazonaws.services.neptune.AmazonNeptune;
import com.amazonaws.services.neptune.AmazonNeptuneClientBuilder;
import com.amazonaws.services.neptune.model.DBCluster;
import com.amazonaws.services.neptune.model.DescribeDBClustersRequest;
import com.amazonaws.services.neptune.model.DescribeDBClustersResult;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public class DriverSupplier implements Supplier<Driver> {

	Logger logger = LoggerFactory.getLogger(DriverSupplier.class);

	ApplicationContext ctx;

	String url;

	DriverSupplier(ApplicationContext ctx) {
		this.ctx = ctx;
	}

	DriverSupplier(String url) {
		this.url = url;
	}

	@Override
	public Driver get() {

		String url = this.url;

		if (ctx != null) {
			url = ctx.getEnvironment().getProperty("GREMLIN_URL", ctx.getEnvironment().getProperty("gremlin.url"));
		}

		if (Strings.isNullOrEmpty(url)) {
			try {
				logger.info("trying neptune against //127.0.0.1:8182 ...");
				ConfigBuilder privateBuilder = Config.build();
				privateBuilder = privateBuilder.ignoreIds().withTranslation(TranslatorFlavor.neptune());
				Driver driver = GremlinDatabase.driver("//127.0.0.1:8182", privateBuilder.toConfig());
				try (Session session = driver.session()) {
					session.run("match (a:NeptuneHealthCheck) return a limit 1");
					return driver;
				}
			} catch (Exception e) {
				logger.info("neptune not available at //127.0.0.1:8182");
			}

			try {
				InetAddress.getByName("docker.host.internal");
				ConfigBuilder privateBuilder = Config.build();
				privateBuilder = privateBuilder.ignoreIds().withTranslation(TranslatorFlavor.neptune());
				Driver driver = GremlinDatabase.driver("//docker.host.internal:8182", privateBuilder.toConfig());
				try (Session session = driver.session()) {
					session.run("match (a:NeptuneHealthCheck) return a limit 1");
					return driver;
				}
			}
			catch (Exception e) {
				logger.info("neptune not available at //docker.host.internal:8182");
			}
		}

		ConfigBuilder cb = Config.build();
		if (Strings.isNullOrEmpty(url) || url.toLowerCase().startsWith("neptune://discover")) {
			Optional<String> discoveredUrl = discoverNeptuneUrl(url);
			if (!discoveredUrl.isPresent()) {
				throw new GizmoException("could not auto-discover neptune url");
			}
			url = discoveredUrl.get();
		}

		List<String> parts = Splitter.on("://").splitToList(url);
		System.out.println(parts);
		if (parts.size() != 2) {
			throw new GizmoException(
					"GREMLIN_URL must be of the form neptune://<host>[:<port>] or gremlin://host[:<port>]");
		}
		String protocol = parts.get(0).trim().toLowerCase();
		String remainder = parts.get(1);

		if (protocol.equals("neptune") || url.toLowerCase().contains("neptune")) {
			logger.info("using neptune config");
			cb = cb.ignoreIds().withTranslation(TranslatorFlavor.neptune());
		}
		String dbUrl = "";
		if (!remainder.startsWith("//")) {
			dbUrl = "//";
		}
		dbUrl = dbUrl + remainder;
		if (!dbUrl.contains(":")) {
			dbUrl = dbUrl + ":8182";
		}

		logger.info("creating Driver to {}", dbUrl);
		Driver driver = GremlinDatabase.driver(dbUrl, cb.toConfig());

		try (Session session = driver.session()) {
			session.run("merge (a:DummyHealthCheck) return a limit 1").forEachRemaining(x -> {
				System.out.println(x);
			});
		}

		return driver;
	}

	Optional<String> discoverNeptuneUrl(String input) {
		logger.info("using neptune discovery");
		AmazonNeptune neptune = AmazonNeptuneClientBuilder.standard().build();
		DescribeDBClustersRequest request = new DescribeDBClustersRequest();
		do {
			DescribeDBClustersResult result = neptune.describeDBClusters(request);
			for (DBCluster c : result.getDBClusters()) {

				int port = c.getPort();
				String endpoint = c.getEndpoint();
				String url = "neptune://" + endpoint + ":" + port;
				logger.info("discovered {} - {}", c.getDBClusterArn(), url);
				return Optional.of(url);
			}
			request.setMarker(result.getMarker());
		} while (!Strings.isNullOrEmpty(request.getMarker()));

		return Optional.empty();
	}
}
