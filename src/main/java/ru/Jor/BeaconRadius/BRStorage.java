package ru.Jor.BeaconRadius;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import ru.Jor.BeaconRadius.BRStorage;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.include.com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BRStorage {
    private static File StorageDir = new File(FabricLoader.getInstance().getGameDir().toFile(),"BeaconRadius");

    public static List<BlockPos> blockCache = new ArrayList<>();

    private static Type listType = new TypeToken<List<BlockPos>>() {}.getType();

    public static void Toggle(BlockPos block){
        if(blockCache.contains(block)){
            blockCache.remove(block);
            BeaconRadius.LOGGER.info("removed block");
        }else{
            blockCache.add(block);
            BeaconRadius.LOGGER.info("added block");
        }
        save();
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
            Gson gson = new Gson();
            String result = gson.toJson(blockCache,listType);
            writer.write(result);
            /*
            JsonArray array = new JsonArray();
            blockCache.forEach((block)->{
                JsonObject object = new JsonObject();
                object.addProperty("x",block.getX());
                object.addProperty("y",block.getY());
                object.addProperty("z",block.getZ());
                array.add(object);}
            );
            new GsonBuilder().create().toJson(array,writer);*/
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
            Gson gson = new Gson();
            blockCache.clear();
            blockCache = gson.fromJson(reader,listType);

            /*JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            System.out.println("2");
            for(JsonElement element : array){
                System.out.println("cycle");
                JsonObject object = element.getAsJsonObject();
                blockCache.add(new BlockPos(object.get("x").getAsInt(),object.get("y").getAsInt(),object.get("z").getAsInt()));
            }*/
        } catch(Exception e) {
           BeaconRadius.LOGGER.error("Error while reading file " + e.toString());
           return false;
        }
        System.out.println(blockCache.toString());
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
