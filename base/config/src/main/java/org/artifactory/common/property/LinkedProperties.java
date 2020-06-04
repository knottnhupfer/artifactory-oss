/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.common.property;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * class extends Properties in order to enable
 * reading and writing properties file in order with comments
 *
 * TODO [by fsi] very dangerous class where put() is not equivalent to setProperty()!!!
 * TODO: Basically it is not extending Properties but full override. All methods of Properties should be overridden!
 *
 * @author Chen Keinan
 */
public class LinkedProperties extends Properties {

    private static final Logger log = LoggerFactory.getLogger(LinkedProperties.class);
    private LinkedHashMap<String, String> linkedProps = new LinkedHashMap<>();
    private int commentCount;

    public static String toString(String key, String value) {
        if (key.charAt(0) == '#') {
            return value;
        }
        return key + "=" + value;
    }

    @Override
    public String getProperty(String key) {
        return linkedProps.get(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String propKey = linkedProps.get(key);
        if (propKey != null) {
            return propKey;
        }
        return defaultValue;
    }

    @Override
    public synchronized boolean isEmpty() {
        return linkedProps.isEmpty();
    }

    @Override
    public synchronized boolean contains(Object value) {
        return linkedProps.containsKey(value);
    }

    @Override
    public boolean containsValue(Object value) {
        return linkedProps.containsValue(value);
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return linkedProps.containsKey(key);
    }

    @Override
    public synchronized Object remove(Object key) {
        return linkedProps.remove(key);
    }

    @Override
    public synchronized void putAll(Map<?, ?> t) {
        throw new UnsupportedOperationException("putAll() not supported as this class manages only property String:String");
    }

    @Override
    public synchronized void clear() {
        linkedProps.clear();
    }

    @Override
    public Set<Object> keySet() {
        throw new UnsupportedOperationException("keySet() not supported as this class manages only property String:String");
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        throw new UnsupportedOperationException("entrySet() not supported as this class manages only property String:String");
    }

    @Override
    public Collection<Object> values() {
        throw new UnsupportedOperationException("values() not supported as this class manages only property String:String");
    }

    @Override
    public synchronized Object getOrDefault(Object key, Object defaultValue) {
        return linkedProps.getOrDefault(key, (String) defaultValue);
    }

    @Override
    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        linkedProps.forEach(action);
    }

    @Override
    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
        linkedProps.replaceAll((BiFunction<? super String, ? super String, ? extends String>) function);
    }

    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        return linkedProps.putIfAbsent((String) key, (String) value);
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        return linkedProps.remove(key, value);
    }

    @Override
    public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
        return linkedProps.replace((String) key, (String) oldValue, (String) newValue);
    }

    @Override
    public synchronized Object replace(Object key, Object value) {
        return linkedProps.replace((String) key, (String) value);
    }

    @Override
    public synchronized Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
        return linkedProps.computeIfAbsent((String) key, (Function<? super String, ? extends String>) mappingFunction);
    }

    @Override
    public synchronized Object computeIfPresent(Object key,
            BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return linkedProps.computeIfPresent((String) key,
                (BiFunction<? super String, ? super String, ? extends String>) remappingFunction);
    }

    @Override
    public synchronized Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return linkedProps.compute((String) key,
                (BiFunction<? super String, ? super String, ? extends String>) remappingFunction);
    }

    @Override
    public synchronized Object merge(Object key, Object value,
            BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return linkedProps.merge((String) key, (String) value,
                (BiFunction<? super String, ? super String, ? extends String>) remappingFunction);
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        throw new UnsupportedOperationException("elements() not supported as this class manages only property String:String");
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        throw new UnsupportedOperationException("keys() not supported as this class manages only property String:String");
    }

    @Override
    public synchronized Object get(Object key) {
        return linkedProps.get(key);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        return linkedProps.put((String) key, (String) value);
    }

    @Override
    public synchronized int size() {
        return linkedProps.size();
    }

    @Override
    public synchronized Object setProperty(String key, String value) {
        String trimmed = StringUtils.trimToEmpty(value); // nulls are not allowed
        return linkedProps.put(key, trimmed);
    }

    /**
     * Set properties file data line
     *
     * @param data   - property data
     * @param lineNo - line number
     */
    private void setLine(String data, int lineNo) {
        int i = 0;
        char c;
        int state = 0;
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();

        for (; i < data.length(); i++) {
            c = data.charAt(i);
            switch (state) {
                case 0:
                    if (!Character.isWhitespace(c)) {
                        state = 1;
                        i--;
                    }
                    break;
                case 1:
                    if (c == '#' || c == '!') {
                        i = data.length();
                    } else {
                        key.append(c);
                        state = 2;
                    }
                    break;
                case 2:
                    if (Character.isWhitespace(c) || c == '=' || c == ':') {
                        state = 3;
                        i--;
                    } else {
                        key.append(c);
                    }
                    break;
                case 3:
                    if (!Character.isWhitespace(c)) {
                        state = 4;
                        i--;
                    }
                    break;
                case 4:
                    if (c == '=' || c == ':') {
                        state = 5;
                    } else {
                        throw new IllegalArgumentException("Line in properties file is malformed: " + lineNo);
                    }
                    break;
                case 5:
                    if (!Character.isWhitespace(c)) {
                        state = 6;
                        i--;
                    }
                    break;
                case 6:
                    value.append(c);
                    break;
            }
        }

        if (key.length() == 0) {
            addComment(data);
        } else {
            linkedProps.put(key.toString(), value.toString());
        }
    }

    public void addComment(String comment) {
        linkedProps.put("#" + (++commentCount), comment);
    }

    public void load(String fname) throws IOException {
        File file = new File(fname);
        if (file.exists() && file.isFile()) {
            InputStream inputStream = null;
            try {
                load(inputStream = new FileInputStream(file));
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                        log.error("Error Loading properties File" + ex.getMessage(), ex, log);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void load(InputStream inputStream) throws IOException {
        load(new InputStreamReader(inputStream));
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        String dataLine;
        int lineNo = 0;
        commentCount = 0;
        BufferedReader in = new BufferedReader(reader);
        while ((dataLine = in.readLine()) != null) {
            setLine(dataLine, ++lineNo);
        }
    }

    public void store(String fName, String comment)
            throws IOException {
        File file = new File(fName);
        if (file.exists() && !file.isFile()) {
            return;
        }
        OutputStream outputStream = null;
        try {
            store(outputStream = new FileOutputStream(file), comment);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    log.error("Error Saving properties File" + ex.getMessage(), ex, log);
                }
            }
        }
    }

    @Override
    public void store(OutputStream out, String comment) throws IOException {
        store(new PrintWriter(out, true), comment);
    }

    @Override
    public void store(Writer writer, String comment) throws IOException {
        store(new PrintWriter(writer, true), comment);
    }

    public void store(PrintWriter out, String comment) throws IOException {
        Set<String> keySet = linkedProps.keySet();
        for (String key : keySet) {
            out.println(toString(key, linkedProps.get(key)));
        }
    }

    @Override
    public void list(PrintStream out) {
        Set<String> keySet = linkedProps.keySet();
        for (String key : keySet) {
            out.println(toString(key, linkedProps.get(key)));
        }
    }

    public Iterator<Entry<String, String>> iterator() {
        return linkedProps.entrySet().iterator();
    }
}