package org.ftn.vecDSL.generator.redis;

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
 * Redis target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/redis/python, typescript, java) and writes a Docker Compose file for Redis Open Source
 * (generated/redis/docker-compose.yml). Python uses redis-py, TypeScript node-redis, Java Jedis.
 * 
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
@SuppressWarnings("all")
public class RedisGenerator implements TargetGenerator {
  public static final String VENDOR = RedisPrinter.VENDOR;

  public static final String LANGUAGES_PROPERTY = "vecdsl.languages";

  private final RedisManifest manifest = RedisManifest.load();

  @Override
  public String vendor() {
    return RedisGenerator.VENDOR;
  }

  @Override
  public void generate(final Script script, final IFileSystemAccess2 fsa) {
    final Program program = this.program(script);
    List<RedisPrinter> _printers = this.printers(script);
    for (final RedisPrinter printer : _printers) {
      Set<Map.Entry<String, String>> _entrySet = printer.files(program, this.manifest).entrySet();
      for (final Map.Entry<String, String> file : _entrySet) {
        String _language = printer.language();
        String _plus = (_language + "/");
        String _key = file.getKey();
        String _plus_1 = (_plus + _key);
        fsa.generateFile(_plus_1, RedisGenerator.VENDOR, file.getValue());
      }
    }
    fsa.generateFile("docker-compose.yml", RedisGenerator.VENDOR, this.manifest.render("environment/docker-compose.yml"));
  }

  public List<RedisPrinter> printers(final Script script) {
    List<RedisPrinter> _xblockexpression = null;
    {
      RedisPython _redisPython = new RedisPython();
      RedisTypeScript _redisTypeScript = new RedisTypeScript();
      RedisJava _redisJava = new RedisJava();
      final List<RedisPrinter> all = Collections.<RedisPrinter>unmodifiableList(CollectionLiterals.<RedisPrinter>newArrayList(_redisPython, _redisTypeScript, _redisJava));
      final String requested = System.getProperty(RedisGenerator.LANGUAGES_PROPERTY);
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
          final Function1<RedisPrinter, String> _function_1 = (RedisPrinter it) -> {
            return it.language();
          };
          _xifexpression_1 = IterableExtensions.<String>toSet(ListExtensions.<RedisPrinter, String>map(all, _function_1));
        }
        _xifexpression = _xifexpression_1;
      }
      final Set<String> selected = _xifexpression;
      final Function1<RedisPrinter, Boolean> _function_2 = (RedisPrinter it) -> {
        return Boolean.valueOf(selected.contains(it.language()));
      };
      _xblockexpression = IterableExtensions.<RedisPrinter>toList(IterableExtensions.<RedisPrinter>filter(all, _function_2));
    }
    return _xblockexpression;
  }

  public Program program(final Script script) {
    Program _xblockexpression = null;
    {
      final RedisStatements statements = new RedisStatements(script);
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
   * The default URL of the program: the connection's uri, or one built from host and port (rediss with tls). Credentials never
   * enter the URL: the user and password come from the environment (the connection's env(...) reference names the variable).
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
      String _xifexpression_1 = null;
      String _uri = declared.getUri();
      boolean _tripleNotEquals = (_uri != null);
      if (_tripleNotEquals) {
        _xifexpression_1 = declared.getUri();
      } else {
        String _xifexpression_2 = null;
        if ((((declared.getHost() != null) || (declared.getPort() > 0)) || declared.isTls())) {
          String _xifexpression_3 = null;
          boolean _isTls = declared.isTls();
          if (_isTls) {
            _xifexpression_3 = "rediss://";
          } else {
            _xifexpression_3 = "redis://";
          }
          String _elvis = null;
          String _host = declared.getHost();
          if (_host != null) {
            _elvis = _host;
          } else {
            _elvis = "127.0.0.1";
          }
          String _plus = (_xifexpression_3 + _elvis);
          String _plus_1 = (_plus + ":");
          Object _xifexpression_4 = null;
          int _port = declared.getPort();
          boolean _greaterThan = (_port > 0);
          if (_greaterThan) {
            _xifexpression_4 = Integer.valueOf(declared.getPort());
          } else {
            _xifexpression_4 = this.manifest.get("server.port");
          }
          _xifexpression_2 = (_plus_1 + _xifexpression_4);
        } else {
          _xifexpression_2 = defaultUrl;
        }
        _xifexpression_1 = _xifexpression_2;
      }
      final String url = _xifexpression_1;
      String _username = declared.getUsername();
      _xblockexpression = new Program.Connection(url, null, _username, password, null, null);
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
