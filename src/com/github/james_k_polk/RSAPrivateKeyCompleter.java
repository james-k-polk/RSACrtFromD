/*
MIT License

Copyright (c) 2017 President James K. Polk

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.github.james_k_polk;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;

public class RSAPrivateKeyCompleter {

    /**
     * Find a factor of n by following the algorithm outlined in Handbook of Applied Cryptography, section
     * 8.2.2(i). See http://cacr.uwaterloo.ca/hac/about/chap8.pdf.
     *
     * @param e the RSA public exponent.
     * @param d the RSA private exponent.
     * @param n the RSA modulus.
     * @return a BigInteger non-trivial proper factor of n
     */
    private static BigInteger findFactor(BigInteger e, BigInteger d, BigInteger n) {
        BigInteger edMinus1 = e.multiply(d).subtract(BigInteger.ONE);
        int s = edMinus1.getLowestSetBit();
        int s2 = numberOfTrailingZeros(edMinus1);
        assert s == s2;
        BigInteger t = edMinus1.shiftRight(s);
        assert t.shiftLeft(s).equals(edMinus1);

        for (int aInt = 2; true; aInt++) {
            BigInteger aPow = BigInteger.valueOf(aInt).modPow(t, n);
            for (int i = 1; i <= s; i++) {
                if (aPow.equals(BigInteger.ONE)) {
                    break;
                }
                if (aPow.equals(n.subtract(BigInteger.ONE))) {
                    break;
                }
                BigInteger aPowSquared = aPow.multiply(aPow).mod(n);
                if (aPowSquared.equals(BigInteger.ONE)) {
                    return aPow.subtract(BigInteger.ONE).gcd(n);
                }
                aPow = aPowSquared;
            }
        }

    }

    /**
     * Create a complete RSA CRT private key from a non-CRT RSA private key by using
     * an algorithm to factor the modulus and then computing each of the remaining
     * CRT parameters.
     *
     * @param rsaPub  RSA public key,includes public exponent e and modulus n.
     * @param rsaPriv RSA private key, include private exponent d and modulus n.
     * @return an RSAPrivateCrtKey containing all the CRT parameters.
     */

    public static RSAPrivateCrtKey createCrtKey(RSAPublicKey rsaPub, RSAPrivateKey rsaPriv) throws NoSuchAlgorithmException, InvalidKeySpecException {

        BigInteger e = rsaPub.getPublicExponent();
        BigInteger d = rsaPriv.getPrivateExponent();
        BigInteger n = rsaPub.getModulus();
        BigInteger p = findFactor(e, d, n);
        BigInteger q = n.divide(p);
        if (p.compareTo(q) > 0) {
            BigInteger t = p;
            p = q;
            q = t;
        }
        assert p.multiply(q).equals(n);
        BigInteger exp1 = d.mod(p.subtract(BigInteger.ONE));
        BigInteger exp2 = d.mod(q.subtract(BigInteger.ONE));
        BigInteger coeff = q.modInverse(p);
        RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(n, e, d, p, q, exp1, exp2, coeff);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateCrtKey) kf.generatePrivate(keySpec);

    }

    private static int numberOfTrailingZeros(BigInteger x) {
        int result = 0;
        if (x.signum() == 0) {
            return -1;
        }
        while (!x.testBit(0)) {
            ++result;
            x = x.shiftRight(1);
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        while (true) {
            KeyPair keyPair = kpg.generateKeyPair();
            RSAPublicKey rsaPub = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey) keyPair.getPrivate();
            RSAPrivateCrtKey rsaPrivateCrtKey1 = createCrtKey(rsaPub, rsaPrivateCrtKey);
            assert keyEquals(rsaPrivateCrtKey, rsaPrivateCrtKey1);
        }
    }

    private static boolean keyEquals(RSAPrivateCrtKey k1, RSAPrivateCrtKey k2) {
        boolean result = true;
        result = result && k1.getModulus().equals(k2.getModulus());
        result = result && k1.getPublicExponent().equals(k2.getPublicExponent());
        result = result && k1.getPrivateExponent().equals(k2.getPrivateExponent());
        result = result && k1.getPrimeP().equals(k2.getPrimeP());
        result = result && k1.getPrimeQ().equals(k2.getPrimeQ());
        result = result && k1.getPrimeExponentP().equals(k2.getPrimeExponentP());
        result = result && k1.getPrimeExponentQ().equals(k2.getPrimeExponentQ());
        result = result && k1.getCrtCoefficient().equals(k2.getCrtCoefficient());

        return result;
    }
    private static void printKey(RSAPrivateCrtKey rsaPriv) {
        System.out.printf("n = %x%n", rsaPriv.getModulus());
        System.out.printf("e = %x%n", rsaPriv.getPublicExponent());
        System.out.printf("d = %x%n", rsaPriv.getPrivateExponent());
        System.out.printf("p = %x%n", rsaPriv.getPrimeP());
        System.out.printf("q = %x%n", rsaPriv.getPrimeQ());
        System.out.printf("exp1 = %x%n", rsaPriv.getPrimeExponentP());
        System.out.printf("exp2 = %x%n", rsaPriv.getPrimeExponentQ());
        System.out.printf("coeff = %x%n", rsaPriv.getCrtCoefficient());
    }
}
