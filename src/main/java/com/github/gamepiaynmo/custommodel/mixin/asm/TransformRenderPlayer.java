package com.github.gamepiaynmo.custommodel.mixin.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import squeek.asmhelper.ASMHelper;
import squeek.asmhelper.ObfHelper;

import java.io.File;
import java.io.IOException;

public class TransformRenderPlayer implements IClassTransformer {
    private static String RENDER_PLAYER_CLASS = "net.minecraft.client.renderer.entity.RenderPlayer";
    private static String RENDER_MODEL_SIGNATURE = "(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)V";
    private static String RENDER_PLAYER_HANDLER_CLASS = "com/github/gamepiaynmo/custommodel/mixin/RenderPlayerHandler";
    private static String RENDER_MODEL_METHOD = "(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)Z";
    private static String RENDER_LIVING_CLASS = "net/minecraft/client/renderer/entity/RenderLivingBase";
    private static String RENDER_LIVING_SIGNATURE = "(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)V";

    private static String RENDER_HAND_SIGNATURE = "(Lnet/minecraft/client/entity/AbstractClientPlayer;)V";
    private static String RENDER_HAND_METHOD = "(Lnet/minecraft/client/entity/AbstractClientPlayer;)Z";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(RENDER_PLAYER_CLASS)) {
            ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

            MethodVisitor visitor = classNode.visitMethod(Opcodes.ACC_PROTECTED, ObfHelper.isObfuscated() ? "func_77036_a" : "renderModel", RENDER_MODEL_SIGNATURE, null, null);
            visitor.visitCode();
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            visitor.visitVarInsn(Opcodes.FLOAD, 2);
            visitor.visitVarInsn(Opcodes.FLOAD, 3);
            visitor.visitVarInsn(Opcodes.FLOAD, 4);
            visitor.visitVarInsn(Opcodes.FLOAD, 5);
            visitor.visitVarInsn(Opcodes.FLOAD, 6);
            visitor.visitVarInsn(Opcodes.FLOAD, 7);
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, RENDER_PLAYER_HANDLER_CLASS, "renderModel", RENDER_MODEL_METHOD, false);
            Label ret = new Label();
            visitor.visitJumpInsn(Opcodes.IFEQ, ret);
            visitor.visitInsn(Opcodes.RETURN);
            visitor.visitLabel(ret);
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            visitor.visitVarInsn(Opcodes.FLOAD, 2);
            visitor.visitVarInsn(Opcodes.FLOAD, 3);
            visitor.visitVarInsn(Opcodes.FLOAD, 4);
            visitor.visitVarInsn(Opcodes.FLOAD, 5);
            visitor.visitVarInsn(Opcodes.FLOAD, 6);
            visitor.visitVarInsn(Opcodes.FLOAD, 7);
            visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, RENDER_LIVING_CLASS, ObfHelper.isObfuscated() ? "func_77036_a" : "renderModel", RENDER_LIVING_SIGNATURE, false);
            visitor.visitInsn(Opcodes.RETURN);
            visitor.visitEnd();

            MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_177138_b", "renderRightArm", RENDER_HAND_SIGNATURE);
            AbstractInsnNode startNode = ASMHelper.findFirstInstruction(methodNode);
            InsnList insn = new InsnList();
            insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
            insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RENDER_PLAYER_HANDLER_CLASS, "renderRightArm", RENDER_HAND_METHOD, false));
            LabelNode retNode = new LabelNode();
            insn.add(new JumpInsnNode(Opcodes.IFEQ, retNode));
            insn.add(new InsnNode(Opcodes.RETURN));
            insn.add(retNode);
            methodNode.instructions.insertBefore(startNode, insn);

            methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_177139_c", "renderLeftArm", RENDER_HAND_SIGNATURE);
            startNode = ASMHelper.findFirstInstruction(methodNode);
            insn.clear();
            insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
            insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RENDER_PLAYER_HANDLER_CLASS, "renderLeftArm", RENDER_HAND_METHOD, false));
            retNode = new LabelNode();
            insn.add(new JumpInsnNode(Opcodes.IFEQ, retNode));
            insn.add(new InsnNode(Opcodes.RETURN));
            insn.add(retNode);
            methodNode.instructions.insertBefore(startNode, insn);

            return ASMHelper.writeClassToBytes(classNode, ClassWriter.COMPUTE_FRAMES);
        }

        return basicClass;
    }
}
