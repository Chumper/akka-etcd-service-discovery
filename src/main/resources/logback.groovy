appender("Console-Appender", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d [%thread] %-5level %logger{20}: %msg %n"
    }
}

root(DEBUG, ["Console-Appender"])