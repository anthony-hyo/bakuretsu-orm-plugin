package com.bakuretsu;

import com.bakuretsu.others.CompletionParametersCompletionProvider;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;


public class BakuretsuRelationCompletionContributor extends CompletionContributor { //REFERENCE LIST

	public BakuretsuRelationCompletionContributor() {
		extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionParametersCompletionProvider());
	}

}
