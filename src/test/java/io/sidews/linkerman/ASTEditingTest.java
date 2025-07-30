package io.sidews.linkerman;

import io.sidews.linkerman.base.Descriptors;
import io.sidews.linkerman.base.LinkStrategies;
import org.eclipse.cdt.linkerscript.linkerScript.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ASTEditingTest {
    private static final String EXPECTED_FINAL_LINKER_SCRIPT_TXT = """
            MEMORY
            {
               RAM   (rwx) : ORIGIN = 0x20000000, LENGTH = 1024K
               FLASH (rx) : ORIGIN = 0x08000000, LENGTH = 256K
            }
            
            SECTIONS
            {
              .text 0xFFE00000 : AT(0xFFE00000)
              {
                __text_start = .
                *(.text)
                *(.text.*)
                *(P)
                etext = .;
                KEEP(*(.text.*isr))
                KEEP(*(.text.Excep_*))
                KEEP(*(.text.*ISR))
                KEEP(*(.text.*interrupt))
                __text_end = .
              } >ROM
            }
            """;

    private static final String CURRENT_LINKER_SCRIPT_TXT = """
            MEMORY
            {
               RAM   (rwx) : ORIGIN = 0x20000000L, LENGTH = 1024K
            }
            
            SECTIONS
            {
              .text 0xFFE00000 : AT(0xFFE00000)
              {
                *(.text)
                *(.text.*)
                *(P)
                etext = .;
              } >ROM
            }
            """;

    private static final String INPUT_SECTIONS_TXT = """
            KEEP(*(.text.*isr))
            KEEP(*(.text.Excep_*))
            KEEP(*(.text.*ISR))
            KEEP(*(.text.*interrupt))
            """;

    private static final String TEXT_START_ASSIGNMENT_TXT = "__text_start = .";

    private static final String TEXT_END_ASSIGNMENT_TXT = "__text_end = .";

    private static final String FLASH_MEMORY_TXT = "FLASH (rx) : ORIGIN = 0x08000000, LENGTH = 256K";

    private Compiler compiler;

    private DynamicModel.Registry modelRegistry;

    private Serializer serializer;

    @Test
    void using_AST_editing_and_compiler_API() {
        // load current linkerscript
        CompilationResult<LinkerScript> linkerScriptResult = compiler.compile(
                CURRENT_LINKER_SCRIPT_TXT, Descriptors.LINKER_SCRIPT);
        assertFalse(linkerScriptResult.hasError());

        DynamicModel<LinkerScript> linkerScript = linkerScriptResult.single();

        // edit the loaded scripts
        editTextOutputSection(linkerScript);
        editMemoryBlock(linkerScript);

        // serialize the edited linker script model into final textual script
        var resultingScript = serializer.serializeToDSL(linkerScript);

        // all of the above code should work as expected
        assertEquals(EXPECTED_FINAL_LINKER_SCRIPT_TXT, resultingScript);
    }

    private void editTextOutputSection(DynamicModel<LinkerScript> linkerScript) {
        // load new input sections
        // The below Descriptors is just a public utility class that declares all model descriptors for convenient
        CompilationResult<StatementInputSection> inputSectionsResult = compiler.compile(
                INPUT_SECTIONS_TXT, Descriptors.STATEMENT_INPUT_SECTION);
        assertFalse(inputSectionsResult.hasError());

        // load new text start assignment
        CompilationResult<StatementAssignment> textStartAssigmentResult = compiler.compile(
                TEXT_START_ASSIGNMENT_TXT, Descriptors.STATEMENT_ASSIGNMENT);
        assertFalse(textStartAssigmentResult.hasError());

        // load new text end assigment
        CompilationResult<StatementAssignment> textEndAssignmentResult = compiler.compile(
                TEXT_END_ASSIGNMENT_TXT, Descriptors.STATEMENT_ASSIGNMENT);
        assertFalse(textStartAssigmentResult.hasError());

        // retrieve the existing .text output section
        DynamicModel<OutputSection> textOutputSection = findOutputSection(".text", linkerScript);

        // link the child models to the text output section model
        textStartAssigmentResult.single()
                .linkToParentAtFirst(textOutputSection, LinkStrategies.DEFAULT_SAFEST);
        inputSectionsResult.stream().forEach(
                sectionModel -> sectionModel.linkToParentAtLast(linkerScript, LinkStrategies.DEFAULT_SAFEST));
        textEndAssignmentResult.single()
                .linkToParentAtLast(textOutputSection, LinkStrategies.DEFAULT_SAFEST);
    }

    private void editMemoryBlock(DynamicModel<LinkerScript> linkerScript) {
        // load new flash memory
        // The below Descriptors is just a public utility class that declares all model descriptors for convenient
        CompilationResult<Memory> flashMemoryResult = compiler.compile(
                FLASH_MEMORY_TXT, Descriptors.MEMORY);
        assertFalse(flashMemoryResult.hasError());

        // retrieve the existing memory parent block
        DynamicModel<MemoryCommand> memoryContainer = findMemoryContainer(linkerScript);

        // link the flash memory to memory container
        flashMemoryResult.single().linkToParentAtLast(memoryContainer, LinkStrategies.DEFAULT_SAFEST);
    }

    /**
     * Search an output section with a given name in a linker script model.
     * Note: This method should be later implemented using a new AST model search API.
     */
    private DynamicModel<OutputSection> findOutputSection(String sectionName, DynamicModel<LinkerScript> rootModel) {
        LinkerScript root = rootModel.getSymbolInstance();
        SectionsCommand sectionsCommand = (SectionsCommand) root.getStatements().getLast();
        OutputSection result = (OutputSection) sectionsCommand.getSectionCommands().getFirst();
        if (!sectionName.equals(result.getName())) {
            throw new RuntimeException("Should not reach here!");
        }
        return modelRegistry.getFactory(OutputSection.class).createWith(result);
    }

    /**
     * Search memory parent block in a linker script.
     * Note: this should be later implemented by an AST model search API
     */
    private DynamicModel<MemoryCommand> findMemoryContainer(DynamicModel<LinkerScript> rootModel) {
        LinkerScript root = rootModel.getSymbolInstance();
        MemoryCommand result = (MemoryCommand) root.getStatements().getFirst();
        return modelRegistry.getFactory(MemoryCommand.class).createWith(result);
    }
}