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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import com.google.gson.GsonBuilder;
import com.sangupta.jerry.http.service.HttpService;
import com.sangupta.jerry.http.service.impl.DefaultHttpServiceImpl;
import com.sangupta.jerry.util.AssertUtils;
import com.sangupta.jerry.util.ReflectionUtils;
import com.sangupta.tsparserJ2V8.ast.SourceFile;

/**
 * 
 * Parse Typescript code (TS/TSX) using J2V8.
 *
 */
public class TypescriptParserExample {

	public static void main(String[] args) {
		NodeJS nodeJS = null;
		V8Object typescript = null;
		V8Object compilerOptions = null;
		V8Object moduleKind = null;
		V8Object result = null;
		
		// define HTTP service
		HttpService httpService = new DefaultHttpServiceImpl();

		// fetch typescript library
		String tsLibUrl = "https://unpkg.com/typescript@4.6.3/lib/typescript.js";

		System.out.println("Fetching Typescript library from: " + tsLibUrl);
		String tsLib = httpService.getTextResponse(tsLibUrl);
		
		// check if we could read it
		if(AssertUtils.isEmpty(tsLib)) {
			System.out.println("Unable to read the Typescript library from: " + tsLibUrl);
			return;
		}
		
		// fetch code to parse
		String codeUrl = "https://raw.githubusercontent.com/sangupta/bedrock/main/src/components/form/Button.tsx";
		System.out.println("Fetching sample TSX code from: " + codeUrl);
		String code = httpService.getTextResponse(codeUrl);
		
		if(AssertUtils.isEmpty(code)) {
			System.out.println("Unable to read sample Typescript code from: " + tsLibUrl);
			return;
		}
		
		try {
			// write it to a temporary file
			File tempTsFile = File.createTempFile("typescript-", ".js");
			FileUtils.writeStringToFile(tempTsFile, tsLib, StandardCharsets.UTF_8);

			// initialize V8/NodeJS runtime
			System.out.println("Initializing V8 environment...");
			nodeJS = NodeJS.createNodeJS();
			
			System.out.println("Loading Typescript in V8...");
			typescript = nodeJS.require(tempTsFile);

			// setup compiler options
			moduleKind = typescript.getObject("ScriptTarget");
			final Integer system = moduleKind.getInteger("Latest");

			compilerOptions = new V8Object(nodeJS.getRuntime());
			compilerOptions.add("module", system);
			
			// extract the AST
			System.out.println("Parsing code...");
			result = (V8Object) typescript.executeJSFunction("createSourceFile", "BedrockButton.tsx", code, compilerOptions, true);
			Map<String, ? super Object> astAsMap = V8ObjectUtils.toMap(result);
			
			// convert this to source file
			System.out.println("Converting AST to strongly-typed objects...");
			SourceFile sourceFile = convertToAst(astAsMap, SourceFile.class);
			
			// print out the nicely formatted JSON
			System.out.println("Convertin to JSON...");
			String json = new GsonBuilder().setPrettyPrinting().create().toJson(sourceFile);
			
			System.out.println("JSON tree: \n");			
			System.out.println(json);
			
			// wait for NodeJS to finish up everything
			while (nodeJS.isRunning()) {
				nodeJS.handleMessage();
			}
			
			// start releasing all results
			result.release();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			release(result);
			release(compilerOptions);
			release(moduleKind);
			release(typescript);

			if (nodeJS != null) {
				try {
					nodeJS.release();
				} catch (Exception e) {
					// eat up
				}
			}
		}
	}

	private static void release(Releasable obj) {
		if (obj == null) {
			return;
		}

		try {
			obj.release();
		} catch (Exception e) {
			// eat up
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T> T convertToAst(Map<String, Object> map, Class<T> clazz) {
		if(map == null) {
			return null;
		}
		
		T instance = newInstance(clazz);

		if(AssertUtils.isEmpty(map)) {
			return instance;
		}
		
		// check fields
		List<Field> fields = ReflectionUtils.getAllFields(clazz);
		if(AssertUtils.isEmpty(fields)) {
			return instance;
		}
		
		// iterate fields
		for(Field field : fields) {
			// find variable name
			String name = field.getName();

			// find value from object map
			Object value = map.get(name);
			if(value == null) {
				// nothing can be done here
				continue;
			}

			// another way to check undefined
			if("class com.eclipsesource.v8.V8Object$Undefined".equals(value.getClass().toString())) {
				continue;
			}
			
			// check for primitives
			if(field.getType().isPrimitive()) {
				setFieldValue(field, instance, value);
				continue;
			}
			
			// check for string
			if(String.class.isAssignableFrom(field.getType())) {
				setFieldValue(field, instance, value.toString());
				continue;
			}
			
			// check for collection
			if(value instanceof Collection) {
				Type type = field.getGenericType();
				Type innerType = null;
				if (type instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) type;
		            innerType = pt.getActualTypeArguments()[0];
		            
		            Collection toPopulate = getCollection(instance, field);
		            Collection<? extends Object> actual = (Collection<?>) value;
		            for(Object item : actual) {
		            	Object arrayItem = convertToAst((Map) item, getClass(innerType));
		            	
		            	toPopulate.add(arrayItem);
		            }
				}
				
				continue;
			}
			
			// this is a pure single object
			setFieldValue(field, instance, convertToAst((Map) value, field.getType()));
		}
		
		return instance;
	}
	
	private static Collection<?> getCollection(Object instance, Field field) {
		try {
			return (Collection<?>) field.get(instance);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static Class<?> getClass(Type type) {
		try {
			return Class.forName(type.getTypeName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static <T> T newInstance(Class<T> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		} 
	}

	private static void setFieldValue(Field field, Object instance, Object value) {
		try {
			ReflectionUtils.bindValue(field, instance, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}			
	}
}
