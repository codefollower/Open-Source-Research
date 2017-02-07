package com.sun.tools.javac.processing;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleElementVisitor7;
import javax.lang.model.SourceVersion;
import javax.tools.JavaFileObject;

import douyu.http.HttpMethod;
import douyu.mvc.Action;
import douyu.mvc.Async;
import douyu.mvc.Controller;
import douyu.mvc.Model;

/**
 * 
 * @author ZHH
 * 
 */

// 如果用这个，则在java1.6中会出错
// 见com.sun.tools.javac.processing.JavacProcessingEnvironment.ProcessorState.checkSourceVersionCompatibility
// Override掉超类的getSupportedSourceVersion()更合适一些
// @SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("douyu.mvc.Controller")
// @SupportedAnnotationTypes("*")
public class ControllerProcessor extends AbstractProcessor {

	private static String SUFFIX = "$DOUYU";
	private static String CONTROLLER_VAR_NAME = "_c";

	private static int varCount = 1;

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public boolean process(Set<? extends TypeElement> tes, RoundEnvironment renv) {
		// JavacProcessingEnvironment处理到最后一个Round时，我们就不再管它了。
		if (tes == null || tes.size() == 0)
			return false;

		// renv.getRootElements()返回的Element并不是都有@Controller的
		// 按理说用@SupportedAnnotationTypes指明我的Processor只处理哪些注解，javac就应该返回带有@Controller注解的Element，
		// 但是实际情况并不是这样，所以这里要再判断一下。
		for (Element element : renv.getRootElements()) {
			ElementKind kind = element.getKind();
			if (kind != ElementKind.CLASS) // 有可能是package-info.java中指定的PackageElement(也就是PackageSymbol)
				continue;
			// 只处理最顶层的类
			NestingKind nestingKind = ((TypeElement) element).getNestingKind();
			if (nestingKind != NestingKind.TOP_LEVEL)
				continue;

			Controller controller = element.getAnnotation(Controller.class);
			if (controller == null)
				continue;

			varCount = 1; // 在开发阶段，如果不断更改源文件，会不断重新编译，这个值如果不清零，会一直加

			String className = ((TypeElement) element).getQualifiedName().toString() + SUFFIX;
			new ControllerElementVisitor(controller, processingEnv).visit(element).writeTo(className);
		}

		return true;
	}

	private static class ControllerElementVisitor extends SimpleElementVisitor7<ControllerElementVisitor, Void> {
		private final ProcessingEnvironment processingEnv;
		private final Elements elementUtils;
		private final Messager messager;
		private final Filer filer;
		private final Controller controller;
		private final PrettyPrinter p = new PrettyPrinter();

		private final PrettyPrinter p_isAsyncAction = new PrettyPrinter(2);

		boolean isAsyncController;

		public ControllerElementVisitor(Controller controller, ProcessingEnvironment processingEnv) {
			this.processingEnv = processingEnv;
			this.elementUtils = processingEnv.getElementUtils();
			this.messager = processingEnv.getMessager();
			this.filer = processingEnv.getFiler();
			this.controller = controller;
		}

