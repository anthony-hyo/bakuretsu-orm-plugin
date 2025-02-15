package com.bakuretsu;


import com.bakuretsu.others.OrmRelationReferenceProvider;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

public class BakuretsuReferenceContributor extends PsiReferenceContributor { //REFERENCE CLICK

	@Override
	public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
		registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiLiteralExpression.class), new OrmRelationReferenceProvider());
	}

}