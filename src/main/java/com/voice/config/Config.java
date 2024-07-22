package com.voice.config;

import java.io.*;
import java.net.URL;
import java.util.Map;

public class Config implements Serializable {
    private URL savePathURL = Thread.currentThread().getContextClassLoader().getResource("");
    private String dictionary = "voiceChat";
    private String fileName = "config.set";
    private static Config config;

    public static Config getConfigInstance(){
        return config==null?new Config():config;
    }

    public void saveVoiceSettings(Map<String, Object> serializableMap) {
        try {
            String savePath;
            if(savePathURL.getProtocol().equals("jar")){
               savePath = savePathURL.getPath().substring(5).replaceAll("%20", " ");
            }else{
                savePath=savePathURL.getPath().replaceAll("%20"," ");
            }

            File configFile = new File(new File(savePath).getParent() + "/" + dictionary + "/" + fileName);
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

    public Map<String, Object> readConfigSettings() {
        try {
            String savePath;
            if(savePathURL.getProtocol().equals("jar")){
                savePath = savePathURL.getPath().substring(5).replaceAll("%20", " ");
            }else{
                savePath=savePathURL.getPath().replaceAll("%20"," ");
            }
            File file = new File(new File(savePath).getParent());
            savePath = file.getPath().replaceAll("%20", " ");
            File dic = new File(savePath + "/" + dictionary);
            File configFile = new File(dic.getPath() + "/" + fileName);
            if (!dic.exists()) {
                dic.mkdir();
                configFile.createNewFile();
                return null;
            } else if (!configFile.exists()) {
                configFile.createNewFile();
                return null;
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
