package io.sidews.linkerman.misc;

import io.sidews.linkerman.DSLContext;
import io.sidews.linkerman.LinkerScriptContext;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.serializer.ISerializer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PartialDSLSerdesTest {

    private static final Logger logger = LoggerFactory.getLogger(PartialDSLSerdesTest.class);

    private static final DSLContext context  = new LinkerScriptContext();

    private static final IParser parser = context.getInjector().getInstance(IParser.class);

    private static final ISerializer serializer = context.getXtextSerializer();

    @Test
    void testPartialParsing() {
        var content = """
                .
                """;
        var parserRule = findParserRuleByName("LExpression");
        var result = parser.parse(parserRule, wrapToReader(content));
        assertFalse(result.hasSyntaxErrors());
        assertNotNull(result.getRootNode());
        assertNotNull(result.getRootASTElement());
        logger.info(result.getRootASTElement().toString());
    }

    @Test
    void testPartialSerializing() {
        var content = """
                .text : { ${input_section1} *(.rodata*) } > FLASH
                """;
        var parserRule = findParserRuleByName("OutputSection");
        var parseResult = parser.parse(parserRule, wrapToReader(content));
        for (var errorNode : parseResult.getSyntaxErrors()) {
            logger.info("Error: [{}:{}] {}",
                    errorNode.getOffset(),
                    errorNode.getOffset() + errorNode.getLength(),
                    errorNode.getText());
        }
        //assertFalse(parseResult.hasSyntaxErrors());
        var model = parseResult.getRootASTElement();
        var resultText = serializer.serialize(model);
        logger.info(resultText);
    }

    private static ParserRule findParserRuleByName(String name) {
        return GrammarUtil.allParserRules(context.getGrammar())
                .stream()
                .filter(r -> r.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private static Reader wrapToReader(String content) {
        var inStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return new InputStreamReader(inStream);
    }
}



























