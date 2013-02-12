/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************************/

package net.hedtech.restfulapi

import net.hedtech.restfulapi.marshallers.xml.JSONObjectMarshaller
import grails.converters.*

import grails.test.mixin.*
import org.junit.*

class JSONObjectMarshallerTests {

    void testJSONasXML() {
        def namedConfig = this.getClass().getName() + "_testJSONasXML"
        XML.createNamedConfig( namedConfig  ) {
            it.registerObjectMarshaller(new JSONObjectMarshaller(), 999)
        }
        String data = """
        {
            text:"text 'with single quote'",

            newLine:"a\\nb",
            carriageReturn:"a\\rb",
            horizontalTab:"a\\tb",

            unicode:"\\u00c0",
            number1:123456789,
            number2:0.1234,
            number3:-0.1234,
            number4:123.4567,
            number5:1.34E-93,
            number6:1.34E+52,
            booleanTrue: true,
            booleanFalse: false,
            aNull: null,
            object:{
                text:"i'm an object"
            },
            anArray:["i'm an array elt", 4.5, 1.34E+52]

        }
        """

        def json = JSON.parse( data )
        def xml
        XML.use( namedConfig ) {
            StringWriter w = new StringWriter()
            (json as XML).render( w )
            xml = XML.parse( w.toString() )
        }
        assert "text 'with single quote'" == xml.text.text()
        assert "a\nb" == xml.newLine.text()
        //XML 1.0 processor rules will cause a carriage return to be parsed as a newline
        assert "a\nb" == xml.carriageReturn.text()
        assert "a\tb" == xml.horizontalTab.text()
        assert "\u00c0" == xml.unicode.text()
        assert "123456789" == xml.number1.text()
        assert "0.1234" == xml.number2.text()
        assert "-0.1234" == xml.number3.text()
        assert "123.4567" == xml.number4.text()
        assert "1.34E-93" == xml.number5.text()
        assert "1.34E52" == xml.number6.text()
        assert "true" == xml.booleanTrue.text()
        assert "false" == xml.booleanFalse.text()
        assert "i'm an object" == xml.object[0].text.text()
        assert "i'm an array elt" == xml.anArray[0].'net-hedtech-array'.'net-hedtech-arrayElement'[0].text()
        assert "4.5" == xml.anArray[0].'net-hedtech-array'.'net-hedtech-arrayElement'[1].text()
        assert "1.34E52" == xml.anArray[0].'net-hedtech-array'.'net-hedtech-arrayElement'[2].text()
    }

    void testNested() {
        def namedConfig = this.getClass().getName() + "_testNested"
        XML.createNamedConfig( namedConfig  ) {
            it.registerObjectMarshaller(new JSONObjectMarshaller(), 999)
        }
        def data = """
            {"anArray": [
                ["nested1","nested2"],
                {
                    "foo": ["bar",5.3],
                    bar:"bar text"
                }
            ]}
        """

        def json = JSON.parse( data )
        def xml
        XML.use( namedConfig ) {
            StringWriter w = new StringWriter()
            (json as XML).render( w )
            xml = XML.parse( w.toString() )
        }

        assert "nested1" == xml.anArray.'net-hedtech-array'.'net-hedtech-arrayElement'[0].'net-hedtech-array'.'net-hedtech-arrayElement'[0].text()
        assert "nested2" == xml.anArray.'net-hedtech-array'.'net-hedtech-arrayElement'[0].'net-hedtech-array'.'net-hedtech-arrayElement'[1].text()
        assert "bar" == xml.anArray.'net-hedtech-array'.'net-hedtech-arrayElement'[1].foo.'net-hedtech-array'.'net-hedtech-arrayElement'[0].text()
        assert "5.3" == xml.anArray.'net-hedtech-array'.'net-hedtech-arrayElement'[1].foo.'net-hedtech-array'.'net-hedtech-arrayElement'[1].text()
        assert "bar text" == xml.anArray.'net-hedtech-array'.'net-hedtech-arrayElement'[1].bar.text()

    }

}