package com.voice.config;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DistanceConfig {
    private URL savePathURL = Thread.currentThread().getContextClassLoader().getResource("");
    private String dictionary = "voiceChat";
    private static DistanceConfig config;

    public static  DistanceConfig getConfigInstance(){
        return config==null?new  DistanceConfig():config;
    }

    public void saveVoiceSettings(Map<String, Object> serializableMap,String fileName,String suffix) {
        try {
            String savePath;
            if(savePathURL.getProtocol().equals("jar")){
                savePath = savePathURL.getPath().substring(5);
            }else{
                savePath = savePathURL.getPath();
            }
            savePath = savePath.replaceAll("%20", " ");
            File configFile = new File(new File(savePath).getParent() + "/" + dictionary + "/" + fileName+suffix);
            FileOutputStream stream = new FileOutputStream(configFile);
            ObjectOutputStream obout = new ObjectOutputStream(stream);
            obout.writeObject(serializableMap);
            obout.close();
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> readConfigSettings(String fileName,String suffix) {
        try {
            String savePath;
            if(savePathURL.getProtocol().equals("jar")){
                savePath = savePathURL.getPath().substring(5);
            }else{
                savePath = savePathURL.getPath();
            }
            File file = new File(new File(savePath).getParent());
            savePath = file.getPath().replaceAll("%20", " ");
            File dic = new File(savePath + "/" + dictionary);
            File configFile = new File(dic.getPath() + "/" + fileName+suffix);
            if (!dic.exists()) {
                dic.mkdir();
                configFile.createNewFile();
                Map<String, Object> serializableMap = new HashMap<>();
                serializableMap.put(fileName, 100);
                saveVoiceSettings(serializableMap,fileName,suffix);
                return serializableMap;
            } else if (!configFile.exists()) {
                configFile.createNewFile();
                Map<String, Object> serializableMap = new HashMap<>();
                serializableMap.put(fileName, 100);
                saveVoiceSettings(serializableMap,fileName,suffix);
            }
            FileInputStream inputStream = new FileInputStream(configFile);
            ObjectInputStream obin = new ObjectInputStream(inputStream);
            Map<String, Object> serializableMap = (Map<String, Object>) obin.readObject();
            obin.close();
            inputStream.close();
            return serializableMap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
