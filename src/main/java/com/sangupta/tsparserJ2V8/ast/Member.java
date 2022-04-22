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

import java.util.ArrayList;
import java.util.List;

public class Member extends AstNode {
	
	public AstObject name;
	
	public TypeReference type;
	
	public AstObject questionToken;
	
	public final List<AstObject> jsDoc = new ArrayList<>();

	public final List<AstObject> modifiers = new ArrayList<>();
	
	public Initializer initializer;
	
	@Override
	public String toString() {
		return "[Member: " + this.name + "; Kind: " + this.kind + "]";
	}
}
