# Risk - World Domination Game

### Fully-featured java server with web-client implementation

- Host a game.
- Connect to a game over a network.
- Play as an A.I.


### Get Started
1. Clone this Repository or Download as a zip.
2. Run release/v1/Risk.jar either by double clicking on file or running java -jar Risk.jar
3. The java game engine will start and automatically open the game join/host screen in your web browser (shown below):
![Image of Join/Host Screen](https://github.com/AlexWilton/Risk-World-Domination-Game/raw/master/images/Host-Join-Screen.jpg)

### Game Play
- After game setup and all players have joined, game play starts according to the standard rules of Risk.
- Players to take it in turns to make their move. Below is a screenshot of a player about to make an attack:
![Image of Game Play Screen](https://github.com/AlexWilton/Risk-World-Domination-Game/raw/master/images/Game-Play.jpg)

- Responsive design and networking enable the game to be access and joinned from any device on the network with a web browser:
![Image of Playing on Tablet](https://github.com/AlexWilton/Risk-World-Domination-Game/raw/master/images/Tablet.jpg)

### Description
Our task was to implement a ”peer-to-peer world domination game”. We has decided very quickly in favour of the famous board game Risk. We have chosen a version of Risk that is focused on World Domination, therefore the objectives were completely removed from the game except for one: the player who manages to beat all other players is going to be the winner. We also decided that the P2P structure will rely on a known, trusted host implementation and that there will be weekly meetings of groups.

Our implementation aims to provide all the functionality specified in the protocol in Java and JavaScript, using an MVC (Model/View/Controller) pattern. The game engine is built in Java, that allowed for easy unit testing and object orientation, The game engine implements and checks the logic of the game while keeping everything in the network communication separate from here.
The back-end of the game relies on Java Sockets for network communication. Both the server and the client are multi-threaded and update the game engine in the background while still keeping a constant flow of network protocol messages. These messages are implemented via separate classes as well to increase decoupling of components in the system.

The front-end appears and runs inside a web browser, in a scalable window, heavily relying on JavaScript and bootstrap. It queries the game state from time to time and sends messages to the game engine. This completes the circle of MVC architecture.

### Team Members
[Alex Wilton](https://github.com/AlexWilton)  
[Bence Szabo](https://github.com/bentlor)  
[Ryo Yanagida](https://github.com/yryo617)  
[Patrick Opgenoorth](https://www.linkedin.com/in/patrickopgenoorth/)

### Supervisor
[Professor Steve Linton](https://www.cs.st-andrews.ac.uk/directory/person?id=sal)

### Grade and Reception
Project received a First-class honours classifcation.

### Related Documents
- [(Comphrensive) Final Report](https://github.com/AlexWilton/Risk-World-Domination-Game/blob/master/documents/Project%20Report.pdf)
- [Initial Design and Requirements Document](https://github.com/AlexWilton/Risk-World-Domination-Game/blob/master/documents/Requirements%20Specification.pdf) 

###
