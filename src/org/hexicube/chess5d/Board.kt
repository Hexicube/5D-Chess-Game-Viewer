package org.hexicube.chess5d

enum class Piece (val identifier: Char, val imgX: Int) {
    PAWN('P', 0),
    ROOK('R', 3),
    KNIGHT('N', 1),
    BISHOP('B', 2),
    QUEEN('Q', 4),
    KING('K', 5),
    UNICORN('U', 6),
    DRAGON('D', 7);
}

data class BoardTarget(val time: Int, val line: Int, val ply: Boolean, val x: Int, val y: Int)

data class Board(var time: Int, var line: Int, var ply: Boolean, val pieces: Array<Array<Pair<Piece, Boolean>?>>) {
    private var locked = false
    private fun lock() { locked = true }
    var moveStart: Pair<Int,Int>? = null
    var moveEnd: Pair<Int,Int>? = null
    var moveTravelData: BoardTarget? = null
    
    fun print() {
        println("+--+--+--+--+--+--+--+--+")
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                print("|")
                val p = getPiece(x, 7-y)
                if (p == null) print("  ")
                else print("${if (p.second) "B" else "W"}${p.first.identifier}")
            }
            println("|")
            println("+--+--+--+--+--+--+--+--+")
        }
    }
    
    fun getPiece(file: Int, rank: Int): Pair<Piece, Boolean>? {
        return try {
            pieces[rank][file]
        } catch (_: IndexOutOfBoundsException) {
            null
        }
    }
    
    fun raySearch(startFile: Int, startRank: Int, moveFile: Int, moveRank: Int, limit: Int = -1): Triple<Int, Int, Pair<Piece, Boolean>>? {
        try {
            var file = startFile
            var rank = startRank
            var dist = 0
            while (true) {
                file += moveFile
                rank += moveRank
                dist++
                if (limit > 0 && dist > limit) return null
                val p = pieces[rank][file]
                if (p != null) return Triple(file, rank, p)
            }
        } catch (_: IndexOutOfBoundsException) {
            return null
        }
    }
    
    fun movePiece(startFile: Int, startRank: Int, endFile: Int, endRank: Int, capture: Boolean, progressTurn: Boolean = true) {
        if (locked) throw IllegalStateException("Illegal move on L$line T$time ${(startFile+'a'.toInt()).toChar()}$startRank: Board locked")
        
        val start = pieces[startRank][startFile] ?: throw IllegalArgumentException("Illegal move on L$line T$time ${(startFile+'a'.toInt()).toChar()}$startRank: No piece to move")
        if (start.second != ply) throw IllegalArgumentException("Illegal move on L$line T$time ${(startFile+'a'.toInt()).toChar()}$startRank: Piece belongs to opponent")
        val end = pieces[endRank][endFile]
        if (capture) {
            if (end == null) {
                val enP = pieces[endRank][startFile]
                if (start.first == Piece.PAWN && enP != null && enP.first == Piece.PAWN && enP.second != ply) {
                    // en passant assumed - it is the only piece allowed to capture onto a blank square
                    pieces[endRank][startFile] = null
                }
                else println("WARN: Illegal move on L$line T$time ${(startFile+'a'.toInt()).toChar()}$startRank: Destination ${(endFile+'a'.toInt()).toChar()}$endRank is empty")
            }
            else if (end.second == ply) throw IllegalArgumentException("Illegal move on L$line T$time ${(startFile+'a'.toInt()).toChar()}$startRank: Destination piece at ${(endFile+'a'.toInt()).toChar()}$endRank does not belong to opponent")
        }
        else if (end != null) println("WARN: Illegal move on L$line T$time ${(startFile+'a'.toInt()).toChar()}$startRank: Destination ${(endFile+'a'.toInt()).toChar()}$endRank is not empty")
        pieces[endRank][endFile] = pieces[startRank][startFile]
        pieces[startRank][startFile] = null
        
        if (progressTurn) {
            if (ply) time++
            ply = !ply
            moveStart = Pair(startFile, startRank)
            moveEnd = Pair(endFile, endRank)
            lock()
        }
    }
    
    fun moveFromBoard(file: Int, rank: Int, type: Piece, target: BoardTarget) {
        if (locked) throw IllegalStateException("Illegal move on L$line T$time ${(file+'a'.toInt()).toChar()}$rank: Board locked")
        
        val p = pieces[rank][file] ?: throw IllegalArgumentException("Illegal move on L$line T$time ${(file+'a'.toInt()).toChar()}$rank: No piece to move")
        if (p.first != type) throw IllegalArgumentException("Illegal move on L$line T$time ${(file+'a'.toInt()).toChar()}$rank: Piece is incorrect type")
        if (p.second != ply) throw IllegalArgumentException("Illegal move on L$line T$time ${(file+'a'.toInt()).toChar()}$rank: Piece belongs to opponent")
        pieces[rank][file] = null
        if (ply) time++
        ply = !ply
        moveStart = Pair(file, rank)
        moveTravelData = target
        lock()
    }
    
    fun moveToBoard(file: Int, rank: Int, type: Piece, capture: Boolean) {
        if (locked) throw IllegalStateException("Illegal move on L$line T$time ${(file+'a'.toInt()).toChar()}$rank: Board locked")
        
        val p = pieces[rank][file]
        if (capture) {
            if (p == null) println("WARN: Illegal move on L$line T$time ${(file+'a'.toInt()).toChar()}$rank: Destination is empty")
            else if (p.second == ply) println("WARN: Illegal move on L$line T$time ${(file+'a'.toInt()).toChar()}$rank: Destination piece does not belong to opponent")
        }
        else {
            if (p != null) println("WARN: Illegal move on L$line T$time ${(file+'a'.toInt()).toChar()}$rank: Destination is not empty")
        }
        pieces[rank][file] = Pair(type, ply)
        if (ply) time++
        ply = !ply
        moveEnd = Pair(file, rank)
        lock()
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as Board
        
        if (time != other.time) return false
        if (line != other.line) return false
        if (ply != other.ply) return false
        if (pieces.size != other.pieces.size) return false
        for (a in 0 until pieces.size) {
            if (pieces[a].size != other.pieces[a].size) return false
            for (b in 0 until pieces[a].size) {
                if (pieces[a][b] != other.pieces[a][b]) return false
            }
        }
        
        return true
    }
    
    override fun hashCode(): Int {
        // should be unique in any reasonable scenario
        var result = time
        result = 31 * result + line
        return result
    }
    
    fun clone(keepMove: Boolean = false): Board {
        val b = Board(time, line, ply, pieces.map { outer ->
            outer.map { it -> if (it == null) null else Pair(it.first, it.second) }.toTypedArray()
        }.toTypedArray())
        if (keepMove) {
            b.moveStart = moveStart
            b.moveEnd = moveEnd
            b.moveTravelData = moveTravelData
        }
        return b
    }
}