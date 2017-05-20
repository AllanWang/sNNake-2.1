# sNNake 2.1

[![Build Status](https://travis-ci.org/AllanWang/sNNake-2.1.svg?branch=master)](https://travis-ci.org/AllanWang/sNNake-2.1)

A multiplayer snake game with Neural Networks.

The following project consists of two main portions:

### Multiplayer Snake

Just like the original snake game, but with up to 2 players. You score extra points if your opponent hits your body, but you die if you hit theirs.

The player statuses (Human, NN, None) can be defined in the UI

The keybindings for snake players are as follow:

|||
|---|---|
Snake 1:    | WASD
Snake 2:    | arrow keys
|||

Game key bindings:

|||
|---|---|
Pause:      | space bar
Restart:    | ctrl + r
Close:      | ctrl + c
|||

### NeuralNet

A Neural Net implementation written from scratch, containing

* Matrix - a mutable implementation of matrices using 2D arrays, with various operations and helper functions
* NeuralNet - a matrix wrapper with forward and backward propagation, and customizable layer sizes, activators, and randomness
* NNGenetics - a neural net wrapper which keeps track of population fitness and breeding, storing outputs in the target resource folder

Combined, these two portions work to teach a NN how to play the game, without any training explicitly done by a player.

## Special Thanks To
* [@wollip](https://github.com/wollip) for his [Snake game implementation](https://github.com/wollip/snake)
    * Which helped me create [sNNake 2.0](https://github.com/AllanWang/sNNake-2.0)
* [@GregSkl](https://github.com/GregSkl) for his [SnakeNN implementation](https://github.com/GregSkl/SnakeNN)
    * Which acquainted me to Genetic Neural Networks
* [Welch Labs](https://www.youtube.com/user/Taylorns34) for his [Neural Networks Demystified Series](https://www.youtube.com/watch?v=bxe2T-V8XRs&list=PLiaHhY2iBX9hdHaRr6b7XevZtgZRa1PoU)
    * Which greatly simplified the concepts of Neural Networks
* [@edvin](https://github.com/edvin) for [TornadoFx](https://github.com/edvin/tornadofx)
    * Which greatly simplified the UI creation
