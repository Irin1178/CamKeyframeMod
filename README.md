# CamKey

CamKey is a lightweight cinematic camera keyframe mod for **Minecraft 1.21.1 / NeoForge 21.1.248**.

It is intended as a simple production tool for setting up repeatable camera moves during a recording session.

A user captures a series of camera poses into a named sequence and then plays that sequence back over a configurable duration:

```text
/camkey add intro
/camkey play intro 5

```

During playback, the camera moves smoothly between the saved positions and rotations using per-frame interpolation and smoothstep easing.

## Requirements

- Java 21
- Minecraft 1.21.1
- NeoForge 21.1.248

The repository includes the Gradle wrapper and should build without installing Gradle separately.

## Build

From the repository root:

### Windows

```powershell
.\gradlew.bat build

```

### macOS / Linux

```bash
./gradlew build

```

The built mod JAR will be placed under:

```text
build/libs/

```

## Run in Development

To launch the Minecraft development client:

### Windows

```powershell
.\gradlew.bat runClient

```

### macOS / Linux

```bash
./gradlew runClient

```

Create or open a single-player world and use the `/camkey` commands from the in-game command console.

No cheats or elevated command permissions are required for CamKey commands.

## Usage

### Add a Keyframe

Move the player to the position you want and aim the camera in the desired direction.

Then run:

```text
/camkey add <sequenceName>

```

Example:

```text
/camkey add intro

```

Each call appends the current position and camera rotation to that named sequence.

Repeat the command from additional positions to build the camera path.

### Play a Sequence

```text
/camkey play <sequenceName> <durationSeconds>

```

Example:

```text
/camkey play intro 5

```

This plays the entire `intro` sequence over five seconds.

Fractional durations are supported:

```text
/camkey play intro 2.5

```

A sequence must contain at least two keyframes before it can be played.

## Persistence

Camera sequences are stored with the Minecraft world using Minecraft's `SavedData` system.

The data is written to the world save rather than a global configuration file, so:

- sequences survive closing and reopening the world;
- two different worlds may contain sequences with the same name without sharing data;
- camera data follows the normal Minecraft world-save lifecycle.

The saved data contains:

- sequence name;
- sequence dimension;
- ordered keyframes;
- X, Y, and Z position;
- yaw and pitch.

Sequences are currently limited to one dimension. Attempting to add to or play a sequence from the wrong dimension produces an error rather than silently using the same coordinates in another dimension.

## Architecture

The implementation is deliberately small, but the major responsibilities are separated so the system can grow without concentrating everything into the command handler.

### `CameraKeyframe`

Represents a captured camera pose:

- position;
- yaw;
- pitch;
- interpolation and persistence helpers.

### `CameraSequence`

Represents a named, ordered collection of keyframes and the Minecraft dimension where the sequence was created.

### `CameraCapture`

Defines how the current player/camera pose is converted into a keyframe.

Keeping capture separate means command code does not need to know which Minecraft player fields make up a cinematic pose.

### `CameraSequenceSavedData`

Owns world persistence using Minecraft's `SavedData` system.

Command and playback code do not directly manage NBT serialization.

### `CamKeyCommands`

Registers the Brigadier `/camkey` command tree and handles user-facing validation and feedback.

### `CameraClientPlayback`

Owns active cinematic playback state and calculates the interpolated camera pose for the current rendered frame.

A snapshot of the sequence is taken when playback begins so later modifications cannot change an in-progress camera move.

### `CameraMixin`

Provides one narrowly scoped client-side camera hook.

Minecraft 1.21.1 exposes camera rotation through NeoForge events, but does not expose a public event that can override rendered camera position after vanilla camera setup. The mixin runs at the end of `Camera.setup` and applies the current CamKey pose only while playback is active.

The physical player is never moved during cinematic playback.

## Interpolation

The requested duration represents the **total duration of the entire sequence**, not the duration of each segment.

For example, a sequence containing three keyframes:

```text
A -> B -> C

```

played over:

```text
10 seconds

```

allocates approximately:

```text
A -> B : 5 seconds
B -> C : 5 seconds

```

Position is linearly interpolated between keyframes.

