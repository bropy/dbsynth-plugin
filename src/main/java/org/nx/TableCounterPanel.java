package org.nx;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TableCounterPanel extends JPanel {
    private final Project project;
    private final JLabel tableCountLabel;
    private JButton refreshButton;

    public TableCounterPanel(Project project) {
        this.project = project;

        setLayout(new BorderLayout());

        refreshButton = new JButton("Refresh Table Count");
        refreshButton.addActionListener(e -> updateTableCount());
        add(refreshButton, BorderLayout.NORTH);

        tableCountLabel = new JLabel("Calculating tables...");
        add(tableCountLabel, BorderLayout.CENTER);

        updateTableCount();
    }

    private void updateTableCount() {
        SwingWorker<List<PsiClass>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PsiClass> doInBackground() {
                return findEntityClasses(project);
            }

            @Override
            protected void done() {
                try {
                    List<PsiClass> entityClasses = get();

                    String labelText = String.format("Tables in project: %d", entityClasses.size());

                    if (!entityClasses.isEmpty()) {
                        String classNames = entityClasses.stream()
                                .map(PsiClass::getName)
                                .collect(Collectors.joining(", ", " (", ")"));
                        labelText += classNames;
                    }

                    tableCountLabel.setText(labelText);
                } catch (Exception e) {
                    tableCountLabel.setText("Error calculating tables: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private List<PsiClass> findEntityClasses(Project project) {
        List<PsiClass> entityClasses = new ArrayList<>();

        // Find all Java files in the project
        Collection<VirtualFile> javaFiles = FileTypeIndex.getFiles(
                JavaFileType.INSTANCE,
                GlobalSearchScope.projectScope(project)
        );

        // Iterate over all Java files
        for (VirtualFile javaFile : javaFiles) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(javaFile);
            if (psiFile instanceof PsiJavaFile psiJavaFile) {
                // Find all classes in the Java file
                for (PsiClass psiClass : psiJavaFile.getClasses()) {
                    if (hasEntityAnnotation(psiClass)) {
                        entityClasses.add(psiClass);
                    }
                }
            }
        }

        return entityClasses;
    }

    private boolean hasEntityAnnotation(PsiClass psiClass) {
        if (psiClass == null || psiClass.getModifierList() == null) {
            return false;
        }

        // Direct search for Entity annotation
        PsiAnnotation[] annotations = psiClass.getModifierList().getAnnotations();

        for (PsiAnnotation annotation : annotations) {
            // Check the simple name and full qualified name to cover different import scenarios
            String annotationName = annotation.getQualifiedName();
            if (annotationName != null && (
                    annotationName.equals("jakarta.persistence.Entity") ||
                            annotationName.equals("javax.persistence.Entity") ||
                            annotation.getNameReferenceElement() != null &&
                                    "Entity".equals(annotation.getNameReferenceElement().getText())
            )) {
                // Additional check to ensure it's not in a comment
                return !isCommented(annotation);
            }
        }

        return false;
    }

    private boolean isCommented(PsiElement element) {
        PsiElement parent = element.getParent();
        while (parent != null) {
            if (parent instanceof PsiComment) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
}