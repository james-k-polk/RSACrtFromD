package com.github.james_k_polk;

import java.math.BigInteger;

/**
 * This is based on a comment,
 * https://stackoverflow.com/questions/4078902/cracking-short-rsa-keys/4083501#comment84706047_4083501
 */
public class RSACrtFromDMethod2 {

    /**
     * Compute the integer square root of n, which is defined as the greatest integer
     * less than or equal to the real-valued square root of n. If n is an integer
     * perfect square, n = y*y for integer y, then y is returned, else null is.
     *
     * @param n
     * @return the integer square root of n is an integer perfect square,
     * null otherwise
     */
    public static BigInteger perfectSqrt(BigInteger n) {
        BigInteger prev = BigInteger.ZERO;
        BigInteger current = BigInteger.ONE.shiftLeft(n.bitLength() / 2);
        while (prev.subtract(current).abs().compareTo(BigInteger.ONE) > 0) {
            prev = current;
            current = current.add(n.divide(current)).shiftRight(1);
        }
        BigInteger isqrt = prev.min(current);
        if (n.equals(isqrt.multiply(isqrt))) {
            return isqrt;
        } else {
            return null;
        }
    }

    /**
     * Solve for the unknown in a quadratic polynominal using the quadratic formula,
     * x = (-b +/- sqrt(b*b - 4ac)) / 2a. We are only interest in integer solutions,
     * therefore the discriminant must be an integer perfect square and 2a must be a
     * divisor of the numerator.
     *
     * @param a
     * @param b
     * @param c
     * @return one of the two solutions if they exist, null otherwise
     */
    public static BigInteger solveQuadratic(BigInteger a, BigInteger b, BigInteger c) {
        BigInteger discriminant = b.multiply(b).subtract(a.multiply(c).shiftLeft(2));
        BigInteger isqrt = perfectSqrt(discriminant);
        if (isqrt == null) {
            return null;
        }
        BigInteger numerator = isqrt.subtract(b);
        BigInteger[] result = numerator.divideAndRemainder(a.shiftLeft(1));
        if (result[1].equals(BigInteger.ZERO)) {
            return result[0];
        } else {
            return null;
        }
    }

    /**
     * Given e, d, and n, and a guess k for ed-1==k*(p-1)*(q-1), attempt to solve for
     * p.
     *
     * @param n
     * @param e
     * @param d
     * @param k
     * @return p if it exists, null otherwise
     */
    public static BigInteger solveForP(BigInteger n, BigInteger e, BigInteger d, BigInteger k) {
        /**
         * Starting with ed - 1 == k(x-1)(n/x-1),
         * sagemath gives the coefficients of the quadratic ax*x + bx + c in x as:
         * a == k*k, b == d*e - k*n - k - 1, c == k*n
         */

        BigInteger a = k;
        BigInteger temp = k.multiply(n);
        BigInteger b = d.multiply(e).subtract(temp).subtract(k).subtract(BigInteger.ONE);
        BigInteger c = temp;
        BigInteger p = solveQuadratic(a, b, c);
        return p;
    }

    /**
     * Try different values of k in the equation ed - 1 == k(x-1)(n/x-1), hoping
     * to find one that results in a solution for x.
     *
     * @param n
     * @param e
     * @param d
     * @return x (==p) if found, null for failure.
     */
    public static BigInteger solveForPandK(BigInteger n, BigInteger e, BigInteger d) {
        /**
         * d is typically about the size of n, therefore e*d is typically about the size of
         * e*n. Therefore we try multiples of k that are around e.
         */
        for (BigInteger k = BigInteger.ONE; true; k = k.add(BigInteger.ONE)) {
//            if (k.mod(BigInteger.valueOf(500L)).equals(BigInteger.ZERO)) {
//                System.out.printf("%d,", k);
//            }
            BigInteger p = solveForP(n, e, d, k);
            if (p != null) {
                return p;
            }
        }
    }

    public static void main(String[] args) {
        BigInteger n = new BigInteger("95b103fd0839926540b28aa5fe46583f97c346121d9e082235694a080b7b5ef5e4f6293e7200993e5fac01e2b73875a36722e7dbfa198d1c44c7a2543b8c9544da190bb5fdcdecd91a1713e70a6d10aeae06468969c0b70f3c5998896b70134825211fa48596d54a4fc2e436cb9c2e82b09ac8f45e84e6f376df0bc428278a6aef103a1a3fb8d41bf591ff186c45669299f23373cca4526f140c222af358949981f8ea94cd1ee632383e2b660ae77bbb2e8d2b230f6ff059797bc6e5a9359df17c60d16f8f116d92b44370eb8ad7f048e0fbd8fb00c99dcc9f1210167639bf7e761ad9fbe263968cbc4c062e07823614854f9439a6262a65aa9cbcd132c4412f", 16);
        BigInteger e = BigInteger.valueOf(65537L);
//        BigInteger d = new BigInteger("85b19cf35076430745da70b4c2bec730bcf5fe4e4dbf885a2d440587dbe16b35c93206322c95d6487c205f6ac2e39d45bce6d318886e6bb6b4c36c7b38d85db529e9b6a8ebaf10fbf22e6c6b2cc7ae5fa5341ba9bd170f287f69323082118e57203a4e4d9db6d276d5782665be4296d3c8b1e60b43a405b5ab149928b00868b9a7da718a679335d49908bd9ea11bda770863c7a9b07647c6a0674c463c934ebaa88a32c659914f52a943a514322e4c926299ac45d55c58d69d2bb70076d27cb77db374b5a8a61a2c98d2fdfc06f7ac782f1f25e46ba48cdfb90fe58982730ba4de8a658d532d3acc7686443c2195d3f77f5e164790da2a3ee21ddb8a12e2aeb1", 16);
        BigInteger d = new BigInteger("df0ccf5b048344fdee534fcc4201a311059c63fcfa781d868efca4e391852711206b20037c88ee362c9f78230500bf637314ccef38d2e067dbd5104a2ce4cb1483c13b0ed70b9e7dd4f294bf13d3aa0b3957cd5687ce31c4f2184f5c5b7e5509c1fce96cc715b082f42a306b4f8d804d502dee15e6ce68c7f2ef6255cb593988d369da41d2ca7a7e5f79c4ad9b72b91ff8f64035902654e88383192580bf677320bde4ee2e9f4196b896aa4943d1f7bd4e69108ebfcfd1b80ddc50975695594486eefcec5fbc12d0c98babbf2b062cf86bca310bc0c12c87347e7227c9fdb694f8a1f6124cc0cd3495302789d7f8f26c29721a3239295dfe2462c423f12ddf1", 16);
        BigInteger p = solveForPandK(n, e, d);
        // sanity check
        BigInteger q = n.divide(p);
        if (!p.multiply(q).equals(n)) {
            System.out.println("Sanity check failure");
        } else {
            System.out.println(p);
        }
    }
}
