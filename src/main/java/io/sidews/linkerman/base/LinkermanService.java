package io.sidews.linkerman.base;

import io.sidews.linkerman.DSLContext;

import java.util.HashMap;

public class LinkermanService {

    private final static HashMap<DSLContext, GrammarAnalyzer> grammarAnalyzers = new HashMap<>();

    private final static HashMap<DSLContext, SymbolManager> symbolManagers = new HashMap<>();

    public static GrammarAnalyzer getGrammarAnalyzer(DSLContext context) {
        return grammarAnalyzers.computeIfAbsent(context, LinkermanService::createGrammarAnalyzer);
    }

    public static SymbolManager getSymbolManager(DSLContext context) {
        return symbolManagers.computeIfAbsent(context, LinkermanService::createSymbolManager);
    }

    private static GrammarAnalyzer createGrammarAnalyzer(DSLContext context) {
        return new BasicGrammarAnalyzer(context);
    }

    private static SymbolManager createSymbolManager(DSLContext context) {
        return new SymbolManager(getGrammarAnalyzer(context), context);
    }

}
