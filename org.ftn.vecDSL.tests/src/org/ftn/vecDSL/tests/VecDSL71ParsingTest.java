package org.ftn.vecDSL.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.ftn.vecDSL.util.ModelUtil;
import org.ftn.vecDSL.vecDSL.Quantization;
import org.ftn.vecDSL.vecDSL.Script;
import org.ftn.vecDSL.vecDSL.SearchParams;
import org.ftn.vecDSL.vecDSL.StorageConfig;
import org.ftn.vecDSL.vecDSL.VectorField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * Parser tests added in v7.1: the target-recall clause on every form that admits it, and the optional-name positions,
 * which v7.0 and v7.1 already parse as intended.
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(VecDSLInjectorProvider.class)
public class VecDSL71ParsingTest {

	private static final String DOCS = "script t\ncreate collection docs { primary id field id type int vector-field type float_vector dim 3 }\n";

	@Inject
	private ParseHelper<Script> parseHelper;

	private Script parse(String source) throws Exception {
		Script script = parseHelper.parse(source);
		List<?> syntax = script.eResource().getErrors().stream().filter(d -> d instanceof XtextSyntaxDiagnostic).toList();
		assertTrue(syntax.isEmpty(), () -> "unexpected syntax errors: " + syntax);
		return script;
	}

	private void assertRecall(String query) throws Exception {
		SearchParams params = EcoreUtil2.getAllContentsOfType(parse(DOCS + query), SearchParams.class).get(0);
		assertTrue(ModelUtil.written(params, "targetRecall"));
		assertEquals(0.9, params.getTargetRecall(), 1e-9);
	}

	@Test
	public void targetRecallOnVectorSearch() throws Exception {
		assertRecall("vector-search in docs top 3 query vector [0.1, 0.2, 0.3] params { target-recall 0.9 } output *");
	}

	@Test
	public void targetRecallOnRecommend() throws Exception {
		assertRecall("recommend in docs top 3 positive [1] params { target-recall 0.9 } output *");
	}

	@Test
	public void targetRecallOnHybridVectorLeg() throws Exception {
		assertRecall("hybrid-search in docs top 3 { vector { query vector [0.1, 0.2, 0.3] params { target-recall 0.9 } } } output *");
	}

	@Test
	public void targetRecallOnPrefetchStage() throws Exception {
		assertRecall("vector-search in docs top 3 query vector [0.1, 0.2, 0.3] prefetch { stage limit 10 params { target-recall 0.9 } } output *");
	}

	@Test
	public void targetRecallPrecedesOtherSearchParams() throws Exception {
		assertRecall("vector-search in docs top 3 query vector [0.1, 0.2, 0.3] params { target-recall 0.9 timeout 200 oversampling 2.0 rescore } output *");
	}

	@Test
	public void unnamedVectorFieldBeforeTypeKeyword() throws Exception {
		VectorField field = only(parse("script t create collection c { primary id field id type int vector-field type float_vector dim 3 }"), VectorField.class);
		assertNull(field.getName());
		assertEquals("vector", ModelUtil.fieldName(field));
	}

	@Test
	public void escapedTypeAsVectorFieldName() throws Exception {
		VectorField field = only(parse("script t create collection c { primary id field id type int vector-field ^type type float_vector dim 3 }"), VectorField.class);
		assertEquals("type", field.getName());
	}

	@Test
	public void softKeywordTypeAsVectorFieldNameStillParses() throws Exception {
		VectorField field = only(parse("script t create collection c { primary id field id type int vector-field type type float_vector dim 3 }"), VectorField.class);
		assertEquals("type", field.getName());
	}

	@Test
	public void unnamedQuantizationBeforeTypeKeyword() throws Exception {
		assertNull(only(parse("script t create quantization type scalar bits 8"), Quantization.class).getName());
	}

	@Test
	public void escapedTypeAsQuantizationName() throws Exception {
		assertEquals("type", only(parse("script t create quantization ^type type scalar bits 8"), Quantization.class).getName());
	}

	@Test
	public void unnamedStorageBeforeTypeKeyword() throws Exception {
		assertNull(only(parse("script t create storage type disk"), StorageConfig.class).getName());
	}

	@Test
	public void escapedTypeAsStorageName() throws Exception {
		assertEquals("type", only(parse("script t create storage ^type type disk"), StorageConfig.class).getName());
	}

	private static <T extends EObject> T only(Script script, Class<T> type) {
		List<T> found = EcoreUtil2.getAllContentsOfType(script, type);
		assertEquals(1, found.size());
		return found.get(0);
	}
}
