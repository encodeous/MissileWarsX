package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.MwPlugin;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class MwxSuggestions {
    private MwxSuggestions() {
    }

    public static CompletableFuture<Suggestions> suggestPlayers(final SuggestionsBuilder builder) {
        final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (final Player p : Bukkit.getOnlinePlayers()) {
            final String name = p.getName();
            if (name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestTeams(final SuggestionsBuilder builder) {
        final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (final String s : new String[]{"red", "green"}) {
            if (s.startsWith(remaining)) {
                builder.suggest(s);
            }
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestGiveOptions(final MwPlugin plugin, final SuggestionsBuilder builder) {
        final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        final Collection<String> options = new ArrayList<>();
        options.add("all");
        for (final var item : plugin.getGame().getConfig().items) {
            options.add(item.id);
        }
        for (final String s : options) {
            if (s.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(s);
            }
        }
        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestMaps(final MwPlugin plugin, final SuggestionsBuilder builder) {
        final String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        final File mapsDir = new File(plugin.getDataFolder(), "maps");
        if (!mapsDir.exists() || !mapsDir.isDirectory()) {
            return builder.buildFuture();
        }

        final File[] files = mapsDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) {
            return builder.buildFuture();
        }

        for (final File f : files) {
            final String name = f.getName();
            final String base = name.substring(0, name.length() - 4); // strip .yml
            if (base.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(base);
            }
        }
        return builder.buildFuture();
    }
}
