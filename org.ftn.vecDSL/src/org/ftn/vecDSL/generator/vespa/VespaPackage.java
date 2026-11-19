package org.ftn.vecDSL.generator.vespa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * The application package of a program at one point of its run: services.xml with one container cluster and one content
 * cluster, one schema file per collection, and the default query profile. Every file is text built here, at generation time;
 * the runtimes zip and deploy it.
 */
public final class VespaPackage {

	/** Id of the content cluster that holds every collection. */
	public static final String CLUSTER = "vecdsl";
	/** Id of the container cluster that serves the query and document APIs. */
	public static final String CONTAINER = "default";
	public static final String TENANT = "vecdsl_tenant";
	public static final String NAMESPACE = "vecdsl_namespace";
	public static final String PARTITION = "vecdsl_partition";
	public static final String EXPIRES = "vecdsl_expires";
	public static final String VERSION = "vecdsl_version";
	public static final String EXACT = "vecdsl_exact_";
	/** The query tensor of a nearest-neighbour rank profile, sent as input.query(vecdsl_q). */
	public static final String QUERY_TENSOR = "vecdsl_q";
	public static final String TEXT_PROFILE = "vecdsl_text";
	public static final String RANDOM_PROFILE = "vecdsl_random";
	public static final String WEIGHT = "vecdsl_w_";
	/** The document summary of highlighted searches: every document field and a bolded copy vecdsl_hl_<f> of each full-text field. */
	public static final String HIGHLIGHT_SUMMARY = "vecdsl_highlight";
	public static final String HIGHLIGHT = "vecdsl_hl_";
	/** Hits and offset allowed per query (the defaults are 400 and 1000). */
	public static final int MAX_HITS = 10000;
	public static final int MAX_OFFSET = 100000;
	/** Fields of a schema that the config model rejects as field names (Schema.RESERVED_NAMES of config-model). */
	public static final Set<String> RESERVED = Set.of("index", "index_url", "summary", "attribute", "select_input", "host",
			"documentid", "position", "split_foreach", "tokenize", "if", "else", "switch", "case", "sddocname", "relevancy");

	private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

	private VespaPackage() {
	}

	/** HNSW settings; null values keep Vespa's defaults (16 links, 200 neighbours explored at insert). */
	public record Hnsw(Integer maxLinks, Integer exploreAtInsert) {
	}

	/**
	 * A field of a schema. Kinds: NUMERIC, BOOL, STRING (an exact, case-sensitive attribute), TEXT (an index field with BM25),
	 * TENSOR (a vector). A field with a source is a synthetic field outside the document, filled from that field.
	 */
	public record Field(String name, String type, String kind, boolean array, boolean fastSearch, String metric, Hnsw hnsw,
			String source) {

		public boolean vector() {
			return "TENSOR".equals(kind);
		}

		public boolean text() {
			return "TEXT".equals(kind);
		}

		public Field renamed(String newName) {
			return new Field(newName, type, kind, array, fastSearch, metric, hnsw, source);
		}

		public Field indexed(String newMetric, Hnsw newHnsw) {
			return new Field(name, type, kind, array, fastSearch, newMetric, newHnsw, source);
		}

		public Field searchable() {
			return new Field(name, type, kind, array, true, metric, hnsw, source);
		}
	}

	/**
	 * A rank profile the script declares in an extension vespa block (PhasedRankProfile): its expressions are the script's own text,
	 * written into the schema as given.
	 */
	public record Rank(String name, String inherits, List<String> inputs, Map<String, String> functions, String firstPhase,
			String secondPhase, String globalPhase, Integer rerankCount, List<String> matchFeatures, List<String> summaryFeatures,
			Map<String, String> settings) {
	}

