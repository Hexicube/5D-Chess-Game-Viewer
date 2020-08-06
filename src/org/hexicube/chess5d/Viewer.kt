package org.hexicube.chess5d

import java.awt.*
import javax.imageio.ImageIO
import javax.swing.*
import java.awt.Graphics2D

fun sizedButton(text: String, width: Int, height: Int): JButton {
    val b = JButton(text)
    b.size = Dimension(width, height)
    b.minimumSize = Dimension(width, height)
    b.maximumSize = Dimension(width, height)
    b.preferredSize = Dimension(width, height)
    b.margin = Insets(0,0,0,0)
    // set styling?
    return b
}

private const val TILE_SIZE = 32
private const val BOARD_SIZE = TILE_SIZE * 10

private val BORDER_BG = Color.DARK_GRAY
private val DARK_BG = Color.ORANGE.darker().darker()
private val LIGHT_BG = Color.ORANGE.darker()

private val GREEN_BG = Color(0, 200, 0)
private val GREENDARK_BG = Color(0, 150, 0)
private val BLUE_BG = Color(150, 150, 255)
private val BLUEDARK_BG = Color(100, 100, 255)

private val PIECE_IMG = ImageIO.read(Board::class.java.getResource("/pieces.png"))

fun renderBoard(renderArea: JPanel, board: Board) {
    val boardContainer = JPanel()
    boardContainer.layout = null
    val boardPanel = JPanel()
    boardPanel.size = Dimension(BOARD_SIZE, BOARD_SIZE)
    boardPanel.minimumSize = Dimension(BOARD_SIZE, BOARD_SIZE)
    boardPanel.maximumSize = Dimension(BOARD_SIZE, BOARD_SIZE)
    boardPanel.preferredSize = Dimension(BOARD_SIZE, BOARD_SIZE)
    boardPanel.layout = GridLayout(10, 10)
    for (a in 0 until 10) boardPanel.add(renderTile(null, -1, -1, board))
    for (y in 0 until 8) {
        boardPanel.add(renderTile(null, -1, -1, board))
        for (x in 0 until 8) boardPanel.add(renderTile(board.getPiece(x, 7-y), x, 7-y, board))
        boardPanel.add(renderTile(null, -1, -1, board))
    }
    for (a in 0 until 10) boardPanel.add(renderTile(null, -1, -1, board))
    
    val label = JLabel("L${board.line} T${board.time - (if(board.ply) 0 else 1)} ${if (!board.ply) "Black" else "White"}")
    label.setBounds(TILE_SIZE/2, TILE_SIZE/2 - 10, TILE_SIZE*9, 20)
    label.foreground = Color.WHITE
    boardContainer.add(label)
    
    with (board.moveTravelData) {
        if (this != null) {
            val subtext = if (board.moveEnd == null) "Piece sent to L${this.line} T${this.time} ${(this.x+'a'.toInt()).toChar()}${this.y+1}"
            else "Piece arrived from L${this.line} T${this.time} ${(this.x+'a'.toInt()).toChar()}${this.y+1}"
            val label2 = JLabel(subtext)
            label2.setBounds(TILE_SIZE / 2, TILE_SIZE * 9 + TILE_SIZE / 2 - 10, TILE_SIZE * 9, 20)
            label2.foreground = Color.WHITE
            boardContainer.add(label2)
        }
    }
    
    boardPanel.setBounds(0, 0, BOARD_SIZE, BOARD_SIZE)
    boardContainer.add(boardPanel)
    
    boardContainer.size = Dimension(BOARD_SIZE, BOARD_SIZE)
    boardContainer.minimumSize = Dimension(BOARD_SIZE, BOARD_SIZE)
    boardContainer.maximumSize = Dimension(BOARD_SIZE, BOARD_SIZE)
    boardContainer.preferredSize = Dimension(BOARD_SIZE, BOARD_SIZE)
    renderArea.add(boardContainer)
}
fun renderTile(type: Pair<Piece,Boolean>?, x: Int, y: Int, board: Board): JPanel {
    val p = object : JPanel() {
        override fun paint(g: Graphics) {
            super.paint(g)
            if (type != null) g.drawImage(PIECE_IMG, 0, 0, 32, 32, type.first.imgX * 32, if (type.second) 32 else 0, type.first.imgX * 32 + 32, if (type.second) 64 else 32, null)
        }
    }
    p.layout = null
    p.size = Dimension(TILE_SIZE, TILE_SIZE)
    p.minimumSize = Dimension(TILE_SIZE, TILE_SIZE)
    p.maximumSize = Dimension(TILE_SIZE, TILE_SIZE)
    p.preferredSize = Dimension(TILE_SIZE, TILE_SIZE)
    p.insets.set(0,0,0,0)
    val sq = Pair(x,y)
    p.background = when {
        x == -1 -> BORDER_BG
        (x+y)%2 == 0 -> DARK_BG
        else -> LIGHT_BG
    }
    if (sq == board.moveStart || sq == board.moveEnd) {
        if (board.moveStart != null && board.moveEnd != null) {
            if (p.background == DARK_BG) p.background = GREENDARK_BG
            else p.background = GREEN_BG
        }
        else {
            if (p.background == DARK_BG) p.background = BLUEDARK_BG
            else p.background = BLUE_BG
        }
    }
    return p
}

