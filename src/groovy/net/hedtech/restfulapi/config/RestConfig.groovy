/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************************/
package net.hedtech.restfulapi.config

import org.codehaus.groovy.grails.commons.GrailsApplication

class RestConfig {

    GrailsApplication grailsApplication

    //map of resource name to its configuration
    //A resource with an empty name "" is treated as
    //a default resource block matching any resource
    //not explicitly named
    def resources = [:]

    //map of group name to MarshallerGroupConfig instance
    def marshallerGroups = [:]
    ConfigGroup jsonDomain     = new ConfigGroup()
    ConfigGroup jsonGroovyBean = new ConfigGroup()
    ConfigGroup jsonExtractor  = new ConfigGroup()

    ConfigGroup xmlDomain      = new ConfigGroup()
    ConfigGroup xmlGroovyBean  = new ConfigGroup()
    ConfigGroup xmlExtractor   = new ConfigGroup()

    RestConfig( GrailsApplication grailsApplication ) {
        this.grailsApplication = grailsApplication
    }

    ResourceConfig getResource( String pluralizedName ) {
        def resource = resources[pluralizedName]
        if (resource == null) {
            resource = resources[""]
        }
        resource
    }

    RepresentationConfig getRepresentation(String pluralizedResourceName, String type ) {
        getRepresentation(pluralizedResourceName, [type])
    }

    RepresentationConfig getRepresentation(String pluralizedResourceName, allowedTypes) {
        ResourceConfig resource = getResource( pluralizedResourceName )
        if (!resource) return null
        for (def type : allowedTypes) {
            def rep = resource.getRepresentation( type )
            if (rep != null) return rep
        }
        return null
    }

    void validate() {
        resources.values().each() { it.validate() }
    }

//------------ These methods exist to support the closures used to provide configuration ------------------
//------------ They may throw exceptions to indicate errors when processing configuration -----------------

    static RestConfig parse(GrailsApplication app, def c) {
        RestConfig config = new RestConfig( app )
        c.delegate = config
        c.resolveStrategy = Closure.DELEGATE_ONLY
        c.call()
        return config
    }

    def resource(String name) {
        ResourceConfig rc = new ResourceConfig(restConfig:this,name:name)
        Closure closure = { Closure c->
            c.delegate = rc
            c.resolveStrategy = Closure.DELEGATE_FIRST
            c.call()
            if (rc.name != name) throw new RuntimeException("Name of resource illegally changed")
            resources.put( name, rc )
        }
        [config:closure]
    }

    RestConfig anyResource(Closure c) {
        ResourceConfig rc = new ResourceConfig(restConfig:this,name:"")
        c.delegate = rc
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.call()
        if (rc.name != "") throw new RuntimeException("Name of resource illegally changed")
        resources.put( "", rc )
        this
    }

    RestConfig marshallerGroups(Closure c) {
        MarshallerGroupsDelegate delegate = new MarshallerGroupsDelegate( this )
        c.delegate = delegate
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.call()
        this
    }

    MarshallerGroupConfig getMarshallerGroup( String name ) {
        def group = marshallerGroups[name]
        if (group == null) {
            throw new MissingMarshallerGroupException( name:name )
        }
        group
    }

    def jsonDomainMarshallerTemplates(Closure c) {
        JSONDomainTemplates delegate = new JSONDomainTemplates(this)
        c.delegate = delegate
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.call()
        this
    }

    def jsonGroovyBeanMarshallerTemplates(Closure c) {
        JSONGroovyBeanTemplates delegate = new JSONGroovyBeanTemplates(this)
        c.delegate = delegate
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.call()
        this
    }

    def jsonExtractorTemplates(Closure c) {
        JSONExtractorTemplates delegate = new JSONExtractorTemplates(this)
        c.delegate = delegate
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.call()
        this
    }

    def xmlDomainMarshallerTemplates(Closure c) {
        XMLDomainTemplates delegate = new XMLDomainTemplates(this)
        c.delegate = delegate
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.call()
        this
    }

    def xmlGroovyBeanMarshallerTemplates(Closure c) {
        XMLGroovyBeanTemplates delegate = new XMLGroovyBeanTemplates(this)
        c.delegate = delegate
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.call()
        this
    }

    def xmlExtractorTemplates(Closure c) {
        XMLExtractorTemplates delegate = new XMLExtractorTemplates(this)
        c.delegate = delegate
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.call()
        this
    }

    class JSONDomainTemplates {
        RestConfig parent
        JSONDomainTemplates(RestConfig parent) {
            this.parent = parent
        }

        def template(String name) {
            def closure = { Closure c ->
                JSONDomainMarshallerDelegate delegate = new JSONDomainMarshallerDelegate()
                c.delegate = delegate
                c.resolveStrategy = Closure.DELEGATE_FIRST
                c.call()
                parent.jsonDomain.configs[name] = delegate.config
            }
            [config:closure]
        }
    }

    class XMLDomainTemplates {
        RestConfig parent
        XMLDomainTemplates(RestConfig parent) {
            this.parent = parent
        }

        def template(String name) {
            def closure = { Closure c ->
                XMLDomainMarshallerDelegate delegate = new XMLDomainMarshallerDelegate()
                c.delegate = delegate
                c.resolveStrategy = Closure.DELEGATE_FIRST
                c.call()
                parent.xmlDomain.configs[name] = delegate.config
            }
            [config:closure]
        }
    }

    class JSONGroovyBeanTemplates {
        RestConfig parent
        JSONGroovyBeanTemplates(RestConfig parent) {
            this.parent = parent
        }

        def template(String name) {
            def closure = { Closure c ->
                JSONGroovyBeanMarshallerDelegate delegate = new JSONGroovyBeanMarshallerDelegate()
                c.delegate = delegate
                c.resolveStrategy = Closure.DELEGATE_FIRST
                c.call()
                parent.jsonGroovyBean.configs[name] = delegate.config
            }
            [config:closure]
        }
    }

    class XMLGroovyBeanTemplates {
        RestConfig parent
        XMLGroovyBeanTemplates(RestConfig parent) {
            this.parent = parent
        }

        def template(String name) {
            def closure = { Closure c ->
                XMLGroovyBeanMarshallerDelegate delegate = new XMLGroovyBeanMarshallerDelegate()
                c.delegate = delegate
                c.resolveStrategy = Closure.DELEGATE_FIRST
                c.call()
                parent.xmlGroovyBean.configs[name] = delegate.config
            }
            [config:closure]
        }
    }

    class JSONExtractorTemplates {
        RestConfig parent
        JSONExtractorTemplates(RestConfig parent) {
            this.parent = parent
        }

        def template(String name) {
            def closure = { Closure c ->
                JSONExtractorDelegate delegate = new JSONExtractorDelegate()
                c.delegate = delegate
                c.resolveStrategy = Closure.DELEGATE_FIRST
                c.call()
                parent.jsonExtractor.configs[name] = delegate.config
            }
            [config:closure]
        }
    }

    class XMLExtractorTemplates {
        RestConfig parent
        XMLExtractorTemplates(RestConfig parent) {
            this.parent = parent
        }

        def template(String name) {
            def closure = { Closure c ->
                XMLExtractorDelegate delegate = new XMLExtractorDelegate()
                c.delegate = delegate
                c.resolveStrategy = Closure.DELEGATE_FIRST
                c.call()
                parent.xmlExtractor.configs[name] = delegate.config
            }
            [config:closure]
        }
    }

}
