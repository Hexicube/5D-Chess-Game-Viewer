package org.hexicube.chess5d

import java.util.ArrayList
import java.util.HashMap
import kotlin.math.min

class GameState {
    val timelines = HashMap<Int, ArrayList<Board>>()
    
    fun findAllLines(lineArray: ArrayList<Int>, ply: Boolean) {
        timelines.forEach { line, boardList ->
            val last = boardList.last()
            if (last.ply == ply) lineArray.add(line)
        }
        lineArray.sort()
    }
    
    private fun trySpeculate(oldSpec: Pair<Int,Int>?, sX: Int, sY: Int, newSpec: Triple<Int,Int,Pair<Piece,Boolean>>?, type: Piece, ply: Boolean, errType: String): Pair<Int,Int>? {
        if (newSpec == null) return oldSpec
        if (type != newSpec.third.first) return oldSpec
        if (ply != newSpec.third.second) return oldSpec
        if (sX != -1 && sX != newSpec.first) return oldSpec
        if (sY != -1 && sY != newSpec.second) return oldSpec
        if (oldSpec != null) throw IllegalArgumentException("Unable to interpret move: More than one $errType can do this move")
        return Pair(newSpec.first, newSpec.second)
    }
    
    private fun withBasicMove(move: String, ply: Boolean, board: Board): GameState {
        // no cross-line or time-travel
        var theMove = move
        
        // castling
        // NOTE: no checks to make sure it is still actually legal (neither king nor rook moved)
        if (theMove == "O-O") {
            val rank = if (ply) 7 else 0
            var p = board.getPiece(4, rank)
            if (p == null || p.first != Piece.KING || p.second != ply)
                throw IllegalArgumentException("Unable to interpret move: e${rank+1} is not a ${if (ply) "black" else "white"} king")
            p = board.getPiece(7, rank)
            if (p == null || p.first != Piece.ROOK || p.second != ply)
                throw IllegalArgumentException("Unable to interpret move: h${rank+1} is not a ${if (ply) "black" else "white"} rook")
            if (board.getPiece(5, rank) != null || board.getPiece(6, rank) != null)
                throw IllegalArgumentException("Unable to interpret move: f${rank+1} and g${rank+1} are not both vacant")
            board.movePiece(7, rank, 5, rank, false, false)
            board.movePiece(4, rank, 6, rank, false)
            timelines[board.line]!!.add(board)
            return this
        }
        if (theMove == "O-O-O") {
            val rank = if (ply) 7 else 0
            var p = board.getPiece(4, rank)
            if (p == null || p.first != Piece.KING || p.second != ply)
                throw IllegalArgumentException("Unable to interpret move: e${rank+1} is not a ${if (ply) "black" else "white"} king")
            p = board.getPiece(0, rank)
            if (p == null || p.first != Piece.ROOK || p.second != ply)
                throw IllegalArgumentException("Unable to interpret move: a${rank+1} is not a ${if (ply) "black" else "white"} rook")
            if (board.getPiece(1, rank) != null || board.getPiece(2, rank) != null || board.getPiece(3, rank) != null)
                throw IllegalArgumentException("Unable to interpret move: b${rank+1}, c${rank+1}, and d${rank+1} are not all vacant")
            board.movePiece(0, rank, 3, rank, false, false)
            board.movePiece(4, rank, 2, rank, false)
            timelines[board.line]!!.add(board)
            return this
        }
    
        var thePiece = Piece.PAWN
        Piece.values().forEach {
            if (theMove.startsWith(it.identifier)) {
                thePiece = it
                return@forEach
            }
        }
        if (thePiece != Piece.PAWN) theMove = theMove.substring(1)
    
        var isCapture = false
        val withoutCap = theMove.replace("x", "")
        if (theMove != withoutCap) {
            isCapture = true
            theMove =  withoutCap
        }
    
        var startX = -1
        var startY = -1
        var endX = -1
        var endY = -1
        when (theMove.length) {
            2 -> {
                // unambiguous source
                if (thePiece == Piece.PAWN) {
                    // pawn move
                    if (isCapture) {
                        startX = theMove[0] - 'a'
                        endX = theMove[1] - 'a'
                    }
                    else {
                        endX = theMove[0] - 'a'
                        endY = theMove[1] - '1'
                        startX = endX
                    }
                }
                else {
                    endX = theMove[0] - 'a'
                    endY = theMove[1] - '1'
                }
            }
            3 -> {
                // ambiguous source, rank or file specified for source
                if (thePiece == Piece.PAWN) {
                    // pawn capture
                    startX = theMove[0] - 'a'
                    endX = theMove[1] - 'a'
                    endY = theMove[2] - '1'
                    startY = endY + if (ply) 1 else -1
                }
                else {
                    if (theMove[0].isDigit()) { // rank
                        startY = theMove[0] - '1'
                        endX = theMove[1] - 'a'
                        endY = theMove[2] - '1'
                    }
                    else { // file
                        startX = theMove[0] - 'a'
                        endX = theMove[1] - 'a'
                        endY = theMove[2] - '1'
                    }
                }
            }
            4 -> {
                // ambiguous source, rank AND file specified for source
                startX = theMove[0] - 'a'
                startY = theMove[1] - '0'
                endX = theMove[2] - 'a'
                endY = theMove[3] - '0'
            }
        }
    
        if (startX == -1 || startY == -1) {
            // find the only possible piece
            var speculate: Pair<Int,Int>? = null
            when (thePiece) {
                Piece.PAWN -> {
                    if (isCapture && startY == -1 && endY == -1) {
                        var specStart = -1
                        var pos = 0
                        while (true) {
                            if (pos >= board.pieces.size) break
                            val p = board.getPiece(startX, pos)
                            if (p != null) {
                                if (p.first == Piece.PAWN && p.second == ply) {
                                    if (specStart == -1) specStart = pos
                                    else throw IllegalArgumentException("Unable to interpret move: More than one pawn can do this move")
                                }
                            }
                            pos++
                        }
                        if (specStart == -1) throw IllegalStateException("Unable to interpret move: Failed to find the moving piece on board L${board.line}T${board.time}")
                        startY = specStart
                        endY = startY + if (ply) -1 else 1
                    }
                    else if (isCapture)
                        throw IllegalArgumentException("Unable to interpret move: Pawn move specified with ambiguity on capture that should be impossible, can only be ambiguous files on captures")
                    else {
                        // pawn moved one or two spaces forward - assume a +2 is legal no matter what
                        val mod = if (ply) 1 else -1
                        val possiblePawn = board.getPiece(startX, endY+mod)
                        val possiblePawn2 = board.getPiece(startX, endY+mod+mod)
                        startY = when {
                            possiblePawn?.first == Piece.PAWN && possiblePawn.second == ply -> endY+mod
                            possiblePawn != null -> throw IllegalArgumentException("Unable to interpret move: The space behind the target is occupied by a non-pawn")
                            possiblePawn2?.first == Piece.PAWN && possiblePawn2.second == ply -> endY+mod+mod
                            else -> throw IllegalArgumentException("Unable to interpret move: There is no pawn within 2 spaces to move forward")
                        }
                    }
                }
                Piece.ROOK -> {
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 1, 0), Piece.ROOK, ply, "rook")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, -1, 0), Piece.ROOK, ply, "rook")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 0, 1), Piece.ROOK, ply, "rook")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 0, -1), Piece.ROOK, ply, "rook")
                }
                Piece.KNIGHT -> {
                    val possibles = arrayOf(
                        Pair(endX+2,endY+1),
                        Pair(endX+2,endY-1),
                        Pair(endX-2,endY+1),
                        Pair(endX-2,endY-1),
                        Pair(endX+1,endY+2),
                        Pair(endX+1,endY-2),
                        Pair(endX-1,endY+2),
                        Pair(endX-1,endY-2)
                    )
                    for (pair in possibles) {
                        if (startX != -1 && pair.first != startX) continue
                        if (startY != -1 && pair.second != startY) continue
                        val p = board.getPiece(pair.first, pair.second)
                        if (p == null || p.first != Piece.KNIGHT || p.second != ply) continue
                        if (speculate != null) throw IllegalArgumentException("Unable to interpret move: More than one knight can do this move")
                        speculate = pair
                    }
                }
                Piece.BISHOP -> {
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 1, 1), Piece.BISHOP, ply, "bishop")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, -1, 1), Piece.BISHOP, ply, "bishop")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 1, -1), Piece.BISHOP, ply, "bishop")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, -1, -1), Piece.BISHOP, ply, "bishop")
                }
                Piece.QUEEN -> {
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 1, 0), Piece.QUEEN, ply, "queen")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, -1, 0), Piece.QUEEN, ply, "queen")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 0, 1), Piece.QUEEN, ply, "queen")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 0, -1), Piece.QUEEN, ply, "queen")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 1, 1), Piece.QUEEN, ply, "queen")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, -1, 1), Piece.QUEEN, ply, "queen")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 1, -1), Piece.QUEEN, ply, "queen")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, -1, -1), Piece.QUEEN, ply, "queen")
                }
                Piece.KING -> {
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 1, 0, 1), Piece.KING, ply, "king")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, -1, 0, 1), Piece.KING, ply, "king")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 0, 1, 1), Piece.KING, ply, "king")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 0, -1, 1), Piece.KING, ply, "king")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 1, 1, 1), Piece.KING, ply, "king")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, -1, 1, 1), Piece.KING, ply, "king")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, 1, -1, 1), Piece.KING, ply, "king")
                    speculate = trySpeculate(speculate, startX, startY, board.raySearch(endX, endY, -1, -1, 1), Piece.KING, ply, "king")
                }
                Piece.UNICORN -> throw IllegalArgumentException("Unable to interpret move: Unicorns are incapable of basic moves")
                Piece.DRAGON -> throw IllegalArgumentException("Unable to interpret move: Dragons are incapable of basic moves")
            }
            if (speculate == null) {
                if (startX == -1 || startY == -1) {
                    // debug stuff
                    board.print()
                    println("${thePiece.identifier}${(endX+'a'.toInt()).toChar()}${endY+1}")
                    throw IllegalStateException("Unable to interpret move: Failed to find the moving piece on board L${board.line}T${board.time}")
                }
            }
            else {
                startX = speculate.first
                startY = speculate.second
            }
        }
        
        board.movePiece(startX, startY, endX, endY, isCapture)
        timelines[board.line]!!.add(board)
        return this
    }
    
    var mostRecentMove = ""
    fun withMove(move: ArrayList<String>, ply: Boolean, lines: ArrayList<Int>): GameState {
        if (move.count() == 0) return this // should not happen, but just in case it does...
        print(move.joinToString(" "))
        mostRecentMove = move.joinToString(" ")
        when (move.count()) {
            1 -> {
                if (move[0] == "#") return this // checkmate declared, effectively a null move
                if (lines.isEmpty()) throw IllegalStateException("Unable to interpret move: No lines left to move on")
                val line = lines.removeAt(0)
                if (move[0] == "-") return this // pass on most recent
                val tl = timelines[line] ?: throw IllegalStateException("Unable to interpret move: Missing timeline L$line")
                val board = tl.last()
                if (board.ply != ply) throw IllegalStateException("Unable to interpret move: L$line is the wrong ply")
                return withBasicMove(move[0], ply, board.clone())
            }
            2 -> {
                if (lines.isEmpty()) throw IllegalStateException("Unable to interpret move: No lines left to move on")
                // timeline specified due to options, but otherwise a basic move
                if (!move[0].startsWith('L')) throw IllegalArgumentException("Unable to interpret move: First segment is not a timeline")
                val line = move[0].substring(1).toInt()
                if (!lines.remove(line)) println("WARN: Unable to interpret move: L$line not in the list of available lines")
                val board = timelines[line]?.last() ?: throw IllegalArgumentException("Unable to interpret move: Missing timeline L$line")
                if (board.ply != ply) throw IllegalArgumentException("Unable to interpret move: Wrong player's turn on L$line")
                return withBasicMove(move[1], ply, board.clone())
            }
            3 -> {
                if (lines.isEmpty()) throw IllegalStateException("Unable to interpret move: No lines left to move on")
                // time travel or cross-line move
                // example: L0d6 Bx T10d4
                
                var sourceStr = move[0]
                if (!sourceStr.startsWith('L')) throw IllegalArgumentException("Unable to interpret move: Source has no line indicator")
                sourceStr = sourceStr.substring(1)
                val line = sourceStr.substring(0, sourceStr.length-2).toInt()
                if (!lines.remove(line)) println("WARN: Unable to interpret move: L$line not in the list of available lines")
                val sourceBoard = timelines[line]?.last() ?: throw IllegalArgumentException("Unable to interpret move: Missing timeline L$line")
                if (sourceBoard.ply != ply) throw IllegalArgumentException("Unable to interpret move: Wrong player's turn on L$line")
                val sourceX = sourceStr[sourceStr.length-2] - 'a'
                val sourceY = sourceStr[sourceStr.length-1] - '1'
                
                var pieceStr = move[1]
                var isCapture = false
                val newPieceStr = pieceStr.replace("x", "")
                if (newPieceStr.length != pieceStr.length) {
                    pieceStr = newPieceStr
                    isCapture = true
                }
                var thePiece = Piece.PAWN
                Piece.values().forEach {
                    if (pieceStr.startsWith(it.identifier)) {
                        thePiece = it
                        return@forEach
                    }
                }
                
                var targStr = move[2]
                var targLine = line
                var targTime = sourceBoard.time
                var targX = sourceX
                var targY = sourceY
                if (targStr.startsWith('L')) {
                    targStr = targStr.substring(1)
                    var l = if (targStr.startsWith('-')) 2 else 1
                    var res = 0
                    while (true) {
                        try {
                            res = targStr.substring(0, l).toInt()
                            l++
                        }
                        catch (_: NumberFormatException) {
                            l--
                            break
                        }
                    }
                    targStr = targStr.substring(l)
                    targLine = res
                }
                if (targStr.startsWith('T')) {
                    targStr = targStr.substring(1)
                    var l = 1
                    var res = 0
                    while (true) {
                        try {
                            res = targStr.substring(0, l).toInt()
                            l++
                        }
                        catch (_: NumberFormatException) {
                            l--
                            break
                        }
                    }
                    targStr = targStr.substring(l)
                    targTime = res
                }
                if (targStr.isNotEmpty()) {
                    targX = targStr[0] - 'a'
                    targY = targStr[1] - '1'
                }
                val targTimeline = timelines[targLine] ?: throw IllegalArgumentException("Unable to interpret move: Missing timeline L$targLine")
                val targBoard = targTimeline.first {
                    it.ply == ply && it.time == targTime
                }
                val targLineLast = targTimeline.last()
                val isTimeTravel = targBoard != targLineLast
                
                val diffX = targX - sourceX
                val diffY = targY - sourceY
                val diffLine = targLine - sourceBoard.line
                val diffTime = targTime - sourceBoard.time
                
                //println()
                //println("DEBUG: source(L${sourceBoard.line}T${sourceBoard.time}${(sourceX+'a'.toInt()).toChar()}${sourceY+1})")
                //println("DEBUG: type(${thePiece.identifier}${if (isCapture) "x" else ""})")
                //println("DEBUG: target(L${targLine}T$targTime${(targX+'a'.toInt()).toChar()}${targY+1})($targLine|$targTime|$targX|$targY)")
                //println("DEBUG: delta(L${diffLine}T$diffTime $diffX $diffY)")
    
                when (thePiece) {
                    Piece.PAWN -> {
                        if (diffX == 0 && diffY == 0) {
                            // spacetime move
                            if (isCapture) {
                                if (Math.abs(diffTime) != 1)  throw IllegalArgumentException("Unable to interpret move: Pawn attempted to move incorrectly")
                                if (diffLine != if (ply) 1 else -1) throw IllegalArgumentException("Unable to interpret move: Pawn attempted to move incorrectly")
                            }
                            else {
                                if (diffTime != 0)  throw IllegalArgumentException("Unable to interpret move: Pawn attempted to move incorrectly")
                                if (ply) {
                                    if (diffLine != 1 && diffLine != 2) throw IllegalArgumentException("Unable to interpret move: Pawn attempted to move incorrectly")
                                } else {
                                    if (diffLine != -1 && diffLine != -2) throw IllegalArgumentException("Unable to interpret move: Pawn attempted to move incorrectly")
                                }
                            }
                        }
                        else {
                            // would be a regular move, but that should not use this syntax
                            throw IllegalArgumentException("Unable to interpret move: Pawn attempted to move incorrectly")
                        }
                    }
                    Piece.ROOK -> {
                        var numZero = 0
                        if (diffX == 0) numZero++
                        if (diffY == 0) numZero++
                        if (diffLine == 0) numZero++
                        if (diffTime == 0) numZero++
                        if (numZero != 3) throw IllegalArgumentException("Unable to interpret move: Rook attempted to move incorrectly")
                    }
                    Piece.KNIGHT -> {
                        var found1 = false
                        var found2 = false
                        when (diffX) {
                            1, -1 -> found1 = true
                            2, -2 -> found2 = true
                            0 -> {}
                            else -> throw IllegalArgumentException("Unable to interpret move: Knight attempted to move incorrectly")
                        }
                        when (diffY) {
                            1, -1 -> { if (!found1) found1 = true else throw IllegalArgumentException("Unable to interpret move: Knight attempted to move incorrectly") }
                            2, -2 -> { if (!found2) found2 = true else throw IllegalArgumentException("Unable to interpret move: Knight attempted to move incorrectly") }
                            0 -> {}
                            else -> throw IllegalArgumentException("Unable to interpret move: Knight attempted to move incorrectly")
                        }
                        when (diffLine) {
                            1, -1 -> { if (!found1) found1 = true else throw IllegalArgumentException("Unable to interpret move: Knight attempted to move incorrectly") }
                            2, -2 -> { if (!found2) found2 = true else throw IllegalArgumentException("Unable to interpret move: Knight attempted to move incorrectly") }
                            0 -> {}
                            else -> throw IllegalArgumentException("Unable to interpret move: Knight attempted to move incorrectly")
                        }
                        when (diffTime) {
                            1, -1 -> { if (!found1) found1 = true else throw IllegalArgumentException("Unable to interpret move: Knight attempted to move incorrectly") }
                            2, -2 -> { if (!found2) found2 = true else throw IllegalArgumentException("Unable to interpret move: Knight attempted to move incorrectly") }
                            0 -> {}
                            else -> throw IllegalArgumentException("Unable to interpret move: Knight attempted to move incorrectly")
                        }
                        if (!found1 || !found2) throw IllegalArgumentException("Unable to interpret move: Knight attempted to move incorrectly")
                    }
                    Piece.BISHOP -> {
                        var expect = 0
                        var count = 0
                        if (diffX != 0) {
                            expect = Math.abs(diffX)
                            count++
                        }
                        if (diffY != 0) {
                            if (expect != 0 && Math.abs(diffY) != expect) throw IllegalArgumentException("Unable to interpret move: Bishop attempted to move incorrectly")
                            if (expect == 0) expect = Math.abs(diffY)
                            count++
                        }
                        if (diffLine != 0) {
                            if (expect != 0 && Math.abs(diffLine) != expect) throw IllegalArgumentException("Unable to interpret move: Bishop attempted to move incorrectly")
                            if (expect == 0) expect = Math.abs(diffLine)
                            count++
                        }
                        if (diffTime != 0) {
                            if (expect != 0 && Math.abs(diffTime) != expect) throw IllegalArgumentException("Unable to interpret move: Bishop attempted to move incorrectly")
                            count++
                        }
                        if (count != 2) throw IllegalArgumentException("Unable to interpret move: Bishop attempted to move incorrectly")
                    }
                    Piece.QUEEN -> {
                        var expect = 0
                        if (diffX != 0) expect = Math.abs(diffX)
                        if (diffY != 0) {
                            if (expect != 0 && Math.abs(diffY) != expect) throw IllegalArgumentException("Unable to interpret move: Queen attempted to move incorrectly")
                            if (expect == 0) expect = Math.abs(diffY)
                        }
                        if (diffLine != 0) {
                            if (expect != 0 && Math.abs(diffLine) != expect) throw IllegalArgumentException("Unable to interpret move: Queen attempted to move incorrectly")
                            if (expect == 0) expect = Math.abs(diffLine)
                        }
                        if (diffTime != 0) {
                            if (expect != 0 && Math.abs(diffTime) != expect) throw IllegalArgumentException("Unable to interpret move: Queen attempted to move incorrectly")
                            if (expect == 0) expect = Math.abs(diffTime)
                        }
                        if (expect == 0) throw IllegalArgumentException("Unable to interpret move: Queen attempted to move incorrectly")
                    }
                    Piece.KING -> {
                        if (diffX == 0 && diffY == 0 && diffLine == 0 && diffTime == 0)
                            throw IllegalArgumentException("Unable to interpret move: King attempted to move incorrectly")
                        if (Math.abs(diffX) > 1 || Math.abs(diffY) > 1 || Math.abs(diffLine) > 1 || Math.abs(diffTime) > 1)
                            throw IllegalArgumentException("Unable to interpret move: King attempted to move incorrectly")
                    }
                    Piece.UNICORN -> {
                        var expect = 0
                        var count = 0
                        if (diffX != 0) {
                            expect = Math.abs(diffX)
                            count++
                        }
                        if (diffY != 0) {
                            if (expect != 0 && Math.abs(diffY) != expect) throw IllegalArgumentException("Unable to interpret move: Unicorn attempted to move incorrectly")
                            if (expect == 0) expect = Math.abs(diffY)
                            count++
                        }
                        if (diffLine != 0) {
                            if (expect != 0 && Math.abs(diffLine) != expect) throw IllegalArgumentException("Unable to interpret move: Unicorn attempted to move incorrectly")
                            if (expect == 0) expect = Math.abs(diffLine)
                            count++
                        }
                        if (diffTime != 0) {
                            if (expect != 0 && Math.abs(diffTime) != expect) throw IllegalArgumentException("Unable to interpret move: Unicorn attempted to move incorrectly")
                            count++
                        }
                        if (count != 3) throw IllegalArgumentException("Unable to interpret move: Unicorn attempted to move incorrectly")
                    }
                    Piece.DRAGON -> {
                        if (diffX == 0 || Math.abs(diffX) != Math.abs(diffY) || Math.abs(diffX) != Math.abs(diffLine) || Math.abs(diffX) != Math.abs(diffTime))
                            throw IllegalArgumentException("Unable to interpret move: Bishop attempted to move incorrectly")
                    }
                }
                // move has passed basic validation
                // TODO: check for obstructions
                
                val newSourceBoard = sourceBoard.clone()
                newSourceBoard.moveFromBoard(sourceX, sourceY, thePiece, BoardTarget(targTime, targLine, targX, targY))
                timelines[newSourceBoard.line]!!.add(newSourceBoard)
                val newTargetBoard = targBoard.clone()
                newTargetBoard.moveToBoard(targX, targY, thePiece, isCapture, BoardTarget(sourceBoard.time, sourceBoard.line, sourceX, sourceY))
                if (isTimeTravel) {
                    // make a new line
                    val newLine = if (ply) (timelines.keys.min()!!-1) else (timelines.keys.max()!!+1)
                    val newBoardList = ArrayList<Board>()
                    newBoardList.add(newTargetBoard)
                    newTargetBoard.line = newLine
                    timelines[newLine] = newBoardList
                }
                else {
                    timelines[newTargetBoard.line]!!.add(newTargetBoard)
                    if (!lines.remove(newTargetBoard.line)) throw IllegalStateException("Unable to interpret move: L$line not in the list of available lines")
                }
                return this
                
                // sourceBoard, sourceX, sourceY
                // thePiece, isCapture
                // targBoard, targX, targY
                // isTimeTravel
                
                /*
                Possible formats:
                L0a1 Bx T10d4 - Implied same line
                L0a1 Bx L1a2 - Implied same time
                L0a1 Bx L1T12 - Implied same square
                L0a1 Qx L1T12d4 - Verbose
                */
            }
            else -> throw IllegalArgumentException("Unable to interpret move: More than 3 segments")
        }
    }
    
    fun clone(): GameState {
        val newState = GameState()
        timelines.onEach {
            newState.timelines[it.key] = ArrayList(it.value.map { it.clone(true) })
        }
        return newState
    }
    
    // (activePos, activeNeg, curPresent)
    fun getLineInformation(ply: Boolean): Triple<Int,Int,Int> {
        var numPos = 0
        var numNeg = 0
        timelines.forEach {
            when {
                it.key > 0 -> numPos++
                it.key < 0 -> numNeg++
            }
        }
        val activePos = min(numPos, -numNeg+1)
        val activeNeg = min(-numNeg, numPos+1)
        val earliest = timelines.mapNotNull {
            val lastBoard = it.value.last()
            if (lastBoard.ply == ply) lastBoard.line else null
        }.min() ?: Int.MAX_VALUE
        return Triple(activePos, activeNeg, earliest)
    }
    
    companion object {
        fun startPosition(): GameState {
            val resp = GameState()
            resp.timelines[0] = arrayListOf(
                Board(
                    1, 0, false,
                    arrayOf<Array<Pair<Piece, Boolean>?>>(
                        arrayOf(
                            Pair(Piece.ROOK, false), Pair(Piece.KNIGHT, false), Pair(Piece.BISHOP, false), Pair(Piece.QUEEN, false), Pair(Piece.KING, false), Pair(Piece.BISHOP, false), Pair(Piece.KNIGHT, false), Pair(Piece.ROOK, false)
                        ),
                        arrayOf(
                            Pair(Piece.PAWN, false), Pair(Piece.PAWN, false), Pair(Piece.PAWN, false), Pair(Piece.PAWN, false), Pair(Piece.PAWN, false), Pair(Piece.PAWN, false), Pair(Piece.PAWN, false), Pair(Piece.PAWN, false)
                        ),
                        arrayOf(
                            null, null, null, null, null, null, null, null
                        ),
                        arrayOf(
                            null, null, null, null, null, null, null, null
                        ),
                        arrayOf(
                            null, null, null, null, null, null, null, null
                        ),
                        arrayOf(
                            null, null, null, null, null, null, null, null
                        ),
                        arrayOf(
                            Pair(Piece.PAWN, true), Pair(Piece.PAWN, true), Pair(Piece.PAWN, true), Pair(Piece.PAWN, true), Pair(Piece.PAWN, true), Pair(Piece.PAWN, true), Pair(Piece.PAWN, true), Pair(Piece.PAWN, true)
                        ),
                        arrayOf(
                            Pair(Piece.ROOK, true), Pair(Piece.KNIGHT, true), Pair(Piece.BISHOP, true), Pair(Piece.QUEEN, true), Pair(Piece.KING, true), Pair(Piece.BISHOP, true), Pair(Piece.KNIGHT, true), Pair(Piece.ROOK, true)
                        )
                    )
                )
            )
            return resp
        }
    }
}