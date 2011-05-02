/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.repository.jackrabbit;

import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.PathFactory;
import org.apache.jackrabbit.spi.commons.conversion.IdentifierResolver;
import org.apache.jackrabbit.spi.commons.conversion.IllegalNameException;
import org.apache.jackrabbit.spi.commons.conversion.MalformedPathException;
import org.apache.jackrabbit.spi.commons.conversion.NameParser;
import org.apache.jackrabbit.spi.commons.conversion.NameResolver;
import org.apache.jackrabbit.spi.commons.name.CargoNamePath;
import org.apache.jackrabbit.spi.commons.name.PathBuilder;
import org.apache.jackrabbit.spi.commons.name.PathFactoryImpl;

import javax.jcr.query.Query;
import javax.jcr.NamespaceException;

/**
 * <code>PathParser</code> formats a {@link Path} using a
 * {@link NameResolver} and a {@link PathFactory}.
 */
public class HippoPathParser {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id: ";

    // constants for parser
    private static final int STATE_PREFIX_START = 0;
    private static final int STATE_PREFIX = 1;
    private static final int STATE_NAME_START = 2;
    private static final int STATE_NAME = 3;
    private static final int STATE_INDEX = 4;
    private static final int STATE_INDEX_END = 5;
    private static final int STATE_DOT = 6;
    private static final int STATE_DOTDOT = 7;
    private static final int STATE_IDENTIFIER = 8;
    private static final int STATE_URI = 9;
    private static final int STATE_URI_END = 10;

    /**
     * Parses <code>jcrPath</code> into a <code>Path</code> object using
     * <code>resolver</code> to convert prefixes into namespace URI's. If
     * resolver is <code>null</code> this method only checks the format of the
     * passed String and returns <code>null</code>.
     *
     * @param jcrPath the jcr path.
     * @param resolver the namespace resolver.
     * @param factory <code>PathFactory</code> to be used.
     * @return A path object.
     * @throws MalformedPathException If the <code>jcrPath</code> is malformed.
     * @throws IllegalNameException if any of the jcrNames is malformed.
     * @throws NamespaceException If an unresolvable prefix is encountered.
     */
    public static Path parse(String jcrPath, NameResolver resolver, PathFactory factory)
            throws MalformedPathException, IllegalNameException, NamespaceException {
        return parse(null, jcrPath, resolver, factory);
    }

    /**
     * Parses <code>jcrPath</code> into a <code>Path</code> object using
     * <code>resolver</code> to convert prefixes into namespace URI's. If the
     * specified <code>jcrPath</code> is an identifier based absolute path
     * beginning with an identifier segment the specified
     * <code>IdentifierResolver</code> will be used to resolve it to an
     * absolute path.<p/>
     * If <code>namResolver</code> is <code>null</code> or if <code>identifierResolver</code>
     * is <code>null</code> and the path starts with an identifier segment, this
     * method only checks the format of the string and returns <code>null</code>.
     *
     * @param jcrPath the jcr path.
     * @param nameResolver the namespace resolver.
     * @param identifierResolver the resolver to validate any trailing identifier
     * segment and resolve to an absolute path.
     * @param factory
     * @return A path object.
     * @throws MalformedPathException If the <code>jcrPath</code> is malformed.
     * @throws IllegalNameException if any of the jcrNames is malformed.
     * @throws NamespaceException If an unresolvable prefix is encountered.
     * @since JCR 2.0
     */
    public static Path parse(String jcrPath, NameResolver nameResolver,
                             IdentifierResolver identifierResolver, PathFactory factory)
            throws MalformedPathException, IllegalNameException, NamespaceException {
        return parse(null, jcrPath, nameResolver, identifierResolver, factory);
    }

