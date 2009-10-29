/*
 * Copyright (C) 2009 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.support;

public class Chars {

    private Chars() {}

    // special, reserved Unicode non-characters (guaranteed to never actually denote a real char) we use for special meaning
    public static final char EOI = '\uFFFF'; // end of inputLine
    public static final char ANY = '\uFFFE';
    public static final char EMPTY = '\uFFFD';

    public static boolean isSpecial(char c) {
        return c == EOI || c == ANY || c == EMPTY;
    }

}