		public void writeTo(String className) {
			try {
				JavaFileObject jfo = filer.createSourceFile(className, (Element[]) null);
				Writer w = jfo.openWriter();
				w.write(p.toString());
				// 在com.sun.tools.javac.processing.JavacProcessingEnvironment.doProcessing中会调用filer.warnIfUnclosedFiles()
				// 所以必需要close，否则JavacProcessingEnvironment会出错
				w.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public ControllerElementVisitor visitType(TypeElement e, Void v) {
			// 带有@Controller的类必需是public的
			if (!e.getModifiers().contains(Modifier.PUBLIC)) {
				messager.printMessage(javax.tools.Diagnostic.Kind.ERROR, "class with @Controller must be 'public'", e);
				return this;
			}

			isAsyncController = e.getAnnotation(Async.class) != null;

			List<? extends Element> enclosedElements = e.getEnclosedElements();

			// 带有@Controller的类必需有一个public的默认构造函数
			boolean find = false;
			for (Element element : enclosedElements) {
				if (element.getKind() == ElementKind.CONSTRUCTOR && element.getModifiers().contains(Modifier.PUBLIC)
						&& ((ExecutableElement) element).getParameters().size() == 0) {
					find = true;
					break;
				}
			}

			if (!find) {
				messager.printMessage(javax.tools.Diagnostic.Kind.ERROR,
						"class with @Controller must contain a 'public' 0-parameter constructor", e);
				return this;
			}

			PackageElement pkg = elementUtils.getPackageOf(e);
			if (!pkg.isUnnamed()) {
				p.println("package " + pkg.getQualifiedName() + ";");
				p.println();
			}

			String simpleName = e.getSimpleName().toString();
			p.print("public class ").append(simpleName).append(SUFFIX).append(
					" extends com.codefollower.douyu.mvc.DouyuContext {");
			p.println();

			p.tab++;
			p.print("private static ").append(simpleName).append(" ").append(CONTROLLER_VAR_NAME).append(" = new ")
					.append(simpleName).append("();");
			p.println();
			p.println();

			p.println("protected void executeAction() throws Exception {");
			p.tab++;

			p.println("String p;");
			p.print("if(actionName == null) actionName = \"").append(controller.defaultAction()).append("\";");
			p.println();
			p.println();

			for (Element element : enclosedElements) {
				if (element.getKind() == ElementKind.METHOD)
					this.visit(element);
			}

			p.println("else {");
			p.tab++;
			p.println("response.sendError(404, request.getRequestURI());");
			p.tab--;
			p.println("}");

			p.tab--;
			p.println("}");
			
			if(isAsyncController) {
				p.println("public boolean isAsyncAction(String actionName) {return true;}");
			} else {
				String str = p_isAsyncAction.toString();
				if(str.length()>0) {
					p.println("public boolean isAsyncAction(String actionName) {");
					p.tab++;
					p.print("if(actionName == null) actionName = \"").append(controller.defaultAction()).append("\";");
					p.println();
					p.printNoAlign(str);
					p.println();
					p.println("return false;");
					p.tab--;
					p.println("}");
				}
			}
			p.tab--;
			p.println("}");
			return this;
		}

		private boolean isFirstAction = true;

		@Override
		public ControllerElementVisitor visitExecutable(ExecutableElement e, Void v) {
			ElementKind kind = e.getKind();
			if (kind != ElementKind.METHOD)
				return this;

			Set<Modifier> modifiers = e.getModifiers();
			if (!modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.STATIC))
				return this;

			HttpMethod[] httpMethods = null;

			boolean isAsyncAction = !isAsyncController && e.getAnnotation(Async.class) != null;
			if(isAsyncAction) {
				p_isAsyncAction.print("if(actionName.equals(\"").append(e.getSimpleName()).append("\")) return true;");
				p_isAsyncAction.println();
			}

			Action action = e.getAnnotation(Action.class);
			if (action != null) {
				httpMethods = action.httpMethods();
			}
			if (httpMethods == null || httpMethods.length == 0) {
				httpMethods = controller.httpMethods();
			}
			StringBuilder methods = new StringBuilder("checkHttpMethods(");
			String sep = "";
			if (httpMethods != null && httpMethods.length > 0) {
				for (HttpMethod hm : httpMethods) {
					methods.append(sep).append("\"").append(hm.name()).append("\"");
					sep = ",";
				}
			}

			methods.append(");");
			if (isFirstAction) {
				p.print("if(actionName.equals(\"").append(e.getSimpleName()).append("\")) {");
				isFirstAction = false;
			} else {
				p.print("else if(actionName.equals(\"").append(e.getSimpleName()).append("\")) {");
			}
			p.println();
			p.tab++;
			p.println(methods);

			printMethodParameters(p, CONTROLLER_VAR_NAME, e, processingEnv, null);

			p.tab--;
			p.println("}");
			return this;
		}

		@Override
		public ControllerElementVisitor visitVariable(VariableElement e, Void v) {
			return this;
		}

		@Override
		public ControllerElementVisitor visitTypeParameter(TypeParameterElement e, Void v) {
			return this;
		}

		@Override
		public ControllerElementVisitor visitPackage(PackageElement e, Void v) {
			return this;
		}

		@Override
		protected ControllerElementVisitor defaultAction(Element e, Void v) {
			return this;
		}
	}

