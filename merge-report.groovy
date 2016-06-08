import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import groovyx.net.http.HTTPBuilder
import org.slf4j.LoggerFactory

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET

@Grapes([
        @Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.1'),

        // fix all logging
        @Grab(group = 'org.slf4j', module = 'slf4j-api', version = '1.7.21'),
        @Grab(group = 'org.slf4j', module = 'jcl-over-slf4j', version = '1.7.21'),
        @Grab(group = 'org.slf4j', module = 'log4j-over-slf4j', version = '1.7.21'),
        @Grab(group = 'org.slf4j', module = 'jul-to-slf4j', version = '1.7.21'),
        @Grab(group = 'org.slf4j', module = 'jul-to-slf4j', version = '1.7.21'),
        @Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.7'),
        @Grab(group = 'ch.qos.logback', module = 'logback-core', version = '1.1.7')
])
@Slf4j
class GetMergeRequestService {
    String base = "https://www.somewebsite.net"
    def token = [private_token: "some_private_token"]
    def http = new HTTPBuilder(base)

    GetMergeRequestService() {
        http.ignoreSSLIssues()
    }

    def getProjects(projectId) {
        http.request(GET, JSON) {
            if (projectId != null) {
                uri.path = "/api/v3/projects/${projectId}"
            } else {
                uri.path = "/api/v3/projects"
            }

            uri.query = token

            response.success = { resp, json ->
                println "My response handler got response: ${resp.statusLine}"
                println "Response length: ${resp.headers.'Content-Length'}"
                println JsonOutput.prettyPrint(JsonOutput.toJson(json))
            }

            // called only for a 404 (not found) status code:
            response.'404' = { resp ->
                println 'Not found'
            }
        }
    }

    def getMergeRequests(projectId, mergeRequestId) {
        if (projectId == null) {
            log.error "Must provide a projectId"
        }
        if (mergeRequestId != null) {
            uri.path = "/projects/${projectId}/merge_requests"
        } else {
            uri.path = "/projects/${projectId}/merge_requests/${mergeRequestId}"
        }

        uri.query = token

        http.request(GET, JSON) {
            response.success = { resp, json ->
                log.info "asd" + uri.path
                assert resp.status == 200

                println "My response handler got response: ${resp.statusLine}"
                println "Response length: ${resp.headers.'Content-Length'}"
                println JsonOutput.prettyPrint(JsonOutput.toJson(json))
            }

            // called only for a 404 (not found) status code:
            response.'404' = { resp ->
                println 'Not found'
            }
        }
    }

    static main(String[] args) {
        setupLoggers()

        // this will NOT print/write as the loglevel is info
        log.info 'Execute GetMergeRequestService.'

        def service = new GetMergeRequestService()
        //service.initializeHttpBuilder(2);
        try {
            service.getProjects(2)


        } catch (Exception e) {
            log.error("Uncaught Exception: ", e)
        }
    }

    static setupLoggers() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.ALL)
        LoggerContext loggerContext = rootLogger.getLoggerContext();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern('%d{yyyy-MM-dd HH:mm:ss,SSS Z} [%t] %-5p %c{1}:%L %x - %m%n');
        encoder.start();

        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(loggerContext);
        appender.setEncoder(encoder);
        appender.start();
    }

}