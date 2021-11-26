package ca.phon.query.pql;

import ca.phon.project.Project;
import ca.phon.query.db.ResultSet;
import ca.phon.session.SessionPath;
import org.antlr.v4.runtime.*;

import java.text.ParseException;
import java.util.*;

/**
 * Represents a compiled PQL query
 */
public class PQLQuery {

	private final String pql;

	/*
	 * Parsed query context
	 */
	private PQL.StartContext pqlContext;

	public static PQLQuery compile(String pql) throws ParseException {
		CharStream charStream = CharStreams.fromString(pql);
		PQLTokens tokens = new PQLTokens(charStream);
		CommonTokenStream tokenStream = new CommonTokenStream(tokens);

		PQL parser = new PQL(tokenStream);
		PQL.StartContext ctx = parser.start();

		PQLSessionCollector sessionCollector = new PQLSessionCollector();
		ctx.accept(sessionCollector);

		return new PQLQuery(pql, ctx);
	}

	private PQLQuery(String pql, PQL.StartContext pqlContext) {
		super();

		this.pql = pql;
		this.pqlContext = pqlContext;
	}

	public Map<String, ResultSet> execute(Project project) {
		PQLContext ctx = new PQLContext();
		return execute(ctx);
	}

	public Map<String, ResultSet> execute(PQLContext context) {
		Map<String, ResultSet> retVal = new HashMap<>();

		// TODO

		return retVal;
	}

	@Override
	public String toString() {
		return this.pql;
	}

}