    /**
     * Parses <code>jcrPath</code> into a <code>Path</code> object using
     * <code>resolver</code> to convert prefixes into namespace URI's. If the
     * specified <code>jcrPath</code> is an identifier based absolute path
     * beginning with an identifier segment the specified
     * <code>IdentifierResolver</code> will be used to resolve it to an
     * absolute path.<p/>
     * If <code>namResolver</code> is <code>null</code> or if <code>identifierResolver</code>
     * is <code>null</code> and the path starts with an identifier segment, this
     * method only checks the format of the string and returns <code>null</code>.
     *
     * @param jcrPath the jcr path.
     * @param nameResolver the namespace resolver.
     * @param identifierResolver the resolver to validate any trailing identifier
     * segment and resolve to an absolute path.
     * @param factory
     * @param normalizeIdentifier
     * @return A path object.
     * @throws MalformedPathException If the <code>jcrPath</code> is malformed.
     * @throws IllegalNameException if any of the jcrNames is malformed.
     * @throws NamespaceException If an unresolvable prefix is encountered.
     * @since JCR 2.0
     */
    public static Path parse(String jcrPath, NameResolver nameResolver,
                             IdentifierResolver identifierResolver,
                             PathFactory factory, boolean normalizeIdentifier)
            throws MalformedPathException, IllegalNameException, NamespaceException {
        return parse(null, jcrPath, nameResolver, identifierResolver, factory, normalizeIdentifier);
    }

    /**
     * Parses the given <code>jcrPath</code> and returns a <code>Path</code>. If
     * <code>parent</code> is not <code>null</code>, it is prepended to the
     * built path before it is returned. If <code>resolver</code> is
     * <code>null</code>, this method only checks the format of the string and
     * returns <code>null</code>.
     *
     * @param parent   the parent path
     * @param jcrPath  the JCR path
     * @param resolver the namespace resolver to get prefixes for namespace
     *                 URI's.
     * @param factory
     * @return the <code>Path</code> object.
     * @throws MalformedPathException If the <code>jcrPath</code> is malformed.
     * @throws IllegalNameException if any of the jcrNames is malformed.
     * @throws NamespaceException If an unresolvable prefix is encountered.
     */
    public static Path parse(Path parent, String jcrPath,
                             NameResolver resolver,
                             PathFactory factory) throws MalformedPathException, IllegalNameException, NamespaceException {
        return parse(parent, jcrPath, resolver, null, factory);
    }

    /**
     * Parses the given <code>jcrPath</code> and returns a <code>Path</code>. If
     * <code>parent</code> is not <code>null</code>, it is prepended to the
     * built path before it is returned. If the specifed <code>jcrPath</code>
     * is an identifier based absolute path beginning with an identifier segment
     * the given <code>identifierResolver</code> will be used to resolve it to an
     * absolute path.<p/>
     * If <code>nameResolver</code> is <code>null</code> or if <code>identifierResolver</code>
     * is <code>null</code> and the path starts with an identifier segment, this
     * method only checks the format of the string and returns <code>null</code>.
     *
     * @param parent the parent path.
     * @param jcrPath the jcr path.
     * @param nameResolver the namespace resolver.
     * @param identifierResolver the resolver to validate any trailing identifier
     * segment and resolve it to an absolute path.
     * @param factory The path factory.
     * @return the <code>Path</code> object.
     * @throws MalformedPathException
     * @throws IllegalNameException
     * @throws NamespaceException
     */
    public static Path parse(Path parent, String jcrPath, NameResolver nameResolver,
                             IdentifierResolver identifierResolver, PathFactory factory)
            throws MalformedPathException, IllegalNameException, NamespaceException {
        return parse(parent, jcrPath, nameResolver, identifierResolver, factory, true);
    }

