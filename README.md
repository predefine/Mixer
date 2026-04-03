# Mixer
lightweight java bytecode injector
inspired by `Mixin` and `Xposed`

## How to use
compile `impl` and `example`(aka `recipebook`) project from that repository and run 
java with arguments like this:
```
-javaagent:<path to impl>/build/libs/impl-1.0-SNAPSHOT-all.jar=<path to recipebook>/build/libs/<recipebook>-1.0-SNAPSHOT-all.jar -jar <path to .jar>
```