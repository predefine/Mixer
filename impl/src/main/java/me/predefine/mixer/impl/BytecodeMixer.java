package me.predefine.mixer.impl;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
        ClassWriter writer = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
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

                return new MixerMethodVisitor(mv, descriptor, recipes);
            }
        }, 0);

        return writer.toByteArray();
    }
}