	/** One collection: its document type, fields (document fields first), the key field, BM25 parameters and declared rank profiles. */
	public record Schema(String name, String key, boolean numericKey, List<Field> fields, Map<String, Double> bm25,
			boolean expiring, List<Rank> ranks) {

		public Field field(String fieldName) {
			for (Field field : fields) {
				if (field.name().equals(fieldName)) {
					return field;
				}
			}
			throw Unsupported.of("field " + fieldName + ", which document type " + name + " does not have at this statement");
		}

		public boolean has(String fieldName) {
			return fields.stream().anyMatch(field -> field.name().equals(fieldName));
		}

		public List<Field> documentFields() {
			return fields.stream().filter(field -> field.source() == null).toList();
		}

		public List<Field> vectors() {
			return fields.stream().filter(Field::vector).toList();
		}

		public List<Field> texts() {
			return fields.stream().filter(Field::text).toList();
		}

		public Schema with(Field added) {
			List<Field> result = new ArrayList<>(fields);
			result.add(added);
			return copy(name, result);
		}

		public Schema without(String fieldName) {
			return copy(name, fields.stream().filter(field -> !field.name().equals(fieldName)
					&& !fieldName.equals(field.source())).toList());
		}

		public Schema replaced(Field field) {
			return copy(name, fields.stream().map(f -> f.name().equals(field.name()) ? field : f).toList());
		}

		public Schema named(String newName) {
			return copy(newName, fields);
		}

		public Schema expiringNow() {
			if (expiring) {
				return this;
			}
			List<Field> result = new ArrayList<>(fields);
			result.add(new Field(EXPIRES, "long", "NUMERIC", false, true, null, null, null));
			return new Schema(name, key, numericKey, result, bm25, true, ranks);
		}

		public Schema ranked(List<Rank> declared) {
			return new Schema(name, key, numericKey, fields, bm25, expiring, List.copyOf(declared));
		}

		private Schema copy(String newName, List<Field> newFields) {
			return new Schema(newName, key, numericKey, List.copyOf(newFields), bm25, expiring, ranks);
		}
	}

	public static boolean identifier(String name) {
		return IDENTIFIER.matcher(name).matches();
	}

	/** The files of the package: services.xml, the query profile and schemas/<name>.sd, or no files for an empty application. */
	public static Map<String, String> files(List<Schema> schemas) {
		Map<String, String> files = new LinkedHashMap<>();
		if (schemas.isEmpty()) {
			return files;
		}
		files.put("services.xml", services(schemas));
		files.put("search/query-profiles/default.xml", queryProfile());
		for (Schema schema : schemas) {
			files.put("schemas/" + schema.name() + ".sd", schema(schema));
		}
		return files;
	}

	public static String services(List<Schema> schemas) {
		StringBuilder out = new StringBuilder();
		out.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		out.append("<services version=\"1.0\">\n");
		out.append("    <container id=\"").append(CONTAINER).append("\" version=\"1.0\">\n");
		out.append("        <document-api/>\n");
		out.append("        <search/>\n");
		out.append("        <nodes>\n");
		out.append("            <node hostalias=\"node1\"/>\n");
		out.append("        </nodes>\n");
		out.append("    </container>\n");
		out.append("    <content id=\"").append(CLUSTER).append("\" version=\"1.0\">\n");
		out.append("        <min-redundancy>1</min-redundancy>\n");
		boolean expiring = schemas.stream().anyMatch(Schema::expiring);
		out.append(expiring ? "        <documents garbage-collection=\"true\">\n" : "        <documents>\n");
		for (Schema schema : schemas) {
			out.append("            <document type=\"").append(schema.name()).append("\" mode=\"index\"");
			if (schema.expiring()) {
				out.append(" selection=\"").append(xml(expiry(schema.name()))).append("\"");
			}
			out.append("/>\n");
		}
		out.append("        </documents>\n");
		out.append("        <nodes>\n");
		out.append("            <node hostalias=\"node1\" distribution-key=\"0\"/>\n");
		out.append("        </nodes>\n");
		out.append("    </content>\n");
		out.append("</services>\n");
		return out.toString();
	}

	/** The documents a garbage-collected document type keeps: those whose expiry time lies ahead. */
	public static String expiry(String documentType) {
		return documentType + "." + EXPIRES + " > now()";
	}

	public static String queryProfile() {
		return String.join("\n", "<query-profile id=\"default\">",
				"    <field name=\"maxHits\">" + MAX_HITS + "</field>",
				"    <field name=\"maxOffset\">" + MAX_OFFSET + "</field>",
				"</query-profile>") + "\n";
	}

