package com.artemis.asmdemo;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by xrealm on 2020/8/13.
 */
public class AsmTest {

    public static void main(String[] args) throws IOException {
//        testJava();
        testMethod();
    }

    private static void testJava() {
        TestMethod testMethod = new TestMethod();
        try {
            Method method1 = TestMethod.class.getMethod("method1");
            Annotation[][] parameterAnnotations = method1.getParameterAnnotations();
            Annotation[] declaredAnnotations = method1.getDeclaredAnnotations();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private static void testClass() throws IOException {
        ClassReader classReader = new ClassReader("com.artemis.asmdemo.TestClass");
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new ADCalssVisitor(classWriter);
        classReader.accept(classVisitor, ClassReader.SKIP_DEBUG);

        byte[] bytes = classWriter.toByteArray();
        FileOutputStream fos = new FileOutputStream(new File("asmdemo/src/main/java/com/artemis/asmdemo/Tgg.class"));
        fos.write(bytes);
        fos.close();
    }

    private static void testMethod() throws IOException {
        ClassReader classReader = new ClassReader("com.artemis.asmdemo.TestMethod");
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new ADCalssVisitor(classWriter);
        classReader.accept(classVisitor, ClassReader.SKIP_DEBUG);

        byte[] bytes = classWriter.toByteArray();
        FileOutputStream fos = new FileOutputStream(new File("asmdemo/src/main/java/com/artemis/asmdemo/Tggm.class"));
        fos.write(bytes);
        fos.close();
    }
}
