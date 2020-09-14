package com.artemis.asmdemo;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by xrealm on 2020/8/13.
 */
public class MethodAdapter extends AdviceAdapter {

    private String methodName;
    private boolean weavingMethod;

    private String targetClassName;
    private String targetMethodName;

    private AnnotationCallback annotationCallback = new AnnotationCallback() {
        @Override
        public void onAnnotation(String name, Object value) {
            System.out.println(name+value);
            if ("targetClass".equals(name)) {
                targetClassName = ((Type) value).getInternalName();
            } else if ("methodName".equals(name)) {
                targetMethodName = (String) value;
            }
        }
    };

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
        System.out.println(descriptor);
        if (descriptor.endsWith("Lcom/artemis/asmdemo/DebugTrace;")) {
            weavingMethod = true;
        }
        Method method2 = getMethod(TestMethod.class, "method2");
        if (method2 != null) {
            Annotation[][] annotations = method2.getParameterAnnotations();
            Annotation[] annotations1 = method2.getAnnotations();
            System.out.println(annotations1.length);
            System.out.println(method2.getName());
            System.out.println(annotations.length);
            for (Annotation[] annotation : annotations) {
                System.out.println(annotation);
            }
        }

        AnnotationVisitor annotationVisitor = super.visitAnnotation(descriptor, visible);
        System.out.println(annotationVisitor.getClass().getName());

        return weavingMethod ? new AnnotationAdapter(annotationVisitor, annotationCallback) : annotationVisitor;
    }

    Method getMethod(Class clazz, String methodName, Class... param) {
        System.out.println(clazz.toString());
        try {
           return  clazz.getMethod(methodName, param);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        if (weavingMethod) {
            System.out.println("onMethodEnter");
            System.out.println("class=" + targetClassName + "   method=" + methodName);
        }

        if (weavingMethod) {
            mv.visitLdcInsn(methodName);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/artemis/asmdemo/.MethodDebugTrace", "begin", "(Ljava/lang/String;)V", false);
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        if (weavingMethod) {
            mv.visitLdcInsn(methodName);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/artemis/asmdemo/.MethodDebugTrace", "end", "(Ljava/lang/String;)V", false);
        }
    }
}
