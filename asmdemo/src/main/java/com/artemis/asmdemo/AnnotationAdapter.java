package com.artemis.asmdemo;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by mocmao on 2020/9/2.
 */
public class AnnotationAdapter extends AnnotationVisitor {

    private static final String TAG = "TAG";

    private AnnotationCallback annotationCallback;

    public AnnotationAdapter(AnnotationVisitor annotationVisitor, AnnotationCallback annotationCallback) {
        super(Opcodes.ASM7, annotationVisitor);
        this.annotationCallback = annotationCallback;
    }

    @Override
    public void visit(String name, Object value) {
        if (annotationCallback != null) {
            annotationCallback.onAnnotation(name, value);
        }
        super.visit(name, value);
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        super.visitEnum(name, descriptor, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        return super.visitAnnotation(name, descriptor);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return super.visitArray(name);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
