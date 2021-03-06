package com.github.johnnyjayjay.spigotmaps.example;

import com.github.johnnyjayjay.spigotmaps.*;
import com.github.johnnyjayjay.spigotmaps.rendering.*;
import com.github.johnnyjayjay.spigotmaps.util.ImageTools;
import com.madgag.gif.fmsware.GifDecoder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class SamplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("image");
        getCommand("text");
        getCommand("atext");
        getCommand("gif");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        try {
            RenderedMap map = createMap(player, name, String.join(" ", args));
            map.give(player);
            player.sendMessage("§aLook in your inventory!");
        } catch (IOException e) {
            player.sendMessage("§cCould not create map: " + e.getMessage());
        }
        return true;
    }

    private RenderedMap createMap(Player player, String kind, String text) throws IOException {
        MapRenderer renderer = switch (kind) {
            case "image" -> {
                URL imageUrl = new URL(text);
                BufferedImage image = ImageTools.resizeToMapSize(ImageTools.loadWithUserAgentFrom(imageUrl));
                break ImageRenderer.builder().addPlayers(player).image(image).build();
            }
            case "text" -> SimpleTextRenderer.builder().addPlayers(player).addText(text).build();
            case "atext" -> AnimatedTextRenderer.builder().addPlayers(player).addText(text).build();
            case "gif" -> {
                GifDecoder decoder = new GifDecoder();
                int code = decoder.read(text);
                if (code != GifDecoder.STATUS_OK)
                    throw new IOException("Could not load gif image. Code: " + code);

                GifImage gif = ImageTools.resizeToMapSize(GifImage.fromDecoder(decoder));
                break GifRenderer.builder()
                        .addPlayers(player)
                        .gif(gif)
                        .repeat(decoder.getLoopCount() == 0 ? GifRenderer.REPEAT_FOREVER : decoder.getLoopCount())
                        .build();
            }
            default -> throw new AssertionError();
        };
        return MapBuilder.create().world(player.getWorld()).addRenderers(renderer).build();
    }
}