	/** The validation overrides a deployment needs, with the date placeholder the runtime fills (the day after the deploy). */
	public static String validationOverrides(List<String> ids) {
		StringBuilder out = new StringBuilder("<validation-overrides>\n");
		for (String id : ids) {
			out.append("    <allow until=\"{{until}}\">").append(id).append("</allow>\n");
		}
		return out.append("</validation-overrides>\n").toString();
	}

	public static String schema(Schema schema) {
		StringBuilder out = new StringBuilder();
		out.append("schema ").append(schema.name()).append(" {\n");
		out.append("    document ").append(schema.name()).append(" {\n");
		for (Field field : schema.documentFields()) {
			field(out, field, "        ");
		}
		out.append("    }\n");
		for (Field field : schema.fields()) {
			if (field.source() != null) {
				field(out, field, "    ");
			}
		}
		List<Field> texts = schema.texts();
		if (!texts.isEmpty()) {
			out.append("    fieldset default {\n");
			out.append("        fields: ").append(String.join(", ", texts.stream().map(Field::name).toList())).append("\n");
			out.append("    }\n");
		}
		for (Field vector : schema.vectors()) {
			out.append("    rank-profile ").append(nearestProfile(vector.name())).append(" {\n");
			out.append("        inputs {\n");
			out.append("            query(").append(QUERY_TENSOR).append(") ").append(vector.type()).append("\n");
			out.append("        }\n");
			out.append("        first-phase {\n");
			out.append("            expression: closeness(field,").append(vector.name()).append(")\n");
			out.append("        }\n");
			out.append("        match-features: ").append(distanceFeature(vector.name())).append("\n");
			out.append("    }\n");
		}
		if (!texts.isEmpty()) {
			out.append("    rank-profile ").append(TEXT_PROFILE).append(" {\n");
			out.append("        inputs {\n");
			for (Field text : texts) {
				out.append("            query(").append(WEIGHT).append(text.name()).append(") double: 1.0\n");
			}
			out.append("        }\n");
			out.append("        first-phase {\n");
			out.append("            expression: ").append(String.join(" + ", texts.stream()
					.map(text -> "query(" + WEIGHT + text.name() + ") * bm25(" + text.name() + ")").toList())).append("\n");
			out.append("        }\n");
			List<String> properties = new ArrayList<>();
			for (Field text : texts) {
				Double k1 = schema.bm25().get("k1");
				Double b = schema.bm25().get("b");
				if (k1 != null) {
					properties.add("            bm25(" + text.name() + ").k1: " + number(k1));
				}
				if (b != null) {
					properties.add("            bm25(" + text.name() + ").b: " + number(b));
				}
			}
			if (!properties.isEmpty()) {
				out.append("        rank-properties {\n").append(String.join("\n", properties)).append("\n        }\n");
			}
			out.append("    }\n");
		}
		for (Rank rank : schema.ranks()) {
			rank(out, rank);
		}
		if (!texts.isEmpty()) {
			out.append("    document-summary ").append(HIGHLIGHT_SUMMARY).append(" {\n");
			for (Field field : schema.documentFields()) {
				out.append("        summary ").append(field.name()).append(" {}\n");
			}
			for (Field text : texts) {
				out.append("        summary ").append(HIGHLIGHT).append(text.name()).append(" {\n");
				out.append("            source: ").append(text.name()).append("\n");
				out.append("            bolding: on\n");
				out.append("        }\n");
			}
			out.append("    }\n");
		}
		out.append("    rank-profile ").append(RANDOM_PROFILE).append(" {\n");
		out.append("        first-phase {\n");
		out.append("            expression: random\n");
		out.append("        }\n");
		out.append("    }\n");
		out.append("}\n");
		return out.toString();
	}

	/** The match feature that carries the distance of a hit, as the result renders its name. */
	public static String distanceFeature(String vectorField) {
		return "distance(field," + vectorField + ")";
	}

	public static String nearestProfile(String vectorField) {
		return "vecdsl_nn_" + vectorField;
	}

