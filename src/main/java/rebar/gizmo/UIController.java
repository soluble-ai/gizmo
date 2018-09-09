package rebar.gizmo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.opencypher.v9_0.util.SyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Controller
@Component
public class UIController {

	Logger logger = LoggerFactory.getLogger(UIController.class);
	
	@Autowired
	Gizmo console;
	ObjectMapper mapper = new ObjectMapper();

	@RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView home() {
		return new ModelAndView("console");
	}

	@RequestMapping(value = "/exec", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView exec(HttpServletRequest request) {
		Map<String, Object> data = Maps.newHashMap();

		try {

			String cypher = Strings.nullToEmpty(request.getParameter("cypher"));
			cypher = cypher.trim();
			while (cypher.endsWith(";")) {
				cypher = cypher.substring(0, cypher.length() - 1).trim();
			}
			data.put("cypher", cypher);
		
			if (Strings.isNullOrEmpty(cypher)) {
				return new ModelAndView("console");
			}

			
			List<String> expandedKeyList = Lists.newArrayList();
			List<Map<String, String>> rows = Lists.newArrayList();
			AtomicInteger count = new AtomicInteger(0);
			try (Session session = console.getDriver().session()) {
				StatementResult sr = session.run(cypher);

				sr.forEachRemaining(r -> {
					if (count.incrementAndGet() < 5000) {

						Map<String, String> recordData = Maps.newHashMap();
						r.keys().forEach(key -> {
							Value v = r.get(key);
							if (v.isNull() || v.isNull()) {
								if (!expandedKeyList.contains(key)) {
									expandedKeyList.add(key);
								}
								recordData.put(key, "");
							} else if (v instanceof NodeValue) {
								Map<String, Object> ov = v.asMap();

								ov.entrySet().forEach(ovx -> {
									String subKey = key + "." + ovx.getKey();
									String val = Objects.toString(ovx.getValue(), "null");
									recordData.put(subKey, val);
									if (!expandedKeyList.contains(subKey)) {
										expandedKeyList.add(subKey);
									}
								});

							} else {
								if (!expandedKeyList.contains(key)) {
									expandedKeyList.add(key);
								}
								recordData.put(key, Objects.toString(key, "null"));
							}
							rows.add(recordData);

						});
					}

				});

			}

			List<Map<String, Object>> tabularList = Lists.newArrayList();
			rows.forEach(row -> {
				ArrayNode an = mapper.createArrayNode();
				expandedKeyList.forEach(x -> {
					an.add(row.get(x));
				});
				tabularList.add(ImmutableMap.of("vals", an));
			});

			data.put("results", ImmutableMap.of("rows", tabularList, "columnNames", expandedKeyList));
	

		} catch (RuntimeException e) {
			logger.warn("",e);
			String msg = Strings.nullToEmpty(e.getMessage());
			if (Strings.isNullOrEmpty(msg)) {
				msg =e.toString();
			}
			data.put("errorMessage", msg);
		}

		return new ModelAndView("console", data);
	}

	@RequestMapping(value = "/logout", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView logout() {
		return new ModelAndView("redirect:/");
	}
}
