package net.himeki.mcmtfabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.himeki.mcmtfabric.ParallelProcessor;
import net.himeki.mcmtfabric.config.GeneralConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Tickable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class ConfigCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> mcmtconfig = CommandManager.literal("mcmt");
		mcmtconfig = mcmtconfig.then(registerConfig(CommandManager.literal("config")));
		mcmtconfig = mcmtconfig.then(DebugCommands.registerDebug(CommandManager.literal("debug")));
		mcmtconfig = StatsCommand.registerStatus(mcmtconfig);
		dispatcher.register(mcmtconfig);
	}

	public static LiteralArgumentBuilder<ServerCommandSource> registerConfig(LiteralArgumentBuilder<ServerCommandSource> root) {
		return root.then(CommandManager.literal("toggle").requires(cmdSrc -> {
			return cmdSrc.hasPermissionLevel(2);
		}).executes(cmdCtx -> {
			GeneralConfig.disabled = !GeneralConfig.disabled;
			LiteralText message = new LiteralText(
					"MCMT is now " + (GeneralConfig.disabled ? "disabled" : "enabled"));
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		}).then(CommandManager.literal("te").executes(cmdCtx -> {
			GeneralConfig.disableTileEntity = !GeneralConfig.disableTileEntity;
			LiteralText message = new LiteralText("MCMT's tile entity threading is now "
					+ (GeneralConfig.disableTileEntity ? "disabled" : "enabled"));
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		})).then(CommandManager.literal("entity").executes(cmdCtx -> {
			GeneralConfig.disableEntity = !GeneralConfig.disableEntity;
			LiteralText message = new LiteralText(
					"MCMT's entity threading is now " + (GeneralConfig.disableEntity ? "disabled" : "enabled"));
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		})).then(CommandManager.literal("environment").executes(cmdCtx -> {
			GeneralConfig.disableEnvironment = !GeneralConfig.disableEnvironment;
			LiteralText message = new LiteralText("MCMT's environment threading is now "
					+ (GeneralConfig.disableEnvironment ? "disabled" : "enabled"));
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		})).then(CommandManager.literal("world").executes(cmdCtx -> {
			GeneralConfig.disableWorld = !GeneralConfig.disableWorld;
			LiteralText message = new LiteralText(
					"MCMT's world threading is now " + (GeneralConfig.disableWorld ? "disabled" : "enabled"));
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		})).then(CommandManager.literal("chunkprovider").executes(cmdCtx -> {
			GeneralConfig.disableChunkProvider = !GeneralConfig.disableChunkProvider;
			LiteralText message = new LiteralText(
					"MCMT's SCP threading is now " + (GeneralConfig.disableChunkProvider ? "disabled" : "enabled"));
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		}))).then(CommandManager.literal("state").executes(cmdCtx -> {
			StringBuilder messageString = new StringBuilder(
					"MCMT is currently " + (GeneralConfig.disabled ? "disabled" : "enabled"));
			if (!GeneralConfig.disabled) {
				messageString.append(" World:" + (GeneralConfig.disableWorld ? "disabled" : "enabled"));
				messageString.append(" Entity:" + (GeneralConfig.disableEntity ? "disabled" : "enabled"));
				messageString.append(" TE:" + (GeneralConfig.disableTileEntity ? "disabled"
						: "enabled" + (GeneralConfig.chunkLockModded ? "(ChunkLocking Modded)" : "")));
				messageString.append(" Env:" + (GeneralConfig.disableEnvironment ? "disabled" : "enabled"));
				messageString.append(" SCP:" + (GeneralConfig.disableChunkProvider ? "disabled" : "enabled"));
			}
			LiteralText message = new LiteralText(messageString.toString());
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		}))
		.then(CommandManager.literal("save").requires(cmdSrc -> {
			return cmdSrc.hasPermissionLevel(2);
		}).executes(cmdCtx -> {
			LiteralText message = new LiteralText("Saving MCMT config to disk...");
			cmdCtx.getSource().sendFeedback(message, true);
			//GeneralConfig.saveConfig();
			message = new LiteralText("Done!");
			cmdCtx.getSource().sendFeedback(message, true);
			return 1;
		}))
		.then(CommandManager.literal("temanage").requires(cmdSrc -> {
				return cmdSrc.hasPermissionLevel(2);
			})
			.then(CommandManager.literal("list")
				.executes(cmdCtx -> {
					LiteralText message = new LiteralText("NYI");
					cmdCtx.getSource().sendFeedback(message, true);
					return 1;
				}))
			.then(CommandManager.literal("target")
				.requires(cmdSrc -> {
					try {
						if (cmdSrc.getPlayer() != null) {
							return true;
						}
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
					}
					LiteralText message = new LiteralText("Only runable by player!");
					cmdSrc.sendError(message);
					return false;
				})
				.then(CommandManager.literal("whitelist").executes(cmdCtx -> {
					LiteralText message;
					HitResult rtr = cmdCtx.getSource().getPlayer().raycast(20, 0.0F, false);
					if (rtr.getType() == HitResult.Type.BLOCK) {
						BlockPos bp = ((BlockHitResult)rtr).getBlockPos();
						BlockEntity te = cmdCtx.getSource().getWorld().getBlockEntity(bp);
						if (te != null && te instanceof Tickable) {
							// TODO FIX
							//GeneralConfig.teWhiteList.add(te.getClass());
							//GeneralConfig.teBlackList.remove(te.getClass());
							message = new LiteralText("Added "+te.getClass().getName()+" to TE Whitelist");
							cmdCtx.getSource().sendFeedback(message, true);
							return 1;
						}
						message = new LiteralText("That block doesn't contain a tickable TE!");
						cmdCtx.getSource().sendError(message);
						return 0;
					}
					message = new LiteralText("Only runable by player!");
					cmdCtx.getSource().sendError(message);
					return 0;
				}))
				.then(CommandManager.literal("blacklist").executes(cmdCtx -> {
					LiteralText message;
					HitResult rtr = cmdCtx.getSource().getPlayer().raycast(20, 0.0F, false);
					if (rtr.getType() == HitResult.Type.BLOCK) {
						BlockPos bp = ((BlockHitResult)rtr).getBlockPos();
						BlockEntity te = cmdCtx.getSource().getWorld().getBlockEntity(bp);
						if (te != null && te instanceof Tickable) {
							// TODO FIX
							//GeneralConfig.teWhiteList.add(te.getClass());
							//GeneralConfig.teBlackList.remove(te.getClass());
							message = new LiteralText("Added "+te.getClass().getName()+" to TE Blacklist");
							cmdCtx.getSource().sendFeedback(message, true);
							return 1;
						}
						message = new LiteralText("That block doesn't contain a tickable TE!");
						cmdCtx.getSource().sendError(message);
						return 0;
					}
					message = new LiteralText("Only runable by player!");
					cmdCtx.getSource().sendError(message);
					return 0;
				}))
				.then(CommandManager.literal("remove").executes(cmdCtx -> {
					LiteralText message;
					HitResult rtr = cmdCtx.getSource().getPlayer().raycast(20, 0.0F, false);
					if (rtr.getType() == HitResult.Type.BLOCK) {
						BlockPos bp = ((BlockHitResult)rtr).getBlockPos();
						BlockEntity te = cmdCtx.getSource().getWorld().getBlockEntity(bp);
						if (te != null && te instanceof Tickable) {
							// TODO FIX
							//GeneralConfig.teWhiteList.add(te.getClass());
							//GeneralConfig.teBlackList.remove(te.getClass());
							message = new LiteralText("Removed "+te.getClass().getName()+" from TE classlists");
							cmdCtx.getSource().sendFeedback(message, true);
							return 1;
						}
						message = new LiteralText("That block doesn't contain a tickable TE!");
						cmdCtx.getSource().sendError(message);
						return 0;
					}
					message = new LiteralText("Only runable by player!");
					cmdCtx.getSource().sendError(message);
					return 0;
				}))
				.then(CommandManager.literal("willtick").executes(cmdCtx -> {
					LiteralText message;
					HitResult rtr = cmdCtx.getSource().getPlayer().raycast(20, 0.0F, false);
					if (rtr.getType() == HitResult.Type.BLOCK) {
						BlockPos bp = ((BlockHitResult)rtr).getBlockPos();
						BlockEntity te = cmdCtx.getSource().getWorld().getBlockEntity(bp);
						if (te != null && te instanceof Tickable) {
							boolean willSerial = ParallelProcessor.filterTE((Tickable)te);
							message = new LiteralText("That TE " + (!willSerial ? "will" : "will not") + " tick fully parallelised");
							cmdCtx.getSource().sendFeedback(message, true);
							return 1;
						}
						message = new LiteralText("That block doesn't contain a tickable TE!");
						cmdCtx.getSource().sendError(message);
						return 0;
					}
					message = new LiteralText("Only runable by player!");
					cmdCtx.getSource().sendError(message);
					return 0;
				}))
			)
		);
	}

}