	private static class ModelElementVisitor extends SimpleElementVisitor7<ModelElementVisitor, Void> {
		private final ProcessingEnvironment processingEnv;
		private final Messager messager;
		private final PrettyPrinter p;
		private final String prefixModelVarName;
		private final String invokerModelVarName;

		public ModelElementVisitor(PrettyPrinter p, String prefixModelVarName, String invokerModelVarName,
				ProcessingEnvironment processingEnv) {
			this.processingEnv = processingEnv;
			this.messager = processingEnv.getMessager();
			this.p = p;
			this.prefixModelVarName = prefixModelVarName;
			this.invokerModelVarName = invokerModelVarName;
		}

		@Override
		public ModelElementVisitor visitType(TypeElement e, Void v) {
			// 带有@Model的类必需是public的
			if (!e.getModifiers().contains(Modifier.PUBLIC)) {
				messager.printMessage(javax.tools.Diagnostic.Kind.ERROR, "class with @Model must be 'public'", e);
				return this;
			}

			List<? extends Element> enclosedElements = e.getEnclosedElements();

			// 带有@Model的类必需有一个public的默认构造函数
			boolean find = false;
			for (Element element : enclosedElements) {
				if (element.getKind() == ElementKind.CONSTRUCTOR && element.getModifiers().contains(Modifier.PUBLIC)
						&& ((ExecutableElement) element).getParameters().size() == 0) {
					find = true;
					break;
				}
			}

			if (!find) {
				messager.printMessage(javax.tools.Diagnostic.Kind.ERROR,
						"class with @Model must contain a 'public' 0-parameter constructor", e);
				return this;
			}

			String qualifiedName = e.getQualifiedName().toString();
			p.print(qualifiedName).append(" ").append(invokerModelVarName).append(" = new ").append(qualifiedName)
					.append("();");
			p.println();

			for (Element element : enclosedElements) {
				if (element.getKind() == ElementKind.METHOD) {
					ExecutableElement method = (ExecutableElement) element;
					Set<Modifier> modifiers = method.getModifiers();
					if (!modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.STATIC))
						continue;

					if (method.getReturnType().getKind() != TypeKind.VOID)
						continue;

					if (method.getParameters().size() == 0)
						continue;

					if (!method.getSimpleName().toString().startsWith("set"))
						continue;

					printMethodParameters(p, invokerModelVarName, method, processingEnv, prefixModelVarName);
				}
			}
			return this;
		}

		@Override
		public ModelElementVisitor visitExecutable(ExecutableElement e, Void v) {
			return this;
		}

		@Override
		public ModelElementVisitor visitVariable(VariableElement e, Void v) {
			return this;
		}

		@Override
		public ModelElementVisitor visitTypeParameter(TypeParameterElement e, Void v) {
			return this;
		}

		@Override
		public ModelElementVisitor visitPackage(PackageElement e, Void v) {
			return this;
		}

		@Override
		protected ModelElementVisitor defaultAction(Element e, Void v) {
			return this;
		}
	}