	private static void rank(StringBuilder out, Rank rank) {
		out.append("    rank-profile ").append(rank.name());
		if (rank.inherits() != null) {
			out.append(" inherits ").append(rank.inherits());
		}
		out.append(" {\n");
		if (!rank.inputs().isEmpty()) {
			out.append("        inputs {\n");
			for (String input : rank.inputs()) {
				out.append("            ").append(input).append("\n");
			}
			out.append("        }\n");
		}
		for (Map.Entry<String, String> function : rank.functions().entrySet()) {
			out.append("        function ").append(function.getKey()).append("() {\n");
			out.append("            expression: ").append(function.getValue()).append("\n");
			out.append("        }\n");
		}
		phase(out, "first-phase", rank.firstPhase(), null);
		phase(out, "second-phase", rank.secondPhase(), rank.rerankCount());
		phase(out, "global-phase", rank.globalPhase(), rank.secondPhase() == null ? rank.rerankCount() : null);
		if (!rank.matchFeatures().isEmpty()) {
			out.append("        match-features: ").append(String.join(" ", rank.matchFeatures())).append("\n");
		}
		if (!rank.summaryFeatures().isEmpty()) {
			out.append("        summary-features: ").append(String.join(" ", rank.summaryFeatures())).append("\n");
		}
		for (Map.Entry<String, String> setting : rank.settings().entrySet()) {
			out.append("        ").append(setting.getKey()).append(": ").append(setting.getValue()).append("\n");
		}
		out.append("    }\n");
	}

	private static void phase(StringBuilder out, String phase, String expression, Integer rerankCount) {
		if (expression == null) {
			return;
		}
		out.append("        ").append(phase).append(" {\n");
		if (rerankCount != null) {
			out.append("            rerank-count: ").append(rerankCount).append("\n");
		}
		out.append("            expression: ").append(expression).append("\n");
		out.append("        }\n");
	}

	private static void field(StringBuilder out, Field field, String indent) {
		out.append(indent).append("field ").append(field.name()).append(" type ").append(field.type()).append(" {\n");
		String inner = indent + "    ";
		List<String> indexing = new ArrayList<>();
		if (field.source() != null) {
			indexing.add("input " + field.source());
		}
		if (field.text()) {
			indexing.add("index");
		} else {
			indexing.add("attribute");
			if (field.vector() && field.hnsw() != null) {
				indexing.add("index");
			}
		}
		if (field.source() == null) {
			indexing.add("summary");
		}
		out.append(inner).append("indexing: ").append(String.join(" | ", indexing)).append("\n");
		switch (field.kind()) {
		case "TEXT" -> out.append(inner).append("index: enable-bm25\n");
		case "STRING" -> {
			out.append(inner).append("match: cased\n");
			if (field.fastSearch()) {
				out.append(inner).append("attribute: fast-search\n");
			}
		}
		case "TENSOR" -> {
			out.append(inner).append("attribute {\n");
			out.append(inner).append("    distance-metric: ").append(field.metric()).append("\n");
			out.append(inner).append("}\n");
			// "index" in the indexing statement already builds an HNSW graph with the default settings; the block only overrides them
			if (field.hnsw() != null && (field.hnsw().maxLinks() != null || field.hnsw().exploreAtInsert() != null)) {
				out.append(inner).append("index {\n");
				out.append(inner).append("    hnsw {\n");
				if (field.hnsw().maxLinks() != null) {
					out.append(inner).append("        max-links-per-node: ").append(field.hnsw().maxLinks()).append("\n");
				}
				if (field.hnsw().exploreAtInsert() != null) {
					out.append(inner).append("        neighbors-to-explore-at-insert: ").append(field.hnsw().exploreAtInsert()).append("\n");
				}
				out.append(inner).append("    }\n");
				out.append(inner).append("}\n");
			}
		}
		default -> {
			if (field.fastSearch()) {
				out.append(inner).append("attribute: fast-search\n");
			}
		}
		}
		out.append(indent).append("}\n");
	}

	/** A number as written in a schema: integers without a fraction. */
	public static String number(double value) {
		if (value == Math.rint(value) && Math.abs(value) < 1e15) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}

	private static String xml(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
}
