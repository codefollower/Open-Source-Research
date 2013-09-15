/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibatis.sqlmap.engine.builder.xml;

import com.ibatis.common.resources.*;
import org.xml.sax.*;

import java.io.*;
import java.util.*;

/**
 * Offline entity resolver for the iBATIS DTDs
 */
public class SqlMapClasspathEntityResolver implements EntityResolver {
    private static my.Debug DEBUG = new my.Debug(my.Debug.SqlMapConfigParser);//我加上的

    private static final String SQL_MAP_CONFIG_DTD = "com/ibatis/sqlmap/engine/builder/xml/sql-map-config-2.dtd";
    private static final String SQL_MAP_DTD = "com/ibatis/sqlmap/engine/builder/xml/sql-map-2.dtd";

    private static final Map doctypeMap = new HashMap();

    static {
        doctypeMap.put("http://www.ibatis.com/dtd/sql-map-config-2.dtd".toUpperCase(), SQL_MAP_CONFIG_DTD);
        doctypeMap.put("http://ibatis.apache.org/dtd/sql-map-config-2.dtd".toUpperCase(), SQL_MAP_CONFIG_DTD);
        doctypeMap.put("-//iBATIS.com//DTD SQL Map Config 2.0//EN".toUpperCase(), SQL_MAP_CONFIG_DTD);
        doctypeMap.put("-//ibatis.apache.org//DTD SQL Map Config 2.0//EN".toUpperCase(), SQL_MAP_CONFIG_DTD);

        doctypeMap.put("http://www.ibatis.com/dtd/sql-map-2.dtd".toUpperCase(), SQL_MAP_DTD);
        doctypeMap.put("http://ibatis.apache.org/dtd/sql-map-2.dtd".toUpperCase(), SQL_MAP_DTD);
        doctypeMap.put("-//iBATIS.com//DTD SQL Map 2.0//EN".toUpperCase(), SQL_MAP_DTD);
        doctypeMap.put("-//ibatis.apache.org//DTD SQL Map 2.0//EN".toUpperCase(), SQL_MAP_DTD);
    }

    /**
     * Converts a public DTD into a local one
     *
     * @param publicId Unused but required by EntityResolver interface
     * @param systemId The DTD that is being requested
     * @return The InputSource for the DTD
     * @throws SAXException If anything goes wrong
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
        try {//我加上的
            DEBUG.P(this, "resolveEntity(2)");
            DEBUG.P("publicId=" + publicId); //以“-//”开头的
            DEBUG.P("systemId=" + systemId); //以"http://"开头的

            if (publicId != null)
                publicId = publicId.toUpperCase();
            if (systemId != null)
                systemId = systemId.toUpperCase();

            InputSource source = null;
            try {
                String path = (String) doctypeMap.get(publicId);
                DEBUG.P("path=" + path);
                source = getInputSource(path, source);
                DEBUG.P("source=" + source);
                if (source == null) {
                    path = (String) doctypeMap.get(systemId);
                    source = getInputSource(path, source);
                }
            } catch (Exception e) {
                throw new SAXException(e.toString());
            }
            return source;

        } finally {//我加上的
            DEBUG.P(0, this, "resolveEntity(2)");
        }
    }

    private InputSource getInputSource(String path, InputSource source) {
        if (path != null) {
            InputStream in = null;
            try {
                in = Resources.getResourceAsStream(path);
                source = new InputSource(in);
            } catch (IOException e) {
                // ignore, null is ok
            }
        }
        return source;
    }

}
