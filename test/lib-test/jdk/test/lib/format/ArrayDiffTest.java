/*
 * Copyright (c) 2020, 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.test.lib.format;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
 * @test
 * @summary Check ArrayDiff formatting
 * @library /test/lib
 * @run junit jdk.test.lib.format.ArrayDiffTest
 */
class ArrayDiffTest {

    @Test
    void testEqualArrays() {
        char[] first = new char[]  {'a', 'b', 'c', 'd', 'e', 'f', 'g'};
        char[] second = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g'};

        assertTrue(ArrayDiff.of(first, second).areEqual());
    }

    @Test
    void testOutputFitsWidth() {
        new AssertBuilder()
            .withDefaultParams()
            .withArrays(
                new byte[] {7, 8, 9, 10,  11, 12, 13},
                new byte[] {7, 8, 9, 10, 125, 12, 13})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                4,
                "[7, 8, 9, 10,  11, 12, 13]",
                "[7, 8, 9, 10, 125, 12, 13]",
                "             ^^^^")
            .assertTwoWay();
    }

    @Test
    void testIntegers() {
        new AssertBuilder()
            .withDefaultParams()
            .withArrays(
                new int[] {7, 8, 10, 11, 12},
                new int[] {7, 8, 9, 10, 11, 12, 13})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                2,
                "[7, 8, 10, 11, 12]",
                "[7, 8,  9, 10, 11, 12, 13]",
                "      ^^^")
            .assertTwoWay();
    }

    @Test
    void testLongs() {
        new AssertBuilder()
            .withDefaultParams()
            .withArrays(
                new long[] {1, 2, 3, 4},
                new long[] {1, 2, 3, 10})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                3,
                "[1, 2, 3,  4]",
                "[1, 2, 3, 10]",
                "         ^^^")
            .assertTwoWay();
    }

    @Test
    void testFirstElementIsWrong() {
        new AssertBuilder()
            .withDefaultParams()
            .withArrays(
                new byte[] {122},
                new byte[] {7, 8, 9, 10, 125, 12, 13})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                0,
                "[122]",
                "[  7, 8, 9, 10, 125, 12, 13]",
                " ^^^")
            .assertTwoWay();
    }

    @Test
    void testOneElementIsEmpty() {
        new AssertBuilder()
            .withDefaultParams()
            .withArrays(
                new byte[] {7, 8, 9, 10, 125, 12, 13},
                new byte[] {})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                0,
                "[7, 8, 9, 10, 125, 12, 13]",
                "[]",
                " ^")
            .assertTwoWay();
    }

    @Test
    void testOutputDoesntFitWidth() {
        new AssertBuilder()
            .withParams(20, Integer.MAX_VALUE)
            .withArrays(
                new char[] {'1', '2', '3', '4', '5', '6', '7'},
                new char[] {'1', 'F', '3', '4', '5', '6', '7'})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                1,
                "[1, 2, 3, 4, 5, ...",
                "[1, F, 3, 4, 5, ...",
                "   ^^")
            .assertTwoWay();
    }

    @Test
    void testVariableElementWidthOutputDoesntFitWidth() {
        new AssertBuilder()
            .withParams(20, Integer.MAX_VALUE)
            .withArrays(
                new byte[] {1,   2, 3, 4, 5, 6, 7},
                new byte[] {1, 112, 3, 4, 5, 6, 7})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                1,
                "[1,   2, 3, 4, 5, ...",
                "[1, 112, 3, 4, 5, ...",
                "   ^^^^")
            .assertTwoWay();
    }

    @Test
    void testContextBefore() {
        new AssertBuilder()
            .withParams(20, 2)
            .withArrays(
                new char[] {'1', '2', '3', '4', '5', '6', '7'},
                new char[] {'1', '2', '3', '4', 'F', '6', '7'})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                4,
                "... 3, 4, 5, 6, 7]",
                "... 3, 4, F, 6, 7]",
                "         ^^")
            .assertTwoWay();
    }

    @Test
    void testBoundedBytesWithDifferentWidth() {
        new AssertBuilder()
            .withParams(24, 2)
            .withArrays(
                new byte[] {0, 1, 2, 3, 125, 5, 6, 7},
                new byte[] {0, 1, 2, 3,   4, 5, 6, 7})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                4,
                "... 2, 3, 125, 5, 6, 7]",
                "... 2, 3,   4, 5, 6, 7]",
                "         ^^^^")
            .assertTwoWay();
    }

    @Test
    void testBoundedFirstElementIsWrong() {
        new AssertBuilder()
            .withParams(25, 2)
            .withArrays(
                new byte[] {101, 102, 103, 104, 105, 110},
                new byte[] {2})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                0,
                "[101, 102, 103, 104, ...",
                "[  2]",
                " ^^^")
            .assertTwoWay();
    }

    @Test
    void testBoundedOneArchiveIsEmpty() {
        new AssertBuilder()
            .withParams(10, 2)
            .withArrays(
                new char[] {'a', 'b', 'c', 'd', 'e'},
                new char[] {})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                0,
                "[a, b, ...",
                "[]",
                " ^")
            .assertTwoWay();
    }

    @Test
    void testUnboundedOneArchiveIsEmpty() {
        new AssertBuilder()
            .withDefaultParams()
            .withArrays(
                new char[] {'a', 'b', 'c', 'd', 'e'},
                new char[] {})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                0,
                "[a, b, c, d, e]",
                "[]",
                " ^")
            .assertTwoWay();
    }

    @Test
    void testUnprintableCharFormatting() {
        new AssertBuilder()
            .withDefaultParams()
            .withArrays(
                new char[] {0, 1, 2, 3, 4, 5, 6,   7, 8, 9, 10, 11, 12, 13, 14, 15, 16},
                new char[] {0, 1, 2, 3, 4, 5, 6, 125, 8, 9, 10, 11, 12, 13, 14, 15, 16})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                7,
                "... \\u0005, \\u0006, \\u0007, \\u0008, \\u0009, \\n, \\u000B, \\u000C, \\r, \\u000E, ...",
                "... \\u0005, \\u0006,      }, \\u0008, \\u0009, \\n, \\u000B, \\u000C, \\r, \\u000E, ...",
                "                   ^^^^^^^")
            .assertTwoWay();
    }

    @Test
    void testStringElements() {
        new AssertBuilder()
            .withDefaultParams()
            .withArrays(
                new String[]  {"first", "second", "third", "u\nprintable"},
                new String[] {"first", "second", "incorrect", "u\nprintable"})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                2,
                "[\"first\", \"second\",     \"third\", \"u\\nprintable\"]",
                "[\"first\", \"second\", \"incorrect\", \"u\\nprintable\"]",
                "                   ^^^^^^^^^^^^")
            .assertTwoWay();
    }

    @Test
    void testToStringableObjects() {
        class StrObj {
            private final String value;
            public boolean equals(Object another) { return ((StrObj)another).value.equals(value); }
            public int hashCode() { return value.hashCode(); }
            public StrObj(String value) { this.value = value; }
            public String toString() { return value; }
        }

        new AssertBuilder()
            .withDefaultParams()
            .withArrays(
                new StrObj[] {new StrObj("1"), new StrObj("Unp\rintable"), new StrObj("5")},
                new StrObj[] {new StrObj("1"), new StrObj("2"),            new StrObj("5")})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                1,
                "[1, Unp\\rintable, 5]",
                "[1,            2, 5]",
                "   ^^^^^^^^^^^^^")
            .assertTwoWay();
    }

    @Test
    void testNullElements() {
        new AssertBuilder()
            .withDefaultParams()
            .withArrays(
                new String[] {"Anna", null,   "Bill",    "Julia"},
                new String[] {"Anna", "null", "William", "Julia"})
            .thatResultIs(false)
            .thatFormattedValuesAre(
                1,
                "[\"Anna\",   null,    \"Bill\", \"Julia\"]",
                "[\"Anna\", \"null\", \"William\", \"Julia\"]",
                "        ^^^^^^^")
            .assertTwoWay();
    }

    @Test
    void testFirstArrayIsNull() {
        assertThrows(NullPointerException.class, () -> ArrayDiff.of(null, new String[] {"a", "b"}));
    }

    @Test
    void testSecondArrayIsNull() {
        assertThrows(NullPointerException.class, () -> ArrayDiff.of(new String[] {"a", "b"}, null));
    }

    class AssertBuilder {
        private boolean defaultParameters;
        private int width;
        private int contextBefore;
        private Object firstArray;
        private Object secondArray;
        private boolean expectedResult;
        private int expectedIndex;
        private String firstFormattedArray;
        private String secondFormattedArray;
        private String failureMark;

        AssertBuilder withDefaultParams() {
            defaultParameters = true;
            return this;
        }

        AssertBuilder withParams(int width, int contextBefore) {
            defaultParameters = false;
            this.width = width;
            this.contextBefore = contextBefore;
            return this;
        }

        AssertBuilder withArrays(Object first, Object second) {
            firstArray = first;
            secondArray = second;
            return this;
        }

        AssertBuilder thatResultIs(boolean result) {
            expectedResult = result;
            return this;
        }

        AssertBuilder thatFormattedValuesAre(
                int idx, String first, String second, String mark) {
            expectedIndex = idx;
            firstFormattedArray = first;
            secondFormattedArray = second;
            failureMark = mark;
            return this;
        }

        void assertTwoWay() {
            ArrayDiff<?> diff;

            // Direct
            if (defaultParameters) {
                diff = ArrayDiff.of(firstArray, secondArray);
            } else {
                diff = ArrayDiff.of(firstArray, secondArray, width, contextBefore);
            }

            if (expectedResult == true) {
                assertTrue(diff.areEqual());
            } else {
                String expected = String.format(
                    "Arrays differ starting from [index: %d]:%n" +
                    "%s%n" + "%s%n" + "%s",
                    expectedIndex, firstFormattedArray, secondFormattedArray, failureMark);

                assertFalse(diff.areEqual());
                assertEquals(expected, diff.format());
            }

            // Reversed
            if (defaultParameters) {
                diff = ArrayDiff.of(secondArray, firstArray);
            } else {
                diff = ArrayDiff.of(secondArray, firstArray, width, contextBefore);
            }

            if (expectedResult == true) {
                assertTrue(diff.areEqual());
            } else {
                String expected = String.format(
                    "Arrays differ starting from [index: %d]:%n" +
                    "%s%n" + "%s%n" + "%s",
                    expectedIndex, secondFormattedArray, firstFormattedArray, failureMark);

                assertFalse(diff.areEqual());
                assertEquals(expected, diff.format());
            }
        }

    }

}