    /**
     * Parses the given <code>jcrPath</code> and returns a <code>Path</code>. If
     * <code>parent</code> is not <code>null</code>, it is prepended to the
     * built path before it is returned. If the specifed <code>jcrPath</code>
     * is an identifier based absolute path beginning with an identifier segment
     * the given <code>identifierResolver</code> will be used to resolve it to an
     * absolute path.<p/>
     * If <code>nameResolver</code> is <code>null</code> or if <code>identifierResolver</code>
     * is <code>null</code> and the path starts with an identifier segment, this
     * method only checks the format of the string and returns <code>null</code>.
     *
     * @param parent the parent path.
     * @param jcrPath the jcr path.
     * @param nameResolver the namespace resolver.
     * @param identifierResolver the resolver to validate any trailing identifier
     * segment and resolve it to an absolute path.
     * @param factory The path factory.
     * @param normalizeIdentifier
     * @return the <code>Path</code> object.
     * @throws MalformedPathException
     * @throws IllegalNameException
     * @throws NamespaceException
     */
    private static Path parse(Path parent, String jcrPath, NameResolver nameResolver,
                             IdentifierResolver identifierResolver, PathFactory factory,
                             boolean normalizeIdentifier)
            throws MalformedPathException, IllegalNameException, NamespaceException {
        
        final char EOF = (char) -1;

        // check for length
        int len = jcrPath == null ? 0 : jcrPath.length();

        // shortcut
        if (len == 1 && jcrPath.charAt(0) == '/') {
            return factory.getRootPath();
        }

        if (len == 0) {
            throw new MalformedPathException("empty path");
        }

        // check if absolute path
        PathBuilder builder = new PathBuilder(factory);
        int pos = 0;
        if (jcrPath.charAt(0) == '/') {
            if (parent != null) {
                throw new MalformedPathException("'" + jcrPath + "' is not a relative path.");
            }
            builder.addRoot();
            pos++;
        }

        // add master if present
        if (parent != null) {
            builder.addAll(parent.getElements());
        }

        // parse the path
        int state;
        if (jcrPath.charAt(0) == '[') {
            if (parent != null) {
                throw new MalformedPathException("'" + jcrPath + "' is not a relative path.");
            }
            state = STATE_IDENTIFIER;
            pos++;
        } else {
            state = STATE_PREFIX_START;
        }

        int lastPos = pos;

        String name = null;

        int index = Path.INDEX_UNDEFINED;
        String argument = null;
        boolean wasSlash = false;

        boolean checkFormat = (nameResolver == null);

        while (pos <= len) {
            char c = pos == len ? EOF : jcrPath.charAt(pos);
            pos++;
            // special check for whitespace
            if (c != ' ' && Character.isWhitespace(c)) {
                c = '\t';
            }
            switch (c) {
                case '/':
                case EOF:
                    if (state == STATE_PREFIX_START && c != EOF) {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. double slash '//' not allowed.");
                    }
                    if (state == STATE_PREFIX
                            || state == STATE_NAME
                            || state == STATE_INDEX_END
                            || state == STATE_URI_END) {

                        // eof pathelement
                        if (name == null) {
                            if (wasSlash) {
                                throw new MalformedPathException("'" + jcrPath + "' is not a valid path: Trailing slashes not allowed in prefixes and names.");
                            }
                            name = jcrPath.substring(lastPos, pos - 1);
                        }

                        // only add element if resolver not null. otherwise this
                        // is just a check for valid format.
                        if (checkFormat) {
                            NameParser.checkFormat(name);
                        } else {
                            Name qName = nameResolver.getQName(name);
                            if(argument != null) {
                                builder.addLast(new CargoNamePath(null, qName, argument));
                            } else {
                                builder.addLast(qName, index);
                            }
                        }
                        state = STATE_PREFIX_START;
                        lastPos = pos;
                        name = null;
                        index = Path.INDEX_UNDEFINED;
                        argument = null;
                    } else if (state == STATE_IDENTIFIER) {
                        if (c == EOF) {
                            // eof identifier reached                            
                            if (jcrPath.charAt(pos - 2) != ']') {
                                throw new MalformedPathException("'" + jcrPath + "' is not a valid path: Unterminated identifier segment.");
                            }
                            String identifier = jcrPath.substring(lastPos, pos - 2);
                            if (checkFormat) {
                                if (identifierResolver != null) {
                                    identifierResolver.checkFormat(identifier);
                                } // else ignore. TODO: rather throw?
                            } else if (identifierResolver == null) {
                                throw new MalformedPathException("'" + jcrPath + "' is not a valid path: Identifier segments are not supported.");
                            } else if (normalizeIdentifier) {
                                builder.addAll(identifierResolver.getPath(identifier).getElements());
                            } else {
                                identifierResolver.checkFormat(identifier);
                                builder.addLast(factory.createElement(identifier));
                            }
                            state = STATE_PREFIX_START;
                            lastPos = pos;
                        }
                    } else if (state == STATE_DOT) {
                        builder.addLast(factory.getCurrentElement());
                        lastPos = pos;
                        state = STATE_PREFIX_START;
                    } else if (state == STATE_DOTDOT) {
                        builder.addLast(factory.getParentElement());
                        lastPos = pos;
                        state = STATE_PREFIX_START;
                    } else if (state == STATE_PREFIX_START && c == EOF) {
                        // ignore trailing slash
                    } else if (state != STATE_URI) {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. '" + c + "' not a valid name character.");
                    }
                    break;

                case '.':
                    if (state == STATE_PREFIX_START) {
                        state = STATE_DOT;
                    } else if (state == STATE_DOT) {
                        state = STATE_DOTDOT;
                    } else if (state == STATE_DOTDOT) {
                        state = STATE_PREFIX;
                    } else if (state == STATE_INDEX_END) {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. '" + c + "' not valid after index. '/' expected.");
                    }
                    break;

                case ':':
                    if (state == STATE_PREFIX_START) {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. Prefix must not be empty");
                    } else if (state == STATE_PREFIX) {
                        if (wasSlash) {
                            throw new MalformedPathException("'" + jcrPath + "' is not a valid path: Trailing slashes not allowed in prefixes and names.");
                        }
                        state = STATE_NAME_START;
                        // don't reset the lastPos/pos since prefix+name are passed together to the NameResolver
                    } else if (state == STATE_IDENTIFIER || state == STATE_URI) {
                        // nothing do
                    } else {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. '" + c + "' not valid name character");
                    }
                    break;

                case '[':
                    if (state == STATE_PREFIX || state == STATE_NAME) {
                        if (wasSlash) {
                            throw new MalformedPathException("'" + jcrPath + "' is not a valid path: Trailing slashes not allowed in prefixes and names.");
                        }
                        state = STATE_INDEX;
                        name = jcrPath.substring(lastPos, pos - 1);
                        lastPos = pos;
                        String matchString = null;
                        switch(jcrPath.charAt(pos)) {
                            case '{':   matchString = "}";  break;
                            case '\'':  matchString = "\'"; break;
                            case '[':   matchString = "]";  break;
                        }
                        if(matchString != null) {
                            if (jcrPath.length() >= Query.XPATH.length() + 2 + pos + 1 &&
                                jcrPath.substring(pos+1, pos+1+Query.XPATH.length()+1).equalsIgnoreCase(Query.XPATH+"(")) {
                                matchString = ")" + matchString;
                                lastPos = ++pos;
                            } else if (jcrPath.length() >= Query.SQL.length() + 2 + pos + 1 &&
                                       jcrPath.substring(pos+1, pos+1+Query.SQL.length()+1).equalsIgnoreCase(Query.SQL+"(")) {
                                matchString = ")" + matchString;
                                lastPos = ++pos;
                            } else {
                                lastPos = ++pos;
                            }
                            while(pos <= len && !jcrPath.substring(pos).startsWith(matchString))
                                ++pos;
                            index = Path.INDEX_DEFAULT;
                            argument = jcrPath.substring(lastPos, pos+(matchString.length()-1));
                            if(pos+matchString.length() > len)
                                throw new MalformedPathException("'" + jcrPath + "' is not a valid path: Mismatching ] character");
                            pos += matchString.length();
                            if(jcrPath.charAt(pos) != ']')
                                throw new MalformedPathException("'" + jcrPath + "' is not a valid path: Mismatching ] character");
                            pos += 1;
                            state = STATE_INDEX_END;
                        }
                    } else if (state == STATE_INDEX) {
                        lastPos = pos;
                        while(pos <= len && jcrPath.charAt(pos) != ']')
                            ++pos;
                        if(pos > len || jcrPath.charAt(++pos) != ']')
                            throw new MalformedPathException("'" + jcrPath + "' is not a valid path: Mismatching ] character");
                        index = Path.INDEX_DEFAULT;
                        argument = jcrPath.substring(lastPos, pos - 1);
                        ++pos;
                        state = STATE_INDEX_END;
                    } else if (state == STATE_IDENTIFIER) {
                        // nothing do
                    } else {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. '" + c + "' not a valid name character.");
                    }
                    break;

                case ']':
                    if (state == STATE_INDEX) {
                        try {
                            index = Integer.parseInt(jcrPath.substring(lastPos, pos - 1));
                        } catch (NumberFormatException e) {
                            throw new MalformedPathException("'" + jcrPath + "' is not a valid path. NumberFormatException in index: " + jcrPath.substring(lastPos, pos - 1));
                        }
                        if (index < Path.INDEX_DEFAULT) {
                            throw new MalformedPathException("'" + jcrPath + "' is not a valid path. Index number invalid: " + index);
                        }
                        state = STATE_INDEX_END;
                    } else if (state == STATE_IDENTIFIER) {
                        // nothing do
                    } else {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. '" + c + "' not a valid name character.");
                    }
                    break;

                case ' ':
                    if (state == STATE_PREFIX_START || state == STATE_NAME_START) {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. '" + c + "' not valid name start");
                    } else if (state == STATE_INDEX_END) {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. '" + c + "' not valid after index. '/' expected.");
                    } else if (state == STATE_DOT || state == STATE_DOTDOT) {
                        state = STATE_PREFIX;
                    } else if (state == STATE_INDEX_END) {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. '" + c + "' not valid after index. '/' expected.");
                    }
                    break;

                case '\t':
                    if (state != STATE_IDENTIFIER) {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. Whitespace not a allowed in name.");
                    }
                case '*':
                case '|':
                    if (state != STATE_IDENTIFIER) {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. '" + c + "' not a valid name character.");
                    }
                case '{':
                    if (state == STATE_PREFIX_START) {
                        state = STATE_URI;
                    }
                    break;

                case '}':
                    if (state == STATE_URI) {
                        state = STATE_URI_END;
                    }
                    break;
                
                default:
                    if (state == STATE_PREFIX_START || state == STATE_DOT || state == STATE_DOTDOT) {
                        state = STATE_PREFIX;
                    } else if (state == STATE_NAME_START) {
                        state = STATE_NAME;
                    } else if (state == STATE_INDEX_END) {
                        throw new MalformedPathException("'" + jcrPath + "' is not a valid path. '" + c + "' not valid after index. '/' expected.");
                    }
            }
            wasSlash = c == ' ';
        }

        if (checkFormat) {
            // this was only for checking the format
            return null;
        } else {
            return builder.getPath();
        }
    }

    /**
     * Check the format of the given jcr path. Note, the neither name nor
     * namespace validation (resolution of prefix to URI) is performed and
     * therefore will not be detected.
     *
     * @param jcrPath
     * @throws MalformedPathException If the <code>jcrPath</code> is malformed.
     */
    public static void checkFormat(String jcrPath) throws MalformedPathException {
        try {
            // since no path is created -> use default factory
            parse(jcrPath, null, null, PathFactoryImpl.getInstance());
        } catch (NamespaceException e) {
            // will never occur
        } catch (IllegalNameException e) {
            // will never occur
        }
    }

    static Path.Element createElement(Name name, String argument) throws IllegalArgumentException {
        if (name == null)
            throw new IllegalArgumentException("The name must not be null");
        return new CargoNamePath(null, name, argument);
    }
}
