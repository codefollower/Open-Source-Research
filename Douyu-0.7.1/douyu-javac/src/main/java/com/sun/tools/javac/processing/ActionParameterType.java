//package com.sun.tools.javac.processing;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.lang.model.element.VariableElement;
//
//public abstract class ActionParameterType {
//	public abstract String genCode(String name);
//
//	public static Map<String, ActionParameterType> parameterTypes = new HashMap<String, ActionParameterType>();
//	static {
//		parameterTypes.put("douyu.mvc.Context", new ContextType());
//
//		RequestType requestType = new RequestType();
//		parameterTypes.put("javax.servlet.ServletRequest", requestType);
//		parameterTypes.put("javax.servlet.http.HttpServletRequest", requestType);
//
//		ResponseType responseType = new ResponseType();
//		parameterTypes.put("javax.servlet.ServletResponse", responseType);
//		parameterTypes.put("javax.servlet.http.HttpServletResponse", responseType);
//
//		StringType stringType = new StringType();
//		parameterTypes.put("java.lang.String", stringType);
//		parameterTypes.put("String", stringType);
//	}
//
//	public static String parse(VariableElement var) {
//		String type = var.asType().toString();
//		ActionParameterType apt = parameterTypes.get(type);
//		String name = var.getSimpleName().toString();
//		return apt.genCode(name);
//	}
//
//	public static class ContextType extends ActionParameterType {
//		@Override
//		public String genCode(String name) {
//			return "this";
//		}
//	}
//
//	public static class RequestType extends ActionParameterType {
//		@Override
//		public String genCode(String name) {
//			return "this";
//		}
//	}
//
//	public static class ResponseType extends ActionParameterType {
//		@Override
//		public String genCode(String name) {
//			return "this";
//		}
//	}
//
//	public static class StringType extends ActionParameterType {
//		@Override
//		public String genCode(String name) {
//			return "request.getParameter(\"" + name + "\")";
//		}
//	}
//
//}
