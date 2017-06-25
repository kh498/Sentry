package net.aufdemrand.sentry.enums;

public enum Target {
    ALL(powTwo(0)),                 /* 1 */
    PLAYERS(powTwo(1)),             /* 2 */
    NPCS(powTwo(2)),                /* 4 */
    MONSTERS(powTwo(3)),            /* 8 */
    EVENTS(powTwo(4)),              /* 16 */
    NAMED_ENTITIES(powTwo(5)),      /* 32 */
    NAMED_PLAYERS(powTwo(6)),       /* 64 */
    NAMED_NPCS(powTwo(7)),          /* 128 */
    GROUPS(powTwo(11)),             /* 2048 */
    OWNER(powTwo(12)),              /* 4098 */
    MC_TEAMS(powTwo(16));           /* 65536 */

    public final int level;

    Target(final int level) {
        this.level = level;
    }

    /**
     * @param exponent The number 2 will be timed by it self
     *
     * @return {@code 2^a}
     */
    private static int powTwo(final int exponent) {
        return 1 << exponent;
    }
}
