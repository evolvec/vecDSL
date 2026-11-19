package org.ftn.vecDSL.generator.qdrant;

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
 * Qdrant target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/qdrant/python, typescript, java) and writes a Docker Compose file for the Qdrant server
 * (generated/qdrant/docker-compose.yml). Python and TypeScript use Qdrant's REST API, Java its gRPC API.
 * 
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
@SuppressWarnings("all")
public class QdrantGenerator implements TargetGenerator {
  public static final String VENDOR = QdrantPrinter.VENDOR;

  public static final String LANGUAGES_PROPERTY = "vecdsl.languages";

  private final QdrantManifest manifest = QdrantManifest.load();

  @Override
  public String vendor() {
    return QdrantGenerator.VENDOR;
  }

  @Override
  public void generate(final Script script, final IFileSystemAccess2 fsa) {
    final Program program = this.program(script);
    final int grpcPort = this.grpcPort(script);
    List<QdrantPrinter> _printers = this.printers(script);
    for (final QdrantPrinter printer : _printers) {
      Set<Map.Entry<String, String>> _entrySet = printer.files(program, this.manifest, grpcPort).entrySet();
      for (final Map.Entry<String, String> file : _entrySet) {
        String _language = printer.language();
        String _plus = (_language + "/");
        String _key = file.getKey();
        String _plus_1 = (_plus + _key);
        fsa.generateFile(_plus_1, QdrantGenerator.VENDOR, file.getValue());
      }
    }
    fsa.generateFile("docker-compose.yml", QdrantGenerator.VENDOR, this.manifest.render("environment/docker-compose.yml"));
  }

  public List<QdrantPrinter> printers(final Script script) {
    List<QdrantPrinter> _xblockexpression = null;
    {
      QdrantPython _qdrantPython = new QdrantPython();
      QdrantTypeScript _qdrantTypeScript = new QdrantTypeScript();
      QdrantJava _qdrantJava = new QdrantJava();
      final List<QdrantPrinter> all = Collections.<QdrantPrinter>unmodifiableList(CollectionLiterals.<QdrantPrinter>newArrayList(_qdrantPython, _qdrantTypeScript, _qdrantJava));
      final String requested = System.getProperty(QdrantGenerator.LANGUAGES_PROPERTY);
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
          final Function1<QdrantPrinter, String> _function_1 = (QdrantPrinter it) -> {
            return it.language();
          };
          _xifexpression_1 = IterableExtensions.<String>toSet(ListExtensions.<QdrantPrinter, String>map(all, _function_1));
        }
        _xifexpression = _xifexpression_1;
      }
      final Set<String> selected = _xifexpression;
      final Function1<QdrantPrinter, Boolean> _function_2 = (QdrantPrinter it) -> {
        return Boolean.valueOf(selected.contains(it.language()));
      };
      _xblockexpression = IterableExtensions.<QdrantPrinter>toList(IterableExtensions.<QdrantPrinter>filter(all, _function_2));
    }
    return _xblockexpression;
  }

  public Program program(final Script script) {
    Program _xblockexpression = null;
    {
      final QdrantStatements statements = new QdrantStatements(script);
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
   * REST endpoint and the environment variable that holds the API key, if the script names one.
   */
  private Program.Connection connection(final Script script) {
    Program.Connection _xblockexpression = null;
    {
      final Connection connection = IterableExtensions.<Connection>head(EcoreUtil2.<Connection>getAllContentsOfType(script, Connection.class));
      final String defaultUri = this.manifest.get("rest.uri");
      if ((connection == null)) {
        return new Program.Connection(defaultUri, null, null, null, null, null);
      }
      Literal _elvis = null;
      Literal _apiKey = connection.getApiKey();
      if (_apiKey != null) {
        _elvis = _apiKey;
      } else {
        Literal _token = connection.getToken();
        _elvis = _token;
      }
      final Literal secret = _elvis;
      String _xifexpression = null;
      String _uri = connection.getUri();
      boolean _tripleNotEquals = (_uri != null);
      if (_tripleNotEquals) {
        _xifexpression = connection.getUri();
      } else {
        String _xifexpression_1 = null;
        String _host = connection.getHost();
        boolean _tripleNotEquals_1 = (_host != null);
        if (_tripleNotEquals_1) {
          String _xifexpression_2 = null;
          boolean _isTls = connection.isTls();
          if (_isTls) {
            _xifexpression_2 = "https://";
          } else {
            _xifexpression_2 = "http://";
          }
          String _host_1 = connection.getHost();
          String _plus = (_xifexpression_2 + _host_1);
          String _plus_1 = (_plus + ":");
          Object _xifexpression_3 = null;
          int _port = connection.getPort();
          boolean _greaterThan = (_port > 0);
          if (_greaterThan) {
            _xifexpression_3 = Integer.valueOf(connection.getPort());
          } else {
            _xifexpression_3 = this.manifest.get("rest.port");
          }
          _xifexpression_1 = (_plus_1 + _xifexpression_3);
        } else {
          _xifexpression_1 = defaultUri;
        }
        _xifexpression = _xifexpression_1;
      }
      final String uri = _xifexpression;
      Code.Env _xifexpression_4 = null;
      if ((secret instanceof EnvRef)) {
        String _variable = ((EnvRef)secret).getVariable();
        _xifexpression_4 = new Code.Env(_variable);
      } else {
        _xifexpression_4 = null;
      }
      Double _xifexpression_5 = null;
      int _timeoutMillis = connection.getTimeoutMillis();
      boolean _greaterThan_1 = (_timeoutMillis > 0);
      if (_greaterThan_1) {
        int _timeoutMillis_1 = connection.getTimeoutMillis();
        _xifexpression_5 = Double.valueOf((_timeoutMillis_1 / 1000.0));
      } else {
        _xifexpression_5 = null;
      }
      _xblockexpression = new Program.Connection(uri, _xifexpression_4, 
        null, 
        null, 
        null, _xifexpression_5);
    }
    return _xblockexpression;
  }

  /**
   * gRPC port of the Java client: the connection's grpc-port, else Qdrant's default.
   */
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
