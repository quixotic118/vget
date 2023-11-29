package cn.lqs.vget.core.common.utils;

import cn.lqs.vget.core.exceptions.DecodeFailException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils {

    public final static String AES = "AES";
    public static final String AES_TYPE = "AES/CBC/PKCS5Padding";

    public static byte[] aesDecode(byte[] encData, byte[] key, byte[] iv) throws DecodeFailException {
        try {
            // 两个参数，第一个为私钥字节数组， 第二个为加密方式 AES或者DES
            SecretKeySpec keySpec = new SecretKeySpec(key, AES);
            // // 实例化加密类，参数为加密方式，要写全
            Cipher cipher = Cipher.getInstance(AES_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            return cipher.doFinal(encData);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new DecodeFailException(e);
        }
    }
}
