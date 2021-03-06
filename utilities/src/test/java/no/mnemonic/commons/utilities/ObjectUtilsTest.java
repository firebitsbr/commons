package no.mnemonic.commons.utilities;

import org.junit.Test;

import java.util.concurrent.atomic.LongAdder;

import static org.junit.Assert.*;

public class ObjectUtilsTest {

  @Test
  public void notNullReturnsValueOnNotNull() throws Exception {
    Object value = new Object();
    assertEquals(value, ObjectUtils.notNull(value, "notThrown"));
    assertEquals(value, ObjectUtils.notNull(value, new IllegalArgumentException()));
  }

  @Test
  public void notNullWithMessageThrowsExceptionOnNull() {
    try {
      ObjectUtils.notNull(null, "test");
      fail();
    } catch (Exception e) {
      assertEquals("test", e.getMessage());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void notNullWithExceptionThrowsExceptionOnNull() throws Exception {
    ObjectUtils.notNull(null, new IllegalArgumentException());
  }

  @Test(expected = IllegalArgumentException.class)
  public void notNullFailsWithNullException() throws Exception {
    ObjectUtils.notNull(new Object(), (Exception) null);
  }

  @Test
  public void ifNullReturnsValueOnNotNull() {
    Object value = new Object();
    assertEquals(value, ObjectUtils.ifNull(value, "defaultValue"));
    assertEquals(value, ObjectUtils.ifNull(value, () -> "defaultValue"));
  }

  @Test
  public void ifNullReturnsDefaultValueOnNull() {
    assertEquals("defaultValue", ObjectUtils.ifNull(null, "defaultValue"));
    assertEquals("defaultValue", ObjectUtils.ifNull(null, () -> "defaultValue"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void ifNullWithoutSupplierThrowsException() {
    ObjectUtils.ifNull(42, null);
  }

  @Test
  public void ifNotNullReturnsConvertedValue() {
    assertEquals("convertedValue", ObjectUtils.ifNotNull(42, (v) -> "convertedValue"));
    assertEquals("convertedValue", ObjectUtils.ifNotNull(42, (v) -> "convertedValue", "nullValue"));
  }

  @Test
  public void ifNotNullReturnsNullValue() {
    assertNull(ObjectUtils.ifNotNull(null, (v) -> "convertedValue"));
    assertEquals("nullValue", ObjectUtils.ifNotNull(null, (v) -> "convertedValue", "nullValue"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void ifNotNullWithoutConverterThrowsException() {
    ObjectUtils.ifNotNull(42, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void ifNotNullWithNullValueWithoutConverterThrowsException() {
    ObjectUtils.ifNotNull(42, null, "nullValue");
  }

  @Test(expected = IllegalArgumentException.class)
  public void ifNotNullDoWithNullConsumerThrowsException() {
    ObjectUtils.ifNotNullDo(42, null);
  }

  @Test
  public void ifNotNullDoWithConsumer() {
    LongAdder adder = new LongAdder();
    ObjectUtils.ifNotNullDo(42L, adder::add);
    assertEquals(42, adder.longValue());
  }

  @Test
  public void ifNotNullDoWithNullValue() {
    LongAdder adder = new LongAdder();
    ObjectUtils.ifNotNullDo(null, adder::add);
    assertEquals(0, adder.longValue());
  }

}
