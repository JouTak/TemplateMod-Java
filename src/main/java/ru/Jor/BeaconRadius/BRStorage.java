package ru.Jor.BeaconRadius;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import ru.Jor.BeaconRadius.BRStorage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.include.com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BRStorage {
    private static File StorageDir = new File(FabricLoader.getInstance().getGameDir().toFile(),"BeaconRadius");

    public static Map<BlockPos,Boolean> blockCache = new HashMap<>();

    public static void Toggle(BlockPos block){
        if(blockCache.containsKey(block)){
            blockCache.remove(block);
            BeaconRadius.LOGGER.info("removed block");
        }else{
            blockCache.put(block,true);
            BeaconRadius.LOGGER.info("added block");
        }
        save();
    }
    public static void setVisibility(BlockPos block, Boolean visible){
        blockCache.replace(block,visible);
    }


    public static void onJoin()
    {
        load();
    }

    private static boolean save(){
        File worldStorage = FileName();

        if(worldStorage==null) return false;
        if(!worldStorage.exists()){
            worldStorage.getParentFile().mkdirs();
        }
        try(FileWriter writer = new FileWriter(worldStorage)){
            JsonArray array = new JsonArray();
            blockCache.forEach((block,visible)->{
                JsonObject object = new JsonObject();
                object.addProperty("x",block.getX());
                object.addProperty("y",block.getY());
                object.addProperty("z",block.getZ());
                array.add(object);}
            );
            new GsonBuilder().setPrettyPrinting().create().toJson(array,writer);
        } catch(Exception e) {
            BeaconRadius.LOGGER.error("Error while writing file " + e.toString());
            return false;
        }
        return true;
    }

    public static boolean load(){
        File worldStorage = FileName();

        if(worldStorage==null) return false;
        if(!worldStorage.exists()) return true;

        try(FileReader reader = new FileReader(worldStorage)){
            blockCache.clear();
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            for(JsonElement element : array){
                JsonObject object = element.getAsJsonObject();
                blockCache.put(new BlockPos(object.get("x").getAsInt(),object.get("y").getAsInt(),object.get("z").getAsInt()),false);
            }
        } catch(Exception e) {
           BeaconRadius.LOGGER.error("Error while reading file " + e.toString());
           return false;
        }
        return true;
    }

    private static File FileName(){
        Minecraft client = Minecraft.getInstance();
        if (client.getCurrentServer()!=null){
            return new File(StorageDir,client.getCurrentServer().ip.replace(".","_").replace(":","-").toString()+".json");

        }
        else if(client.getSingleplayerServer()!=null){
            return new File(StorageDir,client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).getParent().getFileName().toString()+".json");
        }
        else return null;

    }
}
