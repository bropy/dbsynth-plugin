package org.nx;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class TableCounterPanel extends JPanel {
    private JLabel tableCountLabel;

    public TableCounterPanel(Project project) {
        setLayout(new BorderLayout());

        tableCountLabel = new JLabel("Calculating tables...");
        add(tableCountLabel, BorderLayout.CENTER);

        // Пошук JPA Entity класів
        List<PsiClass> entityClasses = findEntityClasses(project);

        tableCountLabel.setText("Tables in project: " + entityClasses.size());
    }

    private List<PsiClass> findEntityClasses(Project project) {
        List<PsiClass> entityClasses = new ArrayList<>();

        // Варіанти імпорту Entity
        List<String> entityAnnotations = Arrays.asList(
                "javax.persistence.Entity",
                "jakarta.persistence.Entity"
        );

        // Знаходження всіх класів у проекті
        PsiFile projectFile = PsiManager.getInstance(project).findFile(project.getProjectFile());
        if (projectFile != null) {
            // Знаходження всіх класів у файлі
            List<PsiClass> allClasses = PsiTreeUtil.getChildrenOfTypeAsList(projectFile, PsiClass.class);

            // Фільтрація класів з Entity анотаціями
            for (PsiClass psiClass : allClasses) {
                for (String annotation : entityAnnotations) {
                    if (psiClass.hasAnnotation(annotation)) {
                        entityClasses.add(psiClass);
                        break;
                    }
                }
            }
        }

        return entityClasses;
    }
}