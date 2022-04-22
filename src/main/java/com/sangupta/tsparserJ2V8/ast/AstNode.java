/**
 * 
 * tsparser-j2v8: Parsing Typescript using V8 in Java 
 * https://sangupta.com/projects/tsparser-j2v8
 *
 * MIT License.
 * Copyright (c) 2022, Sandeep Gupta.
 *
 * Use of this source code is governed by a MIT style license
 * that can be found in LICENSE file in the code repository.
 * 
 */

package com.sangupta.tsparserJ2V8.ast;

public abstract class AstNode {

	public int kind;

	@Override
	public String toString() {
		return "[" + this.getClass().getName() + "; Kind=" + this.kind + "]";
	}

}
