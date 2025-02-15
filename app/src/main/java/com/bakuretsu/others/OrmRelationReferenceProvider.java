package com.bakuretsu.others;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrmRelationReferenceProvider extends PsiReferenceProvider {
	@Override
	public @NotNull PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
		if (!(element instanceof PsiLiteralExpression literalExpression)) {
			return PsiReference.EMPTY_ARRAY;
		}

		Object value = literalExpression.getValue();

		if (!(value instanceof String relationName)) {
			return PsiReference.EMPTY_ARRAY;
		}

		List<String> parts = Arrays.asList(relationName.split("\\."));
		String elementText = element.getText();

		String content = elementText.startsWith("\"") && elementText.endsWith("\"") ? elementText.substring(1, elementText.length() - 1) : elementText;

		List<TextRange> ranges = new ArrayList<>();
		int partStart = 0;

		for (int i = 0; i <= content.length(); i++) {
			if (i == content.length() || content.charAt(i) == '.') {
				int startOffset = elementText.indexOf(content) + partStart;
				int endOffset = startOffset + (i - partStart);
				ranges.add(TextRange.create(startOffset, endOffset));
				partStart = i + 1;
			}
		}

		List<PsiReference> references = new ArrayList<>();

		for (int i = 0; i < parts.size() && i < ranges.size(); i++) {
			TextRange range = ranges.get(i);
			List<String> currentChain = parts.subList(0, i + 1);
			references.add(new OrmRelationReference(literalExpression, currentChain, range));
		}

		return references.toArray(new PsiReference[0]);
	}
}
