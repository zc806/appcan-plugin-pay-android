package org.zywx.wbpalmstar.plugin.uexalipay;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

public class Rsa {

	private static final String ALGORITHM = "RSA";
	private static final String PROVIDER = "BC";
	private static final String CHARSET = "UTF-8";
	private static final String SIGN_ALGORITHMS = "SHA1WithRSA";


	private static PublicKey getPublicKeyFromX509(String algorithm, String bysKey) throws NoSuchAlgorithmException, Exception {
		byte[] decodedKey = Base64.decode(bysKey);
		X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodedKey);
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		return keyFactory.generatePublic(x509);
	}

	public static String encrypt(String content, String key) {
		try {
			PublicKey pubkey = getPublicKeyFromX509(ALGORITHM, key);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubkey);
			byte plaintext[] = content.getBytes(CHARSET);
			byte[] output = cipher.doFinal(plaintext);
			String s = new String(Base64.encode(output));
			return s;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String sign(String content, String privateKey) {
		try {
			byte[] dec = Base64.decode(privateKey);
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(dec);
			KeyFactory keyf = null;
			if(null != Security.getProvider(PROVIDER)){
				keyf = KeyFactory.getInstance(ALGORITHM, PROVIDER);
			}else{
				keyf = KeyFactory.getInstance(ALGORITHM);
			}
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);
			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
			signature.initSign(priKey);
			signature.update(content.getBytes(CHARSET));
			byte[] signed = signature.sign();
			return Base64.encode(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean doCheck(String content, String sign, String publicKey) {
		try {
			KeyFactory keyf = null;
			if(null != Security.getProvider(PROVIDER)){
				keyf = KeyFactory.getInstance(ALGORITHM, PROVIDER);
			}else{
				keyf = KeyFactory.getInstance(ALGORITHM);
			}
			byte[] encodedKey = Base64.decode(publicKey);
			PublicKey pubKey = keyf.generatePublic(new X509EncodedKeySpec(encodedKey));
			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
			signature.initVerify(pubKey);
			signature.update(content.getBytes(CHARSET));
			boolean bverify = signature.verify(Base64.decode(sign));
			return bverify;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}