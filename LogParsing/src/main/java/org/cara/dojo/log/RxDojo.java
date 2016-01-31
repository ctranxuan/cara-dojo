package org.cara.dojo.log;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

import java.util.Objects;
import java.util.Optional;


/**
 * @author ctranxuan
 */
public class RxDojo {
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

        public NewLogStatement(LogStatement aLogStatement) {
            this(toLogInfo(aLogStatement.firstLine), toResponse(aLogStatement.lastLine.orElse(aLogStatement.firstLine)));
        }

        private static String toLogInfo(String aLine) {
            Objects.requireNonNull(aLine, "aLine cannot be null!");
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

    static class LogSubscriber extends Subscriber<String> {
        private final PublishSubject<LogStatement> subject;
        private LogStatement logStatement;

        LogSubscriber(final PublishSubject<LogStatement> aSubject) {
            subject = aSubject;
        }


        @Override
        public void onCompleted() {
            subject.onNext(logStatement);
        }

        @Override
        public void onError(final Throwable aThrowable) {

        }

        @Override
        public void onNext(final String aLine) {
            if (aLine.startsWith(DEBUG_MARKER)) {
                if( logStatement != null ) {
                    subject.onNext(logStatement);

                }
                logStatement = new LogStatement(aLine);

            } else {
                logStatement.lastLine(aLine);

            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        PublishSubject<LogStatement> subject;
        subject = PublishSubject.create();
        subject.map(stmt -> new NewLogStatement(stmt))
               .subscribe(item -> {
                    System.out.format("logInfo=%s, response=%s\n", item.logInfo, item.response);
            });

        Observable<String> lines;
        lines = Observable.just("[debug] record1 | other info | <response>hey</response>",
                "[debug] record2",
                "middle of record2",
                "end of record2 | response2: error msg from response2",
                "[debug] record3 | response3",
                "[debug] record4 blabla | some info | other info | response4 blablba");
        lines.subscribe(new LogSubscriber(subject));

        Thread.sleep(5000);
    }

}
