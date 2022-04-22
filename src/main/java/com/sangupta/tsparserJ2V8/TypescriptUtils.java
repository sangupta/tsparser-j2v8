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

package com.sangupta.tsparserJ2V8;

import java.util.List;

import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.StringUtils;
import com.sangupta.tsparserJ2V8.ast.AstNode;
import com.sangupta.tsparserJ2V8.ast.AstObject;
import com.sangupta.tsparserJ2V8.ast.Block;
import com.sangupta.tsparserJ2V8.ast.Expression;
import com.sangupta.tsparserJ2V8.ast.Member;
import com.sangupta.tsparserJ2V8.ast.Statement;

public class TypescriptUtils {
	
	public static final String UNKNOWN = "$unknown";
	
	private static final int KIND_VOID_KEYWORD = 113;
	private static final int KIND_ANY_KEYWORD = 128;
	private static final int KIND_BOOLEAN_KEYWORD = 131;
	private static final int KIND_NEVER_KEYWORD = 141;
	private static final int KIND_NUMBER_KEYWORD = 144;
	private static final int KIND_STRING_KEYWORD = 147;
	private static final int KIND_UNDEFINED_KEYWORD = 150;
	private static final int KIND_ARROW_METHOD = 164;
	private static final int KIND_SIMPLE_METHOD = 166;
	private static final int KIND_FUNCTION_TYPE = 175;
	private static final int KIND_UNION_TYPE = 183;
	private static final int KIND_NULL_KEYWORD = 192;
	private static final int KIND_PROPERTY_ACCESS_EXPR = 198;
	private static final int KIND_PARANTHESIS_EXPR = 208;	
	private static final int KIND_EXPR_WITH_TYPE_ARGS = 224;
	private static final int KIND_RETURN_STATEMENT = 243;
	private static final int KIND_CLASS_DECL = 253;	
	private static final int KIND_FUNCTION_DECL = 252;
	private static final int KIND_INTERFACE_DECL = 254;
	private static final int KIND_IMPORT_DECL = 262;
	private static final int KIND_JSX_ELEMENT = 274;
	private static final int KIND_JSX_FRAGMENT = 278;
	private static final int KIND_HERITAGE_CLAUSE = 287;
	
	
	public static String getNodeType(AstNode node) {
		switch(node.kind) {
			case 253:
				return "ClassDeclaration";
			case 252:
				return "FunctionDeclaration";
			case 254:
				return "InterfaceDeclaration";
			case 262:
				return "ImportDeclaration";
		}
		
		return "Unknown";
	}
	
	private static boolean isReturnStatement(AstNode node) {
		return node.kind == KIND_RETURN_STATEMENT;
	}
	
	public static boolean isClassDeclaration(AstNode node) {
		return node.kind == KIND_CLASS_DECL;
	}
	
	public static boolean isMethodDeclaration(AstNode node) {
		return isSimpleMethodDeclaration(node) || isArrowMethodDeclaration(node);
	}

	public static boolean isArrowMethodDeclaration(AstNode node) {
		return node.kind == KIND_ARROW_METHOD;
	}
	
	public static boolean isSimpleMethodDeclaration(AstNode node) {
		return node.kind == KIND_SIMPLE_METHOD;
	}

	public static boolean isInterfaceDeclaration(AstNode node) {
		return node.kind == KIND_INTERFACE_DECL;
	}

	public static boolean isImportDeclaration(AstNode node) {
		return node.kind == KIND_IMPORT_DECL;
	}

	public static boolean isPropertyAccessExpression(AstNode node) {
		return node.kind == KIND_PROPERTY_ACCESS_EXPR;
	}

	public static boolean isExpressionWithTypeArguments(AstNode node) {
		return node.kind == KIND_EXPR_WITH_TYPE_ARGS;
	}

	public static boolean isHeritageClause(AstNode node) {
		return node.kind == KIND_HERITAGE_CLAUSE;
	}
	
	public static boolean isUnionType(AstNode node) {
		return node.kind == KIND_UNION_TYPE;
	}

	public static boolean isFunctionDeclaration(AstNode node) {
		return node.kind == KIND_FUNCTION_DECL;
	}

	public static boolean hasMethod(Statement statement, String methodName) {
		if(!isClassDeclaration(statement)) {
			return false;
		}
		
		for(Member member : statement.members) {
			if(isMethodDeclaration(member) && member.name.escapedText.equals(methodName)) {
				return true;
			}
		}
		
		return false;
	}

	public static String getJsDoc(List<AstObject> jsDoc) {
		if(AssertUtils.isEmpty(jsDoc)) {
			return StringUtils.EMPTY_STRING;
		}
		
		if(jsDoc.size() == 1) {
			return jsDoc.get(0).comment;
		}
		
		StringBuilder builder = new StringBuilder(1024);
		for(AstObject doc : jsDoc) {
			builder.append(doc.comment);
			builder.append("\n");
		}
		
		return builder.toString();
	}

	public static boolean returnsJsxFragement(Block body) {
		for(Statement statement : body.statements) {
			if(isReturnStatement(statement) && isJsxElement(statement.expression)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Check if given expression returns a JsxElement or a JsxFragment.
	 * 
	 * @param expression
	 * @return
	 */
	private static boolean isJsxElement(Expression expression) {
		if(expression == null) {
			return false;
		}
		
		if(expression.kind == KIND_JSX_ELEMENT) {
			return true;
		}
		
		if(expression.kind == KIND_JSX_FRAGMENT) {
			return true;
		}
		
		if(expression.kind == KIND_PARANTHESIS_EXPR && isJsxElement(expression.expression)) {
			return true;
		}
		
		return false;
	}

	public static boolean isFunctionType(AstNode node) {
		return node.kind == KIND_FUNCTION_TYPE;
	}

	public static String getType(AstNode node) {
		if(node == null) {
			return null;
		}
		
		switch(node.kind) {
			case KIND_NUMBER_KEYWORD:
				return "number";
			case KIND_STRING_KEYWORD:
				return "string";
			case KIND_BOOLEAN_KEYWORD:
				return "boolean";
			case KIND_VOID_KEYWORD:
				return "void";
			case KIND_FUNCTION_TYPE:
				return "Function";
			case KIND_ANY_KEYWORD:
				return "any";
			case KIND_NULL_KEYWORD:
				return "null";
			case KIND_UNDEFINED_KEYWORD:
				return "undefined";
			case KIND_NEVER_KEYWORD:
				return "never";
		}
		
		return UNKNOWN;
	}
	
}
