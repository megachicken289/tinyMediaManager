/*
 * Copyright 2012 - 2017 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.core;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class AesUtil {
  private final int    keySize;
  private final int    iterationCount;
  private final Cipher cipher;

  protected AesUtil(int keySize, int iterationCount) {
    this.keySize = keySize;
    this.iterationCount = iterationCount;
    try {
      cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }
    catch (Exception e) {
      throw fail(e);
    }
  }

  protected String encrypt(String salt, String iv, String passphrase, String plaintext) {
    try {
      SecretKey key = generateKey(salt, passphrase);
      byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, key, iv, plaintext.getBytes("UTF-8"));
      return base64(encrypted);
    }
    catch (UnsupportedEncodingException e) {
      throw fail(e);
    }
  }

  protected String decrypt(String salt, String iv, String passphrase, String ciphertext) {
    try {
      SecretKey key = generateKey(salt, passphrase);
      byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, key, iv, base64(ciphertext));
      return new String(decrypted, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw fail(e);
    }
  }

  private byte[] doFinal(int encryptMode, SecretKey key, String iv, byte[] bytes) {
    try {
      cipher.init(encryptMode, key, new IvParameterSpec(hex(iv)));
      return cipher.doFinal(bytes);
    }
    catch (Exception e) {
      throw fail(e);
    }
  }

  private SecretKey generateKey(String salt, String passphrase) {
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), hex(salt), iterationCount, keySize);
      SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
      return key;
    }
    catch (Exception e) {
      throw fail(e);
    }
  }

  protected static String random(int length) {
    byte[] salt = new byte[length];
    new SecureRandom().nextBytes(salt);
    return hex(salt);
  }

  protected static String base64(byte[] bytes) {
    return DatatypeConverter.printBase64Binary(bytes);
  }

  protected static byte[] base64(String str) {
    return DatatypeConverter.parseBase64Binary(str);
  }

  protected static String hex(byte[] bytes) {
    return DatatypeConverter.printHexBinary(bytes);
  }

  protected static byte[] hex(String str) {
    return DatatypeConverter.parseHexBinary(str);
  }

  private IllegalStateException fail(Exception e) {
    return new IllegalStateException(e);
  }
}
