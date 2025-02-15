package com.bakuretsu.others;

import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

public class Util {

	@Nullable
	public static PsiClass findRelation(PsiClass currentModelClass, PsiClassType fieldType) {
		PsiClassType classType = fieldType;

		if (classType.getClassName().equals("List")) {
			PsiType[] parameters = classType.getParameters();
			if (parameters.length > 0 && parameters[0] instanceof PsiClassType) {
				currentModelClass = ((PsiClassType) parameters[0]).resolve();
			}
		} else {
			currentModelClass = classType.resolve();
		}

		return currentModelClass;
	}

	public static void show(Project project, String message, String title, NotificationType notificationType, String groupDisplayId, NotificationAction notificationAction) {
		if (message == null || message.trim().isEmpty()) {
			message = "[empty message]";
		}

		NotificationGroup notificationGroup = new NotificationGroup(groupDisplayId.isEmpty() ? "MyPluginNotificationGroup" : groupDisplayId, NotificationDisplayType.BALLOON, true);

		Notification notification = notificationGroup.createNotification(title, message, notificationType);

		if (notificationAction != null) {
			notification.addAction(notificationAction);
		}

		ApplicationManager.getApplication().invokeLater(() -> {
			notification.notify(project);
		});
	}

}
