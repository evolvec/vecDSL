package org.ftn.vecDSL.generator.chroma;

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
 * Chroma target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/chroma/python, typescript, java) and writes a Docker Compose file for the Chroma server
 * (generated/chroma/docker-compose.yml). Python and TypeScript use Chroma's SDKs, Java its HTTP API directly.
 * 
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
@SuppressWarnings("all")
public class ChromaGenerator implements TargetGenerator {
  public static final String VENDOR = ChromaPrinter.VENDOR;

  public static final String LANGUAGES_PROPERTY = "vecdsl.languages";

  private final ChromaManifest manifest = ChromaManifest.load();

  @Override
  public String vendor() {
    return ChromaGenerator.VENDOR;
  }

  @Override
  public void generate(final Script script, final IFileSystemAccess2 fsa) {
    final Program program = this.program(script);
    List<ChromaPrinter> _printers = this.printers(script);
    for (final ChromaPrinter printer : _printers) {
      Set<Map.Entry<String, String>> _entrySet = printer.files(program, this.manifest).entrySet();
      for (final Map.Entry<String, String> file : _entrySet) {
        String _language = printer.language();
        String _plus = (_language + "/");
        String _key = file.getKey();
        String _plus_1 = (_plus + _key);
        fsa.generateFile(_plus_1, ChromaGenerator.VENDOR, file.getValue());
      }
    }
    fsa.generateFile("docker-compose.yml", ChromaGenerator.VENDOR, this.manifest.render("environment/docker-compose.yml"));
  }

  public List<ChromaPrinter> printers(final Script script) {
    List<ChromaPrinter> _xblockexpression = null;
    {
      ChromaPython _chromaPython = new ChromaPython();
      ChromaTypeScript _chromaTypeScript = new ChromaTypeScript();
      ChromaJava _chromaJava = new ChromaJava();
      final List<ChromaPrinter> all = Collections.<ChromaPrinter>unmodifiableList(CollectionLiterals.<ChromaPrinter>newArrayList(_chromaPython, _chromaTypeScript, _chromaJava));
      final String requested = System.getProperty(ChromaGenerator.LANGUAGES_PROPERTY);
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
          final Function1<ChromaPrinter, String> _function_1 = (ChromaPrinter it) -> {
            return it.language();
          };
          _xifexpression_1 = IterableExtensions.<String>toSet(ListExtensions.<ChromaPrinter, String>map(all, _function_1));
        }
        _xifexpression = _xifexpression_1;
      }
      final Set<String> selected = _xifexpression;
      final Function1<ChromaPrinter, Boolean> _function_2 = (ChromaPrinter it) -> {
        return Boolean.valueOf(selected.contains(it.language()));
      };
      _xblockexpression = IterableExtensions.<ChromaPrinter>toList(IterableExtensions.<ChromaPrinter>filter(all, _function_2));
    }
    return _xblockexpression;
  }

  public Program program(final Script script) {
    Program _xblockexpression = null;
    {
      final ChromaStatements statements = new ChromaStatements(script);
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
   * HTTP endpoint and database; ChromaStatements rejects what a Chroma connection cannot carry.
   */
  private Program.Connection connection(final Script script) {
    Program.Connection _xblockexpression = null;
    {
      final Connection declared = IterableExtensions.<Connection>head(EcoreUtil2.<Connection>getAllContentsOfType(script, Connection.class));
      final String defaultUri = this.manifest.get("http.uri");
      if ((declared == null)) {
        return new Program.Connection(defaultUri, null, null, null, null, null);
      }
      String _xifexpression = null;
      String _uri = declared.getUri();
      boolean _tripleNotEquals = (_uri != null);
      if (_tripleNotEquals) {
        _xifexpression = declared.getUri();
      } else {
        String _xifexpression_1 = null;
        String _host = declared.getHost();
        boolean _tripleNotEquals_1 = (_host != null);
        if (_tripleNotEquals_1) {
          String _xifexpression_2 = null;
          boolean _isTls = declared.isTls();
          if (_isTls) {
            _xifexpression_2 = "https://";
          } else {
            _xifexpression_2 = "http://";
          }
          String _host_1 = declared.getHost();
          String _plus = (_xifexpression_2 + _host_1);
          String _plus_1 = (_plus + ":");
          Object _xifexpression_3 = null;
          int _port = declared.getPort();
          boolean _greaterThan = (_port > 0);
          if (_greaterThan) {
            _xifexpression_3 = Integer.valueOf(declared.getPort());
          } else {
            _xifexpression_3 = this.manifest.get("http.port");
          }
          _xifexpression_1 = (_plus_1 + _xifexpression_3);
        } else {
          _xifexpression_1 = defaultUri;
        }
        _xifexpression = _xifexpression_1;
      }
      final String uri = _xifexpression;
      String _databaseName = declared.getDatabaseName();
      _xblockexpression = new Program.Connection(uri, null, null, null, _databaseName, null);
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
