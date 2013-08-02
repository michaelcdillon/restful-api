/* ***************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************************/
package net.hedtech.restfulapi.marshallers.json

import grails.converters.JSON
import grails.test.mixin.*
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.support.*
import grails.test.mixin.web.*

import java.beans.PropertyDescriptor
import java.lang.reflect.Field

import net.hedtech.restfulapi.*

import net.hedtech.restfulapi.beans.*

import org.apache.commons.lang.UnhandledException

import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.support.MockApplicationContext
import org.codehaus.groovy.grails.web.converters.configuration.ConvertersConfigurationInitializer
import org.codehaus.groovy.grails.web.json.JSONObject

import org.springframework.beans.BeanWrapper
import org.springframework.web.context.WebApplicationContext

import org.junit.Rule
import org.junit.rules.TestName

import spock.lang.*


@TestMixin([GrailsUnitTestMixin, ControllerUnitTestMixin])
class GroovyBeanMarshallerSpec extends Specification {

    @Rule TestName testName = new TestName()

    void setup() {
    }

    def "Test field access"() {
        setup:
        def marshaller = new GroovyBeanMarshaller(
            app:grailsApplication
        )
        register(marshaller)
        SimpleBean bean = new SimpleBean(property:'foo', publicField:'bar')


        when:
        def content = render( bean )
        def json = JSON.parse content

        then:
        'foo' == json.property
        'bar' == json.publicField
        !json.containsKey('protectedField')
        !json.containsKey('privateField')
        !json.containsKey('transientField')
        !json.containsKey('staticField')

    }

    def "Test excluding fields"() {
        setup:
        def marshaller = new TestMarshaller(
            app:grailsApplication
        )
        marshaller.excludesClosure = {Object value-> ['property','publicField'] }
        register( marshaller )
        SimpleBean bean = new SimpleBean(property:'foo', publicField:'bar')

        when:
        def content = render( bean )
        def json = JSON.parse content

        then:
        !json.containsKey('property')
        !json.containsKey('publicField')
    }

    def "Test default exclusions"() {
        setup:
        def marshaller = new GroovyBeanMarshaller(
            app:grailsApplication
        )
        register( marshaller )
        SimpleBean bean = new SimpleBean(property:'foo', publicField:'bar')

        when:
        def content = render( bean )
        def json = JSON.parse content

        then:
        !json.containsKey('password')
        !json.containsKey('class')
        !json.containsKey('metaClass')

    }

    def "Test including fields"() {
        setup:
        def marshaller = new TestMarshaller(
            app:grailsApplication
        )
        marshaller.includesClosure = {Object value-> [ 'property', 'publicField'] }
        register( marshaller )
        SimpleBean bean = new SimpleBean(property:'foo', publicField:'bar')

        when:
        def content = render( bean )
        def json = JSON.parse content

        then:
        'foo' == json.property
        'bar' == json.publicField
        !json.containsKey('property2')
        !json.containsKey('publicField2')
    }

    def "Test require included fields"() {
        setup:
        def marshaller = new TestMarshaller(
            app:grailsApplication
        )
        marshaller.includesClosure = {Object value-> [ 'property', 'publicField', 'missingField1', 'missingField2'] }
        marshaller.requireIncludedFieldsClosure = {Object o -> true}
        register( marshaller )
        SimpleBean bean = new SimpleBean(property:'foo', publicField:'bar')

        when:
        render( bean )

        then:
        UnhandledException e = thrown()
        ['missingField1', 'missingField2'] == e.getCause().missingNames
    }

    def "Test that included fields overrides excluded fields"() {
        setup:
        def marshaller = new TestMarshaller(
            app:grailsApplication
        )
        def exclusionCalled
        marshaller.includesClosure = {Object value-> [ 'property', 'property2', 'publicField','publicField2'] }
        marshaller.excludesClosure = {Object value->
            exclusionCalled = true
            [ 'property', 'publicField']
        }
        register( marshaller )
        SimpleBean bean = new SimpleBean(
            property:'foo', publicField:'bar',
            property2:'prop2', publicField2:'field2')

        when:
        def content = render( bean )
        def json = JSON.parse content

        then:
        'foo'    == json.property
        'bar'    == json.publicField
        'prop2'  == json.property2
        'field2' == json.publicField2
        4        == json.keySet().size()
    }

    def "Test special processing of properties"() {
        setup:
        def marshaller = new TestMarshaller(
            app:grailsApplication
        )
        marshaller.processPropertyClosure = {
            BeanWrapper beanWrapper, PropertyDescriptor property, JSON json ->
            if (property.getName() == 'property') {
                json.getWriter().key('modProperty')
                json.convertAnother(beanWrapper.getPropertyValue(property.getName()))
                return false
            } else {
                return true
            }
        }
        register( marshaller )
        SimpleBean bean = new SimpleBean(property:'foo', publicField:'bar')

        when:
        def content = render( bean )
        def json = JSON.parse content

        then:
        !json.containsKey('property')
        'foo' == json.modProperty
    }

    def "Test special processing of fields"() {
        setup:
        def marshaller = new TestMarshaller(
            app:grailsApplication
        )
        marshaller.processFieldClosure = {
            Object obj, Field field, JSON json ->
            if (field.getName() == 'publicField') {
                json.getWriter().key('modPublicField')
                json.convertAnother(field.get(obj))
                return false
            } else {
                return true
            }
        }
        register( marshaller )
        SimpleBean bean = new SimpleBean(property:'foo', publicField:'bar')

        when:
        def content = render( bean )
        def json = JSON.parse content

        then:
        !json.containsKey('publicField')
        'bar' == json.modPublicField
    }