Yaw and pitch use shortest-path rotation interpolation so a transition such as `170° -> -170°` does not make the camera rotate almost a full circle.

A small smoothstep curve is applied to each segment's interpolation value:

```text
t * t * (3 - 2 * t)

```

This adds a natural ease-in/ease-out while keeping the implementation intentionally simple.

## Why Playback Is Client-Side

The first implementation used server-authoritative `ServerPlayer.teleportTo()` calls each game tick.

That approach used standard Minecraft APIs, but runtime testing exposed three problems:

1. movement was visibly stepped at Minecraft's 20 Hz game tick rate;
2. player camera rotation did not behave reliably;
3. moving the physical player along the cinematic path introduced collision and suffocation problems.

For a recording tool, those behaviors were not acceptable.

The final implementation leaves the player stationary and interpolates the rendered camera every frame instead.

This provides substantially smoother movement and avoids changing gameplay state merely to position the recording camera.

## Error Handling

CamKey fails without starting playback when:

- a requested sequence does not exist;
- a sequence contains fewer than two keyframes;
- the player is in a different dimension from the sequence;
- the command cannot start a local client playback session.

Starting another sequence while playback is active replaces the existing playback session cleanly.

Normal camera and player controls return immediately when playback ends.

## Known Limitations

This assessment implementation intentionally focuses on the core cinematic camera workflow.

Current limitations include:

- single-player/local playback only;
- no multiplayer synchronization;
- sequences cannot cross Minecraft dimensions;
- keyframes use equal-duration path segments;
- no command for removing or reordering keyframes;
- no graphical timeline/editor;
- no explicit pause, stop, or seek controls;
- no configurable interpolation modes;
- camera FOV is not captured.

These were intentionally left out to keep the implementation focused on the requested 3–4 hour assessment scope.

## If I Had Another Week

The first improvements I would explore would be:

- `/camkey list`, `/camkey stop`, and editing/removal commands;
- per-keyframe timing rather than equal segment duration;
- configurable easing/interpolation strategies;
- FOV keyframes;
- a simple production-facing sequence editor;
- optional path visualization while authoring;
- more explicit client/server synchronization if multiplayer production workflows were required.

I would keep the existing capture, persistence, command, and playback boundaries so those features could be added without rewriting the core system.

## AI-Assisted Development

I used **Cursor** as an AI pair-programming tool during the assessment.

I used it primarily for:

- exploring unfamiliar NeoForge 1.21.1 APIs;
- checking version-specific command, event, `SavedData`, and camera behavior;
- generating implementation boilerplate after architectural decisions were made;
- stress-testing the proposed architecture before implementation;
- iterating on compile errors and API mismatches.

I made the scope and architectural decisions myself, tested each major step in-game, and reviewed runtime behavior rather than treating a successful build as proof that the implementation was correct.

### Example of an AI Recommendation I Rejected

The initial AI-assisted playback design recommended interpolating the physical `ServerPlayer` position with `teleportTo()` every server tick.

It was attractive because it used supported public Minecraft APIs and avoided modifying the client camera.

The implementation compiled and technically moved through the saved keyframes, but runtime testing showed that it was the wrong solution for the actual production use case:

- camera movement visibly stepped at 20 Hz;
- rotation did not behave as expected;
- the physical player moved through terrain and could take damage.

I stopped development on that approach rather than trying to patch around those symptoms.

I then used Cursor to inspect the Minecraft 1.21.1 camera implementation and identify why the behavior occurred. That investigation showed that vanilla camera position is calculated inside `Camera.setup` each rendered frame and that NeoForge exposes rotation events but no equivalent post-setup position override.

The final solution uses a small client-side mixin at the end of `Camera.setup` to apply the interpolated cinematic pose while leaving the physical player stationary.

This was a useful example of where AI accelerated unfamiliar API research, but runtime testing and engineering judgment were still necessary to determine whether its proposed solution actually met the product requirement.

## Scope

The goal of this project is intentionally narrow:

**Capture camera keyframes, persist them with the world, and play them back as a smooth cinematic camera sequence.**

Additional production-tool features were intentionally deferred rather than expanding the assessment beyond that core workflow.