/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************************/

import grails.converters.JSON

import net.hedtech.restfulapi.*
import net.hedtech.restfulapi.marshallers.json.*

import org.codehaus.groovy.grails.web.converters.configuration.ConvertersConfigurationHolder as CCH
import org.codehaus.groovy.grails.web.converters.configuration.DefaultConverterConfiguration as DCC

class RestfulApiGrailsPlugin {

    def version = "0.6.0"
    def grailsVersion = "2.2.1 > *"
    def dependsOn = [:]
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "RESTful API Plugin"
    def author = "Ellucian"
    def authorEmail = ""
    def description = '''\
        |The resful-api plugin facilitates exposing a non-trivial,
        |versioned RESTful API. The plugin provides a DSL that may
        |be used to declaratively specify how resources should be
        |marshalled. Please see the README.md for details.
        |'''.stripMargin()

    def documentation = "README.md" // FYI: We use color-marked to generate HTML

    def organization = [ name: "Ellucian", url: "http://www.ellucian.com/" ]


// ----------------------------------------------------------------------------


    def doWithWebDescriptor = { xml -> }

    def doWithSpring = { }

    def doWithDynamicMethods = { ctx -> }

    def doWithApplicationContext = { applicationContext ->
        // ------------------------ Common marshallers -----------------------
        // Initialize the Restful API controller (so it will register JSON and XML marshallers)
        //
        def artefact = application.getArtefactByLogicalPropertyName("Controller", "restfulApi")
        def restfulApiController = applicationContext.getBean(artefact.clazz.name)
        restfulApiController.init()
    }

    def onChange = { event -> }

    def onConfigChange = { event -> }

    def onShutdown = { event -> }
}

