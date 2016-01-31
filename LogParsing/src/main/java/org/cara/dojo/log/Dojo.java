package org.cara.dojo.log;

/**
 * @author ctranxuan
 */
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author ctranxuan (streamdata.io).
 */
public class Dojo {

    private static final String PIPE_DELIMITER = "|";
    private static final String DEBUG_MARKER = "[debug]";
    
    static class LogStatement {
        private String firstLine;
        private Optional<String> lastLine;
        
        public LogStatement(final String aFirstLine) {
            firstLine = aFirstLine;
            lastLine = Optional.empty();
        }
        
        public void lastLine(String aLine) {
            if (Objects.nonNull(aLine)) {
                lastLine = Optional.of(aLine);
                
            }
        }
    }
    
    static class NewLogStatement {
        private final String logInfo;
        private final String response;
        
        public NewLogStatement(final String aLogInfo, final String aResponse) {
            logInfo = aLogInfo;
            response = aResponse;
        }
    }
    
    public static void main(String[] args) {
        /*
         * This assumes:
         *  - that [debug] recordXXX is the pattern that must capture
         *  - that the response is the last string after '|' of the last line that precedes a line starting with "[debug]"
         */
        Stream<String> lines;
        lines = Stream.of("[debug] record1 | other info | <response>hey</response>",
                "[debug] record2",
                "middle of record2",
                "end of record2 | response2: error msg from response2",
                "[debug] record3 | response3",
                "[debug] record4 blabla | some info | other info | response4 blablba");
        
        // Collect the log lines
        lines.collect(ArrayDeque<LogStatement>::new,
                (acc, line) -> {
                    if (line.startsWith(DEBUG_MARKER)) {
                        acc.add(new LogStatement(line));
                        
                    } else {
                        acc.getLast().lastLine(line);
                        
                    }
                }, ArrayDeque::addAll)
                .stream()
                // Transform into a new log structure
                .map(stmt -> {
                    String logInfo;
                    logInfo = toLogInfo(stmt.firstLine);
                    
                    String response;
                    response = toResponse(stmt.lastLine.orElse(stmt.firstLine));
                    
                    return new NewLogStatement(logInfo, response);
                })
                // Simulate the write into a file
                .forEach((item) -> System.out.format("logInfo=%s, response=%s\n", item.logInfo, item.response));
        
        /*
         * The potential issue is that the ArrayDeque will grow with the number of lines,
         * which can lead to memory issues
         */
    }
    
    private static String toLogInfo(String aLine) {
        Objects.requireNonNull(aLine, "aLine cannot be null");
        String result = aLine;

        int endIndex = aLine.indexOf(PIPE_DELIMITER);
        if (endIndex > 0) {
            result = aLine.substring(0, endIndex);
        }

        return result;
    }

    private static String toResponse(String aLine) {
        Objects.requireNonNull(aLine, "aLine cannot be null");
        return aLine.substring(aLine.lastIndexOf(PIPE_DELIMITER) + 1);
    }
}