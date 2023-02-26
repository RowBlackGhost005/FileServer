package fileServerClient;

import java.util.Objects;

/**
 * Class that represents a File Packet sent by the server.
 * This class is used to envelop both parts of the packet.
 *
 * @author Luis Angel Marin
 */
public class FilePacket{

    //Index of the packet.
    private int filePacketNumber;

    //Data of the packet.
    private byte[] filePacketInfo;

    /**
     * Creates a new FilePacket using a packetNumber and packetInfo given.
     * @param packetNumber Packet number of this packet.
     * @param packetInfo Byte array that represents the Data of a file.
     */
    public FilePacket(int packetNumber , byte[] packetInfo){
        this.filePacketNumber = packetNumber;
        this.filePacketInfo = packetInfo;
    }

    public int getFilePacketNumber() {
        return filePacketNumber;
    }

    public void setFilePacketNumber(int filePacketNumber) {
        this.filePacketNumber = filePacketNumber;
    }

    public byte[] getFilePacketInfo() {
        return filePacketInfo;
    }

    public void setFilePacketInfo(byte[] filePacketInfo) {
        this.filePacketInfo = filePacketInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilePacket that = (FilePacket) o;
        return filePacketNumber == that.filePacketNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePacketNumber);
    }
}
