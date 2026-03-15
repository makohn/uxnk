# uxnk

A Kotlin implementation of the [Uxn][uxn] virtual machine and the [Varvara][varvara] computer system.

## Building

You can build an executable JAR using the following command:

```shell
./gradlew jar
```

## Usage

To launch a rom in the `uxnk` emulator, use the following command:

```shell
java -jar uxnk.jar [ROM] [ARGS]...
```

## Emulator Controls

| Key            | Description                      |
|----------------|----------------------------------|
| <kbd>F1</kbd>  | Toggle between three zoom levels |
| <kbd>F2</kdb>  | Print stack contents to console  |
| <kbd>F3</kbd>  | Exit the program with code `127` |
| <kbd>F11</kbd> | Toggle fullscreen mode           |
| <kbd>F12</kbd> | Toggle borderless window mode    |

[uxn]: https://wiki.xxiivv.com/site/uxn.html
[varvara]: https://wiki.xxiivv.com/site/varvara.html