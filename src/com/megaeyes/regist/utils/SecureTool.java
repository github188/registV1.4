package com.megaeyes.regist.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SecureTool
{
  public static String encrypt(String content)
  {
    MessageDigest md = null;
    try
    {
      md = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException nsae)
    {
    	nsae.printStackTrace();
    }

    md.update(content.getBytes());
    byte[] bytes = md.digest();

    StringBuffer buff = new StringBuffer();
    for (int i = 0; i < bytes.length; i++)
    {
      String hexString = Integer.toHexString(bytes[i] & 0XFF);
      buff.append(hexString.length() == 2 ? hexString : "0" + hexString);
    }
    

    return buff.toString();
  }


  public static void main(String[] args)
  {
    if (args.length == 0)
    {
      args = new String[]
             {"111111","megaeyes", "12345678", "abc", "biwei", "123", "abc", "biwei",
             "abcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz123456", "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890",
      };

    }
    for (int i = 0; i < args.length; i++)
    {
      System.out.println("[" + args[i] + "]:[" + encrypt(args[i]) + "]");
    }
  }
}