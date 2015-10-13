package com.shankyank.slf4j.lambda

import spock.lang.Unroll

import java.time.OffsetDateTime

import static com.shankyank.slf4j.lambda.LambdaLevel.*

import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by gshankman on 10/13/15.
 */
class LambdaLoggerFactorySpec extends Specification {
    @Shared
    LambdaLoggerFactory factory = new LambdaLoggerFactory()

    def 'loggers default to OFF when config file does not exist'() {
        given:
        LambdaLoggerFactory emptyFactory = new LambdaLoggerFactory('notfound.properties')

        when: 'Any logger is generated'
        LambdaLoggerAdapter root = emptyFactory.getLogger(null)
        LambdaLoggerAdapter logger1 = emptyFactory.getLogger('com.shankyank')
        LambdaLoggerAdapter myLogger = emptyFactory.getLogger(LambdaLoggerFactorySpec.class.name)

        then: 'Log levels are off'
        !root.@outLevel
        !logger1.@outLevel
        !myLogger.@outLevel
    }

    @Unroll
    def 'loggers are generated appropriately when config file is found'() {
        expect:
        factory.getLogger(name).@outLevel == level

        where:
        name                                             || level
        null                                             || INFO
        ''                                               || INFO
        'com'                                            || INFO
        'com.shankyank'                                  || DEBUG
        'com.shankyank.slf4j.lambda.LambdaLevel'         || null
        'com.shankyank.slf4j.lambda.LambdaLoggerFactory' || DEBUG
        'java'                                           || null
        'java.lang'                                      || ERROR
        'java.util'                                      || null
        'org'                                            || INFO
        'org.slf4j'                                      || TRACE
        'org.slf4j.core'                                 || TRACE
        'org.springframework'                            || WARN
        'org.springframework.web'                        || WARN
    }
}
