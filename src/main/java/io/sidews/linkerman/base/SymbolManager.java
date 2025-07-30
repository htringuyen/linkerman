package io.sidews.linkerman.base;

import io.sidews.linkerman.DSLContext;
import io.sidews.linkerman.DSLSymbol;
import io.sidews.linkerman.internal.DSLSymbolImpl;
import io.sidews.linkerman.util.EMFUtil;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import java.util.HashMap;

public final class SymbolManager {

    private final GrammarAnalyzer analyzer;

    private final DSLContext context;

    private final HashMap<EClass, DSLSymbol> symbols = new HashMap<>();

    public SymbolManager(GrammarAnalyzer grammarAnalyzer, DSLContext context) {
        this.analyzer = grammarAnalyzer;
        this.context = context;
    }

    public DSLSymbol getSymbol(EClass type) {
        return symbols.computeIfAbsent(type, this::createSymbolFor);
    }

    public DSLSymbol createSymbolFor(EClass symbolType) {
        return new DSLSymbolImpl(analyzer.isASTRoot(symbolType),
                symbolType,
                analyzer.getSymbolReturnTypes(symbolType),
                analyzer.getAllSymbolContainers(symbolType));
    }

    public <T extends EObject> DSLSymbol getSymbol(Class<T> type) {
        var eType = EMFUtil.findEClassOf(type, context.getEPackage());
        return getSymbol(eType);
    }

}
