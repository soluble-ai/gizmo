package rebar.gizmo.mustache;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.samskivert.mustache.DefaultCollector;
import com.samskivert.mustache.Mustache.Collector;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.VariableFetcher;

public class JacksonMustacheSupport {

	/**
	 * Convenience method for configuring the Compiler with Jackson support.
	 * 
	 * @param compiler
	 * @return Compiler
	 */
	public static Compiler configure(Compiler compiler) {
		return compiler.withFormatter(new JacksonFormatter())
				.withCollector(new JacksonCollector(new DefaultCollector()));
	}
	/**
	 * Convenience method for configuring the Compiler with Jackson support.
	 * 
	 * @param compiler 
	 * @param collector
	 * @return Compiler
	 */
	public static Compiler configure(Compiler compiler, Collector collector) {
		return compiler.withFormatter(new JacksonFormatter())
				.withCollector(new JacksonCollector(collector));
	}

	protected static class JacksonFormatter implements com.samskivert.mustache.Mustache.Formatter {

		@Override
		public String format(Object value) {

			if (value instanceof JsonNode) {
				JsonNode n = (JsonNode) value;

				if (n.isNull() || n.isMissingNode()) {
					return "";
				}
				if (((JsonNode) value).isValueNode()) {
					return n.asText();
				}

				return n.toString();
			}

			return String.valueOf(value);

		}

	}

	public static class JacksonCollector implements Collector {
		Collector delegate;

		public JacksonCollector() {
			this(new DefaultCollector());
		}

		public JacksonCollector(Collector c) {
			this.delegate = c;
		}

		@Override
		public VariableFetcher createFetcher(Object ctx, String name) {

			if (ctx != null && ctx instanceof JsonNode) {
				return new JsonVariableFetcher();
			}

			return delegate.createFetcher(ctx, name);
		}

		@Override
		public Iterator<?> toIterator(Object value) {

			// JsonNodes are Iterable. The default behavior ends up not being
			// correct since we
			// do not want to iterate across things other than arrays.
			if (value instanceof JsonNode) {
				if (value instanceof ArrayNode) {

					return delegate.toIterator(value);
				}
				return null; // return null if nothing to iterate
			}
			return delegate.toIterator(value);
		}

		@Override
		public <K, V> Map<K, V> createFetcherCache() {
			return delegate.createFetcherCache();
		}
	}

	public static class JsonVariableFetcher implements VariableFetcher {

		private Object jsonValue(JsonNode n) {
			if (n.isObject() || n.isArray()) {
				return n;
			}
			if (n.isNull() || n.isMissingNode()) {
				return null;
			}
			return n.asText();
		}

		@Override
		public Object get(Object ctx, String name) throws Exception {

			JsonNode n = (JsonNode) ctx;
			if (name.equals("this") || name.equals(".")) {
				return jsonValue(n);
			}
			return jsonValue(n.path(name));
		}

	}

}

