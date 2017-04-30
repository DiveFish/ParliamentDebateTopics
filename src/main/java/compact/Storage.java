package compact;

import java.io.*;

/**
 *
 * @author Patricia Fischer
 */
public class Storage {

    private StorageInformation info;
    private String storageDirectory;

    public Storage(String storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    public void serialize() {
        try {
            FileOutputStream fileOut = new FileOutputStream(storageDirectory+"storageInfo.ser");
            ObjectOutputStream os = new ObjectOutputStream(fileOut);
            os.writeObject(info);
            os.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in %sstorageInfo.ser", storageDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deserialize() {
        try {
            FileInputStream fileIn = new FileInputStream(storageDirectory+"storageInfo.ser");
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

        System.out.println("Deserializing StorageInformation...");
    }

    public StorageInformation getStorageInfo() {
        return info;
    }

    public void setStorageInfo(StorageInformation info) {
        this.info = info;
    }
}