    def "Test alternative names"() {
        setup:
        def marshaller = new TestMarshaller(
            app:grailsApplication
        )
        marshaller.substitutionNames = ['property':'modProperty','publicField':'modPublicField']
        register( marshaller )
        SimpleBean bean = new SimpleBean(property:'foo', publicField:'bar')

        when:
        def content = render( bean )
        def json = JSON.parse content

        then:
        !json.containsKey('property')
        !json.containsKey('publicField')
        'foo' == json.modProperty
        'bar' == json.modPublicField
    }

    def "Test additional fields"() {
        setup:
        def marshaller = new TestMarshaller(
            app:grailsApplication
        )
        marshaller.additionalFieldsClosure = {BeanWrapper wrapper, JSON json ->
            json.property('additionalProp',"some value")
        }
        register( marshaller )
        SimpleBean bean = new SimpleBean(property:'foo', publicField:'bar')

        when:
        def content = render( bean )
        def json = JSON.parse content

        then:
        'some value' == json.additionalProp
    }

    def "Test overriding available properties"() {
        setup:
        def marshaller = new TestMarshaller(
            app:grailsApplication
        )
        marshaller.availablePropertiesClosure = {BeanWrapper wrapper ->
            [wrapper.getPropertyDescriptor('property2')]
        }
        register( marshaller )
        SimpleBean bean = new SimpleBean(property:'foo', publicField:'bar')

        when:
        def content = render( bean )
        def json = JSON.parse content

        then:
        !json.containsKey('property')
        json.containsKey('property2')
    }


    def "Test overriding available fields"() {
        setup:
        def marshaller = new TestMarshaller(
            app:grailsApplication
        )
        marshaller.availableFieldsClosure = {Object value ->
            [value.getClass().getDeclaredField('publicField2')]
        }
        register( marshaller )
        SimpleBean bean = new SimpleBean(property:'foo', publicField:'bar')

        when:
        def content = render( bean )
        def json = JSON.parse content

        then:
        !json.containsKey('publicField')
        json.containsKey('publicField2')
    }

    private void register( String name, def marshaller ) {
        JSON.createNamedConfig( "GroovyBeanMarshallerSpec:" + testName + ":$name" ) { json ->
            json.registerObjectMarshaller( marshaller, 100 )
        }
    }

    private void register( def marshaller ) {
        register( "default", marshaller )
    }

    private String render( String name, def obj ) {
        JSON.use( "GroovyBeanMarshallerSpec:" + testName + ":$name" ) {
            return (obj as JSON) as String
        }
    }

    private String render( def obj ) {
        render( "default", obj )
    }

    class TestMarshaller extends GroovyBeanMarshaller {
        def availablePropertiesClosure
        def availableFieldsClosure
        def excludesClosure
        def includesClosure
        def requireIncludedFieldsClosure
        def processPropertyClosure
        def processFieldClosure
        def substitutionNames = [:]
        def additionalFieldsClosure

        @Override
        protected List<PropertyDescriptor> getAvailableProperties(BeanWrapper beanWrapper) {
            if (availablePropertiesClosure != null) {
                availablePropertiesClosure.call(beanWrapper)
            } else {
                super.getAvailableProperties(beanWrapper)
            }
        }

        @Override
        protected List<Field> getAvailableFields(Object value) {
            if (availableFieldsClosure != null) {
                availableFieldsClosure.call(value)
            } else {
                super.getAvailableFields(value)
            }
        }

        @Override
        protected List<String> getExcludedFields(Object value) {
            if (excludesClosure != null) {
                return excludesClosure.call(value)
            } else {
                return super.getExcludedFields(value)
            }
        }

        @Override
        protected List<String> getIncludedFields(Object value) {
            if (includesClosure != null) {
                return includesClosure.call(value)
            } else {
                return super.getExcludedFields(value)
            }
        }

        @Override
        protected boolean requireIncludedFields(Object value) {
            if (requireIncludedFieldsClosure != null) {
                return requireIncludedFieldsClosure.call(value)
            } else {
                return super.requireIncludedFields(value)
            }
        }

        protected boolean processProperty(BeanWrapper beanWrapper,
                                       PropertyDescriptor property,
                                       JSON json) {
            if (processPropertyClosure != null) {
                processPropertyClosure.call(beanWrapper, property, json)
            } else {
                super.processProperty(beanWrapper, property, json)
            }
        }

        protected boolean processField(Object obj,
                                       Field field,
                                       JSON json) {
            if (processFieldClosure != null) {
                processFieldClosure.call(obj, field, json)
            } else {
                super.processField(obj, field, json)
            }
        }

        protected String getSubstitutionName(BeanWrapper beanWrapper, PropertyDescriptor property) {
            def name = substitutionNames[property.getName()]
            if (name) {
                name
            } else {
                null
            }
        }

        /**
         * Return the name to use when marshalling the field, or
         * null if the field name should be used as-is.
         * @return the name to use when marshalling the field,
         *         or null if the domain field name should be used
         */
        protected String getSubstitutionName(Object value, Field field) {
            def name = substitutionNames[field.getName()]
            if (name) {
                name
            } else {
                null
            }
        }

        protected void processAdditionalFields(BeanWrapper beanWrapper, JSON json) {
            if (additionalFieldsClosure != null) {
                additionalFieldsClosure.call(beanWrapper, json)
            } else {
                super.processAdditionalFields(beanWrapper, json)
            }
        }
    }
}
