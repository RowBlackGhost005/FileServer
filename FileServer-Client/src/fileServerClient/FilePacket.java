package fileServerClient;

import java.util.Objects;

public class FilePacket implements Comparable{

    private int filePacketNumber;

    private byte[] filePacketInfo;

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


    @Override
    public int compareTo(Object o) {
        FilePacket packetToCompare = (FilePacket) o;

        if(packetToCompare.getFilePacketNumber() > getFilePacketNumber()){
            return -1;
        }else if(packetToCompare.getFilePacketNumber() < getFilePacketNumber()){
            return 1;
        }else{
            return 0;
        }
    }
}
