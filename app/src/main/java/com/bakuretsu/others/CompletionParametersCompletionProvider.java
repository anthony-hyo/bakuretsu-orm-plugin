package com.bakuretsu.others;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CompletionParametersCompletionProvider extends CompletionProvider<CompletionParameters> {  //REFERENCE LIST

	@Override
	protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
		PsiElement element = parameters.getPosition();

		PsiMethodCallExpression withCall = getParentMethodCall(element);
		if (withCall == null) {
			return;
		}

		PsiMethodCallExpression queryCall = OrmRelationReference.findQueryCall(withCall);
		if (queryCall == null) {
			return;
		}

		PsiClass modelClass = OrmRelationReference.extractModelClass(queryCall);
		if (modelClass == null) {
			return;
		}

		addRelationSuggestions(result, modelClass);
	}

	private PsiMethodCallExpression getParentMethodCall(PsiElement element) {
		PsiElement parent = element.getParent();

		if (parent == null) {
			return null;
		}

		parent = parent.getParent();

		if (parent == null) {
			return null;
		}

		parent = parent.getParent();

		if (parent instanceof PsiMethodCallExpression) {
			return (PsiMethodCallExpression) parent;
		}

		return null;
	}

	private void addRelationSuggestions(CompletionResultSet result, PsiClass modelClass) {
		Collection<String> suggestions = collectRelations(modelClass);

		for (String relation : suggestions) {
			result.addElement(LookupElementBuilder.create(relation).withPresentableText(relation).withIcon(modelClass.getIcon(0)).withTypeText("Relation", true).withBoldness(true));
		}
	}

	/*private Collection<String> collectRelations(PsiClass modelClass) {
		Set<String> relations = new LinkedHashSet<>();
		Map<String, PsiClass> visited = new HashMap<>();
		collectRelationsRecursive(modelClass, "", relations, visited, 0, modelClass);
		return relations;
	}*/

	private Collection<String> collectRelations(PsiClass modelClass) {
		Set<String> relations = new LinkedHashSet<>();

		// Use a map to record visited paths (using a joined string as key)
		Map<String, PsiClass> visited = new HashMap<>();

		// Start with an empty chain
		List<String> currentChain = new ArrayList<>();

		collectRelationsRecursive(modelClass, currentChain, relations, visited, 0, modelClass);
		return relations;
	}

	private void collectRelationsRecursive(PsiClass currentClass, List<String> currentChain, Set<String> relations, Map<String, PsiClass> visited, int depth, PsiClass initialClass) {
		if (depth > 10 || currentClass == null) {
			return;
		}

		// Create a unique key for the current chain
		String pathKey = String.join(".", currentChain);

		if (visited.containsKey(pathKey)) {
			return;
		}

		visited.put(pathKey, currentClass);

		for (PsiField field : currentClass.getFields()) {
			if (!hasRelationAnnotation(field)) {
				continue;
			}

			String relationName = field.getName();

			// If this relation name is already in the current chain, skip it to avoid repetition
			if (currentChain.contains(relationName)) {
				continue;
			}

			// Build a new chain for this branch
			List<String> newChain = new ArrayList<>(currentChain);
			newChain.add(relationName);
			String fullPath = String.join(".", newChain);
			relations.add(fullPath);

			PsiClass relatedClass = getRelatedClass(field);
			if (relatedClass != null) {

				// If the related class is the initial class, don't recurse further down this branch.
				if (relatedClass.equals(initialClass)) {
					continue;
				}

				collectRelationsRecursive(relatedClass, newChain, relations, visited, depth + 1, initialClass);
			}
		}
	}

	/*private void collectRelationsRecursive(PsiClass currentClass, String currentPath, Set<String> relations, Map<String, PsiClass> visited, int depth, PsiClass initialClass) {
		if (depth > 10 || currentClass == null || visited.containsKey(currentPath)) {
			return;
		}

		visited.put(currentPath, currentClass);

		for (PsiField field : currentClass.getFields()) {
			if (!hasRelationAnnotation(field)) {
				continue;
			}

			PsiClass relatedClass = getRelatedClass(field);

			if (relatedClass != null && relatedClass.equals(initialClass)) {
				continue;
			}

			String relationName = field.getName();
			String fullPath = currentPath.isEmpty() ? relationName : currentPath + "." + relationName;
			relations.add(fullPath);

			if (relatedClass != null) {
				collectRelationsRecursive(relatedClass, fullPath, relations, visited, depth + 1, initialClass);
			}
		}
	}*/

	/*private void collectRelationsRecursive(PsiClass currentClass, String currentPath, Set<String> relations, Map<String, PsiClass> visited, int depth, PsiClass initialClass) {
		if (depth > 5 || currentClass == null || visited.containsKey(currentPath)) {
			return;
		}

		visited.put(currentPath, currentClass);

		for (PsiField field : currentClass.getFields()) {
			if (!hasRelationAnnotation(field)) {
				continue;
			}

			String relationName = field.getName();
			String fullPath = currentPath.isEmpty() ? relationName : currentPath + "." + relationName;
			relations.add(fullPath);

			PsiClass relatedClass = getRelatedClass(field);

			if (relatedClass != null) {
				if (relatedClass.equals(initialClass)) {
					break;
				}

				collectRelationsRecursive(relatedClass, fullPath, relations, visited, depth + 1, initialClass);
			}
		}
	}*/

	private boolean hasRelationAnnotation(PsiField field) {
		return Arrays.stream(field.getAnnotations()).map(PsiAnnotation::getQualifiedName).anyMatch(name -> name != null && name.endsWith("Relation"));
	}

	private PsiClass getRelatedClass(PsiField field) {
		PsiType fieldType = field.getType();

		if (!(fieldType instanceof PsiClassType classType)) {
			return null;
		}

		if ("List".equals(classType.getClassName())) {
			PsiType[] parameters = classType.getParameters();
			if (parameters.length > 0 && parameters[0] instanceof PsiClassType) {
				return ((PsiClassType) parameters[0]).resolve();
			}
		} else {
			return classType.resolve();
		}

		return null;
	}

}