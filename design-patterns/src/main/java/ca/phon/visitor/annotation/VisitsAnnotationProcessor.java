package ca.phon.visitor.annotation;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

/**
 * <p>Annotation processor for the {@link Visits} annotation.
 * This processor ensures that the following holds true:
 * <ul>
 * <li>The class which using the {@link Visits} annotation is a 
 * sub-class for {@link VisitorAdapter}</li>
 * <li>The method has one argument of the type declared by the
 * {@link Visits} annotation</li>
 * <li>The object declarsed visitable by the annotation is a sub-class
 * of the parameterized type of {@link VisitorAdapter}</li>
 * </ul>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({"ca.phon.visitor.annotation.Visits"})
public class VisitsAnnotationProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		Set<? extends Element> visitsElement = 
				roundEnv.getElementsAnnotatedWith(Visits.class);
		for(Element ele:visitsElement) {
			if(ele.getKind() != ElementKind.METHOD) {
				processingEnv.getMessager().printMessage(Kind.ERROR, 
						"@Visits can only be used on methods.", ele);
			}
			checkType(ele);
		}
		return false;
	}
	
	/**
	 * Check enclosing type to make sure it is derived from
	 * VisitorAdapter
	 */
	private void checkType(Element ele) {
		Types typeUtils = processingEnv.getTypeUtils();
		Elements eleUtils = processingEnv.getElementUtils();
		
		Element parentType = ele.getEnclosingElement();
		
		// check enclosing type
		TypeElement typeElement = 
				TypeElement.class.cast(parentType);
		
		if(!typeUtils.isSubtype(parentType.asType(), eleUtils.getTypeElement("ca.phon.visitor.VisitorAdapter").asType())) {
			processingEnv.getMessager().printMessage(Kind.ERROR,
					"@Visits must only be used in classes derived from ca.phon.visitor.VisitorAdapter",
					typeElement);
		}
		
		TypeMirror superClassMirror = typeElement.getSuperclass();
		DeclaredType decTypeMirror = DeclaredType.class.cast(superClassMirror);
		TypeMirror paramSupertypeMirror = decTypeMirror.getTypeArguments().get(0);
		
		ExecutableType methodType = ExecutableType.class.cast(ele.asType());
		if(methodType.getParameterTypes().size() != 1) {
			processingEnv.getMessager().printMessage(Kind.ERROR, 
					"Method " + ele.getSimpleName() + " must declare one paramater of type " + paramSupertypeMirror.toString(),
					ele);
		} else {
			TypeMirror methodParamType = methodType.getParameterTypes().get(0);
			if(!typeUtils.isSubtype(methodParamType, paramSupertypeMirror)) {
				processingEnv.getMessager().printMessage(Kind.ERROR, 
						"Method " + ele.getSimpleName() + " must declare one paramater of type " + paramSupertypeMirror.toString(),
						ele);
			}
		}
	}
	
}
