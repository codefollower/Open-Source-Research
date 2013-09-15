package test;

import java.util.*;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class Security {
	public static boolean check411(byte[] reply, byte[] hash_stage2, byte[] seeds) {
    	MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} //$NON-NLS-1$
        md.update(seeds);
        md.update(hash_stage2);

        byte[] hash_stage1 = md.digest();
        int numToXor = hash_stage1.length;
        for (int i = 0; i < numToXor; i++) {
        	hash_stage1[i] = (byte) (hash_stage1[i] ^ reply[i]);
        }
        
        md.reset();
        byte[] candidate_hash2 = md.digest(hash_stage1);
        return equals(candidate_hash2, hash_stage2);
    }
	static byte[] scramble411(String password, String seed) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$

		byte[] passwordHashStage1 = md.digest(password.getBytes());
		md.reset();

		byte[] passwordHashStage2 = md.digest(passwordHashStage1);
		md.reset();

		byte[] seedAsBytes = seed.getBytes("ASCII"); // for debugging
		md.update(seedAsBytes);
		md.update(passwordHashStage2);

		byte[] toBeXord = md.digest();

		int numToXor = toBeXord.length;

		for (int i = 0; i < numToXor; i++) {
			toBeXord[i] = (byte) (toBeXord[i] ^ passwordHashStage1[i]);
		}

		return toBeXord;
	}
	
	public static byte[] sha1Sha1(byte[] password) throws Exception {
    	MessageDigest md = MessageDigest.getInstance("SHA-1");

    	password = md.digest(password);
    	md.reset();
    	return md.digest(password);
    }
	
	public static boolean equals(byte[] a, byte[] a2) {
        if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++)
            if (a[i] != a2[i])
                return false;

        return true;
    }
}