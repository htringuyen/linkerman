package io.sidews.linkerman.query;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XQueryParserTest {

    private static final Logger logger = LoggerFactory.getLogger(XQueryParserTest.class);

    @Test
    void testParserPattern_Sample1() {
        var query = "/LinkerScript//OutputSection//Assignment[position()=1]";
        var pattern = doTestParserPattern(query);
        assertEquals(query, pattern.getQuery());
    }

    @Test
    void testParserPattern_Sample2() {
        var query = "/LinkerScript//OutputSection//assignment::Assignment[position()=1]";
        var pattern = doTestParserPattern(query);
        assertEquals(query, pattern.getQuery());
    }

    private XQueryPattern doTestParserPattern(String query) {
        var pattern = XQueryParser.parse(query);
        logger.info(pattern.toString());
        return pattern;
    }
}
