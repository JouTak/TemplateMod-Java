package ru.Jor.BeaconRadius;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;

public class BRStorage {
    private static final File StorageDir = new File(FabricLoader.getInstance().getGameDir().toFile(),"BeaconRadius");

    public static Map<BlockPos, Identifier> blockCache = new HashMap<>();

    public static void Toggle(BlockPos block,Identifier dim){
        if(blockCache.entrySet().contains((Map.entry(block,dim)))){
            blockCache.remove(block,dim);
        }else{
            blockCache.put(block,dim);
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
        //"f*ck gson" variable
        Map<String,String> tmp = new HashMap<>();

        try(FileWriter writer = new FileWriter(worldStorage)){
            Gson gson = new Gson();
            blockCache.entrySet().forEach(entry->{
                tmp.put(gson.toJson(entry.getKey()),gson.toJson(entry.getValue()));
            });
            String result = gson.toJson(tmp);

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
        //"f*ck gson" variable
        Map<String,String> tmp = new HashMap<>();

        try(JsonReader reader = new JsonReader( new FileReader(worldStorage))){
            Gson gson = new Gson();
            blockCache.clear();
            tmp = gson.fromJson(reader,blockCache.getClass());
            tmp.entrySet().forEach(entry ->{
                BlockPos pos = gson.fromJson(entry.getKey(),new TypeToken<BlockPos>() {}.getType());
                Identifier id = gson.fromJson(entry.getValue(),new TypeToken<Identifier>() {}.getType());
                blockCache.put(pos,id);

            });
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
