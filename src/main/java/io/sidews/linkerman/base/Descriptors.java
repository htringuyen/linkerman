package io.sidews.linkerman.base;

import io.sidews.linkerman.ModelDescriptor;
import org.eclipse.cdt.linkerscript.linkerScript.*;

public class Descriptors {

    public static ModelDescriptor<LinkerScript> LINKER_SCRIPT;

    public static ModelDescriptor<OutputSection> OUTPUT_SECTION;

    public static ModelDescriptor<LNumberLiteral> LNUMBER_LITERAL;

    public static ModelDescriptor<StatementInputSection> STATEMENT_INPUT_SECTION;

    public static ModelDescriptor<StatementAssignment> STATEMENT_ASSIGNMENT;

    public static ModelDescriptor<Memory> MEMORY;
}