	// TODO 是否考虑重构，做成ActionParameterType类那样吗?
	// 难点是下面每个if语句并没有统一规律，不好生成字符串。
	private static void printMethodParameters(PrettyPrinter p, String invoker, ExecutableElement e,
			ProcessingEnvironment processingEnv, String varPrefix) {
		List<? extends VariableElement> vars = e.getParameters();
		if (vars == null || vars.size() == 0) {
			p.print(invoker).append(".").append(e.getSimpleName()).append("();");
			p.println();
		} else {
			String sep = "";
			StringBuilder s = new StringBuilder();

			TypeMirror varType;
			String type;
			String name;

			for (VariableElement var : vars) {
				varType = var.asType();
				type = varType.toString();
				name = var.getSimpleName().toString();
				if (varPrefix != null)
					name = varPrefix + "." + name;

				if ("douyu.mvc.Context".equals(type) || "douyu.mvc.ModelManager".equals(type)
						|| "douyu.mvc.ViewManager".equals(type) || "douyu.mvc.ControllerManager".equals(type)) {
					s.append(sep).append("this");
				} else if ("douyu.http.HttpRequest".equals(type)) {
					s.append(sep).append("request");
				} else if ("douyu.http.HttpResponse".equals(type)) {
					s.append(sep).append("response");
				} else if ("java.io.PrintWriter".equals(type) || "java.io.Writer".equals(type)) {
					s.append(sep).append("response.getWriter()");
				} else if ("java.lang.String".equals(type)) {
					s.append(sep).append("request.getParameter(\"");
					s.append(name).append("\")");
				} else if ("int".equals(type)) {
					p.print("int ").append("v").append(varCount).append(" = 0;");
					p.println();
					p.print("p = request.getParameter(\"").append(name).append("\");");
					p.println();

					p.print("if(p != null) v").append(varCount).append(" = Integer.parseInt(p);");
					p.println();
					p.println();

					s.append(sep).append("v").append(varCount);
					varCount++;
				} else if ("long".equals(type)) {
					p.print("long ").append("v").append(varCount).append(" = 0L;");
					p.println();
					p.print("p = request.getParameter(\"").append(name).append("\");");
					p.println();

					p.print("if(p != null) v").append(varCount).append(" = Long.parseLong(p);");
					p.println();
					p.println();

					s.append(sep).append("v").append(varCount);
					varCount++;
				} else if ("float".equals(type)) {
					p.print("float ").append("v").append(varCount).append(" = 0.0F;");
					p.println();
					p.print("p = request.getParameter(\"").append(name).append("\");");
					p.println();

					p.print("if(p != null) v").append(varCount).append(" = Float.parseFloat(p);");
					p.println();
					p.println();

					s.append(sep).append("v").append(varCount);
					varCount++;
				} else if ("double".equals(type)) {
					p.print("double ").append("v").append(varCount).append(" = 0.0D;");
					p.println();
					p.print("p = request.getParameter(\"").append(name).append("\");");
					p.println();

					p.print("if(p != null) v").append(varCount).append(" = Double.parseDouble(p);");
					p.println();
					p.println();

					s.append(sep).append("v").append(varCount);
					varCount++;
				} else if ("boolean".equals(type)) {
					p.print("boolean ").append("v").append(varCount).append(" = false;");
					p.println();
					p.print("p = request.getParameter(\"").append(name).append("\");");
					p.println();

					p.print("if(p != null) v").append(varCount).append(" = Boolean.parseBoolean(p);");
					p.println();
					p.println();

					s.append(sep).append("v").append(varCount);
					varCount++;
				} else if ("byte".equals(type)) {
					p.print("byte ").append("v").append(varCount).append(" = 0;");
					p.println();
					p.print("p = request.getParameter(\"").append(name).append("\");");
					p.println();

					p.print("if(p != null) v").append(varCount).append(" = Byte.parseByte(p);");
					p.println();
					p.println();

					s.append(sep).append("v").append(varCount);
					varCount++;
				} else if ("short".equals(type)) {
					p.print("short ").append("v").append(varCount).append(" = 0;");
					p.println();
					p.print("p = request.getParameter(\"").append(name).append("\");");
					p.println();

					p.print("if(p != null) v").append(varCount).append(" = Short.parseShort(p);");
					p.println();
					p.println();

					s.append(sep).append("v").append(varCount);
					varCount++;
				} else if ("char".equals(type)) {
					p.print("char ").append("v").append(varCount).append(" = 0;");
					p.println();

					p.print("p = request.getParameter(\"").append(name).append("\");");
					p.println();

					p.print("if(p != null && p.length() > 0) v").append(varCount).append(" = p.charAt(0);");
					p.println();
					p.println();

					s.append(sep).append("v").append(varCount);
					varCount++;
				} else if ("java.lang.Integer".equals(type) || "java.lang.Long".equals(type)
						|| "java.lang.Float".equals(type) || "java.lang.Double".equals(type)
						|| "java.lang.Boolean".equals(type) || "java.lang.Byte".equals(type)
						|| "java.lang.Short".equals(type)) {

					type = type.substring(type.lastIndexOf('.') + 1);

					p.print(type).append(" v").append(varCount).append(" = null;");
					p.println();

					p.print("p = request.getParameter(\"").append(name).append("\");");
					p.println();

					p.print("if(p != null) v").append(varCount).append(" = ").append(type).append(".valueOf(p);");
					p.println();
					p.println();

					s.append(sep).append("v").append(varCount);
					varCount++;
				} else if ("java.lang.Character".equals(type)) {
					p.print("Character v").append(varCount).append(" = null;");
					p.println();

					p.print("p = request.getParameter(\"").append(name).append("\");");
					p.println();

					p.print("if(p != null && p.length() > 0) v").append(varCount).append(" = p.charAt(0);");
					p.println();
					p.println();

					s.append(sep).append("v").append(varCount);
					varCount++;
				} else if (varType.getKind() == TypeKind.ARRAY
						&& "java.lang.String".equals(((ArrayType) varType).getComponentType().toString())) {

					s.append(sep).append("request.getParameterValues(\"").append(name).append("\")");

				} else if ("douyu.http.UploadedFile".equals(type)) {
					s.append(sep).append("request.getUploadedFile(\"").append(name).append("\")");

				} else if (varType.getKind() == TypeKind.ARRAY
						&& "douyu.http.UploadedFile".equals(((ArrayType) varType).getComponentType().toString())) {

					s.append(sep).append("request.getUploadedFiles()");
				} else {
					// 只注入带有@Model的类，其他的都注入null
					if (varType.getKind() == TypeKind.DECLARED) {
						Element element = ((DeclaredType) varType).asElement();

						if (element.getAnnotation(Model.class) != null) {
							String invokerModelVarName;

							// 防止循环setXXX
							// 如A.setXXX(B b), B.setYYY(A a)
							// 出现这种情况时，B直接用A的引用
							int index = modelElementStack.indexOf(element);
							if (index != -1) {
								invokerModelVarName = modelVarNameStack.get(index);
							} else {
								invokerModelVarName = "v" + varCount;
								varCount++;

								modelElementStack.push(element);
								modelVarNameStack.push(invokerModelVarName);

								try {
									new ModelElementVisitor(p, name, invokerModelVarName, processingEnv).visit(element);
								} finally {
									modelElementStack.pop();
									modelVarNameStack.pop();
								}
							}
							s.append(sep).append(invokerModelVarName);
						} else {
							s.append(sep).append("null");
						}
						// TODO 支持Model数组
						// } else if (varType.getKind() == TypeKind.ARRAY
						// && ((ArrayType) varType).getComponentType().getKind()
						// == TypeKind.DECLARED) {
						// Element element = ((DeclaredType) ((ArrayType)
						// varType).getComponentType()).asElement();
					} else {
						s.append(sep).append("null");
					}
				}

				sep = ",";
			}
			p.print(invoker).append(".").append(e.getSimpleName()).append("(").append(s).append(");");
			p.println();
		}
	}

	private static Stack<Element> modelElementStack = new Stack<Element>();
	private static Stack<String> modelVarNameStack = new Stack<String>();
}
