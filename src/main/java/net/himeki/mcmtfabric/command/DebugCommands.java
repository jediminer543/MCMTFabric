package net.himeki.mcmtfabric.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;

public class DebugCommands {

	public static LiteralArgumentBuilder<ServerCommandSource> registerDebug(LiteralArgumentBuilder<ServerCommandSource> root) {
		return root.then(CommandManager.literal("getBlockState")
				.then(CommandManager.argument("location", Vec3ArgumentType.vec3()).executes(cmdCtx -> {
					PosArgument loc = Vec3ArgumentType.getPosArgument(cmdCtx, "location");
					BlockPos bp = loc.toAbsoluteBlockPos(cmdCtx.getSource());
					ServerWorld sw = cmdCtx.getSource().getWorld();
					BlockState bs = sw.getBlockState(bp);
					LiteralText message = new LiteralText(
							"Block at " + bp + " is " + bs.getBlock().getName());
					cmdCtx.getSource().sendFeedback(message, true);
					System.out.println(message.toString());
					return 1;
				}))).then(CommandManager.literal("nbtdump")
						.then(CommandManager.argument("location", Vec3ArgumentType.vec3()).executes(cmdCtx -> {
							PosArgument loc = Vec3ArgumentType.getPosArgument(cmdCtx, "location");
							BlockPos bp = loc.toAbsoluteBlockPos(cmdCtx.getSource());
							ServerWorld sw = cmdCtx.getSource().getWorld();
							BlockState bs = sw.getBlockState(bp);
							BlockEntity te = sw.getBlockEntity(bp);
							if (te == null) {
								LiteralText message = new LiteralText(
										"Block at " + bp + " is " + bs.getBlock().getName() + " has no NBT");
								cmdCtx.getSource().sendFeedback(message, true);
							}
							CompoundTag nbt = te.toInitialChunkDataTag();
							Text itc = nbt.toText();
							LiteralText message = new LiteralText(
									"Block at " + bp + " is " + bs.getBlock().getName() + " with BE NBT:");
							cmdCtx.getSource().sendFeedback(message, true);
							cmdCtx.getSource().sendFeedback(itc, true);
							//System.out.println(message.toString());
							return 1;
				}))).then(CommandManager.literal("tick").requires(cmdSrc -> {
					return cmdSrc.hasPermissionLevel(2);
				}).then(CommandManager.literal("te"))
						.then(CommandManager.argument("location", Vec3ArgumentType.vec3()).executes(cmdCtx -> {
							PosArgument loc = Vec3ArgumentType.getPosArgument(cmdCtx, "location");
							BlockPos bp = loc.toAbsoluteBlockPos(cmdCtx.getSource());
							ServerWorld sw = cmdCtx.getSource().getWorld();
							BlockEntity te = sw.getBlockEntity(bp);
							if (te instanceof Tickable) {
								((Tickable) te).tick();
								LiteralText message = new LiteralText(
										"Ticked " + te.getClass().getName() + " at " + bp);
								cmdCtx.getSource().sendFeedback(message, true);
							} else {
								LiteralText message = new LiteralText("No tickable BE at " + bp);
								cmdCtx.getSource().sendError(message);
							}
							return 1;
						})))
				.then(CommandManager.literal("classpathDump").requires(cmdSrc -> {
					return cmdSrc.hasPermissionLevel(2);
				}).executes(cmdCtx -> {
					Path base = Paths.get("classpath_dump/");
					try {
						Files.createDirectories(base);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					// Copypasta from syncfu;
					Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).flatMap(path -> {
			        	File file = new File(path);
			        	if (file.isDirectory()) {
			        		return Arrays.stream(file.list((d, n) -> n.endsWith(".jar")));
			        	}
			        	return Arrays.stream(new String[] {path});
			        }).filter(s -> s.endsWith(".jar"))
					.map(Paths::get).forEach(path -> {
			        	Path name = path.getFileName();
			        	try {
							Files.copy(path, Paths.get(base.toString(), name.toString()), StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
					
					
					LiteralText message = new LiteralText("Classpath Dumped to: " + base.toAbsolutePath().toString());
					cmdCtx.getSource().sendFeedback(message, true);
					System.out.println(message.toString());
					return 1;
				}));
	}
}
