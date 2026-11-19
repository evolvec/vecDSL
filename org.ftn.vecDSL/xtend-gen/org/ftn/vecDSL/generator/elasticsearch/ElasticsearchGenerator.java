package org.ftn.vecDSL.generator.elasticsearch;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.ftn.vecDSL.generator.TargetGenerator;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.code.Program;
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Connection;
import org.ftn.vecDSL.vecDSL.EnvRef;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.Parameter;
import org.ftn.vecDSL.vecDSL.Script;

/**
 * Elasticsearch target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/elasticsearch/python, typescript, java) and writes a Docker Compose file for a single-node Elasticsearch server
 * (generated/elasticsearch/docker-compose.yml). Every language uses the official client: elasticsearch (Python),
 * @elastic/elasticsearch (TypeScript) and the Rest5Client of elasticsearch-java (Java).
 * 
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
@SuppressWarnings("all")
public class ElasticsearchGenerator implements TargetGenerator {
  public static final String VENDOR = ElasticsearchPrinter.VENDOR;

  public static final String LANGUAGES_PROPERTY = "vecdsl.languages";

  private final ElasticsearchManifest manifest = ElasticsearchManifest.load();

  @Override
  public String vendor() {
    return ElasticsearchGenerator.VENDOR;
  }

  @Override
  public void generate(final Script script, final IFileSystemAccess2 fsa) {
    final Program program = this.program(script);
    List<ElasticsearchPrinter> _printers = this.printers(script);
    for (final ElasticsearchPrinter printer : _printers) {
      Set<Map.Entry<String, String>> _entrySet = printer.files(program, this.manifest).entrySet();
      for (final Map.Entry<String, String> file : _entrySet) {
        String _language = printer.language();
        String _plus = (_language + "/");
        String _key = file.getKey();
        String _plus_1 = (_plus + _key);
        fsa.generateFile(_plus_1, ElasticsearchGenerator.VENDOR, file.getValue());
      }
    }
    fsa.generateFile("docker-compose.yml", ElasticsearchGenerator.VENDOR, this.manifest.render("environment/docker-compose.yml"));
  }

  public List<ElasticsearchPrinter> printers(final Script script) {
    List<ElasticsearchPrinter> _xblockexpression = null;
    {
      ElasticsearchPython _elasticsearchPython = new ElasticsearchPython();
      ElasticsearchTypeScript _elasticsearchTypeScript = new ElasticsearchTypeScript();
      ElasticsearchJava _elasticsearchJava = new ElasticsearchJava();
      final List<ElasticsearchPrinter> all = Collections.<ElasticsearchPrinter>unmodifiableList(CollectionLiterals.<ElasticsearchPrinter>newArrayList(_elasticsearchPython, _elasticsearchTypeScript, _elasticsearchJava));
      final String requested = System.getProperty(ElasticsearchGenerator.LANGUAGES_PROPERTY);
      Set<String> _xifexpression = null;
      if (((requested != null) && (!requested.isBlank()))) {
        final Function1<String, String> _function = (String it) -> {
          return it.trim().toLowerCase();
        };
        _xifexpression = IterableExtensions.<String>toSet(ListExtensions.<String, String>map(((List<String>)Conversions.doWrapArray(requested.split(","))), _function));
      } else {
        Set<String> _xifexpression_1 = null;
        boolean _written = ModelUtil.written(script, "hostLanguage");
        if (_written) {
          String _literal = script.getHostLanguage().getLiteral();
          _xifexpression_1 = Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet(_literal));
        } else {
          final Function1<ElasticsearchPrinter, String> _function_1 = (ElasticsearchPrinter it) -> {
            return it.language();
          };
          _xifexpression_1 = IterableExtensions.<String>toSet(ListExtensions.<ElasticsearchPrinter, String>map(all, _function_1));
        }
        _xifexpression = _xifexpression_1;
      }
      final Set<String> selected = _xifexpression;
      final Function1<ElasticsearchPrinter, Boolean> _function_2 = (ElasticsearchPrinter it) -> {
        return Boolean.valueOf(selected.contains(it.language()));
      };
      _xblockexpression = IterableExtensions.<ElasticsearchPrinter>toList(IterableExtensions.<ElasticsearchPrinter>filter(all, _function_2));
    }
    return _xblockexpression;
  }

  public Program program(final Script script) {
    Program _xblockexpression = null;
    {
      final ElasticsearchStatements statements = new ElasticsearchStatements(script);
      final List<Program.Method> methods = CollectionLiterals.<Program.Method>newArrayList();
      final Set<String> used = CollectionLiterals.<String>newHashSet();
      int index = 0;
      EList<Method> _methods = script.getMethods();
      for (final Method statement : _methods) {
        {
          index = (index + 1);
          List<Program.Method> _methods_1 = statements.methods(statement, this.uniqueName(statement, index, used));
          Iterables.<Program.Method>addAll(methods, _methods_1);
        }
      }
      final Function1<Parameter, Program.Parameter> _function = (Parameter it) -> {
        String _name = it.getName();
        String _literal = it.getType().getLiteral();
        Code.Expr _xifexpression = null;
        Literal _defaultValue = it.getDefaultValue();
        boolean _tripleEquals = (_defaultValue == null);
        if (_tripleEquals) {
          _xifexpression = null;
        } else {
          _xifexpression = Literals.of(it.getDefaultValue());
        }
        return new Program.Parameter(_name, _literal, _xifexpression);
      };
      final List<Program.Parameter> parameters = ListExtensions.<Parameter, Program.Parameter>map(script.getParameters(), _function);
      String _name = script.getName();
      String _version = script.getVersion();
      String _description = script.getDescription();
      Program.Connection _connection = this.connection(script);
      _xblockexpression = new Program(_name, _version, _description, parameters, _connection, methods);
    }
    return _xblockexpression;
  }

  /**
   * The default server URL of the program: the connection's uri, or one built from host and port (https with tls); the user
   * is the connection's username, else elastic. Passwords and API keys never enter the URL: the generated program reads them
   * from the environment variables the connection names with env(...).
   */
  private Program.Connection connection(final Script script) {
    Program.Connection _xblockexpression = null;
    {
      final Connection declared = IterableExtensions.<Connection>head(EcoreUtil2.<Connection>getAllContentsOfType(script, Connection.class));
      final String defaultUrl = this.manifest.get("url");
      if ((declared == null)) {
        return new Program.Connection(defaultUrl, null, null, null, null, null);
      }
      Code.Expr _xifexpression = null;
      Literal _password = declared.getPassword();
      if ((_password instanceof EnvRef)) {
        _xifexpression = Literals.of(declared.getPassword());
      } else {
        _xifexpression = null;
      }
      final Code.Expr password = _xifexpression;
      Code.Expr _xifexpression_1 = null;
      Literal _apiKey = declared.getApiKey();
      if ((_apiKey instanceof EnvRef)) {
        _xifexpression_1 = Literals.of(declared.getApiKey());
      } else {
        _xifexpression_1 = null;
      }
      final Code.Expr apiKey = _xifexpression_1;
      String _xifexpression_2 = null;
      String _uri = declared.getUri();
      boolean _tripleNotEquals = (_uri != null);
      if (_tripleNotEquals) {
        _xifexpression_2 = declared.getUri();
      } else {
        String _xifexpression_3 = null;
        String _host = declared.getHost();
        boolean _tripleNotEquals_1 = (_host != null);
        if (_tripleNotEquals_1) {
          String _xifexpression_4 = null;
          boolean _isTls = declared.isTls();
          if (_isTls) {
            _xifexpression_4 = "https://";
          } else {
            _xifexpression_4 = "http://";
          }
          String _host_1 = declared.getHost();
          String _plus = (_xifexpression_4 + _host_1);
          String _plus_1 = (_plus + ":");
          Object _xifexpression_5 = null;
          int _port = declared.getPort();
          boolean _greaterThan = (_port > 0);
          if (_greaterThan) {
            _xifexpression_5 = Integer.valueOf(declared.getPort());
          } else {
            _xifexpression_5 = this.manifest.get("server.port");
          }
          _xifexpression_3 = (_plus_1 + _xifexpression_5);
        } else {
          _xifexpression_3 = defaultUrl;
        }
        _xifexpression_2 = _xifexpression_3;
      }
      final String url = _xifexpression_2;
      Double _xifexpression_6 = null;
      int _timeoutMillis = declared.getTimeoutMillis();
      boolean _greaterThan_1 = (_timeoutMillis > 0);
      if (_greaterThan_1) {
        int _timeoutMillis_1 = declared.getTimeoutMillis();
        double _divide = (_timeoutMillis_1 / 1000.0);
        _xifexpression_6 = Double.valueOf(_divide);
      } else {
        _xifexpression_6 = null;
      }
      final Double timeout = _xifexpression_6;
      String _username = declared.getUsername();
      _xblockexpression = new Program.Connection(url, apiKey, _username, password, null, timeout);
    }
    return _xblockexpression;
  }

  private String uniqueName(final Method statement, final int index, final Set<String> used) {
    String _xblockexpression = null;
    {
      final EStructuralFeature feature = statement.eClass().getEStructuralFeature("name");
      String _xifexpression = null;
      if ((feature == null)) {
        _xifexpression = null;
      } else {
        Object _eGet = statement.eGet(feature);
        _xifexpression = ((String) _eGet);
      }
      final String label = _xifexpression;
      String _elvis = null;
      if (label != null) {
        _elvis = label;
      } else {
        String _name = statement.eClass().getName();
        String _plus = (_name + "_");
        String _plus_1 = (_plus + Integer.valueOf(index));
        _elvis = _plus_1;
      }
      String name = Syntax.snake(_elvis);
      boolean _add = used.add(name);
      boolean _not = (!_add);
      if (_not) {
        name = ((name + "_") + Integer.valueOf(index));
        used.add(name);
      }
      _xblockexpression = name;
    }
    return _xblockexpression;
  }
}
