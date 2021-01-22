package com.claim.entity;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.text.DecimalFormat;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity // says this is a database entity
@Table(name = "student") // This is a table
public class Student {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long studentID;
	
	@Column(name = "first_name") // @Column allows you to specify the column mapping
	private String firstName = "";
	@Column(name = "last_name")
	private String lastName = "";
	@Column(name = "email")
	private String email = "";
	@Column(name = "age")
	private int age = 0;
	@Column(name = "telephone")
	private long telephone = 0L;
	@Column(name = "password")
	private String password = "";
	@Column(name = "salt")
	private String salt = "";

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public long getTelephone() {
		return telephone;
	}

	public void setTelephone(long telephone) {
		this.telephone = telephone;
	}

	public String getPassword() {
		return "encrypted"; //Retained in case Spring needs a getter.
//		return this.password; //For troubleshooting
	}

	public void setPassword(String password) {
		StringChecker check = new StringChecker();
		if (!check.isValidString(password)) {
			this.password = "";
		} 
		if (setEncryptedPassword(password.trim())) {
//System.out.println("setPassword result is: " + this.password); //For troubleshooting
		} else {
//System.out.println("setPassword result is blank");
			this.password = "";
		}
	}

	/**
	 * isCorrectPassword encodes the inputPassword with the current user's Base64
	 * encrypted salt and compares it to the current user's encrypted password. If
	 * it matches
	 * 
	 * @param inputPassword
	 * @return
	 */
	public boolean isCorrectPassword(String inputPassword) {
		StringChecker check = new StringChecker();
		if (!check.isValidString(inputPassword)) {
			return false;
		}
		// No encryption.
//		inputPassword = inputPassword.trim();
//		if (this.password.equals(inputPassword)) {
//			return true;
//		}
		// Uses encryption.
		try {
			String inputHash = generateEncryptedString(inputPassword, this.salt);
			if (inputHash.equals(this.password)) {
				return true;
			}
		} catch (Exception ex) {
			System.out.println("isCorrectPassword - Out of memory during decoding or other unspecified error.\n" + ex);
			ex.printStackTrace();
		}
		return false;
	}

	/**
	 * Method setEncryptedPassword takes a password string, generates an encrypted
	 * version of the password using a Base64 encoded salt value which it generates
	 * as well.
	 * 
	 * @param password
	 * @return
	 */
	private boolean setEncryptedPassword(String inputString) {
		StringChecker check = new StringChecker();
		if (!check.isValidString(inputString)) {
			return false;
		}
		inputString = inputString.trim();
		try {
			String salt = generateNewSalt();
//System.out.println("New salt is: " + salt);  //for troubleshooting
			String encryptedPassword = generateEncryptedString(inputString, salt);
//System.out.println("encrypted password is: " + encryptedPassword); //for troubleshooting
			this.password = encryptedPassword;
			this.salt = salt;
			return true;
		} catch (Exception ex) {
			System.out.println("setEncryptedPassword - Out of memory during decoding or other unspecified error.\n" + ex);
			ex.printStackTrace();
		}
		return false;
	}

	/**
	 * Method generateNewSalt uses a SecureRandom 8 byte value based on the SHA1PRNG
	 * algorithm, converts it to a string returns the string after it has been
	 * encoded with a Base64 encoder.
	 * 
	 * @return
	 * @throws Exception
	 */
	private String generateNewSalt() throws Exception {
		// Generate a new salt value every time a password is set.
		// Don't use Random. SecureRandom complies with FIPS 140-2.
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		// NIST recommends minimum 4 bytes. Safer to use 8 bytes.
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		return Base64.getEncoder().encodeToString(salt);
	}

	/**
	 * Method generateEncryptedString takes a inputString and Base64 encoded salt
	 * value and uses it to generate an encrypted inputString using the PBKDF2 hash
	 * algorithm and returns it as a string.
	 * 
	 * @param inputString
	 * @param salt
	 * @return
	 * @throws Exception
	 */
	private String generateEncryptedString(String inputString, String salt) throws Exception {
		// Get a encrypted inputString using PBKDF2 hash algorithm from
		// https://www.quickprogrammingtips.com/java/how-to-securely-store-passwords-in-java.html
		String algorithm = "PBKDF2WithHmacSHA1";
		int derivedKeyLength = 160; // for SHA1
		int iterations = 20000; // NIST specifies 10000, safer with 20000

		byte[] saltAsBytes = Base64.getDecoder().decode(salt);
		KeySpec spec = new PBEKeySpec(inputString.toCharArray(), saltAsBytes, iterations, derivedKeyLength);
		SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);

		byte[] encodedBytes = f.generateSecret(spec).getEncoded();
		return Base64.getEncoder().encodeToString(encodedBytes);
	}

	@Override
	public String toString() {
		String outputString = (this.firstName + " " + this.lastName + " is " + this.age + " years old.\nEmail: "
				+ this.email);
		String phoneString = Long.toString(this.telephone);
		if (phoneString.length() < 10) {
			DecimalFormat paddedPhone = new DecimalFormat("0000000000");
			phoneString = paddedPhone.format(this.telephone);
		}
		if (phoneString.length() == 10) {
			phoneString = "(" + phoneString.subSequence(0, 3) + ") " + phoneString.subSequence(3, 6) + "-"
					+ phoneString.subSequence(6, 10);
		}
		outputString += " Phone: " + phoneString;
		return outputString;
	}
}
