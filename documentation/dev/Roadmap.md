# Roadmap

## Version 2.0

### Features

- New engine based on Protobuf communication (makes for cleaner multi-agent implementations, and image based scenarios).
- Command line interface

### Scenarios

- Multi-armed Bandits
- Coin Game
- High/low Cards
- Mazes
- Mine Hunter
- Twisty Puzzles
  - Cubes 2x2x2, 3x3x3, 4x4x4, 5x5x5, 6x6x6, 7x7x7, 8x8x8, 10x10x10
  - Cuboid 2x2x3, 2x2x4, 2x2x5, 2x2x6, 3x3x2, 3x3x4, 3x3x5

Note: At this point we have feature parity between v1 and v2 codebases and v1 will no longer be supported.

## Scenario Roadmap

- Car Driving – Advanced car driving simulation including navigation and obstacle avoidance.
- Car Racing – Top down racing game where the AI must drive round a track with simple physics.
- Nethack clone
- Platform arcade game
- Cart-Pole – Traditional physics simulation, balance a pole, keeping it upright for as long as possible.
- Snake
- Sokoban Puzzle
- “Space Invaders” – Clone of an 80’s style arcade game.
- Wargame 
- Zebra Puzzle – A puzzle involving understanding the relationships between elements. Normally solved by constraints based programming, but could be adapted for learning algorithms. Zebra Puzzle page on Wikipedia. Note: This was originally intended to be in the v1.2 release but has since been dropped.

## Wish list

The following would make great scenarios, but involve intellectual property / licencing issues; perhaps once we’re established we could approach the owners, or we focus on a key game mechanic.

- Dracula’s dance (not sure of the name) – this is a print and play game that was previously available as part of a kickstarter campaign.
- Scotland Yard – board game where multiple agents have to work together to trap the ‘bad guy’ as they race across London
- Dominion – Deck building game created by Donald X. Vaccarino and distributed by Rio Grand. Would make for an interesting environment but potentially hard to code due to the volume of rules to be implemented. Also, a lot of graphics assets to licence / replace.
- Hanabi – This game has been identified by several groups as a good target for AI research. Several online versions exist so it may be possible to implement the game however the publishers would still need to be approached.