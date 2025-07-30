package io.sidews.linkerman;

import io.sidews.linkerman.base.Descriptors;
import org.eclipse.cdt.linkerscript.linkerScript.LNumberLiteral;
import org.eclipse.cdt.linkerscript.linkerScript.LinkerScript;
import org.eclipse.cdt.linkerscript.linkerScript.OutputSection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateWithEmbeddingsTest {

    private static final String GCCRX_TEMPLATE_TXT = """
            /*
            * LinkerScript template with embeddings
            */
            
            MEMORY
            {
                /* Ram with embedding values */
               RAM   (rwx) : ORIGIN = ${RAM_ORIGIN_EMBEDDING}, LENGTH = ${RAM_LENGTH_EMBEDDING}
               FLASH (rx) : ORIGIN = 0x08000000, LENGTH = 256K
            }
            
            SECTIONS
            {
              .text : { *(.text*) *(.rodata*) } > FLASH
            
              /* BSP configuration will be placed in the below embedding */
              ${BSP_CONFIG_SECTION_EMBEDDING}
            }
            """;

    private static final Long RX_RAM_ORIGIN_VALUE = 0x20000000L;

    private static final Long RX_RAM_LENGTH_VALUE = 64_000L;

    private static final String BSP_CONFIGURATION_SECTION_TXT = """
            .r_bsp_NULL :
            {
                . += 0x100;
                "_r_bsp_NULL_end" = .;
            } >RAM AT>RAM
            
            .r_bsp_istack ALIGN(0x4) (NOLOAD) :
            {
                KEEP(*(.r_bsp_istack))
            } >RAM AT>RAM
            """;

    private static final String EXPECTED_LINKER_SCRIPT_TEXT = """
            /*
            * LinkerScript template with embeddings
            */
            
            MEMORY
            {
                /* Ram with embedding values */
               RAM   (rwx) : ORIGIN = 0x20000000, LENGTH = 64k
               FLASH (rx) : ORIGIN = 0x08000000, LENGTH = 256K
            }
            
            SECTIONS
            {
              .text : { *(.text*) *(.rodata*) } > FLASH
            
              /* BSP configuration will be placed in the below embedding */
              .r_bsp_NULL :
               {
                  . += 0x100;
                   "_r_bsp_NULL_end" = .;
               } >RAM AT>RAM
               
               .r_bsp_istack ALIGN(0x4) (NOLOAD) :
               {
                   KEEP(*(.r_bsp_istack))
               } >RAM AT>RAM
            """;

    private Compiler compiler;

    private Serializer serializer;

    private DynamicModel.Registry registry;

    private EmbeddingDefinition<LNumberLiteral> RAM_ORIGIN_EMBEDDING_DEF;

    private EmbeddingDefinition<LNumberLiteral> RAM_LENGTH_EMBEDDING_DEF;

    private EmbeddingDefinition<OutputSection> BSP_CONFIG_SECTION_EMBEDDING_DEF;


    @Test
    void generate_LinkerScript_For_RX_With_BSP() {
        // Prepare compilation for GCCRX LinkerScript with embeddings that will be set later
        Compilation<LinkerScript> templateCompilation =
                compiler.prepareCompilation(GCCRX_TEMPLATE_TXT, Descriptors.LINKER_SCRIPT,
                        RAM_ORIGIN_EMBEDDING_DEF, RAM_LENGTH_EMBEDDING_DEF, BSP_CONFIG_SECTION_EMBEDDING_DEF);

        // Compile for BSP Sections snippet into dynamic model
        // The below Descriptors is just a public utility class that declares all model descriptors for convenient
        CompilationResult<OutputSection> bspSectionResult = compiler.compile(
                BSP_CONFIGURATION_SECTION_TXT, Descriptors.OUTPUT_SECTION);
        assertFalse(bspSectionResult.hasError());

        // Compile ram origin snippet into dynamic model
        CompilationResult<LNumberLiteral> ramOriginResult = compiler.compile(
                String.valueOf(RX_RAM_ORIGIN_VALUE), Descriptors.LNUMBER_LITERAL);
        assertFalse(ramOriginResult.hasError());

        // Compile ram length snippet into dynamic model
        CompilationResult<LNumberLiteral> ramLengthResult = compiler.compile(
                String.valueOf(RX_RAM_LENGTH_VALUE), Descriptors.LNUMBER_LITERAL);
        assertFalse(ramOriginResult.hasError());

        // Set embeddings for the template compilation
        templateCompilation.setEmbedding(
                RAM_ORIGIN_EMBEDDING_DEF.createEmbedding(ramOriginResult.single()));

        templateCompilation.setEmbedding(
                RAM_LENGTH_EMBEDDING_DEF.createEmbedding(ramLengthResult.single()));

        templateCompilation.setEmbedding(
                BSP_CONFIG_SECTION_EMBEDDING_DEF.createEmbedding(bspSectionResult.stream().toList()));

        // Execute the compilation
        CompilationResult<LinkerScript> finalScriptResult = templateCompilation.compile();
        assertFalse(finalScriptResult.hasError());

        // Serialize to LinkerScript text
        String linkerScriptText = serializer.serializeToDSL(finalScriptResult.single());

        // The final script should be as expected
        assertEquals(EXPECTED_LINKER_SCRIPT_TEXT, linkerScriptText);
    }
}