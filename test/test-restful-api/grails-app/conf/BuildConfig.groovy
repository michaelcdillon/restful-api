/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************************/

grails.servlet.version          = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir        = "target/classes"
grails.project.test.class.dir   = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level     = 1.6
grails.project.source.level     = 1.6

grails.plugin.location.'restful-api' = "../.."

grails.project.dependency.resolution = {

    inherits("global") { }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()
    }

    dependencies {
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"

        // Dependency for CORS testing. see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6996110
        test "commons-httpclient:commons-httpclient:3.1"
    }

    plugins {
        compile(":inflector:0.2",
                ":cache-headers:1.1.5",
                ":functional-spock:0.6",
                ':cache:1.0.0')

        runtime(":hibernate:$grailsVersion",
                ":jquery:1.7.2",
                ":resources:1.1.6",
                ":database-migration:1.1",
                ":cors:1.1.0")

        test(":spock:0.7") {
          exclude "spock-grails-support"
        }

        build   ":tomcat:$grailsVersion"
    }
}

//functional-spock as a compile dependencies drags
//spock dependencies into the war, which aren't needed
//and can cause deployment issues as it pulls
//in a version of spock-grails-support that isn't compatible
//with the groovy version used in grails >= 2.2
//
grails.war.resources = { stagingDir, args ->
    List unwanted = [ "spock" ]

    new File(stagingDir, "WEB-INF/lib").eachFile { File file ->
        String name = file.name.toLowerCase()
        if (file.name.endsWith(".jar")) {
            if (unwanted.find { name.startsWith it }) {
                delete(file: file)
            }
        }
    }

}
