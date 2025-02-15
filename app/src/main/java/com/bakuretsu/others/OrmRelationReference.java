package com.bakuretsu.others;


import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class OrmRelationReference extends PsiReferenceBase<PsiLiteralExpression> { //REFERENCE CLICK

	private final List<String> currentChain;

	public OrmRelationReference(@NotNull PsiLiteralExpression element, List<String> currentChain, TextRange range) {
		super(element, range);
		this.currentChain = currentChain;
	}

	@Override
	public @Nullable PsiElement resolve() {
		PsiMethodCallExpression methodCall = findSurroundingMethodCall(getElement(), "with");

		if (methodCall == null) {
			return null;
		}

		PsiMethodCallExpression queryCall = findQueryCall(methodCall);

		if (queryCall == null) {
			return null;
		}

		PsiClass modelClass = extractModelClass(queryCall);

		if (modelClass == null) {
			return null;
		}

		PsiClass currentModelClass = modelClass;
		PsiField resolvedField = null;

		for (String part : currentChain) {
			if (currentModelClass == null) {
				return null;
			}

			PsiField field = findFieldInClass(currentModelClass, part);

			if (field == null || !isRelationField(field)) {
				return null;
			}

			resolvedField = field;
			PsiType fieldType = field.getType();

			if (fieldType instanceof PsiClassType) {
				currentModelClass = Util.findRelation(currentModelClass, (PsiClassType) fieldType);
			} else {
				return null;
			}
		}

		return resolvedField;
	}

	private static PsiField findFieldInClass(PsiClass psiClass, String fieldName) {
		return Arrays.stream(psiClass.getFields()).filter(field -> fieldName.equals(field.getName())).findFirst().orElse(null);
	}

	private static PsiMethodCallExpression findSurroundingMethodCall(PsiElement element, String methodName) {
		PsiElement parent = element.getParent();
		while (parent != null) {
			if (parent instanceof PsiMethodCallExpression methodCall) {
				if (methodName.equals(methodCall.getMethodExpression().getReferenceName())) {
					return methodCall;
				}
			}
			parent = parent.getParent();
		}
		return null;
	}

	public static @Nullable PsiMethodCallExpression findQueryCall(PsiMethodCallExpression expression) {
		PsiMethodCallExpression current = expression;
		while (current != null) {
			PsiExpression qualifier = current.getMethodExpression().getQualifierExpression();
			if (qualifier instanceof PsiMethodCallExpression qualifierCall) {
				if ("query".equals(qualifierCall.getMethodExpression().getReferenceName())) {
					return qualifierCall;
				}
				current = qualifierCall;
			} else {
				break;
			}
		}
		return null;
	}

	public static @Nullable PsiClass extractModelClass(PsiMethodCallExpression queryCall) {
		PsiExpression[] args = queryCall.getArgumentList().getExpressions();
		if (args.length == 0 || !(args[0] instanceof PsiClassObjectAccessExpression classAccess)) {
			return null;
		}
		PsiType type = classAccess.getOperand().getType();
		return type instanceof PsiClassType ? ((PsiClassType) type).resolve() : null;
	}

	private static boolean isRelationField(PsiField field) {
		return Arrays.stream(field.getAnnotations()).map(PsiAnnotation::getQualifiedName).anyMatch(name -> name != null && name.endsWith("Relation"));
	}
}