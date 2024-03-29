
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes("CheckGetter")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CheckGetterProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {

            if (!"CheckGetter".equals(annotation.getQualifiedName().toString())) {
                continue;
            }

            for (TypeElement annotatedClass : ElementFilter.typesIn(roundEnv.getRootElements())) {
                for (VariableElement field : ElementFilter.fieldsIn(annotatedClass.getEnclosedElements())) {
                    if (containCheckGetter(annotation, field) && !containsGetter(annotatedClass, field.getSimpleName().toString())) {
                        processingEnv.getMessager().printMessage(Kind.ERROR,
                                String.format("getter not found for '%s.%s'.", annotatedClass.getSimpleName(), field.getSimpleName()));
                    }
                }
            }

            for (TypeElement annotatedClass : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(CheckGetter.class))) {
                for (VariableElement field : ElementFilter.fieldsIn(annotatedClass.getEnclosedElements())) {
                    if (!containsGetter(annotatedClass, field.getSimpleName().toString())) {
                        processingEnv.getMessager().printMessage(Kind.ERROR,
                                String.format("getter not found for '%s.%s'.", annotatedClass.getSimpleName(), field.getSimpleName()));
                    }
                }
            }
        }
        return true;
    }

    private static boolean containsGetter(TypeElement typeElement, String name) {
        String getter = "get" + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (!executableElement.getModifiers().contains(Modifier.STATIC)
                    && executableElement.getSimpleName().toString().equals(getter)
                    && executableElement.getParameters().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean containCheckGetter(TypeElement annotation, VariableElement variableElement) {
        TypeMirror annotationType = annotation.asType();
        for (AnnotationMirror annotationMirror : variableElement.getAnnotationMirrors()) {
            if (processingEnv.getTypeUtils().isSameType(annotationMirror.getAnnotationType(), annotationType))
                return true;
        }
        return false;
    }
}
