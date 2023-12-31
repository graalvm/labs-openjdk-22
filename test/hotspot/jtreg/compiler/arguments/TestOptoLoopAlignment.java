/*
 * Copyright (c) 2022, Red Hat, Inc. All rights reserved.
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

/*
 * @test
 * @library /test/lib /
 * @bug 8281467
 * @requires vm.flagless
 * @requires os.arch=="amd64" | os.arch=="x86_64"
 *
 * @summary Test large OptoLoopAlignments are accepted
 * @run driver compiler.arguments.TestOptoLoopAlignment
 */

package compiler.arguments;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import jdk.test.lib.process.ProcessTools;
import jdk.test.lib.process.OutputAnalyzer;

public class TestOptoLoopAlignment {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            driver();
        } else {
            System.out.println("Pass");
        }
    }

    private static final String MSG = "must be less or equal to CodeEntryAlignment";

    private static List<String> cmdline(String[] args) {
        List<String> r = new ArrayList();
        r.addAll(Arrays.asList(args));
        r.add("compiler.arguments.TestOptoLoopAlignment");
        r.add("run");
        return r;
    }

    public static void shouldFail(String... args) throws IOException {
        ProcessBuilder pb = ProcessTools.createLimitedTestJavaProcessBuilder(cmdline(args));
        OutputAnalyzer output = new OutputAnalyzer(pb.start());
        output.shouldNotHaveExitValue(0);
        output.shouldContain(MSG);
    }

    public static void shouldPass(String... args) throws IOException {
        ProcessBuilder pb = ProcessTools.createLimitedTestJavaProcessBuilder(cmdline(args));
        OutputAnalyzer output = new OutputAnalyzer(pb.start());
        output.shouldHaveExitValue(0);
        output.shouldNotContain(MSG);
    }

    public static void driver() throws IOException {
        for (int align = 1; align < 64; align *= 2) {
            shouldPass(
                "-XX:OptoLoopAlignment=" + align
            );
        }
        for (int align = 64; align <= 128; align *= 2) {
            shouldFail(
                "-XX:OptoLoopAlignment=" + align
            );
        }
    }

}
