package org.ftn.vecDSL.generator.milvus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.ftn.vecDSL.generator.TargetGenerator;
import org.ftn.vecDSL.generator.code.Code;
import org.ftn.vecDSL.generator.code.Literals;
import org.ftn.vecDSL.generator.code.Program;
import org.ftn.vecDSL.generator.code.Syntax;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Connection;
import org.ftn.vecDSL.vecDSL.EDeploymentMode;
import org.ftn.vecDSL.vecDSL.Literal;
import org.ftn.vecDSL.vecDSL.Method;
import org.ftn.vecDSL.vecDSL.Parameter;
import org.ftn.vecDSL.vecDSL.Script;

/**
 * Milvus target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/milvus/python, typescript, java) and writes a Docker Compose file for a standalone server of
 * the supported version (generated/milvus/docker-compose.yml).
 * 
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
@SuppressWarnings("all")
public class MilvusGenerator implements TargetGenerator {
  public static final String VENDOR = MilvusPrinter.VENDOR;

  public static final String LANGUAGES_PROPERTY = "vecdsl.languages";

  /**
   * Emitted constructs that Milvus Lite rejects, with the feature named in the run-time error.
   */
  private static final List<Pair<String, String>> LITE_GAPS = Collections.<Pair<String, String>>unmodifiableList(CollectionLiterals.<Pair<String, String>>newArrayList(Pair.<String, String>of("PHRASE_MATCH(", "phrase matching"), Pair.<String, String>of("STL_SORT", "STL_SORT scalar indexes"), Pair.<String, String>of("add_collection_field", "adding fields to an existing collection")));

  private final MilvusManifest manifest = MilvusManifest.load();

  @Override
  public String vendor() {
    return MilvusGenerator.VENDOR;
  }

  @Override
  public void generate(final Script script, final IFileSystemAccess2 fsa) {
    final Program program = this.program(script);
    List<MilvusPrinter> _printers = this.printers(script);
    for (final MilvusPrinter printer : _printers) {
      Set<Map.Entry<String, String>> _entrySet = printer.files(program, this.manifest).entrySet();
      for (final Map.Entry<String, String> file : _entrySet) {
        String _language = printer.language();
        String _plus = (_language + "/");
        String _key = file.getKey();
        String _plus_1 = (_plus + _key);
        fsa.generateFile(_plus_1, MilvusGenerator.VENDOR, file.getValue());
      }
    }
    fsa.generateFile("docker-compose.yml", MilvusGenerator.VENDOR, this.manifest.render("environment/docker-compose.yml"));
  }

  public List<MilvusPrinter> printers(final Script script) {
    List<MilvusPrinter> _xblockexpression = null;
    {
      MilvusPython _milvusPython = new MilvusPython();
      MilvusTypeScript _milvusTypeScript = new MilvusTypeScript();
      MilvusJava _milvusJava = new MilvusJava();
      final List<MilvusPrinter> all = Collections.<MilvusPrinter>unmodifiableList(CollectionLiterals.<MilvusPrinter>newArrayList(_milvusPython, _milvusTypeScript, _milvusJava));
      final String requested = System.getProperty(MilvusGenerator.LANGUAGES_PROPERTY);
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
          final Function1<MilvusPrinter, String> _function_1 = (MilvusPrinter it) -> {
            return it.language();
          };
          _xifexpression_1 = IterableExtensions.<String>toSet(ListExtensions.<MilvusPrinter, String>map(all, _function_1));
        }
        _xifexpression = _xifexpression_1;
      }
      final Set<String> selected = _xifexpression;
      final Function1<MilvusPrinter, Boolean> _function_2 = (MilvusPrinter it) -> {
        return Boolean.valueOf(selected.contains(it.language()));
      };
      _xblockexpression = IterableExtensions.<MilvusPrinter>toList(IterableExtensions.<MilvusPrinter>filter(all, _function_2));
    }
    return _xblockexpression;
  }

  public Program program(final Script script) {
    Program _xblockexpression = null;
    {
      final MilvusStatements statements = new MilvusStatements(script);
      final List<Program.Method> methods = CollectionLiterals.<Program.Method>newArrayList();
      final Set<String> used = CollectionLiterals.<String>newHashSet();
      int index = 0;
      EList<Method> _methods = script.getMethods();
      for (final Method statement : _methods) {
        {
          index = (index + 1);
          List<Program.Method> _methods_1 = statements.methods(statement, this.uniqueName(statement, index, used));
          for (final Program.Method method : _methods_1) {
            String _name = method.name();
            List<String> _parameters = method.parameters();
            List<Code.Stmt> _body = method.body();
            List<String> _notes = method.notes();
            List<String> _serverFeatures = this.serverFeatures(method.body());
            boolean _helper = method.helper();
            Program.Method _method = new Program.Method(_name, _parameters, _body, _notes, _serverFeatures, _helper);
            methods.add(_method);
          }
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
      Literal _token = connection.getToken();
      if (_token != null) {
        _elvis = _token;
      } else {
        Literal _apiKey = connection.getApiKey();
        _elvis = _apiKey;
      }
      final Literal token = _elvis;
      String _uri = this.uri(connection, script);
      Code.Expr _xifexpression = null;
      if ((token == null)) {
        _xifexpression = null;
      } else {
        _xifexpression = Literals.of(token);
      }
      String _username = connection.getUsername();
      Code.Expr _xifexpression_1 = null;
      Literal _password = connection.getPassword();
      boolean _tripleEquals = (_password == null);
      if (_tripleEquals) {
        _xifexpression_1 = null;
      } else {
        _xifexpression_1 = Literals.of(connection.getPassword());
      }
      String _databaseName = connection.getDatabaseName();
      Double _xifexpression_2 = null;
      int _timeoutMillis = connection.getTimeoutMillis();
      boolean _greaterThan = (_timeoutMillis > 0);
      if (_greaterThan) {
        int _timeoutMillis_1 = connection.getTimeoutMillis();
        _xifexpression_2 = Double.valueOf((_timeoutMillis_1 / 1000.0));
      } else {
        _xifexpression_2 = null;
      }
      _xblockexpression = new Program.Connection(_uri, _xifexpression, _username, _xifexpression_1, _databaseName, _xifexpression_2);
    }
    return _xblockexpression;
  }

  private String uri(final Connection connection, final Script script) {
    String _xblockexpression = null;
    {
      String _uri = connection.getUri();
      boolean _tripleNotEquals = (_uri != null);
      if (_tripleNotEquals) {
        return connection.getUri();
      }
      String _localPath = connection.getLocalPath();
      boolean _tripleNotEquals_1 = (_localPath != null);
      if (_tripleNotEquals_1) {
        return connection.getLocalPath();
      }
      if ((connection.isInMemory() || Objects.equals(connection.getDeployment(), EDeploymentMode.EMBEDDED))) {
        String _snake = Syntax.snake(script.getName());
        String _plus = ("./" + _snake);
        return (_plus + ".db");
      }
      String _host = connection.getHost();
      boolean _tripleNotEquals_2 = (_host != null);
      if (_tripleNotEquals_2) {
        String _xifexpression = null;
        boolean _isTls = connection.isTls();
        if (_isTls) {
          _xifexpression = "https";
        } else {
          _xifexpression = "http";
        }
        final String scheme = _xifexpression;
        String _host_1 = connection.getHost();
        String _plus_1 = ((scheme + "://") + _host_1);
        String _plus_2 = (_plus_1 + ":");
        String _xifexpression_1 = null;
        int _port = connection.getPort();
        boolean _greaterThan = (_port > 0);
        if (_greaterThan) {
          _xifexpression_1 = String.valueOf(connection.getPort());
        } else {
          _xifexpression_1 = this.manifest.get("server.port");
        }
        return (_plus_2 + _xifexpression_1);
      }
      _xblockexpression = this.defaultUri();
    }
    return _xblockexpression;
  }

  private String defaultUri() {
    String _get = this.manifest.get("server.port");
    return ("http://localhost:" + _get);
  }

  private List<String> serverFeatures(final List<Code.Stmt> body) {
    List<String> _xblockexpression = null;
    {
      final List<String> found = CollectionLiterals.<String>newArrayList();
      final Consumer<Code.Expr> _function = (Code.Expr expr) -> {
        String _switchResult = null;
        boolean _matched = false;
        if (expr instanceof Code.Filter) {
          _matched=true;
          _switchResult = ((Code.Filter)expr).template();
        }
        if (!_matched) {
          if (expr instanceof Code.Text) {
            _matched=true;
            _switchResult = ((Code.Text)expr).value();
          }
        }
        if (!_matched) {
          if (expr instanceof Code.Call) {
            _matched=true;
            _switchResult = ((Code.Call)expr).op();
          }
        }
        if (!_matched) {
          _switchResult = null;
        }
        final String marker = _switchResult;
        if ((marker != null)) {
          for (final Pair<String, String> gap : MilvusGenerator.LITE_GAPS) {
            if ((marker.contains(gap.getKey()) && (!found.contains(gap.getValue())))) {
              String _value = gap.getValue();
              found.add(_value);
            }
          }
        }
      };
      Code.visit(body, _function);
      _xblockexpression = found;
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