fun main(args: Array<String>) {
    /*
    Game.states = Array<GameState>
    GameState.timelines = HashMap<Int, ArrayList<Board>>
    Board.time = Turn indicator as represented in-game
    Board.line = Line indicator as represented in-game
    Board.ply = true if black to play, false if white to play
    Board.pieces = Array<Array<Pair<Piece, Boolean>?>> = A 2D array of all pieces in the format (Type, IsBlack), nullable for blank spots
    */
    
    // hexicube vs someone
    /*val theGame = Game("""
        1. c4 .. e6
        2. Nf3 .. Nc6
        3. g3 .. Nb4
        4. Qb3 .. h5
        5. Bg2 .. Qe7
        6. O-O .. Na6
        7. d4 .. Nf6
        8. Ne5 .. Ne4
        9. Bxe4 .. h4
        10. g4 .. Qg5
        11. Bxg5 .. Bd6
        12. f4 .. L0d6 Bx T10d4
        13. Nf3; - .. Be3
        14. Bxe3; - .. Rh6
        15. L0e5 Nx L-1e7 .. Bxe7; O-O
        16. Bxh6; f5 .. Bf8; exf
        17. Bxb7; Bxf5 .. Bd6; Re8
        18. Bxc8; Bd8  .. Rxc8; Rxd8
        19. Bxg7; Qe3
    """.trimIndent())*/
    
    // lexi vs computersmoke (tourney final)
    /*val theGame = Game("""
        1. e4 .. c6
        2. c3 .. Nf6
        3. e5 .. Ne4
        4. Qf3 .. Ng5
        5. Qh5 .. e6
        6. d4 .. Qc7
        7. Bxg5 .. L0c7 Qx T5e5
        8. Qe2; - .. Qdc7
        9. d4; - .. Qxe2
        10. Bxe2; Qh4 .. Ne4; d5
        11. Be3; Qf4 .. d5; Nd7
        12. Nf3; Bd8 .. L-1c7 Qx L0d8
        13. L0f4 Qx T8f7
    """.trimIndent())*/
    
    var theGame: Game?
    
    do {
        val input = JTextArea()
        val pane = JScrollPane(input)
        pane.preferredSize = Dimension(150, 400)
        val opt = JOptionPane.showConfirmDialog(null, pane, "Input transcript", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        if (opt != JOptionPane.OK_OPTION) System.exit(0)
    
        theGame = Game(input.text)
        if (theGame.error != null) {
            JOptionPane.showMessageDialog(null, "Error on move: ${theGame.erroringMove}\n${theGame.error!!.message}\n${theGame.error!!.stackTrace.joinToString("\n")}")
            theGame = null
        }
    }
    while (theGame == null)
    
    val gameStateLineArea = JPanel()
    gameStateLineArea.layout = BoxLayout(gameStateLineArea, BoxLayout.X_AXIS)
    val gameStateLine = JScrollPane(gameStateLineArea)
    gameStateLine.preferredSize = Dimension(999999, 42)
    gameStateLine.maximumSize = Dimension(999999, 42)
    
    val listOfLines = ArrayList<Board>()
    var smallestLine = 0
    
    val gameContentArea = object : JPanel() {
        override fun paint(g: Graphics?) {
            super.paint(g)
            if (g == null) return
            val g2 = g as Graphics2D
            g2.color = Color(150, 0, 255, 200)
            g2.stroke = BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f)
            listOfLines.forEach {
                val startBX = it.time * 2 - if (it.ply) 1 else 2
                val startTX = it.moveStart!!.first
                val startBY = it.line - smallestLine
                val startTY = it.moveStart!!.second
                
                // println("$startBX-$startTX : $startBY-$startTY")
                
                val srcX = startBX * (BOARD_SIZE + 10) + (startTX) * TILE_SIZE + TILE_SIZE + TILE_SIZE/2 + 10
                val srcY = startBY * (BOARD_SIZE + 10) + (7-startTY) * TILE_SIZE + TILE_SIZE + TILE_SIZE/2
                
                val endBX = it.moveTravelData!!.time * 2 - if (it.ply) 1 else 0
                val endTX = it.moveTravelData!!.x
                val endBY = it.moveTravelData!!.line - smallestLine
                val endTY = it.moveTravelData!!.y
    
                // println("$endBX-$endTX : $endBY-$endTY")
                
                val dstX = endBX * (BOARD_SIZE + 10) + (endTX) * TILE_SIZE + TILE_SIZE + TILE_SIZE/2 + 10
                val dstY = endBY * (BOARD_SIZE + 10) + (7-endTY) * TILE_SIZE + TILE_SIZE + TILE_SIZE/2
                
                // println("$srcX:$srcY -> $dstX:$dstY")
                
                g.drawLine(srcX, srcY, dstX, dstY)
            }
        }
    }
    gameContentArea.layout = BoxLayout(gameContentArea, BoxLayout.Y_AXIS)
    val gamePane = JScrollPane(gameContentArea)
    
    var curTurn = 1
    var curPly = false
    theGame.states.forEach { state ->
        val stateInfo = Triple(curTurn, curPly, state)
        val btn = sizedButton("$curTurn${if (curPly) "B" else "W"}.", 50, 24)
        btn.addActionListener {
            gameStateLineArea.components.forEach {
                it.background = null
                it.isEnabled = true
            }
            btn.background = GREENDARK_BG
            btn.isEnabled = false
            
            gameContentArea.removeAll()
            listOfLines.clear()
            smallestLine = 0
            
            stateInfo.third.timelines.toSortedMap().values.forEach {
                val linePane = JPanel()
                linePane.layout = BoxLayout(linePane, BoxLayout.X_AXIS)
                for (a in 2 until (it[0].time * 2 + if (it[0].ply) 1 else 0)) linePane.add(Box.createRigidArea(Dimension(BOARD_SIZE+10, BOARD_SIZE)))
                linePane.add(Box.createRigidArea(Dimension(10,10)))
                for (board in it) {
                    smallestLine = Math.min(smallestLine, board.line)
                    renderBoard(linePane, board)
                    linePane.add(Box.createRigidArea(Dimension(10,10)))
                    if (board.moveTravelData != null && board.moveStart != null) listOfLines.add(board)
                }
                linePane.add(Box.createHorizontalGlue())
                gameContentArea.add(linePane)
                gameContentArea.add(Box.createVerticalStrut(10))
            }
            
            gameContentArea.add(Box.createVerticalGlue())
            gamePane.validate()
            gamePane.horizontalScrollBar.value = Int.MAX_VALUE
            gamePane.repaint()
        }
        if (curTurn == 1 && !curPly) btn.actionListeners.last().actionPerformed(null)
        if (curPly) curTurn++
        curPly = !curPly
        gameStateLineArea.add(btn)
    }
    
    val frame = JFrame("5D Chess Viewer")
    frame.contentPane.layout = BoxLayout(frame.contentPane, BoxLayout.Y_AXIS)
    frame.add(gameStateLine)
    frame.add(gamePane)
    
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
    frame.size = Dimension(800, 600)
}