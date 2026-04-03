package me.predefine.mixer.impl;

import me.predefine.mixer.api.Ingredient;
import me.predefine.mixer.api.IngredientContext;
import me.predefine.mixer.api.InjectionPoint;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MixerMethodVisitor extends MethodVisitor {
    private final String IngredientContextClassName;
    private final String IngredientContextImplClassName;
    private final Type descriptor;
    private final ArrayList<MixRecipe> recipes;
    private boolean isVisitingRealCode = false;
    private final Label afterImplementationLabel = new Label();

    public MixerMethodVisitor(MethodVisitor mv, String descriptor, ArrayList<MixRecipe> recipes) {
        super(Opcodes.ASM9, mv);
        this.descriptor = Type.getType(descriptor);
        this.recipes = recipes;

        IngredientContextClassName = IngredientContext.class.getTypeName().replaceAll("\\.", "/");
        IngredientContextImplClassName = IngredientContextImpl.class.getTypeName().replaceAll("\\.", "/");
    }

    private void initIngredientContext() {
        visitTypeInsn(Opcodes.NEW, IngredientContextImplClassName);
        visitInsn(Opcodes.DUP);
        visitMethodInsn(Opcodes.INVOKESPECIAL, IngredientContextImplClassName, "<init>", "()V", false);
    }

    private void callIngredientHooks(InjectionPoint point) {
        recipes.forEach(recipe -> {
            recipe.ingredients.forEach(ingredientEntry -> {
                Ingredient ingredient = ingredientEntry.getKey();
                Method hook = ingredientEntry.getValue();
                if (ingredient.point() != point)
                    return;

                if (ingredient.point() == InjectionPoint.MODIFY_ARGUMENT)
                {
                    if (descriptor.getArgumentCount() <= ingredient.argumentIndex())
                        return;
                    Type argumentType = descriptor.getArgumentTypes()[ingredient.argumentIndex()];
                    loadFromLocals(ingredient.argumentIndex() + 1, argumentType);
                    visitMethodInsn(Opcodes.INVOKESTATIC, hook.getDeclaringClass().getTypeName().replaceAll("\\.", "/"),
                            hook.getName(), "(" + argumentType + ")" + argumentType, false);
                    saveToLocals(ingredient.argumentIndex() + 1, argumentType);
                    return;
                }
                visitInsn(Opcodes.DUP);
                visitMethodInsn(Opcodes.INVOKESTATIC, hook.getDeclaringClass().getTypeName().replaceAll("\\.", "/"),
                        hook.getName(), "(L" + IngredientContextClassName + ";)V", false);
            });
        });
    }

    private void checkIfCanceled(boolean jumpIfCanceled, Label jump)
    {
        visitInsn(Opcodes.DUP);
        visitMethodInsn(Opcodes.INVOKEVIRTUAL, IngredientContextImplClassName, "isCanceled", "()Z", false);
        visitJumpInsn(jumpIfCanceled ? Opcodes.IFNE : Opcodes.IFEQ, jump);
    }

    private void generateReturnOpcode()
    {
        Type returnType = descriptor.getReturnType();
        castObjectToPrimitive(returnType);
        switch (returnType.getSort())
        {
            case Type.VOID:
                visitInsn(Opcodes.RETURN);
                break;
            case Type.BOOLEAN:
                visitInsn(Opcodes.IRETURN);
                break;
            case Type.OBJECT:
                visitInsn(Opcodes.ARETURN);
                break;
            default:
                System.out.println("Unknown return type: " + returnType);
                break;
        }
    }

    private void castObjectToPrimitive(Type type)
    {
        switch (type.getSort())
        {
            case Type.VOID:
                visitInsn(Opcodes.POP);
                break;
            case Type.BOOLEAN:
                visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
                visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                break;
            case Type.OBJECT:
                // Ljava/lang/Object;
                //  ^^^^^^^^^^^^^^^^
                // Opcodes.CHECKCAST java/lang/Object
                visitTypeInsn(Opcodes.CHECKCAST, type.getClassName().replaceAll("\\.", "/"));
                break;
            default:
                System.out.println("Unknown type: " + type);
                break;
        }
    }

    private void castPrimitiveToObject(Type type)
    {
        switch (type.getSort())
        {
            case Type.VOID:
                visitInsn(Opcodes.ACONST_NULL);
                break;
            case Type.BOOLEAN:
                visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                break;
            case Type.OBJECT:
                // already an object
                break;
            default:
                System.out.println("Unknown type: " + type);
                break;
        }
    }

    private void saveToLocals(int idx, Type type)
    {
        switch (type.getSort())
        {
            case Type.BOOLEAN:
            case Type.INT:
                visitVarInsn(Opcodes.ISTORE, idx);
                break;
            case Type.OBJECT:
                visitVarInsn(Opcodes.ASTORE, idx);
                break;
            default:
                System.out.println("Unknown type: " + type);
                break;
        }
    }

    private void loadFromLocals(int idx, Type type)
    {
        switch (type.getSort())
        {
            case Type.BOOLEAN:
            case Type.INT:
                visitVarInsn(Opcodes.ILOAD, idx);
                break;
            case Type.OBJECT:
                visitVarInsn(Opcodes.ALOAD, idx);
                visitTypeInsn(Opcodes.CHECKCAST, type.getClassName().replaceAll("\\.", "/"));
                break;
            default:
                System.out.println("Unknown type: " + type);
                break;
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (isVisitingRealCode)
            if (opcode == Opcodes.ARETURN || opcode == Opcodes.IRETURN || opcode == Opcodes.RETURN) {
                super.visitJumpInsn(Opcodes.GOTO, afterImplementationLabel);
                return;
            }
        super.visitInsn(opcode);
    }

    @Override
    public void visitCode() {
        super.visitCode();

        // context = new IngedientContextImpl();
        initIngredientContext();

        callIngredientHooks(InjectionPoint.MODIFY_ARGUMENT);

        // ingredients.forEach(ingredient -> {
        //   if (ingredient.getInjectionPoint() == BEFORE_IMPLEMENTATION)
        //     callIngredientHook(ingredient);
        // });
        callIngredientHooks(InjectionPoint.BEFORE_IMPLEMENTATION);

        Label realCodeLabel = new Label();

        // if (context.isCanceled() != false) goto realCode;
        checkIfCanceled(false, realCodeLabel);

        // else: return context.getReturn();
        visitMethodInsn(Opcodes.INVOKEVIRTUAL, IngredientContextImplClassName, "getReturn", "()Ljava/lang/Object;", false);
        generateReturnOpcode();

        visitLabel(realCodeLabel);
        visitInsn(Opcodes.POP); // remove IngredientContext from stack

        // enable visitInsn hook
        isVisitingRealCode = true;
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // disable visitInsn hook
        isVisitingRealCode = false;

        visitLabel(afterImplementationLabel);
        castPrimitiveToObject(descriptor.getReturnType());
        // ..., original return
        visitTypeInsn(Opcodes.NEW, IngredientContextImplClassName);
        // ..., original return, context
        visitInsn(Opcodes.DUP_X1);
        // ..., context, original return, context
        visitInsn(Opcodes.DUP_X1);
        // ..., context, context, original return, context
        visitMethodInsn(Opcodes.INVOKESPECIAL, IngredientContextImplClassName, "<init>", "()V", false);
        // ..., context, context, original return
        visitMethodInsn(Opcodes.INVOKEVIRTUAL, IngredientContextImplClassName, "setReturn", "(Ljava/lang/Object;)V", false);
        // ..., context?
        callIngredientHooks(InjectionPoint.AFTER_IMPLEMENTATION);
        visitMethodInsn(Opcodes.INVOKEVIRTUAL, IngredientContextImplClassName, "getReturn", "()Ljava/lang/Object;", false);
        generateReturnOpcode();

        super.visitMaxs(maxStack + 4, maxLocals);
    }
}
