package net.aufdemrand.sentry.enums;

import no.kh498.util.BitFlags;

/**
 * A enum constant of all the targets/ignore targets a sentry will attack/ignore
 * A bitmask is used to minimize the amount of memory used.
 * <p>
 * Learn more <a href=http://www.learncpp.com/cpp-tutorial/3-8a-bit-flags-and-bit-masks/>here</a>
 * (it's for c++ but the same principles and operators applies in java)
 */
public enum TargetMask {
    ALL(0x1),             /* hex for ... 0000 0000 0001 */
    PLAYERS(0x2),         /* hex for ... 0000 0000 0010 */
    NPCS(0x4),            /* hex for ... 0000 0000 0100 */
    MONSTERS(0x8),        /* hex for ... 0000 0000 1000 */
    EVENTS(0x10),         /* hex for ... 0000 0001 0000 */
    NAMED_ENTITIES(0x20), /* hex for ... 0000 0010 0000 */
    NAMED_PLAYERS(0x40),  /* hex for ... 0000 0100 0000 */
    NAMED_NPCS(0x80),     /* hex for ... 0000 1000 0000 */
    GROUPS(0x100),        /* hex for ... 0001 0000 0000 */
    OWNER(0x200),         /* hex for ... 0010 0000 0000 */
    MC_TEAMS(0x400);      /* hex for ... 0100 0000 0000 */

    private final int mask;
    TargetMask(final int mask) {
        this.mask = mask;
    }

    /**
     * @param flags      The flag to check
     * @param targetMask The mask to check
     *
     * @return {@code true} if the bit at Target.mask is 1
     */
    public static boolean test(final int flags, final TargetMask targetMask) {
        return BitFlags.test(flags, targetMask.mask);
    }

    /**
     * @param flags      The flag to check
     * @param targetMask The mask to check
     *
     * @return return {@code true} if and only if {@code flags} is equal to {@code targetMask}
     */
    public static boolean testOnly(final int flags, final TargetMask targetMask) {
        return BitFlags.testOnly(flags, targetMask.mask);
    }

    /**
     * @param flags      The flag to change
     * @param targetMask The mask to enable
     *
     * @return {@code flags} with {@code target} enabled
     */
    public static int set(final int flags, final TargetMask targetMask) {
        return BitFlags.set(flags, targetMask.mask);
    }

    /**
     * @param flags       The flag to change
     * @param targetMasks The masks to enable
     *
     * @return {@code flags} with all {@code targetMasks} enabled
     */
    public static int set(final int flags, final TargetMask... targetMasks) {
        return BitFlags.set(flags, targetMaskToInt(targetMasks));
    }

    /**
     * @param flags      The flag to change
     * @param targetMask The mask to disable
     *
     * @return {@code flags} with {@code targetMasks} disabled
     */
    public static int reset(final int flags, final TargetMask targetMask) {
        return BitFlags.reset(flags, targetMask.mask);
    }
    /**
     * @param flags       The flag to change
     * @param targetMasks The masks to disable
     *
     * @return {@code flags} with {@code targetMasks} disabled
     */
    public static int reset(final int flags, final TargetMask... targetMasks) {
        return BitFlags.reset(flags, targetMaskToInt(targetMasks));
    }

    /**
     * @param flags      The flag to change
     * @param targetMask The mask to be toggled
     *
     * @return {@code flags} with {@code target} flipped
     */
    public static int flip(final int flags, final TargetMask targetMask) {
        return BitFlags.flip(flags, targetMask.mask);
    }

    /**
     * @param flags       The flag to change
     * @param targetMasks The masks to be toggled
     *
     * @return {@code flags} with {@code target} flipped
     */
    public static int flip(final int flags, final TargetMask... targetMasks) {
        return BitFlags.flip(flags, targetMaskToInt(targetMasks));
    }

    private static int[] targetMaskToInt(final TargetMask... targetMasks) {
        final int[] targetMasksInt = new int[targetMasks.length];
        for (int i = 0; i < targetMasks.length; i++) {
            targetMasksInt[i] = targetMasks[i].mask;
        }
        return targetMasksInt;
    }
}
