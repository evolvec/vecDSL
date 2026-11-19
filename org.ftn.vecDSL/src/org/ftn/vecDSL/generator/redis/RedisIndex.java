package org.ftn.vecDSL.generator.redis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.ftn.vecDSL.generator.common.Unsupported;

/**
 * The Query Engine side of a vecDSL collection: JSON documents under one key prefix, indexed by one FT.CREATE index whose
 * attributes are the declared fields. Builds the FT.CREATE and FT.ALTER argument arrays at generation time.
 */
public final class RedisIndex {

	public static final String TENANT = "vecdsl_tenant";
	public static final String NAMESPACE = "vecdsl_namespace";
	public static final String PARTITION = "vecdsl_partition";
	public static final String SCORE = "vecdsl_score";
	public static final String EXACT = "vecdsl_exact_";
	public static final String DEFAULT_PARTITION = "default";

	private static final Pattern NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

	private RedisIndex() {
	}

	/** A vector attribute: algorithm (FLAT, HNSW, SVS-VAMANA), element type, dimension, vecDSL metric and algorithm options. */
	public record Vec(String algorithm, String type, int dim, String metric, Map<String, String> options) {
		public String distance() {
			return switch (metric) {
			case "cosine" -> "COSINE";
			case "euclidean" -> "L2";
			case "dot" -> "IP";
			default -> throw Unsupported.of("the " + metric + " metric; the Query Engine compares vectors with L2, IP and COSINE");
			};
		}
	}

	/**
	 * One index attribute: the name queries use, the JSONPath it reads, its kind (TEXT, TAG, NUMERIC, VECTOR), and for TEXT the
	 * weight and whether terms are stemmed. The document field is the first segment of the path.
	 */
	public record Attr(String name, String path, String kind, Vec vec, double weight, boolean stem) {
		public String field() {
			String rest = path.substring(2);
			int end = rest.length();
			for (int i = 0; i < rest.length(); i++) {
				if (rest.charAt(i) == '.' || rest.charAt(i) == '[') {
					end = i;
					break;
				}
			}
			return rest.substring(0, end);
		}

		public boolean array() {
			return path.endsWith("[*]");
		}
	}

	/**
	 * The index of one collection: a logical database (a name prefix) or null, the collection name, the key field and whether
	 * keys are numbers, the attributes, the document fields in declaration order with their vector dimensions, the hidden
	 * slice fields, the index language and stop words (null: Redis's default list), and the default ttl of written keys.
	 */
	public record Index(String database, String collection, String key, boolean numericKey, List<Attr> attrs, List<String> fields,
			Map<String, Integer> vectors, String tenant, String namespace, String partition, String language, List<String> stopwords,
			Integer ttl) {

		public String name() {
			return database == null ? collection : database + ":" + collection;
		}

		public String prefix() {
			return name() + ":";
		}

		public Attr attr(String name) {
			return attrs.stream().filter(a -> a.name().equals(name)).findFirst()
					.orElseThrow(() -> Unsupported.of("field " + name + ", which is not an indexed field of collection " + collection));
		}

		public boolean has(String name) {
			return attrs.stream().anyMatch(a -> a.name().equals(name));
		}

		public Attr vector(String name) {
			Attr attr = attr(name);
			if (attr.vec() == null) {
				throw Unsupported.of("field " + name + ", which is not a vector field");
			}
			return attr;
		}

		public Index named(String newName) {
			return new Index(database, newName, key, numericKey, attrs, fields, vectors, tenant, namespace, partition, language,
					stopwords, ttl);
		}

		public Index with(List<Attr> added, String field, Integer dim) {
			List<Attr> all = new ArrayList<>(attrs);
			all.addAll(added);
			List<String> names = new ArrayList<>(fields);
			if (!names.contains(field)) {
				names.add(field);
			}
			Map<String, Integer> dims = new LinkedHashMap<>(vectors);
			if (dim != null) {
				dims.put(field, dim);
			}
			return new Index(database, collection, key, numericKey, all, names, dims, tenant, namespace, partition, language, stopwords,
					ttl);
		}

		public Index without(String field) {
			List<String> names = new ArrayList<>(fields);
			names.remove(field);
			Map<String, Integer> dims = new LinkedHashMap<>(vectors);
			dims.remove(field);
			return new Index(database, collection, key, numericKey, attrs.stream().filter(a -> !a.field().equals(field)).toList(), names,
					dims, tenant, namespace, partition, language, stopwords, ttl);
		}

		public Index renamed(String field, String newName) {
			List<String> names = fields.stream().map(f -> f.equals(field) ? newName : f).toList();
			Map<String, Integer> dims = new LinkedHashMap<>();
			vectors.forEach((f, d) -> dims.put(f.equals(field) ? newName : f, d));
			List<Attr> renamedAttrs = attrs.stream().map(a -> a.field().equals(field) ? rename(a, field, newName) : a).toList();
			return new Index(database, collection, field.equals(key) ? newName : key, numericKey, renamedAttrs, names, dims, tenant,
					namespace, partition, language, stopwords, ttl);
		}

		public Index expiring(Integer seconds) {
			return new Index(database, collection, key, numericKey, attrs, fields, vectors, tenant, namespace, partition, language,
					stopwords, seconds);
		}
	}

