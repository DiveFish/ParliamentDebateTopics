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
        info = new StorageInformation();
    }

    public void serialize() {
        try {
            FileOutputStream fileOut = new FileOutputStream(storageDirectory+"storageInfo.ser");
            ObjectOutputStream os = new ObjectOutputStream(fileOut);
            os.writeObject(info);
            os.close();
            fileOut.close();
            System.err.printf("Serialized data is saved in %sstorageInfo.ser\n", storageDirectory);
        } catch (IOException e) {
            System.err.println("StorageDirectory not found");
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
            System.err.println("StorageInformation class not found");
            c.printStackTrace();
            return;
        }

        System.err.println("Deserializing StorageInformation...");
    }

    public StorageInformation getStorageInfo() {
        return info;
    }

    public void setStorageInfo(StorageInformation info) {
        this.info = info;
    }
}