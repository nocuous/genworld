GenWorld
========

Version 1.1 jar for Forge 1.7.2 available at: http://adf.ly/lPbmv

Minecraft server world pre-generator created for Forge 1.7.2

Commands

/genworld    - displays help including a list of world names

/genworld SpawnSquare <world> <size>  - pregenerates chunks in a square of blocks size x size around the spawn point for the world

/genworld box <min x> <min y> <max x> <max y> - pregenerates chunks in a rectangle within the given block coordinates


/genworld SpawnCircle <radius> - pregenerates chunks in a circle around the world spawn using radius in block coordinates

/genworld circle <x> <y> <radius> - pregenerates chunks in a circle around point x,y using radius in block coordinates


Upcoming Changes:
Switch to operating during tick instead of operating on command processing
Test usage of ForgeChunkLoading instead of pulling from ServerChunkProvider
