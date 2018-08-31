package com.maocq;

import io.vavr.concurrent.Future;
import io.vavr.control.Either;

public class Hello {

    public static void main(String[] args) {

        Future<Either<String, Integer>> resuno = FutureE.of(futureEither(1))
          .flatMap(a -> FutureE.of(futureEither(a)))
          .flatMap(b -> FutureE.fromEither(metodoEither(b)))
          .flatMap(c -> FutureE.of(futureEither(c)))
          .getValue();

        Future<String> resfold = FutureE.of(futureEither(1))
          .flatMap(a -> FutureE.of(futureEither(a)))
          .flatMap(b -> FutureE.fromEither(metodoEither(b)))
          .flatMap(c -> FutureE.of(futureEither(c)))
          .fold(l -> "left", r -> "right");

        Future<String> resfoldLeft = FutureE.of(futureEither(1))
          .flatMap(a -> FutureE.of(futureEither(a)))
          .flatMap(b -> FutureE.fromEither(metodoEither(b)))
          .flatMap(c -> FutureE.of(futureFailure(c)))
          .fold(l -> "left", r -> "right");

        Future<String> resRecover = resfoldLeft.recover(error -> "Mori");

        Future<Either<String, String>> resdos = FutureE.of(futureEitherLeft()).flatMap(uno ->
          FutureE.of(futureEither(1)).map(dos -> uno + dos)
        ).getValue();

        Future<Either<String, Integer>> restres = FutureE.of(futureEither(1)).flatMap(uno ->
          FutureE.of(futureEither(1)).flatMap(dos ->
            FutureE.of(futureEither(1)).flatMap(tres ->
              FutureE.of(futureEither(1)).map(cuatro ->
                uno + dos + tres + cuatro
              )
            )
          )
        ).getValue();

    }

    private static Future<Either<String, Integer>> futureEither(Integer n) {
        return Future.successful(Either.right(n + 1));
    }

    private static Future<Either<String, String>> futureEitherLeft() {
        return Future.successful(Either.left("Bummmm!"));
    }

    private static Future<Either<String, Integer>> futureFailure(Integer n) {
        return Future.of(() -> {
            if (n < 100) throw new IllegalArgumentException("Error =(");
            return Either.right(n + 1);
        });
    }

    private static Either<String, Integer> metodoEither(Integer n) {
        return Either.right(n + 1);
    }

}
