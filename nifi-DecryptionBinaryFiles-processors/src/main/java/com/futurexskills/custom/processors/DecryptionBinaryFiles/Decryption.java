package com.futurexskills.custom.processors.DecryptionBinaryFiles;


import org.apache.nifi.processor.io.StreamCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Decryption implements StreamCallback {

    public String BinarPath;

    public byte[] BytesChid;

    public void setBytesChid(byte[] bytes){
        this.BytesChid = bytes;
    }

    public byte[] getBytesChid(){return BytesChid;}
    public void setBinarPath(String Path){
        this.BinarPath = Path;
    }

    public String getBinarPath(){
        return BinarPath;
    }


    @Override
    public void process(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bytes_bracket_open = {40}; // UTF_8: (
        byte[] bytes_bracket_open_sec = {44,40}; // UTF_8: ,(
        byte[] bytes_status_false = {44, 48, 41}; // UTF_8: ,0)
        byte[] bytes_status_true = {44, 49, 41}; // UTF_8: ,1)
        byte byte_good_status = 0x30; // значение 48, СВБУ записывает такое значение, как хорошее
        byte[] bytes_text_func = {44}; // UTF_8: ,
        byte[] binaryContent = new byte[18];

        byte[] bytes_chid = getBytesChid();
        int count = inputStream.available()/18;
        inputStream.readNBytes(binaryContent, 0, 18);
        outputStream.write(bytes_bracket_open);
        outputStream.write(Decryption.getBytesFromInt(bytes_chid[0], bytes_chid[1], bytes_chid[2], bytes_chid[3]));


        outputStream.write(bytes_text_func);
        outputStream.write(getBytesFromTS(binaryContent[4],binaryContent[5],binaryContent[6],binaryContent[7]));
        outputStream.write(getBytesFromShort(binaryContent[8],binaryContent[9]));
        outputStream.write(getBytesFromFloat(binaryContent[12],binaryContent[13],binaryContent[14],binaryContent[15]));
        if ((binaryContent[16] == byte_good_status)) {
            outputStream.write(bytes_status_false);
        } else {
            outputStream.write(bytes_status_true);
        }

        for (int par = 1; par < count; par++)
        {
            inputStream.readNBytes(binaryContent, 0, 18);
            outputStream.write(bytes_bracket_open_sec);

            int pos_chid = par * 4;
            outputStream.write(getBytesFromInt(bytes_chid[pos_chid], bytes_chid[pos_chid+1], bytes_chid[pos_chid+2], bytes_chid[pos_chid+3]));

            outputStream.write(bytes_text_func);
            outputStream.write(Decryption.getBytesFromTS(binaryContent[4],binaryContent[5],binaryContent[6],binaryContent[7]));

            outputStream.write(Decryption.getBytesFromShort(binaryContent[8],binaryContent[9]));
            outputStream.write(Decryption.getBytesFromFloat(binaryContent[12],binaryContent[13],binaryContent[14],binaryContent[15]));
            if ((binaryContent[16] == byte_good_status)) {
                outputStream.write(bytes_status_false);
            } else {
                outputStream.write(bytes_status_true);
            }
        }
    }

    static byte[] getBytesFromShort(byte b0, byte b1) {
        if (b0 == 0x00 && b1 == 0x00) {
            return new byte[]{48, 48, 48, 44};
        } else {
            short value = (short) ((b1 << 8) | (b0 & 0xFF));
            byte[] arr = String.valueOf(value).getBytes(StandardCharsets.UTF_8);
            if (arr.length == 3) {
                return new byte[]{arr[0], arr[1], arr[2], 44};
            } else if (arr.length == 2) {
                return new byte[]{48, arr[0], arr[1], 44};
            } else {
                return new byte[]{48, 48, arr[0], 44};
            }
        }
    }

    static byte[] getBytesFromTS(byte b0, byte b1, byte b2, byte b3) {
        int value = (b3 << 24) | ((b2 & 0xFF) << 16) | ((b1 & 0xFF) << 8) | (b0 & 0xFF);
        return String.valueOf(value).getBytes(StandardCharsets.UTF_8);
    }

    static byte[] getBytesFromInt(byte b0, byte b1, byte b2, byte b3) {
        int value = (b3 << 24) | ((b2 & 0xFF) << 16) | ((b1 & 0xFF) << 8) | (b0 & 0xFF);
        return String.valueOf(value).getBytes(StandardCharsets.UTF_8);
    }

    static byte[] getBytesFromFloat (byte b0, byte b1, byte b2, byte b3) {
        int value = (b3 << 24) | ((b2 & 0xFF) << 16) | ((b1 & 0xFF) << 8) | (b0 & 0xFF);
        return String.valueOf(Float.intBitsToFloat(value)).getBytes(StandardCharsets.UTF_8);
    }


}
