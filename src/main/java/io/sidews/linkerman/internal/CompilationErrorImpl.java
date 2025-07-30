package io.sidews.linkerman.internal;

import io.sidews.linkerman.CompilationError;
import org.eclipse.xtext.validation.Issue;

public class CompilationErrorImpl implements CompilationError {

    private final Issue issue;

    public CompilationErrorImpl(Issue issue) {
        this.issue = issue;
    }

    @Override
    public String getMessage() {
        return issue.getMessage();
    }

    @Override
    public int getLineStart() {
        return issue.getLineNumber();
    }

    @Override
    public int getLineEnd() {
        return issue.getLineNumberEnd();
    }

    @Override
    public int getColumnStart() {
        return issue.getColumn();
    }

    @Override
    public int getColumnEnd() {
        return issue.getColumnEnd();
    }

    public static CompilationError createFrom(Issue issue) {
        return new CompilationErrorImpl(issue);
    }
}