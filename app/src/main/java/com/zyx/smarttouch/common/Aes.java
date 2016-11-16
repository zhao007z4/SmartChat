package com.zyx.smarttouch.common;


import android.os.Build;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Aes
{
    private final static String iv = "1234567898765432";
	public static String encrypt(String sKey, String sData)
	{
		try
		{
			byte[] key = sKey.getBytes("UTF-8");
			byte[] data = sData.getBytes("UTF-8");
			
			byte[] result = encrypt(key, data);

            return Base64.encodeToString(result,Base64.DEFAULT);

		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static String decrypt(String sKey, String sData)
	{
		try
		{
			byte[] key = sKey.getBytes("UTF-8");
            byte[] data = sData.getBytes("UTF-8");
			byte[] newData = Base64.decode(data,Base64.DEFAULT);


			byte[] result = decrypt(key, newData);
			if(result==null)
			{
				return null;
			}
			return new String(result,"UTF-8");
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
		
	private static byte[] encrypt(byte[] key, byte[] data)
	{
		byte[] encrypted = null;
		try
		{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            encrypted = cipher.doFinal(data);
		} 
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} 
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		} 
		catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} 
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (BadPaddingException e)
		{
			e.printStackTrace();
		}
        catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
		return encrypted;
	}

	private static byte[] decrypt(byte key[], byte[] data)
	{
		byte[] decrypted = null;
		try
		{

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

            decrypted= cipher.doFinal(data);
		} 
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} 
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
		} 
		catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} 
		catch (IllegalBlockSizeException e)
		{
			e.printStackTrace();
		} catch (BadPaddingException e)
		{
			e.printStackTrace();
		}
        catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
		return decrypted;
	}

	public static byte[] toByte(String hexString)
	{
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
		{
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),16).byteValue();
		}
		return result;
	}
	
	private static char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8','9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String HexFromBytes(byte[] bytes) 
	{
		char[] hexChars = new char[bytes.length * 2];
		int j = 0;
		for (int i = 0; i < bytes.length; i++) 
		{
			int v = bytes[i] & 0xFF;
			hexChars[(j++)] = hexArray[(v >> 4)];
			hexChars[(j++)] = hexArray[(v & 0xF)];
		}
		return new String(hexChars);
	}

	public static String md5(String string) {
		byte[] hash = null;
		try {
			hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if(hash==null)
		{
			return null;
		}
		BigInteger bigInt = new BigInteger(1, hash);
		return bigInt.toString(16);

//        StringBuilder hex = new StringBuilder(hash.length * 2);
//        for (byte b : hash) {
//            if ((b & 0xFF) < 0x10) hex.append("0");
//            hex.append(Integer.toHexString(b & 0xFF));
//        }
//        return hex.toString();
	}

	public static boolean isRegedit()
	{
		return true;
	}

	public static String getBrand()
	{
		return Build.BRAND;
	}

	public static String getProduct()
	{
		return Build.PRODUCT;
	}
}
