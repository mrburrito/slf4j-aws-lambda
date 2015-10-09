package com.shankyank.slf4j.lambda

import static com.shankyank.slf4j.lambda.LambdaLevel.*

import spock.lang.Specification

class LambdaLevelSpec extends Specification {
    def 'log levels correctly indicate whether they enable another level'() {
        expect:
        setLevel.isLoggingEnabled(targetLevel) == enabled

        where:
        setLevel | targetLevel || enabled
        TRACE    | TRACE       || true
        TRACE    | DEBUG       || true
        TRACE    | INFO        || true
        TRACE    | WARN        || true
        TRACE    | ERROR       || true
        TRACE    | null        || false
        DEBUG    | TRACE       || false
        DEBUG    | DEBUG       || true
        DEBUG    | INFO        || true
        DEBUG    | WARN        || true
        DEBUG    | ERROR       || true
        DEBUG    | null        || false
        INFO     | TRACE       || false
        INFO     | DEBUG       || false
        INFO     | INFO        || true
        INFO     | WARN        || true
        INFO     | ERROR       || true
        INFO     | null        || false
        WARN     | TRACE       || false
        WARN     | DEBUG       || false
        WARN     | INFO        || false
        WARN     | WARN        || true
        WARN     | ERROR       || true
        WARN     | null        || false
        ERROR    | TRACE       || false
        ERROR    | DEBUG       || false
        ERROR    | INFO        || false
        ERROR    | WARN        || false
        ERROR    | ERROR       || true
        ERROR    | null        || false
    }
}
