/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import java.util.NoSuchElementException;
import java.util.Objects;

import net.sourceforge.pmd.lang.ast.GenericToken;
import net.sourceforge.pmd.lang.ast.impl.javacc.JavaccToken;

/**
 * PRIVATE FOR NOW, find out what is useful to move to the interface
 * (probably everything).
 *
 * @author Clément Fournier
 */
final class TokenUtils {

    // mind: getBeginLine and getEndLine on JavaccToken are now very slow.

    private TokenUtils() {

    }

    public static <T extends GenericToken<T>> T nthFollower(T token, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Negative index?");
        }
        while (n-- > 0 && token != null) {
            token = token.getNext();
        }
        if (token == null) {
            throw new NoSuchElementException("No such token");
        }

        return token;
    }

    /**
     * This is why we need to doubly link tokens... otherwise we need a
     * start hint.
     *
     * @param startHint Token from which to start iterating,
     *                  needed because tokens are not linked to their
     *                  previous token. Must be strictly before the anchor
     *                  and as close as possible to the expected position of
     *                  the anchor.
     * @param anchor    Anchor from which to apply the shift. The n-th previous
     *                  token will be returned
     * @param n         An int > 0
     *
     * @throws NoSuchElementException If there's less than n tokens to the left of the anchor.
     */
    // test only
    public static <T extends GenericToken<T>> T nthPrevious(T startHint, T anchor, int n) {
        validateInput(startHint, anchor, n);
    
        int numAway = 0;
        T target = startHint;
        T current = startHint;
    
        while (current != null && !current.equals(anchor)) {
            current = current.getNext();
            if (numAway == n) {
                target = target.getNext();
            } else {
                numAway++;
            }
        }
    
        validateOutput(current, anchor, numAway, n);
    
        return target;
    }
    
    private static <T extends GenericToken<T>> void validateInput(T startHint, T anchor, int n) {
        if (startHint.compareTo(anchor) >= 0) {
            throw new IllegalStateException("Wrong left hint, possibly not left enough");
        }
        if (n <= 0) {
            throw new IllegalArgumentException("Offset can't be less than 1");
        }
    }
    
    private static <T extends GenericToken<T>> void validateOutput(T current, T anchor, int numAway, int n) {
        if (!Objects.equals(current, anchor)) {
            throw new IllegalStateException("Wrong left hint, possibly not left enough");
        }
        if (numAway != n) {
            throw new NoSuchElementException("No such token");
        }
    }    

    public static void expectKind(JavaccToken token, int kind) {
        assert token.kind == kind : "Expected " + token.getDocument().describeKind(kind) + ", got " + token;
    }

}
