package ca.phon.query.pql;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;

@RunWith(JUnit4.class)
public class TestPQL {

	public void testExpr(String expr) throws ParseException {
		PQLQuery query = PQLQuery.compile(expr);
		Assert.assertEquals(expr, query.toString());
	}

	@Test
	public void testSelectStatement() throws ParseException {
		final String expr = """
				SELECT IPATarget FROM "Anne.*"#1, 3, 5..10#
				WHERE WORD {
					EQUALS STRESS PATTERN "AAA"
				}
				""";
		testExpr(expr);
	}

}
