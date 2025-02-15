package com.bakuretsu;


import com.bakuretsu.others.OpenModelQuickFix;
import com.bakuretsu.others.OrmRelationReference;
import com.bakuretsu.others.Util;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.ex.ProblemDescriptorImpl;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BakuretsuRelationInspection extends AbstractBaseJavaLocalInspectionTool {

	@Override
	public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
		return new JavaElementVisitor() {
			@Override
			public void visitMethodCallExpression(PsiMethodCallExpression expression) {
				super.visitMethodCallExpression(expression);

				String methodName = expression.getMethodExpression().getReferenceName();

				if (!"with".equals(methodName)) {
					return;
				}

				PsiExpression[] arguments = expression.getArgumentList().getExpressions();

				if (arguments.length == 0 || !(arguments[0] instanceof PsiLiteralExpression argument)) {
					return;
				}

				if (!(argument.getValue() instanceof String relationPath)) {
					return;
				}

				PsiMethodCallExpression queryCall = OrmRelationReference.findQueryCall(expression);

				if (queryCall == null) {
					return;
				}

				PsiClass modelClass = OrmRelationReference.extractModelClass(queryCall);

				if (modelClass == null) {
					return;
				}

				validateRelationPath(holder, argument, relationPath, modelClass);
			}
		};
	}

	private void validateRelationPath(ProblemsHolder holder, PsiLiteralExpression argument, String relationPath, PsiClass modelClass) {
		String[] relationParts = relationPath.split("\\.");

		PsiClass currentModelClass = modelClass;

		for (String relationPart : relationParts) {
			if (currentModelClass == null) {
				invalidRelation(holder, argument, relationPath, modelClass, relationPart);
				continue;
			}

			PsiField field = Arrays.stream(currentModelClass.getFields())
				.filter(psiField ->
					relationPart.equals(psiField.getName()) && Arrays.stream(psiField.getAnnotations())
						.map(PsiAnnotation::getQualifiedName)
						.anyMatch(name -> name != null && name.endsWith("Relation"))
				)
				.findFirst()
				.orElse(null);

			if (field == null) {
				invalidRelation(holder, argument, relationPath, modelClass, relationPart);
				continue;
			}

			PsiType fieldType = field.getType();

			if (fieldType instanceof PsiClassType) {
				currentModelClass = Util.findRelation(currentModelClass, (PsiClassType) fieldType);
			}
		}
	}

	private void invalidRelation(ProblemsHolder holder, PsiLiteralExpression argument, String relationPath, PsiClass modelClass, String relationPart) {
		int offset = relationPath.indexOf(relationPart) + 1;
		int length = relationPart.length();

		TextRange range = new TextRange(offset, offset + length);

		ProblemDescriptorImpl problemDescriptor = new ProblemDescriptorImpl(
			argument,
			argument,
			"Relation '" + relationPart + "' not found in " + modelClass.getName(),
			new LocalQuickFix[] {new OpenModelQuickFix(relationPart, modelClass)},
			ProblemHighlightType.ERROR,
			false,
			range,
			true
		);

		holder.registerProblem(problemDescriptor);
	}

	@Override
	public @NotNull String getGroupDisplayName() {
		return "ORM";
	}

	@Override
	public @NotNull String getDisplayName() {
		return "A plugin to help development and usage of Bakuretsu.";
	}

	@Override
	public @NotNull String getShortName() {
		return "BakuretsuRelationInspection";
	}

	@Override
	public boolean isEnabledByDefault() {
		return true;
	}

}