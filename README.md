# The Drawing Board
The Drawing Board is a game based on the popular guessing game 'Pictionary'.
The general goal is to guess words based on someone else's drawing. 
4 modes are to be developed :
## Solo/Practice 
Think of this as a light Microsoft Paint, where you can draw whatever you want and save it as an image file. Even better, it displays random words to practice with. The same words database is used on every mode, so you could theoretically learn all the words and become unbeatable :^)

## 2 Players (to be implemented) 

## Multiplayer
Main Mode (2+ players). Several players each take turns drawing and guessing. Each turn, the 'artist' (person drawing, chosen either sequentially or randomly) is given a word and starts drawing. The other players can see the artist drawing and must use the chat to try and guess the word behind the artist drawing (only hint is the number of letters). Whoever guesses right gets a point. The game ends when a player reaches the maximum score (set up at the beginning).
Details:
One player hosts the game and the others connect to it with his IP address (local in LAN, public address if not in LAN). If not in LAN, the host must configure port forwarding on his router (the specific port is shown in the application).

## The Board 
All the players share a same drawing and can draw whatever they want, then save it. A chat is also provided, since it's another multiplayer mode. 
Join someone's board using their IP address. As with before, port-forwarding must be enabled if you want people outside of a LAN to connect (port 4444).

# How to use
* Verify that Java 8+ is installed
* Download the repo as a .zip
* Launch 'The Drawing Board.jar' in the dist folder
