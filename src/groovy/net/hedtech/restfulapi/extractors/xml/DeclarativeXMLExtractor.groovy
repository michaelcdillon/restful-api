/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************************/
package net.hedtech.restfulapi.extractors.xml

import net.hedtech.restfulapi.extractors.*

class DeclarativeXMLExtractor extends BasicXMLExtractor {

    Map<String,String> dottedRenamedPaths = [:]
    Map<String,Object> dottedValuePaths = [:]
    List<String> dottedShortObjectPaths = []
    List<String> dottedFlattenedPaths = []
    Closure shortObjectClosure


    /**
     * Returns a map of rename rules.  The keys
     * are List of String denoting paths to keys in the map which
     * should be renamed as the value the path is mapped to.
     */
    @Override
    protected Map<List<String>,String> getRenamePaths() {
        def result = [:]
        dottedRenamedPaths.entrySet().each { Map.Entry entry ->
            result.put(parse(entry.key),entry.value)
        }
        result
    }

    /**
     * Returns a map of default value rules.  The keys
     * are List of String denoting paths to keys in the map
     * which should have the default value if the key is not
     * already present.
     */
    @Override
    protected Map<List<String>,Object> getDefaultValuePaths() {
        def result = [:]
        dottedValuePaths.entrySet().each { Map.Entry entry ->
            result.put(parse(entry.key),entry.value)
        }
        result
    }

    /**
     * Returns a List of String denoting paths whose
     * values should be treated as short objects
     * and converted according the the short object closure.
     **/
    @Override
    protected List<List<String>> getShortObjectPaths() {
        def result = []
        dottedShortObjectPaths.each { String path ->
            result.add(parse(path))
        }
        result
    }

    /**
     * Returns a List of String denoting paths whose
     * values should be flattened into the containing map.
     **/
    @Override
     protected List<List<String>> getFlattenPaths() {
        def result = []
        dottedFlattenedPaths.each { String path ->
            result.add(parse(path))
        }
        result
     }

    /**
     * Returns a closure that can convert a 'short object'
     * representation to a map containing the id represented
     * by the short object reference.
     */
    @Override
    protected Closure getShortObjectClosure() {
        shortObjectClosure == null ? super.getShortObjectClosure() : shortObjectClosure
    }

    protected List<String> parse(String path) {
        path.split("\\.")
    }
}
