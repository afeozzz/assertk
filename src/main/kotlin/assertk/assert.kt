package assertk

import assertk.assertions.support.show

class Assert<out T> internal constructor(val name: String? = null, val actual: T)

sealed class AssertBlock<out T> {
    abstract fun thrownError(f: Assert<Throwable>.() -> Unit)
    abstract fun returnedValue(f: Assert<T>.() -> Unit)

    class Value<out T> internal constructor(private val value: T) : AssertBlock<T>() {
        override fun thrownError(f: Assert<Throwable>.() -> Unit) = fail("expected exception but was:${show(value)}")

        override fun returnedValue(f: Assert<T>.() -> Unit) {
            f(Assert(actual = value))
        }
    }

    class Error<out T> internal constructor(private val error: Throwable) : AssertBlock<T>() {
        override fun thrownError(f: Assert<Throwable>.() -> Unit) {
            f(Assert(actual = error))
        }

        override fun returnedValue(f: Assert<T>.() -> Unit) = fail("expected value but threw:${show(error)}")
    }
}

fun <T> assert(actual: T, name: String? = null): Assert<T> = Assert(name, actual)

fun <T> assert(actual: T, name: String? = null, f: Assert<T>.() -> Unit) {
    FailureContext.run(SoftFailure()) {
        f(Assert(name, actual))
    }
}

fun <T> assert(f: () -> T): AssertBlock<T> {
    return FailureContext.run(SoftFailure()) {
        try {
            AssertBlock.Value(f())
        } catch (error: Throwable) {
            AssertBlock.Error(error)
        }
    }
}

fun assertAll(f: () -> Unit) {
    FailureContext.run(SoftFailure(), f)
}


fun catch(f: () -> Unit): Throwable? {
    try {
        f()
        return null
    } catch (e: Throwable) {
        return e
    }
}
