package org.ftn.vecDSL.generator.mongodb;

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
 * MongoDB target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/mongodb/python, typescript, java) and writes a Docker Compose file for MongoDB with mongot (the Atlas Local image,
 * generated/mongodb/docker-compose.yml). Every language uses the official driver: pymongo (Python), mongodb (TypeScript) and
 * mongodb-driver-sync (Java).
 * 
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
@SuppressWarnings("all")
public class MongodbGenerator implements TargetGenerator {
  public static final String VENDOR = MongodbPrinter.VENDOR;

  public static final String LANGUAGES_PROPERTY = "vecdsl.languages";

  private final MongodbManifest manifest = MongodbManifest.load();

  @Override
  public String vendor() {
    return MongodbGenerator.VENDOR;
  }

  @Override
  public void generate(final Script script, final IFileSystemAccess2 fsa) {
    final Program program = this.program(script);
    List<MongodbPrinter> _printers = this.printers(script);
    for (final MongodbPrinter printer : _printers) {
      Set<Map.Entry<String, String>> _entrySet = printer.files(program, this.manifest).entrySet();
      for (final Map.Entry<String, String> file : _entrySet) {
        String _language = printer.language();
        String _plus = (_language + "/");
        String _key = file.getKey();
        String _plus_1 = (_plus + _key);
        fsa.generateFile(_plus_1, MongodbGenerator.VENDOR, file.getValue());
      }
    }
    fsa.generateFile("docker-compose.yml", MongodbGenerator.VENDOR, this.manifest.render("environment/docker-compose.yml"));
  }

  public List<MongodbPrinter> printers(final Script script) {
    List<MongodbPrinter> _xblockexpression = null;
    {
      MongodbPython _mongodbPython = new MongodbPython();
      MongodbTypeScript _mongodbTypeScript = new MongodbTypeScript();
      MongodbJava _mongodbJava = new MongodbJava();
      final List<MongodbPrinter> all = Collections.<MongodbPrinter>unmodifiableList(CollectionLiterals.<MongodbPrinter>newArrayList(_mongodbPython, _mongodbTypeScript, _mongodbJava));
      final String requested = System.getProperty(MongodbGenerator.LANGUAGES_PROPERTY);
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
          final Function1<MongodbPrinter, String> _function_1 = (MongodbPrinter it) -> {
            return it.language();
          };
          _xifexpression_1 = IterableExtensions.<String>toSet(ListExtensions.<MongodbPrinter, String>map(all, _function_1));
        }
        _xifexpression = _xifexpression_1;
      }
      final Set<String> selected = _xifexpression;
      final Function1<MongodbPrinter, Boolean> _function_2 = (MongodbPrinter it) -> {
        return Boolean.valueOf(selected.contains(it.language()));
      };
      _xblockexpression = IterableExtensions.<MongodbPrinter>toList(IterableExtensions.<MongodbPrinter>filter(all, _function_2));
    }
    return _xblockexpression;
  }

  public Program program(final Script script) {
    Program _xblockexpression = null;
    {
      String _get = this.manifest.get("database");
      final MongodbStatements statements = new MongodbStatements(script, _get);
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
   * The default URI of the program: the connection's uri, or one built from host and port (tls adds tls=true); the user is the
   * connection's username. Credentials never enter the URI (user information in a declared uri is removed): the generated program reads them from the environment.
   */
  private Program.Connection connection(final Script script) {
    Program.Connection _xblockexpression = null;
    {
      final Connection declared = IterableExtensions.<Connection>head(EcoreUtil2.<Connection>getAllContentsOfType(script, Connection.class));
      final String defaultUri = this.manifest.get("uri");
      if ((declared == null)) {
        return new Program.Connection(defaultUri, null, null, null, null, null);
      }
      Code.Expr _xifexpression = null;
      Literal _password = declared.getPassword();
      if ((_password instanceof EnvRef)) {
        _xifexpression = Literals.of(declared.getPassword());
      } else {
        _xifexpression = null;
      }
      final Code.Expr password = _xifexpression;
      String _xifexpression_1 = null;
      String _uri = declared.getUri();
      boolean _tripleNotEquals = (_uri != null);
      if (_tripleNotEquals) {
        _xifexpression_1 = declared.getUri().replaceFirst("//[^/@]*@", "//");
      } else {
        String _xifexpression_2 = null;
        String _host = declared.getHost();
        boolean _tripleNotEquals_1 = (_host != null);
        if (_tripleNotEquals_1) {
          String _host_1 = declared.getHost();
          String _plus = ("mongodb://" + _host_1);
          String _plus_1 = (_plus + ":");
          Object _xifexpression_3 = null;
          int _port = declared.getPort();
          boolean _greaterThan = (_port > 0);
          if (_greaterThan) {
            _xifexpression_3 = Integer.valueOf(declared.getPort());
          } else {
            _xifexpression_3 = this.manifest.get("server.port");
          }
          String _plus_2 = (_plus_1 + _xifexpression_3);
          String _plus_3 = (_plus_2 + "/?directConnection=true");
          String _xifexpression_4 = null;
          boolean _isTls = declared.isTls();
          if (_isTls) {
            _xifexpression_4 = "&tls=true";
          } else {
            _xifexpression_4 = "";
          }
          _xifexpression_2 = (_plus_3 + _xifexpression_4);
        } else {
          _xifexpression_2 = defaultUri;
        }
        _xifexpression_1 = _xifexpression_2;
      }
      final String uri = _xifexpression_1;
      Double _xifexpression_5 = null;
      int _timeoutMillis = declared.getTimeoutMillis();
      boolean _greaterThan_1 = (_timeoutMillis > 0);
      if (_greaterThan_1) {
        int _timeoutMillis_1 = declared.getTimeoutMillis();
        double _divide = (_timeoutMillis_1 / 1000.0);
        _xifexpression_5 = Double.valueOf(_divide);
      } else {
        _xifexpression_5 = null;
      }
      final Double timeout = _xifexpression_5;
      String _username = declared.getUsername();
      String _databaseName = declared.getDatabaseName();
      _xblockexpression = new Program.Connection(uri, null, _username, password, _databaseName, timeout);
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
