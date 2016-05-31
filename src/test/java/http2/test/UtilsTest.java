package http2.test;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class UtilsTest {

  @Test
  public void testParse() {
    BigInteger _1K = BigInteger.valueOf(1024);
    assertEquals(BigInteger.valueOf(567), Utils.parseSize("567"));
    assertEquals(BigInteger.valueOf(2).multiply(_1K), Utils.parseSize("2kb"));
    assertEquals(BigInteger.valueOf(7).multiply(_1K).multiply(_1K), Utils.parseSize("7mb"));
    assertEquals(BigInteger.valueOf(3).multiply(_1K).multiply(_1K).multiply(_1K), Utils.parseSize("3gb"));
  }
}
