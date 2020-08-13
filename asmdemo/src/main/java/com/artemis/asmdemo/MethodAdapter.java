package com.artemis.asmdemo;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Created by xrealm on 2020/8/13.
 */
public class MethodAdapter extends AdviceAdapter {

    private String methodName;
    private boolean weavingMethod;

    /**
     * Constructs a new {@link AdviceAdapter}.
     *
     * @param methodVisitor the method visitor to which this adapter delegates calls.
     * @param access        the method's access flags (see {@link Opcodes}).
     * @param name          the method's name.
     * @param descriptor    the method's descriptor (see {@link Type Type}).
     */
    protected MethodAdapter( MethodVisitor methodVisitor, int access, String name, String descriptor, boolean weavingMethod) {
        super(Opcodes.ASM7, methodVisitor, access, name, descriptor);
        this.methodName = name;
        this.weavingMethod = weavingMethod;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (descriptor.endsWith("Lcom/artemis/asmdemo/DebugTrace;")) {
            weavingMethod = true;
        }
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        if (weavingMethod) {
            visitLdcInsn(methodName);
            visitMethodInsn(Opcodes.INVOKESTATIC, "com/artemis/asmdemo/.MethodDebugTrace", "begin", "(Ljava/lang/String;)V", false);
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        if (weavingMethod) {
            visitLdcInsn(methodName);
            visitMethodInsn(Opcodes.INVOKESTATIC, "com/artemis/asmdemo/.MethodDebugTrace", "end", "(Ljava/lang/String;)V", false);
        }
    }
}
