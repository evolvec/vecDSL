package org.ftn.vecDSL.generator.vespa;

import com.google.common.collect.Iterables;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.xtext.xbase.lib.Exceptions;
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
 * Vespa target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/vespa/python, typescript, java) and writes a Docker Compose file for a single Vespa node
 * (generated/vespa/docker-compose.yml). The programs deploy the application packages the generator builds and use the
 * document and query APIs over HTTP: Python with urllib, TypeScript with fetch, Java with java.net.http and Gson.
 * 
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
@SuppressWarnings("all")
public class VespaGenerator implements TargetGenerator {
  public static final String VENDOR = VespaPrinter.VENDOR;

  public static final String LANGUAGES_PROPERTY = "vecdsl.languages";

  private final VespaManifest manifest = VespaManifest.load();

  @Override
  public String vendor() {
    return VespaGenerator.VENDOR;
  }

  @Override
  public void generate(final Script script, final IFileSystemAccess2 fsa) {
    final Program program = this.program(script);
    final String configUrl = this.configUrl(script);
    List<VespaPrinter> _printers = this.printers(script);
    for (final VespaPrinter printer : _printers) {
      Set<Map.Entry<String, String>> _entrySet = printer.files(program, this.manifest, configUrl).entrySet();
      for (final Map.Entry<String, String> file : _entrySet) {
        String _language = printer.language();
        String _plus = (_language + "/");
        String _key = file.getKey();
        String _plus_1 = (_plus + _key);
        fsa.generateFile(_plus_1, VespaGenerator.VENDOR, file.getValue());
      }
    }
    fsa.generateFile("docker-compose.yml", VespaGenerator.VENDOR, this.manifest.render("environment/docker-compose.yml"));
  }

