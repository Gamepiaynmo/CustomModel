package com.github.gamepiaynmo.custommodel.mixin.asm;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.render.EntityParameter;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import squeek.asmhelper.ASMHelper;

import java.io.File;
import java.io.IOException;

public class TransformGuiInventory implements IClassTransformer {
    private static String GUI_INVENTORY_CLASS = "net.minecraft.client.gui.inventory.GuiInventory";
    private static String DRAW_ENTITY_SIGNATURE = "(IIIFFLnet/minecraft/entity/EntityLivingBase;)V";
    private static String TRANSFORM_GUI_CLASS = "com/github/gamepiaynmo/custommodel/mixin/DrawEntityInventoryHandler";
    private static String DRAW_ENTITY_METHOD = "(Lnet/minecraft/entity/EntityLivingBase;)V";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(GUI_INVENTORY_CLASS)) {
            ClassNode classNode = ASMHelper.readClassFromBytes(basicClass);

            MethodNode methodNode = ASMHelper.findMethodNodeOfClass(classNode, "func_147046_a", "drawEntityOnScreen", DRAW_ENTITY_SIGNATURE);
            AbstractInsnNode startNode = ASMHelper.findFirstInstruction(methodNode);
            InsnList insn = new InsnList();
            insn.add(new VarInsnNode(Opcodes.ALOAD, 5));
            insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORM_GUI_CLASS, "preDrawEntityInventory", DRAW_ENTITY_METHOD, false));
            methodNode.instructions.insertBefore(startNode, insn);

            AbstractInsnNode endNode = ASMHelper.findLastInstructionWithOpcode(methodNode, Opcodes.RETURN);
            AbstractInsnNode node = new MethodInsnNode(Opcodes.INVOKESTATIC, TRANSFORM_GUI_CLASS, "postDrawEntityInventory", "()V", false);
            methodNode.instructions.insertBefore(endNode, node);

            return ASMHelper.writeClassToBytes(classNode, ClassWriter.COMPUTE_FRAMES);
        }

        return basicClass;
    }
}
