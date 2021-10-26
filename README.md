# P0 Sudoku
Moiya Josephs

## Description
A CLI Sudoku game where user gets a board generated with certain hint values.
Uses SQL to access database with saved boards and allows users to save their progress if they make 3 mistakes.
Shows if user wins or loses at the end of the game and if no zero values are in the board.

## Technologies
| Programming Lang | Version|
| ---------------- | -------|
| Scala| 2.11  |
|Java | 8.0.25|

## How to Play
1) Command asks user if they would like the rules explained to them. They answer with yes or no.
2) Next the user is asked to load a previous game, if available.
3) If available, previous game is loaded, else new game is generated based on the difficulty level they enter.
   1) Difficulty level of 1 generates a 4x4 board
   2) Difficulty level of 2 generates a 6x6 board
   3) Difficulty level of 3 generates a 9x9 board
4) Each turn, user is prompted to enter the row/col combination to enter and the value. Row/Col starts from 0 index.
   1) If the user tries to override a hint value, warning is issued but mistake is not recorded
   2) If the user enters a duplicate value, mistake is recorded. Three strikes, user loses game but gets the option to save
5) End of game, when either mistakes =3 or 0 is no longer in the board. Program checks user input against actual board and returns if they win or not


