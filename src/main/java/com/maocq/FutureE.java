package com.maocq;

import io.vavr.concurrent.Future;
import io.vavr.control.Either;

import java.util.Objects;
import java.util.function.Function;

public class FutureE<L, R> {

    private final Future<Either<L, R>> value;

    private FutureE(Future<Either<L, R>> value) {
        this.value = value;
    }

    public Future<Either<L, R>> getValue() {
        return value;
    }

    public static <L, R> FutureE<L, R> of(Future<Either<L, R>> value) {
        return new FutureE<>(value);
    }

    public static <L, R> FutureE<L, R> fromEither(Either<L, R> either) {
        return new FutureE<>(Future.successful(either));
    }

    public static <L, R> FutureE<L, R> right(R r) {
        return new FutureE<>(Future.successful(Either.right(r)));
    }

    public static <L, R> FutureE<L, R> left(L l) {
        return new FutureE<>(Future.successful(Either.left(l)));
    }

    public <U> FutureE<L, U> map(Function<? super R, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return new FutureE<>(value.map(either -> either.map(mapper)));
    }

    public <U> FutureE<U, R> mapLeft(Function<? super L, ? extends U> leftMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        return new FutureE<>(value.map(either -> either.mapLeft(leftMapper)));
    }

    public <U> FutureE<L, U> flatMap(Function<? super R, ? extends FutureE<L, U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return new FutureE<>(value.flatMap(either -> either.fold(
          l -> Future.successful(Either.left(l)),
          r -> mapper.apply(r).getValue()
        )));
    }

    public <U> Future<U> fold(Function<? super L, ? extends U> leftMapper, Function<? super R, ? extends U> rightMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        Objects.requireNonNull(rightMapper, "rightMapper is null");
        return value.map(either -> either.fold(leftMapper, rightMapper));
    }

    public <X, Y> FutureE<X, Y> bimap(Function<? super L, ? extends X> leftMapper, Function<? super R, ? extends Y> rightMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        Objects.requireNonNull(rightMapper, "rightMapper is null");
        return new FutureE<>(value.map(either -> either.bimap(leftMapper, rightMapper)));
    }

    public FutureE<L, R> recover(Function<? super Throwable, ? extends Either<L, R>> f) {
        Objects.requireNonNull(f, "f is null");
        return new FutureE<>(value.recover(f));
    }

}