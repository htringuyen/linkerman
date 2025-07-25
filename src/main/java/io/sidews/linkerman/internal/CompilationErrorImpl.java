package io.sidews.linkerman.internal;

import io.sidews.linkerman.CompilationError;
import org.eclipse.xtext.diagnostics.Diagnostic;

public class CompilationErrorImpl implements CompilationError {

    private final Diagnostic diagnostic;

    public CompilationErrorImpl(Diagnostic diagnostic) {
        this.diagnostic = diagnostic;
    }

    @Override
    public String getMessage() {
        return diagnostic.getMessage();
    }

    @Override
    public int getLineStart() {
        return diagnostic.getLine();
    }

    @Override
    public int getLineEnd() {
        return diagnostic.getLineEnd();
    }

    @Override
    public int getColumnStart() {
        return diagnostic.getColumn();
    }

    @Override
    public int getColumnEnd() {
        return diagnostic.getColumnEnd();
    }
}






















