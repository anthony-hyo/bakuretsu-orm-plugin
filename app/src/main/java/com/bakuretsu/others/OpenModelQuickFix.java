package com.bakuretsu.others;


import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class OpenModelQuickFix implements LocalQuickFix {

	private final String relation;

	@SafeFieldForPreview
	private final PsiClass modelClass;

	public OpenModelQuickFix(String relation, PsiClass modelClass) {
		this.relation = relation;
		this.modelClass = modelClass;
	}

	@Override
	public @NotNull String getFamilyName() {
		return "Open Model '" + modelClass.getName() + "'";
	}

	@Override
	public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
		PsiField field = Arrays.stream(modelClass.getFields()).filter(f -> relation.equals(f.getName())).findFirst().orElse(null);

		if (field == null) {
			modelClass.navigate(true);
		} else {
			field.navigate(true);
		}
	}

}