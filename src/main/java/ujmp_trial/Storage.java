package ujmp_trial;

import java.io.*;
/**
 * Created by patricia on 26/03/17.
 */
public class Storage {

    private StorageInformation info;

    public void serialize() {
        try {
            FileOutputStream fileOut = new FileOutputStream("/tmp/storageInfo.ser");
            ObjectOutputStream os = new ObjectOutputStream(fileOut);
            os.writeObject(info);
            os.close();
            fileOut.close();
            System.out.println("Serialized data is saved in /tmp/storageInfo.ser");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deserialize() {
        try {
            FileInputStream fileIn = new FileInputStream("/tmp/storageInfo.ser");
            ObjectInputStream is = new ObjectInputStream(fileIn);
            setStorageInfo((StorageInformation) is.readObject());
            is.close();
            fileIn.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("StorageInformation class not found");
            c.printStackTrace();
            return;
        }

        System.out.println("Deserialized StorageInformation...");
    }

    public StorageInformation getStorageInfo() {
        return info;
    }

    public void setStorageInfo(StorageInformation info) {
        this.info = info;
    }
}