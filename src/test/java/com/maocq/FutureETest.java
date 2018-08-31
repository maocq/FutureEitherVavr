package com.maocq;

import io.vavr.concurrent.Future;
import io.vavr.control.Either;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class FutureETest {

    /**
     *  Se puede crear un FutureE desde un Future<Either<L, R>>
     */
    @Test
    public void futureEither() {
        Future<Either<String, Integer>> futureEither = futureEither(1);
        FutureE<String, Integer> futureE = FutureE.of(futureEither);

        assertEquals("Deben ser iguales", futureEither, futureE.getValue());
    }

    /**
     *  Se puede crear un FutureE desde un <Either<L, R>
     */
    @Test
    public void either() {
        Either<String, Integer> either = either(1);
        FutureE<String, Integer> futureE = FutureE.fromEither(either);

        assertEquals("Deben ser iguales", either, futureE.getValue().get());
    }

    /**
     *  Se puede crear un FutureE desde un valor (right)
     */
    @Test
    public void right() {
        Integer valor = 1;
        FutureE<String, Integer> futureE = FutureE.right(valor);

        assertEquals("Deben ser iguales", valor, futureE.getValue().get().get());
    }

    /**
     *  Se puede crear un FutureE desde un valor (left)
     */
    @Test
    public void left() {
        String valor = "=(";
        FutureE<String, Integer> futureE = FutureE.left("=(");

        assertEquals("Deben ser iguales", valor, futureE.getValue().get().getLeft());
    }

    /**
     * Se pueden componer FutureE's con flatMap
     */
    @Test
    public void flatMap() {
        Future<Either<String, Integer>> resultado = FutureE.of(futureEither(1))
          .flatMap(x -> FutureE.of(futureEither(x)))
          .flatMap(y -> FutureE.of(futureEither(y)))
          .flatMap(z -> FutureE.of(futureEither(z)))
          .getValue();

        Either<String, Integer> either = resultado.get();
        Integer esperado = 1 + 1 + 1 + 1 + 1;

        assertTrue("either debe ser right", either.isRight());
        assertEquals("either debe ser 5", esperado, either.get());
    }

    /**
     * Se pueden componer FutureE's con flatMap (Either left)
     */
    @Test
    public void flatMapLeft() {
        Future<Either<String, Integer>> resultado = FutureE.of(futureEither(1))
          .flatMap(x -> FutureE.of(futureEitherLeft(x)))  // left
          .flatMap(y -> FutureE.of(futureEither(y)))
          .getValue();

        Either<String, Integer> either = resultado.get();

        assertTrue("either debe ser left", either.isLeft());
        assertEquals("Debe ser Bummmm!", "Bummmm!", either.getLeft());
    }

    /**
     * Se pueden componer FutureE's con flatMap (Future Failure)
     */
    @Test
    public void flatMapFutureFailure() {
        Future<Either<String, Integer>> resultado = FutureE.of(futureEither(1))
          .flatMap(x -> FutureE.of(futureFailure(x)))  // Failure
          .flatMap(y -> FutureE.of(futureEither(y)))
          .getValue();

        resultado.await();

        assertTrue("resultado debe ser Failure ", resultado.isFailure());
        assertEquals("Debe ser Error =(", "Error =(", resultado.getCause().get().getMessage());
    }

    /**
     * Se pueden transformar el valor final con map
     */
    @Test
    public void map() {
        Future<Either<String, String>> resultado = FutureE.of(futureEither(1))
          .map(r -> "-" + r )
          .getValue();
        Either<String, String> either = resultado.get();
        assertTrue("either debe ser right", either.isRight());
        assertEquals("either debe ser -5", "-2", either.get());


        Future<Either<String, String>> resultadoLeft = FutureE.of(futureEitherLeft(1))
          .map(r -> "-" + r )
          .getValue();
        Either<String, String> eitherLeft = resultadoLeft.get();
        assertTrue("eitherLeft debe ser left", eitherLeft.isLeft());
        assertEquals("Debe ser Bummmm!", "Bummmm!", eitherLeft.getLeft());


        Future<Either<String, String>> resultadoFailure = FutureE.of(futureFailure(1))
          .map(r -> "-" + r )
          .getValue();
        resultadoFailure.await();
        assertTrue("resultadoFailure debe ser Failure ", resultadoFailure.isFailure());
        assertEquals("Debe ser Error =(", "Error =(", resultadoFailure.getCause().get().getMessage());
    }

    /**
     * Se puede transformar un FutureE en un Future<U> con fold
     */
    @Test
    public void fold() {
        Future<String> foldRight = FutureE.of(futureEither(1))
          .flatMap(x -> FutureE.of(futureEither(x)))
          .fold(l -> "l  " + l, r -> "r " + r);

        String resultado = foldRight.get();
        assertEquals("resultado debe ser r 3", "r 3", resultado);
    }

    @Test
    public void foldLeft() {
        Future<String> foldLeft = FutureE.of(futureEither(1))
          .flatMap(x -> FutureE.of(futureEitherLeft(x)))
          .fold(l -> "l " + l, r -> "r " + r);

        String resultado = foldLeft.get();
        assertEquals("resultado debe ser l Bummmm!", "l Bummmm!", resultado);
    }

    /**
     * Se pueden componer FutureE's con flatMap y trabajar con todos sus resultado
     */
    @Test
    public void flatMapResultados() {
        Future<Either<String, Integer>> resultado = FutureE.of(futureEither(1)).flatMap(x ->
          FutureE.of(futureEither(1)).flatMap(y ->
            FutureE.of(futureEither(1))
              .map(z -> x + y + z)
          )
        ).getValue();

        Either<String, Integer> either = resultado.get();
        Integer esperado = 2 + 2 + 2;

        assertTrue("either debe ser right", either.isRight());
        assertEquals("either debe ser 6", esperado, either.get());
    }

    @Test
    public void ejemplo() {
        //Sin FutureE, y puede ser peor =(
        Future<Either<String, Integer>> sin = futureEither(1).flatMap(xe -> xe.fold(
          error -> Future.successful(Either.left(error)),
          x -> futureEither(x).flatMap(ye -> ye.fold(
            error -> Future.successful(Either.left(error)),
            y -> futureEither(y).flatMap(ze -> ze.fold(
              error -> Future.successful(Either.left(error)),
              z -> futureEither(z)
            ))
          ))
        ));

        //Con FutureE
        Future<Either<String, Integer>> con = FutureE.of(futureEither(1))
          .flatMap(x -> FutureE.of(futureEither(x)))
          .flatMap(y -> FutureE.of(futureEither(y)))
          .flatMap(z -> FutureE.of(futureEither(z)))
          .getValue();

        Either<String, Integer> eitherSin = sin.get();
        Either<String, Integer> eitherCon = con.get();
        Integer esperado = 1 + 1 + 1 + 1 + 1;

        assertEquals("either debe ser 6", esperado, eitherSin.get());
        assertEquals("either debe ser 6", esperado, eitherCon.get());
        assertEquals("either debe ser 6", eitherSin.get(), eitherCon.get());
    }

    private static Future<Either<String, Integer>> futureEither(Integer n) {
        return Future.successful(Either.right(n + 1));
    }

    private static Future<Either<String, Integer>> futureEitherLeft(Integer n) {
        return Future.successful(Either.left("Bummmm!"));
    }

    private static Future<Either<String, Integer>> futureFailure(Integer n) {
        return Future.of(() -> {
            if (n < 100) throw new IllegalArgumentException("Error =(");
            return Either.right(n + 1);
        });
    }

    private static Either<String, Integer> either(Integer n) {
        return Either.right(n + 1);
    }

}
