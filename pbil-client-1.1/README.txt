Pbil Client 1.1 (Fabric, Minecraft 1.21.11)
===========================================

BAUEN (GitHub): ALLE Dateien dieses Ordners ins Repository hochladen, auch den versteckten Ordner .github.
build.gradle liegt zusaetzlich als build.gradle.txt dabei - der Workflow kopiert sie automatisch, falls build.gradle fehlt.
Unter "Actions" laeuft der Build, die Jar liegt bei "Artifacts" (pbil-client). Jar + Fabric API in den mods-Ordner.

BENUTZEN
  Rechts-Shift  Menue (goldenes Click-GUI, Mausrad scrollt, ">" = Einstellungen)
  O             loescht die gemerkten Spieler-Koordinaten (Tasten aenderbar unter Steuerung -> Pbil Client)
  Einstellungen: config/pbil_client.properties     Makros: config/pbil_macros.txt (Name=Text, mit / = Befehl)

MODULE
  Combat    KillAura, TriggerBot, CrystalAura, AutoTotem, Surround, Hitboxes, AimAssist, AutoArmor
  Misc      AutoRespawn, AutoEat, AntiAFK, AutoClicker, LegitAutoTool, CoordSnapper, WeatherNotifier, BlockNotifier
  Donut     Netherite Finder (Ancient Debris, Standard 20 Bloecke), Spawner Notifier, Player Detection
  Base      Chunk Scanner, Sus Chunk Finder
  Pbil Client  Watermark, ModuleList, Coordinates, ArmorHUD, PvPInfo, Speed (BPS), Radar, FPS Boost
  Movement  Sprint, Speed, Fly, Step, NoFall, AutoWalk, AutoJump, AirStuck, BoatFly, EntitySpeed, ElytraFly, Jesus, Sneak, Spider
  Macro     eigene Chat-Makros aus config/pbil_macros.txt
  Render    PlayerESP (3D-Hitbox, roter Chunk bis Bedrock, Koordinaten, Items ueber dem Kopf), MobESP, ItemESP, CrystalESP,
            StorageESP, SpawnerESP, BlockESP (bis Deepslate, Hoehe einstellbar), Tracers (ab Fadenkreuz), Nametags, Fullbright, Zoom,
            Glow ESP, Hole ESP, Low Health Warning, Trajectories, Pearl Trajectory, Spawner Nametag
  Visuals   Custom Crosshair, Custom Fov, Block Overlay, Target ESP, Hit Particles, Jump Circles, Particles, Target Spirit, Color Grading

NICHT DABEI (brauchen Eingriffe in den Minecraft-Renderer oder Server-Spezialwissen)
  Freecam/Freelook, Xray, Motion Blur, 3D Hand/Character, Custom Glint, Hand-Module, Swing Animation, Time Changer,
  Discord/Spotify, Auto Reconnect, Name Protect, Fast Use sowie die Donut-Wirtschafts-Macros (AH Sniper usw.).
