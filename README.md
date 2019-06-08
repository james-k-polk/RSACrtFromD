# RSACrtFromD
Java code for factoring the RSA modulus given the RSA modulus n, and public and private
exponents e and d.

## Usage
There is just a single Java class with static methods. Grab the entire file,
or just copy & paste a method or two.

`findFactor(...)` is the centerpiece method. There is also a method `findFactorSlow(...)`
that is designed to match the text in HAC as close as possible. You might want to
run that method in your Java debugger with breakpoints set to inspect intermediate 
values.

```java
import java.math.BigInteger;

public class Examples {
    public static void main(String[] args) throws java.security.GeneralSecurityException {
        BigInteger n = new BigInteger("e000216ac013b89c70079f237899daf2e81875d68d6bcfb0ee1d19452915b57f60ded5830d608fa9b9ffa34796a043ea024b3e8388c5f20cdb4de80ebd7779f9", 16);
        BigInteger e = new BigInteger("10001", 16);
        BigInteger d = new BigInteger("c6f196bc56c7ad28d39f1149d1ace3f6e50804707fbe07021f191cfe7dd4d8121623df40d9e102f009cc6b0ba2c9b3c81caa11688f4d86ba25cd7aad0e044301", 16);
        BigInteger p = RSACrtFromD.findFactor(e, d, n);
        BigInteger q = n.divide(p);
    }
}
```



## Credit
The code is adapted from section 8.2.2(i) of the Handbook of Applied Cryptography,
by A. Menezes, P. van Oorschot, and S. Vanstone, CRC Press, 1996. The online version
of the book is at http://cacr.uwaterloo.ca/hac/, and chapter 8 is at
http://cacr.uwaterloo.ca/hac/about/chap8.pdf