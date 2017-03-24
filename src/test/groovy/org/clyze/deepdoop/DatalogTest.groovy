package org.clyze.deepdoop

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.clyze.deepdoop.actions.*
import org.clyze.deepdoop.datalog.*
import org.clyze.deepdoop.datalog.DatalogParser
import org.clyze.deepdoop.system.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DatalogTest {

	DatalogListenerImpl _listener
	ParseTreeWalker     _walker

	DatalogParser open(String filename) throws IOException {
		_listener = new DatalogListenerImpl(filename)
		return new DatalogParser(
				new CommonTokenStream(
					new DatalogLexer(
						new ANTLRInputStream(
							getClass().getResourceAsStream(filename)))))
	}

	void test(String filename) throws IOException {
		test(filename, null)
	}

	void test(String filename, ErrorId expectedErrorId) throws IOException {
		filename = "/deepdoop/" + filename
		try {
			ParseTree tree = open(filename).program()
			_walker.walk(_listener, tree)

			def p = _listener.getProgram()

			PostOrderVisitor<IVisitable> v = new PostOrderVisitor<>(new FlatteningActor(p.comps))
			def flatP = p.accept(v) as Program

			flatP.accept(new LBCodeGenVisitingActor("build/"))
		}
		catch (DeepDoopException e) {
			if (expectedErrorId == null || e.errorId != expectedErrorId)
				Assert.fail(e.errorId + e.getMessage() + " on " + filename)
			System.err.println("Expected failure on " + filename)
			return
		}
		catch (Exception e) {
			Assert.fail(e.getMessage() + " on " + filename)
		}
		if (expectedErrorId != null)
			Assert.fail("Test on " + filename + " did not fail (as expected)")
	}

	// This method is run before each method annotated with @Test
	@Before
	void setup() throws IOException {
		_walker   = new ParseTreeWalker()
	}

	@Test
	void testT1() throws IOException {
		test("t1.logic")
	}
	@Test
	void testT2() throws IOException {
		test("t2.logic")
	}
	@Test
	void testT3() throws IOException {
		test("t3.logic")
	}
	@Test
	void testT4() throws IOException {
		test("t4.logic")
	}
	@Test
	void testT5() throws IOException {
		test("t5.logic")
	}
	@Test
	void testT6() throws IOException {
		test("t6.logic")
	}
	@Test
	void testT7() throws IOException {
		test("t7.logic")
	}
	@Test
	void testT8() throws IOException {
		test("t8.logic")
	}
	@Test
	void testT9() throws IOException {
		test("t9.logic")
	}
	@Test
	void testT10() throws IOException {
		test("t10.logic")
	}
	@Test
	void testT11() throws IOException {
		test("t11.logic")
	}
	@Test
	void testT12() throws IOException {
		test("t12.logic")
	}
	@Test
	void testSample() throws IOException {
		test("sample.logic")
	}

	@Test
	void test_Fail1() throws IOException {
		test("fail1.logic", ErrorId.DEP_CYCLE)
	}
	@Test
	void test_Fail2() throws IOException {
		test("fail2.logic", ErrorId.DEP_GLOBAL)
	}
	@Test
	void test_Fail5() throws IOException {
		test("fail5.logic", ErrorId.CMD_RULE)
	}
	@Test
	void test_Fail6() throws IOException {
		test("fail6.logic", ErrorId.CMD_CONSTRAINT)
	}
	@Test
	void test_Fail7() throws IOException {
		test("fail7.logic", ErrorId.CMD_DIRECTIVE)
	}
	@Test
	void test_Fail8() throws IOException {
		test("fail8.logic", ErrorId.CMD_NO_DECL)
	}
	@Test
	void test_Fail9() throws IOException {
		test("fail9.logic", ErrorId.CMD_NO_IMPORT)
	}
	@Test
	void test_Fail10() throws IOException {
		test("fail10.logic", ErrorId.CMD_EVAL)
	}
	@Test
	void test_Fail11() throws IOException {
		test("fail11.logic", ErrorId.ID_IN_USE)
	}
	@Test
	void test_Fail12() throws IOException {
		test("fail12.logic", ErrorId.UNKNOWN_VAR)
	}
	@Test
	void test_Fail13() throws IOException {
		test("fail13.logic", ErrorId.UNKNOWN_COMP)
	}
	@Test
	void test_Fail14() throws IOException {
		test("fail14.logic", ErrorId.UNKNOWN_COMP)
	}
	//@Test
	//void test_Fail15() throws IOException {
	//	test("fail15.logic", ErrorId.MULTIPLE_ENT_DECLS)
	//}


	@Test
	void testA01() throws IOException {
		test("analysis/cfg-tests.logic")
	}
	@Test
	void testA02() throws IOException {
		test("analysis/context-insensitive-declarations.logic")
	}
	@Test
	void testA03() throws IOException {
		test("analysis/context-insensitive.logic")
	}
	@Test
	void testA04() throws IOException {
		test("analysis/facts-declarations.logic")
	}
	@Test
	void testA05() throws IOException {
		test("analysis/flow-insensitivity-declarations.logic")
	}
	@Test
	void testA06() throws IOException {
		test("analysis/statistics-simple.logic")
	}
	@Test
	void testA07() throws IOException {
		test("analysis/tamiflex-declarations.logic")
	}
	@Test
	void testA08() throws IOException {
		test("analysis/tamiflex-fact-declarations.logic")
	}
	@Test
	void testA09() throws IOException {
		test("analysis/tamiflex.logic")
	}
}
