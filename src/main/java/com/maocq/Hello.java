package com.maocq;

import io.vavr.concurrent.Future;
import io.vavr.control.Either;

public class Hello {

    public static void main(String[] args) {

        Future<String> fold = FutureE.of(futureEither(1))
          .flatMap(x -> FutureE.of(futureEither(x)))
          .fold(l -> "Error", Object::toString);
    }

    private static Future<Either<String, Integer>> futureEither(Integer n) {
        return Future.successful(Either.right(n + 1));
    }

}
