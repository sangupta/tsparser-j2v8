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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sangupta.tsparserJ2V8.TypescriptUtils;

public class SourceFile extends AstNode {
	
	private final Map<String, String> resolvedImports = new HashMap<>();
	
	private boolean importsResolved = false;
	
	public List<Statement> statements = new ArrayList<>();
	
	public String getImportPath(String key) {
		if(!this.importsResolved) {
			this.resolveImports();
		}
		
		return this.resolvedImports.get(key);
	}

	private void resolveImports() {
		for(Statement statement : this.statements) {
			if(!TypescriptUtils.isImportDeclaration(statement)) {
				continue;
			}

			// this is an imports clause
			final String library = statement.moduleSpecifier.text;
			if(statement.importClause.name != null) {
				this.resolvedImports.put(statement.importClause.name.escapedText, library);
			}
			
			if(statement.importClause.namedBindings != null) {
				if(statement.importClause.namedBindings.name != null) {
					this.resolvedImports.put(statement.importClause.namedBindings.name.escapedText, library);
				}
				
				for(Element element : statement.importClause.namedBindings.elements) {
					this.resolvedImports.put(element.name.escapedText, library);
				}
			}
		}
		
		this.importsResolved = true;
	}

	public boolean hasClassDeclaration() {
		for(Statement statement : this.statements) {
			if(TypescriptUtils.isClassDeclaration(statement)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Find members from the interface and its inherited interfaces.
	 * 
	 * @param escapedText
	 * @return
	 */
	public List<Member> getMembersOfType(String typeName) {
		// is this an imported typeName
		String importLibrary = this.getImportPath(typeName);
		if(importLibrary != null) {
			return this.getMembersOfTypeFromLibrary(importLibrary, typeName);
		}
		
		for(Statement statement : this.statements) {
			if(TypescriptUtils.isInterfaceDeclaration(statement)) {
				if(statement.name != null && typeName.equals(statement.name.escapedText)) {
					return statement.members;
				}
			}
		}
		
		return null;
	}

	private List<Member> getMembersOfTypeFromLibrary(String importLibrary, String typeName) {
		return null;
	}
}
