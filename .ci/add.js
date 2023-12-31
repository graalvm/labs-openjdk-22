/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
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

/**
 * Example of an add function that gets specialized to doubles
 * if run with --optimize flag set
 */

function add(a,b) {
    return a + b;
}


function bench() {
    var sum = 1;
    for (var x = 0 ; x < 10e8/2 ; x ++) {
    sum *= add(x,x + 1);
    }
    return sum;
}

print("doing first run");
var d = new Date;
print(bench());
d = new Date - d;
print("first run took " + d + " ms");

// invalidate add, replace it with something else
add = function(a,b) {
    return b + a;
}

print("doing second run");
var d = new Date;
print(bench());
d = new Date - d;
print("second run took " + d + " ms");
