package ca.encodeous.mwx.engines.structure;

import ca.encodeous.mwx.configuration.Missile;
import ca.encodeous.mwx.core.utils.MCVersion;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourceLoader {
    public static void LoadWorldFiles(JavaPlugin plugin) throws IOException {
        if(MCVersion.QueryVersion().getValue() < MCVersion.v1_13.getValue()){
            // load legacy world

            if(!new File(Bukkit.getWorldContainer(), "mwx_template_auto").exists())
                Unzip(plugin, "legacy_mwx_template_auto", new File(Bukkit.getWorldContainer(), "mwx_template_auto"));

            if(!new File(Bukkit.getWorldContainer(), "mwx_template_manual").exists())
                Unzip(plugin, "legacy_mwx_template_manual", new File(Bukkit.getWorldContainer(), "mwx_template_manual"));
        }else{
            if(!new File(Bukkit.getWorldContainer(), "mwx_template_auto").exists())
                Unzip(plugin, "mwx_template_auto", new File(Bukkit.getWorldContainer(), "mwx_template_auto"));

            if(!new File(Bukkit.getWorldContainer(), "mwx_template_manual").exists())
                Unzip(plugin, "mwx_template_manual", new File(Bukkit.getWorldContainer(), "mwx_template_manual"));
        }
    }
    public static void UnzipMissiles(JavaPlugin mwPlugin) throws IOException {
        Unzip(mwPlugin, "missiles", new File(mwPlugin.getDataFolder(), "missiles"));
    }

    public static void Unzip(JavaPlugin plugin, String sourceResourceFolder, File destinationFolder) throws IOException {
        if(!destinationFolder.exists()){
            destinationFolder.mkdirs();
        }
        ZipFile zip = null;
        try {
            zip = new ZipFile(
                    new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().startsWith(sourceResourceFolder + "/")) {
                File file = new File(destinationFolder,
                        entry.getName().substring((sourceResourceFolder + "/").length()));
                CopyFile(zip, entry, file);
            }
        }
        zip.close();
    }

    private static void CopyFile(ZipFile zip, ZipEntry entry, File file) throws IOException {
        if (entry.isDirectory()) {
            file.mkdirs();
        } else {
            InputStream in = zip.getInputStream(entry);
            FileOutputStream out = new FileOutputStream(file);
            IOUtils.copy(in, out);
            in.close();
            out.close();
        }
    }

    public static HashMap<String, Missile> LoadMissiles(JavaPlugin plugin){
        HashMap<String, Missile> missiles = new HashMap<>();
        if(new File(plugin.getDataFolder(), "missiles").exists()){
            File missileFolder = new File(plugin.getDataFolder(), "missiles");
            for(File f : missileFolder.listFiles()){
                try{
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
                    Missile ms = (Missile) config.get("data");
                    missiles.put(ms.MissileItemId, ms);
                }catch(Exception e){
                    // ignored
                }
            }
        }
        return missiles;
    }
    public static void SaveMissiles(JavaPlugin plugin, HashMap<String, Missile> missiles){
        File missileFolder = new File(plugin.getDataFolder(), "missiles");
        if(!missileFolder.exists()){
            missileFolder.mkdirs();
        }
        for(Missile missile : missiles.values()){
            YamlConfiguration conf = new YamlConfiguration();
            conf.set("data", missile);
            File mwconff = new File(missileFolder, missile.MissileItemId + ".yml");
            if(mwconff.exists()){
                mwconff.delete();
            }
            try {
                conf.save(mwconff);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
