package no.mnemonic.commons.utilities.lambda;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.mnemonic.commons.utilities.collections.ListUtils.list;
import static no.mnemonic.commons.utilities.lambda.LambdaUtils.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LambdaUtilsTest {
  @Mock
  private ExceptionalConsumer<Integer, MyException> consumer;
  @Mock
  private Consumer<Throwable> exceptionConsumer;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testWaitForWaitsUntilTimeout() throws Exception {
    AtomicBoolean bool = new AtomicBoolean(false);
    long t1 = System.currentTimeMillis();
    assertFalse(waitFor(bool::get, 1, TimeUnit.SECONDS));
    long t2 = System.currentTimeMillis();
    assertTrue(t2 >= t1 + 1000);
    assertTrue(t2 <= t1 + 2000);
  }

  @Test
  public void testWaitForNoWaitIfInitialTrue() throws Exception {
    AtomicBoolean bool = new AtomicBoolean(true);
    long t1 = System.currentTimeMillis();
    assertTrue(waitFor(bool::get, 1, TimeUnit.SECONDS));
    long t2 = System.currentTimeMillis();
    assertTrue(t2 <= t1 + 500);
  }

  @Test
  public void testWaitForReturnsWhenTrue() throws Exception {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    AtomicBoolean bool = new AtomicBoolean(false);
    long t1 = System.currentTimeMillis();
    Future<Boolean> value = executor.submit(() -> waitFor(bool::get, 1, TimeUnit.SECONDS));
    assertFalse(value.isDone());
    Thread.sleep(100);
    bool.set(true);
    assertTrue(value.get(500, TimeUnit.MILLISECONDS));
    long t2 = System.currentTimeMillis();
    assertTrue(t2 <= t1 + 1000);
  }

  @Test
  public void testForEachTry() throws Exception {
    forEachTry(list(1, 2, 3), consumer);
    verify(consumer, times(3)).accept(any());
  }

  @Test
  public void testForEachTryNoValues() throws Exception {
    forEachTry(null, i -> {
    });
    verify(consumer, never()).accept(any());
  }

  @Test
  public void testForEachTryNoConsumer() throws Exception {
    forEachTry(list(1, 2, 3), null);
  }

  @Test
  public void testForEachTryWithExceptionConsumer() throws Exception {
    forEachTry(list(1, 2, 3), consumer, exceptionConsumer);
    verify(consumer, times(3)).accept(any());
    verify(exceptionConsumer, never()).accept(any());
  }

  @Test
  public void testForEachTryWithExceptionConsumerOnException() throws Exception {
    doThrow(new Exception("error")).when(consumer).accept(any());
    forEachTry(list(1, 2, 3), consumer, exceptionConsumer);
    verify(consumer, times(3)).accept(any());
    verify(exceptionConsumer, times(3)).accept(any());
  }

  @Test
  public void testTryStreamCollect() throws Exception {
    assertEquals(list(1, 2, 3), tryStream(list(1, 2, 3).stream()).collect(Collectors.toList()));
  }

  @Test
  public void testTryStreamMapToLong() throws Exception {
    assertEquals(6L, tryStream(list(1, 2, 3).stream()).mapToLong(Integer::longValue).sum());
  }

  @Test
  public void testTryStreamMapToInt() throws Exception {
    assertEquals(6, tryStream(list(1, 2, 3).stream()).mapToInt(v -> v).sum());
  }

  @Test
  public void testTryStreamForEach() throws Exception {
    tryStream(list(1, 2, 3).stream()).forEach(i -> consumer.accept(i));
    verify(consumer).accept(1);
    verify(consumer).accept(2);
    verify(consumer).accept(3);
  }

  @Test
  public void testTryStreamWithNullInput() throws Exception {
    assertNull(tryStream((Stream<? extends Object>) null));
  }

  @Test
  public void testTryStreamWithExceptionalFunctionOnCollect() throws Exception {
    assertEquals(list(1L, 2L, 3L), tryStream(list(1, 2, 3).stream()).map(this::myFunction).collect(Collectors.toList()));
  }

  @Test
  public void testTryStreamWithExceptionalPredicateOnCollect() throws Exception {
    assertEquals(list(1, 2, 3), tryStream(list(1, 2, 3).stream()).filter(this::myPredicate).collect(Collectors.toList()));
  }

  @Test(expected = MyException.class)
  public void testTryStreamMappingThrowingExceptionOnCollect() throws Exception {
    tryStream(list(1, 2, 3).stream()).map(this::myErrorFunction).collect(Collectors.toList());
  }

  @Test(expected = MyException.class)
  public void testTryStreamPredicateThrowingExceptionOnCollect() throws Exception {
    tryStream(list(1, 2, 3).stream()).filter(this::myErrorPredicate).collect(Collectors.toList());
  }

  @Test(expected = MyException.class)
  public void testTryStreamPredicateThrowingExceptionOnForEach() throws Exception {
    tryStream(list(1, 2, 3).stream()).filter(this::myErrorPredicate).forEach(e -> consumer.accept(e));
  }

  @Test(expected = MyException.class)
  public void testTryStreamConsumerThrowingExceptionOnForEach() throws Exception {
    tryStream(list(1, 2, 3).stream()).forEach(this::myErrorConsumer);
  }

  @Test
  public void testTryTo() throws Exception {
    assertTrue(tryTo(() -> myFunction(1)));
  }

  @Test
  public void testTryToWithException() throws Exception {
    assertFalse(tryTo(() -> myErrorFunction(1)));
  }

  @Test
  public void testTryToWithExceptionConsumer() throws Exception {
    assertFalse(tryTo(() -> myErrorFunction(1), exceptionConsumer));
    verify(exceptionConsumer).accept(any());
  }

  @Test
  public void testTryToWithNullExceptionConsumer() throws Exception {
    assertFalse(tryTo(() -> myErrorFunction(1), null));
  }

  @Test
  public void testTryToWithNullInput() throws Exception {
    assertFalse(tryTo(null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTryResultWithNullSupplierThrowsException() {
    tryResult(null, "default");
  }

  @Test
  public void testTryResultWithNullDefaultValue() {
    assertEquals("value", tryResult(() -> "value", "default"));
  }

  @Test
  public void testTryResultWithNullValue() {
    assertNull(tryResult(() -> null, "default"));
  }

  @Test
  public void testTryResultWithExceptionReturnsDefault() {
    assertEquals("default", tryResult(() -> {
      throw new RuntimeException();
    }, "default"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTryResultWithNullDefaultSupplierThrowsException() {
    tryResult(()->"value", null, e->{});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTryResultWithNullExceptionHandlerThrowsException() {
    tryResult(()->"value", ()->"default", null);
  }

  @Test
  public void testTryResultWithInvokesExceptionHandler() {
    //noinspection unchecked
    Consumer<Throwable> exceptionHandler = mock(Consumer.class);
    assertEquals("default", tryResult(() -> {
      throw new RuntimeException();
    }, ()->"default", exceptionHandler));
    verify(exceptionHandler).accept(isA(RuntimeException.class));
  }

  //helper methods

  private long myFunction(int input) throws MyException {
    return (long) input;
  }

  private long myErrorFunction(int input) throws MyException {
    throw new MyException();
  }

  private void myErrorConsumer(int input) throws MyException {
    throw new MyException();
  }

  private boolean myPredicate(int input) throws MyException {
    return true;
  }

  private boolean myErrorPredicate(int input) throws MyException {
    throw new MyException();
  }

  private static class MyException extends Exception {
  }

}
