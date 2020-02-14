package io.github.wanggit.antrpc.commons.javassist;

import com.google.common.collect.Lists;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.MethodInfo;
import org.junit.Test;
import org.springframework.asm.*;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JavassistTest {

    @Test
    public void test4() {
        ArrayList<Integer> list = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7);
        List<Integer> collect = list.stream().filter(it -> it > 4).collect(Collectors.toList());
        System.out.println(collect);
    }

    @Test
    public void test() throws NotFoundException, ClassNotFoundException {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.get(Dog.class.getName());
        CtMethod[] methods = ctClass.getMethods();
        for (CtMethod method : methods) {
            if (method.getName().equals("say")) {
                MethodInfo methodInfo2 = method.getMethodInfo2();
                System.out.println(methodInfo2);
            }
        }
    }

    @Test
    public void test2() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Dog.class);
        enhancer.setCallback(
                new MethodInterceptor() {
                    @Override
                    public Object intercept(
                            Object o, Method method, Object[] objects, MethodProxy methodProxy)
                            throws Throwable {
                        Object result = methodProxy.invokeSuper(o, objects);
                        return result.toString() + " En";
                    }
                });
        Object o = enhancer.create();
        String result = ((Dog) o).say("Xiao Hei");
        System.out.println(result);
    }

    @Test
    public void test3() throws IOException {
        ClassReader classReader = new ClassReader(Dog.class.getName());
        classReader.accept(
                new ClassVisitor(Opcodes.ASM5) {
                    @Override
                    public void visitSource(String source, String debug) {
                        System.out.println(source);
                        super.visitSource(source, debug);
                    }

                    @Override
                    public void visit(
                            int version,
                            int access,
                            String name,
                            String signature,
                            String superName,
                            String[] interfaces) {
                        super.visit(version, access, name, signature, superName, interfaces);
                    }

                    @Override
                    public MethodVisitor visitMethod(
                            int access,
                            String name,
                            String descriptor,
                            String signature,
                            String[] exceptions) {
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                    }

                    @Override
                    public FieldVisitor visitField(
                            int access,
                            String name,
                            String descriptor,
                            String signature,
                            Object value) {
                        return super.visitField(access, name, descriptor, signature, value);
                    }
                },
                ClassReader.SKIP_CODE);
        System.out.println(classReader);
    }

    public static class Dog {
        String prefix;

        String say(String name) {
            if (prefix == null) {
                this.prefix = "Ready ";
            }
            return name + " " + this.prefix + "Wang Wang";
        }
    }
}
