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

public class AstObject extends AstNode {
	
	public String escapedText;
	
	public String comment;
	
	public String text;
	
	public boolean hasExtendedUnicodeEscape;
	
	@Override
	public String toString() {
		String txt = this.text != null ? this.text : this.escapedText;
		if(txt == null) {
			if(this.comment == null) {
				return "[Kind: " + this.kind + "]";
			}
			
			return "[Kind: " + this.kind + "; Comment: " + this.comment + "]";
		}
		
		return "[Kind: " + this.kind + "; Text: " + txt  + "]";
	}

}
