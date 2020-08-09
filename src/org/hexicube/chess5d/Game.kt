package org.hexicube.chess5d

import java.util.ArrayList

data class Game(val input: String) {
    val states: Array<GameState>
    var erroringMove = ""
    var error: Exception? = null
    var lastSuccessful = ""
    init {
        var workingState = GameState.startPosition()
        val theStates = ArrayList<GameState>()
        states = try {
            var currentTurn = 0
            val dataStream = input.split(' ', '\n', '\r').filterNot { it.isBlank() }.toMutableList()
            var ply = true
            val partialMove = ArrayList<String>()
            val availLines = ArrayList<Int>()
            var success = ""
            println("Parsing moves...")
            while (true) {
                if (dataStream.isEmpty()) {
                    if (partialMove.isNotEmpty()) {
                        workingState.withMove(partialMove, ply, availLines)
                        success += partialMove.joinToString(" ") + ";"
                    }
                    println()
                    println()
                    theStates.add(workingState)
                    if (ply) println("Parsing complete, game ended on turn ${currentTurn + 1} (white)")
                    else println("Parsing complete, game ended on turn $currentTurn (black)")
                    break
                }
                if (dataStream[0].isBlank()) dataStream.removeAt(0)
                else when {
                    dataStream[0] == "${currentTurn + 1}." || dataStream[0] == "${currentTurn + 1}W." -> {
                        if (partialMove.isNotEmpty()) {
                            workingState.withMove(partialMove, ply, availLines)
                            success += partialMove.joinToString(" ") + ";"
                            partialMove.clear()
                        }
                        println()
                        if (availLines.isNotEmpty()) println("WARN: Illegal turn advance into turn ${currentTurn + 1}, the following lines need a move: ${availLines.joinToString()}")
                        // workingState.timelines[0]!!.last().print()
                        if (!ply) throw IllegalStateException("Illegal turn advance into turn ${currentTurn + 1}, only done one ply.")
                        if (currentTurn != 0) {
                            theStates.add(workingState)
                            workingState = workingState.clone()
                        }
                        currentTurn++
                        ply = false
                        dataStream.removeAt(0)
                        print("$currentTurn. ")
                        workingState.findAllLines(availLines, ply)
                        lastSuccessful = success
                        success = "${currentTurn}W. "
                    }
                    dataStream[0] == ".." || dataStream[0] == "${currentTurn}B." -> {
                        if (partialMove.isNotEmpty()) {
                            workingState.withMove(partialMove, ply, availLines)
                            success += partialMove.joinToString(" ") + ";"
                            partialMove.clear()
                        }
                        println()
                        if (availLines.isNotEmpty()) println("WARN: Illegal ply advance on turn ${currentTurn + 1}, the following lines need a move: ${availLines.joinToString()}")
                        // workingState.timelines[0]!!.last().print()
                        if (ply) throw IllegalStateException("Illegal ply advance on turn $currentTurn, was already advanced.")
                        theStates.add(workingState)
                        workingState = workingState.clone()
                        ply = true
                        dataStream.removeAt(0)
                        print("${currentTurn.toString().replace(Regex("."), " ")}. ")
                        workingState.findAllLines(availLines, ply)
                        lastSuccessful = success
                        success = "${currentTurn}B. "
                    }
                    else -> {
                        var str = dataStream.removeAt(0).trim()
                        val advance: Boolean
                        if (str.endsWith(";")) {
                            str = str.substring(0, str.length - 1).trim()
                            advance = true
                        } else advance = false
                        if (str.isNotBlank()) partialMove.add(str)
                        if (advance) {
                            workingState.withMove(partialMove, ply, availLines)
                            success += partialMove.joinToString(" ") + ";"
                            partialMove.clear()
                            print("; ")
                        }
                    }
                }
            }
            theStates.toTypedArray()
        }
        catch (e: Exception) {
            erroringMove = workingState.mostRecentMove
            error = e
            theStates.toTypedArray()
        }
    }
}