  public List<VespaPrinter> printers(final Script script) {
    List<VespaPrinter> _xblockexpression = null;
    {
      VespaPython _vespaPython = new VespaPython();
      VespaTypeScript _vespaTypeScript = new VespaTypeScript();
      VespaJava _vespaJava = new VespaJava();
      final List<VespaPrinter> all = Collections.<VespaPrinter>unmodifiableList(CollectionLiterals.<VespaPrinter>newArrayList(_vespaPython, _vespaTypeScript, _vespaJava));
      final String requested = System.getProperty(VespaGenerator.LANGUAGES_PROPERTY);
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
          final Function1<VespaPrinter, String> _function_1 = (VespaPrinter it) -> {
            return it.language();
          };
          _xifexpression_1 = IterableExtensions.<String>toSet(ListExtensions.<VespaPrinter, String>map(all, _function_1));
        }
        _xifexpression = _xifexpression_1;
      }
      final Set<String> selected = _xifexpression;
      final Function1<VespaPrinter, Boolean> _function_2 = (VespaPrinter it) -> {
        return Boolean.valueOf(selected.contains(it.language()));
      };
      _xblockexpression = IterableExtensions.<VespaPrinter>toList(IterableExtensions.<VespaPrinter>filter(all, _function_2));
    }
    return _xblockexpression;
  }

  public Program program(final Script script) {
    Program _xblockexpression = null;
    {
      final VespaStatements statements = new VespaStatements(script);
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
   * The query and document API URL of the program: the connection's uri read by endpoint(), or one built from host and port (https with
   * tls). Tokens never enter the URL; the runtime reads VECDSL_VESPA_TOKEN from the environment.
   */
  private Program.Connection connection(final Script script) {
    Program.Connection _xblockexpression = null;
    {
      final Connection declared = IterableExtensions.<Connection>head(EcoreUtil2.<Connection>getAllContentsOfType(script, Connection.class));
      final String defaultUrl = this.manifest.get("url");
      if ((declared == null)) {
        return new Program.Connection(defaultUrl, null, null, null, null, null);
      }
      String _xifexpression = null;
      String _uri = declared.getUri();
      boolean _tripleNotEquals = (_uri != null);
      if (_tripleNotEquals) {
        String _elvis = null;
        URI _endpoint = VespaGenerator.endpoint(declared);
        String _string = null;
        if (_endpoint!=null) {
          _string=_endpoint.toString();
        }
        if (_string != null) {
          _elvis = _string;
        } else {
          String _uri_1 = declared.getUri();
          _elvis = _uri_1;
        }
        _xifexpression = _elvis;
      } else {
        String _xifexpression_1 = null;
        if ((((declared.getHost() != null) || (declared.getPort() > 0)) || declared.isTls())) {
          String _xifexpression_2 = null;
          boolean _isTls = declared.isTls();
          if (_isTls) {
            _xifexpression_2 = "https://";
          } else {
            _xifexpression_2 = "http://";
          }
          String _elvis_1 = null;
          String _host = declared.getHost();
          if (_host != null) {
            _elvis_1 = _host;
          } else {
            _elvis_1 = "127.0.0.1";
          }
          String _plus = (_xifexpression_2 + _elvis_1);
          String _plus_1 = (_plus + ":");
          Object _xifexpression_3 = null;
          int _port = declared.getPort();
          boolean _greaterThan = (_port > 0);
          if (_greaterThan) {
            _xifexpression_3 = Integer.valueOf(declared.getPort());
          } else {
            _xifexpression_3 = this.manifest.get("query.port");
          }
          _xifexpression_1 = (_plus_1 + _xifexpression_3);
        } else {
          _xifexpression_1 = defaultUrl;
        }
        _xifexpression = _xifexpression_1;
      }
      final String url = _xifexpression;
      Double _xifexpression_4 = null;
      boolean _written = ModelUtil.written(declared, "timeoutMillis");
      if (_written) {
        int _timeoutMillis = declared.getTimeoutMillis();
        _xifexpression_4 = Double.valueOf((_timeoutMillis / 1000.0));
      } else {
        _xifexpression_4 = null;
      }
      final Double seconds = _xifexpression_4;
      _xblockexpression = new Program.Connection(url, null, null, null, null, seconds);
    }
    return _xblockexpression;
  }

  /**
   * The config server URL: the host of the connection's endpoint or host with the config server port, or the default.
   */
  public String configUrl(final Script script) {
    String _xblockexpression = null;
    {
      final Connection declared = IterableExtensions.<Connection>head(EcoreUtil2.<Connection>getAllContentsOfType(script, Connection.class));
      if (((declared == null) || ((declared.getUri() == null) && (declared.getHost() == null)))) {
        return this.manifest.get("config.url");
      }
      URI _xifexpression = null;
      String _uri = declared.getUri();
      boolean _tripleNotEquals = (_uri != null);
      if (_tripleNotEquals) {
        _xifexpression = VespaGenerator.endpoint(declared);
      } else {
        _xifexpression = null;
      }
      final URI address = _xifexpression;
      String _xifexpression_1 = null;
      if ((address != null)) {
        _xifexpression_1 = address.getHost();
      } else {
        String _xifexpression_2 = null;
        String _uri_1 = declared.getUri();
        boolean _tripleEquals = (_uri_1 == null);
        if (_tripleEquals) {
          _xifexpression_2 = declared.getHost();
        } else {
          _xifexpression_2 = null;
        }
        _xifexpression_1 = _xifexpression_2;
      }
      final String host = _xifexpression_1;
      String _xifexpression_3 = null;
      if ((address != null)) {
        _xifexpression_3 = address.getScheme();
      } else {
        String _xifexpression_4 = null;
        boolean _isTls = declared.isTls();
        if (_isTls) {
          _xifexpression_4 = "https";
        } else {
          _xifexpression_4 = "http";
        }
        _xifexpression_3 = _xifexpression_4;
      }
      final String scheme = _xifexpression_3;
      if ((host == null)) {
        return this.manifest.get("config.url");
      }
      String _get = this.manifest.get("config.port");
      _xblockexpression = ((((scheme + "://") + host) + ":") + _get);
    }
    return _xblockexpression;
  }

  /**
   * The connection's uri as an http or https URL: a uri without a scheme (host:port or host:port/path) is read
   * as http://<uri>, https with tls. Null when the uri does not parse as a URL with a host or has another scheme; VespaStatements
   * raises that reason in the connection statement, and the program keeps the uri as written and the default config server URL.
   */
  public static URI endpoint(final Connection declared) {
    URI _xblockexpression = null;
    {
      String _xifexpression = null;
      boolean _matches = declared.getUri().matches("[A-Za-z][A-Za-z0-9+.-]*://.*");
      if (_matches) {
        _xifexpression = declared.getUri();
      } else {
        String _xifexpression_1 = null;
        boolean _isTls = declared.isTls();
        if (_isTls) {
          _xifexpression_1 = "https://";
        } else {
          _xifexpression_1 = "http://";
        }
        String _uri = declared.getUri();
        _xifexpression = (_xifexpression_1 + _uri);
      }
      final String text = _xifexpression;
      URI _xtrycatchfinallyexpression = null;
      try {
        _xtrycatchfinallyexpression = new URI(text);
      } catch (final Throwable _t) {
        if (_t instanceof URISyntaxException) {
          _xtrycatchfinallyexpression = null;
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      final URI address = _xtrycatchfinallyexpression;
      URI _xifexpression_2 = null;
      if ((((address == null) || (address.getHost() == null)) || (!Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("http", "https")).contains(address.getScheme().toLowerCase())))) {
        _xifexpression_2 = null;
      } else {
        _xifexpression_2 = address;
      }
      _xblockexpression = _xifexpression_2;
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
