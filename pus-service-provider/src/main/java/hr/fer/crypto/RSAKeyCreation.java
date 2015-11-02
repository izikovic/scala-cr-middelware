package hr.fer.crypto;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAKeyCreation {

	/**
	 * @param args
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, InvalidKeySpecException,
			IOException {
		if (args.length != 1) {
			System.out.println("Usage: java RSAKeyCreation <name>");
		}

		String name = args[0];

		RSAKeyGenParameterSpec rsaKeyGenParameterSpec = new RSAKeyGenParameterSpec(
				1024, RSAKeyGenParameterSpec.F4);
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(rsaKeyGenParameterSpec);

		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		KeyFactory keyFac = KeyFactory.getInstance("RSA");

		X509EncodedKeySpec pubKey = (X509EncodedKeySpec) keyFac.getKeySpec(
				keyPair.getPublic(), X509EncodedKeySpec.class);
		PKCS8EncodedKeySpec prvKey = (PKCS8EncodedKeySpec) keyFac.getKeySpec(
				keyPair.getPrivate(), PKCS8EncodedKeySpec.class);

		byte[] pubEncoded = pubKey.getEncoded();
		byte[] prvEncoded = prvKey.getEncoded();

		DataOutputStream pubOut = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(name + ".pub")));
		DataOutputStream prvOut = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(name + ".prv")));

		pubOut.writeInt(name.getBytes().length);
		prvOut.writeInt(name.getBytes().length);

		pubOut.writeBytes(name);
		prvOut.writeBytes(name);

		pubOut.writeInt(pubEncoded.length);
		prvOut.writeInt(prvEncoded.length);

		pubOut.write(pubEncoded); // [X.509-Format]
		prvOut.write(prvEncoded); // [PKCS8-Format]

		pubOut.close();
		prvOut.close();
	}
}
