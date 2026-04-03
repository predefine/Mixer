package me.predefine.mixer.impl;

import me.predefine.mixer.api.Ingredient;
import me.predefine.mixer.api.IngredientContext;
import me.predefine.mixer.api.InjectionPoint;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class BytecodeMixer {
    private ArrayList<MixRecipe> recipes = new ArrayList<>();
    private ClassReader classReader;

    public BytecodeMixer(byte[] bytecode) {
         classReader = new ClassReader(bytecode);
    }

    public void mix(MixRecipe recipe) {
        this.recipes.add(recipe);
    }

    public byte[] toBytecode()
    {
        ClassWriter writer = new ClassWriter(classReader,ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classReader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                boolean mixMethod = false;
                for (MixRecipe recipe : recipes)
                {
                    if (recipe.haveIngredient(access, name, descriptor, exceptions)) {
                        mixMethod = true;
                        break;
                    }
                }
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!mixMethod)
                    return mv;

                return new MethodVisitor(Opcodes.ASM9, mv) {
                    private final String IngredientContextClassName = IngredientContext.class.getTypeName().replaceAll("\\.", "/");
                    private final String IngredientContextImplClassName = IngredientContextImpl.class.getTypeName().replaceAll("\\.", "/");

                    @Override
                    public void visitCode() {
                        super.visitCode();
                        visitTypeInsn(Opcodes.NEW, IngredientContextImplClassName);
                        visitInsn(Opcodes.DUP);
                        visitMethodInsn(Opcodes.INVOKESPECIAL, IngredientContextImplClassName, "<init>", "()V", false);

                        recipes.forEach(recipe -> {
                            recipe.ingredients.forEach(ingredientEntry -> {
                                Ingredient ingredient = ingredientEntry.getKey();
                                Method hook = ingredientEntry.getValue();
                                if (ingredient.point() != InjectionPoint.BEFORE_IMPLEMENTATION)
                                    return;

                                visitInsn(Opcodes.DUP);
                                visitMethodInsn(Opcodes.INVOKESTATIC, hook.getDeclaringClass().getTypeName().replaceAll("\\.", "/"),
                                                hook.getName(), "(L" + IngredientContextClassName + ";)V", false);
                            });
                        });

                        Label realCodeLabel = new Label();

                        visitInsn(Opcodes.DUP);
                        visitMethodInsn(Opcodes.INVOKEVIRTUAL, IngredientContextImplClassName, "isCanceled", "()Z", false);
                        visitJumpInsn(Opcodes.IFEQ, realCodeLabel); // jump to real code if method shouldn't be canceled

                        visitMethodInsn(Opcodes.INVOKEVIRTUAL, IngredientContextImplClassName, "getReturn", "()Ljava/lang/Object;", false);

                        String returnType = descriptor.substring(descriptor.lastIndexOf(")") + 1);
                        if (returnType.equals("V"))
                            visitInsn(Opcodes.RETURN);
                        else if (returnType.equals("Z"))
                        {
                            visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
                            visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                            visitInsn(Opcodes.IRETURN);
                        } else if (returnType.startsWith("L"))
                        {
                            visitTypeInsn(Opcodes.CHECKCAST, returnType.substring(1, returnType.length() - 1));
                            visitInsn(Opcodes.ARETURN);
                        } else {
                            System.out.println("Unknown return type: " + returnType);
                        }

                        visitLabel(realCodeLabel);
                        visitInsn(Opcodes.POP); // remove IngredientContext from stack
                    }

                    @Override
                    public void visitEnd() {
                        // TODO: implement AFTER_IMPLEMENTATION
                        super.visitEnd();
                    }

                    @Override
                    public void visitMaxs(int maxStack, int maxLocals) {
                        super.visitMaxs(maxStack + 4, maxLocals);
                    }
                };
            }
        }, 0);

        return writer.toByteArray();
    }
}
