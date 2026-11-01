package ru.Jor.BeaconRadius;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BRStorage {
    private static File StorageDir = new File(FabricLoader.getInstance().getGameDir().toFile(),"BeaconRadius");

    public static List<BlockPos> blockCache = new ArrayList<>();

    private static Type listType = new TypeToken<List<BlockPos>>() {}.getType();

    public static void Toggle(BlockPos block){
        if(blockCache.contains(block)){
            blockCache.remove(block);
        }else{
            blockCache.add(block);
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
        } catch(Exception e) {
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
        } catch(Exception e) {
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
