package org.ftn.vecDSL.generator.pinecone;

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
 * Pinecone target. Maps a script once to the code model, then prints it for every selected host language
 * (generated/pinecone/python, typescript, java) and writes a Docker Compose file for the Pinecone API mock
 * (generated/pinecone/docker-compose.yml), which checks requests against the API specification. Pinecone itself
 * runs only as a managed service; Pinecone Local reached end of life on 7 October 2026.
 * 
 * Host languages: the system property vecdsl.languages (comma-separated) if set, otherwise the script's
 * 'language' hint, otherwise all.
 */
@SuppressWarnings("all")
public class PineconeGenerator implements TargetGenerator {
  public static final String VENDOR = PineconePrinter.VENDOR;

  public static final String LANGUAGES_PROPERTY = "vecdsl.languages";

  private final PineconeManifest manifest = PineconeManifest.load();

  @Override
  public String vendor() {
    return PineconeGenerator.VENDOR;
  }

  @Override
  public void generate(final Script script, final IFileSystemAccess2 fsa) {
    final Program program = this.program(script);
    List<PineconePrinter> _printers = this.printers(script);
    for (final PineconePrinter printer : _printers) {
      Set<Map.Entry<String, String>> _entrySet = printer.files(program, this.manifest).entrySet();
      for (final Map.Entry<String, String> file : _entrySet) {
        String _language = printer.language();
        String _plus = (_language + "/");
        String _key = file.getKey();
        String _plus_1 = (_plus + _key);
        fsa.generateFile(_plus_1, PineconeGenerator.VENDOR, file.getValue());
      }
    }
    fsa.generateFile("docker-compose.yml", PineconeGenerator.VENDOR, this.manifest.render("environment/docker-compose.yml"));
  }

  public List<PineconePrinter> printers(final Script script) {
    List<PineconePrinter> _xblockexpression = null;
    {
      PineconePython _pineconePython = new PineconePython();
      PineconeTypeScript _pineconeTypeScript = new PineconeTypeScript();
      PineconeJava _pineconeJava = new PineconeJava();
      final List<PineconePrinter> all = Collections.<PineconePrinter>unmodifiableList(CollectionLiterals.<PineconePrinter>newArrayList(_pineconePython, _pineconeTypeScript, _pineconeJava));
      final String requested = System.getProperty(PineconeGenerator.LANGUAGES_PROPERTY);
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
          final Function1<PineconePrinter, String> _function_1 = (PineconePrinter it) -> {
            return it.language();
          };
          _xifexpression_1 = IterableExtensions.<String>toSet(ListExtensions.<PineconePrinter, String>map(all, _function_1));
        }
        _xifexpression = _xifexpression_1;
      }
      final Set<String> selected = _xifexpression;
      final Function1<PineconePrinter, Boolean> _function_2 = (PineconePrinter it) -> {
        return Boolean.valueOf(selected.contains(it.language()));
      };
      _xblockexpression = IterableExtensions.<PineconePrinter>toList(IterableExtensions.<PineconePrinter>filter(all, _function_2));
    }
    return _xblockexpression;
  }

  public Program program(final Script script) {
    Program _xblockexpression = null;
    {
      String _get = this.manifest.get("namespace");
      final PineconeStatements statements = new PineconeStatements(script, _get);
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
   * Control-plane URI and the environment variable that holds the API key (VECDSL_PINECONE_API_KEY by default).
   */
  private Program.Connection connection(final Script script) {
    Program.Connection _xblockexpression = null;
    {
      final Connection connection = IterableExtensions.<Connection>head(EcoreUtil2.<Connection>getAllContentsOfType(script, Connection.class));
      final String defaultUri = this.manifest.get("control.uri");
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
      String _elvis_1 = null;
      String _uri = connection.getUri();
      if (_uri != null) {
        _elvis_1 = _uri;
      } else {
        _elvis_1 = defaultUri;
      }
      Code.Env _xifexpression = null;
      if ((secret instanceof EnvRef)) {
        String _variable = ((EnvRef)secret).getVariable();
        _xifexpression = new Code.Env(_variable);
      } else {
        _xifexpression = null;
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
      _xblockexpression = new Program.Connection(_elvis_1, _xifexpression, 
        null, 
        null, 
        null, _xifexpression_1);
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
