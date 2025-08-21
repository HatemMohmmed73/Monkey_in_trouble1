# Monkey In Trouble

A small LibGDX desktop game where you guide a monkey through tile-based rooms, collect items, and avoid hazards like saws and fire. Some rooms include simple puzzles (e.g., buttons, boxes, traps) that you need to interact with to progress.

## Features
- LibGDX + LWJGL3 desktop game
- Tile-based maps with hazards (saws, fire) and interactables (boxes, buttons, traps)
- Lightweight: runs with Java 11+ via Gradle wrapper
- Docker support for quick, dependency-free runs

---

## Run the Game (Native)

Requirements:
- Java 11+ (OpenJDK 11 recommended)
- Linux/macOS/Windows

Commands (from the project root):
```bash
# Make sure the Gradle wrapper is executable (Linux/macOS)
chmod +x gradlew

# Run the desktop game
./gradlew desktop:run
```
Notes:
- macOS-only JVM flag `-XstartOnFirstThread` is applied automatically by the build script.
- Assets are wired to the desktop run task; no extra setup needed.

---

## Run the Game (Docker)

You can run the game inside a container without installing Java or Gradle. The container is configured to forward the game window to your desktop via X11.

Build the image:
```bash
docker build -t monkey-in-trouble .
```
Allow Docker to talk to your X server (Linux/X11):
```bash
xhost +local:docker
```
Run the game:
```bash
docker run -it --rm \
  -e DISPLAY=$DISPLAY \
  -v /tmp/.X11-unix:/tmp/.X11-unix \
  monkey-in-trouble
```
After you finish (optional security step):
```bash
xhost -local:docker
```
Notes:
- The container sets its working directory to `/app/assets` so the game can find resources.
- This guide assumes an X11 session. On Wayland, you may need XWayland or alternatives (e.g., `--device /dev/dri` and different display sharing solutions).

Troubleshooting X11:
```bash
# Verify X11 forwarding by running xeyes (should show a small window)
docker run -it --rm -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix x11-apps xeyes

# Check your session type on the host
echo $XDG_SESSION_TYPE   # should print: x11
```

---

## Common Issues
- JVM error `Unrecognized option: -XstartOnFirstThread` on Linux/Windows:
  - Fixed in this repo: the flag is only applied on macOS.
- No game window when running in Docker:
  - Ensure you ran `xhost +local:docker`.
  - Confirm you are on X11 (`echo $XDG_SESSION_TYPE` → `x11`).
  - Test with `x11-apps xeyes` as shown above.

---

## Project Layout
```
assets/                # Shared game assets (images, fonts, UI skins)
core/                  # Core game logic (entities, map, screens, states, UI)
desktop/               # Desktop launcher + platform configuration
lwjgl3/                # Alternative LWJGL3 launcher (not used by default)
build.gradle           # Root Gradle build
settings.gradle        # Declares modules (core, desktop)
```

---

## Developer Notes
- Run tasks:
  - `./gradlew desktop:run` — run the game
  - `./gradlew build` — build all modules
  - `./gradlew :desktop:tasks` — list desktop tasks
- Java toolchain: Java 11
- Assets are included on the desktop classpath via `desktop/build.gradle` and the run task sets the working directory appropriately.

---

## License
Provide your license here if required.
