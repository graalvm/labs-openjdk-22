/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 8273408
 * @summary The compiler shouldn't crash when record component uses the class generated by the annotation processor.
 * @library /tools/lib
 * @modules jdk.compiler/com.sun.tools.javac.api
 *          jdk.compiler/com.sun.tools.javac.main
 *          java.base/jdk.internal.classfile
 *          java.base/jdk.internal.classfile.attribute
 *          java.base/jdk.internal.classfile.constantpool
 *          java.base/jdk.internal.classfile.instruction
 *          java.base/jdk.internal.classfile.components
 *          java.base/jdk.internal.classfile.impl
 * @compile GenerateTypeProcessor.java
 * @run main RecordComponentTypeTest
 */

import jdk.internal.classfile.*;
import jdk.internal.classfile.attribute.RuntimeVisibleAnnotationsAttribute;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import toolbox.JavacTask;
import toolbox.TestRunner;
import toolbox.ToolBox;
import toolbox.Task;

public class RecordComponentTypeTest extends TestRunner {

    ToolBox tb;
    ClassModel cf;

    public RecordComponentTypeTest() {
        super(System.err);
        tb = new ToolBox();
    }

    public static void main(String[] args) throws Exception {
        RecordComponentTypeTest t = new RecordComponentTypeTest();
        t.runTests();
    }

    @Test
    public void testRecordComponentUsingGeneratedType() throws Exception {
        String code = "public record RecordComponentUsingGeneratedType(GeneratedType generatedType) { }";
        Path curPath = Path.of(".");

        // Have no annotation processor.
        List<String> output = new JavacTask(tb)
                .sources(code)
                .outdir(curPath)
                .options("-XDrawDiagnostics")
                .run(Task.Expect.FAIL)
                .writeAll()
                .getOutputLines(Task.OutputKind.DIRECT);
        List<String> expected = Arrays.asList(
                "RecordComponentUsingGeneratedType.java:1:49: compiler.err.cant.resolve.location: kindname.class, " +
                "GeneratedType, , , (compiler.misc.location: kindname.class, RecordComponentUsingGeneratedType, null)",
                "1 error");
        tb.checkEqual(expected, output);

        // Have annotation processor, and processor generates expected type.
        new JavacTask(tb)
                .sources(code)
                .options("-processor", "GenerateTypeProcessor")
                .outdir(curPath)
                .run();
    }

    @Test
    public void testRecordComponentUsingUnknownType() throws Exception {
        String code = "public record RecordComponentUsingUnknownType(UnknownType unknownType) { }";
        Path curPath = Path.of(".");

        // Have no annotation processor.
        List<String> output = new JavacTask(tb)
                .sources(code)
                .outdir(curPath)
                .options("-XDrawDiagnostics")
                .run(Task.Expect.FAIL)
                .writeAll()
                .getOutputLines(Task.OutputKind.DIRECT);
        List<String> expected = Arrays.asList(
                "RecordComponentUsingUnknownType.java:1:47: compiler.err.cant.resolve.location: kindname.class, " +
                "UnknownType, , , (compiler.misc.location: kindname.class, RecordComponentUsingUnknownType, null)",
                "1 error");
        tb.checkEqual(expected, output);

        // Have annotation processor, but processor doesn't generate the expected type.
        List<String> output2 = new JavacTask(tb)
                .sources(code)
                .outdir(curPath)
                .options("-XDrawDiagnostics", "-processor", "GenerateTypeProcessor")
                .run(Task.Expect.FAIL)
                .writeAll()
                .getOutputLines(Task.OutputKind.DIRECT);
        List<String> expected2 = Arrays.asList(
                "RecordComponentUsingUnknownType.java:1:47: compiler.err.cant.resolve.location: kindname.class, " +
                "UnknownType, , , (compiler.misc.location: kindname.class, RecordComponentUsingUnknownType, null)",
                "1 error");
        tb.checkEqual(expected2, output2);
    }


    @Test
    public void testRecordComponentUsingGeneratedTypeWithAnnotation() throws Exception {
        String code = """
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                public record RecordComponentUsingGeneratedTypeWithAnnotation(@TestAnnotation GeneratedType generatedType) { }

                @Retention(RetentionPolicy.RUNTIME)
                @interface TestAnnotation { }
                """;
        Path curPath = Path.of(".");
        new JavacTask(tb)
                .sources(code)
                .options("-processor", "GenerateTypeProcessor")
                .outdir(curPath)
                .run();
        cf = Classfile.of().parse(curPath.resolve("RecordComponentUsingGeneratedTypeWithAnnotation.class"));

        for (FieldModel field : cf.fields()) {
            if (field.fieldName().equalsString("generatedType")){
                checkRuntimeVisibleAnnotation(field);
            }
        }

        for (MethodModel method : cf.methods()) {
            if (method.methodName().equalsString("generatedType")) {
                checkRuntimeVisibleAnnotation(method);
            }
        }
    }

    private void checkRuntimeVisibleAnnotation(AttributedElement attributedElement) throws Exception {
        RuntimeVisibleAnnotationsAttribute annotations = attributedElement.findAttribute(Attributes.RUNTIME_VISIBLE_ANNOTATIONS).orElseThrow();
        boolean hasAnnotation = false;
        for (Annotation annotation : annotations.annotations()) {
            if (annotation.classSymbol().descriptorString().equals("LTestAnnotation;")) {
                hasAnnotation = true;
            }
        }
        if (!hasAnnotation) {
            throw new AssertionError("Expected RuntimeVisibleAnnotation doesn't appear");
        }
    }
}
