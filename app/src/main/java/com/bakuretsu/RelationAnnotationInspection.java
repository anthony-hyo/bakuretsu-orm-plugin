package com.bakuretsu;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * This inspection validates that for a @Relation annotation:
 * - The 'localKey' exists as a field (or getter) in the current class.
 * - The 'foreignKey' exists as a field (or getter) in the related class.
 */
public class RelationAnnotationInspection extends LocalInspectionTool {


	@Override
	public boolean runForWholeFile() {
		return true;
	}

	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder,
	                                               boolean isOnTheFly) {
		return new JavaElementVisitor() {

			@Override
			public void visitAnnotation(PsiAnnotation annotation) {
				// Check if the annotation is our @Relation annotation.
				// You may want to check the fully qualified name if you know it.
				String annotationName = annotation.getQualifiedName();
				if (annotationName == null || !annotationName.endsWith("Relation")) {
					return;
				}

				// Get the attribute values from the annotation.
				PsiAnnotationMemberValue localKeyValue = annotation.findAttributeValue("localKey");
				PsiAnnotationMemberValue foreignKeyValue = annotation.findAttributeValue("foreignKey");
				PsiAnnotationMemberValue relatedValue = annotation.findAttributeValue("related");

				// If any attribute is missing, exit.
				if (localKeyValue == null || foreignKeyValue == null || relatedValue == null) {
					return;
				}

				// Extract string values for localKey and foreignKey.
				String localKey = extractStringValue(localKeyValue);
				String foreignKey = extractStringValue(foreignKeyValue);
				if (localKey == null || foreignKey == null) {
					return;
				}

				// Get the current class (the class where the annotation is declared).
				PsiClass containingClass = PsiTreeUtil.getParentOfType(annotation, PsiClass.class);
				if (containingClass == null) {
					return;
				}

				// Validate that the current class contains a field or method matching 'localKey'.
				if (!hasFieldOrMethod(containingClass, localKey)) {
					holder.registerProblem(localKeyValue, "Local key '" + localKey +
						"' does not exist in class '" + containingClass.getName() + "'.");
				}

				// Resolve the related class from the class literal in the 'related' attribute.
				PsiClass relatedClass = resolveClassFromClassLiteral(relatedValue);
				if (relatedClass == null) {
					holder.registerProblem(relatedValue, "Unable to resolve related class.");
					return;
				}

				// Validate that the related class contains a field or method matching 'foreignKey'.
				if (!hasFieldOrMethod(relatedClass, foreignKey)) {
					holder.registerProblem(foreignKeyValue, "Foreign key '" + foreignKey +
						"' does not exist in related class '" + relatedClass.getName() + "'.");
				}
			}
		};
	}

	/**
	 * Extracts a string value from an annotation attribute.
	 */
	private String extractStringValue(PsiAnnotationMemberValue value) {
		if (value instanceof PsiLiteralExpression) {
			Object literal = ((PsiLiteralExpression) value).getValue();
			if (literal instanceof String) {
				return (String) literal;
			}
		}
		return null;
	}

	/**
	 * Checks if the given class has a field or a getter method for the provided key.
	 */
	private boolean hasFieldOrMethod(PsiClass psiClass, String key) {
		// Check for a field with the given name.
		if (psiClass.findFieldByName(key, true) != null) {
			return true;
		}
		// Optionally, check for a getter method.
		String getterName = "get" + capitalize(key);
		for (PsiMethod method : psiClass.getMethods()) {
			if (getterName.equals(method.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Capitalizes the first letter of the given string.
	 */
	private String capitalize(String s) {
		if (s == null || s.isEmpty()) return s;
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	/**
	 * Resolves a PsiClass from a class literal (e.g. MapNpc.class).
	 */
	private PsiClass resolveClassFromClassLiteral(PsiAnnotationMemberValue value) {
		if (value instanceof PsiClassObjectAccessExpression) {
			PsiTypeElement operand = ((PsiClassObjectAccessExpression) value).getOperand();
			PsiType type = operand.getType();
			if (type instanceof PsiClassType) {
				return ((PsiClassType) type).resolve();
			}
		}
		return null;
	}
}
