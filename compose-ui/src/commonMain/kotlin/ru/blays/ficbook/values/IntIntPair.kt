package ru.blays.ficbook.values

/**
 * Container to ease passing around a tuple of two [Int] values.
 *
 * *Note*: This class is optimized by using a value class, a Kotlin language featured
 * not available from Java code. Java developers can get the same functionality by
 * using [Pair] or by constructing a custom implementation using Int parameters
 * directly (see [LongLongPair] for an example).
 */
@JvmInline
public value class IntIntPair internal constructor(
    @PublishedApi @JvmField internal val packedValue: Long
) {
    /**
     * Constructs a [IntIntPair] with two [Int] values.
     *
     * @param first the first value in the pair
     * @param second the second value in the pair
     */
    public constructor(first: Int, second: Int) : this(packInts(first, second))

    /**
     * The first value in the pair.
     */
    public val first: Int
        get() = (packedValue shr 32).toInt()

    /**
     * The second value in the pair.
     */
    public val second: Int
        get() = (packedValue and 0xFFFFFFFF).toInt()

    /**
     * Returns the [first] component of the pair. For instance, the first component
     * of `PairIntInt(3, 4)` is `3`.
     *
     * This method allows to use destructuring declarations when working with pairs,
     * for example:
     * ```
     * val (first, second) = myPair
     * ```
     */
    // NOTE: Unpack the value directly because using `first` forces an invokestatic
    public inline operator fun component1(): Int = (packedValue shr 32).toInt()

    /**
     * Returns the [second] component of the pair. For instance, the second component
     * of `PairIntInt(3, 4)` is `4`.
     *
     * This method allows to use destructuring declarations when working with pairs,
     * for example:
     * ```
     * val (first, second) = myPair
     * ```
     */
    // NOTE: Unpack the value directly because using `second` forces an invokestatic
    public inline operator fun component2(): Int = (packedValue and 0xFFFFFFFF).toInt()

    override fun toString(): String = "($first, $second)"
}

/**
 * Packs two Int values into one Long value for use in inline classes.
 */
inline fun packInts(val1: Int, val2: Int): Long {
    return (val1.toLong() shl 32) or (val2.toLong() and 0xFFFFFFFF)
}