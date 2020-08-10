# 5D Chess Game Viewer

A tool for reviewing games of 5D chess based on transcripts.

Proper verification is incomplete, it is assumed games are legitimate and the only checks in place are ensuring that moves at least make sense.

## Example game 1: Hexicube (white, win) vs ??? (black, loss)

```
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
```

## Example game 2: SexyLexi (white, win) vs ComputerSmoke (black, loss)

```
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
```

## Example game 3: Hexicube (white, loss) vs Shaevor (black, win)

```
1. c4 .. e6
2. Nf3 .. Bc5
3. d4 .. Bb4
4. Bd2 .. Qe7
5. a3 .. Bxd2
6. Qxd2 .. d5
7. Ne5 .. Nc6
8. L0e5 Nx T7e7 .. -; Nxe7
9. Qb4 .. Nf6; Nbc6
10. g3; Qa4 .. L0c6 Nx L1c4
11. Qc3; e3 .. Ne4; L1c4 Nx T9a4
12. e3; -; - .. O-O
13W. L1a4 Qx L-1a4; Qf3
13B. Bd7; c5; O-O
14W. Nc3; dxc; Nc3
14B. L-1c6 Nx L0c4; Nf5
15W. L1d4 P L0d4; Qd1
15B. Rab8; e5; e5
16W. Ne5; Qh5; Nxd5
16B. b5; g6; Be6
17W. Qh5; Qxe5; Ne7
17B. g6; O-O; Nfxe7
18W. Qh6; Qxe7; Ng5
18B. L-1d7 Bx L0e7; Rfd8
19W. L-1h6 Q L0g7; Bb5
19B. L-1e7 Nx L0g7; a6
20W. c5; b3; Bxc6
20B. Rfe8; Nf5; bxc
21W. Nc6; Bg2; O-O
21B. Rbd8; L0e4 N L1e2
22W. Nxb5; L0e1 Kx L1e2
22B. a6; Nce3; Bc4
23W. Nxc7; fxe; Ke1
23B. Rb8; Nxe3; Nf5
24W. L-1c6 Nx T20c7; -; -
24B. -; Rc8
25W. -; -; Be2
25B. -; Rxc7
```
