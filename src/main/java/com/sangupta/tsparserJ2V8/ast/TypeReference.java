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

public class TypeReference extends AstNode {

	public AstObject typeName;
	
	public AstObject type;
	
	public final List<LiteralType> types = new ArrayList<>();
	
	public final List<Parameter> parameters = new ArrayList<>();
	

}
