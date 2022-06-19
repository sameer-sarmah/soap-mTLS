package northwind.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Objects;

public class SecurityUtil {
	public static void analyseKeystore(KeyStore keyStore,List<String> publicKeys,String privateKeyName) {
		try {
			analyseCertificate(keyStore,publicKeys);
			Key privateKey = keyStore.getKey(privateKeyName, "password".toCharArray());
			if(Objects.nonNull(privateKey)) {
			System.out.println(String.format("algorithm : %s,format : %s",privateKey.getAlgorithm(),privateKey.getFormat()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void analyseCertificate(KeyStore keyStore,List<String> publicKeys) {
		try {
			System.out.println(String.format("Size of keystore: %s, type of keystore: %s ",keyStore.size(),keyStore.getType()));
			publicKeys.stream().forEach((publicKey) ->{
				try {
					Certificate clientCertificate = keyStore.getCertificate(publicKey);
					analyseCertificate(clientCertificate);
				} catch (KeyStoreException e) {
					e.printStackTrace();
				}	
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void analyseCertificate(Certificate certificate) {
		if(Objects.nonNull(certificate)){
			PublicKey serverPublicKey = certificate.getPublicKey();
			System.out.println(String.format("algorithm : %s,format : %s",serverPublicKey.getAlgorithm(),serverPublicKey.getFormat()));
			try {
				certificate.verify(serverPublicKey);
			} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
					| SignatureException e) {
				e.printStackTrace();
			}
		}
	}
}
