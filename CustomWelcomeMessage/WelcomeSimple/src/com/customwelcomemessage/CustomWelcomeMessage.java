package com.customwelcomemessage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CustomWelcomeMessage extends JavaPlugin implements Listener, CommandExecutor {

    private File playersFile;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("ws").setExecutor(this);

        // Créer le fichier players.txt s'il n'existe pas
        playersFile = new File(getDataFolder(), "players.txt");
        if (!playersFile.exists()) {
            try {
                playersFile.getParentFile().mkdirs();
                playersFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        FileConfiguration config = this.getConfig();
        String playerName = event.getPlayer().getName();

        try {
            // Vérifier si le joueur est déjà enregistré dans le fichier players.txt
            List<String> playerNames = Files.readAllLines(playersFile.toPath());

            if (!playerNames.contains(playerName)) {
                // Enregistrer le joueur et afficher le message de bienvenue
                recordPlayerConnection(playerName);
                String welcomeMessage = config.getString("welcome-message");
                event.setJoinMessage(welcomeMessage.replace("%player%", playerName));
            } else {
                // Ne pas afficher de message de bienvenue
                event.setJoinMessage(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().warning("Erreur lors de la lecture ou de l'enregistrement des informations du joueur dans players.txt.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ws")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("customwelcomemessage.reload")) {
                    this.reloadConfig();
                    sender.sendMessage("§aLa configuration du message de bienvenue a été rechargée.");
                } else {
                    sender.sendMessage("§cVous n'avez pas la permission d'exécuter cette commande.");
                }
            } else {
                sender.sendMessage("§cUsage: /ws reload");
            }
            return true;
        }
        return false;
    }

    private void recordPlayerConnection(String playerName) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(playersFile, true))) {
            bw.write(playerName);
            bw.newLine();
        }
    }
}
