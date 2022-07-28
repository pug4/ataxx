/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.ArrayList;

import java.util.Random;

import static ataxx.PieceColor.*;


/**
 * A Player that computes its own moves.
 *
 * @author Jayu Patel
 */
class AI extends Player {

    /**
     * Maximum minimax search depth before going to static evaluation.
     */
    private static final int MAX_DEPTH = 4;
    /**
     * A position magnitude indicating a win (for red if positive, blue
     * if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     * a random-number generator for use in move computations.  Identical
     * seeds produce identical behaviour.
     */


    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _rows = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g'};
        _cols = new char[]{'1', '2', '3', '4', '5', '6', '7'};
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /**
     * The move found by the last call to the findMove method
     * above.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _foundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _foundMove. If the game is over
     * on BOARD, does not set _foundMove.
     */
    private int minMax(Board board, int depth,
                       boolean saveMove, int sense, int alpha, int beta) {
        /* We use WINNING_VALUE + depth as the winning value so as to favor
         * wins that happen sooner rather than later (depth is larger the
         * fewer moves have been made. */
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        Move best;
        best = null;
        int bestScore;
        bestScore = 0;
        ArrayList<Move> legalMoves = new ArrayList<Move>();
        for (char i : _rows) {
            for (char j : _cols) {
                if (board.get(i, j) == board.whoseMove()) {
                    for (int k = -2; k <= 2; k++) {
                        for (int l = -2; l <= 2; l++) {
                            char kChar = (char) (k + j);
                            char lChar = (char) (l + i);
                            if (board.legalMove(i, j, lChar, kChar)) {
                                Move m = Move.move(i, j, lChar, kChar);
                                legalMoves.add(m);
                            }
                        }
                    }
                }
            }
        }
        int scoreA = alpha;
        int scoreB = beta;
        for (Move g : legalMoves) {
            if (sense == -1) {
                board.makeMove(g);
                bestScore = minMax(board, depth - 1, false, 1, scoreA, scoreB);
                if (bestScore < scoreB) {
                    scoreB = bestScore;
                    if (saveMove) {
                        _lastFoundMove = g;
                    }
                }
            } else if (sense == 1) {
                board.makeMove(g);
                bestScore = minMax(board, depth - 1, false, -1, scoreA, scoreB);
                if (bestScore > scoreA) {
                    scoreA = bestScore;
                    if (saveMove) {
                        _lastFoundMove = g;
                    }
                }

            }
            board.undo();
        }
        return staticScore(board, WINNING_VALUE + depth);
    }

    /**
     * Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     * won positions, and 0 for ties.
     */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        }
        return board.numPieces(board.whoseMove())
                - board.numPieces(board.whoseMove().opposite());
    }
    /**
     * all possible rows.
     */
    private char[] _rows;
    /**
     * all possible columns.
     */
    private char[] _cols;

    /**
     * Pseudo-random number generator for move computation.
     */
    private Random _random = new Random();
}
