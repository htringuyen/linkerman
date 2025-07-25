package io.sidews.linkerman;

public interface CompilationError {

    int getLineStart();

    int getLineEnd();

    int getColumnStart();

    int getColumnEnd();

    String getMessage();

    @Override
    String toString();
}