	private static Attr rename(Attr attr, String field, String newName) {
		String path = "$." + newName + attr.path().substring(2 + field.length());
		String name = attr.name().equals(field) ? newName
				: attr.name().startsWith(field + "_") ? newName + attr.name().substring(field.length())
						: attr.name().equals(EXACT + field) ? EXACT + newName : attr.name();
		return new Attr(name, path, attr.kind(), attr.vec(), attr.weight(), attr.stem());
	}

	/** A collection, database, field or alias name: an identifier, so that it can name keys, attributes and JSONPath members. */
	public static String id(String name) {
		if (name == null || !NAME.matcher(name).matches()) {
			throw Unsupported.of("the name " + name + "; the generator uses identifiers as key prefixes, attribute names and JSONPath members");
		}
		return name;
	}

	/** FT.CREATE for the index: JSON documents under the prefix, language, stop words and every attribute. */
	public static List<String> definition(Index index) {
		List<String> args = new ArrayList<>(List.of("FT.CREATE", index.name(), "ON", "JSON", "PREFIX", "1", index.prefix()));
		if (index.language() != null) {
			args.add("LANGUAGE");
			args.add(index.language());
		}
		if (index.stopwords() != null) {
			args.add("STOPWORDS");
			args.add(String.valueOf(index.stopwords().size()));
			args.addAll(index.stopwords());
		}
		args.add("SCHEMA");
		for (Attr attr : index.attrs()) {
			args.addAll(attribute(attr));
		}
		return args;
	}

	/** FT.ALTER ... SCHEMA ADD for attributes added to an existing index. */
	public static List<String> alter(Index index, List<Attr> added) {
		List<String> args = new ArrayList<>(List.of("FT.ALTER", index.name(), "SCHEMA", "ADD"));
		for (Attr attr : added) {
			args.addAll(attribute(attr));
		}
		return args;
	}

	/**
	 * One attribute of a SCHEMA: TAG values are case-sensitive (exact matches as on the other targets); every non-vector attribute
	 * indexes missing values (ismissing), NUMERIC and TAG attributes are sortable.
	 */
	public static List<String> attribute(Attr attr) {
		List<String> args = new ArrayList<>(List.of(attr.path(), "AS", attr.name(), attr.kind()));
		switch (attr.kind()) {
		case "VECTOR" -> {
			Vec vec = attr.vec();
			Map<String, String> options = new LinkedHashMap<>();
			options.put("TYPE", vec.type());
			options.put("DIM", String.valueOf(vec.dim()));
			options.put("DISTANCE_METRIC", vec.distance());
			options.putAll(vec.options());
			args.add(vec.algorithm());
			args.add(String.valueOf(2 * options.size()));
			options.forEach((key, value) -> {
				args.add(key);
				args.add(value);
			});
		}
		case "TEXT" -> {
			if (attr.weight() != 1.0) {
				args.add("WEIGHT");
				args.add(number(attr.weight()));
			}
			if (!attr.stem()) {
				args.add("NOSTEM");
			}
			args.add("INDEXMISSING");
		}
		case "TAG" -> args.addAll(List.of("CASESENSITIVE", "INDEXMISSING", "SORTABLE"));
		case "NUMERIC" -> args.addAll(List.of("INDEXMISSING", "SORTABLE"));
		default -> throw new IllegalStateException("attribute kind " + attr.kind());
		}
		return args;
	}

	/** The Redis language name of an ISO 639-1 code or a language name, or null when the Query Engine has no stemmer for it. */
	public static String language(String language) {
		String code = language.toLowerCase(Locale.ROOT);
		Map<String, String> codes = Map.ofEntries(Map.entry("ar", "arabic"), Map.entry("hy", "armenian"), Map.entry("eu", "basque"),
				Map.entry("ca", "catalan"), Map.entry("da", "danish"), Map.entry("nl", "dutch"), Map.entry("en", "english"),
				Map.entry("fi", "finnish"), Map.entry("fr", "french"), Map.entry("de", "german"), Map.entry("el", "greek"),
				Map.entry("hi", "hindi"), Map.entry("hu", "hungarian"), Map.entry("id", "indonesian"), Map.entry("ga", "irish"),
				Map.entry("it", "italian"), Map.entry("lt", "lithuanian"), Map.entry("ne", "nepali"), Map.entry("no", "norwegian"),
				Map.entry("pt", "portuguese"), Map.entry("ro", "romanian"), Map.entry("ru", "russian"), Map.entry("sr", "serbian"),
				Map.entry("es", "spanish"), Map.entry("sv", "swedish"), Map.entry("ta", "tamil"), Map.entry("tr", "turkish"),
				Map.entry("yi", "yiddish"), Map.entry("zh", "chinese"), Map.entry("ms", "malay"), Map.entry("tl", "tagalog"));
		if (codes.containsKey(code)) {
			return codes.get(code);
		}
		return codes.containsValue(code) ? code : null;
	}

	/** A number as the Query Engine reads it: integers without a fraction, other values in plain decimal notation. */
	public static String number(double value) {
		if (value == Math.rint(value) && !Double.isInfinite(value) && Math.abs(value) < 1e15) {
			return Long.toString((long) value);
		}
		return new java.math.BigDecimal(Double.toString(value)).stripTrailingZeros().toPlainString();
	}
}
