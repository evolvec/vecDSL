package org.ftn.vecDSL.generator.weaviate;

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
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.Parameter;
import org.ftn.vecDSL.vecDSL.Script;

/**
 * Weaviate target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/weaviate/python, typescript, java) and writes a Docker Compose file for a single-node server of
 * the supported version (generated/weaviate/docker-compose.yml).
 * 
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
@SuppressWarnings("all")
public class WeaviateGenerator implements TargetGenerator {
  public static final String VENDOR = WeaviatePrinter.VENDOR;

  public static final String LANGUAGES_PROPERTY = "vecdsl.languages";

  private final WeaviateManifest manifest = WeaviateManifest.load();

  @Override
  public String vendor() {
    return WeaviateGenerator.VENDOR;
  }

  @Override
  public void generate(final Script script, final IFileSystemAccess2 fsa) {
    final Program program = this.program(script);
    final WeaviateManifest target = this.manifest.with("grpc.port", String.valueOf(this.grpcPort(script)));
    List<WeaviatePrinter> _printers = this.printers(script);
    for (final WeaviatePrinter printer : _printers) {
      Set<Map.Entry<String, String>> _entrySet = printer.files(program, target).entrySet();
      for (final Map.Entry<String, String> file : _entrySet) {
        String _language = printer.language();
        String _plus = (_language + "/");
        String _key = file.getKey();
        String _plus_1 = (_plus + _key);
        fsa.generateFile(_plus_1, WeaviateGenerator.VENDOR, file.getValue());
      }
    }
    fsa.generateFile("docker-compose.yml", WeaviateGenerator.VENDOR, this.manifest.render("environment/docker-compose.yml"));
  }

  public List<WeaviatePrinter> printers(final Script script) {
    List<WeaviatePrinter> _xblockexpression = null;
    {
      WeaviatePython _weaviatePython = new WeaviatePython();
      WeaviateTypeScript _weaviateTypeScript = new WeaviateTypeScript();
      WeaviateJava _weaviateJava = new WeaviateJava();
      final List<WeaviatePrinter> all = Collections.<WeaviatePrinter>unmodifiableList(CollectionLiterals.<WeaviatePrinter>newArrayList(_weaviatePython, _weaviateTypeScript, _weaviateJava));
      final String requested = System.getProperty(WeaviateGenerator.LANGUAGES_PROPERTY);
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
          final Function1<WeaviatePrinter, String> _function_1 = (WeaviatePrinter it) -> {
            return it.language();
          };
          _xifexpression_1 = IterableExtensions.<String>toSet(ListExtensions.<WeaviatePrinter, String>map(all, _function_1));
        }
        _xifexpression = _xifexpression_1;
      }
      final Set<String> selected = _xifexpression;
      final Function1<WeaviatePrinter, Boolean> _function_2 = (WeaviatePrinter it) -> {
        return Boolean.valueOf(selected.contains(it.language()));
      };
      _xblockexpression = IterableExtensions.<WeaviatePrinter>toList(IterableExtensions.<WeaviatePrinter>filter(all, _function_2));
    }
    return _xblockexpression;
  }

  public Program program(final Script script) {
    Program _xblockexpression = null;
    {
      final WeaviateStatements statements = new WeaviateStatements(script);
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

  private Program.Connection connection(final Script script) {
    Program.Connection _xblockexpression = null;
    {
      final Connection connection = IterableExtensions.<Connection>head(EcoreUtil2.<Connection>getAllContentsOfType(script, Connection.class));
      if ((connection == null)) {
        String _defaultUri = this.defaultUri();
        return new Program.Connection(_defaultUri, null, null, null, null, null);
      }
      Literal _elvis = null;
      Literal _apiKey = connection.getApiKey();
      if (_apiKey != null) {
        _elvis = _apiKey;
      } else {
        Literal _token = connection.getToken();
        _elvis = _token;
      }
      final Literal token = _elvis;
      String _uri = this.uri(connection);
      Code.Expr _xifexpression = null;
      if ((token == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = Literals.of(token);
      }
      Double _xifexpression_1 = null;
      int _timeoutMillis = connection.getTimeoutMillis();
      boolean _greaterThan = (_timeoutMillis > 0);
      if (_greaterThan) {
        int _timeoutMillis_1 = connection.getTimeoutMillis();
        _xifexpression_1 = Double.valueOf((_timeoutMillis_1 / 1000.0));
      } else {
        _xifexpression_1 = null;
      }
      _xblockexpression = new Program.Connection(_uri, _xifexpression, 
        null, 
        null, 
        null, _xifexpression_1);
    }
    return _xblockexpression;
  }

  private String uri(final Connection connection) {
    String _xblockexpression = null;
    {
      String _uri = connection.getUri();
      boolean _tripleNotEquals = (_uri != null);
      if (_tripleNotEquals) {
        return connection.getUri();
      }
      String _host = connection.getHost();
      boolean _tripleNotEquals_1 = (_host != null);
      if (_tripleNotEquals_1) {
        String _xifexpression = null;
        boolean _isTls = connection.isTls();
        if (_isTls) {
          _xifexpression = "https";
        } else {
          _xifexpression = "http";
        }
        final String scheme = _xifexpression;
        String _host_1 = connection.getHost();
        String _plus = ((scheme + "://") + _host_1);
        String _plus_1 = (_plus + ":");
        String _xifexpression_1 = null;
        int _port = connection.getPort();
        boolean _greaterThan = (_port > 0);
        if (_greaterThan) {
          _xifexpression_1 = String.valueOf(connection.getPort());
        } else {
          _xifexpression_1 = this.manifest.get("server.port");
        }
        return (_plus_1 + _xifexpression_1);
      }
      _xblockexpression = this.defaultUri();
    }
    return _xblockexpression;
  }

  private int grpcPort(final Script script) {
    int _xblockexpression = (int) 0;
    {
      final Connection connection = IterableExtensions.<Connection>head(EcoreUtil2.<Connection>getAllContentsOfType(script, Connection.class));
      int _xifexpression = (int) 0;
      if (((connection != null) && (connection.getGrpcPort() > 0))) {
        _xifexpression = connection.getGrpcPort();
      } else {
        _xifexpression = Integer.parseInt(this.manifest.get("grpc.port"));
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  private String defaultUri() {
    String _get = this.manifest.get("server.port");
    return ("http://localhost:" + _get);
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
