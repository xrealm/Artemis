package com.artemis.asmdemo;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by xrealm on 2020/8/13.
 */
public class ADCalssVisitor extends ClassVisitor {

    private boolean weavingAllMethod;

    public ADCalssVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM7, classVisitor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.endsWith("Lcom/artemis/asmdemo/DebugTrace;")) {
            weavingAllMethod = true;
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        MethodVisitor methodVisitor =  super.visitMethod(access, name, descriptor, signature, exceptions);
        return new MethodAdapter(methodVisitor, access, name, descriptor, weavingAllMethod);
    }
}
