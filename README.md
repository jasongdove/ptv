# PTV

PTV is a test to create custom live channels. The channels will use content sourced from places like [Plex](https://www.plex.tv/) or [Jellyfin](https://jellyfin.org/), and the channels will be made available in the [Channels](https://getchannels.com) apps.

This project is inspired by [pseudotv-plex](https://github.com/DEFENDORe/pseudotv).

## Providing content to Channels apps

The Channels apps support [HDHomeRun](https://www.silicondust.com/) devices, so this project will emulate an HDHomeRun.

When adding the emulated tuner to Channels, it is important to specify the port number `5004`. This signals to the Channels app that the tuner is not a real HDHomeRun, which will disable some hardware-specific features and protocols that aren't needed.

The default port of `5004` should also not be changed because the Channels apps ignore the stream URL properties of the channels returned by the `lineup.json` endpoint. Instead, Channels will always connect to port `5004` and will always use the route `auto/v:channel` (like `/auto/v2` or `/auto/v58.1`).